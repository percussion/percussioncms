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

package com.percussion.services.filestorage;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Locator for the Cataloger servivice.  This service
 * helps to locate and store a record of database columns containing 
 * references to binaries by hash.  This is used to make sure we can
 * accurately and safely remove unused binaries.  We have to be careful as we do
 * not want an error to cause us to miss a reference and then remove
 * more binaries that we should.
 * @author stephenbolton
 *
 */
public class PSHashedFieldCatalogerLocator extends PSBaseServiceLocator {

   private static volatile IPSHashedFieldCataloger hfc = null;
   /**
    * @return the singleton bean instance
    */
   public static IPSHashedFieldCataloger getHashedFileCatalogerService()
   {
       if (hfc==null)
       {
           synchronized (PSHashedFieldCatalogerLocator.class)
           {
               if (hfc==null)
               {
                   hfc = (IPSHashedFieldCataloger) PSBaseServiceLocator.getBean(
                           HASHED_FIELD_CATALOGER_BEAN);
               }
           }
       }
      return hfc;
   }

   public static final String HASHED_FIELD_CATALOGER_BEAN = "sys_hashedFieldCatalogerService";
}
