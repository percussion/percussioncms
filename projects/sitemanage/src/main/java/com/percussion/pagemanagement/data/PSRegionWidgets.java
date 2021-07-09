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
import com.percussion.share.data.PSAbstractPersistantObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

/**
 * 
 * Contains a {@link PSRegion} id to list of {@link PSWidgetItem}  association.
 * The order of the list from {@link #getWidgetItems()} is important and is
 * the order that should be presented in the region.
 * <p>
 * This object contains the serialized {@link PSWidgetItem}. In other words
 * the {@link PSWidgetItem} is stored in this object but with out the asset
 * associations. The associations to the assets are stored elsewhere.
 * 
 * 
 * @author adamgent
 *
 */
@JsonRootName("WidgetRegion")
public class PSRegionWidgets extends PSAbstractPersistantObject
{
    
    @NotNull
    @NotBlank
    private String regionId;
    
    @AssertValid(requireValidElements=true)
    private List<PSWidgetItem> widgetItems = new ArrayList<>();
    
    /**
     * The id of the region.
     * @return never <code>null</code>.
     */
    @NotNull
    @NotBlank
    public String getRegionId()
    {
        return regionId;
    }
    public void setRegionId(String regionId)
    {
        this.regionId = regionId;
    }
    
    /**
     * Returns the widgets in the correct order.
     * @return never <code>null</code>, maybe empty.
     */
    @AssertValid(requireValidElements=true)
    @XmlElementWrapper(name = "widgetItems")
    @XmlElement(name = "widgetItem")
    public List<PSWidgetItem> getWidgetItems()
    {
        return widgetItems;
    }
    public void setWidgetItems(List<PSWidgetItem> widgetItems)
    {
        this.widgetItems = widgetItems;
    }
    
    @Override
    public String getId()
    {
        return getRegionId();
    }
    @Override
    public void setId(String id)
    {
        setRegionId(id);
    }
    
    private static final long serialVersionUID = 1L;
    

}
