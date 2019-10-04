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
        .set("fs.sftp.proxy.host", config.sftp.sftpHost)
        .set("fs.sftp.host", config.sftp.sftpHost)
        .set("fs.sftp.user", config.sftp.sftpUser)
        .set("fs.sftp.password", config.sftp.sftpPass)
        .set("fs.sftp.host.port", "22")
        .set("fs.sftp.impl.disable.cache", "true")
        .registerKryoClasses(RegisterInKryo.classes.toArray)

      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

//      sftpUser = sys.props.get("spark.executorEnv.SFTP_USER")
//      sftpPass = sys.props.get("spark.executorEnv.SFTP_PASS")
//      sftpHost = sys.props.get("spark.executorEnv.SFTP_HOST")
//      sftpPath = sys.props.get("spark.executorEnv.SFTP_PATH")
//      _ = println(s"$sftpUser, $sftpPass, $sftpHost, $sftpPath")
//      sftpUri = s"sftp://${sftpUser}:${sftpPass}@${sftpHost}" + s":${sftpPath}"

//      sftpUser = sys.props.getOrElse("spark.sftp.SFTP_USER", config.sftp.sftpUser)
//      sftpPass = sys.props.getOrElse("spark.sftp.SFTP_PASS", config.sftp.sftpPass)
//      sftpHost = sys.props.getOrElse("spark.sftp.SFTP_HOST", config.sftp.sftpHost)
//      sftpPath = sys.props.getOrElse("spark.sftp.SFTP_PATH", config.sftp.sftpPath)

      sftpUser1 = sparkSession.sparkContext.getConf
        .getOption("spark.sftp.SFTP_USER")
        .getOrElse(config.sftp.sftpUser)
      sftpPass1 = sparkSession.sparkContext.getConf
        .getOption("spark.sftp.SFTP_PASS")
        .getOrElse(config.sftp.sftpPass)
      sftpHost1 = sparkSession.sparkContext.getConf
        .getOption("spark.sftp.SFTP_HOST")
        .getOrElse(config.sftp.sftpHost)
      sftpPath1 = sparkSession.sparkContext.getConf
        .getOption("spark.sftp.SFTP_PATH")
        .getOrElse(config.sftp.sftpPath)

      _ = println(s"$sftpUser1, $sftpHost1, $sftpPath1")

      sftpUri = s"sftp://${sftpUser1}:${sftpPass1}@${sftpHost1}" + s":${sftpPath1}"

      // Others way of reading, if we wanted to read as inputstreamT
      // context = sparkSession.sparkContext
      // _ = context.addFile(sftpPath)
      // fileName = SparkFiles.get(sftpPath.split("/").last)
      // fileRDD = context.textFile(fileName)
      // sourceFS = FileSystem.get(sourceUri, context.hadoopConfiguration)
      // fsinputStream = sourceFS.open(new Path(sourceUri.getPath))
      // context.textFile
      // context.hadoopFile
      // context.newAPIHadoopFile()
      inferSchema         = true
      first_row_is_header = true
      sourceUri           = new URI(sftpUri)

      //TODO: Option 1. Storing it, as fast as possible, without processing. Probably easier. Implement
      //TODO: Option 2. Convert to RDD (optionally process it) and store it.
      //TODO: Test reading zip, and multiple files in a directory.
      //TODO: Test reading by partitions in parallel.

//      _ = println(
//        s"sourceUri $sourceUri, getPath ${sourceUri.getPath}, sourceUri.toString ${sourceUri.toString}"
//      )

      df = sparkSession.read
        .option("header", first_row_is_header)
        //    option("delimiter", delimiter).
        //    option("quote", quote).
        //    option("escape", escape).
        //    option("multiLine", multiLine).
        .option("inferSchema", inferSchema)
        .csv(sourceUri.toString)

      _ = df.printSchema()
      _ = println(s"##############COUNT: ${df.count()}")
      _ = df.show(false)

      //_ = df.collect().foreach(println)
      //_ = data.printSchema()
      //_ = data.show(false)

      exitCode = ExitCode.Success

    } yield exitCode
}
