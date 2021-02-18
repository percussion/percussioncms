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
package com.percussion.linkmanagement.service.impl;

import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.pagemanagement.data.PSInlineLinkRequest;
import com.percussion.pagemanagement.data.PSInlineRenderLink;
import com.percussion.pagemanagement.service.IPSRenderLinkService;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A field input transformer to convert any new style managed links in the specified content to old style inline links. 
 * If an anchor link has IPSManagedLinkService#PERC_MANAGED_ATTR with value as true, then calls the managed link service to 
 * get the dependent id corresponding to the link element and then calls renderlink service to get the details required for the
 * old style managed link and adds the them as attributes on the link element. The inline link processor will take care of the
 * creation of management of links.
 * 
 * Caution: This gets loaded from old style inline link handlers from core to generate the paths for 
 * new style links. 
 *
 */
public class PSManagedLinksConverter extends PSDefaultExtension implements IPSFieldInputTransformer
{

    public static final String RXHYPERLINK = "rxhyperlink";
    public static final String RXIMAGE = "rximage";
    public static final String INLINETYPE = "inlinetype";
    public static final String RXINLINESLOT = "rxinlineslot";
    public static final String SYS_DEPENDENTID = "sys_dependentid";
    public static final String SYS_DEPENDENTVARIANTID = "sys_dependentvariantid";
    private static final Log log = LogFactory.getLog(PSManagedLinksConverter.class);
    
    private IPSManagedLinkService managedService;
    private IPSRenderLinkService renderService;
    
    /* (non-Javadoc)
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
        super.init(def, codeRoot);
        //This is for wiring the services
        PSSpringWebApplicationContextUtils.injectDependencies(this);

    }

    @Override
    public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
    {
        PSExtensionParams ep = new PSExtensionParams(params);
        String value = ep.getStringParam(0, null, true);
        boolean returnMap = params.length > 1 && Boolean.parseBoolean(ep.getStringParam(1, "false", false));
        if(StringUtils.isBlank(value))
            return value;
        Map<String, String> attribs = new HashMap<>();
        String updatedValue = processLinksAndImages(value, attribs);
        return returnMap?attribs:updatedValue;
    }
    
    public String processLinksAndImages(String value, Map<String, String> attribs)
    {
        Document doc = Jsoup.parseBodyFragment(value);
        Elements elems = null;
        Elements imgElems = null;
    	elems = doc.select(IPSManagedLinkService.A_HREF);
    	imgElems = doc.select(IPSManagedLinkService.IMG_SRC);
        
        if(elems.size()<1 && imgElems.size()<1)
            return value;
        for (Element elem : elems)
        {
            if(!elem.hasAttr(IPSManagedLinkService.LEGACY_INLINETYPE) && (managedService.doManageAll() || elem.attr(IPSManagedLinkService.PERC_MANAGED_ATTR).equalsIgnoreCase(IPSManagedLinkService.TRUE_VAL)))
                convertToOldLinks(elem, RXHYPERLINK, attribs);
        }
        for (Element elem : imgElems)
        {
            if(!elem.hasAttr(IPSManagedLinkService.LEGACY_INLINETYPE) && (managedService.doManageAll() || elem.attr(IPSManagedLinkService.PERC_MANAGED_ATTR).equalsIgnoreCase(IPSManagedLinkService.TRUE_VAL)))
                convertToOldLinks(elem, RXIMAGE, attribs);
        }        

        return doc.html();
    }
    
    @SuppressWarnings("deprecation")
    private void convertToOldLinks(Element elem, String type, Map<String, String> attribs)
    {
        int dependent = -1;
        try
        {
            dependent = managedService.getDependent(elem);
            if(dependent != -1)
            {
                String depGuid = PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(dependent)).toString();
                if(depGuid == null)
                    return;
                PSInlineRenderLink renderLink;
                String path = "img".equalsIgnoreCase(elem.tagName())?elem.attr(IPSManagedLinkService.SRC_ATTR):elem.attr(IPSManagedLinkService.HREF_ATTR);
                if (RXHYPERLINK.equalsIgnoreCase(type) && (path.startsWith("/Sites/") || path.startsWith("//Sites/")))
                {
                	renderLink = renderService.renderPreviewPageLink(depGuid);
                }
                else
                {
                    PSInlineLinkRequest linkRequest = new PSInlineLinkRequest();
                    linkRequest.setTargetId(depGuid);
                    renderLink = renderService.renderPreviewResourceLink(linkRequest);
                }
                
                if (renderLink != null)
                {
                    // Fix path for link
                    if ("img".equalsIgnoreCase(elem.tagName()))
                    {
                        managedService.renderImageLink(null, elem);
                        attribs.put("path",elem.attr("src"));
                    }
                    else
                    {
                        managedService.renderLink(null, elem);
                        attribs.put("path",elem.attr("href"));
                    }
                    
                    //As it creates an old style link, these deprecated values are required to make the link.
                    elem.attr(SYS_DEPENDENTVARIANTID, "" + renderLink.getLegacyDependentVariantId());
                    attribs.put(SYS_DEPENDENTVARIANTID, renderLink.getLegacyDependentVariantId().toString());
                    elem.attr(SYS_DEPENDENTID, "" + renderLink.getLegacyDependentId());
                    attribs.put(SYS_DEPENDENTID, renderLink.getLegacyDependentId().toString());
                    elem.attr(RXINLINESLOT, "" + renderLink.getLegacyRxInlineSlot());
                    attribs.put(RXINLINESLOT, renderLink.getLegacyRxInlineSlot().toString());
                    elem.attr(INLINETYPE, type);
                    attribs.put(INLINETYPE, type);
                   
                  
                } 
               
                elem.removeAttr(IPSManagedLinkService.PERC_MANAGED_ATTR);
                elem.removeAttr(IPSManagedLinkService.PERC_LINKID_ATTR);
                elem.removeAttr(IPSManagedLinkService.PERC_LINKID_OLD_ATTR);
                elem.removeAttr(IPSManagedLinkService.PERC_MANAGED_OLD_ATTR);
            }
        }
        catch(Exception e)
        {
            log.warn("Failed to convert the managed link in rich text editor.",e);
        }
    }
    /**
     * Setter for dependency injection
     * 
     * @param service the service to set
     */
    public void setManagedService(IPSManagedLinkService managedService)
    {
        this.managedService = managedService;
    }

    /**
     * Setter for dependency injection
     * 
     * @param service the service to set
     */
    public void setRenderService(IPSRenderLinkService renderService)
    {
        this.renderService = renderService;
    }
    
}
