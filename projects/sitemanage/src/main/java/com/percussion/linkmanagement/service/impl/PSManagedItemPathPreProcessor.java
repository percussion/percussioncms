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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * @author JaySeletz
 *
 */
public class PSManagedItemPathPreProcessor extends PSDefaultExtension implements IPSItemInputTransformer
{
    private IPSManagedLinkService service;
    
    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
        super.init(def, codeRoot);
        //This is for wiring the services
        PSSpringWebApplicationContextUtils.injectDependencies(this);

    }    


    @SuppressWarnings("unused")
    @Override
    public void preProcessRequest(Object[] params, IPSRequestContext request) throws PSAuthorizationException,
            PSRequestValidationException, PSParameterMismatchException, PSExtensionProcessingException
    {
        String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
        if(StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))
        {
            service.initNewItemLinks();
            request.setPrivateObject(PSManagedLinksPostProcessor.PERC_UPDATE_NEW_MANAGED_LINKS, true);
        }
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
