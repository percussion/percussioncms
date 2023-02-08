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
package com.percussion.rx.publisher;

import java.util.List;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;

/**
 * 
 * Expands an assembly result into multiple assembly items that
 * are then added immediatly (front of the queue) to the current publishing job.
 * 
 * @author adamgent
 *
 */
public interface IPSAssemblyResultExpander
{

   /**
    * The parameter in {@link IPSAssemblyResult#getParameters()} that designates
    * the name of the expander to run.
    */
   public static final String ASSEMBLY_RESULT_EXPANDER_PARAM = "perc_expander";
   
   /**
    * The publisher handler will call this for assembly results
    * that are marked as paginate and have the parameter {@value #ASSEMBLY_RESULT_EXPANDER_PARAM}
    * set to this expander.
    * 
    * @param assemblyResult never <code>null</code>.
    * @return never <code>null</code>, maybe empty.
    * @throws Exception
    * 
    */
   public List<IPSAssemblyItem> expand(IPSAssemblyResult assemblyResult) throws Exception;
   
}
