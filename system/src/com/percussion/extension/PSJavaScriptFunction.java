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

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.util.PSCharSets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Context;

/**
 * The PSJavaScriptFunction class stores compiled JavaScript
 * functions executed by PSJavaScriptUdfExtension objects.
 * <p>
 * The class is implemented by calling native routines to run
 * the JavaScript interpreter.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
class PSJavaScriptFunction implements ErrorReporter
{
   private static final Logger log = LogManager.getLogger(PSJavaScriptFunction.class);
   /**
    * Create an executable function for JavaScript extension.
    *
    * @param   exit      the UDF extension to be compiled
    */
   PSJavaScriptFunction( IPSExtensionDef def )
   {
      String my_key = "";

      // Do we really need this "if" block? DVG created this for caching purpose.
      String context = def.getRef().getContext();
      if ( context.length() > 0 )
         my_key += context + "/";

      my_key += def.getRef().getExtensionName();

      Iterator iter = def.getRuntimeParameterNames();
      ArrayList params = new ArrayList();
      while ( iter.hasNext())
         params.add( iter.next());

      int paramCount = params.size();
      m_paramNames = new String[paramCount];
      // we'll copy the param values in below as we get their names

      /* for ECMAScript, we must build the function into this format:
       *
       * function f(arg1, arg2)
       * {
       *    ... body ...
       * }
       */
      StringBuffer buf = new StringBuffer();
      buf.append("function ");
      buf.append(def.getRef().getExtensionName());
      buf.append("(");
      for (int i = 0; i < paramCount; i++) {
         String paramName = (String) params.get(i);
         buf.append( paramName );
         m_paramNames[i] = paramName;
      }

      buf.append(") {\n");
      buf.append(def.getInitParameter( "scriptBody" ));
      buf.append("\n}");
      String functionText = buf.toString();

      String digestedString = digestString(my_key + functionText);

      /* first check the hashtable to see if we've got one already */
      synchronized (ms_CompiledFunctions) {
         if (ms_CompiledFunctions.containsKey(my_key)) {
            String digestedDef = (String) ms_digestedFunctionDefs.get(my_key);

            /* Make sure the function hasn't changed ... */
            if (digestedDef != null && digestedDef.equals(digestedString))
            {
               m_myFunction = (Function) ms_CompiledFunctions.get(my_key);
               return;
            } else
            {
               /* Remove the function's entry in the static table, replace
                  it with the new definition below ...*/
               ms_CompiledFunctions.remove(my_key);
               ms_digestedFunctionDefs.remove(my_key);
            }
         }

         /* Javascript function representing:
          *
          * function <name> ( <params> )
          * {
          *    <body>
          * }
          *
          * where:
          *
          * name      = exit.getName()
          * params   = exit.getParamDefs() (an array of PSExtensionParamDef objects)
          * body      = exit.getBody()
          */
         Context cx = Context.enter();
         ErrorReporter prevReporter = null;
         try {
            prevReporter = cx.setErrorReporter(this);
            Scriptable scope = cx.initStandardObjects(null);
            m_myFunction = cx.compileFunction(
               scope, functionText, def.getRef().getExtensionName(), 1, null);
         } finally {
            cx.setErrorReporter(prevReporter);
            cx.exit();
         }

         ms_CompiledFunctions.put(my_key, m_myFunction);
         ms_digestedFunctionDefs.put(my_key, digestedString);
      }
   }

   /**
    * Convert the input raw string into a processed Java string.
    *
    * @param   rawString   the input raw string
    *
    * @return              the processed Java string
    */
   private String digestString(String rawString)
   {
      try
      {
           MessageDigest md = MessageDigest.getInstance("SHA-1");

         md.update(rawString.getBytes(PSCharSets.rxJavaEnc()));
         byte[] digest = md.digest();

         StringBuffer buf = new StringBuffer(digest.length * 2);
         String sTemp;
         for (int i = 0; i < digest.length; i++)
         {
            sTemp = Integer.toHexString(digest[i]);
            if (sTemp.length() == 0)
               sTemp = "00";
            else if  (sTemp.length() == 1)
               sTemp = "0" + sTemp;
            else if (sTemp.length() > 2)
               sTemp = sTemp.substring(sTemp.length() - 2);

            buf.append(sTemp);
         }

         return buf.toString();
      }
      catch (NoSuchAlgorithmException e)
      {
         return rawString;
      }
      catch (java.io.UnsupportedEncodingException e)
      {
         // should not happen
         return rawString;
      }
   }

   /**
    * Get runnable context and execute the function with the supplied arguments.
    *
    * @param   args      an array of String parameters
    *
    * @param   req      a request context object
    *
    * @return            the execution result of the JavaScript function
    */
   public Object processUdf(Object[] args, IPSRequestContext req)
   {
      /* This function must have thrown a compile error, now it
         will always return null */
      if (m_myFunction == null)
      {
         return null;
      }

      Context cx = Context.enter();
      try {
         Scriptable scope = null;
         if (req == null)
         {   // need to create it for each call if there's no context
            scope = cx.initStandardObjects(null);
         }
         else
         {
            scope = (Scriptable)req.getPrivateObject(SCOPE_CONTEXT_KEY);
            if (scope == null)
            {
               scope = cx.initStandardObjects(null);
               req.setPrivateObject(SCOPE_CONTEXT_KEY, scope);
            }
         }

         // now we need to go through and convert the Java input args to
         // JavaScript input args
         if (args == null)
            args = new Object[0];   // don't want to crash JS

         int paramCount = m_paramNames.length;
         int argCount = args.length;
         for (int i = 0; i < paramCount; i++) {
            Object arg = (i < argCount) ? args[i] : "";
            if (arg == null)
               arg = "";

            /* Need to tweak the args array to have a JS native date */
            if (arg instanceof java.util.Date) {
               args[i] = getJSDate((java.util.Date) arg, cx, scope);
               arg = args[i];
            }

            scope.put(m_paramNames[i], scope, arg);
         }

         Object retObject = m_myFunction.call(
            cx, scope, null /* no "this" object */, args);

         /* The toString method for this object returns the default
            Object.toString() !!!  */
         retObject = Context.jsToJava(retObject, Date.class);
         /* NativeDate no longer accessible should use jsToJava; https://stackoverflow.com/questions/7741699/parse-org-mozilla-javascript-nativedate-in-java-util-date
         if (retObject instanceof org.mozilla.javascript.NativeDate)
         {
            retObject = new Date
               ( (long) ((NativeDate) retObject).jsFunction_valueOf() );
         }
         */

         for (int i = 0; i < paramCount; i++) {
            Object arg = (argCount <= i) ? args[i] : null;
            scope.delete(m_paramNames[i]);
         }

         return retObject;
      } 
      catch (Exception e) 
      {
         PSConsole.printMsg("Extension", e);
         return null;
      } 
      finally 
      {
         cx.exit();
      }
   }


   /* Pass a context to here, to attempt to make JS Date work
      as per rhino bug db resolution... *** DVG *** */
   // Make that a scope -and- a context, -DG
   private static Object getJSDate(java.util.Date d, Context cx, Scriptable scope)
   {
      try {
         Object[] args = new Object[1];
         args[0] = new Long(d.getTime());
         scope.put("d", scope, args[0]);

         Object retObject = ms_dateCreatorFunction.call(
            cx, scope, null, args);

         scope.delete("d");

         return retObject;
      } 
      catch (Exception e)
      {
         PSConsole.printMsg("Extension", e);
         return null;
      } 
      finally 
      {
         cx.exit();
      }
   }

   /* ************** ErrorReporter Interface Implementation ************** */

   public void error(
      String message, String sourceName, int line,
      String lineSource, int lineOffset)
   {
      log.error("Error in {} : {}", sourceName, message);
      log.error("  source line (" + lineOffset + "): " + lineSource);
   }

   public EvaluatorException runtimeError(
      String message, String sourceName, int line,
      String lineSource, int lineOffset)
   {
      String msgText
         = "Error on line " + line + ", offset " + lineOffset
         + " of function " + sourceName + ": " + message;
      return new EvaluatorException(msgText);
   }

   public void warning(
      String message, String sourceName, int line,
      String lineSource, int lineOffset)
   {
      log.warn("Warning in {} : {} ", sourceName, message);
      log.warn("  source line ( {} ): {} ",lineOffset, lineSource);
   }


   private static final String SCOPE_CONTEXT_KEY = "PSJavaScriptScope";


   private static Function    ms_dateCreatorFunction;

   /* Build the date Creator Function! */
   static {
      Context cx = Context.enter();
      try {
         Scriptable scope = cx.initStandardObjects(null);
         ms_dateCreatorFunction = cx.compileFunction(
            scope, "function psdcf (d) { return new Date(d); }", "psdcf", 1, null);
      } finally {
         cx.exit();
      }
   }

   /**
    * hash table of compiled functions where:
    *    key = appName/exitName
    *      value = Integer(compiledScriptHandle)
    */
   private static Hashtable   ms_CompiledFunctions = new Hashtable();

   private static Hashtable   ms_digestedFunctionDefs = new Hashtable();

   private Function            m_myFunction;

   /**
    * Contains all of the parameter definitions for this function. If a fct
    * has no params, this will be an array of 0 elements. Never <code>null
    * </code> once initialized in ctor.
    */
   private String[]   m_paramNames;
}


