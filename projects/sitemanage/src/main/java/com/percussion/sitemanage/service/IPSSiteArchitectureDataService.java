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
package com.percussion.sitemanage.service;

import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteArchitecture;

/**
 * Site architecture data service class. This interface extends from
 * {@link IPSDataService}.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSSiteArchitectureDataService extends
      IPSDataService<PSSiteArchitecture, PSSiteArchitecture, String>
{
   /**
    * Returns the site site architecture object.
    */
   PSSiteArchitecture find(String id) throws PSValidationException, DataServiceLoadException,DataServiceNotFoundException;
}
