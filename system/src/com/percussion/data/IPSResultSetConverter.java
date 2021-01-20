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
 * The IPSResultSetConverter interface must be implemented by any classes
 * capable of converting a result set to an alternative data format
 * (such as XML or HTML).
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSResultSetConverter extends IPSResultGenerator
{
   /**
    * Result set converters take the data which was extracted from the
    * back-end and generate the appropriate output from it. This includes
    * mapping the back-end columns to XML and applying any style sheets.
    *
    * @param   data          the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param filter If supplied, will be applied to every row in the result
    *    set. Only those rows that are accepted by this filter will be
    *    included in the resulting document. May be <code>null</code>.
    *
    * @exception   PSConversionException
    *                              if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *                      if conversion to the format required by the
    *                      specified request URL is not supported
    */
   public abstract void convert(PSExecutionData data,
         IPSResultSetDataFilter filter)
      throws PSConversionException, PSUnsupportedConversionException;

   /**
    * What is the default MIME type for this converter?
    *
    * @return               the default MIME type
    */
   public abstract String getDefaultMimeType();

   /**
    * Evaluate any result page conditionals to determine the index of
    * the result page to use.
    *
    * @param   data         the execution data associated with this request.
    *                              This includes all context data, result sets, etc.
    *
    * @return               the 0-based index of the result page or -1
    */
   public abstract int getResultPageIndex(PSExecutionData data);


   /* ****************  IPSResultGenerator Implementation **************** */

   /**
    * Generate the results for this request.
    *
    * @param   data         the execution data associated with this request.
    *                              This includes all context data, result sets, etc.
    *
    * @exception   PSConversionException
    *                              if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *                              if conversion to the format required by the
    *                              specified request URL is not supported
    */
   public abstract void generateResults(PSExecutionData data)
      throws PSConversionException, PSUnsupportedConversionException;
}

