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
