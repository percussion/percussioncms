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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.lang.RandomStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.xml.PSXmlDocumentBuilder;

public class PSRevisionHistoryTest extends TestCase
{
   public PSRevisionHistoryTest(String name)
   {
      super(name);
   }

   public void setUp()
   {
   }

   /*
    * Test that the <CODE>PSRevisionHistory</CODE> constructor runs but does  
    * not create any Revision History entries.
    */   
   public void testConstructor() throws Exception
   {
      PSRevisionHistory hist = new PSRevisionHistory();
      // No Revision History Entries Have been created yet
      assertTrue(hist.getInitialRevision() == null);
      assertTrue(hist.getLatestRevision() == null);
   }

   /*
    * Test that the initial and latest values for the Major and Minor
    * Revisions are correct when revision history entries are set.
    */
   public void testSetRevision() throws Exception
   {
      int numEntries = 100;
      PSRevisionHistory hist = new PSRevisionHistory();

      /*
       * Test the result of making the first three entries individually
       * The Minor Version should start at 0 for the first entry,
       * and increment by one each time, so it should always be one
       * less than the number of entries made.
       * The Major Version should always be 0.
       */
      
      // Entry 1
      hist.setRevision(null, null, null);
      assertTrue(hist.getInitialRevision() != null);
      assertEquals(hist.getInitialRevision().getMinorVersion(), 0);
      assertTrue(hist.getLatestRevision() != null);
      assertEquals(hist.getLatestMinorVersion(), 0);

      // Entry 2
      hist.setRevision(null, null, null);
      assertTrue(hist.getInitialRevision() != null);
      assertEquals(hist.getInitialRevision().getMinorVersion(), 0);
      assertTrue(hist.getLatestRevision() != null);
      assertEquals(hist.getLatestMinorVersion(), 1);
            
      // Entry 3
      hist.setRevision(null, null, null);
      assertTrue(hist.getInitialRevision() != null);
      assertEquals(hist.getInitialRevision().getMinorVersion(), 0);
      assertTrue(hist.getLatestRevision() != null);
      assertEquals(hist.getLatestMinorVersion(), 2);
      
      // The Remaining Entries
      for (int i = 0; i < numEntries - 3; i++)
      {
         hist.setRevision(null, null, null);
      }
      assertTrue(hist.getInitialRevision() != null);
      assertEquals(hist.getInitialRevision().getMinorVersion(), 0);
      assertTrue(hist.getLatestRevision() != null);
      assertEquals(hist.getLatestMinorVersion(), numEntries - 1);
   }

   /*
    * Test that the revision history is unchanged when written to an XML
    * document and read back.
    */   
   public void testXml() throws Exception
   {
      int numEntries = 100;
      PSRevisionHistory hist = new PSRevisionHistory();

      for (int i = 0; i < numEntries; i++)
      {
         hist.setRevision(
            RandomStringUtils.randomAscii(99),
            RandomStringUtils.randomAscii(99),
            new Date());
      }

      // The usual test
      assertTrue(hist.getInitialRevision() != null);
      assertEquals(hist.getInitialRevision().getMinorVersion(), 0);
      assertTrue(hist.getLatestRevision() != null);
      assertEquals(hist.getLatestMinorVersion(), numEntries - 1);       

      /*
       * Now create an XML document, write the revision history and
       * read it back. It should be the same.
       */
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element el = hist.toXml(doc);
      doc.appendChild(el);

      PSRevisionHistory otherHist = new PSRevisionHistory();
      otherHist.fromXml(doc.getDocumentElement(), null, null);
      assertEquals(hist, otherHist);
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSRevisionHistoryTest("testConstructor"));
      suite.addTest(new PSRevisionHistoryTest("testSetRevision"));
      suite.addTest(new PSRevisionHistoryTest("testXml"));
      return suite;
   }
    
}
