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

import static com.percussion.security.PSSecurityProvider.SP_TYPE_DIRCONN;
import static com.percussion.security.PSSecurityProvider.SP_TYPE_BETABLE;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

public class PSGroupProviderInstanceTest extends TestCase
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   public void testEqualsHashCode()
   {
      final PSGroupProviderInstance providerInstance =
            createInstance(NAME, SP_TYPE_DIRCONN, CLASSNAME, ID);
      
      assertFalse(providerInstance.equals(new Object()));
      assertEqualsWithHash(providerInstance,
            createInstance(NAME, SP_TYPE_DIRCONN, CLASSNAME, ID));

      assertFalse(providerInstance.equals(
            createInstance(OTHER_STR, SP_TYPE_DIRCONN, CLASSNAME, ID)));
      assertFalse(providerInstance.equals(
            createInstance(NAME, SP_TYPE_BETABLE, CLASSNAME, ID)));
      assertFalse(providerInstance.equals(
            createInstance(NAME, SP_TYPE_DIRCONN, OTHER_STR, ID)));
      assertFalse(providerInstance.equals(
            createInstance(NAME, SP_TYPE_DIRCONN, CLASSNAME, ID + 1)));
}

   /**
    * Creates new provider instance initialized with provided parameters.
    */
   private PSGroupProviderInstance createInstance(final String name,
         final int type, final String className, final int id)
   {
      final PSGroupProviderInstance providerInstance =
         new PSGroupProviderInstance(name, type, className) {

         @Override
         protected Element toXmlEx(Document doc)
         {
            throw new AssertionError();
         }

         @Override
         protected void fromXmlEx(Element source)
         {
            throw new AssertionError();
         }};
      providerInstance.setId(id);
      return providerInstance;
   }

   /**
    * Sample name.
    */
   private static final String NAME = "Name";
   
   /**
    * Sample classname.
    */
   private static final String CLASSNAME = "Classname";

   /**
    * Sample id.
    */
   private static final int ID = 321;
   
   /**
    * Sample string.
    */
   private static final String OTHER_STR = "Other String";
}
