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
package com.percussion.pagemanagement.dao;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService.PSResourceDefinitionNotFoundException;
import com.percussion.pagemanagement.service.PSResourceServiceException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;

import java.util.List;

/**
 * 
 * Low level Retrieving of Resource Definitions.
 * 
 * @author adamgent
 *
 */
public interface IPSResourceDefinitionGroupDao extends IPSGenericDao<PSResourceDefinitionGroup, String>
{


    PSResourceDefinitionGroup find(String id) throws PSDataServiceException;

    List<PSResourceDefinition> findAllResources() throws PSResourceServiceException, PSDataServiceException;
    
    /**
     * Finds a resource based on the uniqueId.
     * 
     * @param uniqueId never <code>null</code>.
     * @return maybe <code>null</code>.
     * @throws PSResourceDefinitionNotFoundException 
     */
    PSResourceDefinition findResource(String uniqueId) throws PSDataServiceException;
    
    /**
     * Finds the primary {@link PSAssetResource} for the given content type.
     * @param contentType never <code>null</code>.
     * @return maybe <code>null</code>.
     * @throws PSResourceDefinitionNotFoundException
     */
    PSAssetResource findAssetResourceForType(String contentType) throws PSDataServiceException;
    
    /**
     * Finds all asset resources definitions for a given content type.
     * @param contentType
     * @return never <code>null</code>, maybe empty.
     */
    List<PSAssetResource> findAssetResourcesForType(String contentType) throws PSResourceServiceException, PSDataServiceException;
    
    /**
     * Finds resources associated to a legacy template.
     * @param template never <code>null</code>.
     * @return never <code>null</code>, maybe empty.
     */
    List<PSAssetResource> findAssetResourcesForLegacyTemplate(String template) throws PSResourceServiceException, PSDataServiceException;;
    
}
