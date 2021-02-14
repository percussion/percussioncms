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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.data;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.data.PSOrphanedAssetSummary;
import com.percussion.pagemanagement.assembler.impl.PSNullSiteSummary;
import com.percussion.pagemanagement.data.PSEmptyPage;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSWidgetItemSummary;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.sitemanage.data.PSSiteSummary;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.List;

@XmlSeeAlso({PSPage.class,
        PSSiteSummary.class,
        PSAsset.class,
        PSAssetSummary.class,
        PSDataItemSummarySingleFolderPath.class,
        PSEmptyPage.class,
        PSNullSiteSummary.class,
        PSOrphanedAssetSummary.class,
        PSPathItem.class,
        PSWidgetItemSummary.class
})
@XmlRootElement
public class PSDataItemSummary extends PSAbstractPersistantObject implements IPSItemSummary
{


    private String id;
    private String name;
    
    private List<String> folderPaths;
    
    private String icon;
    
    private Category category;
    
    private boolean revisionable = false;
    
    private static final long serialVersionUID = 1L;
    
    /**
     * See {@link #getType()} for detail.
     */
    private String type;

    /**
     * See {@link #getLabel()} for detail.
     */
    private String label;
    
    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }    
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getFolderPaths()
    {
        return folderPaths;
    }

    public void setFolderPaths(List<String> paths)
    {
        this.folderPaths = paths;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    /**
     * Gets the content type of the item.
     * 
     * @return the content type, not blank for a valid object.
     */
    @NotNull
    @NotEmpty
    @NotBlank
    public String getType()
    {
        return type;
    }

    /**
     * Sets the content type of the item.
     * 
     * @param type new content type, should not be blank for a valid object.
     *  
     * @see #getType()
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Determines if this is a folder.
     * 
     * @return <code>true</code> if this is a folder; otherwise return 
     * <code>false</code>.
     */
    public boolean isFolder()
    {
        return "Folder".equals(type) || "FSFolder".equals(type);
    }
    
    @Override
    public boolean isPage()
    {
        return "percPage".equals(type);
    }
    
    public boolean isResource()
    {
        return !isPage() && ("percImageAsset".equals(type) || "percFileAsset".equals(type) ||
                "percFlashAsset".equals(type));
    }

    public Category getCategory()
    {
        return category;
    }
    
    public void setCategory(Category category)
    {
        this.category = category;
    }
    
    /**
     * Gets the content type label of the item.
     * 
     * @return the content type label, not blank for a valid object.
     */
    @NotNull
    @NotEmpty
    @NotBlank
    public String getLabel()
    {
        return label;
    }

    /**
     * Sets the content type label of the item.
     * 
     * @param label new content type label, should not be blank for a valid object.
     *  
     * @see #getLabel()
     */
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    
    
    public boolean isRevisionable()
    {
        return revisionable;
    }

    public void setRevisionable(boolean revisionable)
    {
        this.revisionable = revisionable;
    }



    /**
     * The type for a site item summary
     */
    public static final String TYPE_SITE = "site";    
}
