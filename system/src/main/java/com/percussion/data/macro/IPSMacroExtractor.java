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
