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
package com.percussion.services.contentmgr.ui;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.contentmgr.impl.legacy.PSContentRepository;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.naming.NamingException;

/**
 * This class contains the methods needed to fix a given content item
 * 
 * @author dougrand
 */
public class PSConsistencyFixer extends PSConsistencyBase
{
   /**
    * Ctor
    * 
    * @throws SQLException see {@link PSConnectionHelper#getConnectionDetail()}
    *            for details
    * @throws NamingException see
    *            {@link PSConnectionHelper#getConnectionDetail()} for details
    */
   public PSConsistencyFixer() throws NamingException, SQLException {
      // No init
   }

   /**
    * Fix one given problem
    * 
    * @param p the problem to fix, never <code>null</code>
    * @throws SQLException see {@link PSConnectionHelper#getConnectionDetail()}
    *            for details
    * @throws NamingException see
    *            {@link PSConnectionHelper#getConnectionDetail()} for details
    */
   public void fix(Problem p) throws NamingException, SQLException
   {
      if (p == null)
      {
         throw new IllegalArgumentException("p may not be null");
      }

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();

      int contentid = p.getContentid();
      PSComponentSummary sum = cms.loadComponentSummary(contentid);
      PSTypeConfiguration config = PSContentRepository
            .getTypeConfiguration((int) sum.getContentTypeId());

      for (int rev : p.getMissingRevisions())
      {
         fix(contentid, rev, config);
      }
   }

   /**
    * Fix a given missing revision. This clones the data in each table and child
    * table for the given missing revision. We start by cloning the parent data,
    * then work on the child data. For children, we first check to see if there
    * is any data for the missing revision and only fill in if there is no data
    * for the missing revision
    * 
    * @param contentid the missing content id
    * @param rev the missing revision
    * @param config the type configuration, used to determine what tables are
    *           involved, assumed not <code>null</code>
    * @throws SQLException
    * @throws NamingException
    */
   private void fix(int contentid, int rev, PSTypeConfiguration config)
         throws NamingException, SQLException
   {
      for (String table : config.getTableNames())
      {
         if (table.equalsIgnoreCase("CONTENTSTATUS")) continue;
         fixTable(contentid, rev, table);
      }

      for (PSTypeConfiguration child : config.getChildren())
      {
         for (String childtable : child.getTableNames())
         {
            fixTable(contentid, rev, childtable);
         }
      }

      for (String table : config.getSimpleChildPropertiesTables())
      {
         fixTable(contentid, rev, table);
      }
   }

   /**
    * Fixing the table consists of first checking to see if there are any
    * records available for the given revision, which allows us to leave 
    * the main tables alone that have the records, but also allows us to know
    * if there are any child records available before inserting new ones.
    * @param contentid the id of the item being fixed
    * @param rev the revision of the item being fixed, this will iterate from
    * low to high, so this method can assume that prior revisions are present
    * @param table the table involved, assumed not <code>null</code> or empty
    * @throws NamingException problems looking up the connection
    * @throws SQLException problems with the JDBC calls may throw this
    */
   private void fixTable(int contentid, int rev, String table)
         throws NamingException, SQLException
   {
      Connection c = PSConnectionHelper.getDbConnection();
      String qualifiedname = getTableName(table);
      String query = "SELECT * FROM " + qualifiedname
            + " WHERE CONTENTID = ? AND REVISIONID = ?";
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      
      try
      {
         StringBuilder insert = new StringBuilder();
         PreparedStatement qst = c.prepareStatement(query);
         qst.setInt(1, contentid);
         qst.setInt(2, rev);
         ResultSet qrs = qst.executeQuery();
         if (qrs.next()) return;
         
         qst.setInt(2, rev - 1);
         qrs = qst.executeQuery();
         
         int sysid = -1;
         int revcol = -1;
         insert.append("INSERT INTO ");
         insert.append(table);
         while(qrs.next())
         {
            ResultSetMetaData rsmd = qrs.getMetaData();
            
            // Check for SYSID and also build the values list for the insert
            if (sysid < 0)
            {
               StringBuilder values = new StringBuilder();
               insert.append(" (");
               for(int i = 1; i <= rsmd.getColumnCount(); i++)
               {
                  String column = rsmd.getColumnName(i);
                  if (i > 1)
                  {
                     values.append(',');
                     insert.append(',');
                  }
                  values.append('?');
                  insert.append(column);
                  if (column.equalsIgnoreCase("SYSID"))
                  {
                     sysid = i;
                  } 
                  else if (column.equalsIgnoreCase("REVISIONID"))
                  {
                     revcol = i;
                  }
               }
               insert.append(") VALUES (");
               insert.append(values);
               insert.append(")");
               if (sysid < 0) sysid = 0;
            }
            
            PreparedStatement irs = c.prepareStatement(insert.toString());
            
            for(int i = 1; i <= rsmd.getColumnCount(); i++)
            {
               // Copy the data into an insert row
               if (i == sysid)
               {
                  int sid[] = gmgr.createIdBlock(table, 1);
                  irs.setInt(i, sid[0]);
               }
               else
               {
                  irs.setObject(i, qrs.getObject(i));
               }
            }
            irs.setInt(revcol, rev); // Override the revision
            irs.executeUpdate();
         }
      }
      finally
      {
         if (c != null) c.close();
      }

   }
}
