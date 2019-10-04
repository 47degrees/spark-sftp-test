package org.fortysevendeg.sparksftp

import java.net.URI

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.fortysevendeg.sparksftp.common.RegisterInKryo
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader

object ReadingSFTPHadoopApp extends IOApp {

  def setupConfig: IO[ReadingSFTPConfig] =
    ConfigLoader[IO]
      .loadConfig[ReadingSFTPConfig]

  def run(args: List[String]): IO[ExitCode] =
    for {
      config: ReadingSFTPConfig <- setupConfig
      defaultSparkConf: SparkConf = new SparkConf()
        .set("spark.serializer", config.spark.serializer)
        .set("spark.master", "local")
        .set(
          "spark.kryo.registrationRequired",
          config.spark.serializer.contains("KryoSerializer").toString
        )
        .set("fs.sftp.impl", "org.apache.hadoop.fs.sftpwithseek.SFTPFileSystem")
        .set("fs.sftp.impl.disable.cache", "true")
        .set("spark.sql.legacy.allowCreatingManagedTableUsingNonemptyLocation","true") //https://kb.azuredatabricks.net/jobs/spark-overwrite-cancel.html
        .registerKryoClasses(RegisterInKryo.classes.toArray)

      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

      sftpUser1 = sparkSession.sparkContext.getConf
        .getOption("spark.executorEnv.SFTP_USER")
        .getOrElse(config.sftp.sftpUser)
      sftpPass1 = sparkSession.sparkContext.getConf
        .getOption("spark.executorEnv.SFTP_PASS")
        .getOrElse(config.sftp.sftpPass)
      sftpHost1 = sparkSession.sparkContext.getConf
        .getOption("spark.executorEnv.SFTP_HOST")
        .getOrElse(config.sftp.sftpHost)
      sftpPath1 = sparkSession.sparkContext.getConf
        .getOption("spark.executorEnv.SFTP_PATH")
        .getOrElse(config.sftp.sftpPath)

      _ = println(s"#####From SparkConf: $sftpUser1, $sftpHost1, $sftpPath1")

      sftpUri = s"sftp://${sftpUser1}:${sftpPass1}@${sftpHost1}" + s":${sftpPath1}"

      inferSchema         = true
      first_row_is_header = true
      sourceUri           = new URI(sftpUri)

      // TODO: Test reading zip
      // TODO: Test reading splittable formats: snappy, lzo, bzip2
      // TODO: Test reading multiple files in a directory.

      df = sparkSession.read
        .option("header", first_row_is_header)
        .option("inferSchema", inferSchema)
        .csv(sourceUri.toString)

      _ = df.printSchema()

      //Testing the content of the dataframe, the time in doing the count can be using to measure time in reading.
      _ = df.printSchema()
      _ = println(s"### COUNT: ${df.count()}")
      _ = df.show(false)

      //_ = sparkSession.sparkContext.setLogLevel("DEBUG") //If we wanted to debug, we could use this.
      _ = sparkSession.sqlContext.sql("DROP TABLE IF EXISTS user_data")
      _ = df.write.mode(SaveMode.Overwrite).format("parquet").saveAsTable("user_data")
      // TODO: Check if it still could fail with the below error or has been solved with the flag: allowCreatingManagedTableUsingNonemptyLocation
      //  org.apache.spark.sql.catalyst.analysis.NoSuchDatabaseException: Database 'default' not found;
      //	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.org$apache$spark$sql$catalyst$catalog$SessionCatalog$$requireDbExists(SessionCatalog.scala:178)
      //	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.createTable(SessionCatalog.scala:316)
      //	at org.apache.spark.sql.execution.command.CreateDataSourceTableAsSelectCommand.run(createDataSourceTables.scala:185)

      _ = sparkSession.catalog.listTables().show(truncate = false)
      _ = sparkSession.sql("show tables").show(truncate = false)

      // Sample operations to query the Hive database
      dataFromHive = sparkSession.sql("select name from user_data")
      _            = dataFromHive.show(false)

      // Write dataframe as CSV file to FTP server
      _ = dataFromHive.write
        .mode(SaveMode.Overwrite)
        .csv(s"${sftpPath1}.processed.csv")

      exitCode = ExitCode.Success

    } yield exitCode
}
