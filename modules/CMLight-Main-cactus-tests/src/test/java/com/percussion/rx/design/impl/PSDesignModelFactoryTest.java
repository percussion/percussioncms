/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.rx.design.impl;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.server.IPSLockerId;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSAssociationSet.AssociationAction;
import com.percussion.rx.design.IPSAssociationSet.AssociationType;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSCustomControlManager;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Category(IntegrationTest.class)
public class PSDesignModelFactoryTest extends ServletTestCase
{
   public void testDesignModelFactory() throws PSNotFoundException {
      for (IPSGuid guid : testGuids)
      {
         PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
         IPSDesignModel model = getModel(type);
         //Test loadModifiable
         Object obj = model.loadModifiable(guid);
         //guid to name test
         String name = model.guidToName(guid);
         try
         {
            Method method = obj.getClass().getMethod("getName",
                  new Class[0]);
            Object oname = method.invoke(obj, new Object[0]);
            assertEquals(name, oname);
         }
         catch (Exception e)
         {
            //ignore we check the name only if a getName method exists on the object.
         }
         //Name to guid test
         IPSGuid nguid = model.nameToGuid(name);
         assertEquals(guid, nguid);
         //Save test
         model.save(obj);
         //Test load
         Object obj1 = model.load(guid);
         Object obj2 = model.load(guid);
         assertEquals(obj1,obj2);
      }
   }
   
   public void testContentTypeAssociations() throws PSNotFoundException {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
      .getDesignModelFactory();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid ctypeguid =  gmgr.makeGuid(311, PSTypeEnum.NODEDEF);
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.NODEDEF);
      PSItemDefinition obj = (PSItemDefinition)model.loadModifiable(ctypeguid);
      PSNodeDefinition nodeDef = PSContentTypeHelper.findNodeDef(obj
            .getGuid());
      List<IPSAssociationSet> asets = model.getAssociationSets();
      int numTemplAssns = 0;
      int numWflAssns = 0;
      for (IPSAssociationSet set : asets)
      {
         if(set.getType() == AssociationType.CONTENTTYPE_TEMPLATE)
         {
            IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
            Set<IPSGuid> guids = nodeDef.getVariantGuids();
            numTemplAssns = guids.size();
            List<String> templs = new ArrayList<String>();
            for (IPSGuid guid : guids)
            {
               IPSAssemblyTemplate asmT = asm.findTemplate(guid);
               templs.add(asmT.getName());
            }
            set.setAssociations(templs);
         }
         else if(set.getType() == AssociationType.CONTENTTYPE_WORKFLOW)
         {
            IPSWorkflowService wfSrvc = PSWorkflowServiceLocator.getWorkflowService();
            Set<IPSGuid> guids = nodeDef.getWorkflowGuids();
            numWflAssns = guids.size();
            List<String> wfs = new ArrayList<String>();
            for (IPSGuid guid : guids)
            {
               PSWorkflow wf = wfSrvc.loadWorkflow(guid);
               wfs.add(wf.getName());
            }
            set.setAssociations(wfs);
         }
      }
      model.save(obj, asets);
      nodeDef = PSContentTypeHelper.findNodeDef(obj
            .getGuid());
      assertTrue(numTemplAssns == nodeDef.getVariantGuids().size());
      assertTrue(numWflAssns == nodeDef.getWorkflowGuids().size());
   }
   
   public void testTemplateSlotAssociations() throws PSNotFoundException {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
      .getDesignModelFactory();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid tempguid =  gmgr.makeGuid(505, PSTypeEnum.TEMPLATE);
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.TEMPLATE);
      IPSAssemblyTemplate obj = (IPSAssemblyTemplate)model.loadModifiable(tempguid);
      List<IPSAssociationSet> asets = model.getAssociationSets();
      int numSlotAssns = 0;
      List<String> slotNames = new ArrayList<String>();
      for (IPSAssociationSet set : asets)
      {
         if(set.getType() == AssociationType.TEMPLATE_SLOT)
         {
            Set<IPSTemplateSlot> slots = obj.getSlots();
            numSlotAssns = slots.size();
            for (IPSTemplateSlot slot : slots)
            {
               slotNames.add(slot.getName());
            }
            set.setAssociations(slotNames);
         }
      }
      model.save(obj, asets);
      obj = (IPSAssemblyTemplate)model.load(tempguid);
      assertTrue(numSlotAssns == obj.getSlots().size());
      //Test Emptying the slot associations.
      obj = (IPSAssemblyTemplate)model.loadModifiable(tempguid);
      asets.get(0).setAssociations(new ArrayList<String>());
      model.save(obj,asets);
      obj = (IPSAssemblyTemplate)model.load(tempguid);
      assertTrue(obj.getSlots().size() == 0);
      //Reset to the original list
      obj = (IPSAssemblyTemplate)model.loadModifiable(tempguid);
      asets.get(0).setAssociations(slotNames);
      model.save(obj,asets);
      obj = (IPSAssemblyTemplate)model.load(tempguid);
      assertTrue(numSlotAssns == obj.getSlots().size());
   }
   
   @SuppressWarnings("unchecked")
   public void testSlotTemplateAssociations() throws PSNotFoundException {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid slotguid = gmgr.makeGuid(502, PSTypeEnum.SLOT);
      IPSDesignModel model = getModel(PSTypeEnum.SLOT);
      IPSDesignModel templModel = getModel(PSTypeEnum.TEMPLATE);
      IPSDesignModel ctypeModel = getModel(PSTypeEnum.NODEDEF);
      IPSTemplateSlot obj = (IPSTemplateSlot) model.loadModifiable(slotguid);
      List<IPSAssociationSet> asets = model.getAssociationSets();
      int numSlotAssns = 0;
      List<PSPair<String, String>> typeTempPairs = new ArrayList<PSPair<String, String>>();
      for (IPSAssociationSet set : asets)
      {
         if (set.getType().equals(AssociationType.SLOT_CONTENTTYPE_TEMPLATE)
               && set.getAction().equals(AssociationAction.MERGE))
         {
            List<PSPair<IPSGuid, IPSGuid>> slotAssns = new ArrayList<PSPair<IPSGuid, IPSGuid>>(
                  obj.getSlotAssociations());
            numSlotAssns = slotAssns.size();
            for (PSPair pair : slotAssns)
            {
               String ct = ctypeModel.guidToName((IPSGuid) pair.getFirst());
               String te = templModel.guidToName((IPSGuid) pair.getSecond());
               typeTempPairs.add(new PSPair(ct, te));
            }
            set.setAssociations(typeTempPairs);
         }
      }
      model.save(obj, asets);
      obj = (IPSTemplateSlot) model.load(slotguid);
      assertTrue(numSlotAssns == obj.getSlotAssociations().size());
      
      //Test Emptying the slot associations --> no change to association.
      obj = (IPSTemplateSlot)model.loadModifiable(slotguid);
      asets = model.getAssociationSets();
      model.save(obj,asets);
      obj = (IPSTemplateSlot)model.load(slotguid);
      assertTrue(numSlotAssns == obj.getSlotAssociations().size());
   }
   
   public void testGetVersionByGuid() throws Exception
   {
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      guids.addAll(testGuids);
      guids.addAll(limitedTestGuids);
      
      for (IPSGuid guid : guids)
      {
         PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
         IPSDesignModel model = getModel(type);
                  
         //test version
         testVersionByGuid(model, guid);
      }
   }
         
   public void testGetVersionByName() throws PSNotFoundException {
      for (IPSGuid guid : testGuids)
      {
         PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
         IPSDesignModel model = getModel(type);
                  
         //test version
         testVersionByName(model, model.guidToName(guid));
      }
      
      Iterator<PSTypeEnum> iter = typeNameVersionMap.keySet().iterator();
      while (iter.hasNext())
      {
         PSTypeEnum type = iter.next();
         IPSDesignModel model = getModel(type);
                  
         //test version
         testVersionByName(model, typeNameVersionMap.get(type));
      }
   }
   
   private IPSDesignModel getModel(PSTypeEnum type)
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();

      IPSDesignModel model = factory.getDesignModel(type);
      if (model instanceof PSLocationSchemeModel)
      {
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         ((PSLocationSchemeModel) model).setContextId(gmgr.makeGuid(1,
               PSTypeEnum.CONTEXT));
      }
      
      return model;
   }
   
   public void testSiteModel()
   {
      IPSDesignModel model = getModel(PSTypeEnum.SITE);
      Collection<IPSGuid> ids = model.findAllIds();
      Collection<String> names = model.findAllNames();

      assertTrue(ids.size() >= 2);
      assertTrue(ids.size() == names.size());
   }
   
   /**
    * Tests the relationship config model (Delete only).
    * 
    * @throws Exception if any errors occur.
    */
   public void testRelationshipConfigModel() throws Exception
   {
      String name = "test";
      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig cfg = cfgSet.getConfig(name);
      if (cfg == null)
      {
         cfgSet.addConfig(name, PSRelationshipConfig.RS_TYPE_USER);
         PSWebserviceUtils.saveRelationshipConfigSet(cfgSet,
               IPSWebserviceErrors.SAVE_FAILED);

         // make sure it was saved
         cfgSet = PSRelationshipCommandHandler.getConfigurationSet();
         assertNotNull(cfgSet.getConfig(name));
      }

      // test model
      IPSDesignModel model = getModel(PSTypeEnum.RELATIONSHIP_CONFIGNAME);
      model.delete(name);

      // make sure it was removed
      cfgSet = PSRelationshipCommandHandler.getConfigurationSet();
      assertNull(cfgSet.getConfig(name));         
   }
     
   /**
    * Tests the control model (Delete only).
    * 
    * @throws Exception if any errors occur.
    */
   public void testControlModel() throws Exception
   {
      FileWriter fw = null;
      IPSLockerId lockId = null;
      PSServerXmlObjectStore os = null;
                    
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSDesignModelUtils.setRequestToInternalUser(req);
      String appName = "rx_resources";
      
      try 
      {
         PSRequest appReq = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
         PSSecurityToken tok = appReq.getSecurityToken();
         lockId = new PSXmlObjectStoreLockerId("DesignModelFactoryTest", true,
               true, tok.getUserSessionId());
         
         os = PSServerXmlObjectStore.getInstance();
         os.getApplicationLock(lockId, appName, 30);
      
         String ctrlContents =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<!DOCTYPE xsl:stylesheet [\n" +
               "<!ENTITY % HTMLlat1 SYSTEM \"/Rhythmyx/DTD/HTMLlat1x.ent\">\n" +
                  "%HTMLlat1;\n" +
               "<!ENTITY % HTMLsymbol SYSTEM \"/Rhythmyx/DTD/HTMLsymbolx.ent\">\n" +
                  "%HTMLsymbol;\n" +
               "<!ENTITY % HTMLspecial SYSTEM \"/Rhythmyx/DTD/HTMLspecialx.ent\">\n" +
                  "%HTMLspecial;\n" +
            "]>\n" +
            "<xsl:stylesheet version=\"1.1\" " +
            "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " +
            "xmlns:psxctl=\"URN:percussion.com/control\" " +
            "xmlns=\"http://www.w3.org/1999/xhtml\" " +
            "exclude-result-prefixes=\"psxi18n\" " +
            "xmlns:psxi18n=\"urn:www.percussion.com/i18n\" >\n" +
            "<xsl:template match=\"/\" />\n" +
            "<psxctl:ControlMeta name=\"control1\" dimension=\"single\" choiceset=\"none\">\n" +
            "</psxctl:ControlMeta>\n" +
            "</xsl:stylesheet>";
         File ctrlFile = new File(PSServer.getRxDir(), "control1.xsl");
         fw = new FileWriter(ctrlFile);
         fw.write(ctrlContents);
         fw.close();
         
         FileInputStream fin = new FileInputStream(ctrlFile);
         String path = "stylesheets/controls/control1.xsl";
         File appFile = new File(path);
         os.saveApplicationFile(appName, appFile, fin, true, lockId, tok);
         
         // make sure control was saved
         String relPath = appName + '/' + path;
         appFile = new File(PSServer.getRxDir(), relPath);
         assertTrue(appFile.exists());
         PSCustomControlManager ctrlMgr = PSCustomControlManager.getInstance();
         ctrlMgr.writeImports();
         Set<String> imports = ctrlMgr.getImports();
         assertTrue(imports.contains(ctrlMgr.createImport(relPath)));     
         
         // test model
         IPSDesignModel model = getModel(PSTypeEnum.CONTROL);
         model.delete("control1");
         
         // make sure control was removed
         assertFalse(appFile.exists());
         imports = ctrlMgr.getImports();
         assertFalse(imports.contains(ctrlMgr.createImport(relPath)));                           
      }
      finally
      {
         if (fw != null)
         {
            try
            {
               fw.close();
            }
            catch (IOException e)
            {
               
            }
         }
         
         PSDesignModelUtils.resetRequestToOriginal(req, origUser);
         
         if (lockId != null)
         {
            try
            {
               if (os != null)
               {
                  os.releaseApplicationLock(lockId, appName);
               }
            }
            catch(PSServerException e)
            {
               // not fatal
            }
         }
      }
   }
   
   /**
    * Tests {@link IPSDesignModel#getVersion(IPSGuid)}.
    * 
    * @param model The design model, assumed not <code>null</code>.
    * @param guid The id of the design object to be queried, assumed not
    * <code>null</code>.
    */
   private void testVersionByGuid(IPSDesignModel model, IPSGuid guid) throws PSNotFoundException {
      Object obj = model.load(guid);
      Long version = model.getVersion(guid);
      assertFalse(version == null);
      try
      {
         Method method = obj.getClass().getMethod("getVersion",
               new Class[0]);
         Integer oversion = (Integer) method.invoke(obj, new Object[0]);
         assertEquals(version.longValue(), oversion.longValue());
      }
      catch (Exception e)
      {
         //ignore we check the version only if a getVersion method exists on the
         //object.
      }
   }
   
   /**
    * Tests {@link IPSDesignModel#getVersion(String)}.
    * 
    * @param model The design model, assumed not <code>null</code>.
    * @param name The name of the design object to be queried, assumed not
    * <code>null</code>.
    */
   private void testVersionByName(IPSDesignModel model, String name)
   {
      Long version = model.getVersion(name);
      assertFalse(version == null);
   }
   
   /**
     * List of test guids which are accessed/modified via {@link PSDesignModel}
     * objects.
     */
   private List<IPSGuid> testGuids = new ArrayList<IPSGuid>();
   
   /**
    * List of test guids which are accessed via {@link PSLimitedDesignModel}
    * objects.
    */
   private List<IPSGuid> limitedTestGuids = new ArrayList<IPSGuid>();
   
   /**
    * Map of type to name where the key is a {@link PSTypeEnum} and the
    * value is a design object name.  Design models for these objects load
    * version by name.
    */
   private Map<PSTypeEnum, String> typeNameVersionMap =
      new HashMap<PSTypeEnum, String>();
     
   @Override
   protected void setUp() throws Exception
   {
      testGuids.clear();
      limitedTestGuids.clear();
      typeNameVersionMap.clear();
      
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      testGuids.add(gmgr.makeGuid(501, PSTypeEnum.TEMPLATE));
      testGuids.add(gmgr.makeGuid(301, PSTypeEnum.NODEDEF));
      testGuids.add(gmgr.makeGuid(501, PSTypeEnum.SLOT));
      testGuids.add(gmgr.makeGuid(2, PSTypeEnum.KEYWORD_DEF));
      testGuids.add(gmgr.makeGuid(301, PSTypeEnum.SITE));
      testGuids.add(gmgr.makeGuid(301, PSTypeEnum.EDITION));
      testGuids.add(gmgr.makeGuid(310, PSTypeEnum.CONTENT_LIST));
      testGuids.add(gmgr.makeGuid(2, PSTypeEnum.ACTION));
      testGuids.add(gmgr.makeGuid(1, PSTypeEnum.CONTEXT));
      testGuids.add(gmgr.makeGuid(318, PSTypeEnum.LOCATION_SCHEME));
      testGuids.add(new PSGuid(PSTypeEnum.ITEM_FILTER, 7008019964183445505L));
      testGuids.add(gmgr.makeGuid(0, PSTypeEnum.DISPLAY_FORMAT));

      // the search works fine at the very 1st time in a freshly installed 
      // database, but does not work on any subsequent time. Comment out for now.
      //testGuids.add(gmgr.makeGuid(10, PSTypeEnum.SEARCH_DEF));
      
      limitedTestGuids.add(gmgr.makeGuid(2, PSTypeEnum.ACTION));
      limitedTestGuids.add(gmgr.makeGuid(1001, PSTypeEnum.COMMUNITY_DEF));
      limitedTestGuids.add(gmgr.makeGuid(4, PSTypeEnum.WORKFLOW));
      
      typeNameVersionMap.put(PSTypeEnum.ACL, "3772753221890605105");
      typeNameVersionMap.put(PSTypeEnum.APPLICATION, "psx_cerffGeneric");
      typeNameVersionMap.put(PSTypeEnum.CONTROL, "sys_CalendarSimple");
      typeNameVersionMap.put(PSTypeEnum.DISPLAY_FORMAT, "Default");
      typeNameVersionMap.put(PSTypeEnum.EXTENSION,
            "Java/global/percussion/cx/sys_addNewItemToFolder");
      typeNameVersionMap.put(PSTypeEnum.LOCALE, "en-us");
      typeNameVersionMap.put(PSTypeEnum.RELATIONSHIP_CONFIGNAME,
            "ActiveAssembly-Mandatory");
      typeNameVersionMap.put(PSTypeEnum.SHARED_GROUP, "shared");
   }
}
