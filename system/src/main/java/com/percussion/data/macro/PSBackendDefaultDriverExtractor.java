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
import com.percussion.extension.services.PSDatabasePool;

/**
 * Macro extractor to get the database defaut driver name. 
 */
public class PSBackendDefaultDriverExtractor implements IPSMacroExtractor
{
   /**
    * This macro extractor extracts the driver name of the default backend
    * database. The name returned is always uppercased.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      PSDatabasePool dbPool = PSDatabasePool.getDatabasePool();
      return dbPool.getDefaultDriver().toUpperCase();
   }
}
