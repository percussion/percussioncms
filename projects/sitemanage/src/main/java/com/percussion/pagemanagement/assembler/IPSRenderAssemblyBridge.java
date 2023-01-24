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
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.guid.IPSGuid;

/**
 * 
 * Low level api that works directly with Rhythmyx Assembly engine. All render
 * methods assume editMode unless they expose the flag in their signature.
 * 
 * @author adamgent
 * 
 */
public interface IPSRenderAssemblyBridge
{

    /**
     * Just like {@link #renderPage(String, boolean)}, except an object is
     * supplied rather than the id of an existing object. TODO: ph - does the
     * page need to exist in this case?
     * 
     * @param page never <code>null</code>.
     * @param editMode 
     * @param scriptsOff, a boolean scripts off flag that is bind to the context so that it is available to the macros
     * and macros then make use of this variable to strip the script tags.
     * @return never <code>null</code>.
     */
    public String renderPage(PSPage page, boolean editMode, boolean scriptsOff) throws IPSPageService.PSPageException;
    
    /**
     * Renders a valid template. Sets editMode to <code>true</code>.
     * @param template never <code>null</code>.
     * @param scriptsOff, a boolean scripts off flag that is bind to the context so that it is available to the macros
     * and macros then make use of this variable to strip the script tags.
     * @return never <code>null</code>.
     */
    public String renderTemplate(PSTemplate template, boolean scriptsOff) throws IPSPageService.PSPageException;
    
    /**
     * Renders a valid template with a page in it. Sets editMode to <code>true</code>.
     * @param template never <code>null</code>.
     * @param page never <code>null</code>.
     * @param scriptsOff, a boolean scripts off flag that is bind to the context so that it is available to the macros
     * and macros then make use of this variable to strip the script tags.
     * @return never <code>null</code>.
     */
    public String renderTemplateWithPage(PSTemplate template, PSPage page, boolean scriptsOff) throws IPSPageService.PSPageException;

    /**
     * Assembles a page with the given id.
     * 
     * @param id not blank. TODO - ph - what format is expected (string format of a
     * {@link IPSGuid}?)
     * 
     * @param editMode If <code>true</code> and a widget on the page has no
     * content, it can optionally add some content to aid the viewer; or it may
     * render in such a way as to allow in-line editing. Whether a widget will
     * add sample content is up to the widget developer.
     * @param scriptsOff, a boolean scripts off flag that is bind to the context so that it is available to the macros
     * and macros then make use of this variable to strip the script tags.
     * 
     * @return never <code>null</code>.
     */
    public String renderPage(String id, boolean editMode, boolean scriptsOff) throws IPSPageService.PSPageException, PSValidationException;

    /**
     * This is the same as {@link #renderPage(String, boolean, boolean)}, 
     * in addition, it provides an option to specify the edited item type. 
     * @param type the edited item type.
     */
    public String renderPage(String id, boolean editMode, boolean scriptsOff, EditType type) throws IPSPageService.PSPageException, PSValidationException;


    /**
     * Renders a template with the given id. Sets editMode to <code>true</code>.
     * @param id never <code>null</code>, empty, or blank.
     * @param scriptsOff, a boolean scripts off flag that is bind to the context so that it is available to the macros
     * and macros then make use of this variable to strip the script tags.
     * @return never <code>null</code>.
     */
    public String renderTemplate(String id, boolean scriptsOff) throws IPSPageService.PSPageException, PSValidationException;
    
    /**
     * The <strong>name</strong> of the legacy assembly template used to assemble the page/template.
     * This is for the legacy assembler and is usually configured through spring.
     * @return never <code>null</code>, empty, or blank.
     */
    public String getDispatchTemplate();
    
    /**
     * Gets the ID of the dispatch template.  See {@link #getDispatchTemplate()}.
     * 
     * @return the ID of the dispatch template, never <code>null</code>.
     */
    public IPSGuid getDispatchTemplateId() throws IPSPageService.PSPageException;
    
    /**
     * Gets the {@link IPSAssemblyItem} object used for previewing the specified page.
     * 
     * @param id the ID of the page, must not be blank.
     * @param editMode See {@link IPSRenderAssemblyBridge#renderPage(String, boolean, boolean)}.
     * @param scriptsOff See  {@link IPSRenderAssemblyBridge#renderPage(String, boolean, boolean)}. 
     * @param editType the edited item type. This is only useful if {@param editMode} is <code>true</code>.
     * 
     * @return a {@link IPSAssemblyItem} object, never <code>null</code> or empty.
     */
    public IPSAssemblyItem getWorkItemForPreview(String id, boolean editMode, boolean scriptsOff, EditType editType) throws IPSPageService.PSPageException;
    
    public static class PSRenderAssemblyBridgeException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public PSRenderAssemblyBridgeException(String message)
        {
            super(message);
        }

        public PSRenderAssemblyBridgeException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSRenderAssemblyBridgeException(Throwable cause)
        {
            super(cause);
        }

    }

}
