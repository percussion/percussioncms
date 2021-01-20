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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionProperty;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.cms.objectstore.PSMenuModeContextMapping;
import com.percussion.design.objectstore.PSRelationshipConfigTest;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.transformation.PSTransformationException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit tests for the {@link PSActionConverter} class.
 */
@Category(IntegrationTest.class)
public class PSActionConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object and vice versa.
    */
   public void testConversion() throws Exception
   {
      // test with simple action
      PSAction simpleAction = getSimpleAction("simpleAction");
      roundTripConvertion(simpleAction);
      
      // test item (menu) action
      PSAction source = getAction("testAction");
      roundTripConvertion(source);

      // test without "target" property
      source.getProperties().removeProperty(PSAction.PROP_TARGET_STYLE);
      source.getProperties().removeProperty(PSAction.PROP_TARGET);
      roundTripConvertion(source);

      // test cascading menu action
      source.setURL(null);
      roundTripConvertion(source);

      // test dynamic menu action
      source.setMenuType(PSAction.TYPE_MENU);
      roundTripConvertion(source);
   }

   /**
    * Test with modified properties.
    *
    * @throws Exception
    */
   public void testConversionWithPropChanges() throws Exception
   {
      // test simple action object
      PSAction source = getAction("testAction");
      PSAction modified = (PSAction) source.cloneFull();
      assertTrue(source.equals(modified));

      PSActionVisibilityContexts vctxs = modified.getVisibilityContexts();
      vctxs.clear();

      roundTripConvertion(modified);
   }

   /**
    * Test a list of server object convert to client array, and vice versa.
    * 
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unchecked")
   public void testListToArray() throws Exception
   {
      // test simple action objects
      List<PSAction> srcList = new ArrayList<PSAction>();
      srcList.add(getAction("testAction"));
      srcList.add(getAction("testAction_2"));
      
      List<PSAction> srcList2 = roundTripListConversion(
            com.percussion.webservices.ui.data.PSAction[].class, srcList);

      assertTrue(srcList.equals(srcList2));
      
      // test complex actions, just to make sure the round trip works for now.
      srcList = getActions();
      srcList2 = roundTripListConversion(
            com.percussion.webservices.ui.data.PSAction[].class, srcList);
      // assertTrue(srcList.equals(srcList2));
   }
   
   @SuppressWarnings("unused")
   private void roundTripConvertion(PSAction source) throws PSTransformationException
   {
      PSAction target = (PSAction) roundTripConversion(PSAction.class,
            com.percussion.webservices.ui.data.PSAction.class, source);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      //System.out.println(PSXmlDocumentBuilder.toString(source.toXml(doc)));
      //System.out.println(PSXmlDocumentBuilder.toString(target.toXml(doc)));

      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }

   /**
    * Creates an action with the given name.
    * 
    * @param name the name of the new action, assumed not <code>null</code>.
    * 
    * @return the created action, never <code>null</code>.
    */
   private PSAction getSimpleAction(String name)
   {
      PSAction target = new PSAction(name, name + " label");
      target.setLocator(PSAction.createKey(String.valueOf(123)));
      return target;
   }

   /**
    * Creates an action with the given name.
    * 
    * @param name the name of the new action, assumed not <code>null</code>.
    * 
    * @return the created action, never <code>null</code>.
    */
   private PSAction getAction(String name)
   {
      PSAction target = new PSAction(name, name + " label");

      int actionId = 100;

      target.setLocator(PSAction.createKey(String.valueOf(actionId)));
      target.setMenuType(PSAction.TYPE_MENUITEM);
      target.setSortRank(1);
      target.setDescription("Test Action Desc");
      target.setURL("http://localhost:9999/Rhythmyx/testAction");
      target.setClientAction(true);

      target.getParameters().add(new PSActionParameter("p1", "v1"));
      target.getParameters().add(new PSActionParameter("p2", "v2"));

      // set properties
      addProperty(target, PSAction.PROP_ACCEL_KEY, "Alt-K");
      addProperty(target, PSAction.PROP_MNEM_KEY, "Enum-K");
      addProperty(target, PSAction.PROP_SHORT_DESC, "tooltip");
      addProperty(target, PSAction.PROP_SMALL_ICON, "http://localhost:9999/Rhytmyx/smallicom");
      addProperty(target, PSAction.PROP_REFRESH_HINT, "selected");
      addProperty(target, PSAction.PROP_LAUNCH_NEW_WND, PSAction.YES);
      addProperty(target, PSAction.PROP_MUTLI_SELECT, PSAction.NO);

      addProperty(target, PSAction.PROP_TARGET, "test-target");
      addProperty(target, PSAction.PROP_TARGET_STYLE, "target-style");

      target.setModeUIContexts(getModeUIContexts(actionId));

      target.setVisibilityContexts(getVisibilityContexts());

      // get child (reference) actions
      PSChildActions children = target.getChildren();
      PSMenuChild tgtChild = new PSMenuChild(1, "child_name_1", actionId);
      children.add(tgtChild);
      tgtChild = new PSMenuChild(2, "child_name_2", actionId);
      children.add(tgtChild);

      return target;

   }

   private void addProperty(PSAction action, String name, String value)
   {
      if (value != null && value.trim().length() > 0)
      {
         PSActionProperty prop = new PSActionProperty(name, value);
         action.getProperties().add(prop);
      }
   }

   private PSDbComponentCollection getModeUIContexts(int parentId)
   {
      String actionId = String.valueOf(parentId);

      PSDbComponentCollection modeCtxs = new PSDbComponentCollection(
            PSMenuModeContextMapping.class);
      modeCtxs.add(new PSMenuModeContextMapping("1", "2", actionId));
      modeCtxs.add(new PSMenuModeContextMapping("1", "3", actionId));
      modeCtxs.add(new PSMenuModeContextMapping("4", "5", actionId));

      return modeCtxs;
   }

   private PSActionVisibilityContexts getVisibilityContexts()
   {
      PSActionVisibilityContexts vises = new PSActionVisibilityContexts();
      vises.addContext("ctx-1", "ctx-1-value");
      vises.addContext("ctx-2", "ctx-2-value");
      vises.addContext("ctx-3", new String[]{"ctx-3-value_1", "ctx-3-value_2"});

      return vises;
   }
   
   /**
    * @return a list of display formats from test data file, never 
    *    <code>null</code> or empty.
    *
    * @throws Exception if an error occurs.
    */
   @SuppressWarnings("unused")
   private List<PSAction> getActions() throws Exception
   {
      Element elems = PSRelationshipConfigTest.loadXmlResource(
            "../../rhythmyxdesign/PSActions.xml", this.getClass());

      NodeList nodes = elems.getElementsByTagName(PSAction.XML_NODE_NAME);
      List<PSAction> actions = new ArrayList<PSAction>();
      int length = nodes.getLength();
      for (int i=0; i < length; i++)
      {
         Element actionElem = (Element) nodes.item(i);
         actions.add(new PSAction(actionElem));
      }
      
      return actions;
   }


}

