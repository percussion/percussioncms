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
package com.percussion.search.lucene.textconverter;

import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.search.lucene.IPSLuceneConstants;
import com.percussion.server.PSServer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is a factory class designed to get the {@link IPSLuceneTextConverter}
 * objects for the given mimetype. This class implements singleton pattern and
 * users of this class need to call {@link getInstance()} method get the
 * instance.
 */
public class PSLuceneTextConverterFactory
{
   /**
    * private constructor as it implements singleton pattern.
    */
   private PSLuceneTextConverterFactory()
   {
      // Call init method to initialize
      init();
   }

   /**
    * This class uses a singleton pattern and call this method to get the
    * instance of this class.
    * 
    * @return The one and only object of this class, <code>never</code> null.
    */
   public synchronized static PSLuceneTextConverterFactory getInstance()
   {
      if (ms_instance == null)
      {
         ms_instance = new PSLuceneTextConverterFactory();
      }
      return ms_instance;
   }

   /**
    * Initializes the converters map. Gets the converters registered in search
    * configuration and adds them to the m_converters map and then adds all
    * system converters to it. If the mimetype of the registered converter
    * matches with the system converter then the system converter is not added
    * to the map.
    * 
    */
   private void init()
   {
      PSSearchConfig sconf = PSServer.getServerConfiguration()
            .getSearchConfig();
      Map<String, PSExtensionCall> extset = sconf.getTextConverters();
      Iterator<String> extiter = extset.keySet().iterator();
      IPSExtensionManager extmgr = PSServer.getExtensionManager(null);
      while (extiter.hasNext())
      {
         String mtype = extiter.next();
         PSExtensionCall extcall = extset.get(mtype);
         try
         {
            IPSExtension ext = extmgr.prepareExtension(extcall
                  .getExtensionRef(), null);
            if (ext instanceof IPSLuceneTextConverter)
            {
               m_converters.put(StringUtils.lowerCase(mtype),
                     (IPSLuceneTextConverter) ext);
            }
            else
            {
               //UI does not allow this to happen, but incase if happens
               //write error to the log so that user corrects it.
               String msg = "Registered extension " + extcall.getName()
                     + " with mimetype " + mtype
                     + " for text conversion is not an instance of "
                     + "IPSLuceneTextConverter";
               log.error(msg);
            }
         }
         catch (PSNotFoundException e)
         {
            String msg = "Error loading registered extension "
                  + extcall.getName()
                  + " with mimetype "
                  + mtype
                  + " for text conversion";
            log.error(msg,e);
         }
         catch (PSExtensionException e)
         {
            String msg = "Error loading registered extension "
                  + extcall.getName()
                  + " with mimetype "
                  + mtype
                  + " for text conversion";
            log.error(msg,e);
         }
      }
      addSystemConverters();
   }

   /**
    * Adds the system converters to the converters map. Adds only when converter
    * is not registerd for the mimetype.
    */
   private void addSystemConverters()
   {
      buildSystemConverterMap();
      Iterator<String> iter = m_systemConverters.keySet().iterator();
      while (iter.hasNext())
      {
         String mtype = StringUtils.lowerCase(iter.next());
         if (!m_converters.containsKey(mtype))
         {
            m_converters.put(mtype,m_systemConverters.get(mtype));
         }
      }
   }
   
   /**
    * Creates the instances of system converters and adds them to the
    * m_systemConverters map. This map only contains those mimetypes not well
    * supported by the PSTikaTextConvertor, all other mimetypes are delegated to
    * tika. Note that the PSTikaTextConvertor now filters out unsupported
    * mimetypes.  
    */
   private void buildSystemConverterMap()
   {

      m_systemConverters.put(IPSLuceneConstants.MIME_TYPE_TEXT_BY_HTML,
            new PSTextConverterHtml());
   }

   /**
    * Returns the lucene text converter for the supplied mimetype. May be
    * <code>null</code>, if there is no converter assigned to the mimetype.
    * 
    * @param mimetype The mimetype for which the converter needs to be returned.
    * If <code>null</code> or empty returns <code>null</code>.
    * @return An object of IPSLuceneTextConverter if found for the supplied
    * mimetype or <code>null</code>.
    */
   public IPSLuceneTextConverter getLuceneTextConverter(String mimetype)
   {
      IPSLuceneTextConverter convertor = m_converters.get(StringUtils.lowerCase(mimetype));
      return (convertor==null) ? new PSTikaTextConvertor() :convertor;
   }
   
   /**
    * Returns all the lucene text converters. It is a map of mimetype and
    * associated text converter.
    * 
    * @return A Map of String and IPSLuceneTextConverter never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public Map<String, IPSLuceneTextConverter> getAllLuceneTextConverters()
   {
      return MapUtils.unmodifiableMap(m_converters);
   }
   
   /**
    * It is a map of lowercase mimetype and the corresponding text converter.
    */
   private Map<String, IPSLuceneTextConverter> m_converters = 
      new HashMap<>();

   /**
    * The one and only instance of this class. Initialized by {@link 
    * #getInstance()}, then never <code>null</code>.
    */
   private static PSLuceneTextConverterFactory ms_instance;

   /**
    * Map of lowercase mimetype and system converters, the converters are
    * created and added during the creation of this class.
    */
   private Map<String,IPSLuceneTextConverter> m_systemConverters = 
      new HashMap<>();
   
   /**
    * Reference to log for this class
    */
   private final static Logger log = LogManager
         .getLogger(IPSConstants.SEARCH_LOG);
}
