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
package com.percussion.pubserver.impl;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.pubserver.data.PSPublishServerProperty;
import com.percussion.rx.delivery.impl.PSBaseDeliveryHandler;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSDeliveryType;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.IPSDatabasePubServerFilesService;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.data.PSDatabasePubServer;
import com.percussion.services.pubserver.data.PSDatabasePubServer.DriverType;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.pubserver.data.PSPubServerProperty;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.data.PSEnumVals;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.dao.impl.PSSitePublishDao;
import com.percussion.sitemanage.data.PSPubInfo;
import com.percussion.sitemanage.data.PSPublisherInfo;
import com.percussion.sitemanage.data.PSSaasSiteConfig;
import com.percussion.sitemanage.data.PSSitePublishJob;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;
import com.percussion.sitemanage.service.IPSSitePublishService.PubType;
import com.percussion.sitemanage.service.IPSSitePublishStatusService;
import com.percussion.tools.Base64;
import com.percussion.utils.PSNamedLockManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.ToDoVulnerability;
import com.percussion.utils.security.deprecated.PSAesCBC;
import com.percussion.utils.service.IPSUtilityService;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeServerEntry;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

/**
 * See the interface for documentation.
 *
 * @author leonardohildt
 * @author ignacioerro
 *
 */
@Component("pubServerService")
@Lazy
public class PSPubServerService implements IPSPubServerService
{
    //Our Placeholder for the FTP password
    public static final String PASSWORD_ENTRY = "passwordEntry";

    private static final Logger log = LogManager.getLogger(PSPubServerService.class);

    private IPSPubServerDao pubServerDao;
    private IPSSiteManager siteMgr;
    private IPSDatabasePubServerFilesService serverFileService;
    private IPSSiteDataService siteDataService;
    private IPSSitePublishStatusService statusService;
    private IPSGuidManager guidMgr;
    private IPSRxPublisherService rxPubService;
    private PSNamedLockManager lockMgr;
    private PSSitePublishDao sitePublishDao;
    private IPSPublisherService publisherService;
    private IPSContentChangeService contentChangeService;
    private IPSUtilityService utilityService;
    private static Boolean isEC2Instance = null;
    private IPSPublishingWs pubWs;



    private static Map<String, Object > handlerMap;

    @Autowired
    public PSPubServerService(IPSPubServerDao pubServerDao, IPSSiteManager siteMgr,
                              @Qualifier("sys_dbPubServerFileService") IPSDatabasePubServerFilesService serverFileService, IPSSiteDataService siteDataService,
                              IPSSitePublishStatusService statusService, IPSGuidManager guidMgr, IPSRxPublisherService rxPubService,
                              PSSitePublishDao sitePublishDao, IPSPublisherService publisherService, IPSContentChangeService contentChangeService,
                              IPSUtilityService utilityService)
    {
        this.pubServerDao = pubServerDao;
        this.siteMgr = siteMgr;
        this.serverFileService = serverFileService;
        this.siteDataService = siteDataService;
        this.statusService = statusService;
        this.guidMgr = guidMgr;
        this.rxPubService = rxPubService;
        this.sitePublishDao = sitePublishDao;
        this.publisherService = publisherService;
        this.contentChangeService = contentChangeService;
        this.utilityService = utilityService;
        //Create map of handler types to handle checking of pubserver configuration.
        this.handlerMap = new HashMap<>();
        handlerMap.putAll(generatePubServerHandlerMap());



        lockMgr = new PSNamedLockManager(5000);
    }

    /**
     * Returns a map of all pubserver handlers
     * the _only tagged pubservers represent non-default pubservers
     * @return Map of all the pub server handlers
     */
    private Map<String, Object> generatePubServerHandlerMap(){

        IPSPublisherService pubsvc = PSPublisherServiceLocator.getPublisherService();

        Map<String, Object> handlerMap = new HashMap<>();

        IPSDeliveryType tmp = pubsvc.loadDeliveryType("ftp");
        handlerMap.put("ftp",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("ftp_only");
        handlerMap.put("ftp_only",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("ftps");
        handlerMap.put("ftps",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("ftps_only");
        handlerMap.put("ftps_only",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("sftp");
        handlerMap.put("sftp",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("sftp_only");
        handlerMap.put("sftp_only",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("filesystem");
        handlerMap.put("filesystem",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("filesystem_only");
        handlerMap.put("filesystem_only",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("database");
        handlerMap.put("database",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("amazon_s3");
        handlerMap.put("amazon_s3",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        tmp = pubsvc.loadDeliveryType("amazon_s3_only");
        handlerMap.put("amazon_s3_only",
                PSBaseServiceLocator.getBean(tmp.getBeanName()));

        return handlerMap;
    }

    @Override
    public PSPublishServerInfo getPubServer(String siteId, String serverId) throws PSPubServerServiceException {
        if (isBlank(siteId))
            throw new IllegalArgumentException("Site id cannot be blank.");
        if (isBlank(serverId))
            throw new IllegalArgumentException("Server id cannot be blank.");

        PSPublishServerInfo serverInfo = null;
        PSPubServer pubServer = null;

        try
        {
            IPSSite site = siteMgr.findSite(getSiteGuid(siteId));

            if (site == null)
            {
                throw new PSPubServerServiceException("Invalid site " + siteId);
            }

            pubServer = pubServerDao.findPubServer(getPubServerGuid(serverId));

            if (pubServer == null)
            {
                throw new PSPubServerServiceException("Invalid server " + serverId);
            }
            if (!serverBelongsToSite(site.getGUID(), pubServer))
            {
                throw new PSPubServerServiceException("Invalid server " + pubServer.getName() + " for site " + site.getName());
            }

            serverInfo = toPSPublishServerInfo(pubServer, site, true);
        }
        catch (Exception e)
        {
            throw new PSPubServerServiceException(e.getMessage(), e);
        }

        return serverInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.pubserver.IPSPubServerService#getPubServerList()
     */
    @Override
    public List<PSPublishServerInfo> getPubServerList(String siteId) throws PSPubServerServiceException
    {
        if (isBlank(siteId))
            throw new IllegalArgumentException("Site id cannot be blank.");

        List<PSPublishServerInfo> serversList = new ArrayList<>();
        try
        {
            IPSSite site = siteMgr.findSite(getSiteGuid(siteId));
            List<PSPubServer> servers = pubServerDao.findPubServersBySite(site.getGUID());

            if (servers == null)
            {
                throw new PSPubServerServiceException("No servers in the system for the given site.");
            }
            else
            {
                for (PSPubServer server : servers)
                {
                    PSPublishServerInfo serverInfo = new PSPublishServerInfo();
                    serverInfo = toPSPublishServerInfo(server, site, false);
                    serversList.add(serverInfo);
                }
            }
        }
        catch (Exception ex)
        {
            String msg = "Failed to get the list of publish servers";
            log.error(msg, ex);
            throw new PSPubServerServiceException(msg, ex);
        }
        return serversList;
    }

    private boolean isDatabaseType(String type)
    {
        return equalsIgnoreCase(type, PUBLISH_DB_TYPE);
    }

    @Override
    public synchronized PSPublishServerInfo createPubServer(String siteId, String serverName, PSPublishServerInfo pubServerInfo)
            throws PSPubServerServiceException, PSDataServiceException {
        if (isBlank(siteId))
            throw new IllegalArgumentException("Site id cannot be blank.");

        convertPasswordFromBase64(pubServerInfo);

        serverName = getPubServerName(serverName);

        IPSSite site = siteMgr.findSite(getSiteGuid(siteId));
        String siteName = (site.getName() != null)?site.getName().trim() : "";
        serverName = (serverName != null)?serverName.trim() : "";

        PSPubServer server;
        PSPubServer currentDefaultServer = getDefaultPubServer(site.getGUID());

        validateServerName(serverName, "", siteId);
        validateProperties(pubServerInfo, siteId, true);

        site = siteMgr.findSite(siteName);
        PSDatabasePubServer dbPubServer = null;

        if (isDatabaseType(pubServerInfo.getType()))
        {
            // Test the connection and update the DB configuration files
            dbPubServer = createDatabasePubServer(siteName, serverName, pubServerInfo, site);
        }

        if (StringUtils.isBlank(pubServerInfo.getServerType()))
        {
            pubServerInfo.setServerType(PSPubServer.PRODUCTION);
        }

        try
        {
            server = pubServerDao.createServer(site);

            server.setName(serverName);
            server.setDescription(pubServerInfo.getDescription());
            server.setServerType(pubServerInfo.getServerType());

            boolean isDefaultServer = pubServerInfo.getIsDefault() ? true: false;
            boolean isXmlSet = Boolean.valueOf(pubServerInfo.findProperty(XML_FLAG)) ? true : false;
            String formatValue = isXmlSet ? XML_FLAG : HTML_FLAG;

            setPublishType(server, pubServerInfo.getType(), pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY), pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_SECURE_FTP_PROPERTY), isDefaultServer, formatValue);
            updatePublishTypeForStaging(server);

            // Set properties to the server
            setProperties(server, pubServerInfo, site);

            // If this is the default server
            if(isDefaultServer)
            {
                siteDataService.setPublishServerAsDefault(site, server);
            }

            // Creates a full publish edition for the server
            siteDataService.createPublishingItemsForPubServer(site, server, isDefaultServer);

            /*
             * Check if the server being updated is the same default server. If not, then
             * the previous default server needs to be updated in order to change the publishing type
             * and update the content list appropriately.
             *
             */
            boolean clearIncrementalQueue = false;
            if(currentDefaultServer != null)
                clearIncrementalQueue = updatePreviousDefaultPubServer(currentDefaultServer, server, site, isDefaultServer);

            pubServerDao.savePubServer(server);
            if (dbPubServer != null)
                serverFileService.saveDatabasePubServer(dbPubServer);

            //Create staging ondemand editions
            if(PSPubServer.STAGING.equalsIgnoreCase( server.getServerType()))
            {
                sitePublishDao.addStagingPublishNow(site);
                sitePublishDao.addStagingUnpublishNow(site);
            }

            if (clearIncrementalQueue)
            {
                clearLiveIncrementalQueue(site);
            }
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            throw new PSPubServerServiceException(ex.getMessage(), ex);
        }

        return toPSPublishServerInfo(server, site, true);
    }

    @Override
    public PSPublishServerInfo updatePubServer(String siteId, String serverId, PSPublishServerInfo pubServerInfo) throws PSPubServerServiceException, PSDataServiceException {
        if (isBlank(siteId))
            throw new IllegalArgumentException("Site id cannot be blank.");
        if (isBlank(serverId))
            throw new IllegalArgumentException("Server id cannot be blank.");

        boolean locked = tryToLockSite(siteId);

        convertPasswordFromBase64(pubServerInfo);

        try
        {
            IPSSite site = siteMgr.findSite(getSiteGuid(siteId));

            PSPubServer server = null;
            try
            {
                server = pubServerDao.loadPubServerModifiable(getPubServerGuid(serverId));
            }
            catch(Exception ex)
            {
                throw new PSPubServerServiceException("The server you are trying to act on no longer exists in the system.", ex);
            }

            String serverName = getPubServerName(pubServerInfo.getServerName());

            String currentServerName = (server.getName() != null) ? server.getName().trim() : "";
            String newServerName = (serverName != null)? serverName : currentServerName;

            PSPubServer currentDefaultServer = getDefaultPubServer(site.getGUID());

            /*
             * Validate the server name and properties for the publishing server to be updated
             */
            validateServerName(newServerName, currentServerName, siteId);
            validateProperties(pubServerInfo, siteId, false);

            server.setName(newServerName);

            if (pubServerInfo.getDescription() != null)
                server.setDescription(pubServerInfo.getDescription().trim());

            String oldType = server.getPublishType();
            boolean isDefaultServer = pubServerInfo.getIsDefault() ? true: false;

            if (pubServerInfo.getType() != null)
            {
                boolean isXmlSet = Boolean.valueOf(pubServerInfo.findProperty(XML_FLAG)) ? true : false;
                String formatValue = isXmlSet ? XML_FLAG : HTML_FLAG;
                setPublishType(server, pubServerInfo.getType(), pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY), pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_SECURE_FTP_PROPERTY), isDefaultServer, formatValue);
                updatePublishTypeForStaging(server);
            }

            setProperties(server, pubServerInfo, site);

            if (isDefaultServer)
            {
                siteDataService.setPublishServerAsDefault(site, server);
            }

            PSPubServer oldServer = pubServerDao.findPubServer(server.getServerId());
            siteDataService.updateServerEditions(site, oldServer, server, isDefaultServer);

            /*
             * Check if the server being updated is the same default server. If not, then
             * the previous default server needs to be updated in order to change the publishing type
             * and update the content list appropriately.
             *
             */
            boolean clearIncrementalQueue = updatePreviousDefaultPubServer(currentDefaultServer, server, site, isDefaultServer);

            if (!server.isSamePublish(oldServer))
            {
                server.setHasFullPublisehd(false);
                if (isDefaultServer)
                    clearIncrementalQueue = true;
            }

            String error = updateDBConfigFiles(site, oldType, currentServerName, newServerName, pubServerInfo);
            if (error != null)
                throw new PSPubServerServiceException(error);

            pubServerDao.savePubServer(server);

            try
            {
                removeServerEntry(site.getName(), server.getServerId());
            }
            catch (IOException e)
            {
                log.error("Failed to update site configure cache file", e);
            }

            if (clearIncrementalQueue)
            {
                clearLiveIncrementalQueue(site);
            }


            return toPSPublishServerInfo(server, site, true);
        }
        finally
        {
            if (locked)
                tryToUnlockSite(siteId);
        }
    }

    private void clearLiveIncrementalQueue(IPSSite site)
    {
        clearIncrementalQueue(site, PSContentChangeType.PENDING_LIVE);
    }

    private void clearStagingIncrementalQueue(IPSSite site)
    {
        clearIncrementalQueue(site, PSContentChangeType.PENDING_STAGED);
    }

    private void clearIncrementalQueue(IPSSite site, PSContentChangeType changeType)
    {
        try
        {
            contentChangeService.deleteChangeEventsForSite(site.getSiteId(), changeType);
        }
        catch (Exception e)
        {
            log.error("Failed to clear the incremental queue for site: " + site.getName(), e);
        }
    }

    /**
     * Gets the publish server name and validate the name.
     * @param serverName the info contains the publish server name in question, assumed not <code>null</code>.
     * @return the publish server name. It may be <code>null</code> or empty if the info does not contain the name.
     */
    private String getPubServerName(String serverName) throws PSPubServerServiceException {
        serverName = StringUtils.trim(serverName);

        if (StringUtils.isBlank(serverName))
            throw new PSPubServerServiceException("The server name cannot be empty.");

        return serverName;
    }

    @Override
    public List<PSPublishServerInfo> deleteServer(String siteId, String serverId) throws PSPubServerServiceException, PSDataServiceException {
        boolean locked = tryToLockSite(siteId);

        try
        {
            IPSSite site = siteMgr.findSite(getSiteGuid(siteId));
            PSPubServer pubServer = pubServerDao.findPubServer(getPubServerGuid(serverId));

            if (pubServer == null)
                throw new PSPubServerServiceException("The Server you have selected doesn't exist in the system. Please refresh and try again.");

            if (site == null)
                throw new PSPubServerServiceException("The Site you have selected doesn't exist in the system. Please refresh and try again.");


            if (site.getDefaultPubServer() == pubServer.getServerId())
                throw new PSPubServerServiceException("The server cannot be deleted because it is the default server.");

            List<PSSitePublishJob> jobs = statusService.getCurrentJobsBySite(siteId);
            for (PSSitePublishJob job : jobs)
            {
                if (pubServer.getServerId() == job.getPubServerId())
                    throw new PSPubServerServiceException("The server is being used by other user and cannot be deleted.");
            }
            if(PSPubServer.STAGING.equalsIgnoreCase( pubServer.getServerType()))
            {
                clearStagingIncrementalQueue(site);
            }
            deletePubServer(pubServer);

            return getPubServerList(siteId);
        }
        finally
        {
            if (locked)
                tryToUnlockSite(siteId);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.pubserver.IPSPubServerService#deletePubServersBySite(com.percussion.utils.guid.IPSGuid)
     */
    public void deletePubServersBySite(IPSGuid siteId)
    {
        notNull(siteId);

        List<PSPubServer> pubServers = pubServerDao.findPubServersBySite(siteId);
        if (pubServers == null)
            return;

        for (PSPubServer pubServer : pubServers)
        {
            try {
                deletePubServer(pubServer);
            } catch (PSPubServerServiceException e) {
                log.error("Error deleting publishing server: {} Error:{}",
                        pubServer.getName(),e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }
    }

    @Override
    public void stopPublishing(String jobId) throws PSPubServerServiceException
    {
        Long job = null;
        try
        {
            job = Long.parseLong(jobId);
        }
        catch (Exception e)
        {
            throw new PSPubServerServiceException("Error trying to stop publishing.");
        }
        rxPubService.cancelPublishingJob(job);
    }

    @Override
    public Boolean isDefaultServerModified(String siteId)
    {
        if (isBlank(siteId))
            throw new IllegalArgumentException("Site id cannot be blank.");

        IPSSite site = siteMgr.findSite(getSiteGuid(siteId));

        IPSPubServer server = pubServerDao.findPubServer(site.getDefaultPubServer());

        return serverFileService.isServerModified(site.getSiteId(), server.getName());
    }

    @Override
    public Map<String, Boolean> getAvailableDrivers()
    {
        return serverFileService.getAvailableDrivers();
    }

    @Override
    public String getDefaultFolderLocation(String siteId, String publishType, String driver, String serverType)
    {
        IPSSite site = null;
        String defaultFolderLocation = "";
        String type = null;
        boolean isStaging = false;

        try
        {
            site = siteMgr.findSite(getSiteGuid(siteId));
        }
        catch(Exception e)
        {
            log.error("The site " + siteId + "does not exist.");
        }

        if (publishType.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_LOCAL))
        {
            type = PublishType.filesystem.toString();
            isStaging = serverType.equalsIgnoreCase("STAGING");
        }
        if (publishType.equalsIgnoreCase(PUBLISH_FILE_TYPE) && (driver.equalsIgnoreCase(DRIVER_FTP)  ))
        {
            type = PublishType.ftp.toString();
        }
        if (publishType.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_SFTP) )
        {
            type = PublishType.sftp.toString();
        }
        if (publishType.equalsIgnoreCase(PUBLISH_FILE_TYPE) &&  driver.equalsIgnoreCase(DRIVER_FTPS))
        {
            type = PublishType.ftps.toString();
        }

        if (site != null && type != null)
        {
            defaultFolderLocation = siteDataService.getDefaultPublishingRoot(site, type, "");
            if( isStaging )
                defaultFolderLocation = defaultFolderLocation.replace("Deployment","Staging/Deployment");
        }

        return normalizePath(defaultFolderLocation);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.pubserver.IPSPubServerService#getDefaultPubServer()
     */
    @Override
    public PSPubServer getDefaultPubServer(IPSGuid siteId)
    {
        return PSSitePublishDaoHelper.getDefaultPubServer(siteId);
    }

    @Override
    public PSPubServer getStagingPubServer(IPSGuid siteId) {
        return PSSitePublishDaoHelper.getStagingPubServer(siteId);
    }

    @Override
    public PSPubServer createDefaultPubServer(IPSSite site, String serverName) throws PSPubServerServiceException
    {
        String serverNameToSet = (serverName != null)?serverName.trim() : "";
        PSPubServer server = null;

        try
        {
            server = pubServerDao.createServer(site);
            server.setName(serverNameToSet);

            pubServerDao.savePubServer(server);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage());
            throw new PSPubServerServiceException(ex.getMessage(), ex);
        }

        return server;
    }

    private PSPubServer createAmazonS3Server(IPSSite site) throws Exception{
        PSSaasSiteConfig siteConfig = siteDataService.getSaasSiteConfig(site.getName());
        if(siteConfig == null){
            throw new PSPubServerServiceException("");
        }
        PSPubServer server = pubServerDao.createServer(site);
        if(server.getProperties() != null)
            server.getProperties().clear();
        server.setPublishType(PublishType.amazon_s3.toString());
        PSPublisherInfo pubInfo = siteConfig.getSiteConfig().getPublisherInfo();
        server.addProperty(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, pubInfo.getBucketName());
        server.addProperty(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY, encrypt(pubInfo.getAccessKey()));
        server.addProperty(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY, encrypt(pubInfo.getSecretKey()));
        server.addProperty(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, DRIVER_AMAZONS3);
        server.addProperty(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY, "");
        server.addProperty(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false");
        server.addProperty(IPSPubServerDao.PUBLISH_FORMAT_PROPERTY, "HTML");

        Region selectedRegion = pubInfo.getRegion();
        if(selectedRegion == null){
            selectedRegion = Regions.getCurrentRegion();
        }
        server.addProperty(IPSPubServerDao.PUBLISH_EC2_REGION,selectedRegion.getName());
        return server;
    }

    public static Boolean isEC2Instance(){
        if(isEC2Instance != null){
            return isEC2Instance;
        }
        try {
            Client client = newClient();

            WebTarget resource = client.target("http://169.254.169.254/latest/meta-data/");

            Invocation.Builder request = resource.request();
            request.accept(MediaType.APPLICATION_JSON);

            Response response = request.get();

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                isEC2Instance = Boolean.TRUE;
                return true;
            } else {
                isEC2Instance = Boolean.FALSE;
            }
        }catch(Exception e){
            //means not an EC2 Server
            isEC2Instance = Boolean.FALSE;
        }
        return isEC2Instance;
    }

    @Override
    public boolean updateDefaultFolderLocation(IPSSite site, String root, String oldName)
    {
        boolean didChange = false;

        List<PSPubServer> pubServers = pubServerDao.findPubServersBySite(site.getGUID());

        String serverRoot = root;

        for (PSPubServer pubServer : pubServers)
        {
            if (!pubServer.isDatabaseType() &&
                    pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY).equalsIgnoreCase(Boolean.FALSE.toString()))
            {
                if (pubServer.isFtpType())
                {
                    String curRoot = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY);

                    if (oldName != null)
                    {
                        serverRoot = siteDataService.getBasePublishingRoot(curRoot, oldName);
                    }
                    else
                    {
                        serverRoot = siteDataService.getBasePublishingRoot(curRoot, site.getName());
                    }
                }
                else
                {
                    serverRoot = root;
                }
                didChange = didChange | setFolderProperty(pubServer, site, serverRoot, false, null, true);

                // if this has been changed, we need to require a new full publish
                if (didChange)
                    pubServer.setHasFullPublisehd(false);

                pubServerDao.savePubServer(pubServer);
            }
        }

        return didChange;
    }

    private PSDatabasePubServer createDatabasePubServer(String siteName, String serverName, PSPublishServerInfo pubServerInfo,
                                                        IPSSite site) throws PSDataServiceException {
        PSDatabasePubServer dbPubServer = generateDBPubServer(site.getSiteId(), siteName, serverName, pubServerInfo);
        String error = serverFileService.testDatabasePubServer(dbPubServer);
        if (error != null)
        {
            throw new PSDataServiceException("Failed to connect to '" + serverName + "', the underlying error is: " + error);
        }
        return dbPubServer;
    }

    /**
     * Deletes the specified publish-server.
     * @param pubServer the publish-server, assumed not <code>null</code>.
     */
    private void deletePubServer(PSPubServer pubServer) throws PSPubServerServiceException {
        siteDataService.deletePublishingItemsByPubServer(pubServer);

        pubServerDao.deletePubServer(pubServer);

        if (!equalsIgnoreCase(pubServer.getPublishType(), PublishType.database.name()))
            return;

        try
        {
            PSDatabasePubServer dbServer = new PSDatabasePubServer(pubServer);
            serverFileService.deleteDatabasePubServer(dbServer);
        }
        catch (Throwable e)
        {
            throw new PSPubServerServiceException("Error removing datasource for server " + pubServer.getName() + " from site: " + pubServer.getSiteId(), e);
        }
    }

    private String updateDBConfigFiles(IPSSite site, String oldType, String currentServerName, String newServerName,
                                       PSPublishServerInfo pubServerInfo) throws PSDataServiceException {
        if (isDatabaseType(pubServerInfo.getType()))
        {
            PSDatabasePubServer dbPubServer = generateDBPubServer(site.getSiteId(), site.getName(), currentServerName, pubServerInfo);
            // Test the connection.
            String error = serverFileService.testDatabasePubServer(dbPubServer);
            if (error != null)
            {
                return error;
            }
            // Delete the old server configurations if the name is changed
            if (!equalsIgnoreCase(currentServerName, newServerName)
                    && isDatabaseType(oldType))
            {
                serverFileService.deleteDatabasePubServer(dbPubServer);
            }
            dbPubServer.setName(newServerName);
            serverFileService.saveDatabasePubServer(dbPubServer);
        }
        else if (isDatabaseType(oldType))
        {
            PSDatabasePubServer dbPubServer = new PSDatabasePubServer();
            dbPubServer.setSiteId(site.getSiteId());
            dbPubServer.setName(currentServerName);
            serverFileService.deleteDatabasePubServer(dbPubServer);
        }
        return null;
    }

    private PSDatabasePubServer generateDBPubServer(Long siteId, String siteName, String serverName, PSPublishServerInfo pubServerInfo)
    {
        PSDatabasePubServer dbPubServer = new PSDatabasePubServer();
        dbPubServer.setSiteId(siteId);
        dbPubServer.setName(serverName);
        dbPubServer.setServer(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DATABASE_SERVER_NAME));
        dbPubServer.setPort(Integer.parseInt(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_PORT_PROPERTY)));
        dbPubServer.setOracleSid(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_SID_PROPERTY));
        dbPubServer.setUserName(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_USER_ID_PROPERTY));
        dbPubServer.setPassword(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY));
        dbPubServer.setDatabase(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DATABASE_NAME_PROPERTY));
        dbPubServer.setOwner(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_OWNER_PROPERTY));
        dbPubServer.setDriverType(DriverType.valueOf(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY).toUpperCase()));
        return dbPubServer;
    }

    /**
     *
     * @param server
     * @param pubServerInfo
     * @param site
     */
    private void setProperties(PSPubServer server, PSPublishServerInfo pubServerInfo, IPSSite site) throws PSPubServerServiceException {
        //Grab old password for FTP in case we need it
        PSPubServerProperty oldPasswordProperty = server.getProperty(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);

        if (server.getProperties() != null)
            server.getProperties().clear();

        setPasswordProperty(server, pubServerInfo, oldPasswordProperty);

        setFolderProperty(server, pubServerInfo, site);

        setFormatProperty(server, pubServerInfo, site);

        List<PSPublishServerProperty> properties = pubServerInfo.getProperties();

        for (PSPublishServerProperty property : properties)
        {
            if (!excludedManagedProperties.contains(property.getKey()))
            {
                String value = property.getValue();
                if(ArrayUtils.contains(encryptableProperties, property.getKey()))
                {
                    try
                    {
                        value = encrypt(value);
                    }
                    catch (Exception e)
                    {
                        throw new PSPubServerServiceException("Error occurred while encrypting the server properties", e);
                    }
                }
                server.addProperty(property.getKey(),  value);
            }
        }
    }

    /**
     * Helper method to set the properties related to folder and own server settings
     *
     * @param server the server to be updated
     * @param pubServerInfo the wrapper object supplied as parameter
     * @param site the site associated with the server
     *
     */
    private void setFolderProperty(PSPubServer server, PSPublishServerInfo pubServerInfo, IPSSite site)
    {
        String ownServerFlagVal = pubServerInfo.findProperty(OWN_SERVER_FLAG);
        String defaultServerVal = pubServerInfo.findProperty(DEFAULT_SERVER_FLAG);
        String publishFolderVal = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY);
        String ownServerVal = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY);

        Boolean isOwnServerSet = Boolean.parseBoolean(ownServerFlagVal);
        Boolean isDefaultServerSet = Boolean.parseBoolean(defaultServerVal);

        setFolderProperty(server, site, publishFolderVal, isOwnServerSet, ownServerVal, isDefaultServerSet);
    }

    /**
     * Configure the folder property value and set on the supplied pub server.
     *
     * @param server The server to update
     * @param site The site for which the server is being updated
     * @param publishFolderValue The folder value to use, either explicit isOwnServerSet is <code>true</code>, or else
     * it is the base folder path used to compute the publish folder location from the site name
     * @param isOwnServerSet <code><code>false</code> if using tomcat and the default publishing folder configuration, <code>true</code>
     * if not tomcat or if not using the default publishing config.
     * @param ownServerVal If file system and using own server, this is the folder value to use.
     * @param isDefaultServerSet <code>true</code> if using the default folder location, <code>false</code> otherwise.
     *
     * @return <code>true</code> if the property of was changed, <code>false</code> if not
     */
    private boolean setFolderProperty(PSPubServer server, IPSSite site, String publishFolderValue, Boolean isOwnServerSet,
                                      String ownServerVal, Boolean isDefaultServerSet)
    {
        String propertyValue = EMPTY;
        Boolean ownServer = false;

        String publishType = server.getPublishType();
        String serverType = server.getServerType();

        // Set the properties when the publish type is filesystem
        if (publishType.equalsIgnoreCase(PublishType.filesystem.toString()) || publishType.equalsIgnoreCase(PublishType.filesystem_only.toString()))
        {
            if(isDefaultServerSet)
            {
                propertyValue = siteDataService.getDefaultPublishingRoot(site, server.getPublishType(), Boolean.toString(isDefaultServerSet));
            }
            else if (isOwnServerSet)
            {
                //The property value is set from the ownServer value
                propertyValue = ownServerVal;
                ownServer = true;
            }
        }

        // Set the properties when the publish type is FTP or SFTP or FTPS
        if (publishType.equalsIgnoreCase(PublishType.ftp.toString()) || publishType.equalsIgnoreCase(PublishType.sftp.toString())
               || publishType.equalsIgnoreCase(PublishType.ftps.toString()) || publishType.equalsIgnoreCase(PublishType.ftps_only.toString())
                || publishType.equalsIgnoreCase(PublishType.ftp_only.toString()) || publishType.equalsIgnoreCase(PublishType.sftp_only.toString()))
        {
            if(isDefaultServerSet)
            {
                propertyValue = siteDataService.getDefaultPublishingRoot(site, server.getPublishType(), publishFolderValue);
            }
            else if (isOwnServerSet)
            {
                //The property value is set from the ownServer value
                propertyValue = publishFolderValue;
                ownServer = true;
            }
        }

        if (serverType.equalsIgnoreCase("STAGING")
                && isDefaultServerSet
                && !StringUtils.containsIgnoreCase(propertyValue,"Staging/Deployment"))
        {
            propertyValue = propertyValue.replace("Deployment", "Staging" + File.separator + "Deployment");
        }

        propertyValue = normalizePath(propertyValue);

        // Add the folder property to the server properties list
        String oldVal = server.getPropertyValue(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY);
        server.addProperty(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY, propertyValue);

        // Add the own server property to the server properties list
        server.addProperty(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, ownServer.toString());

        return !propertyValue.equals(oldVal);
    }

    /**
     *
     * @param server
     * @param pubServerInfo
     * @param oldPasswordProperty
     */
    private void setPasswordProperty(PSPubServer server, PSPublishServerInfo pubServerInfo, PSPubServerProperty oldPasswordProperty)
    {
        Boolean isPasswordSet = Boolean.valueOf(pubServerInfo.findProperty(PASSWORD_FLAG)) ? true : false;
        Boolean isPrivateKeySet = Boolean.valueOf(pubServerInfo.findProperty(PRIVATE_KEY_FLAG)) ? true : false;

        String password = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
        String privateKey = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY);
        if(password == null){
            password="";
        }
        if(privateKey == null){
            privateKey = "";
        }

        if (oldPasswordProperty != null && password.equals(PASSWORD_ENTRY))
        {
            password =  oldPasswordProperty.getValue();
            final String pass = password;
            pubServerInfo.getProperties().stream()
                    .filter( p -> p.getKey().equals(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY))
                    .findFirst().ifPresent(p -> p.setValue(pass));
        }



        if (!isBlank(privateKey) && isPrivateKeySet )
        {
            server.addProperty(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY, privateKey);
        }
        else if (!isBlank(password))
        {
            server.addProperty(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY, password);
        }


    }

    /**
     * Helper method to set the properties related to format
     *
     * @param server the server to be updated
     * @param pubServerInfo the wrapper object supplied as parameter
     * @param site the site associated with the server
     *
     */
    private void setFormatProperty(PSPubServer server, PSPublishServerInfo pubServerInfo, IPSSite site)
    {
        Boolean isXmlSet = Boolean.valueOf(pubServerInfo.findProperty(XML_FLAG)) ? true : false;
        String propertyValue = isXmlSet ? XML_FLAG : HTML_FLAG;

        server.addProperty(IPSPubServerDao.PUBLISH_FORMAT_PROPERTY, propertyValue);
    }

    /**
     * Converts a <code>PSPubServer</code> object to <code>PSPublishServerInfo</code>
     * @param pubServer
     * @param site
     * @param includeProperties
     * @return a <code>PSPublishServer</code> object
     */
    private PSPublishServerInfo toPSPublishServerInfo(IPSPubServer pubServer, IPSSite site, boolean includeProperties) throws PSPubServerServiceException {
        PSPublishServerInfo serverInfo = new PSPublishServerInfo();

        Set<PSPubServerProperty> properties = pubServer.getProperties();

        if (includeProperties)
        {
            setFolderFlags(pubServer, site, serverInfo);

            setFormatFlags(pubServer, serverInfo);

            setPasswordFlags(pubServer, serverInfo);

            for (PSPubServerProperty property : properties)
            {
                String propertyName = property.getName();
                String propertyValue = property.getValue();
                if(ArrayUtils.contains(encryptableProperties, propertyName))
                {
                    try
                    {
                        propertyValue = decrypt(propertyValue);
                    }
                    catch (Exception e)
                    {
                        throw new PSPubServerServiceException("Error occurred while decrypting the server properties", e);
                    }
                }

                if (!propertyName.equalsIgnoreCase(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY)
                        && !propertyName.equalsIgnoreCase(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY)
                        && !propertyName.equalsIgnoreCase(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY)
                        && !propertyName.equalsIgnoreCase(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY)
                        && !propertyName.equalsIgnoreCase(IPSPubServerDao.PUBLISH_FORMAT_PROPERTY))
                {
                    PSPublishServerProperty serverProperty = new PSPublishServerProperty();
                    serverProperty.setKey(propertyName);
                    serverProperty.setValue(propertyValue);
                    serverInfo.getProperties().add(serverProperty);
                }

                if(propertyName.equals(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY)){
                    PSPublishServerProperty obfuPasswordProperty = new PSPublishServerProperty();
                    obfuPasswordProperty.setKey(propertyName);
                    obfuPasswordProperty.setValue(propertyValue);
                    serverInfo.getProperties().remove(obfuPasswordProperty);
                    obfuPasswordProperty.setValue(PASSWORD_ENTRY);
                    serverInfo.getProperties().add(obfuPasswordProperty);
                }
            }

            setPublishDates(pubServer, site, serverInfo);
        }

        serverInfo.setServerId(pubServer.getServerId());
        serverInfo.setServerName(pubServer.getName());
        serverInfo.setIsDefault(site.getDefaultPubServer() == pubServer.getServerId());
        serverInfo.setServerType(pubServer.getServerType());

        String pubType = pubServer.getPublishType();
        if (equalsIgnoreCase(pubType, PublishType.filesystem.toString())
                || equalsIgnoreCase(pubType, PublishType.ftp.toString())
                || equalsIgnoreCase(pubType, PublishType.sftp.toString())
                || equalsIgnoreCase(pubType, PublishType.filesystem_only.toString())
                || equalsIgnoreCase(pubType, PublishType.ftp_only.toString())
                || equalsIgnoreCase(pubType, PublishType.ftps.toString())
                || equalsIgnoreCase(pubType, PublishType.ftps_only.toString())
                || equalsIgnoreCase(pubType, PublishType.sftp_only.toString())
                || equalsIgnoreCase(pubType, PublishType.amazon_s3.toString())
                || equalsIgnoreCase(pubType, PublishType.amazon_s3_only.toString()))
        {
            if(PublishType.sftp_only.toString().equals(pubType)){
                String driver = findProperty(properties, IPSPubServerDao.PUBLISH_DRIVER_PROPERTY);
                if(driver != null && driver.equals(PSPubServerService.DRIVER_FTP)){
                    updateProperty(serverInfo.getProperties(),IPSPubServerDao.PUBLISH_DRIVER_PROPERTY,PSPubServerService.DRIVER_SFTP);
                }
            }
            serverInfo.setType(PUBLISH_FILE_TYPE);
            serverInfo.setIsModified(false);
        }
        else
        {
            if (equalsIgnoreCase(pubServer.getPublishType(), PublishType.database.toString()))
            {
                serverInfo.setType(PUBLISH_DB_TYPE);
                serverInfo.setIsModified(serverFileService.isServerModified(site.getSiteId(), pubServer.getName()));
            }
        }

        // only the default server can incrementally publish
        serverInfo.setCanIncrementalPublish(serverInfo.getIsDefault() || PSPubServer.STAGING.equalsIgnoreCase(pubServer.getServerType()));
        serverInfo.setIsFullPublishRequired(!pubServer.hasFullPublished());


        return serverInfo;
    }

    private String findProperty(Set<PSPubServerProperty> properties,String key)
    {
        for (PSPubServerProperty property : properties)
        {
            if (property.getName().equalsIgnoreCase(key))
                return property.getValue();
        }
        return null;
    }
    private void updateProperty(List<PSPublishServerProperty> properties,String key,String value)
    {
        for (PSPublishServerProperty property : properties)
        {
            if (property.getKey().equalsIgnoreCase(key))
                property.setValue(value);
        }

    }

    private void setPublishDates(IPSPubServer pubServer, IPSSite site, PSPublishServerInfo serverInfo)
    {
        IPSEdition fullEdition = sitePublishDao.findEdition(pubServer.getGUID(), PubType.FULL);
        IPSPubStatus fullPubStatus = publisherService.findLastPubStatusByEdition(fullEdition.getGUID());

        if (fullPubStatus != null)
            serverInfo.setLastFullPublishDate(fullPubStatus.getStartDate());

        // don't add incremental date if full pub required
        if (!pubServer.hasFullPublished())
            return;

        IPSEdition incrEdition = sitePublishDao.findEdition(pubServer.getGUID(), PubType.INCREMENTAL);
        if (incrEdition == null)
            return;

        IPSPubStatus incrPubStatus = publisherService.findLastPubStatusByEdition(incrEdition.getGUID());
        if (incrPubStatus != null)
            serverInfo.setLastIncrementalPublishDate(incrPubStatus.getStartDate());
    }

    /**
     * Helper method to set the flag properties related to password
     *
     * @param pubServer the wrapper object to be updated
     * @param serverInfo the server info to be handled
     *
     */
    private void setPasswordFlags(IPSPubServer pubServer, PSPublishServerInfo serverInfo)
    {
        String passwordValue = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
        PSPubServerProperty privateKeyProperty = pubServer.getProperty(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY);
        String privatekeyValue = EMPTY;
        if (privateKeyProperty != null)
        {
            privatekeyValue = pubServer.getProperty(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY).getValue();
        }

        if (isNotBlank(passwordValue))
        {
            if (equalsIgnoreCase(pubServer.getPublishType(), PublishType.database.toString()))
            {
                // Add the password property
                PSPublishServerProperty serverProperty = new PSPublishServerProperty();
                serverProperty.setKey(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
                serverProperty.setValue(PASSWORD_ENTRY);
                serverInfo.getProperties().add(serverProperty);
            }
            else
            {
                // Add the password flag
                PSPublishServerProperty serverProperty = new PSPublishServerProperty();
                serverProperty.setKey(PASSWORD_FLAG);
                serverProperty.setValue("true");
                serverInfo.getProperties().add(serverProperty);

                // Add the password property
                serverProperty = new PSPublishServerProperty();
                serverProperty.setKey(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
                // Do not send back real password but dummy value.  If this is passed back we keep same password
                serverProperty.setValue(PASSWORD_ENTRY);
                serverInfo.getProperties().add(serverProperty);

                // Add the private key flag
                serverProperty = new PSPublishServerProperty();
                serverProperty.setKey(PRIVATE_KEY_FLAG);
                serverProperty.setValue("false");
                serverInfo.getProperties().add(serverProperty);

                // Add the private key property
                serverProperty = new PSPublishServerProperty();
                serverProperty.setKey(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY);
                serverProperty.setValue(EMPTY);
                serverInfo.getProperties().add(serverProperty);
            }
        }

        if (isNotBlank(privatekeyValue))
        {
            // Add the password flag
            PSPublishServerProperty serverProperty = new PSPublishServerProperty();
            serverProperty.setKey(PRIVATE_KEY_FLAG);
            serverProperty.setValue("true");
            serverInfo.getProperties().add(serverProperty);

            // Add the private key property
            serverProperty = new PSPublishServerProperty();
            serverProperty.setKey(IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY);
            serverProperty.setValue(privatekeyValue);
            serverInfo.getProperties().add(serverProperty);

            // Add the password flag
            serverProperty = new PSPublishServerProperty();
            serverProperty.setKey(PASSWORD_FLAG);
            serverProperty.setValue("false");
            serverInfo.getProperties().add(serverProperty);

            // Add the password property
            serverProperty = new PSPublishServerProperty();
            serverProperty.setKey(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
            serverProperty.setValue(EMPTY);
            serverInfo.getProperties().add(serverProperty);
        }
    }

    /**
     * Helper method to set the flag properties related to the folder
     *
     * @param pubServer the publishing server
     * @param site the site associated to the server
     * @param serverInfo the wrapper object to be updated
     *
     */
    private void setFolderFlags(IPSPubServer pubServer, IPSSite site, PSPublishServerInfo serverInfo)
    {
        Boolean isOwnServer = Boolean.valueOf(pubServer.getProperty(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY)
                .getValue()) ? true : false;
        String folderValue = pubServer.getProperty(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY).getValue();
        String publishType = pubServer.getPublishType();

        PSPublishServerProperty defaultServerFlagProperty = new PSPublishServerProperty();
        defaultServerFlagProperty.setKey(DEFAULT_SERVER_FLAG);

        PSPublishServerProperty ownServeFlagProperty = new PSPublishServerProperty();
        ownServeFlagProperty.setKey(OWN_SERVER_FLAG);

        if (publishType.equalsIgnoreCase(PublishType.filesystem.toString()) || publishType.equalsIgnoreCase(PublishType.filesystem_only.toString()))
        {
            String defaultServerValue = EMPTY;
            String ownServerValue = EMPTY;

            if (isOwnServer)
            {
                defaultServerFlagProperty.setValue("false");
                ownServeFlagProperty.setValue("true");
                ownServerValue = folderValue;
            }
            else
            {
                // This is a default CM1 server
                defaultServerFlagProperty.setValue("true");
                ownServeFlagProperty.setValue("false");
                defaultServerValue = normalizePath(siteDataService.getBasePublishingRoot(folderValue, site.getName()));
                folderValue = defaultServerValue;
            }
            // Add the default flag property
            serverInfo.getProperties().add(defaultServerFlagProperty);

            // Add the own flag property
            serverInfo.getProperties().add(ownServeFlagProperty);

            // Add the default server property
            PSPublishServerProperty defaultServerProperty = new PSPublishServerProperty();
            defaultServerProperty.setKey(IPSPubServerDao.PUBLISH_DEFAULT_SERVER_PROPERTY);
            defaultServerProperty.setValue(defaultServerValue);
            serverInfo.getProperties().add(defaultServerProperty);

            // Add the own server property
            PSPublishServerProperty ownServerProperty = new PSPublishServerProperty();
            ownServerProperty.setKey(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY);
            ownServerProperty.setValue(normalizePath(ownServerValue));
            serverInfo.getProperties().add(ownServerProperty);

            // Add the folder property
            PSPublishServerProperty folderProperty = new PSPublishServerProperty();
            folderProperty.setKey(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY);
            folderProperty.setValue(normalizePath(folderValue));
            serverInfo.getProperties().add(folderProperty);
        }

        if (publishType.equalsIgnoreCase(PublishType.ftp.toString()) || publishType.equalsIgnoreCase(PublishType.ftp_only.toString())
                || publishType.equalsIgnoreCase(PublishType.ftps.toString()) || publishType.equalsIgnoreCase(PublishType.ftps_only.toString())
                || publishType.equalsIgnoreCase(PublishType.sftp.toString()) || publishType.equalsIgnoreCase(PublishType.sftp_only.toString()))
        {
            String folderPropertyValue = EMPTY;

            if (isOwnServer)
            {
                defaultServerFlagProperty.setValue("false");
                ownServeFlagProperty.setValue("true");
                folderPropertyValue = folderValue;
            }
            else
            {
                // This is a default CM1 server
                defaultServerFlagProperty.setValue("true");
                ownServeFlagProperty.setValue("false");
                folderPropertyValue = normalizePath(siteDataService.getBasePublishingRoot(folderValue, site.getName()));
            }
            // Add the default flag property
            serverInfo.getProperties().add(defaultServerFlagProperty);

            // Add the own flag property
            serverInfo.getProperties().add(ownServeFlagProperty);

            // Add the folder property
            PSPublishServerProperty folderProperty = new PSPublishServerProperty();
            folderProperty.setKey(IPSPubServerDao.PUBLISH_FOLDER_PROPERTY);
            folderProperty.setValue(folderPropertyValue);
            serverInfo.getProperties().add(folderProperty);
        }
    }

    /**
     *
     * @param pubServer
     * @param serverInfo
     */
    private void setFormatFlags(IPSPubServer pubServer, PSPublishServerInfo serverInfo)
    {
        String formatValue = pubServer.getProperty(IPSPubServerDao.PUBLISH_FORMAT_PROPERTY).getValue();
        String htmlValue = formatValue.equalsIgnoreCase(HTML_FLAG) ? "true" : "false";
        String xmlValue = formatValue.equalsIgnoreCase(XML_FLAG) ? "true" : "false";

        // Add the HTML flag setting
        PSPublishServerProperty serverProperty = new PSPublishServerProperty();
        serverProperty.setKey(HTML_FLAG);
        serverProperty.setValue(htmlValue);
        serverInfo.getProperties().add(serverProperty);

        // Add the XML flag setting
        serverProperty = new PSPublishServerProperty();
        serverProperty.setKey(XML_FLAG);
        serverProperty.setValue(xmlValue);
        serverInfo.getProperties().add(serverProperty);
    }

    /**
     * Helper method to set the publish type according to the driver.
     *
     * @param server
     * @param type: file/database
     * @param driver Local/FTP/Oracle/MS SQL/MySQL
     */
    private void setPublishType(IPSPubServer server, String type, String driver, String secure, boolean isDefaultServer, String format)
    {
        // Check for file type and local driver
        if (type.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_LOCAL))
        {
            String publishTypeValue = PublishType.filesystem.toString();
            if (format.equalsIgnoreCase(XML_FLAG))
            {
                publishTypeValue = PublishType.filesystem_only.toString();
            }
            server.setPublishType(publishTypeValue);
            return;
        }

        // Check for file type and FTP driver
        if (type.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_FTP))
        {
            boolean isSecureFtp = secure != null && secure.equalsIgnoreCase("true") ? true : false;
            String publishTypeValue = isDefaultServer ? PublishType.ftp.toString() : PublishType.ftp_only.toString();
            if (isSecureFtp)
            {
                publishTypeValue = isDefaultServer ? PublishType.sftp.toString() : PublishType.sftp_only.toString();
            }

            if (isDefaultServer && format.equalsIgnoreCase(XML_FLAG))
            {
                publishTypeValue = PublishType.ftp_only.toString();
                if (isSecureFtp)
                {
                    publishTypeValue = PublishType.sftp_only.toString();
                }
            }
            server.setPublishType(publishTypeValue);
            return;
        }
        if (type.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_SFTP))
        {

            String publishTypeValue =  isDefaultServer ? PublishType.sftp.toString() : PublishType.sftp_only.toString();


            if (isDefaultServer && format.equalsIgnoreCase(XML_FLAG))
            {
                publishTypeValue = PublishType.sftp_only.toString();

            }
            server.setPublishType(publishTypeValue);
            return;
        }
        // Check for file type and FTPS driver
        if (type.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_FTPS))
        {
            server.setPublishType(PublishType.ftps_only.toString());
            return;
        }
        if(type.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_AMAZONS3)){
            String publishTypeValue = isDefaultServer ? PublishType.amazon_s3.toString() : PublishType.amazon_s3_only.toString();
            server.setPublishType(publishTypeValue);
        }

        if(type.equalsIgnoreCase(PUBLISH_FILE_TYPE) && driver.equalsIgnoreCase(DRIVER_AMAZONS3)){
            String publishTypeValue = isDefaultServer ? PublishType.amazon_s3.toString() : PublishType.amazon_s3_only.toString();
            server.setPublishType(publishTypeValue);
        }
        // Check for database type
        if (isDatabaseType(type))
        {
            server.setPublishType(PublishType.database.toString());
            return;
        }
    }

    /**
     * If the supplied server is a staging server then updates the publishing type.
     * Checks the current publish type and if the supplied type ends with _only then removes _only and resets the publish type.
     * @param server assumed not <code>null</code>
     */
    private void updatePublishTypeForStaging(IPSPubServer server)
    {
        String pubType = server.getPublishType();
        if(PSPubServer.STAGING.equalsIgnoreCase( server.getServerType())){
            if(pubType.endsWith("_only")){
                server.setPublishType(pubType.replace("_only", ""));
            }
        }
    }

    /**
     *
     * @param serverName
     * @param previousServername
     * @param siteId
     * @throws PSPubServerServiceException
     * @throws PSValidationException
     */
    private void validateServerName(String serverName, String previousServername, String siteId) throws PSPubServerServiceException, PSValidationException {
        PSValidationErrorsBuilder builder = validateParameters("validateServerName").rejectIfNull("SERVER_NAME_FIELD",
                serverName).throwIfInvalid();

        // Make sure server name is not blank
        if (isBlank(serverName))
        {
            builder.rejectField(SERVER_NAME_FIELD, SERVER_NAME_IS_EMPTY, serverName);
            builder.throwIfInvalid();
        }

        // Too long server name
        if (serverName.length() > NAME_MAX_LENGHT)
        {
            builder.rejectField(SERVER_NAME_FIELD, SERVER_NAME_IS_TOO_LONG, serverName);
            builder.throwIfInvalid();
        }

        // Validate characters
        Pattern pattern = Pattern.compile("[\\w-\\.]+");
        Matcher matcher = pattern.matcher(serverName);
        if (!matcher.matches())
        {
            builder.rejectField(SERVER_NAME_FIELD, SERVER_NAME_IS_INVALID, serverName);
            builder.throwIfInvalid();
        }

        // Unique server names
        List<PSPublishServerInfo> servers = getPubServerList(siteId);
        for (PSPublishServerInfo server : servers)
        {
            // If is an update and the name did not change, ignore it.
            if (serverName.equalsIgnoreCase(previousServername) && server.getServerName().equalsIgnoreCase(serverName))
                return;

            if(server.getServerName().equalsIgnoreCase(serverName))
            {
                if (previousServername.isEmpty())
                {
                    builder.rejectField(SERVER_NAME_FIELD, MessageFormat.format(SERVER_NAME_CREATE_IS_NOT_UNIQUE, serverName, server.getServerName()), serverName);
                    builder.throwIfInvalid();
                }
                else
                {
                    builder.rejectField(SERVER_NAME_FIELD, MessageFormat.format(SERVER_NAME_UPDATE_IS_NOT_UNIQUE, previousServername, serverName, server.getServerName()), serverName);
                    builder.throwIfInvalid();
                }
            }
        }
    }

    /**
     * Validate the port value name. Cannot be empty and only contains numbers characters
     * 0-9, strip leading and trailing spaces.
     *
     * @param port the port value, may not <code>null</code>
     */
    private void validatePort(String port) throws PSValidationException {
        PSValidationErrorsBuilder builder = validateParameters("validatePort").rejectIfNull("PORT_FIELD", port)
                .throwIfInvalid();

        // Validate characters
        Pattern pattern = Pattern.compile("[\\d]+");
        Matcher matcher = pattern.matcher(port);
        if (!matcher.matches())
        {
            builder.rejectField(PORT_FIELD, PORT_VALUE_IS_INVALID, PORT_FIELD);
            builder.throwIfInvalid();
        }
    }

    /**
     * Checks if the server belongs to the current site
     *
     * @param siteGuid guid of the site. Never <code>null</code>
     * @param pubServer server being retrieved <code>null</code>
     * @return true if the server belongs to the current site.
     * @throws Exception
     */
    private boolean serverBelongsToSite(IPSGuid siteGuid, IPSPubServer pubServer) throws Exception
    {
        List<PSPubServer> servers = pubServerDao.findPubServersBySite(siteGuid);
        for (PSPubServer server : servers)
        {
            if(server.getServerId() == pubServer.getServerId())
                return true;
        }
        return false;
    }

    /**
     * Validates the properties that are different given the type and driver
     * specified for the publishing server.
     *
     * @param pubServerInfo
     */
    private void validateProperties(PSPublishServerInfo pubServerInfo, String siteId, boolean isNew) throws PSValidationException, PSPubServerServiceException {
        String driver = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY);
        String type = pubServerInfo.getType();
        String serverType = pubServerInfo.getServerType();
        String secure = StringUtils.defaultString(pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_SECURE_FTP_PROPERTY));
        String port = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_PORT_PROPERTY);
        String privateKeyFlag = pubServerInfo.findProperty(PRIVATE_KEY_FLAG);
        boolean isPrivateKeySelected = privateKeyFlag != null && privateKeyFlag.equalsIgnoreCase("true") ? true : false;

        if (isBlank(driver))
        {
            validateParameters("validateProperties").rejectField(IPSPubServerDao.PUBLISH_DRIVER_PROPERTY.toString(),
                    PROPERTY_FIELD_REQUIRED, driver).throwIfInvalid();
        }

        if(isNew && StringUtils.isNotBlank(serverType) && PSPubServer.STAGING.equalsIgnoreCase( serverType)){
            //Make sure there are no other staging pub server for the supplied type.
            List<PSPublishServerInfo> pubSrvs =  getPubServerList(siteId);
            for (PSPublishServerInfo srvInfo : pubSrvs) {
                if(PSPubServer.STAGING.equalsIgnoreCase( srvInfo.getServerType())){
                    throw new PSPubServerServiceException("Only one staging server is allowed per site.");
                }
            }
        }

        if (type.equalsIgnoreCase(PUBLISH_FILE_TYPE))
        {
            if(driver.equalsIgnoreCase(DRIVER_LOCAL))
            {
                validatePropertiesByDriver(pubServerInfo, FILESYSTEM_PROPERTIES);

                String ownProperty = StringUtils.defaultString(pubServerInfo.findProperty(OWN_SERVER_FLAG));
                if (ownProperty.equalsIgnoreCase(Boolean.TRUE.toString()))
                {
                    String ownServer = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY);
                    if (isBlank(ownServer))
                        validateParameters("validateProperties").rejectField(IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY.toString(),
                                PROPERTY_FIELD_REQUIRED, ownServer).throwIfInvalid();
                }

                String defaultProperty = StringUtils.defaultString(pubServerInfo.findProperty(DEFAULT_SERVER_FLAG));
                if (defaultProperty.equalsIgnoreCase(Boolean.TRUE.toString()))
                {
                    String defaultServer = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_DEFAULT_SERVER_PROPERTY);
                    if (isBlank(defaultServer))
                        validateParameters("validateProperties").rejectField(IPSPubServerDao.PUBLISH_DEFAULT_SERVER_PROPERTY.toString(),
                                PROPERTY_FIELD_REQUIRED, defaultServer).throwIfInvalid();
                }
            }
            else if(driver.equalsIgnoreCase(DRIVER_FTP) || driver.equalsIgnoreCase(DRIVER_FTPS) || driver.equalsIgnoreCase(DRIVER_SFTP)  )
            {
                // validate the port value
                if (port != null)
                {
                    validatePort(port);
                }
                //Taking care of Upgrade Cases and new cases
                if(driver.equalsIgnoreCase(DRIVER_SFTP)){
                    secure = "true";
                }

                String[] propsToCheck = secure.equalsIgnoreCase(Boolean.TRUE.toString())?
                        (isPrivateKeySelected?SFTP_PROPERTIES_WITHOUT_PASS:SFTP_PROPERTIES):
                        FTP_PROPERTIES;
                if(driver.equalsIgnoreCase(DRIVER_FTPS)) {
                    propsToCheck = FTPS_PROPERTIES;
                }
                validatePropertiesByDriver(pubServerInfo, propsToCheck);
            }
            else if(driver.equalsIgnoreCase(DRIVER_AMAZONS3)){
                validatePropertiesByDriver(pubServerInfo, AMAZON_S3_PROPERTIES);
            }
            return;
        }
        else if(isDatabaseType(type))
        {
            // validate the port value
            if (port != null)
            {
                validatePort(port);
            }
            String[] propsToCheck = driver.equalsIgnoreCase(DRIVER_ORACLE)?ORACLE_PROPERTIES:
                    driver.equalsIgnoreCase(DRIVER_MSSQL)?MSSQL_PROPERTIES:DB_PROPERTIES;
            validatePropertiesByDriver(pubServerInfo, propsToCheck);
            return;
        }
    }

    /**
     * Validates properties for based on an specific driver.
     * @param pubServerInfo
     * @param requieredProperties
     */
    private void validatePropertiesByDriver(PSPublishServerInfo pubServerInfo, String[] requieredProperties) throws PSValidationException {
        PSValidationErrorsBuilder builder = validateParameters("validatePropertiesByDriver");
        for (String property : requieredProperties)
        {
            if (isBlank(pubServerInfo.findProperty(property)))
            {
                if(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY.equals(property)){
                    if(!isEC2Instance()){
                        builder.rejectField(property, PROPERTY_FIELD_REQUIRED, property);
                    }
                }else if(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY.equals(property)){
                    if(!isEC2Instance()){
                        builder.rejectField(property, PROPERTY_FIELD_REQUIRED, property);
                    }
                }

            }
        }
        builder.throwIfInvalid();
    }

    /**
     *
     * @param previousDefaultServer
     * @param currentServer
     * @param site
     * @param isDefaultServer
     * @return
     */
    private boolean updatePreviousDefaultPubServer(PSPubServer previousDefaultServer, PSPubServer currentServer,
                                                   IPSSite site, boolean isDefaultServer)
    {
        if ((previousDefaultServer.getServerId() != currentServer.getServerId()) && isDefaultServer)
        {
            String currentPublishingType = previousDefaultServer.getPublishType();
            String newPublishingType = previousDefaultServer.getPublishType();

            PSPubServer oldDefaultServer = pubServerDao.loadPubServerModifiable(previousDefaultServer.getGUID());

            if (currentPublishingType.equalsIgnoreCase(PublishType.filesystem.toString()))
                newPublishingType = PublishType.filesystem_only.toString();

            if (currentPublishingType.equalsIgnoreCase(PublishType.ftp.toString()))
                newPublishingType = PublishType.ftp_only.toString();
            if (currentPublishingType.equalsIgnoreCase(PublishType.ftps.toString()))
                newPublishingType = PublishType.ftps_only.toString();

            if (currentPublishingType.equalsIgnoreCase(PublishType.sftp.toString()))
                newPublishingType = PublishType.sftp_only.toString();

            previousDefaultServer.setPublishType(newPublishingType);

            siteDataService.updateServerEditions(site, oldDefaultServer, previousDefaultServer, false);
            pubServerDao.savePubServer(previousDefaultServer);
            return true;
        }

        return false;
    }

    /**
     * Helper method that return the normalized path so all back slashes are
     * converted into forward slashes. This conversion is done just before
     * sending the value to the client, and before saving the value into the
     * server properties, just to make sure the correct value is saved properly.
     *
     * @param path the path as string to be replaced
     *
     */
    private String normalizePath(String path)
    {
        String result = path.replaceAll("\\\\{1,}", "/");

        return result;
    }

    /**
     * Return the GUID for the given id supplied as parameter
     *
     * @param siteId the site id to build the GUID
     *
     */
    private IPSGuid getSiteGuid(String siteId)
    {
        return guidMgr.makeGuid(siteId, PSTypeEnum.SITE);
    }

    /**
     * Return the GUID for the given id supplied as parameter
     *
     * @param serverId the pub server id to build the GUID
     *
     */
    private IPSGuid getPubServerGuid(String serverId)
    {
        return guidMgr.makeGuid(serverId, PSTypeEnum.PUBLISHING_SERVER);
    }

    private boolean tryToLockSite(String siteId)
    {
        if (StringUtils.isBlank(siteId))
            return false;

        boolean locked = lockMgr.getLock(siteId);
        if (!locked)
        {
            log.error("Timeout attempting to lock publishing server for modification : " + siteId);
            throw new RuntimeException("Timeout attempting to lock publishing server for modification, please try again later.");
        }
        return locked;
    }

    private void tryToUnlockSite(String siteId)
    {
        boolean unlocked = lockMgr.releaseLock(siteId);
        if (!unlocked)
        {
            log.warn("Unabled to release lock for site: " + siteId);
        }
    }

    // Maximum name lenght when creating/updating a server
    private static final Integer NAME_MAX_LENGHT = 50;

    private static final String SERVER_NAME_FIELD = "serverName";

    private static final String PORT_FIELD = "port";

    // Server error messages
    private static final String SERVER_NAME_IS_EMPTY = "Name is a required field.";

    private static final String SERVER_NAME_IS_INVALID = "Invalid character in server name. Characters allowed are: a-z, 0-9, ., _ and -.";

    private static final String SERVER_NAME_IS_TOO_LONG = "Server name cannot have more than " + NAME_MAX_LENGHT + " characters.";

    // Server creating error messages
    private static String SERVER_NAME_CREATE_IS_NOT_UNIQUE = "Cannot create server ''{0}'' because a server named ''{1}'' already exists.";

    // Server updating error messages
    private static String SERVER_NAME_UPDATE_IS_NOT_UNIQUE = "Cannot rename server ''{0}'' to ''{1}'' because a server named ''{2}'' already exists.";

    // Port error message
    private static final String PORT_VALUE_IS_INVALID = "Invalid character in port field. Characters allowed are: 0-9.";

    private static String PUBLISH_FILE_TYPE = "File";
    private static String PUBLISH_DB_TYPE = "Database";
    private static String DRIVER_LOCAL = "Local";
    private static String DRIVER_FTP = "FTP";
    private static String DRIVER_SFTP = "SFTP";
    private static String DRIVER_FTPS = "FTPS";
    private static String DRIVER_AMAZONS3 = "AMAZONS3";
    private static String DRIVER_ORACLE = "Oracle";
    private static String DRIVER_MSSQL = "MSSQL";
    private static String PASSWORD_FLAG = "passwordFlag";
    private static String PRIVATE_KEY_FLAG = "privateKeyFlag";
    private static String OWN_SERVER_FLAG = "ownServerFlag";
    private static String PUBLISH_SECURE_SITE_CONF = "publishSecureSiteConfigOnExactPath";

    private static String DEFAULT_SERVER_FLAG = "defaultServerFlag";
    private static String XML_FLAG = "XML";
    private static String HTML_FLAG = "HTML";

    private static String PROPERTY_FIELD_REQUIRED = "This is a required field.";

    // File system Properties names that should exist in the object
    private String[] FILESYSTEM_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, XML_FLAG, HTML_FLAG, OWN_SERVER_FLAG, DEFAULT_SERVER_FLAG};

    // File system Properties names that should exist in the object
    private String[] FTP_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, XML_FLAG, HTML_FLAG, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY,
                    IPSPubServerDao.PUBLISH_PORT_PROPERTY, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY,
                    IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, OWN_SERVER_FLAG, DEFAULT_SERVER_FLAG,
                    IPSPubServerDao.PUBLISH_FOLDER_PROPERTY};
    // File system Properties names that should exist in the object
    private String[] FTPS_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, XML_FLAG, HTML_FLAG, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY,
                    IPSPubServerDao.PUBLISH_PORT_PROPERTY, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY,
                    IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, OWN_SERVER_FLAG, DEFAULT_SERVER_FLAG,
                    IPSPubServerDao.PUBLISH_FOLDER_PROPERTY};

    // File system Properties names that should exist in the object when the server is SFTP
    private String[] SFTP_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, XML_FLAG, HTML_FLAG, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY,
                    IPSPubServerDao.PUBLISH_PORT_PROPERTY, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY,
                    IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, OWN_SERVER_FLAG, DEFAULT_SERVER_FLAG,
                    IPSPubServerDao.PUBLISH_FOLDER_PROPERTY};

    // File system Properties names that should exist in the object when the server is SFTP with private key
    private String[] SFTP_PROPERTIES_WITHOUT_PASS =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, XML_FLAG, HTML_FLAG,
                    IPSPubServerDao.PUBLISH_PORT_PROPERTY, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY,
                    IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, OWN_SERVER_FLAG, DEFAULT_SERVER_FLAG,
                    IPSPubServerDao.PUBLISH_FOLDER_PROPERTY};

    // Database Properties names that should exist in the object
    private String[] DB_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY,
                    IPSPubServerDao.PUBLISH_PORT_PROPERTY, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY,
                    IPSPubServerDao.PUBLISH_DATABASE_NAME_PROPERTY, IPSPubServerDao.PUBLISH_DATABASE_SERVER_NAME};

    // Database Properties names that should exist in the object
    private String[] MSSQL_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY, IPSPubServerDao.PUBLISH_PORT_PROPERTY,
                    IPSPubServerDao.PUBLISH_USER_ID_PROPERTY, IPSPubServerDao.PUBLISH_DATABASE_NAME_PROPERTY,
                    IPSPubServerDao.PUBLISH_DATABASE_SERVER_NAME, IPSPubServerDao.PUBLISH_OWNER_PROPERTY};

    // Oracle Database Properties names that should exist in the object
    private String[] ORACLE_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY,
                    IPSPubServerDao.PUBLISH_PORT_PROPERTY, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY,
                    IPSPubServerDao.PUBLISH_DATABASE_SERVER_NAME, IPSPubServerDao.PUBLISH_SID_PROPERTY,
                    IPSPubServerDao.PUBLISH_SCHEMA_PROPERTY};

    // AMAZON S3 BUCKET PROPERTIES Properties names that should exist in the object
    private String[] AMAZON_S3_PROPERTIES =
            {IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY,
                    IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY, IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY,
                    IPSPubServerDao.PUBLISH_EC2_REGION};


    private static String[] specialCasesValues =
            {PASSWORD_FLAG, PRIVATE_KEY_FLAG, OWN_SERVER_FLAG, DEFAULT_SERVER_FLAG, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY,
                    IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY, IPSPubServerDao.PUBLISH_DEFAULT_SERVER_PROPERTY,
                    IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, IPSPubServerDao.PUBLISH_FOLDER_PROPERTY,
                    IPSPubServerDao.PUBLISH_FORMAT_PROPERTY, XML_FLAG, HTML_FLAG};

    private List<String> excludedManagedProperties = new ArrayList<>(Arrays.asList(specialCasesValues));


    public static final String[] encryptableProperties =
            {IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY, IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY};

    @ToDoVulnerability
    private String encrypt(String estr) throws Exception {
        PSAesCBC aes = new PSAesCBC();
        return aes.encrypt(estr, IPSPubServerDao.encryptionKey);
    }

    @ToDoVulnerability
    private String decrypt(String dstr) throws Exception {
        PSAesCBC aes = new PSAesCBC();
        return aes.decrypt(dstr, IPSPubServerDao.encryptionKey);
    }

    /**
     *  Converts the password property of the publishing server info from
     *	Base64 to a String.
     *
     * @param pubServerInfo The publishing server info containing the password property
     */
    private void convertPasswordFromBase64(PSPublishServerInfo pubServerInfo) {
        String encodedPassword = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
        if(encodedPassword != null){
            PSPublishServerProperty pubProperty = new PSPublishServerProperty();
            pubProperty.setKey(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
            pubProperty.setValue(encodedPassword);
            pubServerInfo.getProperties().remove(pubProperty);

            //After upgrade or for older unit tests, the password may not be encoded.
            String decodedPassword = null;
            try{
                decodedPassword = new String(Base64.decode(encodedPassword));
            }catch(Exception e){
                log.warn("Unable to decode stored Publishing Server password, proceeding as if it is clear text.  Try editing the Publishing server to clear this Warning.");
                decodedPassword = encodedPassword;
            }

            pubProperty.setValue(decodedPassword);
            pubServerInfo.getProperties().add(pubProperty);
        }
    }

    /**
     *
     */
    @Override
    public boolean checkPubServerConfig(PSPublishServerInfo pubServerInfo, IPSSite site)
    {
        IPSPubServer pubServer = pubServerDao.findPubServer(pubServerInfo.getServerId());
        String publishType = pubServer.getPublishType();
        PSBaseDeliveryHandler handler = null;
        boolean result = true;
        if(this.handlerMap.containsKey(publishType))
            handler = (PSBaseDeliveryHandler) this.handlerMap.get(publishType);
        if(handler != null)
            result = handler.checkConnection(pubServer, site);
        return result;
    }

    @Override
    public PSPubInfo getS3PubInfo(IPSGuid siteId) throws PSPubServerServiceException {
        if (siteId == null)
            throw new IllegalArgumentException("siteId must not be null");
        PSPubInfo pubInfo = null;

        IPSPubServer pubServer = getDefaultPubServer(siteId);
        String pubType = pubServer.getPublishType();
        PSPubServerProperty buckProp =  pubServer.getProperty(IPSPubServerDao.PUBLISH_AS3_BUCKET_PROPERTY);
        String bucketProperty = null;
        if(buckProp != null){
            bucketProperty = buckProp.getValue();
        }


        PSPubServerProperty accessKeyProp = pubServer.getProperty(IPSPubServerDao.PUBLISH_AS3_ACCESSKEY_PROPERTY);

        //As will be null in case of EC2 instance
        String accessKey = null;
        if (accessKeyProp != null) {
            accessKey = accessKeyProp.getValue();

            try {
                if (accessKey != null)
                    accessKey = decrypt(accessKey);
            } catch (Exception e) {
                throw new PSPubServerServiceException(e);
            }
        }

        String securityKey = null;
        PSPubServerProperty securityKeyProp = pubServer.getProperty(IPSPubServerDao.PUBLISH_AS3_SECURITYKEY_PROPERTY);
        if (securityKeyProp != null) {
            securityKey = securityKeyProp.getValue();
            try {
                securityKey = decrypt(securityKey);
            } catch (Exception e) {
                throw new PSPubServerServiceException(e);
            }
        }
        String region = null;
        PSPubServerProperty regioProp = pubServer.getProperty(IPSPubServerDao.PUBLISH_EC2_REGION);
        if (regioProp != null){
            region = regioProp.getValue();
        }
        if(equalsIgnoreCase(pubType, PublishType.amazon_s3.toString())
                || equalsIgnoreCase(pubType, PublishType.amazon_s3_only.toString())){
            pubInfo = new PSPubInfo(bucketProperty,accessKey,securityKey,region);
        }else{
            //In some cases the S3 publisher may not be the default - loop through pubservers to see if any are pointed at s3
            List<PSPublishServerInfo> pubservers = getPubServerList(siteId.toString());
            for(PSPublishServerInfo p : pubservers){
                String driver = p.findProperty("driver");
                if(equalsIgnoreCase(driver, PublishType.amazon_s3.toString())
                        || equalsIgnoreCase(pubType, PublishType.amazon_s3_only.toString())){
                    pubInfo = new PSPubInfo(bucketProperty,accessKey,securityKey,region);
                    break;
                }
            }
        }
        return pubInfo;
    }

    @Override
    public PSPubServer findPubServer(long serverId)	throws PSPubServerServiceException
    {
        PSPubServer server = null;
        try
        {
            server = pubServerDao.findPubServer(serverId);
        }
        catch(Exception e)
        {
            log.error("Error finding the pub server for the supplied id: " + serverId, e);
        }
        return server;
    }

    @Override
    public String getDefaultAdminURL(String siteName) throws PSPubServerServiceException {

        IPSSite site = siteMgr.findSite(siteName);
        if(site == null){
            PSEnumVals sites = siteDataService.getChoices();
            List siteNames = sites.getEntries();
            if(siteNames != null && siteNames.size() > 0){
                siteName = ((PSEnumVals.EnumVal)siteNames.get(0)).getValue();
                site = siteMgr.findSite(siteName);
            }
        }
        if(site == null){
            log.info("No Site found in the system");
            return null;
        }else {
            PSPubServer currentDefaultServer = getDefaultPubServer(site.getGUID());
            String adminUrl = currentDefaultServer.getPropertyValue("publishServer");
            return adminUrl;
        }
    }

}
