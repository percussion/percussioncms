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

