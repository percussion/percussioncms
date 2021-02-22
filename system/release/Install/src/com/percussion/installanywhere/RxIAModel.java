/******************************************************************************
 *
 * [ RxIAModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installanywhere;

import com.installshield.util.FileUtils;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.file.FileService;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installer.action.PSDeliveryTierUpgradeFlag;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxSaveProperties;
import com.percussion.installer.action.RxUpdateUpgradeFlag;
import com.percussion.installer.model.RxModel;
import com.percussion.util.PSProperties;
import com.zerog.awt.ZGStandardDialog;
import com.zerog.ia.api.pub.CustomCodeConsoleProxy;
import com.zerog.ia.api.pub.CustomCodePanelProxy;
import com.zerog.ia.api.pub.ResourceAccess;
import com.zerog.ia.api.pub.ServiceAccess;
import com.zerog.ia.api.pub.VariableAccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.StringTokenizer;


/**
 * This class is used to customize the InstallAnywhere panels.
 *
 * This model will map the properties in the wizard to a property file
 * on the file system.  The properties can be retrieved by other panels
 * by using <CODE>getValue</CODE> and the "propertyobject" property
 * returns the <CODE>PSProperties</CODE> object.  This model will not save the
 * property file back to disk. The {@link RxSaveProperties} object will
 * do that and needs to be inserted into the wizard tree at the very end.
 *
 * Errors are captured using the <CODE>validationError</CODE>.
 */
public class RxIAModel extends RxModel
{
   /**
    * Constructor.
    * 
    * @param locator an instance of {@link IPSProxyLocator} used to get the
    * proxy object, which must be of type {@link CustomCodePanelProxy}
    * or {@link CustomCodeConsoleProxy}.
    */
   public RxIAModel(IPSProxyLocator locator)
   {
      Object proxy = locator.getProxy();
      
      if (!(proxy instanceof CustomCodePanelProxy ||
            proxy instanceof CustomCodeConsoleProxy))
      {
         throw new IllegalArgumentException("proxy must be an instance of " +
         "CustomCodePanelProxy or CustomCodeConsoleProxy");
      }
      
      m_proxy = proxy;
   }
   
   /**
    * This method will return <CODE>true</CODE> normally.  If <CODE>false</CODE>
    * is returned the corresponding panel/console will be skipped.  If skipped,
    * the model's createUI, entered, queryExit and exited methods are not
    * called. In this method, any pre-condition check could be performed before
    * creating the panel's ui and entering into the panel.
    *
    * We read the property file and set the values here before entered is
    * called.
    */
   @Override
   public boolean queryEnter()
   {
      //reset the error information
      setHasError(false);
      setError("");
      
      if (!super.queryEnter())
         return (false);
      
      loadFromPropFile();
      
      //let model set its defaults
      initModel();
      
      if(PSDeliveryTierUpgradeFlag.isUpgrade() && isHideOnUpgrade())
      {
         logEvent(RxIAUtils.WARNING, "Skipping model during upgrade.");
         return false; //skip this model
      }
      
      if (RxUpdateUpgradeFlag.isUpgrade() && isHideOnUpgrade())
      {
         logEvent(RxIAUtils.WARNING, "Skipping model during upgrade.");
         
         return false; //skip this model
      }
      
      return entered();
   }
   
   @Override
   public boolean entered()
   {
      if (!super.entered())
         return false;
      
      return true;
   }
   
   /**
    *  Called just before destination model exits.  Will validate the product
    *  code.
    *
    *  @return true if the model can be exited.
    *  @see #queryEnter
    *  @see #entered
    *  @see #exited
    */
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
      
      if (!validateModel())
         return false;
      
      if (isPersistPropertiesUsingReflection())
         savePersistentPropsOnExit();
      
      exited();
      return true;
   }
   
   @Override
   protected boolean validateModel()
   {
      return validateStringFields();
   }   
   
   /**
    * Initializes the model, should be overridden.  By default will log a 
    * warning event.
    */
   protected void initModel()
   {
      logEvent(RxIAUtils.WARNING, "call default initModel");
   }
   
   /**
    * Uses reflection to discover and set given persistent properties. 
    * Doesn't do anything If no persistent properties are given or
    * isPersistPropertiesUsingReflection returns false.
    */
   protected void savePersistentPropsOnExit()
   {
      if (!isPersistPropertiesUsingReflection())
      {
         logEvent(RxIAUtils.WARNING,
         "PersistPropertiesUsingReflection is false - not saving any props.");
         return;
      }
      
      //find persisted properties and set their values on the bean
      String[] names = getPersistProperties();
      
      if (names == null || names.length == 0)
      {
         logEvent(RxIAUtils.WARNING,
         "PersistProperties is empty - not saving any props.");
         return;
      }
      
      for (int i = 0; i < names.length; i++)
      {
         String propKey = getPersistPropKey(names[i], true);
         String propName = getPersistPropKey(names[i], false);
         
         //there must be an accessor method with a name such a getName or isName
         Method[] methods = getClass().getMethods();
         
         for (int j = 0; j < methods.length; j++)
         {
            Method method = methods[j];
            
            String mName = method.getName();
            
            if (mName.equalsIgnoreCase("is" + propName)
                  || mName.equalsIgnoreCase("get" + propName))
            {
               Object val = null;
               try
               {
                  val = method.invoke(this, new Object[0]);
               }
               catch (IllegalArgumentException e)
               {
                  logEvent(
                        RxIAUtils.ERROR,
                        "Failed to Get Persistent Property: "
                        + propKey
                        + " method invoked: "
                        + method.toString(),
                        e);
               }
               catch (IllegalAccessException e)
               {
                  logEvent(
                        RxIAUtils.ERROR,
                        "Failed to Get Persistent Property: "
                        + propKey
                        + " method invoked: "
                        + method.toString(),
                        e);
               }
               catch (InvocationTargetException e)
               {
                  logEvent(
                        RxIAUtils.ERROR,
                        "Failed to Get Persistent Property: "
                        + propKey
                        + " method invoked: "
                        + method.toString(),
                        e);
               }
               
               //set value on the bean
               if (val != null)
                  setValue(propKey, val);
               else
                  logEvent(
                        RxIAUtils.WARNING,
                        "Not setting null value for persistent property: " +
                        propKey);
               
               break;
            }
         }
      }
   }
   
   /**
    * Sets persisted properties on this bean using a given set of props
    * loaded from a props. file and reflected mutator names. 
    * note: this method doesn't do anything If NO persistent properties were
    * given or isPersistPropertiesUsingReflection returns false.
    * 
    * @param props properties to be loaded and set on the model.
    */
   private void loadPersistentPropsOnEnter(PSProperties props)
   {
      if (props == null || props.isEmpty())
         return;
      
      if (!isPersistPropertiesUsingReflection())
      {
         logEvent(RxIAUtils.WARNING,
         "PersistPropertiesUsingReflection is false - not calling setters.");
         return;
      }
      
      //find persisted properties and set their values on the bean
      String[] names = getPersistProperties();
      if (names == null || names.length == 0)
      {
         logEvent(RxIAUtils.WARNING,
         "PersistProperties not set - not calling setters.");
         
         return;
      }
      
      for (int i = 0; i < names.length; i++)
      {
         String propKey = getPersistPropKey(names[i], true);
         String propName = getPersistPropKey(names[i], false);
         
         Object val = props.get(propKey);
         
         //there must be an accessor method with a name such a getName or isName
         Method[] methods = getClass().getDeclaredMethods();
         
         for (int j = 0; j < methods.length; j++)
         {
            Method method = methods[j];
            
            String mName = method.getName();
            
            if (!mName.equalsIgnoreCase("set" + propName))
               continue;
            
            Class[] argTypes = method.getParameterTypes();
            Class argType = argTypes.length == 1 ? argTypes[0] : null;
            
            if (argType == null)
            {
               //setter must have one argument
               logEvent(
                     RxIAUtils.WARNING,
                     "Setter method for Persistent Property: "
                     + propKey
                     + " must have one argument declared, ignoring;"
                     + " found declared accessor method: "
                     + method.toString());
               continue;
            }
            
            Object[] args = new Object[1];
            
            if (val != null)
            {
               if (argType.equals(Boolean.class))
               {
                  args[0] = Boolean.valueOf(val.toString());
               }
               else if (argType.equals(String.class))
               {
                  args[0] = new String(val.toString());
               }
               else if (argType.equals(String[].class))
               {
                  if (val instanceof String[])
                  {
                     String[] arr = (String[]) val;
                     args[0] = arr;
                  }
                  else
                  {
                     String[] arr = new String[1];
                     arr[0] = val.toString();
                     args[0] = arr;
                  }
               }
               else
               {
                  //don't know what to do with it
                  logEvent(
                        RxIAUtils.WARNING,
                        "Not supported Persistent Property Type: "
                        + propKey
                        + " type: "
                        + val
                        + " found declared accessor method: "
                        + method.toString());
                  
                  continue;
               }
            }
            else
            {
               args[0] = null;
            }
            
            try
            {
               //set loaded property
               method.invoke(this, args);
            }
            catch (IllegalArgumentException e)
            {
               logEvent(
                     RxIAUtils.ERROR,
                     "Failed to Set Persistent Property: "
                     + propKey
                     + " value: "
                     + val
                     + " method invoked: "
                     + method.toString(),
                     e);
            }
            catch (IllegalAccessException e)
            {
               logEvent(
                     RxIAUtils.ERROR,
                     "Failed to Set Persistent Property: "
                     + propKey
                     + " value: "
                     + val
                     + " method invoked: "
                     + method.toString(),
                     e);
            }
            catch (InvocationTargetException e)
            {
               logEvent(
                     RxIAUtils.ERROR,
                     "Failed to Set Persistent Property: "
                     + propKey
                     + " value: "
                     + val
                     + " method invoked: "
                     + method.toString(),
                     e);
            }
         }
      }
   }
   
   /**
    *  This method notifies that the model is exiting, any condition that needs
    *  to be performed, like, for example, updating the product tree, should be
    *  done within this method. This method will be called only if the queryExit
    *  method returns true. Also, this method will be called when the Next
    *  button is pressed.
    *
    *  We will set the properties to the property file here.
    */
   @Override
   public void exited()
   {
      super.exited();
      
      //only do this if the properties are set
      if (!isPersistProps())
         return;
      
      //load the properties from the file
      
      //get the root dir
      String strRootDir = getRootDir();
      
      if (strRootDir != null)
      {
         File propFile =
            new File(strRootDir + File.separator + getPropertyFileName());
         
         //if the property file does not exist create it now
         if (!propFile.exists())
         {
            try
            {
               createPropertyFile(propFile);
            }
            catch (IOException e)
            {
               logEvent(RxIAUtils.ERROR, e.getMessage(), e);
            }
         }
         
         saveToPropFile();
      }
   }
   
   /*************************************************************************
    * Worker functions
    *************************************************************************/
   
   /**
    *  Loads properties from property file.
    */
   protected void loadFromPropFile()
   {
      if (!isPersistProps())
         return;
      
      //load the properties from the file
      //get the root dir
      String strRootDir = getRootDir();
      
      if (strRootDir != null)
      {
         File propFile =
            new File(strRootDir + File.separator + getPropertyFileName());
         
         try
         {
            if (!propFile.exists())
            {
               createPropertyFile(propFile);
            }
            
            PSProperties propertyFile = null;

            Object propObj = getValue(PROPS_VAR_NAME);
            if (propObj != null)
               propertyFile = (PSProperties) propObj;
            else
               propertyFile = new PSProperties(propFile.getPath());
            
            for (int iProp = 0; iProp < getPersistProperties().length; ++iProp)
            {
               String propKey = getPersistPropKey(getPersistProperties()[iProp],
                     true);
               
               if (propKey == null || propertyFile.getProperty(propKey) == null)
                  continue;
               
               setValue(propKey, propertyFile.getProperty(propKey));
            }
            
            loadPersistentPropsOnEnter(propertyFile);
         }
         catch (IOException ioExc)
         {
            logEvent(RxIAUtils.ERROR, ioExc.getMessage(), ioExc);
         }
      }
   }
   
   /**
    * Tokenizes a given prop. name by '=', where on the left side of '=' there
    * expected to be a property key that is set in the properties file,
    * on the right side it expects a IS bean property name that is used
    * to set the  value via a reflected method call.
    * ie: DB2_INSTALL_DIR=Db2InstallDir
    * The above means that a IS Bean wants to persist its property accesible
    * through a getter - 'getDb2InstallDir' and the value should be persisted
    * in a given props. file with a key: 'DB2_INSTALL_DIR'.  
    *
    * @param persistPropName prop name, may be <code>null</code> 
    * may be <code>empty</code>.
    * @param fileKey <code>true</code> means that a file key portion is needed
    * <code>false</code> means that a IS Bean prop name portion is needed.
    * 
    * @return requested property Key - either file key or IS Bean prop name,
    * may be <code>null</code> or may <code>empty</code>.   
    */
   private String getPersistPropKey(String persistPropName, boolean fileKey)
   {
      String propKey1 = persistPropName;
      String propKey2 = propKey1;
      if (propKey1 == null || propKey1.trim().length() == 0)
         return propKey1;
      
      StringTokenizer tokens = 
         new StringTokenizer(propKey1, "=", false);
      
      if(tokens.countTokens() > 1)
      {
         propKey1 = tokens.nextToken();
         propKey2 = tokens.nextToken();
      }
      
      return fileKey ? propKey1 : propKey2;
   }
   
   /**
    * @return <code>true</code> is both props file and properties array are set,
    * <code>false</code> otherwise.
    */
   private boolean isPersistProps()
   {
      if (getPropertyFileName() == null ||
            getPropertyFileName().trim().length() == 0)
      {
         logEvent(RxIAUtils.WARNING,
         "PropertyFileName not set - not saving any props.");
         
         return false;
      }
      
      if (getPersistProperties() == null || getPersistProperties().length == 0)
      {
         logEvent(RxIAUtils.WARNING,
         "PersistProperties not set - not saving any props.");
         
         return false;
      }
      
      return true;
   } 
   
   /**
    *  Save the current properties to the property file.
    */
   public void saveToPropFile()
   {
      if (!isPersistProps())
         return;
      
      String strRootDir = getRootDir();
      
      if (strRootDir != null)
      {
         File propFile =
            new File(strRootDir + File.separator + getPropertyFileName());
         
         PSProperties propertyFile = null;
         
         try
         {
            Object propObj = getValue(PROPS_VAR_NAME);
            if (propObj != null)
               propertyFile = (PSProperties) propObj;
            else
               propertyFile = new PSProperties(propFile.getPath());
            
            for (int iProp = 0; iProp < getPersistProperties().length; ++iProp)
            {
               String propKey = 
                  getPersistPropKey(getPersistProperties()[iProp], true);
               
               String strPropertyValue = (String) getValue(propKey);
               
               if (strPropertyValue != null)
                  propertyFile.setProperty(propKey, strPropertyValue);
            }
         }
         catch (IOException ioExc)
         {
            logEvent(RxIAUtils.ERROR, ioExc.getMessage(), ioExc);
         }
         
         //put the properties object in as a property so that others can get it
         setValue(PROPS_VAR_NAME, propertyFile);
      }
   }
   
   /**
    * Creates the property file and loads the default property file from the
    * resources.
    *
    * @param propFile - Property file to create.
    * @throws IOException
    */
   protected void createPropertyFile(File propFile) throws IOException
   {
      if (propFile.exists())
         return;
      
      if (propFile.getParentFile() != null)
         FileUtils.createDirs(propFile.getParentFile());
      
      propFile.createNewFile();
      
      //check to see if this file exists in the resources
      URL source = null;
      try
      {
         String rsrcPropFile = getResourcePropertyFile();
         
         if (rsrcPropFile.trim().length() > 0)
         {
            source = getResource(rsrcPropFile);
                  
            if (source != null)
            {
               try(InputStream in = source.openStream()) {
                  if (in != null) {
                     //read from the in and write to the out
                     try(FileOutputStream out = new FileOutputStream(propFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = in.read(buffer)) != -1)
                           out.write(buffer, 0, bytesRead);
                     }
                  }
               }
            }
         }
      }
      catch (IOException io)
      {
         logEvent(RxIAUtils.WARNING, io.getMessage());
      }
   }
   
   /**
    * Gets the resource described by the specified path.  Calls
    * {@link RxIAUtils#getResource(ResourceAccess, String)}.
    * 
    * @param archivePath the path of the desired resource in the installer
    * archive.
    * 
    * @return a {@link URL} reference to the resource.
    */
   public URL getResource(String archivePath)
   {
      return RxIAUtils.getResource(getResourceAccess(), archivePath);
   }
   
   /**
    * Provides access to the {@link FileService} for this model.
    * Calls {@link RxIAUtils#getFileService(ServiceAccess)}.
    * 
    * @return the {@link FileService} for this model.
    */
   public FileService getFileService()
   {
      return RxIAUtils.getFileService(getServiceAccess());
   }
   
   /**
    * Uses reflection to discover and validate public non static non final 
    * string fields, which have corresponding getter method (follows java bean
    * convention used by the IS beans) and been set on the list of fields to
    * validate using ValidateFieldsErr and/or ValidateFieldsWarn properties on
    * this bean.
    * Values assumed not to be null or empty, once invalid valid is found
    * pops up a message box and returns false afterwards.
    * @return <code>true</code> if all validates, <code>false</code> otherwise.
    */
   protected boolean validateStringFields()
   {
      String[] ferr = getValidateFieldsErr();
      String[] fwarn = getValidateFieldsWarn();
      
      if (ferr.length==0 && fwarn.length==0)
         return true; //not told to validate
      
      Method[] methods = getClass().getMethods();
      
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         
         int mod = method.getModifiers();
         
         if (!Modifier.isPublic(mod))
            continue;
         
         if (Modifier.isStatic(mod))
            continue;
         
         if (Modifier.isFinal(mod))
            continue;
         
         String name = method.getName();
         if (name.length() < 4)
            continue; //can't be a getter name
         
         if (!name.substring(0, 3).equals("get"))
            continue; //must be something other than getter 
         
         String fieldName = name.substring(3);
         
         //found a public property field, check if we want to validate
         boolean validateErr = false;
         boolean validateWarn = false;         
         
         for (int j = 0; j < ferr.length; j++)
         {
            //is it on the list or error validate?
            String fName = ferr[j];
            
            if (fieldName.equalsIgnoreCase(fName))
            {
               validateErr = true;
               break;
            }
         }
         
         for (int j = 0; !validateErr && j < fwarn.length; j++)
         {
            //is it on the list or warning validate?
            String fName = fwarn[j];
            
            if (fieldName.equalsIgnoreCase(fName))
            {
               validateWarn = true;
               break;
            }
         }
         
         if (!validateErr && !validateWarn)
            continue; //no need to validate
         
         String val = "";
         
         try
         {
            val = (String) method.invoke(this, null);
         }
         catch (Exception e1)
         {
            logEvent(RxIAUtils.WARNING, e1.getMessage(), e1);
            return false;
         }
         
         if (val == null || val.trim().length() < 1)
         {
            showValidateMessage(fieldName, val, validateErr);
            return false;
         }
      }
      
      return true;
   }
   
   /**
    * Shows a Validate Message for a given field.
    * Lookup a validate message and label for a given field name, using
    * the following keys to fetch strings from the RxIS properties file:
    * validateMessageKey: beanId + "." + fieldName + ".validateMsg"
    * labelKey: beanId + "." + fieldName + ".label".
    * If resources is not found falls back on showing a "Value Required" message
    * with a field label as it appears on the IS IDE - tokenized field name with
    * spaces before each capitalized letter.
    * @param fieldName name of the field to be validated, never
    * <code>null</code> or <code>empty</code>
    * @param val extracted value, may be <code>null</code> or
    * <code>empty</code>.
    * @param isError <code>true</code> if error message should be generated on
    * failure, <code>false</code> for warning message.
    * 
    * @return <code>true</code> if the install should continue,
    * <code>false</code> otherwise.
    */
   protected boolean showValidateMessage(String fieldName, String val,
         boolean isError)
   {
      if (fieldName == null || fieldName.trim().length() < 1)
         throw new IllegalArgumentException("fieldName may not be null or " +
               "empty");
      
      //capitalize the first letter
      fieldName = fieldName.substring(0, 1).toUpperCase() +
      fieldName.substring(1);
      
      StringBuffer readableName = new StringBuffer();
      for (int i = 0; i < fieldName.length(); i++)
      {
         char ch = fieldName.charAt(i);
         
         if (i > 0 && Character.isUpperCase(ch))
         {  
            // add one space before each capitalized letter.
            readableName.append(" "); 
         }
         
         readableName.append(ch);
      }
      
      String msg = (isError ? "Error!\n" : "Warning!\n") + "Value is required.";
      String lbl = readableName.toString();
      
      String className = "";//getClassname();
      
      try
      {
         msg =
            RxInstallerProperties.getResources().getString(
                  className + "." + fieldName + ".validateMsg");
         
         lbl =
            RxInstallerProperties.getResources().getString(
                  className + "." + fieldName + ".label");
      }
      catch (MissingResourceException ex)
      {
         //no string resource doesn't mean failure - log the debug message
         logEvent(RxIAUtils.WARNING, ex.getMessage(), ex);
      }
      
      if (isError)
      {
         validationError(msg, lbl, val);
         return false;
      }
      else
         return validationWarning(msg, lbl, val);
   }
   
   /**
    *  This function will set the hasError property to true and set the
    *  error property to the contents of<CODE>strError</CODE>. An error
    *  will appear with this message.
    
    * @param msg error that describes the failure, may not be <code>null</code>.
    * @param label additional information which will follow the error.
    * @param value additional information which will follow the label.
    */
   public void validationError(String msg, String label, Object value)
   {
      if (msg == null)
         throw new IllegalArgumentException("msg may not be null");
      
      setHasError(true);
      
      if (label != null)
         msg += ("\n" + label);
      
      if (value != null)
         msg += ("\n" + value);
      
      setError(msg);
      
      getUserInput("Validation error", msg, null);
   }
   
   /**
    * This function will give the warning and ask if they want to continue.
    * If they choose not to continue then an error is returned and the
    * console ui is run.
    *
    * @param msg warning for the user, may not be <code>null</code>.
    * @param label additional information which will follow the error.
    * @param value additional information which will follow the label.
    *
    * @return <CODE>false</CODE> if the user chooses not to continue.
    */
   public boolean validationWarning(String msg, String label, Object value)
   {
      if (msg == null)
         throw new IllegalArgumentException("msg may not be null");
      
      boolean bContinue = true;
      setHasError(true);
      
      if (label != null)
         msg += ("\n" + label);
      
      if (value != null)
         msg += ("\n" + value);
      
      setError(msg);
      
      int response = getUserInput("Validation warning", msg, null, "Yes", "No");
      
      if (response == BUTTON2_RESPONSE)
         bContinue = false;
      
      return bContinue;
   }
   
   /**
    * Helper method that copies a file from src to dest.
    * @param src file path, never <code>null</code> or <code>empty</code>.
    * @param dest file path, never <code>null</code> or <code>empty</code>.
    * @param mustExist if <code>true</code> and src file doesn't exist, the
    * copy operation will not be attempted, otherwise it will.
    * @throws ServiceException if FileService can not be aquired.
    */
   protected void copyFile(String src, String dest, boolean mustExist)
   throws ServiceException
   {
      if (src== null || src.trim().length()==0)
         throw new IllegalArgumentException("src may not be null or empty");
      
      if (dest== null || dest.trim().length()==0)
         throw new IllegalArgumentException("dest may not be null or empty");      
      
      String srcFilePath = resolveString(src);
      String destFileName = resolveString(dest); 
      FileService fs = getFileService();
      File myFile = new File(srcFilePath);
      
      if (!myFile.exists() && mustExist)
         return;
      
      destFileName = FileUtils.normalizeFileName(destFileName);
      fs.copyFile(myFile.getAbsolutePath(), destFileName, true);
   }
   
   /**
    * Calls {@link RxIAUtils#getRootDir(VariableAccess)} to get the Rhythmyx
    * root installation directory.
    * 
    * @return the user-defined root installation directory.
    */
   protected String getRootDir()
   {
      return RxIAUtils.getRootDir(getVariableAccess());
   }
   
   /**
    * Calls {@link RxIAUtils#getValue(VariableAccess, String)} to get the
    * value of the specified variable.
    * 
    * @param var the install variable, may not be <code>null</code> or
    * empty.
    * 
    * @return the value of the variable.
    */
   protected String getInstallValue(String var)
   {
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("var may not be null or empty");
      
      return RxIAUtils.getValue(getVariableAccess(), var);
   }
   
   /**
    * Calls {@link RxIAUtils#setValue(VariableAccess, String, String)} to set
    * the value of the specified variable.
    * 
    * @param var the install variable, may not be <code>null</code> or
    * empty.
    * @param val the value to set for the install variable, may not be
    * <code>null</code> or empty.
    */
   protected void setInstallValue(String var, String val)
   {
      if (var == null || var.trim().length() == 0)
         throw new IllegalArgumentException("var may not be null or empty");
      
      if (val == null || val.trim().length() == 0)
         throw new IllegalArgumentException("val may not be null or empty");
      
      RxIAUtils.setValue(getVariableAccess(), var, val);
   }
   
   /**
    * Resolves all InstallAnywhere variables in the given string.
    * 
    * @param str the string to resolve, may not be <code>null</code> or
    * empty.
    * 
    * @return the resolved string.  All InstallAnywhere variables are replaced
    * by their corresponding values.
    */
   protected String resolveString(String str)
   {
      return RxIAUtils.resolve(getVariableAccess(), str);
   }
   
   /**
    * Convenience method to display a simple dialog with a message and two
    * buttons, OK and Cancel.  Calls 
    * {@link #getUserInput(String, String, String, String, String)}.
    */
   public int getUserInput(String guimsg, String conmsg)
   {
      return getUserInput("", guimsg, conmsg, null, null);
   }
   
   /**
    * Convenience method to display a simple dialog with a title, message, and
    * two buttons, OK and Cancel.  Calls
    * {@link #getUserInput(String, String, String, String, String)}.
    */
   public int getUserInput(String title, String guimsg, String conmsg)
   {
      return getUserInput(title, guimsg, conmsg, null, null);
   }
   
   /**
    * Displays a simple dialog with a message and two buttons.
    * 
    * @param title the dialog title, may not be <code>null</code>.
    * @param guimsg the message to be displayed in the gui dialog, may not be
    * <code>null</code>.
    * @param conmsg the message to be displayed in the console dialog, may be
    * <code>null</code>, in which case, the value specified by guimsg will be
    * used.
    * @param button1 the label for the first button.
    * @param button2 the label for the second button.
    * 
    * @return int value of the last button pressed, either 
    * <code>ZGStandardDialog.DEFAULT_RESPONSE</code> (button1) or
    * <code>ZGStandardDialog.CANCEL_RESPONSE</code> (button2).  
    */
   public int getUserInput(String title, String guimsg, String conmsg,
         String button1, String button2)
   {
      if (title == null)
         throw new IllegalArgumentException("title may not be null");
      
      if (guimsg == null)
         throw new IllegalArgumentException("guimsg may not be null");
      
      int buttonPressed = ZGStandardDialog.DEFAULT_BUTTON;
      
      if (getProxy() instanceof CustomCodePanelProxy)
      {      
         ZGStandardDialog zgsd = new ZGStandardDialog(title, "", guimsg);
         
         if (button1 != null)
            zgsd.setDefaultButtonLabel(button1);
         
         if (button2 != null)
            zgsd.setCancelButtonLabel(button2);
         
         zgsd.setCancelButtonVisible(true);
         zgsd.show();
         buttonPressed = zgsd.getLastButtonPressed();
      }
      else
      {
         RxIAConsoleUtils cu = new RxIAConsoleUtils(
               (CustomCodeConsoleProxy) getProxy());
         String option1 = "OK";
         String option2 = "Cancel";
         
         if (button1 != null)
            option1 = button1;
         
         if (button2 != null)
            option2 = button2;
         
         String response = "";
         String msg = conmsg != null ? conmsg : guimsg;
         do
         {
            try
            {
               response = cu.promptAndBilateralChoice(msg, option1, option2);
            }
            catch (RxIAPreviousRequestException e)
            {
            }
         }
         while (!response.equalsIgnoreCase(option1) &&
               !response.equalsIgnoreCase(option2));
         
         if (response.equalsIgnoreCase(
               option1))
            buttonPressed = ZGStandardDialog.DEFAULT_BUTTON;
         else
            buttonPressed = ZGStandardDialog.CANCEL_BUTTON;
      }
      
      return buttonPressed;
   }
   
   /**
    * Helper method to validate the data entered for the PORT.
    *
    * @param port the port to be validated, never <code>null</code>
    * @return <code>true</code> if the specified port value is valid, 
    * <code>false</code> otherwise.
    */
   protected boolean validatePort(String port)
   {
      if (port == null)
         throw new IllegalArgumentException("port may not be null");
      
      boolean isOK = false;
      int portNum  = 0;
      String err   = null;
      String title = null;
      try
      {
         portNum = Integer.parseInt(port);
         err   = RxInstallerProperties.getResources().getString("portValueErr"); 
         title = RxInstallerProperties.getResources().
         getString("portValueErrorTitle");
         if ( portNum <= 1024 || portNum > 65535 )
            getUserInput(title + " " + port, err, null); 
         else
            isOK = true;
      }
      catch ( NumberFormatException nex)
      {
         err   = RxInstallerProperties.getResources().getString("portValueErr"); 
         title = RxInstallerProperties.getResources().
         getString("portValueErrorTitle");
         getUserInput(title + " " + port,  err, null); 
         RxLogger.logInfo("RxIAModel#validatePort : " + nex.getMessage());
      }
      return isOK;
   }
   
   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/
   
   /**
    * Accessor for the Property Names property.
    *
    * @return the property names to map to the property file.
    */
   public String[] getPersistProperties()
   {
      return (m_strPersistPropertyNames);
   }
   
   /**
    * Mutator for the Property Names property.
    *
    * @param strPropertyNames - the property names to map to the property file.
    */
   public void setPersistProperties(String[] strPropertyNames)
   {
      m_strPersistPropertyNames = strPropertyNames;
   }
   
   /**
    * Accessor for the Property File Name property.
    *
    * @return the property file name relative to the install root.
    */
   public String getPropertyFileName()
   {
      return (m_strPropertyFileName);
   }
   
   /**
    * Mutator for the Property File Name property.
    *
    * @param strPropertyFileName - the property file name
    */
   public void setPropertyFileName(String strPropertyFileName)
   {
      m_strPropertyFileName = strPropertyFileName;
   }
   
   /**
    * Accessor for the Resource Property File property.
    *
    * @return the resource property file path in the installer archive.
    */
   public String getResourcePropertyFile()
   {
      return (m_strResourcePropertyFile);
   }
   
   /**
    * Mutator for the Resource Property File property.
    *
    * @param strResourcePropertyFile - the resource property file path
    */
   public void setResourcePropertyFile(String strResourcePropertyFile)
   {
      m_strResourcePropertyFile = strResourcePropertyFile;
   }
   
   /**
    *  Accessor for Has Error property.
    *
    * @return <CODE>true</CODE> if the model has an error.
    */
   public boolean getHasError()
   {
      return (m_bHasError);
   }
   
   /**
    *  Mutator for Has Error property.
    *
    * @param bHasError <CODE>true</CODE> if the model has an error.
    */
   public void setHasError(boolean bHasError)
   {
      m_bHasError = bHasError;
   }
   
   /**
    *  Mutator for isPersistPropertiesUsingReflection property.
    *
    * @param b <CODE>true</CODE> if reflection should be
    * used to lookup persistent property values.
    */
   public void setPersistPropertiesUsingReflection(boolean b)
   {
      m_isPersistPropertiesUsingReflection = b;
   }
   
   /**
    * Getter for isPersistPropertiesUsingReflection property.
    *
    * @return <CODE>true</CODE> if reflection is to be
    * used for persistent property values.
    */
   public boolean isPersistPropertiesUsingReflection()
   {
      return m_isPersistPropertiesUsingReflection;
   }
   
   @Override
   public String getError()
   {
      return (m_strError);
   }
   
   /**
    * Mutator for the error property.
    *
    * @param strError - the error property file name
    */
   public void setError(String strError)
   {
      m_strError = strError;
   }
   
   /**
    * Mutator for the copy file.
    *
    * @param strFile - the property file name
    */
   public void setCopyPropertyFile(String strFile)
   {
      m_strCopyPropertyFile = strFile;
   }
   
   /**
    *  Accessor for the copy file property.
    *
    * @return the copy file
    */
   public String getCopyPropertyFile()
   {
      return m_strCopyPropertyFile;
   }
   
   /**
    * @return an array of field names to be validated for errors in
    * {@link #validateStringFields()}.
    */
   public String[] getValidateFieldsErr()
   {
      return m_validateFieldsErr;
   }
   
   /**
    * @param strings an array of field names to be validated for errors in
    * {@link #validateStringFields()}.
    */
   public void setValidateFieldsErr(String[] strings)
   {
      m_validateFieldsErr = strings;
   }
   
   /**
    * @return an array of field names to be validated for warnings in
    * {@link #validateStringFields()}.
    */
   public String[] getValidateFieldsWarn()
   {
      return m_validateFieldsWarn;
   }
   
   /**
    * @param strings an array of field names to be validated for warnings in
    * {@link #validateStringFields()}.
    */
   public void setValidateFieldsWarn(String[] strings)
   {
      m_validateFieldsWarn = strings;
   }
   
   /**
    * @return <code>true</code> if the model's associated panel and console
    * should not be displayed during upgrade, <code>false</code> otherwise.
    */
   public boolean isHideOnUpgrade()
   {
      return m_isHideOnUpgrade;
   }
   
   /**
    * @param b <code>true</code> if the model's associated panel and console
    * should not be displayed during upgrade, <code>false</code> otherwise.
    */
   public void setHideOnUpgrade(boolean b)
   {
      m_isHideOnUpgrade = b;
   }   
   
   /**
    * Accessor for this model's proxy object.
    * 
    * @return a proxy object which must be cast correctly for use, never
    * <code>null</code>.
    */
   private Object getProxy()
   {
      return m_proxy;
   }
   
   /**
    * Accessor for this model's proxy object to be used for variable management.
    * 
    * @return a proxy object cast as a {@link VariableAccess} object, never
    * <code>null</code>. 
    */
   public VariableAccess getVariableAccess()
   {
      return (VariableAccess) getProxy();
   }
   
   /**
    * Accessor for this model's proxy object to be used for resource management.
    * 
    * @return a proxy object cast as a {@link ResourceAccess} object, never
    * <code>null</code>. 
    */
   public ResourceAccess getResourceAccess()
   {
      return (ResourceAccess) getProxy();
   }
   
   /**
    * Accessor for this model's proxy object to be used for service management.
    * 
    * @return a proxy object cast as a {@link ServiceAccess} object, never
    * <code>null</code>. 
    */
   public ServiceAccess getServiceAccess()
   {
      return (ServiceAccess) getProxy();
   }
   
   @Override
   public String getTitle()
   {
      return "RxIAModel";
   }
   
   /*************************************************************************
    * Static variables
    *************************************************************************/
   /**
    *  The property name for the {@link PSProperties} object.
    */
   public static final String PROPS_VAR_NAME = "rxpropertyobject";
   
   /**
    *  The property name for the error flag.
    */
   public static final String HAS_ERROR_NAME = "hasError";
   
   /**
    *  The property name for the error message.
    */
   public static final String ERROR_NAME = "error";
   
   /**
    * Constant for button1 response from
    * {@link #getUserInput(String, String, String, String)}.
    */
   public static final int BUTTON1_RESPONSE = ZGStandardDialog.DEFAULT_BUTTON;
   
   /**
    * Constant for button2 response from
    * {@link #getUserInput(String, String, String, String)}.
    */
   public static final int BUTTON2_RESPONSE = ZGStandardDialog.CANCEL_BUTTON;
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * Determines whether properties should be persisted using reflection.
    */
   private boolean m_isPersistPropertiesUsingReflection = true;
   
   /**
    * The list of property names to map to the property file.
    */
   private String[] m_strPersistPropertyNames = {};
   
   /**
    * The name of the file to map the properties to.  This file will be
    * relative to the root install directory.
    */
   private String m_strPropertyFileName = "";
   
   /**
    * The path of the properties file in the installer archive as it appears in
    * the installer's $DO_NOT_INSTALL$ folder in the IDE.
    */
   private String m_strResourcePropertyFile = "";
   
   /**
    * The error state for this model
    */
   private boolean m_bHasError = false;
   
   /**
    *  Copy Property File - Make a copy of the property file specified by
    *  Property File Name.
    */
   private String m_strCopyPropertyFile = "";
   
   /**
    * List of fields to validate with error message.
    */
   private String[] m_validateFieldsErr = {};
   
   /**
    * List of fields to validate with warn message.
    */
   private String[] m_validateFieldsWarn = {};
   
   /**
    * <code>true</code> makes it to skip this model on upgrades, 
    * <code>false</code> otherwise.
    */
   private boolean  m_isHideOnUpgrade = true; 
   
   /**
    * Proxy object for this model.  Initialized in ctor.  Will be an instance
    * of type {@link CustomCodePanelProxy} or {@link CustomCodeConsoleProxy}.
    */   
   private Object m_proxy;   
}
