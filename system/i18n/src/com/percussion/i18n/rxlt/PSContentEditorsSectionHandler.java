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
package com.percussion.i18n.rxlt;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.i18n.tmxdom.IPSTmxTranslationUnit;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.utils.*;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class handles processing of generating the keys for the Content Editors
 * section of the Rhythmyx Content Manager. Content Editor section involves
 * processing of the ContentEditorSystemDef.xml, all Content Editor shared
 * definition files and Content Editor local definition files. The logic of
 * creating the keys from these three categories of content editors is built
 * into the XSL stylesheet {@link #CONTENTEDITORS_TRANSFORM_XSL}. All that this
 * class does is to parse the above categories of files and run this XSL
 * stylesheet to collect the keys and generate a combined and simple XML
 * document and then to generate the TMX document for this entire section.
 */

public class PSContentEditorsSectionHandler extends PSIdleDotter
   implements IPSSectionHandler
{
   /**
    * Constructor. Loads the XSL Stylesheet from the file which is part the JAR
    * file.
    * @throws PSSectionProcessingException if any error occurs parsing
    * contenteditortransform.xsl file
    */
   public PSContentEditorsSectionHandler()
      throws PSSectionProcessingException
   {

      try(InputStream is = getClass()
              .getResourceAsStream(CONTENTEDITORS_TRANSFORM_XSL)) {
         ms_XslDoc = PSXmlDocumentBuilder.createXmlDocument(is, false);
      }

      catch(Exception e) //IOException, SAXException
      {
         throw new PSSectionProcessingException(e.getMessage());
      }
   }

   /*
    * Implementation of the method defined in the interface.
    * See {@link IPSSectionHandler#process(Element)} for
    * details about this method.
    */
   public IPSTmxDocument process(Element cfgData)
      throws PSSectionProcessingException
   {
      if(cfgData == null)
         throw new IllegalArgumentException("cfgData element must not be null");

      String rxroot = cfgData.getOwnerDocument().getDocumentElement()
            .getAttribute(PSRxltConfigUtils.ATTR_RXROOT);
      ms_SectionName = cfgData.getAttribute(PSRxltConfigUtils.ATTR_NAME);

      PSCommandLineProcessor.logMessage("blankLine", "");
      PSCommandLineProcessor.logMessage("processingSection", ms_SectionName);
      PSCommandLineProcessor.logMessage("blankLine", "");

      PSTmxDocument tmxDoc = null;
      try
      {
         //Create an empty DOM document to store all the keys
         Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(dataDoc, "root");
         handleSystemDef(rxroot, dataDoc);
         handleSharedDef(rxroot, dataDoc);
         handleLocalDef(rxroot, dataDoc);
         //Now the dataDoc has all the keys from all content editors.
         //Create an empty TMX Document
         tmxDoc = new PSTmxDocument();
         NodeList nl = dataDoc.getElementsByTagName("key");
         Node node = null;
         Element key = null;
         String name, desc;
         IPSTmxTranslationUnit tu = null;
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            key = (Element)nl.item(i);
            name = key.getAttribute(PSRxltConfigUtils.ATTR_NAME);
            node = key.getFirstChild();
            desc = "";
            if(node instanceof Text)
               desc = ((Text)node).getData();
            tu = tmxDoc.createTranslationUnit(name, desc);
            tu.addProperty( tmxDoc.createProperty(
                  IPSTmxDtdConstants.ATTR_VAL_SECTIONNAME,
                  PSI18nUtils.DEFAULT_LANG, ms_SectionName));
            tmxDoc.merge(tu);
         }
      }
      catch(Exception e)
      {
         throw new PSSectionProcessingException(e.getMessage());
      }
      finally
      {
         endDotSession();
      }
      return tmxDoc;
   }

   /**
    * Helper method to gather and add all keys from the Content Editor System
    * Definition file.
    * @param rxroot must not be <code>null</code> may be <code>empty</code>
    * @param keysDoc must not be <code>null</code>.
    * @throws IOException if any error occurs reading ContentEditorSystemDef.xml
    * @throws FileNotFoundException if ContentEditorSystemDef.xml is not found
    */
   private void handleSystemDef(String rxroot, Document keysDoc)
      throws IOException, FileNotFoundException
   {
      File file = new File(rxroot, SYSTEMDEF_FILE);
      PSCommandLineProcessor.logMessage("processingFile",
         file.getCanonicalPath());
      try
      {
         Document doc =
            PSXmlDocumentBuilder.createXmlDocument(new FileReader(file), false);
         doc = getKeys(doc);
         PSXmlDocumentBuilder.copyTree(keysDoc, keysDoc.getDocumentElement(),
            doc.getDocumentElement(), false);
      }
      catch(Exception e)
      {
         PSCommandLineProcessor.logMessage("warningMessageException",
            e.getMessage());
      }
   }

   /**
    * Helper method to gather and add all keys from the Content Editor Shared
    * Definition files.
    * @param rxroot must not be <code>null</code> may be empty <code>empty</code>
    * @param keysDoc must not be <code>null</code>.
    * @throws IOException if any error occurs reading shared definition files
    * @throws FileNotFoundException if any of the shared definition files could
    * not be found.
    */
   private void handleSharedDef(String rxroot, Document keysDoc)
      throws IOException, FileNotFoundException
   {
      PSCommandLineProcessor.logMessage("retrievingSharedDefFiles", "");
      //Show idle dots
      showDots(true);
      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.xml");
      PSFileFilter filter = new PSFileFilter(PSFileFilter.IS_FILE);
      filter.setNamePattern(pattern);
      PSFilteredFileList  lister = new PSFilteredFileList(filter);
      //get list of all XML files from this folder
      List listFiles = lister.getFiles(new File(rxroot, SHAREDDEF_DIR));
      //Stop showing idle dots
      showDots(false);
      Document doc = null;
      File file = null;
      for(int i=0; listFiles!=null && i<listFiles.size(); i++)
      {
         file = (File)listFiles.get(i);
         PSCommandLineProcessor.logMessage("processingFile",
            file.getCanonicalPath());
         try
         {
            doc =
               PSXmlDocumentBuilder.createXmlDocument(new FileReader(file), false);
            doc = getKeys(doc);
            PSXmlDocumentBuilder.copyTree(keysDoc, keysDoc.getDocumentElement(),
               doc.getDocumentElement(), false);
         }
         catch(Exception e)
         {
            PSCommandLineProcessor.logMessage("warningMessageException",
               e.getMessage());
            continue;
         }
      }
   }

   /**
    * Helper method to gather and add all keys from the Content Editor Local
    * Definition files.
    * @param rxroot must not be <code>null</code> may be <code>empty</code>
    * @param keysDoc must not be <code>null</code>.
    * @throws IOException if any error occurs reading local definition files
    * @throws FileNotFoundException if any of the local definition files could
    * not be found.
    */
   /*
    * TODO: Getting the list of content editors this way is not the best since we
    * need to parse each file to know if it is a content editor. A better way
    * would be to query the CONTENTTYPES table for the registered content
    * editors and locate the files.
    */
   private void handleLocalDef(String rxroot, Document keysDoc)
      throws IOException, FileNotFoundException
   {
      PSCommandLineProcessor.logMessage("retrievingLocalDefFiles", "");
      //Show idle dots
      showDots(true);
      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.xml");
      PSFileFilter filter = new PSFileFilter(PSFileFilter.IS_FILE);
      filter.setNamePattern(pattern);
      PSFilteredFileList  lister = new PSFilteredFileList(filter);
      //get list of all XML files from this folder
      List listFiles = lister.getFiles(new File(rxroot, LOCALDEF_DIR));
      //Stop showing idle dots
      showDots(false);

      Document doc = null;
      File file = null;
      for(int i=0; listFiles!=null && i<listFiles.size(); i++)
      {
         file = (File)listFiles.get(i);
         PSCommandLineProcessor.logMessage("processingFile",
            file.getCanonicalPath());
         try
         {
            doc =
               PSXmlDocumentBuilder.createXmlDocument(new FileReader(file), false);
            NodeList nl = doc.getElementsByTagName("PSXContentEditor");
            if(nl==null || nl.getLength() < 1)
            {
               PSCommandLineProcessor.logMessage("notContentEditorSkipping", "");
               continue;
            }
            doc = getKeys(doc);
            PSXmlDocumentBuilder.copyTree(keysDoc, keysDoc.getDocumentElement(),
               doc.getDocumentElement(), false);
         }
         catch(Exception e)
         {
            PSCommandLineProcessor.logMessage("warningMessageException",
               e.getMessage());
            continue;
         }
      }
   }

   /**
    * Helper method to apply the stylesheet to the content editor XML document
    * (system, shared or local) to generate a new XML document with all keys.
    * @param doc must not be <code>null</code>.
    * @return XML document with generated keys.
    * @throws TransformerException if XSL processing to get the XML document
    * containing the kesy fails for any reason
    * @throws SAXException if result XML document cannot be created for any
    * reason

    */
   private Document getKeys(Document doc)
      throws TransformerException, SAXException
   {
      return PSCmsTablesSectionHandler.transformXML(doc, ms_XslDoc);
   }

   /**
    * Default name of section that is implemented by this class. Overridden
    * during processing by the name specified in the config element.
    * @see #process
    */
   private static String ms_SectionName = "Content Editors";

   /**
    * DOM Document for the XSL stylesheet that is run to produce required keys
    * from the Content Editor system, shared and local definition files. Never
    * <code>null</code> after this class object is initialized.
    */
   private static Document ms_XslDoc = null;

   /**
    * String constant defining the location of Content Editor System Definition
    * file relative to the Rhythmyx root directory.
    */
   private static final String SYSTEMDEF_FILE =
      "rxconfig" + File.separator + "Server" + File.separator +
      "ContentEditors" + File.separator + "ContentEditorSystemDef.xml";

   /**
    * String constant defining the location of Content Editor Shared Definition
    * files relative to the Rhythmyx root directory.
    */
   private static final String SHAREDDEF_DIR =
      "rxconfig" + File.separator + "Server" + File.separator +
      "ContentEditors" + File.separator + "shared";

   /**
    * String constant defining the location of Content Editor Local Definition
    * files relative to the Rhythmyx root directory.
    */
   private static final String LOCALDEF_DIR = "ObjectStore";

   /**
    * Name of the XSL stylesheet file that will be applied to any content editor
    * definition document to generate the keys. Entire logic and generation
    * scheme is embedded in this file.
    */
   private static final String CONTENTEDITORS_TRANSFORM_XSL =
      "contenteditortransform.xsl";
}
