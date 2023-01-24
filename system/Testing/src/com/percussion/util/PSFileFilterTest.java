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

package com.percussion.util;

import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.tools.PSPatternMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the PSFileFilter class
 * TODO: date and size testing
 */
public class PSFileFilterTest
{

   private static final Logger log = LogManager.getLogger(PSFileFilterTest.class);

   private File m_testDir;
   private String[] m_fileNames;
   private String[] m_dirNames;

   @Rule
   public TemporaryFolder temporaryFolder = new TemporaryFolder();

   public PSFileFilterTest(){}

   @Test
   public void testDefaultConstructor()
   {
      // test allow everything
      PSFileFilter filter;
      filter = new PSFileFilter();
      File[] files = m_testDir.listFiles((java.io.FileFilter)filter);
      assertEquals(m_fileNames.length + m_dirNames.length, files.length);
   }

   @Test
   public void testAttributes()
   {
      // only allow files
      PSFileFilter filt = new PSFileFilter(PSFileFilter.IS_FILE);
      File[] files = m_testDir.listFiles((java.io.FileFilter)filt);
      assertEquals(m_fileNames.length, files.length);

      // now only allow dirs
      filt = new PSFileFilter(PSFileFilter.IS_DIRECTORY);
      files = m_testDir.listFiles((java.io.FileFilter)filt);
      assertEquals(m_dirNames.length, files.length);

      // now only allow hidden files/dirs (there are none)
      filt = new PSFileFilter(PSFileFilter.IS_HIDDEN);
      files = m_testDir.listFiles((java.io.FileFilter)filt);
      assertEquals(0, files.length);

      // now allow everything again by bitwise ORing them together
      filt = new PSFileFilter(PSFileFilter.IS_HIDDEN | PSFileFilter.IS_FILE
         | PSFileFilter.IS_DIRECTORY);
      files = m_testDir.listFiles((java.io.FileFilter)filt);
      assertEquals(m_fileNames.length + m_dirNames.length, files.length);
   }

   @Test
   public void testNamePatterns()
   {
      // only allow files/dirs with an 'a' in their name
      // (every file has an 'a' in its name)
      PSFileFilter filt = new PSFileFilter(
         PSPatternMatcher.FileWildcardMatcher("*a*"));
      File[] files = m_testDir.listFiles((java.io.FileFilter)filt);
      assertEquals(m_fileNames.length + m_dirNames.length, files.length);

      // only allow files/dirs that start witg 'a' (there are 9 of them)
      filt = new PSFileFilter(
         PSPatternMatcher.FileWildcardMatcher("a*"));
      // PSFileFilter has a new attribute "IS_INCLUDE_ALL_DIRECTORIES" which
      // is set by default so all the directories are listed. To avoid this
      // set the attribute explicitly to not include the directories.
      filt.setAllowableAttributes(
         PSFileFilter.IS_DIRECTORY | PSFileFilter.IS_FILE | PSFileFilter.IS_HIDDEN);
      files = m_testDir.listFiles((java.io.FileFilter)filt);
      assertEquals(9, files.length);
   }

   @Before
   public void setUp()  {

      try
      {
      m_testDir = temporaryFolder.newFolder("PSFileFilterTest");

      m_dirNames = new String[] {
         "ad", "dda", "dad", "dada", "daad", "da", "adad"
      };

      m_fileNames = new String[] {
         "a", "aa", "aaa", "ab", "abba",
         "abbb", "ababa", "baba", "bab", "ba",
         "baa", "baab"
      };


         RandomAccessFile f;
         for (int i = 0; i < m_fileNames.length; i++)
         {
            f = new RandomAccessFile(new File(m_testDir, m_fileNames[i]), "rw");
            f.write(i);
            f.close();
         }
         for (int i = 0; i < m_dirNames.length; i++)
         {
            new File(m_testDir, m_dirNames[i]).mkdir();
         }
      }
      catch (IOException e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
   }

}
