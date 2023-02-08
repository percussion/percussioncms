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

import net.sf.oval.constraint.AssertValid;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * A data object to represent widgets associated to a region.
 * 
 * @see PSRegionWidgets
 * @author adamgent
 *
 */
public abstract class PSRegionWidgetAssociations implements Serializable {

    @AssertValid()
    private Set<PSRegionWidgets> regionWidgetAssociations = new HashSet<>();

    protected PSRegionWidgetAssociations()
    {
        super();
    }


    /**
     * Represents a map of region to a list of widget items.
     * 
     * @return region-widgets assocation.
     */
    @AssertValid()
    @XmlElementWrapper(name = "regionWidgetAssociations")
    @XmlElement(name = "regionWidget")
    public Set<PSRegionWidgets> getRegionWidgetAssociations()
    {
        return regionWidgetAssociations;
    }

    public void setRegionWidgetAssociations(Set<PSRegionWidgets> widgetRegions)
    {
        if (widgetRegions == null) return;
        this.regionWidgetAssociations = new HashSet<> (widgetRegions);
    }

    
    public Map<String, List<PSWidgetItem>> getRegionWidgetsMap() {
        Map<String, List<PSWidgetItem>> map = new HashMap<>();
        Collection<PSRegionWidgets> regionWidgets = getRegionWidgetAssociations();
        if (regionWidgets != null) {
            for (PSRegionWidgets w : regionWidgets) {
                map.put(w.getRegionId(), w.getWidgetItems());
            }
        }
        return map;
    }
    
    public void setRegionWidgets(String regionId, List<PSWidgetItem> widgetItems) {
        notEmpty(regionId, "regionId");
        notNull(widgetItems, "widgetItems");
        PSRegionWidgets a = getRegion(regionId);
        if ( a != null) {
            /*
             * We have to remove a from the set because we are modifying it.
             * You have to be really careful with sets when modifying what it contains.
             */
            getRegionWidgetAssociations().remove(a);
        }
        else if ( a == null) {
            a = new PSRegionWidgets();
        }
        a.setRegionId(regionId);
        a.setWidgetItems(widgetItems);
        getRegionWidgetAssociations().add(a);
        
        
    }
    
    public PSRegionWidgets getRegion(String regionId) {
        Collection<PSRegionWidgets> regionWidgets = getRegionWidgetAssociations();
        if (regionWidgets == null) return null;
        for(PSRegionWidgets w: regionWidgets) {
            if (StringUtils.equals(w.getRegionId(), regionId)) {
                return w;
            }
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSRegionWidgetAssociations)) return false;
        PSRegionWidgetAssociations that = (PSRegionWidgetAssociations) o;
        return Objects.equals(getRegionWidgetAssociations(), that.getRegionWidgetAssociations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegionWidgetAssociations());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSRegionWidgetAssociations{");
        sb.append("regionWidgetAssociations=").append(regionWidgetAssociations);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public PSRegionWidgetAssociations clone()
    {
        try
        {
            return (PSRegionWidgetAssociations) BeanUtils.cloneBean(this);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot clone", e);
        }
    }
    


}
