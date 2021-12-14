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
package com.percussion.i18n.rxlt;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.i18n.tmxdom.IPSTmxTranslationUnit;
import com.percussion.i18n.tmxdom.IPSTmxTranslationUnitVariant;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.util.PSFileFilter;
import com.percussion.util.PSFilteredFileList;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * This class handles generation of translation unit keys from all XSL
 * stylesheet files under Rhythmyx server. This assumes the following scheme in
 * looking up for the keys:
 * <p>
 * Any stylesheet that requires static labels localized will include the
 * definitions as
 * &lt;psxi18n:lookupkeys&gt;
 * &lt;key name="keyName"&gt;keyDescription&lt;/key&gt;
 * ...
 * &lt;/psxi18n:lookupkeys&gt;
 * &lt;/p&gt;
 *
 */
public class PSXslStylesheetsSectionHandler extends PSIdleDotter
   implements IPSSectionHandler
{
   /*
    * Implementation of the interface defined in the interface.
    * See {@link IPSSectionHandler#process(Element)} for
    * details about this method.
    */
   public IPSTmxDocument process(Element cfgData)
      throws PSActionProcessingException
   {
      if(cfgData == null)
      {
         throw new IllegalArgumentException("cfgdata must not be null");
      }
      PSTmxDocument tmxDoc = null;
      try
      {
         String rxroot = cfgData.getOwnerDocument().getDocumentElement().
            getAttribute(PSRxltConfigUtils.ATTR_RXROOT);
         ms_SectionName = cfgData.getAttribute(PSRxltConfigUtils.ATTR_NAME);

         PSCommandLineProcessor.logMessage("blankLine", "");
         PSCommandLineProcessor.logMessage("processingSection", ms_SectionName);
         PSCommandLineProcessor.logMessage("blankLine", "");

         Document dataDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(dataDoc, "root");
         handleAllFiles(rxroot, dataDoc);
         tmxDoc = new PSTmxDocument();
         NodeList nl = dataDoc.getElementsByTagName("key");
         Node node = null;
         Element key = null;
         String name, desc, mnemonic, tooltip;
         IPSTmxTranslationUnit tu = null;
         for(int i=0; nl!=null && i<nl.getLength(); i++)
         {
            key = (Element)nl.item(i);
            name = key.getAttribute(PSRxltConfigUtils.ATTR_NAME);
            mnemonic = key.getAttribute(PSRxltConfigUtils.ATTR_MNEMONIC);
            tooltip = key.getAttribute(PSRxltConfigUtils.ATTR_TOOLTIP);
            node = key.getFirstChild();
            desc = "";
            if(node instanceof Text)
               desc = ((Text)node).getData();
            tu = tmxDoc.createTranslationUnit(name, desc);
            tu.addProperty( tmxDoc.createProperty(
                  IPSTmxDtdConstants.ATTR_VAL_SECTIONNAME,
                  PSI18nUtils.DEFAULT_LANG, ms_SectionName));
            if ((mnemonic != null && mnemonic.trim().length() > 0) ||
                (tooltip != null && tooltip.trim().length() > 0))
            {
               IPSTmxTranslationUnitVariant variant = 
                  tu.getTransUnitVariant("en-us");
               if (variant != null)
               {
                  if (mnemonic != null && mnemonic.trim().length() > 0)
                  {
                     variant.addProperty(tmxDoc.createProperty("mnemonic",
                           "en-us", mnemonic));
                  }
                  if (tooltip != null && tooltip.trim().length() > 0)
                  {
                     variant.addProperty(tmxDoc.createProperty("tooltip",
                           "en-us", tooltip));
                  }
               }
            }
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
    * Helper method that actually builds the DOM document containing all keys.
    * @param rxroot must not be <code>null</code>.
    * @param keysDoc DOM document to append all keys, must not be
    * <code>null</code>.
    * @throws IOException
    */
   private void handleAllFiles(String rxroot, Document keysDoc)
      throws IOException
   {
      PSCommandLineProcessor.logMessage("retrievingXSLFiles", rxroot);
      //start displaying idle dots
      showDots(true);
      PSPatternMatcher pattern = new PSPatternMatcher('?', '*', "*.xsl");
      PSFileFilter filter = new PSFileFilter(
         PSFileFilter.IS_FILE|PSFileFilter.IS_INCLUDE_ALL_DIRECTORIES);
      filter.setNamePattern(pattern);
      PSFilteredFileList  lister = new PSFilteredFileList(filter);
      List listFiles = lister.getFiles(rxroot);
      //stop displaying idle dots
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
         }
         catch(Exception e) //catch any exception to report and continue
         {
            PSCommandLineProcessor.logMessage("warningMessageException",
               e.getMessage());
            continue;
         }
         NodeList nl = doc.getElementsByTagName("psxi18n:lookupkeys");
         if(nl==null || nl.getLength() < 1)
         {
            continue;
         }
         PSXmlDocumentBuilder.copyTree(keysDoc, keysDoc.getDocumentElement(),
            doc.getDocumentElement(), false);
      }
   }

   /**
    * Default name of section that is implemented by this class. Overridden
    * during processing by the name specified in the config element.
    * @see #process
    */
   protected static String ms_SectionName = "XSL Stylesheets";

   /**
    * Starting directory to search for all XSL stylesheet files relative to
    * Rhythmyx root. All subdirectories will recursed. We start from Rhthmyx
    * root.
    */
   public static final String XSLFILES_ROOT_DIR = "";
}
