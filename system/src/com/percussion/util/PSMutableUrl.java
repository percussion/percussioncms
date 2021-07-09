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
package com.percussion.util;

import com.percussion.server.PSRequestParsingException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A URL that can be modified. Methods for changing the root as well as as the
 * HTML Parmaeter exist. This does not validate the URL string to conform to URL
 * spec. The part before the first occurence of the
 * {@link QUERY_SEP query separator character} is taken as base of the URL and 
 * the rest is treated as a {@link PARAM_SEP parameter separator character} to 
 * parsing the parameters.
 */
public class PSMutableUrl
{
   /**
    * Ctor taking URL string. Parses the URL base and parameters int 
    * approrpiate fields for future use.
    * @param Url URL string, must not be <code>null</code> or empty.
    */
   public PSMutableUrl(String Url) throws PSRequestParsingException
   {
      if (Url == null || Url.length() < 1)
      {
         throw new IllegalArgumentException("Url must not be null or empty");
      }
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
      // inner class provided by PSParseUrlQueryString 
      this.m_param = new HashMap(
            PSParseUrlQueryString.parseParameters(queryString));      
    }
     
   
   /**
    * @return Returns base of the URL. May be <code>null</code> or empty.
    */
   public String getBase()
   {
      return m_base;
   }
   
   /**
    * @param m_base The URL base to set, must not be <code>null</code> or empty.
    */
   public void setBase(String base)
   {
      if (base == null || base.length() < 1)
      {
         throw new IllegalArgumentException("base must not be null or empty");
      }
      this.m_base = base;
   }
   
   /**
    * Add or modify the named parameter to the parameter list.
    * 
    * @param pName name of the parameter to add or modify, must not be
    *           <code>null</code>.
    * @param pValue value of the parameter to set, may be <code>null</code>
    */
   public void setParam(String pName, String pValue)
   {
      if (pName == null || pName.length() < 1)
      {
         throw new IllegalArgumentException("pName must not be null or empty");
      }
      m_param.put(pName, pValue);    
   }
   
   /**
    * Append the supplied parameter list to the param list.
    * 
    * @param newParams Map of the parameters to append, must notbe
    *           <code>null</code>.
    */
   public void setParamList(Map newParams)
   {
      if (newParams == null)
      {
         throw new IllegalArgumentException("newParams must not be null");
      }
      m_param.putAll(newParams);
   }  
   
   /**
    * Get the value of the specified parameter.
    * 
    * @param pName name of the parameter, must not be <code>null</code> or
    *           empty.
    * @return
    */
   public String getParam(String pName)
   {
      if (pName == null || pName.length() < 1)
      {
         throw new IllegalArgumentException("pName must not be null or empty");
      }
      Object obj = m_param.get(pName); 
      if(obj == null)
      {
         return null;
      }
      else
      {     
         return obj.toString();
      }
   }
   
   /**
    * Remove a named parameter from the list of parameters, if exists.
    * 
    * @param pName name of the param to be removed, must not be
    *           <code>null</code> or empty.
    */
   public void dropParam(String pName)
   {
      if (pName == null || pName.length() < 1)
      {
         throw new IllegalArgumentException("pName must not be null or empty");
      }
      m_param.remove(pName); 
   } 
   
   /**
    * @return map of all parameter name-value pairs.
    */
   public Map getParamMap()
   {
      return m_param; 
   } 
   
   /**
    * Create the URL string from the base, and the parameter map in the syntax
    * of <base>? <paramName1=paramValue1>& <paramName2=paramValue2>... where ?
    * is the query separator and & is the parameter separator.
    * 
    * @return new URL string described above.
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
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
   
   /**
    * The part before the first occurence of the
    * {@link QUERY_SEP query separator character}. Initialized in the ctor. May
    * be <code>null</code>
    */
   private String m_base = null;

   /**
    * Map of parameters parsed from the URL query string in the ctor, Never
    * <code>null</code> may be empty.
    */
   private Map m_param = new HashMap(); 
   
   /**
    * Query separator character.
    */
   private static final char QUERY_SEP = '?';

   /**
    * Parameter separator character.
    */
   private static final char PARAM_SEP = '&'; 
}
