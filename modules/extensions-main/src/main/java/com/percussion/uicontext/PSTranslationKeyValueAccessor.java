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
package com.percussion.uicontext;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.i18n.ui.PSI18NTranslationKeyValues;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.i18n.PSLocale;
import com.percussion.i18n.PSTmxUnit;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * See {@link #processResultDocument(Object[], IPSRequestContext, Document)
 *  processResultDocument(Object[], IPSRequestContext, Document)}
 */
public class PSTranslationKeyValueAccessor implements IPSResultDocumentProcessor
{
   /** @see IPSResultDocumentProcessor **/
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    *  Returns a XML document constrained to the dtd specified here:
    * {@link com.percussion.i18n.PSI18NTranslationKeyValues#toXml(Document doc)
    * PSI18NTranslationKeyValues.toXml(Document doc)} that is based on the
    * {@link com.percussion.util.IPSHtmlParameters#SYS_LANG
    * IPSHtmlParameters.SYS_LANG}
    *
    * @see IPSResultDocumentProcessor **/
   public Document processResultDocument(
     Object[] parameters, IPSRequestContext request, Document doc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // get the locale info from the request sys_lang:
      String theLang = (String)request.getSessionPrivateObject(
         IPSHtmlParameters.SYS_LANG);

      Object ps[] = request.getParameterList("sys_package");
      String packages[] = null;
      if (ps != null && ps.length >0)
      {
         packages = new String[ps.length];
         System.arraycopy(ps, 0, packages, 0, ps.length);
      }

      Map keyValueMap = getMapFromKeys(theLang, packages);
      if(keyValueMap == null)
      {
         Object[] errs = {getClass().getName(), theLang};

         throw new PSExtensionProcessingException(
            IPSExtensionErrors.CATALOG_EXT_RESOURCE_ERROR, errs);
      }

      PSI18NTranslationKeyValues keyVals =
         PSI18NTranslationKeyValues.getInstance();

      // populate it from the map;
      keyVals.fromMap(keyValueMap);

      Document keyValdoc = PSXmlDocumentBuilder.createXmlDocument();
      keyVals.toXml(keyValdoc);

      return keyValdoc;
   }

   /**
    * Creates a key value map of the language provided from the specified
    * language bundle or in the default language bundle, keys provided in the
    * iterator.
    * 
    * @param language the language key, assumed never <code>null</code> or
    *           empty
    * @param packages if this is non-<code>null</code> and has any values
    *           then the values are used to limit the returned map
    * @return may return <code>null</code> if default language or the passed
    *         language isn't supported.
    */
   private Map getMapFromKeys(String language, String[] packages)
   {
      // this calls a method that will already check the default.
      Iterator it = PSI18nUtils.getKeys(getActiveLocale(language));
      Map keyValueMap = null;

      if(it != null)
      {
         keyValueMap =  new HashMap();

         String theKey = "";
         while(it.hasNext())
         {
            theKey = (String)it.next();
            String value = PSI18nUtils.getString(theKey, language);
            String mnemonic = PSI18nUtils.getMnemonic(theKey, language);
            String tooltip = PSI18nUtils.getTooltip(theKey, language);
            PSTmxUnit unit = new PSTmxUnit(value, mnemonic, tooltip);
            
            boolean found = false;
            
            if (packages == null || packages.length == 0)
            {
               found = true;
            }
            else
            {
               for(String packagename : packages)
               {
                  if (theKey.startsWith(packagename))
                  {
                     found = true;
                     break;
                  }
               }
            }
            
            if (found)
            {
               keyValueMap.put(theKey, unit);
            }
         }
      }

      return keyValueMap;
   }

   /**
    * Helper method to return the active locale for the passed in
    * lang string. This is need for the case where only the lang part
    * of the locale is passed in. So for the case of "fr" the first
    * locale found in alpha order with the "fr" lang part will be returned.
    * @param lang the locale string full or just the lang part. Assumed
    * not <code>null</code>.
    * @return the active locale or the default lang if none found.
    */
   private String getActiveLocale(String lang)
   {
      IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
      List<String> langs = new ArrayList<>();
      List<PSLocale> locales = 
         mgr.findLocaleByStatus(PSLocale.STATUS_ACTIVE);
      for(PSLocale locale : locales)
      {
         langs.add(locale.getLanguageString());
      }
      Collections.sort(langs);
      for(String lg : langs)
      {
         if(lg.startsWith(lang))
            return lg;
      }
      return PSI18nUtils.DEFAULT_LANG;
   }

   /** @see IPSResultDocumentProcessor **/
   public void init(IPSExtensionDef parm1, File parm2)
      throws PSExtensionException
   {
   }
}
