package org.fortysevendeg.sparksftp

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.fortysevendeg.sparksftp.common.{HiveUserData, SparkUtils}
import org.fortysevendeg.sparksftp.common.SparkUtils._
import org.fortysevendeg.sparksftp.config.model.configs
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig
import org.training.trainingbot.config.ConfigLoader
import org.apache.spark.sql.{Dataset, Encoder, Encoders}
import org.apache.spark.sql.functions._
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
        //.set("spark.speculation", "false")
        //.set("spark.hadoop.mapreduce.map.speculative", "false")
        //.set("spark.hadoop.mapreduce.reduce.speculative", "false")

      sparkSession = getSparkSessionWithHive(defaultSparkConf)

      //_ = import sparkSession.implicits._

      sftpConfig = configs.SFTPConfig
        .configFromContextProperties(sparkSession.sparkContext, config.sftp)

      // Read the source files from SFTP into dataframes
      usersPath    = s"sftp://${sftpConfig.sftpUser}:${sftpConfig.sftpPass}@${sftpConfig.sftpHost}" + s":${sftpConfig.sftpUserPath}"
      salariesPath = s"sftp://${sftpConfig.sftpUser}:${sftpConfig.sftpPass}@${sftpConfig.sftpHost}" + s":${sftpConfig.sftpSalaryPath}"

      users    = dataframeFromCSV(sparkSession, usersPath)
        .repartition(8)
      salaries = dataframeFromCSV(sparkSession, salariesPath)
        .repartition(8)


      // Sample operations to persist and query the Hive database
      _                                        = HiveUserData.persistUserData(sparkSession, users, salaries)
      (userDataFromHive, salariesDataFromHive, userSalaries) = HiveUserData.readUserData(sparkSession)


      //implicit0(longimplicitEncoder: Encoder[Double]) = Encoders.kryo[Double]
      //implicit0(salaryEncoder: Encoder[Salary]) = Encoders.kryo[Salary]
      //salariesDataSet = salariesDataFromHive.as[Salary]
//      newSalaries = salariesDataSet.withColumn("raisedSalary", salariesDataSet.col("salary").as[Long] * 1.1)
//        .select("ID", "salary", "raisedSalary")
      //.withColumnRenamed("raisedSalary", "salary")

      //newSalaries = userSalaries.withColumn("new_salaries", col("salary").as[Int](sparkSession.implicits.newIntEncoder) * 1.1)
      //ds:Dataset[Salary] = userSalaries.as[Salary]
      //newSalaries = userSalaries.withColumn("new_salaries", lit(Math.floor( ds("salary").as[Double] * 1.1).toInt))
      //_ = newSalaries.map( d => d.getAs[Long]("salary"))(sparkSession.implicits.newLongEncoder)//, //d.getAs[Long]("salary") * 1.1 )

      newSalaries = userSalaries.withColumn("new_salary", (userSalaries("salary") * 1.1).cast(IntegerType))
      _ = SparkUtils.persistDataFrame(sparkSession, newSalaries, "user_new_salary")
      userNewSalariesFromHive = sparkSession.sql("select name,salary from user_new_salary")


      //newSalaries = salariesDataSet.withColumn("raisedSalary", lit()
      //_ = salariesDataSet.map(_.salary)//(LONG)
      //coalesce(8) //partitions

      // Write dataframe as CSV file to FTP server
      _ = dataframeToCompressedCsv(userDataFromHive, s"${sftpConfig.sftpUserPath}_output")
      _ = dataframeToCompressedCsv(salariesDataFromHive, s"${sftpConfig.sftpSalaryPath}_output")
      _ = dataframeToCompressedCsv(userSalaries, s"${sftpConfig.sftpSalaryPath}_transformed_output")

    } yield ExitCode.Success
}
