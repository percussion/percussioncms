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
package com.percussion.services.contentchange;

import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;

/**
 * Handle changes to items in the system
 * 
 * @author JaySeletz
 *
 */
public interface IPSContentChangeHandler
{

   /**
    * Handle the supplied change to a content item
    * 
    * @param e The event, not <code>null</code>.
    */
   public void handleEvent(PSEditorChangeEvent e) throws PSDataServiceException, PSNotFoundException ;

   /**
    * Handle the supplied change to a relationship
    *  
    * @param e The event, not <code>null</code>.
    */
   public void handleEvent(PSRelationshipChangeEvent e) throws PSDataServiceException, PSNotFoundException;

}
