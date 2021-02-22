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
package com.percussion.pagemanagement.data;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.percussion.sitemanage.data.PSSiteSummary;

/**
 * 
 * Holds all the information needed to create a link
 * other than the resource or page.
 * <p>
 * Although the page and or resource may have most of this
 * information there are some times that it may not.
 * The item will always take priority over the context when
 * generating links but if there is missing information in the item
 * the context can be used.
 * <p>
 * The context can potentially help generate relative links for assets
 * that are on a page that are in the same folder as the page.
 * <p>
 * Example: asset resources do not know what site they are in
 * thus the site information is from this object is used.
 * 
 * @author adamgent
 *
 */
public abstract class PSRenderLinkContext
{
    
    /**
     * @see #getFolderPath()
     */
    private String folderPath;
    
    /**
     * See {@link #isDeliveryContext}
     */
    private boolean isDeliveryContext = false;

    /**
     * The mode for generating links.
     * <p>
     * In the future there might be different types of preview
     * or publish modes. Such as preview with finder decoration.
     * 
     * @author adamgent
     *
     */
    public static enum Mode {
        PUBLISH,PREVIEW;
    }
    

    /**
     * Represents the owner of the link context or the surrounding object type
     * of the link. Basically where or how this link context was generated.
     * <p>
     * If its  standalone asset with an inline link the 
     * owner of that context will be {@link #ASSET}.
     * <p>
     * If its a rendering of a page that has widget links
     * the owner will be {@link #PAGE}.
     * Ditto for {@link #TEMPLATE}.
     * @author adamgent
     *
     */
    public static enum OwnerType {
        PAGE,TEMPLATE,ASSET;
    }
    
    
    
    /**
     * There are atleast two modes of generating links.
     * {@link Mode#PREVIEW} mode and {@link Mode#PUBLISH} mode.
     * @return the mode for generating links, never <code>null</code>.
     */
    @NotNull
    public abstract Mode getMode();
    
    /**
     * A site
     * @return the site never <code>null</code> but maybe a the empty site for preview.
     */
    public abstract PSSiteSummary getSite();
    
    
    /**
     * The current cm system folder path.
     * <p>
     * <strong>This is not the published url or file path!</strong> 
     * @return never <code>null</code>.
     */
    @NotBlank
    @NotNull
    public String getFolderPath()
    {
        return folderPath;
    }
    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    /**
     * Determines if the context is delivery context or assembly context.
     * The delivery context is used to generate publishing locations.
     * The assembly context is used to generate links within HTML pages.
     */
    public boolean isDeliveryContext()
    {
        return isDeliveryContext;
    }
    
    public void setDeliveryContext(boolean context)
    {
        isDeliveryContext = context;
    }
    
    @Override
    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot clone link legacyLinkContext", e);
        }
    }
    
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
    
    

}
