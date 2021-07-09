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

import com.percussion.cms.PSContentEditorWalker;
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

import java.io.File;

import org.apache.commons.lang.StringUtils;

/**
 * Field output transformer to update the managed item paths on edit. This is a thin wrapper, calls the managedlink service to do the actual work.
 * @author JaySeletz
 *
 */
public class PSManagedItemPathOutputTransformer extends PSDefaultExtension implements IPSFieldOutputTransformer
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
        String path = ep.getStringParam(0, null, true);
        String linkIdField = ep.getStringParam(1, null, false);
        
        if(StringUtils.isBlank(linkIdField)) {
            return path;
        }
        String linkId = (String) PSContentEditorWalker.getDisplayFieldValue(request.getInputDocument(), linkIdField);
        if(StringUtils.isBlank(linkIdField)) {
            return path;
        }
        
        
        String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
        //For new items content id doesn't exists, simply return the value
        if(StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))
        {
            return path;
        }
        
        return service.renderItemPath(null, linkId);
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
