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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rx.delivery.impl;

import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class PSFtpsDeliveryHandler extends PSBaseFtpDeliveryHandler{


    /**
     * Logger.
     */
    private static final Logger log = LogManager.getLogger(PSFtpsDeliveryHandler.class);

    private int bufferSize=0;

    private boolean useEPSVwithIPv4=false;

    private int controlKeepAliveTimeout=300;

    private boolean useClientMode = true;
    /**
     * @see {@link #getUsePrivateDataChannel()}
     */
    private boolean usePrivateDataChannel = true;

    /**
     * See {@link #getMaxRetries()}
     */
    private int maxRetries = 10;

    /**
     * See {@link #getActivePortRange()}
     */
    private String activePortRange = "";

    /**
     * start of port range for active FTP
     * See {@link #getActivePortStart()}
     */
    private int activePortStart = 0;

    /**
     * end of port range for active FTP
     * See {@link #getActivePortEnd()}
     */
    private int activePortEnd = 0;

    /**
     * See {@link #getConnectTimeout()}
     */
    private int connectTimeout = 0;

    /**
     * See {@link #getTimeout()}
     */
    private int m_timeout = -1;

    /**
     * See {@link #getUsePassiveMode()}
     */
    private boolean usePassiveMode = true;

    private boolean implicitMode = false;


    /**
     * Holds the per thread ftp client. Initialized in {@link #commit(long)} and
     * used in <code>doDelivery</code> and <code>doRemoval</code>
     */
    protected ThreadLocal<PSFtpsClient> ms_ftps = new ThreadLocal<>();
    /**
     * Wraps {@link FTPClient} to manage login and logout from the FTP Server.
     */
    private static class PSFtpsClient extends FTPSClient
    {
        // FB: SIC_THREADLOCAL_DEADLY_EMBRACE NC 1-17-16
        /**
         * Determines if the {@link #mi_ftp} has been logged in. It is
         * <code>true</code> if it has been logged in. Defaults to
         * <code>false</code>. It is set be {@link #login(FTPClient, long)}
         * and reset by {@link #logout(FTPClient)}
         */
        private boolean mi_hasLogin = false;

        public PSFtpsClient() throws NoSuchAlgorithmException {
        }
    }

    /**
     * Get the FTP client for use in this handler.
     *
     * @return the FTP client, never <code>null</code>.
     */
    protected PSFtpsClient getFtpsClient()
    {
        PSFtpsClient rval = ms_ftps.get();
        if (rval == null)
        {
            try {
                rval = new PSFtpsClient();
            } catch (NoSuchAlgorithmException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
            }
            ms_ftps.set(rval);
        }
        return rval;
    }






    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    public boolean isUseClientMode() {
        return useClientMode;
    }

    public void setUseClientMode(boolean useClientMode) {
        this.useClientMode = useClientMode;
    }


    public int getControlKeepAliveTimeout() {
        return controlKeepAliveTimeout;
    }

    public void setControlKeepAliveTimeout(int controlKeepAliveTimeout) {
        this.controlKeepAliveTimeout = controlKeepAliveTimeout;
    }

    public boolean isUseEPSVwithIPv4() {
        return this.useEPSVwithIPv4;
    }

    public void setUseEPSVwithIPv4(boolean useEPSMode) {
        this.useEPSVwithIPv4 = useEPSMode;
    }

    public void setActivePortStart(int activePortStart) {
        this.activePortStart = activePortStart;
    }

    public void setActivePortEnd(int activePortEnd) {
        this.activePortEnd = activePortEnd;
    }


    /***
     * Sets whether or not a secure private data channel should be used.  This is required when FTPS server
     * like IIS requires SSL. Defaults to true.
     *
     * @param val true or false
     */
    public void setUsePrivateDataChannel(boolean val){
        usePrivateDataChannel = val;
    }

    /***
     * Gets whether of not a secure private data channel should be used.  This is required when using FTPS servers
     * like IIS and requiring TLS connection. Defaults to true.
     *
     * @return True if enabled, false if not.
     */
    public boolean getUsePrivateDataChannel(){
        return usePrivateDataChannel;
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
    private void openSocketConnection(FTPSClient ftp, String ipAddress, int port)
            throws SocketException, IOException
    {

        ftp.setDefaultPort(port);

        // must be called before connect
        if (getTimeout() != -1) {
            log.debug("Setting default timeout to {} ", getTimeout());
            ftp.setDefaultTimeout(getTimeout());
        }

        if (getConnectTimeout() != 0) {
            log.debug("Setting connect timeout to {}", getConnectTimeout());
            ftp.setConnectTimeout(getConnectTimeout());
        }

        if(this.controlKeepAliveTimeout>0){
            log.debug("Setting Control Keep Alive TimeOut to {} ", controlKeepAliveTimeout);
            // ftp.setControlKeepAliveReplyTimeout(controlKeepAliveTimeout);
        }

        log.debug("Setting client mode to {} ", useClientMode);
        ftp.setUseClientMode(useClientMode);
        log.debug("Enabling UTF8 auto detection...");
        //  ftp.setAutodetectUTF8(true);
        log.debug("Setting buffer size to {}", bufferSize);
        ftp.setBufferSize(bufferSize);

        if(ftp.isConnected()){
            ftp.disconnect();
        }

        logDebugInformation();

        ftp.connect(ipAddress);

        if (getUsePassiveMode())
        {
            if(this.useEPSVwithIPv4){
                log.debug("Enabling Extended Passive mode...");
                ftp.setUseEPSVwithIPv4(true);
            }
            log.debug("Entering passive mode...");
            ftp.enterLocalPassiveMode();
            log.debug("Disabling Remote verification...");
            ftp.setRemoteVerificationEnabled(false);
        }else if (getActivePortEnd() != 0 && getActivePortStart() != 0 &&
                ftp.getDataConnectionMode() == FTPSClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE)
        {
            ftp.enterLocalActiveMode();
            ftp.setActivePortRange(getActivePortStart(), getActivePortEnd());
            log.debug("Entering active mode...");
            log.debug("Setting active port range to {} - {}", getActivePortStart(),  getActivePortEnd());
        }

        // must be called after connect
        if (getTimeout() != -1) {
            ftp.setSoTimeout(getTimeout());
        }

        if(usePrivateDataChannel){
            log.debug("Enabling private Data Channel protocol...");
            ftp.execPBSZ(0);
            ftp.execPROT("P");
        }

    }

    /**
     * Logs out the given FTP client.
     * @param ftp the FTP client, assumed not <code>null</code>.
     */
    private void logout(PSFtpsClient ftps)
    {

        try
        {
            if (ftps.isConnected() && ftps.mi_hasLogin)
            {
                try
                {
                    ftps.logout();
                }
                catch (IOException ex)
                {
                    log.error("Problem logout FTP {}", ex.getMessage());
                    log.error(ex.getMessage());
                    log.debug(ex.getMessage(), ex);
                }
            }

            if (ftps.isConnected())
            {
                ftps.disconnect();
            }
        }
        catch (IOException e)
        {
            log.error("Problem closing ftp connection {}", e.getMessage());
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        finally
        {
            ftps.mi_hasLogin = false;
            ms_ftps.remove();
        }
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
                                                             boolean failAll, Exception ex) throws PSDeliveryException
    {
        String errorMsg = "An error has occurred while attempting to login to the FTPS server";
        if (!StringUtils.isBlank(msg))
        {
            errorMsg = msg;
        }

        if(ex != null)
            log.error(errorMsg,ex);
        else
            log.error(errorMsg);

        if (failAll)
        {
            log.error("Cancelling publishing job...");

            return failAll(jobId, errorMsg);
        }

        throw new PSDeliveryException(IPSDeliveryErrors.UNEXPECTED_ERROR, errorMsg);
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
    private Collection<IPSDeliveryResult> login(PSFtpsClient ftp, long jobId,
                                                boolean failAll, Integer connectionTimeout, Integer retries)
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
                log.warn("Error sending 'OPTS UTF8 ON' to ftp server, may not be a problem if server default is UTF-8: {}", ftp.getReplyString());
            }

            if (usePassiveMode)
            {
                ftp.enterLocalPassiveMode();
                ftp.setRemoteVerificationEnabled(false);

                if (log.isDebugEnabled())
                {
                    log.debug("Entering passive mode.");
                    log.debug("Disabling Remote verification.");
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            return handleLoginError(jobId, "Problem connecting to ftps server: "
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
        PSFtpsClient ftp = getFtpsClient();
        FTPLoginInfo info = new FTPLoginInfo(pubServer, site);
        int timeout = 10000;
        if(this.m_timeout >= 0)
            timeout = this.m_timeout;
        try
        {
            ftp.setDefaultTimeout(timeout);

            log.debug(String.format("Checking connection to %s on port %s with timeout %s",info.ipAddress, info.port, timeout));
            openSocketConnection(ftp, info.ipAddress, info.port);
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                log.error(String.format("Did not get positive reply from FTP Server: %s", info.ipAddress));
                ftp.disconnect();
                return false;
            }

            log.debug(String.format("Authenticating to FTP Server with Username %s",info.userName));
            if (!ftp.login(info.userName, info.password))
            {
                log.error(String.format("Authenticating to FTP Server with Username %s failed",info.userName));

                return false;
            }

            ftp.mi_hasLogin = true;
            connected = true;
        }
        catch (Exception e)
        {
            log.error("FTP Connection Check Failed to connect {}", e.getMessage());
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
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
        getFtpsClient();

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
        PSFtpsClient ftp = getFtpsClient();
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
                    jobId, item.getReferenceId(), location.getBytes(StandardCharsets.UTF_8));
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
     * Gets the root location to start publishing in. If the server should
     * publish relative to an absolute path, the path is empty. If not, the
     * current working directory from the ftp is used, so it will publish
     * relative to home.
     *
     * @param jobId the id of the job
     * @param ftp {@link PSFtpsClient} object, assumed not <code>null</code> and
     *           already logged in.
     * @return {@link String} never <code>null</code> but may be empty.
     * @throws {@link IOException} if an error occurs reading from ftp client.
     */
    private String getRootLocation(long jobId, PSFtpsClient ftp) throws IOException
    {
        JobData jobData = m_jobData.get(jobId);
        if(publishToAbsolutePath(jobData))
        {
            return "";
        }
        return ftp.printWorkingDirectory();
    }


    /**
     * Search up the paths until we find one and start creating the directories.
     *
     * @param ftp the ftp client, assumed never <code>null</code>
     * @param dir the directory, assumed never <code>null</code>
     * @throws IOException
     */
    private void makeDirectories(FTPSClient ftp, File dir) throws IOException
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
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
                log.error("Could not restore working directory: {}", currentWorkingDirectory, e);
            }
        }
    }

    @Override
    public Collection<IPSDeliveryResult> doLogin(long jobId, boolean failAll, Integer timeout, Integer retries)
            throws PSDeliveryException
    {
        return login(getFtpsClient(), jobId, failAll, timeout, retries);
    }
    /**
     * Determines if the specified directory is empty.
     * @param dir the directory in question, assumed not blank.
     * @param ftp the ftp handler, assumed not <code>null</code>.
     * @return <code>true</code> if the directory is empty.
     * @throws IOException if error occurs.
     */
    private boolean isEmptyDirectory(String dir, FTPSClient ftp) throws IOException
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
        PSFtpsClient ftp = getFtpsClient();
        if (ftp.mi_hasLogin)
        {
            logout(ftp);
        }
    }



    @Override
    protected IPSDeliveryResult removeFileOrDir(Item item, long jobId, String location, boolean isFile)
    {
        PSFtpsClient ftp = getFtpsClient();

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
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            log.error("Error remove " + (isFile ? "file" : "directory") + ": \"" + location + "\"", e);
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
     * Sets the socket timeout in milliseconds for both when opening a socket and
     * a currently open connection.
     * @param m_timeout the timeout in milliseconds.
     */
    public void setTimeout(int m_timeout)
    {
        this.m_timeout = m_timeout;
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
     * Sets the connection timeout in milliseconds,
     * which will be passed to the Socket object's connect() method)
     * @param timeout the timeout in milliseconds.
     */
    public void setConnectTimeout(int timeout)
    {
        connectTimeout = timeout;
    }

    /**
     * Get the underlying socket connection timeout.
     */
    public int getConnectTimeout()
    {
        return connectTimeout;
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
        this.usePassiveMode = usePassiveMode;
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
        return usePassiveMode;
    }

    /**
     * parses the active FTP port range and sets
     * the start and end range port variables
     */
    private void parseActiveFTPPortRange() {
        if (getActivePortRange().indexOf("-") <= 0) {
            log.error("Active port range in publisher-beans.xml is incorrectly set."
                    + " Must be in the format 60000-65000, for example.");
            return;
        }
        activePortStart = Integer.parseInt(getActivePortRange().split("-")[0]);
        activePortEnd = Integer.parseInt(getActivePortRange().split("-")[1]);
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
        activePortRange = portRange;
        parseActiveFTPPortRange();
    }

    /**
     * Gets the unparsed port range for active FTP.
     *
     * @return the active FTP port range.
     */
    public String getActivePortRange()
    {
        return activePortRange;
    }

    /**
     * Gets the parsed start of port range for active FTP.
     * @return the active FTP port start range
     */
    public int getActivePortStart() {
        return activePortStart;
    }

    /**
     * Gets the parsed end of port range for active FTP.
     * @return the active FTP port end range
     */
    public int getActivePortEnd() {
        return activePortEnd;
    }

    /**
     * Sets the max number of retries the ftpClient will use
     * when using a socket connection  before failing the publishing job
     * @param retries the max number of retries to use
     */
    public void setMaxRetries(int retries)
    {
        maxRetries = retries;
    }

    /**
     * Gets the max number of retries to use when
     * using a socket connection.
     */
    public int getMaxRetries()
    {
        return maxRetries;
    }


}
