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
package com.percussion.services.assembly;

import com.percussion.extension.IPSExtension;
import com.percussion.services.filter.PSFilterException;

import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

/**
 * Content finders calculate what items are related to a particular content item.
 * Each finder is responsible for filtering the returned list of assembly items
 * for the filter passed into the find method. Information needed for a 
 * particular use of a content finder is passed to the
 * {@link #find(IPSAssemblyItem, Object, Map)} method
 * <p>
 * In general, the content finders are reusable across different contexts.
 * 
 * @author dougrand
 */
public interface IPSContentFinder<T extends Object> extends IPSExtension
{
   /**
    * The JSR-170 query to evaluate
    */
   static final String PARAM_QUERY = "query";

   /**
    * The type of the JSR-170 query
    */
   static final String PARAM_TYPE = "type";
   
   /**
    * The maximum number of results to return
    */
   static final String PARAM_MAX_RESULTS = "max_results";
   
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
    * @param target the target container object that the finder is being invoked
    *           for, never <code>null</code>
    * @param parameters a set of zero or more parameters, never <code>null</code>.
    *           Standard parameter names are defined as constants on this
    *           interface.
    * @return an array of zero or more ordered related slot items, never
    *         <code>null</code>, but may be empty
    * @throws RepositoryException if the source content id does not exist or
    *            there is another error in running the content finder
    * @throws PSFilterException if the named filter is not found
    * @throws PSAssemblyException if a problem occurs with the assembly service
    */
   List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem, T target,
         Map<String, Object> parameters) throws RepositoryException,
         PSFilterException, PSAssemblyException;
}
