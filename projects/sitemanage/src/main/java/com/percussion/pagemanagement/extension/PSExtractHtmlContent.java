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

package com.percussion.pagemanagement.extension;

import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.pagemanagement.service.IPSRenderService;
import com.percussion.search.lucene.textconverter.PSTextConverterHtml;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.publishing.IPSPublishingWs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This exit assembles a page in the preview context and then extracts the html content.
 * @author peterfrontiero
 *
 */
public class PSExtractHtmlContent implements IPSUdfProcessor
{
    private IPSRenderService renderService;
    private IPSGuidManager guidMgr;
    private IPSContentDesignWs contentDesignWs;
    private IPSIdMapper idMapper;
    private IPSPublishingWs publishingWs;
    
    @Override
    public void init(IPSExtensionDef extDef, File file) throws PSExtensionException
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }

    @Override
    public Object processUdf(Object[] params, IPSRequestContext request)
    {
        // Only run if a load by a search index update is in progress
        Object objIsLoadForSearch = request.getPrivateObject(IPSConstants.LOAD_FOR_SEARCH_INDEX);
        boolean isLoadForSearch = (objIsLoadForSearch instanceof Boolean)
                && ((Boolean) objIsLoadForSearch).booleanValue();
        if (!isLoadForSearch)
            return "";

    	// We need at least the content id
        if (null == params || params.length == 0 || null == params[0] || StringUtils.isEmpty(params[0].toString()))
        {
            return "";
        }
        String cTypeIdStr = params[0].toString();
        IPSGuid guid = guidMgr.makeGuid(cTypeIdStr, PSTypeEnum.LEGACY_CONTENT);

        if (publishingWs.getItemSites(guid).isEmpty())
        {
            // page is not associated with a site, cannot be assembled
            return "";
        }

        PSWebserviceUtils.setUserName(request.getOriginalSubject().getName());

        // assemble the page
        String renderedPage = renderService.renderPage(idMapper.getString(guid));
        if (renderedPage.contains("<html"))
        {
            // remove everything before the start of the html tag to allow for proper extraction
            renderedPage = renderedPage.substring(renderedPage.indexOf("<html"));
        }

        // extract the html content

        try(InputStream bis = new ByteArrayInputStream(renderedPage.getBytes(IPSUtilsConstants.RX_JAVA_ENC)) ){
            PSTextConverterHtml converter = new PSTextConverterHtml();
            return converter.getConvertedText(bis,"");

        } catch (IOException | PSExtensionProcessingException  e) {
            PSConsole.printMsg(this.getClass().getName(), e.getLocalizedMessage());
        }

        return "";
    }  
    
    public IPSRenderService getRenderService()
    {
        return renderService;
    }

    public void setRenderService(IPSRenderService renderService)
    {
        this.renderService = renderService;
    }
    
    public IPSGuidManager getGuidMgr()
    {
        return guidMgr;
    }

    public void setGuidMgr(IPSGuidManager guidMgr)
    {
        this.guidMgr = guidMgr;
    }

    public IPSContentDesignWs getContentDesignWs()
    {
        return contentDesignWs;
    }

    public void setContentDesignWs(IPSContentDesignWs contentDesignWs)
    {
        this.contentDesignWs = contentDesignWs;
    }
    
    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }
    
    public IPSPublishingWs getPublishingWs()
    {
        return publishingWs;
    }

    public void setPublishingWs(IPSPublishingWs publishingWs)
    {
        this.publishingWs = publishingWs;
    }

    /**
     * Logger for this exit.
     */
    public static Log log = LogFactory.getLog(PSExtractHtmlContent.class);
}
