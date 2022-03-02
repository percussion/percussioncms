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
import com.percussion.sitemanage.service.IPSSitePublishService;
import com.percussion.utils.guid.IPSGuid;

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

    PSSiteSummary findByLegacySiteId(String id, boolean isValidate);

    PSSiteSummary findSummary(String name) throws IPSGenericDao.LoadException;

    boolean updateSite(IPSSite site, String newName, String newDescrption) throws PSNotFoundException;

    void updateSitePublishProperties(IPSSite site, PSSitePublishProperties publishProps) throws PSNotFoundException;

    IPSSite createSite(String siteName) throws PSNotFoundException;

    IPSEdition findEdition(IPSGuid pubServerId, IPSSitePublishService.PubType pubType);

    String getPublishingRoot(String basePath, String siteName);

    String getPublishingBase(String siteRoot, String siteName);

    String getPublishingDeliveryRoot(String siteName, String publishServerType, String deliveryRootPath);

    String makePublishingDir(String siteName);

    void convertToSummary(IPSSite site, PSSiteSummary summary);

    void createPublishingItemsForPubServer(IPSSite site, PSPubServer pubServer, boolean isDefaultServer) throws PSNotFoundException;

    String getWebServerFileSystemRoot();

    void setWebServerFileSystemRoot(String fileSystemRoot);

    String getWebServerPort();

    void setWebServerPort(String webServerPort);

    void updateServerEditions(IPSSite site, PSPubServer oldServer, PSPubServer server, boolean isDefaultServer) throws PSNotFoundException;

    void deleteSite(String name);

    String getSiteDeliveryType(IPSSite site) throws PSNotFoundException;

    String getStagingDeliveryType(IPSSite site) throws PSNotFoundException;

    void addPublishNow(IPSSite site) throws PSNotFoundException;

    void addUnpublishNow(IPSSite site) throws PSNotFoundException;

    void addStagingPublishNow(IPSSite site) throws PSNotFoundException;

    void addStagingUnpublishNow(IPSSite site) throws PSNotFoundException;

    void setPublishServerAsDefault(IPSSite site, PSPubServer pubServer) throws PSNotFoundException;

    void deletePublishingItemsByPubServer(PSPubServer pubServer) throws PSNotFoundException;

    boolean saveSite(PSSite site) throws IPSPubServerService.PSPubServerServiceException, PSNotFoundException;
}
