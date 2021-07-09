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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.jdbc.PSConnectionHelper;

public class PSFixOrphanedContentChangeEvents extends PSFixDBBase implements IPSFix
{
   
   private static String SELECT_CONTENT_SQL = "SELECT CONTENTID FROM PSX_CONTENTCHANGEEVENT WHERE CONTENTID NOT IN (SELECT CONTENTID FROM CONTENTSTATUS)";
   private static String SELECT_SITE_SQL = "SELECT SITEID FROM PSX_CONTENTCHANGEEVENT WHERE SITEID NOT IN (SELECT SITEID FROM RXSITES)";
   private static String DELETE_SQL = "DELETE FROM PSX_CONTENTCHANGEEVENT WHERE CONTENTID=?";
   private static String SITE_DELETE_SQL = "DELETE FROM PSX_CONTENTCHANGEEVENT WHERE SITEID=?";
   
   
   /**
    * The log4j logger used for this class.
    */
   private static final Logger log = LogManager.getLogger(PSFixOrphanedContentChangeEvents.class);


   public PSFixOrphanedContentChangeEvents() throws NamingException, SQLException
   {
      super();
      // TODO Auto-generated constructor stub
   }


   @Override
   public String getOperation()
   {
      return "Clear deleted content from the Incremental queue";
   }
   
   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);
   
     
      Connection c = PSConnectionHelper.getDbConnection();
     
      try{
      PreparedStatement stContent = PSPreparedStatement.getPreparedStatement(c,SELECT_CONTENT_SQL);
      PreparedStatement stSites = PSPreparedStatement.getPreparedStatement(c,SELECT_SITE_SQL);
      
      PreparedStatement stDelete = PSPreparedStatement.getPreparedStatement(c,DELETE_SQL);
      PreparedStatement stSiteDelete = PSPreparedStatement.getPreparedStatement(c,SITE_DELETE_SQL);
      
  
      ArrayList<String> orphanedItems = new ArrayList<String>();
      ArrayList<String> orphanedSites = new ArrayList<String>();
      
      
      //Get all current orphaned items. 
      ResultSet rs = stContent.executeQuery();
      while (rs.next())
      {
         orphanedItems.add(Integer.toString(rs.getInt(1)));
     
      }
      
      rs.close();
  
      rs = stSites.executeQuery();
      while (rs.next())
      {
         orphanedSites.add(Integer.toString(rs.getInt(1)));
     
      }
    
      logPreview(null, "Found " + orphanedItems.size() + " orphaned items.");
         int itemCount=0;
         for(String s : orphanedItems){
            if(!preview){
               stDelete.setInt(1, Integer.parseInt(s));
               stDelete.execute();
               log.info("Removed " + s + " from Content Change event queue.");
            }else{
               logPreview(s,"Would remove from Incremental queue.");
            }
            itemCount++;
         }
    
         int siteCount=0;
         for(String s : orphanedSites){
            if(!preview){
               stSiteDelete.setInt(1, Integer.parseInt(s));
               stSiteDelete.execute();
               log.info("Removed references to deleted Site id " + s + " from Content Change event queue.");
            }else{
               logPreview(s,"Would remove from Incremental queue.");
            }
            siteCount++;
         }
         
         
      if (itemCount == 0 && siteCount ==0)
      {
         logInfo(null, "No problems found");
      }
      else
      {
         if (preview)
         {
            logPreview(null, "Would have corrected " + itemCount
                  + " orphaned items and " + siteCount + " site items stuck in the Incremental queue");
            
         }
         else
         {
            logSuccess(null, "Corrected " + itemCount
                  + " orphaned items and "+ siteCount + " items stuck in the Incremental queue");
         }
      }
     rs.close();
     stDelete.close();
     stContent.close();
     stSiteDelete.close();
      }finally{c.close();}
   }


}
