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
package com.percussion.services.assembly;

import com.percussion.services.assembly.impl.PSAssemblyJexlEvaluator;
import com.percussion.services.filter.PSFilterException;

import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Assembles the given items into results for preview or publishing. The type of
 * the result is dependent on the configuration and parameters in the assembly
 * item. Note that any given item can appear multiple times in a site hierarchy,
 * which necessitates passing in the path of the item and not just the item's
 * content id.
 * <P>
 * Assembler plug-ins are configured as part of the extensions configuration.
 * The name of the assembly plugin referenced by the template is a fully
 * qualified extension name.
 * 
 * @author dougrand
 */
public interface IPSAssembler
{
   /**
    * For each item in the list of ids, create one or more assembly results. The
    * assembly services version of this simply dispatches to the correct,
    * registered assembler. The incoming parameters will tell the assembler
    * what, if any, site is in use. Note that the assembly items may be modified
    * by this call.
    * <p>
    * For reporting purposes, the assembly plugin must record the time (or an
    * approximation of the time) that it took for each item to be assembled
    * in the <code>elapsed</code> property of the underlying work item.
    * 
    * @param items items to be assembled, must have one or more elements.
    *           Instances must have been created with
    *           {@link IPSAssemblyService#createAssemblyItem(String, long, int, 
    *           IPSAssemblyTemplate, Map, Map, javax.jcr.Node, boolean)}
    * @return a list of {@link IPSAssemblyResult} values, generally one but may
    *         be more then one per item. Note that each result contains all the
    *         original information. Assembly result objects may share data with
    *         the item objects. Items that do not assemble successfully will
    *         still result in a result object, but the status will indicate an
    *         error. The order of the list is in the same order of the (above)
    *         input items.
    * @throws ItemNotFoundException if an item is missing from the repository
    * @throws RepositoryException if an error occurs loading data from the
    *            repository
    * @throws PSTemplateNotImplementedException if a passed template is not
    *            supported
    * @throws PSAssemblyException if there's a problem rendering the content
    * @throws PSFilterException if there's a problem finding or interpreting the
    *            item filter
    */
   List<IPSAssemblyResult> assemble(List<IPSAssemblyItem> items)
         throws ItemNotFoundException, RepositoryException,
         PSTemplateNotImplementedException, PSAssemblyException,
         PSFilterException;
   
   /**
    * The extension point for assembler to "inject" its specific information into the JEXL evaluator
    * before the evaluator is used to process bindings defined in item's template, which 
    * will be done by the assembler framework.
    *  
    * @param item the assembly item, not <code>null</code>.
    * @param eval the JEXL evaluator, created by the assembly framework, not <code>null</code>.
    * 
    * @throws PSAssemblyException if an error occurs.
    */
   void preProcessItemBinding(IPSAssemblyItem item, PSAssemblyJexlEvaluator eval) throws PSAssemblyException;
}
