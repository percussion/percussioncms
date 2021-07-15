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
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

/**
 * Finds all orphaned folders (all folders without any parent except the ROOT)
 * and reports in preview mode. In fix mode, it attaches all these folders to
 * the root folder "Folders" so that user can see these folders in the content
 * explorer. Also it renames each of these to include the folderid in
 * paranthesis at the end of the title to avoid name conflicts. The tool's
 * responsibility is limited to attaching the orphaned folders to the "Folders"
 * folder and does not know which is the right parent folder for each orphaned
 * folder. The user must take appropriate action once he sees the folders in the
 * CX.
 */
public class PSFixOrphanedFolders extends PSFixDBBase implements IPSFix
{

   /**
    * Ctor
    * @throws SQLException 
    * @throws NamingException 
    */
   public PSFixOrphanedFolders() throws NamingException, SQLException
   {
   }

   @Override
   public void fix(boolean preview)
      throws Exception
   {
      super.fix(preview);
      
      int config = PSRelationshipConfig.ID_FOLDER_CONTENT;
      Connection c = PSConnectionHelper.getDbConnection();
      try
      {
         Map<Integer,String> orphans = new HashMap<Integer,String>();

         PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
            ms_query_orphans.expand(m_defDict));
         ResultSet rs = st.executeQuery();
         while (rs.next())
         {
            String title = rs.getString(2);
            if (title == null)
               title = "";
            orphans.put(new Integer(rs.getInt(1)), title);
         }
         rs.close();
         st.close();
         if (orphans.isEmpty())
         {
            logInfo(null, "No orphaned folders found in the system");
            return;
         }
         if (preview)
         {
            logPreview(null, "Would attach (if run in non-preview mode) the "
               + "following orpahed folders to the "
               + "root folder \"Folders\"");
            Iterator iter = orphans.keySet().iterator();
            while (iter.hasNext())
            {
               Integer contentid = (Integer) iter.next();
               String title = orphans.get(contentid).toString();
               logInfo(null, title + "(" + contentid + ")");
            }
         }
         else
         {
            logInfo(null, "Renaming all the orphaned folders to append the "
               + "content id to the title to avoid name conflicts");
            PreparedStatement renameStmt = PSPreparedStatement
               .getPreparedStatement(c, ms_rename_orphans.expand(m_defDict));

            Iterator iter = orphans.keySet().iterator();
            while (iter.hasNext())
            {
               Integer contentid = (Integer) iter.next();
               String title = orphans.get(contentid).toString();
               String newTitle = title + "(" + contentid + ")";
               logSuccess(contentid.toString(), "Renaming item from '"
                  + title + "' to '" + newTitle + "'...");
               renameStmt.setString(1, newTitle);
               renameStmt.setInt(2, contentid.intValue());
               renameStmt.executeUpdate();
            }
            renameStmt.close();
            logInfo(null, "Finished renaming");
            logInfo(null, "Attaching orphaned folders to \"Folders\"...");
            PreparedStatement insertRelationshipStmt = PSPreparedStatement
               .getPreparedStatement(c, ms_insert_relationship
                  .expand(m_defDict));
            iter = orphans.keySet().iterator();
            PreparedStatement insertRelationshipPropertyStmt = PSPreparedStatement
               .getPreparedStatement(c, ms_insert_relationship_property
                  .expand(m_defDict));
            int rid = getNextIdBlock(orphans.size(), "RXRELATEDCONTENT", false);
            while (iter.hasNext())
            {
               Integer dependentid = (Integer) iter.next();
               insertRelationshipStmt.setInt(1, rid);
               insertRelationshipStmt.setInt(2, config);
               insertRelationshipStmt.setInt(3, dependentid.intValue());
               insertRelationshipStmt.executeUpdate();

               insertRelationshipPropertyStmt.setInt(1, rid);
               insertRelationshipPropertyStmt.setString(2, "rs_allowcloning");
               insertRelationshipPropertyStmt.setString(3, "yes");
               insertRelationshipPropertyStmt.executeUpdate();

               insertRelationshipPropertyStmt.setInt(1, rid);
               insertRelationshipPropertyStmt.setString(2, "rs_expirationtime");
               insertRelationshipPropertyStmt.setString(3, null);
               insertRelationshipPropertyStmt.executeUpdate();

               insertRelationshipPropertyStmt.setInt(1, rid);
               insertRelationshipPropertyStmt.setString(2,
                  "rs_usedependentrevision");
               insertRelationshipPropertyStmt.setString(3, "no");
               insertRelationshipPropertyStmt.executeUpdate();

               insertRelationshipPropertyStmt.setInt(1, rid);
               insertRelationshipPropertyStmt.setString(2,
                  "rs_useownerrevision");
               insertRelationshipPropertyStmt.setString(3, "yes");
               insertRelationshipPropertyStmt.executeUpdate();

               insertRelationshipPropertyStmt.setInt(1, rid);
               insertRelationshipPropertyStmt.setString(2, "rs_useserverid");
               insertRelationshipPropertyStmt.setString(3, "yes");
               insertRelationshipPropertyStmt.executeUpdate();
               rid++;
            }
            insertRelationshipStmt.close();
            insertRelationshipPropertyStmt.close();

            logInfo(null, "Finished attaching folders");
         }
      }

      finally
      {
         c.close();
      }
   }

   @Override
   public String getOperation()
   {
      return "Fix orphaned folders";
   }
   
   /**
    * String template to query all orphaned folders.
    */
   static private PSStringTemplate ms_query_orphans = new PSStringTemplate(
      "SELECT CONTENTID, TITLE FROM {schema}.CONTENTSTATUS "
         + "WHERE CONTENTID>1 AND OBJECTTYPE=2 AND CONTENTID NOT IN "
         + "(SELECT DEPENDENT_ID FROM {schema}." + IPSConstants.PSX_RELATIONSHIPS 
         + " WHERE CONFIG_ID = 3)");

   /**
    * String template to rename the orphaned folders.
    */
   static private PSStringTemplate ms_rename_orphans = new PSStringTemplate(
      "UPDATE {schema}.CONTENTSTATUS SET TITLE=? WHERE CONTENTID=?");

   /**
    * String template to insert a relationship.
    */
   static private PSStringTemplate ms_insert_relationship = new PSStringTemplate(
      "INSERT INTO {schema}." + IPSConstants.PSX_RELATIONSHIPS + 
      " VALUES(?, ?, 3, -1, ?, -1, null, null, null, null, null, null, null)");

   /**
    * String template to insert a relationship property.
    */
   static private PSStringTemplate ms_insert_relationship_property = new PSStringTemplate(
      "INSERT INTO {schema}." + IPSConstants.PSX_RELATIONSHIPPROPERTIES + " VALUES(?, ?, ?)");
}
