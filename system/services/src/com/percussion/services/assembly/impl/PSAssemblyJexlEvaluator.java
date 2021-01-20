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
package com.percussion.services.assembly.impl;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Override service implementation to load bound functions
 * 
 * @author dougrand
 */
public class PSAssemblyJexlEvaluator extends PSServiceJexlEvaluatorBase
{
   private static Log ms_log1 = LogFactory.getLog(PSAssemblyJexlEvaluator.class);
   
   private static IPSScript ms_rx = null;
   private static IPSScript ms_user = null;
   
   /**
    * Default ctor, prebind necessary assembly methods here
    * 
    * @param work the work item passed to the assembly, never <code>null</code>
    */
   @SuppressWarnings("unchecked")
   public PSAssemblyJexlEvaluator(IPSAssemblyItem work) {
      super(false);
      
      if (work == null)
      {
         throw new IllegalArgumentException("work may not be null");
      }
      
      synchronized(PSAssemblyJexlEvaluator.class)
      {
         if (ms_rx == null)
         {
            try
            {
               ms_rx = PSJexlEvaluator.createExpression("$rx");
               ms_user = PSJexlEvaluator.createExpression("$user");
            }
            catch (Exception e)
            {
               ms_log1.error("problem creating expressions ", e);
            }
         }
      }
      
      /*
       * Copy initial data from any existing bindings
       */
      setValues(work.getBindings());

      /**
       * Setup function bindings
       */
      try
      {
         add("$rx", ms_rx, getJexlFunctions(SYS_CONTEXT));
         add("$user", ms_user, getJexlFunctions(USER_CONTEXT));
         bind("$tools", getVelocityToolBindings());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error setting up bindings", e);
      }
      
      Map<String, String[]> params = work.getParameters();

      bind("$sys.params", params);
   }

}
