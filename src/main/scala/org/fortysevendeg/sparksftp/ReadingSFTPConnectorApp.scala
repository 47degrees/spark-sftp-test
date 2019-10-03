package org.fortysevendeg.sparksftp

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.functor._
import pureconfig.generic.auto._
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.fortysevendeg.sparksftp.common.RegisterInKryo
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader
import org.apache.spark.sql.SaveMode

object ReadingSFTPConnectorApp extends IOApp {

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
        .set("spark.hadoop.fs.sftp.impl", "org.apache.hadoop.fs.sftp.SFTPFileSystem")
        .registerKryoClasses(RegisterInKryo.classes.toArray)

      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

      // Construct Spark data frame reading a file from SFTP
      data: DataFrame = sparkSession.read
        .format("com.springml.spark.sftp") //TODO: Is this library single threaded?
        .option("host", config.sftp.sftpHost)
        .option("username", config.sftp.sftpUser)
        .option("password", config.sftp.sftpPass)
        .option("header", true)
        .option("fileType", "csv")
        .option("delimiter", "|")
        .option("inferSchema", "true")
        .load("/tmp/spark/sample.psv")

      //Testing the content of the dataframe, the time in doing the count can be using to measure time in reading.
      _ = data.printSchema()
      _ = println(s"##############COUNT: ${data.count()}")
      _ = data.show(false)

      // https://stackoverflow.com/questions/30664008/how-to-save-dataframe-directly-to-hive
      //tables = sparkSession.sqlContext.tables("sampledb")
      _ = if (sparkSession.catalog.databaseExists("sampledb") == false)
        sparkSession.sqlContext.sql("create database sampledb")
      _ = data.write.mode(SaveMode.Overwrite).saveAsTable("sampledb.user_data")
      // Other operations when persisting
      // data.select(df.col("col1"), df.col("col2"), df.col("col3")).write.mode("overwrite").saveAsTable("schemaName.tableName")
      // data.write.mode(SaveMode.Overwrite).saveAsTable("dbName.tableName")

      // Some other sample operations with databases and tables
      _ = sparkSession.catalog.listDatabases().show(truncate = false)
      _ = sparkSession.catalog.setCurrentDatabase("sampledb")
      _ = sparkSession.catalog.listTables().show(truncate = false)
      _ = sparkSession.sql("show tables").show(truncate = false)

      // Query database
      //dataFromHive = sparkSession.sql("select * from sampledb.user_data")
      //justName = dataFromHive.select("name")
      dataFromHive = sparkSession.sql("select name from sampledb.user_data")

      _ = dataFromHive.show(false)
      //_ = println(justName.collect())

      // Other steps to do after the processing.
      // Write dataframe as CSV file to FTP server
      _ = dataFromHive.write
        .format("com.springml.spark.sftp")
        .option("host", config.sftp.sftpHost)
        .option("username", config.sftp.sftpUser)
        .option("password", config.sftp.sftpPass)
        .option("delimiter", "|")
        .option("fileType", "csv")
        .save("/tmp/spark/sample_processed.csv")

      exitCode = ExitCode.Success

    } yield exitCode
}
