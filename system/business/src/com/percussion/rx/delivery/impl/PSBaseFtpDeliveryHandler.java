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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSSite;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base ftp delivery handler. This adds retry logic to the <code>login</code>
 * and <code>doDelivery</code> methods in an attempt to prevent complete
 * publishing failure in the event of a network interruption or ftp server
 * failure.
 * 
 * @author peterfrontiero
 */
public abstract class PSBaseFtpDeliveryHandler extends PSBaseDeliveryHandler
{
    /**
     * See {@link #getMaxRetries()}
     */
    private int m_maxRetries = 10;


    /**
     * See {@link #getActivePortRange()}
     */
    private static String m_activePortRange = "";

    /**
     * start of port range for active FTP
     * See {@link #getActivePortStart()}
     */
    private static int m_activePortStart = 0;

    /**
     * end of port range for active FTP
     * See {@link #getActivePortEnd()}
     */
    private static int m_activePortEnd = 0;

    /**
     * See {@link #getConnectTimeout()}
     */
    private static int m_connectTimeout = 0;

    /**
     * See {@link #getTimeout()}
     */
    private static int m_timeout = -1;

    /**
     * See {@link #getUsePassiveMode()}
     */
    private static boolean m_usePassiveMode = false;

    private static boolean implicitMode = false;


    /**
     * Sets the connection timeout in milliseconds,
     * which will be passed to the Socket object's connect() method)
     * @param timeout the timeout in milliseconds.
     */
    public void setConnectTimeout(int timeout)
    {
        m_connectTimeout = timeout;
    }

    /**
     * Get the underlying socket connection timeout.
     */
    public int getConnectTimeout()
    {
        return m_connectTimeout;
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
     * Sets the port range for active FTP.
     * In turn also sets the start and ending port
     * variables.
     *
     * @param portRange the port range in format 1024-25000
     *
     * @see #getActivePortRange()
     */
    public void setActivePortRange(String portRange)
    {
        m_activePortRange = portRange;
        parseActiveFTPPortRange();
    }

    /**
     * parses the active FTP port range and sets
     * the start and end range port variables
     */
    private void parseActiveFTPPortRange() {
        if (getActivePortRange().indexOf("-") <= 0) {
            ms_log.error("Active port range in publisher-beans.xml is incorrectly set."
                    + " Must be in the format 60000-65000, for example.");
            return;
        }
        m_activePortStart = Integer.parseInt(getActivePortRange().split("-")[0]);
        m_activePortEnd = Integer.parseInt(getActivePortRange().split("-")[1]);
    }


    /**
     * Gets the unparsed port range for active FTP.
     *
     * @return the active FTP port range.
     */
    public String getActivePortRange()
    {
        return m_activePortRange;
    }

    /**
     * Gets the parsed start of port range for active FTP.
     * @return the active FTP port start range
     */
    public int getActivePortStart() {
        return m_activePortStart;
    }

    /**
     * Gets the parsed end of port range for active FTP.
     * @return the active FTP port end range
     */
    public int getActivePortEnd() {
        return m_activePortEnd;
    }

    /**
     * Sets the max number of retries the ftpClient will use
     * when using a socket connection  before failing the publishing job
     * @param retries the max number of retries to use
     */
    public void setMaxRetries(int retries)
    {
        m_maxRetries = retries;
    }

    /**
     * Gets the max number of retries to use when
     * using a socket connection.
     */
    public int getMaxRetries()
    {
        return m_maxRetries;
    }

    public static boolean isImplicitMode() {
        return implicitMode;
    }

    public static void setImplicitMode(boolean implicitMode) {
        PSBaseFtpDeliveryHandler.implicitMode = implicitMode;
    }

    /**
     * Logs debug related information on
     * properties set in the publisher-beans.xml
     */
    public void logDebugInformation() {
        ms_log.debug("Default timeout: " + getTimeout());
        ms_log.debug("Use passive mode: " + getUsePassiveMode());
        ms_log.debug("Connect timeout: " + getConnectTimeout());
        ms_log.debug("Max retries: " + getMaxRetries());
        ms_log.debug("Active port start: " + getActivePortStart() +
                ". Active port end: " + getActivePortEnd());
    }

   /**
    * Sets the socket timeout in milliseconds for both when opening a socket and
    * a currently open connection.
    * @param timeout the timeout in milliseconds.
    */
   public abstract void setTimeout(int timeout);  
   
   /**
    * Gets the socket timeout in milliseconds for both when opening a socket and
    * a currently open connection. Defaults to <code>-1</code> if not defined.
    */
   public abstract int getTimeout();

   /**
    * 
    * Internal class for managing the item's stream so that it can be handled
    * properly on a retry. Because the item can use a file or a stream the
    * mechanism for repositioning back to the beginning is different.
    * 
    * @author BillLanglais
    */
   public class RetryItem
   {
      /**
       * underlying item to publish
       */
      Item mi_item;

      /**
       * can the item be reused after it is read
       */
      boolean mi_retryPossible = true;

      /**
       * The content stream.
       * 
       * If {@link #mi_item} has a file then it is opened when
       * {@link #getContentStream()} is called. If m_item has a file it is
       * closed if open. If {@link #mi_item} does not have a file then the
       * {@link # m_item.getContentStream()} is called during construction and
       * the stream is kept open. When {@link #getContentStream()} is called in
       * this case if the stream supports mark, the stream is reset.
       */
      InputStream mi_stream = null;

      /**
       * Save the item and determine if retry is possible for the item. Because
       * we only want to get the result stream from the item once we need to
       * save it now. We use the same internal variable for keeping track of the
       * open stream but it is the presence or lack there of the items file
       * member that determines how we handle the stream.
       * 
       * @param item - the item whose stream we need to manage
       */
      public RetryItem(Item item)
      {
         notNull(item, "item may not be null");
         
         mi_item = item;

         if (mi_item.getFile() == null)
         {
            mi_stream = mi_item.getResultStream();

            if (mi_stream.markSupported())
            {
               mi_stream.mark(Integer.MAX_VALUE);
            }
            else
            {
               mi_retryPossible = false;
            }
         }
      }

      /**
       * 
       * @return the Items Content Stream ready for reading from the beginning.
       * @throws IOException - if the file is missing or the result stream is
       * bad.
       */
      public InputStream getContentStream() throws IOException
      {
         if (mi_item.getFile() != null)
         {
            IOUtils.closeQuietly(mi_stream);
            mi_stream = new FileInputStream(mi_item.getFile());
         }
         else
         {
            if (mi_retryPossible)
            {
               mi_stream.reset();
            }
         }

         return (mi_stream);
      }

      /**
       * Releases the item resource which will eliminate acess to the content.
       * It does not free the item but it can no longer be used to retrieve the
       * content. Must be called when done with the item.
       */
      public void release()
      {
         IOUtils.closeQuietly(mi_stream);
         
         mi_item.release();
      }

      /**
       * 
       * @return if we can retrieve the stream for the item more then once.
       */
      public boolean getRetryPossible()
      {
         return mi_retryPossible;
      }
   }
   
   /**
    * The class contains information that is required to login to a FTP server.
    */
   protected class FTPLoginInfo
   {
      protected FTPLoginInfo(JobData job)
      {
         setInfo(job.m_pubServer, job.m_site);
      }
      
      protected FTPLoginInfo(IPSPubServer pubserver, IPSSite site){
         setInfo(pubserver, site);
      }
      
      protected void setInfo(IPSPubServer pubServer, IPSSite site){
         
         if (pubServer != null)
         {
            password = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY, "");

            ipAddress = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, "");

            port = Integer.parseInt(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PORT_PROPERTY, "21"));

            userName = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_USER_ID_PROPERTY, "");
         }
         else
         {       
            port = site.getPort();
            ipAddress = site.getIpAddress();
            userName = site.getUserId();
            password = site.getPassword();
         }
      }
      
      protected int port;
      protected String ipAddress;
      protected String userName;
      protected String password;
   }

   /**
    * Object to save the login properties. Similar to {@link FTPLoginInfo}, but
    * adds the private key.
    * 
    * @author Santiago M. Murchio
    * 
    */
   protected class SFTPLoginInfo extends FTPLoginInfo
   {
      protected String privateKey;

      /**
       * Builds an object with the corresponding fields. If the server exists,
       * it takes the data from there. If it does not, the information is
       * retrieved from the site object. (This is intended to keep the
       * compatibility with CM System)
       * 
       * @param job {@link JobData} assumed not <code>null</code>
       */
      protected SFTPLoginInfo(JobData job)
      {
         super(job);
         setSftpInfo(job.m_pubServer, job.m_site);
      }
      
      protected SFTPLoginInfo(IPSPubServer pubServer, IPSSite site)
      {
         super(pubServer, site); 
         setSftpInfo(pubServer, site);
       
      }
      
      protected void setSftpInfo(IPSPubServer pubServer, IPSSite site){
         if (pubServer != null)
         {
            privateKey = pubServer.getPropertyValue(
                  IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY, "");
            
            port = Integer.parseInt(pubServer.getPropertyValue(
                  IPSPubServerDao.PUBLISH_PORT_PROPERTY, "22"));

         }
         else
         {
            privateKey = site.getPrivateKey();
         }
      }
      
      
      
      public String getPrivateKey()
      {
         return privateKey;
      }
      
      public void setPrivateKey(String privateKey)
      {
         this.privateKey = privateKey;
      }
   }
   
   /* (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#doRemoval(com.percussion.rx.delivery.impl.PSBaseDeliveryHandler.Item, long, java.lang.String)
    */
   @Override
   protected IPSDeliveryResult doRemoval(Item item, long jobId, String location)
   {
      return removeFileOrDir(item, jobId, location, true);
   }
   
   @Override
   protected void removeEmptyDirectory(String dir)
   {
      removeFileOrDir(null, -1, dir, false);
   }
   
   /**
    * This is the same as {@link #doRemoval(Item, long, String)}, except this
    * can be used to remove either a file or directory.
    * 
    * @param item the processed item, it may be <code>null</code> if removing a directory.
    * @param jobId the job ID, it is not used if removing a directory.
    * @param location the file or directory path, not blank.
    * @param isFile it is <code>true</code> if removing a file; otherwise removing a directory.
    * 
    * @return the delivery result. It is <code>null</code> if removing a directory.
    */
   protected abstract IPSDeliveryResult removeFileOrDir(Item item, long jobId, String location, boolean isFile);

   /**
    * Performs the actual connect and login to the ftp server.
    * 
    * @param jobId the publishing job ID.
    * @param failAll if <code>true</code>, then all items for the current
    * job will be marked as failed if the login was unsuccessful, otherwise,
    * a {@link PSDeliveryException} will be thrown in the event of a failed
    * connection.
    * @param retries 
    * @param
    *  
    * @return error results for the specified job.  Will be
    * <code>null</code> if the connection and login were successful.
    * 
    * @throws PSDeliveryException if <code>failAll</code> is
    * <code>false</code> and a connection could not be established or the
    * login was unsuccessful.
    */
   public abstract Collection<IPSDeliveryResult> doLogin(long jobId, boolean failAll, 
         Integer timeout, Integer retries)
      throws PSDeliveryException;
   
   /**
    * Logs off of the ftp server.
    */
   public abstract void logoff();
   
   /**
    * Performs the actual delivery of the item to the ftp server.
    * 
    * @param item - Item to be written to destination
    * @param inputStream - Stream content is read from
    * @param jobId - Id of publishing job
    * @param location - Path of content at destination
    * @return the result of delivering the item, never <code>null</code>.
    */
   protected abstract IPSDeliveryResult deliverItem(Item item,
         InputStream inputStream, long jobId, String location);

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#doDelivery(com.
    * percussion.rx.delivery.impl.PSBaseDeliveryHandler.Item, long,
    * java.lang.String)
    */
   @Override
   protected IPSDeliveryResult doDelivery(Item item, long jobId,
         String location)
   {
      boolean isDebugEnabled = ms_log.isDebugEnabled();
      RetryItem retryItem =new RetryItem(item);
      IPSDeliveryResult result = null;
      // if FTP should fail we want to retry
       try{
         for (int ftpPutRetriesLeft = 5; ftpPutRetriesLeft > 0; ftpPutRetriesLeft--)
         {
                 try(InputStream inputStream = retryItem.getContentStream()) {
                     if (isDebugEnabled) {
                         ms_log.debug("About to ftp publish content item"
                                 + " to remoteFilepath: " + location);
                     }

                     result = deliverItem(item, inputStream, jobId, location);
                     if (result.getOutcome() != IPSDeliveryResult.Outcome.FAILED) {
                         // success - there is no need to retry
                         break;
                     } else if (ftpPutRetriesLeft > 1 && retryItem.getRetryPossible()) {
                         prepareForRetry(jobId, isDebugEnabled, ftpPutRetriesLeft);
                     } else {
                         logError(retryItem, location, result.getFailureMessage());
                         if (!retryItem.getRetryPossible()) {
                             break;
                         }
                     }
                 }
             }
            }catch (Exception e) {
                  return getItemResult(Outcome.FAILED, item, jobId,
                          e.getLocalizedMessage());
         }finally {
             retryItem.release();
          }

          return result;
       }

   /**
    * Puts out error information if we have either exceeded the number of
    * retries or if the stream does not support resetting back to the beginning.
    * 
    * @param retryItem - item we tried to publish.
    * @param location - location of the published item assumed never
    * <code>null</null>
    * @param failureMessage - the reason the item did not publish may be <code> null</code>
    */
   private void logError(RetryItem retryItem, String location,
         String failureMessage)
   {
      // we retried several times and still failed or we are
      // dealing with a stream that does not support mark/reset-
      // time to give up

      String message;

      if (retryItem.getRetryPossible())
      {
         message = "No more retries left";
      }
      else
      {
         message = "Input stream does not support retries";
      }
      
      message += ", failed to publish to remoteFilepath: " + location;

      ms_log.debug(message);

      if (StringUtils.isNotBlank(failureMessage))
      {
         ms_log.debug("Error in doDelivery(): " + failureMessage);
      }
   }

   /**
    * If the delivery fails we assume it is because of a network problem
    * that may be temporary do we retry.  We delay a every increasing amount 
    * of time, attempt to relogin and prepare the content stream to be read 
    * again from the beginning.
    * 
    * @param jobId - id of job being published.
    * @param isDebugEnabled - is debug enabled.
    * @param ftpPutRetriesLeft - how many time more will we try.
    */
   private void prepareForRetry(long jobId,
         boolean isDebugEnabled, int ftpPutRetriesLeft)
   {
      /*
       * this may be caused by a firewall timeout or any other network
       * failure. to make it more robust we want to retry few times.
       */

      if (isDebugEnabled)
      {
         ms_log.debug("Ftp session disconnected, attempting to republish.");

         ms_log.debug("About to relogin, ftpPutRetriesLeft: "
               + (ftpPutRetriesLeft - 1));
      }
      
      try
      {
         // first time wait the least, next time wait more, etc.
         Thread.sleep(10000 / ftpPutRetriesLeft);
      }
      catch (InterruptedException intex)
      {
      }

      try
      {
         // logoff, login, wait and retry on failure
         relogin(jobId);
      }
      catch (PSDeliveryException e)
      {
         // We allow the retry to continue and the delivery code
         // will try to login again.
         if (isDebugEnabled)
         {
            ms_log.debug("Attempt to relogin failed.", e);
         }
      }
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#prepareForDelivery(long)
    */
   protected Collection<IPSDeliveryResult> prepareForDelivery(long jobId) throws PSDeliveryException
   {
      if (isTransactional())
         return login(jobId, true, null, null);
      else
         return login(jobId, false, null, null);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#releaseForDelivery(long)
    */
   protected void releaseForDelivery(long jobId)
   {
      logoff();
   }
   
   /**
    * Logs in to the ftp server using {@link # doLogin(long, boolean)}.
    * Multiple attempts are made in an effort to establish a connection.
    * 
    * @param jobId the publishing job ID.
    * @param failAll if <code>true</code>, then all items for the current job will be
    * marked as failed if the login was unsuccessful, otherwise, a
    * {@link PSDeliveryException} will be thrown in the event of a login failure.
    *  
    * @return error results for the specified job if the login was unsuccessful.
    * Will be <code>null</code> if the login was successful.
    * 
    * @throws PSDeliveryException if <code>failAll</code> is <code>false</code> and
    * login was unsuccessful.
    */
   protected Collection<IPSDeliveryResult> login(long jobId, boolean failAll, Integer timeout, Integer retries)
      throws PSDeliveryException
   {
      if (retries == null)
         retries = 3;
      if (timeout == null)
         timeout = 5000;
      
      boolean isDebugEnabled = ms_log.isDebugEnabled();

      if (isDebugEnabled)
      {
         ms_log.debug("About to login to FTP server...");
      }

      Collection<IPSDeliveryResult> results = null;
      
      for(int loginRetriesLeft = retries; loginRetriesLeft > 0; loginRetriesLeft--)
      {
         logoff(); //make sure that we are not logged on

         try
         {
            boolean shouldFailAll = false;
            if (loginRetriesLeft == 1 && failAll)
            {
               //this is the last attempt, fail all items if unsuccessful
               shouldFailAll = true;
            }
            
            results = doLogin(jobId, shouldFailAll, timeout, retries);
            if (results == null)
            {
               if (isDebugEnabled)
               {
                  ms_log.debug("Logged in.");
               }
               
               break;
            }
            else
            {
               String msg = "Error in login";
               if (!results.isEmpty())
               {
                  Iterator<IPSDeliveryResult> iter = results.iterator();
                  IPSDeliveryResult result = iter.next();
                  msg = result.getFailureMessage();
               }

               ms_log.warn(msg + ", loginRetriesLeft:" + loginRetriesLeft);

               if (loginRetriesLeft <= 1)
               {
                  ms_log.error("No more loginRetries left - failed to login.");
                  // tried few times, but we still fail - give up
                  break;
               }

               try
               {
                  // first time wait the least, next time wait more, etc.
                  Thread.sleep(15000 / loginRetriesLeft);
               }
               catch (InterruptedException intex)
               {
               }
            }
         }
         catch(PSDeliveryException de)
         {
            String msg = "PSDeliveryException in login, loginRetriesLeft:" +
               loginRetriesLeft;
            if (!isDebugEnabled)
            {
               ms_log.warn(msg);
            }
            else
            {
               ms_log.debug(msg, de);
            }

            if (loginRetriesLeft <= 1)
            {
               ms_log.error("No more loginRetries left - failed to login.");
               //tried few times, but we still fail - give up
               throw de;
            }

            try
            {
               //first time wait the least, next time wait more, etc.
               Thread.sleep(15000 / loginRetriesLeft);
            }
            catch(InterruptedException intex){}
         }
         catch(Exception e)
         {
            String msg = "Exception in login, loginRetriesLeft:"
               + loginRetriesLeft;
            if (!isDebugEnabled)
            {
               ms_log.error(msg);
            }
            else
            {
               ms_log.debug(msg, e);
            }

            if (loginRetriesLeft <= 1)
            {
               ms_log.error("No more loginRetries left - failed to login.");
               //tried few times, but we still fail - give up
               throw new PSDeliveryException(IPSDeliveryErrors.UNEXPECTED_ERROR, 
                     e.getMessage());
            }

            try
            {
               //first time wait the least, next time wait more, etc.
               Thread.sleep(15000 / loginRetriesLeft);
            }
            catch(InterruptedException intex){}
         }
      } 
      
      return results;
   }
   
   /**
    * Performs a logoff followed by a login to the ftp server.
    * 
    * @param jobId the publishing job ID.
    * @throws PSDeliveryException if the login was unsuccessful.
    */
   protected void relogin(long jobId) throws PSDeliveryException
   {
      logoff();
         
      login(jobId, false, null, null);
   }
   
   /**
    * Handles an error encountered during login.
    * 
    * @param jobId the publishing job ID.
    * @param msg the error message.  May be blank.
    * @param failAll if <code>true</code>, then all items for the current
    * job will be marked as failed, otherwise, a {@link PSDeliveryException}
    * will be thrown.
    * 
    * @return error results, never <code>null</code>.
    *    
    * @throws PSDeliveryException if <code>failAll</code> is <code>false</code>.
    */
   protected Collection<IPSDeliveryResult> handleLoginError(long jobId, String msg,
         boolean failAll) throws PSDeliveryException
   {
      String errorMsg = "An error has occurred while attempting to login to the ftp "
         + "server";
      if (!StringUtils.isBlank(msg))
      {
         errorMsg = msg;
      }
         
      if (failAll)
      {
         return failAll(jobId, errorMsg);
      }
      else
      {
         throw new PSDeliveryException(IPSDeliveryErrors.UNEXPECTED_ERROR, errorMsg);
      }
   }

   /**
    * Verifies if the server corresponding to the given publishing job is
    * suppose to publish items relatively to the home directory or relative to
    * an absolute path.
    * 
    * @param jobData {@link IPSPubServer} the publish server to use. Assumed
    *           not <code>null</code>.
    * @return <code>false</code> if the server should publish relatively to
    *         home. <code>true</code> otherwise.
    */
   protected boolean publishToAbsolutePath(JobData jobData)
   {
      if (jobData == null || jobData.m_pubServer == null)
      {
         return false;
      }

      return Boolean.valueOf(jobData.m_pubServer.getPropertyValue(
            IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false"));
   }
   

   /**
    * Logger.
    */
   private static Log ms_log = LogFactory.getLog(PSBaseFtpDeliveryHandler.class);


   @Override
   public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
   {
      return true;
   }

  
}
