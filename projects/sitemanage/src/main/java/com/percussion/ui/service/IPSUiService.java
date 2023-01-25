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
package com.percussion.ui.service;

import com.percussion.ui.data.PSSimpleDisplayFormat;

/**
 * Service to interact with the servers ui components.
 * @author erikserating
 *
 */
public interface IPSUiService
{
   /**
    * Retrieve a display format by its internal name.
    * @param name the internal name of the display format to be found. May
    * be <code>null</code> or empty, in which case it will return the default
    * CMS display format.
    * @return the display format or <code>null</code> if not found.
    */
   public PSSimpleDisplayFormat getDisplayFormatByName(String name);
   
   /**
    * Retrieve the display format by its passed in id.
    * @param id the the display format id. 
    * @return the display format or <code>null</code> if not found.
    */
   public PSSimpleDisplayFormat getDisplayFormat(int id);
   
   
}
