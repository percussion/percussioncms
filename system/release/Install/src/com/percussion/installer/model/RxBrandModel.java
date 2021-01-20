/******************************************************************************
 *
 * [ RxBrandModel.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.model;

import com.percussion.install.Code;
import com.percussion.install.RxInstallerProperties;
import com.percussion.installanywhere.IPSProxyLocator;
import com.percussion.installanywhere.RxIAModel;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.action.RxUpdateUpgradeFlag;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * This is the model that stores the license and product code information.
 */
public class RxBrandModel extends RxIAModel
{
   /**
    * Constructs an {@link RxBrandModel} object.
    *  
    * @param locator the proxy locator which will retrieve the proxy used to
    * interact with the InstallAnywhere runtime platform.
    */
   public RxBrandModel(IPSProxyLocator locator)
   {
      super(locator);
      setHideOnUpgrade(false);
      setPersistProperties(new String[]{LICENSE_PROPERTY, BRAND_PROPERTY});
      setPersistPropertiesUsingReflection(true);
      setPropertyFileName("rxconfig/Installer/installation.properties");
      
      m_featuresMap = new TreeMap<String, Map<String, RxComponent>>();
      m_featuresMap.put(RxProductModel.SERVER_NAME,
            RxServerFeatureModel.getServerFeaturesMap());
   }
   
   /**
    * Returns the license number entered by the user on the brand panel during
    * the installation, never <code>null</code> after the brand panel is
    * displayed since the user has to provide a non-<code>null</code> and
    * non-empty license number
    *
    * @return the license number entered by the user on the brand panel,
    * <code>null</code> before the brand panel is displayed, never
    * <code>null</code> after the brand panel is displayed
    */
   public static String fetchLicenseNumber()
   {
      return ms_licenseNumber;
   }
   
   /**
    * Returns the install code entered by the user on the brand panel during
    * the installation, never <code>null</code> after the brand panel is
    * displayed since the user has to provide a non-<code>null</code> and
    * non-empty install code.
    *
    * @return the install code entered by the user on the brand panel,
    * <code>null</code> before the brand panel is displayed, never
    * <code>null</code> after the brand panel is displayed
    */
   public static Code fetchBrandCode()
   {
      return ms_code;
   }
   
   /**
    *  Returns true normally. If false is returned, the panel will be skipped.
    *  If skipped, the panel's queryExit and exited methods are not called.
    *  This method is called after initializing panel ui, i.e after createUI
    *  method is called.
    *
    *  We read the values here because the super will update the properties
    *  in queryEnter.
    *
    *  @return <CODE>true</CODE> if the panel should not be skipped,
    *  else <CODE>false</CODE>.
    */
   @Override
   public boolean entered()
   {
      //set values from properties
      m_strOldBrand = (String)getValue(BRAND_PROPERTY);
      String strLicense = (String)getValue(LICENSE_PROPERTY);
      
      if (m_strOldBrand != null)
      {
         RxLogger.logInfo("Brand Code : " + m_strOldBrand);
         
         //m_txtBrandCode= m_strOldBrand;
         setBrandCode(m_strOldBrand);
      }
      
      if (strLicense != null)
      {
         RxLogger.logInfo("License Number : " + strLicense);
         //m_txtLicense = strLicense;
         setLicense(strLicense);
      }
      
      return(super.entered());
   }
   
   
   /**
    *  Called by the runtime framework just before destination panel exits. 
    *  Will validate the product code.
    *
    *  @return true if the panel can be exited.
    */
   @Override
   public boolean queryExit()
   {
      if (!super.queryExit())
         return false;
           
      return validateModel();
   }
   
   /*************************************************************************
    * Worker functions.
    *************************************************************************/
   
   @Override
   protected boolean validateModel()
   {
      if(m_txtBrandCode == null ||
            m_txtLicense == null)
      {
         return true;
      }
      
      if(m_txtLicense == null ||
            m_txtLicense.trim().length() == 0)
      {
         validationError(
               RxInstallerProperties.getResources().getString("licensereq"),
               m_lblLicense, m_txtLicense);
         
         return false;
      }
      ms_licenseNumber = m_txtLicense.trim();
      
      if(m_txtBrandCode == null ||
            m_txtBrandCode.trim().length() == 0)
      {
         
         validationError(
               RxInstallerProperties.getResources().getString("codereq"),
               m_lblProductCode, m_txtBrandCode);
         
         return(false);
      }
      
      try
      {
         ms_code = new Code(m_txtBrandCode.trim());
      }
      catch (Exception ex)
      {
         ms_code = null;
         RxLogger.logInfo("ERROR : " + ex.getMessage());
         RxLogger.logInfo(ex);
      }
      if (ms_code == null)
      {
         validationError(
               RxInstallerProperties.getResources().getString("invalidcode"),
               m_lblProductCode, m_txtBrandCode);
         
         return(false);
      }
      
      // (1) Only check if it has expired if this is not an upgrade
      // (2) Always check if the code expired if this is an eval install
      if (codeChanged())
      {
         if(ms_code.hasExpired())
         {
            validationError(
                  RxInstallerProperties.getResources().getString("expiredcode"),
                  m_lblProductCode, m_txtBrandCode);
            
            return(false);
         }
      }
          
      // Check for unlicensed products and product features which must be
      // upgraded
      String unLicensedUpgProds = "";
      String unLicensedUpgFtrs = "";
            
      if (RxUpdateUpgradeFlag.checkUpgradeInstall() && !m_isBrander)
      {
         Map<String, RxComponent> productsMap = RxProductModel.getProductsMap();
         Iterator<String> productKeyIter = productsMap.keySet().iterator();
         while (productKeyIter.hasNext())
         {
            RxComponent product = productsMap.get(productKeyIter.next());
            if (product.isInstalled(getRootDir()))
            {
               int code = product.getCode();
               if (!(code == -1 || fetchBrandCode().isComponentLicensed(code)))
               {
                  // Product needs to be upgraded but is not licensed
                  if (unLicensedUpgProds.trim().length() > 0)
                     unLicensedUpgProds += ", ";
                  
                  unLicensedUpgProds += product.getName();
                }
            }
         }
         
         if (unLicensedUpgProds.trim().length() > 0)
         {
            validationError(RxInstallerProperties.getResources().
                  getString("unlicensedUpgProds") + "\n\n" + unLicensedUpgProds,
                  null, null);
            return false;
         }
       
         Iterator<String> featureKeyIter = m_featuresMap.keySet().iterator();
         while (featureKeyIter.hasNext())
         {
            String productName = featureKeyIter.next();
            Map<String, RxComponent> featuresMap = m_featuresMap.get(
                  productName);
            
            Iterator<String> prodFeatureIter = featuresMap.keySet().iterator();
            while (prodFeatureIter.hasNext())
            {
               RxComponent feature = featuresMap.get(prodFeatureIter.next());
               if (feature.isInstalled(getRootDir()))
               {
                  int code = feature.getCode();
                  if (!(code == -1 || fetchBrandCode().isComponentLicensed(
                        code)))
                  {
                     // Feature needs to be upgraded but is not licensed
                     if (unLicensedUpgFtrs.trim().length() > 0)
                        unLicensedUpgFtrs += ", ";
                     
                     unLicensedUpgFtrs += feature.getName() + '(' +
                        productName + ')';
                   }
               }
            }
         }
         
         if (unLicensedUpgFtrs.trim().length() > 0)
         {
            validationError(RxInstallerProperties.getResources().
                  getString("unlicensedUpgFtrs") + "\n\n" + unLicensedUpgFtrs,
                  null, null);
            return false;
         }
      }
      
      return true;
   }
   
   /**
    * Determine whether the brand code has been changed.
    *
    * @return  <code>true</code> if changed; or <code>false</code> if not
    * changed
    */
   public boolean codeChanged()
   {
      if ((m_strOldBrand == null) ||
            (!m_strOldBrand.equals(m_txtBrandCode)))
         return (true);
      
      return (false);
   }
   
   
   /*************************************************************************
    * Properties Accessors and Mutators
    *************************************************************************/
   
   /**
    * Returns if it is being used in the brander.
    * @return <code>true</code> if it is being used in the brander,
    * <code>false</code> otherwise
    */
   public boolean getIsBrander()
   {
      return m_isBrander;
   }
   
   /**
    * Sets if it is being used in the brander.
    * @param isBrander <code>true</code> if it is being used in the brander,
    * <code>false</code> otherwise
    */
   public void setIsBrander(boolean isBrander)
   {
      this.m_isBrander = isBrander;
   }
   
   /**
    * Accessor for the license number.
    * 
    * @return the license number of the installation
    */
   public String getLicense()
   {
      return m_txtLicense;
   }
   
   /**
    * UI implementer calls this method to set license.
    * @param license
    */
   public void setLicense(String license)
   {
      m_txtLicense = license;
      propertyChanged("license");
   }
   
   /**
    * Accessor for the product code.
    * 
    * @return the product code of the installation.
    */
   public String getBrandCode()
   {
      return m_txtBrandCode;
   }
   
   /**
    * UI implementer calls this method to set product code.
    * @param brandCode
    */
   public void setBrandCode(String brandCode)
   {
      m_txtBrandCode = brandCode;
      propertyChanged("brandCode");
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.installer.model.RxModel#getTitle()
    */
   @Override
   public String getTitle()
   {
      return RxInstallerProperties.getResources().getString("brandtitle");
   }
   
   /*************************************************************************
    * Properties
    *************************************************************************/
   
   /**
    * property to determine if it is being used in the brander
    */
   private boolean m_isBrander = false;
   
   /*************************************************************************
    * Hidden Properties
    *  These properties will be loaded and saved by the
    *  RxISExtenededWizardPanel.  They are not visible in Installshield
    *  because we do not have accessors and mutators.
    *************************************************************************/
   
   /**
    *  Property that holds the brand code.
    */
   static final public String BRAND_PROPERTY = "BRANDCODE";
   
   /**
    *  Property that holds the license number.
    */
   static final public String LICENSE_PROPERTY = "LICENSE";
   
   /*************************************************************************
    * Variables
    *************************************************************************/
   
   /**
    * The brand code from the previous install.
    */
   private String m_strOldBrand = null;
   
   /**
    * Holds the features maps for each of the products which are available to be
    * upgraded.  The key is the corresponding product name.  This is used for
    * code validation during upgrade.  Initialized in ctor, never
    * <code>null</code> after that.
    */
   private Map<String, Map<String, RxComponent>> m_featuresMap = null;
      
   /*************************************************************************
    * UI Component Variables
    *************************************************************************/
   
   /**
    * The license input component.
    */
   private String  m_txtLicense = "";
   
   /**
    * The product code input component.
    */
   protected String m_txtBrandCode = "";
   
   /**
    * The license label component.
    */
   private String  m_lblLicense = null;
   
   /**
    * The product code label component.
    */
   protected String m_lblProductCode = null;
   
   /**
    * See {@link #fetchBrandCode()}.
    */
   protected static Code ms_code = null;
   
   /**
    * Stores the license number provided by the user on the brand panel
    * during the install, initialized to <code>null</code>, set in the
    * <code>validatePanel()</code> method, never <code>null</code> after
    * initialization.
    */
   protected static String ms_licenseNumber = null;
   
   /**
    * The variable name of the isBrander parameter passed to this model's
    * corresponding panel and console via the IDE.
    */
   public static final String IS_BRANDER_VAR = "isBrander";   
}
