package org.fortysevendeg.sparksftp.config.model

import org.apache.spark.SparkContext

object configs {

  case class SFTPConfig(
      sftpHost: String,
      sftpUser: String,
      sftpPass: String,
      sftpUserPath: String,
      sftpSalaryPath: String
  )

  final case class SparkConfig(partitions: Int, serializer: String, storagePath: String)

  final case class ReadingSFTPConfig(sftp: SFTPConfig, spark: SparkConfig)

  object ReadingSFTPConfig {
    def configFromContextProperties(sparkContext: SparkContext, config: ReadingSFTPConfig): ReadingSFTPConfig =
      config.copy(
        sftp =  SFTPConfig(
        sftpUser = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_USER")
          .getOrElse(config.sftp.sftpUser),
        sftpPass = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_PASS")
          .getOrElse(config.sftp.sftpPass),
        sftpHost = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_HOST")
          .getOrElse(config.sftp.sftpHost),
        sftpUserPath = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_USERS_PATH")
          .getOrElse(config.sftp.sftpUserPath),
        sftpSalaryPath = sparkContext.getConf
          .getOption("spark.executorEnv.SFTP_SALARIES_PATH")
          .getOrElse(config.sftp.sftpSalaryPath)
      ),
        spark = config.spark.copy(
          storagePath = sparkContext.getConf
              .getOption("spark.executorEnv.STORAGE_PATH")
              .getOrElse(config.spark.storagePath)
        )
      )
  }
}
