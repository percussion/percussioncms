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

