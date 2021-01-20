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
package com.percussion.cms;

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;

/**
 * A sequence of operations is used to build the XML document resulting from
 * a query. Since the document is composed mainly of fields which may or may
 * not be present depending on specified data values, a model was created
 * which creates each field as a single step in the build process. As the
 * field nodes are created, they are added to the IPSBuildContext object. If
 * the node is not included, the step is basically a no-op.
 * <p>The value associated with each field can be of several different types.
 * This is managed by having a different class for each type. Each class
 * implements this interface. All of the build steps are created at init time.
 * <p>After all steps have been executed, the final document is created using
 * the list of hidden and visible fields.
 *
 * @see IPSBuildContext
 */
public interface IPSBuildStep
{
   /**
    * Performs the sequence of operations needed for this step in the document
    * building process. Any data needed by following steps is stored in the
    * supplied context. The execution data may contain multiple result sets.
    * If a step needs its own result set, it should save the current state
    * of the exec data, pop the result set stack and set up the exec data
    * for this step. Before it returns, it must restore the exec data to the
    * previous state.
    *
    * @param ctx Information particular to this invocation. Never <code>null
    *    </code>.
    *
    * @param data The data associated with this request. Never <code>null
    *    </code>.
    *
    * @param isNewDoc A flag to indicate whether it is required to get data
    *    from the backend. If <code>true</code>, the field should be left
    *    blank if the extractor is a backend column (other extractors can
    *    be executed).
    *
    * @throws IllegalArgumentException If ctx or data is <code>null</code>.
    *
    * @throws PSDataExtractionException If a failure occurs while trying to
    *    process extractors.
    *
    * @see PSExecutionData
    */
   public void execute( IPSBuildContext ctx, PSExecutionData data, boolean
         isNewDoc )
      throws PSDataExtractionException;
}


