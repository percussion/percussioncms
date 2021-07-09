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
package com.percussion.assetmanagement.data;

import static org.apache.commons.lang.Validate.notEmpty;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

/**
 * Object to represent an unused asset in a given page.
 * 
 * @author Santiago M. Murchio
 * 
 */
@XmlRootElement(name = "UnusedAssetSummary")
@JsonRootName("UnusedAssetSummary")
public class PSUnusedAssetSummary extends PSDataItemSummary implements IPSItemSummary, Comparable<PSUnusedAssetSummary>
{
    /**
     * This fields is used to label the asset on the UI, in the unused assets
     * tray. The items will have the form 'Untitled < asset type label > <#>'.
     */
    private String title = "Untitled";
    
    /**
     * This is the icon to use for the asset when the even 'onhover' takes
     * place. It is the same as the asset icon, just adding the 'Over' word in
     * the last part of the file name. For example, for icon widgetIcon.png, the
     * over icon would be widgetIconOver.png.
     */
    private String overIcon;
    
    /**
     * This field is used to get the widget Id on the UI, in the unused assets
     * tray. This data is required to edit and delete unused assets.
     */
    private String widgetId;
    
    private int relationshipId;
    
    public PSUnusedAssetSummary()
    {
        super();
    }
    
    /**
     * @param summary
     */
    public PSUnusedAssetSummary(PSDataItemSummary summary)
    {
        super(); 
        setName(summary.getName());
        setId(summary.getId());
        setLabel(summary.getLabel());
        setIcon(summary.getIcon());
        setCategory(summary.getCategory());
        setFolderPaths(summary.getFolderPaths());
        setType(summary.getType());
        setRevisionable(summary.isRevisionable());
    }

    @NotNull
    @NotEmpty
    @NotBlank
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        notEmpty(title);
        this.title = title;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(PSUnusedAssetSummary other)
    {
        return title.compareTo(other.title);
    }

    /**
     * @param overIcon the overIcon to set
     */
    public void setOverIcon(String overIcon)
    {
        notEmpty(overIcon);
        this.overIcon = overIcon;
    }

    /**
     * @return the overIcon
     */
    @NotNull
    @NotEmpty
    @NotBlank
    public String getOverIcon()
    {
        return overIcon;
    }

    /**
     * @return the widgetId
     */
    @NotNull
    @NotEmpty
    @NotBlank
    public String getWidgetId()
    {
        return widgetId;
    }

    /**
     * @param widgetId the widgetId to set
     */
    public void setWidgetId(String widgetId)
    {
        this.widgetId = widgetId;
    }

    public int getRelationshipId()
    {
        return relationshipId;
    }

    /**
     * @param relationshipId the relationshipId to set
     */
    public void setRelationshipId(int relationshipId)
    {
        this.relationshipId = relationshipId;
    }
    
}
