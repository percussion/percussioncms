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
