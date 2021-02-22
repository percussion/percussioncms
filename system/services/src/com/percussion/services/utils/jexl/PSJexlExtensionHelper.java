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
package com.percussion.services.utils.jexl;

import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionDef;
import com.percussion.extension.PSExtensionMethod;
import com.percussion.extension.PSExtensionMethodParam;
import com.percussion.extension.PSExtensionRef;
import com.percussion.server.PSServer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides convenient methods to catalog the definitions of 
 * utilities ($tools...) and RX extensions ($rx...), which implements JEXL 
 * interface,{@link PSServiceJexlEvaluatorBase#IPSJEXL_EXPRESSION}.
 */
public class PSJexlExtensionHelper
{
   protected static Log ms_log = LogFactory.getLog(PSJexlExtensionHelper.class);

   /**
    * Catalog all JEXL extensions from the extension manager.
    * 
    * @return the JEXL extensions. It is <code>null</code> if couldn't catalog
    *    the extensions.
    */
   @SuppressWarnings("unchecked")
   public static Collection<IPSExtensionDef> getJexlExtensionDefs()
   {
      List<IPSExtensionDef> defs = new ArrayList<>();
      IPSExtensionManager emgr = PSServer.getExtensionManager(null);
      try
      {
         Iterator<PSExtensionRef> refs;
         refs = emgr.getExtensionNames(null, null,
               PSServiceJexlEvaluatorBase.IPSJEXL_EXPRESSION, null);
         IPSExtensionDef def;
         while (refs.hasNext())
         {
            def = emgr.getExtensionDef(refs.next());
            defs.add(def);
         }
         
         return defs;
      }
      catch (PSException e)
      {
         ms_log.error("Couldn't initialize extensions information", e);
         return null;
      }
   }
   
   /**
    * Get the velocity tools and catalog their methods
    * 
    * @return the list of extension defs to return, never <code>null</code>
    */   
   @SuppressWarnings("unchecked")
   public static List<IPSExtensionDef> getVelocityTools()
   {
      List<IPSExtensionDef> rval = new ArrayList<>();
      PSServiceJexlEvaluatorBase jexl = new PSServiceJexlEvaluatorBase(true);
      // Grab the $tools
      Map<String, Object> tools = (Map<String, Object>) jexl.getVars().get("$tools");
      List<String> xfaces = new ArrayList<>();
      xfaces.add(PSServiceJexlEvaluatorBase.IPSJEXL_EXPRESSION);
      for (String key : tools.keySet())
      {
         Object o = tools.get(key);
         Class cofo = o.getClass();
         Method methods[] = cofo.getMethods();
         PSExtensionDef def = new PSExtensionDef();
         PSExtensionRef ref = new PSExtensionRef("jexl", "Java",
               PSServiceJexlEvaluatorBase.TOOLS_CONTEXT, key);
         def.setInterfaces(xfaces);
         def.setExtensionRef(ref);
         def.setInitParameter("className", cofo.getCanonicalName());
         def.setInitParameter("com.percussion.extension.reentrant", "yes");
         for (Method method : methods)
         {
            // Don't catalog trivial methods from Object, and we can't
            // catalog methods that have no return value
            if (method.getDeclaringClass().equals(Object.class)
                  || method.getReturnType() == null)
               continue;
            PSExtensionMethod extmethod = new PSExtensionMethod(method
                  .getName(), method.getReturnType().getCanonicalName());
            def.addExtensionMethod(extmethod);
            int p = 1;
            for (Class param : method.getParameterTypes())
            {
               String paramname = "p" + Integer.toString(p++);
               extmethod.addParameter(new PSExtensionMethodParam(paramname,
                     param.getCanonicalName()));
            }
         }
         rval.add(def);
      }
      return rval;
   }
}
