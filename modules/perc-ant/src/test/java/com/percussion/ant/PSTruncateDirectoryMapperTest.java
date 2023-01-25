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
