package org.fortysevendeg.sparksftp

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.fortysevendeg.sparksftp.common.{HiveUserData, SparkUtils}
import org.fortysevendeg.sparksftp.common.SparkUtils._
import org.fortysevendeg.sparksftp.config.model.configs
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader
import org.apache.spark.sql.types.IntegerType

case class Salary(name: String, salary: Long)

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

      /**
        We could also use:
          .set("spark.master", "local")
          .set("spark.master", "local[*]")
        or to run the application locally, for example, through "sbt run"
       */
      sparkSession = getSparkSessionWithHive(defaultSparkConf)

      readConfig = configs.ReadingSFTPConfig
        .configFromContextProperties(sparkSession.sparkContext, config)

      // Read the source files from SFTP into dataframes
      usersPath    = s"sftp://${readConfig.sftp.sftpUser}:${readConfig.sftp.sftpPass}@${readConfig.sftp.sftpHost}" + s":${readConfig.sftp.sftpUserPath}"
      salariesPath = s"sftp://${readConfig.sftp.sftpUser}:${readConfig.sftp.sftpPass}@${readConfig.sftp.sftpHost}" + s":${readConfig.sftp.sftpSalaryPath}"

      users    = dataframeFromCSV(sparkSession, usersPath)
      salaries = dataframeFromCSV(sparkSession, salariesPath)

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
      _ = dataframeToCompressedCsv(userDataFromHive, s"${usersPath}_output")
      _ = dataframeToCompressedCsv(salariesDataFromHive, s"${salariesPath}_output")
      _ = dataframeToCompressedCsv(userNewSalariesFromHive, s"${salariesPath}_transformed_output")

    } yield ExitCode.Success
}
