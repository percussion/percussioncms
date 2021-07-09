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
package com.percussion.pagemanagement.service;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRenderResult;
import com.percussion.pagemanagement.data.PSTemplate;

/**
 * 
 * Renders templates, pages and regions.
 * 
 * @author adamgent
 *
 */
public interface IPSRenderService
{
    /**
     * Assembles a single region that appears on the supplied page and returns
     * the serialized version. Sets the edit mode flag. See
     * {@link #renderPageForEdit(String)} for details of this flag.
     * 
     * @param page The page to assemble. Not <code>null</code>.
     * @param regionId The name of the region within the supplied page. Not
     * blank.
     * 
     * @return Just the rendered region as a string within the returned object.
     * The supplied <code>regionId</code> is set on the returned object.
     * 
     * @throws PSRenderServiceException
     */
    public PSRenderResult renderRegion(PSPage page, String regionId) throws PSRenderServiceException;

    public String renderRegionAll( PSTemplate template ) throws PSRenderServiceException;
    
    public PSRenderResult renderRegion(PSTemplate template, String regionId) throws PSRenderServiceException;
    
    public String renderTemplate(String id) throws PSRenderServiceException;
    
    /**
     * Similar to the {@link #renderTemplate(String)}, except sets a scriptsOff variable as true to the context.
     * So that the macros can render the template by stripping the script tags.
     * @param id, the string format of template item guid.
     * @return The rendered template, typically an (x)html document. Never
     * <code>null</code> or empty.
     * @throws PSRenderServiceException
     */
    public String renderTemplateScriptsOff(String id) throws PSRenderServiceException;
    
    public String renderPage(String id) throws PSRenderServiceException;

    /**
     * Similar to {@link #renderPage(String)}, except sets a flag that can be
     * used by widgets if they want or need to render their output differently
     * when a page is being edited. For example, if a widget has no content, it
     * may render some sample content or it may allow in-line editing of its
     * content. Each widget is responsible for what is rendered in this
     * situation.
     * 
     * @param id The unique identifier of the page to be rendered. Not blank.
     * (TODO: ph - as a user of the API, where do I get one of these ids?)
     * 
     * @return The rendered page, typically an (x)html document. Never
     * <code>null</code> or empty.
     * @throws PSRenderServiceException 
     */
    public String renderPageForEdit(String id, String editType) throws PSRenderServiceException;
    
    /**
     * Similar to {@link #renderPageForEdit(String)}, this method sets scripts off flag to the $perc context. So that
     * the velocity macros can strip the scripts if the flag is set to false.
     * @param id, the string format of the guid of the page.
     * @return The rendered page, typically an (x)html document. Never
     * <code>null</code> or empty.
     * @throws PSRenderServiceException
     */
    public String renderPageForEditScriptsOff(String id, String editType) throws PSRenderServiceException;

    public PSRegion parse(String html) throws PSRenderServiceException;
    
    public static class PSRenderServiceException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        public PSRenderServiceException(String message) {
            super(message);
        }
        
        public PSRenderServiceException(String message, Throwable cause) {
            super(message, cause);
        }
        public PSRenderServiceException(Throwable cause) {
            super(cause);
        }
        
    }

}
