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

package com.percussion.rxfix.dbfixes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
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
   private static final Logger LOG = Logger.getLogger(PSFixZerosInRelationshipProperties.class.getName());

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
