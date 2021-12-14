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

package com.percussion.sitemanage.service;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishProperties;
import com.percussion.sitemanage.data.PSSitePublishRequest;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;
import com.percussion.sitemanage.service.IPSSitePublishService.PubType;
import com.percussion.test.PSServletTestCase;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.List;

/**
 * Test the site manager.
 */
@Category(IntegrationTest.class)
public class PSSiteDaoTest extends PSServletTestCase
{

    private static final Logger log = LogManager.getLogger(PSSiteDaoTest.class);

    /**
     * Test all crud methods.
     *  
     * @throws Exception
     */
    public void testCRUD() throws Exception
    {
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);

        PSSite site1 = createSite("Site1-" + System.currentTimeMillis(), "Site 1");
        final String fileExt = "xhtml";
        final String siteProtocol = "http";
        final String defaultDocument = "index.xhtml";
        final String canonicalDist = "sections";
        site1.setDefaultFileExtention(fileExt);
        site1.setCanonical(true);
        site1.setSiteProtocol(siteProtocol);
        site1.setDefaultDocument(defaultDocument);
        site1.setCanonicalDist(canonicalDist);
        site1.setCanonicalReplace(false);
        PSSite site2 = createSite("Site2-" + System.currentTimeMillis(), "Site 2");
        IPSiteDao siteDao = (IPSiteDao) getBean("siteDao");

        try
        {
            // test save
            siteDao.save(site1);

            // test load existing site
            String siteName = site1.getName();
            PSSite savedSite1 = siteDao.find(siteName);
            // set the folder path for site1
            assertNotNull("Should retrieve saved site", savedSite1);
            assertEquals("The default file extension must be set to: " + fileExt, fileExt, savedSite1.getDefaultFileExtention());
            assertTrue("Canonical should be ON", savedSite1.isCanonical());
            assertEquals("The site protocol should be set to: " + siteProtocol, siteProtocol, savedSite1.getSiteProtocol());
            assertEquals("The default document should be set to: " + defaultDocument, defaultDocument, savedSite1.getDefaultDocument());
            assertEquals("The canonical links should be point to: " + canonicalDist, canonicalDist, savedSite1.getCanonicalDist());
            assertFalse("The canonical replace should be OFF", savedSite1.isCanonicalReplace());
            site1.setFolderPath(savedSite1.getFolderPath());
            site1.setSiteId(savedSite1.getSiteId());
            
            assertEquals("Saved sites should equal:", site1, savedSite1);
            assertSite(site1, true);
            
            // publish the site
            PSSitePublishRequest pubReq = new PSSitePublishRequest();
            pubReq.setSiteName(siteName);
            pubReq.setType(PubType.FULL);
         
            IPSGuid siteId = getSiteManager().findSite(siteName).getGUID();
            
            IPSGuid edtnGuid = null;
            List<IPSEdition> editions = getPublisherService().findAllEditionsBySite(siteId);
            for (IPSEdition edition : editions)
            {
                if (edition.getName().endsWith("FULL"))
                {
                    edtnGuid = edition.getGUID();
                    break;
                }
            }
            assertNotNull(edtnGuid);
            
            long jobId = getRxPublisherService().startPublishingJob(edtnGuid, null);
            assertTrue(jobId > -1);
            
            IPSPublisherJobStatus jobStatus;
            State jobState;
         
            //Time this out after a few  minutes
            long endTimeMillis = System.currentTimeMillis() + 100000;
            
            do
            {
               jobStatus = getRxPublisherService().getPublishingJobStatus(jobId);
               jobState = jobStatus.getState();
               
               if (System.currentTimeMillis() > endTimeMillis) {
            	   break;
               }
            }
            while (jobState != State.COMPLETED &&
                  jobState != State.COMPLETED_W_FAILURE &&
                  jobState != State.ABORTED &&
                  jobState != State.BADCONFIG &&
                  jobState != State.CANCELLED &&
                  jobState != State.FORBIDDEN &&
                  jobState != State.INACTIVE &&
                  jobState != State.INVALID);
            
            // check for publishing info
            assertTrue(hasPublishingInfo(jobId));
                                
            // test delete
            siteDao.delete(siteName);

            savedSite1 = siteDao.find(siteName);
    
            assertNull(savedSite1);

            // site data should not exist
            assertFalse(doesSiteFolderExist(siteName));
            assertFalse(hasRelatedItems(siteName));
            //TODO Temporary commented until the fix is ready.
            //assertFalse(hasPublishingInfo(jobId));
          
            // test find all
            int currSites = siteDao.findAll().size();
            siteDao.save(site1);
            siteDao.save(site2);

            // set the folder path for site2
            PSSite savedSite2 = siteDao.find(site2.getName());
            site2.setFolderPath(savedSite2.getFolderPath());

            List<PSSite> sites = siteDao.findAll();
            assertTrue(sites.size() == 2 + currSites);
            assertTrue(sites.contains(site1));
            assertTrue(sites.contains(site2));          
        }
        finally
        {
            try
            {
                siteDao.delete(site1.getName());
                siteDao.delete(site2.getName());
            }
            catch (DeleteException e)
            {
            	//TODO: Shouldn't this fail the test????
            }
            PSSiteDataServletTestCaseFixture.templateCleanUp(TEST_TEMPLATE_BASE, request, response);
        }
    }

    public void testDefault_defaultFileExtention() throws Exception {
    	PSSite site = createSite("Site-" + System.currentTimeMillis(), "Site");
    	assertEquals("The default file extension must be set to html", "html", site.getDefaultFileExtention());
    }
    
    public void testDefault_isCanonical() throws Exception {
    	PSSite site = createSite("Site-" + System.currentTimeMillis(), "Site");    	
        assertTrue("Canonical should be ON", site.isCanonical());
    }
    
    public void testDefault_siteProtocol() throws Exception {
    	PSSite site = createSite("Site-" + System.currentTimeMillis(), "Site");    	
        assertEquals("The site protocol should be set to: https", "https", site.getSiteProtocol());
    }
    
    public void testDefault_defaultDocument() throws Exception {
    	PSSite site = createSite("Site-" + System.currentTimeMillis(), "Site");    	
        assertEquals("The default document should be set to: index.html", "index.html", site.getDefaultDocument());
    }
    
    public void testDefault_canonicalDist() throws Exception {
    	PSSite site = createSite("Site-" + System.currentTimeMillis(), "Site");    	
        assertEquals("The canonical links should be point to: pages", "pages", site.getCanonicalDist());
    }
    
    public void testDefault_isCanonicalReplace() throws Exception {
    	PSSite site = createSite("Site-" + System.currentTimeMillis(), "Site");
        assertTrue("The canonical replace should be ON", site.isCanonicalReplace());
    }
    
    public void testUpdateSitePublishProperties() throws Exception
    {
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);
        
        PSSite testingsite = null;
        IPSiteDao siteDao = (IPSiteDao) getBean("siteDao");
        IPSSiteManager sitemgr = getSiteManager();  
        try
        {
            try
            {
                testingsite = createSite("testingsite", "testingsite");
            }
            catch (Exception e)
            {
                log.error(e.getMessage(),e);
            }
            // test save
            siteDao.save(testingsite);
            //Create the publish properties object 
            PSSitePublishProperties publishProps = new PSSitePublishProperties();
            publishProps.setSiteName(testingsite.getName());
            publishProps.setDeliveryRootPath("ftpsitelocation");
            publishProps.setFtpServerName("ftpserver");
            publishProps.setFtpUserName("user");
            publishProps.setFtpPassword("password");
            publishProps.setPublishType(PublishType.valueOf("ftp"));
            publishProps.setFtpServerPort(9999);
            publishProps.setId(testingsite.getId());
            
            //update the site with publish properties
            IPSSite psSite =  sitemgr.loadSiteModifiable(testingsite.getName()); 
            String localDeliveryLocation = psSite.getRoot();
            siteDao.updateSitePublishProperties(psSite, publishProps);
            
            //Now check updated publish properties for this site
            IPSSite modifiedSite = sitemgr.loadSite(testingsite.getName());
            assertTrue(modifiedSite.getRoot().startsWith(publishProps.getDeliveryRootPath()));
            assertEquals(publishProps.getFtpServerName(), modifiedSite.getIpAddress() );
            assertEquals(publishProps.getPublishType().toString(), getSiteDeliveryType(modifiedSite, siteDao));
            assertEquals(publishProps.getFtpUserName(), modifiedSite.getUserId());
            assertEquals(publishProps.getFtpPassword(), modifiedSite.getPassword());
            assertEquals(publishProps.getFtpServerPort(), modifiedSite.getPort());
            
            //let's switch it back to local
            publishProps.setPublishType(PublishType.valueOf("filesystem"));
            psSite = sitemgr.loadSiteModifiable(testingsite.getName());
            siteDao.updateSitePublishProperties(psSite, publishProps);
            modifiedSite = sitemgr.loadSite(testingsite.getName());
            assertEquals(localDeliveryLocation, modifiedSite.getRoot());
        } 
        finally
        {
            try
            {
                siteDao.delete(testingsite.getName());

            }
            catch (DeleteException e)
            {

            }
        }    
    }
   
    public void testGetSiteDeliveryType() throws Exception
    {    
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);
        
        PSSite testingsite1 = null;
        IPSiteDao siteDao = (IPSiteDao) getBean("siteDao");
        IPSSiteManager sitemgr = getSiteManager();  
        try
        {
            try
            {
                testingsite1 = createSite("testingsite1", "testingsite1");
            }
            catch (Exception e)
            {
               log.error(e.getMessage(),e);
            }
            // test save
            siteDao.save(testingsite1);
            IPSSite psSite =  sitemgr.loadSiteModifiable(testingsite1.getName()); 
            assertEquals("filesystem", getSiteDeliveryType(psSite, siteDao));
        } 
        finally
        {
            try
            {
                siteDao.delete(testingsite1.getName());
            }
            catch (DeleteException e)
            {

            }
        }    
    }   
    
    public void testOnDemandPublish() throws Exception
    {    
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);
        
        PSSite testingsite2 = null;
        IPSiteDao siteDao = (IPSiteDao) getBean("siteDao");
        IPSSiteManager sitemgr = getSiteManager();  
        try
        {
            testingsite2 = createSite("testingsite2", "testingsite2");
            testingsite2 = siteDao.save(testingsite2);
            
            String siteName = testingsite2.getName();
            
            PSSitePublishProperties publishProps = new PSSitePublishProperties();
            publishProps.setDeliveryRootPath("");
            publishProps.setSiteName(siteName);
            publishProps.setFtpServerName("ftpserver");
            publishProps.setFtpUserName("user");
            publishProps.setFtpPassword("password");
            publishProps.setPublishType(PublishType.valueOf("sftp"));
            publishProps.setFtpServerPort(9999);
            publishProps.setId(testingsite2.getId());
            
            //update the site with publish properties
            IPSSite psSite =  sitemgr.loadSiteModifiable(siteName); 
            siteDao.updateSitePublishProperties(psSite, publishProps);
            String deliveryType = getSiteDeliveryType(psSite, siteDao);
            assertEquals(publishProps.getPublishType().toString(), deliveryType);
            
            checkOnDemandPublishEdition(siteDao, siteName, psSite, deliveryType, PSSitePublishDaoHelper.PUBLISH_NOW);
            
            checkOnDemandPublishEdition(siteDao, siteName, psSite, deliveryType, PSSitePublishDaoHelper.UNPUBLISH_NOW);
        } 
        finally
        {
            try
            {
                if (testingsite2 != null)
                {
                    siteDao.delete(testingsite2.getName());
                }
            }
            catch (DeleteException e)
            {

            }
        }    
    }

    private void checkOnDemandPublishEdition(IPSiteDao siteDao, String siteName,
            IPSSite psSite, String deliveryType, String publishType) throws PSNotFoundException {
        // remove publish now from site
        String edtnCListName = PSSitePublishDaoHelper.createName(siteName, publishType);
        IPSPublisherService pubSvc = getPublisherService();
        pubSvc.deleteEdition(pubSvc.findEditionByName(edtnCListName));
        pubSvc.deleteContentLists(Collections.singletonList(pubSvc.findContentListByName(edtnCListName)));
        
        assertNull(pubSvc.findEditionByName(edtnCListName));
        assertNull(pubSvc.findContentListByName(edtnCListName));
        
        // add publish now to site
        if (publishType == PSSitePublishDaoHelper.PUBLISH_NOW)
            siteDao.addPublishNow(psSite);
        else
            siteDao.addUnpublishNow(psSite);
        
        assertNotNull(pubSvc.findEditionByName(edtnCListName));
        IPSContentList cList = pubSvc.findContentListByName(edtnCListName);
        assertNotNull(cList);
        assertEquals(deliveryType, PSUrlUtils.getUrlParameterValue(cList.getUrl(),
                IPSHtmlParameters.SYS_DELIVERYTYPE));
    } 
    
    public void testCopy() throws Exception
    {    
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);
        
        PSSite origSite = null;
        PSSite copySite = null;
        PSSite site1    = null;
        PSSite site2    = null;
        IPSiteDao siteDao = (IPSiteDao) getBean("siteDao");
        IPSPageDao pageDao = (IPSPageDao) getBean("pageDao");
        
        PSPage site1Page1 = null;
        PSPage site1Page2 = null;
        try
        {
            // create a site
        	String origSiteName = "origsite"+System.currentTimeMillis();
        	origSite = createSite(origSiteName, origSiteName);
            origSite = siteDao.save(origSite);
            String origSiteId = origSite.getId();
                        
            // create a new copy of the site
            String copySiteName = "copysite" + System.currentTimeMillis();
            copySite = siteDao.createSiteWithContent(origSiteId, copySiteName);
            String copySiteId = copySite.getId();
            
            // compare the copy with the original
            assertEquals(copySiteName, copySite.getName());
            assertEquals(origSite.getDescription(), copySite.getDescription());
            assertFalse(copySiteId.equals(origSiteId));
            assertFalse(copySite.getFolderPath().equals(origSite.getFolderPath()));
            assertFalse(copySite.getLabel().equals(origSite.getLabel()));
                        
            // check the copy
            PSSite copySiteObj = siteDao.find(copySiteId);
            assertSite(copySiteObj, false);  
            
            //Create a site to test sections, folders, pages
            String site1Name = "site1" + System.currentTimeMillis();
            site1 = createSite(site1Name, site1Name);
            site1 = siteDao.save(site1);
            String site1Id = site1.getId();
           
            //Add a folder
            createFolder("site1folder1","//Sites/" + site1Name);
            
            // Create and save the page under the folder
            String folderPath = site1.getFolderPath()+ "/" + "sitefolder1";
            site1Page2 = new PSPage();
            site1Page2.setName("site1page2");
            site1Page2.setTitle("page under the folder");
            site1Page2.setFolderPath(folderPath);
            site1Page2.setTemplateId(site1.getTemplateName());
            site1Page2.setLinkTitle("testing page2");
            pageDao.save(site1Page2);
            
                        
            //Create and save the page under the root itself
            site1Page1 = new PSPage();
            site1Page1.setName("site1page1");
            site1Page1.setTitle("test new page title");
            site1Page1.setFolderPath(site1.getFolderPath());
            site1Page1.setTemplateId(site1.getTemplateName());
            site1Page1.setLinkTitle("testing");
            pageDao.save(site1Page1);
            
            //create the copy of the site1
            
            String site2Name = "site2" + System.currentTimeMillis();
            site2 = siteDao.createSiteWithContent(site1Id, site2Name);
            
            IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
            assertTrue(contentWs.isChildExistInFolder(site2.getFolderPath()+ "/" + "sitefolder1", "site1page2"));
            assertTrue(contentWs.isChildExistInFolder(site2.getFolderPath(), "site1page1"));
            
        } 
        finally
        {
        	    // If any of these Delete operations fail then there is a bug in delete site
        	    // that needs to be fixed. 
                if (origSite != null)
                {
                    siteDao.delete(origSite.getId());
                }
                
                if (copySite != null)
                {
                    siteDao.delete(copySite.getId());
                }
                
                if(site1 != null)
                {
                    siteDao.delete(site1.getId());
                }
                
                if(site2 != null)
                {
                    siteDao.delete(site2.getId());
                }
                
        }    
    }
    
    public void testFindByPath() throws Exception
    {    
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);
        
        PSSiteSummary testingSite3 = null;
        PSSiteSummary testingSite4 = null;
        IPSiteDao siteDao = (IPSiteDao) getBean("siteDao");
        try
        {
            // shouldn't be any sites
            assertNull(siteDao.findByPath("//Sites/foo/fooPage"));
            
            // create two sites
            PSSite ts3 = siteDao.save(createSite("testingsite3", "testingsite3"));
            testingSite3 = siteDao.findSummary(ts3.getId());
            assertNotNull(testingSite3);
            PSSite ts4 = siteDao.save(createSite("testingsite4", "testingsite4"));
            testingSite4 = siteDao.findSummary(ts4.getId());
            assertNotNull(testingSite4);
              
            // find paths
            PSPage ts3Home = getSiteContentDao().getHomePage(testingSite3);
            assertEquals(testingSite3, siteDao.findByPath(ts3Home.getFolderPath()));
            PSPage ts4Home = getSiteContentDao().getHomePage(testingSite4);
            assertEquals(testingSite4, siteDao.findByPath(ts4Home.getFolderPath()));
            assertNull(siteDao.findByPath("//Sites/foo/fooPage"));
        } 
        finally
        {
            try
            {
                if (testingSite3 != null)
                {
                    siteDao.delete(testingSite3.getId());
                }
                
                if (testingSite4 != null)
                {
                    siteDao.delete(testingSite4.getId());
                }
            }
            catch (DeleteException e)
            {

            }
        }    
    } 
    
    private String getSiteDeliveryType(IPSSite psSite, IPSiteDao siteDao) throws PSNotFoundException {
        return siteDao.getSiteDeliveryType(psSite);
    }

    /**
     * Checks if required related items exist for the specified site.
     * 
     * @param siteName The name of the site, assumed not <code>null</code>.
     * @return <code>true</code> if the items exist, <code>false</code>
     * otherwise.
     * 
     * @throws Exception
     */
    private boolean hasRelatedItems(String siteName) throws Exception
    {
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();

        String folderRoot = "//Sites/" + siteName;

        try
        {
            // Check for Site Folder
            contentWs.loadFolders(new String[]{folderRoot});
        }
        catch (PSErrorResultsException e)
        {
            return false;
        }

        // Check for NavTree, home page
        boolean foundNavTree = false;
        boolean foundHomePage = false;

        String navTreeName = getManagedNavService().getNavtreeContentTypeName();
        List<PSItemSummary> items = contentWs.findFolderChildren(folderRoot,
                false);
        for (PSItemSummary item : items)
        {
            String ctName = item.getContentTypeName();
            if (ctName.equals(navTreeName))
            {
                foundNavTree = true;
            }
            else if (item.getName().equals(PSSiteContentDao.HOME_PAGE_NAME))
            {
                foundHomePage = true;
            }
        }

        if (!foundNavTree || !foundHomePage)
        {
            return false;
        }

        return true;
    }

    /**
     * Checks if required related items exist for the specified site.
     * 
     * @param siteName The name of the site, assumed not <code>null</code>.
     * @return <code>true</code> if the items exist, <code>false</code>
     * otherwise.
     * 
     * @throws Exception
     */
    private boolean doesSiteFolderExist(String siteName) throws Exception
    {
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();

        String folderRoot = "//Sites/" + siteName;

        try
        {
            // check for Site Folder
            contentWs.loadFolders(new String[]{folderRoot});
        }
        catch (PSErrorResultsException e)
        {
            return false;
        }

        return true;
    }

    private void assertPublishingItems(String siteName) throws Exception
    {
        assertPublishingItems(siteName, 4, 5);
    }
    
    private void assertPublishingItems(String siteName, int edtns, int clists) throws Exception
    {
        IPSPublisherService pubSvc = getPublisherService();

        IPSSite site = getSiteManager().loadSite(siteName);
        IPSGuid siteId = site.getGUID();
        List<IPSEdition> editions = pubSvc.findAllEditionsBySite(siteId);
        List<IPSContentList> cLists = pubSvc.findAllContentListsBySite(siteId);
        
        assertEquals("editions: " + editions.size(), edtns, editions.size());
        assertEquals("content lists: " + cLists.size(), clists, cLists.size());
    }

    /**
     * Determines if publishing info exists for the specified publishing job.
     * 
     * @param jobId
     * 
     * @return <code>true</code> if pub info exists, <code>false</code> otherwise.
     */
    private boolean hasPublishingInfo(long jobId)
    {
        IPSPublisherService pubSvc = getPublisherService();
        
        return pubSvc.findPubStatusForJob(jobId) != null;
    }
    
    /**
     * Creates a test site.
     * 
     * @param name the name of the site, assumed not <code>null</code>.
     * @param title the title of the home page of the site, assumed not
     * <code>null</code>.
     * 
     * @return the site, never <code>null</code>.
     * 
     * @throws Exception if an error occurs creating the template.
     */
    private PSSite createSite(String name, String title)
    throws Exception
    {
        PSSite site = new PSSite();
        site.setName(name);
        site.setHomePageTitle(title);
        site.setNavigationTitle(name);
        site.setDescription("This is " + title);
        site.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        site.setTemplateName(name + "PageTemplate");

        return site;
    }
   
    private PSFolder createFolder(String folderName, String sitePath)throws Exception
    {
        IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
        PSFolder testFolder  = contentWs.addFolder(folderName, sitePath);
        contentWs.saveFolder(testFolder);
        return testFolder;
    }
    

    /**
     * Tests that a site was created properly.
     * 
     * @param site
     * @param checkTemplate <code>true</code> if the home page template should be verified, <code>false</code>
     * otherwise.
     * 
     * @throws Exception
     */
    private void assertSite(PSSite site, boolean checkTemplate) throws Exception
    {
        String siteName = site.getName();
        assertTrue(doesSiteFolderExist(siteName));
        assertTrue(hasRelatedItems(siteName));
        assertPublishingItems(siteName);

        if (checkTemplate)
        {
            // make sure template association was created
            List<PSTemplateSummary> templates = getSiteTemplateService().findTemplatesBySite(siteName);
            assertEquals(templates.size(), 1);

            PSTemplateSummary siteTemplate = null;
            List<PSTemplateSummary> userTemplates = getTemplateService().findAllUserTemplates();
            for (PSTemplateSummary userTemplate : userTemplates)
            {
                if (userTemplate.getName().equals(site.getTemplateName()))
                {
                    siteTemplate = userTemplate;
                    break;
                }
            }

            assertNotNull("Template was not created for site " + siteName, siteTemplate);
            assertEquals(templates.get(0).getName(), siteTemplate.getName());
        }
    }
    
    private IPSTemplateService getTemplateService()
    {
        return (IPSTemplateService) getBean("sys_templateService");
    }

    private IPSSiteTemplateService getSiteTemplateService()
    {
        return (IPSSiteTemplateService) getBean("siteTemplateService");
    }

    private IPSManagedNavService getManagedNavService()
    {
        return (IPSManagedNavService) getBean("sys_managedNavService");
    }
    
    private IPSRxPublisherService getRxPublisherService()
    {
        return (IPSRxPublisherService) getBean("sys_rxpublisherservice");
    }
    
    private IPSPublisherService getPublisherService()
    {
        return (IPSPublisherService) getBean("sys_publisherservice");
    }
    
    private IPSSiteManager getSiteManager()
    {
        return (IPSSiteManager) getBean("sys_sitemanager");
    }
    
    private PSSiteContentDao getSiteContentDao()
    {
        return (PSSiteContentDao) getBean("siteContentDao");
    }
    
    /**
     * Base name for test templates created for sites.
     */
    private static final String TEST_TEMPLATE_BASE = "TestTemplate";
}
