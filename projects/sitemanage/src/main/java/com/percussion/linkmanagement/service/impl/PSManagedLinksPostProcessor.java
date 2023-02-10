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
    public static final String PERC_UPDATE_NEW_MANAGED_LINKS = "perc_updateNewManagedLinks";

    
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
