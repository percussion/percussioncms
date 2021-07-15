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

import static org.apache.commons.lang.Validate.notNull;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.MatchPattern;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.lang.StringUtils;

import com.percussion.share.data.PSAbstractDataObject;


/**
 * Defines a relationship between a page or template, widget, and asset.
 * 
 * @author adamgent
 * @author peterfrontiero
 * 
 */
@XmlRootElement(name="AssetWidgetRelationship")
public class PSAssetWidgetRelationship extends PSAbstractDataObject
{
    
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor. For serializers.
     */
    public PSAssetWidgetRelationship()
    {
    }

    /**
     * Constructs an instance of the class.
     * 
     * @param ownerId the id of the owner of this relationship. Should be either
     * a page or template item.
     * @param widgetId the id of the widget instance of this relationship.
     * @param widgetName the name of the widget definition of this relationship.
     * Never blank.
     * @param assetId the id of the asset of this relationship. Assumes that it
     * is a local asset. If a shared asset, you need to call
     * {@link #setResourceType(PSAssetResourceType) setResourceType}(
     * {@link PSAssetResourceType#shared}). Never blank.
     * @param assetOrder the sort order of the asset within the widget.
     */
    public PSAssetWidgetRelationship(String ownerId, long widgetId, String widgetName, String assetId, int assetOrder)
    {
        if (StringUtils.isBlank(ownerId)) {
            throw new IllegalArgumentException("ownerId may not be blank.");
        }
        if (StringUtils.isBlank(widgetName)) {
            throw new IllegalArgumentException("widgetName may not be blank.");
        }
        if (StringUtils.isBlank(assetId)){
            throw new IllegalArgumentException("assetId may not be blank.");}
        
        notNull(resourceType, "resourceType");

        this.ownerId = ownerId;
        this.widgetId = widgetId;
        this.widgetName = widgetName;
        this.assetId = assetId;
        this.assetOrder = assetOrder;
        this.resourceType = PSAssetResourceType.local;
    }
    
    public PSAssetWidgetRelationship(String ownerId, long widgetId, String widgetName, String assetId, int assetOrder,
            String widgetInstanceName)
    {
        this(ownerId, widgetId, widgetName, assetId, assetOrder);
        this.widgetInstanceName = widgetInstanceName;        
    }
    
    
    
    /**
     * @return the ownerId
     */
    public String getOwnerId()
    {
        return ownerId;
    }

    /**
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    /**
     * @return the widget instance Id
     */
    public long getWidgetId()
    {
        return widgetId;
    }

    /**
     * @param widgetId the widget instance Id to set
     */
    public void setWidgetId(long widgetId)
    {
        this.widgetId = widgetId;
    }

    /**
     * @return the widget definition name
     */
    public String getWidgetName()
    {
        return widgetName;
    }

    /**
     * @param widgetName the widget definition name to set
     */
    public void setWidgetName(String widgetName)
    {
        this.widgetName = widgetName;
    }

    /**
     * @return the assetOrder
     */
    public int getAssetOrder()
    {
        return assetOrder;
    }

    /**
     * @param assetOrder the assetOrder to set
     */
    public void setAssetOrder(int assetOrder)
    {
        this.assetOrder = assetOrder;
    }

    /**
     * @return the assetId
     */
    public String getAssetId()
    {
        return assetId;
    }

    /**
     * @param assetId the assetId to set
     */
    public void setAssetId(String assetId)
    {
        this.assetId = assetId;
    }
    
    /**
     * @return the action
     */
    public PSAssetWidgetRelationshipAction getAction()
    {
       return action;
    }

    /**
     * @param action to set
     */
    public void setAction(PSAssetWidgetRelationshipAction action)
    {
       this.action = action;
    }
    
    /**
     * @return the asset resource type
     */
    public PSAssetResourceType getResourceType()
    {
       return resourceType;
    }

    /**
     * @param resourceType to set
     */
    public void setResourceType(PSAssetResourceType resourceType)
    {
        notNull(resourceType, "resourceType");
        
       this.resourceType = resourceType;
    }
    
    
    /**
     * 
     * When associated an asset to a widget the client can 
     * request that the asset be put in a asset library folder.
     * <p>
     * This is not needed for clearing the relationship.
     * @return maybe <code>null</code>.
     */
    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    /**
     * @param widgetInstanceName the widgetInstanceName to set
     */
    public void setWidgetInstanceName(String widgetInstanceName)
    {
        this.widgetInstanceName = widgetInstanceName;
    }

    /**
     * @return the widgetInstanceName
     */
    public String getWidgetInstanceName()
    {
        return widgetInstanceName;
    }

    /**
     * The relationship ID if this is referring to an existing relationship.
     * @return the relationship ID. It is <code>-1</code> if unknown.
     */
    public int getRelationshipId()
    {
        return relationshipId;
    }
    
    /**
     * Sets the relationship ID.
     * @param rid the new relationship ID. It should be greater than <code>0</code> for an valid relationship.
     */
    public void setRelationshipId(int rid)
    {
        relationshipId = rid;
    }
    
    /**
     * The replaced relationship ID. This is used to replace an existing relationship.
     * @return the relationship ID. It is <code>-1</code> if unknown.
     */
    public int getReplacedRelationshipId()
    {
        return replacedRelationshipId;
    }
    
    public void setReplacedRelationshipId(int rid)
    {
        replacedRelationshipId = rid;
    }
    
    @NotNull
    @NotEmpty
    private String ownerId;
    
    @NotNull
    @NotNegative
    @Min(value=1)
    private long widgetId;
        
    @NotNull
    @NotEmpty
    private String widgetName;
    
    @NotNull
    @NotNegative
    private int assetOrder = 0;
        
    @NotNull
    @NotEmpty
    private String assetId;
    
    private String widgetInstanceName;

    private int relationshipId = -1;
    
    private int replacedRelationshipId = -1;
    
    /**
     * See {@link PSAssetWidgetRelationshipAction}.
     */
    private PSAssetWidgetRelationshipAction action;
    
    @NotNull
    private PSAssetResourceType resourceType = PSAssetResourceType.local;
    
    @NotBlank
    @MatchPattern(pattern = {"^/.*$"})
    private String folderPath;
    
    

    /**
     * Describes the type of action to be taken when adding an asset to a widget which already contains assets.
     * 
     * @author peterfrontiero
     */
    public enum PSAssetWidgetRelationshipAction {
        /**
         * The asset will be inserted after all current assets.
         */
        append
    }
    
    /**
     * Describes the type of resource that the asset will be added as. 
     * 
     * @author peterfrontiero
     */
    public enum PSAssetResourceType {
        /**
         * Can be used on only one Page or Template.
         */
        local,
        
        /**
         * Can be used on multiple Pages and/or Templates.
         */
        shared
    }
}
