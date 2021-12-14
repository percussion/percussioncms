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
