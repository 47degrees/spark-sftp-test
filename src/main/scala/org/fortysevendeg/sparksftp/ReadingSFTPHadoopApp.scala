package org.fortysevendeg.sparksftp

import java.net.URI

import pureconfig.generic.auto._
import cats.effect.{ExitCode, IO, IOApp}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
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
        .registerKryoClasses(RegisterInKryo.classes.toArray)

      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

//    This "redundant" code for different ways of reading configuration values is due to an investigation of how to pass
//    parameters and properties in a way that is both usable by spark-submit and also Google Cloud Dataproc properties.
//      sftpUser = sys.props.get("spark.executorEnv.SFTP_USER")
//      sftpPass = sys.props.get("spark.executorEnv.SFTP_PASS")
//      sftpHost = sys.props.get("spark.executorEnv.SFTP_HOST")
//      sftpPath = sys.props.get("spark.executorEnv.SFTP_PATH")
//      _ = println(s"$sftpUser, $sftpPass, $sftpHost, $sftpPath")
//      sftpUri = s"sftp://${sftpUser}:${sftpPass}@${sftpHost}" + s":${sftpPath}"

      sftpUser = sys.props.getOrElse("spark.executorEnv.SFTP_USER", config.sftp.sftpUser)
      sftpPass = sys.props.getOrElse("spark.executorEnv.SFTP_PASS", config.sftp.sftpPass)
      sftpHost = sys.props.getOrElse("spark.executorEnv.SFTP_HOST", config.sftp.sftpHost)
      sftpPath = sys.props.getOrElse("spark.executorEnv.SFTP_PATH", config.sftp.sftpPath)

      _ = println(s"####From sys props: $sftpUser, $sftpHost, $sftpPath")

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
        //    option("delimiter", delimiter).
        //    option("quote", quote).
        //    option("escape", escape).
        //    option("multiLine", multiLine).
        .option("inferSchema", inferSchema)
        .csv(sourceUri.toString)

      _ = df.printSchema()

      //Testing the content of the dataframe, the time in doing the count can be using to measure time in reading.
      _ = println(s"### COUNT: ${df.count()}")
      _ = df.show(false)

      exitCode = ExitCode.Success

    } yield exitCode
}
