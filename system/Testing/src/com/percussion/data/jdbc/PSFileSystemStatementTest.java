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
package com.percussion.data.jdbc;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *   Unit tests for the PSFileSystemStatementTest class
 */
public class PSFileSystemStatementTest
{
   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   public static void main(String[] args)
   {
      StringBuffer buf = new StringBuffer(args.length * 10);
      for (int i = 0; i < args.length; i++)
      {
         buf.append(args[i]);
         buf.append(" ");
      }

      String query = buf.toString();
      System.err.print("Executing query: \"");
      System.err.print(query);
      System.err.println("\"");

      try
      {
         PSFileSystemStatement statement = new PSFileSystemStatement(null);
         ResultSet result = statement.executeQuery(query);
         int i = 0;
         while (result.next())
         {
            System.err.print(result.getLong("length") + " ");
            System.err.print(result.getString("modified") + " ");
            System.err.println("" + result.getString("fullname"));
            i++;
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }

   public PSFileSystemStatementTest()
   {
   }

   /**
    *   Set up the testing directories and files
    */
   @Before
   public void setUp() throws IOException {
      // make the testing directories
      m_rootDir = tempFolder.newFolder("Testing","PSFileSystemStatementTest");
      m_2Dir = new File(m_rootDir, "Dir2");
      m_3Dir = new File(m_2Dir, "Dir3");
      m_4Dir = new File(m_3Dir, "Dir4");

      m_2Files = new String[]
      {
         "To", "him", "who", "in", "the", "love", "of", "nature", "holds",
         "Communion", "with", "her", "visible", "forms", "she", "speaks",
         "a", "various", "language"
      };

      m_3Files = new String[]
      {
         "And", "eloquence", "of", "beauty", "she", "glides", "into",
         "his", "darker", "musings", "with", "a", "mild", "healing",
         "sympathy", "that", "steals", "away", "their", "sharpness",
         "ere", "he", "is", "aware"
      };

      m_4Files = new String[] 
      {
         "When", "thoughts", "of", "the", "last", "bitter", "hour",
         "come", "like", "a", "blight", "over", "thy", "spirit",
         "and", "sad", "images", "stern", "agony", "shroud",
         "pall", "breathless", "darkness", "narrow", "house"
      };

      m_totalNumFiles = 5+ m_2Files.length + m_3Files.length + m_4Files.length ;

      try
      {
         m_4Dir.mkdirs();

         RandomAccessFile file;
         File f;
         for (int i = 0; i < m_2Files.length; i++)
         {
            f = new File(m_2Dir, m_2Files[i]);
            file = new RandomAccessFile(f, "rw");
            file.write(i);
            file.close();
            f.deleteOnExit();
         }
         for (int i = 0; i < m_3Files.length; i++)
         {
            f = new File(m_3Dir, m_3Files[i]);
            file = new RandomAccessFile(f, "rw");
            file.write(i);
            file.close();
            f.deleteOnExit();
         }
         for (int i = 0; i < m_4Files.length; i++)
         {
            f = new File(m_4Dir, m_4Files[i]);
            file = new RandomAccessFile(f, "rw");
            file.write(i);
            file.close();
            f.deleteOnExit();
         }
      } catch (IOException e)
      {
         System.err.println(e);
      }
   }

   /**
    *   Test recursive file building
    */
   @Test
   public void testRecursive() throws Exception
   {
      String tempPath = tempFolder.getRoot().getAbsolutePath();
      PSFileSystemStatement statement = new PSFileSystemStatement(null);
      String sqlQuery =
         "SELECT name, fullname, path, modified, length FROM \""+ tempPath + "/*\"";
      ResultSet result = statement.executeQuery(sqlQuery);
      int i = 0;
      while (result.next())
      {
         System.err.print(result.getLong("length") + " ");
         System.err.print(result.getString("modified") + " ");
         System.err.println("" + result.getString("fullname"));
         i++;
      }

      // test the meta data and make sure the columns match
      ResultSetMetaData meta = result.getMetaData();
      assertTrue(meta != null);

      assertEquals("Column count", 5, meta.getColumnCount());

      assertEquals("Name column type should be VARCHAR",
         meta.getColumnType(1), java.sql.Types.VARCHAR);

      assertEquals("Fullname column type should be VARCHAR",
         meta.getColumnType(2), java.sql.Types.VARCHAR);

      assertEquals("Path column type should be VARCHAR",
         meta.getColumnType(3), java.sql.Types.VARCHAR);

      assertEquals("Modified column type should be VARCHAR",
         meta.getColumnType(4), java.sql.Types.VARCHAR);

      assertEquals("Length column type should be BIGINT",
         meta.getColumnType(5), java.sql.Types.BIGINT);

      assertEquals(m_totalNumFiles, i);

   }


   File m_rootDir;   // the root of the testing directory
   File m_2Dir;   // the second tier of the testing dir
   File m_3Dir;   // the third tier
   File m_4Dir;   // the fourth tier
   String[] m_2Files;
   String[] m_3Files;
   String[] m_4Files;
   int m_totalNumFiles ;
}
