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
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSRenderLinkContext{");
        sb.append("folderPath='").append(folderPath).append('\'');
        sb.append(", isDeliveryContext=").append(isDeliveryContext);
        sb.append('}');
        return sb.toString();
    }
}
