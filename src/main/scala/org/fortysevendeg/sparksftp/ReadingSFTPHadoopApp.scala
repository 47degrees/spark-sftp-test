package org.fortysevendeg.sparksftp

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.fortysevendeg.sparksftp.common.{HiveUserData, SparkUtils}
import org.fortysevendeg.sparksftp.common.SparkUtils._
import org.fortysevendeg.sparksftp.config.model.configs
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader

object ReadingSFTPHadoopApp extends IOApp {

  def setupConfig: IO[ReadingSFTPConfig] =
    ConfigLoader[IO]
      .loadConfig[ReadingSFTPConfig]

  def run(args: List[String]): IO[ExitCode] =
    for {
      // Read the application parameters and prepare the spark session.
      config: ReadingSFTPConfig <- setupConfig
      defaultSparkConf: SparkConf = createSparkConfWithSFTPSupport(config)
        .set("fs.sftp.impl", "org.apache.hadoop.fs.sftpwithseek.SFTPFileSystem")
        .set("fs.sftp.impl.disable.cache", "true")
      sparkSession = getSparkSessionWithHive(defaultSparkConf)

      sftpConfig = configs.SFTPConfig
        .configFromContextProperties(sparkSession.sparkContext, config.sftp)

      // Read the source files from SFTP into dataframes
      usersPath    = s"sftp://${sftpConfig.sftpUser}:${sftpConfig.sftpPass}@${sftpConfig.sftpHost}" + s":${sftpConfig.sftpUserPath}"
      salariesPath = s"sftp://${sftpConfig.sftpUser}:${sftpConfig.sftpPass}@${sftpConfig.sftpHost}" + s":${sftpConfig.sftpSalaryPath}"

      users    = dataframeFromCSV(sparkSession, usersPath)
      salaries = dataframeFromCSV(sparkSession, salariesPath)

      // Sample operations to persist and query the Hive database
      _                                        = HiveUserData.persistUserData(sparkSession, users, salaries)
      (userDataFromHive, salariesDataFromHive) = HiveUserData.readUserData(sparkSession)

      // Write dataframe as CSV file to FTP server
      _ = dataframeToCompressedCsv(userDataFromHive, s"${sftpConfig.sftpUserPath}_output")
      _ = dataframeToCompressedCsv(salariesDataFromHive, s"${sftpConfig.sftpSalaryPath}_output")

    } yield ExitCode.Success
}
