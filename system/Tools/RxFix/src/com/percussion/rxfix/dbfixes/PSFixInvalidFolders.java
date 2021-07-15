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
package com.percussion.rxfix.dbfixes;

import com.percussion.cms.IPSConstants;
import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

/**
 * Finds all invalid folders (all folders that have enrties in CONTENTSTATUS
 * table but not in PSX_FOLDER) and reports in preview mode. In fix mode, it
 * actually removes the rows corresponding to these folderids (contentids) from
 * the following tables.
 * <p>
 * <ul>
 * <li>CONTENTSTATUS</li>
 * <li>PSX_OBJECTACL</li>
 * <li>PSX_PROPERTIES</li>
 * <li>PSX_RELATIONSHIPPROPERTIES</li>
 * <li>PSX_RELATIONSHIPS</li>
 * </ul>
 * <p>
 * There could be entries in CONTENTSTATUSHISTORY table for these folders but we
 * don't clean this table.
 */
public class PSFixInvalidFolders extends PSFixDBBase implements IPSFix
{
   /**
    * Ctor
    * 
    * @throws SQLException
    * @throws NamingException
    */
   public PSFixInvalidFolders() throws NamingException,
         SQLException {
      super();
   }

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);
      Connection c = PSConnectionHelper.getDbConnection();
      try
      {
         Map<Integer, String> invalidFolders = new HashMap<Integer, String>();

         PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
               ms_query_invalid_folders.expand(m_defDict));
         ResultSet rs = st.executeQuery();
         while (rs.next())
         {
            String title = rs.getString(2);
            if (title == null)
               title = ""; //$NON-NLS-1$
            invalidFolders.put(new Integer(rs.getInt(1)), title);
         }
         rs.close();
         st.close();
         if (invalidFolders.isEmpty())
         {
            logInfo(null, "No invalid folders found in the system"); //$NON-NLS-1$
            return;
         }
         logInfo(null, "The following folders in the system have no " //$NON-NLS-1$
               + "entries in PSX_FOLDER tables and are invalid"); //$NON-NLS-1$
         Iterator iter = invalidFolders.keySet().iterator();
         while (iter.hasNext())
         {
            Integer contentid = (Integer) iter.next();
            String title = invalidFolders.get(contentid).toString();
            logInfo(null, title + "(" + contentid + ")"); //$NON-NLS-1$ //$NON-NLS-2$
         }
         if (preview)
         {
            logPreview(null,
                  "The following tables will be cleaned for the above " //$NON-NLS-1$
                        + "folders"); //$NON-NLS-1$
            logPreview(null, CONTENTSTATUS); //$NON-NLS-1$
            logPreview(null, PSX_OBJECTACL); //$NON-NLS-1$
            logPreview(null, PSX_PROPERTIES); //$NON-NLS-1$
            logPreview(null, PSX_RELATIONSHIPPROPERTIES); //$NON-NLS-1$
            logPreview(null, PSX_RELATIONSHIPS); //$NON-NLS-1$
         }
         else
         {
            Object[] cids = invalidFolders.keySet().toArray();
            // build comma separated list of contentids for IN clause
            String in_clause = buildCommaSeparatedList(cids);

            String where = CONTENTID;
            cleanupTable(c, CONTENTSTATUS, where, in_clause);
            cleanupTable(c, PSX_OBJECTACL, where, in_clause);
            cleanupTable(c, PSX_PROPERTIES, where, in_clause);

            // Query relationships that have the bad folderids
            Set<Integer> ridSet = new HashSet<Integer>();
            m_defDict.put(IN_CLAUSE, in_clause); //$NON-NLS-1$
            PreparedStatement query_relatiosnhips = PSPreparedStatement
                  .getPreparedStatement(c, ms_query_relationships
                        .expand(m_defDict));
            rs = query_relatiosnhips.executeQuery();
            while (rs.next())
               ridSet.add(new Integer(rs.getInt(1)));
            rs.close();
            query_relatiosnhips.close();
            m_defDict.remove(IN_CLAUSE);

            // build comma separated list of RIDs for IN clause
            Object[] rids = ridSet.toArray();
            in_clause = buildCommaSeparatedList(rids);
            where = RID;
            cleanupTable(c, PSX_RELATIONSHIPPROPERTIES, where, in_clause);
            cleanupTable(c, PSX_RELATIONSHIPS, where, in_clause);
         }
      }

      finally
      {
         c.close();
      }
   }

   /**
    * Helper method to build a string of comma separated list given an array of
    * objects. The object is converted to string using {@link Object#toString()}
    * method.
    * 
    * @param objects array objects to build the list from, must not be
    *           <code>null</code>.
    * @return string of comma separated list as mentioned above, never
    *         <code>null</code> empty if the supplied array is empty.
    */
   private String buildCommaSeparatedList(Object[] objects)
   {
      if (objects == null)
      {
         throw new IllegalArgumentException("objects must not be null");
      }
      if (objects.length == 0)
         return "";
      StringBuilder sql = new StringBuilder();
      int size = objects.length;
      for (int i = 0; i < size; i++)
         sql.append(objects[i].toString()).append(",");
      // Remove the trailing ","
      sql.deleteCharAt(sql.lastIndexOf(","));
      return sql.toString();
   }

   /**
    * Helper method to cleanup a table using the SQL string {@link #ms_cleanup}.
    * 
    * @param c SQL connection, assumes not <code>null</code>, an open one and
    *           does not close after usage.
    * @param table name of the table to clean, assumed not <code>null</code>
    *           or empty.
    * @param where where clause in the above SQL statement, assumed not
    *           <code>null</code> or empty.
    * @param in_clause in clause in the above SQL statement, assumed not
    *           <code>null</code> or empty.
    * @throws SQLException for any database related error.
    * @throws PSStringTemplateException if there is an error during string
    *            replacement in the string template.
    */
   private void cleanupTable(Connection c, String table,
         String where, String in_clause) throws SQLException,
         PSStringTemplateException
   {
      logInfo(null, "Cleaning " + table + " table to remove entries with ids ("
            + in_clause + ").");
      PreparedStatement stmt = null;
      try
      {
         m_defDict.put(TABLE, table);
         m_defDict.put(WHERE, where);
         m_defDict.put(IN_CLAUSE, in_clause);
         stmt = PSPreparedStatement.getPreparedStatement(c, ms_cleanup
               .expand(m_defDict));
         stmt.executeUpdate();
      }
      finally
      {
         if (stmt != null)
            stmt.close();
         m_defDict.remove(TABLE);
         m_defDict.remove(WHERE);
         m_defDict.remove(IN_CLAUSE);
      }
      logInfo(null, "Finished Cleaning " + table + " table.");
   }

   @Override
   public String getOperation()
   {
      return "Fix invalid folders";
   }
   
   /**
    * String template to query all orphaned folders.
    */
   static private PSStringTemplate ms_query_invalid_folders = new PSStringTemplate(
         "SELECT CONTENTID, TITLE FROM {schema}.CONTENTSTATUS "
               + "WHERE OBJECTTYPE=2 AND CONTENTID NOT IN "
               + "(SELECT CONTENTID FROM {schema}.PSX_FOLDER)");

   /**
    * String template to cleanup a table with CONTENTID column
    */
   static private PSStringTemplate ms_cleanup = new PSStringTemplate(
         "DELETE FROM {schema}.{table} WHERE {where} IN ({in_clause})");

   // String constants being repeatedly used in the SQL statements
   private static final String TABLE = "table";

   private static final String WHERE = "where";

   private static final String IN_CLAUSE = "in_clause";

   private static final String RID = "RID";

   private static final String CONTENTID = "CONTENTID";

   private static final String PSX_RELATIONSHIPS = IPSConstants.PSX_RELATIONSHIPS;

   private static final String PSX_RELATIONSHIPPROPERTIES = IPSConstants.PSX_RELATIONSHIPPROPERTIES;

   private static final String PSX_PROPERTIES = "PSX_PROPERTIES";

   private static final String PSX_OBJECTACL = "PSX_OBJECTACL";

   private static final String CONTENTSTATUS = "CONTENTSTATUS";

   /**
    * String template to query all relationshipids for invalid folders
    */
   static private PSStringTemplate ms_query_relationships = new PSStringTemplate(
         "SELECT RID FROM {schema}."
               + PSX_RELATIONSHIPS
               + " WHERE OWNER_ID IN ({in_clause}) OR DEPENDENT_ID IN ({in_clause})");
}
