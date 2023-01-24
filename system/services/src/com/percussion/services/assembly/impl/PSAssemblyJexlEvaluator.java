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
package com.percussion.services.assembly.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static com.percussion.cms.IPSConstants.SYS_PARAM_PARAMS;
import static com.percussion.cms.IPSConstants.SYS_PARAM_RX;
import static com.percussion.cms.IPSConstants.SYS_PARAM_TOOLS;
import static com.percussion.cms.IPSConstants.SYS_PARAM_USER;

/**
 * Override service implementation to load bound functions
 * 
 * @author dougrand
 */
public class PSAssemblyJexlEvaluator extends PSServiceJexlEvaluatorBase
{
   private static final Logger ms_log1 = LogManager.getLogger(PSAssemblyJexlEvaluator.class);
   
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
               ms_rx = PSJexlEvaluator.createExpression(SYS_PARAM_RX);
               ms_user = PSJexlEvaluator.createExpression(SYS_PARAM_USER);
            }
            catch (Exception e)
            {
               ms_log1.error("Problem creating JEXL expressions. Error: {}",
                       PSExceptionUtils.getMessageForLog(e));
               ms_log1.debug(e);
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
         add(SYS_PARAM_RX, ms_rx, getJexlFunctions(SYS_CONTEXT));
         add(SYS_PARAM_USER, ms_user, getJexlFunctions(USER_CONTEXT));
         bind(SYS_PARAM_TOOLS, getVelocityToolBindings());
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error setting up bindings", e);
      }
      
      Map<String, String[]> params = work.getParameters();

      bind(SYS_PARAM_PARAMS, params);
   }

}
