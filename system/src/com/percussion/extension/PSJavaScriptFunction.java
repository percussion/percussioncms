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

package com.percussion.extension;

import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.util.PSCharSets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

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
    * @param   def      the UDF extension to be compiled
    */
   PSJavaScriptFunction( IPSExtensionDef def )
   {
      String myKey = "";

      // Do we really need this "if" block? DVG created this for caching purpose.
      String context = def.getRef().getContext();
      if ( context.length() > 0 )
         myKey += context + "/";

      myKey += def.getRef().getExtensionName();

      Iterator iter = def.getRuntimeParameterNames();
      ArrayList params = new ArrayList();
      while ( iter.hasNext())
         params.add( iter.next());

      int paramCount = params.size();
      paramNames = new String[paramCount];
      // we'll copy the param values in below as we get their names

      /* for ECMAScript, we must build the function into this format:
       *
       * function f(arg1, arg2)
       * {
       *    ... body ...
       * }
       */
      StringBuilder buf = new StringBuilder();
      buf.append("function ");
      buf.append(def.getRef().getExtensionName());
      buf.append("(");
      for (int i = 0; i < paramCount; i++) {
         String paramName = (String) params.get(i);
         buf.append( paramName );
         paramNames[i] = paramName;
      }

      buf.append(") {\n");
      buf.append(def.getInitParameter( "scriptBody" ));
      buf.append("\n}");
      String functionText = buf.toString();

      String digestedString = digestString(myKey + functionText);

      /* first check the hashtable to see if we've got one already */
      synchronized (compiledFunctions) {
         if (compiledFunctions.containsKey(myKey)) {
            String digestedDef =  digestedFunctionDefs.get(myKey);

            /* Make sure the function hasn't changed ... */
            if (digestedDef != null && digestedDef.equals(digestedString))
            {
               myFunction = compiledFunctions.get(myKey);
               return;
            } else
            {
               /* Remove the function's entry in the static table, replace
                  it with the new definition below ...*/
               compiledFunctions.remove(myKey);
               digestedFunctionDefs.remove(myKey);
            }
         }

         /* Javascript function representing:
          *<code>
          * function <name> ( <params> )
          * {
          *    <body>
          * }
          *</code>
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
            myFunction = cx.compileFunction(
               scope, functionText, def.getRef().getExtensionName(), 1, null);
         } finally {
            cx.setErrorReporter(prevReporter);
            Context.exit();
         }

         compiledFunctions.put(myKey, myFunction);
         digestedFunctionDefs.put(myKey, digestedString);
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
           MessageDigest md = MessageDigest.getInstance("SHA-256");

         md.update(rawString.getBytes(PSCharSets.rxJavaEnc()));
         byte[] digest = md.digest();

         StringBuilder buf = new StringBuilder(digest.length * 2);
         StringBuilder sTemp = new StringBuilder();
         for (byte b : digest) {
            sTemp.append(String.format("%02X", b));
            if (sTemp.length() == 1)
               sTemp.append("0").append(sTemp);
            else if (sTemp.length() > 2)
               sTemp.append(sTemp.substring(sTemp.length() - 2));

            buf.append(sTemp);
         }

         return buf.toString();
      }
      catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
      {
         return rawString;
      }
      // should not happen

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
      if (myFunction == null)
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

         int paramCount = paramNames.length;
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

            scope.put(paramNames[i], scope, arg);
         }

         Object retObject = myFunction.call(
            cx, scope, null /* no "this" object */, args);

         retObject = Context.jsToJava(retObject, Date.class);

         for (int i = 0; i < paramCount; i++) {
            scope.delete(paramNames[i]);
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
         Context.exit();
      }
   }


   /* Pass a context to here, to attempt to make JS Date work
      as per rhino bug db resolution... *** DVG *** */
   // Make that a scope -and- a context, -DG
   private static Object getJSDate(java.util.Date d, Context cx, Scriptable scope)
   {
      try {
         Object[] args = new Object[1];
         args[0] = d.getTime();
         scope.put("d", scope, args[0]);

         Object retObject = dateCreatorFunction.call(
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
         Context.exit();
      }
   }

   /* ************** ErrorReporter Interface Implementation ************** */

   public void error(
      String message, String sourceName, int line,
      String lineSource, int lineOffset)
   {
      log.error("Error in {} : {}", sourceName, message);
      log.error("  source line ({}): {}",lineOffset,  lineSource);
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


   private static Function dateCreatorFunction;

   /* Build the date Creator Function! */
   static {
      Context cx = Context.enter();
      try {
         Scriptable scope = cx.initStandardObjects(null);
         dateCreatorFunction = cx.compileFunction(
            scope, "function psdcf (d) { return new Date(d); }", "psdcf", 1, null);
      } finally {
         Context.exit();
      }
   }

   /**
    * hash table of compiled functions where:
    *    key = appName/exitName
    *      value = Integer(compiledScriptHandle)
    */
   private static final HashMap<String,Function> compiledFunctions = new HashMap<>();

   private static final HashMap<String, String> digestedFunctionDefs = new HashMap<>();

   private Function myFunction;

   /**
    * Contains all of the parameter definitions for this function. If a fct
    * has no params, this will be an array of 0 elements. Never <code>null
    * </code> once initialized in ctor.
    */
   private String[] paramNames;
}


