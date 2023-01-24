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
package com.percussion.pagemanagement.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.assembler.IPSRenderAssemblyBridge;
import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionCode;
import com.percussion.pagemanagement.data.PSRegionTreeUtils;
import com.percussion.pagemanagement.data.PSRenderResult;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.parser.PSParsedRegionTree;
import com.percussion.pagemanagement.parser.PSTemplateRegionParser;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

@Path("/render")
@Component("renderService")
@Lazy
public class PSRenderService implements IPSRenderService
{

    private final IPSRenderAssemblyBridge renderAssemblyBridge;
    private final IPSPageService pageService;
    private final IPSTemplateService templateService;

    @Autowired
    public PSRenderService(IPSPageService pageService, IPSRenderAssemblyBridge renderAssemblyBridge,
                           IPSTemplateService templateService)
    {
        super();
        this.pageService = pageService;
        this.renderAssemblyBridge = renderAssemblyBridge;
        this.templateService = templateService;
    }

    @POST
    @Path("/page/{regionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSRenderResult renderRegion(PSPage page, @PathParam("regionId") String regionId) throws PSRenderServiceException
    {
        try {

            validateRegionId(regionId);
            validatePage(page);
            String result = renderAssemblyBridge.renderPage(page, true, false);

            return createRenderResult(result, regionId);

        } catch (IPSPageService.PSPageException | PSValidationException | IPSDataService.DataServiceSaveException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/templateAll")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public String renderRegionAll( PSTemplate template ) {
        try {
            validateTemplate(template);
            return renderAssemblyBridge.renderTemplate(template, false);
        } catch (IPSPageService.PSPageException | PSValidationException | IPSDataService.DataServiceSaveException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/template/{regionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSRenderResult renderRegion(PSTemplate template, @PathParam("regionId") String regionId) throws PSRenderServiceException
    {
        try {
            validateRegionId(regionId);
            validateTemplate(template);
            String result = renderAssemblyBridge.renderTemplate(template, false);
            return createRenderResult(result, regionId);
        } catch (IPSPageService.PSPageException | PSValidationException | IPSDataService.DataServiceSaveException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/template/{pageId}/{regionId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public PSRenderResult renderRegionWithPage(PSTemplate template, @PathParam("pageId") String pageId, @PathParam("regionId") String regionId) throws PSRenderServiceException
    {
            try {
                validateRegionId(regionId);
                validateTemplate(template);
                PSParameterValidationUtils.rejectIfBlank("renderRegionWithPage", "pageId", pageId);
                PSPage page = pageService.find(pageId);
                String result = renderAssemblyBridge.renderTemplateWithPage(template, page, false);
                return createRenderResult(result, regionId);
            }
            catch (IPSRenderAssemblyBridge.PSRenderAssemblyBridgeException  e){
                try {
                    String result = renderAssemblyBridge.renderTemplate(template, false);
                    return createRenderResult(result, regionId);
                } catch (IPSPageService.PSPageException psPageException) {
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                    throw new WebApplicationException(e);
                }
            } catch (PSValidationException | IPSDataService.DataServiceLoadException | IPSPageService.PSPageException | IPSDataService.DataServiceNotFoundException | IPSDataService.DataServiceSaveException e) {
                log.error(PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                throw new WebApplicationException(e);
            }
    }

    private PSRenderResult createRenderResult(String result, String regionId)
    {
        String regionResult = getRegion(result, regionId);
        PSRenderResult renderResult = new PSRenderResult();
        renderResult.setRegionId(regionId);
        renderResult.setResult(regionResult);
        return renderResult;
    }

    private String getRegion(String result, String regionId) {
        PSParsedRegionTree<PSRegion, PSRegionCode> tree = PSTemplateRegionParser.parse(null, result);
        PSRegion region = tree.getRegions().get(regionId);
        if (region == null) {
            log.error("Missing regionId: {} from render result:\n {}", regionId, result);
            throw new PSRenderServiceException("RegionId: " + regionId + " was not found in result: " + result);
        }
        return PSRegionTreeUtils.treeToString(region);
    }

    private void validateRegionId(String regionId) throws PSValidationException {

        PSParameterValidationUtils.rejectIfBlank("renderRegion", "regionId", regionId);
    }


    protected PSValidationErrors validateTemplate(PSTemplate template) throws PSValidationException, IPSDataService.DataServiceSaveException {
        return templateService.validate(template);
    }

    protected PSValidationErrors validatePage(PSPage page) throws PSValidationException, IPSDataService.DataServiceSaveException {
        return pageService.validate(page);
    }


    /**
     * @{inheritDoc}
     */
    @GET
    @Path("/page/{id}")
    @Produces( {MediaType.APPLICATION_XML,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON} )
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML})
    public String renderPage(@PathParam("id") String id)
    {
        try {
            log.debug("renderPage, pageId: {}", id);
            return renderAssemblyBridge.renderPage(id, false, false);
        } catch (PSValidationException | IPSPageService.PSPageException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    /**
     * @{inheritDoc}
     */
    @GET
    @Path("/page/editmode/{id}")
    @Produces( {MediaType.APPLICATION_XML,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON} )
    public String renderPageForEdit(@PathParam("id") String id, @QueryParam("editType") String editType)
    {
        try{
            log.debug("renderPageForEditing, pageId: {}" , id);
            notEmpty(id, "id");
            return renderAssemblyBridge.renderPage(id, true, false, getEditType(editType));
        }
        catch (IPSPageService.PSPageException | PSValidationException e){
            try {
                PSPage page = pageService.find(id);
                page.getTemplateId();
                return renderAssemblyBridge.renderTemplate(page.getTemplateId(), false);
            } catch (IPSDataService.DataServiceLoadException | IPSPageService.PSPageException | PSValidationException | IPSDataService.DataServiceNotFoundException dataServiceLoadException) {
                log.error(PSExceptionUtils.getMessageForLog(e));
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                throw new WebApplicationException(e);
            }
        }

    }

    private EditType getEditType(String type)
    {
        if (EditType.TEMPLATE.name().equals(type))
            return EditType.TEMPLATE;
        else
            return EditType.PAGE;
    }

    /**
     * @{inheritDoc}
     */
    @GET
    @Path("/page/editmode/scriptsoff/{id}")
    @Produces( {MediaType.APPLICATION_XML,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON} )
    public String renderPageForEditScriptsOff(@PathParam("id") String id, @QueryParam("editType") String editType)
    {
        try {
            log.debug("renderPageForEditing, pageId: {}", id);
            notEmpty(id, "id");
            return renderAssemblyBridge.renderPage(id, true, true, getEditType(editType));
        } catch (PSValidationException | IPSPageService.PSPageException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }


    }



    @GET
    @Path("/template/{id}")
    @Produces( {MediaType.APPLICATION_XML,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON} )
    public String renderTemplate(@PathParam("id") String id)
    {
        try {
            log.debug("renderTemplate, templateId: {}",  id);
            return renderAssemblyBridge.renderTemplate(id, false);
        } catch (PSValidationException | IPSPageService.PSPageException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/template/scriptsoff/{id}")
    @Produces( {MediaType.APPLICATION_XML,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON} )
    public String renderTemplateScriptsOff(@PathParam("id") String id)
    {
        try {
            log.debug("renderTemplate, templateId: {}", id);
            return renderAssemblyBridge.renderTemplate(id, true);
        } catch (PSValidationException | IPSPageService.PSPageException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/parse")
    @Produces( {MediaType.APPLICATION_XML,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON} )
    public PSRegion parse(String html)
    {
        notNull(html, "Html cannot be null");
        PSRegion root = PSTemplateRegionParser.parse(null, html).getRootNode();

        if(log.isDebugEnabled()) {
            log.debug("Parse html: {}", html);
            log.debug(root);
        }
        return root;
    }





    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSRenderService.class);





}
