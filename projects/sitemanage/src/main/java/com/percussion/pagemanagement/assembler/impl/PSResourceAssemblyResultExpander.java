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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.assembler.impl;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceLocation;
import com.percussion.rx.publisher.IPSAssemblyResultExpander;
import com.percussion.rx.publisher.impl.PSAbstractAssemblyResultExpander;
import com.percussion.rx.publisher.impl.PSPublishHandler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.util.PSSiteManageBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * This result expander expands an assembly result into multiple assembly items
 * based on the resource definitions associated with the assembly item's
 * content type.
 * <p>
 * For example an assembly result for an image content type might 
 * have two resource definitions: full and thumbnail.
 * Thus full and thumbnail would be expanded to two assembly items
 * with the full and thumbnail resource definition ids added 
 * to each assembly item respectivley.
 * <p>
 * This expander will also use the newer link/location services to
 * generate the delivery location of the assembly item.
 * 
 * @author adamgent
 *
 */
@PSSiteManageBean("resourceAssemblyResultExpander")
public class PSResourceAssemblyResultExpander extends PSAbstractAssemblyResultExpander 
    implements IPSAssemblyResultExpander
{
    
    public static final String ASSEMBLY_RESULT_EXPANDER_NAME = PSResourceAssemblyResultExpander.class.getSimpleName();
    
    private PSAssemblyItemBridge assemblyItemBridge;

    @Autowired
    public PSResourceAssemblyResultExpander(PSAssemblyItemBridge assemblyItemBridge, PSPublishHandler publishHandler)
    {
        this(assemblyItemBridge, publishHandler, ASSEMBLY_RESULT_EXPANDER_NAME);
    }
    
    protected PSResourceAssemblyResultExpander(PSAssemblyItemBridge assemblyItemBridge, PSPublishHandler publishHandler, String expanderName)
    {
        super();
        this.assemblyItemBridge = assemblyItemBridge;
        /*
         * Register our result expander with the publishing handler.
         */
        publishHandler.getAssemblyResultExpanders().put(expanderName, this);
    }

    @Override
    public List<IPSAssemblyItem> expand(IPSAssemblyResult result) throws Exception
    {
        List<PSAssetResource> resources = new ArrayList<>(getAssemblyItemBridge().getResourceDefinitions(result));

        // AssetResource list is backed by a HashSet need to order in consistent way to get
        // page numbers correct.  Primary resource should always be page 1.
        Comparator<PSAssetResource> resourceComparator = new Comparator<PSAssetResource>() {
            @Override public int compare(PSAssetResource s1, PSAssetResource s2) {
                if (s1.isPrimary() || s2.isPrimary())
                {
                    return (s1.isPrimary() == s2.isPrimary() ? 0 : (s2.isPrimary() ? 1 : -1));
                }
                return s1.getUniqueId().compareTo(s2.getUniqueId());
            }           
        };
        
        Collections.sort(resources, resourceComparator);
        
        List<IPSAssemblyItem> assemblyItems = new ArrayList<>();
        boolean isPaginated = resources.size() > 1;
        int i = 1;
        
        for(PSAssetResource r : resources) {
            IPSAssemblyItem assemblyItem = clone(result);
            if (isPaginated)
                assemblyItem.setPage(i);
            if (i==1) assemblyItem.setReferenceId(result.getReferenceId());
            getAssemblyItemBridge().setResourceDefinititionId(assemblyItem, r.getUniqueId());
            PSResourceLocation location = getAssemblyItemBridge().getResourceLocation(assemblyItem);
            if (location != null) {
                PSAssemblyItemBridge.setDeliveryLocation(assemblyItem, location.getFilePath());
            }
            /*
             * We are assuming that we should not have to set the template as it will be the
             * resource template that requested this expander.
             */
            assemblyItems.add(assemblyItem);
            i++;
        }
        return assemblyItems;
    }
    protected PSAssemblyItemBridge getAssemblyItemBridge()
    {
        return assemblyItemBridge;
    }
}
