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
