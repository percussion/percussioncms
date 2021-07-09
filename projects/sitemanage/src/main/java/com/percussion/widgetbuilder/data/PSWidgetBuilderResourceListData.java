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
package com.percussion.widgetbuilder.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container object for a list of resource files
 * 
 * @author JaySeletz
 *
 */
@XmlRootElement(name="WidgetBuilderResourceListData")
@JsonRootName("WidgetBuilderResourceListData")
public class PSWidgetBuilderResourceListData extends PSAbstractDataObject
{
    private List<String> resourceList = new ArrayList<>();

    public static PSWidgetBuilderResourceListData fromXml(String resourceXml)
    {
        return PSSerializerUtils.unmarshal(resourceXml, PSWidgetBuilderResourceListData.class);
    }
    
    public String toXml()
    {
        return PSSerializerUtils.marshal(this);
    }
    
    /**
     * Get the list of resources in this list.
     * 
     * @return The list, not <code>null</code>, may be empty.
     */
    public List<String> getResourceList()
    {
        return resourceList;
    }

    /**
     * Set the list of resources
     * 
     * @param resourceList The list, not <code>null</code>, may be empty.
     */
    public void setResourceList(List<String> resourceList)
    {
        this.resourceList = resourceList;
    }
}
