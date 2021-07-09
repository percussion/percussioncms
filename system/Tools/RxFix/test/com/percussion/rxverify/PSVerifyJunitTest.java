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
package com.percussion.rxverify;

import com.percussion.rxverify.data.PSFileInfo;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

/**
 * Unit tests for pieces of the verification application
 */
public class PSVerifyJunitTest extends TestCase
{
   /**
    * Ctor
    * @param name
    */
   public PSVerifyJunitTest(String name) {
      super(name);
   }
   
   /**
    * @return the suite
    */
   public static TestSuite suite()
   {
      return new TestSuite(PSVerifyJunitTest.class);
   }
   
   public void testFileInfo() throws Exception
   {
      File testFile = new File("build.xml");
      PSFileInfo fi1 = new PSFileInfo(testFile, "build.xml");
      PSFileInfo fi2 = new PSFileInfo(testFile, "build.xml");
      
      String x = fi1.toString();
      assertTrue(x.length() > 0);
      
      assertEquals(fi1, fi2);
      
      // Save and restore
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(fi1);
      oos.close();
      baos.close();
      
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bais);
      fi2 = (PSFileInfo) ois.readObject();
      
      assertEquals(fi1, fi2);      
   }

}
