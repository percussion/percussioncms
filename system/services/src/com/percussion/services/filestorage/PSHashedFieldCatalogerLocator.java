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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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