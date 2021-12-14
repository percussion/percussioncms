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
