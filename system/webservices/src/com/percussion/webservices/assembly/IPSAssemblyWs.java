/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

