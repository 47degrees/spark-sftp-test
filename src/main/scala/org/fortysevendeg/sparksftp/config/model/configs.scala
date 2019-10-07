package org.fortysevendeg.sparksftp.config.model

import org.apache.spark.SparkContext

object configs {

  case class SFTPConfig(
      sftpHost: String,
      sftpUser: String,
      sftpPass: String,
      sftpPath: String
  )

  final case class SparkConfig(partitions: Int, serializer: String)

  final case class ReadingSFTPConfig(sftp: SFTPConfig, spark: SparkConfig)

  object SFTPConfig {
    def configFromContextProperties(sparkContext: SparkContext, config: SFTPConfig): SFTPConfig =
      new SFTPConfig(
        sftpUser = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_USER")
          .getOrElse(config.sftpUser),
        sftpPass = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_PASS")
          .getOrElse(config.sftpPass),
        sftpHost = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_HOST")
          .getOrElse(config.sftpHost),
        sftpPath = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_PATH")
          .getOrElse(config.sftpPath)
      )
  }
}
