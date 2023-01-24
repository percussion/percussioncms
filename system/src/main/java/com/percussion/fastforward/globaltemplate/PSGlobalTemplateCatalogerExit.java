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
package com.percussion.fastforward.globaltemplate;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Set;

/**
 * This extension catalogs the names of all currently defined global templates
 * and returns it as an XML conforming to the following DTD:
 * <p>
 *    &lt;!ELEMENT GlobalTemplates (Template*)&gt;
 *    &lt;!ELEMENT Template EMPTY&gt;
 *    &lt;!ATTLIST Template
 *       name CDATA #REQUIRED
 *       fileName CDATA #REQUIRED
 *       rootTemplateName CDATA #REQUIRED
 *    &gt;
 * <p>
 * The exit does not take any parameters.
 * <p>
 * The algorithm to find all global template names is as follows:
 * <ol>
 *    <li>
 *       all XSL files are cataloged from the 
 *       &lt;rxroot&gt;/rx_resources/stylesheets/globaltemplates directory
 *    </li>
 *    <li>
 *       all XSL files in which we find a template named 
 *       &lt;filename-without-extension&gt;_root are considered to be a global
 *       template 
 *    </li>
 *    <li>
 *       a list of all global template names as 
 *       &lt;filename-without-extension&gt; is returned
 *    </li>
 * </ol>
 */
public class PSGlobalTemplateCatalogerExit extends PSDefaultExtension 
   implements IPSResultDocumentProcessor
{
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element globalTemplatesElem = doc.createElement(GLOBAL_TEMPLATES_ELEM);
      doc.appendChild(globalTemplatesElem);
      
      Iterator<String> names = getTemplateNames();
      while (names.hasNext())
      {
         Element templateElem = doc.createElement(TEMPLATE_ELEM);
         String name = names.next();
         
         templateElem.setAttribute(NAME_ATTR, name);
         templateElem.setAttribute(FILE_NAME_ATTR, name);
         templateElem.setAttribute(ROOT_TEMPLATE_NAME_ATTR, name + 
            ROOT_EXTENSION);

         globalTemplatesElem.appendChild(templateElem);
      }
      
      return doc;
   }
   
   /**
    * Get a list of all global template names.
    * 
    * @return a <code>String</code> iterator over all global template names, 
    *    never <code>null</code>, may be empty.
    * @throws PSExtensionProcessingException for any error.
    */
   private Iterator<String> getTemplateNames()
         throws PSExtensionProcessingException
   {
      final IPSAssemblyService assembly = 
            PSAssemblyServiceLocator.getAssemblyService();
      try
      {
         final Set<String> templateNames = assembly.findAll57GlobalTemplates();
         final Set<IPSAssemblyTemplate> templates =
               assembly.findAllGlobalTemplates();
         for(IPSAssemblyTemplate t : templates)
         {
            templateNames.add(t.getName());
         }
         return templateNames.iterator();
      }
      catch (PSAssemblyException e)
      {
         throw new PSExtensionProcessingException(EXCEPTION_MSG, e);
      }
   }

   /**
    * The error number indicating to <code>PSException</code> to use the message
    * from the provided exception. 
    */
   private static final int EXCEPTION_MSG = 1002;

   /**
    * The extension appended to the file name for the root template name.
    */
   private final static String ROOT_EXTENSION = "_root";
   
   // private XML constants
   private final static String GLOBAL_TEMPLATES_ELEM = "GlobalTemplates";
   private final static String TEMPLATE_ELEM = "Template";
   private final static String NAME_ATTR = "name";
   private final static String FILE_NAME_ATTR = "fileName";
   private final static String ROOT_TEMPLATE_NAME_ATTR = "rootTemplateName";
}
