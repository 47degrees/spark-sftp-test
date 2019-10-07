package org.fortysevendeg.sparksftp.common

import org.apache.spark.SparkConf
import org.fortysevendeg.sparksftp.config.model.configs.ReadingSFTPConfig

object SparkUtils {

  def createSparkConfWithSFTPSupport(config: ReadingSFTPConfig): SparkConf = {
    new SparkConf()
      .set("spark.serializer", config.spark.serializer)
      .set("spark.master", "local")
      .set(
        "spark.kryo.registrationRequired",
        config.spark.serializer.contains("KryoSerializer").toString
      )
      .set("spark.hadoop.fs.sftp.impl", "org.apache.hadoop.fs.sftp.SFTPFileSystem")
      .set("spark.sql.legacy.allowCreatingManagedTableUsingNonemptyLocation", "true") //https://kb.azuredatabricks.net/jobs/spark-overwrite-cancel.html
      .registerKryoClasses(RegisterInKryo.classes.toArray)

  }
}
