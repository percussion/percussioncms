/******************************************************************************
 *
 * [ PSUtils ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.fastforward.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import com.percussion.server.IPSRequestContext;

/**
 * A collection of useful utility methods for Rhythmyx exits.
 */
public class PSUtils
{
   /**
    * Tokenizes a string of comma-separated values into a Set.
    * 
    * @param sourceCSV must not be <code>null</code>.
    * @return set of strings parsed of of the source string of comma separated
    *         values. Never <code>null</code>, may be empty.
    */
   public static Set tokenizeCommaSeparatedValues(String sourceCSV)
   {
      if (sourceCSV == null)
         throw new IllegalArgumentException("sourceCSV may not be null");

      Set values = new HashSet();
      StringTokenizer tok = new StringTokenizer(sourceCSV, ",");
      while (tok.hasMoreTokens())
      {
         String value = tok.nextToken().trim();
         values.add(value);
      }
      return values;
   }

   /**
    * Safely gets the specified index of the parameter array as a String. The
    * default value will be returned if parameter array is null, or does not
    * contain a non-empty string at the specified index.
    * 
    * @param params array of parameter objects from the calling function. if
    *           <code>null</code> or the value requested for the index is
    *           <code>null</code> or empty, the default value is returned.
    * @param index specifies which parameter from the array will be returned
    * @param defaultValue returned if array does not have a non-empty string at
    *           the specified index.
    * 
    * @return the parameter at the specified index (converted to a String and
    *         trimmed), or the defaultValue.
    */
   public static String getParameter(Object[] params, int index,
         String defaultValue)
   {
      if (params == null || params.length < index + 1 || params[index] == null
            || params[index].toString().trim().length() == 0)
      {
         return defaultValue;
      }
      else
      {
         return params[index].toString().trim();
      }
   }

   /**
    * Convience method that calls
    * {@link PSUtils#getParameter(Object[], int, String)}with <code>null</code>
    * as the default value.
    * 
    * @param params
    * @param index
    * @return
    */
   public static String getParameter(Object[] params, int index)
   {
      return getParameter(params, index, null);
   }

   /**
    * Safely gets the specified index of the parameter array. The default value
    * will be returned if parameter array is null, or contains a
    * <code>null</code> value at the specified index.
    * 
    * @param params array of parameter objects from the calling function.
    * @param index specifies which parameter from the array will be returned
    * @param defaultValue returned if array does not have a non-null value at
    *           the specified index.
    * @return the parameter at the specified index, or the defaultValue.
    */
   public static Object getParameterObject(Object[] params, int index,
         Object defaultValue)
   {
      if (params == null || params.length < index + 1 || params[index] == null)
      {
         return defaultValue;
      }
      else
      {
         return params[index];
      }
   }

   /**
    * Convience method that calls
    * {@link PSUtils#getParameterObject(Object[], int, Object)}with
    * <code>null</code> as the default value.
    * 
    * @param params
    * @param index
    * @return
    */
   public static Object getParameterObject(Object[] params, int index)
   {
      return getParameterObject(params, index, null);
   }

   /**
    * Logs debug messages to the Rhythmyx application trace log and possibily to
    * standard output.
    * 
    * @param request the current request object, used for accessing the Rhythmyx
    *           application trace log, not <code>null</code>.
    * @param msg the message to be logged, not <code>null</code>.
    */
   public static void printTraceMessage(IPSRequestContext request, String msg)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");
      if (msg == null)
         throw new IllegalArgumentException("message may not be null");

      request.printTraceMessage(msg);
      if (false) // switch to true when debugging for easier access to the msgs
         System.out.println(msg);
   }
}