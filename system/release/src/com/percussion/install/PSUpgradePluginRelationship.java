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
package com.percussion.install;

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSCloneOverrideField;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSContentItemStatus;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.tablefactory.PSJdbcColumnData;
import com.percussion.tablefactory.PSJdbcColumnDef;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcSelectFilter;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.tablefactory.tools.PSCatalogTableData;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This pluguin is used to migrate relationships data from 5.x to Rhino (6.x?) 
 */
public class PSUpgradePluginRelationship implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSUpgradePluginRelationship() 
   {
   }
   
   /**
    * Set the properties that can be used to connect to the repository.
    * 
    * @param dbProps the database properties, never <code>null</code>.
    */
   public void setDbProperties(Properties dbProps)
   {
      m_dbProps = dbProps;
      m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
   }

   /**
    * This is essentially the same as {@link PSRelationshipConfig}, except it
    * allows whitespace characters for the name of the relationship.
    */
   public static class RelationshipConfig extends PSRelationshipConfig
   {
      /**
       * Generated number
       */
      private static final long serialVersionUID = -7879716053741162922L;

      /**
       * Construct an object from its XML representation.
       * 
       * @param src the XML representation of the to be constructed object, may
       *           not be <code>null</code>.
       * @param parentDoc the Java object which is the parent of this object, it
       *           may be <code>null</code>.
       * @param parentComponents the parent objects of this object, it may be
       *           <code>null</code>.
       * 
       * @throws PSUnknownNodeTypeException if malformed XML in 'src'.
       */
      RelationshipConfig(Element src, IPSDocument parentDoc,
            ArrayList parentComponents) throws PSUnknownNodeTypeException {
         super(src, parentDoc, parentComponents);
      }

      /**
       * Override {@link PSRelationshipConfig#setName(String)} to allow the name
       * contains whitespace characters
       * 
       * @param name the name of the relationship configuration. It may not be
       *           <code>null</code> or empty, but it may contain whitespace
       *           characters.
       */
      public void setName(String name)
      {
         m_name = name;
      }
   }

   /**
    * This is essentaily the same as {@link PSRelationshipConfigSet}, except
    * its child class is {@link RelationshipConfig}.
    */
   public static class RelationshipConfigSet extends PSRelationshipConfigSet
   {
      /**
       * Generated number
       */
      private static final long serialVersionUID = -4374436883055871148L;

      /**
       * Construct an object from its XML representation.
       * 
       * @param src the XML representation of the to be constructed object, may
       *           not be <code>null</code>.
       * 
       * @throws PSUnknownNodeTypeException if malformed XML in 'src'.
       */
      public RelationshipConfigSet(Element sourceNode)
            throws PSUnknownNodeTypeException 
      {
         super();
         super.fromXml(sourceNode, null, null);
      }

      // Override super.createMemberObject(...)
      protected PSRelationshipConfig createMemberObject(Element sourceNode,
            IPSDocument parentDoc, ArrayList parentComponents)
            throws PSUnknownNodeTypeException
      {
         return new RelationshipConfig(sourceNode, parentDoc, parentComponents);
      }
   }

   /**
    * Gets all relationship configurations from its XML representation.
    * 
    * @param doc the XML representation of the relationship configs, never
    *   <code>null</code>.
    * 
    * @return all relationship configurations, never <code>null</code>.
    * 
    * @throws PSUnknownNodeTypeException if the XML is not welformed.
    */ 
   public PSRelationshipConfigSet getConfigSet(Document doc)
         throws PSUnknownNodeTypeException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      return new RelationshipConfigSet(doc.getDocumentElement());
   }
   
   /**
    * Implements process method of IPSUpgradePlugin.
    * 
    * @param module IPSUpgradeModule object. may not be <code>null<code>.
    * @param elemData data element of plugin.
    */
   public PSPluginResponse process(IPSUpgradeModule module, Element elemData)
   {
      PrintStream logger = module.getLogStream();
      logger.println("Running Update Relationships plugin");

      // determine if this is for upgrade from 6.0
      boolean isUpgradeVersion60 = false;
      if (elemData != null)
      {
         String overrideAttr = 
            elemData.getAttribute("upgradeCommunityIdFieldOverride");
         if (overrideAttr != null)
         {
            isUpgradeVersion60 = overrideAttr.equals("true");
         }
      }
      
      // upgrade relationship related data
      Connection conn = null;
      try
      {
         m_dbProps = RxUpgrade.getRxRepositoryProps();
         m_dbProps.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
         conn = RxUpgrade.getJdbcConnection();
         conn.setAutoCommit(false);

         if (isUpgradeVersion60)
         {
            upgradeForVersion60(logger, conn);
         }
         else
         {
            upgradeBeforeVersion60(logger, conn);
         }

         conn.commit();
         
         logger.println("Successfully finished upgrading relationships.\n");
      }
      catch (Exception e)
      {
         try
         {
            if (conn != null)
               conn.rollback();
         }
         catch (SQLException se)
         {
         }

         e.printStackTrace(logger);
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
            conn = null;
         }

         logger.println("leaving the process() of the Update Relationships plugin.");
      }

      return null;
   }

   /**
    * Upgrade the relationship configuration and other related tables
    * for Version 4 or 5, but not 6.0, build 200609R01
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the connection object; assumed not <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private void upgradeBeforeVersion60(PrintStream logger, Connection conn)
      throws Exception
   {
      Document doc = getRelationshipConfigs(logger, conn);
      PSRelationshipConfigSet configSet = getConfigSet(doc);

      // fix up relationship configurations & gets the mapper
      // which maps new to old relationship name
      Map configNames = fixupRelationshipConfigNameIds(logger, configSet, 50);
            
      addKnownExecutionContexts(logger, configSet);
      removeExpirationProperty(logger, configSet);
      updateActiveAssemblyProperties(logger, configSet);
      
      saveRelationshipConfigs(logger, conn, configSet);

      saveRelationshipConfigNameIds(logger, conn, configSet, configNames);

      populateRelationshipData(logger, conn, configSet);

      dropRelationshipTables(logger, conn);

      createRelationshipViews(logger, conn, configSet, configNames);
      
      updateMenuActionTable(logger, conn, configNames);
      
      updateMenuActionParamTable(logger, conn, configNames);
      
      updateSlotTable(logger, conn, configNames);
    
      upgradeSystemDef(logger, configNames);
      
      fixProcessChecks(logger, configSet, configNames);
   }

   /**
    * Traverse the process checks looking for conditionals. The conditionals may
    * contain literals that reference the names of relationships.
    * 
    * @param logger the logging stream, assumed never <code>null</code>
    * @param configSet the configurations, assumed never <code>null</code>
    * @param configNames the map of new to old configuration names, assumed
    *           never <code>null</code>
    */
   private void fixProcessChecks(PrintStream logger,
         PSRelationshipConfigSet configSet, Map configNames)
   {
      // Reverse the config name map
      Map oldToNew = new HashMap();
      Iterator eiter = configNames.entrySet().iterator();
      while(eiter.hasNext())
      {
         Entry entry = (Entry) eiter.next();
         oldToNew.put(entry.getValue(), entry.getKey());
      }
      Iterator citer = configSet.getConfigList().iterator();
      while (citer.hasNext())
      {
         fixProcessChecks(logger, (PSRelationshipConfig) citer.next(),
               oldToNew);
      }
   }

   /**
    * Traverse the process checks looking for conditionals. The conditionals may
    * contain literals that reference the names of relationships.
    * 
    * @param logger the logging stream, assumed never <code>null</code>
    * @param config the configuration, assumed never <code>null</code>
    * @param oldToNew the map of old to new configuration names, assumed
    *           never <code>null</code>
    */
   private void fixProcessChecks(PrintStream logger,
         PSRelationshipConfig config, Map oldToNew)
   {
      Iterator iter = config.getProcessChecks();
      while (iter.hasNext())
      {
         fixProcessCheck(logger, (PSProcessCheck) iter.next(), oldToNew);
      }
   }

   /**
    * Fix the process check's conditionals that contain literals that reference
    * names of old relationships.
    * 
    * @param logger the logging stream, assumed never <code>null</code>
    * @param check the process check, assumed never <code>null</code>
    * @param oldToNew the map of old to new configuration names, assumed
    *           never <code>null</code>
    */
   private void fixProcessCheck(PrintStream logger, PSProcessCheck check,
         Map oldToNew)
   {
      Iterator ruleiter = check.getConditions();
      while(ruleiter.hasNext())
      {
         fixRule(logger, (PSRule) ruleiter.next(), oldToNew);
      }
   }

   /**
    * Fix the rule's literals that reference names of old relationships
    * 
    * @param logger the logging stream, assumed never <code>null</code>
    * @param rule the rule, assumed never <code>null</code>
    * @param oldToNew the map of old to new configuration names, assumed
    *           never <code>null</code>
    */
   private void fixRule(PrintStream logger, PSRule rule,
         Map oldToNew)
   {
      PSExtensionCallSet callSet = rule.getExtensionRules();
      if (callSet != null)
      {
         Iterator call = callSet.iterator();
         while (call.hasNext())
         {
            fixCall(logger, (PSExtensionCall) call.next(), oldToNew);
         }
      }
      Iterator citer = rule.getConditionalRules();
      while(citer.hasNext())
      {
         fixConditional(logger, (PSConditional) citer.next(), oldToNew);
      }
   }

   /**
    * Fix the conditionals that reference names of old relationships
    * 
    * @param logger the logging stream, assumed never <code>null</code>
    * @param conditional the conditional, assumed never <code>null</code>
    * @param oldToNew the map of old to new configuration names, assumed never
    *           <code>null</code>
    */
   private void fixConditional(PrintStream logger, PSConditional conditional,
         Map oldToNew)
   {
      fixReplacementValue(logger, oldToNew, conditional.getValue());
      fixReplacementValue(logger, oldToNew, conditional.getVariable());
   }

   /**
    * Fix the call's literals that reference names of old relationships
    * 
    * @param logger the logging stream, assumed never <code>null</code>
    * @param call the call, assumed never <code>null</code>
    * @param oldToNew the map of old to new configuration names, assumed
    *           never <code>null</code>
    */
   private void fixCall(PrintStream logger, PSExtensionCall call,
         Map oldToNew)
   {
      PSExtensionParamValue values[] = call.getParamValues();
      for (int i = 0; i < values.length; i++)
      {
         fixReplacementValue(logger, oldToNew, values[i].getValue());
      }
   }

   /**
    * Fix the replacement value, if appropriate
    * @param logger the logging stream, assumed never <code>null</code>
    * @param oldToNew the map of old to new configuration names, assumed
    *           never <code>null</code>
    * @param value the value that may reference an old relationship name,
    *   assumed never <code>null</code>
    */
   private void fixReplacementValue(PrintStream logger,
         Map oldToNew, IPSReplacementValue value)
   {
      if (value instanceof PSTextLiteral)
      {
         PSTextLiteral tl = (PSTextLiteral) value;
         String mapped = (String) oldToNew.get(tl.getText());
         if (mapped != null)
         {
            logger.println("Updating relationship config, replaced old "
                  + "relationship name in extension call or conditional: "
                  + tl.getText() + " with: " + mapped);
            tl.setText(mapped);
         }
      }
   }

   /**
    * Upgrade the relationship configuration for Version 6.0, build 200609R01
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the connection object; assumed not <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private void upgradeForVersion60(PrintStream logger, Connection conn)
      throws Exception
   {
      Document doc = getRelationshipConfigs(logger, conn);
      PSRelationshipConfigSet configSet = getConfigSet(doc);

      updateOverrideFieldForCommunityId(logger, configSet);
      
      saveRelationshipConfigs(logger, conn, configSet);
   }
   

   /**
    * Updates the Content Editor System Definition, replace the relationship 
    * names with the new names. The relationship names are specified in the
    * conditions for the relationship command handler. 
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param nameMap it maps the new relationship name to its old one,
    *    assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void upgradeSystemDef(PrintStream logger, Map nameMap)
         throws Exception
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      Document doc = null;
      File defFile = null;
      String strSystemDef = RxUpgrade.getRxRoot() + 
         "/rxconfig/Server/ContentEditors/ContentEditorSystemDef.xml";

      try
      {
         // retrieve the system def
         defFile = new File(strSystemDef);
         in = new FileInputStream(defFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         in.close();
         in = null;

         // convert the relationship names
         //
         // get the <PSXApplicationFlow>/<CommandHandler name="relate"> element
         Element root = doc.getDocumentElement();
         NodeList nodeList = root.getElementsByTagName("PSXApplicationFlow");
         Element appFlowEl = (Element) nodeList.item(0);
         nodeList = appFlowEl.getElementsByTagName("CommandHandler");
         int length = nodeList.getLength();
         Element commandEl;
         Element relateCommandEl = null;
         String name;
         for (int i=0; i<length; i++)
         {
            commandEl = (Element) nodeList.item(i);
            name = commandEl.getAttribute("name");
            if (name.equals("relate"))
            {
               relateCommandEl = commandEl;
               break;
            }
         }
         
         if (relateCommandEl == null)
         {
            logger.println("Cannot find ../PSXApplicationFlow/<CommandHandler name = \"relate\"> element.");
            throw new RuntimeException("Cannot find relationship command handler element.");
         }
         
         // /PSXConditionalRequest/Conditions/PSXRule/PSXConditional+
         nodeList = relateCommandEl.getElementsByTagName("PSXConditionalRequest");
         int numReq = nodeList.getLength();
         for (int i=0; i < numReq; i++)
         {
            Element condReqEl = (Element) nodeList.item(i);
            NodeList condList = condReqEl.getElementsByTagName("Conditions");
            for (int j=0; j < condList.getLength(); j++)
            {
               Element conditionEl = (Element) condList.item(j);
               NodeList ruleList = conditionEl.getElementsByTagName("PSXRule");
               for (int r=0; r < ruleList.getLength(); r++)
               {
                  Element ruleEl = (Element) ruleList.item(r);
                  NodeList conditionalList = ruleEl
                        .getElementsByTagName("PSXConditional");
                  for (int c=0; c < conditionalList.getLength(); c++)
                  {
                     Element conditionalEl = (Element) conditionalList.item(c);
                     NodeList valueList = conditionalEl
                           .getElementsByTagName("value");
                     // ../PSXConditional/value
                     for (int v=0; v < valueList.getLength(); v++)
                     {
                        Element valueEl = (Element) valueList.item(v);
                        NodeList litList = valueEl
                              .getElementsByTagName("PSXTextLiteral");
                        // ../value/PSXTextLiteral
                        for (int l=0; l < litList.getLength(); l++)
                        {
                           Element litEl = (Element) litList.item(l);
                           NodeList textList = litEl
                                 .getElementsByTagName("text");
                           //   ../value/PSXTextLiteral/text
                           for (int t=0; t < textList.getLength(); t++)
                           {
                              Element txtEl = (Element) textList.item(t);
                              String relName = PSXMLDomUtil.getElementData(txtEl);
                              String newRelName = getNewRelName(relName, nameMap);
                              if (newRelName != null
                                    && (!relName.equals(newRelName)))
                              {
                                 logger.println("Replace relationship name ("
                                       + relName + ") with the new name \""
                                       + newRelName
                                       + "\" in ContentEditorSystemDef.xml.");
                                 PSXmlDocumentBuilder.replaceText(doc, txtEl,
                                       newRelName);
                              }
                           }
                           
                        }
                     }
                  }
               }
            }
            
         }

         //Save the definition
         out = new FileOutputStream(defFile);
         PSXmlDocumentBuilder.write(doc, out);
         
         logger.println("Successfully updated the relationship names in ContentEditorSystemDef.xml.\n");
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();
         }
         catch (Exception e)
         {
         }
         try
         {
            if (out != null)
               out.close();
         }
         catch (Exception e)
         {
         }
      }
   }
   
   /**
    * Gets the new relationship name from the supplied old name.
    * 
    * @param oldName the old relationship name, assumed not <code>null</code>.
    * @param nameMap it maps new relationship name to its old name, assumed
    *    not <code>null</code>
    *    
    * @return the new relationship name, it may be <code>null</code> if cannot
    *    find the related new name.
    */
   private String getNewRelName(String oldName, Map nameMap)
   {
      Iterator entries = nameMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         if (oldName.equalsIgnoreCase((String)entry.getValue()))
            return (String) entry.getKey();
      }
      
      return null;
   }
   
   /**
    * Updates the relationship names in the slot table. 
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param nameMap it maps the new relationship name to its old one,
    *    assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void updateSlotTable(PrintStream logger, Connection conn,
         Map configNames) throws Exception
   {
      Iterator names = configNames.entrySet().iterator();
      while (names.hasNext())
      {
         Map.Entry entry = (Map.Entry) names.next();
         String newName = (String) entry.getKey();
         String oldName = (String) entry.getValue();
         if (!oldName.equals(newName))
         {
            String sqlStmt = "update " + qualifyTableName("RXSLOTTYPE")
                  + " set RELATIONSHIPNAME = '" + newName
                  + "' where RELATIONSHIPNAME = '" + oldName + "'";
            executeUpdate(logger, conn, sqlStmt, null);
         }
      }
      
      logger.println("Successfully updated slot table with the new relationship names.\n");
   }
   
   /**
    * Updates the relationship names in the URL column of RXMENUACTION table. 
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param nameMap it maps the new relationship name to its old one,
    *    assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void updateMenuActionTable(PrintStream logger, Connection conn,
         Map configNames) throws Exception
   {
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(m_dbProps);
      PSJdbcTableSchema tableSchema = getTableSchema(conn, dbmsDef,
            MENUACTION_TABLE);
      
      PSJdbcTableData tableData = PSJdbcTableFactory.catalogTableData(conn,
            dbmsDef, tableSchema, null, null, PSJdbcRowData.ACTION_UPDATE);
      
      logger.println("Catalog table '" + MENUACTION_TABLE + "'; got "
            + tableData.getRowCount() + " row(s) in the result set.");

      if (tableData.getRowCount() == 0)
      {
         return; // do nothing if there is no action defined 
      }
      
      List updatedRows = new ArrayList();
      Iterator rows = tableData.getRows();
      while (rows.hasNext())
      {
         PSJdbcRowData srcRow = (PSJdbcRowData) rows.next();
         PSJdbcRowData tgtRow = getUpdatedRow(srcRow, configNames,
               MENUACTION_TABLE);
         if (tgtRow != null)
            updatedRows.add(tgtRow);
      }
      
      if (!updatedRows.isEmpty())
      {
         tableData = new PSJdbcTableData(MENUACTION_TABLE, updatedRows
               .iterator());
         tableSchema.setAllowSchemaChanges(false);
         tableSchema.setTableData(tableData);
         PSJdbcTableFactory.processTable(conn, dbmsDef, tableSchema, logger, true);
      }
      
      logger.println("Successfully updated '" + MENUACTION_TABLE
            + "' table with the new relationship names.\n");
   }
   
   /**
    * the name of the menu action table
    */
   private final static String MENUACTION_TABLE = "RXMENUACTION";
   
   /**
    * the name of the menu action param table
    */
   private final static String MENUACTIONPARAM_TABLE = "RXMENUACTIONPARAM";
   
   /**
    * the name of the object relationship table
    */
   private final static String OBJECTRELATIONSHIP_TABLE =
        "PSX_OBJECTRELATIONSHIP";
      
   /**
    * Gets the updated row from the supplied source row if it contains 
    * old relationship name which needs to be updated.
    * 
    * @param srcRow the source row, assumed not <code>null</code>.
    * @param configNames the new config name maps to the old config name, 
    *    assumed not <code>null</code>.
    * @param table the table name, assumed not <code>null</code> or empty.
    * 
    * @return the updated row. It may be <code>null</code> if the source row
    *    does not need to be updated.
    */
   private PSJdbcRowData getUpdatedRow(PSJdbcRowData srcRow, Map configNames, 
      String table)
   {
      PSJdbcColumnData col = srcRow.getColumn("URL");
      String url = col.getValue();
      if (url == null || url.trim().length() == 0)
         return null;
      
      int index = url.indexOf(SYS_RELATIONSHIP_TYPE_EQ);
      
      // do nothing if there is no relationship type
      if (index == -1 ||  
          (index + SYS_RELATIONSHIP_TYPE_EQ.length()) >= url.length())
         return null;
      
      // get the relationship name
      String name = url.substring(index + SYS_RELATIONSHIP_TYPE_EQ.length());
      index = name.indexOf('&');
      if (index != -1)
         name = name.substring(0, index);
      
      String newName = name.replaceAll(" ", "");
      // do nothing if there is no space in the relationship name or cannot
      // find the new name in the mapper
      if (newName.equals(name) || configNames.get(newName) == null)
         return null; 
      
      // construct the updated row
      url = url.replaceAll(SYS_RELATIONSHIP_TYPE_EQ + name,
            SYS_RELATIONSHIP_TYPE_EQ + newName);
      col.setValue(url);

      List columns = new ArrayList();
      columns.add(srcRow.getColumn("ACTIONID"));
      columns.add(col);

      return new PSJdbcRowData(columns.iterator(), PSJdbcRowData.ACTION_UPDATE);
   }
   
   private final static String SYS_RELATIONSHIP_TYPE = "sys_relationshiptype";
   private final static String SYS_RELATIONSHIP_TYPE_EQ = 
      SYS_RELATIONSHIP_TYPE + "=";
   
   /**
    * Creates relationship views for backwards compatable. 
    * Note, the views will translate the relationship name from new to old. 
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param configs the relationship configurations. Assumned not
    *           <code>null</code>.
    * @param nameMap it maps the new relationship name to its old one,
    *    assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void createRelationshipViews(PrintStream logger, Connection conn,
         PSRelationshipConfigSet configSet, Map nameMap)
         throws Exception
   {
      // create the view to map to the old relationship main table
      
      String relView = "PSX_RELATIONSHIPS";
      String qualViewName = 
         PSSqlHelper.qualifyViewName(
               relView,
               m_dbProps.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY),
               m_dbProps.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY),
               m_dbProps.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY));
            
      String sqlStmt = "CREATE VIEW " + qualViewName + " AS "
         + "SELECT RL.RID, " + getSelectConfigName(configSet, nameMap)
         + "RL.OWNER_ID AS OWNERID, RL.OWNER_REVISION AS OWNERREVISION, " 
         + "RL.DEPENDENT_ID AS DEPENDENTID, "
         + "RL.DEPENDENT_REVISION AS DEPENDENTREVISION, '' AS DESCRIPTION "
         + "FROM " + getNewRelMainTable() + " RL, "
         + getRelNameTable() + " RN "
         + "WHERE RL.CONFIG_ID = RN.CONFIG_ID";
      
      executeUpdate(logger, conn, sqlStmt, null);
      
      // create the RXRELATEDCONTENT view
      
      String relConView = "RXRELATEDCONTENT";
      qualViewName = 
         PSSqlHelper.qualifyViewName(
               relConView,
               m_dbProps.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY),
               m_dbProps.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY),
               m_dbProps.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY));
      
      StringBuffer buf = new StringBuffer();
      buf.append("CREATE VIEW " + qualViewName + " AS SELECT RL.RID AS SYSID, " 
         + "RL.OWNER_ID AS CONTENTID, RL.OWNER_REVISION AS REVISIONID, "
         + "RL.DEPENDENT_ID AS ITEMCONTENTID, '' AS ITEMDESCRIPTION, "
         + "RL.SLOT_ID AS RELATIONSHIP, RL.SORT_RANK AS SORTRANK, "
         + "RL.VARIANT_ID AS VARIANTID, '' AS ITEMTEXT, 0 AS SYSSORTORDER, ");
      
      buf.append(getSelectConfigName(configSet, nameMap));
      
      buf.append("RL.SITE_ID AS SITEID, RL.FOLDER_ID AS FOLDERID "
         + "FROM " + getNewRelMainTable() + " RL, "
         + getRelNameTable() + " RN "
         + "WHERE (RL.CONFIG_ID = RN.CONFIG_ID) AND (RL.SLOT_ID IS NOT NULL) "
         + "AND (RL.SORT_RANK IS NOT NULL) AND (RL.VARIANT_ID IS NOT NULL)");
      
      executeUpdate(logger, conn, buf.toString(), null);
      
      logger.println("Successfully created backwards compatible views.\n");
   }

   /**
    * Create partial SELECT clause for the relationship config name.
    * 
    * @param configSet the relationship configurations, never <code>null</code>.
    * @param nameMap it maps new relationship name to old once, 
    *    never <code>null</code>.
    * 
    * @return the created partial SELECT clause, never <code>null</code> or
    *    empty.
    */
   private String getSelectConfigName(PSRelationshipConfigSet configSet,
         Map nameMap)
   {
      StringBuffer caseBuf = new StringBuffer();
      boolean hasMismatchName = false;
      caseBuf.append("CASE RN.CONFIG_NAME ");
      String cname;
      Iterator configs = configSet.iterator();
      while (configs.hasNext())
      {
         cname = ((PSRelationshipConfig) configs.next()).getName();
         
         if (!cname.equals((String)nameMap.get(cname)))
         {
            hasMismatchName = true;
            caseBuf.append("WHEN '" + cname + "' ");
            caseBuf.append("THEN '" + nameMap.get(cname) + "' ");
         }
      }
      caseBuf.append("ELSE RN.CONFIG_NAME END AS CONFIG, ");
      
      if (hasMismatchName)
         return caseBuf.toString();
      else
         return "RN.CONFIG_NAME AS CONFIG, ";
   }
   
   /**
    * Removes old relationship tables.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void dropRelationshipTables(PrintStream logger, Connection conn)
         throws Exception
   {
      String sqlStmt = "DROP TABLE " + getOldRelPropTable();
      executeUpdate(logger, conn, sqlStmt, null);
      
      sqlStmt = "DROP TABLE " + getOldRelMainTable();
      executeUpdate(logger, conn, sqlStmt, null);
      
      // drop the old view
      sqlStmt = "DROP VIEW RXRELATEDCONTENT";
      executeUpdate(logger, conn, sqlStmt, null);
   }
   
   /**
    * Retrieves all relationship configurations from the repository.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param configs the relationship configurations. Assumned not
    *           <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void populateRelationshipData(PrintStream logger, Connection conn,
         PSRelationshipConfigSet configs)
         throws Exception
   {
      // First catalog the new relationship table to get column info
      
      PSJdbcTableSchema newSchema = PSCatalogTableData.catalogTable(
            new PSJdbcDbmsDef(m_dbProps), OBJECTRELATIONSHIP_TABLE);
      
      // Remove all rows in the new relationship tables first
      
      String sqlStmt = "DELETE FROM " + getNewRelPropTable();
      executeUpdate(logger, conn, sqlStmt, null);

      sqlStmt = "DELETE FROM " + getNewRelMainTable();
      executeUpdate(logger, conn, sqlStmt, null);

      // populate the data from PSX_RELATIONSHIPS table
     
      sqlStmt = "INSERT INTO " + getNewRelMainTable()
         + " (RID, CONFIG_ID, OWNER_ID, OWNER_REVISION, DEPENDENT_ID, "
         + "DEPENDENT_REVISION) "
         + "SELECT r.RID, rn.CONFIG_ID, r.OWNERID, r.OWNERREVISION, "
         + "r.DEPENDENTID, r.DEPENDENTREVISION "
         + "FROM " + getOldRelMainTable() + " r, "
         + getRelNameTable() + " rn "
         + "WHERE r.CONFIG = rn.CONFIG_NAME";
   
      executeUpdate(logger, conn, sqlStmt, null);
      
      logger.println("Finished insert into Relationship Main table.");
      
      // populate pre-defined user properties
                 
      PSJdbcColumnDef slotIdColumn = newSchema.getColumn(SLOT_ID);
      PSJdbcColumnDef sortRankColumn = newSchema.getColumn(SORT_RANK);
      PSJdbcColumnDef variantIdColumn = newSchema.getColumn(VARIANT_ID);
      PSJdbcColumnDef folderIdColumn = newSchema.getColumn(FOLDER_ID);
      PSJdbcColumnDef siteIdColumn = newSchema.getColumn(SITE_ID);
      PSJdbcColumnDef inlineRelationshipColumn = newSchema.getColumn(
            INLINE_RELATIONSHIP);
      
      boolean isDB2;
      if (!m_dbProps.getProperty(
            PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY).equalsIgnoreCase(
                  PSJdbcUtils.DB2))
         isDB2 = false;
      else
         isDB2 = true;
          
      updatePduProperty(
            logger, conn, slotIdColumn, SLOT_ID, "sys_slotid", isDB2);
      updatePduProperty(
            logger, conn, sortRankColumn, SORT_RANK, "sys_sortrank", isDB2);
      updatePduProperty(
            logger, conn, variantIdColumn, VARIANT_ID, "sys_variantid", isDB2);
      updatePduProperty(
            logger, conn, folderIdColumn, FOLDER_ID, "sys_folderid", isDB2);
      updatePduProperty(
            logger, conn, siteIdColumn, SITE_ID, "sys_siteid", isDB2);
      updatePduProperty(
            logger, conn, inlineRelationshipColumn, INLINE_RELATIONSHIP,
            "rs_inlinerelationship", isDB2);
            
      logger.println("Finished update pre-defined user properties.");
      
      // populate user defined properties
      updateCustomProperties(logger, conn, configs);
   }

   /**
    * Update a pre-defined user property from the old property table to 
    * the new relationship main table.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param configSet the relationship configurations, assumed not
    *           <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void updateCustomProperties(PrintStream logger, Connection conn,
         PSRelationshipConfigSet configSet) throws Exception
   {
      Iterator configs = configSet.iterator();
      PSRelationshipConfig config;
      Iterator propNames;
      while (configs.hasNext())
      {
         config = (PSRelationshipConfig) configs.next();
         propNames = config.getCustomPropertyNames().iterator();
         while (propNames.hasNext())
         {
            String propName = (String) propNames.next();
            String sqlStmt = "INSERT INTO " + getNewRelPropTable() 
            + " SELECT p.RID, p.PROPERTYNAME, p.PROPERTYVALUE "
            + "FROM " + getOldRelPropTable() + " p " 
            + "WHERE p.PROPERTYNAME = '" + propName + "'"; 
            
            executeUpdate(logger, conn, sqlStmt, null);
         }
      }
      logger.println("Finished update custom (or user defined) properties.");
   }
   
   /**
    * Update a pre-defined user property from the old property table to 
    * the new relationship main table.  If the property is a numeric
    * property, all rows containing non-numeric property values for the
    * property will be removed from the old table.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param column the column of the new relationship name table. 
    *           May be <code>null</code>.
    * @param columnName the column name of the new relationship name table.
    *           Assumed not <code>null</code>.
    * @param propName the property name of the updated property. 
    *           Assumed not <code>null</code>.
    * @param isDB2 <code>true</code> if the driver is DB2 and differing types
    *           must be cast, <code>false</code> otherwise.
    * 
    * @throws Exception if an error occurs.
    */
   private void updatePduProperty(PrintStream logger, Connection conn,
         PSJdbcColumnDef column, String columnName, String propName,
         boolean isDB2) throws Exception
   {
      String sqlStmt;
      
      if (column == null)
         logger.println("Column " + columnName + " does not exist in table "
               + getNewRelMainTable());
      else
      {
         if (propName.equals("sys_slotid") ||
               propName.equals("sys_sortrank") ||
               propName.equals("sys_variantid") ||
               propName.equals("sys_folderid") ||
               propName.equals("sys_siteid"))
            removeNonNumericPropertyValues(logger, conn, propName);
         
         if (!isDB2)
            sqlStmt = "UPDATE " + getNewRelMainTable()  
               + " SET " + columnName + " = (SELECT PROPERTYVALUE "
               + "FROM " + getOldRelPropTable() + " p " 
               + "WHERE p.PROPERTYNAME = '" + propName + "' " 
               + "AND p.RID = " + getNewRelMainTable() + ".RID)"; 
         else
         {
            String columnType = column.getNativeType();
            String columnSize = column.getSize();
            
            if (columnSize != null)
            {
               // Add size
               columnType += "(" + columnSize + ")";
            }
            
            sqlStmt = "UPDATE " + getNewRelMainTable()  
               + " SET " + columnName + " = CAST((SELECT PROPERTYVALUE "
               + "FROM " + getOldRelPropTable() + " p " 
               + "WHERE p.PROPERTYNAME = '" + propName + "' " 
               + "AND p.RID = " + getNewRelMainTable() + ".RID) as "
               + columnType + ")"; 
         }
         executeUpdate(logger, conn, sqlStmt, null);
      }
   }
   
   /**
    * @return the name of the relationship name table.
    */
   private String getRelNameTable()
   {
      return qualifyTableName("PSX_RELATIONSHIPCONFIGNAME");
   }
   
   /**
    * @return the name of the old relationship main table.
    */
   String getOldRelMainTable()
   {
      return qualifyTableName("PSX_RELATIONSHIPS");
   }

   /**
    * @return the name of the old relationship properties table.
    */
   String getOldRelPropTable()
   {
      return qualifyTableName("PSX_RELATIONSHIPPROPERTIES");
   }
   
   /**
    * @return the name of the new relationship main table.
    */
   private String getNewRelMainTable()
   {
      return qualifyTableName(OBJECTRELATIONSHIP_TABLE);
   }

   /**
    * @return the name of the new relationship properties table.
    */
   private String getNewRelPropTable()
   {
      return qualifyTableName("PSX_OBJECTRELATIONSHIPPROP");
   }
   
   /**
    * @return the name of the menu action param table.
    */
   private String getMenuActionParamTable()
   {
      return qualifyTableName(MENUACTIONPARAM_TABLE);
   }
   
   /**
    * This will create a fully qualified table name. Depending on the provided
    * driver type we will return table, owner.table or db.owner.table.
    * 
    * @param table the table name to qualify, must be valid
    */
   private String qualifyTableName(String table)
   {
      String database = m_dbProps.getProperty("DB_NAME");
      String schema = m_dbProps.getProperty("DB_SCHEMA");
      String driver = m_dbProps.getProperty("DB_DRIVER_NAME");

      return PSSqlHelper.qualifyTableName(table, database, schema, driver);
   }

   /**
    * Retrieves all relationship configurations from the repository.
    * 
    * @param logger the logger used to log messages, may not be
    *           <code>null</code>.
    * @param conn the JDBC connection, may not be <code>null</code>.
    * 
    * @return the document with contains all relationship configurations, never
    *         <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public Document getRelationshipConfigs(PrintStream logger, Connection conn)
         throws Exception
   {
      if (logger == null)
         throw new IllegalArgumentException("logger may not be null");
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
      
      StringReader reader = null;
      try
      {
         String configXml = processRelationshipConfigs(logger, conn, null);
         reader = new StringReader(configXml);
         return PSXmlDocumentBuilder.createXmlDocument(reader, false);
      }
      finally
      {
         if (reader != null)
            reader.close();

         logger.println("retrieved all relationship configurations");
      }

   }
   
   

   /**
    * Retrieves and updates the relationship configurations from the
    * configuration table.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param newConfigXml the new (or to be updated) relationship 
    *    configurations. It is <code>null</code> if retrieving the 
    *    relationship configs only; otherwise this will be persisted
    *    into the configuration table.
    * 
    * @return the relationship configs that is either retrieved or
    *    updated from the repository.
    * 
    * @throws Exception if any error occurs.
    */
   public String processRelationshipConfigs(PrintStream logger, 
      Connection conn, String newConfigXml) throws Exception
   {      
      PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(m_dbProps);
      PSJdbcTableSchema tableSchema = getTableSchema(conn, dbmsDef,
            CONFIG_TABLE);
      
      String[] columns = new String[] {REL_XML_COL_NAME};
      PSJdbcSelectFilter filter = new PSJdbcSelectFilter(REL_KEY_COL_NAME, 
            PSJdbcSelectFilter.EQUALS, REL_KEY_COL_VALUE, Types.VARCHAR);
      
      PSJdbcTableData tableData = PSJdbcTableFactory.catalogTableData(conn,
            dbmsDef, tableSchema, columns, filter, PSJdbcRowData.ACTION_UPDATE);
      
      logger.println("Catalog table '" + CONFIG_TABLE + "'  where "
            + filter.toString() + "; got " + tableData.getRowCount()
            + " row(s) in the result set.");

      if (tableData.getRowCount() != 1)
      {
         String errorMsg = "Failed to retrieve existing relationship configurations.";
         logger.println(errorMsg);
         throw new IllegalStateException(errorMsg);
      }
      
      PSJdbcRowData row = (PSJdbcRowData)tableData.getRows().next();
      PSJdbcColumnData colData = row.getColumn(REL_XML_COL_NAME);
      if (newConfigXml == null) // retrieve only
      {
         return colData.getValue();
      }
      else
      {
         // prepare the updated row
         colData.setValue(newConfigXml);
         PSJdbcColumnData colName = new PSJdbcColumnData(REL_KEY_COL_NAME, 
            REL_KEY_COL_VALUE);
         row.addColumn(colName);  
         
         tableSchema.setAllowSchemaChanges(false);
         tableSchema.setTableData(tableData);
         PSJdbcTableFactory.processTable(conn, dbmsDef, tableSchema, logger, true);
         return newConfigXml;
      }
   }

   /**
    * Catalog the table schema for the specified table.
    * 
    * @param conn database connection, assumed not <code>null</code>.
    * @param dbmsDef the database definition, assumed not <code>null</code>.
    * @param table the table name, assumed not <code>null</code> or empty.
    * 
    * @return the cataloged table schema, never <code>null</code>.
    * 
    * @throws Exception if any error occurs.
    */
   private PSJdbcTableSchema getTableSchema(Connection conn,
      PSJdbcDbmsDef dbmsDef, String table) throws Exception
   {
      PSJdbcDataTypeMap dataTypeMap = new PSJdbcDataTypeMap(m_dbProps
            .getProperty("DB_BACKEND"),
            m_dbProps.getProperty("DB_DRIVER_NAME"), null);

      // catalog the table schema
      PSJdbcTableSchema tableSchema = PSJdbcTableFactory.catalogTable(conn,
            dbmsDef, dataTypeMap, table, false);

      return tableSchema;
   }
   
   /**
    * The key column name of the configuration table.
    */
   private final static String REL_KEY_COL_NAME = "NAME";

   /**
    * The value of the key column of the config table for relationship configs
    */
   private final static String REL_KEY_COL_VALUE = "relationships";
   
   /**
    * The column name of the config table for relationship configs.
    */
   private final static String REL_XML_COL_NAME = "CONFIGURATION";
   
   /**
    * The name of the configuration table.
    */
   private final static String CONFIG_TABLE = "PSX_RXCONFIGURATIONS";

   /**
    * The column name of the slot id for relationship configs.
    */
   private final static String SLOT_ID = "SLOT_ID";
   
   /**
    * The column name of the sort rank for relationship configs.
    */
   private final static String SORT_RANK = "SORT_RANK";
   
   /**
    * The column name of the variant id for relationship configs.
    */
   private final static String VARIANT_ID = "VARIANT_ID";
   
   /**
    * The column name of the folder id for relationship configs.
    */
   private final static String FOLDER_ID = "FOLDER_ID";
   
   /**
    * The column name of the site id for relationship configs.
    */
   private final static String SITE_ID = "SITE_ID";
   
   /**
    * The column name of the inline relationship for relationship configs.
    */
   private final static String INLINE_RELATIONSHIP = "INLINE_RELATIONSHIP";
   
   /**
    * Sends a specified query to the database and expecting the result set is
    * a list of strings.
    *  
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param sqlStmt the query string, assumed not <code>null</code>.
    * 
    * @return a list of strings from the result set of the query, never
    *   <code>null</code>, but may be empty.
    * 
    * @throws Exception if error occurs.
    */
   List queryStringList(PrintStream logger, Connection conn, String sqlStmt)
         throws Exception
   {
      List result = new ArrayList();
      Statement stmt = null;
      ResultSet rs = null;
      try
      {
         logger.println("queryStringList SQL[" + ++ms_sqlCount + "]: "
               + sqlStmt);

         stmt = PSSQLStatement.getStatement(conn);
         rs = stmt.executeQuery(sqlStmt);

         while (rs.next())
         {
            result.add(rs.getString(1));
         }
         
         logger.println("                SQL[" + ms_sqlCount + "]: "
               + "got " + result.size() + " row(s) in the result set.");

         return result;
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
            stmt = null;
         }
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
            }
            rs = null;
         }

         logger.println("retrieved all relationship configurations");
      }
   }

   /**
    * Saves all relationship configurations to the repository.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param configSet the to be saved relationship configurations, assumed not
    *           <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   protected void saveRelationshipConfigs(PrintStream logger, Connection conn,
         PSRelationshipConfigSet configSet) throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element configEl = configSet.toXml(doc);
      
      processRelationshipConfigs(logger, conn, PSXmlDocumentBuilder
            .toString(configEl));

      logger.println("saved all relationship configurations");
   }

   /**
    * Saves all relationship configurations to the repository.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param sqlStmt the SQL statement for the updates, assumed not
    *           <code>null</code>.
    * @param bindValue the bind values for the above SQL statement. It may be
    *           <code>null</code> if there is no bind value. The type of the
    *           bind values must be either {@link Integer} or {@link String}.
    * 
    * @return either (1) the row count for <code>INSERT</code>,
    *         <code>UPDATE</code>, or <code>DELETE</code> statements or (2)
    *         0 for SQL statements that return nothing
    * 
    * @throws Exception if an error occurs.
    */
   private int executeUpdate(PrintStream logger, Connection conn,
         String sqlStmt, Object[] bindValue) throws Exception
   {
      PreparedStatement stmt = null;
      try
      {
         logger.println("ExecuteUpdate SQL[" + ++ms_sqlCount + "]: " + sqlStmt);
         
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         if (bindValue != null)
         {
            for (int i=0; i < bindValue.length; i++)
            {
               if (bindValue[i] instanceof String)
                  stmt.setString(i+1, (String)bindValue[i]);
               else if (bindValue[i] instanceof Integer)
                  stmt.setInt(i+1, ((Integer)bindValue[i]).intValue());
               else
                  throw new IllegalArgumentException("bindValue[" + i + "] "
                        + "must be either String or Integer type.");
               
               logger.println("              SQL bind value[" + i + "]: "
                     + bindValue[i]);
            }
         }
         int rowCount = stmt.executeUpdate();
         
         logger.println("Successful execute SQL[" + ms_sqlCount + "].");
         return rowCount;
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
            stmt = null;
         }
      }
   }

   /**
    * Counter for tracking the number of SQL statement as part of logging info.
    */
   private int ms_sqlCount = 0;
   
   /**
    * Updates the config name in the old relationship main table for later use.
    * It also saves the user defined relationship names to the relationship 
    * config name table.
    * <p>
    * Note, assumed the system relationship names have already installed by
    * the installer.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the JDBC connection, assumed not <code>null</code>.
    * @param configSet the to be saved relationship configurations, assumed not
    *           <code>null</code>.
    * @param configNames it maps the new config name to old name. Assumned not
    *           <code>null</code>.
    * @param startId the starting id number for the CONFIG_ID column of the
    *   relationship name table. 
    * 
    * @throws Exception if an error occurs.
    */
   private void saveRelationshipConfigNameIds(PrintStream logger,
         Connection conn, PSRelationshipConfigSet configSet,
         Map configNames)
         throws Exception
   {
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Modify the config names in (old) PSX_RELATIONSHIPS table for later use
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      Iterator entries = configNames.entrySet().iterator();
      while (entries.hasNext()) 
      {
         Map.Entry entry = (Map.Entry) entries.next();
         if (!entry.getKey().equals(entry.getValue()))
         {
            String sqlStmt = "UPDATE " + getOldRelMainTable()
               + " SET CONFIG = '" + entry.getKey() + "'"
               + " WHERE CONFIG = '" + entry.getValue() + "'";
            executeUpdate(logger, conn, sqlStmt, null);
         }
      }
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Add a list of unknown ids & names into the name table
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      
      Iterator configs = configSet.iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
         if (config.getId() > PSRelationshipConfig.ID_TRANSLATION_MANDATORY)
         {
            String sqlStmt = "INSERT INTO " + getRelNameTable()
               + " VALUES(?, ?, 0)";
            Object[] bindValue = new Object[2];
            bindValue[0] = new Integer(config.getId());
            bindValue[1] = config.getName();
            
            executeUpdate(logger, conn, sqlStmt, bindValue);
            
            logger.println("Inserted user defined relationship config id/name: "
                  + config.getId() + " / " + config.getName());
         }
      }

      logger.println("Inserted all user defined relationship config names.");
      
   }
   
   /**
    * Adds a list execution contexts to a set of known effects
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, assumed not
    *           <code>null</code>.
    */
   private void addKnownExecutionContexts(PrintStream logger,
         PSRelationshipConfigSet configSet)
   {
      Iterator configs = configSet.iterator();
      PSRelationshipConfig config;
      while (configs.hasNext())
      {
         config = (PSRelationshipConfig) configs.next();
         Iterator effects = config.getEffects();
         while (effects.hasNext())
         {
            PSConditionalEffect effect = (PSConditionalEffect) effects.next();
            PSExtensionCall ext = effect.getEffect();
            String fullName = ext.getExtensionRef().getFQN();

            if (ms_knownExeCtx.get(fullName) != null)
            {
               effect.setExecutionContexts((Collection) ms_knownExeCtx
                     .get(fullName));
               logger.println("Added known execution contexts for '" + fullName
                     + "' effect in '" + config.getName() + "' relationship.");
            }
         }
      }
   }

   /**
    * Fix up the relationship config name, remove the spaces from the
    * relationship config name if exist.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, assumed not
    *           <code>null</code>.
    * 
    * @return a mapper that maps new relationship name to its original (old) 
    *   name, never <code>null</code>.
    */
   private Map fixupRelationshipConfigNameIds(PrintStream logger,
         PSRelationshipConfigSet configSet, int startId)
   {
      Map configName = new HashMap();
      Iterator configs = configSet.iterator();
      PSRelationshipConfig config;
      while (configs.hasNext())
      {
         config = (PSRelationshipConfig) configs.next();
         String oldName = config.getName();
         String newName = oldName.replaceAll(" ", "");
         configName.put(newName, oldName);
         if (!oldName.equals(newName))
         {
            config.setName(newName);
            logger.println("Renamed relationship name from '" + oldName
                  + "' to '" + newName + "'.");
         }
         // assigned id
         config.resetId();
         if (! setPreDefinedSysConfigId(config))
            config.setId(startId++);
      }
      
      return configName;
   }
   
   /**
    * Sets the id for a given system config.
    * @param config the system config, assumed not <code>null</code>.
    */
   private boolean setPreDefinedSysConfigId(PSRelationshipConfig config)
   {
      if (! config.isSystem())
         return false;

      boolean hasSetId = false;
      String name = config.getName();
      if (PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_ACTIVE_ASSEMBLY);
         hasSetId = true;
      }
      else if (PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_ACTIVE_ASSEMBLY_MANDATORY);
         hasSetId = true;
      }
      else if (PSRelationshipConfig.TYPE_FOLDER_CONTENT.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_FOLDER_CONTENT);
         hasSetId = true;
      }
      else if (PSRelationshipConfig.TYPE_NEW_COPY.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_NEW_COPY);
         hasSetId = true;
      }
      else if (PSRelationshipConfig.TYPE_PROMOTABLE_VERSION.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_PROMOTABLE_VERSION);
         hasSetId = true;
      }
      else if (PSRelationshipConfig.TYPE_TRANSLATION.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_TRANSLATION);
         hasSetId = true;
      }
      else if (PSRelationshipConfig.TYPE_TRANSLATION_MANDATORY.equalsIgnoreCase(name))
      {
         config.setId(PSRelationshipConfig.ID_TRANSLATION_MANDATORY);
         hasSetId = true;
      }
      
      return hasSetId;
   }

   /**
    * Removes the 'rs_expirationtime' system property from all relationship
    * configs.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, assumed not
    *           <code>null</code>.
    */
   private void removeExpirationProperty(PrintStream logger,
         PSRelationshipConfigSet configSet)
   {
      Iterator configs = configSet.iterator();
      PSRelationshipConfig config;
      while (configs.hasNext())
      {
         config = (PSRelationshipConfig) configs.next();
         if (config.removeSysProperty("rs_expirationtime"))
            logger.println("Removed 'rs_expirationtime' system property from '"
                  + config.getName() + "' relationship configuration.");
      }
   }

   /**
    * Populate {@link #ms_knownExeCtx}
    */
   private static void initKnownExeCtx()
   {
      ms_knownExeCtx = new HashMap();

      String name = "Java/global/percussion/relationship/effect/sys_TouchParentFolderEffect";
      List exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_CONSTRUCTION));
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_DESTRUCTION));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/fastforward/managednav/rxs_NavFolderEffect";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_CONSTRUCTION));
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_DESTRUCTION));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/relationship/effect/sys_isCloneExists";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_CLONE));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/relationship/effect/sys_PublishMandatory";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_WORKFLOW));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/relationship/effect/sys_UnpublishMandatory";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_WORKFLOW));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/relationship/effect/sys_AttachTranslatedFolder";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_CONSTRUCTION));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/relationship/effect/sys_Promote";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_POST_WORKFLOW));
      ms_knownExeCtx.put(name, exeCtxs);

      name = "Java/global/percussion/relationship/effect/sys_AddCloneToFolder";
      exeCtxs = new ArrayList();
      exeCtxs.add(new Integer(IPSExecutionContext.RS_PRE_CONSTRUCTION));
      ms_knownExeCtx.put(name, exeCtxs);
   }
   
   /**
    * Updates the clone override field of sys_communityid in the NewCopy
    * relationship configuration with the new definition. It does nothing
    * if there are more than one clone override field of sys_communityid.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, assumed not
    *           <code>null</code>.
    */
   private void updateOverrideFieldForCommunityId(PrintStream logger,
         PSRelationshipConfigSet configSet)
   {
      PSRelationshipConfig newCopy = configSet
            .getConfig(PSRelationshipConfig.TYPE_NEW_COPY);
      if (newCopy == null)
         return;  
      
      PSCloneOverrideFieldList src = newCopy.getCloneOverrideFieldList();
      PSCloneOverrideFieldList target = new PSCloneOverrideFieldList();
      
      Iterator srcIt = src.iterator();
      PSCloneOverrideField overrideField;
      boolean isAdded = false;
      while (srcIt.hasNext())
      {
         overrideField = (PSCloneOverrideField) srcIt.next();
         if (overrideField.getName().equalsIgnoreCase(
               IPSHtmlParameters.SYS_COMMUNITYID))
         {
            // replacing 1st and skip the rest.
            if (! isAdded)
            {
               target.add(getOverrideFieldWithCommunityOverride());
               
               isAdded = true;
            }
         }
         else
         {
            target.add(overrideField);
         }
      }
      newCopy.setCloneOverrideFieldList(target);
      
      logger.println("Updated the Clone Override Field of '" 
            + IPSHtmlParameters.SYS_COMMUNITYID 
            + "' for '" + PSRelationshipConfig.TYPE_NEW_COPY 
            + "' type.");      
   }
   
   /**
    * Creates an override field to override sys_communityid with
    * the sys_communityid_override HTML parameter.
    * 
    * @return the override field described above, never <code>null</code>.
    */
   private PSCloneOverrideField getOverrideFieldWithCommunityOverride()
   {
      // create parameters[2], 
      // [0] 1st parameter (as the default) points to the CONTENTSTATUS table
      // [1] 2nd parameter points to the value of HTML Parameter "sys_communityid_override"  
      PSExtensionParamValue[] params = new PSExtensionParamValue[2];
      PSContentItemStatus itemColumn = new PSContentItemStatus("CONTENTSTATUS",
            "COMMUNITYID");
      params[0] = new PSExtensionParamValue(itemColumn);
      PSTextLiteral text = new PSTextLiteral("sys_communityOverride");
      params[1] = new PSExtensionParamValue(text);

      PSExtensionRef extRef = new PSExtensionRef("Java",
            "global/percussion/generic/", "sys_OverrideLiteral");

      PSExtensionCall overrideExt = new PSExtensionCall(extRef, params);

      PSCloneOverrideField commOverrideField = new PSCloneOverrideField(
            IPSHtmlParameters.SYS_COMMUNITYID, overrideExt);

      return commOverrideField;
   }   

   /**
    * This method will go through all relationship configs with Active Assembly
    * category, and add the required user defined properties should any of them
    * are missing in the configs. The required user defined properties are from
    * {@link PSRelationshipConfig#getPreDefinedUserPropertyNames()}.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, assumed not
    *           <code>null</code>.
    */
   protected void updateActiveAssemblyProperties(PrintStream logger,
         PSRelationshipConfigSet configSet)
   {
      final Collection rqdNames = PSRelationshipConfig.getPreDefinedUserPropertyNames();

      Iterator aaConfigs = configSet.getConfigListByCategory(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY).iterator();
      while (aaConfigs.hasNext())
      {
         PSRelationshipConfig config = (PSRelationshipConfig) aaConfigs.next();
         Set userPropNames = config.getUserProperties().keySet();
         if (!userPropNames.containsAll(rqdNames))
         {
            // get the missing names
            List missingNames = new ArrayList(rqdNames);
            missingNames.removeAll(userPropNames);

            // get the current properties
            PSPropertySet userProps = new PSPropertySet();
            Iterator props = config.getUserDefProperties();
            while (props.hasNext())
               userProps.add(props.next());

            // add the missing properties
            Iterator names = missingNames.iterator();
            while (names.hasNext())
            {
               String propName = (String) names.next();
               PSProperty prop = new PSProperty(propName);
               userProps.add(prop);
               
               logger.println("Added (required) user defined property '"
                     + propName + "' for '" + config.getName() + "' type.");      
            }
            
            // save the updated properties
            config.setUserDefProperties(userProps.iterator());
         }
      }
   }
   
   /**
    * This method will go through all property values for the specified property
    * name in the old relationship properties table {@link #getOldRelPropTable()}
    * and remove any rows in which the value is non-numeric, may be
    * <code>null</code>.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the database connection object, assumed not
    *           <code>null</code>.
    * @param property the name of the property for which to remove invalid
    *           values, assumed not <code>null</code>. 
    *           
    * @throws Exception if an error occurs.
    */
   private void removeNonNumericPropertyValues(PrintStream logger,
         Connection conn, String property)
      throws Exception
   {
      List invalidRows = new ArrayList();
      PreparedStatement stmt = null;
      ResultSet rs = null;
      int rowCount = 0;
      
      try
      {
         logger.println("Searching for invalid " + property + " property values "
               + "in table '" + getOldRelPropTable() + "'");
         String sqlStmt = "SELECT RID,PROPERTYVALUE FROM " + getOldRelPropTable()
                        + " WHERE PROPERTYNAME='" + property + "'";
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         
         while (rs.next())
         {
            int rid = rs.getInt(1);
            String value = rs.getString(2);
            if (value == null)
               continue;
            
            try
            {
               Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
               invalidRows.add(new Integer(rid));
            }
         }
         
         for (int i = 0; i < invalidRows.size(); i++)
         {
            int invalidRID = ((Integer) invalidRows.get(i)).intValue();
            sqlStmt = "DELETE FROM " + getOldRelPropTable() + " WHERE "
                    + "RID=" + invalidRID + " AND PROPERTYNAME='" + property
                    + "'";
                     
            logger.println("***Invalid " + property + " property value found "
                  + "for RID=" + invalidRID + ", this row will be removed***");
            rowCount += executeUpdate(logger, conn, sqlStmt, null);
         }
         
         logger.println("Successfully removed " + rowCount + " row(s) from "
               + "table '" + getOldRelPropTable() + "'");
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
            stmt = null;
         }
      }
   }
   
   /**
    * This method will look for all relationship type parameter names in the
    * RXMENUACTIONPARAM table.  For each of these parameters, any whitespace
    * found in the parameter value will be removed.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param conn the database connection object, assumed not
    *           <code>null</code>.
    * @param configNames the new config name maps to the old config name, 
    *    assumed not <code>null</code>.
    *           
    * @throws Exception if an error occurs.
    */
   private void updateMenuActionParamTable(PrintStream logger,
         Connection conn, Map configNames)
      throws Exception
   {
      Map updateVals = new HashMap();
      PreparedStatement stmt = null;
      ResultSet rs = null;
      int rowCount = 0;
      
      try
      {
         logger.println("Searching for relationship type property parameters "
               + "in table '" + getMenuActionParamTable() + "'");
         String sqlStmt = "SELECT ACTIONID,PARAMVALUE FROM "
                        + getMenuActionParamTable() + " "
                        + "WHERE PARAMNAME='" + SYS_RELATIONSHIP_TYPE + "'";
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         
         while (rs.next())
         {
            int actionid = rs.getInt(1);
            String value = rs.getString(2);
            if (value == null)
               continue;
            
            String newValue = value.replaceAll(" ", "");
            if (newValue.equals(value) || configNames.get(newValue) == null)
               continue;
            
            updateVals.put(new Integer(actionid), newValue);
         }
         
         Set keys = updateVals.keySet();
         Iterator iter = keys.iterator();
         while (iter.hasNext())
         {
            Integer aid = (Integer) iter.next();
            String val = (String) updateVals.get(aid);
            sqlStmt = "UPDATE " + getMenuActionParamTable() + " "
                    + "SET PARAMVALUE='" + val + "' "
                    + "WHERE ACTIONID=" + aid.intValue() + " AND "
                    + "PARAMNAME='" + SYS_RELATIONSHIP_TYPE + "'";
                     
            rowCount += executeUpdate(logger, conn, sqlStmt, null);
         }
         
         logger.println("Successfully updated " + rowCount + " row(s) in "
               + "table '" + getMenuActionParamTable() + "'");
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
            stmt = null;
         }
      }
   }
   
   /**
    * The properties contains database information such as 'DB_NAME',
    * 'DB_SCHEMA' and 'DB_DRIVER_NAME'. It is initialized at the beginning of
    * the {@link #process(IPSUpgradeModule, Element)} method.
    */
   protected Properties m_dbProps = null;
   
   /**
    * It maps the effect (full) name to its known execution contexts. Init when
    * the class is loaded.
    */
   private static Map ms_knownExeCtx;

   static
   {
      initKnownExeCtx();
   }

}
