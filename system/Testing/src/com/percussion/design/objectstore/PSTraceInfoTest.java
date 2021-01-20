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

import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 *   Unit tests for the PSTraceInfo class
 */
public class PSTraceInfoTest
{

   public PSTraceInfoTest()
   {

   }

   @org.junit.Test
   public void testXml() throws Exception
   {
      // assert that two empty traceinfo objects are equal
      PSTraceInfo info = new PSTraceInfo();
      PSTraceInfo fromXmlInfo = new PSTraceInfo();
      assertEquals(info, fromXmlInfo);


      // modify one and assert they are not equal
      info.setTraceEnabled(true);
      assertTrue(!info.equals(fromXmlInfo));

      // construct one from the other and assert they are still equal
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = info.toXml(doc);
      doc.appendChild(el);
      fromXmlInfo.fromXml(el, null, null);
      assertEquals(info, fromXmlInfo);

      // repeat in the other direction
      info.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, false);
      info.setTimeStampOnlyTrace(false);
      info.setTraceEnabled(false);

      fromXmlInfo.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, true);
      fromXmlInfo.setColumnWidth(info.getColumnWidth() + 1);
      info.setTimeStampOnlyTrace(true);
      info.setTraceEnabled(true);
      assertFalse(info.equals(fromXmlInfo));

      doc = PSXmlDocumentBuilder.createXmlDocument();
      el = fromXmlInfo.toXml(doc);
      doc.appendChild(el);
      info.fromXml(el, null, null);
      assertEquals(info, fromXmlInfo);

   }

   @Test
   public void testFlags() throws Exception
   {
      // set flags on separate instances
      PSTraceInfo info1 = new PSTraceInfo();
      PSTraceInfo info2 = new PSTraceInfo();
      assertEquals(info1, info2);

      info1.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, true);
      info1.setTraceEnabled(PSTraceMessageFactory.RESULT_SET, true);
      info2.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, false);
      info2.setTraceEnabled(PSTraceMessageFactory.RESULT_SET, false);
      assertFalse(info1.equals(info2));

      info2.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, true);
      info2.setTraceEnabled(PSTraceMessageFactory.RESULT_SET, true);
      assertEquals(info1, info2);

      // test composite flags
      info2.setTraceEnabled(true);
      info2.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, false);
      info2.setTraceEnabled(PSTraceMessageFactory.RESULT_SET, false);
      assertTrue(!info1.equals(info2));
      info1.setTraceOptionsFlag(info2.getTraceOptionsFlag());
      assertEquals(info1, info2);

   }

   @Test
   public void testConstructors() throws Exception
   {
      // test with and without locale
      PSTraceInfo info1 = new PSTraceInfo();
      PSTraceInfo info2 = new PSTraceInfo(Locale.getDefault());
      assertEquals(info1, info2);

      // test copyfrom
      info2.copyFrom(info1);
      assertEquals(info1, info2);

      info2 = new PSTraceInfo(Locale.JAPANESE);
      assertEquals(info1, info2);

      // test with and without xml
      info1 = new PSTraceInfo();
      info1.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, true);
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = info1.toXml(doc);
      doc.appendChild(el);

      info2 = new PSTraceInfo(el, null, null);
      assertEquals(info1, info2);

      // test with and without xml and locale
      info2 = new PSTraceInfo(el, null, null, Locale.getDefault());
      assertEquals(info1, info2);

      info2 = new PSTraceInfo(el, null, null, Locale.KOREA);
      assertTrue(info1.equals(info2));

      // test copyfrom
      info2.copyFrom(info1);
      assertTrue(info1.equals(info2));

   }

   @Test
   public void testDefaultFlags() throws Exception
   {
      // create flags
      PSTraceInfo info1 = new PSTraceInfo();
      PSTraceInfo info2 = new PSTraceInfo();
      // make a copy of the original
      info2.copyFrom(info1);
      assertEquals(info1, info2);

      // change the original
      info1.setTraceEnabled(PSTraceMessageFactory.SESSION_INFO_FLAG, true);
      info1.setTraceEnabled(PSTraceMessageFactory.RESULT_SET, true);
      assertFalse(info1.equals(info2));

      // now make sure the default options match
      assertEquals(info2.getTraceOptionsFlag(), info1.getInitialOptionsFlag());

      // test it with xml
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element el = info1.toXml(doc);
      doc.appendChild(el);
      info2 = new PSTraceInfo(el, null, null);
      assertEquals(info2.getInitialOptionsFlag(), info1.getTraceOptionsFlag());
   }

}
