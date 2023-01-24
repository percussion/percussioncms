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
package com.percussion.pso.restservice.support;

import com.percussion.services.PSBaseServiceLocator;

/**
 */
public class ImportItemSystemInfoLocator extends PSBaseServiceLocator {
	
	   /**
	    * Gets the PSO Workflow Action Service bean. 
	   
	    * @return the PSO Workflow Action Service bean.  */
	   public static IImportItemSystemInfo getImportItemSystemInfo()
	   {
	      return (IImportItemSystemInfo) PSBaseServiceLocator.getBean(IMPORT_SYSTEM_INFO_BEAN); 
	   }
	   
	   /**
	    * Field IMPORT_SYSTEM_INFO_BEAN.
	    * (value is ""psoImportSystemInfo"")
	    */
	   public static final String IMPORT_SYSTEM_INFO_BEAN = "psoImportSystemInfo";
	}
