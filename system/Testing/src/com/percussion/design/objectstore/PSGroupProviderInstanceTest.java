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

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.percussion.security.PSSecurityProvider.SP_TYPE_BETABLE;
import static com.percussion.security.PSSecurityProvider.SP_TYPE_DIRCONN;
import static com.percussion.testing.PSTestCompare.assertEqualsWithHash;
import static org.junit.Assert.assertNotEquals;


public class PSGroupProviderInstanceTest
{
   /**
    * Tests behavior of equals() and hashCode() methods.
    */
   @Test
   public void testEqualsHashCode()
   {
      final PSGroupProviderInstance providerInstance =
            createInstance(NAME, SP_TYPE_DIRCONN, CLASSNAME, ID);

       assertNotEquals(providerInstance, new Object());
      assertEqualsWithHash(providerInstance,
            createInstance(NAME, SP_TYPE_DIRCONN, CLASSNAME, ID));

      PSGroupProviderInstance t2 = createInstance(OTHER_STR, SP_TYPE_DIRCONN, CLASSNAME, ID);
       assertNotEquals(providerInstance, t2);

       assertNotEquals(providerInstance, createInstance(NAME, SP_TYPE_BETABLE, CLASSNAME, ID));
       assertNotEquals(providerInstance, createInstance(NAME, SP_TYPE_DIRCONN, OTHER_STR, ID));
       assertNotEquals(providerInstance, createInstance(NAME, SP_TYPE_DIRCONN, CLASSNAME, ID + 1));
}

   /**
    * Creates new provider instance initialized with provided parameters.
    */
   private PSGroupProviderInstance createInstance( String name,
          int type,  String className,  int id)
   {
       PSGroupProviderInstance providerInstance =
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
