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

package com.percussion.server;

/**
* This class was previously used to check to see if the product has a valid brand code
* and if the product has timed out. (The real code is in PSServerBrand.prod
* We don't want to brand anymore, so this code will be left for anything still checking.
*/

public class PSServerBrand
{
   public PSServerBrand()
   {
   }

   /*just return true - PSServerBrand.prod has the real stuff and will be put
   in during manufacturing*/
   public boolean isValidCode()
   {
      return(true);
   }

   /*just return false - PSServerBrand.prod has the real stuff and will be put
   in during manufacturing*/
   public boolean hasTimedOut()
   {
      return(false);
   }

   /**
    * Always returns <code>true</code>.
    * @param componentId not used.
    * @return Always returns <code>true</code>.
    */
   public boolean isComponentLicensed(int componentId)
   {
      return true;
   }
}
