/******************************************************************************
 *
 * [ RxLogLicenseInfo.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.action;

import com.percussion.install.Code;
import com.percussion.installanywhere.RxIAAction;
import com.percussion.installer.RxVariables;
import com.percussion.installer.model.RxBrandModel;
import com.percussion.installer.model.RxComponent;
import com.percussion.installer.model.RxComponentModel;
import com.percussion.installer.model.RxModel;
import com.percussion.installer.model.RxProductModel;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This action obtains the following information and then
 * writes it to an Xml file specified by <code>licenseFile</code> :
 * 1> The features and sub-features which were installed and which were not
 * installed.
 * 2> The type of license and the parts licensed by the brand code entered
 * by the user.
 * 3> Rhythmyx version and build number being installed.
 */
public class RxLogLicenseInfo extends RxIAAction
{
   @Override
   public void execute()
   {
      String licenseNumber = RxBrandModel.fetchLicenseNumber();
      Code brandCode = RxBrandModel.fetchBrandCode();
      if (brandCode == null)
         return;
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement(EL_ROOT);
      doc.appendChild(root);
      
      InputStream inStream = null;
      try
      {
         inStream = this.getClass().getResourceAsStream("Version.properties");
         if (inStream != null)
         {
            Properties versionProp = new Properties();
            versionProp.load(inStream);
            String majorVersion = versionProp.getProperty("majorVersion", "5");
            String minorVersion = versionProp.getProperty("minorVersion", "0");
            String buildNumber = versionProp.getProperty("buildNumber", "");
            
            Element rxVerEl = doc.createElement(EL_RHYTHMYX);
            rxVerEl.setAttribute(VERSION_ATTR, majorVersion + "." +
                  minorVersion);
            rxVerEl.setAttribute(BUILD_NUMBER_ATTR, buildNumber);
            root.appendChild(rxVerEl);
         }
      }
      catch (Exception ex)
      {
         RxLogger.logInfo("Exception : " + ex.getLocalizedMessage());
         RxLogger.logInfo(ex);
      }
      finally
      {
         if (inStream != null)
         {
            try
            {
               inStream.close();
            }
            catch (Exception e)
            {
               // no-op
            }
         }
      }
      
      Element licenseEl = doc.createElement(EL_LICENSE_NUMBER);
      licenseEl.setAttribute(VALUE_ATTR, licenseNumber);
      root.appendChild(licenseEl);
      
      Element brandCodeEl = brandCode.toXml(doc);
      root.appendChild(brandCodeEl);
      
      Element featuresEl = doc.createElement(EL_FEATURES);
      root.appendChild(featuresEl);
      
      loadInstallerFeatures(doc, featuresEl);
      
      // write it to the licenseFile
      Writer fw = null;
      try
      {
         String filePath = getInstallValue(RxVariables.INSTALL_DIR);
         if (!filePath.endsWith(File.separator))
            filePath += File.separator;
         filePath += m_licenseFile;
         
         File f = new File(filePath);
         f.getParentFile().mkdirs();
         if (f.exists())
         {
            // create a backup of the file
            IOTools.createBackupFile(f);
            f.delete();
         }
         
         fw = new FileWriter(filePath);
         PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
         walker.write(fw);
      }
      catch (Exception ex)
      {
         RxLogger.logInfo("Exception : " + ex.getLocalizedMessage());
         RxLogger.logInfo(ex);
      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (Exception e)
            {
               // no-op
            }
         }
      }
   }
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns the path of the file relative to the root installation directory
    * in which the information about the license and parts will be stored.
    *
    * @return the path of the file relative to the installation directory in
    * which license information will be stored, never <code>null</code> or
    * empty.
    */
   public String getLicenseFile()
   {
      return m_licenseFile;
   }
   
   /**
    * Sets the path of the file relative to the root installation directory
    * in which the information about the license and parts will be stored.
    *
    * @param licenseFile the path of the file relative to the installation
    * directory in which license information will be stored, may not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if licenseFile is <code>null</code>
    * or empty.
    */
   public void setLicenseFile(String licenseFile)
   {
      if ((licenseFile == null) || (licenseFile.trim().length() == 0))
         throw new IllegalArgumentException(
         "licenseFile may not be null or empty");
      this.m_licenseFile = licenseFile;
   }
   
   /*************************************************************************
    * Public functions
    *************************************************************************/
   
   /*************************************************************************
    * Private functions
    *************************************************************************/
   
   /**
    * Loads the features of a suite installer and adds their information
    * as child elements of <code>featuresEl</code>.
    *
    * @param doc the owner document of element <code>featuresEl</code>, used
    * for creating elements for appending them as child of
    * <code>featuresEl</code>, assumed not <code>null</code>.
    * @param featuresEl the element to which all child elements representing
    * an installable feature will be appended, assumed not <code>null</code>.
    */
   private void loadInstallerFeatures(Document doc, Element featuresEl)
   {
      for (RxModel model : getModels())
      {
         if (!(model instanceof RxProductModel))
            continue;
         else
         {
           RxProductModel prodModel = (RxProductModel) model;
           loadFeatures(doc, featuresEl, prodModel, true, EL_FEATURE);
           break;
         }
      }
   }
   
   /**
    * Loads the sub-features of a product and adds their information as child
    * elements of <code>featureEl</code>.
    *
    * @param doc the owner document of element <code>featureEl</code>, used
    * for creating elements for appending them as child of
    * <code>featureEl</code>, assumed not <code>null</code>.
    * @param featureEl the element to which all child elements representing
    * installable sub-feature will be appended, assumed not <code>null</code>.
    * @param productName the name of the product for which the subfeatures will
    * be loaded, assumed not <code>null</code>.
    */
   private void loadSubFeatures(Document doc, Element featureEl,
         String productName)
   {
      for (RxModel model : getModels())
      {
         if (!(model instanceof RxComponentModel) ||
               model instanceof RxProductModel)
            continue;
         
         RxComponentModel compModel = (RxComponentModel) model;
         String parent = compModel.getParent();
         if (parent != null && parent.equals(productName))
         {
            loadFeatures(doc, featureEl, compModel, false, EL_SUB_FEATURE);
            break;
         }
      }
   }
   
   /**
    * Loads the features of an <code>RxComponentModel</code> and adds their
    * information as child elements of <code>featuresEl</code>.
    *
    * @param doc the owner document of element <code>featuresEl</code>, used
    * for creating elements for appending them as child of
    * <code>featuresEl</code>, assumed not <code>null</code>.
    * @param featuresEl the element to which all child elements representing
    * installable sub-feature will be appended, assumed not <code>null</code>.
    * @param model the component model object for which the features will be
    * loaded, assumed not <code>null</code>.
    * @param subfeatures if <code>true</code> the sub-features of each of the
    * component's features will also be loaded.
    * @param type the type of element to be written, either feature or
    * sub-feature.
    */
   private void loadFeatures(Document doc, Element featuresEl, 
         RxComponentModel model, boolean subfeatures, String type)
   {
      Set<String> featureNames = model.getComponentNames();
      Iterator<String> featureNamesIter = featureNames.iterator();
      while (featureNamesIter.hasNext())
      {
         String featureName = featureNamesIter.next();
         RxComponent featureComp = model.getComponent(featureName);
         
         Element featureEl = doc.createElement(type);
         featuresEl.appendChild(featureEl);
         featureEl.setAttribute(NAME_ATTR, featureName);
         
         if (featureComp != null && featureComp.isSelected())
            featureEl.setAttribute(INSTALLED_ATTR, XML_YES);
         else
            featureEl.setAttribute(INSTALLED_ATTR, XML_NO);
         
         if (subfeatures)
            loadSubFeatures(doc, featureEl, featureName);
      }
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * The path of the file relative to the root installation directory in which
    * the information about the license and parts will be stored, may not
    * be <code>null</code> or empty, modified using
    * {@link #setLicenseFile(String)}.
    * method.
    */
   private String m_licenseFile = "rxconfig/Server/sys_license.xml";
   
   /*************************************************************************
    * Static Variables
    *************************************************************************/
   
   /**
    * Xml root element name.
    */
   public static final String EL_ROOT = "license";
   
   /**
    * Xml Rhythmyx element name.
    */
   public static final String EL_RHYTHMYX = "Rhythmyx";
   
   /**
    * Xml license number element name.
    */
   public static final String EL_LICENSE_NUMBER = "licenseNumber";
   
   /**
    * Xml features element name.
    */
   public static final String EL_FEATURES = "Features";
   
   /**
    * Xml feature element name.
    */
   public static final String EL_FEATURE = "Feature";
   
   /**
    * Xml sub-feature element name.
    */
   public static final String EL_SUB_FEATURE = "sub-feature";
   
   /**
    * Xml version attribute name.
    */
   public static final String VERSION_ATTR = "version";
   
   /**
    * Xml build number attribute name.
    */
   public static final String BUILD_NUMBER_ATTR = "buildNumber";
   
   /**
    * Xml value attribute name.
    */
   public static final String VALUE_ATTR = "value";
   
   /**
    * Xml name attribute name.
    */
   public static final String NAME_ATTR = "name";
   
   /**
    * Xml installed attribute name.
    */
   public static final String INSTALLED_ATTR = "installed";
   
   /**
    * Xml yes attribute value.
    */
   public static final String XML_YES = "yes";
   
   /**
    * Xml no attribute value.
    */
   public static final String XML_NO = "no";
}
