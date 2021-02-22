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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;

/**
 * 
 * Represents an abstract object that is bound to the
 * the rendering of a {@link PSPage}, {@link PSWidgetItem},
 * or {@link PSTemplate}.
 * <p>
 * Concrete implementations are bound to <code>$perc</code>
 * 
 * @see PSPageAssemblyContext
 * @see PSWidgetAssemblyContext
 * @author adamgent
 *
 */
public abstract class PSAbstractAssemblyContext implements Cloneable
{
    /**
     * never <code>null</code>.
     */
    private PSPage page;

    /**
     * never <code>null</code>.
     */
    private PSTemplate template;
    
    /**
     * never <code>null</code>.
     * @see #getRootRenderType()
     */
    private RootRenderType rootRenderType = RootRenderType.PAGE;
    
    private EditType editType = EditType.PAGE;

    /**
     * never <code>null</code>.
     */
    private PSRenderLinkContext linkContext;

    /**
     * See {@link #isEditMode()} for details. Defaults to <code>false</code>.
     */
    private boolean editMode;
    
    /**
     * See {@link #isPreviewMode()} for details
     */
    private boolean previewMode;    

    /**
     * See {@link #isScriptsOff()} for details. Defaults to <code>false</code>.
     */
    private boolean scriptsOff;
    
    /**
     * The page object. If in template layout mode this will be a blank page.
     * <p>
     * <b>Binding:</b> $perc.page
     * @return never <code>null</code>.
     */
    public PSPage getPage()
    {
        return page;
    }

    public void setPage(PSPage page)
    {
        this.page = page;
    }

    /**
     * The template object.
     * <p>
     * <b>Binding:</b> $perc.template
     * @return never <code>null</code>.
     */
    public PSTemplate getTemplate()
    {
        return template;
    }

    public void setTemplate(PSTemplate template)
    {
        this.template = template;
    }
    
    
    /**
     * The link context used to create links.
     * <p>
     * <b>Binding:</b> $perc.linkContext
     * @return never <code>null</code>.
     */
    public PSRenderLinkContext getLinkContext()
    {
        return linkContext;
    }

    public void setLinkContext(PSRenderLinkContext linkContext)
    {
        this.linkContext = linkContext;
    }

    
    /**
     * The type of item that this context was created for.
     * If the rendering is for a page than {@link RootRenderType#PAGE}
     * will be returned. If the rendering is for a template than
     * the return value will be {@link RootRenderType#TEMPLATE}
     * returned.
     * 
     * @return never <code>null</code>.
     */
    public RootRenderType getRootRenderType()
    {
        return rootRenderType;
    }

    public void setRootRenderType(RootRenderType renderType)
    {
        this.rootRenderType = renderType;
    }

    /**
     * The type of the item this context is created for while the item is been edited.
     * This value is only useful if {@link #isEditMode()} is <code>true</code>.
     * It is default to {@link EditType#PAGE}.
     * 
     * @return the edit type, never <code>null</code>.
     */
    public EditType getEditType()
    {
        return editType;
    }
    
    public void setEditType(EditType editType)
    {
        this.editType = editType;
    }
    
    @Override
    protected Object clone() 
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * See {@link #isEditMode()} for details.
     * @param editMode 
     */
    public void setEditMode(boolean editMode)
    {
        this.editMode = editMode;
    }

    /**
     * This flag is provided to indicate whether the requester plans on using
     * the assembled document in an editor. Widgets can use this flag to change
     * their behavior. For example, if they don't have an asset, they should
     * render some sample content such as 'Add image here' for an image widget.
     * Other possibilities would be to build an in-line editor to re-order a
     * manual list.
     * 
     * @return The mode as requested by the caller.
     */
    public boolean isEditMode()
    {
        return editMode;
    }
    
    

    /**
    * A flag indicating that the assembled document will be used in preview mode.
    * Widgets can use this flag to change their behavior. For example this
    * may be used to show a placeholder when in preview mode but the real content
    * on a published page. Usually needed for delivery side content widgets.
    * @return the previewMode
    */
   public boolean isPreviewMode()
   {
      return previewMode;
   }

   /**
    * See {@link #isPreviewMode()} for details
    * @param previewMode the previewMode to set
    */
   public void setPreviewMode(boolean previewMode)
   {
      this.previewMode = previewMode;
   }

   /**
     * See {@link #isScriptsOff()} for details.
     * @param scriptsOff 
     */
    public void setScriptsOff(boolean scriptsOff)
    {
        this.scriptsOff = scriptsOff;
    }

    /**
     * This flag is provided to indicate whether the requester plans on using
     * the assembled document with javascript code stripped or not. Some customer 
     * javascript code causes conflicts with our code, this flag can be used to 
     * determine whether to render the javascript code or strip it. 
     * 
     * @return The scripts on flag as requested by the caller.
     */
    public boolean isScriptsOff()
    {
        return scriptsOff;
    }

    public enum RootRenderType {
        PAGE, TEMPLATE
    }
    
    public enum EditType {
        PAGE, TEMPLATE
    }

}