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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.jdbc.PSConnectionHelper;

/**
 * 
 * @author chriswright
 *
 */
public class PSFixZerosInRelationshipProperties extends PSFixDBBase implements IPSFix
{
   private static final Logger LOG = LogManager.getLogger(PSFixZerosInRelationshipProperties.class.getName());

   private static final String SQL_SELECT_SITE = "SELECT COUNT(*) FROM PSX_OBJECTRELATIONSHIP WHERE SITE_ID = 0";
   private static final String SQL_SELECT_FOLDER = "SELECT COUNT(*) FROM PSX_OBJECTRELATIONSHIP WHERE FOLDER_ID = 0";
   private static final String SQL_UPDATE_SITE = "UPDATE PSX_OBJECTRELATIONSHIP SET SITE_ID = NULL WHERE SITE_ID = 0";
   private static final String SQL_UPDATE_FOLDER = "UPDATE PSX_OBJECTRELATIONSHIP SET FOLDER_ID = NULL WHERE FOLDER_ID = 0";

   public PSFixZerosInRelationshipProperties() throws NamingException, SQLException
   {
      super();
   }

   @Override
   public String getOperation()
   {
      return "Fix relationships with zeros in the relationship properties.";
   }

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);

      Connection c = PSConnectionHelper.getDbConnection();

      try
      {
         // select statements
         PreparedStatement selectStatementSite = PSPreparedStatement.getPreparedStatement(c, SQL_SELECT_SITE);
         
         ResultSet rs = selectStatementSite.executeQuery();
         
         rs.next();
         
         logPreview(null, "Found " + rs.getInt(1) + " entrie(s) with 0 as the site_id.");
         
         rs.close();
         
         PreparedStatement selectStatementFolder = PSPreparedStatement.getPreparedStatement(c, SQL_SELECT_FOLDER);
         
         rs = selectStatementFolder.executeQuery();
         
         rs.next();
         
         logPreview(null, "Found " + rs.getInt(1) + " entrie(s) with 0 as the folder_id.");
         
         if (!preview) {
            // delete site_id statements
            PreparedStatement updateStatementSite = PSPreparedStatement.getPreparedStatement(c, SQL_UPDATE_SITE);
            
            int resultCount = updateStatementSite.executeUpdate();
            
            logInfo(null, resultCount + " rows with SITE_ID of 0 were updated.");
            LOG.info(resultCount + " rows with SITE_ID of 0 were updated.");
            
            rs.close();
            
             //delete folder_id statements
            PreparedStatement updateStatementFolder = PSPreparedStatement.getPreparedStatement(c, SQL_UPDATE_FOLDER);
            
            resultCount = updateStatementFolder.executeUpdate();
            
            logInfo(null, resultCount + " rows with FOLDER_ID of 0 were updated.");
            LOG.info(resultCount + " rows with FOLDER_ID of 0 were updated.");
            
            rs.close();
            selectStatementSite.close();
            selectStatementFolder.close();
            updateStatementSite.close();
            updateStatementFolder.close();
         }
      }
      finally
      {
         c.close();
      }
   }

}
