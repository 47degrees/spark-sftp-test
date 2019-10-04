package org.apache.hadoop.fs.sftpwithseek

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp._
import com.jcraft.jsch.SftpATTRS
import com.jcraft.jsch.SftpException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.fs.FSDataOutputStream
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.fs.permission.FsPermission
import org.apache.hadoop.util.Progressable
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.net.URLDecoder
import java.util
import scala.util.control.Breaks._

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
/** SFTP FileSystem.
 * This class and its dependency classes were copied from hadoop-2.8 to support SFTP FileSystem so that FTP batch source
 * can fetch data from SFTP server. Once CDAP-5826 is addressed these classes can be removed.
 * */
object SFTPFileSystem {
  val LOG: Log                       = LogFactory.getLog(classOf[SFTPFileSystem])
  private val DEFAULT_SFTP_PORT      = 22
  private val DEFAULT_MAX_CONNECTION = 5
  val DEFAULT_BUFFER_SIZE: Int       = 1024 * 1024
  val DEFAULT_BLOCK_SIZE: Int        = 4 * 1024
  val FS_SFTP_USER_PREFIX            = "fs.sftp.user."
  val FS_SFTP_PASSWORD_PREFIX        = "fs.sftp.password."
  val FS_SFTP_HOST                   = "fs.sftp.host"
  val FS_SFTP_HOST_PORT              = "fs.sftp.host.port"
  val FS_SFTP_KEYFILE                = "fs.sftp.keyfile"
  val FS_SFTP_CONNECTION_MAX         = "fs.sftp.connection.max"
  val E_SAME_DIRECTORY_ONLY          = "only same directory renames are supported"
  val E_HOST_NULL                    = "Invalid host specified"
  val E_USER_NULL                    = "No user specified for sftp connection. Expand URI or credential file."
  val E_PATH_DIR                     = "Path %s is a directory."
  val E_FILE_STATUS                  = "Failed to get file status"
  val E_FILE_NOTFOUND                = "File %s does not exist."
  val E_FILE_EXIST                   = "File already exists: %s"
  val E_CREATE_DIR                   = "create(): Mkdirs failed to create: %s"
  val E_DIR_CREATE_FROMFILE          = "Can't make directory for path %s since it is a file."
  val E_MAKE_DIR_FORPATH             = "Can't make directory for path \"%s\" under \"%s\"."
  val E_DIR_NOTEMPTY                 = "Directory: %s is not empty."
  val E_FILE_CHECK_FAILED            = "File check failed"
  val E_NOT_SUPPORTED                = "Not supported"
  val E_SPATH_NOTEXIST               = "Source path %s does not exist"
  val E_DPATH_EXIST                  = "Destination path %s already exist, cannot rename!"
  val E_FAILED_GETHOME               = "Failed to get home directory"
  val E_FAILED_DISCONNECT            = "Failed to disconnect"
}
class SFTPFileSystem extends FileSystem {
  private var connectionPool: SFTPConnectionPool = null
  private var uri: URI                           = null

  /**
   * Set configuration from UI.
   *
   * @param uriInfo
   * @param conf
   * @throws IOException
   */ @throws[IOException]
  private def setConfigurationFromURI(uriInfo: URI, conf: Configuration): Unit = { // get host information from URI
    var host = uriInfo.getHost
    host = if (host == null) conf.get(SFTPFileSystem.FS_SFTP_HOST, null) else host
    if (host == null) throw new IOException(SFTPFileSystem.E_HOST_NULL)
    conf.set(SFTPFileSystem.FS_SFTP_HOST, host)
    var port = uriInfo.getPort
    port =
      if (port == -(1))
        conf.getInt(SFTPFileSystem.FS_SFTP_HOST_PORT, SFTPFileSystem.DEFAULT_SFTP_PORT)
      else port
    conf.setInt(SFTPFileSystem.FS_SFTP_HOST_PORT, port)
    // get user/password information from URI
    val userAndPwdFromUri = uriInfo.getUserInfo
    if (userAndPwdFromUri != null) {
      val userPasswdInfo = userAndPwdFromUri.split(":")
      var user           = userPasswdInfo(0)
      user = URLDecoder.decode(user, "UTF-8")
      conf.set(SFTPFileSystem.FS_SFTP_USER_PREFIX + host, user)
      if (userPasswdInfo.length > 1)
        conf.set(SFTPFileSystem.FS_SFTP_PASSWORD_PREFIX + host + "." + user, userPasswdInfo(1))
    }
    val user = conf.get(SFTPFileSystem.FS_SFTP_USER_PREFIX + host)
    if (user == null || user == "") throw new IllegalStateException(SFTPFileSystem.E_USER_NULL)
    val connectionMax =
      conf.getInt(SFTPFileSystem.FS_SFTP_CONNECTION_MAX, SFTPFileSystem.DEFAULT_MAX_CONNECTION)
    connectionPool = new SFTPConnectionPool(connectionMax)
  }

  /**
   * Connecting by using configuration parameters.
   *
   * @return An FTPClient instance
   * @throws IOException
   */ @throws[IOException]
  private def connect: ChannelSftp = {
    val conf                 = getConf
    val host                 = conf.get(SFTPFileSystem.FS_SFTP_HOST, null)
    val port                 = conf.getInt(SFTPFileSystem.FS_SFTP_HOST_PORT, SFTPFileSystem.DEFAULT_SFTP_PORT)
    val user                 = conf.get(SFTPFileSystem.FS_SFTP_USER_PREFIX + host, null)
    val pwd                  = conf.get(SFTPFileSystem.FS_SFTP_PASSWORD_PREFIX + host + "." + user, null)
    val keyFile              = conf.get(SFTPFileSystem.FS_SFTP_KEYFILE, null)
    val channel: ChannelSftp = connectionPool.connect(host, port, user, pwd, keyFile)
    channel
  }

  /**
   * Logout and disconnect the given channel.
   *
   * @param channel
   * @throws IOException
   */ @throws[IOException]
  private def disconnect(channel: ChannelSftp): Unit = connectionPool.disconnect(channel)

  /**
   * Resolve against given working directory.
   *
   * @param workDir
   * @param path
   * @return absolute path
   */
  private def makeAbsolute(workDir: Path, path: Path): Path = {
    if (path.isAbsolute) return path
    new Path(workDir, path)
  }

  /**
   * Convenience method, so that we don't open a new connection when using this
   * method from within another method. Otherwise every API invocation incurs
   * the overhead of opening/closing a TCP connection.
   * @throws IOException
   */ @throws[IOException]
  private def exists(channel: ChannelSftp, file: Path) =
    try {
      getFileStatus(channel, file)
      true
    } catch {
      case fnfe: FileNotFoundException =>
        false
      case ioe: IOException =>
        throw new IOException(SFTPFileSystem.E_FILE_STATUS, ioe)
    }

  /**
   * Convenience method, so that we don't open a new connection when using this
   * method from within another method. Otherwise every API invocation incurs
   * the overhead of opening/closing a TCP connection.
   */ @SuppressWarnings(Array("unchecked")) @throws[IOException]
  private def getFileStatus(client: ChannelSftp, file: Path): FileStatus = {
    var fileStat: FileStatus = null
    var workDir: Path        = null
    try workDir = new Path(client.pwd)
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val absolute   = makeAbsolute(workDir, file)
    val parentPath = absolute.getParent
    if (parentPath == null) { // root directory
      val length           = -1 // Length of root directory on server not known
      val isDir            = true
      val blockReplication = 1
      val blockSize        = SFTPFileSystem.DEFAULT_BLOCK_SIZE // Block Size not known.
      val modTime          = -1 // Modification time of root directory not known.
      val root             = new Path("/")
      return new FileStatus(
        length,
        isDir,
        blockReplication,
        blockSize,
        modTime,
        root.makeQualified(this.getUri, this.getWorkingDirectory)
      )
    }
    val pathName                                    = parentPath.toUri.getPath
    var sftpFiles: util.Vector[ChannelSftp#LsEntry] = null
    try sftpFiles = client.ls(pathName).asInstanceOf[util.Vector[ChannelSftp#LsEntry]]
    catch {
      case e: SftpException =>
        throw new FileNotFoundException(String.format(SFTPFileSystem.E_FILE_NOTFOUND, file))
    }
    if (sftpFiles != null) {
      import scala.collection.JavaConversions._
      breakable {
        for (sftpFile <- sftpFiles) {
          if (sftpFile.getFilename == file.getName) { // file found in directory
            fileStat = getFileStatus(client, sftpFile, parentPath)
            break //todo: break is not supported
          }
        }
      }
      if (fileStat == null)
        throw new FileNotFoundException(String.format(SFTPFileSystem.E_FILE_NOTFOUND, file))
    } else throw new FileNotFoundException(String.format(SFTPFileSystem.E_FILE_NOTFOUND, file))
    fileStat
  }

  /**
   * Convert the file information in LsEntry to a {@link FileStatus} object. *
   *
   * @param sftpFile
   * @param parentPath
   * @return file status
   * @throws IOException
   */ @throws[IOException]
  private def getFileStatus(
      channel: ChannelSftp,
      sftpFile: ChannelSftp#LsEntry,
      parentPath: Path
  ): FileStatus = {
    val attr   = sftpFile.getAttrs
    var length = attr.getSize
    var isDir  = attr.isDir
    val isLink = attr.isLink
    if (isLink) {
      var link = parentPath.toUri.getPath + "/" + sftpFile.getFilename
      try {
        link = channel.realpath(link)
        val linkParent        = new Path("/", link)
        val fstat: FileStatus = getFileStatus(channel, linkParent)
        isDir = fstat.isDirectory
        length = fstat.getLen
      } catch {
        case e: Exception =>
          throw new IOException(e)
      }
    }
    val blockReplication = 1
    // Using default block size since there is no way in SFTP channel to know of
    // block sizes on server. The assumption could be less than ideal.
    val blockSize  = SFTPFileSystem.DEFAULT_BLOCK_SIZE
    val modTime    = attr.getMTime * 1000 // convert to milliseconds
    val accessTime = 0
    val permission = getPermissions(sftpFile)
    // not be able to get the real user group name, just use the user and group
    // id
    val user     = Integer.toString(attr.getUId)
    val group    = Integer.toString(attr.getGId)
    val filePath = new Path(parentPath, sftpFile.getFilename)
    new FileStatus(
      length,
      isDir,
      blockReplication,
      blockSize,
      modTime,
      accessTime,
      permission,
      user,
      group,
      filePath.makeQualified(this.getUri, this.getWorkingDirectory)
    )
  }

  /**
   * Return file permission.
   *
   * @param sftpFile
   * @return file permission
   */
  private def getPermissions(sftpFile: ChannelSftp#LsEntry) =
    new FsPermission(sftpFile.getAttrs.getPermissions.toShort)
  @throws[IOException]
  private def mkdirs(client: ChannelSftp, file: Path, permission: FsPermission): Boolean = {
    var created       = true
    var workDir: Path = null
    try {
      workDir = new Path(client.pwd)
    } catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val absolute = makeAbsolute(workDir, file)
    val pathName = absolute.getName
    if (!exists(client, absolute)) {
      val parent = absolute.getParent
      created = parent == null || mkdirs(client, parent, FsPermission.getDefault)
      if (created) {
        val parentDir = parent.toUri.getPath
        val succeeded = true
        try {
          client.cd(parentDir)
          client.mkdir(pathName)
        } catch {
          case e: SftpException =>
            throw new IOException(
              String.format(SFTPFileSystem.E_MAKE_DIR_FORPATH, pathName, parentDir)
            )
        }
        created = created & succeeded
      }
    } else if (isFile(client, absolute))
      throw new IOException(String.format(SFTPFileSystem.E_DIR_CREATE_FROMFILE, absolute))
    created
  }
  @throws[IOException]
  private def isFile(channel: ChannelSftp, file: Path) =
    try !getFileStatus(channel, file).isDirectory
    catch {
      case e: FileNotFoundException =>
        false // file does not exist

      case ioe: IOException =>
        throw new IOException(SFTPFileSystem.E_FILE_CHECK_FAILED, ioe)
    }
  @throws[IOException]
  private def delete(channel: ChannelSftp, file: Path, recursive: Boolean): Boolean = {
    var workDir: Path = null
    try workDir = new Path(channel.pwd)
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val absolute             = makeAbsolute(workDir, file)
    val pathName             = absolute.toUri.getPath
    var fileStat: FileStatus = null
    try fileStat = getFileStatus(channel, absolute)
    catch {
      case e: FileNotFoundException =>
        // file not found, no need to delete, return true
        return false
    }
    if (!fileStat.isDirectory) {
      var status = true
      try channel.rm(pathName)
      catch {
        case e: SftpException =>
          status = false
      }
      status
    } else {
      var status     = true
      val dirEntries = listStatus(channel, absolute)
      if (dirEntries != null && dirEntries.length > 0) {
        if (!recursive) throw new IOException(String.format(SFTPFileSystem.E_DIR_NOTEMPTY, file))
        var i = 0
        while ({ i < dirEntries.length }) {
          delete(channel, new Path(absolute, dirEntries(i).getPath), recursive)

          { i += 1; i }
        }
      }
      try channel.rmdir(pathName)
      catch {
        case e: SftpException =>
          status = false
      }
      status
    }
  }
  @SuppressWarnings(Array("unchecked")) @throws[IOException]
  private def listStatus(client: ChannelSftp, file: Path): Array[FileStatus] = {
    var workDir: Path = null
    try workDir = new Path(client.pwd)
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val absolute = makeAbsolute(workDir, file)
    val fileStat = getFileStatus(client, absolute)
    if (!fileStat.isDirectory) return Array[FileStatus](fileStat)
    var sftpFiles: util.Vector[ChannelSftp#LsEntry] = null
    try sftpFiles = client.ls(absolute.toUri.getPath).asInstanceOf[util.Vector[ChannelSftp#LsEntry]]
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val fileStats = new util.ArrayList[FileStatus]
    var i         = 0
    while ({ i < sftpFiles.size }) {
      val entry = sftpFiles.get(i)
      val fname = entry.getFilename
      // skip current and parent directory, ie. "." and ".."
      if (!".".equalsIgnoreCase(fname) && !"..".equalsIgnoreCase(fname))
        fileStats.add(getFileStatus(client, entry, absolute))

      { i += 1; i - 1 }
    }
    fileStats.toArray(new Array[FileStatus](fileStats.size))
  }

  /**
   * Convenience method, so that we don't open a new connection when using this
   * method from within another method. Otherwise every API invocation incurs
   * the overhead of opening/closing a TCP connection.
   *
   * @param channel
   * @param src
   * @param dst
   * @return rename successful?
   * @throws IOException
   */ @throws[IOException]
  private def rename(channel: ChannelSftp, src: Path, dst: Path) = {
    var workDir: Path = null
    try workDir = new Path(channel.pwd)
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val absoluteSrc = makeAbsolute(workDir, src)
    val absoluteDst = makeAbsolute(workDir, dst)
    if (!exists(channel, absoluteSrc))
      throw new IOException(String.format(SFTPFileSystem.E_SPATH_NOTEXIST, src))
    if (exists(channel, absoluteDst))
      throw new IOException(String.format(SFTPFileSystem.E_DPATH_EXIST, dst))
    var renamed = true
    try {
      channel.cd("/")
      channel.rename(src.toUri.getPath, dst.toUri.getPath)
    } catch {
      case e: SftpException =>
        renamed = false
    }
    renamed
  }
  @throws[IOException]
  override def initialize(uriInfo: URI, conf: Configuration): Unit = {
    super.initialize(uriInfo, conf)
    setConfigurationFromURI(uriInfo, conf)
    setConf(conf)
    this.uri = uriInfo
  }
  override def getScheme   = "sftp"
  override def getUri: URI = uri
  @throws[IOException]
  override def open(f: Path, bufferSize: Int): FSDataInputStream = {
    val channel       = connect
    var workDir: Path = null
    try workDir = new Path(channel.pwd)
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    var absolute = makeAbsolute(workDir, f)
    val fileStat = getFileStatus(channel, absolute)
    if (fileStat.isDirectory) {
      disconnect(channel)
      throw new IOException(String.format(SFTPFileSystem.E_PATH_DIR, f))
    }
    var is: InputStream = null
    try { // the path could be a symbolic link, so get the real path
      absolute = new Path("/", channel.realpath(absolute.toUri.getPath))
      is = channel.get(absolute.toUri.getPath)
    } catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val fis = new FSDataInputStream(new SFTPInputStream(is, channel, statistics))
    fis
  }

  /**
   * A stream obtained via this call must be closed before using other APIs of
   * this class or else the invocation will block.
   */ @throws[IOException]
  override def create(
      f: Path,
      permission: FsPermission,
      overwrite: Boolean,
      bufferSize: Int,
      replication: Short,
      blockSize: Long,
      progress: Progressable
  ): FSDataOutputStream = {
    val client        = connect
    var workDir: Path = null
    try workDir = new Path(client.pwd)
    catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val absolute = makeAbsolute(workDir, f)
    if (exists(client, f))
      if (overwrite) delete(client, f, false)
      else {
        disconnect(client)
        throw new IOException(String.format(SFTPFileSystem.E_FILE_EXIST, f))
      }
    var parent = absolute.getParent
    if (parent == null || !mkdirs(client, parent, FsPermission.getDefault)) {
      parent = if (parent == null) new Path("/") else parent
      disconnect(client)
      throw new IOException(String.format(SFTPFileSystem.E_CREATE_DIR, parent))
    }
    var os: OutputStream = null
    try {
      client.cd(parent.toUri.getPath)
      os = client.put(f.getName)
    } catch {
      case e: SftpException =>
        throw new IOException(e)
    }
    val fos = new FSDataOutputStream(os, statistics) {
      @throws[IOException]
      override def close(): Unit = {
        super.close()
        disconnect(client)
      }
    }
    fos
  }
  @throws[IOException]
  override def append(f: Path, bufferSize: Int, progress: Progressable) =
    throw new IOException(SFTPFileSystem.E_NOT_SUPPORTED)
  /*
   * The parent of source and destination can be different. It is suppose to
   * work like 'move'
   */ @throws[IOException]
  override def rename(src: Path, dst: Path): Boolean = {
    val channel = connect
    try {
      val success = rename(channel, src, dst)
      success
    } finally disconnect(channel)
  }
  @throws[IOException]
  override def delete(f: Path, recursive: Boolean): Boolean = {
    val channel = connect
    try {
      val success = delete(channel, f, recursive)
      success
    } finally disconnect(channel)
  }
  @throws[IOException]
  override def listStatus(f: Path): Array[FileStatus] = {
    val client = connect
    try {
      val stats = listStatus(client, f)
      stats
    } finally disconnect(client)
  }
  override def setWorkingDirectory(newDir: Path): Unit = {
    // we do not maintain the working directory state
  }
  override def getWorkingDirectory: Path = // Return home directory always since we do not maintain state.
    getHomeDirectory
  override def getHomeDirectory: Path = {
    var channel: ChannelSftp = null
    try {
      channel = connect
      val homeDir: Path = new Path(channel.pwd)
      homeDir
    } catch {
      case ioe: Exception =>
        null
    } finally try disconnect(channel)
    catch {
      case ioe: IOException =>
        return null
    }
  }
  @throws[IOException]
  override def mkdirs(f: Path, permission: FsPermission): Boolean = {
    val client = connect
    try {
      val success = mkdirs(client, f, permission)
      success
    } finally disconnect(client)
  }
  @throws[IOException]
  override def getFileStatus(f: Path): FileStatus = {
    val channel = connect
    try {
      val status = getFileStatus(channel, f)
      status
    } finally disconnect(channel)
  }
}
