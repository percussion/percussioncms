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
