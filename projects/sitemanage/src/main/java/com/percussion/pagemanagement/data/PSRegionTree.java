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
package com.percussion.pagemanagement.data;


import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotNull;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains the region tree and 
 * region-widget association.
 * @author adamgent
 *
 */
@XmlRootElement(name = "RegionTree")
@JsonRootName("RegionTree")
public class PSRegionTree extends PSRegionWidgetAssociations
{
    @NotNull
    private PSRegion rootRegion;
    
    public PSRegion getRootRegion()
    {
        return rootRegion;
    }

    public void setRootRegion(PSRegion rootRegion)
    {
        this.rootRegion = rootRegion;
    }

    /**
     * Gets all descendant regions, which does not include the root region.
     * If the region tree is not empty, then the 1st element of the returned list 
     * is the most outer region and there is only one outer region for a valid region tree.
     * 
     * @return the list of descendant regions. It may be empty if there is no child regions, never <code>null</code>. 
     * 
     * @exception IllegalStateException if there is more than one direct child region node
     * under the root node.
     */
    public List<PSRegion> getDescendentRegions()
    {
        PSRegion outer = getOuterRegion();
        if (outer == null) {
            return Collections.emptyList();
        }
        
        return outer.getAllRegions();
    }

    private PSRegion getOuterRegion()
    {
        if (rootRegion == null) {
            return null;
        }
        
        PSRegion outerRegion = null;
        for (PSRegionNode node : rootRegion.getChildren())
        {
            if (node instanceof PSRegion)
            {
                if (outerRegion != null) {
                    throw new IllegalStateException("Region Tree must have only one outer region, but there are more than one.");
                }
                
                outerRegion = (PSRegion) node;
            }
        }
        return outerRegion;
    }
    
}
