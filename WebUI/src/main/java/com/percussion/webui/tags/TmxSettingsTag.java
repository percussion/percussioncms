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
package com.percussion.webui.tags;

import com.percussion.i18n.PSTmxResourceBundle;
import org.xml.sax.SAXException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author erikserating
 *
 */
public class TmxSettingsTag extends TagSupport
{

      
   public void setPrefixes(String prefixes)
   {
      m_prefixes = prefixes;   
   }
   
   public void setLang(String lang)
   {
      if(m_lang == null || m_lang.length() == 0)
         return;
      m_lang = lang;   
   }
   
   public void setDebug(String debug)
   {
      if(debug == null)
         debug = "false";
      m_debug = debug;
   }
   
   /* (non-Javadoc)
    * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
    */
   @Override
   public int doStartTag() throws JspException
   {      
      try
      {
         pageContext.setAttribute("debug",m_debug);
         pageContext.setAttribute("sys_lang", m_lang);
         loadTmx();
      }
      catch (Exception e)
      {
         throw new JspException(e);
      }      
      
      return SKIP_BODY;
   }
   
   /**
    * Loads the tmx keys for the specified lang and
    * prefixes if not yet cached.
    * @throws IOException
    * @throws ParserConfigurationException
    * @throws SAXException
    */
   private void loadTmx() throws IOException, ParserConfigurationException, SAXException
   {
      TmxCache cache = TmxCache.getInstance();
      String prefixStr = m_prefixes == null || m_prefixes.length() == 0
         ? "*"
         : m_prefixes;         
      if(cache.isIndexed(m_lang, prefixStr))
         return;
      
      String[] prefixes = prefixStr.split(",");
      
      PSTmxResourceBundle tmxBundle = PSTmxResourceBundle.getInstance();
      Iterator<?> keys = tmxBundle.getKeys(m_lang);
      
      while(keys.hasNext())
      {
          String key = (String)keys.next();
          if(!prefixStr.equals("*") && !accept(prefixes, key))
              continue;
          String val = tmxBundle.getString(key, m_lang).replaceAll("\"", "\\\"");
          cache.addEntry(m_lang, key, val);
      }


      cache.setIndexed(m_lang, prefixStr);
      
   }
   
   public boolean accept(String[] prefixes, String key)
   {
       for(int i = 0; i < prefixes.length; i++)
       {
           if(key.startsWith(prefixes[i]))
               return true;
       }
       return false;
   }
   
   private String m_prefixes;
   private String m_lang = DEFAULT_LANG;
   private String m_debug = "false";

   private static final String DEFAULT_LANG = "en-us";
   
   
}
