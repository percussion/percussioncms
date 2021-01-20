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
package com.percussion.uicontext;

import com.percussion.cx.PSOptionManagerConstants;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.IPSXmlErrors;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class works directly with ContentExplorer PSOptionManager, this
 * loads and persists Option information to the session object.  The option
 * information object will be a <code>Document</code> but will be stored as
 * a <code>String</code> in the session.  This way if there are any parsing
 * problems they will be caught before adding it to the session, so on the
 * way out there won't be any problems and we won't be storing bad data.
 */
public class PSGetAndSetCxOptions implements IPSResultDocumentProcessor,
   IPSRequestPreProcessor
{
   /** @see IPSResultDocumentProcessor **/
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * This will store the {@link PSOptionManagerConstants#SESSIONOBJECT_CXOPTIONS
    * PSOptionManager.SESSIONOBJECT_CXOPTIONS} as the
    * value in the user session if the
    * {@link PSOptionManager#LOAD_SAVE_COMMAND_KEY
    * PSOptionManager.LOAD_SAVE_COMMAND_KEY}
    * has as its value: {@link PSOptionManagerConstants#SAVE_COMMAND
    * PSOptionManager.SAVE_COMMAND}.
    *
    * @see IPSRequestPreProcessor.preProcessRequest(Object, IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      // check for both params, if either null, leave:
      String thePut = request.getParameter(
         IPSHtmlParameters.SYS_COMMAND);
      String docValue =
         request.getParameter(PSOptionManagerConstants.SESSIONOBJECT_CXOPTIONS);

      // is there a "put" as the sys_command and is there a cxoption value?
      if(thePut != null && thePut.equals(PSOptionManagerConstants.SAVE_COMMAND) &&
         docValue != null)
      {
         request.setSessionPrivateObject(createSessionKey(request),docValue);
      }
   }

   /**
    * Creates a Document from a string representation.  This is called when
    * setting the session value to a doc.
    *
    * @param theDocString assumed not <code>null</code>.
    * @return Document
    * @throws PSExtensionProcessingException is there is a problem
    * parsing the document
    */
   private Document createDocFromString(String theDocString)
      throws PSExtensionProcessingException
   {
      Document doc = null;

      try
      {
         InputSource inSource = new InputSource(new StringReader(theDocString));

         doc = PSXmlDocumentBuilder.createXmlDocument(inSource, false);
      }
      catch(IOException e)
      {
         throw new PSExtensionProcessingException(
            IPSXmlErrors.XML_PROCESSING_ERROR);
      }
      catch(SAXException e)
      {
         throw new PSExtensionProcessingException(
            IPSXmlErrors.XML_PROCESSING_ERROR);
      }
      return doc;
   }

   /**
    * This will return the options document from the session if the
    * {@link PSOptionManager#LOAD_SAVE_COMMAND_KEY
    * PSOptionManager.LOAD_SAVE_COMMAND_KEY}
    * has as its value: {@link PSOptionManagerConstants#LOAD_COMMAND
    * PSOptionManager.LOAD_COMMAND} if there is one otherwise if those same
    * conditions are matched this will return the default options document
    * from the system.
    *
    * @return will not be <code>null</code>.
    * @see IPSResultDocumentProcessor
    */
   public Document processResultDocument(
      Object[] parameters, IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // check for get, if null, leave:
      String theGet = request.getParameter(IPSHtmlParameters.SYS_COMMAND);

      Document docValue = null;

      // if the get is valid, return the options doc:
      if(theGet != null && theGet.equals(PSOptionManagerConstants.LOAD_COMMAND))
      {
         // get the doc from the session:
         String docString = (String)request.getSessionPrivateObject(
            createSessionKey(request));

         if(docString != null )
            // parse docvalue and return the document:
            docValue = createDocFromString(docString);

         // if doc from the session is null, return deafult:
         if(docValue == null)
         {
            // we want to get the locale:
            docValue = getOptionDoc(request);
         }
      }
      // if its an explicit call for the default return it:
      else if(theGet != null && theGet.equals(PSOptionManagerConstants.LOAD_DEFAULT))
      {
            docValue = getOptionDoc(request);
      }
      else
         docValue = doc;

      return docValue;
    }

   /**
    * Initializes the default options document path.
    * @see IPSResultDocumentProcessor
    */
   public void init(IPSExtensionDef parm1, File parm2)
      throws PSExtensionException
   {
      m_defaultLocalOptionMap = new HashMap();


      StringBuffer sb = new StringBuffer();
      sb.append("rx_resources");
      sb.append(File.separator);
      sb.append("css");

      m_defaultOptionPathBase = sb.toString();
   }

   /**
    * Gets the proper option doc based on the locale specified in the request.
    *
    * @param request a valid requesta assumed not <code>null</code>.
    * @return the loaded option doc, may be <code>null</code> if the doc can't
    * be found.
    * @throws PSExtensionException will be thrown if there is a problem
    * loading the doc.
    */
   private Document getOptionDoc(IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      Document doc = null;

      // get the locale info from the request sys_lang:
      String theLang = (String)request.getSessionPrivateObject(
         IPSHtmlParameters.SYS_LANG);

      // verify that it is a valid locale:
      Locale tempLocale = PSI18nUtils.getLocaleFromString(theLang);

      // if null use default:
      if(tempLocale == null)
         theLang = PSI18nUtils.DEFAULT_LANG;

      // is it in the map:
      if(m_defaultLocalOptionMap.containsKey(theLang))
      {
         doc = (Document)m_defaultLocalOptionMap.get(theLang);
      }
      else
      {
         doc = loadOptionDoc(theLang);
      }

      return doc;

   }

   /**
    * This creates the key that corresponds to the option doc in the
    * session object.  The locale is used in the key so that the user may have
    * locale based option settings.
    *
    * @param request assumed not <code>null</code>
    * @return the key whose value will be the option doc.
    */
   private String createSessionKey(IPSRequestContext request)
   {
      // get the locale info from the request sys_lang:
      String theLang = (String)request.getSessionPrivateObject(
         IPSHtmlParameters.SYS_LANG);

      return theLang + "_" + PSOptionManagerConstants.SESSIONOBJECT_CXOPTIONS;
   }

   /**
    * Initializes the default options document.  This method
    * caches the loaded doc as value for the supplied locale as key.
    */
   private Document loadOptionDoc(String localeKey)
      throws PSExtensionProcessingException
   {
      FileInputStream inStream = null;
      Document doc = null;
      try
      {
         IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
         String rxRootDir = (String) rxInfo
               .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);

         String fullPath = rxRootDir + File.separator + m_defaultOptionPathBase
               + File.separator + localeKey + File.separator
               + USER_OPTION_FILE_NAME;

         File file = new File(fullPath);
         inStream = new FileInputStream(file);

         doc = PSXmlDocumentBuilder.createXmlDocument(
            new InputSource(inStream), false);

         addDocToMap(localeKey, doc);

      }
      catch (FileNotFoundException e)
      {
         // this message constant doesn't look right, however,
         //it does display the right message:
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.CATALOG_EXT_RESOURCE_ERROR,
            e.getLocalizedMessage());
      }
      catch (SAXException e)
      {
         throw new PSExtensionProcessingException(
            IPSXmlErrors.RAW_XML_DUMP, e.getLocalizedMessage());
      }
      catch (IOException e)
      {
         throw new PSExtensionProcessingException(
            IPSExtensionErrors.CATALOG_EXT_RESOURCE_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         try
         {
            if(inStream != null)
            {
               inStream.close();
            }
         }
         catch (IOException e)
         {
            // ignore
         }
      }
      return doc;
   }

   /**
    * Adds the arguments to the local option map.
    *
    * @param localeKey must not be <code>null</code> or empty.
    * @param optionDoc will be the value, must not be <code>null</code>.
    */
   private void addDocToMap(String localeKey, Document optionDoc)
   {
      if(localeKey == null || localeKey.trim().length() == 0)
         throw new IllegalArgumentException(
            "localeKey must not be null or empty");

      if(optionDoc == null)
         throw new IllegalArgumentException("optionDoc must not be null");

      m_defaultLocalOptionMap.put(localeKey, optionDoc);
   }

   /**
    * This <code>Map</code> contains all of the <code>Locale</code> specific
    * option documents that have been requested.  This is initialized in the
    * <code>init()</code> and documents are added as the value with the string
    * representation of the <code>Locale</code> as the key the first time a
    * request occurs for a <code>Locale</code> and that <code>Locale</code>
    * is not in this <code>Map</code>.  Once added, the <code>Locale</code>
    * and document are then held for the lifetime of this object.
    */
   private Map m_defaultLocalOptionMap = null;

   /**
    * This is base path to the default option file starting below the
    * PSServer.RequestRoot() and going until the local directory example:
    * <code>/Rhythmyx/rx_resources/css</code>.  This is initialized in the
    * <code>init()</code>, is never <code>null</code> and invariant.
    */
   private String m_defaultOptionPathBase = "";

   /**
    * The filename that all of the user options must have.
    */
   private final static String USER_OPTION_FILE_NAME = "UserOptions.xml";

}