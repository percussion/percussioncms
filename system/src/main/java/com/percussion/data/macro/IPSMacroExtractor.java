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
package com.percussion.data.macro;

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;

/**
 * This interface must be implemented for each macro extractor.
 */
public interface IPSMacroExtractor
{
   /**
    * Extract a data value using the run-time data.
    *
    * @param execData the execution data associated with this request. This 
    *    includes all context data, result sets, etc.
    * @return the associated value, may be <code>null</code>.
    * @throws PSDataExtractionException if an error condition causes the 
    *    extraction to fail. This is not thrown if the requested data does 
    *    not exist.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException;
}
