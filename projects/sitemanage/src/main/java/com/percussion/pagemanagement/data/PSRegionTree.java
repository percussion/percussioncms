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
