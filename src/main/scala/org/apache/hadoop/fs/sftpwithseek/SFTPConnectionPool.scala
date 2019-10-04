package org.apache.hadoop.fs.sftpwithseek

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.hadoop.util.StringUtils
import java.io.IOException
import java.util
import java.util.Properties

import org.apache.hadoop.fs.sftpwithseek.SFTPConnectionPool.ConnectionInfo

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/** Concurrent/Multiple Connections. */
object SFTPConnectionPool {

  val LOG: Log = LogFactory.getLog(classOf[SFTPFileSystem])

  /**
   * Class to capture the minimal set of information that distinguish
   * between different connections.
   */
  private[sftpwithseek] class ConnectionInfo private[sftpwithseek] (
      val hst: String,
      var port: Int,
      val usr: String
  ) {

    private var host = ""
    private var user = ""
    this.host = hst
    this.user = usr
    def getHost: String            = host
    def setHost(hst: String): Unit = this.host = hst
    def getPort: Int               = port
    def setPort(prt: Int): Unit    = this.port = prt
    def getUser: String            = user
    def setUser(usr: String): Unit = this.user = usr

    def canEqual(a: Any) = a.isInstanceOf[ConnectionInfo]

    override def equals(obj: Any): Boolean = {

      obj match {
        case obj: ConnectionInfo => {
          obj.canEqual(this) &&
          (!this.host.isEmpty && this.host.equalsIgnoreCase(obj.host)) &&
          (!this.user.isEmpty && this.user.equalsIgnoreCase(obj.user)) &&
          (this.port >= 0 && this.port == obj.port)
        }
        case _ => false
      }

//      if (this eq obj) return true
//      if (obj.isInstanceOf[ConnectionInfo])  {
//        val con = obj.asInstanceOf[ConnectionInfo]
//        var ret = true
//        if (this.host == null || !this.host.equalsIgnoreCase(con.host)) ret = false
//        if (this.port >= 0 && this.port != con.port) ret = false
//        if (this.user == null || !this.user.equalsIgnoreCase(con.user)) ret = false
//        ret
//      }
//      else false
    }
    override def hashCode: Int = {
      var hashCode = 0
      if (host != null) hashCode += host.hashCode
      hashCode += port
      if (user != null) hashCode += user.hashCode
      hashCode
    }
  }
}
class SFTPConnectionPool private[sftpwithseek] ( // Maximum number of allowed live connections. This doesn't mean we cannot
    // have more live connections. It means that when we have more
    // live connections than this threshold, any unused connection will be
    // closed.
    var maxConnection: Int
) {

  import SFTPConnectionPool._

  private var liveConnectionCount = 0
  private var idleConnections     = new util.HashMap[ConnectionInfo, util.HashSet[ChannelSftp]]
  private var con2infoMap         = new util.HashMap[ChannelSftp, ConnectionInfo]
  @throws[IOException]
  private[sftpwithseek] def getFromPool(info: ConnectionInfo): ChannelSftp = {
    val cons                 = idleConnections.get(info)
    var channel: ChannelSftp = null
    if (cons != null && cons.size > 0) {
      val it = cons.iterator
      if (it.hasNext) {
        channel = it.next
        idleConnections.remove(info)
        return channel
      } else throw new IOException("Connection pool error.")
    }
    null
  }

  /** Add the channel into pool.
   * @param channel
   */
  private[sftpwithseek] def returnToPool(channel: ChannelSftp): Unit = {
    val info = con2infoMap.get(channel)
    var cons = idleConnections.get(info)
    if (cons == null) {
      cons = new util.HashSet[ChannelSftp]
      idleConnections.put(info, cons)
    }
    cons.add(channel)
  }

  /** Shutdown the connection pool and close all open connections. */
  private[sftpwithseek] def shutdown(): Unit = {
    if (this.con2infoMap == null) return // already shutdown in case it is called
    SFTPConnectionPool.LOG.info("Inside shutdown, con2infoMap size=" + con2infoMap.size)
    this.maxConnection = 0
    val cons = con2infoMap.keySet
    if (cons != null && cons.size > 0) { // make a copy since we need to modify the underlying Map
      val copy = new util.HashSet[ChannelSftp](cons)
      // Initiate disconnect from all outstanding connections
      import scala.collection.JavaConversions._
      for (con <- copy) {
        try disconnect(con)
        catch {
          case ioe: IOException =>
            val info = con2infoMap.get(con)
            SFTPConnectionPool.LOG
              .error("Error encountered while closing connection to " + info.getHost, ioe)
        }
      }
    }
    // make sure no further connections can be returned.
    this.idleConnections = null
    this.con2infoMap = null
  }
  def getMaxConnection: Int                = maxConnection
  def setMaxConnection(maxConn: Int): Unit = this.maxConnection = maxConn
  @throws[IOException]
  def connect(
      host: String,
      port: Int,
      user: String,
      password: String,
      keyFile: String
  ): ChannelSftp = { // get connection from pool
    val info    = new ConnectionInfo(host, port, user)
    var channel = getFromPool(info)
    if (channel != null)
      if (channel.isConnected) return channel
      else {
        channel = null
        this.synchronized {
          liveConnectionCount -= 1
          con2infoMap.remove(channel)
        }
      }
    // create a new connection and add to pool
    val jsch                             = new JSch
    var session: com.jcraft.jsch.Session = null
    try {
      val user2: String =
        if (user == null || user.length == 0) System.getProperty("user.name") else user
      val password2: String = if (password == null) "" else password
      if (keyFile != null && keyFile.length > 0) jsch.addIdentity(keyFile)
      if (port <= 0) session = jsch.getSession(user2, host)
      else session = jsch.getSession(user2, host, port)
      session.setPassword(password2)
      val config = new Properties
      config.put("StrictHostKeyChecking", "no")
      session.setConfig(config)
      session.connect()
      channel = session.openChannel("sftp").asInstanceOf[ChannelSftp]
      channel.connect()
      this synchronized con2infoMap.put(channel, info)
      liveConnectionCount += 1

      channel
    } catch {
      case e: JSchException =>
        throw new IOException(StringUtils.stringifyException(e))
    }
  }
  @throws[IOException]
  private[sftpwithseek] def disconnect(channel: ChannelSftp): Unit = {
    if (channel != null) { // close connection if too many active connections
      var closeConnection = false
      this.synchronized {
        if (liveConnectionCount > maxConnection) {
          liveConnectionCount -= 1
          con2infoMap.remove(channel)
          closeConnection = true
        }
      }

      if (closeConnection) if (channel.isConnected) try {
        val session = channel.getSession
        channel.disconnect()
        session.disconnect()
      } catch {
        case e: JSchException =>
          throw new IOException(StringUtils.stringifyException(e))
      } else returnToPool(channel)
    }
  }
  def getIdleCount: Int     = this.idleConnections.size
  def getLiveConnCount: Int = this.liveConnectionCount
  def getConnPoolSize: Int  = this.con2infoMap.size
}
