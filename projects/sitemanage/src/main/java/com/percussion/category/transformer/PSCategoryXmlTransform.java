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

package com.percussion.category.transformer;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.data.PSTransformCategory;
import com.percussion.category.data.PSTransformCategoryNode;
import com.percussion.server.PSServer;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;



public class PSCategoryXmlTransform
{
	private static final String CATEGORIES_ROOT = "/Categories";
	private static final String CAT_SEP="/";
    private static final Logger log = LogManager.getLogger(PSCategoryXmlTransform.class);

    private Connection conn = null;
    private PSJdbcDbmsDef dbmsDef = null;
	public PSCategoryXmlTransform() {
		super();
	}

  
   public void transformXml(File fromFile, File toFile) {
      
      // Read the old category xml file
      PSTransformCategory category = getCategoryFromXml(fromFile);

      if(category!=null) {
          PSCategory newFormatCategory = transformToNewFormat(category);

          createNewFormatXml(newFormatCategory, toFile);

          Map<String, String> categoryMap = createCategoryMap(newFormatCategory);

          log.debug("CategoryMap = {}", categoryMap);

          updateDbCategories(categoryMap);
      }
   }
   
   
   private void updateDbCategories(Map<String,String> categoryMap)
   {
       try(Connection c = getConnection())
       {
           String categoryTable = PSSqlHelper.qualifyTableName("CT_PAGE_PAGE_CATEGORIES_SET");
           String categoryUpdate = "UPDATE " + categoryTable +" SET PAGE_CATEGORIES_TREE=? WHERE PAGE_CATEGORIES_TREE LIKE ?";
        
           try(PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
                   categoryUpdate)) {

               for (Entry<String, String> entry : categoryMap.entrySet()) {
                   st.setString(1, entry.getValue());
                   st.setString(2, entry.getKey());
                   st.execute();
                   int update = st.getUpdateCount();
                   if (update > 0)
                       log.info("Updated {} rows converting {} to {}", update, entry.getKey(), entry.getValue());
               }
           }
       }
       catch(Exception e)
       {
           log.error("Error updating DB Categories to new format: {}",e.getMessage());
           log.debug(e);
       }
       log.info("Finished updating categories in db to new structure");
       
   }
   
   
   private Map<String,String> createCategoryMap(PSCategory newFormatCategory)
   {
       Map<String,String> itemTransformMap = new HashMap<>();
       createCategoryMap(itemTransformMap, newFormatCategory.getTopLevelNodes(), CATEGORIES_ROOT, CATEGORIES_ROOT);
       return itemTransformMap;
   }
   
   private void createCategoryMap(Map<String,String> transformMap, List<PSCategoryNode> nodes, String namePath, String idPath)
   {
       
       for (PSCategoryNode node : nodes)
       {
           String title = node.getOldId();
           String id = node.getId();
           
           String newNamePath = namePath + CAT_SEP + title;
           String newIdPath =  idPath + CAT_SEP + id;
           
          
           transformMap.put(newNamePath, newIdPath);
           
           List<PSCategoryNode> children = node.getChildNodes();
           if (children != null && !children.isEmpty())
           {
               createCategoryMap(transformMap,children,newNamePath,newIdPath);
           }
       }     
   }
   
   
   
   private PSTransformCategory getCategoryFromXml(File oldFile) {
      
      PSTransformCategory category = null;
      if(!oldFile.exists())
         log.error("Old format Category Xml file does not exist.");
      else {
         
         try {

            JAXBContext jaxbContext = JAXBContext.newInstance(PSTransformCategory.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            category = (PSTransformCategory) jaxbUnmarshaller.unmarshal(oldFile);

          } catch (JAXBException e) {
              log.error("Error XML Parsing old categories file {} : {}",oldFile.getAbsolutePath(),e.getMessage() );
              log.debug(e);
          }
         
      }
      return category;
      
   }
   
   private PSCategory transformToNewFormat(@NotNull PSTransformCategory category) {
      
      PSCategory newFormatCategory = new PSCategory();
      List<PSCategoryNode> topNodes = new ArrayList<>();
      
      newFormatCategory.setTitle(category.getLabel());
      if(category.getTopNodes() != null && !category.getTopNodes().isEmpty()) {
         for(PSTransformCategoryNode node : category.getTopNodes()) {

            PSCategoryNode newNode = new PSCategoryNode();

            newNode.setId((UUID.randomUUID()).toString());
            newNode.setTitle(node.getLabel());
            newNode.setSelectable(node.getSelectable() != null && node.getSelectable().equalsIgnoreCase("yes"));
            newNode.setCreationDate(LocalDateTime.now());
            newNode.setCreatedBy("Transformation");
            newNode.setOldId(node.getId());
            if(node.getChildNodes() != null && ! node.getChildNodes().isEmpty())
               newNode.setChildNodes(getChildNodes(node));
            topNodes.add(newNode);
         }
      } 
      
      newFormatCategory.setTopLevelNodes(topNodes);
      
      return newFormatCategory;
   }
   
   private List<PSCategoryNode> getChildNodes(PSTransformCategoryNode node) {
      
      List<PSCategoryNode> childNodes = new ArrayList<>();
      
      for(PSTransformCategoryNode n : node.getChildNodes()) {
         
         PSCategoryNode newChildNode = new PSCategoryNode();
         newChildNode.setId((UUID.randomUUID()).toString());
         newChildNode.setOldId(n.getId());
         newChildNode.setTitle(n.getLabel());
         newChildNode.setSelectable(n.getSelectable() != null && n.getSelectable().equalsIgnoreCase("yes"));
         newChildNode.setCreationDate(LocalDateTime.now());
         newChildNode.setCreatedBy("Transform");
         
         if(n.getChildNodes() != null && !n.getChildNodes().isEmpty())
            newChildNode.setChildNodes(getChildNodes(n));
         
         childNodes.add(newChildNode);
      }
      
      return childNodes;
   }
   
   private void createNewFormatXml(PSCategory category, File toFile) {
      
      try {

         JAXBContext jaxbContext = JAXBContext.newInstance(PSCategory.class);
         Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

         // output pretty printed
         jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

         jaxbMarshaller.marshal(category, toFile);

     } catch (JAXBException e) {
        log.error(e.getMessage());
        log.debug(e);
     }
      
   }
   
   /**
    * Execute a sql statement against the connection and return the resultset
    * @param sqlStat Statement to be executed
    * @return ResultSet the results from the sql statement execution, may be null if no connection available
    */
   public ResultSet executeSqlStatement(Statement stat, String sqlStat)
   {
       ResultSet result = null;
       if(conn == null)
       {
           log.warn("Connection Object not available to execute against");
           return result;
       }
      
       try {
           result = stat.executeQuery(sqlStat);
       } catch (Exception e) {
           log.error("executeSqlStatement : {}", e.getMessage());
           log.debug(e);
       } 
       return result;
   }
   
   /**
    * Create a connection to the database
    * @return Connection Object may be null
    */
   public Connection getConnection()
   {
       Connection connection = null;
       try
       {
           if (dbmsDef==null)
           {
               Properties repprops = PSJdbcDbmsDef.loadRxRepositoryProperties(PSServer.getRxDir().getAbsolutePath());
               dbmsDef = new PSJdbcDbmsDef(repprops);
           }
           connection = PSJdbcTableFactory.getConnection(dbmsDef);

           if(connection!=null) {
               String msg = connection.toString();
               log.debug("Connection Made: {}" , msg);
           }
       }
       catch(Exception e)
       {
           log.warn(e.getMessage());
           log.debug(e);
       }
       return connection;
   }
   
   /**
    * Close the connection to the database
    * @return boolean true if connection is closed, false if close fails
    */
   public boolean closeConnection()
   {
       if(conn !=null)
       {
           try
           {
               conn.close();
           }
           catch(SQLException e)
           {
               log.warn(e.getMessage());
               log.debug(e);
               return false;
           }
           conn = null; 
       }
       else
           log.warn("Connection already closed");
       return true;
   }
}
