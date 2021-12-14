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
