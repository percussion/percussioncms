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

package com.percussion.ant.install;

import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSSecurityConfiguration;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.legacy.IPSComponentConverter;
import com.percussion.design.objectstore.legacy.IPSConfigFileLocator;
import com.percussion.design.objectstore.legacy.IPSRepositoryInfo;
import com.percussion.design.objectstore.legacy.PSBackendTableConverter;
import com.percussion.design.objectstore.legacy.PSConfigurationCtx;
import com.percussion.design.objectstore.legacy.PSInstConfigFileLocator;
import com.percussion.design.objectstore.legacy.PSInstRepositoryInfo;
import com.percussion.design.objectstore.legacy.PSSecurityProviderConverter;
import com.percussion.design.objectstore.legacy.PSTableLocatorConverter;
import com.percussion.install.PSUpgradeDbAndHtmlAndXslFilesForSlotNames;
import com.percussion.install.PSLogger;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import com.percussion.util.PSProperties;
import com.percussion.xml.PSXmlDocumentBuilder;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * PSModifyProviders is a class which modifies legacy security providers
 * to conform to the newly supported configurations.  It also utilizes an
 * instance of RxISConfigureAppsDefs to convert the Applications, Defs, and
 * server configurations accordingly.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="modifyProviders"
 *              class="com.percussion.ant.install.PSModifyProviders"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to perform the necessary conversions.
 *
 *  <code>
 *  &lt;modifyProviders/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSModifyProviders extends PSAction
{

   private static final Logger log = LogManager.getLogger(PSModifyProviders.class);
   // see base class
   @Override
   public void execute()
   {
      try
      {
         PSLogger.logInfo("Modifying security providers");

         IPSConfigFileLocator cfgFileLocator = new PSInstConfigFileLocator(
               m_strRxRoot);
         PSConfigurationCtx configCtx = new PSConfigurationCtx(cfgFileLocator,
               PSServer.getPartOneKey());
         IPSRepositoryInfo repInfo = new PSInstRepositoryInfo(m_strRxRoot);
         PSSecurityProviderConverter spConverter = new PSSecurityProviderConverter(
               configCtx, repInfo, true);

         // perform necessary security provider conversions
         spConverter.convert();

         // perform necessary app/def/server configuration conversions
         doConversion(configCtx, repInfo);

         // save configurations
         configCtx.saveConfigs();
      }
      catch(Exception e)
      {
         PSLogger.logError(e.getMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Performs the conversion of Apps, Defs, server config.
    *
    * @param configCtx the object which holds the necessary configuration
    *  information.
    * @param repInfo the object which holds the necessary repository information
    */
   private void doConversion(PSConfigurationCtx configCtx,
         IPSRepositoryInfo repInfo)
   {
      try
      {
         PSLogger.logInfo("Beginning Application/server/def configuration conversion");

         PSBackendTableConverter betConverter = new PSBackendTableConverter(
               configCtx, repInfo, true);
         PSTableLocatorConverter tblConverter = new PSTableLocatorConverter(
               configCtx, true);

         List<IPSComponentConverter> converters =
            new ArrayList<IPSComponentConverter>();

         converters.add(betConverter);
         converters.add(tblConverter);

         PSComponent.setComponentConverters(converters);

         //Get the server properties
         PSProperties serverProps = new PSProperties(m_strServerPropsFile);

         //Get the objectstore properties
         PSProperties objProps = new PSProperties(
               m_strRxRoot + File.separator +
               serverProps.getProperty(PROPS_OBJECT_STORE_VAR,
                     PROPS_OBJECT_STORE));

         //ObjectStore directory
         File objDir = new File(
               m_strRxRoot + File.separator +
               objProps.getProperty(PROPS_OBJECT_STORE_DIR));

         //Convert the applications
         convertApplications(objDir);

         //Convert the definitions
         File systemDef = new File(m_strSystemDef);
         File sharedDef = new File(m_strSharedDefDir);

         if (systemDef.exists())
            convertDef(systemDef);

         if (sharedDef.exists() && sharedDef.isDirectory())
         {
            File[] defs = sharedDef.listFiles();

            for (int i=0; i < defs.length; i++)
            {
               File def = defs[i];
               if (def.getName().endsWith(".xml"))
                  convertDef(def);
            }
         }

      }
      catch(Exception e)
      {
         PSLogger.logError(e.getMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Converts the application objects stored in the objectstore directory to
    * datasources.
    *
    * @param objDir file which represents the objectstore directory,
    * assumed not <code>null</code>
    */
   private void convertApplications(File objDir)
   {
      String appName = null;
      File appFile = null;
      String[] apps = objDir.list();
     
      for (int i = 0; i < apps.length; i++)
      {
         appName = apps[i];
         appFile = new File(objDir, appName);

         if (appFile.isDirectory() || !appName.endsWith(".xml"))
            continue;
        
         //Convert the application
         convertApp(appFile);
      }
   }

   /**
    * Converts an application to use datasources.
    * It does this by simply loading and then saving it.  Also adds paths which
    * allow anonymous access to the user security configuration file.
    *
    * @param appFile the app file, assumed not <code>null</code>
    */
   private void convertApp(File appFile)
   {
      FileInputStream in = null;
      FileInputStream userIn = null;
      FileOutputStream out = null;
      FileOutputStream userOut = null;
      Document doc = null;
      Document userDoc = null;
      PSApplication app = null;
      boolean anonymousAccess = false;

      try
      {
         in = new FileInputStream(appFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         in.close();

         //Load the application
         app = new PSApplication(doc);

         String path = "";
         String authType = "";
         PSAcl acl = app.getAcl();
         PSAclEntry aclEntry = null;
         PSCollection aclEntries = acl.getEntries();
         PSSecurityConfiguration secConf = null;
         File userSecConfFile = null;
         int i;

         //Add any entry with anonymous access allowed to user security configuration
         for (i=0; aclEntries != null && i < aclEntries.size(); i++)
         {
            aclEntry = (PSAclEntry) aclEntries.get(i);
            authType = aclEntry.getName();

            if (authType.equalsIgnoreCase(PSAclEntry.ANONYMOUS_USER_NAME))
            {
               userSecConfFile = new File(m_strUserSecConf);
               userIn = new FileInputStream(userSecConfFile);
               userOut = null;
               userDoc = PSXmlDocumentBuilder.createXmlDocument(userIn, false);

               //Load the user security configuration
               secConf = new PSSecurityConfiguration(userDoc);

               //Add the new path with anonymous access
               path = app.getRequestRoot();
               authType = PSSecurityConfiguration.ANONYMOUS_AUTH_TYPE;
               secConf.addPath(authType, path);
               anonymousAccess = true;
               break;
            }
         }

         //Convert slot name on any rxs_NavTreeSlotMarker exit
         PSLogger.logInfo("Converting exit parameter slot names in application "
               + app.getName());
         int convertedSlots =
            PSUpgradeDbAndHtmlAndXslFilesForSlotNames.convertSlotName(app);
         PSLogger.logInfo("Converted " + convertedSlots + " exit parameter "
               + "slot names in application " + app.getName());

         //Save the application
         doc = app.toXml();
         out = new FileOutputStream(appFile);
         PSXmlDocumentBuilder.write(doc, out);

         //Save the updated user security configuration
         if (anonymousAccess)
         {
            userIn.close();
            userDoc = secConf.toXml();
            userOut = new FileOutputStream(userSecConfFile);
            PSXmlDocumentBuilder.write(userDoc, userOut);
         }
      }
      catch (PSUnknownNodeTypeException ex)
      {
         PSLogger.logError(ex.getMessage());
         PSLogger.logError("This application will no longer be available from the workbench.");
      }
      catch (Exception e)
      {
         PSLogger.logError(e.getMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
         try
         {
            if (out != null)
               out.close();
         }
         catch (Exception e)
         {
         }

         try
         {
            if (userOut != null)
               userOut.close();
         }
         catch (Exception e)
         {
         }
      }
   }

   /**
    * Converts a definition to use datasources.
    * It does this by simply loading and then saving it.
    *
    * @param defFile the def file, assumed not <code>null</code>
    */
   private void convertDef(File defFile)
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      Document doc = null;
      IPSDocument def = null;

      try
      {
         in = new FileInputStream(defFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(in, false);

         String defName = defFile.getName();

         //Load the definition
         if (defName.equalsIgnoreCase("ContentEditorSystemDef.xml"))
            def = new PSContentEditorSystemDef(doc);
         else
            def = new PSContentEditorSharedDef(doc);

         in.close();

         //Save the definition
         doc = def.toXml();
         out = new FileOutputStream(defFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         PSLogger.logError(e.getMessage());
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      finally
      {
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

   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    * The rhythmyx root directory
    */
   private String m_strRxRoot = getRootDir();

   /**
    * Location of server.properties
    */
   private String m_strServerPropsFile = m_strRxRoot + File.separator
   + "rxconfig/Server/server.properties";

   /**
    * Location of system definition
    */
   private String m_strSystemDef = m_strRxRoot + File.separator
   + "rxconfig/Server/ContentEditors/ContentEditorSystemDef.xml";

   /**
    * Location of shared definition
    */
   private String m_strSharedDefDir = m_strRxRoot + File.separator
   + "rxconfig/Server/ContentEditors/shared";

   /**
    * Location of user-security-conf.xml file
    */
   private String m_strUserSecConf = m_strRxRoot + File.separator
   + "AppServer/server/rx/deploy/rxapp.ear/"
   + "rxapp.war/WEB-INF/config/user/security/user-security-conf.xml";

   /**
    * The objectstore property name in server.properties
    */
   private final String PROPS_OBJECT_STORE_VAR = "objectStoreProperties";

   /**
    * The default objectstore properties file
    */
   private final String PROPS_OBJECT_STORE =
      "rxconfig/Server/objectstore.properties";

   /**
    * The objectstore directory property name in objectstore.properties
    */
   private final String PROPS_OBJECT_STORE_DIR = "objectDirectory";


}

