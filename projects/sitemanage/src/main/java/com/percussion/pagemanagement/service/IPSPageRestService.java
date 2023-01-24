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

package com.percussion.pagemanagement.service;

import com.percussion.pagemanagement.data.PSPage;

/**
 * The REST layer for page service.
 *   
 * @author YuBingChen
 */
public interface IPSPageRestService
{
   
   /**
    * Creates a page.
    *
    * @param page the new page info, not <code>null</code>.
    * 
    * @return the created page ID, not blank.
    */
   String create(PSPage page);
   
   /**
    * Loads the specified page.
    * 
    * @param id the ID of the page, not blank.
    * 
    * @return the loaded page, not <code>null</code>.
    */
   PSPage load(String id);
   
   void delete(String id);
}
