package org.fortysevendeg.sparksftp

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.flatMap._
import cats.syntax.functor._
import pureconfig.generic.auto._
import org.apache.spark.SparkConf
import org.fortysevendeg.sparksftp.common.SparkUtils._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.IntegerType
import org.fortysevendeg.sparksftp.common.{HiveUserData, SparkUtils}
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader
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

      readConfig = configs.ReadingSFTPConfig
        .configFromContextProperties(sparkSession.sparkContext, config)

      _ = sparkSession.sparkContext.getConf.set("config.spark.storagePath", config.spark.storagePath)


      // Read the source files from SFTP into dataframes
      users = dataframeFromCsvWithSFTPConnector(sparkSession, readConfig.sftp, readConfig.sftp.sftpUserPath)
        .repartition(8)
      salaries = dataframeFromCsvWithSFTPConnector(
        sparkSession,
        readConfig.sftp,
        readConfig.sftp.sftpSalaryPath
      )

      // Sample operations to persist and query the Hive database
      _ = HiveUserData.persistUserData(sparkSession, users, salaries)
      (userDataFromHive, salariesDataFromHive, userSalaries) = HiveUserData.readUserData(
        sparkSession
      )

      newSalaries = userSalaries.withColumn(
        "new_salary",
        (userSalaries("salary") * 1.1).cast(IntegerType)
      )

      _                       = SparkUtils.persistDataFrame(sparkSession, newSalaries, "user_new_salary")
      userNewSalariesFromHive = sparkSession.sql("select name,salary from user_new_salary")

      // Write dataframe as CSV file to FTP server
      _ = dataframeToCompressedCsvWithSFTPConnector(
        userDataFromHive,
        readConfig.sftp,
        s"${readConfig.sftp.sftpUserPath}_output"
      )
      _ = dataframeToCompressedCsvWithSFTPConnector(
        salariesDataFromHive,
        readConfig.sftp,
        s"${readConfig.sftp.sftpSalaryPath}_output"
      )

      _ = dataframeToCompressedCsvWithSFTPConnector(
        userNewSalariesFromHive,
        readConfig.sftp,
        s"${readConfig.sftp.sftpSalaryPath}_transformed_output"
      )

    } yield ExitCode.Success
}
