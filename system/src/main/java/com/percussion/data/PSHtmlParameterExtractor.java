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

import com.percussion.server.PSRequest;


/**
 * The PSHtmlParameterExtractor class is used to extract data from the
 * HTML parameters associated with the request.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSHtmlParameterExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param   source      the object defining the source of this value
    */
   public PSHtmlParameterExtractor(
      com.percussion.design.objectstore.PSHtmlParameter source)
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
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue      the default value to use if a value is not found
    *
    * @return               the associated value; <code>defValue</code> if a
    *                        value is not found
    */
   public Object extract(PSExecutionData data, Object defValue)
   {
      Object value = null;

      if (data != null) {
         PSRequest request = data.getRequest();
         if (request != null)
            value = request.getParameterObject(m_source, defValue);
      }

      return (value == null) ? defValue : value;
   }

   private String m_source;
}

