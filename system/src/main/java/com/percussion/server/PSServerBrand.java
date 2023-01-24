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
