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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSColumnInfo;
import com.percussion.rxverify.data.PSInstallation;
import com.percussion.rxverify.data.PSTableInfo;
import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableMetaData;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.*;

/**
 * @author dougrand
 * 
 * Generates the list of database tables by reading
 * <code>sys_cmstableDef.xml</code>. Verifies by examining the database that
 * the installation is pointing at.
 */
public class PSVerifyDatabaseTables extends PSVerifyDatabaseBase
      implements
         IPSVerify
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#generate(java.io.File,
    *      com.percussion.rxverify.PSInstallation)
    */
   @Override
   public void generate(File rxdir, PSInstallation installation)
         throws Exception
   {
      File tableDef = new File(rxdir, "rxconfig/Server/sys_cmsTableDef.xml");
      Reader r = new FileReader(tableDef);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);

      // Get the elements we care about
      NodeList nodes = doc.getElementsByTagName("table");
      int len = nodes.getLength();
      for (int i = 0; i < len; i++)
      {
         Element e = (Element) nodes.item(i);
         PSTableInfo tableinfo = new PSTableInfo(e);
         installation.addTable(tableinfo);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#verify(com.percussion.rxverify.PSInstallation,
    *      java.io.File)
    */
   @Override
   public void verify(File rxdir, File originalRxDir,
         PSInstallation installation) throws Exception
   {
      checkColumns(rxdir, installation);
   }

   /**
    * Checks database columns for null and size and logs appropriate infomation.
    * 
    * @param rxdir the rhythmyx directory, must not be <code>null</code>
    * @param installation the information about an installation, must not be
    *           <code>null</code>
    * @throws Exception when there is a problem using the verification
    *            information
    */
   private void checkColumns(File rxdir, PSInstallation installation)
         throws Exception
   {
      Logger l = LogManager.getLogger(getClass());
      PSJdbcDbmsDef def = getDbmsDef(rxdir);
      Connection c = null;
      boolean hasErrors = false;

      l.info("Check database columns");

      try
      {
         c = def.getConnection();
         DatabaseMetaData dmd = c.getMetaData();
         PSJdbcDataTypeMap typemap = def.getTypemap();

         // Check each table for existence
         Iterator iter = installation.getTables().iterator();
         while (iter.hasNext())
         {
            PSTableInfo table = (PSTableInfo) iter.next();
            PSColumnInfo cols[] = table.getColumns();
            Map<String, PSColumnInfo> columnInfo = new HashMap<String, PSColumnInfo>();
            for (PSColumnInfo col : cols)
            {
               String colname = col.getName().toUpperCase();
               columnInfo.put(colname, col);
            }

            PSJdbcTableMetaData tableinfo = new PSJdbcTableMetaData(dmd, def,
                  typemap, table.getName());
            String tablename = table.getName().toUpperCase();

            hasErrors = false;

            if (tableinfo.exists() != false)
            {
               Set<String> columnNames = new HashSet<String>();
               Iterator ci = tableinfo.getColumns();
               while (ci.hasNext())
               {
                  PSJdbcColumnDef col = (PSJdbcColumnDef) ci.next();
                  String realColName = col.getName().toUpperCase();
                  columnNames.add(realColName);
                  PSColumnInfo colinfo = columnInfo
                        .get(realColName);
                  // Compare information
                  // Get database type to check
                  if (colinfo == null)
                  {
                     l.warn("Table " + tablename + " has unknown column "
                           + realColName);
                     continue;

                  }

                  // TODO: Should check data type here

                  if (colinfo.isNullable() != col.allowsNull())
                  {
                     l.error("Table " + tablename + " column " + realColName
                           + " doesn't agree with allows null from definition");
                     hasErrors = true;
                     continue;
                  }
                  long realSize = col.getSize() != null ? Long.parseLong(col
                        .getSize()) : 0;
                  if (colinfo.getSize() != 0 && colinfo.getSize() != realSize)
                  {
                     l.error("Table " + tablename + " column " + realColName
                           + " size does not match " + colinfo.getSize()
                           + " != " + col.getSize());
                     hasErrors = true;
                  }

                  // TODO: Will revisit data type check later,
                  // it will look something like this.
                  // Having problem matching DATE types.
                  /*
                   * short realType =
                   * PSSqlHelper.convertNativeDataType(def.getDataBase(),
                   * colinfo.getType(), (short) colinfo.getJdbcType());
                   * 
                   * if (realType != col.getType()) { l.error("Table " +
                   * tablename + " column " + realColName + " type does not
                   * match " + colinfo.getType() + " != " +
                   * col.getNativeType()); }
                   */
               }

               if (columnNames.equals(columnInfo.keySet()) == false)
               {
                  Set<String> missingColumns = new HashSet<String>(columnInfo.keySet());
                  missingColumns.removeAll(columnNames);
                  if (missingColumns.size() > 0)
                  {
                     l.error("Table " + tablename + " is missing column(s) "
                           + missingColumns);
                     hasErrors = true;
                  }
               }

            }

         }

         if (!hasErrors)
            l
                  .info("SUCCESS: All backend tables match sys_cmstableDef.xml for column");
         else
            l.info("FAILED: See error(s) above.");
      }
      finally
      {
         if (c != null)
            c.close();
      }

   }

}
