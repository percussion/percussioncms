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

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSDataSourceFactory;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This base class takes care of the details of initializing the spring
 * configuration for upgrade plugins.
 * 
 * @author dougrand
 */
public abstract class PSSpringUpgradePluginBase implements IPSUpgradePlugin
{

    /**
    * String constant for spring directory path relative to Rhythmyx root
    */
   private static final String SPRING_DIRECTORY = PSServletUtils.getSpringConfigPath();



   
   /**
    * Ctor, make sure your ctor calls super()
    */
   protected PSSpringUpgradePluginBase()
   {
      initializeSpringConfiguration();
   }
   
   /**
    * Initialize the configuration by telling the base service locator to 
    * load the configuration from the given spring directory. Note that once
    * done, this method will simply return. This can be used by unit tests
    * to their advantage. The unit test can cause the locator to be initialized,
    * which will prevent this code from running.
    */
   protected void initializeSpringConfiguration()
   {
      if (PSBaseServiceLocator.isInitialized())
      {
         return;
      }
      
      String rxRoot = PathUtils.getRxDir(null).getAbsolutePath();
      File spring = new File(rxRoot,PSServletUtils.getSpringConfigPath());
      File beansXml = new File(spring + "/beans.xml");
      File deployerBeansXml = new File(spring
            + "/deployer-beans.xml");
      File installBeansXml = new File(spring
            + "/install-beans.xml");
            
      //Initialize the install beans file with repository info on upgrade
      initializeInstallBeans(installBeansXml);
            
      String[] files = new String[] {installBeansXml.getAbsolutePath(),
            beansXml.getAbsolutePath(), deployerBeansXml.getAbsolutePath()};
            
      PSBaseServiceLocator.initCtxHib(files, rxRoot);
   }
   
   /**
    * Initialize the 'install-beans.xml' spring configuration file used by the
    * installer during upgrade.  Modifies the file accordingly to use current
    * installation's repository information.
    * 
    * @param installBeansXml the spring configuration file, assumed not 
    * <code>null</code>
    */
   private void initializeInstallBeans(File installBeansXml)
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      try 
      {
         in = new FileInputStream(installBeansXml);
         
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
               in, false);
         
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade.getRxRepositoryProps());
                           
         NodeList nodeList = doc.getElementsByTagName(BEAN_TAGNAME);
         for (int i = 0; i < nodeList.getLength(); i++)
         {
            Element node = (Element) nodeList.item(i);
            String id = node.getAttribute(ID_ATTR);
            if (id.equals(PROTOLEGDS))
            {
               //Must modify the driver class name and url props
               NodeList propNodes = node.getElementsByTagName(PROP_TAGNAME);
               for (int idx = 0; idx < propNodes.getLength(); idx++)
               {
                  Element propNode = (Element) propNodes.item(idx);
                  String key = propNode.getAttribute(KEY_ATTR);
                                    
                  if (key.equals(PSDataSourceFactory.DRIVER_CLASS_PROP_NAME))
                  {
                     propNode.setTextContent(dbmsDef.getDriverClassName());
                  }
                                    
                  if (key.equals(PSDataSourceFactory.URL_PROP_NAME))
                  {
                     String url = PSJdbcUtils.getJdbcUrl(dbmsDef.getDriver(),
                              dbmsDef.getServer());

                     //Must add user, pass to url for jtds based drivers
                     if (dbmsDef.getDriver().equals(PSJdbcUtils.JTDS_DRIVER))
                     {
                        url += ";" + "user=" + dbmsDef.getUserId() + ";" +
                        "password=" + dbmsDef.getPassword();
                     }
                                       
                     propNode.setTextContent(url);
                  }
                  
                  if (key.equals(PSDataSourceFactory.USER_PROP_NAME))
                     propNode.setTextContent(dbmsDef.getUserId());
                  
                  if (key.equals(PSDataSourceFactory.PWD_PROP_NAME))
                     propNode.setTextContent(dbmsDef.getPassword());
                  
                  if (key.equals(PSDataSourceFactory.DB_PROP_NAME))
                     propNode.setTextContent(dbmsDef.getDataBase());
                  
                  if (key.equals(PSDataSourceFactory.DRIVER_LOC_PROP_NAME))
                  {
                      if (PSJdbcUtils.isExternalDriver(dbmsDef.getDriver()))
                      {
                          propNode.setTextContent(RxUpgrade.getRxRoot() + PSJdbcUtils.MYSQL_DRIVER_LOCATION);
                      }
                  }
               }
            }
            
            if (id.equals(DSRESOLVER))
            {
               //Must modify database, origin
               NodeList propertyNodes = node.getElementsByTagName(PROPERTY_TAGNAME);
               
               for (int idx2 = 0; idx2 < propertyNodes.getLength(); idx2++)
               {
                  Element propertyNode = (Element) propertyNodes.item(idx2);
                  String name = propertyNode.getAttribute(NAME_ATTR);
                  
                  if (name.equals(DATABASE_VAL))
                     propertyNode.setAttribute(VAL_ATTR, dbmsDef.getDataBase());
                     
                  if (name.equals(ORIGIN_VAL))
                     propertyNode.setAttribute(VAL_ATTR, dbmsDef.getSchema());
               }
            }
         }
         
         out = new FileOutputStream(installBeansXml);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
         System.err.println("PSSpringUpgradePluginBase: could not find " +
               "install-beans.xml");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.err.println("PSSpringUpgradePluginBase: error encountered");
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();
            
            if (out != null)
               out.close();
         }
         catch (Exception e)
         {
            
         }
         
      }
   }
   
   /**
    * Xml tag, attribute, value constants 
    */
   
   private static final String BEAN_TAGNAME = "bean";
   private static final String CLASS_ATTR = "class";
   private static final String ID_ATTR = "id";
   private static final String PROP_TAGNAME = "prop";
   private static final String PROPERTY_TAGNAME = "property";
   private static final String KEY_ATTR = "key";
   private static final String NAME_ATTR = "name";
   private static final String VAL_ATTR = "value";
   private static final String DATABASE_VAL = "database";
   private static final String ORIGIN_VAL = "origin";
   private static final String PROTOLEGDS = "sys_protoLegacyDataSource";
   private static final String DSRESOLVER = "sys_datasourceResolver";
   
}
