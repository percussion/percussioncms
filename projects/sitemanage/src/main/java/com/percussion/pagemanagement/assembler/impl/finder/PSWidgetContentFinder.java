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

package com.percussion.pagemanagement.assembler.impl.finder;

import com.percussion.pagemanagement.assembler.IPSWidgetContentFinder;
import com.percussion.pagemanagement.assembler.PSWidgetInstance;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.PSFilterException;
import org.springframework.transaction.annotation.Transactional;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 * The base widget content finder provides the common functionality needed by 
 * each widget content finder implementation. The general pattern is to 
 * implement the abstract method .
 * This method provides the information to a general implementation of the
 *  method.
 * 
 * @see PSContentFinderBase
 */
@Transactional(readOnly = true, noRollbackFor = Exception.class)
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
           throws RepositoryException, PSFilterException, PSAssemblyException, PSNotFoundException {
      return super.find(sourceItem, widget, params);
   }   
}
