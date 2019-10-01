package org.fortysevendeg.sparksftp.config.model

object configs {

  final case class SFTPConfig(sftpHost: String, sftpUser: String, sftpPass: String, sftpPath: String)
  final case class SparkConfig(partitions: Int, serializer: String)

  final case class ReadingSFTPConfig(sftp: SFTPConfig, spark: SparkConfig)

}
