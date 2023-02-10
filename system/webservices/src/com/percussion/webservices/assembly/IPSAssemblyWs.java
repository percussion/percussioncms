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
package com.percussion.webservices.assembly;

import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;

import java.util.List;

/**
 * This interface defines all assembly related webservices.
 */
public interface IPSAssemblyWs
{
   /**
    * Loads all slots for the supplied name in read-only mode.
    * 
    * @param name the name of the slot to load, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. If not 
    *    supplied or empty, all template slots will be returned. 
    * @return a list with all loaded slots in read-only mode, 
    *    never <code>null</code>, may be empty, alpha ordered by name.
    */
   public List<IPSTemplateSlot> loadSlots(String name);
   
   /**
    * Loads all assembly templates for the supplied parameters in read-only 
    * mode.
    * 
    * @param name the name of the assembly template to load, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. All 
    *    assembly templates will be loaded if not supplied or empty.
    * @param contentType the content type name for which to load the assembly 
    *    templates, may be <code>null</code> or empty, asterisk wildcards are 
    *    accepted. All assembly templates will be loaded if not supplied 
    *    or empty.
    * @return a list with all loaded assembly templates in read-only mode, 
    *    never <code>null</code>, may be empty, alpha ordered by name.
    */
   public List<PSAssemblyTemplateWs> loadAssemblyTemplates(String name, 
      String contentType);
}

