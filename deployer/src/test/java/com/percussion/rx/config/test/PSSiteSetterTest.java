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
package com.percussion.rx.config.test;

import com.percussion.rx.config.impl.PSSiteSetter;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSSiteSetterTest extends PSConfigurationTest
{
   public void testConfigFiles() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);

         // validates
         String CI_NAME = "Corporate_Investments";
         String EI_NAME = "Enterprise_Investments";
         IPSDesignModelFactory factory = PSDesignModelFactoryLocator
               .getDesignModelFactory();
         IPSDesignModel model = factory.getDesignModel(PSTypeEnum.SITE);

         // validate CI
         IPSSite ciSite = (IPSSite) model.load(CI_NAME);
         IPSGuid CTX_ID = new PSGuid(PSTypeEnum.CONTEXT, 301); // Site_Folder_Assembly

         // validate CI site
         assertTrue(ciSite.getUnpublishFlags().equals("n"));
         assertTrue(ciSite.getUserId().equals("gbond"));
         assertTrue(ciSite.getPassword().equals("rhythmyx"));
         assertTrue(ciSite.getPort() == 22);
         assertTrue(ciSite.getBaseUrl().equals("http://blar:9992/Blar_Home"));
         assertTrue(ciSite.getRoot().equals("../Blar.war"));
         assertTrue(ciSite.getFolderRoot().equals("//Sites/Blar"));

         // validate site variables
         String value = ciSite.getProperty("rxs_urlroot", CTX_ID);
         assertTrue(value.equals("BlarBlar-/CI_Home"));
         value = ciSite.getProperty("rxs_navbase", CTX_ID);
         assertTrue(value.equals("BlarBlar-/CI_Home/resources"));

         value = ciSite.getProperty(ADDED_VAR, CTX_ID);
         assertTrue(value.equals("Added Value"));

         // validate EI
         ciSite = (IPSSite) model.load(EI_NAME);
         value = ciSite.getProperty(ADDED_VAR, CTX_ID);
         assertTrue(value.equals("Added Value"));

         // \/\/\/\/\/\
         // Cleanup
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);

         ciSite = (IPSSite) model.load(CI_NAME);
         assertTrue(ciSite.getUnpublishFlags().equals("u"));
         assertTrue(StringUtils.isBlank(ciSite.getUserId()));
         assertTrue(ciSite.getPort() == 21);
      }
      finally
      {
         cleanupCiEiSite(false);
      }
   }

   @SuppressWarnings("unchecked")
   public void testAddPropertyDefs() throws Exception
   {
      String EI_NAME = "Enterprise_Investments";
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.SITE);

      IPSSite eiSite = (IPSSite) model.load(EI_NAME);
      
      PSSiteSetter setter = new PSSiteSetter();
      
      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();

      props.put("globalTemplate", "${perc.prefix.gTemplate}");
      props.put(PSSiteSetter.VARIABLES, "${perc.prefix.vars}");
      setter.setProperties(props);
      setter.addPropertyDefs(eiSite, defs);
      
      assertTrue("Expect 2 defs", defs.size() == 2);
      List<Map<String, String>> vars = (List<Map<String, String>>) defs
            .get("perc.prefix.vars");
      assertTrue("Expect 3 variables", vars.size() == 3);
      String gTemplate = (String) defs.get("perc.prefix.gTemplate");
      assertTrue("Expect \"rffGtEnterpriseInvestmentsCommon\"", gTemplate
            .equals("rffGtEnterpriseInvestmentsCommon"));      
   }
   
   /**
    * Tests Site Variables configuration.
    * 
    * @throws Exception if an error occurs.
    */
   public void testAddRemoveSiteVariables() throws Exception
   {
      try
      {
         // create EXTRA_SITE
         createSite();

         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
               LOCAL_CFG_2_PREV);

         // validate --> add ADDED_VAR & ADDED_VAR_2 into CI_SITE and EXTRA_SITE
         validateSiteVar(CI_SITE, ADDED_VAR, true);
         validateSiteVar(CI_SITE, ADDED_VAR_2, true);
         validateSiteVar(EXTRA_SITE, ADDED_VAR, true);
         validateSiteVar(EXTRA_SITE, ADDED_VAR_2, true);

         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
               LOCAL_CFG_2, LOCAL_CFG_2_PREV);

         // validate the following:
         // remove ADDED_VAR & ADDED_VAR_2 from EXTRA_SITE
         // remove ADDED_VAR_2 from CI_SITE
         // add ADDED_VAR into EI_SITE
         validateSiteVar(EI_SITE, ADDED_VAR, true);

         validateSiteVar(CI_SITE, ADDED_VAR, true);
         validateSiteVar(CI_SITE, ADDED_VAR_2, false);
         validateSiteVar(EXTRA_SITE, ADDED_VAR, false);
         validateSiteVar(EXTRA_SITE, ADDED_VAR_2, false);
      }
      finally
      {
         cleanupCiEiSite(true);
      }
   }

   /**
    * Tests Site Variables configuration.
    * 
    * @throws Exception if an error occurs.
    */
   public void testValidateSiteVariables() throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(PKG_NAME,
               CONFIG_DEF_2, LOCAL_CFG_2);

         // validate --> add ADDED_VAR into CI_SITE and EI_SITE
         validateSiteVar(CI_SITE, ADDED_VAR, true);
         validateSiteVar(EI_SITE, ADDED_VAR, true);

         try
         {
            PSConfigFilesFactoryTest.applyConfig(PKG_NAME + "_2",
                  CONFIG_DEF_2, LOCAL_CFG_2);
            fail("Should have failed due to validation failure");
         }
         catch (Exception e) {
         }
      }
      finally
      {
         factory.release();
         cleanupCiEiSite(false);
      }
   }

   
   private void validateSiteVar(String siteName, String varName, boolean isExist) throws PSNotFoundException {
      IPSGuid CTX_ID = new PSGuid(PSTypeEnum.CONTEXT, 301); // Site_Folder_Assembly
      IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();

      IPSSite site = sitemgr.loadSiteModifiable(siteName);
      String value = site.getProperty(varName, CTX_ID);
      if (isExist)
         assertTrue(StringUtils.isNotBlank(value));
      else
         assertTrue(StringUtils.isBlank(value));
   }

   /**
    * Removes ADDED_VAR and ADDED_VAR_2 from CI_SITE and EI_SITE
    * @throws Exception 
    */
   private void cleanupCiEiSite(boolean removeExtraSite) throws Exception
   {
      IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
      IPSSite site = sitemgr.loadSiteModifiable(CI_SITE);
      site.removeProperty(ADDED_VAR);
      site.removeProperty(ADDED_VAR_2);
      sitemgr.saveSite(site);
      
      site = sitemgr.loadSiteModifiable(EI_SITE);
      site.removeProperty(ADDED_VAR);
      site.removeProperty(ADDED_VAR_2);
      sitemgr.saveSite(site);
      
      if (removeExtraSite)
      {
         site = createSite();
         sitemgr.deleteSite(site);
      }
      
   }
   
   /**
    * Creates EXTRA_SITE 
    * @throws Exception if an error occurs
    */
   private IPSSite createSite() throws Exception
   {
      IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
      IPSSite site = null;
      try
      {
         site = sitemgr.loadSiteModifiable(EXTRA_SITE);
      }
      catch (PSNotFoundException e)
      {
         IPSSite ciSite = sitemgr.loadSiteModifiable("Corporate_Investments");
         site = sitemgr.createSite();
         site.copy(ciSite);
         site.setName(EXTRA_SITE);
      }
      sitemgr.saveSite(site);
      return site;
   }

   private static final String CI_SITE = "Corporate_Investments";
   private static final String EI_SITE = "Enterprise_Investments";
   private static final String EXTRA_SITE = "Site_Created_By_UnitTest";
   
   private static final String ADDED_VAR = "added_sitevar"; 
   private static final String ADDED_VAR_2 = "added_sitevar2"; 
   
   public static final String PKG_NAME = "PSSiteSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";
   

   public static final String CONFIG_DEF_2 = PKG_NAME + "_2_configDef.xml";
   public static final String LOCAL_CFG_2 = PKG_NAME + "_2_localConfig.xml";
   public static final String LOCAL_CFG_2_PREV = PKG_NAME + "_2_previous_localConfig.xml";
}
