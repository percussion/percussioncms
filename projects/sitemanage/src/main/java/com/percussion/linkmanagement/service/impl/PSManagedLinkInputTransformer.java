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

import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * A item input transformer to process/update any html links in the specified content. This is a thin wrapper, calls the managedlink service to do the actual work.
 * For new items the item id is not yet created when this exit gets processed, it calls manageNewItemLinks method in that case and sets a request private object
 * to track that. The managed link post processor exit checks the private object and if it is there and if its value is true then updates the parent id.
 * 
 * @author JaySeletz
 *
 */
public class PSManagedLinkInputTransformer extends PSDefaultExtension implements IPSFieldInputTransformer
{

    private IPSManagedLinkService service;
    
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
        if(StringUtils.isBlank(value)) {
            return value;
        }
        String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
        if(StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))
        {
            value = service.manageNewItemLinks(value);
            request.setPrivateObject(PSManagedLinksPostProcessor.PERC_UPDATE_NEW_MANAGED_LINKS, true);
        }
        else
        {
            cid = PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(cid)).toString();
            value = service.manageLinks(cid, value);
        }
        return value;
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
