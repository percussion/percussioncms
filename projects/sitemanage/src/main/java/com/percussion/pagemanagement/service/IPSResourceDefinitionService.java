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

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSCatalogService;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;

/**
 * 
 * Loads and {@link PSResourceDefinitionGroup}s and {@link PSResourceDefinition}.
 * This service does <em>NOT</em> convert the resources to preview/publish links.
 * 
 * @author adamgent
 *
 */
public interface IPSResourceDefinitionService extends IPSCatalogService<PSResourceDefinitionGroup, String>
{
    
    /**
     * {@link PSResourceDefinition} have a global unique identifier
     * based on the {@link PSResourceDefinitionGroup} that contains them
     * and there own id with {@link #NAMESPACE_SEPARATOR} between them.
     */
    public static final String NAMESPACE_SEPARATOR = ".";
    
    public static final String THEME_GROUP_NAME = "theme";
    
    /**
     * Find all resource definition groups.
     * 
     * @return List of populated objects
     */
    List<PSResourceDefinitionGroup> findAll() throws PSDataServiceException;
    
    /**
     * Finds all resource definitions.
     * @return never <code>null</code> maybe empty.
     * @throws DataServiceLoadException
     * @throws DataServiceNotFoundException
     */
    List<PSResourceDefinition> findAllResources() throws PSDataServiceException;

    /**
     * 
     * Will find a resource definition group.
     *
     * @param id the id is the filename minus the extension.
     * @return a resource definition group.
     * @throws DataServiceLoadException TODO
     */
    PSResourceDefinitionGroup find(String id) throws DataServiceLoadException, PSResourceDefinitionGroupNotFoundException;
    
    /**
     * Finds a resource based on the uniqueId.
     * For unique id see {@link #createUniqueId(String, String)}.
     * @param uniqueId never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSResourceDefinitionNotFoundException
     */
    PSResourceDefinition findResource(String uniqueId) throws PSDataServiceException;
    
    
    /**
     * Finds the default resource based on the content type of the asset.
     * @param contentType never <code>null</code>.
     * @return never <code>null</code>.
     * @throws PSResourceDefinitionNotFoundException if a default asset could not be found.
     */
    PSAssetResource findDefaultAssetResourceForType(String contentType) throws PSDataServiceException;

    /**
     * Finds resources based on the content type of the asset.
     * @param contentType never <code>null</code>.
     * @return never <code>null</code> maybe empty.
     */
    List<PSAssetResource> findAssetResourcesForType(String contentType) throws PSDataServiceException;
    
    /**
     * Finds resources associated to a legacy template.
     * @param template never <code>null</code> or empty.
     * @return never <code>null</code>, maybe empty.
     */
    List<PSAssetResource> findAssetResourcesForLegacyTemplate(String template) throws PSDataServiceException;
    
    
    /**
     * Creates a unique identifier to resource given its group id
     * and its local id.
     * @param groupId id of the {@link PSResourceDefinitionGroup}, never <code>null</code>.
     * @param id the {@link PSResourceDefinition#getId()} never <code>null</code>.
     * @return uniqueId never <code>null</code>.
     * @throws PSResourceDefinitionInvalidIdException
     */
    String createUniqueId(String groupId, String id) throws PSResourceDefinitionInvalidIdException;
    
    
    /**
     * Thrown for system failures like an invalid definition id.
     * @author adamgent
     *
     */
    public static class PSResourceDefinitionInvalidIdException extends PSDataServiceException {
        
        private static final long serialVersionUID = 1L;
        
        public PSResourceDefinitionInvalidIdException(String message) {
            super(message);
        }
        
        public PSResourceDefinitionInvalidIdException(String message, Throwable cause) {
            super(message, cause);
        }
        public PSResourceDefinitionInvalidIdException(Throwable cause) {
            super(cause);
        }
        
    }
    
    /**
     * The service guarentees that it will find a definition otherwise an exception is thrown.
     * @author adamgent
     *
     */
    public static class PSResourceDefinitionNotFoundException extends DataServiceNotFoundException {
        
        private static final long serialVersionUID = 1L;
        
        public PSResourceDefinitionNotFoundException(String message) {
            super(message);
        }
        
        public PSResourceDefinitionNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
        public PSResourceDefinitionNotFoundException(Throwable cause) {
            super(cause);
        }
        
    }
    
    /**
     * The service guarentees that it will find a definition otherwise an exception is thrown.
     * @author adamgent
     *
     */
    public static class PSResourceDefinitionGroupNotFoundException extends DataServiceNotFoundException {
        
        private static final long serialVersionUID = 1L;
        
        public PSResourceDefinitionGroupNotFoundException(String message) {
            super(message);
        }
        
        public PSResourceDefinitionGroupNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
        public PSResourceDefinitionGroupNotFoundException(Throwable cause) {
            super(cause);
        }
        
    }
    

}
