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
import java.util.Arrays;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.jdbc.PSConnectionHelper;

/***
 * Fix Allowed Sites folder properties that reference sites that do not exist on this system.  This is cleanup for bug:
 * 
 * @author natechadwick
 *
 */
public class PSFixAllowedSitePropertiesWithBadSites extends PSFixDBBase implements IPSFix
{
   
   static final String SITESQL = "SELECT SITEID FROM RXSITES";
   static final String PROPERTYSQL = "SELECT CONTENTID, REVISIONID, SYSID, PROPERTYVALUE, DESCRIPTION FROM PSX_PROPERTIES WHERE PROPERTYNAME='sys_allowed_sites'";
   static final String UPDATESQL = "UPDATE PSX_PROPERTIES SET PROPERTYVALUE=?, DESCRIPTION=? WHERE CONTENTID=? AND REVISIONID=? AND SYSID=?";
   
   
   /**
    * The log4j logger used for this class.
    */
   private static final Logger log = LogManager.getLogger(PSFixAllowedSitePropertiesWithBadSites.class);

   public PSFixAllowedSitePropertiesWithBadSites() throws NamingException, SQLException
   {
      super();
      // TODO Auto-generated constructor stub
   }

   @Override
   public String getOperation()
   {
      return "Fix invalid sites in Allowed Sites property of Folders";
   }
   
   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);
   
     
      Connection c = PSConnectionHelper.getDbConnection();
     
      try{
      PreparedStatement stSites = PSPreparedStatement.getPreparedStatement(c,SITESQL);
      PreparedStatement stProps = PSPreparedStatement.getPreparedStatement(c,PROPERTYSQL);
      ArrayList validSites = new ArrayList();
      
      //Get all current valid sites. 
      ResultSet rsSites = stSites.executeQuery();
      while (rsSites.next())
      {
         validSites.add(Integer.toString(rsSites.getInt(1)));
     
      }
      logPreview(null, "Found " + validSites.size() + " valid Sites.");
     
      rsSites.close();
      
      ResultSet rsProps = stProps.executeQuery();
      
      Integer contentId = null;
      Integer revisionId = null;
      Integer sysId = null;
      String allowedSites = null;
      String description = null;
      
      PreparedStatement update = PSPreparedStatement.getPreparedStatement(c, UPDATESQL);
      int correctCount = 0;
      int folderCount = 0;
      while (rsProps.next()){
         contentId = rsProps.getInt(1);
         revisionId = rsProps.getInt(2);
         sysId = rsProps.getInt(3);
         allowedSites = rsProps.getString(4);
         description = rsProps.getString(5);
       
         log.debug("Checking folder "  + contentId + " for invalid sites...");
         
         ArrayList<String> listOfSites =   new ArrayList<String>(Arrays.asList(allowedSites.split(",")));
         @SuppressWarnings("unchecked")
         ArrayList<String> masterCopy = (ArrayList<String>) listOfSites.clone();
         listOfSites.removeAll(validSites);
         
         if(!listOfSites.isEmpty()){
            folderCount++;
            //There are sites that don't belong in the list.
            for (String site : listOfSites)
            {
               correctCount++;
               masterCopy.remove(site);
               log.warn("Allowed Site reference to invalid Site {} detected.", site);
            }
         
            if(!preview){
               update.setString(1, StringUtils.join(masterCopy.toArray(), ","));
               update.setString(2, description);
               update.setInt(3, contentId);
               update.setInt(4, revisionId);
               update.setInt(5, sysId);
               
               update.executeUpdate();
               log.info("Removed invalid Site references on Folder {}", contentId);
            }else{
               logPreview(Integer.toString(contentId),"Would remove invalid Site from folder.");
            }
         }
      }
      
      if (correctCount == 0)
      {
         logInfo(null, "No problems found");
      }
      else
      {
         if (preview)
         {
            logPreview(null, "Would have corrected " + correctCount
                  + " invalid Allowed Site references on " + folderCount + " folders");
         }
         else
         {
            logSuccess(null, "Corrected " + correctCount
                  + " invalid Allowed Site references on " + folderCount + " folders");
         }
      }
     rsProps.close();
     stProps.close();
     stSites.close();
     
      }finally{c.close();}
   }

   
}
