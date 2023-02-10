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
package com.percussion.tablefactory;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit test for PSJdbcForeignKey.
 */
public class PSJdbcForeignKeyTest
{
   public PSJdbcForeignKeyTest(){
   }

   /**
    * Test the def
    */
   @Test
   public void testDef() throws Exception
   {
      PSJdbcForeignKey fk = new PSJdbcForeignKey("col1", "extTable", "extCol1",
         PSJdbcTableComponent.ACTION_REPLACE);
      fk.addColumn("col2", "extTable2", "extCol2");
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = fk.toXml(doc);
      PSJdbcForeignKey fk2 = new PSJdbcForeignKey(el);
      assertEquals(fk, fk2);

      //Now ForeignKeys are different incase their components are different,
      // if name is different and components are same, they are considered same
      fk2.setName("FK_SOME_OTHER_NAME_1");
      assertEquals("Should not be equal", fk, fk2);

      fk2.addColumn("col3", "extTable3", "extCol3");
      assertNotEquals("Should not be equal", fk, fk2);

   }


}
