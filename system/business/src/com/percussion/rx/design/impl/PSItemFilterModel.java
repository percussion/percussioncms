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
package com.percussion.rx.design.impl;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.utils.guid.IPSGuid;

public class PSItemFilterModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid guid) throws PSNotFoundException {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSFilterService service = (IPSFilterService) getService();
      return service.loadUnmodifiableFilter(guid);
   }
   
   @Override
   public Object loadModifiable(IPSGuid guid) throws PSNotFoundException {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSFilterService service = (IPSFilterService) getService();
      return service.loadFilter(guid);
   }
   
   @Override
   public void delete(IPSGuid guid) throws PSNotFoundException {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      IPSItemFilter filter = (IPSItemFilter) load(guid);
      IPSFilterService service = (IPSFilterService) getService();
      service.deleteFilter(filter);
   }
   
}
