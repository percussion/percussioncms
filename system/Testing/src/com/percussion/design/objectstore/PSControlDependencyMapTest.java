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
package com.percussion.design.objectstore;

import com.percussion.utils.testing.UnitTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSControlDependencyMap} class.
 */
@Category(UnitTest.class)
public class PSControlDependencyMapTest
{
   /**
    * Test all functionality of the map
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testMap() throws Exception
   {
      Map<String, String> userProps = loadUserProps();
      Map<String, PSControlMeta> metaMap = getControlMetaMap();
      List<PSDisplayMapping> mappings = loadDisplayMappings();
      PSExtensionCallSet extList = loadExtensions();
      PSExtensionCallSet extListOrig = (PSExtensionCallSet) extList.clone();
      
      int extCount = extList.size();
      
      PSControlDependencyMap depMap = new PSControlDependencyMap(userProps, 
         extList);
      
      // one extension call is not for a control
      assertTrue(extList.size() != extCount);
      assertTrue(extCount == extList.size() + 
         depMap.getInputDataExtensions().size());

      // test round trip no changes
      PSExtensionCallSet modExtList = (PSExtensionCallSet) extList.clone();
      Map<String, List<PSDependency>> resultMap = validateMap(userProps, 
         metaMap, mappings, extList, extListOrig, depMap);
      
      // clear and re-set, revalidate
      depMap.clearControlDependencies();
      for (PSDisplayMapping mapping : mappings)
      {
         depMap.setControlDependencies(mapping, resultMap.get(
            mapping.getFieldRef()));
      }
      
      resultMap = validateMap(userProps, metaMap, mappings,
         (PSExtensionCallSet) modExtList.clone(), extListOrig, depMap);      
      
      // now test adds and removals
      // test w/sys_webImageFX to test 2 deps (this has been rigged in the data)
      extList = (PSExtensionCallSet) extListOrig.clone();
      PSControlMeta meta = metaMap.get("sys_webImageFX");
      assertNotNull(meta);
      String name = "imageField";
      PSUISet uiset = new PSUISet();
      PSControlRef control = new PSControlRef(meta.getName());
      control.setId(25);
      uiset.setControl(control);
      PSDisplayMapping newMapping = new PSDisplayMapping(name, uiset);
      mappings.add(newMapping);
      List<PSDependency> newDepList = depMap.getControlDependencies(newMapping, 
         meta);
      int nextDepId = 100;
      assertTrue(newDepList.size() > 1);
      for (PSDependency dependency : newDepList)
      {
         
         if (dependency.getOccurrence() == PSDependency.SINGLE_OCCURRENCE)
         {
            assertTrue(dependency.getId() != 0);
         }
         else
         {
            assertEquals(dependency.getId(), 0);
            dependency.setId(nextDepId++);
            extList.add(0, dependency.getDependent());
         }
      }
      depMap.setControlDependencies(newMapping, newDepList);
      
      PSExtensionCallSet newExtList = (PSExtensionCallSet) modExtList.clone();
      Map<String, String> newUserProps = depMap.generateUserProperties(
         newExtList);
      assertEquals(extList.size(), newExtList.size());
      assertEquals(newExtList.size(), depMap.getInputDataExtensions().size() 
         + 1);
      resultMap = validateMap(newUserProps, metaMap, mappings, 
         (PSExtensionCallSet) modExtList.clone(), newExtList, depMap);
      
      // test removal
      mappings.remove(mappings.size() - 1);
      depMap.clearControlDependencies();
      
      for (PSDisplayMapping mapping : mappings)
      {
         depMap.setControlDependencies(mapping, resultMap.get(
            mapping.getFieldRef()));
      }
      
      validateMap(userProps, metaMap, mappings, modExtList, extListOrig, 
         depMap);
   }


   /**
    * Validates the supplied map against the supplied values.
    * 
    * @param userProps The expected user props, assumed not <code>null</code>.
    * @param metaMap The map of control name to control meta, assumed not 
    * <code>null</code>.
    * @param mappings List of mappings whose controls are expected in the map,
    * assumed not <code>null</code>. 
    * @param extList List of input data extensions left after originally 
    * constructing the map, assumed not <code>null</code>.
    * @param extListOrig List of all extensions expected, assumed not 
    * <code>null</code>. 
    * @param depMap The map to validate, assumed not <code>null</code>.
    * 
    * @return Map of dependencies found in the map, key is the mapping field 
    * name, value is the list of the mapping's control dependencies. 
    */
   private Map<String, List<PSDependency>> validateMap(
      Map<String, String> userProps, Map<String, PSControlMeta> metaMap, 
      List<PSDisplayMapping> mappings, PSExtensionCallSet extList, 
      PSExtensionCallSet extListOrig, PSControlDependencyMap depMap)
   {
      int extCount = extListOrig.size();
    
      Map<String, List<PSDependency>> resultMap = 
         new HashMap<String, List<PSDependency>>();
      
      Map<String, String> newUserProps = depMap.generateUserProperties(extList);
      assertEquals(userProps, newUserProps);
      assertEquals(extList.size(), extCount);
      // ensure the non-control exit remains at the end
      assertEquals(extList.get(extCount - 1), extListOrig.get(extCount - 1));
      compareExtensions(extList, extListOrig);
      
      
      Map<Integer, IPSDependentObject> singleMap = 
         new HashMap<Integer, IPSDependentObject>();
      // check get
      for (PSDisplayMapping mapping : mappings)
      {
         PSControlMeta meta = metaMap.get(
            mapping.getUISet().getControl().getName()); 
         assertNotNull(meta);
         List<PSDependency> deps = depMap.getControlDependencies(mapping, meta);
         assertFalse(deps.isEmpty());
         assertEquals(deps.size(), meta.getDependencies().size());
         resultMap.put(mapping.getFieldRef(), deps);
         for (PSDependency dep : deps)
         {
            IPSDependentObject depObj = dep.getDependent();
            int depObjId = depObj.getId();
            assertTrue(depObjId != 0);
            
            if (dep.getOccurrence() == PSDependency.SINGLE_OCCURRENCE)
            {
               IPSDependentObject singleOcc = singleMap.get(depObjId);
               if (singleOcc == null)
                  singleMap.put(depObjId, depObj);
               else
                  assertTrue(depObj == singleOcc);
            }
            
            // check macros
            PSExtensionCall call = (PSExtensionCall) depObj;
            if (call.getName().equals("sys_xdTextCleanup"))
            {
               assertEquals("$(fieldName)", 
                  call.getParamValues()[0].getValue().toString());
            }
         }
      }
      
      return resultMap;
   }

   /**
    * Asserts the two sets of extensions are equal without regard for order.
    * 
    * @param extCalls The first set of extensions, assumed not 
    * <code>null</code>, may be empty.
    * @param otherExtCalls The second set of extensions, assumed not
    * <code>null</code>, may be empty.
    */
   private void compareExtensions(PSExtensionCallSet extCalls, 
      PSExtensionCallSet otherExtCalls)
   {
      assertEquals(extCalls.size(), otherExtCalls.size());
      
      Map<Integer, PSExtensionCall> extmap = 
         new HashMap<Integer, PSExtensionCall>();
      Iterator calls = otherExtCalls.iterator();
      while (calls.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall) calls.next();
         extmap.put(call.getId(), call);
      }
      
      calls = extCalls.iterator();
      while (calls.hasNext())
      {
         PSExtensionCall call = (PSExtensionCall) calls.next();
         assertEquals(call, extmap.get(call.getId()));
      }
   }

   /**
    * Get the map of test control meta.
    * 
    * @return The map, key is the control name, value is the meta, never 
    * <code>null</code> or empty.
    * 
    * @throws Exception if there are any errors loading the map from test data.
    */
   private Map<String, PSControlMeta> getControlMetaMap() throws Exception
   {
      List<PSControlMeta> metaList = loadControlMeta();
      Map<String, PSControlMeta> map = new HashMap<String, PSControlMeta>();
      for (PSControlMeta meta : metaList)
      {
         map.put(meta.getName(), meta);
      }
      
      return map;
   }

   /**
    * Loads the set of test extension calls.
    * 
    * @return The set, never <code>null</code> or empty.
    * 
    * @throws Exception if there is an error loading the test data.
    */
   private PSExtensionCallSet loadExtensions() throws Exception
   {
      Document doc = getDocument("controlInputDataExits.xml");
      return new PSExtensionCallSet(doc.getDocumentElement(), null, null);
   }

   /**
    * Loads the test user properties.
    * 
    * @return The map of user props, key is the property name, value is the
    * property value, never <code>null</code> or empty.
    * 
    * @throws Exception if there is an error loading the test data.
    */
   private Map<String, String> loadUserProps() throws Exception
   {
      Document doc = getDocument("controlUserProperties.xml");
      return PSContentEditorPipe.loadUserProps(doc.getDocumentElement());
   }

   /**
    * Load the list of test control meta objects.
    * 
    * @return The list, never <code>null</code> or empty.
    * 
    * @throws Exception if there is an error loading the test data.
    */
   private List<PSControlMeta> loadControlMeta() throws Exception
   {
      Document doc = getDocument("controlmeta.xml");
      
      List<PSControlMeta> controls = new ArrayList<PSControlMeta>();
      NodeList nodes = doc.getElementsByTagName(
         PSControlMeta.XML_NODE_NAME);
      for (int i = 0; i < nodes.getLength(); i++) 
      {
         Element control = (Element)nodes.item(i);
         PSControlMeta meta = new PSControlMeta(control);
         controls.add(meta);
      }
      
      return controls;            
   }

   /**
    * Load the list of test display mappings.
    * 
    * @return The list, never <code>null</code> or empty.
    * 
    * @throws Exception if there is an error loading the test data.
    */
   @SuppressWarnings("unchecked")
   private List<PSDisplayMapping> loadDisplayMappings() throws Exception
   {
      Document doc = getDocument("controlDisplayMappings.xml");
      PSDisplayMapper mapper = new PSDisplayMapper(doc.getDocumentElement(), 
         null, null);
      
      List<PSDisplayMapping> mappings = new ArrayList<PSDisplayMapping>();
      mappings.addAll(mapper);
      
      return mappings;
   }
   
   /**
    * Load the specified xml document from the {@link #RESOURCE_BASE} directory.
    * 
    * @param fileName The name of the file to load, assumed not 
    * <code>null</code> or empty and to exist in that directory.
    * 
    * @return The document, never <code>null</code>.
    * 
    * @throws Exception if there are any errors loading or parsing the document.
    */
   private Document getDocument(String fileName) throws Exception
   {
      try(InputStream is = this.getClass().getResourceAsStream(RESOURCE_BASE + "/" + fileName))
      {
         return PSXmlDocumentBuilder.createXmlDocument(is, false);
      }
   }
   
   /**
    * The directory containing test resource files.
    */
   private static final String RESOURCE_BASE = 
      "/com/percussion/design/objectstore";
}

