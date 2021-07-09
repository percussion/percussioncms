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
package com.percussion.pagemanagement.service;

import java.util.List;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;

/**
 * Manages creating links, file locations from {@link PSResourceInstance}s.
 * 
 * 
 * @author adamgent
 * @see IPSResourceService
 */
public interface IPSResourceLinkAndLocationService
{
    
    /**
     * Creates a resource instance from the link context, item and resource definition id.
     * The resource definition id should be fully qualified if its not <code>null</code>.
     * If the resource definition id resolves to a resource definition that is not applicable for the item
     * an exception will be thrown. 
     *  
     * @param context never <code>null</code>.
     * @param item never <code>null</code>.
     * @param resourceDefinitionId The unique id for a resource definition. 
     * If <code>null</code> the resource definition with {@link PSAssetResource#isPrimary()} set to <code>true</code>
     * for the items type ({@link IPSLinkableItem#getType()}) will be used.
     * @return never <code>null</code>.
     * 
     * @throws PSResourceServiceException If the resource definition is not applicable for the item.
     */
    PSResourceInstance createResourceInstance(PSRenderLinkContext context, IPSLinkableItem item, String resourceDefinitionId)
            throws PSResourceServiceException, PSDataServiceException;
    
    /**
     * Resolves link and locations for the resource instance.
     * 
     * @param resourceInstance never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSResourceServiceException
     * @see {@link PSResourceInstance#getLinkAndLocations()}
     */
    List<PSResourceLinkAndLocation> resolveLinkAndLocations(PSResourceInstance resourceInstance)
            throws PSResourceServiceException, IPSAssetService.PSAssetServiceException;
    
}
