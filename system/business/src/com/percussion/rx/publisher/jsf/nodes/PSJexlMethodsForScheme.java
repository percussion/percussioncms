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
package com.percussion.rx.publisher.jsf.nodes;

import static com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase.RX_PREFIX;
import static com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase.SYS_CONTEXT;
import static com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase.TOOLS_PREFIX;
import static com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase.USER_CONTEXT;
import static com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase.USER_PREFIX;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionMethod;
import com.percussion.extension.PSExtensionMethodParam;
import com.percussion.extension.PSExtensionRef;
import com.percussion.services.utils.jexl.PSJexlExtensionHelper;
import com.percussion.utils.jexl.PSPredefinedJexlVariableDefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A convenient class to get a list of predefined JEXL methods and variables
 * used by {@link PSJexlLocationGenerator}
 */
public class PSJexlMethodsForScheme
{
   /**
    * The backing bean for a predefined JEXL method or variable displayed in
    * the Location Scheme Editor.
    */
   public static class JexlMethod
   {
      // see {@link #getName)
      private String mi_name;
      // see {@link #getDescription}
      private String mi_description;

      /**
       * Constructor.
       * @param name name of the method or variable, not <code>null</code> or 
       *    empty.
       * @param desc the description of the method or variable, may be 
       *    <code>null</code> or empty.
       */
      public JexlMethod(String name, String desc)
      {
         if (name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name may not be null or empty.");
         
         mi_name = name;
         mi_description = desc;
      }
      
      /**
       * @return name of the method or variable, never <code>null</code> or 
       *    empty.
       */
      public String getName()
      {
         return mi_name;
      }
      
      /**
       * @return description of the method or variable, may be <code>null</code>
       *    or empty.
       */
      public String getDescription()
      {
         return mi_description;
      }
   }
   
   /**
    * Apply a supplied filter to a list of predefined JEXL methods and 
    * variables that can be used by {@link PSJexlLocationGenerator}
    *  
    * @param filter the filter, never <code>null</code>, may be empty.
    * 
    * @return the filtered JEXL methods and variables, never <code>null</code>
    *    may be empty.
    */
   public static List<JexlMethod> getFilteredJexlMethods(String filter)
   {
      
      final String star = "*";
      final String allPattern = ".*";
      String pattern = filter.replaceAll("\\*", allPattern);
      
      // escape special character '$' with "\$"
      pattern = pattern.replaceAll("\\$", "\\\\\\$");

      if (!filter.endsWith(star))
      {
         pattern = pattern + allPattern;
      }
      if (!filter.startsWith(star))
      {
         pattern = allPattern + pattern;
      }
      pattern = pattern.toLowerCase();

      List<JexlMethod> methods = new ArrayList<>();
      for (JexlMethod m : PSJexlMethodsForScheme.getJexlMethods())
      {
         if (Pattern.matches(pattern, m.getName().toLowerCase()))
         {
            methods.add(m);
         }
      }
      
      return Collections.unmodifiableList(methods);
   }
   
   /**
    * @return all predefined JEXL methods and variables that can be used by
    *    {@link PSJexlLocationGenerator}, never <code>null</code> or empty.
    */
   private static synchronized List<JexlMethod> getJexlMethods()
   {
      if (ms_allMethodsVars != null)
         return ms_allMethodsVars;
      
      List<IPSExtensionDef> defList = new ArrayList<>();
      Collection<IPSExtensionDef> defs;
      defs = PSJexlExtensionHelper.getJexlExtensionDefs();
      if (defs != null)
         defList.addAll(defs);
      defs = PSJexlExtensionHelper.getVelocityTools();
      defList.addAll(defs);
      
      ms_allMethodsVars = new ArrayList<>();
      for (IPSExtensionDef def : defList)
      {
         ms_allMethodsVars.addAll(getMethodsFromExtensionDef(def));
      }
      
      for (String[] var : PSPredefinedJexlVariableDefs
            .getPredefinedJexlVarDefs())
      {
         JexlMethod m = new JexlMethod(var[0], "[" + var[1] + "] " + var[2]);
         ms_allMethodsVars.add(m);
      }
      
      // filter out the methods defined in NONE_JEXL_METHODS
      List<JexlMethod> nonJexlMethods = new ArrayList<>();
      for (JexlMethod m : ms_allMethodsVars)
      {
         if (isNotJexlMethod(m.mi_name))
         {
            nonJexlMethods.add(m);
         }
      }
      ms_allMethodsVars.removeAll(nonJexlMethods);
      
      return ms_allMethodsVars;
   }

   /**
    * Gets all methods defined in the supplied extension definition.
    * @param def the extension definition, assumed not <code>null</code>.
    * @return a list of methods, never <code>null</code>, but may be empty.
    */
   private static List<JexlMethod> getMethodsFromExtensionDef(
         IPSExtensionDef def)
   {
      List<JexlMethod> methods = new ArrayList<>();

      // get the start name
      PSExtensionRef ref = def.getRef();
      String context = ref.getContext();
      String startname;
      if (context.equalsIgnoreCase(USER_CONTEXT))
         startname = USER_PREFIX;
      else if (context.equalsIgnoreCase(SYS_CONTEXT))
         startname = RX_PREFIX;
      else
         startname = TOOLS_PREFIX;
      startname = startname + "." + ref.getExtensionName();

      Iterator<PSExtensionMethod> miter = def.getMethods();

      String desc, name;
      // processing one method at a time
      while (miter.hasNext())
      {
         PSExtensionMethod method = miter.next();

         // collecting parameter names in the format of (p1, p2, ...)
         Iterator<PSExtensionMethodParam> piter = method.getParameters();
         StringBuffer paramNames = new StringBuffer();
         paramNames.append('(');
         boolean isFirstParam = true;
         while (piter.hasNext())
         {
            PSExtensionMethodParam param = piter.next();
            if (!isFirstParam)
               paramNames.append(", ");
            
            isFirstParam = false;
            paramNames.append(param.getName());
         }
         paramNames.append(')');
         
         name = startname + "." + method.getName() + paramNames.toString();
         desc = "[" + method.getReturnType() + "] " + method.getDescription();
         
         methods.add(new JexlMethod(name, desc));
      }

      return methods;
   }
   
   /**
    * Determines if the given method name is one of the methods specified in
    * {@link #NONE_JEXL_METHODS}.
    * 
    * @param name the name in question. Assumed not <code>null</code> or empty.
    * 
    * @return <code>true</code> if the name is prefixed with one of the
    *    element in {@link #NONE_JEXL_METHODS}.
    */
   private static boolean isNotJexlMethod(String name)
   {
      for (String prefix : NONE_JEXL_METHODS)
      {
         if (name.startsWith(prefix))
            return true;
      }
      return false;
   }
   
   /**
    * A complete a list of predefined methods and variables that can be used in
    * {@link PSJexlLocationGenerator}.
    */
   private static List<JexlMethod> ms_allMethodsVars = null;   
   
   /**
    * A list of prefix method names that are used in Template, but not in JEXL 
    * expression.
    */
   private static String[] NONE_JEXL_METHODS = new String[]
   {
      "$rx.asmhelper",
      "$rx.codec",
      "$rx.cond",  // deprecated method for JEXL expression.
      "$rx.db",
      "$rx.doc",
      "$rx.defaulttemplate",
      "$rx.i18n",
      "$rx.link",
      "$rx.keyword",
      "$rx.nav",
      "$rx.paginate",
      "$rx.session"
   };
   
}
