package org.apache.hadoop.fs.sftpwithseek

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.apache.hadoop.fs.FSInputStream
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader
import org.apache.hadoop.util.StringUtils
import java.io.IOException
import java.io.InputStream
/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * {@link SFTPInputStream}, copied from Hadoop and modified, that doesn't throw an exception when seeks are attempted
 * to the current position. Position equality check logic in {@link SFTPInputStream#seek} is the only change from the
 * original class in Hadoop. This change is required since {@link LineRecordReader} calls {@link SFTPInputStream#seek}
 * with value of 0. TODO: This file can be removed once https://issues.cask.co/browse/CDAP-5387 is addressed.
 */
object SFTPInputStream {
  val E_SEEK_NOTSUPPORTED   = "Seek not supported"
  val E_CLIENT_NULL         = "SFTP client null or not connected"
  val E_NULL_INPUTSTREAM    = "Null InputStream"
  val E_STREAM_CLOSED       = "Stream closed"
  val E_CLIENT_NOTCONNECTED = "Client not connected"
}
class SFTPInputStream private[sftpwithseek] (
    var wrappedStream: InputStream,
    var channel: ChannelSftp,
    var stats: FileSystem.Statistics
) extends FSInputStream {
  if (wrappedStream == null) throw new IllegalArgumentException(SFTPInputStream.E_NULL_INPUTSTREAM)
  if (channel == null || !channel.isConnected)
    throw new IllegalArgumentException(SFTPInputStream.E_CLIENT_NULL)
  private var closed = false
  private var pos    = 0L
  this.pos = 0
  this.closed = false

// We don't support seek unless the current position is same as the desired position.
//  @throws[IOException]
//  override def seek(position: Long): Unit = { // If seek is to the current pos, then simply return. This logic was added so that the seek call in
//    // LineRecordReader#initialize method to '0' does not fail.
//    if (getPos == position) return
//    throw new IOException(SFTPInputStream.E_SEEK_NOTSUPPORTED)
//  }
//  @throws[IOException]
//  override def seekToNewSource(targetPos: Long) =
//    throw new IOException(SFTPInputStream.E_SEEK_NOTSUPPORTED)

  @throws[IOException]
  def seek(position: Long): Unit = {
    this.wrappedStream.skip(position)
    this.pos = position
  }
  @throws[IOException]
  def seekToNewSource(targetPos: Long) = false

  @throws[IOException]
  override def getPos: Long = pos
  @throws[IOException]
  override def read: Int = {
    if (closed) throw new IOException(SFTPInputStream.E_STREAM_CLOSED)
    val byteRead = wrappedStream.read
    if (byteRead >= 0) pos += 1
    if (stats != null & byteRead >= 0) stats.incrementBytesRead(1)
    byteRead
  }
  @throws[IOException]
  override def read(buf: Array[Byte], off: Int, len: Int): Int = {
    if (closed) throw new IOException(SFTPInputStream.E_STREAM_CLOSED)
    val result = wrappedStream.read(buf, off, len)
    if (result > 0) pos += result
    if (stats != null & result > 0) stats.incrementBytesRead(result)
    result
  }
  @throws[IOException]
  override def close(): Unit = {
    if (closed) return
    super.close()
    closed = true
    if (!channel.isConnected) throw new IOException(SFTPInputStream.E_CLIENT_NOTCONNECTED)
    try {
      val session = channel.getSession
      channel.disconnect()
      session.disconnect()
    } catch {
      case e: JSchException =>
        throw new IOException(StringUtils.stringifyException(e))
    }
  }
}
