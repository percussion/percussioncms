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

package com.percussion.extension;

import com.percussion.data.IPSDataErrors;
import com.percussion.data.PSConversionException;
import com.percussion.data.PSDataConverter;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.server.IPSRequestContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The PSJavaScriptUdfExtension executes JavaScript functions.
 *
 * @author     Paul Howard
 * @version    1.1
 * @since      2.0
 */
public class PSJavaScriptUdfExtension implements IPSUdfProcessor
{
   /**
    * The standard ctor for this object.
    *
    * @param ref A valid name reference for this object.
    *
    * @throws IllegalArgumentException if ref is <code>null</code>

   public PSJavaScriptUdfExtension()
   {
      m_ref = ref;
   }
    */

   /*************** Implement IPSExtension and IPSUdfProcessor ***************/

   /**
    * Initialize this extension. The init properties in the def must contain
    * a ScriptBody key, whose value is the function body. If already initialized,
    * it is re-initialized w/ the passed definition.
    *
    * @param codeRoot Not used by this class since all needed resources are
    * in the def.
    *
    * See {@link IPSExtension#init(IPSExtensionDef,File) init} for more info.
    */
   public void init( IPSExtensionDef def, File codeRoot )
      throws PSExtensionException
   {
      if ( null == def )
         throw new IllegalArgumentException( "extension def can't be null" );

//      try
      {
         m_JavaScriptFunction = new PSJavaScriptFunction( def );
      }
/*      catch(com.percussion.error.PSIllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.toString());
      }*/
      m_extensionDef = def;
      // extract the param definitions for use during processing
      Iterator iter = def.getRuntimeParameterNames();
      ArrayList pdefs = new ArrayList();
      while ( iter.hasNext())
         pdefs.add( def.getRuntimeParameter((String) iter.next()));
      m_paramDefs = new IPSExtensionParamDef[pdefs.size()];
      pdefs.toArray( m_paramDefs );
   }

   /**
    * Execute the function with the supplied arguments.
    *
    * @param      params         the parameter values to use in the UDF
    * @param      request         the current request context
    *
    * @return            the result of the function execution
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws com.percussion.data.PSConversionException
   {
      final int reqdParamCount = m_paramDefs.length;
      final int paramCount = (params == null) ? 0 : params.length;
      if (reqdParamCount != paramCount)
      {
         String arg0 = String.valueOf(reqdParamCount) + " parameters required, but " +
               String.valueOf(paramCount) + " parameters were specified.";
         int errCode = com.percussion.server.IPSServerErrors.ARGUMENT_ERROR;
         Object[] args = { arg0, "PSJavaScriptUdfExtension/processUdf" };
         throw new PSConversionException(errCode, args);
      }

      Object[] paramValues = new Object[paramCount];
      for (int iCnt = 0; iCnt < paramCount; iCnt++) {
         paramValues[iCnt] = convertParameter(params[iCnt], m_paramDefs[iCnt]);
      }

      return m_JavaScriptFunction.processUdf(paramValues, request);
   }

   /**
    * Convert the JavaScript parameters into a Java object.
    *
    * @param   p  the JavaScript parameter definition
    * @param   o  the Java object
    * @exception  PSConversionException   if cannot convert
    */
   private Object convertParameter(Object o, IPSExtensionParamDef p)
      throws com.percussion.data.PSConversionException
   {
      if (o == null)
         return o;

      int type = getTypeCode(p.getDataType());

      switch (type)
      {
         case JAVASCRIPT_TYPECODE_DATE:
            return getDate(o);
         case JAVASCRIPT_TYPECODE_NUMBER:
            return getNumber(o);
         case JAVASCRIPT_TYPECODE_STRING:
            return getString(o);
         case JAVASCRIPT_TYPECODE_BOOLEAN:
            return getBoolean(o);
         default:
            throw new com.percussion.data.PSConversionException(
               IPSExtensionErrors.UNKNOWN_PARAMETER_TYPE,  "Date, Number, String, Boolean");
      }
   }

   private Object getDate(Object o)
      throws com.percussion.data.PSConversionException
   {
      /* Bug Id: Rx-99-11-0016 Adding literal handling
         (PSDateLiteral/PSNumericLiteral/PSTextLiteral) */
      if (o instanceof java.lang.Number){
         return new java.util.Date(((java.lang.Number) o).longValue());
      }
      else if ((o instanceof java.lang.String) ||
               (o instanceof PSTextLiteral)) {
         java.lang.String dateText = o.toString();
         java.util.Date d;

         try {
            d = PSDataConverter.parseStringToDate(dateText);
            return d; //new java.lang.Long(d.getTime());
         } catch (java.text.ParseException e) {
            Object args[] = {   o.getClass().getName(), "String", dateText };
            throw new com.percussion.data.PSConversionException(
               IPSDataErrors.UNSUPPORTED_CONVERSION, args);
         }
      }
      else if (o instanceof java.util.Date){
         return /*new java.lang.Long(((java.util.Date)*/ o/*).getTime())*/;
      } else if (o instanceof PSNumericLiteral) {
         return new java.util.Date(((PSNumericLiteral) o).getNumber().longValue());
      } else if (o instanceof PSDateLiteral) {
         return new java.util.Date(((PSDateLiteral) o).getDate().getTime());
      }


      // Throw conversion exception.  o will contain exception information
      //      for string conversion exceptions...
      Object args[] = {   o.getClass().getName(), "Date", o.toString() };
      throw new com.percussion.data.PSConversionException(
         IPSDataErrors.UNSUPPORTED_CONVERSION, args);
   }

   private Object getNumber(Object o)
      throws com.percussion.data.PSConversionException
   {
      try {
         /* Bug Id: Rx-99-11-0016 Adding literal handling
            (PSDateLiteral/PSNumericLiteral/PSTextLiteral) */
         if (o instanceof java.lang.Number)
            return new java.lang.Double(((java.lang.Number) o).doubleValue());
         else if (o instanceof java.lang.String)
            return new java.lang.Double((java.lang.String) o);
         else if (o instanceof PSTextLiteral)
            return new java.lang.Double(((PSTextLiteral) o).toString());
         else if (o instanceof java.lang.Boolean)
            return new java.lang.Double( ((java.lang.Boolean) o).booleanValue() ? 1 : 0 );
         else if (o instanceof java.util.Date){
            java.util.Date day = (java.util.Date)o;
            double dayValue = day.getTime();
            return new java.lang.Double(dayValue);
         } else if (o instanceof PSNumericLiteral) {
            return new java.lang.Double(((PSNumericLiteral) o).getNumber().doubleValue());
         } else if (o instanceof PSDateLiteral) {
            java.util.Date day = ((PSDateLiteral) o).getDate();
            double dayValue = day.getTime();
            return new java.lang.Double(dayValue);
         }
      } catch (Exception e){
         Object args[] = { o.getClass().getName(), "Number", o.toString() };
         throw new com.percussion.data.PSConversionException(
            IPSDataErrors.UNSUPPORTED_CONVERSION, args);
      }

      // Throw conversion exception.
      Object args[] = { o.getClass().getName(), "Number", o.toString() };
      throw new com.percussion.data.PSConversionException(
         IPSDataErrors.UNSUPPORTED_CONVERSION, args);
   }

   private Object getBoolean(Object o)
      throws com.percussion.data.PSConversionException
   {
      /* Bug Id: Rx-99-11-0016 Adding literal handling
         (PSNumericLiteral/PSTextLiteral) */
      if (o instanceof java.lang.Number)
      {
         return ((java.lang.Number) o).doubleValue() != 0;
      } else if (o instanceof PSNumericLiteral) {
         return ((PSNumericLiteral) o).getNumber().doubleValue() != 0;
      } else if (   (o instanceof java.lang.String) ||
                  (o instanceof PSTextLiteral) )
      {
         Boolean b = (Boolean) ms_truthMap.get(o.toString().toLowerCase());
         if (b != null)
            return b;
      } else if (o instanceof java.lang.Boolean)
      {
         return o;
      }

      // Throw conversion exception.
      Object args[] = { o.getClass().getName(), "Boolean", o.toString() };
      throw new com.percussion.data.PSConversionException(
         IPSDataErrors.UNSUPPORTED_CONVERSION, args);
   }

   private Object getString(Object o)
      throws com.percussion.data.PSConversionException
   {
      if (o == null)
         return "";

      if (o instanceof java.lang.Object[]) {
         /* Do magic here */
         java.lang.Object[] arr = (java.lang.Object[]) o;
         if (arr.length > 1) {
            String str = "";
            String sepStr = "";
            for (int i = 0; i < arr.length; i++)
            {
               if (arr[i] != null)
               {
                  str += sepStr + arr[i].toString();
                  if (sepStr.equals(""))
                     sepStr = ";";
               }
            }
            o = str;
         } else {
            o = ((java.lang.Object[])o)[0];
         }
      }

      if (o instanceof java.lang.Boolean)
      {
         return ((java.lang.Boolean) o).booleanValue() ? "true" : "false";
      } else
      {
         return o.toString();
      }
   }

   private int getTypeCode(String str)
   {
      if (str == null || str.length() == 0)
         return JAVASCRIPT_TYPECODE_STRING;
      else if (str.equals(JAVASCRIPT_TYPE_STRING))
         return JAVASCRIPT_TYPECODE_STRING;
      else if (str.equals(JAVASCRIPT_TYPE_NUMBER))
         return JAVASCRIPT_TYPECODE_NUMBER;
      else if (str.equals(JAVASCRIPT_TYPE_BOOLEAN))
         return JAVASCRIPT_TYPECODE_BOOLEAN;
      else if (str.equals(JAVASCRIPT_TYPE_DATE))
         return JAVASCRIPT_TYPECODE_DATE;
      else
         return JAVASCRIPT_TYPECODE_UNKNOWN;
   }

   /** A hashmap storing boolean information. */
   private static HashMap<String, Boolean> ms_truthMap;

   static {
      ms_truthMap = new HashMap<>();
      /* True */                           /* False */
//      ms_truthMap.put ("TRUE", btrue);      ms_truthMap.put ("FALSE", bfalse);
//      ms_truthMap.put ("True", btrue);      ms_truthMap.put ("False", bfalse);
      ms_truthMap.put ("true", true);      ms_truthMap.put ("false", false);
      ms_truthMap.put ("t", true);         ms_truthMap.put ("f", false);
//      ms_truthMap.put ("T", btrue);         ms_truthMap.put ("F", bfalse);
//      ms_truthMap.put ("YES", btrue);      ms_truthMap.put ("NO", bfalse);
//      ms_truthMap.put ("Yes", btrue);      ms_truthMap.put ("No", bfalse);
      ms_truthMap.put ("yes", true);      ms_truthMap.put ("no", false);
      ms_truthMap.put ("y", true);         ms_truthMap.put ("n", false);
//      ms_truthMap.put ("Y", btrue);         ms_truthMap.put ("N", bfalse);
      ms_truthMap.put ("1", true);         ms_truthMap.put ("0", false);
   }

   private static final String JAVASCRIPT_TYPE_DATE      = "Date";
   private static final String JAVASCRIPT_TYPE_NUMBER      = "Number";
   private static final String JAVASCRIPT_TYPE_STRING      = "String";
   private static final String JAVASCRIPT_TYPE_BOOLEAN   = "Boolean";

   private static final int JAVASCRIPT_TYPECODE_UNKNOWN   = 0;
   private static final int JAVASCRIPT_TYPECODE_DATE      = 1;
   private static final int JAVASCRIPT_TYPECODE_NUMBER   = 2;
   private static final int JAVASCRIPT_TYPECODE_STRING   = 3;
   private static final int JAVASCRIPT_TYPECODE_BOOLEAN   = 4;

   /** An object of JavaScript function. */
   private PSJavaScriptFunction   m_JavaScriptFunction;

   /** The extension definition interface. */
   private IPSExtensionDef m_extensionDef;

   /**
    * Contains all of the parameter definitions for this function. If a fct
    * has no params, this will be an array of 0 elements. Never <code>null
    * </code> once initialized in ctor.
    */
   private IPSExtensionParamDef [] m_paramDefs;
}
