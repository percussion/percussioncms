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
package com.percussion.services.legacy;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSConfig;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.error.PSException;
import com.percussion.i18n.PSLocale;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.server.PSPersistentProperty;
import com.percussion.server.PSPersistentPropertyManager;
import com.percussion.server.PSPersistentPropertyMeta;
import com.percussion.server.config.PSConfigManager;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.relationship.data.PSRelationshipConfigName;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.utils.types.PSPair;
import com.percussion.workflow.IPSStatesContext;
import com.percussion.workflow.IPSWorkflowAppsContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.*;

/**
 * Test CMS Accessors
 * 
 * @author dougrand
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
public class PSCmsObjectMgrTest
{
   /**
    * As self documented
    */
   private static final int MAX_TEST_CONTENTID = 375;
   /**
    * As self documented
    */
   private static final int MIN_TEST_CONTENTID = 366;
   
   /**
    * All fast forward content type id values
    */
   private static int ALL_FF_TYPES[] =
      {301, 302, 303, 305, 306, 307, 308, 309, 310, 311,
         312, 313, 314, 315, 316};
   /**
    * The cms object manager instance
    */
   static IPSCmsObjectMgr ms_cms = PSCmsObjectMgrLocator.getObjectManager();   
   
   /**
    *  Locale counts before test
    */
   private int ALL_LOCALES = ms_cms.findAllLocales().size();
   private int ACTIVE_LOCALES = ms_cms.findLocaleByStatus(PSLocale.STATUS_ACTIVE).size();
   
   /**
    * @throws Exception
    */
   @Test
   public void test01LocaleCreate() throws Exception
   {
      // Make sure we start clean
      try
      {
         List<PSLocale> locales = ms_cms.findLocales(null, "Test%");
         for (PSLocale locale : locales)
         {
            ms_cms.deleteLocale(locale);
         }
            
      }
      catch(Throwable t)
      {
         // Ignore
      }
      
      PSLocale l = ms_cms.createLocale("xy-zy", "Test locale");
      l.setDescription("Test desc");
      l.setStatus(PSLocale.STATUS_INACTIVE);
      ms_cms.saveLocale(l);
      
      // Change status
      l.setStatus(PSLocale.STATUS_ACTIVE);
      ms_cms.saveLocale(l);

      PSLocale l2 = ms_cms.createLocale("de-ch", "Test Swiss German");
      l2.setDescription("German language used in Switzerland");
      ms_cms.saveLocale(l2);
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test02LocaleFinders() throws Exception
   {
      PSLocale l = ms_cms.findLocaleByLanguageString("xy-zy");
      assertNotNull(l);
      
      int id = l.getLocaleId();
      assertTrue(id > 0);
      
      PSLocale l2 = ms_cms.loadLocale(id);
      assertEquals(l, l2);
      
      Collection locales = ms_cms.findLocaleByStatus(PSLocale.STATUS_ACTIVE);
      assertTrue(locales.size() == ACTIVE_LOCALES + 1);
      
      locales = ms_cms.findAllLocales();
      assertTrue(locales.size() == ALL_LOCALES + 2);
      
      locales = ms_cms.findLocales(null, null);
      assertTrue(locales.size() == ALL_LOCALES + 2);
      
      locales = ms_cms.findLocales("xy-zy", null);
      assertTrue(locales.size() == 1);

      locales = ms_cms.findLocales("", "Test%");
      assertTrue(locales.size() == 2);
      
      locales = ms_cms.findLocales("xy-zy", "Test%");
      assertTrue(locales.size() == 1);
      
      locales = ms_cms.findLocales("de-ch", "Test%");
      assertTrue(locales.size() == 1);
      
      locales = ms_cms.findLocales("foo", "Test%");
      assertTrue(locales.size() == 0);
      
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test03LocaleRemove() throws Exception
   {
      PSLocale l = ms_cms.findLocaleByLanguageString("xy-zy");
      ms_cms.deleteLocale(l);

      PSLocale l2 = ms_cms.findLocaleByLanguageString("de-ch");
      ms_cms.deleteLocale(l2);
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test04CSLoad() throws Exception
   {
      List<Integer> ids = new ArrayList<Integer>();
      List<PSComponentSummary> summaries;
      // Cleanup new item id, just in case we had a bad run
      ids.add(10000000);
      summaries = ms_cms.loadComponentSummaries(ids);
      if (summaries != null && summaries.size() > 0)
      {
         ms_cms.deleteComponentSummaries(summaries);
      }
      
      // Known ids from fast forward
      ids.clear();
      ids.add(318); // Folder
      ids.add(319); // Not folder
      ids.add(320); // Not folder
      summaries = ms_cms.loadComponentSummaries(ids);
      assertEquals(3, summaries.size());

      // Create new summary and save 
      PSComponentSummary sum = new PSComponentSummary(10000000, 1, 1, 1, 1, "Test item", 311, -1);
      sum.setCheckoutUserName("GeorgeWashington");
      sum.setCommunityId(1010);
      sum.setContentCreatedBy("BenFranklin");
      sum.setContentSuffix("foo");
      sum.setWorkflowAppId(4);
      sum.setContentStateId(1);
      summaries.clear();
      summaries.add(sum);
      try
      {
         ms_cms.saveComponentSummaries(summaries);
      }
      catch (Exception e)
      {
         // OK if we can't insert, might be running this test over and over
         // again with a row still in place.
         if (! e.getLocalizedMessage().contains("could not insert"))
         {
            throw e;
         }
         else
         {
            System.err.println("Warning - couldn't insert");
         }
      }
      // Load and check for identity
      ids.clear();
      ids.add(sum.getContentId());
      List<PSComponentSummary> ret = ms_cms.loadComponentSummaries(ids);
      PSComponentSummary sum2 = ret.get(0);
      assertEquals(sum, sum2);
      sum = ret.get(0);
      // Update and save
      sum2.setCommunityId(2000);
      summaries.clear();
      summaries.add(sum2);
      ms_cms.saveComponentSummaries(summaries);
      // Remove 
      ms_cms.deleteComponentSummaries(ret);
      // Find a group by type
      Collection<PSComponentSummary> col;
      col = ms_cms.findComponentSummariesByType(311); // Generic
      assertTrue(col.size() > 0);
      assertTrue(col.iterator().next().getContentTypeId() == 311);     
      
      // Test performance
      PSStopwatch sw = new PSStopwatch();
      
      for(int i = MIN_TEST_CONTENTID; i < MAX_TEST_CONTENTID; i++)
      {
         ids.add(i);
      }
      
      System.out.println("testCSLoad " + ids.size() + " items"); 
      sw.start();
      summaries = ms_cms.loadComponentSummaries(ids);
      sw.stop();
      System.out.println("testCSLoad First run " + sw);
      
      sw.start();
      summaries = ms_cms.loadComponentSummaries(ids);
      sw.stop();
      System.out.println("testCSLoad Second run " + sw);
      
      sw.start();
      summaries = ms_cms.loadComponentSummaries(ids);
      sw.stop();
      System.out.println("testCSLoad Third run " + sw);      
   }
   
   /**
    * Check that find content types works
    * @throws Exception
    */
   @Test
   public void test05FindContentTypes() throws Exception
   {
      List<Integer> cids = new ArrayList<Integer>();
      
      cids.addAll(ms_cms.findContentIdsByType(311));
      cids.addAll(ms_cms.findContentIdsByType(305));
      cids.addAll(ms_cms.findContentIdsByType(313));
      
      Set<Long> contenttypeids = ms_cms.findContentTypesForIds(cids);
      assertNotNull(contenttypeids);
      assertTrue(contenttypeids.size() == 3);
      assertTrue(contenttypeids.contains(311L));
      assertTrue(contenttypeids.contains(305L));
      assertTrue(contenttypeids.contains(313L));
      
      // Test large collection of content ids
      cids.clear();
      
      for(int i = 0; i < ALL_FF_TYPES.length; i++)
      {
         cids.addAll(ms_cms.findContentIdsByType(ALL_FF_TYPES[i]));  
      }
      
      contenttypeids = ms_cms.findContentTypesForIds(cids);
      assertNotNull(contenttypeids);
      assertTrue(contenttypeids.size() == ALL_FF_TYPES.length);
      for(int i = 0; i < ALL_FF_TYPES.length; i++)
      {
         assertTrue(contenttypeids.contains(new Long(ALL_FF_TYPES[i])));
      }
   }
 
   /**
    * 
    * @throws Exception
    */
   @Test
   public void test06ItemsByWorkflowStatus() throws Exception
   {
      Collection<PSComponentSummary> col;
      col = ms_cms.findComponentSummariesByType(311); // Generic
      if(col.size()>0)
      {
         PSComponentSummary fitem = col.iterator().next();
         Collection<Integer> cids = ms_cms.findContentIdsByWorkflowStatus(fitem.getWorkflowAppId(), fitem.getContentStateId());
         assertTrue(cids.size()>=1); 
      }
   }
   
   /**
    * 
    * @throws Exception
    */
   @Test
   public void test07ItemsByWorkflow() throws Exception
   {
      Collection<PSComponentSummary> col;
      col = ms_cms.findComponentSummariesByType(311); // Generic
      if(col.size()>0)
      {
         PSComponentSummary fitem = col.iterator().next();
         Collection<Integer> cids = ms_cms.findContentIdsByWorkflow(fitem.getWorkflowAppId());
         assertTrue(cids.size()>=1); 
      }
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test08ContentTypeFinders() throws Exception
   {
      Collection<Integer> cids = ms_cms.findContentIdsByType(311);
      assertNotNull(cids);
      assertTrue(cids.size() > 0);
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test09WorkflowObjects() throws Exception
   {
      IPSWorkflowAppsContext wf = ms_cms.loadWorkflowAppContext(4);
      assertNotNull(wf);
      assertEquals(4, wf.getWorkFlowAppID());
      assertEquals("Simple Workflow", wf.getWorkFlowAppName());
      assertEquals(1, wf.getWorkFlowInitialStateID());
      
      IPSStatesContext state = ms_cms.loadWorkflowState(4, 1);
      assertNotNull(state);
      assertEquals(1, state.getStateID());
      assertEquals("Draft", state.getStateName());
      assertEquals(4, state.getStatePK().getWorkflowAppId());
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test10Filtering() throws Exception
   {
      List<IPSFilterItem> items = new ArrayList<IPSFilterItem>();
      List<String> flags = new ArrayList<String>();
      flags.add("y");
      items.add(new PSFilterItem(new PSLegacyGuid(319, 1), null, null));
      items.add(new PSFilterItem(new PSLegacyGuid(320, 1), null, null));
      items.add(new PSFilterItem(new PSLegacyGuid(321, 1), null, null));
      items = ms_cms.filterItemsByPublishableFlag(items, flags);
      assertTrue(items.size() == 3);
      
      items.clear();
      for(int i = 0; i < ALL_FF_TYPES.length; i++)
      {
         for(Integer cid : ms_cms.findContentIdsByType(ALL_FF_TYPES[i]))
         {
            items.add(new PSFilterItem(new PSLegacyGuid(cid, 1), null, null));
         }
      }
      items = ms_cms.filterItemsByPublishableFlag(items, flags);
      assertNotNull(items);
      assertTrue(items.size() > 50);
   }
   
   /**
    * Negative test for managing the Relationship Configuration IDs and Names.
    * 
    * @throws Exception if error occurs
    */
   @Test
   public void test11RelationshipConfigIdName() throws Exception
   {
      Collection<PSConfig> configs = ms_cms.findAllConfigs();
      assertTrue(configs.size() == 2);
      
      List<PSRelationshipConfigName> aaList = 
         ms_cms.findRelationshipConfigNames("%Assembly%");
      for (PSRelationshipConfigName cfg : aaList)
      {
         assertTrue(cfg.getName().contains("Assembly"));
      }
      
      PSConfig cfg = ms_cms.findConfig(
            PSConfigurationFactory.RELATIONSHIPS_CFG);
      assertNotNull(cfg);
      
      PSRelationshipConfigSet relConfigSet = loadRelationshipConfigSet();

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Negative testing various operations on SYSTEM relationship configs
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      
      // modify an id of a system relationship config
      PSRelationshipConfig sysConfig = relConfigSet
            .getConfig(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      try
      {
         sysConfig.setId(100);
         // above line should fail, cannot set id to an object which already
         // has an assigned id.
         assertTrue(false); 
      }
      catch (Exception e) {}
      sysConfig.resetId();
      sysConfig.setId(100);      
      try
      {
         saveRelationshipConfigSet(relConfigSet, cfg);
         // above line should fail, id of a system config cannot be modified.
         assertTrue(false); 
      }
      catch (Exception e) {}

      try
      {
         relConfigSet.deleteConfig(sysConfig.getName());
         // above line should fail, cannot delete a system config.
         assertTrue(false); 
      }
      catch (Exception e) {}
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Negative testing various operations on USER relationship configs
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      
      relConfigSet = loadRelationshipConfigSet();
      PSRelationshipConfig userConfig = (PSRelationshipConfig) relConfigSet
         .getConfig(PSRelationshipConfig.TYPE_FOLDER_CONTENT).clone();
      userConfig.setType(PSRelationshipConfig.RS_TYPE_USER);
      userConfig.resetId();
      relConfigSet.add(userConfig);
      try
      {
         saveRelationshipConfigSet(relConfigSet, cfg);
         // above line should fail, cannot save with a user config with DUP-NAME.
         assertTrue(false); 
      }
      catch (Exception e) {}

      userConfig.setName("myFolderContent");
      userConfig.resetId();
      userConfig.setId(
            PSRelationshipConfig.SysConfigEnum.FOLDER_CONTENT.getId());
      try
      {
         saveRelationshipConfigSet(relConfigSet, cfg);
         // above line should fail, cannot save with a user config with DUP-ID.
         assertTrue(false); 
      }
      catch (Exception e) {}
      
   }

   /**
    * Loads all relationship configurations from the repository.
    * 
    * @return the loaded relationship configs, never <code>null</code>.
    * 
    * @throws PSException if an error occurs.
    */
   private PSRelationshipConfigSet loadRelationshipConfigSet()
         throws PSException
   {
      return new PSRelationshipConfigSet(PSConfigManager.getInstance()
            .getXMLConfig(PSConfigurationFactory.RELATIONSHIPS_CFG)
            .getDocumentElement(), null, null);
   }


   /**
    * Saves the specified relationship config set through a supplied 
    * {@link PSConfig} object.
    * 
    * @param relConfigSet the to be saved configs, assumed not <code>null</code>.
    * @param cfg used to save the above configs, assumed not <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private void saveRelationshipConfigSet(PSRelationshipConfigSet relConfigSet,
         PSConfig cfg) throws PSCmsException
   {
      Document newDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = relConfigSet.toXml(newDoc);
      PSXmlDocumentBuilder.replaceRoot(newDoc, newRoot);
      cfg.setConfig(newRoot.getOwnerDocument());
      cfg.releaseLock();
      ms_cms.saveConfig(cfg);
   }
   
   /**
    * Testing {@link IPSCmsObjectMgr#loadCmsObject(int)} method.
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void test12LoadCmsObject() throws Exception
   {
      PSCmsObject itemObject = ms_cms.loadCmsObject(PSCmsObject.TYPE_ITEM);
      assertTrue(itemObject.isRevisionable());
      assertTrue(itemObject.isWorkflowable());
      
      PSCmsObject folderObject = ms_cms.loadCmsObject(PSCmsObject.TYPE_FOLDER);
      assertTrue(!folderObject.isRevisionable());
      assertTrue(!folderObject.isWorkflowable());

      PSCmsObject metaObject = ms_cms.loadCmsObject(PSCmsObject.TYPE_META);
      assertTrue(!metaObject.isRevisionable());
      assertTrue(!metaObject.isWorkflowable());

      List<PSCmsObject> cmsObjects = ms_cms.findAllCmsObjects();
      assertTrue(cmsObjects.size() == 3);
   }
   
   /**
    * Testing the API for managing Relationship Configuration objects,
    * {@link PSRelationshipConfigSet} and {@link PSRelationshipConfigName}
    * 
    * @throws Exception if error occurs
    */
   @Test
   public void test13RelationshipConfigs() throws Exception
   {
      Collection<PSConfig> configs = ms_cms.findAllConfigs();
      assertTrue(configs.size() == 2);
      
      PSConfig cfg = ms_cms.findConfig(
            PSConfigurationFactory.RELATIONSHIPS_CFG);
      assertNotNull(cfg);
      
      cfg.lock("Ben" + System.currentTimeMillis());
      ms_cms.saveConfig(cfg);

      cfg.releaseLock();
      ms_cms.saveConfig(cfg);
      
      // Validate the default Execution Contexts in the Effects 

      PSRelationshipConfigSet relConfigSet = loadRelationshipConfigSet();
      
      // validate all configurations
      for (int i=0; i<relConfigSet.size(); i++)
      {
         PSRelationshipConfig config = 
            (PSRelationshipConfig) relConfigSet.get(i);
         assertTrue(config.isSystem());
      }
      
      // test to/from XML
      Element origRoot = relConfigSet.toXml(PSXmlDocumentBuilder
            .createXmlDocument());
      String origText = PSXmlDocumentBuilder.toString(origRoot);      
      //System.out.println("\norigText: \n" + origText);
      
      setDefaultExeContexts(relConfigSet);
      origRoot = relConfigSet.toXml(PSXmlDocumentBuilder.createXmlDocument());
      String newText = PSXmlDocumentBuilder.toString(origRoot);
      
      //System.out.println("\n New Doc Text: \n" + newText);

      assertTrue(origText.equals(newText));
      
      // add configuration
      Collection<PSRelationshipConfigName> names = ms_cms
      .findAllRelationshipConfigNames();
      int currentSize = names.size();
            
      final String NEW_CFG_NAME = "CopyOfNewCopy";
      PSRelationshipConfig newConfig = (PSRelationshipConfig) relConfigSet
            .getConfig(PSRelationshipConfig.TYPE_NEW_COPY).clone();
      newConfig.setName(NEW_CFG_NAME);
      newConfig.setLabel("Copy Of New Copy");
      newConfig.setType(PSRelationshipConfig.RS_TYPE_USER);
      newConfig.resetId();
      relConfigSet.add(newConfig);
      saveRelationshipConfigSet(relConfigSet, cfg);
      
      // the new relationship config name should be added also
      names = ms_cms.findAllRelationshipConfigNames();
      assertTrue(names.size() == currentSize + 1);
      assertTrue(findRelCfgName(NEW_CFG_NAME, names));
      
      // remove configuration
      cfg.setConfig(origText);
      cfg.releaseLock();
      ms_cms.saveConfig(cfg);

      // the extra relationship config name should be removed also
      names = ms_cms.findAllRelationshipConfigNames();
      assertTrue(names.size() == currentSize);
      assertFalse(findRelCfgName(NEW_CFG_NAME, names));
      relConfigSet = loadRelationshipConfigSet();
      for (PSRelationshipConfigName cname : names)
      {
         PSRelationshipConfig config = relConfigSet.getConfig(cname.getName());
         assertTrue(config.isSystem());
      }
   }
   
   /**
    * Look for a relationship config name from a list of the config objects.
    * 
    * @param name the looked up name, assmed not <code>null</code>.
    * @param names the list of relationship configs, assumed not
    *           <code>null</code>.
    * 
    * @return <code>true</code> if found one from the list; otherwise return
    *         <code>false</code>.
    */
   private boolean findRelCfgName(String name,
         Collection<PSRelationshipConfigName> names)
   {
      for (PSRelationshipConfigName cfgname : names)
      {
         if (cfgname.getName().equals(name))
            return true;
      }

      return false;
   }
   
   /**
    * Set Execution Contexts for all known relationship effects for the
    * supplied configuration set.
    * 
    * @param configs the relationship configuration set, assumed not 
    *   <code>null</code>.
    */
   private void setDefaultExeContexts(PSRelationshipConfigSet configs)
   {
      // set Execution Context for "Folder Context"
      PSRelationshipConfig config = configs
            .getConfig(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      Iterator effects = config.getEffects();
      PSConditionalEffect effect;
      String effectName;
      while (effects.hasNext())
      {
         ArrayList<Integer> ctxs = new ArrayList<Integer>();
         effect = (PSConditionalEffect) effects.next();
         effectName = effect.getEffect().getName();
         if (effectName.equals("sys_TouchParentFolderEffect") ||
             effectName.equals("rxs_NavFolderEffect") )
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CONSTRUCTION);
            ctxs.add(IPSExecutionContext.RS_PRE_DESTRUCTION);
            effect.setExecutionContexts(ctxs);
         }
      }
   
      // set Execution Context for "Translation - Mandatory"
      config = configs
            .getConfig(PSRelationshipConfig.TYPE_TRANSLATION_MANDATORY);
      effects = config.getEffects();
      ArrayList<Integer> ctxs = new ArrayList<Integer>();
      while (effects.hasNext())
      {
         ctxs = new ArrayList<Integer>();
         effect = (PSConditionalEffect) effects.next();
         effectName = effect.getEffect().getName();
         if (effectName.equals("sys_isCloneExists"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CLONE);
            effect.setExecutionContexts(ctxs);
         }
         else if (effectName.equals("sys_PublishMandatory") ||
               effectName.equals("sys_UnpublishMandatory"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_WORKFLOW);
            effect.setExecutionContexts(ctxs);
         }
         else if (effectName.equals("sys_AttachTranslatedFolder"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CONSTRUCTION);
            effect.setExecutionContexts(ctxs);
         }
      }
            
      // set Execution Context for "Translation"
      config = configs
            .getConfig(PSRelationshipConfig.TYPE_TRANSLATION);
      effects = config.getEffects();
      while (effects.hasNext())
      {
         ctxs = new ArrayList<Integer>();
         effect = (PSConditionalEffect) effects.next();
         effectName = effect.getEffect().getName();
         if (effectName.equals("sys_isCloneExists"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CLONE);
            effect.setExecutionContexts(ctxs);
         }
         else if (effectName.equals("sys_AttachTranslatedFolder"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CONSTRUCTION);
            effect.setExecutionContexts(ctxs);
         }
      }
         
      // set Execution Context for "Active Assembly - Mandatory"
      config = configs
            .getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY);
      effects = config.getEffects();
      while (effects.hasNext())
      {
         ctxs = new ArrayList<Integer>();
         effect = (PSConditionalEffect) effects.next();
         effectName = effect.getEffect().getName();
         if (effectName.equals("sys_PublishMandatory") ||
               effectName.equals("sys_UnpublishMandatory"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_WORKFLOW);
            effect.setExecutionContexts(ctxs);
         }
      }
   
      // set Execution Context for "Promotable Version"
      config = configs
            .getConfig(PSRelationshipConfig.TYPE_PROMOTABLE_VERSION);
      effects = config.getEffects();
      while (effects.hasNext())
      {
         ctxs = new ArrayList<Integer>();
         effect = (PSConditionalEffect) effects.next();
         effectName = effect.getEffect().getName();
         if (effectName.equals("sys_Promote"))
         {
            ctxs.add(IPSExecutionContext.RS_POST_WORKFLOW);
            effect.setExecutionContexts(ctxs);               
         }
         else if (effectName.equals("sys_AddCloneToFolder"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CONSTRUCTION);
            effect.setExecutionContexts(ctxs);               
         }
      }

      // set Execution Context for "New Copy"
      config = configs
            .getConfig(PSRelationshipConfig.TYPE_NEW_COPY);
      effects = config.getEffects();
      while (effects.hasNext())
      {
         ctxs = new ArrayList<Integer>();
         effect = (PSConditionalEffect) effects.next();
         effectName = effect.getEffect().getName();
         if (effectName.equals("sys_AddCloneToFolder"))
         {
            ctxs.add(IPSExecutionContext.RS_PRE_CONSTRUCTION);
            effect.setExecutionContexts(ctxs);               
         }
      }
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test14FlushCache()
   throws Exception
   {
      ms_cms.flushSecondLevelCache();
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void test15FindPublicGuids()
   throws Exception
   {
      List<IPSGuid> guids;
      List<Integer> ids = new ArrayList<Integer>();
      PSStopwatch sw = new PSStopwatch();
      
      for(int i = MIN_TEST_CONTENTID; i < MAX_TEST_CONTENTID; i++)
      {
         ids.add(i);
      }
      
      System.out.println("testFindPublicGuids " + ids.size() + " items");
      // First
      sw.start();
      guids = ms_cms.findPublicOrCurrentGuids(ids);
      sw.stop();
      System.out.println("testFindPublicGuids First run " + sw);

      // Test results
      for(int i = 0; i < ids.size(); i++)
      {
         PSLegacyGuid g = (PSLegacyGuid) guids.get(i);
         int id = ids.get(i);
         assertEquals(id, g.getContentId());
         assertTrue(g.getRevision() > 0);
      }
      
      
      sw.start();
      guids = ms_cms.findPublicOrCurrentGuids(ids);
      sw.stop();
      System.out.println("testFindPublicGuids Second run " + sw);
      
      sw.start();
      guids = ms_cms.findPublicOrCurrentGuids(ids);
      sw.stop();
      System.out.println("testFindPublicGuids Third run " + sw);
      
      sw.start();
      guids = ms_cms.findPublicOrCurrentGuids(ids);
      sw.stop();
      System.out.println("testFindPublicGuids Fourth run " + sw);
   }

   /**
    * Tests all persistent property related API
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void test16PersistentProperty() throws Exception
   {
      // test find all meta
      List<PSPersistentPropertyMeta> listMeta = ms_cms.findAllPersistentMeta();
      assertTrue(listMeta.size() == 3);

      // test find persistent meta by name
      listMeta = ms_cms
               .findPersistentMetaByName(PSPersistentPropertyManager.SYS_USER);
      assertTrue(listMeta.size() == 3);

      // test find all properties
      List<PSPersistentProperty> origProps = ms_cms.findAllPersistentProperties();

      String myUser = "myUser" + System.currentTimeMillis();

      // test find by name
      List<PSPersistentProperty> props = ms_cms
               .findPersistentPropertiesByName(myUser);
      assertTrue(props.size() == 0);

      // test insert / save property
      PSPersistentProperty prop = new PSPersistentProperty(myUser, "sys_lang",
               "sys_session", "private", "admin2 sys_lang private");
      ms_cms.savePersistentProperty(prop);
      props = ms_cms.findPersistentPropertiesByName(myUser);
      assertTrue(props.size() == 1);

      // test modify / update property
      String newValue = "changed";
      prop.setValue(newValue);
      ms_cms.updatePersistentProperty(prop);

      // test delete property
      ms_cms.deletePersistentProperty(prop);
      props = ms_cms.findPersistentPropertiesByName(myUser);
      assertTrue(props.size() == 0);

      props = ms_cms.findAllPersistentProperties();
      assertTrue(origProps.size() == props.size());
   }
   
   @Test
   public void test17ClearStartDate() throws Exception
   {
      Set<Integer> ids = new HashSet<Integer>();
      List<PSComponentSummary> sums = ms_cms.findComponentSummariesByType(311);
      
      try
      {
         for (PSComponentSummary sum : sums)
         {
            assertNotNull(sum.getContentStartDate());
            ids.add(sum.getContentId());
         }

         ms_cms.clearStartDate(ids);

         for (PSComponentSummary sum : ms_cms.findComponentSummariesByType(311))
         {
            assertNull(sum.getContentStartDate());
         }
      }
      finally
      {
         try
         {
            ms_cms.saveComponentSummaries(sums);
         }
         catch (Exception ex)
         {
            
         }
      }
   }

   @Test
   public void test18ClearExpiryDate() throws Exception
   {
      Set<Integer> ids = new HashSet<Integer>();
      List<PSComponentSummary> sums = ms_cms.findComponentSummariesByType(311);
      
      try
      {
         Date date = new Date();
         List<PSComponentSummary> testSums = ms_cms.findComponentSummariesByType(311);
         for (PSComponentSummary sum : testSums)
         {
            sum.setContentExpiryDate(date);
            ids.add(sum.getContentId());
         }
         
         ms_cms.saveComponentSummaries(testSums);
         
         for (PSComponentSummary sum : ms_cms.findComponentSummariesByType(311))
         {
            assertNotNull(sum.getContentExpiryDate());
         }

         ms_cms.clearExpiryDate(ids);

         for (PSComponentSummary sum : ms_cms.findComponentSummariesByType(311))
         {
            assertNull(sum.getContentExpiryDate());
         }
      }
      finally
      {
         try
         {
            ms_cms.saveComponentSummaries(sums);
         }
         catch (Exception ex)
         {
            
         }
      }
   }
}
