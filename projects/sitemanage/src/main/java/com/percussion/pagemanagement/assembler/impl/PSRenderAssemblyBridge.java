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
package com.percussion.pagemanagement.assembler.impl;

import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pagemanagement.assembler.IPSRenderAssemblyBridge;
import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.assembler.PSPageAssemblyContextFactory;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService.PSPageException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSTemplateNotImplementedException;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("renderAssemblyBridge")
public class PSRenderAssemblyBridge implements IPSRenderAssemblyBridge
{
    
    /**
     * The id mapper, Initialized by constructor, never <code>null</code> after
     * that.
     */
    private IPSIdMapper idMapper;

    /**
     * The assembly service, auto wired by Spring framework
     */
    private IPSAssemblyService assemblyService;

    /**
     * The content design web-service. Initialized by constructor, never
     * <code>null</code> after that.
     */
    private IPSContentDesignWs contentDesignWs;

    private IPSSiteManager siteManager;
    
    private IPSContentWs contentWs;

    private IPSCmsObjectMgr cmsMgr;
    
    /**
     * The name of the system dispatch template for pages. Initialized in spring
     * configuration, never <code>null</code> after that.
     */
    @Value("${assemblyBridge.dispatchTemplate:perc.base.plain}")
    private String dispatchTemplate;
    
    
    @Autowired
    public PSRenderAssemblyBridge(IPSAssemblyService assemblyService, IPSContentDesignWs contentDesignWs,
            IPSContentWs contentWs, IPSIdMapper idMapper, IPSSiteManager siteManager, IPSCmsObjectMgr cmsMgr)
    {
        super();
        this.assemblyService = assemblyService;
        this.contentDesignWs = contentDesignWs;
        this.contentWs = contentWs;
        this.idMapper = idMapper;
        this.siteManager = siteManager;
        this.cmsMgr = cmsMgr;
    }

    public String renderPage(String id, boolean editMode, boolean scriptsOff, EditType editType)
    {
        return render(id, editMode, scriptsOff, editType);
    }
    
    /**
     * {@inheritDoc}
     */
    public String renderPage(String id, boolean editMode, boolean scriptsOff) {
        return render(id, editMode, scriptsOff, EditType.PAGE);
    }
    
    public String renderTemplate(String id, boolean scriptsOff) {
        return render(id, true, scriptsOff, EditType.TEMPLATE);
    }
    
    protected String render(String id, boolean editMode, boolean scriptsOff, EditType editType) {
        notEmpty(id, "id may not be blank");

        validateExistingItem(id);

        try
        {
            IPSAssemblyItem work = getWorkItemForPreview(id, editMode, scriptsOff, editType);
            return assemble(work);
        }
        catch (Exception e)
        {
            String errorMsg = "Failed to preview page: " + id;
            log.error(errorMsg);
            throw new PSPageException(errorMsg, e);
        }
    }
    
    public String renderPage(PSPage page, boolean editMode, boolean scriptsOff)
    {
        notNull(page);
        notEmpty(page.getId());
        IPSAssemblyItem ai = getWorkItemForPreview(page.getId(), null, page, true, scriptsOff, EditType.PAGE);
        return render(ai, page);
    }

    public String renderTemplate(PSTemplate template, boolean scriptsOff)
    {
        notNull(template);
        notEmpty(template.getId());
        IPSAssemblyItem ai = getWorkItemForPreview(template.getId(), template, null, true, scriptsOff, EditType.TEMPLATE);
        return render(ai, template);
    }

    public String renderTemplateWithPage(PSTemplate template, PSPage page, boolean scriptsOff)
    {
        notNull(template);
        notEmpty(template.getId());
        IPSAssemblyItem ai = getWorkItemForPreview(page.getId(), template, page, true, scriptsOff, EditType.TEMPLATE);
        return render(ai, template);
    }
    
    private String render(IPSAssemblyItem work, PSAbstractPersistantObject object) {
        notNull(work, "work");
        notNull(object, "object");
        isTrue(isNotBlank(object.getId()), "id may not be blank");

        try
        {
            return assemble(work);
        }
        catch (Exception e)
        {
            if(log.isDebugEnabled()) {
                String errorMsg = "Failed to preview: " + object;
                log.error(errorMsg);
            }
            throw new PSPageException("Failed to preview:", e);
        }
    }

    private String assemble(IPSAssemblyItem work) throws ItemNotFoundException, RepositoryException,
            PSTemplateNotImplementedException, PSAssemblyException, PSFilterException, IOException
    {
        List<IPSAssemblyResult> results = assemblyService.assemble(Collections.singletonList(work));
        IPSAssemblyResult result = results.get(0);
        String charSet = result.getTemplate().getCharset();
        String rendered = IOUtils.toString(result.getResultStream(), charSet);
        return rendered;
    }

    /**
     * Calls
     * {@link #getWorkItemForPreview(String, boolean) getWorkItemForPreview(id, editMode)}
     * then adds the supplied template and page to the result if they are not
     * <code>null</code>.
     * 
     * @param id For a template or page. Not blank.
     * @param template May be <code>null</code>. 
     * @param page May be <code>null</code>. 
     * @param editMode See
     * {@link IPSRenderAssemblyBridge#renderPage(String, boolean, boolean)}.
     * @param scriptsOff See  {@link IPSRenderAssemblyBridge#renderPage(String, boolean, boolean)}. 
     * @param editType the edited item type, assumed not <code>null</code>.
     * 
     * @return Never <code>null</code>.
     */
    private IPSAssemblyItem getWorkItemForPreview(String id, PSTemplate template, PSPage page, boolean editMode, boolean scriptsOff, EditType editType) 
    {
        IPSAssemblyItem item = getWorkItemForPreview(id, editMode, scriptsOff, editType);
        if (template != null) {
            PSAssemblyItemBridge.setTemplate(item, template);
        }
        if (page != null) {
            PSAssemblyItemBridge.setPage(item, page);
        }
        return item;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IPSAssemblyItem getWorkItemForPreview(String id, boolean editMode, boolean scriptsOff, EditType editType)
    {
        notEmpty(id);
        
        PSLegacyGuid guid = (PSLegacyGuid) idMapper.getGuid(id);
        guid = (PSLegacyGuid) contentDesignWs.getItemGuid(guid);
        
        IPSAssemblyItem work = assemblyService.createAssemblyItem();
        work.setId(guid);

        work.setParameterValue(IPSHtmlParameters.SYS_CONTENTID, guid.getContentId() + "");
        work.setParameterValue(IPSHtmlParameters.SYS_REVISION, guid.getRevision() + "");
        work.setParameterValue(IPSHtmlParameters.SYS_ITEMFILTER, "preview");
        work.setParameterValue(IPSHtmlParameters.SYS_TEMPLATE, String.valueOf(getDispatchTemplateId().getUUID()));
        work.setParameterValue(IPSHtmlParameters.SYS_CONTEXT, "0");
        if (editMode)
            work.setParameterValue(PSPageAssemblyContextFactory.ASSEMBLY_PARAM_EDITMODE, "true");
        if(scriptsOff)
            work.setParameterValue(PSPageAssemblyContextFactory.ASSEMBLY_PARAM_SCRIPTSOFF, "true");
        work.setParameterValue(PSPageAssemblyContextFactory.ASSEMBLY_PARAM_EDITTYPE, editType.name());
        
        // get folder ID
        List<PSItemSummary> summs = contentWs.findFolderParents(guid, false);
        if (summs.size() > 0)
        {
            int folderId = summs.get(0).getGUID().getUUID();
            work.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, folderId + "");
        }

        // get site ID
        try
        {
            List<IPSSite> sites = siteManager.getItemSites(guid);
            if (sites.size() > 0)
            {
                if (sites.size() > 1) 
                {
                    log.warn("Page or Template is associated with multiple sites: " + sites);
                }
                int siteId = sites.get(0).getGUID().getUUID();
                work.setParameterValue(IPSHtmlParameters.SYS_SITEID, siteId + "");
            }
            else
            {
                throw new PSRenderAssemblyBridgeException("Page or Template with id: " 
                        + guid 
                        + "  is not in any site folder paths."); 
            }
        }
        catch (Exception e)
        {
            throw new PSRenderAssemblyBridgeException("Failed to get site for page: " 
                    + id.toString(), e);
        }

        return work;
    }

    /**
     * Validates the existence of the specified item. Throws validation exception if it does not
     * exist.
     * 
     * @param id the ID of the item in question, assumed not <code>null</code>.
     */
    private void validateExistingItem(String id)
    {
        PSLegacyGuid guid = (PSLegacyGuid) idMapper.getGuid(id);
        PSComponentSummary summary = cmsMgr.loadComponentSummary(guid.getContentId());
        if (summary == null)
        {
            PSValidationErrorsBuilder builder = validateParameters("render");
            String msg = "Cannot render item (id=" + guid.getContentId() + ") because the item does not exist.";
            builder.reject("page.does.not.exist", msg).throwIfInvalid();
        }
        
    }
    public IPSGuid getDispatchTemplateId()
    {
        try
        {
            IPSAssemblyTemplate template = assemblyService.findTemplateByName(getDispatchTemplate());
            return template.getGUID();
        }
        catch (Exception e)
        {
            String error = "Failed to find dispatcher template: " + getDispatchTemplate();
            log.error(error, e);
            throw new PSPageException(error, e);
        }
    }

    public String getDispatchTemplate()
    {
        return dispatchTemplate;
    }

    public void setDispatchTemplate(String dispatchTemplate)
    {
        this.dispatchTemplate = dispatchTemplate;
    }

    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSRenderAssemblyBridge.class);


}
