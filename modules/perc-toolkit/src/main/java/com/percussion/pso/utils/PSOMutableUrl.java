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
/*
 * com.percussion.consulting.utils PSOMutableUrl.java
 *
 */
package com.percussion.pso.utils;

import com.percussion.server.PSRequestParsingException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A URL that can be modified. Methods for changing the root as well as 
 * as the HTML Parameter exist. 
 * 
 * @author DavidBenua
 *
 */
public class PSOMutableUrl
{
   private String m_base = null;
   Map<String, Object> m_param = new HashMap<String,Object>(); 
   
   /**
    * 
    */
   public PSOMutableUrl(String Url) throws PSRequestParsingException
   {
      int sepPos = Url.indexOf(QUERY_SEP);
      String queryString = ""; 
      if(sepPos < 0)
      {
         this.m_base = Url;              
      }
      else
      {
         this.m_base = Url.substring(0, sepPos); 
         queryString = Url.substring(sepPos+1); 
      }
      
      // we want the default behavior of HashMap() not the special
      // inner class provided by PSOParseUrlQueryString 
      this.m_param = new HashMap<String,Object>(
            PSOParseUrlQueryString.parseParameters(queryString));      
    }
     
   
   /**
    * @return Returns the m_base.
    */
   public String getBase()
   {
      return m_base;
   }
   
   /**
    * @param base The base to set.
    */
   public void setBase(String base)
   {
      this.m_base = base;
   }
    
   public void setParam(String pName, String pValue)
   {
      this.m_param.put(pName, pValue);    
   }
   
   public void setParamList(Map<String,Object> newParams)
   {
      this.m_param.putAll(newParams); 
   }  
   
   public String getParam(String pName)
   {
      Object obj = this.m_param.get(pName); 
      if(obj == null)
      {
         return null;
      }
      return obj.toString();
   }
   
   public void dropParam(String pName)
   {
      this.m_param.remove(pName); 
   } 
   public Map<String, Object> getParamMap()
   {
      return m_param; 
   } 
   
   @SuppressWarnings("unchecked")
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(m_base);
      
      char sep = QUERY_SEP; 
      Iterator iter = this.m_param.entrySet().iterator(); 
      while(iter.hasNext()) 
      {
        Map.Entry entry = (Map.Entry) iter.next(); 
        sb.append(sep);
        sb.append(entry.getKey().toString());
        sb.append('='); 
        sb.append(entry.getValue().toString()); 
        sep = PARAM_SEP; 
      }
      return sb.toString(); 
   }
   
   
   private static char QUERY_SEP = '?';
   private static char PARAM_SEP = '&'; 
}
