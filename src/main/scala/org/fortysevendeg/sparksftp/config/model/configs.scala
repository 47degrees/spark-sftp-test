package org.fortysevendeg.sparksftp.config.model

object configs {

  final case class SFTPConfig(sftpHost: String, sftpUser: String, sftpPass: String)
  final case class SparkConfig(serializer: String)

  final case class ReadingSFTPConfig(sftp: SFTPConfig, spark: SparkConfig)

}