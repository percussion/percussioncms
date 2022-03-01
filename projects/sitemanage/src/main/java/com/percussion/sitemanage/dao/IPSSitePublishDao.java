package com.percussion.sitemanage.dao;

import com.percussion.pubserver.IPSPubServerService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSitePublishService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;

import java.util.List;

public interface IPSSitePublishDao {
    /**
     * Constant for the full edition name.
     */
    String FULL = "FULL";
    /**
     * Constant for the full site content list name.
     */
    String FULL_SITE = "FULL_SITE";
    /**
     * Constant for the full asset content list name.
     */
    String FULL_ASSET = "FULL_ASSET";
    /**
     * Constant for the staging site content list name.
     */
    String STAGING_SITE = "STAGING_SITE";
    /**
     * Constant for the staging asset content list name.
     */
    String STAGING_ASSET = "STAGING_ASSET";

     List<PSSiteSummary> findAllSummaries();

    /**
     * Finds the site summary by the legacy ID.
     *
     * @param id         the legacy ID of the site, not <code>null</code>.
     * @param isValidate it is <code>true</code> if wants to validate the site that contains "category" folder;
     *                   otherwise don't validate the returned site object. The returned object should not to be validated if it is
     *                   used for assembly process, such as previewing or publishing.
     * @return the site with the specified ID. It may be <code>null</code> if cannot find the site.
     */
    PSSiteSummary findByLegacySiteId(String id, boolean isValidate);

    PSSiteSummary findSummary(String name) throws IPSGenericDao.LoadException;

    /**
     * Updates the specified site and its related edition/content-list with the
     * new name and description.
     *
     * @param site          the existing site, not <code>null</code>.
     * @param newName       the new name of the site, not blank.
     * @param newDescrption the new description of the site, may be blank.
     * @return <code>true</code> if the update resulted in a change to a pubserver definition, <code>false</code>
     * if not.
     */
    boolean updateSite(IPSSite site, String newName, String newDescrption) throws PSNotFoundException;

    /**
     * Updates the specified site with passed in publishing properties and updates
     * Content-list with user passed in delivery type and this is part of the
     * publish properties.
     *
     * @param site         the existing site, not <code>null</code>.
     * @param publishProps publishing properties to be updated on the site. not <code>null</code>.
     */
    void updateSitePublishProperties(IPSSite site, PSSitePublishProperties publishProps) throws PSNotFoundException;

    IPSSite createSite(String siteName) throws PSNotFoundException;

    /**
     * Find the specified edition
     *
     * @param pubServerId The pub server guid, not <code>null</code>.
     * @param pubType     The publication type, not <code>null</code>.
     * @return The edition, or <code>null</code> if not found.
     */
    IPSEdition findEdition(IPSGuid pubServerId, IPSSitePublishService.PubType pubType);

    /**
     * Generates the publishing root location for the specified site.  This location is tomcat specific, which means
     * that the resulting path will end in {siteName}apps/ROOT.  See {@link #makePublishingDir(String)}.
     *
     * @param basePath assumed to be the main tomcat server directory.  May not be <code>null</code>.
     * @param siteName never blank.
     * @return the absolute path of the root publishing location, never blank.
     */
     String getPublishingRoot(String basePath, String siteName);

    /**
     * Extracts the base path from the specified publishing root location.  The base path represents the location of the
     * tomcat server.
     *
     * @param siteRoot the publishing root location, never blank.
     * @param siteName never blank.
     * @return the base path, never <code>null</code>.
     */
     String getPublishingBase(String siteRoot, String siteName);

    /**
     * Generates the directory which will be used as the root publishing location for the
     * specified site.
     *
     * @param siteName          never blank.
     * @param publishServerType never blank
     * @param deliveryRootPath  never blank
     * @return the relative directory of the publishing path for the site, never blank.  Forward slash is used as the
     * file separator.
     */
     String getPublishingDeliveryRoot(String siteName, String publishServerType, String deliveryRootPath);

    /**
     * Generates the directory under a tomcat server which will be used as the root publishing location for the
     * specified site.
     *
     * @param siteName never blank.
     * @return the relative directory of the publishing path for the site, never blank.  Forward slash is used as the
     * file separator.
     */
    String makePublishingDir(String siteName);

    void convertToSummary(IPSSite site, PSSiteSummary summary);

    /**
     * Creates the content lists and the publishing editions for the given
     * {@link PSPubServer publish server}. It creates one content list only
     * (for pages) if the server is {@link IPSSiteDataService.PublishType} Database or it is for
     * xml publishing, otherwise it creates two content lists, one for pages and
     * one for assets.
     *
     * @param site      The {@link IPSSite site}, must not be <code>null</code>.
     * @param pubServer the {@link PSPubServer publish server}, must not be
     *                  <code>null</code>.
     */
    void createPublishingItemsForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSNotFoundException;

    /**
     * Location of where the files get published on the filesystem
     * if one is not provided by the user.
     *
     * @return should be never <code>null</code>, but not guaranteed.
     */
    String getWebServerFileSystemRoot();

    void setWebServerFileSystemRoot(String fileSystemRoot);

    /**
     * The default server port of the webserver.
     *
     * @return never <code>null</code>.
     */
    String getWebServerPort();

    void setWebServerPort(String webServerPort);

    /**
     * Update the editions associated with a publish server being updated.
     *
     * @param site
     * @param oldServer
     * @param server
     */
    void updateServerEditions(IPSSite site, PSPubServer oldServer, PSPubServer server, boolean isDefaultServer) throws PSNotFoundException;

    void deleteSite(String name);

    /**
     * Gets delivery type for this site based on the default publish server.
     * Doesn't iterate all editions and Contentlists. Just get the first Edition
     * and content list and find delivery type from there.
     *
     * @param site the existing site, not <code>null</code>.
     * @return the delivery type for the given site
     * @throws PSErrorException if no edition is found for the default publish
     *                          server.
     */
    String getSiteDeliveryType(IPSSite site) throws PSNotFoundException;

    /**
     * Gets delivery type for this site based on the staging server.
     * Doesn't iterate all editions and Contentlists. Just get the first Edition
     * and content list and find delivery type from there.
     *
     * @param site the existing site, not <code>null</code>.
     * @return the delivery type for the given site
     * @throws PSErrorException if no edition is found for the staging publish
     *                          server.
     */
    String getStagingDeliveryType(IPSSite site) throws PSNotFoundException;

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     *
     * @param site may not be <code>null</code>.
     */
    void addPublishNow(IPSSite site) throws PSNotFoundException;

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     *
     * @param site may not be <code>null</code>.
     */
    void addUnpublishNow(IPSSite site) throws PSNotFoundException;

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     *
     * @param site may not be <code>null</code>.
     */
    void addStagingPublishNow(IPSSite site) throws PSNotFoundException;

    /**
     * Creates the publish now infrastructure for the specified site.  This includes a new content list and edition.
     *
     * @param site may not be <code>null</code>.
     */
    void addStagingUnpublishNow(IPSSite site) throws PSNotFoundException;

    /**
     * Updates the specified publish server and site to match the new default server id.
     *
     * @param site      the existing site, not <code>null</code>.
     * @param pubServer The publish server, may not be <code>null</code>.
     */
     void setPublishServerAsDefault(IPSSite site, PSPubServer pubServer) throws PSNotFoundException;

    /**
     * Deletes all editions associated with the specified publish server id.
     *
     * @param pubServer The pubServer, may not be <code>null</code>.
     */
    void deletePublishingItemsByPubServer(PSPubServer pubServer) throws PSNotFoundException;

    boolean saveSite(PSSite site) throws PSErrorException, IPSPubServerService.PSPubServerServiceException, PSNotFoundException;
}
