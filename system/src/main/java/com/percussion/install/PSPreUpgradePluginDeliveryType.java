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
