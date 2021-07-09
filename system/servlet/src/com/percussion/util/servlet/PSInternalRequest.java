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
package com.percussion.util.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * An internal request for calling other servlets. This request is a
 * basic implementation of HttpServletRequest that allows the caller
 * to recapture the buffered output after a call to
 * <code>RequestDispatcher.include()</code>
 *
 * This class implements basic parameter support and HTTP headers.
 * For an implementation that supports multipart/form-data as required
 * by certain environments (such as the Rhythmyx server)
 * see {@link PSInternalRequestMultiPart}.
 *
 * @author DavidBenua
 */
public class PSInternalRequest
   extends HttpServletRequestWrapper
   implements HttpServletRequest
{
   /**
    * creates a new PSInternalRequest as a wrapper to an original
    * request received from a servlet container.
    *
    * This constructor copies all headers from the original request. It also
    * set the method to be the same with the original request.
    *
    * @param req the original request, it may not be <code>null</code>.
    */
   public PSInternalRequest(HttpServletRequest req)
   {
      super(req);
      Enumeration headers = req.getHeaderNames();
      if (headers != null)
      {
         m_headers = new HashMap();
         while (headers.hasMoreElements())
         {
            String headerName = (String) headers.nextElement();
            Enumeration hValues = req.getHeaders(headerName);
            List myValues = new ArrayList();
            while (hValues.hasMoreElements())
            {
               Object theValue = hValues.nextElement();
               myValues.add(theValue);
            }
            setHeader(headerName, myValues);
         }
      }
      setMethod(req.getMethod());
   }

   /**
    * Get the Method stored in this instance.
    *
    * @return the stored method, never <code>null</code> or empty.
    */
   public String getMethod()
   {
      return m_method;
   }

   /**
    * Sets the method.
    *
    * @param method the method to set, it may not be <code>null</code> or empty.
    */
   public void setMethod(String method)
   {
      if (method == null || method.trim().length() == 0)
         throw new IllegalArgumentException("method may not be null");
      m_method = method;
   }

   /**
    * Gets the HTML parameters as a Map. Each element of this
    * map is an Array of {@link java.lang.String} objects representing
    * all values of that particular HTTP parameter.
    *
    * @return The parameter map, the key is <code>String</code> object,
    *    the value is <code>String[]</code> object, never <code>null</code>.
    */
   public Map getParameterMap()
   {
      return m_params;
   }

   /**
    * Get an Enumeration of the names of all parameters.
    *
    * @return an enumetation over zero or more {@link java.lang.String}
    *    objects, never <code>null</code>.
    */
   public Enumeration getParameterNames()
   {
      return Collections.enumeration(m_params.keySet());
   }

   /**
    * Gets all values for a given header name. Use this method
    * when the HTML parameter can have more than one value.
    *
    * @param pName the name of the parameter. It may not be
    *    <code>null</code> or empty.
    *
    * @return an Array of Strings, it may be <code>null</code> if the
    *    parameter does not exist.
    */
   public String[] getParameterValues(String pName)
   {
      if (pName == null || pName.trim().length() == 0)
         throw new IllegalArgumentException("pName may not be null or empty");
      return (String[]) m_params.get(pName);
   }

   /**
    * Gets the first value of the parameter.
    *
    * @param pName the name of the parameter, it may not be <code>null</code>
    *    or empty.
    *
    * @return The String value, it may be <code>null</code> if the
    *    parameter is not exist.
    */
   public String getParameter(String pName)
   {
      String[] values = (String[]) m_params.get(pName);
      if (values == null || values.length == 0)
      {
         return null;
      }
      else
      {
         return values[0];
      }
   }

   /**
    * Sets the value of the named parameter. Use this method when
    * the parameter has only one value.
    * Use {@link #setParameterValues(String, String[])} when there is more
    * than one value.
    *
    * @param pName the name of this parameter, it may not be <code>null</code>
    *    empty.
    *
    * @param pValue the value to set. It may be <code>null</code> or empty.
    */
   public void setParameter(String pName, String pValue)
   {
      if (pName == null || pName.trim().length() == 0)
         throw new IllegalArgumentException("pName may not be null or empty");
      
      String values[] = new String[1];
      values[0] = pValue;
      m_params.put(pName, values);
   }

   /**
    * Sets a series of values for an HTML parameter.  Use this
    * method when setting more than one value for the same parameer
    * name.
    *
    * @param pName the name of the parameter, it may not be <code>null</code>
    *    or empty.
    *
    * @param pValues A set of values for the given parameter, it may not
    *    be <code>null</code> or empty.
    */
   public void setParameterValues(String pName, String[] pValues)
   {
      if (pName == null || pName.trim().length() == 0)
         throw new IllegalArgumentException("pName may not be null or empty");
      if (pValues == null || pValues.length == 0)
         throw new IllegalArgumentException("pValues may not be null or empty");
      m_params.put(pName, pValues);
   }

   /**
    * Get the header names.
    *
    * @return An enumeration over zero or more <code>String</code> objects,
    *    never <code>null</code>. The returned names are lowercase.
    */
   public Enumeration getHeaderNames()
   {
      return Collections.enumeration(m_headers.keySet());
   }

   /**
    * Get the values of the given header name.
    *
    * @param headerName the header name, it may not be <code>null</code> or
    *    empty. The header name is case insensitive.
    *
    * @return An enumeration over zero or more {@link java.lang.String}
    *    objects. It may be <code>null</code> if the supplied head does
    *    not exist.
    */
   public Enumeration getHeaders(String headerName)
   {
      if (headerName == null || headerName.trim().length() == 0)
         throw new IllegalArgumentException("headerName may not be null or empty");
      
      Collection values = (Collection) m_headers.get(headerName.toLowerCase());
      if (values != null)
         return Collections.enumeration(values);
      else
         return null;
   }

   /**
    * Get the 1st value (if there are more than one) of the given header name.
    *
    * @param headerName the header name, it is case insensitive and it may not 
    *    be <code>null</code> or empty.
    *
    * @return The 1st value. It may be <code>null</code> if the supplied head
    *    does not exist.
    */
   public String getHeader(String headerName)
   {
      if (headerName == null || headerName.trim().length() == 0)
         throw new IllegalArgumentException("headerName may not be null or empty");
      
      List header = (List) m_headers.get(headerName.toLowerCase());
      if (header != null && !header.isEmpty())
      {
         return (String) header.get(0);
      }
      else
      {
         return null;
      }
   }

   /**
    * Get the numeric value of the given header.
    *
    * @param headerName the header name, it may not be <code>null</code> or
    *    empty.
    *
    * @return the numeric value of the given header; <code>-1</code> if the
    *    header does not exist.
    */
   public int getIntHeader(String headerName) throws NumberFormatException
   {
      int result = -1;
      String headerStr = this.getHeader(headerName);
      if (headerStr != null && headerStr.length() > 0)
      {
         result = Integer.parseInt(headerStr);
      }
      return result;
   }

   /**
    * Gets the specified header as a Date.
    *
    * @param headerName the header name, it may not be <code>null</code> or
    *    empty.
    *
    * @return The date value; It may be <code>-1</code> if the header does
    *    not exist.
    *
    * @throws IllegalArgumentException when the header cannot be converted to
    *    a date.
    */
   public long getDateHeader(String headerName) throws IllegalArgumentException
   {
      long result = -1L;
      String headerStr = this.getHeader(headerName);
      if (headerStr != null && headerStr.length() > 0)
      {
         try
         {
            DateFormat df = DateFormat.getInstance();
            Date dr = df.parse(headerStr);
            result = dr.getTime();
         }
         catch (ParseException e)
         {
            throw new IllegalArgumentException();
         }
      }
      return result;
   }

   /**
    * Sets a header with multiple values.
    * Use {@link #setHeader(String,String)}
    * when there is only one value.
    *
    * @param hName the header name, it may not be <code>null</code> or empty.
    *
    * @param hValues the values, it may not be <code>null</code> or empty.
    */
   public void setHeader(String hName, List hValues)
   {
      if (hName == null || hName.trim().length() == 0)
         throw new IllegalArgumentException("hName may not be null or empty");
      if (hValues == null || hValues.isEmpty())
         throw new IllegalArgumentException("hValues may not be null or empty");
      m_headers.put(hName.toLowerCase(), hValues);
   }

   /**
    * Sets a header with a single value.
    * Use {@link #setHeader(String,List)} when there are
    * multiple values.
    *
    * @param hName the header name, it may not be <code>null</code> or empty.
    *
    * @param hValue the header value, it may not be <code>null</code> or empty.
    */
   public void setHeader(String hName, String hValue)
   {
      if (hName == null || hName.trim().length() == 0)
         throw new IllegalArgumentException("hName may not be null or empty");
      if (hValue == null || hValue.trim().length() == 0)
         throw new IllegalArgumentException("hValue may not be null or empty");
      List values = Collections.singletonList(hValue);
      this.setHeader(hName, values);
   }

   /**
    * A convenience method for setting a header from an
    * Array of Strings.
    *
    * @param hName the header name, it may not be <code>null</code> or empty.
    *
    * @param hValue the header values, it may not be <code>null</code> or empty.
    */
   public void setHeader(String hName, String[] hValues)
   {
      if (hName == null || hName.trim().length() == 0)
         throw new IllegalArgumentException("hName may not be null or empty");
      if (hValues == null || hValues.length == 0)
         throw new IllegalArgumentException("hValues may not be null or empty");
      List values = Arrays.asList(hValues);
      this.setHeader(hName, values);
   }

   /**
    * Removes a header.
    *
    * @param hName the name of the header.
    */
   public void removeHeader(String hName)
   {
      m_headers.remove(hName);
   }

   /**
    * Holds the method (usually GET or POST). Default to POST. It is reset
    * by {@link #setMethod(String)}. It is never <code>null</code> or empty.
    */
   private String m_method = "POST";
   
   /**
    * Holds the HTTP_headers, never <code>null</code>, but may be empty.
    * The map key is the name of the header as <code>String</code>, in 
    * lower case. The map value is the corresponde value of the header as
    * <code>List</code>.   
    */
   private Map m_headers = new HashMap();
   /**
    * a local parameter map. The key is the parameter name (as a
    * <code>String</code>) and the value is a <code>String[]</code> of all
    * values, even if there is only one value.  This is to conform to the Map
    * in the Servlet interface.
    */
   protected Map m_params = new HashMap();
}
