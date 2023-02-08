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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.percussion.cms.IPSConstants.SYS_PARAM_SYSTEM;
import static com.percussion.cms.IPSConstants.SYS_PARAM_USER;

/**
 * Helper class loads bound jexl functions by reading them from the extensions
 * manager.
 * 
 * @author dougrand
 *
 */
public class PSJexlHelper extends PSServiceJexlEvaluatorBase
{
   
   /**
    * Default ctor
    */
   public PSJexlHelper() {
      super(false);
      initExtensionNamesSet();
   }
   
   /**
    * Top level method to extract the extension names given a JEXL expression
    * This method will extract a hashmap of extension names based on $sys or 
    * $user variable
    * @param exp the jexl expression, cannot not be <code>null</code> or empty
    * @return the extensions map, never <code>null</code>
    */
   public static HashMap<String, List<String>> getExtensionsFromBindings(
         String exp)
   {
      HashMap<String, List<String>> bindingsMap = 
         new HashMap<>();
      bindingsMap.put(SYS,  parseExpression(SYS_PREFIX, exp));
      bindingsMap.put(USER, parseExpression(USER_PREFIX, exp));
      return bindingsMap;
   }
   
   /**
    * Match a pattern such as "$sys.*.*(" like in 
    * "$sys.guid.getContentId($sys.assemblyitem.Id)"
    * @param exp
    * @return a list of string parts from the match
    */
   private static List<String> parseExpression( int prefix, String exp)
   {
      if (exp == null || exp.trim().length() == 0)
         throw new IllegalArgumentException(
               "JEXL expression may not be null or empty");
      
      List<String> extNames = new ArrayList<>();
      // search for pattern such as "$sys.*.*("
      Pattern p = Pattern.compile((prefix==SYS_PREFIX?SYS_PATTERN:USER_PATTERN));
      Matcher m = p.matcher(exp.toLowerCase());
      int index = 0;
      while ( m.find(index) )
      {
         String substr = m.group();
         StringTokenizer tok = new StringTokenizer(substr, ".");
         if (tok.countTokens() > 2) 
         {
            tok.nextToken();
            extNames.add(tok.nextToken());
         }
         index = m.end();
      }      
      return extNames;
   }

   /**
    * Given an extension name, get the extension ref
    * @param name cannot be <code>null</code> or empty
    * @return the extension ref for the given name
    * @throws PSExtensionException
    */
   @SuppressWarnings("unchecked")
   public PSExtensionRef getExtensionRef(String name)
         throws PSExtensionException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
               "Extension name may not be null or empty");
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      Iterator<PSExtensionRef> refs;
      boolean found = false;
      refs = emgr.getExtensionNames(null, null, IPSJEXL_EXPRESSION, null);
      PSExtensionRef ref = null;
      while (refs.hasNext() && !found)
      {
         ref = refs.next();
         if (ref.getExtensionName().equals(name))
         {
            found = true;
            break;
         }
      }
      return found == true ? ref : null;
   }
   
   /**
    * Init a map of jexl extensions
    *
    */
   private void initExtensionNamesSet()
   {
      m_sysFuncs  = getJexlFunctions(SYS_CONTEXT);
      Set<String> skeys = m_sysFuncs.keySet();
      for (String key : skeys)
         m_extNameSet.add(SYS + "." + key);
      m_userFuncs = getJexlFunctions(USER_CONTEXT);
      Set<String> ukeys = m_userFuncs.keySet();
      for (String key: ukeys)
         m_extNameSet.add(USER + "." + key);    
   }
   
   /**
    * A holder of extension names such as "$sys.codec", "$user.myext" etc
    */
   private HashSet<String> m_extNameSet   = new HashSet<>();
   
   /**
    * Place holder for system Extensions that are used for JEXL context
    */
   private Map<String, Object>m_sysFuncs  = null;
   
   /**
    * Place holder for system Extensions that are used for JEXL context
    */

   private Map<String, Object>m_userFuncs = null;
   
   /**
    * Prefix for system extensions: JEXL expr has something like this:
    * $sys.codec etc
    */
   public static final String SYS  = SYS_PARAM_SYSTEM;

   /**
    * Prefix for system extensions: JEXL expr has something like this:
    * $sys.codec etc
    */
   public static final String USER = SYS_PARAM_USER;
   
   /**
    * Escape pattern for the regular expression
    */
   private static final String ESCAPE = "\\";
   
   /**
    * The pattern: any "foo.bar("
    */
   private static final String PATTERN = "[a-z]+?.[a-z]+?\\(";
   
   private static final String SYS_PATTERN  = ESCAPE + SYS + "." + PATTERN;   
   private static final String USER_PATTERN = ESCAPE + USER + "." + PATTERN;
   private static final int SYS_PREFIX  = 1;
   private static final int USER_PREFIX = 2;
}
