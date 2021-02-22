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
package com.percussion.sitemanage.importer;

import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.PSSiteDataServletTestCaseFixture;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.IPSAsyncJobFactory;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.helpers.PSHelperTestUtils;
import com.percussion.sitemanage.importer.helpers.impl.PSSiteCreationHelper;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author LucasPiccoli
 * 
 *         Tests if current existing import jobs (importSite and importTemplate)
 *         are running successfully. They are considered successful when all
 *         their helpers are executed without errors and content gets created.
 */
@Category(IntegrationTest.class)
public class PSImportFromUrlJobTest extends PSSiteImportTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Inject dependencies
        setSecurityWs((IPSSecurityWs) getBean("sys_securityWs"));
        setSiteDao((IPSiteDao) getBean("siteDao"));
        setPageService((IPSPageService) getBean("pageService"));
        setAsyncJobService((IPSAsyncJobService) getBean("asyncJobService"));
        setJobFactory((IPSAsyncJobFactory)getBean("asyncJobFactory"));

        // Login is needed to create folder for the new site.
        securityWs.login("Admin", "demo", "Default", null);

        initData();
    }

    /**
     * Placeholder test to keep junit happy, all other tests ignored as tech debt
     */
    @Test
    public void testNothing()
    {
        
    }
    
    /**
     * Tests if the jobs can gather the helpers they need from the context.
     */
    @Ignore
    public void ignore_testHelperAvailability()
    {
        // Create jobs
        PSImportFromUrlJob siteImportJob = (PSImportFromUrlJob) jobFactory.getJob(SITE_IMPORT_JOB);
        PSImportFromUrlJob templateImportJob = (PSImportFromUrlJob) jobFactory.getJob(TEMPLATE_IMPORT_JOB);
        
        assertNotNull(siteImportJob);

        assertNotNull(siteImportJob.getMandatoryHelpers());
        assertTrue(!siteImportJob.getMandatoryHelpers().isEmpty());

        assertNotNull(siteImportJob.getOptionalHelpers());
        assertTrue(!siteImportJob.getOptionalHelpers().isEmpty());

        assertNotNull(templateImportJob);

        assertNotNull(templateImportJob.getMandatoryHelpers());
        assertTrue(!templateImportJob.getMandatoryHelpers().isEmpty());

        assertNotNull(templateImportJob.getOptionalHelpers());
        assertTrue(!templateImportJob.getOptionalHelpers().isEmpty());
    }

    /**
     * Tests if the whole process of importing a site is successful.
     */
    @Ignore
    public void ignore_testImportSite()
    {
        importedSite = new PSSite();
        importedSite.setName(TEST_SITE_NAME);
        importedSite.setBaseUrl(EXTERNAL_TEST_URL);

        PSSiteImportCtx importContext = new PSSiteImportCtx();
        importContext.setSite(importedSite);
        importContext.setSiteUrl(EXTERNAL_TEST_URL);
        importContext.setUserAgent(PSHelperTestUtils.USER_AGENT);

        try
        {
            long jobId = asyncJobService.startJob(SITE_IMPORT_JOB, importContext);

            // Keep polling for status until import is complete, and return
            // result.
            importContext = getImportedContext(jobId);

            // Set siteCreated to true if a site was created, to delete it on
            // teardown.
            siteCreated = true;
        }
        catch (RuntimeException | IPSFolderService.PSWorkflowNotFoundException e)
        {
            fail();
        }
        assertNotNull(importContext);
        assertNotNull(importContext.getSite());
        assertNotNull(importContext.getTemplateId());
        assertNotNull(importContext.getPageName());
    }

    /**
     * Test if the correct helpers configured for templateImportService bean are
     * executed correctly and without errors.
     * @throws Exception 
     * 
     */
    @Ignore
    public void ignore_testImportTemplateFromUrl() throws Exception
    {
        createFixture();

        PSSiteImportCtx importContext = new PSSiteImportCtx();
        importContext.setSite((PSSite) fixture.site1);
        importContext.setSiteUrl(EXTERNAL_TEST_URL);
        importContext.setUserAgent(PSHelperTestUtils.USER_AGENT);

        try
        {
            long jobId = asyncJobService.startJob(TEMPLATE_IMPORT_JOB, importContext);
            // This method keeps polling for status until import is complete,
            // and returns result.
            importContext = getImportedContext(jobId);
        }
        catch (RuntimeException e)
        {
            fail();
        }
        assertNotNull(importContext);
        assertNotNull(importContext.getSite());
        assertNotNull(importContext.getTemplateId());
        assertNotNull(importContext.getPageName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        if (siteCreated)
        {
            deleteSite();
            siteCreated = false;
        }
        if (fixtureCreated)
        {
            fixture.tearDown();
            fixtureCreated = false;
        }
    }

    private void initData() throws Exception
    {
        createHelpers();
    }

    private void deleteSite()
    {
        if (importedSite != null)
        {
            PSSiteImportCtx importCtx = new PSSiteImportCtx();
            importCtx.setSite(importedSite);
            siteCreationHelper.rollback(null, importCtx);
            importedSite = null;
        }
    }
    
    /**
     * Polls job with jobId for status until it its completed or aborted. If the
     * job ran successfully, return its result, which in this case is a
     * PSSiteImportCtx.
     * 
     * @param jobId the Id of the job to poll for status.
     * @return Site import context with information of created elements during
     *         import.
     * @throws InterruptedException 
     */
    private PSSiteImportCtx getImportedContext(long jobId)
    {

        PSAsyncJobStatus jobStatus = null;
        try
        {
            do
            {
                Thread.sleep(500);
                jobStatus = asyncJobService.getJobStatus(jobId);
                assertNotNull(jobStatus);
            }
            while (!jobStatus.getStatus().equals(IPSAsyncJob.ABORT_STATUS)
                    && !jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS));

            if (jobStatus.getStatus().equals(IPSAsyncJob.ABORT_STATUS))
            {
                return null;
            }
            else
            {
                return (PSSiteImportCtx) asyncJobService.getJobResult(jobId);
            }
        }
        catch (Exception e)
        {
            if (jobStatus.getStatus() != IPSAsyncJob.ABORT_STATUS)
            {
                asyncJobService.cancelJob(jobId);
            }
            return null;
        }
    }

    private void createHelpers()
    {
        siteCreationHelper = new PSSiteCreationHelper(siteDao, pageService);
    }

    private void createFixture() throws Exception
    {
        fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.setUp();
        fixture.pageCleaner.add(fixture.site1.getFolderPath() + "/Page1");
        fixtureCreated = true;
    }

    /**
     * @return the securityWs
     */
    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    /**
     * @param securityWs the securityWs to set
     */
    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    /**
     * @return the siteDao
     */
    public IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    /**
     * @param siteDao the siteDao to set
     */
    public void setSiteDao(IPSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }

    /**
     * @return the pageService
     */
    public IPSPageService getPageService()
    {
        return pageService;
    }

    /**
     * @param pageService the pageService to set
     */
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    /**
     * @return the asyncJobService
     */
    public IPSAsyncJobService getAsyncJobService()
    {
        return asyncJobService;
    }

    /**
     * @param asyncJobService the asyncJobService to set
     */
    public void setAsyncJobService(IPSAsyncJobService asyncJobService)
    {
        this.asyncJobService = asyncJobService;
    }

    /**
     * @param jobFactory the jobFactory to set
     */
    public void setJobFactory(IPSAsyncJobFactory jobFactory)
    {
        this.jobFactory = jobFactory;
    }

    private PSSiteDataServletTestCaseFixture fixture;

    private PSSiteCreationHelper siteCreationHelper;

    private PSSite importedSite;

    private IPSSecurityWs securityWs;

    private IPSiteDao siteDao;

    private IPSPageService pageService;

    private IPSAsyncJobFactory jobFactory;

    private IPSAsyncJobService asyncJobService;

    private final String EXTERNAL_TEST_URL = "http://www.google.com";
    
    private final String TEST_SITE_NAME = "TestImportSite";

    private final String SITE_IMPORT_JOB = "siteImportJob";

    private final String TEMPLATE_IMPORT_JOB = "templateImportJob";

    private boolean siteCreated = false;

    private boolean fixtureCreated = false;
    
}
