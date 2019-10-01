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

object ReadingSFTPConnectorApp extends IOApp {

  def setupConfig: IO[ReadingSFTPConfig] =
    ConfigLoader[IO]
      .loadConfig[ReadingSFTPConfig]

  def run(args: List[String]): IO[ExitCode] = {

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
        .option("fileType", "csv")
        .option("inferSchema", "true")
        .load("/tmp/spark/sample.psv")
        .repartition(config.spark.partitions)

      // Some other sample operations
      //_ = sparkSession.catalog.listDatabases().show(truncate = false)
      //_ = sparkSession.catalog.listTables().show(truncate = false)
      //_ = sparkSession.sql("show tables").show(truncate = false)

      _ = data.printSchema()
      _ = println(s"##############COUNT: ${data.count()}")
      _ = data.show(false)

      // TODO: Other steps to do after the processing.

      //      Write dataframe as CSV file to FTP server
      //      df.write().
      //      format("com.springml.spark.sftp").
      //      option("host", "SFTP_HOST").
      //      option("username", "SFTP_USER").
      //      option("password", "****").
      //      option("fileType", "csv").
      //      save("/ftp/files/sample.csv")

      exitCode = ExitCode.Success

    } yield exitCode

  }
}
