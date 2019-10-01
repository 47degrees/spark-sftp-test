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
        .set("spark.hadoop.fs.sftp.impl", "org.apache.hadoop.fs.sftp.SFTPFileSystem")
        .set("fs.sftp.impl", "org.apache.hadoop.fs.sftp.SFTPFileSystem")
        .set("fs.sftp.proxy.host", config.sftp.sftpHost)
        .set("fs.sftp.host.port", "22")
        .set("fs.sftp.impl.disable.cache", "true")
        .registerKryoClasses(RegisterInKryo.classes.toArray)

      sftpPath = s"sftp://${config.sftp.sftpUser}:${config.sftp.sftpPass}@${config.sftp.sftpHost}" + s":${config.sftp.sftpPath}"

      sparkSession: SparkSession = SparkSession.builder
        .config(defaultSparkConf)
        .enableHiveSupport
        .getOrCreate()

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
      sourceUri           = new URI(sftpPath)

      //TODO: Option 1. Storing it, as fast as possible, without processing. Probably easier. Implement
      //TODO: Option 2. Convert to RDD (optionally process it) and store it.
      //TODO: Test reading zip, and multiple files in a directory.
      //TODO: Test reading by partitions in parallel.

      df = sparkSession.read
        .option("header", first_row_is_header)
        //    option("delimiter", delimiter).
        //    option("quote", quote).
        //    option("escape", escape).
        //    option("multiLine", multiLine).
        .option("inferSchema", inferSchema)
        .csv(sourceUri.getPath)
        .repartition(config.spark.partitions)

      _ = df.printSchema()
      _ = println(s"##############COUNT: ${df.count()}")
      _ = df.show(false)

      //_ = df.collect().foreach(println)
      //_ = data.printSchema()
      //_ = data.show(false)

      exitCode = ExitCode.Success

    } yield exitCode

}
