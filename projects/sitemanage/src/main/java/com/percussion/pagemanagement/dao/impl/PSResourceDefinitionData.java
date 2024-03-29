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
package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.data.IPSResourceDefinitionVisitor;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFolderResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.data.PSThemeResource;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * A container to hold resource definition data in memory.
 * Hash Maps are used for performance instead of other collections.
 * @author adamgent
 *
 */
@Component("resourceDefinitionData")
@Lazy
public class PSResourceDefinitionData
{
    private Map<PSResourceDefinitionUniqueId, PSResourceDefinition> resourceDefinitions = new HashMap<>();
    private Map<String, PSResourceDefinitionGroup> resourceDefinitionGroups = new HashMap<>();
    private Map<String, PSAssetResource> primaryAssetResources = new HashMap<>();
    private Map<String, Set<PSAssetResource>> contentTypeAssetResources = new HashMap<>();
    private Map<String, Set<PSAssetResource>> legacyTemplateAssetResources = new HashMap<>();
    
    private IPSResourceDefinitionVisitor resourceVisitor = new ResourceVisitor();
    
    public void add(PSResourceDefinitionGroup group) throws PSDataServiceException {
        notNull(group);
        notEmpty(group.getId());
        resourceDefinitionGroups.put(group.getId(), group);
        List<PSResourceDefinition> rds = new ArrayList<>();
        add(rds, group.getAssetResources());
        add(rds, group.getFileResources());
        add(rds, group.getFolderResources());
        for (PSResourceDefinition rd : rds) {
            PSResourceDefinitionUniqueId uid = new PSResourceDefinitionUniqueId(group.getId(), rd.getId());
            rd.setGroupId(uid.getGroupId());
            rd.setId(uid.getLocalId());
            rd.setUniqueId(uid.getUniqueId());
            resourceDefinitions.put(uid, rd);
            rd.accept(resourceVisitor);
        }
        
    }
    
    private void add(Collection<PSResourceDefinition> merged, Collection<? extends PSResourceDefinition> add) {
        if (add != null) {
            merged.addAll(add);
        }
    }
    
    
    /**
     * Returns resources marked as primary where the key is the content type. 
     * @return ContentTypeName ==> AssetResource map, never <code>null</code>.
     */
    public final Map<String, PSAssetResource> getPrimaryAssetResources()
    {
        return primaryAssetResources;
    }
    
    /**
     * Legacy templates associated to a resources.
     * Key is the legacy template name and value is a set of assets with
     * that legacy template.
     * 
     * @return LegacyTemplateName ==> Set of Asset resources.
     */
    public Map<String, Set<PSAssetResource>> getLegacyTemplateAssetResources()
    {
        return legacyTemplateAssetResources;
    }

    /**
     * Resource map where the key is the content type.
     * @return never <code>null</code>.
     */
    public final Map<String, Set<PSAssetResource>> getContentTypeAssetResources()
    {
        return contentTypeAssetResources;
    }

    public final Map<PSResourceDefinitionUniqueId, PSResourceDefinition> getResourceDefinitions()
    {
        return resourceDefinitions;
    }


    public final Map<String, PSResourceDefinitionGroup> getResourceDefinitionGroups()
    {
        return resourceDefinitionGroups;
    }
    
    
    protected class ResourceVisitor implements IPSResourceDefinitionVisitor {

        public void visit(PSAssetResource resource)
        {
            String ct = resource.getContentType();
            if (isBlank(ct)) {
                log.error("Content type is null for resource: {} " , resource);
                return;
            }
            String template = resource.getLegacyTemplate();
            
            /*
             * Add content type asset resources assocations.
             */
            Set<PSAssetResource> ars = contentTypeAssetResources.get(ct);
            ars = ars == null ? new HashSet<>() : ars;
            ars.add(resource);
            contentTypeAssetResources.put(ct, ars);
            
            /*
             * Add to primary asset resource assocations.
             */
            if(resource.isPrimary()) {
                if (ct != null) {
                    primaryAssetResources.put(ct, resource);
                }
            }
            
            /*
             * Add to template asset resource associates.
             */
            if (template != null) {
                Set<PSAssetResource> trs = legacyTemplateAssetResources.get(template);
                trs = trs == null ? new HashSet<>() : trs;
                trs.add(resource);
                legacyTemplateAssetResources.put(template, trs);
            }
        }

        public void visit(@SuppressWarnings("unused") PSFileResource resource)
        {
            return;
        }

        public void visit(@SuppressWarnings("unused") PSFolderResource resource)
        {
            return;
        }

        @Override
        public void visit(@SuppressWarnings("unused") PSThemeResource resource)
        {
            return;
        }
    
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSResourceDefinitionData.class);


}
