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
package com.percussion.sitemanage.service;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.licensemanagement.service.impl.PSLicenseService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.pagemanagement.service.impl.PSPageService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSPublishingAction;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSitePublishJob;
import com.percussion.sitemanage.data.PSSitePublishLogRequest;
import com.percussion.sitemanage.data.PSSitePublishResponse;
import com.percussion.sitemanage.service.IPSSitePublishService.PubType;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import com.percussion.webservices.system.IPSSystemWs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * Test the publishing service.
 *
 */
@Category(IntegrationTest.class)
public class PSSitePublishServiceTest extends PSServletTestCase
{
	
    private IPSiteDao siteDao;

    private IPSSitePublishService sitePublishService;

    private IPSTemplateService templateService;

    private IPSPageService pageService;
    
    private IPSIdMapper idMapper;
    
    private IPSContentWs contentWs;
    
    private IPSSystemWs systemWs;
    
    private IPSWorkflowHelper workflowHelper;
    
    private IPSItemWorkflowService itemWorkflowService;
    
    private IPSPubServerService pubServerService;
    
    private IPSSitePublishStatusService pubStatusService;
    
    private IPSRxPublisherService rxPubService;
    
    private PSLicenseService licenseService;


    String siteName = "TestSite";

    String testTemplateName = "TestSitePageTemplate";

    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        super.setUp();
    }
    
    public void testDummy() throws Exception
    {
        //Added a dummy test as all other tests in this file are ignored.
    }
    
    /**
     * Test the publish action.
     * 
     * @throws Exception if an error occurs.
     */
    @Ignore("Failing on indiana but could not reproduce it on dev build.")
    public void testPublish() throws Exception
    {
        try
        {
            PSSite testSite = new PSSite();
            testSite.setName(siteName);
            testSite.setHomePageTitle("Test Site");
            testSite.setNavigationTitle("TestSite");
            testSite.setDescription("This is a TestSite");
            testSite.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
            testSite.setTemplateName("TestSitePageTemplate");

            IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
            secWs.login(request, response, "admin1", "demo", null, "Enterprise_Investments_Admin", null);;

            // save the test site
            testSite = siteDao.save(testSite);
            long siteId = testSite.getSiteId();

            String folderPath = testSite.getFolderPath();

            assertNotNull(folderPath);

            PSTemplateSummary tempSum = null;
            List<PSTemplateSummary> summaries = templateService.findAllUserTemplates();
            for (PSTemplateSummary summary : summaries)
            {
                if (summary.getName().equalsIgnoreCase(testSite.getTemplateName()))
                {
                    tempSum = summary;
                    break;
                }
            }
            PSPage page = new PSPage();
            page.setName("TestPage");
            page.setTitle("Test Page");
            page.setAfterBodyStartContent("Test Page Header");
            page.setBeforeBodyCloseContent("Test Page Footer");
            page.setAdditionalHeadContent("keywords");
            page.setTemplateId(tempSum.getId());
            page.setFolderPath(folderPath);
            page.setLinkTitle("dummy");
            // create a test page under the site folder
            pageService.save(page);


            List<IPSGuid> ids = new ArrayList<IPSGuid>();
            List<PSItemSummary> items = contentWs.findFolderChildren(folderPath, false);
            for (PSItemSummary item : items)
            {
                if (item.getContentTypeName().equals(PSPageService.PAGE_CONTENT_TYPE))
                {
                    ids.add(item.getGUID());
                }
            }

            // transition the items to Approve
            for (IPSGuid guid : ids)
            {
                itemWorkflowService.performApproveTransition(guid.toString(), false, null);
            }

            
            PSPubServer pubServer = pubServerService.getDefaultPubServer(PSGuidUtils.makeGuid(siteId, PSTypeEnum.SITE));
            assertNotNull(pubServer);
            assertFalse(pubServer.hasFullPublished());
            
            // test incremental publish - should be invalid until full publish
            PSSitePublishResponse pubResponse = sitePublishService.publish(siteName, PubType.INCREMENTAL, null, false, pubServer.getName());
            assertTrue(pubResponse.getStatus().equals(State.INVALID.toString()));
            
            pubServer = pubServerService.getDefaultPubServer(PSGuidUtils.makeGuid(siteId, PSTypeEnum.SITE));
            assertFalse(pubServer.hasFullPublished());
            
            // test full publish
            pubResponse = sitePublishService.publish(siteName, PubType.FULL, null, false, pubServer.getName());
            validatePubJob(pubResponse, siteId, ids.size());
            
            //Possibly try isSitePublished for full publish? only works on first publish
            //
            // Publish is async If we try to incremental too quick we cannot yet incremental
            Thread.sleep(5000);
            // now incremental should work
            pubResponse = sitePublishService.publish(siteName, PubType.INCREMENTAL, null, false, pubServer.getName());
            validatePubJob(pubResponse, siteId, ids.size());
            pubServer = pubServerService.getDefaultPubServer(PSGuidUtils.makeGuid(siteId, PSTypeEnum.SITE));
            assertTrue(pubServer.hasFullPublished());
            
            // test publish now
            pubResponse = sitePublishService.publish(null, PubType.PUBLISH_NOW, idMapper.getString(ids.get(0)), false, pubServer.getName());
            
            Thread.sleep(10000);
            // Wait to publish otherwise site is deleted when job still running
            //validatePubJob(pubResponse, siteId, 1);
            
        }
        finally
        {
            try
            {
                siteDao.delete(siteName);
            }
            catch (Exception e)
            {
                log.error("Failed to delete test site: " + siteName);
            }
        }
    }
    
    @Ignore("Failing on Null Errors")
    public void ignoreGetPublishingActions() throws Exception
    {
        PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                "Enterprise_Investments_Admin", null);
        
        PSSite testSite1 = null;
        try
        {
            testSite1 = new PSSite();
            testSite1.setName("testingSiteAction");
            testSite1.setHomePageTitle("testingSiteAction");
            testSite1.setNavigationTitle("testingSiteAction");
            testSite1.setDescription("This is a TestActionSite");
            testSite1.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
            testSite1.setTemplateName("TestSitePageTemplate");

            // save the test site
            testSite1 = siteDao.save(testSite1);

            String folderPath = testSite1.getFolderPath();
            List<IPSGuid> ids = new ArrayList<IPSGuid>();
            List<PSItemSummary> items = contentWs.findFolderChildren(folderPath, false);
            for (PSItemSummary item : items)
            {
                if (item.getContentTypeName().equals(PSPageService.PAGE_CONTENT_TYPE))
                {
                    ids.add(item.getGUID());
                }
            }
            String pageId = idMapper.getString(ids.get(0));
            List<PSPublishingAction> actions= sitePublishService.getPublishingActions(pageId);
            List<String> actionNames = new ArrayList<String>();
            actionNames.add(PSPublishingAction.PUBLISHING_ACTION_PUBLISH);
            actionNames.add(PSPublishingAction.PUBLISHING_ACTION_SCHEDULE);
            actionNames.add(PSPublishingAction.PUBLISHING_ACTION_TAKEDOWN);
            List<Boolean> enabledVals = new ArrayList<Boolean>();
            enabledVals.add(Boolean.TRUE);
            enabledVals.add(Boolean.TRUE);
            enabledVals.add(Boolean.FALSE);
            validatePubActions(actions, actionNames, enabledVals);
                                                
            //Login as Editor to check available actions
            PSSecurityWsLocator.getSecurityWebservice().login(request, response, "Editor", "demo", null,
                    "Enterprise_Investments_Admin", null);
            
            actions = sitePublishService.getPublishingActions(pageId);
            validatePubActions(actions, actionNames, enabledVals);
                        
            //Login as Contributor to check available actions
            PSSecurityWsLocator.getSecurityWebservice().login(request, response, "Contributor", "demo", null,
                    "Enterprise_Investments_Admin", null);
            
            actions = sitePublishService.getPublishingActions(pageId);
            assertEquals(0, actions.size());
            
            //Login as Admin
            PSSecurityWsLocator.getSecurityWebservice().login(request, response, "admin1", "demo", null,
                    "Enterprise_Investments_Admin", null);
            
            //Publish the page
            sitePublishService.publish("testingSiteAction", PubType.PUBLISH_NOW, pageId, false, null);
            itemWorkflowService.transition(pageId, IPSItemWorkflowService.TRANSITION_TRIGGER_LIVE);
            
            //Wait for the page to go Live
            for (int i = 0; i <= 10; i++)
            {
                Thread.sleep(1000);
                
                if (workflowHelper.isLive(pageId))
                {
                    break;
                }
                
                if (i == 10)
                {
                    fail("Published page was not transitioned to Live");
                }
            }
            
            //Check out the page to put it in Quick Edit
            itemWorkflowService.checkOut(pageId);
            
            actions = sitePublishService.getPublishingActions(pageId);
            int index = actionNames.indexOf(PSPublishingAction.PUBLISHING_ACTION_TAKEDOWN);
            enabledVals.remove(index);
            enabledVals.add(index, Boolean.FALSE);
            validatePubActions(actions, actionNames, enabledVals);
            
            //Check the page in
            itemWorkflowService.checkIn(pageId);
            
            //Login as Editor
            PSSecurityWsLocator.getSecurityWebservice().login(request, response, "Editor", "demo", null,
                    "Enterprise_Investments_Admin", null);
            
            //Check out the page to put it in Quick Edit
            itemWorkflowService.checkOut(pageId);
            
            actions = sitePublishService.getPublishingActions(pageId);
            validatePubActions(actions, actionNames, enabledVals);
            
            //Check the page in
            itemWorkflowService.checkIn(pageId);
            
            //Login as Contributor
            PSSecurityWsLocator.getSecurityWebservice().login(request, response, "Contributor", "demo", null,
                    "Enterprise_Investments_Admin", null);
  
            //Check out the page to put it in Quick Edit
            itemWorkflowService.checkOut(pageId);
            
            actions = sitePublishService.getPublishingActions(pageId);
            assertEquals(0, actions.size());
            
            //Check the page in
            itemWorkflowService.checkIn(pageId);
        }
        finally
        {
            try
            {
                siteDao.delete(testSite1.getName());
            }
            catch (DeleteException e)
            {

            }
             
        }
        
    }
    
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
        
        PSSiteDataServletTestCaseFixture.templateCleanUp(testTemplateName, request, response);
    }

    /**
     * Validates that the status of the publishing job completed without error.
     * 
     * @param pubResponse the publishing job response, assumed not
     *            <code>null</code>.
     * @param siteId 
     * @param delivered the number of items expected to have been delivered.
     */
    private void validatePubJob(PSSitePublishResponse pubResponse, long siteId, int delivered)
    {
        assertFalse(pubResponse.getStatus().equals(State.ABORTED.toString()));
        assertFalse(pubResponse.getStatus().equals(State.INVALID.toString()));
        assertFalse(pubResponse.getStatus().equals(State.FORBIDDEN.toString()));
        assertFalse(pubResponse.getStatus().equals(State.BADCONFIG.toString()));
        assertFalse(pubResponse.getStatus().equals(State.BADCONFIGMULTIPLESITES.toString()));
        
        
       long jobId = pubResponse.getJobid();
        
       if(jobId == 0)
           return;

        IPSPublisherJobStatus jobStatus = rxPubService.getPublishingJobStatus(jobId);
        int count = 0;
        try
        {
            while(!isCompleted(jobStatus.getState()))
            {
                
                Thread.sleep(1000);
                jobStatus = rxPubService.getPublishingJobStatus(jobId);
                if (count++ > 1200)
                {
                    log.error("Timed out waiting for publishing status for jobId "+jobId);
                    fail();
                }
            }
        }
        catch (InterruptedException e)
        {
           log.error("Iterrupted while waiting for pub status",e);
           fail();
        }
        if (jobStatus.getState()==State.COMPLETED_W_FAILURE)
        {
            log.error("Error in validatePubJob");
            IPSPublisherService psPubService = PSPublisherServiceLocator.getPublisherService();
            Iterator<IPSPubItemStatus> it = psPubService.findPubItemStatusForJobIterable(jobId).iterator();
            while (it.hasNext())
            {
                IPSPubItemStatus status = it.next();
                if (status.getStatus()!=IPSSiteItem.Status.SUCCESS)
                {
                    log.error("Error publishing "+ status.getContentId() + " message="+status.getMessage());
                }
            }
        }
        assertEquals(State.COMPLETED, jobStatus.getState());
        assertEquals(0, jobStatus.countFailedItems());

    }

    /**
     * @param state
     * @return
     */
    private boolean isCompleted(State jobState)
    {
        return jobState.equals(State.COMPLETED) ||
                jobState.equals(State.COMPLETED_W_FAILURE) ||
                jobState.equals(State.ABORTED);
    }


    /**
     * Validates the enabled status of the specified publishing actions.
     * 
     * @param actions
     * @param actionNames order must match the enabled values.
     * @param enabledVals order must match the action names.
     */
    private void validatePubActions(List<PSPublishingAction> actions, List<String> actionNames,
            List<Boolean> enabledVals)
    {
        for (PSPublishingAction action : actions)
        {
            Boolean isEnabled = enabledVals.get(actionNames.indexOf(action.getName()));
            
            assertTrue(isEnabled.booleanValue() == action.isEnabled());
        }
    }
    
    public IPSSitePublishService getSitePublishService()
    {
        return sitePublishService;
    }

    public void setSitePublishService(IPSSitePublishService sitePublishService)
    {
        this.sitePublishService = sitePublishService;
    }

    public IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    public void setSiteDao(IPSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }

    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }

    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }
    
    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSContentWs getContentWs()
    {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs)
    {
        this.contentWs = contentWs;
    }

    public IPSSystemWs getSystemWs()
    {
        return systemWs;
    }

    public void setSystemWs(IPSSystemWs systemWs)
    {
        this.systemWs = systemWs;
    }

    public IPSWorkflowHelper getWorkflowHelper()
    {
        return workflowHelper;
    }

    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
    {
        this.workflowHelper = workflowHelper;
    }

    public IPSItemWorkflowService getItemWorkflowService()
    {
        return itemWorkflowService;
    }

    public void setItemWorkflowService(IPSItemWorkflowService itemWorkflowService)
    {
        this.itemWorkflowService = itemWorkflowService;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSitePublishServiceTest.class);

    public void setPubServerService(IPSPubServerService pubServerService)
    {
        this.pubServerService = pubServerService;
    }

    public void setPubStatusService(IPSSitePublishStatusService pubStatusService)
    {
        this.pubStatusService = pubStatusService;
    }


    public void setRxPubService(IPSRxPublisherService rxPubService)
    {
        this.rxPubService = rxPubService;
    }


    public void setLicenseService(PSLicenseService licenseService)
    {
        this.licenseService = licenseService;
    }
    
}
