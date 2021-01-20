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

package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;

/**
 * The PSObjectFactory abstract class provides mechanisms for creating
 * base objects, such as PSApplication objects. This is intended for use
 * by the com.percussion.design.objectstore.server package only.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSObjectFactory
{
   /**
    * Create an empty application object.
    */
   protected static PSApplication createApplication()
   {
      return createApplication(false);
   }

   /**
    * Create an empty application object.
    *
    * @param initialize if <code>true</code>, then some basic elements are
    * created and set on the app.  Currently these are:
    * <ol>
    * <li>An Acl with an anonymous entry that has full data access and a
    * default entry that has full data and design access</li>
    * </ol>
    *
    * @return the application.
    */
   protected static PSApplication createApplication(boolean initialize)
   {
      try
      {
         PSApplication app = new PSApplication();

         if (initialize)
         {
            PSAcl acl = new PSAcl();

            PSAclEntry anonymous = new PSAclEntry(
               PSAclEntry.ANONYMOUS_USER_NAME,
               PSAclEntry.ACE_TYPE_USER);

            // allow data access query/update by default
            anonymous.setAccessLevel(PSAclEntry.AACE_DATA_CREATE |
               PSAclEntry.AACE_DATA_DELETE |
               PSAclEntry.AACE_DATA_QUERY |
               PSAclEntry.AACE_DATA_UPDATE);

            PSAclEntry defaultEntry = new PSAclEntry(
               PSAclEntry.DEFAULT_USER_NAME,
               PSAclEntry.ACE_TYPE_USER);

            // allow data access AND design access query/update by default
            defaultEntry.setAccessLevel(PSAclEntry.AACE_DATA_CREATE |
               PSAclEntry.AACE_DATA_DELETE |
               PSAclEntry.AACE_DATA_QUERY |
               PSAclEntry.AACE_DATA_UPDATE |
               PSAclEntry.AACE_DESIGN_DELETE |
               PSAclEntry.AACE_DESIGN_MODIFY_ACL |
               PSAclEntry.AACE_DESIGN_READ |
               PSAclEntry.AACE_DESIGN_UPDATE);

            PSCollection entries = acl.getEntries();
            entries.clear();
            entries.add(anonymous);
            entries.add(defaultEntry);
            acl.setEntries(entries);
            app.setAcl(acl);
         }

         return app;
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Create an empty user configuration object for the specified user.
    */
   protected static PSUserConfiguration createUserConfiguration(
      String userName)
   {
      return new PSUserConfiguration(userName);
   }

   /**
    * Create an empty user configuration object.
    */
   protected static PSUserConfiguration createUserConfiguration()
   {
      return new PSUserConfiguration();
   }

   protected static PSServerConfiguration createServerConfiguration()
   {
      return new PSServerConfiguration();
   }

   protected static PSAcl createAcl()
   {
      return new PSAcl();
   }
}

