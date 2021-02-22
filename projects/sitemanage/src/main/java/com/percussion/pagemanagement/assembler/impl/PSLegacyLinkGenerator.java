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

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.error.PSException;
import com.percussion.pagemanagement.data.*;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.validation.ValidationException;
import com.percussion.webservices.content.IPSContentDesignWs;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Node;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringEscapeUtils.unescapeXml;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * A wrapper around {@link PSLocationUtils}.
 * This is the bridge between the legacy location
 * scheme generator and the new render link service.
 * <p>
 * <em>This component only generates preview links</em> 
 * 
 * @author adamgent
 * 
 */
@PSSiteManageBean("legacyLinkGenerator")
public class PSLegacyLinkGenerator
{
    private static final String IMAGE_ASSET_CONTENTTYPE = "percImageAsset";
    private PSLocationUtils locationUtils;
    private IPSSiteManager siteManager;
    private IPSIdMapper idMapper;
    private IPSContentDesignWs contentDesignWs;
    private IPSAssemblyService assemblyService;
    
    
    @Autowired
    public PSLegacyLinkGenerator(IPSAssemblyService assemblyService, IPSContentDesignWs contentDesignWs,
            IPSIdMapper idMapper, IPSSiteManager siteManager)
    {
        super();
        this.assemblyService = assemblyService;
        this.contentDesignWs = contentDesignWs;
        this.idMapper = idMapper;
        this.siteManager = siteManager;
    }

    protected PSLocationUtils getLocationUtils()
    {
        if (locationUtils == null)
            locationUtils = new PSLocationUtils();
        return locationUtils;
    }

    /**
     * Generates logical link using the legacy location generator.
     * <strong>This should only be used for preview links</strong>
     * @param l never <code>null</code>.
     * @return never <code>null</code> or empty.
     */
    public String generate(PSLegacyLink l) throws PSBeanValidationException {
        PSBeanValidationUtils.getValidationErrorsOrFailIfInvalid(l);
        String url = getLocationUtils().generate(l.getLegacyTemplate(), l.getNode(), l.getFolderPath(), l.getFilter(),
                l.getSiteId(), l.getContext());
        url = unescapeXml(url);
        if (l.getContext().intValue() != 0) 
        {
            /*
             * We should only be using the legacy link generator for preview urls.
             */
            log.error("Used legacy link generator for non-preview url");
        }
        return url;
    }
    
    /**
     * Adds legacy meta data needed for the inline link processor(s).
     * 
     * @param renLink never <code>null</code>.
     * @param page never <code>null</code>.
     */
    @SuppressWarnings("deprecation")
    public void addLegacyDataToInlineLink(PSInlineRenderLink renLink, PSPage page) {
        notNull(page,"page");
        notNull(renLink,"renLink");
        fillInlineLinkHelper(renLink, page.getId());
    }
    
    /**
     * Adds legacy meta data needed for the inline link processor(s).
     * @param renLink
     * @param request
     * @param asset
     */
    @SuppressWarnings("deprecation")
    public void addLegacyDataToInlineLink(PSInlineRenderLink renLink, PSInlineLinkRequest request, PSAsset asset) {
        notNull(asset,"asset");
        notNull(renLink,"renLink");
        if( IMAGE_ASSET_CONTENTTYPE.equals(asset.getType()) && isNotBlank(renLink.getThumbResourceDefinitionId()) ) {
            fillInlineImageLink(renLink, request);
        }
        else {
            fillInlineLinkHelper(renLink, asset.getId());
        }
    }
    
    @SuppressWarnings("deprecation")
    private void fillInlineLinkHelper(PSInlineRenderLink renLink, String targetId) {
        notNull(renLink.getResourceDefinition(), "The resource definition should be loaded by now.");
        PSAssetResource resourceDef = (PSAssetResource) renLink.getResourceDefinition();
        renLink.setInlineType("rxhyperlink");
        Integer templateId = findLegacyTemplateIdForName(resourceDef.getLegacyTemplate());
        renLink.setLegacyDependentId(getContentId(targetId));
        renLink.setLegacyDependentVariantId(templateId);
        renLink.setLegacyRxInlineSlot("103");
    }
    
    
    private Integer getContentId(String id) {
        notEmpty(id,"id");
        return idMapper.getLocator(idMapper.getGuid(id)).getId();
    }
    
    @SuppressWarnings("deprecation")
    private void fillInlineImageLink(PSInlineRenderLink renLink, PSInlineLinkRequest request) {
        PSAssetResource resourceDef = (PSAssetResource) renLink.getResourceDefinition();
        PSAssetResource thumbResourceDef = (PSAssetResource) renLink.getThumbResourceDefinition();
        renLink.setInlineType("rximage");
        renLink.setLegacyRxInlineSlot("104");
        Integer dependentId = getContentId(request.getTargetId());
        renLink.setLegacyDependentId(dependentId);
        Integer templateId = findLegacyTemplateIdForName(resourceDef.getLegacyTemplate());
        Integer thumbTemplateId = findLegacyTemplateIdForName(thumbResourceDef.getLegacyTemplate());
        renLink.setLegacyDependentVariantId(templateId);
        renLink.setLegacyThumbDependentVariantId(thumbTemplateId);
    }
    
    
    private Integer findLegacyTemplateIdForName(String name) {
        try
        {
            IPSAssemblyTemplate template = assemblyService.findTemplateByName(name);
            IPSGuid guid = template.getGUID();
            return guid.getUUID();
        }
        catch (PSAssemblyException e)
        {
            throw new RuntimeException("Failed to find legacy assembly template.", e);
        }
    }
    
    private IPSGuid siteNameToGuid(String name)
    {
        IPSSite site = siteManager.findSite(name);
        if (site == null)
        {
            // if cannot find by name, then it must be an GUID in string format
            return idMapper.getGuid(name);
        }
        return site.getGUID();

    }
    
    private Node getNode(IPSLinkableItem item) throws PSException {
        IPSGuid guid = idMapper.getGuid(item.getId());
        List<Node> nodes = contentDesignWs.findNodesByIds(asList(guid), true);
        if (nodes == null || nodes.isEmpty())
        {
            throw new PSException("Cannot generate link. Item does not have a node. Item: " + item);
        }
        return nodes.get(0);
    }
    
    /**
     * 
     * Sets all the appropriate properties on a legacy link with data from given parameters.
     * <p>
     * This should be called after the link object is created.
     * 
     * @param legacyContext has to be a subclass of {@link PSAssemblyRenderLinkContext}.
     * @param item never <code>null</code>.
     * @param resourceDefinition never <code>null</code>.
     * @param link never <code>null</code>.
     */
    public void fillLegacyLink(
            PSRenderLinkContext legacyContext, 
            IPSLinkableItem item, 
            PSAssetResource resourceDefinition, 
            PSLegacyLink link) throws ValidationException, PSException {
        notNull(legacyContext);
        notNull(link);
        notNull(resourceDefinition);
        notNull(item);
        isTrue(legacyContext instanceof PSAssemblyRenderLinkContext, 
                "Legacy link generator doesn't work for the inputted link context.");
        
        PSAssemblyRenderLinkContext context = (PSAssemblyRenderLinkContext) legacyContext;

        link.setResourceDefinitionId(resourceDefinition.getUniqueId());
        link.setContext(context.getLegacyLinkContext());
        link.setFilter(context.getFilter());
        if (item.getFolderPath() == null)
        {
            link.setFolderPath(context.getFolderPath());
        }
        else
        {
            link.setFolderPath(item.getFolderPath());
        }
        notNull(context.getSite(), "Site cannot be null");
        
        if (context.getSite() instanceof PSNullSiteSummary) {
            link.setSiteId(0);
        }
        else {
            String siteName = context.getSite().getId();
            notNull(siteName);
            IPSGuid siteId = siteNameToGuid(siteName);
            /*
             * Legacy Legacy Site id.
             */
            Number siteNumber = siteId.getUUID();
            link.setSiteId(siteNumber);
        }

        if(! (item instanceof PSEmptyPage)) {
            Node node = getNode(item);
            link.setNode(node);
            link.setTemplate(PSAssemblyConfig.PERC_RESOURCE_ASSEMBLY_TEMPLATE);
            link.setLegacyTemplate(resourceDefinition.getLegacyTemplate());
        }else{
            throw new ValidationException("No Page Associated With this template");
        }
    }
    
    /**
     * 
     * Represents a legacy link that has not been resolved yet.
     * 
     * @author adamgent
     *
     */
    public static class PSLegacyLink
    {

        @NotNull
        @NotBlank
        private String template;
        
        @NotNull
        @NotBlank
        private String legacyTemplate;

        @NotNull
        private Node node;

        @NotNull
        @NotBlank
        private String folderPath;

        @NotNull
        @NotBlank
        private String filter;

        @NotNegative
        @NotNull
        private Number siteId;

        @NotNegative
        @NotNull
        private Number context;
        
        @NotNull
        @NotBlank
        private String resourceDefinitionId;
        
        
        

        public String getLegacyTemplate()
        {
            return legacyTemplate;
        }

        public void setLegacyTemplate(String legacyTemplate)
        {
            this.legacyTemplate = legacyTemplate;
        }

        public String getResourceDefinitionId()
        {
            return resourceDefinitionId;
        }

        public void setResourceDefinitionId(String resourceDefinitionId)
        {
            this.resourceDefinitionId = resourceDefinitionId;
        }

        public String getTemplate()
        {
            return template;
        }

        public void setTemplate(String template)
        {
            this.template = template;
        }

        public Node getNode()
        {
            return node;
        }

        public void setNode(Node node)
        {
            this.node = node;
        }

        public String getFolderPath()
        {
            return folderPath;
        }

        public void setFolderPath(String folderPath)
        {
            this.folderPath = folderPath;
        }

        public String getFilter()
        {
            return filter;
        }

        public void setFilter(String filter)
        {
            this.filter = filter;
        }

        public Number getSiteId()
        {
            return siteId;
        }

        public void setSiteId(Number siteId)
        {
            this.siteId = siteId;
        }

        public Number getContext()
        {
            return context;
        }

        public void setContext(Number context)
        {
            this.context = context;
        }

    }
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSLegacyLinkGenerator.class);
}

