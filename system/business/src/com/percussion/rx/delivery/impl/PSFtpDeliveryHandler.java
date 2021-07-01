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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * A subclass of the file delivery handler that does the delivery to a remote
 * FTP server rather than the file system directly.
 * 
 * @author dougrand
 */
public class PSFtpDeliveryHandler extends PSBaseFtpDeliveryHandler
{
   /**
    * Logger.
    */
   private static final Logger ms_log = LogManager.getLogger(PSFtpDeliveryHandler.class);

   /**
    * Holds the per thread ftp client. Initialized in {@link #commit(long)} and
    * used in <code>doDelivery</code> and <code>doRemoval</code>
    */
   protected ThreadLocal<PSFtpClient> ms_ftp = new ThreadLocal<>();

   /**
    * See {@link #getTimeout()}
    */
   private int m_timeout = -1;
   
   /**
    * @see {@link #getUsePassiveMode()}.
    */
   private boolean m_usePassiveMode = false;
   
   /**
    * Wraps {@link FTPClient} to manage login and logout from the FTP Server.
    */
   private static class PSFtpClient extends FTPClient
   {
   // FB: SIC_THREADLOCAL_DEADLY_EMBRACE NC 1-17-16
      /**
       * Determines if the {@link #mi_ftp} has been logged in. It is 
       * <code>true</code> if it has been logged in. Defaults to 
       * <code>false</code>. It is set be {@link #login(FTPClient, long)} 
       * and reset by {@link #logout(FTPClient)}
       */
      private boolean mi_hasLogin = false;      
   }
   
   /**
    * Get the FTP client for use in this handler.
    * 
    * @return the FTP client, never <code>null</code>.
    */
   protected PSFtpClient getFtpClient()
   {
      PSFtpClient rval = ms_ftp.get();
      if (rval == null)
      {
         rval = new PSFtpClient();
         ms_ftp.set(rval);
      }
      return rval;
   }

   /**
    * Sets the socket timeout in milliseconds for both when opening a socket and
    * a currently open connection.
    * @param timeout the timeout in milliseconds.
    */
   public void setTimeout(int timeout)
   {
      m_timeout = timeout;
   }
   
   /**
    * Gets the socket timeout in milliseconds for both when opening a socket and
    * a currently open connection. Defaults to <code>-1</code> if not defined.
    */
   public int getTimeout()
   {
      return m_timeout;
   }

   /**
    * Sets whether to use passive or active mode for the FTP client.
    * 
    * @param usePassiveMode it is <code>true</code> if enable passive mode;
    * otherwise use active mode.
    * 
    * @see #getUsePassiveMode()
    */
   public void setUsePassiveMode(boolean usePassiveMode)
   {
      m_usePassiveMode = usePassiveMode;
   }
   
   /**
    * Determines if using passive or active mode for the FTP client. Defaults
    * to use active mode. If using passive mode is on, then it will also
    * disable the remote verification.
    *  
    * @return <code>true</code> if using passive mode is on; otherwise using
    * active mode.
    */
   public boolean getUsePassiveMode()
   {
      return m_usePassiveMode;
   }
   
   /**
    * Opens a socket connection from the given FTP client and the site.
    *  
    * @param ftp the FTP client, assumed not <code>null</code>.
    * @param site the target site, assumed not <code>null</code>.
    * 
    * @throws SocketException If the socket timeout could not be set.
    * @throws IOException If the socket could not be opened. In most cases you 
    * will only want to catch IOException since SocketException is derived from 
    * it. 
    */
   private void openSocketConnection(FTPClient ftp, String ipAddress, int port)
      throws SocketException, IOException
   {
      ftp.setDefaultPort(port);

      if (m_timeout != -1)
         ftp.setDefaultTimeout(m_timeout);

      ftp.setControlEncoding("UTF-8");
     
      ftp.connect(ipAddress);
              
      if (m_timeout != -1)
         ftp.setSoTimeout(m_timeout);
      
      if (m_timeout != -1)
         ms_log.debug("set socket timeout: " + m_timeout);
   }

   /**
    * Logs out the given FTP client.
    * @param ftp the FTP client, assumed not <code>null</code>.
    */
   private void logout(PSFtpClient ftp)
   {
      try
      {
         if (ftp.isConnected() && ftp.mi_hasLogin)
         {
            try
            {
               ftp.logout();
            }
            catch (IOException ex)
            {
               ms_log.error("Problem logout FTP", ex);
            }
         }

         if (ftp.isConnected())
         {
            ftp.disconnect();
         }
      }
      catch (IOException e)
      {
         ms_log.error("Problem closing ftp connection", e);
      }
      finally
      {
         ftp.mi_hasLogin = false;
         ms_ftp.remove();
      }
   }
   
   /**
    * Opens a connection and log in with the given FTP information for the 
    * supplied job ID.
    * 
    * @param ftp it contains information for opening socket connection and
    * log in FTP server. Assumed not <code>null</code>.
    * @param jobId the job ID.
    * @param failAll if <code>true</code>, then all items for the current
    * job will be marked as failed if the login was unsuccessful, otherwise,
    * a {@link PSDeliveryException} will be thrown in the event of a failed
    * connection, login, or if the ftp server could not be set to binary
    * mode.
    * 
    * @return error result if there is any. It is <code>null</code> if there
    * is no error.
    *    
    * @throws PSDeliveryException if <code>failAll</code> is
    * <code>false</code> and a connection could not be established.   
    */
   private Collection<IPSDeliveryResult> login(PSFtpClient ftp, long jobId,
         boolean failAll,  Integer connectionTimeout, Integer retries)
      throws PSDeliveryException
   {
      if (connectionTimeout != null && connectionTimeout != -1)
         ftp.setConnectTimeout(connectionTimeout);
      
      if (ftp.mi_hasLogin)
         throw new IllegalStateException("Unexpected FTP login state.");
      
      FTPLoginInfo info = new FTPLoginInfo(m_jobData.get(jobId));
      try
      {
         openSocketConnection(ftp, info.ipAddress, info.port);

         int reply = ftp.getReplyCode();
         if (!FTPReply.isPositiveCompletion(reply))
         {
            ftp.disconnect();
            
            return handleLoginError(jobId, "FTP server refused connection "
               + info.ipAddress, failAll);
         }
         if (!ftp.login(info.userName, info.password))
         {
            return handleLoginError(jobId, "FTP server could not authenticate "
               + "site credentials", failAll);
         }
         ftp.mi_hasLogin = true;
         
         if (!ftp.setFileType(FTP.BINARY_FILE_TYPE))
         {
            return handleLoginError(jobId,
                  "FTP server could not be set to binary mode", failAll);
         }
          
         ftp.setBufferSize(1024*1024);
         
         if(!FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF8 ON"))) { 
            // May only be a problem with Windows IIS FTP that does not follow spec and default to UTF-8
            ms_log.warn("Error sending 'OPTS UTF8 ON' to ftp server, may not be a problem if server default is UTF-8:" + ftp.getReplyString()); 
         }
         
         if (m_usePassiveMode)
         {
            ftp.enterLocalPassiveMode();
            ftp.setRemoteVerificationEnabled(false);
            
            if (ms_log.isDebugEnabled())
            {
               ms_log.debug("Entering passive mode.");
               ms_log.debug("Disabling Remote verification.");
            }
         }
      }
      catch (Exception e)
      {
         return handleLoginError(jobId, "Problem connecting to ftp server: "
               + e.getLocalizedMessage(), failAll);
      }

      return null;
   }
   
  /**
   * Check the FTP Connection 
   */
   @Override
   public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
   {
      boolean connected = false;
      PSFtpClient ftp = getFtpClient();
      FTPLoginInfo info = new FTPLoginInfo(pubServer, site);
      int timeout = 10000;
      if(this.m_timeout >= 0)
         timeout = this.m_timeout;
      try
      {
         ftp.setDefaultTimeout(timeout);
         
         ms_log.debug(String.format("Checking connection to %s on port %s with timeout %s",info.ipAddress, info.port, timeout));
         openSocketConnection(ftp, info.ipAddress, info.port);
         int reply = ftp.getReplyCode();
         if (!FTPReply.isPositiveCompletion(reply))
         {
            ms_log.error(String.format("Did not get positive reply from FTP Server: %s", info.ipAddress));
            ftp.disconnect();
            return false;
         }
         
         ms_log.debug(String.format("Authenticating to FTP Server with Username %s",info.userName));
         if (!ftp.login(info.userName, info.password))
         {
            ms_log.error(String.format("Authenticating to FTP Server with Username %s failed",info.userName));
      
            return false;
         }
         
         ftp.mi_hasLogin = true;
         connected = true;
      }
      catch (Exception e)
      {
         ms_log.error("FTP Connection Check Failed to connect", e);
         connected = false;
      }
      finally
      {
         logout(ftp);
      }

      return connected;
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#prepareForDelivery(long)
    */
   protected Collection<IPSDeliveryResult> prepareForDelivery(long jobId) throws PSDeliveryException
   {
      // initialize data 
      getFtpClient();

      return super.prepareForDelivery(jobId);
   }



   /* 
    * @see
    * com.percussion.rx.delivery.impl.PSBaseFtpDeliveryHandler#deliverItem(com
    * .percussion.rx.delivery.impl.PSBaseDeliveryHandler.Item, long,
    * java.lang.String)
    */
   @Override
   protected IPSDeliveryResult deliverItem(Item item, InputStream inputStream,
         long jobId, String location)
   {
      PSFtpClient ftp = getFtpClient();
      String currentWorkingDirectory = null;

      try
      {
         if (!ftp.mi_hasLogin)
         {
            // ftp client is not logged in, let's try
            login(ftp, jobId, false, null, null);
         }
         
         currentWorkingDirectory = getRootLocation(jobId, ftp);

         File file = new File(currentWorkingDirectory, location);
         String parentPath = canonicalPath(file.getParent());
         if (!ftp.changeWorkingDirectory(parentPath))
         {
            makeDirectories(ftp, file.getParentFile());
            if (!ftp.changeWorkingDirectory(parentPath))
            {
               return getItemResult(Outcome.FAILED, item, jobId,
                     "Could not create file directory: " + location);
            }
         }

         ftp.storeFile(file.getName(), inputStream);

         return new PSDeliveryResult(Outcome.DELIVERED, null, item.getId(),
               jobId, item.getReferenceId(), location.getBytes("UTF8"));
      }
      catch (Exception e)
      {
         return getItemResult(Outcome.FAILED, item, jobId,
               e.getLocalizedMessage());
      }
      finally
      {
         // no need to change working directory if publishing to absolute path
         if (!publishToAbsolutePath(m_jobData.get(jobId)))
         {
            changeDirectory(ftp, currentWorkingDirectory);
         }
      }
   }

   /**
    * Search up the paths until we find one and start creating the directories.
    * 
    * @param ftp the ftp client, assumed never <code>null</code>
    * @param dir the directory, assumed never <code>null</code>
    * @throws IOException
    */
   private void makeDirectories(FTPClient ftp, File dir) throws IOException
   {
      String path = canonicalPath(dir.toString());
      if (ftp.changeWorkingDirectory(path))
      {
         // exists
         return;
      }
      if (dir.getParentFile() != null)
      {
         makeDirectories(ftp, dir.getParentFile());
         String parent = canonicalPath(dir.getParent());
         if (!ftp.changeWorkingDirectory(parent))
         {
            throw new IOException("Could not create directory: "
                  + dir.toString());
         }
      }
      else
      {
         throw new IOException("Could not find a directory in original path");
      }
      if (!ftp.makeDirectory(dir.getName()))
      {
         throw new IOException("Could not create directory: " + dir.toString());
      }
   }

   @Override
   protected IPSDeliveryResult removeFileOrDir(Item item, long jobId, String location, boolean isFile)
   {
      PSFtpClient ftp = getFtpClient();

      // Put location in Unix path format
      File file = new File(location);
      
      String parentPath = file.getParent() == null ? null : canonicalPath(file.getParent());
      
      if (parentPath == null && !isFile)
         return null;
      
      String currentWorkingDirectory = null;
      try
      {
         currentWorkingDirectory = getRootLocation(jobId, ftp);

         if (!ftp.changeWorkingDirectory(parentPath))
         {
            // Directory doesn't exist, ergo file doesn't exist
            return isFile ? getItemResult(Outcome.DELIVERED, item, jobId, null) : null;
         }
         if (isFile)
         {
            ftp.deleteFile(file.getName());
         }
         else
         {
            if (isEmptyDirectory(file.getName(), ftp))
               ftp.removeDirectory(file.getName());
         }
         return isFile ? getItemResult(Outcome.DELIVERED, item, jobId, location) : null;
      }
      catch (IOException e)
      {
         ms_log.error("Error remove " + (isFile ? "file" : "directory") + ": \"" + location + "\"", e);
         return isFile ? getItemResult(Outcome.FAILED, item, jobId, e.getLocalizedMessage()) : null;
      }
      finally
      {
         if(!publishToAbsolutePath(m_jobData.get(jobId)))
         {
            changeDirectory(ftp, currentWorkingDirectory);
         }
      }      
   }

   /**
    * Changes current directory to the specified location.
    * @param ftp the handler, assumed not <code>null</code>.
    * @param currentWorkingDirectory the directory to change to, assumed not blank.
    */
   private void changeDirectory(FTPClient ftp, String currentWorkingDirectory)
   {
      if (isNotBlank(currentWorkingDirectory))
      {
         try
         {
            ftp.changeWorkingDirectory(currentWorkingDirectory);
         }
         catch (Exception e)
         {
            ms_log.error("Could not restore working directory: "
                  + currentWorkingDirectory, e);
         }
      }
   }

   @Override
   public Collection<IPSDeliveryResult> doLogin(long jobId, boolean failAll, Integer timeout, Integer retries)
      throws PSDeliveryException
   {
      return login(getFtpClient(), jobId, failAll, timeout, retries);
   }

   /**
    * Determines if the specified directory is empty.
    * @param dir the directory in question, assumed not blank.
    * @param ftp the ftp handler, assumed not <code>null</code>.
    * @return <code>true</code> if the directory is empty.
    * @throws IOException if error occurs.
    */
   private boolean isEmptyDirectory(String dir, FTPClient ftp) throws IOException
   {
      String[] files = ftp.listNames(dir);
      if (files == null || files.length == 0) // linux FTP server
         return true;

      // CoreFTP server in windows return "." & ".." for empty folder
      if (files.length != 2)
         return false;
      
      for (String name : files)
      {
         if (! (".".equals(name) || "..".equals(name)))
            return false;
      }
      return true;
   }
   
   @Override
   public void logoff()
   {
      PSFtpClient ftp = getFtpClient();
      if (ftp.mi_hasLogin)
      {
         logout(ftp);
      }
   }

   /**
    * Gets the root location to start publishing in. If the server should
    * publish relative to an absolute path, the path is empty. If not, the
    * current working directory from the ftp is used, so it will publish
    * relative to home.
    * 
    * @param jobId the id of the job
    * @param ftp {@link PSFtpClient} object, assumed not <code>null</code> and
    *           already logged in.
    * @return {@link String} never <code>null</code> but may be empty.
    * @throws {@link IOException} if an error occurs reading from ftp client.
    */
   private String getRootLocation(long jobId, PSFtpClient ftp) throws IOException
   {
      JobData jobData = m_jobData.get(jobId);
      if(publishToAbsolutePath(jobData))
      {
         return "";
      }
      return ftp.printWorkingDirectory();
   }

   
}
