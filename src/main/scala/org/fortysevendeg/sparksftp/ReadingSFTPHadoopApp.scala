package org.fortysevendeg.sparksftp

import java.net.URI

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.fortysevendeg.sparksftp.common.SparkUtils
import org.fortysevendeg.sparksftp.config.model.configs
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader

object ReadingSFTPHadoopApp extends IOApp {

  def setupConfig: IO[ReadingSFTPConfig] =
    ConfigLoader[IO]
      .loadConfig[ReadingSFTPConfig]

  def run(args: List[String]): IO[ExitCode] =
    for {
      config: ReadingSFTPConfig <- setupConfig
      defaultSparkConf: SparkConf = SparkUtils
        .createSparkConfWithSFTPSupport(config)
        .set("fs.sftp.impl", "org.apache.hadoop.fs.sftpwithseek.SFTPFileSystem")
        .set("fs.sftp.impl.disable.cache", "true")

      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

      sftpConfig = configs.SFTPConfig
        .configFromContextProperties(sparkSession.sparkContext, config.sftp)
      sftpUri = s"sftp://${sftpConfig.sftpUser}:${sftpConfig.sftpPass}@${sftpConfig.sftpHost}" + s":${sftpConfig.sftpPath}"

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

      //_ = sparkSession.sparkContext.setLogLevel("DEBUG") //If we wanted to debug, we could use this.
      _ = sparkSession.sqlContext.sql("DROP TABLE IF EXISTS user_data")
      _ = df.write.mode(SaveMode.Overwrite).format("parquet").saveAsTable("user_data")

      _ = sparkSession.catalog.listTables().show(truncate = false)
      _ = sparkSession.sql("show tables").show(truncate = false)

      // Sample operations to query the Hive database
      dataFromHive = sparkSession.sql("select name from user_data")
      _            = dataFromHive.show(false)

      // Write dataframe as CSV file to FTP server
      _ = dataFromHive.write
        .mode(SaveMode.Overwrite)
        .csv(s"${sftpConfig.sftpPath}.processed.csv")

      exitCode = ExitCode.Success

    } yield exitCode
}
