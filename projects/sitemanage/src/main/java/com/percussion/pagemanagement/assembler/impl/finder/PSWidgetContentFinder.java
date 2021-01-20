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

package com.percussion.pagemanagement.assembler.impl.finder;

import com.percussion.pagemanagement.assembler.IPSWidgetContentFinder;
import com.percussion.pagemanagement.assembler.PSWidgetInstance;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase;
import com.percussion.services.filter.PSFilterException;

import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

/**
 * The base widget content finder provides the common functionality needed by 
 * each widget content finder implementation. The general pattern is to 
 * implement the abstract method {@link #getContentItems(IPSAssemblyItem, long, Map)}.
 * This method provides the information to a general implementation of the
 * {@link #find(IPSAssemblyItem, Long, Map)} method.
 * 
 * @see PSContentFinderBase
 */
public abstract class PSWidgetContentFinder extends PSContentFinderBase<PSWidgetInstance>
   implements IPSWidgetContentFinder
{
   /**
    * Calculate the related items given the configuration of the content finder
    * instance. The returned items are ordered. Important information for the
    * rendering of the returned items will be contained in the variables bound
    * in each item, for example, the URL of the item for use in a snippet.
    * <p>
    * This method looks up the filter specified in the source item in the
    * <code>sys_itemfilter</code> parameter and then calls the "regular" find
    * method.
    * 
    * @param sourceItem the source content id, never <code>null</code> and
    *           must exist in the repository
    * @param widget the widget instance that the finder is being 
    *           invoked for, never <code>null</code>
    * @param params a set of zero or more parameters, never <code>null</code>.
    *           Standard parameter names are defined as constants on this
    *           interface.
    * @return an array of zero or more ordered related slot items, never
    *         <code>null</code>, but may be empty
    * @throws RepositoryException if the source content id does not exist or
    *            there is another error in running the content finder
    * @throws PSFilterException if the named filter is not found
    * @throws PSAssemblyException if a problem occurs with the assembly service
    */
   @Override
   public List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem,
           PSWidgetInstance widget, Map<String, Object> params)
         throws RepositoryException, PSFilterException, PSAssemblyException
   {
      return super.find(sourceItem, widget, params);
   }   
}
