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
package com.percussion.sitemanage.task.impl;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.configFilesExist;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.filesModifiedAfterPublished;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getNonSecureConfigurationFolder;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getSecureFilesPath;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.updatePublishedDate;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.rx.delivery.IPSDeliveryManager;
import com.percussion.rx.delivery.PSLocalDeliveryManagerLocator;
import com.percussion.rx.delivery.impl.PSSFtpDeliveryHandler;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.server.PSServer;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.data.PSPubServerProperty;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.impl.PSSitePublishDao;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.sitemanage.task.IPSAntService;
import com.percussion.sitemanage.task.IPSAntService.AntScript;
import com.percussion.util.IOTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

/**
 * 
 * Executes an Apache Ant script for a POST/PRE edition task. Currently it is
 * assumed that the Apache Ant scripts are stored in Rhythmyx/sys_resources/ant
 * directory.
 * 
 * TODO allow rx_resources/ant directory. TODO Make {@link #getType()}
 * configurable through extension init properties.
 * 
 * @author adamgent
 * 
 */
public class PSAntEditionTask implements IPSEditionTask
{

    private IPSAntService antService;

    private String antScriptDirectory;

    private String rootDirectory;

    private PSSitePublishDao sitePublishDao;

    private IPSPubServerDao pubServerMgr;

    private static Logger m_log = Logger.getLogger(PSAntEditionTask.class.getName());

    @Override
    public TaskType getType()
    {
        return TaskType.PREEDITION;
    }

    /**
     * Executes an ant script provided in the params.
     * <table border="1">
     * <tr>
     * <th>param</th>
     * <th>description</th>
     * </tr>
     * <tr>
     * <td>ant_file</td>
     * <td>The ant file that lives in sys_resources/ant</td>
     * </tr>
     * </table>
     * <p>
     * The following properties are pushed into the ant script:
     * <table border="1">
     * <tr>
     * <th>name</th>
     * <th>description</th>
     * </tr>
     * <tr>
     * <td>perc.site.name</td>
     * <td>The site name</td>
     * </tr>
     * <tr>
     * <td>perc.site.baseUrl</td>
     * <td>The base url of the site (http://mysite.com)</td>
     * </tr>
     * <tr>
     * <td>perc.site.root</td>
     * <td>The physical root folder path of the site (/)</td>
     * </tr>
     * <tr>
     * <td>perc.site.cmsFolderPath</td>
     * <td>The site cm system folder path.</td>
     * </tr>
     * <tr>
     * <td>perc.sys.dir</td>
     * <td>The cm system directory.</td>
     * </tr>
     * </table>
     */
    @Override
    public void perform(@SuppressWarnings("unused") IPSEdition edition, IPSSite site,
            @SuppressWarnings("unused") Date startTime, @SuppressWarnings("unused") Date endTime, long jobId,
            @SuppressWarnings("unused") long duration, @SuppressWarnings("unused") boolean success,
            Map<String, String> params, @SuppressWarnings("unused") IPSEditionTaskStatusCallback callback)
            throws Exception
    {
        notEmpty(getAntScriptDirectory(), "ant script directory not set");
        notEmpty(getRootDirectory(), "Root directory not set");
        notNull(params, "Params should not be empty");

        String file = params.get("ant_file");
        notEmpty(file, "ant_file");

        IPSAntService service = getAntService();
        AntScript a = new AntScript();
        
        IPSPubServer pubServer = null;
        String root  = EMPTY;
        try
        {           
            pubServer = pubServerMgr.findPubServer(edition.getPubServerId());
            PSPubServerProperty folderProperty = pubServer.getProperty(pubServerMgr.PUBLISH_FOLDER_PROPERTY);
            if (folderProperty != null)
            {
               root = folderProperty.getValue();
            }
        }
        catch(Exception e)
        {
            m_log.error("Failed to get the publishing server properties for server id '" + edition.getPubServerId().getUUID() + "'.");
        }

        String siteRootTemp = prepareSiteRootTemp(jobId, edition.getName(), site, pubServer, root);

        Map<String, String> props = new HashMap<>();
        props.put("perc.site.name", site.getName());
        props.put("perc.site.baseUrl", site.getBaseUrl());
        props.put("perc.site.cmsFolderPath", site.getFolderRoot());
        props.put("perc.sys.dir", getRootDirectory());
        props.put("perc.sys.jobId", "" + jobId);
        props.put("perc.site.root", siteRootTemp);

        prepareFtpProperties(props, site, pubServer);
        
        // If the publish server uses an absolute path, the configuration files are
        // not copyied over.
        if (!Boolean.valueOf(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false")))
        {
            preparePublishConfigFiles(props, site, pubServer.getServerId());
        }

        if (Boolean.valueOf(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false")) && Boolean.valueOf(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_SECURE_SITE_CONF, "false")) )
        {
            preparePublishConfigFiles(props, site, pubServer.getServerId());
        }

        for (Map.Entry<String, String> p : params.entrySet())
        {
            props.put("perc." + p.getKey(), p.getValue());
        }

        a.setBlocking(true);
        File f = new File(new File(getAntScriptDirectory()), file);
        isTrue(f.isFile(), "Ant file: " + f + " does not exist");
        a.setFile(f.getPath());
        a.setProperties(props);

        List<BuildListener> listeners = new ArrayList<>();

        // Pass the siteRootTemp if the site will be published on FTP or SFTP
        String deliveryType = pubServer.getPublishType();
        if (isFilesystemType(deliveryType))
        {
            listeners.add(new AntEditionTaskListener(site.getName(), edition.getName(), null, pubServer));
        }
        else
        {
            listeners.add(new AntEditionTaskListener(site.getName(), edition.getName(), siteRootTemp, pubServer));
        }

        a.setListeners(listeners);

        service.runAnt(a);

    }

    /**
     * Checks if the given delivery type is filesystem.
     * 
     * @param deliveryType {@link String} assumed not <code>null</code> or
     *            blank.
     * @return <code>true</code> if the delivery type if
     *         {@link PublishType#filesystem} or {@link PublishType#filesystem_only}.
     *         Returns <code>false</code> otherwise.
     */
    private boolean isFilesystemType(String deliveryType)
    {
        return PublishType.valueOf(deliveryType) == PublishType.filesystem
                || PublishType.valueOf(deliveryType) == PublishType.filesystem_only;
    }
    
    /**
     * Checks if the given delivery type is sftp.
     * 
     * @param deliveryType {@link String} assumed not <code>null</code> or
     *            blank.
     * @return <code>true</code> if the delivery type if
     *         {@link PublishType#sftp} or {@link PublishType#sftp_only}.
     *         Returns <code>false</code> otherwise.
     */
    private boolean isSftpType(String deliveryType)
    {
        return deliveryType.equals(PublishType.sftp.name()) || deliveryType.equals(PublishType.sftp_only.name());
    }

    /**
     * Checks if the given delivery type is filesystem.
     * 
     * @param deliveryType {@link String} assumed not <code>null</code> or
     *            blank.
     * @return <code>true</code> if the delivery type if {@link PublishType#ftp}
     *         or {@link PublishType#ftp_only}. Returns <code>false</code>
     *         otherwise.
     */
    private boolean isFtpType(String deliveryType)
    {
        return deliveryType.equals(PublishType.ftp.name()) || deliveryType.equals(PublishType.ftp_only.name());
    }

    /**
     * Checks if the given delivery type is FTPS type.
     *
     * @param deliveryType {@link String} assumed not <code>null</code> or
     *            blank.
     * @return <code>true</code> if the delivery type if {@link PublishType#ftp}
     *         or {@link PublishType#ftp_only}. Returns <code>false</code>
     *         otherwise.
     */
    private boolean isFtpsType(String deliveryType)
    {
        return deliveryType.equals(PublishType.ftps.name()) || deliveryType.equals(PublishType.ftps_only.name());
    }


    /**
     * Adds the properties to the map for publishing secure or unsecure
     * configuration files. If the site is secure, it compares the modified date
     * of each of the configuration files with the last-published-date,
     * retrieved from the tch file. If any of the files has been modified after
     * the last-published-date, the configuration files are re-published.
     * Otherwise it just uses the default path.
     * 
     * 
     * @param props the map that holds the properties. Assumed not
     *            <code>null</code>
     * @param site The site to get the properties from.
     * @throws IOException if an error occurs when comparing the files
     */
    private void preparePublishConfigFiles(Map<String, String> props, IPSSite site, long serverId) throws IOException
    {
        if (filesModifiedAfterPublished(site.getName(), serverId))
        {
            if (configFilesExist(site))
            {
                createSecurityUrlPatternFile(site);

                props.put("perc.site.config", getSecureFilesPath(site.getName()));
            }
            else
            {
                props.put("perc.site.config", getNonSecureConfigurationFolder().getPath());
            }
        }
    }

    /**
     * calculate the path for the live site content, if the site will be
     * published on FTP or SFTP we use a temporary directory, if not
     * site.getRoot(); will be used.
     * 
     * @param jobId Id of the current job
     * @param editionName the name of the current edition
     * @param site the current site
     * @param pubServer the publishing server
     * @param root the root path to publish the site
     * @return absolute path of the temporary folder.
     */
    private String prepareSiteRootTemp(long jobId, String editionName, IPSSite site, IPSPubServer pubServer, String root)
    {
        // Local Live site
        String deliveryType = pubServer.getPublishType();
        if (isFilesystemType(deliveryType))
        {
            return root;
        }

        try
        {
            // FTP or SFTP live site
            IPSDeliveryManager deliveryManager = PSLocalDeliveryManagerLocator.getDeliveryManager();
            String tempPath = deliveryManager.getTempDir();
            if (tempPath != "")
            {
                // Check if we could create the temporary directory.
                File tempDir = new File(deliveryManager.getTempDir(), site.getName() + "-" + editionName + "-"
                        + Long.toString(jobId));
                boolean dirCreated = tempDir.mkdirs();
                if (dirCreated)
                {
                    IOTools.deleteFile(tempDir);
                    return tempDir.getAbsolutePath();
                }
            }
        }
        catch (Exception e)
        {
            m_log.warn("Failed to create temporary directory for ftp resource files.", e);
        }

        return site.getRoot();
    }

    /**
     * Creates the configuration files for the site specific security settings.
     * 
     * @param site the site (assumed not <code>null</code>) for which the
     *            configuration files are going to be created.
     */
    private void createSecurityUrlPatternFile(IPSSite site)
    {
        IPSSiteSectionService sectionService = (IPSSiteSectionService) getWebApplicationContext().getBean(
                "siteSectionService");
        if (sectionService != null)
        {
            sectionService.generateSecurityConfigurationFiles(site);
        }
    }

    /**
     * Add the properties for the FTP publishing type if publish type is FTP or
     * SFTP.
     * 
     * @param props the map of the publish properties.
     * @param site the current site.
     * @param pubServer the publishing server
     * @param serverProps the publishing server properties
     */
    private void prepareFtpProperties(Map<String, String> props, IPSSite site, IPSPubServer pubServer)
    {
        String deliveryType = pubServer.getPublishType();
        if (isFilesystemType(deliveryType))
        {
            return;
        }

        props.put("perc.ftp.username", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_USER_ID_PROPERTY, ""));
        props.put("perc.ftp.password", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY, ""));
        props.put("perc.ftp.serverAddress", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, ""));

        props.put("perc.ftp.sourceDir", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY, ""));

        props.put("perc.ftp.remoteDir", getRemoteDir(pubServer));

        // If the publish type is Local don't set any properties, otherwise
        // try to execute the targets
        if (isFtpType(deliveryType))
        {
            props.put("perc.publishingFtp.set", Boolean.TRUE.toString());
            props.put("perc.ftp.port", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PORT_PROPERTY, "21"));
        }
        if (isFtpsType(deliveryType))
        {
            props.put("perc.publishingFtp.set", Boolean.TRUE.toString());
            props.put("perc.ftp.port", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PORT_PROPERTY, "23"));
        }

        else if (isSftpType(deliveryType))
        {
            props.put("perc.publishSecureFtp.set", Boolean.TRUE.toString());
            props.put("perc.ftp.port", pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PORT_PROPERTY, "22"));
        }

        // We first try to use the key from the delivery handler.
        PSSFtpDeliveryHandler sftpDeliveryHandler = (PSSFtpDeliveryHandler) PSBaseServiceLocator.getBean("sys_sftp");
        String serverPrivateKey = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY, "");
        if (!isBlank(sftpDeliveryHandler.getPrivateKeyFilePath()))
        {
            props.put("perc.publishSecureFtp.private_key", sftpDeliveryHandler.getPrivateKeyFilePath());
        }
        // if the property is not set in the delivery handler, use the site one
        else if (isNotBlank(serverPrivateKey))
        {
            String filePath = PSSFtpDeliveryHandler.getSshKeysDir() + serverPrivateKey;
            props.put("perc.publishSecureFtp.private_key", filePath);
        }
        else if (site.getPrivateKey() != null)
        {
            String filePath = PSSFtpDeliveryHandler.getSshKeysDir() + site.getPrivateKey();
            props.put("perc.publishSecureFtp.private_key", filePath);
        }
    }

    /**
     * Builds the remote dir path based on the server properties.
     * 
     * @param pubServer {@link IPSPubServer} object, assumed not
     *            <code>null</code>.
     * @return {@link String} never <code>null</code> but may be empty.
     */
    private String getRemoteDir(IPSPubServer pubServer)
    {
        String fullpath = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY, "");

        // do we need to publish to absolute path?
        if (!Boolean.valueOf(pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false")))
        {
            boolean isAbsolutePath = fullpath.startsWith("/");
            if (isAbsolutePath)
            {
                fullpath = fullpath.substring(1);
            }
        }
        return fullpath;
    }

    @Override
    public void init(@SuppressWarnings("unused") IPSExtensionDef def, @SuppressWarnings("unused") File file)
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        setRootDirectory(PSServer.getRxDir().getPath());
        File f = new File(new File(PSServer.getRxDir(), "sys_resources"), "ant");
        setAntScriptDirectory(f.getPath());
    }

    /**
     * The System Install directory.
     * 
     * @return never <code>null</code>.
     */
    public String getRootDirectory()
    {
        return rootDirectory;
    }

    protected void setRootDirectory(String rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }

    /**
     * The directory where the ant scripts are stored.
     * 
     * @return never <code>null</code>.
     */
    public String getAntScriptDirectory()
    {
        return antScriptDirectory;
    }

    protected void setAntScriptDirectory(String antScriptDirectory)
    {
        this.antScriptDirectory = antScriptDirectory;
    }

    public IPSAntService getAntService()
    {
        return antService;
    }

    public void setAntService(IPSAntService antService)
    {
        this.antService = antService;
    }

    /**
     * @return the sitePublishDao
     */
    public PSSitePublishDao getSitePublishDao()
    {
        return sitePublishDao;
    }

    /**
     * @param sitePublishDao the sitePublishDao to set
     */
    public void setSitePublishDao(PSSitePublishDao sitePublishDao)
    {
        this.sitePublishDao = sitePublishDao;
    }

    public IPSPubServerDao getPubServerMgr()
    {
        return pubServerMgr;
    }

    public void setPubServerMgr(IPSPubServerDao pubServerMgr)
    {
        this.pubServerMgr = pubServerMgr;
    }

    /**
     * Listener object to use with the ant script.
     * 
     * @author Santiago M. Murchio.
     * 
     */
    private static class AntEditionTaskListener implements BuildListener
    {
        private String sitename;

        private static final String COPYING_PREFIX = "Copying";

        private String editionName;

        private String temporaytPath;
        
        private IPSPubServer pubServer;

        private static final Log log = LogFactory.getLog("AntEditionTaskLogger");

        public AntEditionTaskListener(String sitename, String editionName, String temporaytPath, IPSPubServer pubServer)
        {
            this.sitename = sitename;
            this.editionName = editionName;
            this.temporaytPath = temporaytPath;
            this.pubServer = pubServer;
        }

        @Override
        public void buildFinished(BuildEvent event)
        {
            if (event.getException() == null)
            {
                try
                {
                    updatePublishedDate(sitename, pubServer);

                    if (this.temporaytPath != null)
                    {
                        FileUtils.deleteQuietly(new File(this.temporaytPath));
                        log.info("Finish deleting temporary folder '" + this.temporaytPath + "'.");
                    }
                }
                catch (IOException e)
                {
                    // if the published date could not be updated, the next time
                    // we will try to republish the config files
                }
            }
            else
            {
                log.error("Failed to copy files for edition '" + editionName + "'.", event.getException());
            }

            log.info("Finish copying files for edition '" + editionName + "'.");
        }

        @Override
        @SuppressWarnings("unused")
        public void buildStarted(BuildEvent event)
        {
            log.info("Start copying files for edition '" + editionName + "'.");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools
         * .ant.BuildEvent)
         * 
         * The messages that must be logged are the ones that detail the files
         * being copyied. The priority of them is 3 (Verbose)
         */
        @Override
        public void messageLogged(BuildEvent event)
        {
            if (Project.MSG_VERBOSE == event.getPriority()
                    && startsWithIgnoreCase(event.getMessage(), COPYING_PREFIX))
            {
                log.info(event.getMessage());
            }
        }

        @Override
        public void targetFinished(BuildEvent event)
        {
            if (event.getTarget() != null)
            {
                log.debug("Finished Target '" + event.getTarget().getName() + "' for edition '" + editionName + "'.");
            }
            else
            {
                log.debug("Finished Target for edition '" + editionName + "'.");
            }
        }

        @Override
        public void targetStarted(BuildEvent event)
        {
            if (event.getTarget() != null)
            {
                log.debug("Started Target '" + event.getTarget().getName() + "' for edition '" + editionName + "'.");
            }
            else
            {
                log.debug("Started Target for edition '" + editionName + "'.");
            }
        }

        @Override
        public void taskFinished(BuildEvent event)
        {
            if (event.getTask() != null)
            {
                log.debug("Finished Task '" + event.getTask().getTaskName() + "' for edition '" + editionName + "'.");
            }
            else
            {
                log.debug("Finished Task for edition '" + editionName + "'.");
            }
        }

        @Override
        public void taskStarted(BuildEvent event)
        {
            if (event.getTask() != null)
            {
                log.debug("Started Task '" + event.getTask().getTaskName() + "' for edition '" + editionName + "'.");
            }
            else
            {
                log.debug("Started Task for edition '" + editionName + "'.");
            }
        }

    }
}
