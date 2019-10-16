package org.fortysevendeg.sparksftp.common

import java.net.URI

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.fortysevendeg.sparksftp.config.model.configs.{ReadingSFTPConfig, SFTPConfig}

object SparkUtils {

  def createSparkConfWithSFTPSupport(config: ReadingSFTPConfig): SparkConf = {
    new SparkConf()
      .set("spark.serializer", config.spark.serializer)
      //.set("spark.master", "local[*]")
      .set(
        "spark.kryo.registrationRequired",
        config.spark.serializer.contains("KryoSerializer").toString
      )
      .set("spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version", "2")
      .set("spark.hadoop.mapreduce.fileoutputcommitter.cleanup-failures.ignored", "true")
      //.set("spark.hadoop.parquet.enable.summary-metadata", "false")
      .set("spark.hadoop.parquet.summary.metadata.level", "NONE") //Other option "ALL"
      .set("spark.sql.parquet.mergeSchema", "false")
      .set("spark.sql.parquet.filterPushdown", "true")
      .set("spark.sql.hive.metastorePartitionPruning", "true")
      .set("spark.hadoop.fs.sftp.impl", "org.apache.hadoop.fs.sftp.SFTPFileSystem")
      .set("spark.sql.legacy.allowCreatingManagedTableUsingNonemptyLocation", "true") //https://kb.azuredatabricks.net/jobs/spark-overwrite-cancel.html
      .registerKryoClasses(RegisterInKryo.classes.toArray)
  }

  def getSparkSessionWithHive(sparkconf: SparkConf): SparkSession = {
    val sparkSession = SparkSession.builder
      .config(sparkconf)
      .enableHiveSupport
      .getOrCreate()

    sparkSession
  }

  def dataframeFromCSV(sparkSession: SparkSession, sftpUri: String): DataFrame = {
    val inferSchema         = true
    val first_row_is_header = true
    val sourceUri           = new URI(sftpUri)

    sparkSession.read
      .format("csv")
      .option("delimiter", ",")
      .option("header", first_row_is_header)
       .option("inferSchema", inferSchema)
      //.csv(sourceUri.toString)
      .load(sourceUri.toString)
  }

  def persistDataFrame(
      sparkSession: SparkSession,
      df: DataFrame,
      name: String,
      partitionBy: Seq[String] = Seq.empty
  ) = {
    // Persist the dataframes into Hive tables with parquet file format, the default compression for parquet is snappy, that is splittable for parquet.
    // Another option: externalTable (HDFS, Hive)
    // If we wanted to debug any issue with the databases, we could use this: sparkSession.sparkContext.setLogLevel("DEBUG")

    sparkSession.sql("CREATE DATABASE IF NOT EXISTS default")

    sparkSession.sql(s"DROP TABLE IF EXISTS ${name}")

    df.write
      .mode(SaveMode.Overwrite)
      .partitionBy(partitionBy: _*)
      //.parquet(name)
      .format("parquet")
      .saveAsTable(name)

    sparkSession.sql("describe database default").show

  }

  def dataframeToCompressedCsv(df: DataFrame, path: String) = {
    df.coalesce(1)
      .write
      .mode(SaveMode.Overwrite)
      .option("codec", "org.apache.hadoop.io.compress.GzipCodec")
      .csv(path)
  }

  def dataframeToCsv(df: DataFrame, path: String) = {
    df.write
      .mode(SaveMode.Overwrite)
      .csv(path)
  }

  /**
   * Construct a Spark DataFrame reading a file from SFTP using the `springml` connector
   */
  def dataframeFromCsvWithSFTPConnector(
      sparkSession: SparkSession,
      sftpConfig: SFTPConfig,
      path: String
  ): DataFrame = {
    sparkSession.read
      .format("com.springml.spark.sftp")
      .option("host", sftpConfig.sftpHost)
      .option("username", sftpConfig.sftpUser)
      .option("password", sftpConfig.sftpPass)
      .option("header", true)
      .option("fileType", "csv")
      .option("delimiter", ",")
      .option("inferSchema", "true")
      .load(path)
  }

  /**
   * Persist a Spark DataFrame in SFTP using the `springml` connector
   */
  def dataframeToCompressedCsvWithSFTPConnector(
      df: DataFrame,
      sftpConfig: SFTPConfig,
      path: String
  ) = {
    df.write
      .format("com.springml.spark.sftp")
      .option("host", sftpConfig.sftpHost)
      .option("username", sftpConfig.sftpUser)
      .option("password", sftpConfig.sftpPass)
      .option("header", true)
      .option("delimiter", ",")
      .option("fileType", "csv")
      .save(path)
  }

}
