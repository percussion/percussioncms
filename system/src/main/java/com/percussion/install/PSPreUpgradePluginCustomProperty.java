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

//java

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSSqlHelper;
import org.w3c.dom.Element;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



/**
 * This plugin has been written to check for custom property use in views, searches,
 * display formats, and actions.  If this is found, the install will not be
 * allowed to proceed.
 */

public class PSPreUpgradePluginCustomProperty implements IPSUpgradePlugin
{
   /**
    * Default constructor
    */
   public PSPreUpgradePluginCustomProperty()
   {
   }

   /**
    * Implements the process function of IPSUpgradePlugin. Checks views, searches,
    * display formats, and actions for custom property use.  If any is found, 
    * a message is returned informing the user to modify these server objects.
    *
    * @param config PSUpgradeModule object.
    * @param elemData We do not use this element in this function.
    * @return <code>null</code>.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      m_config.getLogStream().println("Checking views, searches, display formats, " +
         "and actions for custom property use.");
      
      //Initialize non-custom properties
      for (int i = 0; i < m_viewSearchProps.length; i++)
         m_nonCustomProps.add(m_viewSearchProps[i]);
      
      for (int i = 0; i < m_displayFormatProps.length; i++)
         m_nonCustomProps.add(m_displayFormatProps[i]);
      
      for (int i = 0; i < m_actionProps.length; i++)
         m_nonCustomProps.add(m_actionProps[i]);
      
      Set results = new HashSet();
      int respType = PSPluginResponse.SUCCESS;
      String respMessage = "The following Server Objects use custom properties:" + "\n\n";
      String endMessage = "\nPlease remove these properties.\n\n";
      String bodyMessage = "";   
      Iterator iter;
      String log = config.getLogFile();
      
      try
      {
         //Check views/searches
         results = checkCustomProps("PSX_SEARCHPROPERTIES", "PROPERTYNAME",
               "PROPERTYID", "PSX_SEARCHES", "DISPLAYNAME", "SEARCHID");
         
         if (results.size() > 0)
         {
            bodyMessage += "Views/Searches:\n\n";
            iter = results.iterator();
            
            while (iter.hasNext())
            {
               bodyMessage += (String) iter.next() + "\n";
            }
         }
         
         //Check display formats
         results = checkCustomProps("PSX_DISPLAYFORMATPROPERTIES", "PROPERTYNAME",
               "PROPERTYID", "PSX_DISPLAYFORMATS", "DISPLAYNAME", "DISPLAYID");
                  
         if (results.size() > 0)
         {
            bodyMessage += "\nDisplay Formats:\n\n";
            iter = results.iterator();
            
            while (iter.hasNext())
            {
               bodyMessage += (String) iter.next() + "\n";
            }
         }
         
         //Check actions
         results = checkCustomProps("RXMENUACTIONPROPERTIES", "PROPNAME",
               "ACTIONID", "RXMENUACTION", "DISPLAYNAME", "ACTIONID");
                  
         if (results.size() > 0)
         {
            bodyMessage += "\nActions:\n\n";
            iter = results.iterator();
            
            while (iter.hasNext())
            {
               bodyMessage += (String) iter.next() + "\n";
            }
         }
         
         if (bodyMessage.trim().length() > 0)
         {
            respMessage += bodyMessage + endMessage;
            respType = PSPluginResponse.EXCEPTION;
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(config.getLogStream());
         respType = PSPluginResponse.EXCEPTION;
         respMessage = "Failed to check Server Objects for custom property use, "
            + "see the \"" + log + "\" located in " + RxUpgrade.getPreLogFileDir()
            + " for errors.";
      }
           
      config.getLogStream().println(
         "Finished process() of the plugin Custom Property...");
      return new PSPluginResponse(respType, respMessage);
   }
      
   /**
    * Helper function that checks for custom property use.  It first finds any
    * custom properties used, then looks up the corresponding object using the
    * id.
    *
    *@param propTable the property table to check, assumed not <code>null</code>
    *@param propColumn the property column to check, assumed not <code>null</code>
    *@param propIdColumn the property id column, assumed not <code>null</code>
    *@param table the corresponding object table, assumed not <code>null</code>
    *@param nameColumn the object name column, assumed not <code>null</code>
    *@param idColumn, the object id column, assumed not <code>null</code>
    * 
    * @return set containing names of any objects which use custom 
    * properties, empty if none are found.
    */
   private Set checkCustomProps(String propTable, String propColumn, String propIdColumn,
         String table, String nameColumn, String idColumn)
    throws Exception
   {
      Set ids = new HashSet();
      Set names = new HashSet();
      Connection conn = RxUpgrade.getJdbcConnection();
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
         
      String qualTableName = 
         PSSqlHelper.qualifyTableName(
               propTable, 
               dbmsDef.getDataBase(), 
               dbmsDef.getSchema(),
               dbmsDef.getDriver());
      
      String queryStmt = "SELECT " + qualTableName + "." + propIdColumn + "," +
                         qualTableName + "." + propColumn + " " + 
                         "FROM " + qualTableName;
       
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(queryStmt);
      Integer propId;
      String propName;
                       
      while (rs.next())
      {
         propId = new Integer(rs.getInt(propIdColumn));
         propName = rs.getString(propColumn);
         
         //if custom prop, add id 
         if (!m_nonCustomProps.contains(propName))
         {
            ids.add(propId);
            
            m_config.getLogStream().println("Custom property found [" +
                  propName + "]");
         }
      }
        
      qualTableName = 
         PSSqlHelper.qualifyTableName(
               table, 
               dbmsDef.getDataBase(), 
               dbmsDef.getSchema(),
               dbmsDef.getDriver());
      
      Iterator iter = ids.iterator();
      
      while (iter.hasNext())
      {
         int id = ((Integer) iter.next()).intValue();
         queryStmt = "SELECT " + qualTableName + "." + nameColumn + " " +
                     "FROM " + qualTableName + " WHERE " +
                     qualTableName + "." + idColumn + "=" + id;
         
         rs = stmt.executeQuery(queryStmt);
         
         if (rs.next())
            names.add(rs.getString(nameColumn));
      
      }
      
      return names;
   }
   
   private IPSUpgradeModule m_config;
   
   /**
    * Non-custom display format properties
    */
   private String[] m_displayFormatProps = new String[] {
         "sortColumn", "sortDirection", "sys_community" };
   
   /**
    * Non-custom action properties
    */
   private String[] m_actionProps = new String[] {
         "AcceleratorKey",
         "Description",
         "launchesWindow",
         "MnemonicKey",
         "refreshHint",
         "ShortDescription",
         "SmallIcon",
         "SupportsMultiSelect",
         "target",
         "targetStyle"
   };
   
   /**
    * Non-custom view/search properties
    */
   private String[] m_viewSearchProps = new String[] {
         "aadNewSearch",
         "bodyfilter",
         "cxNewSearch",
         "expansionlevel",
         "folderPath",
         "FullTextQuery",
         "includeSubFolders",
         "isCustom",
         "querytype",
         "searchEngineType",
         "searchMode",
         "sys_community",
         "sys_username",
         "userCustomizable",
         
         
   };
   
   /**
    * Set of all non-custom properties
    */
   private Set m_nonCustomProps = new HashSet();
}
