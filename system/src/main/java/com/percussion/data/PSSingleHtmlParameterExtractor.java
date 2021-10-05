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

package com.percussion.data;

import com.percussion.design.objectstore.PSSingleHtmlParameter;
import com.percussion.server.PSRequest;

/**
 * The PSSingleHtmlParameterExtractor class is used to extract data from the
 * HTML parameters associated with the request.  If the parameter is a list
 * of values, extracts the first value in the list.
 *
 */
public class PSSingleHtmlParameterExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param   source      the object defining the source of this value
    */
   public PSSingleHtmlParameterExtractor(PSSingleHtmlParameter source)
   {
      super(source);
      m_source = source.getName();
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @return               the associated value; <code>null</code> if a
    *                        value is not found
    */
   public Object extract(PSExecutionData data)
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param  execData  the execution data associated with this request.
    *                   This includes all context data, result sets, etc.
    *
    * @param   defValue    the default value to use if a value is not found. If
    *                      this is a List, the first value of the list is
    *                      returned.
    *
    * @return              the associated value; <code>defValue</code> if a
    *                      value is not found or if value is a List and it is
    *                      empty.  If the value (or defValue if value is
    *                      <code>null</code>)is a non-empty list, then the first
    *                      object in the list is returned.
    */
   public Object extract(PSExecutionData data, Object defValue)
   {
      Object value = defValue;

      if (data != null) {
         PSRequest request = data.getRequest();
         if (request != null)
            value = request.getSingleParameterObject(m_source, defValue);
      }

      return value;
   }

   /**
    * The name of the PSSingleHTMLParameter that is being extracted.
    */
   private String m_source;
}

