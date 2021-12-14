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
package com.percussion.rxfix;

import java.util.List;

/**
 * Each fixup module implements this interface.
 */
public interface IPSFix
{
   /**
    * Perform a fix on the specified installation of Rhythmyx, which references
    * the passed database definition by default. Any existing results are cleared
    * by calling this method
    * 
    * @param preview No actual changes should occur, just output the list of
    *           anticipated changes using log4j
    * @throws Exception if there is an error performing the fixup
    */
   void fix(boolean preview) throws Exception;
   
   /**
    * Recover results from the performed operations.
    * @return the results, may be empty but never <code>null</code>
    */
   List<PSFixResult> getResults();   
   
   /**
    * Get the operation that this module performs 
    * @return the operation, never <code>null</code> or empty
    */
   String getOperation();
}
