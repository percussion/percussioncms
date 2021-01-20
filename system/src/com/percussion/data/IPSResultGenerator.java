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

package com.percussion.data;



/**
 * The IPSResultGenerator interface must be implemented by any classes
 * capable of producing output (results) for an E2 request.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSResultGenerator {
   /**
    * Generate the results for this request.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @exception   PSConversionException
    *                        if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *                      if conversion to the format required by the
    *                      specified request URL is not supported
    */
   public void generateResults( PSExecutionData data)
      throws PSConversionException, PSUnsupportedConversionException;
}

