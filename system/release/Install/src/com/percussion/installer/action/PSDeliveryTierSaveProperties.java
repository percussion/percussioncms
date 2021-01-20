/******************************************************************************
 *
 * [ PSDeliveryTierSaveProperties.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.installshield.util.FileUtils;
import com.percussion.install.RxFileManager;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxBrandModel;
import com.percussion.installer.model.PSDeliveryTierDatabaseModel;
import com.percussion.installer.model.RxModel;
import com.percussion.installer.model.PSDeliveryTierProtocolModel;
import com.percussion.installer.model.PSDeliveryTierServerConnectionModel;
import com.percussion.installer.model.RxServerPropertiesModel;
import com.percussion.util.IOTools;
import com.percussion.util.PSProperties;
import com.percussion.utils.security.PSEncrypter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;


/**
 * This action will go through all models and save the properties, if they
 * have the rxpropertyobject property.  It will also set the appropriate
 * InstallAnywhere variables with current selections for silent installs.
 */
public class PSDeliveryTierSaveProperties extends RxIAAction
{
   /**
    * We will loop through all models that have the rxpropertyobject property
    * and save the properties object.
    */
   @Override
   public void execute()
   {
      m_strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
      
      if (isSilentInstall())
      {
         saveModelPropertiesSilent();
         setSelectedProducts();
      }
      else
      {
         saveModelProperties();
      }
   }
   
   /**************************************************************************
    * Worker functions
    *************************************************************************/
   
   /**
    * Will get the current set of models and for each one, if it has the
    * rxpropertyobject property, the properties will be written to disk.  
    */
   private void saveModelProperties()
   {
      for (RxModel model : getModels())
      {
         if (model instanceof RxIAModel)
         {
            RxIAModel iaModel = (RxIAModel) model;

            Object extProp = iaModel.getValue(RxIAModel.PROPS_VAR_NAME);

            String strPropFilePath = m_strRootDir + File.separator +
            iaModel.getPropertyFileName();

            if (extProp != null && strPropFilePath != null)
            {
               try
               {
                  //get the property object
                  PSProperties psProps = (PSProperties) extProp;

                  //create the property file if needed
                  File propFile = new File(strPropFilePath);
                  if (!propFile.exists())
                     propFile.createNewFile();
                  else
                  {
                     //load in the properties and merge
                     psProps =  new PSProperties(strPropFilePath);
                     iaModel.setValue(
                           RxIAModel.PROPS_VAR_NAME,
                           psProps);

                     iaModel.saveToPropFile();
                  }

                  if (shouldRegisterProps(model))
                  {
                     //register the properties as replay variables
                     VarCategoryType category = getReplayVarCategory(model);
                     Enumeration keys = psProps.keys();
                     while (keys.hasMoreElements())
                     {
                        String key = (String) keys.nextElement();
                        String val = psProps.getProperty(key);

                        if (model instanceof PSDeliveryTierDatabaseModel ||
                              model instanceof PSDeliveryTierServerConnectionModel)
                        {
                           if (key.equals(PSDeliveryTierProtocolModel.PWD))
                           {
                              //store dummy value
                              val = "password";
                           }
                        }

                        registerVariable(key, val, category);
                     }
                  }

                  //save the properties
                  FileOutputStream out =
                     new FileOutputStream(strPropFilePath);
                  psProps.store(out, null);
                  out.close();

                  //copy the property file if needed
                  String strCopyPropertyFile = iaModel.getCopyPropertyFile();
                  if (strCopyPropertyFile != null &&
                        strCopyPropertyFile.length() > 0)
                  {
                     strCopyPropertyFile =  m_strRootDir + File.separator +
                     strCopyPropertyFile;

                     File copyFile = new File(strCopyPropertyFile);

                     if (copyFile.exists())
                        copyFile.delete();

                     if (copyFile.getParentFile() != null)
                        FileUtils.createDirs(copyFile.getParentFile());

                     copyFile.createNewFile();

                     out = new FileOutputStream(strCopyPropertyFile);
                     psProps.store(out, null);
                     out.close();
                  }
               }
               catch(IOException ioExc)
               {
                  RxLogger.logError(ioExc.getMessage());
                  RxLogger.logError(ioExc);
               }
            }
         }
      }
   }
   
   /**
    * For silent installs, the properties will be generated from response file
    * variables, then written to disk.
    */
   private void saveModelPropertiesSilent()
   {
      String configDir = m_strRootDir + "/rxconfig";
      String installerCfgDir = configDir + "/Installer";
      String installPropsFile = installerCfgDir + '/' +
         RxFileManager.INSTALLATION_PROPERTIES_FILE;
      String repositoryPropsFile = installerCfgDir + '/' +
         RxFileManager.REPOSITORY_FILE;
      String serverPropsPath = configDir + "/Server/" +
         RxFileManager.SERVER_PROPERTIES_FILE;
      File serverPropsFile = new File(serverPropsPath);
      serverPropsFile.getParentFile().mkdirs();
      
      FileOutputStream installPropsOut = null;
      FileOutputStream repositoryPropsOut = null;
      FileOutputStream serverPropsOut = null;
      InputStream serverPropsIn = null;
               
      try
      {
         Properties installProps = new Properties();
         Properties repositoryProps = new Properties();
         
         URL serverPropsUrl = getResource(
               "$RX_DIR$/config/server.properties");
         if (serverPropsUrl != null)
         {
            serverPropsIn = serverPropsUrl.openStream();
            serverPropsOut = new FileOutputStream(serverPropsPath);
            IOTools.copyStream(serverPropsIn, serverPropsOut);
            serverPropsIn.close();
            serverPropsOut.close();
         }
         
         PSProperties serverProps = new PSProperties();
         if (serverPropsFile.exists())
         {
            serverProps = new PSProperties(serverPropsPath);
         }

         Enumeration vars = getInstallVariables();
         while (vars.hasMoreElements())
         {
            String var = (String) vars.nextElement();
            if (ms_installProps.contains(var))
            {
               installProps.put(var, getInstallValue(var));
            }
            else if (ms_repositoryProps.contains(var))
            {
               String val = getInstallValue(var);
               if (var.equals(PSDeliveryTierProtocolModel.PWD))
               {
                  //encrypt password
                  val = PSEncrypter.encrypt(val, PSEncrypter.getPartOneKey());
               }

               repositoryProps.put(var, val);
            }
            else if (ms_serverProps.contains(var))
            {
               serverProps.put(var, getInstallValue(var));
            }                        
         }

         // add the server type property
         serverProps.put(RxServerPropertiesModel.RHYTHMYX_SERVER_TYPE,
               RxServerPropertiesModel.getServerType());
         
         installPropsOut = new FileOutputStream(installPropsFile);
         installProps.store(installPropsOut, null);
         repositoryPropsOut = new FileOutputStream(repositoryPropsFile);
         repositoryProps.store(repositoryPropsOut, null);
         serverPropsOut = new FileOutputStream(serverPropsPath);
         serverProps.store(serverPropsOut, null);
      }
      catch(IOException e)
      {
         RxLogger.logError(e.getMessage());
         RxLogger.logError(e);
      }
      finally
      {
         if (installPropsOut != null)
         {
            try
            {
               installPropsOut.close();
            }
            catch (IOException e)
            {
               RxLogger.logError(e.getMessage());
               RxLogger.logError(e);
            }
         }
         
         if (repositoryPropsOut != null)
         {
            try
            {
               repositoryPropsOut.close();
            }
            catch (IOException e)
            {
               RxLogger.logError(e.getMessage());
               RxLogger.logError(e);
            }
         }
         
         if (serverPropsIn != null)
         {
            try
            {
               serverPropsIn.close();
            }
            catch (IOException e)
            {
               RxLogger.logError(e.getMessage());
               RxLogger.logError(e);
            }
         }
         
         if (serverPropsOut != null)
         {
            try
            {
               serverPropsOut.close();
            }
            catch (IOException e)
            {
               RxLogger.logError(e.getMessage());
               RxLogger.logError(e);
            }
         }
      }
   }
   
   /**
    * Sets the selected products InstallAnywhere variable with the current set
    * of products which have been selected for installation.  Also sets the
    * devtools features InstallAnywhere variable as this is used to 
    * determine if the devtool launchers should be installed.  Loaded from
    * InstallAnywhere variables generated from a response file during silent
    * installs. 
    */
   private void setSelectedProducts()
   {
      String selectedProducts = "";
      
      Iterator<String> iter = ms_productMap.keySet().iterator();
      while (iter.hasNext())
      {
         String prop = iter.next();
         String value = getInstallValue(prop);
         if (value.equals(RxInstall.YES_VAL))
         {
            if (StringUtils.isNotEmpty(selectedProducts))
            {
               selectedProducts += ", ";
            }

            selectedProducts += ms_productMap.get(prop);
         }
      }
      
      if (StringUtils.isNotEmpty(selectedProducts))
      {
         // set the product selections
         setInstallValue(RxVariables.RX_INSTALL_PRODUCTS, selectedProducts);
      }
   }
   
   /**
    * Determines the category to be used when registering the specified model's
    * persisted properties as replay variables.
    *  
    * @param model The data model, assumed not <code>null</code>.
    * 
    * @return The replay variable category type as an
    * {@link RxIAAction.VarCategoryType}. Defaults to
    * {@link RxIAAction.VarCategoryType#INSTALLATION_CONFIGURATION}.
    */
   private VarCategoryType getReplayVarCategory(RxModel model)
   {
      VarCategoryType category;
      if (model instanceof RxBrandModel)
      {
         category = VarCategoryType.PRODUCT_SELECTION;
      }
      else if (model instanceof PSDeliveryTierDatabaseModel ||
            model instanceof PSDeliveryTierProtocolModel ||
            model instanceof PSDeliveryTierServerConnectionModel)
      {
         category = VarCategoryType.REPOSITORY_CONFIGURATION;
      }
      else if (model instanceof RxServerPropertiesModel)
      {
         category = VarCategoryType.SERVER_PROPERTIES;
      }
      else
      {
         category = VarCategoryType.INSTALLATION_CONFIGURATION;
      }
      
      return category;
   }
   
   /**
    * Determines if the persisted properties of the specified model should be
    * registered as replay variables used in silent installs.
    * 
    * @param model The data model, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the properties should be registered, 
    * <code>false</code> otherwise.
    */
   private boolean shouldRegisterProps(RxModel model)
   {
      return (model instanceof RxBrandModel ||
            model instanceof PSDeliveryTierDatabaseModel ||
            model instanceof PSDeliveryTierProtocolModel ||
            model instanceof PSDeliveryTierServerConnectionModel);
   }
   
   /**************************************************************************
    * Variables
    **************************************************************************/
   
   /**
    * Root dir of this installation.
    */
   private String m_strRootDir = "";

   /**
    * The set of installation persisted property names.
    */   
   private static Set<String> ms_installProps = new HashSet<String>();      
   
   /**
    * The set of repository persisted property names.
    */
   private static Set<String> ms_repositoryProps = new HashSet<String>();
   
   /**
    * The set of server persisted property names.
    */
   private static Set<String> ms_serverProps = new HashSet<String>();
   
   /**
    * Map of products where the key is the product install property loaded as
    * an InstallAnywhere variable during silent installs and the value is the
    * name of the product referenced by installer rules when checking for 
    * product selection.
    */
   private static Map<String, String> ms_productMap = 
      new HashMap<String, String>();
   
   static
   {
      ms_installProps.add(RxBrandModel.BRAND_PROPERTY);
      ms_installProps.add(RxBrandModel.LICENSE_PROPERTY);
      
      ms_repositoryProps.add(PSDeliveryTierProtocolModel.DB_SERVER_NAME);
      ms_repositoryProps.add(PSDeliveryTierProtocolModel.DRIVER_CLASS_NAME);
      ms_repositoryProps.add(PSDeliveryTierProtocolModel.PWD);
      ms_repositoryProps.add(PSDeliveryTierProtocolModel.SCHEMA_NAME);
      ms_repositoryProps.add(PSDeliveryTierProtocolModel.USER_ID);
   }
}
