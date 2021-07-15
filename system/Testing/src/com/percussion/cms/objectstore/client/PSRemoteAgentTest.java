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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.cms.objectstore.client;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.cms.objectstore.ws.PSWsFolderProcessorTest;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.testing.IPSClientBasedJunitTest;
import com.percussion.testing.PSClientTestCase;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSRemoteRequester;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.testing.SpringContextTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.TestSuite;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSRemoteAgent</code> class.
 */
@Category({IntegrationTest.class})
public class PSRemoteAgentTest extends PSClientTestCase
{

   public PSRemoteAgentTest()
   {
   }


   private PSRemoteAgent getRemoteAgent() throws Exception
   {
      PSRemoteRequester req = new PSRemoteRequester(            
            getConnectionProps(IPSClientBasedJunitTest.CONN_TYPE_RXSERVER));

      return new PSRemoteAgent(req);
   }

   /**
    * Testing copy folder child item for webservices.
    *
    * @throws Exception if an error occurs.
    */
   @Test
   public void testCopyFolderChildrenItem() throws Exception
   {
      PSLocator locator = insertBrief();
      PSWsFolderProcessorTest.testCopyFolderChildrenItem(locator);
      getRemoteAgent().purgeItem(locator);
   }

   /**
    * Test insert & update items
    *
    * @throws Exception if an error occurs.
    */
   @Test
   public void testInsertUpdateItem() throws Exception
   {
      PSLocator locator = insertBrief();
      udpateItem(getRemoteAgent(), locator);

   }

   /**
    * Test get communities
    *
    * @throws Exception if an error occurs
    */
   @Test
   public void testGetCommunities() throws Exception
   {
      PSEntry community = getRemoteAgent().getDefaultUserCommunity();
      PSEntry defaultCommunity =
         new PSEntry("1001", new PSDisplayText("Enterprise_Investments_Admin"));
      // Default community is not the default stored in the roles table unless it is set by
      // first login.  The @defaultCommunity attribute is really the current community set for the
      // user and therefore is affected by previous tests.
      // User/SessionObject/sys_community
      // see login resource on sys_psxWebServices xml application
      //assertTrue("Expected community is Enterprise_Investments_Admin (1001) is "+community.getLabel().getText()+" value="+community.getValue() +"Source type="+community.getSourceType() +" sequence="+community.getSequence(),community.equals(defaultCommunity));
      

      List communities = getRemoteAgent().getCommunities();
      assertTrue(communities.contains(defaultCommunity));
   }

   /**
    * Test get workflows
    *
    * @throws Exception if an error occurs
    */
   @Test
   public void testGetWorkflows() throws Exception
   {
      PSEntry defaultCommunity =
         new PSEntry("1002", new PSDisplayText("Enterprise_Investments"));
      PSEntry wfSimple = new PSEntry("4", new PSDisplayText("Simple Workflow"));
      PSEntry wfStd = new PSEntry("5", new PSDisplayText("Standard Workflow"));

      List workflows = getRemoteAgent().getWorkflows(defaultCommunity);
      assertTrue(workflows.contains(wfSimple));
      assertTrue(workflows.contains(wfStd));
   }

   /**
    * Test get content types
    *
    * @throws Exception if an error occurs
    */
   @Test
   public void testGetContentTypes() throws Exception
   {
      PSEntry defaultCommunity =
         new PSEntry("1002", new PSDisplayText("Enterprise_Investments"));
      PSEntry article = new PSEntry("302", new PSDisplayText("rffBrief"));

      List contentTypes = getRemoteAgent().getContentTypes(defaultCommunity);
      assertTrue(contentTypes.contains(article));
   }

   /**
    * Test get context variables
    *
    * @throws Exception if an error occurs
    */
   @Test
   public void testGetContextVariables() throws Exception
   {
      PSEntry rxcss =
         new PSEntry(
            "web_resources/enterprise_investments",
            new PSDisplayText("rxs_navbase"));

      List ctxVars = getRemoteAgent().getContextVariables();
      assertTrue(ctxVars.contains(rxcss));
   }

   /**
    * Test get transitions for a given workflow
    *
    * @throws Exception if an error occurs
    */
   @Test
   public void testGetTransitions() throws Exception
   {
      PSEntry wfIndex = new PSEntry("4", new PSDisplayText("Simple Workflow"));
      PSEntry toPublic = new PSEntry("DirecttoPublic", new PSDisplayText(
            "Direct to Public"));
      PSEntry approve = new PSEntry("Approve", new PSDisplayText("Approve"));

      List transitions = getRemoteAgent().getTransitions(wfIndex);

      assertTrue(transitions.contains(toPublic));
      assertTrue(transitions.contains(approve));
   }

   /**
    * Insert an Article
    *
    * @return The locator of the inserted article item, never <code>null</code>.
    * @throws Exception
    */
   public static PSLocator insertBrief() throws Exception
   {
      PSRemoteAgentTest tst = new PSRemoteAgentTest();
      PSRemoteAgent remoteAgent = tst.getRemoteAgent();
      PSLocator locator = insertItem(remoteAgent, "rffBrief");

      return locator;
   }

   /**
    * Testing insert an item, only tested with Brief content type for now
    *
    * @param remoteAgent The object used to insert the item, may not
    *    <code>null</code>
    * @param contentType The content type for the to be inserted item, may
    *    not be <code>null</code> or empty.
    *
    * @return the locator of the inserted item. 
    * 
    * @throws PSRemoteException if an error occurs.
    */
   public static PSLocator insertItem(
      PSRemoteAgent remoteAgent,
      String contentType)
      throws PSRemoteException
   {
      if (remoteAgent == null)
         throw new IllegalArgumentException("remoteAgent may not be null");
      if (contentType == null || contentType.trim().length() == 0)
         throw new IllegalArgumentException("contentType may not be null or empty");

      PSClientItem item = remoteAgent.newItem(contentType);

      PSItemField title = item.getFieldByName("sys_title");
      PSTextValue titleVal = new PSTextValue("title-from-PSRemoteClientTest");
      title.addValue(titleVal);

      PSItemField communityid = item.getFieldByName("sys_communityid");
      PSTextValue communityValue = new PSTextValue("1002");
      communityid.addValue(communityValue);

      PSItemField locale = item.getFieldByName("sys_lang");
      PSTextValue localeValue = new PSTextValue("en-us");
      locale.addValue(localeValue);

      // a field exists in Brief content type
      PSItemField callout = item.getFieldByName("callout");
      if (callout != null)
      {
         PSTextValue calloutVal = new PSTextValue(
               "This is test content from the PSRemoteClientTest!");
         callout.addValue(calloutVal);
      }

      // a field exists in Brief content type
      PSItemField displayTitle = item.getFieldByName("displaytitle");
      if (displayTitle != null)
         displayTitle.addValue(titleVal);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSLocator locator = remoteAgent.updateItem(item.toXml(doc), true);

      //System.out.println("contentId: " + locator.getId() +
      //   "  revision: " + locator.getRevision());

      return locator;
   }

   /**
    * Testing update an item through an <code>PSContentData</code> object.
    *
    * @param remoteAgent The object used to update an item, may not betestIns
    *    <code>null</code>
    * @param locator The locator for the to be updated item, may not be
    *    <code>null</code>.
    *
    * @throws Exception if an error occurs.
    */
   public static void udpateItem(PSRemoteAgent remoteAgent, PSLocator locator)
      throws Exception
   {
      PSClientItem item = remoteAgent.openItem(locator, false, false);

      PSItemField title = item.getFieldByName("sys_title");
      PSTextValue titleVal = new PSTextValue("title-from-client-AAAA-"
            + System.currentTimeMillis());
      title.addValue(titleVal);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      if (remoteAgent.checkOutItem(locator))
      {
         locator = remoteAgent.updateItem(item.toXml(doc), true);
      }
      else
      {
         throw new Exception(
            "fail to checkout item: ("
               + locator.getId()
               + ", "
               + locator.getRevision()
               + ")");
      }

   }

   /**
    * Test opening a binary item and then writing it to the local disk
    * 
    * @throws Exception if error occurs
    */
   public void performOpenItem() throws Exception
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSRemoteAgent remoteAgent = getRemoteAgent();
      
      PSLocator locator = new PSLocator(310, 1);
      PSClientItem item = remoteAgent.openItem(locator, true, false);
      
      PSItemField field = item.getFieldByName("imgbody");

      Element el = item.toMinXml(doc, true, true, true, true);
      System.out.println(PSXmlDocumentBuilder.toString(el));

      PSBinaryValue value = (PSBinaryValue) field.getValue();

      File file = new File("c:/testimage3.gif");
      FileOutputStream fos = new FileOutputStream(file);
      fos.write((byte[]) value.getValue());
      fos.close();

   }
   
   /**
    * Test update binary with existing item
    * 
    * @throws Exception if error occurs
    */
   public void performUpdateBinary() throws Exception
   {
      PSRemoteAgent remoteAgent = getRemoteAgent();
      PSLocator locator = new PSLocator(302, 1);
      PSClientItem item = remoteAgent.openItem(locator, false, false);
      
      //Get the binary data
      File file = new File("c:/testimage2.gif");
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      fis.read(data);
      fis.close();
      
      PSItemField binaryField = item.getFieldByName("imgbody");
      binaryField.addValue(new PSBinaryValueEx(data, "testItAgain.gif", null));
      
      remoteAgent.updateItem(item, false); 
      
   }


   /**
    * Test update binary with new item
    * 
    * @throws Exception if error occurs
    */
   public void performUpdateBinaryNew() throws Exception
   {
      PSRemoteAgent remoteAgent = getRemoteAgent();
      
      PSClientItem item = remoteAgent.newItem("4");
      
      //Set fields
      PSItemField itemField = null;
      itemField = item.getFieldByName("sys_title");
      itemField.addValue(new PSTextValue("Automated Test Image (Unit Test)"));
      
      //Get the binary data
      File file = new File("c:/testimage2.gif");
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      fis.read(data);
      fis.close();
      
      PSItemField binaryField = item.getFieldByName("imgbody");
      binaryField.addValue(new PSBinaryValueEx(data, "testItAgain.gif", null));
      
      remoteAgent.updateItem(item, true); 
      
   }
   

   /**
    * Get relationship processor proxy for remote processor.
    *
    * @param rAgent used to communicate with remote Rhythmyx server.
    * 
    * @return The remote proxy, never <code>null</code>.
    *
    * @throws PSCmsException if any error occurs
    */
   private static PSRelationshipProcessorProxy getRemoteRelationshipProxy(
      PSRemoteAgent rAgent)
      throws PSCmsException
   {
      
      IPSRemoteRequester requester = rAgent.getRemoteRequester();

      PSRelationshipProcessorProxy proxy = new PSRelationshipProcessorProxy(
         PSRelationshipProcessorProxy.PROCTYPE_REMOTE, requester);

      return proxy;
   }   


}
