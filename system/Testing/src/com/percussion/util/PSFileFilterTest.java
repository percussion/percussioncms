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

package com.percussion.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


import com.percussion.utils.tools.PSPatternMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

}
