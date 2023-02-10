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
package com.percussion.utils.jexl;

import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This is a helper class, provides convenient methods to catalog predefined 
 * JEXL variables, such as, <code>$sys...</code>
 *
 * @author yubingchen
 */
public class PSPredefinedJexlVariableDefs
{
   /**
    * Gets a list of predefined system variables definitions that can be used 
    * in a template as the value of binding variables.
    * 
    * @return the list described above. Each element contains 3 strings, they
    * are variable name, the type of the variable, and the description of the
    * variable. The list will not be <code>null</code> or empty.
    */
   public static List<String[]> getPredefinedTemplateVarDefs()
   {
      return ms_predefinedTemplateVarDefs;
   }

   /**
    * Gets a list of predefined system variables definitions that can be used 
    * in a JEXL expression that are used in a Location Scheme definition.
    * 
    * @return the list described above. Each element contains 3 strings, they
    * are variable name, the type of the variable, and the description of the
    * variable. The list will not be <code>null</code> or empty.
    */
   public static List<String[]> getPredefinedJexlVarDefs()
   {
      return ms_predefinedJexlVarDefs;
   }

   /**
    * Sort the given variable definitions by the name of the variables, which
    * is the 1st element of the String array.
    * 
    * @param varDefs the to be sorted variable definitions, assumed not 
    *    <code>null</code>.
    */
   private static void sortVarDefs(List<String[]> varDefs)
   {
      Collections.sort(varDefs, new Comparator<String[]>()
      {
         public int compare(String[] o1, String[] o2)
         {
            return o1[0].compareToIgnoreCase(o2[0]);
         }
      });
   }
   
   /**
    * Create variable definition for the given name and type. The description
    * of the definition will be retrieved from the resource bundle.
    * 
    * @param name the name of the variable definition, assumed not 
    *    <code>null</code> or empty.
    * @param type the Java type of the variable, assumed not <code>null</code>
    *    or empty.
    *    
    * @return 3 element array in the order of variable name, type and 
    *    description, never <code>null</code> or empty.
    */
   private static String[] getVarDef(String name, String type)
   {
      return new String[]{name, type, getString(name)};
   }
   
   /**
    * Retrieves the value of the given key from the underline resource bundle.
    * @param key the key, assumed not <code>null</code> or empty.
    * @return the value of the key, which is actually the description of the
    *    predefined variables.
    */
   private static String getString(String key)
   {
      return RESOURCE_BUNDLE.getString(key);
   }
   
   /**
    * The location or base name of the resource bundle, {@link #RESOURCE_BUNDLE}
    */
   private static final String BUNDLE_NAME = 
      "com.percussion.utils.jexl.PSPredefinedJexlVariableDefs";

   /**
    * The name of the resource bundle that contains description of the predefined system
    * variables.
    */
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle(BUNDLE_NAME);

   /**
    * See {@link #getPredefinedTemplateVarDefs()}. It is initialized when the
    * class is loaded.
    */
   private static List<String[]> ms_predefinedTemplateVarDefs;
   
   /**
    * See {@link #getPredefinedJexlVarDefs()}. It is initialized when the
    * class is loaded.
    */
   private static List<String[]> ms_predefinedJexlVarDefs;

   static
   {
      List<String[]> commVarDefs = new ArrayList<String[]>();
      commVarDefs.add(getVarDef("$sys.site.id", IPSGuid.class.getName()));
      commVarDefs.add(getVarDef("$sys.site.path", String.class.getName()));
      commVarDefs.add(getVarDef("$sys.site.url", String.class.getName()));
      commVarDefs.add(getVarDef("$sys.site.globalTemplate", String.class.getName()));
      commVarDefs.add(getVarDef("$sys.params", "java.util.Map<String,String[]>"));
      commVarDefs.add(getVarDef("$sys.item", "javax.jcr.Node"));
      commVarDefs.add(getVarDef("$sys.variables", "java.util.MapMap<String,Object>"));
      
      List<String[]> tmpVarDefs = new ArrayList<String[]>();
      tmpVarDefs.add(getVarDef("$sys.template", String.class.getName()));
      tmpVarDefs.add(getVarDef("$sys.mimetype", String.class.getName()));
      tmpVarDefs.add(getVarDef("$sys.charset", String.class.getName()));
      tmpVarDefs.add(getVarDef("$sys.activeAssembly", Boolean.class.getName()));
      tmpVarDefs.add(getVarDef("$sys.assemblyItem", "com.percussion.services.assembly.IPSAssemblyItem"));
      tmpVarDefs.add(getVarDef("$sys.index", Integer.class.getName()));
      tmpVarDefs.add(getVarDef("$sys.template", String.class.getName()));
      tmpVarDefs.addAll(commVarDefs);
      sortVarDefs(tmpVarDefs);
      
      ms_predefinedTemplateVarDefs = Collections.unmodifiableList(tmpVarDefs);

      List<String[]> jexlVarDefs = new ArrayList<String[]>();
      jexlVarDefs.add(getVarDef("$sys.page", Integer.class.getName()));
      jexlVarDefs.add(getVarDef("$sys.page_suffix", String.class.getName()));
      jexlVarDefs.add(getVarDef("$sys.crossSiteLink", Boolean.class.getName()));
      jexlVarDefs.add(getVarDef("$sys.template.prefix", String.class.getName()));
      jexlVarDefs.add(getVarDef("$sys.template.suffix", String.class.getName()));
      jexlVarDefs.add(getVarDef("$sys.path", String.class.getName()));
      jexlVarDefs.add(getVarDef("$sys.pub_path", String.class.getName()));
      jexlVarDefs.addAll(commVarDefs);
      sortVarDefs(jexlVarDefs);
      
      ms_predefinedJexlVarDefs = Collections.unmodifiableList(jexlVarDefs);      
   }

}
