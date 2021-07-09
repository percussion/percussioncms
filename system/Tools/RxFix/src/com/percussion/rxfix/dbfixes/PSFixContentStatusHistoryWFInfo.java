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

import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;


/**
 * @FIXME - this doesn't handle the case where an item has changed workflows correctly.
 */
/**
 * This class fixes up incorrectly workflow application ids in the
 * <code>CONTENTSTATUSHISTORY</code> table.
 */
public class PSFixContentStatusHistoryWFInfo extends PSFixDBBase
      implements
         IPSFix
{
   /**
    * Find what content items have incorrect <code>workflowappid</code>
    * values.
    */
   private static final PSStringTemplate ms_findIncorrectWFAppId = new PSStringTemplate(
         "SELECT DISTINCT C.CONTENTID, C.WORKFLOWAPPID "
               + "FROM {schema}.CONTENTSTATUS C, {schema}.CONTENTSTATUSHISTORY H "
               + "WHERE C.CONTENTID = H.CONTENTID AND "
               + "C.WORKFLOWAPPID <> H.WORKFLOWAPPID");

   /**
    * Update records for a given content id in the
    * <code>CONTENTSTATUSHISTORY</code> to have the correct
    * <code>workflowappid</code>
    */
   private static final PSStringTemplate ms_updateIncorrectWFAppId = new PSStringTemplate(
         "UPDATE {schema}.CONTENTSTATUSHISTORY "
               + "SET WORKFLOWAPPID = ? WHERE CONTENTID = ?");

   /**
    * Ctor
    * 
    * @throws SQLException
    * @throws NamingException
    */
   public PSFixContentStatusHistoryWFInfo() throws NamingException,
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
         int correctCount = 0;

         PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
               ms_findIncorrectWFAppId.expand(m_defDict));
         ResultSet rs = st.executeQuery();

         PreparedStatement update = PSPreparedStatement.getPreparedStatement(c,
               ms_updateIncorrectWFAppId.expand(m_defDict));

         // For each record, update
         while (rs.next())
         {
            int contentid = rs.getInt(1);
            int workflowappid = rs.getInt(2);
            correctCount++;
            if (preview == false)
            {
               logDebug(Integer.toString(contentid),
                     "Updating content id for workflow " + workflowappid);
               update.setInt(1, workflowappid);
               update.setInt(2, contentid);
               update.executeUpdate();
            }
            else
            {
               logDebug(Integer.toString(contentid),
                     "Would update content id for workflow " + workflowappid);
            }
         }
         rs.close();
         st.close();
         update.close();

         if (correctCount == 0)
         {
            logInfo(null, "No problems found");
         }
         else
         {
            if (preview)
            {
               logPreview(null, "Would have corrected " + correctCount
                     + " content items to have the correct workflow id");
            }
            else
            {
               logSuccess(null, "Corrected " + correctCount
                     + " content items to have the correct workflow id");
            }
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
      return "Fix content status history workflow information";
   }
}
