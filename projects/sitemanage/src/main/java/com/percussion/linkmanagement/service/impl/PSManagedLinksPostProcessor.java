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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.linkmanagement.service.impl;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

/**
 * Managed link post processor, the input transformer creates new links on item creation and sets a request private object.
 * This post processor updates the managed links if the private object exists and if it's true.
 * @author BJoginipally
 *
 */
public class PSManagedLinksPostProcessor extends PSDefaultExtension implements IPSResultDocumentProcessor
{

    private IPSManagedLinkService service;
    public static String PERC_UPDATE_NEW_MANAGED_LINKS = "perc_updateNewManagedLinks";

    
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
    public boolean canModifyStyleSheet()
    {
        return false;
    }

    @SuppressWarnings("unused")
    @Override
    public Document processResultDocument(Object[] params, IPSRequestContext request, Document resultDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException
    {
        Object updateNewLinks = request.getPrivateObject(PERC_UPDATE_NEW_MANAGED_LINKS);
        if(updateNewLinks == null || !(Boolean)updateNewLinks)
        {
            return resultDoc;
        }
        
        request.setPrivateObject(PERC_UPDATE_NEW_MANAGED_LINKS, null);

        String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
        if(StringUtils.isNotBlank(cid) && StringUtils.isNumeric(cid))
        {
            cid = PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(cid)).toString();
            service.updateNewItemLinks(cid);
        }
        return resultDoc;
    }
    /**
     * Setter for dependency injection
     * 
     * @param service the service to set
     */
    public void setService(IPSManagedLinkService service)
    {
        this.service = service;
    }

}
