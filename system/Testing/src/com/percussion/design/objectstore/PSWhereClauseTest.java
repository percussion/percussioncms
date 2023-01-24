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

import com.percussion.xml.PSXmlDocumentBuilder;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;


/**
 * Unit tests for the PSWhereClause class.
 */
public class PSWhereClauseTest
{

   @Test
   public void testXml() throws Exception
   {
      PSTextLiteral foo = new PSTextLiteral("foo");
      PSTextLiteral bar = new PSTextLiteral("bar");

      PSWhereClause clause = new PSWhereClause(foo, "=", bar, true);
      PSWhereClause otherClause = new PSWhereClause();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = clause.toXml(doc);
      otherClause.fromXml(el, null, null);
      assertEquals(clause, otherClause);
   }

}
