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
package com.percussion.install;

import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSPreparedStatement;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Load the url's for all content lists and checks to see if the delivery type
 * parameter value is a standard type, {@link #ms_deliveryTypes}, or if it is
 * custom, in which case it requires re-implementation and registration in
 * publisher-beans.xml.
 */

public class PSPreUpgradePluginDeliveryType implements IPSUpgradePlugin
{
   
   /**
    * Default constructor
    */
   public PSPreUpgradePluginDeliveryType()
   {
   }
   
   /**
    * Implements the process function of IPSUpgradePlugin. Looks for content
    * list url delivery type values which are not standard types.  If any are
    * found, a message is returned informing the user about these delivery
    * types.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      config.getLogStream().println("Looking for custom delivery types");
      PSPluginResponse response = null;
      int respType = PSPluginResponse.SUCCESS;
      String respMessage = "";
            
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Connection conn = null;
      Set<String> customTypes = new HashSet<>();
      boolean ftpTypesFound = false;
      PSConnectionObject connObj = new PSConnectionObject();
      
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         String contentListTable = RxUpgrade.qualifyTableName("RXCONTENTLIST");
         
         String sqlStmt = "SELECT NAME, URL FROM " + contentListTable;
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         connObj.setConnection(conn);
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
         
         while (rs.next())
         {
            String name = rs.getString(1);
            String url = rs.getString(2);
                        
            if (url == null)
            {
               config.getLogStream().println("Url does not exist for content "
                     + "list '" + name + "'");
               continue;
            }
             
            Map<String, Object> params = PSBaseHttpUtils.parseQueryParamsString(
                  url);
            String param = "sys_deliverytype";
            if (!params.containsKey(param))
               continue;
                        
            String value = (String) params.get(param);
            if (value == null)
               continue;
            
            if (!ms_deliveryTypes.contains(value))
               customTypes.add(value);
            
            if (value.equalsIgnoreCase(DELIVERY_TYPE_FTP) ||
                  value.equalsIgnoreCase(DELIVERY_TYPE_SFTP))
            {
               ftpTypesFound = true;               
            }
         }
         
         if (customTypes.size() > 0)
         {
            respType = PSPluginResponse.WARNING;
          
            String types = "";
            for (String type : customTypes)
            {
               if (types.length() > 0)
                  types += "\n";
               types += type;
            }
            
            respMessage += RxInstallerProperties.getString(
                  "customDeliveryTypes") + "\n\n" + types;
         }
         
         if (ftpTypesFound)
         {
            if (respMessage.trim().length() > 0)
            {
               respMessage += "\n\n";
            }
            
            respMessage += RxInstallerProperties.getString(
                  "ftpDeliveryTypes");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         respType = PSPluginResponse.EXCEPTION;
         respMessage = "Failed to check delivery types, see the \""
            + config.getLogFile() + "\" located in "
            + RxUpgrade.getPreLogFileDir() + " for errors.";
      }
      finally
      {
         connObj.close();
      }
           
      response = new PSPluginResponse(respType, respMessage);
                 
      config.getLogStream().println(
         "Finished process() of the plugin Delivery Types...");
      return response;
   }

   /**
    * Set of standard delivery types.
    */
   private static Set<String> ms_deliveryTypes = new HashSet<>();
   
   /**
    * Name of the ftp delivery type.
    */
   private static final String DELIVERY_TYPE_FTP = "ftp";
   
   /**
    * Name of the sftp delivery type.
    */
   private static final String DELIVERY_TYPE_SFTP = "sftp";
   
   static 
   {
      ms_deliveryTypes.add("database");
      ms_deliveryTypes.add("filesystem");
      ms_deliveryTypes.add(DELIVERY_TYPE_FTP);
      ms_deliveryTypes.add(DELIVERY_TYPE_SFTP);      
   }
      
}
