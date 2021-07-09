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

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import net.sf.oval.constraint.AssertValid;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * A data object to represent widgets associated to a region.
 * 
 * @see PSRegionWidgets
 * @author adamgent
 *
 */
public abstract class PSRegionWidgetAssociations
{

    @AssertValid(requireValidElements=true)
    private Set<PSRegionWidgets> regionWidgetAssociations = new HashSet<>();

    public PSRegionWidgetAssociations()
    {
        super();
    }


    /**
     * Represents a map of region to a list of widget items.
     * 
     * @return region-widgets assocation.
     */
    @AssertValid(requireValidElements=true)
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
    
//    public List<PSWidgetItem> getWidgetsForRegion(String regionId) {
//        Collection<PSRegionWidgets> regionWidgets = getRegionWidgetAssocations();
//        if (regionWidgets != null) {
//            for (PSRegionWidgets w : regionWidgets) {
//                if (regionId.equals(w.getRegionId())) {
//                    return w.getWidgetItems();
//                }
//            }
//        }
//        return new ArrayList<PSWidgetItem>();
//    }
    
    public Map<String, List<PSWidgetItem>> getRegionWidgetsMap() {
        Map<String, List<PSWidgetItem>> map = new HashMap<String, List<PSWidgetItem>>();
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
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }
    
    @Override
    public int hashCode()
    {
        //Do not use the id to generate a hash code.
        return HashCodeBuilder.reflectionHashCode(this, new String[]
        {"id"});
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
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
