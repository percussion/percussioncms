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
package com.percussion.ant;

import com.percussion.utils.testing.UnitTest;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class PSTruncateDirectoryMapperTest extends TestCase
{
   public void testSetTo()
   {
      final PSTruncateDirectoryMapper mapper = new PSTruncateDirectoryMapper();
      mapper.setTo("abc/");
      mapper.setTo("123/");
      // is mandatory
      try
      {
         mapper.setTo(null);
         fail();
      }
      catch (BuildException success)
      {
      }

      try
      {
         mapper.setTo("  ");
         fail();
      }
      catch (BuildException success)
      {
      }
   }
   
   public void testMapFileName()
   {
      final PSTruncateDirectoryMapper mapper = new PSTruncateDirectoryMapper();

      // No match
      mapper.setTo("com/");
      assertNull(mapper.mapFileName("src\\org\\linux\\F.XML"));

      // Windows path
      mapper.setTo("com/");
      assertEquals("com\\percussion\\F.xml",
            mapper.mapFileName("src\\com\\percussion\\F.XML")[0]);
      mapper.setTo("com\\");
      assertEquals("com\\percussion\\F.xml",
            mapper.mapFileName("src\\com\\percussion\\F.XML")[0]);

      // Unix path
      mapper.setTo("com/");
      assertEquals("com/percussion/F.xml",
            mapper.mapFileName("src/com/percussion/F.XML")[0]);
      mapper.setTo("com\\");
      assertEquals("com/percussion/F.xml",
            mapper.mapFileName("src/com/percussion/F.XML")[0]);
   }
}
