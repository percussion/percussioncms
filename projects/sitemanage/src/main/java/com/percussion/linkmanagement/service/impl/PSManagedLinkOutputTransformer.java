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
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.server.IPSRequestContext;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Field output transformer to update the managed links on edit. This is a thin wrapper, calls the managedlink service to do the actual work.
 * @author BJoginipally
 *
 */
public class PSManagedLinkOutputTransformer extends PSDefaultExtension implements IPSFieldOutputTransformer
{

    private IPSManagedLinkService service;

    /*
     * (non-Javadoc)
     * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
     */
    @Override
    public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
    {
        PSExtensionParams ep = new PSExtensionParams(params);
        String value = ep.getStringParam(0, null, true);
        if(StringUtils.isBlank(value)) {
            return value;
        }
        String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
        //For new items content id doesn't exists, simply return the value
        if(StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))
        {
            return value;
        }
        
       // cid = PSGuidManagerLocator.getGuidMgr().makeGuid(new PSLocator(cid)).toString();
        value = service.renderLinks(null, value, Integer.parseInt(cid));
        return value;    
    }
    
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
