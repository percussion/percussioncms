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

