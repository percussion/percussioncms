/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.recent.service;

import static java.util.Arrays.asList;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.pagemanagement.dao.impl.PSTemplateDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.PSRequest;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.share.test.PSTestDataCleaner;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.sitemanage.service.PSSiteTemplates;
import com.percussion.sitemanage.service.PSSiteTemplates.CreateTemplate;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSRecentServiceFixture
{
    
    private IPSSiteTemplateService siteTemplateService;
    private IPSSiteDataService siteDataService;
    private IPSTemplateService templateService;
    private IPSAssemblyService assemblyService;
    private IPSAssetService assetService;
    private IPSSecurityWs securityWs;
    private IPSPageService pageService;
    private IPSIdMapper idMapper;
    private IPSFolderHelper folderHelper;
    
    public PSTemplateCleaner templateCleaner = new PSTemplateCleaner();
    public PSSiteCleaner siteCleaner = new PSSiteCleaner();
    public PSAssetCleaner assetCleaner = new PSAssetCleaner();
    public PSPageCleaner pageCleaner = new PSPageCleaner();
    public PSPageCatalogCleaner pageCatalogCleaner = new PSPageCatalogCleaner();
    public PSFolderCleaner folderCleaner = new PSFolderCleaner();
    
    public PSSiteSummary site1;
    public PSTemplateSummary template1;
    public PSTemplateSummary baseTemplate;
    public String baseTemplateId;
    public String prefix = getClass().getSimpleName();
    public boolean noValidateCleaners = false;
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    public PSRecentServiceFixture()
    {
        super();
    }
    public PSRecentServiceFixture(HttpServletRequest request, HttpServletResponse response)
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
        templateCleanUp(prefix);
        baseTemplateId = idMapper.getString(createBasePageTemplate(prefix + "BaseTemplate"));
        templateCleaner.add(prefix+"Template1");
        baseTemplate = getTemplateService().find(baseTemplateId);
        site1 = createSite(prefix, "Site");
        templateCleaner.add(prefix+"Template1");
        template1 = createTemplate(prefix+"Template1");
    }
    
    public PSSiteSummary createSite(String prefix, String name) throws PSDataServiceException {
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
        notNull(site);
        
        return siteSummary;
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
        folderCleaner.clean();
        siteCleaner.clean();
        assetCleaner.clean();
        templateCleanUp(prefix);
        PSTestDataCleaner.validateCleaners(pageCleaner, templateCleaner, siteCleaner, assetCleaner);
    }

    public String createSiteFolder(String folderName) throws Exception
    {
        String path = "/Sites/"+site1.getName()+"/"+folderName;
        String internalFolderPath = PSPathUtils.getFolderPath(path);
        folderHelper.createFolder(internalFolderPath);
        folderCleaner.add(path);
        return path;
    }

    public String createAssetFolder(String folderName) throws Exception
    {
        String path = "/Assets/"+site1.getName()+"/"+folderName;
        String internalFolderPath = PSPathUtils.getFolderPath(path);
        folderHelper.createFolder(internalFolderPath);
        folderCleaner.add(path);
        return path;
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
        PSRecentServiceFixture fixture = new PSRecentServiceFixture(request, response);
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
    
    public List<PSWidgetContentType> getWidgetTypes() throws PSDataServiceException {
        return assetService.getAssetTypes("yes");
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
    
    public class PSFolderCleaner extends PSTestDataCleaner<String> {

        @Override
        protected void clean(String path) throws Exception
        {
            String internalFolderPath = PSPathUtils.getFolderPath(path);
            folderHelper.deleteFolder(internalFolderPath);
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
    
    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }
    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSRecentServiceFixture.class);
    

}

