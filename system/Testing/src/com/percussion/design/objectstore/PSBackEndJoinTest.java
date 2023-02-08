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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the PSBackEndJoin class.
 */
public class PSBackEndJoinTest extends TestCase
{
   public PSBackEndJoinTest(String name)
   {
      super(name);
   }

   public void testConstructor() throws Exception
   {
      PSBackEndTable leftTab = new PSBackEndTable("leftTable");
      PSBackEndColumn leftCol = new PSBackEndColumn(leftTab, "leftColumn");

      PSBackEndTable rightTab = new PSBackEndTable("rightTab");
      PSBackEndColumn rightCol = new PSBackEndColumn(rightTab, "rightColumn");

      PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);
      assertTrue(join.isInnerJoin());
      assertEquals(leftCol, join.getLeftColumn());
      assertEquals(rightCol, join.getRightColumn());

      PSBackEndJoin otherJoin = new PSBackEndJoin(leftCol, rightCol);
      assertEquals(join, otherJoin);

      otherJoin.setFullOuterJoin();
      assertTrue(otherJoin.isFullOuterJoin());

      otherJoin.setLeftOuterJoin();
      assertTrue(otherJoin.isLeftOuterJoin());

      otherJoin.setRightOuterJoin();
      assertTrue(otherJoin.isRightOuterJoin());

      assertTrue(!join.equals(otherJoin));
   }

   public void testXml() throws Exception
   {
      PSBackEndTable leftTab = new PSBackEndTable("leftTable");
      PSBackEndColumn leftCol = new PSBackEndColumn(leftTab, "leftColumn");

      PSBackEndTable rightTab = new PSBackEndTable("rightTab");
      PSBackEndColumn rightCol = new PSBackEndColumn(rightTab, "rightColumn");

      PSBackEndJoin join = new PSBackEndJoin(leftCol, rightCol);
      PSBackEndJoin otherJoin = new PSBackEndJoin();

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = join.toXml(doc);
      otherJoin.fromXml(el, null, null);
      assertEquals(join, otherJoin);

      join.setLeftOuterJoin();
      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = join.toXml(doc);
      otherJoin.fromXml(el, null, null);
      assertEquals(join, otherJoin);
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSBackEndJoinTest("testConstructor"));
      suite.addTest(new PSBackEndJoinTest("testXml"));
      return suite;
   }
}
