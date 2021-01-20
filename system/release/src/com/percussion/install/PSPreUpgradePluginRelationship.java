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

import com.percussion.design.objectstore.PSConditionalExtension;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This plugin is used to validate relationship data before upgrade from
 * pre-Rhino to Rhino+ releases.
 */
public class PSPreUpgradePluginRelationship implements IPSUpgradePlugin
{
   /**
    * Default Constructor.
    */
   public PSPreUpgradePluginRelationship()
   {
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
      logger.println("Running Validating Relationships plugin");

      Connection conn = null;
      boolean successValidated = true;
      String log = module.getLogFile();
      int respType = PSPluginResponse.SUCCESS;
      String respMessage = null;
      try
      {
         PSUpgradePluginRelationship util = new PSUpgradePluginRelationship();
         util.setDbProperties(RxUpgrade.getRxRepositoryProps());
         conn = RxUpgrade.getJdbcConnection();

         Document doc = util.getRelationshipConfigs(logger, conn);
         PSRelationshipConfigSet configs = util.getConfigSet(doc);

         successValidated = validateExtensionSet(logger, doc);

         if (! validateEmptyExpiretime(logger, configs))
            successValidated = false;
         
         if (!validateRelationshipName(logger, configs, conn, util))
            successValidated = false;
         
         if (!validateUnknownRelationshipProperties(logger, configs, conn, util))
            successValidated = false;
         
         if (successValidated)
         {
            respType = PSPluginResponse.SUCCESS;
            respMessage = "Successfully validated relationship data.";
         }
         else
         {
            respType = PSPluginResponse.WARNING;
            respMessage = "Finished validating relationship data, see the "
                  + "\"" + log + "\" located in " + RxUpgrade.getPreLogFileDir()
                  + " for warnings during this process.";
         }
      }
      catch (Exception e)
      {
         successValidated = false;
         logger.println("Caught exception: " + e.getMessage());
         e.printStackTrace(logger);
         
         respType = PSPluginResponse.EXCEPTION;
         respMessage = "<<<ERROR>>>: Failed to validate relationship data, see "
            + "the \"" + log + "\" located in " + RxUpgrade.getPreLogFileDir()
            + " for errors.";
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

         if (successValidated)
            logger.println("leaving the process() of the Validating Relationships plugin, successfully validated without warning or error.");
         else
            logger.println("leaving the process() of the Validating Relationships plugin with WARNINGs.");
      }

      return new PSPluginResponse(respType, respMessage);
   }

   /**
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, never <code>null</code>.          
    * @param conn the database connection, assumed not <code>null</code>.
    * @param util the utility object which contains convenience methods,
    *    assumed not <code>null</code>.
    *  
    * @return <code>true</code> if there is no undefined relationship names;
    *    otherwise, return <code>false</code>.
    * 
    * @throws Exception if an error occurs during the validation.
    */
   private boolean validateUnknownRelationshipProperties(PrintStream logger,
         PSRelationshipConfigSet configSet, Connection conn,
         PSUpgradePluginRelationship util) throws Exception
   {
      boolean isValidated = true;

      Iterator configs = configSet.iterator();
      while (configs.hasNext())
      {
         if (! validateUnknownRelationshipProperties(logger, 
               (PSRelationshipConfig) configs.next(), conn, util))
         {
            isValidated = false;
         }
      }
      
      return isValidated;
   }

   /**
    * Just like {@link #validateUnknownRelationshipProperties(PrintStream, 
    * PSRelationshipConfigSet, Connection, PSUpgradePluginRelationship)}
    * except this validates one relationship configuration.
    */
   private boolean validateUnknownRelationshipProperties(PrintStream logger,
         PSRelationshipConfig config, Connection conn,
         PSUpgradePluginRelationship util) throws Exception
   {
      // collect all property names, both system and user
      Collection pnames = getPropertyNames(config.getSysProperties());
      pnames.addAll(getPropertyNames(config.getUserDefProperties()));
      
      // query the database, see if there is additional property names
      // in the properties table, but not specified in the configuration
      
      // build the name IN clause
      StringBuffer buf = new StringBuffer();
      Iterator pnamesIt = pnames.iterator();
      while (pnamesIt.hasNext())
      {
         if (buf.length() != 0)
            buf.append(",");
         buf.append("'");
         buf.append((String) pnamesIt.next());
         buf.append("'");
      }
      
      String sqlStmt = "select distinct p.PROPERTYNAME from " 
         + util.getOldRelPropTable() + " p, "
         + util.getOldRelMainTable() + " r "
         + "where r.RID = p.RID and r.CONFIG = '" + config.getName() + "' "
         + "and p.PROPERTYNAME not in (" + buf.toString() + ")";
                     
      List unknownNames = util.queryStringList(logger, conn, sqlStmt);
      
      // remove all required property names for Active Assembly config
      if ((!unknownNames.isEmpty()) && config.getCategory().equalsIgnoreCase(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
      {
         unknownNames.removeAll(PSRelationshipConfig
               .getPreDefinedUserPropertyNames());
      }
      
      if (!unknownNames.isEmpty())
      {
         StringBuffer msgBuffer = new StringBuffer();
         msgBuffer.append("Relationship config '" + config.getName() + 
               "' contains unknown properties. This warning may be ignored if " 
               + "the unknown properties are not used; otherwise they must be " 
               + "specified in the relationship configuration before the " 
               + "upgrade. The unknown properties are: ");
         Iterator names = unknownNames.iterator();
         boolean firstTime = true;
         while (names.hasNext())
         {
            if (!firstTime)
               msgBuffer.append(",");
            msgBuffer.append("'" + (String) names.next() + "'");
            firstTime = false;
         }
         msgBuffer.append("");

         logWarningMsg(logger, msgBuffer.toString());
         
         return false;
      }
      else
      {
         return true;
      }
   }
   
   /**
    * Gets the property names from the given property list.
    * 
    * @param props a list of zero or more {@link PSProperty} element, never
    *    <code>null</code>, but may be empty. 
    * 
    * @return the property names, never <code>null</code>.
    */
   private Collection getPropertyNames(Iterator props)
   {
      List names = new ArrayList();
      PSProperty prop;
      while (props.hasNext())
      {
         prop = (PSProperty) props.next();
         names.add(prop.getName());
      }
      return names;
   }
   
   /**
    * Checks the 'ExtensionSet' element from the supplied document. Make sure
    * the element does not have any unknown relationship extensions; otherwise 
    * log the error since the relationship extension is an unsupported feature.
    * It skips the known extension, 'sys_TranslationConstraint', if exists.
    *
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param doc the document contains all relationship configurations. Assumed
    *   not <code>null</code>.
    *
    * @return <code>true</code> if there is no defined pre/post relationship
    *   extensions; otherwise, return <code>false</code>.
    */
   private boolean validateExtensionSet(PrintStream logger, Document doc)
   {
      boolean emptyExtensions = true;
      NodeList extensions = doc.getDocumentElement().getElementsByTagName(
            "ExtensionSet");
      int length = extensions.getLength();
      for (int i=0; i<length; i++)
      {
         Element currEl = (Element) extensions.item(i);
         PSConditionalExtension ext = getUnknownExit(logger, currEl);
         if (ext != null)
         {
            logWarningMsg(logger, "Found an unknown relationship extension, '"
                  + ext.getExtension().getName()
                  + "', which is no longer supported. This warning may be ignored" 
                  + " if this relationship extension is not used; otherwise it "
                  + "must be re-implemented with relationship effects before the "
                  + "upgrade. The unknown extension is defined in: "
                  + PSXmlDocumentBuilder.toString(currEl));
            emptyExtensions = false;
         }
      }

      return emptyExtensions;
   }
   
   /**
    * Logs the specified warning message.
    * 
    * @param logger the logger, assumed not <code>null</code>.
    * @param msg the warning message, assumed not <code>null</code> or empty.
    */
   private void logWarningMsg(PrintStream logger, String msg)
   {
      logger.println("\n<<<WARNING>>>: " + msg + "\n");
   }
   
   
   
   /**
    * Gets an unknown relationship extension from the specified ExtensionSet.
    *  
    * @param logger the logger, assumed not <code>null</code>.
    * @param exitSetNode the extension set node, may be <code>null</code> or 
    *    empty.
    *    
    * @return an unknown relationship extension if found one; otherwise
    *    return <code>null</code>. 
    */
   private PSConditionalExtension getUnknownExit(PrintStream logger, 
      Element exitSetNode)
   {
      NodeList chidNodes = exitSetNode.getChildNodes();
      if (chidNodes == null || chidNodes.getLength() == 0)
         return null;
      
      PSConditionalExtension extension;
      Element childEl = PSXMLDomUtil.getFirstElementChild(exitSetNode);
      while (childEl != null)
      {
         try
         {
            extension = new PSConditionalExtension(childEl, null, null);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // unknown format, skip it
            continue;
         }
         if (extension.getExtension().getName().equalsIgnoreCase(KNOWN_EXIT))
         {
            logger.println("Found the known relationship extension, '"
                        + KNOWN_EXIT
                        + "', which will be removed, and will be replaced by 'sys_isCloneExists' effect after the upgrade process.");
         }
         else
         {
            return extension;
         }

         childEl = PSXMLDomUtil.getNextElementSibling(childEl);
      }
  
      return null;
   }
   
   
   /**
    * The known relationship extension, which is replaced with the 
    * 'sys_isCloneExists' effect if exist. 
    */
   private final static String KNOWN_EXIT = "sys_TranslationConstraint";
   
   /**
    * Validates if the 'rs_expirationtime' system property is empty for all
    * relationship configurations. This property has never been used by the
    * system and it must be removed after upgrade. 
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configSet all relationship configurations, never <code>null</code>.          
    *           
    * @return <code>true</code> if the 'rs_expirationtime' system property
    *   of all relationships are empty; otherwise, return <code>false</code>.
    */
   private boolean validateEmptyExpiretime(PrintStream logger,
         PSRelationshipConfigSet configSet)
   {
      boolean emptyExpiretime = true;
      
      Iterator configs = configSet.iterator();
      PSRelationshipConfig config;
      while (configs.hasNext())
      {
         config = (PSRelationshipConfig) configs.next();
         PSProperty prop = config.getSysProperty("rs_expirationtime");
         if (prop != null)
         {
            String value = (String) prop.getValue();
            if (value != null && value.trim().length() > 0)
            {
               logWarningMsg(logger,
                     "The 'rs_expirationtime' system property is not "
                           + "empty in configuration '" + config.getName()
                           + "'. This warning may be ignored if this system " 
                           + "property is not used; otherwise it must be " 
                           + "re-implemented with a user defined property before " 
                           + "the upgrade since this system property will be "
                           + "removed after the upgrade.");
               emptyExpiretime = false;
            }
         }
      }
      
      return emptyExpiretime;
   }
   
   /**
    * Validates if there are any undefined relationship configuration names,
    * which exist in the relationship main table, but does not exist in the
    * relationship configurations.
    * 
    * @param logger the logger used to log messages, assumed not
    *           <code>null</code>.
    * @param configs all relationship configurations, never <code>null</code>.          
    * @param conn the database connection, assumed not <code>null</code>.
    * @param util the utility object which contains convenience methods,
    *    assumed not <code>null</code>.
    *  
    * @return <code>true</code> if there is no undefined relationship names;
    *    otherwise, return <code>false</code>.
    * 
    * @throws Exception if an error occurs during the validation.
    */
   private boolean validateRelationshipName(PrintStream logger,
         PSRelationshipConfigSet configs, Connection conn,
         PSUpgradePluginRelationship util) throws Exception
   {
      boolean isValidated = true;
      String sqlStmt = "SELECT DISTINCT CONFIG FROM "
            + util.getOldRelMainTable();
      
      List rs = util.queryStringList(logger, conn, sqlStmt);
      Iterator names = rs.iterator();
      while (names.hasNext())
      {
         String name = (String) names.next();
         if (configs.getConfig(name) == null)
         {
            logWarningMsg(logger, "Found undefined relationship config name, '"
                  + name + "'. This warning may be ignored if the relationship " 
                  + "instances of this type are no longer needed; otherwise " 
                  + "specify (or define) this relationship configuration before " 
                  + "the upgrade.");
            isValidated = false;
         }
      }
      
      return isValidated;
   }
}
