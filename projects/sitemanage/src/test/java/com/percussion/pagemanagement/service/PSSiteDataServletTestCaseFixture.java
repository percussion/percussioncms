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
package com.percussion.pagemanagement.service;

import static java.util.Arrays.asList;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.pagemanagement.dao.impl.PSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.sitemanage.service.PSSiteTemplates;
import com.percussion.sitemanage.service.PSSiteTemplates.CreateTemplate;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

public class PSSiteDataServletTestCaseFixture
{
    
    private IPSSiteTemplateService siteTemplateService;
    private IPSSiteDataService siteDataService;
    private IPSTemplateService templateService;
    private IPSAssemblyService assemblyService;
    private IPSAssetService assetService;
    private IPSSecurityWs securityWs;
    private IPSPageService pageService;
    private IPSIdMapper idMapper;
    
    public PSTemplateCleaner templateCleaner = new PSTemplateCleaner();
    public PSSiteCleaner siteCleaner = new PSSiteCleaner();
    public PSAssetCleaner assetCleaner = new PSAssetCleaner();
    public PSPageCleaner pageCleaner = new PSPageCleaner();
    public PSPageCatalogCleaner pageCatalogCleaner = new PSPageCatalogCleaner();
    
    
    public PSSiteSummary site1;
    public PSTemplateSummary template1;
    public PSTemplateSummary baseTemplate;
    public String baseTemplateId;
    public String prefix = getClass().getSimpleName();
    public boolean noValidateCleaners = false;
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    
    public PSSiteDataServletTestCaseFixture()
    {
        super();
    }
    public PSSiteDataServletTestCaseFixture(HttpServletRequest request, HttpServletResponse response)
    {
        super();
        this.request = request;
        this.response = response;
    }
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        init("admin1", "demo", "Enterprise_Investments_Admin");
    }
    
    public void setUp() throws Exception {
        setUp("admin1", "demo", "Enterprise_Investments_Admin");
    }
    
    @SuppressWarnings("unchecked")
    public void init(String uid, String pwd, String community) throws Exception {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        PSRequestInfo.resetRequestInfo();
        PSRequest req = PSRequest.getContextForRequest();
        PSRequestInfo.initRequestInfo((Map) null);
        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
        setSecurityWs(PSSecurityWsLocator.getSecurityWebservice());
        securityWs.login(request, response, uid, pwd, null, community, null);
    }
    
    public void setUp(String uid, String pwd, String community) throws Exception
    {
        notNull(request);
        notNull(response);

        init(uid, pwd, community);
       
        baseTemplateId = idMapper.getString(createBasePageTemplate(prefix + "BaseTemplate"));
        baseTemplate = getTemplateService().find(baseTemplateId);
        site1 = createSite(prefix, "Site");
        template1 = createTemplate(prefix+"Template1");
        templateCleaner.add(prefix+"Template1");
    }
    
    
    public PSSiteSummary createSite(String prefix, String name)
    {
    	
    	
    	try {
    		
    		try {
        		PSSiteProperties sp = getSiteDataService().getSiteProperties(prefix + name);
        		
       			getSiteDataService().delete(prefix + name);
        	} catch(Exception ex) {
        		//Do nothing - site is not present.
        	}
    		
	        PSSite site = new PSSite();
	        site.setName(prefix + name);
	        site.setLabel("My test site - " + prefix);
	        site.setHomePageTitle("homePageTitle");
	        site.setNavigationTitle("navigationTitle");
	        site.setBaseTemplateName(baseTemplate.getName());
	        site.setTemplateName(prefix + "SiteTemplate");
	        site.setBaseUrl("http://" + site.getName() + ".com/");
	        siteCleaner.add(site.getName());
	        PSSiteSummary siteSummary = getSiteDataService().save(site);
	        notNull(siteSummary);
	        
	        return siteSummary;
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return null;
    }

    public PSTemplateSummary createTemplateWithSite(String templateName, String siteId) {
        notEmpty(templateName);
        notEmpty(siteId);
        templateCleaner.add(templateName);
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        CreateTemplate createTemplate = new CreateTemplate();
        createTemplate.setName(templateName);
        createTemplate.setSourceTemplateId(baseTemplate.getId());
        createTemplate.setSiteIds(asList(siteId));
        siteTemplates.setCreateTemplates(asList(createTemplate));
        return getSiteTemplateService().save(siteTemplates).get(0);
    }
    
    public PSTemplateSummary createTemplateFromTemplate(String templateName, String templateId) {
        notEmpty(templateName);
        notEmpty(templateId);
        templateCleaner.add(templateName);
        String siteId = getSiteTemplateService().findSitesByTemplate(templateId).get(0).getId();
        PSSiteTemplates siteTemplates = new PSSiteTemplates();
        CreateTemplate createTemplate = new CreateTemplate();
        createTemplate.setName(templateName);
        createTemplate.setSourceTemplateId(templateId);
        createTemplate.setSiteIds(asList(siteId));
        siteTemplates.setCreateTemplates(asList(createTemplate));
        return getSiteTemplateService().save(siteTemplates).get(0);
    }
    
    public PSPage createPage(PSPage page) throws PSDataServiceException {
        String fullPath = page.getFolderPath() + "/" + page.getName();
        pageCleaner.add(fullPath);
        return getPageService().save(page);
    }
    

    /**
     * Creates and saves a page uses template1 as the template and site1 as folder
     * 
     * @param name must not be <code>null</code>. name is used for title, linktitle and description
     * 
     * @return the created page, never null.
     */
    public PSPage createPage(String name) throws PSDataServiceException {
        notEmpty(name);
        PSPage page = new PSPage();
        page.setFolderPath(site1.getFolderPath());
        page.setName(name);
        page.setTitle(name);
        page.setTemplateId(template1.getId());
        page.setLinkTitle(name);
        page.setDescription(name);

        String fullPath = page.getFolderPath() + "/" + page.getName();
        pageCleaner.add(fullPath);
        return createPage(page);
    }
    
    
    public PSTemplateSummary createTemplate(String templateName) {
        return createTemplateWithSite(templateName, site1.getId());
    }
    
    public PSAsset saveAsset(PSAsset asset) throws PSDataServiceException {
        asset = getAssetService().save(asset);
        assetCleaner.add(asset.getId());
        return asset;
    }
    
    
    public void tearDown() throws Exception {
        pageCatalogCleaner.clean();
        pageCleaner.clean();
        templateCleaner.clean();
        siteCleaner.clean();
        assetCleaner.clean();
        templateCleanUp(prefix);
        PSTestDataCleaner.validateCleaners(pageCleaner, templateCleaner, siteCleaner, assetCleaner);
    }



    /**
     * Removes all templates and template items created by test.
     * 
     * @param prefix the read only template name prefix. All read only templates
     *            whose name begins with this prefix will be deleted. May not be
     *            blank.
     * 
     * @throws Exception if an error occurs.
     */
    public void templateCleanUp(String prefix) throws Exception
    {
        isTrue(isNotBlank(prefix), "prefix may not be blank");

        List<IPSAssemblyTemplate> templates = getAssemblyService().findTemplates(prefix + '%', null, null, null, null, null,
                PSTemplateDao.PAGE_ASSEMBLER);
        for (IPSAssemblyTemplate t : templates)
            getAssemblyService().deleteTemplate(t.getGUID());

        List<PSTemplateSummary> summaries = getTemplateService().findAllUserTemplates();
        for (PSTemplateSummary summary : summaries)
        {
            try
            {
                if (startsWith(summary.getName(), prefix))
                    getTemplateService().delete(summary.getId());
            }
            catch (Exception e)
            {
                log.warn("Failed to delete template: " + summary.getId());
            }
        }
    }
    
    public static void templateCleanUp(String prefix, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PSSiteDataServletTestCaseFixture fixture = new PSSiteDataServletTestCaseFixture(request, response);
        fixture.init();
        fixture.templateCleanUp(prefix);
    }
    
    
    /**
     * Creates a read-only or system template for testing purpose.
     * 
     * @param name the name of the created template, never blank.
     * 
     * @return the created template, never <code>null</code>.
     * 
     * @throws Exception if an error occurs.
     */
    public IPSGuid createBasePageTemplate(String name) throws Exception
    {
        IPSAssemblyService asmSrv = PSAssemblyServiceLocator.getAssemblyService();
        IPSAssemblyTemplate template = asmSrv.createTemplate();
        template.setName(name);
        template.setLabel(name);
        template.setDescription("This is description of " + name);
        template.setTemplate(getSampleTemplateContent(name));
        template.setAssembler(PSTemplateDao.PAGE_ASSEMBLER);

        // add default binding
        LinkedHashMap<String, String> bindings = new LinkedHashMap<String, String>();
        bindings.put("$perc_cssRegion", "#container{margin:0 auto; width: 960px;}");
        template.setBindings(asmSrv.createBindings(bindings, 1));

        // set global template
        IPSAssemblyTemplate gTmp = asmSrv.findTemplateByName("perc.page");
        template.setGlobalTemplate(gTmp.getGUID());

        asmSrv.saveTemplate(template);

        return template.getGUID();
    }
    
    
    public class PSTemplateCleaner extends PSTestDataCleaner<String> {

        @Override
        protected void clean(String name) throws Exception
        {
            PSTemplateSummary t = getTemplateService().findUserTemplateByName_UsedByUnitTestOnly(name);
            getTemplateService().delete(t.getId());
        }
        
    }
    
    public class PSSiteCleaner extends PSTestDataCleaner<String> {

        @Override
        protected void clean(String id) throws Exception
        {
            getSiteDataService().delete(id);
        }
    
    }
    
    public class PSAssetCleaner extends PSTestDataCleaner<String> {

        @Override
        protected void clean(String id) throws Exception
        {
            getAssetService().delete(id);
        }
    
    }
    
    public class PSPageCleaner extends PSTestDataCleaner<String> {
        
        @Override
        protected void clean(String fullPath) throws Exception
        {
            PSPage page = getPageService().findPageByPath(fullPath);
            
            // CM-126: pageService.findPageByPath(copiedPagePath) may return null
            if(page != null)
            {
                getPageService().delete(page.getId());
                
            }
        }
    }


    
    public class PSPageCatalogCleaner extends PSTestDataCleaner<String> {

        @Override
        protected void clean(String id) throws Exception
        {
            getPageService().delete(id);
        }
    
    }
    
    
    private String getSampleTemplateContent(String name)
    {
        return "Hello world for Sample Template Content";
    }
    
    
    
    public IPSPageService getPageService()
    {
        return pageService;
    }
    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }
    public IPSSiteTemplateService getSiteTemplateService()
    {
        return siteTemplateService;
    }

    public void setSiteTemplateService(IPSSiteTemplateService siteTemplateService)
    {
        this.siteTemplateService = siteTemplateService;
    }

    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    
    public IPSAssemblyService getAssemblyService()
    {
        return assemblyService;
    }

    public void setAssemblyService(IPSAssemblyService assemblyService)
    {
        this.assemblyService = assemblyService;
    }
    
    
    
    public IPSSiteDataService getSiteDataService()
    {
        return siteDataService;
    }


    public void setSiteDataService(IPSSiteDataService siteDataService)
    {
        this.siteDataService = siteDataService;
    }

    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }


    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }
    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }






    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSiteDataServletTestCaseFixture.class);
    

}

