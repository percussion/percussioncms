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
package com.percussion.fastforward.globaltemplate;


import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class wraps the part of rx_resources/rx_Globals.xsl file that is
 * specific to global templates for Rhythmyx 5.5 or
 * <em>rx_resources/rx_GlobalTemplates.xsl</em>. This file contains one
 * <em>&lt;xsl:import&gt;</em> for each global template for the CMS. The main
 * responsibility of this class is to create the global template XSLs and
 * maintain corresponding XSL imports within this style sheet. This maintains
 * auto upgradeability by wrapping and modifying one of the two above files
 * looking at the new or old style global templating. It evaluates whether the
 * system is using new or old style templating based on whether the stylesheet
 * imports any global template or not.
 *  
 */
@SuppressWarnings(value={"unchecked"})
public class PSRxGlobals extends PSLoggable
{
   /**
    * Constructor. Loads the <em>rx_Globals.xsl</em> or
    * <em>rx_GlobalTemplates.xsl</em> (post 5.5 of Rhythmyx) file from the
    * known location {@link #RX_GLOBALS_PATH}as an XML document.
    * 
    * @param logger {@link org.apache.logging.log4j.Logger logger}to log the
    *           information, warnings or errors. May be <code>null</code> in
    *           which case a logger with default named category will be created.
    * @throws IOException if the file cannot be read for any reason.
    * @throws SAXException if the file cannot be parsed as an XML document.
    *  
    */
   public PSRxGlobals(Logger logger) throws IOException, SAXException
   {
      super(logger);
      logInfo("Opening file: " + m_globalTemplateFilePath);
      FileReader fr = null;
      try
      {
         fr = new FileReader(m_globalTemplateFilePath);
         m_doc = PSXmlDocumentBuilder.createXmlDocument(fr, false);
      }
      finally
      {
         if (fr != null)
         {
            try
            {
               fr.close();
            }
            catch (Exception e)
            {
            }
         }
      }
      logInfo("Checking if the system is using old style templating...");
      NodeList nlImports = m_doc.getElementsByTagName("xsl:import");
      String fileUrlPath = "file:" + REL_GLOBAL_TEMPLATES_PATH + "/";
      for (int i = 0; i < nlImports.getLength(); i++)
      {
         Element importElem = (Element) nlImports.item(i);
         String name = importElem.getAttribute("href");
         if (name.startsWith(fileUrlPath))
         {
            m_isNewStyle = false;
            break;
         }
      }
      if (m_isNewStyle)
      {
         logInfo("System is using new style templating.");
         logInfo("Opening file: " + m_globalTemplateFilePath);
         m_globalTemplateFilePath = m_RootDir + RX_GLOBAL_TEMPLATES_PATH;
         fr = new FileReader(m_globalTemplateFilePath);
         m_doc = PSXmlDocumentBuilder.createXmlDocument(fr, false);
         fr.close();
      }
      else
      {
         logInfo("System is using old style templating.");
      }
   }

   /**
    * Add the specified global template to the map of global templates of 
    * this object. If a global template by name already exists in the map, 
    * it will keep the existing template and ignore the request.
    * @param template global template object to add, must not be 
    * <code>null</code>. 
    */
   public void addGlobalTemplate(PSGlobalTemplate template)
   {
      if (template == null)
         throw new IllegalArgumentException("template must not be null");

      String name = template.getName();
      logInfo("Adding global template named: " + name);
      if (m_GlobalTemplates.containsKey(name))
      {
         logInfo("Global template named: " + name + " already exists");
      }
      else
      {
         m_GlobalTemplates.put(name, template);
      }
   }

   /**
    * This method does the following:
    * <ol>
    * <li>Save each template file to its respective file/location</li>
    * <li>Make sure one &lt;xsl:import&gt; exists for each template in the
    * object</li>
    * <li>Finally save the file to its location</li>
    * </ol>
    * 
    * @throws IOException in case of any error writing the document.
    * @throws PSGlobalTemplateException in case of any error during creation of
    *            global templates.
    */
   public void save() throws IOException, PSGlobalTemplateException
   {
      saveTemplateFiles();
      updateDispatchingTemplate();
      cleanupImports();

      logInfo("Saving modifed " + m_globalTemplateFilePath + "...");

      OutputStreamWriter writer = null;
      try
      {
         writer = new OutputStreamWriter(new FileOutputStream(
               m_globalTemplateFilePath), StandardCharsets.UTF_8);
         PSXmlDocumentBuilder.write(m_doc, writer);
         writer.flush();
      }
      finally
      {
         if (writer != null)
         {
            try
            {
               writer.close();
            }
            catch (Exception e)
            {
            }
         }
      }

      logInfo("End saving modifed " + m_globalTemplateFilePath + "...");
   }
   
   /**
    * This method walks all <code>choose</code> elements of the 
    * <code>psx-global-template-dispatcher</code> template and adds a new 
    * <code>choose</code> element for each defined global template that has 
    * no entry yet. The entire <code>psx-global-template-dispatcher</code> 
    * template is added if its not defined yet. 
    */
   private void updateDispatchingTemplate()
   {
      logInfo("Update global template dispatching template...");
      
      Element stylesheet = m_doc.getDocumentElement();
      
      Element dispatchingTemplate = null;
      NodeList templates = stylesheet.getElementsByTagName("xsl:template");
      for (int i=0; dispatchingTemplate==null && i<templates.getLength(); i++)
      {
         Element template = (Element) templates.item(i);
         String name = template.getAttribute("name");
         if (name.equals(DISPATCHING_TEMPLATE_NAME))
            dispatchingTemplate = template;
      }
      
      // remove the dispatcher if no global templates are defined
      if (m_GlobalTemplates.isEmpty())
      {
         logInfo("No global templates are defined.");
         if (dispatchingTemplate != null)
         {
            logInfo("Remove dispatching template.");
            stylesheet.removeChild(dispatchingTemplate);
         }
         
         return;
      }
      
      // add the dispatcher if not existing yet
      Element choose = null;
      Element otherwise = null;
      if (dispatchingTemplate == null)
      {
         logInfo("No dispatching template defined, adding a new one.");
         dispatchingTemplate = m_doc.createElement("xsl:template");
         dispatchingTemplate.setAttribute("name", DISPATCHING_TEMPLATE_NAME);
         stylesheet.appendChild(dispatchingTemplate);
         
         choose = m_doc.createElement("xsl:choose");
         dispatchingTemplate.appendChild(choose);
         
         otherwise = m_doc.createElement("xsl:otherwise");
         choose.appendChild(otherwise);
         
         Element rootCall = m_doc.createElement("xsl:call-template");
         rootCall.setAttribute("name", "xsplit_root");
         otherwise.appendChild(rootCall);
      }
      else
      {
         // there is always exactly one choose element
         NodeList chooseElements = dispatchingTemplate.getElementsByTagName(
            "xsl:choose");
         choose = (Element) chooseElements.item(0);

         // there is always maximum one otherwise element
         NodeList otherwiseElements = dispatchingTemplate.getElementsByTagName(
            "xsl:otherwise");
         if (otherwiseElements.getLength() > 0)
            otherwise = (Element) otherwiseElements.item(0);
      }
      
      // add new global templates
      NodeList existingWhens = choose.getElementsByTagName("xsl:when");
      Iterator keys = m_GlobalTemplates.keySet().iterator();
      while (keys.hasNext())
      {
         String name = (String) keys.next();
         String test = "/*/sys_AssemblerInfo[@psxglobaltemplate='" + 
            name + "']";

         boolean found = false;
         for (int i=0; !found && i<existingWhens.getLength(); i++)
         {
            Element existingWhen = (Element) existingWhens.item(i);
            String testAttr = existingWhen.getAttribute("test");
            
            found = testAttr.equals(test);
         }
         
         if (!found)
         {
            logInfo("Adding new dispatch case for template: " + name);
            Element when = m_doc.createElement("xsl:when");
            when.setAttribute("test", test);
            if (otherwise == null)
               choose.appendChild(when);
            else
               choose.insertBefore(when, otherwise);
            
            Element callTemplate = m_doc.createElement("xsl:call-template");
            callTemplate.setAttribute("name", name + "_root");
            when.appendChild(callTemplate);
         }
      }

      // remove not existing global templates
      existingWhens = choose.getElementsByTagName("xsl:when");
      for (int i=existingWhens.getLength()-1; i>=0; i--)
      {
         Element existingWhen = (Element) existingWhens.item(i);
         
         boolean found = false;
         keys = m_GlobalTemplates.keySet().iterator();
         while (!found && keys.hasNext())
         {
            String name = (String) keys.next();
            String test = "/*/sys_AssemblerInfo[@psxglobaltemplate='" + 
               name + "']";
            found = test.equals(existingWhen.getAttribute("test"));
         }

         if (!found)
         {
            logInfo("Removing dispatch case for: " + 
               existingWhen.getAttribute("test"));
            choose.removeChild(existingWhen);
         }
      }
      
      logInfo("...Done updating global template dispatching template.");
   }

   /**
    * Save all template files associated with this object to its respective file
    * path.
    * 
    * @throws IOException in case of any error while saving the template files.
    */
   private void saveTemplateFiles() throws IOException
   {
      File folder = new File(ABS_GLOBAL_TEMPLATES_PATH);
      if (folder.exists())
      {
         emptyFolder(ABS_GLOBAL_TEMPLATES_PATH);
      }
      else
      {
         folder.getParentFile().mkdir();
      }
      logInfo("Saving all global template XSL files...");
      Iterator keys = m_GlobalTemplates.keySet().iterator();
      while (keys.hasNext())
      {
         String key = (String) keys.next();
         PSGlobalTemplate template =
            (PSGlobalTemplate) m_GlobalTemplates.get(key);
         template.save();
      }
   }

   /**
    * Empty the specified folder.
    * 
    * @param folderPath name of the folder to empty, assumed not
    *           <code>null</code> and an existing folder.
    */
   private void emptyFolder(String folderPath)
   {
      logInfo("Deleting old global template XSL files...");

      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.*");
      PSFileFilter filter =
         new PSFileFilter(
            PSFileFilter.IS_FILE | PSFileFilter.IS_INCLUDE_ALL_DIRECTORIES);
      filter.setNamePattern(pattern);
      PSFilteredFileList lister = new PSFilteredFileList(filter);
      List listFiles = lister.getFiles(folderPath);
      for (int i = listFiles.size() - 1; i >= 0; i--)
      {
         File file = (File) listFiles.get(i);
         if(file.isDirectory())
         {
            emptyFolder(file.getAbsolutePath());
         }
         if (file.delete())
         {
            logInfo("Deleted file: " + file.getName());
         }
         else
         {
            logWarning("Could not delete file: " + file.getName());
         }
      }
   }
   
   /**
    * Whether the system is using new (post 5.5) or old (5.5) global templating.
    * @return <code>true</code> if using new style <code>false</code> otherwise.
    */
   public boolean isUsingNewStyle()
   {
      return m_isNewStyle;
   }

   /**
    * Add &lt;xsl:import&gt; for each template and make sure no duplicate 
    * imports. 
    */
   private void cleanupImports()
   {
      logInfo("Cleaning <xsl:import>s...");
      //Collect all import elements
      NodeList nlImports = m_doc.getElementsByTagName("xsl:import");
      String fileUrlPath = "file:" + REL_GLOBAL_TEMPLATES_PATH + "/";
      for (int i = nlImports.getLength() - 1; i >= 0; i--)
      {
         Element importElem = (Element) nlImports.item(i);
         String name = importElem.getAttribute("href");
         if (name.startsWith(fileUrlPath))
         {
            importElem.getParentNode().removeChild(importElem);
         }
      }
      Iterator keys = m_GlobalTemplates.keySet().iterator();
      while (keys.hasNext())
      {
         String name = (String) keys.next();
         String href = fileUrlPath + name + ".xsl";
         Element elem = m_doc.createElement("xsl:import");
         elem.setAttribute("href", href);
         m_doc.getDocumentElement().insertBefore(
            elem,
            m_doc.getDocumentElement().getFirstChild());
      }
      logInfo("End cleaning <xsl:import>s...");
   }

   /**
    * Get file path of the global template style sheet being used.
    * 
    * @return file path of the style sheet that includes/imports the global
    *         templates relative to the server root. Never <code>null</code>
    *         or empty. This will be {@link PSRxGlobals#RX_GLOBALS_PATH}for old
    *         style templating and {@link PSRxGlobals#RX_GLOBAL_TEMPLATES_PATH}
    *         for new style templating.
    */
   public String getGlobalTemplateFilePath()
   {
      return m_globalTemplateFilePath;
   }

   /**
    * Map of all global templates to be attachd to this object. The key in 
    * the map is the name of the template and value is the 
    * {@link PSGlobalTemplate template}. Never <code>null</code>
    * @see PSRxGlobals#addGlobalTemplate(PSGlobalTemplate)
    */
   private Map m_GlobalTemplates = new HashMap();

   /**
    * A utility service to furnish root directory information.
    */
   private static String m_RootDir = (String) PSRhythmyxInfoLocator
         .getRhythmyxInfo().getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY)+"/";
   
   /**
    * String constant for the location of rx_Globals.xsl relative to the 
    * server root.
    */
   public static final String RXGLOBALS_PATH = 
      "rx_resources/stylesheets/assemblers";

   /**
    * String constant for the location of all global templates relative to 
    * the server root.
    */
   public static final String REL_GLOBAL_TEMPLATES_PATH = 
      "rx_resources/stylesheets/globaltemplates";
   
   /**
    * String constant for the absolute location of all global templates.
    */
   public static final String ABS_GLOBAL_TEMPLATES_PATH = m_RootDir +
      REL_GLOBAL_TEMPLATES_PATH;
   
   /**
    * Absolute path of the file rx_Globals.xsl.
    */
   public static final String RX_GLOBALS_PATH = m_RootDir + 
      RXGLOBALS_PATH + "/rx_Globals.xsl";

   /**
    * Full path of the file rx_GlobalTemplates.xsl relative to the server root.
    */
   public static final String RX_GLOBAL_TEMPLATES_PATH =
      RXGLOBALS_PATH + "/rx_GlobalTemplates.xsl";
   
   /**
    * The name used for the global template dispatching template.
    */
   public static final String DISPATCHING_TEMPLATE_NAME = 
      "psx-global-template-dispatcher";

   /**
    * Full path of the global template include style sheet relative to server
    * root. Initialized to {@link #RX_GLOBALS_PATH rx_Globals.xsl}.
    * Reinitialized in the {@link #PSRxGlobals(Logger) ctor}based on whether
    * the system is using new or old style of templating.
    * 
    * @see #getGlobalTemplateFilePath()
    *  
    */
   private String m_globalTemplateFilePath = RX_GLOBALS_PATH;
   
   /**
    * Flag to indicate if the system is using new (post 5.5) or old (5.5) style 
    * global templating. Initialized to <code>true</code>. Reinitialized in the 
    * ctor. Never changed after that.
    */
   private boolean m_isNewStyle = true;
   
   /**
    * XML document for the file {@link #RX_GLOBALS_PATH rx_Globals.xsl}or
    * {@link #RX_GLOBAL_TEMPLATES_PATH rx_GlobalTemplates.xsl}depending on the
    * whether the system is using new (post 5.5) or old style (5.5) of
    * templating. Initialized in the constructor and never <code>null</code>
    * after that.
    */
   private Document m_doc = null;
}
