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
import com.percussion.share.data.PSAbstractPersistantObject;
import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    
    @AssertValid()
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
    @AssertValid()
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSRegionWidgets)) return false;
        if (!super.equals(o)) return false;
        PSRegionWidgets that = (PSRegionWidgets) o;
        return getRegionId().equals(that.getRegionId()) && getWidgetItems().equals(that.getWidgetItems());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getRegionId(), getWidgetItems());
    }
}
