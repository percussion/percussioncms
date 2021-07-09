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

import com.percussion.util.PSPreparedStatement;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Upgrades the publisher spring beans file.  This includes adding properties
 * for timeout, enable passive mode, and umask to the ftp and sftp delivery
 * handler configurations if specified in the publisher config table.
 */
public class PSUpgradePluginPublisherBeans implements IPSUpgradePlugin
{
   /**
    * <code>elemData</code> element must define the following attributes:
    * <ul>
    * <li>publisher-bean-path: relative path to the publisher beans file to
    * update</li>
    * </ul>
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      ms_logger = config.getLogStream();
      File rxRoot = new File(RxUpgrade.getRxRoot());
      String beanpath = elemData.getAttribute("publisher-bean-path");
      
      File target = new File(rxRoot, beanpath);
      File backup = new File(rxRoot, beanpath.replace(".xml", ".bak"));
     
      try
      {
         if (upgradeConfig(target, backup))
         {
            return new PSPluginResponse(PSPluginResponse.SUCCESS, "done");
         }
         else
         {
            return new PSPluginResponse(PSPluginResponse.WARNING,
                  "Failed update");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace(config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e
               .getLocalizedMessage());
      }

   }

   /**
    * Upgrade the configuration.
    * 
    * @param target the target file.  Must be non-<code>null</code> and must
    * exist.
    * @param backup a backup of the current target will be copied to this
    * location, overwriting the current file if it exists.
    * 
    * @return <code>true</code> if this succeeds.
    * 
    * @throws Exception If an error occurs.
    */
   private boolean upgradeConfig(File target, File backup)
         throws Exception
   {
      if (target == null)
      {
         throw new IllegalArgumentException("target may not be null");
      }
      if (backup == null)
      {
         throw new IllegalArgumentException("backup may not be null");
      }
      if (!target.exists())
      {
         throw new IllegalArgumentException("target must exist");
      }
      
      int maxTimeout = 0;
      boolean enablePassiveMode = false;
      String umaskval = null;
      Connection conn = null;
      FileInputStream in = null;
      FileOutputStream out = null;
      PSConnectionObject connObj = new PSConnectionObject();
            
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         connObj.setConnection(conn);
                 
         // Find the max timeout value
         maxTimeout = findMaxTimeout(conn);
         
         // Find an enable passive mode value of 'true'
         enablePassiveMode = isPassiveModeEnabled(conn);
         
         // Find a umask value
         umaskval = findUmask(conn);
                  
         if (maxTimeout > 0 || enablePassiveMode || umaskval != null)
         {
            // Move the current bean file to the backup
            IOUtils.copy(new FileInputStream(target), 
                  new FileOutputStream(backup));

            // Update the publisher beans file
            in = new FileInputStream(target);
            Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  in, false);

            if (maxTimeout > 0)
               updateFtpHandlersTimeout(doc, maxTimeout);
            
            if (enablePassiveMode)
               updateFtpHandlerPassiveMode(doc, enablePassiveMode);
            
            if (umaskval != null)
               updateSftpHandlerUmask(doc, umaskval);
            
            if (in != null)
            {
               try
               {
                  in.close();
                  in = null;
               }
               catch (Exception e)
               {
               }
            }

            out = new FileOutputStream(target);
            PSXmlDocumentBuilder.write(doc, out);
         }
         
         return true;
      }
      finally
      {
         connObj.close();
         
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception e)
            {
            }
         }
         
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (Exception e)
            {
            }
         }
      }
   }

   /**
    * Finds the ftp and sftp handler elements in the given document and updates
    * their timeout attribute values.
    * 
    * @param doc The xml document object, assumed not <code>null</code>.
    * @param newTimeout This value will be used as the new timeout value for
    * the ftp and sftp handlers. 
    */
   private void updateFtpHandlersTimeout(Document doc, int newTimeout)
   {
      NodeList nodeList = doc.getElementsByTagName(BEAN_TAGNAME);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Element node = (Element) nodeList.item(i);
         String id = node.getAttribute(ID_TAGNAME);
         if (id.equals(FTP_HANDLER_NAME) || id.equals(SFTP_HANDLER_NAME))
         {
            if (!propertyExists(node, TIMEOUT_VALUE))
            {
               Element mtProp = PSXmlDocumentBuilder.addEmptyElement(
                     doc, node, PROPERTY_TAGNAME);
               mtProp.setAttribute(NAME_ATTR, TIMEOUT_VALUE);
               mtProp.setAttribute(VAL_ATTR, 
                     String.valueOf(newTimeout));
            }
         }
      }
   }
   
   /**
    * Finds the ftp handler element in the given document and updates its
    * enable passive mode attribute value.
    * 
    * @param doc The xml document object, assumed not <code>null</code>.
    * @param enablePassiveMode This value will be used as the new enable
    * passive mode value for the ftp handler.
    */
   private void updateFtpHandlerPassiveMode(Document doc, 
         boolean enablePassiveMode)
   {
      NodeList nodeList = doc.getElementsByTagName(BEAN_TAGNAME);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Element node = (Element) nodeList.item(i);
         String id = node.getAttribute(ID_TAGNAME);
         if (id.equals(FTP_HANDLER_NAME))
         {
            if (!propertyExists(node, PASSIVE_MODE_VALUE))
            {
               Element epmProp = PSXmlDocumentBuilder.addEmptyElement(
                     doc, node, PROPERTY_TAGNAME);
               epmProp.setAttribute(NAME_ATTR, PASSIVE_MODE_VALUE);
               epmProp.setAttribute(VAL_ATTR, String.valueOf(
                     enablePassiveMode));
               break;
            }
         }   
      }
   }
   
   /**
    * Finds the sftp handler element in the given document and updates its
    * umask attribute value.
    * 
    * @param doc The xml document object, assumed not <code>null</code>.
    * @param umaskval This value will be used as the new umask value for the
    * sftp handler.
    */
   private void updateSftpHandlerUmask(Document doc, String umaskval)
   {
      NodeList nodeList = doc.getElementsByTagName(BEAN_TAGNAME);
      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Element node = (Element) nodeList.item(i);
         String id = node.getAttribute(ID_TAGNAME);
         if (id.equals(SFTP_HANDLER_NAME))
         {
            if (!propertyExists(node, UMASK_VALUE))
            {
               Element epmProp = PSXmlDocumentBuilder.addEmptyElement(
                     doc, node, PROPERTY_TAGNAME);
               epmProp.setAttribute(NAME_ATTR, UMASK_VALUE);
               epmProp.setAttribute(VAL_ATTR, umaskval);
               break;
            }
         }   
      }
   }
   
   /**
    * Determines if a property child element exists with the given name for the
    * given element.
    * 
    * @param elem The element under which to search, assumed not 
    * <code>null</code>.
    * @param name The name of the property child element, assumed not
    * <code>null</code>.
    * 
    * @return <code>true</code> if the element exists, <code>false</code>
    * otherwise.
    */
   private boolean propertyExists(Element elem, String name)
   {
      boolean propExists = false;
      NodeList propElems = elem.getElementsByTagName(PROPERTY_TAGNAME);
      for (int i = 0; i < propElems.getLength(); i++)
      {
         Element propElem = (Element) propElems.item(i);
         String attr = propElem.getAttribute(NAME_ATTR);
         if (attr.trim().length() > 0 &&
               attr.equals(name))
         {
            propExists = true;
            break;
         }
      }
      
      return propExists;
   }
   
   /**
    * Finds the publisher configuration maximum timeout value if one has been
    * specified.
    *  
    * @param conn Database connection, assumed not <code>null</code>.
    * 
    * @return The maximum timeout value in milliseconds greater than zero or -1
    * if one was not found.
    */
   private int findMaxTimeout(Connection conn)
   {
      int maxTimeout = -1; 
      PreparedStatement stmt = null;
      ResultSet rs = null;
      PSConnectionObject connObj = new PSConnectionObject();
      
      try
      {
         String sqlStmt = "SELECT PARAMVALUE FROM " + getPublisherConfigTable()
               + " WHERE PARAMNAME='ftprcvtimeout'";
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
        
         int timeoutvals = 0;

         while (rs.next())
         {
            String value = rs.getString(1);
            if (value == null)
               continue;

            int timeout = -1;
            try
            {
               // Convert from seconds to milliseconds
               timeout = Integer.parseInt(value) * 1000;
            }
            catch (NumberFormatException e)
            {
               continue;
            }

            if (timeout != 0 && timeout > maxTimeout)
               maxTimeout = timeout;

            timeoutvals++;
         }

         if (maxTimeout > 0 && timeoutvals > 1)
         {
            ms_logger.println("WARNING: Multiple publisher timeout values found. "
                  + " Using " + maxTimeout + "ms");
         }
      }
      catch (Exception e)
      {
         ms_logger.println("ERROR: encountered while determining maximum "
               + "timeout value.");
         e.printStackTrace(ms_logger);
      }
      finally
      {
         connObj.close();
      }
      
      return maxTimeout;
   }
   
   /**
    * Determines if a publisher configuration enable passive mode value has
    * been specified and set to 'true.'
    *  
    * @param conn Database connection, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if passive mode has been enabled for a publisher
    * configuration, <code>false</code> otherwise.
    */
   private boolean isPassiveModeEnabled(Connection conn)
   {
      boolean enablePassiveMode = false;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      PSConnectionObject connObj = new PSConnectionObject();
      
      try
      {
         String sqlStmt = "SELECT PARAMVALUE FROM " + getPublisherConfigTable()
            + " WHERE PARAMNAME='enablepassivemode'";
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
      
         while (rs.next())
         {
            String value = rs.getString(1);
            if (value == null)
               continue;

            if ("true".equalsIgnoreCase(value))
            {
               enablePassiveMode = true;
               break;
            }
         }
      }
      catch (Exception e)
      {
         ms_logger.println("ERROR: encountered while determining if passive "
               + "mode is enabled.");
         e.printStackTrace(ms_logger);
      }
      finally
      {
         connObj.close();
      }
      
      return enablePassiveMode;
   }
   
   /**
    * Finds the publisher configuration umask value if one has been specified.
    *  
    * @param conn Database connection, assumed not <code>null</code>.
    * 
    * @return The first non-empty umask value found, <code>null</code>
    * otherwise.
    */
   private String findUmask(Connection conn)
   {
      String umaskval = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      PSConnectionObject connObj = new PSConnectionObject();
      
      try
      {
         String sqlStmt = "SELECT PARAMVALUE FROM " + getPublisherConfigTable()
            + " WHERE PARAMNAME='umask'";
         stmt = PSPreparedStatement.getPreparedStatement(conn, sqlStmt);
         rs = stmt.executeQuery();
         connObj.setStatement(stmt);
         connObj.setResultSet(rs);
       
         int umaskvals = 0;
                  
         while (rs.next())
         {
            String value = rs.getString(1);
            if (value == null)
               continue;

            if (umaskval == null)
               umaskval = value;
            
            umaskvals++;
         }
         
         if (umaskvals > 1)
         {
            ms_logger.println("WARNING: Multiple umask values found.  Using "
                  + umaskval);
         }
      }
      catch (Exception e)
      {
         ms_logger.println("ERROR: encountered while determining umask value.");
         e.printStackTrace(ms_logger);
      }
      finally
      {
         connObj.close();
      }
      
      return umaskval;
   }
   
   /**
    * Get the pre-6.6 publisher configuration table.
    * 
    * @return The fully qualified table name of the publisher configuration
    * table (pre-6.6).
    * 
    * @throws Exception if an error occurs qualifying the table name.
    */
   private String getPublisherConfigTable() throws Exception
      
   {
      return RxUpgrade.qualifyTableName("RXPUBLISHERCONFIG");
   }
   
   /**
    * Xml tag, attribute, value constants 
    */
   
   private static final String BEAN_TAGNAME = "bean";
   private static final String ID_TAGNAME = "id";
   private static final String PROPERTY_TAGNAME = "property";
   private static final String NAME_ATTR = "name";
   private static final String VAL_ATTR = "value";
   private static final String FTP_HANDLER_NAME = "sys_ftpDeliveryHandler";
   private static final String SFTP_HANDLER_NAME = "sys_sftpDeliveryHandler";
   private static final String TIMEOUT_VALUE = "timeout";
   private static final String PASSIVE_MODE_VALUE = "usePassiveMode";
   private static final String UMASK_VALUE = "umask";
   
   /**
    * Used for logging output to the plugin log file, initialized in
    * {@link #process(IPSUpgradeModule, Element)}. 
    */
   private static PrintStream ms_logger;
      
}
