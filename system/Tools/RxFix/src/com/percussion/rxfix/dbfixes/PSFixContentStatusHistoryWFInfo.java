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
