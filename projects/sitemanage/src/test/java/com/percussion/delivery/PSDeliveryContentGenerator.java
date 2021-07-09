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

package com.percussion.delivery;

import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.comments.data.PSComment;
import com.percussion.comments.data.PSCommentModeration;
import com.percussion.comments.data.PSSiteComments;
import com.percussion.comments.service.IPSCommentsService;
import com.percussion.comments.service.impl.PSCommentsService;
import com.percussion.content.PSGenericContentGenerator;
import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.DeliveryServicesContent;
import com.percussion.delivery.data.DeliveryServicesContent.CommentService.Comments;
import com.percussion.delivery.data.DeliveryServicesContent.CommentService.Comments.Comment;
import com.percussion.delivery.data.DeliveryServicesContent.MembershipService.Memberships;
import com.percussion.delivery.data.DeliveryServicesContent.MembershipService.Memberships.Membership;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.membership.service.IPSMembershipService;
import com.percussion.pagemanagement.data.PSNonSEOPagesRequest;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageReportLine;
import com.percussion.pagemanagement.data.PSSEOStatistics;
import com.percussion.pagemanagement.service.IPSPageChangeListener;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSFolderPermission.Access;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSUnassignedResults;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSPubInfo;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSErrorException;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class provide services to create content in a delivery server.
 * You have to specify a server URL, an XML file defining the content to
 * be generated, and an optional user and password to authenticate against
 * the delivery server. You are also able to do a cleanup of the remote
 * data.
 *
 * @author miltonpividori
 *
 */
public class PSDeliveryContentGenerator extends PSGenericContentGenerator<DeliveryServicesContent>
{
    private static final String ADD_COMMENT_URL = "/perc-comments-services/comment";

    private static final String REGISTER_ACCOUNT_URL = "/perc-membership-services/membership/user";

    /**
     * The comment service instance. It's used to handle comments
     * generation. Some dependencies of the comment service are stubbed.
     */
    private IPSCommentsService commentService;

    /**
     * Delivery info service used to store connection data.
     * Used by unsecured and secured connection.
     *
     */
    private IPSDeliveryInfoService deliveryInfoService;

    public PSDeliveryContentGenerator(String serverUrl, InputStream xmlData)
    {
        this(serverUrl, xmlData, null, null,null);
    }

    /**
     * Constructor for a Delivery Content Generator using non-secure connection.
     *
     * @param serverUrl the server url
     * @param xmlData the input Stream for the xml data
     * @param username the username to use in the authentication
     * @param password the password for that user
     */
    public PSDeliveryContentGenerator(final String serverUrl, InputStream xmlData, final String username,
                                      final String password, final String lic_id)
    {
        super(serverUrl, xmlData, username, password, lic_id);

        /*
         * The comment service needs an IPSDeliveryInfoService instance. So here
         * we create one according to the server url, user and password given.
         */
        deliveryInfoService = new IPSDeliveryInfoService()
        {

            @Override
            public List<PSDeliveryInfo> findAll()
            {
                PSDeliveryInfo ds = new PSDeliveryInfo(serverUrl);
                ds.setUsername(username);
                ds.setPassword(password);

                ArrayList<PSDeliveryInfo> list = new ArrayList<PSDeliveryInfo>();
                list.add(ds);

                return list;
            }
            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
            @Override
            public String findBaseByServerType(String type) {
                // TODO Auto-generated method stub
                return null;
            }
            @Override
            public PSDeliveryInfo findByService(String service, String type) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PSDeliveryInfo findByService(String service, String type, String adminURL) {
                return null;
            }

            @Override
            public PSDeliveryInfo findByURL(String arg0) throws MalformedURLException
            {
                // TODO Auto-generated method stub
                return null;
            }
        };

        // As the page service is not needed here, it's stubed.
        commentService = new PSCommentsService(deliveryInfoService, getPageServiceStub(), getFolderHelperStub(),
                getSiteDaoStub());

    }

    /**
     * Constructor for a delivery content generator that uses a secure connection (SSL).
     *
     * @param serverUrl the non-secure url
     * @param serverAdminUrl the secure url
     * @param allowSelfSignedCertificate <code>true</code> if the allowSelfSignedCertificate
     * property is set to <code>true</code>
     * @param xmlData the input stream with the xml data
     * @param username the username for the secure authentication
     * @param password the password to use when authenticating the secure user.
     */
    public PSDeliveryContentGenerator(final String serverUrl, final String serverAdminUrl,
                                      final Boolean allowSelfSignedCertificate, InputStream xmlData, final String username,
                                      final String password, final String lic_id){

        super(serverUrl, xmlData, username, password, lic_id);

        /*
         * The comment service needs an IPSDeliveryInfoService instance. So here
         * we create one according to the server url, user and password given.
         */
        deliveryInfoService = new IPSDeliveryInfoService()
        {

            @Override
            public List<PSDeliveryInfo> findAll()
            {
                PSDeliveryInfo ds = new PSDeliveryInfo(serverUrl);
                ds.setUsername(username);
                ds.setPassword(password);
                ds.setAdminUrl(serverAdminUrl);
                ds.setAllowSelfSignedCertificate(allowSelfSignedCertificate);

                ArrayList<PSDeliveryInfo> list = new ArrayList<PSDeliveryInfo>();
                list.add(ds);

                return list;
            }
            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
            @Override
            public String findBaseByServerType(String type) {
                // TODO Auto-generated method stub
                return null;
            }
            @Override
            public PSDeliveryInfo findByService(String service, String type) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PSDeliveryInfo findByService(String service, String type, String adminURL) {
                return null;
            }

            @Override
            public PSDeliveryInfo findByURL(String arg0) throws MalformedURLException
            {
                // TODO Auto-generated method stub
                return null;
            }
        };

        // As the page service is not needed here, it's stubed.
        commentService = new PSCommentsService(deliveryInfoService, getPageServiceStub(), getFolderHelperStub(),
                getSiteDaoStub());

    }

    /**
     * Returns a stub of IPSPageService.
     *
     * @return An IPSPageService implementation.
     */
    private IPSPageService getPageServiceStub()
    {
        return new IPSPageService()
        {
            public List<PSPage> findAll() throws DataServiceLoadException, DataServiceNotFoundException
            {
                return null;
            }

            public PSValidationErrors validate(PSPage object)
            {
                return null;
            }

            public PSPage save(PSPage page)
            {
                return null;
            }

            public PSPage load(String id)
            {
                return null;
            }

            public String getPageViewUrl(String id)
            {
                return null;
            }

            public String getPageEditUrl(String id)
            {
                return null;
            }

            public PSPage findPageByPath(String fullPath) throws PSPageException
            {
                return null;
            }

            public PSPage findPage(String name, String folderPath) throws PSPageException
            {
                return null;
            }

            public List<PSSEOStatistics> findNonSEOPages(PSNonSEOPagesRequest request) throws PSPageException
            {
                return null;
            }

            public PSPage find(String id)
            {
                return null;
            }

            public void delete(String id, boolean force)
            {

            }

            @Override
            public void delete(String id, boolean force, boolean purgeItem) {

            }

            public void delete(String id)
            {

            }

            public boolean isPageItem(String id)
            {
                return false;
            }

            public String copy(String id, boolean addToRecent)
            {
                return null;
            }

            @Override
            public String copy(String id, String targetFolder, boolean addToRecent) throws DataServiceSaveException {
                return null;
            }

            public void notifyPageChange(PSPageChangeEvent pageChangeEvent)
            {

            }

            public void addPageChangeListener(IPSPageChangeListener pageChangeListener)
            {

            }

            public PSNoContent savePageMetadata(String pageId)
            {
                return null;
            }

            @SuppressWarnings("unused")
            @Override
            public PSPagedItemList findPagesByTemplate(String templateId, Integer startIndex, Integer maxResults,
                                                       String sortColumn, String sortOrder, String pageId) throws PSPageException
            {
                return null;
            }


            public PSNoContent changeTemplate(String pageId, String templateId)
            {
                return null;
            }

            @Override
            public String generateNewPageName(String pageName, String folderPath)
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void updateTemplateMigrationVersion(String pageId)
            {

            }

            @Override
            public void updateMigrationEmptyWidgetFlag(String pageId, boolean flag)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean getMigrationEmptyWidgetFlag(String pageId)
            {
                return false;
                // TODO Auto-generated method stub

            }

            @Override
            public PSNoContent validateDelete(String arg){
                return new PSNoContent();
            }

            @Override
            public PSNoContent clearMigrationEmptyFlag(String pageid){
                return null;
            }

            @Override
            public PSUnassignedResults getUnassignedPagesBySite(String sitename,Integer startIndex, Integer maxResults){
                return null;
            }

            @Override
            public List<PSPageReportLine> findAllPages(String siteName) throws PSReportFailedToRunException{
                return null;
            }

        };
    }

    /**
     * Returns a stub of IPSFolderHelper.
     *
     * @return An IPSFolderHelper implementation.
     */
    private IPSFolderHelper getFolderHelperStub()
    {
        return new IPSFolderHelper()
        {
            public void addItem(String path, String id) throws Exception
            {

            }

            public String concatPath(String start, String... end)
            {
                return null;
            }

            public void createFolder(String path) throws Exception
            {

            }

            public void createFolder(String path, Access acl) throws Exception
            {

            }

            public void deleteFolder(String path) throws Exception
            {

            }

            @Override
            public void deleteFolder(String path, boolean recycleFolder) throws Exception {

            }

            public List<String> findChildren(String path) throws Exception
            {
                return null;
            }

            public IPSItemSummary findFolder(String path) throws Exception
            {
                return null;
            }

            public PSFolderProperties findFolderProperties(String id) throws PSErrorException
            {
                return null;
            }

            public IPSItemSummary findItem(String path) throws Exception
            {
                return null;
            }

            public PSPathItem findItemById(String id)
            {
                return null;
            }

            @Override
            public PSPathItem findItemById(String id, String relationshipTypeName) {
                return null;
            }

            public PSItemProperties findItemProperties(String path) throws Exception
            {
                return null;
            }

            @Override
            public PSItemProperties findItemProperties(String path, String relationshipTypeName) throws Exception {
                return null;
            }

            public List<IPSItemSummary> findItems(String path) throws Exception
            {
                return null;
            }

            public Number findLegacyFolderIdFromPath(String path) throws Exception
            {
                return null;
            }

            public String findPathFromLegacyFolderId(Number id) throws Exception
            {
                return null;
            }

            public List<String> findPaths(String itemId) throws Exception
            {
                return null;
            }

            @Override
            public List<String> findPaths(String itemId, String relationshipTypeName) throws Exception {
                return null;
            }

            public Access getFolderAccessLevel(String id)
            {
                return null;
            }

            public List<IPSSite> getItemSites(String contentId)
            {
                return null;
            }

            public IPSGuid getParentFolderId(IPSGuid itemId)
            {
                return null;
            }

            public IPSGuid getParentFolderId(IPSGuid itemId, boolean isRequired)
            {
                return null;
            }

            public String getUniqueFolderName(String parentPath, String baseName)
            {
                return null;
            }

            public String getUniqueNameInFolder(String parentPath, String baseName, String suffix, int startingIndex,
                                                boolean skipFirstIndex)
            {
                return null;
            }

            public boolean hasFolderPermission(String folderId, Access acl)
            {
                return false;
            }

            public void moveItem(String targetFolderPath, String itemPath, boolean isFolder)
            {

            }

            public String name(String path)
            {
                return null;
            }

            public String parentPath(String path)
            {
                return null;
            }

            public String pathSeparator()
            {
                return null;
            }

            public PathTarget pathTarget(String path)
            {
                return null;
            }

            @Override
            public PathTarget pathTarget(String path, boolean shouldRecycle) {
                return null;
            }

            public void removeItem(String path, String itemId, boolean purgeItem) throws Exception
            {

            }

            public void renameFolder(String path, String name) throws Exception
            {

            }

            public void saveFolderProperties(PSFolderProperties folder)
            {

            }

            public PSPathItem setFolderAccessLevel(PSPathItem item)
            {
                return null;
            }

            public List<PSPathItem> setFolderAccessLevel(List<PSPathItem> items)
            {
                return null;
            }

            public boolean validateFolderPermissionForDelete(String folderId)
            {
                return false;
            }

            public boolean validateFolderReservedName(String name)
            {
                return true;
            }

            public int getValidWorkflowId(PSFolderProperties folder)
            {
                return 0;
            }

            public PSFolder getRootFolderForAsset(String assetId)
            {
                return null;
            }

            public String getRootLevelFolderAllowedSitesPropertyValue(String assetId)
            {
                return null;
            }

            @Override
            public boolean isFolderValidForRecycleOrRestore(String targetPath, String relationshipTypeName,
                                                            String targetRelType, String origRelType) {
                return false;
            }

            public PSItemProperties findItemPropertiesById(String id) throws Exception
            {
                return null;
            }

            @Override
            public PSItemProperties findItemPropertiesById(String id, String relationshipTypeName) throws Exception {
                return null;
            }

            @Override
            public List<String> findItemIdsByPath(String path) throws Exception
            {
                return null;
            }

            @Override
            public PSPair<String, String> fixupLastModified(IPSGuid id, String userName, Date lastModified,
                                                            boolean isPublishable)
            {
                return null;
            }

            @Override
            public int getDefaultWorkflowId()
            {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getFolderPath(String path)
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<IPSItemSummary> findItems(String string, boolean foldersOnly)
            {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }


    /*private IPSPubServerService getPSPubServiceStub(){
        return new IPSPubServerService() {
            @Override
            public PSPublishServerInfo getPubServer(String siteId, String serverId) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public List<PSPublishServerInfo> getPubServerList(String name) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public PSPublishServerInfo createPubServer(String siteId, String serverName, PSPublishServerInfo pubServerInfo) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public PSPublishServerInfo updatePubServer(String siteId, String serverId, PSPublishServerInfo pubServerInfo) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public List<PSPublishServerInfo> deleteServer(String siteId, String serverId) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public void deletePubServersBySite(IPSGuid siteId) {

            }

            @Override
            public void stopPublishing(String jobId) throws PSPubServerServiceException {

            }

            @Override
            public Map<String, Boolean> getAvailableDrivers() {
                return null;
            }

            @Override
            public Boolean isDefaultServerModified(String siteId) {
                return null;
            }

            @Override
            public String getDefaultFolderLocation(String siteId, String publishType, String driver, String serverType) {
                return null;
            }

            @Override
            public PSPubServer getDefaultPubServer(IPSGuid siteId) {
                return null;
            }

            @Override
            public PSPubServer getStagingPubServer(IPSGuid siteId) {
                return null;
            }

            @Override
            public PSPubServer createDefaultPubServer(IPSSite site, String serverName) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public boolean updateDefaultFolderLocation(IPSSite site, String root, String oldName) {
                return false;
            }

            @Override
            public boolean checkPubServerConfig(PSPublishServerInfo pubServerInfo, IPSSite site) {
                return false;
            }

            @Override
            public PSPubInfo getS3PubInfo(IPSGuid siteId) throws Exception {
                return null;
            }

            @Override
            public PSPubServer findPubServer(long serverId) throws PSPubServerServiceException {
                return null;
            }

            @Override
            public String getDefaultAdminURL(String siteName) throws PSPubServerServiceException {
                return null;
            }
        };

    }*/

    private IPSiteDao getSiteDaoStub()
    {
        return new IPSiteDao()
        {
            public void addPublishNow(IPSSite site)
            {

            }

            public void addUnpublishNow(IPSSite site)
            {

            }

            public PSSite createSiteWithContent(String origId, String newName)
            {
                return null;
            }

            public List<PSSiteSummary> findAllSummaries()
            {
                return null;
            }

            public PSSiteSummary findByLegacySiteId(String id, boolean isValidate)
            {
                return null;
            }

            public PSSiteSummary findByPath(String path)
            {
                return null;
            }

            @Override
            public PSSiteSummary findByName(String name) {
                return null;
            }

            public PSSiteSummary findSummary(String id)
            {
                return null;
            }

            public String getSiteDeliveryType(IPSSite site)
            {
                return null;
            }

            public boolean updateSite(IPSSite site, String newName, String newDescrption)
            {
                return false;
            }

            public void updateSitePublishProperties(IPSSite site, PSSitePublishProperties publishProps)
            {

            }

            public void delete(String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException
            {

            }

            public PSSite find(String id) throws com.percussion.share.dao.IPSGenericDao.LoadException
            {
                return null;
            }

            public List<PSSite> findAll() throws com.percussion.share.dao.IPSGenericDao.LoadException
            {
                return null;
            }

            public PSSite save(PSSite object) throws com.percussion.share.dao.IPSGenericDao.SaveException
            {
                return null;
            }


        };
    }

    /**
     * Extracts the sitename from the given pagepath.
     *
     * @param pagePath A pagepath where the sitename will be
     * extracted from.
     * @return The sitename present in the given pagepath.
     */
    private String getSiteName(String pagePath)
    {
        Validate.notNull(pagePath);

        String[] tmp = pagePath.split("/");

        if (tmp == null || tmp.length < 2)
            throw new RuntimeException("Pagepath is invalid");

        return tmp[1];
    }

    /**
     * Given an absolute pagepath, it return a pagepath relative
     * to the site. If the following absolute pagepath is given:
     * '/Site1/folder/page', then this relative pagepath will
     * be returned: '/folder/page'.
     *
     * @param absolutePagepath An absolute pagepath.
     * @return The relative pagepath.
     */
    private String getRelativePagePath(String absolutePagepath)
    {
        Validate.notNull(absolutePagepath);

        return absolutePagepath.substring(1 + getSiteName(absolutePagepath).length());
    }

    public static void main(String[] args) throws Exception
    {
        // If the generation is done under unsecured connection.
        if (args.length == 4)
        {
            // URL - User - Password - XML file.
            // (Example) http://generic:9970 Admin demo
            // C:\membershipAccountsXml.xml
            PSGenericContentGenerator.runMainMethod(args, PSDeliveryContentGenerator.class);
        }
        else
        {
            // Secured connection.
            // URL - AdminUser - AdminPassword - XML file - Secure URL - Allow
            // self signed certificate.
            // (Example) http://generic:9970 {admin user} {admin password}
            // C:\membershipAccountsXml.xml https://generic:8443 true
            PSGenericContentGenerator.runMainMethodSecure(args, PSDeliveryContentGenerator.class);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.content.PSGenericContentGenerator#getRootDataType()
     */
    protected Class<DeliveryServicesContent> getRootDataType()
    {
        return DeliveryServicesContent.class;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.content.PSGenericContentGenerator#generateAllContent()
     */
    protected void generateAllContent()
    {
        generateComments();
        generateMembershipAccounts();
    }

    /**
     * Generates content for the comment service only. Automatically
     * loads the XML data.
     */
    public void generateComments()
    {
        // Load XML data in case this method is called directly from another
        // class.
        if (!loadXmlData())
            return;

        if(content.getCommentService() != null &&
                content.getCommentService().getComments() != null)
        {
            log.info("Generating comments service content");

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            PSDeliveryInfo server = new PSDeliveryInfo(serverUrl, username, password);
            server.setAdminUrl(serverUrl);
            server.setAllowSelfSignedCertificate(true);

            for (Comments allComments : content.getCommentService().getComments())
            {
                createComments(deliveryClient, server, allComments);
            }
        }
    }

    /**
     * Generates content for the membership service only. Automatically
     * loads the XML data.
     */
    public void generateMembershipAccounts()
    {
        // Load XML data in case this method is called directly from another
        // class.
        if (!loadXmlData())
            return;

        if(content.getMembershipService() !=null &&
                content.getMembershipService().getMemberships() != null)
        {
            log.info("Generating membership service content");

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            PSDeliveryInfo server = new PSDeliveryInfo(serverUrl, username, password);
            server.setAdminUrl(serverUrl);
            server.setAllowSelfSignedCertificate(true);

            for (Memberships memberships : content.getMembershipService().getMemberships())
            {
                createMemberships(deliveryClient, server, memberships);
            }
        }
    }

    /**
     * Creates comments according to the given Comments and PSDeliveryInfo objects.
     * It handles the case where Comments count is greater than 1, equals to 1, and
     * less than 1, adding " Copy N" at the end of every comment field.
     *
     * @param deliveryClient A PSDeliveryClient object to communicate with the
     * delivery server. Never <code>null</code>.
     * @param server A PSDeliveryInfo with information of the delivery server (host,
     * port, etc). Never <code>null</code>.
     * @param allComments A Comments object with all comments objects. Never
     * <code>null</code>.
     */
    private void createComments(PSDeliveryClient deliveryClient, PSDeliveryInfo server, Comments allComments)
    {
        int realCount = allComments.getCount();
        int count = realCount <= 0 ? 1: realCount;

        for (int i=1; i<=count; i++)
        {
            for (Comment comment : allComments.getComment())
            {
                // Create a list of key/value pairs to send with a POST method and
                // add a comment.
                List<NameValuePair> values = new ArrayList<NameValuePair>();

                values.add(new NameValuePair("site", getSiteName(allComments.getPagePath())));
                values.add(new NameValuePair("pagepath", getRelativePagePath(allComments.getPagePath())));

                values.add(new NameValuePair("title", addCopyNumber(comment.getTitle(), i, realCount)));
                values.add(new NameValuePair("text", addCopyNumber(comment.getBody(), i, realCount)));
                values.add(new NameValuePair("username", addCopyNumber(comment.getUsername(), i, realCount)));
                values.add(new NameValuePair("email", addCopyNumber(comment.getEmail(), i, realCount)));
                values.add(new NameValuePair("url", addCopyNumber(comment.getUrl(), i, realCount)));

                try
                {
                    log.info("Creating comment '" + comment.getTitle() + "'");
                    deliveryClient.setLicenseOverride(licenseId);
                    deliveryClient.push(
                            new PSDeliveryActionOptions()
                                    .setDeliveryInfo(server)
                                    .setActionUrl(ADD_COMMENT_URL)
                                    .setHttpMethod(HttpMethodType.POST)
                                    .addSuccessfullHttpStatusCode(HttpStatus.SC_SEE_OTHER),
                            values);
                }
                catch (Exception e)
                {
                    log.error("There was an error in creating the comment: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates membership accounts according to the given Memberships and PSDeliveryInfo objects.
     *
     * @param deliveryClient A PSDeliveryClient object to communicate with the
     * delivery server. Never <code>null</code>.
     * @param server A PSDeliveryInfo with information of the delivery server (host,
     * port, etc). Never <code>null</code>.
     * @param memberships A Memberships object with all memberships objects. Never
     * <code>null</code>.
     */
    private void createMemberships(PSDeliveryClient deliveryClient, PSDeliveryInfo server, Memberships memberships)
    {
        for (Membership membership : memberships.getMembership())
        {
            // Create a list of key/value pairs (JSON) to send with a POST method and
            // register a membership account.
            JSONObject registerObject = new JSONObject();
            registerObject.put("email", membership.getEmailAddress());
            registerObject.put("password",membership.getPassword());
            registerObject.put("confirmationRequired",membership.isConfirmationRequired());
            registerObject.put("confirmationPage","null");
            log.info("JSON registration: "+registerObject.toString());
            try
            {
                log.info("Creating membership account '" + membership.getEmailAddress() + "'");
                deliveryClient.setLicenseOverride(licenseId);
                deliveryClient.push(
                        new PSDeliveryActionOptions()
                                .setDeliveryInfo(server)
                                .setActionUrl(REGISTER_ACCOUNT_URL)
                                .setHttpMethod(HttpMethodType.POST)
                                .addSuccessfullHttpStatusCode(HttpStatus.SC_SEE_OTHER),
                        registerObject.toString());
            }
            catch (Exception e)
            {
                log.error("There was an error in creating the membership: " + e.getMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.content.PSGenericContentGenerator#cleanupAllContent()
     */
    protected void cleanupAllContent()
    {
        cleanupComments();
        cleanupMemberships();
    }

    /**
     * Cleans up content for the comment service only, removing all copies
     * ("Copy N") in the delivery server. Automatically loads the XML data.
     *
     * The cleanup method works looking for all comments defined in the XML
     * file. Then it gets all the comments from the delivery server, according
     * to the site and pagepath in the XML file. To mark one for removal, it checks
     * whether all the fields values in delivery comment start with the corresponding
     * one in the XML file.
     */
    public void cleanupComments()
    {
        // Load XML data in case this method is called directly from another
        // class.
        if (!loadXmlData())
            return;

        if(content.getCommentService() != null &&
                content.getCommentService().getComments() != null)
        {
            log.info("Cleaning up comments service content");

            // Create the moderation object to store comments present in the XML
            // file, which will be deleted.
            PSCommentModeration moderation = new PSCommentModeration();
            String sitename="";
            for (Comments comments : content.getCommentService().getComments())
            {
                // Get all comments according to the site and pagepath given.
                // Remove the leading slash from the pagepath.
                sitename = getSiteName(comments.getPagePath());
                List<PSComment> commentsList = getAllComments(sitename,
                        getRelativePagePath(comments.getPagePath()).substring(1));

                if (commentsList == null || commentsList.size() == 0)
                {
                    log.info("No comments returned by the delivery server");
                    return;
                }

                PSSiteComments siteComments = new PSSiteComments();
                siteComments.setSite(sitename);

                for (PSComment com : commentsList)
                {
                    if (!commentPresentInXml(comments, com))
                        continue;

                    siteComments.getComments().add(com.getCommentId());
                }
                moderation.getDeletes().add(siteComments);

            }

            commentService.moderate(sitename,moderation);
        }
    }

    /**
     * Cleans up content for the membership service only, removing all existing
     * memberships in the delivery server. Automatically loads the XML data. It
     * runs before generation methods, cleaning memberships that already exist,
     * so then they can be recreated.
     *
     * The cleanup method works looking for all membership accounts defined in
     * the XML file. Then it gets all the memberships from the delivery server.
     * To mark one for removal, it checks whether the email address in the
     * delivery membership is equal to the corresponding one in the XML file.
     */
    public void cleanupMemberships()
    {
        // Load XML data in case this method is called directly from another
        // class.
        if (!loadXmlData())
            return;

        if (content.getMembershipService() != null && content.getMembershipService().getMemberships() != null)
        {
            log.info("Cleaning up memberships service content");

            for (Memberships memberships : content.getMembershipService().getMemberships())
            {
                for (Membership membership : memberships.getMembership())
                {
                    try
                    {
                        deleteAccount(membership.getEmailAddress());
                        log.info("Account " + membership.getEmailAddress() + " deleted.");
                    }
                    catch (Exception e)
                    {
                        log.info("Account " + membership.getEmailAddress() + " didn't exist or it wasn't deleted.");
                    }
                }
            }
        }
    }

    /***
     * Calls delivery membership service using a secure connection and deletes
     * the account that matches the given email. If there isn't a membership
     * account with that email, it throws an exception.
     *
     * @param email The plain user email.
     */
    private void deleteAccount(String email)
    {
        PSDeliveryClient deliveryClient = new PSDeliveryClient();
        PSDeliveryInfo server = deliveryInfoService.findByService(PSDeliveryInfo.SERVICE_MEMBERSHIP);
        String url = "/" + PSDeliveryInfo.SERVICE_MEMBERSHIP + IPSMembershipService.MEMBERSHIP
                + IPSMembershipService.ADMIN_ACCOUNT + "/" + email;
        deliveryClient.setLicenseOverride(this.licenseId);
        deliveryClient.push(new PSDeliveryActionOptions(server, url, HttpMethodType.DELETE, true), "");
    }

    /**
     * Checks if the given comment is present in the XML file by comparing each
     * field value with the XML one. If all the fields in the given comment
     * <em>start with</em> the value defined in the XML, then the comment is
     * marked for removal. This algorithm may lead to incorrect deletion if
     * a comment has all its fields as a substring of another one's.
     *
     * @param psComment A PSComment object to check if it's present in the XML
     * file. Should never be <code>null</code>.
     * @return <code>true</code> if the comment is present, <code>false</code>
     * otherwise.
     */
    private boolean commentPresentInXml(Comments comments, PSComment psComment)
    {
        for (Comment com : comments.getComment())
        {
            // Compare each comment's field.
            if (StringUtils.startsWith(psComment.getCommentText(), com.getBody()) &&
                    StringUtils.startsWith(psComment.getUserEmail(), com.getEmail()) &&
                    StringUtils.startsWith(psComment.getCommentTitle(), com.getTitle()) &&
                    StringUtils.startsWith(psComment.getUserLinkUrl(), com.getUrl()) &&
                    StringUtils.startsWith(psComment.getUserName(), com.getUsername()))
                return true;
        }

        return false;
    }

    /**
     * Gets all comments from the delivery server, according to the site and
     * pagepath given.
     *
     * @param site The comment site. Should never be <code>null</code>.
     * @param pagepath The comment pagepath. It has to be in the following
     * form: 'folder/path' (without the site nor leading slash). Should never
     * be <code>null</code>.
     * @return A list of all comments in the delivery server that match with
     * site and pagepath given.
     */
    private List<PSComment> getAllComments(String site, String pagepath)
    {
        try
        {
            commentService.setLicenseOverride(licenseId);
            return commentService.getCommentsOnPage(site, pagepath, 0, 0);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * Given a comment's field value, an index and count (these ones corresponds
     * to the values in the 'count' attribute in the XML file), it returns a
     * new field value with the 'Copy N' string, only if it's necessary. If
     * 'count' is equals to 1, then the unmodified field's value is returned.
     * Otherwise, a " Copy N" string is added at the end of the field's value,
     * and this one is returned.
     *
     * @param fieldValue A comment's field value.
     * @param index An index (goes from 1 to 'count').
     * @param count The count value defined in the XML file.
     * @return
     */
    private String addCopyNumber(String fieldValue, int index, int count)
    {
        return count == 1 ? fieldValue : (fieldValue + " Copy " + index);
    }


}
