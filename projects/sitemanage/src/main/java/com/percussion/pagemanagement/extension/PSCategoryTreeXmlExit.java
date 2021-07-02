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

import com.percussion.category.extension.PSCategoryControlUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pagemanagement.service.IPSPageCategoryService;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.jexl.PSDocumentUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.Document;

public class PSCategoryTreeXmlExit implements IPSResultDocumentProcessor
{

    private IPSPageCategoryService pageCategoryService;
    
    @Override
    public void init(@SuppressWarnings("unused") IPSExtensionDef extDef, @SuppressWarnings("unused") File file)
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }

    @Override
    public boolean canModifyStyleSheet()
    {
        return false;
    }

    @Override
    public Document processResultDocument(@SuppressWarnings("unused") Object[] args, IPSRequestContext requestContext, 
            @SuppressWarnings("unused") Document document) throws PSParameterMismatchException,
            PSExtensionProcessingException
    {
        DOMWriter domWriter = new DOMWriter();
        org.dom4j.Document doc;
        try
        {
            String sitename = requestContext.getParameter("sitename");
            String rootpath = requestContext.getParameter("rootpath");
        	String returnString = PSCategoryControlUtils.getCategoryXmlInString(PSCategoryControlUtils.getCategories(sitename, rootpath, false, true));
            returnString =  returnString.replace("<topLevelNodes>", "");
            returnString =  returnString.replace("</topLevelNodes>", "");
        	doc = PSCategoryControlUtils.convertToOldFormatXml(DocumentHelper.parseText(returnString));
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve category xml: {}",e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSExtensionProcessingException("Failed to retrieve category xml: ", e);
        }
        
        try
        {
            return domWriter.write(doc);
        }
        catch (DocumentException e)
        {
            log.error("Failed to retrieve category xml: {}",e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSExtensionProcessingException("Failed to write category xml: ", e);
        }   
    }  
    
    protected String getResourceUrl() throws PSDataServiceException {
        return getPageCategoryService().loadConfiguration().getTree().getUrl();
    }
    protected String loadResource(String resourceUrl) throws IOException, ServletException {
        return new  PSDocumentUtils().getDocument(resourceUrl);
    }

    public IPSPageCategoryService getPageCategoryService()
    {
        return pageCategoryService;
    }

    public void setPageCategoryService(IPSPageCategoryService pageCategoryService)
    {
        this.pageCategoryService = pageCategoryService;
    }
    
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSCategoryTreeXmlExit.class);
}
