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

package com.percussion.data.jdbc;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class PSFileSystemConnectionTest
{
   // the root dir for the tests
   private File m_rootDir;

   // the connection properties for the single root elem doc
   private Properties m_connProperties;

   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   @Before
   public void setUp()
   {

      try
      {
         m_rootDir = tempFolder.newFolder("Testing");
         m_connProperties = new Properties();
         m_rootDir.mkdirs();
         m_connProperties.setProperty("catalog", m_rootDir.getCanonicalPath());
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   @Test
   public void testOpen() throws Exception
   {
      // make sure the class loads itself and registers itself
      Class.forName("com.percussion.data.jdbc.PSFileSystemDriver");

      Connection fileConn = DriverManager.getConnection("jdbc:psfilesystem",
            m_connProperties);

      assertTrue("Connection initially open", !fileConn.isClosed());

      fileConn.close();

      assertTrue("Connection closes properly", fileConn.isClosed());
   }



}
