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
package com.percussion.controls.contenteditor.checkboxtree;

import java.util.Map;

/**
 * This interace allows users to extend existing functionality with extra
 * parameters that may be required for custom behavior.
 */
public interface IPSExtraParameters
{
   /**
    * Get the extra parameters.
    * 
    * @return the extra parameters, never <code>null</code>, may be empty.
    */
   public Map<String, String> getParameters();
   
   /**
    * Set new extra parameters.
    * 
    * @param parameters the extra parameters to set, may be <code>null</code> 
    *    or empty.
    */
   public void setParameters(Map<String, String> parameters);
}

