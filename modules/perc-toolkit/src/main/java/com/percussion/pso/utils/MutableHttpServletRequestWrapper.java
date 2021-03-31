/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.Validate;

import com.percussion.utils.collections.PSFacadeMap;

/**
 * An HttpServletRequestWrapper that allows modification of the request parameters and headers.
 * This is generally useful when forwarding to servlets or building filter  
 * chains.  
 * <p>
 * Parameters are modified by setting the parameter either as a single String value or as an array of
 * String values.  Parameter names are case sensitive.
 * <p>
 * Headers are similarly modified, except that the names are case insensitive.
 * <p>
 * Any header or parameter which is modified in this wrapper loses all values from the wrapped
 * request.  If you need to add values, you should copy them from the original request into
 * this class.   
 * 
 *
 * @author DavidBenua
 *
 */
public class MutableHttpServletRequestWrapper extends HttpServletRequestWrapper
      implements
         HttpServletRequest
{
   PSFacadeMap<String, String[]> localParams; 
   
   Map<String, String[]> localHeaders; 
   /**
    * Constructs a new wrapper based on an existing request.
    * @param request the request to wrap. 
    */
   @SuppressWarnings("unchecked")
   public MutableHttpServletRequestWrapper(HttpServletRequest request)
   {
      super(request);
      localParams = new PSFacadeMap<String, String[]>(request.getParameterMap());
      
      localHeaders = new HashMap<String, String[]>(); 
    
   }
   
   /**
    * Add a parameter with multiple values
    * @param key the parameter name
    * @param values the values to add; 
    */
   public void setParameter(String key, String[]values)
   {
      localParams.put(key, values); 
   }
   
   /**
    * Add a parameter with a single value
    * @param key the parameter name.
    * @param value the new value. 
    */
   public void setParameter(String key, String value)
   {
      String[] values = new String[]{value};
      setParameter(key, values); 
   }
   
   /**
    * Sets a header with a single value. Convenience method for {@link #setHeader(String, String[])}. 
    * @param name header name. Never <code>null</code> or <code>empty</code>. 
    * @param value the value to set. 
    */
   public void setHeader(String name, String value)
   {
      String[] values = new String[]{value};
      setHeader(name, values); 
   }
   
   /**
    * Sets a header with multiple values. Header names are case insensitive. 
    * Any header which is overridden here will have only the local values, the
    * values from the underlying request will not be considered. 
    * @param name header name.  Never <code>null</code> or <code>empty</code>.
    * @param values the values to set. May be <code>null</code>
    */
   public void setHeader(String name, String[] values)
   {
      Validate.notEmpty(name); 
      String key = name.toUpperCase(); //header names are case insensitive. 
      localHeaders.put(key, values); 
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
    */
   @Override
   public String getParameter(String name)
   {
     String[] vals = getParameterValues(name);
     if(vals == null || vals.length == 0)
     {
        return null;
     }
     return vals[0]; 
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameterMap()
    */
   @Override
   @SuppressWarnings("unchecked")
   public Map getParameterMap()
   {
      return Collections.unmodifiableMap(localParams); 
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameterNames()
    */
   @Override
   @SuppressWarnings("unchecked")
   public Enumeration getParameterNames()
   {
      return Collections.<String>enumeration(
            localParams.keySet());
   }

   /**
    * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
    */
   @Override
   public String[] getParameterValues(String name)
   {  
      return localParams.get(name); 
   }

   /**
    * @see javax.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
    */
   @Override
   public String getHeader(String name)
   {
      Validate.notEmpty(name); 
      String key = name.toUpperCase();
      if(localHeaders.containsKey(key))
      {
         String[] values = localHeaders.get(key); 
         if(values == null || values.length == 0)
         {
            return null; 
         }
         return values[0]; 
      }
      return super.getHeader(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequestWrapper#getHeaderNames()
    */
   @Override
   @SuppressWarnings("unchecked")   
   public Enumeration getHeaderNames()
   {
      Set<String> names = new HashSet<String>();
      names.addAll(localHeaders.keySet()); 
      Enumeration e = super.getHeaderNames();
      while(e.hasMoreElements())
      {
         String nm = e.nextElement().toString().toLowerCase(); 
         if(!names.contains(nm)) //faster this way
         {
            names.add(nm);
         }
      }
      return Collections.enumeration(names);
   }

   /**
    * @see javax.servlet.http.HttpServletRequestWrapper#getHeaders(java.lang.String)
    */
   @Override
   @SuppressWarnings("unchecked")
   public Enumeration getHeaders(String name)
   {
      Validate.notEmpty(name); 
      String key = name.toUpperCase();
      if(localHeaders.containsKey(key))
      {
         List<String> values = Arrays.asList(localHeaders.get(key));
         return Collections.enumeration(values); 
      }
      return super.getHeaders(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequestWrapper#getIntHeader(java.lang.String)
    */
   @Override
   public int getIntHeader(String name)
   {
      Validate.notEmpty(name);
      String key = name.toUpperCase();
      if(localHeaders.containsKey(key))
      {
         String value = getHeader(name);
         return Integer.parseInt(value); 
      }
      return super.getIntHeader(name);
   }
   
   /**
    * @see javax.servlet.http.HttpServletRequestWrapper#getDateHeader(java.lang.String)
    */
   @Override
   public long getDateHeader(String name)
   {
      Validate.notEmpty(name);
      String key = name.toUpperCase();
      if(localHeaders.containsKey(key))
      {
         String value = getHeader(name);
         return Long.parseLong(value); 
      }
      return super.getDateHeader(name);
   }
   
}
