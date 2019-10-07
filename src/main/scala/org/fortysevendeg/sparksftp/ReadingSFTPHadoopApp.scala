package org.fortysevendeg.sparksftp

import java.net.URI

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.fortysevendeg.sparksftp.common.RegisterInKryo
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader
import java.io.File

import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ReadingSFTPHadoopApp extends IOApp {

  def setupConfig: IO[ReadingSFTPConfig] =
    ConfigLoader[IO]
      .loadConfig[ReadingSFTPConfig]

  def run(args: List[String]): IO[ExitCode] =
    for {
      config: ReadingSFTPConfig <- setupConfig
      warehouseLocation = new File("spark-warehouse").getAbsolutePath
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
        //.set("checkpointLocation", warehouseLocation)
        .set("spark.sql.warehouse.dir", warehouseLocation)
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

      schema = StructType(Array(
        StructField("ID", IntegerType, true),
        StructField("job", StringType, true),
        StructField("email", StringType, true),
        StructField("name", StringType, true),
        StructField("age", IntegerType, true)
      ))

      ssc = new StreamingContext( sparkSession.sparkContext, Seconds(1))

      df = sparkSession.readStream
        .option("header", first_row_is_header)
        //.option("inferSchema", inferSchema)
        .schema(schema)
        .option("delimiter", ",")
        .option("enforceSchema", true)
        .option("checkpointLocation", "checkpoint")
        .format("csv")
        //.option("samplingRatio", 0.001)
        //.csv(sourceUri.toString)
        .csv(sourceUri.toString)
        //.load("/tmp/spark/")
        //.csv("/tmp/spark/")
        //.start()

      //_ = df.printSchema()

      //Testing the content of the dataframe, the time in doing the count can be using to measure time in reading.
      //_ = df.printSchema()
      //_ = println(s"### COUNT: ${df.count()}")
      //_ = df.show(false)


      //_ = sparkSession.sparkContext.setLogLevel("DEBUG") //If we wanted to debug, we could use this.
      _ = sparkSession.sqlContext.sql("DROP TABLE IF EXISTS user_data")
      //_ = //sparkSession.sqlContext.sql("CREATE TABLE user_data(ID int, job string, email string, name string, age int) STORED AS PARQUET USING hive ")

      _ = sparkSession.sqlContext.sql("CREATE TABLE user_data(ID int, job string, email string, name string, age int) USING hive ")

      //_ = sparkSession.sqlContext.sql("CREATE TABLE user_data(ID int, job string, email string, name string, age int) USING hive")
      //_ = df.write.mode(SaveMode.Overwrite).format("parquet").saveAsTable("user_data")

      //_ = df.createOrReplaceTempView("user_data")
      debugQuery = df.writeStream.format("console").start()
      _ = debugQuery.awaitTermination()

      query = df
        //.withWatermark("time", "5 years")

          //.collect()
        //.coalesce(1)
          .repartition(1)
      .writeStream
          .outputMode(OutputMode.Append())
        //.trigger(Trigger.ProcessingTime("10 seconds"))
        .trigger(Trigger.Once())
          //.outputMode(SaveMode.Overwrite.valueOf())
        //.option("checkpointLocation", "checkpoint")
        .format("parquet")
        .start("user_data")


      _ = Future({() => while(query.status.isDataAvailable) {
        Thread.sleep(100)
        ssc.stop(stopSparkContext = false, stopGracefully = true)
      }})


    _ = ssc.awaitTerminationOrTimeout(10)
      _ = ssc.stop(stopSparkContext = false, stopGracefully = true)
      //_ = query.awaitTermination(10)
      //_ = query.processAllAvailable()

      _ = query.stop


      //_ = query.awaitTermination(10)
      //_ = debugQuery.explain()

      //_ = debugQuery.stop()
      //_ = debugQuery.awaitAnyTermination()
      // TODO: Check if it still could fail with the below error or has been solved with the flag: allowCreatingManagedTableUsingNonemptyLocation
      //  org.apache.spark.sql.catalyst.analysis.NoSuchDatabaseException: Database 'default' not found;
      //	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.org$apache$spark$sql$catalyst$catalog$SessionCatalog$$requireDbExists(SessionCatalog.scala:178)
      //	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.createTable(SessionCatalog.scala:316)
      //	at org.apache.spark.sql.execution.command.CreateDataSourceTableAsSelectCommand.run(createDataSourceTables.scala:185)

      _ = sparkSession.catalog.listTables().show(truncate = false)
      _ = sparkSession.sql("show tables").show(truncate = false)

      // Sample operations to query the Hive database
//      dataFromHive = sparkSession.sql("select name from user_data")
//      _            = dataFromHive.show(false)




      // Write dataframe as CSV file to FTP server
//      query2 = dataFromHive.writeStream
//        .option("checkpointLocation", "checkpoint")
//        .format("csv")
//          .start(s"${sftpPath1}.processed.csv")

        //.mode(SaveMode.Overwrite)
        //.csv(s"${sftpPath1}.processed.csv")

      //_ = query2.awaitTermination()

      exitCode = ExitCode.Success

    } yield exitCode
}
