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
package com.percussion.search.lucene.analyzer;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.i18n.PSLocale;
import com.percussion.i18n.PSLocaleException;
import com.percussion.i18n.PSLocaleManager;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a factory class to provide the analyzer for the given display
 * language of the item's locale. It is implemented as a singleton pattern and
 * users should call {@link PSLuceneAnalyzerFactory#getInstance()} for the instances of this class.
 */
public class PSLuceneAnalyzerFactory
{
   /**
    * Private ctor as it is a singleton class.
    */
   private PSLuceneAnalyzerFactory()
   {
      init();
   }

   /**
    * This class uses a singleton pattern and call this method to get the
    * instance of this class.
    * 
    * @return The one and only instance of this class.
    */
   public synchronized static PSLuceneAnalyzerFactory getInstance()
   {
      if (ms_instance == null)
      {
         ms_instance = new PSLuceneAnalyzerFactory();
      }
      return ms_instance;
   }

   /**
    * Initializes the analyzers registered in the search config. Loops through
    * all the system locales and logs info if an analyzer is not found for any
    * locale.
    */
   private void init()
   {
      PSSearchConfig searchConfig = PSServer.getServerConfiguration()
            .getSearchConfig();
      Map<String, PSExtensionCall> extensionCallMap = searchConfig.getAnalyzers();
      Iterator<String> extensionIterator = extensionCallMap.keySet().iterator();
      IPSExtensionManager manager = PSServer.getExtensionManager(null);
      while (extensionIterator.hasNext())
      {
         String language = extensionIterator.next();
         PSExtensionCall extensionCall = extensionCallMap.get(language);
         try
         {
            if(!isValidLangString(language))
            {
               //UI does not allow this to happen, but in case if happens
               //write error to the log so that user can fix it.
               String msg = "Registered locale "
                     + language
                     + " is not currently supported by the CMS";
               log.error(msg);
            }
            IPSExtension ext = manager.prepareExtension(extensionCall
                  .getExtensionRef(), null);
            if (ext instanceof IPSLuceneAnalyzer)
            {
               IPSLuceneAnalyzer analyzer = (IPSLuceneAnalyzer)ext;
               m_analyzers.put(language,
                     analyzer.getAnalyzer(language));
            }
            else
            {
               //UI does not allow this to happen, but incase if happens
               //write error to the log so that user corrects it.
               String msg = "Registered extension "
                     + extensionCall.getName()
                     + " with locale "
                     + language
                     + " for lucene text analysis is not an instance of " +
                           "IPSLuceneAnalyzer";
               log.error(msg);
            }
         }
         catch (PSNotFoundException | PSExtensionException e)
         {
            String msg = "Error loading registered extension "
                  + extensionCall.getName()
                  + " with locale "
                  + language
                  + " for lucene text analysis";
            log.error(msg,e);
         } catch (PSExtensionProcessingException e)
         {
            String msg = "Failed to get the Analyzer object for registered " +
                  "extension "
                  + extensionCall.getName()
                  + " with locale "
                  + language
                  + " for lucene text analysis";
            log.error(msg,e);
         }
         catch (PSLocaleException e)
         {
            log
            .error(
                  "Failed to get the instance of locale manager skipping " +
                  "analyzer check for system locales.",
                  e);
         }
      }

      // loops through all locales to make sure the registered locales have
      //a registered analyzer or system analyzer.
      try
      {
         PSLocaleManager mgr = PSLocaleManager.getInstance();
         Iterator<PSLocale> iter = mgr.getLocales();
         while(iter.hasNext())
         {
            PSLocale loc = iter.next();
            String ls = loc.getLanguageString();
            if(m_analyzers.get(ls) != null)
               continue;
            Analyzer an = getDefaultAnalyzer(ls);
            if(an instanceof WhitespaceAnalyzer)
            {
               String msg = "Could not find a user registered or system " +
                     "analyzer for locale \""
                     + ls
                     + "\". WhiteSpaceAnalyzer will be used for indexing and " +
                           "searching content.";
               log.info(msg);
            }
         }
      }
      catch (PSLocaleException e)
      {
         log
               .info(
                     "Failed to get the instance of locale manager skipping " +
                     "analyzer check for system locales.",
                     e);
      }

   }

   /**
    * Checks whether a locale is registered with the supplied language string or
    * not.
    * 
    * @param langstr in the form of two letter language code hyphen two
    * letter country code, that needs to be validated assumed not <code>null</code>.
    * @return returns <code>true</code> if the supplied language string
    * corresponds to a registered locale other wise <code>false</code>.
    * @throws PSLocaleException in case of locale cataloging error.
    */
   private boolean isValidLangString(String langstr) throws PSLocaleException
   {
      PSLocaleManager mgr = PSLocaleManager.getInstance();
      Iterator<PSLocale> iter = mgr.getLocales();
      while(iter.hasNext())
      {
         PSLocale loc = iter.next();
         String ls = loc.getLanguageString();
         if(ls.equalsIgnoreCase(langstr))
            return true;
      }
      return false;
   }

   /**
    * Gets the default analyzer for the supplied language string. Checks for the
    * supported analyzer in snowball analyzer and then checks in CJK analyzer
    * list. If not found returns white space analyzer as default.
    * 
    * @param languageString in the form of two letter language code hyphen two
    * letter country code assumed not <code>null</code>.
    * @return lucene Analyzer for the given locale never <code>null</code>.
    */
   private Analyzer getDefaultAnalyzer(String languageString)
   {
      Analyzer al = null;

      PSLocaleSpecificLuceneAnalyzer defaultAnalyzers = PSLocaleSpecificLuceneAnalyzer.getInstance();

      try {
         al = defaultAnalyzers.getAnalyzer(languageString);
      } catch (PSExtensionProcessingException e) {
         log.warn("Unable to detect Analyzer for Locale: {} , defaulting to Whitespace Analyzer.", languageString);
      }

      if(al == null)
         al = new WhitespaceAnalyzer();

      return al;
   }

   /**
    * Returns the analyzer for the given locale. The analyzers for the locales
    * are initialized in the constructor. If not found any, returns
    * org.apache.lucene.analysis.WhitespaceAnalyzer.
    * 
    * @param languageString in the form of two letter language code hyphen two
    * letter country code for which the analyzer needs to be
    *           returned.
    * @return Instance of org.apache.lucene.analysis.Analyzer object never
    *         <code>null</code>.
    */
   public Analyzer getAnalyzer(String languageString)
   {
      Analyzer al = m_analyzers.get(languageString);
      if (al == null)
         al = getDefaultAnalyzer(languageString);
      return al;
   }

   /**
    * It is a map of language string and the corresponding Analyzer. Filled in
    * constructor.
    */
   private Map<String, Analyzer> m_analyzers = new HashMap<>();


   /**
    * The static instance of this class. Created in {@link PSLuceneAnalyzerFactory#getInstance()} }
    * method. Never <code>null</code> after that.
    */
   private static PSLuceneAnalyzerFactory ms_instance = null;
   
   /**
    * Static instance of logger for this class.
    */
   private static final Logger log = LogManager.getLogger(IPSConstants.SEARCH_LOG);
}
