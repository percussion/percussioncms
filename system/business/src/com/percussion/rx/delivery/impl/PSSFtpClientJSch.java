/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.delivery.impl;

import static org.apache.commons.lang.Validate.notEmpty;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.percussion.server.PSServer;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A wrapper for the {@link ChannelSftp} class. This code was integrated from 
 * PSO, <code>com.percussion.consulting.publisher.client.PSOSFtpClientJSch</code>
 */
public class PSSFtpClientJSch
{
   /**
    * The logger of the class.
    */
   private static final Logger ms_log = LogManager.getLogger(PSSFtpClientJSch.class);

    protected static final String JSCH_KEX = "jsch.kex";

    /**
    * The JSch object used to create a session, initialized by the default
    * constructor, never modified after that.
    */
   private JSch m_jsch;
   
   /**
    * The session used to connect to the SSH and SFTP server. Initialized by
    * {@link # login(String, int, String, String, int)}, reset by
    * {@link #logoff()}.
    */
   private Session m_session;
   
   /**
    * The SFTP client, initialized by
    * {@link # login(String, int, String, String, int)}, reset by
    * {@link #logoff()}.
    */
   private ChannelSftp m_channel; 
   
   /**
    * The umask used to control the permissions of created files and 
    * directories. Default to <code>null</code> as undefined.
    */
   private Integer m_umask = null;
   
   /**
    * Default constructor.
    */
   public PSSFtpClientJSch()
   {
      this.m_jsch = new JSch();
      JSch.setConfig("StrictHostKeyChecking", "no");
       setJSCHKexConfig(getJSCHKexProperty());
   }

   /**
    * Connect to the specified server with the given connection info.
    * 
    * @param host the server name or IP, not <code>null</code> or empty.
    * @param port the SSH port. It's default port is 22. 
    * @param userName the user name used to connect to the server,
    *    not <code>null</code> or empty.
    * @param password the password of the above user, not <code>null</code> or 
    *    empty.
    * @param timeout the timeout of the connection in milliseconds. It may be 
    *    <code>-1</code> if the timeout is undefined.
    *    
    * @throws JSchException if failed to connect to the server.
    */
   public void login(String host, int port, String userName, String password,
         Integer timeout, Integer retries) throws JSchException
   {
      if (timeout == null)
         timeout = 10000;

   ms_log.debug("Attempting to login with the following parameters: host-> " + host + " port: "
           + port + " userName: " + userName);

      loginWithPasswordOrKey(host, port, userName, password, null, timeout, retries);
   }


   /**
    * Connect to the specified server with the given connection info using
    * key exchange
    * 
    * @param host the server name or IP, not <code>null</code> or empty.
    * @param port the SSH port. It's default port is 22. 
    * @param userName the user name used to connect to the server,
    *    not <code>null</code> or empty.
    * @param privateKeyFile - name of the file with private key
    *    not <code>null</code> or empty. example: /home/client/.ssh/id_dsa
    * @param timeout the timeout of the connection in milliseconds. It may be 
    *    <code>-1</code> if the timeout is undefined.
    * @param retries 
    *    
    * @throws JSchException if failed to connect to the server.
    */
   public void loginKeyExchange(String host, int port, String userName,
                                String privateKeyFile, int timeout, Integer retries) throws JSchException
   {
      notEmpty(privateKeyFile, "privateKeyFile must not be empty.");
      File keyFile = new File(privateKeyFile);
      if (!keyFile.exists())
          throw new IllegalArgumentException("The private key file, \"" + privateKeyFile + "\", does not exist.");
      
      loginWithPasswordOrKey(host, port, userName, null, privateKeyFile, timeout, retries);
   }

   /**
    * Connect to the specified server with the user/password or user/key pair.
    * 
    * @param host the server name or IP, not <code>null</code> or empty.
    * @param port the SSH port. It's default port is 22. 
    * @param userName the user name used to connect to the server,
    *    not <code>null</code> or empty.
    * @param password the password of the above user. It may be <code>null</code>
    *   if login with the user name and private key, but not with password. 
    * @param keyFile the absolute path of the private key file. It may be <code>null</code>
    *   if login with the user name and password, but not with password. 
    * @param timeout the timeout of the connection in milliseconds. It may be 
    *    <code>-1</code> if the timeout is undefined.
    * @param retries 
    *    
    * @throws JSchException if failed to connect to the server.
    */
   private void loginWithPasswordOrKey(String host, int port, String userName, String password,
         String keyFile, int timeout, Integer retries) throws JSchException
   {
      ms_log.debug("logging in"); 

      m_session = m_jsch.getSession(userName, host, port);
      if (keyFile != null)
          m_jsch.addIdentity(keyFile);
      else
         m_session.setPassword(password);

       if(timeout != -1) {
           m_session.setTimeout(timeout);
       }

       m_session.connect();

      m_channel = (ChannelSftp) m_session.openChannel("sftp"); 
      m_channel.connect();
   }

   
   /**
    * Set the umask to control the permissions of the created files and
    * directories.
    * @param umask a 3-character string of octal digits, never <code>null</code>
    *    or empty.
    */
   public void setUmask(String umask)
   {
      if (!PSSFtpDeliveryHandler.isValidUmask(umask))
      {
         throw new IllegalArgumentException("Invalid umask: " + umask);
      }
      
      m_umask = Integer.parseInt(umask, 8);
   }
   
   /**
    * Disconnect to the remote SSH and SFTP server. 
    */
   public void logoff()
   {
      ms_log.debug("logging off");
      
      if(m_channel != null)
      {
         m_channel.disconnect();
         m_channel = null;
      }
      if(m_session != null)
      {
         m_session.disconnect();
         m_session = null;
      }
   }

   /**
    * Puts a file to the remote location. A wrapper of
    * {@link ChannelSftp#put(InputStream, String)}. Must call
    * {@link # login(String, int, String, String, int)} first.
    * 
    * @param in the file data. Must not be <code>null</code>.
    * @param fileName the remote file name. Must not be <code>null</code> or
    * <code>empty</code>. May contain forward slashes as directory
    * separators. Assumed the directories are exist if the file name contains
    * any directories which is relative to the current directory.
    * 
    * @throws SftpException if failed to put the file.
    */
   public void put(InputStream in, String fileName) throws SftpException
   {
      if (in == null)
         throw new IllegalArgumentException("Input stream may not be null.");
      if (StringUtils.isBlank(fileName))
         throw new IllegalArgumentException(
               "fileName may not be null or empty.");
      
      if (ms_log.isDebugEnabled())
         ms_log.debug("put - name=" + fileName);
      
      checkChannel();
      SftpATTRS attrs = getFileAttrs(fileName);
      m_channel.put(in, fileName);
      
      // set permissions for created file only
      if (attrs == null)
         setPermissions(fileName, null, true);
   }

   /**
    * Change directory to the specified path, a wrapper of
    * {@link ChannelSftp#cd(String)}
    * 
    * @param path the directory to change to, assumed it already exists, 
    *    never <code>null</code> or empty.
    *    
    * @throws SftpException if failed to change to the direcotry.
    */
   public void cd(String path) throws SftpException
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path may not be null or empty.");
      
      if (ms_log.isDebugEnabled())
         ms_log.debug("change directory to: "  + path); 

      checkChannel();
      m_channel.cd(path);
   }
   
   /**
    * Gets the current directory path, wrapper of {@link ChannelSftp#pwd()}.
    * 
    * @return the current directory, never <code>null</code> or empty.
    * 
    * @throws SftpException if failed to get the current directory.
    */
   public String pwd() throws SftpException
   {
      checkChannel();
      String currDir = m_channel.pwd();

      if (ms_log.isDebugEnabled())
         ms_log.debug("current dir: "  + currDir); 

      return currDir;
}

   /**
    * Removes a specified file, wrapper of {@link ChannelSftp#rm(String)}.
    * 
    * @param filename the name or path of the to be removed file, never 
    *    <code>null</code> or empty.
    *    
    * @throws SftpException if failed to remove the file.
    */
   public void rm(String filename) throws SftpException
   {
      if (StringUtils.isBlank(filename))
         throw new IllegalArgumentException(
               "filename may not be null or empty.");

      if (ms_log.isDebugEnabled())
         ms_log.debug("removing file: "  + filename); 
      
      checkChannel();
      m_channel.rm(filename);      
   }
   
   /**
    * Removes a specified directory, wrapper of {@link ChannelSftp#rmdir(String)}.
    * 
    * @param dirname the name or path of the to be removed directory, never 
    *    <code>null</code> or empty.
    *    
    * @throws SftpException if failed to remove the file.
    */
   public void rmdir(String dirname) throws SftpException
   {
      if (StringUtils.isBlank(dirname))
         throw new IllegalArgumentException(
               "dirname may not be null or empty.");

      if (ms_log.isDebugEnabled())
         ms_log.debug("removing directory: "  + dirname); 
      
      checkChannel();
      m_channel.rmdir(dirname);      
   }
   
   /**
    * Gets a list of files or directories for the specified directory, wrapper of {@link ChannelSftp#ls(String)}.
    * 
    * @param dirname the name or path of the to be removed directory, never 
    *    <code>null</code> or empty.
    *    
    * @throws SftpException if failed to remove the file.
    */
   public Vector ls(String dirname) throws SftpException
   {
      if (StringUtils.isBlank(dirname))
         throw new IllegalArgumentException(
               "dirname may not be null or empty.");

      if (ms_log.isDebugEnabled())
         ms_log.debug("ls: "  + dirname); 
      
      checkChannel();
      return m_channel.ls(dirname);      
   }   
   
   /**
    * Makes directories for the specified path.
    * 
    * @param fullpath the path that is relative to the current directory, not
    *    <code>null</code> or empty.
    * @return
    * @throws SftpException
    */
   public boolean mkdirs(String fullpath) throws SftpException
   {
      if (StringUtils.isBlank(fullpath))
         throw new IllegalArgumentException(
               "fullpath may not be null or empty.");
      
      if (ms_log.isDebugEnabled())
         ms_log.debug("mkdirs path: "  + fullpath); 

      // remove leading slash '/'
      boolean isAbsolutePath = fullpath.startsWith("/");
      if (isAbsolutePath)
         fullpath = fullpath.substring(1);

      checkChannel();
      String[] pathElements = fullpath.split(FILE_SEPARATORS_REGEX);
      if(pathElements.length < 2)
      { 
         //just a file name, no path part.
         return true; 
      }
      if (isAbsolutePath)
      {
 
         List<String> aList = new ArrayList<>();
         aList.add("/");
         aList.addAll(Arrays.asList(pathElements));
         String[] aaList = new String[aList.size()];
         pathElements = aList.toArray(aaList);
      }
      
      return makePath("", pathElements[0], 0, pathElements);
   }

   /**
    * The pattern used to convert path to array of directory names.
    */
   private static final String FILE_SEPARATORS_REGEX = "[\\/]"; 

   /**
    * Similar with {@link #mkdirs(String)}, except this makes one directory
    * and recursively call itself to make the next one.
    *  
    * @param leadingpath the leading path that is relative to the current 
    *    directory, assumed not <code>null</code>, but may be empty. Assumed
    *    this directory has already been created if not empty.
    * @param directoryName the name of the directory to be created if not exists
    *    assumed not <code>null</code> or empty.
    * @param ndx the index of the <code>directoryName</code> in 
    *    <code>pathElements</code>. It must be <code>0</code> initially.
    * @param pathElements the array of directories to be created if not exist,
    *    assumed not <code>null</code> or empty.
    * 
    * @return <code>true</code> if all directories are either exist or created.
    * 
    * @throws SftpException if failed to create or validate the existance of 
    *    the directories.
    */
   private boolean makePath(String leadingpath, String directoryName, int ndx,
         String[] pathElements) throws SftpException
   {
       if(leadingpath.length() > 0) {
           leadingpath += "/";
       }

      String localpath = leadingpath + directoryName; 
      SftpATTRS dirAttrs = getFileAttrs(localpath); 
      if(dirAttrs == null)
      {
         if (ms_log.isDebugEnabled())
            ms_log.debug("creating directory: " + localpath);
         
         m_channel.mkdir(localpath);
         dirAttrs = getFileAttrs(localpath);
         // set permissions for created directory only
         setPermissions(localpath, dirAttrs, false);
      }
      if(!dirAttrs.isDir())
      {
         ms_log.error("not a directory, cannot add directories:  " + localpath); 
         return false; 
      }
      if(ndx < pathElements.length - 1)
      {
         ndx++; 
         String nextPath = pathElements[ndx];
         if(nextPath.equals("..") || nextPath.equals("."))
         {
            String msg = "Paths with dots not supported for SFTP";
            ms_log.error(msg);
            return false; 
         }
         return makePath(localpath, nextPath, ndx, pathElements ); 
      }
      return true; 
   }

   /**
    * Tests if the client is connected to a server.
    * 
    * @return <code>true</code> if the client is connected.
    */
   public boolean isAlive()
   {
      if(m_session == null) 
      {
         ms_log.debug("no SSH session"); 
         return false;
      }
      if(!m_session.isConnected())
      {
         ms_log.debug("session disconnected");
         return false; 
      }
      if(m_channel == null)
      {
         ms_log.debug("no SFTP channel"); 
         return false;
      }
      if(!m_channel.isConnected())
      {
         ms_log.debug("channel disconnected"); 
         return false; 
      }
      return true; 
   }
   
   /**
    * Gets the attribute for the specified file or directory.
    * 
    * @param path the path of a file or directory, not <code>null</code> or empty.
    *    
    * @return the attributes of the file or directory or <code>null</code> if the path does not exist.
    */
   public SftpATTRS getFileAttrs(String path)
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path may not be null or empty.");
      }
      
      try
      {
         SftpATTRS attrs = m_channel.stat(path);
         return attrs; 
      } 
      catch (SftpException ex)
      {
         if (ms_log.isDebugEnabled())
            ms_log.debug("got exception testing file " + path + " " + ex);
         
         return null;
      }
   }
   
   /**
    * Validates if the {@link # login(String, int, String, String, int)} is
    * called.
    */
   private void checkChannel()
   {
      if(m_channel == null || m_session == null)
      {
         throw new IllegalStateException(
               "The session and channel must be initialized correctly before use. ");
         
      }
   }

   /**
    * Sets the permission for the given file or directory according to the
    * specified umask. Do nothing if the umask is undefined.
    * 
    * @param filename the name of the file or directory in question, assumed
    *    not <code>null</code> or empty.
    * @param attrs the attributes of the given file or directory. It may be
    *    <code>null</code>.
    * @param isFile determines if the given name is a file or directory. It is
    *    <code>true</code> if set permission for a file; otherwise set 
    *    permission for a directory. 
    */
   private void setPermissions(String filename, SftpATTRS attrs, boolean isFile)
   {
      if(m_umask == null)
         return;

      int expected;
      if (isFile)
         expected = ~m_umask & 0666;  // files will not have execute bit added
      else
         expected = ~m_umask & 0777; // directories get the execute bit. 
      
      if (ms_log.isDebugEnabled())
         ms_log.debug("Set file permissions["
               + Integer.toOctalString(expected) + "] on: " + filename); 

      try 
      { 
         //note: we don't want to fail here.  
         if (attrs == null || ((attrs.getPermissions() & 0777) != expected))
            m_channel.chmod(expected, filename);  
      } 
      catch (Throwable t) 
      {
         //just log message and return success. 
         ms_log.warn("Error setting file permissions " + t.getMessage(), t);
      }
   }


    /**
     * Gets the ciphers specified in server.properties.
     * @return The ciphers, can be null.
     */
    public static String getJSCHKexProperty(){
        return PSServer.getProperty(JSCH_KEX);
    }

    /**
     * Set's the JSCH key exchange algorithms to use.
     *
     * @param kexAlgorithims Comma separated Key exchange algorithms. If null or blank string, it will not change.
     */
    public void setJSCHKexConfig(String kexAlgorithims) {
        if(StringUtils.isNotBlank(kexAlgorithims)){
            ms_log.debug("Using following algorithims for key exchange: " + kexAlgorithims);
            JSch.setConfig("kex", kexAlgorithims);
        }else {
            ms_log.debug("Server.properties was blank, using previously set algorithms for key exchange: " + JSch.getConfig("kex"));
        }
    }

    /**
     * @return Key Exchange algorithms configured for the SFTP client.
     */
    public String getJSCHKexConfig() {
        return JSch.getConfig("kex");
    }

    public boolean applySSHConfig(String configFile){
        boolean ret = false;
        try {
            ConfigRepository config = OpenSSHConfig.parseFile(configFile);
            this.m_jsch.setConfigRepository(config);
            ms_log.info("OpenSSH configuration from: " + configFile + " applied successfully.");
        } catch (IOException e) {
            ms_log.warn("An OpenSSH configuration was found at:" + configFile + ", but failed to be applied with error:" + e.getMessage());
            ms_log.debug("OpenSSH configuration failed with the following exception.",e);
        }

        return ret;

    }
}
