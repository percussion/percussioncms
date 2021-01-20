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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSColumnInfo;
import com.percussion.rxverify.data.PSInstallation;
import com.percussion.rxverify.data.PSTableInfo;
import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableMetaData;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.Logger;
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
      Logger l = Logger.getLogger(getClass());
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
