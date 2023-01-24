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
package com.percussion.services.assembly.impl.plugin;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * This assembler dispatches to another assembler based on field data or a
 * calculation made in the bindings.
 * 
 * @author dougrand
 * 
 */
public class PSDispatchAssembler extends PSAssemblerBase
{

   private static final IPSScript SYS_TEMPLATE = PSJexlEvaluator
         .createStaticExpression("$sys.template");

   /** (non-Javadoc)
    * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#assembleSingle(com.percussion.services.assembly.IPSAssemblyItem)
    */
   @Override
   public IPSAssemblyResult assembleSingle(IPSAssemblyItem item)
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      PSJexlEvaluator jexl = new PSJexlEvaluator(item.getBindings());
      Object subtemplate;
      IPSAssemblyTemplate savedTemplate = item.getTemplate();
      try
      {
         subtemplate = jexl.evaluate(SYS_TEMPLATE);
         if (subtemplate == null)
         {
            throw new IllegalArgumentException(
                  "$sys.template is required for the dispatch assembler");
         }
         IPSAssemblyTemplate template = null;
         if (subtemplate instanceof IPSAssemblyTemplate)
         {
            template = (IPSAssemblyTemplate) subtemplate;
         }
         else if (subtemplate instanceof Number)
         {
            IPSGuid tg = new PSGuid(PSTypeEnum.TEMPLATE, ((Number) subtemplate).longValue());
            template = asm.loadUnmodifiableTemplate(tg);
         }
         else if (subtemplate instanceof String)
         {
            template = asm.findTemplateByName((String) subtemplate);
         }
         if (template == null)
         {
            return getFailureResult(item, "could not find template information");
         }
         item.setTemplate(template);
         
         List<IPSAssemblyItem> items = new ArrayList<>();
         items.add(item);
         List<IPSAssemblyResult> results = asm.assemble(items);
         return results.get(0);
      }
      catch (Exception e)
      {
         return getFailureResult(item, e.getLocalizedMessage());
      }
      finally
      {
         item.setTemplate(savedTemplate);
      }
   }

}
