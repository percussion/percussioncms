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
