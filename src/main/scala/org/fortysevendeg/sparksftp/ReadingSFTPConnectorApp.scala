package org.fortysevendeg.sparksftp

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.functor._
import pureconfig.generic.auto._
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.fortysevendeg.sparksftp.common.SparkUtils
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader
import org.apache.spark.sql.SaveMode
import org.fortysevendeg.sparksftp.config.model.configs

object ReadingSFTPConnectorApp extends IOApp {

  def setupConfig: IO[ReadingSFTPConfig] =
    ConfigLoader[IO]
      .loadConfig[ReadingSFTPConfig]

  def run(args: List[String]): IO[ExitCode] =
    for {
      config: ReadingSFTPConfig <- setupConfig
      defaultSparkConf: SparkConf = SparkUtils.createSparkConfWithSFTPSupport(config)
      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

      sftpConfig = configs.SFTPConfig
        .configFromContextProperties(sparkSession.sparkContext, config.sftp)

      // Construct Spark data frame reading a file from SFTP
      data: DataFrame = sparkSession.read
        .format("com.springml.spark.sftp") //TODO: Is this library single threaded?
        .option("host", sftpConfig.sftpHost)
        .option("username", sftpConfig.sftpUser)
        .option("password", sftpConfig.sftpPass)
        .option("header", true)
        .option("fileType", "csv")
        .option("delimiter", ",")
        .option("inferSchema", "true")
        .load(sftpConfig.sftpPath)

      // Creating databases do not work in Dataproc: https://github.com/mozafari/verdictdb/issues/163
      //_ = if (sparkSession.catalog.databaseExists("sampledb") == false) sparkSession.sqlContext.sql("create database sampledb USING HIVE")
      //_ = sparkSession.catalog.setCurrentDatabase("sampledb")

      // https://stackoverflow.com/questions/30664008/how-to-save-dataframe-directly-to-hive
      _ = sparkSession.sqlContext.sql("DROP TABLE IF EXISTS user_data")
      _ = data.write.mode(SaveMode.Overwrite).format("parquet").saveAsTable("user_data")

      // Some other sample operations with databases and tables
      _ = sparkSession.catalog.listTables().show(truncate = false)
      _ = sparkSession.sql("show tables").show(truncate = false)

      // Sample operations to query the Hive database
      // dataFromHive = sparkSession.sql("select * from sampledb.user_data")
      // justName = dataFromHive.select("name")
      dataFromHive = sparkSession.sql("select name from user_data")
      _            = dataFromHive.show(false)

      // Write dataframe as CSV file to FTP server
      _ = dataFromHive.write
        .format("com.springml.spark.sftp")
        .option("host", sftpConfig.sftpHost)
        .option("username", sftpConfig.sftpUser)
        .option("password", sftpConfig.sftpPass)
        .option("header", true)
        .option("delimiter", ",")
        .option("fileType", "csv")
        .save(s"${sftpConfig.sftpPath}.processed.csv")

      exitCode = ExitCode.Success

    } yield exitCode
}
