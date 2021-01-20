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

import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.config.impl.PSConfigUtils;
import com.percussion.rx.config.impl.PSLocationSchemeSetter;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
import com.percussion.server.PSServer;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Category(IntegrationTest.class)
public class PSLocationSchemeSetterTest extends PSConfigurationTest
{
   public void testGenerator() throws Exception
   {
      PSExtensionRef exit = getExtensionRef("sys_casGenericAssemblyLocation");
      System.out.println("FQN: " + exit.getFQN() + "; name: " + exit.getExtensionName());
   }

   /**
    * Gets the Extension Reference for the specified generator name.
    *  
    * @param extName the extension/generator name. This is not FQN of the
    * java extension. 
    * 
    * @return the extension reference. It may be <code>null</code> if there is
    * no such generator/exist.
    */
   @SuppressWarnings("unchecked")
   private PSExtensionRef getExtensionRef(String extName)
   {
      PSExtensionManager mgr = (PSExtensionManager) PSServer
            .getExtensionManager(null);
      try
      {
         Iterator iterator = mgr.getExtensionNames(null, null,
               "com.percussion.extension.IPSAssemblyLocation", extName);
         while (iterator.hasNext())
         {
            PSExtensionRef exit = (PSExtensionRef) iterator.next();
            return exit;
         }
      }
      catch (PSExtensionException e)
      {
         e.printStackTrace();
      }
      return null;
   }
   
   /**
    * Positive test. Modify expression property for an existing scheme
    * 
    * @throws Exception
    */
   public void testModifyExpress() throws Exception
   {
      IPSGuid siteFolderCtxId = PSConfigUtils.getContextModel().nameToGuid(
            "Site_Folder_Assembly");

      // modify existing Location Scheme
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);      

      // validate result
      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
      model.setContextId(siteFolderCtxId);
      IPSLocationScheme scheme = (IPSLocationScheme) model.loadModifiable("CI_Home");
      String expr = scheme.getParameterValue(PSLocationSchemeSetter.EXPRESSION);
      assertTrue(expr.equals("'/index.html'"));
      assertTrue(scheme.getDescription().equals("CI Home page location \u5929\u5929"));

      //\/\/\/\/\/\
      // Cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);

      // validate cleanup result
      scheme = (IPSLocationScheme) model.loadModifiable("CI_Home");
      expr = scheme.getParameterValue(PSLocationSchemeSetter.EXPRESSION);
      assertTrue(expr.equals("'/CI_Home/index.html'"));
      assertTrue(scheme.getDescription().equals("CI Home page location"));
   }

   public void testAddPropertyDefs() throws Exception
   {
      // load the Location Scheme
      IPSGuid siteFolderCtxId = PSConfigUtils.getContextModel().nameToGuid(
            "Site_Folder_Assembly");
      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
      model.setContextId(siteFolderCtxId);
      IPSLocationScheme scheme = (IPSLocationScheme) model
            .loadModifiable("CI_Home");

      PSLocationSchemeSetter setter = new PSLocationSchemeSetter();
      
      // setup properties
      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      
      props.put(PSLocationSchemeSetter.EXPRESSION, "${perc.prefix.expr}");
      props.put(PSLocationSchemeSetter.GENERATOR, "${perc.prefix.gen}");
      props.put(PSLocationSchemeSetter.GENERATOR_PARAMS, "${perc.prefix.param}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(scheme, defs);
      
      assertTrue("Expect 3 defs", defs.size() == 3);
      String expr = (String) defs.get("perc.prefix.expr");
      assertTrue("Expect \"'/CI_Home/index.html'\"", expr.equals("'/CI_Home/index.html'"));
      List<PSPair<String, String>> params = (List<PSPair<String, String>>) defs.get("perc.prefix.param");
      assertTrue("Expect params.size() == 1", params.size() == 1);
      assertTrue("Expect \"expression\"", params.get(0).getFirst().equals("expression"));
      String gen = (String)defs.get("perc.prefix.gen");
      assertTrue(gen.equals("Java/global/percussion/contentassembler/sys_JexlAssemblyLocation"));
   }
   
   /**
    * Tests validation on the same sets of Context / Location Scheme pairs
    * in different packages.
    * 
    * @throws Exception if an error occurs.
    */
   public void testValidation() throws Exception
   {
      PSConfigFilesFactoryTest factory = null;
      try
      {
         factory = PSConfigFilesFactoryTest.applyConfigAndReturnFactory(
               PKG_NAME, IMPL_CFG, LOCAL_CFG);

         try
         {
            PSConfigFilesFactoryTest.applyConfig(PKG_NAME + "_2", IMPL_CFG,
                  LOCAL_CFG);
            fail("Should not be here, due to validation failure above.");
         }
         catch (Exception e)
         {
         }
      }
      finally
      {
         if (factory != null)
            factory.release();
      }

      // \/\/\/\/\/\
      // Cleanup
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
   }
   
   
   /**
    * Positive test. Created / Added new Location Schemes into both Contexts
    * 
    * @throws Exception
    */
   public void testCreateSchemes() throws Exception
   {
      String EXTRA_SCHEME = "CI_Home_Extra\u5929\u5929";
      try
      {
         // there is no Location Scheme EXTRA_SCHEME
         validateLocationScheme(PUBLISH, EXTRA_SCHEME, false);
         validateLocationScheme(ASSEMBLY, EXTRA_SCHEME, false);

         // add a new Location Scheme EXTRA_SCHEME into
         // both "Publish" and "Site_Folder_Assembly" Context
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_POS,
               LOCAL_CFG);
         validateLocationScheme(PUBLISH, EXTRA_SCHEME, true);
         validateLocationScheme(ASSEMBLY, EXTRA_SCHEME, true);
      }
      finally
      {
         // cleanup
         deleteScheme(PUBLISH, EXTRA_SCHEME);
         deleteScheme(ASSEMBLY, EXTRA_SCHEME);
      }
   }

   public void testLocationScheme_WithPrevProperties() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
               LOCAL_CFG_2_PREV);
         validateLocationScheme(PUBLISH, EXTRA_SCHEME_ONE, true);
         validateLocationScheme(ASSEMBLY, EXTRA_SCHEME_ONE,  true);
         validateLocationScheme(PREVIEW, EXTRA_SCHEME_ONE,  false);

         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
               LOCAL_CFG_2, LOCAL_CFG_2_PREV);
         validateLocationScheme(PUBLISH, EXTRA_SCHEME_ONE,  false);
         validateLocationScheme(PREVIEW, EXTRA_SCHEME_ONE,  true);
         validateLocationScheme(ASSEMBLY, EXTRA_SCHEME_ONE,  true);
      }
      finally
      {

         deleteScheme(PREVIEW, EXTRA_SCHEME_ONE);
         deleteScheme(PUBLISH, EXTRA_SCHEME_ONE);
         deleteScheme(ASSEMBLY, EXTRA_SCHEME_ONE);
      }
   }

   public void testLocationScheme_UnProcess() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
               LOCAL_CFG_2);
         validateLocationScheme(PREVIEW, EXTRA_SCHEME_ONE,  true);
         validateLocationScheme(ASSEMBLY, EXTRA_SCHEME_ONE,  true);

         PSConfigFilesFactoryTest.deApplyConfig(PKG_NAME, CONFIG_DEF_2, 
               LOCAL_CFG_2);
         validateLocationScheme(PREVIEW, EXTRA_SCHEME_ONE,  false);
         validateLocationScheme(ASSEMBLY, EXTRA_SCHEME_ONE,  false);
      }
      finally
      {
         // make sure cleanup in place in case of a failure above
         deleteScheme(PREVIEW, EXTRA_SCHEME_ONE);
         deleteScheme(ASSEMBLY, EXTRA_SCHEME_ONE);
      }
   }
   

   public void testEmptyLocationScheme() throws Exception
   {
      // do nothing if there is no context specified - EMPTY
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
            EMPTY_LOCAL_CFG_2);
      
      // do nothing if there is no context specified - NULL
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
            NULL_CTX_LOCAL_CFG_2);
      
      // create a Location Scheme 
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
            ONE_CTX_LOCAL_CFG_2);
      validateLocationScheme(ASSEMBLY, EXTRA_SCHEME_ONE,  true);
      
      // remove the (created) one Location Scheme
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, CONFIG_DEF_2,
            EMPTY_LOCAL_CFG_2, ONE_CTX_LOCAL_CFG_2);

      validateLocationScheme(ASSEMBLY, EXTRA_SCHEME_ONE,  false);
   }   

   private void validateLocationScheme(String ctxName, String schemeName, boolean isExist)
   {
      IPSGuid ctxId = PSConfigUtils.getContextModel().nameToGuid(ctxName);
      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
      model.setContextId(ctxId);

      Object scheme = null;
      try
      {
         scheme = model.load(schemeName);
      }
      catch (Exception e)
      {
      }
      if (isExist)
         assertTrue(scheme != null);
      else
         assertTrue(scheme == null);
   }
   
   private void deleteScheme(String ctxName, String schemeName)
   {
      IPSGuid ctxId = PSConfigUtils.getContextModel().nameToGuid(ctxName);
      PSLocationSchemeModel model = PSConfigUtils.getSchemeModel();
      model.setContextId(ctxId);
      
      try
      {
         model.delete(model.nameToGuid(schemeName));
      }
      catch (Exception e)
      {
         
      }
   }
   
   private static final String PUBLISH = "Publish";
   private static final String PREVIEW = "Preview";
   private static final String ASSEMBLY = "Site_Folder_Assembly";
   private static final String EXTRA_SCHEME_ONE = "CI_Home_ExtraOne";
   
   /**
    * Negative test. 
    * Content Type / Template association is not unique within the same Context.
    * It is the same with an existing Location Scheme in the repository
    *
    * @throws Exception
    */
   public void testAssocExistInRepository() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_NEG1, LOCAL_CFG);
         assertTrue("Failed the negative test on " + IMPL_CFG_NEG1, false);
      }
      catch (Exception e)
      {
         // it should be here.
      }

   }
   
   /**
    * Negative test, Content Type / Template association is not unique within
    * the same Context. Another Location Scheme (in cachedMap) has the same
    * association.
    * 
    * @throws Exception
    */
   public void testAssocExistInAnotherScheme() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_NEG2, LOCAL_CFG);
         assertTrue("Failed the negative test on " + IMPL_CFG_NEG2, false);
      }
      catch (Exception e)
      {
         // it should be here.
      }
   }

   /**
    * Negative test, Content Type / Template association is invalid.
    * 
    * @throws Exception
    */
   public void testInvalidAssociaiton() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_NEG3, LOCAL_CFG);
         assertTrue("Failed the negative test on " + IMPL_CFG_NEG3, false);
      }
      catch (Exception e)
      {
         // it should be here.
      }
   }
   
   /**
    * Negative test, template is different with another same name Location
    * Scheme under the same Context
    * 
    * @throws Exception
    */
   public void testDiffTemplate() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_NEG4, LOCAL_CFG);
         assertTrue("Failed the negative test on " + IMPL_CFG_NEG4, false);
      }
      catch (Exception e)
      {
         // it should be here.
      }      
   }
   
   /**
    * Negative test, required property Generator or Expression is not defined
    * 
    * @throws Exception
    */
   public void testNoRequiredProperty() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_NEG5, LOCAL_CFG);
         assertTrue("Failed the negative test on " + IMPL_CFG_NEG5, false);
      }
      catch (Exception e)
      {
         // it should be here.
      }      
   }
   
   /**
    * Negative test, invalid Generator property
    * 
    * @throws Exception
    */
   public void testInvalidGenerator() throws Exception
   {
      try
      {
         PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_NEG6, LOCAL_CFG);
         assertTrue("Failed the negative test on " + IMPL_CFG_NEG6, false);
      }
      catch (Exception e)
      {
         // it should be here.
      }      
   }
   
   public static final String PKG_NAME = "PSLocationSchemeSetterTest";
   
   public static final String CONFIG_DEF_2 = PKG_NAME + "_2_configDef.xml";

   public static final String LOCAL_CFG_2 = PKG_NAME + "_2_localConfig.xml";

   public static final String EMPTY_LOCAL_CFG_2 = PKG_NAME + "_2_empty_localConfig.xml";

   public static final String NULL_CTX_LOCAL_CFG_2 = PKG_NAME + "_2_null_ctx_localConfig.xml";

   public static final String ONE_CTX_LOCAL_CFG_2 = PKG_NAME + "_2_one_ctx_localConfig.xml";

   public static final String LOCAL_CFG_2_PREV = PKG_NAME + "_2_previous_localConfig.xml";

   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String IMPL_CFG_POS = PKG_NAME + "_configDef_pos.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";

   public static final String IMPL_CFG_NEG1 = PKG_NAME + "_configDef_neg1.xml";

   public static final String IMPL_CFG_NEG2 = PKG_NAME + "_configDef_neg2.xml";

   public static final String IMPL_CFG_NEG3 = PKG_NAME + "_configDef_neg3.xml";

   public static final String IMPL_CFG_NEG4 = PKG_NAME + "_configDef_neg4.xml";

   public static final String IMPL_CFG_NEG5 = PKG_NAME + "_configDef_neg5.xml";

   public static final String IMPL_CFG_NEG6 = PKG_NAME + "_configDef_neg6.xml";
}
