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
package com.percussion.rx.config.test;

import com.percussion.rx.config.impl.PSConfigDefGenerator;
import com.percussion.utils.tools.PSParseFragments;
import junit.framework.TestCase;

import java.util.Map;

public class PSParseFragmentsTest extends TestCase
{
   public void testAll() throws Exception
   {
      PSConfigDefGenerator gen = PSConfigDefGenerator.getInstance();
      String content = gen.getFragementFileContents();
      
      Map<String, String> frags = PSParseFragments.parseContent(content);
      assertTrue("Must have more than one fragments", frags.size() > 1);
      
      assertTrue("Must have XMLHEAD", frags.get("XMLHEAD") != null);
      assertTrue("Must have SLOT", frags.get("SLOT") != null);
   }
   
   public void testParse() throws Exception
   {
      String text = "1st\n2nd line\r\n3rd line";
      String[] lines = PSParseFragments.splitByNewlines(text);
      for (String line : lines)
      {
         char ch = line.charAt(line.length()-1);
         assertTrue(ch != '\r' && ch != '\n');
      }
      assertTrue(lines[0].equals("1st"));
      assertTrue(lines[1].equals("2nd line"));
      assertTrue(lines[2].equals("3rd line"));
   }
}
