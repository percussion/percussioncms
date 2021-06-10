/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionTree;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * A node on the merged region tree region that has been merged with {@link PSRegionTree}
 * and {@link PSRegionBranches}.
 * 
 * @see PSAbstractMergedRegionTree
 * @author adamgent
 *
 */
public class PSMergedRegion
{

    
    /**
     * If {@link #subRegions} is not empty
     * then {@link #widgetInstances} should be.
     */
    private transient List<PSMergedRegion> subRegions;
    private List<PSWidgetInstance> widgetInstances;
    private List<PSRegionResult> results;
    private transient PSAbstractRegion overriddenRegion;
    
    private PSMergedRegionOwner owner = PSMergedRegionOwner.TEMPLATE;
    
    /**
     * The original region is either
     * or a {@link PSRegion}.
     */
    private transient PSAbstractRegion originalRegion;
    
    
    public PSMergedRegion(PSAbstractRegion originalRegion)
    {
        super();
        this.originalRegion = originalRegion;
    }

    
    public final PSAbstractRegion getOriginalRegion()
    {
        return originalRegion;
    }


    public List<PSRegionResult> getResults()
    {
        return results;
    }


    public void setResults(List<PSRegionResult> results)
    {
        this.results = results;
    }


    public List<PSWidgetInstance> getWidgetInstances()
    {
        return widgetInstances;
    }

    public void setWidgetInstances(List<PSWidgetInstance> widgetInstances)
    {
        this.widgetInstances = widgetInstances;
    }

    public List<PSMergedRegion> getSubRegions()
    {
        return subRegions;
    }

    public void setSubRegions(List<PSMergedRegion> subRegions)
    {
        this.subRegions = subRegions;
    }

    public String getRegionId()
    {
        if (originalRegion == null) {
            return null;
        }

            return originalRegion.getRegionId();

    }
    
    

    public PSMergedRegionOwner getOwner()
    {
        return owner;
    }


    public void setOwner(PSMergedRegionOwner owner)
    {
        this.owner = owner;
    }


    public PSAbstractRegion getOverriddenRegion()
    {
        return overriddenRegion;
    }


    public void setOverriddenRegion(PSAbstractRegion overriddenRegion)
    {
        this.overriddenRegion = overriddenRegion;
    }

    
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("regionId", getRegionId())
            .append("owner", getOwner())
            .append("widgetInstances", getWidgetInstances()).toString();
    }




    /**
     * Indicates whether the template or page owns this region.
     * @author adamgent
     *
     */
    public static enum PSMergedRegionOwner {
        TEMPLATE,PAGE;
    }
}
