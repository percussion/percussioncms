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

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.rx.config.IPSConfigHandler.ObjectState;
import com.percussion.rx.config.IPSPropertySetter;
import com.percussion.rx.config.impl.PSContentTypeFieldSetter;
import com.percussion.rx.config.impl.PSContentTypeSetter;
import com.percussion.rx.config.impl.PSObjectConfigHandler;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.rx.design.impl.PSAssociationSet;
import com.percussion.rx.utils.PSContentTypeUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tests {@link PSContentTypeSetter} and {@link PSContentTypeFieldSetter}
 *
 * @author YuBingChen
 */
@Category(IntegrationTest.class)
public class PSContentTypeSetterTest extends PSConfigurationTest
{
   @SuppressWarnings("unchecked")
   public void testAddPropertyDefs() throws Exception
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);

      // create the setter
      PSContentTypeSetter setter = new PSContentTypeSetter();
      
      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      
      props.put(PSContentTypeSetter.DEFAULT_WORKFLOW, "${perc.prefix.def_wf}");
      props.put(PSContentTypeSetter.TEMPLATES, "${perc.prefix.templates}");
      props.put(PSContentTypeSetter.WORKFLOWS, "${perc.prefix.workflows}");
      props.put(PSContentTypeSetter.ICON_VALUE, "${perc.prefix.iconvalue}");
      props.put(PSContentTypeSetter.FIELDS, "${perc.prefix.fields}");
      
      setter.setProperties(props);
      setter.addPropertyDefs(itemDef, defs);
      
      // validates the returned "defs"
      assertTrue("Expect 5 defs", defs.size() == 5);
      Map fields = (Map) defs.get("perc.prefix.fields");
      assertTrue("Expect 1 field", fields.size() == 1);
      Map field = (Map) fields.get("sys_title");
      assertTrue("Expect 0 sequence", field.get("sequence").equals("0"));
      List<String> templates = (List<String>)defs.get("perc.prefix.templates");
      assertTrue("Expect 1 templates", templates.size() == 1);
      List<String> workflows = (List<String>)defs.get("perc.prefix.workflows");
      assertTrue("Expect 2 workflows", workflows.size() == 2);
      String defWorkflow = (String) defs.get("perc.prefix.def_wf");
      assertTrue("Expect \"Simple Workflow\"", defWorkflow
            .equals("Simple Workflow"));
      String iconValue = (String) defs.get("perc.prefix.iconvalue");
      assertTrue("Expect \"rffBrief.gif\"", iconValue.equals("rffBrief.gif"));
   }
   
   public void testSimpleProperties() throws Exception
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);

      // create the setter
      PSContentTypeSetter setter = new PSContentTypeSetter();
      
      // set Content Type setter
      Map<String, Object> ct_pmap = new HashMap<String, Object>();
      
      String FD_NAME = "placeholder";
      String MY_LABEL = "My LABEL"; 
      String WF_NAME = "Simple Workflow";

      ct_pmap.put("label", MY_LABEL);
      ct_pmap.put(PSContentTypeSetter.DEFAULT_WORKFLOW, WF_NAME);
      
      // association properties
      List<String> assocTemplates = new ArrayList<String>();
      assocTemplates.add("rffSnCallout");
      ct_pmap.put(PSContentTypeSetter.TEMPLATES, assocTemplates);

      List<String> assocWF = new ArrayList<String>();
      assocWF.add(WF_NAME);
      assocWF.add("Standard Workflow");
      ct_pmap.put(PSContentTypeSetter.WORKFLOWS, assocWF);

      setter.setProperties(ct_pmap);

      // set field setter
      PSContentTypeFieldSetter fd_setter = new PSContentTypeFieldSetter();
      fd_setter.setFieldName(FD_NAME);
      Map<String, Object> fd_pmap = new HashMap<String, Object>();
      String FD_TRUE = "true"; 
      fd_pmap.put("userSearchable", FD_TRUE);
      fd_pmap.put(PSContentTypeFieldSetter.REQUIRED, FD_TRUE);
      fd_setter.setProperties(fd_pmap);
      

      // create the handler
      List<IPSPropertySetter> ss = new ArrayList<IPSPropertySetter>();
      ss.add(setter);
      ss.add(fd_setter);
      PSObjectConfigHandler h = new PSObjectConfigHandler();
      h.setPropertySetters(ss);

      // before set properties
      PSField fd = itemDef.getFieldByName(FD_NAME);
      // cleanup/reset properties
      fd.setUserSearchable(false);
      PSContentTypeUtils.setFieldRequiredRule(fd, false);
      itemDef.setLabel("Breif");

      assertTrue(!fd.isUserSearchable());
      assertTrue(!PSContentTypeUtils.hasRequiredRule(fd));

      assertTrue(!itemDef.getLabel().equals(MY_LABEL));

      // create association
      List<IPSAssociationSet> aSets = new ArrayList<IPSAssociationSet>();
      PSAssociationSet aset = new PSAssociationSet(
            IPSAssociationSet.AssociationType.CONTENTTYPE_TEMPLATE);
      aSets.add(aset);
      aset = new PSAssociationSet(
            IPSAssociationSet.AssociationType.CONTENTTYPE_WORKFLOW);
      aSets.add(aset);

      // perform the test
      h.process(itemDef, ObjectState.BOTH, aSets);
      
      assertTrue(itemDef.getLabel().equals(MY_LABEL));
      assertTrue(itemDef.getContentEditor().getWorkflowId() == 4);
      
      assertTrue(fd.isUserSearchable());
      assertTrue(PSContentTypeUtils.hasRequiredRule(fd));

      assertTrue(aSets.get(0).getAssociations().size() == 1);
      assertTrue(aSets.get(1).getAssociations().size() == 2);

      //\/\/\/\/\      
      // Clean up
      //\/\/\/\/\      
      fd.setUserSearchable(false);
      PSContentTypeUtils.setFieldRequiredRule(fd, false);
      itemDef.setLabel("Breif");
   }

   @SuppressWarnings("unchecked")
   public void testFieldProperties() throws Exception
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);

      String FD_NAME = "callout";

      // create field setter
      PSContentTypeFieldSetter fd_setter = new PSContentTypeFieldSetter();
      fd_setter.setFieldName(FD_NAME);
      Map<String, Object> fd_pmap = new HashMap<String, Object>();

      String FD_FALSE = "true";
      String MY_LABEL = "My Label:";
      String MY_DEFAULT_VALUE = "My Default Value";
      
      //\/\/\/\/\/\/\/\/\/\/\/\/
      // configurable properties
      //\/\/\/\/\/\/\/\/\/\/\/\/
      fd_pmap.put("userSearchable", FD_FALSE);
      fd_pmap.put(PSContentTypeFieldSetter.LABEL, MY_LABEL);
      fd_pmap.put(PSContentTypeFieldSetter.DEFAULT_VALUE, MY_DEFAULT_VALUE);

      Map<String, String> controlParams = new HashMap<String, String>();
      controlParams.put("height", "100");
      controlParams.put("width", "40");
      fd_pmap.put(PSContentTypeFieldSetter.CONTROL_PARAMS, controlParams);
      
      fd_setter.setProperties(fd_pmap);
      
      // before set properties
      PSField fd = itemDef.getFieldByName(FD_NAME);
      fd.setUserSearchable(false);

      PSUISet uiset = itemDef.getContentEditor().getFieldUiSet(FD_NAME);
      assertTrue(!uiset.getLabel().getText().equals(MY_LABEL));
      assertTrue(!fd.isUserSearchable());
      
      // perform the test
      PSObjectConfigHandler h = getConfigHandler(fd_setter);
      h.process(itemDef, ObjectState.BOTH, null);
      
      assertTrue(fd.isUserSearchable());
      assertTrue(uiset.getLabel().getText().equals(MY_LABEL));
      assertTrue(fd.getDefault().getValueText().equals(MY_DEFAULT_VALUE));
      
      // validate control parameters
      Iterator<PSParam> params = uiset.getControl().getParameters();
      while (params.hasNext())
      {
         PSParam p = params.next();
         PSTextLiteral v = (PSTextLiteral) p.getValue();
         assertTrue(p.getName().equals("height")
               || p.getName().equals("width"));
         assertTrue(v.getText().equals("100")
               || v.getText().equals("40"));
      }
      
      // test PSContentTypeFieldSetter.addPropertyDefs()
      
      Map<String, Object> defs = new HashMap<String, Object>();
      Map<String, Object> props = new HashMap<String, Object>();
      
      props.put(PSContentTypeFieldSetter.CONTROL_PARAMS, "${perc.pre.params}");
      props.put(PSContentTypeFieldSetter.DEFAULT_VALUE, "${perc.pre.default}");
      props.put(PSContentTypeFieldSetter.LABEL, "${perc.pre.label}");
      props.put(PSContentTypeFieldSetter.REQUIRED, "${perc.pre.required}");
      props.put(PSContentTypeFieldSetter.SEQUENCE, "${perc.pre.sequence}");
      
      fd_setter.setProperties(props);
      fd_setter.setFieldName(FD_NAME);
      fd_setter.addPropertyDefs(itemDef, defs);
      
      assertTrue("Expect 5 defs", defs.size() == 5);
      String defaultVal = (String) defs.get("perc.pre.default");
      assertTrue("Expect \"My Default Value\"", defaultVal.equals(MY_DEFAULT_VALUE));
      Boolean reqValue = (Boolean) defs.get("perc.pre.required");
      assertTrue("Expect required = false", !reqValue);
      String theLabel = (String) defs.get("perc.pre.label");
      assertTrue("Expect \"My Label:\"", theLabel.equals(MY_LABEL));
      Integer theSeq = (Integer) defs.get("perc.pre.sequence");
      assertTrue("Expect sequence = 4", theSeq == 4);
      List<PSPair<String, String>> theParams = (List<PSPair<String, String>>)defs.get("perc.pre.params");
      assertTrue("Expect params.size == 2", theParams.size() == 2);
      PSPair<String, String> pair = theParams.get(0);
      assertTrue("Expect \"height\"", pair.getFirst().equals("height"));
      assertTrue("Expect \"100\"", pair.getSecond().equals("100"));
      
      //\/\/\/\/\      
      // Clean up
      //\/\/\/\/\      
      uiset.setLabel(new PSDisplayText("Callout:"));
      fd.setUserSearchable(false);
   }

   /**
    * Tests attempt to configure the unsupported properties.
    * 
    * @throws Exception if an error occurs.
    */
   public void testUnsupportedProperties() throws Exception
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);

      // create field setter
      PSContentTypeFieldSetter fd_setter = new PSContentTypeFieldSetter();
      fd_setter.setFieldName("sys_title");
      Map<String, Object> fd_pmap = new HashMap<String, Object>();

      String FD_FALSE = "false";

      PSObjectConfigHandler h = getConfigHandler(fd_setter);

      // perform the test.
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // Cannot apply REQUIRED property on a system field
      // which already has a required rule.
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      fd_pmap.put(PSContentTypeFieldSetter.REQUIRED, FD_FALSE);      
      fd_setter.setProperties(fd_pmap);
      try
      {
         h.process(itemDef, ObjectState.BOTH, null);
         assertTrue("Failed to validate apply required property", false);
      }
      catch (Exception e)
      {
         assertTrue(true);
      }
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // cannot apply "SubmitName" property on any field
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      fd_pmap.put("submitName", "blau");      
      fd_setter.setProperties(fd_pmap);
      try
      {
         h.process(itemDef, ObjectState.BOTH, null);
         assertTrue("Failed to validate submitName property", false);
      }
      catch (Exception e)
      {
         assertTrue(true);
      }
   }

   /**
    * Tests attempt to configure the unsupported properties.
    * 
    * @throws Exception if an error occurs.
    */
   public void testRequiredProperties() throws Exception
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffCalendar", 1002);

      // create field setter
      PSContentTypeFieldSetter fd_setter = new PSContentTypeFieldSetter();
      fd_setter.setFieldName("sys_title");
      Map<String, Object> fd_pmap = new HashMap<String, Object>();

      PSObjectConfigHandler h = getConfigHandler(fd_setter);

      // perform the test.
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // OK to apply REQUIRED property on a system field
      // which already has a required rule.
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      fd_pmap.put(PSContentTypeFieldSetter.REQUIRED, "true");      
      fd_setter.setProperties(fd_pmap);
      h.process(itemDef, ObjectState.BOTH, null);
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // OK to apply REQUIRED property on a shared field
      // which does not have a required rule.
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      fd_setter = new PSContentTypeFieldSetter();
      h = getConfigHandler(fd_setter);
      
      fd_setter.setFieldName("body");
      fd_pmap.clear();
      fd_pmap.put(PSContentTypeFieldSetter.REQUIRED, "false");
      fd_setter.setProperties(fd_pmap);
      
      h.process(itemDef, ObjectState.BOTH, null);
      
      fd_pmap.put(PSContentTypeFieldSetter.REQUIRED, "true");      
      h.process(itemDef, ObjectState.BOTH, null);

      // cleanup
      PSField field = itemDef.getFieldByName("body");
      PSContentTypeUtils.setFieldRequiredRule(field, false);      
   }

   public void testAddField() throws Exception
   {
      String FIELD_NAME = "webdavowner";
      
      addSharedField(FIELD_NAME);
      addSharedField("shared." + FIELD_NAME);
      
      String fieldName2 = "img2_filename";
      addSharedField(fieldName2);
      addSharedField("sharedimage." + fieldName2);
   }
   
   /**
    * Tests adding a system or shared field
    * 
    * @throws Exception if an error occurs.
    */
   public void addSharedField(String fieldName) throws Exception
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);

      String justName = PSContentTypeHelper.getSharedFieldName(fieldName)[0];

      String MY_SEQ = "2";
      String MY_LABEL = "My Label:";

      // create field setter
      PSContentTypeSetter setter = new PSContentTypeSetter();
      
      //\/\/\/\/\/\/\/\/\/\/\/\/
      // configurable properties
      //\/\/\/\/\/\/\/\/\/\/\/\/
      
      Map<String, Object> fieldProps = new HashMap<String, Object>();
      fieldProps.put(PSContentTypeFieldSetter.SEQUENCE, MY_SEQ);
      fieldProps.put(PSContentTypeFieldSetter.LABEL, MY_LABEL);
      
      Map<String, Object> fields = new HashMap<String, Object>();
      fields.put(fieldName, fieldProps);

      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(PSContentTypeSetter.FIELDS, fields);
      
      setter.setProperties(properties);

      removeField(itemDef, fieldName, PSField.TYPE_SHARED);
      
      assertTrue(!itemDef.isFieldExists(justName));

      // perform the test
      PSObjectConfigHandler h = getConfigHandler(setter);
      h.process(itemDef, ObjectState.BOTH, null);

      assertTrue(itemDef.isFieldExists(justName));
      
      // validate the SEQUENCE property
      PSDisplayMapper mapper = itemDef.getContentEditorMapper().getUIDefinition().getDisplayMapper();
      PSDisplayMapping mapping = mapper.getMapping(justName);
      int index = mapper.indexOf(mapping);
      assertTrue(index == 2);

      removeField(itemDef, fieldName, PSField.TYPE_SHARED);
      
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // Testing with NUL or empty properties on the field
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      fields.put(fieldName, null);
      properties.put(PSContentTypeSetter.FIELDS, fields);
      h.process(itemDef, ObjectState.BOTH, null);

      assertTrue(itemDef.isFieldExists(justName));

      //\/\/\/\/\/\/\/\/\/
      // cleanup if needed
      //\/\/\/\/\/\/\/\/\/
      removeField(itemDef, fieldName, PSField.TYPE_SHARED);
   }

   /**
    * Removes a field from a given item definition
    * 
    * @param itemDef the item definition that may contain the field, assumed not
    * <code>null</code>.
    * @param fieldName the name of the field, assumed not <code>null</code> or
    * empty.
    * @param type it is either {@link PSField#TYPE_SHARED} or
    * {@link PSField#TYPE_SYSTEM}
    */
   public static void removeField(PSItemDefinition itemDef, String fieldName,
         int type)
   {
      if (type == PSField.TYPE_LOCAL)
         throw new IllegalArgumentException("Cannot remove a local field.");
      
      // get rid of the shared group name (from fieldName) if there is one
      String justName = PSContentTypeHelper.getSharedFieldName(fieldName)[0];
      
      if (!itemDef.isFieldExists(justName))
         return;

      PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
      if (type == PSField.TYPE_SYSTEM)
      {
         itemDef.getContentEditorMapper().getFieldSet().remove(justName);
      }
      else
      {
         PSSharedFieldGroup shGroup = PSContentTypeHelper
               .getSharedGroup(fieldName);
         PSFieldSet fdSet = mapper.getFieldSet(shGroup.getName());
         if (fdSet != null)
            fdSet.remove(justName);
      }
      itemDef.getContentEditorMapper().getUIDefinition().getDisplayMapper()
            .removeMapping(justName);
      addFieldToExcludes(itemDef, justName, type);
   }
   
   /**
    * Adds a field name to the exclude list of an item definition.
    * 
    * @param itemDef the item definition that contains the field. Assumed not
    * <code>null</code>.
    * @param fieldName the name of the field, assumed not <code>null</code> or
    * empty.
    * @param type the type of the field, assumed returned by PSField.getType().
    */
   @SuppressWarnings("unchecked")
   static private void addFieldToExcludes(PSItemDefinition itemDef,
         String fieldName, int type)
   {
      if (type == PSField.TYPE_SYSTEM)
      {
         ArrayList<String> names = new ArrayList<String>();
         Iterator excludes;
         excludes = itemDef.getContentEditorMapper().getSystemFieldExcludes();
         CollectionUtils.addAll(names, excludes);         
         names.add(fieldName);
         itemDef.getContentEditorMapper().setSystemFieldExcludes(names);
      }
      else
      {
         addFieldToSharedExcludes(itemDef, fieldName);
      }
   }

   /**
    * Adds a shared field to the shared includes into the given item definition.
    * However, if all fields of the shared group are in the excludes, then
    * remove the shared group and its names from the item definition.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param fieldName the name of the shared field in question, assumed not
    * <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   static private void addFieldToSharedExcludes(PSItemDefinition itemDef,
         String fieldName)
   {
      ArrayList<String> names = iteratorToList(itemDef.getContentEditorMapper()
            .getSharedFieldExcludes());
      names.add(fieldName);
      
      PSSharedFieldGroup shGrp = PSContentTypeHelper.getSharedGroup(fieldName);
      if (containAllSharedFields(shGrp, names))
         removeSharedGroup(itemDef, shGrp);
      else
         itemDef.getContentEditorMapper().setSharedFieldExcludes(names);
   }
   
   static private boolean containAllSharedFields(PSSharedFieldGroup shGrp,
         List<String> names)
   {
      List<String> shNames = iteratorToList(shGrp.getFieldSet().getNames());
      return names.containsAll(shNames);
   }

   /**
    * Removes a shared group (name and its field) from the given item definition.
    * 
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @param shGrp the to be removed shared group, assumed not <code>null</code>.
    */
   static private void removeSharedGroup(PSItemDefinition itemDef,
         PSSharedFieldGroup shGrp)
   {
      PSContentEditorMapper mapper = itemDef.getContentEditorMapper();
      
      // remove all field names
      ArrayList<String> shNames = iteratorToList(shGrp.getFieldSet().getNames());
      ArrayList<String> excludes = iteratorToList(mapper
            .getSharedFieldExcludes());
      excludes.removeAll(shNames);
      mapper.setSharedFieldExcludes(excludes);
      
      // remove the shared group 
      ArrayList<String> shGrpNames = iteratorToList(mapper
            .getSharedFieldIncludes());
      shGrpNames.remove(shGrp.getName());
      mapper.setSharedFieldIncludes(shGrpNames);
   }

   /**
    * Converts a Iterator to a list.
    * 
    * @param it the iterator, assumed not <code>null</code>.
    * 
    * @return the converted list, never <code>null</code>, may be empty.
    */
   @SuppressWarnings("unchecked")
   static private ArrayList<String> iteratorToList(Iterator it)
   {
      ArrayList<String> list = new ArrayList<String>();
      CollectionUtils.addAll(list, it);

      return list;
   }

   public void testConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, LOCAL_CFG);
      
      IPSGuid id = validateLocalConfig();
      
      //\/\/\/\//\/\/\/\/\/\
      // Cleanup
      cleanup(id);
   }

   /**
    * Validating the result of running {@link #IMPL_CFG} with {@link #LOCAL_CFG}
    * 
    * @return the ID of the configured Content Type, never <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   private IPSGuid validateLocalConfig() throws Exception
   {
      // validate the Content Type
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);

      assertTrue(itemDef.getLabel().equals("My-Brief"));
      assertTrue(itemDef.getContentEditor().getWorkflowId() == 5);
      PSField fd = itemDef.getFieldByName("placeholder");
      assertTrue(fd.isUserSearchable());
      assertTrue(!fd.isShowInPreview());
      assertTrue(PSContentTypeUtils.hasRequiredRule(fd));

      assertTrue(itemDef.isFieldExists("webdavowner"));
      assertTrue(itemDef.isFieldExists("img2_filename"));
      
      assertTrue(itemDef.getContentEditor().getIconSource().equals(
            PSContentEditor.ICON_SOURCE_NONE));
      assertTrue(StringUtils.isBlank(itemDef.getContentEditor().getIconValue()));
   
      // validate label
      PSUISet uiset = itemDef.getContentEditor().getFieldUiSet("callout");
      assertTrue(uiset.getLabel().getText().equals("Call Out"));
      
      // validate control parameters
      PSControlRef control = uiset.getControl();
      Iterator params = control.getParameters();
      while (params.hasNext())
      {
         PSParam param = (PSParam)params.next();
         if (param.getName().equals("width"))
            assertTrue(param.getValue().getValueText().equals("40"));
         else if (param.getName().equals("height"))
            assertTrue(param.getValue().getValueText().equals("100"));
      }
      
      return itemDef.getGuid();
   }
   
   private void cleanup(IPSGuid ctId) throws Exception
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = factory.getDesignModel(PSTypeEnum.NODEDEF);

      Object obj = model.loadModifiable(ctId);
      PSItemDefinition itemDef = (PSItemDefinition) obj;
      removeField(itemDef, "webdavowner",
            PSField.TYPE_SHARED);
      removeField(itemDef, "img2_filename",
            PSField.TYPE_SHARED);

      //Save test
      model.save(obj);

      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG, DEFAULT_CFG);
      
      itemDef = (PSItemDefinition) model.loadModifiable(ctId);
      assertTrue(itemDef.getContentEditor().getIconSource().equals(
            PSContentEditor.ICON_SOURCE_SPECIFIED));
      assertTrue(itemDef.getContentEditor().getIconValue().equals("rffBrief.gif"));

   }
   
   /**
    * Testing Add & Remove fields through configure files
    * 
    * @throws Exception if an error occurs.
    */
   public void testConfigFiles_AddRemoveFields() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_2, LOCAL_CFG_2);

      // validate "webdavowner" and "img2_filename" fields are ADDED
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);
      assertTrue(itemDef.isFieldExists("webdavowner"));
      assertTrue(itemDef.isFieldExists("img2_filename"));

      
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_2,
            DEFAULT_CFG_2, LOCAL_CFG_2);

      // validate "webdavowner" and "img2_filename" fields are REMOVED
      itemDef = mgr.getItemDef("rffBrief", 1002);
      assertTrue(!itemDef.isFieldExists("webdavowner"));
      assertTrue(!itemDef.isFieldExists("img2_filename"));
   }
   
   /**
    * Testing De-apply configure files
    * 
    * @throws Exception if an error occurs.
    */
   public void testDeApplyConfigFiles() throws Exception
   {
      PSConfigFilesFactoryTest.applyConfig(PKG_NAME, IMPL_CFG_2, LOCAL_CFG_2);

      // validate "webdavowner" and "img2_filename" fields are ADDED
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition itemDef = mgr.getItemDef("rffBrief", 1002);
      assertTrue(itemDef.isFieldExists("webdavowner"));
      assertTrue(itemDef.isFieldExists("img2_filename"));

      
      PSConfigFilesFactoryTest.deApplyConfig(PKG_NAME, IMPL_CFG_2, LOCAL_CFG_2);

      // validate "webdavowner" and "img2_filename" fields are REMOVED
      itemDef = mgr.getItemDef("rffBrief", 1002);
      assertTrue(!itemDef.isFieldExists("webdavowner"));
      assertTrue(!itemDef.isFieldExists("img2_filename"));
   }
   
   public static final String PKG_NAME = "PSContentTypeSetterTest";
   
   public static final String IMPL_CFG = PKG_NAME + "_configDef.xml";

   public static final String LOCAL_CFG = PKG_NAME + "_localConfig.xml";

   public static final String DEFAULT_CFG = PKG_NAME + "_defaultConfig.xml";

   public static final String IMPL_CFG_2 = PKG_NAME + "_2_configDef.xml";

   public static final String LOCAL_CFG_2 = PKG_NAME + "_2_localConfig.xml";

   public static final String DEFAULT_CFG_2 = PKG_NAME + "_2_defaultConfig.xml";

}
