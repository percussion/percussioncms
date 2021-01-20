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
package com.percussion.webservices;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.rhythmyx.UiLocator;
import com.percussion.webservices.rhythmyxdesign.UiDesignLocator;
import com.percussion.webservices.ui.UiSOAPStub;
import com.percussion.webservices.ui.data.PSHierarchyNode;
import com.percussion.webservices.ui.data.PSHierarchyNodePropertiesProperty;
import com.percussion.webservices.uidesign.CreateHierarchyNodesRequest;
import com.percussion.webservices.uidesign.CreateHierarchyNodesRequestType;
import com.percussion.webservices.uidesign.DeleteHierarchyNodesRequest;
import com.percussion.webservices.uidesign.FindHierarchyNodesRequest;
import com.percussion.webservices.uidesign.LoadHierarchyNodesRequest;
import com.percussion.webservices.uidesign.SaveHierarchyNodesRequest;
import com.percussion.webservices.uidesign.UiDesignSOAPStub;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;

/**
 * Implements utilities used by all ui test cases.
 */
@Category(IntegrationTest.class)
public class PSUiTestBase extends PSTestBase
{
   /**
    * Create a new binding for the ui SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new assembly
    *    binding.
    */
   protected UiSOAPStub getBinding(Integer timeout) throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         UiLocator locator = new UiLocator();
         locator.setuiSOAPEndpointAddress(getEndpoint("uiSOAP"));

         UiSOAPStub binding = (UiSOAPStub) locator.getuiSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(60000);
         else
            binding.setTimeout(timeout);

         return binding;
      }
      catch (ServiceException e)
      {
         if (e.getLinkedCause() != null)
            e.getLinkedCause().printStackTrace();

         throw new AssertionFailedError("JAX-RPC ServiceException caught: " + e);
      }
   }

   /**
    * Create a new binding for the assembly design SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new assembly
    *    binding.
    */
   protected static UiDesignSOAPStub getDesignBinding(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         UiDesignLocator locator = new UiDesignLocator();
         locator.setuiDesignSOAPEndpointAddress(getEndpoint("uiDesignSOAP"));

         UiDesignSOAPStub binding = (UiDesignSOAPStub) locator
            .getuiDesignSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(60000);
         else
            binding.setTimeout(timeout);

         return binding;
      }
      catch (ServiceException e)
      {
         if (e.getLinkedCause() != null)
            e.getLinkedCause().printStackTrace();

         throw new AssertionFailedError("JAX-RPC ServiceException caught: " + e);
      }
   }

   @BeforeClass
   public static void setup() throws Exception
   {
      m_session = PSTestUtils.login();

      deleteTestHierarchyNodes(m_session);

      try
      {
         UiDesignSOAPStub binding = getDesignBinding(null);
         PSTestUtils.setSessionHeader(binding, m_session);

         FindHierarchyNodesRequest findRequest = new FindHierarchyNodesRequest();
         findRequest.setPath(null);
         PSObjectSummary[] summaries = binding.findHierarchyNodes(findRequest);
         m_nonTestHierarchyNodesCount = summaries.length;

         createTestHierarchyNodes();
      }
      catch (Exception e)
      {
         System.out.println("Could not create test hierarchy nodes. "
            + "All hierarchy node tests may fail!");
      }

   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   @AfterClass
   protected static void tearDown() throws Exception
   {
      try
      {
         deleteTestHierarchyNodes(m_session);
      }
      catch (Exception e)
      {
         System.out.println("Could not delete test hierarchy nodes.");
      }

      PSTestBase.tearDown();
   }

   /**
    * Creates all hierarchy nodes used for testing.
    * 
    * @throws Exception for any error.
    */
   protected static void createTestHierarchyNodes() throws Exception
   {
      PSHierarchyNodePropertiesProperty[] properties = new PSHierarchyNodePropertiesProperty[1];

      m_words = createHierarchyNode("Words", 0,
         CreateHierarchyNodesRequestType.folder, null, m_session);

      m_nouns = createHierarchyNode("Nouns", m_words.getId(),
         CreateHierarchyNodesRequestType.folder, null, m_session);

      properties[0] = createProperty("type", "noun");
      m_book = createHierarchyNode("book", m_nouns.getId(),
         CreateHierarchyNodesRequestType.placeholder, properties, m_session);

      properties[0] = createProperty("type", "noun");
      m_picture = createHierarchyNode("picture", m_nouns.getId(),
         CreateHierarchyNodesRequestType.placeholder, properties, m_session);

      m_verbs = createHierarchyNode("Verbs", m_words.getId(),
         CreateHierarchyNodesRequestType.folder, null, m_session);

      properties[0] = createProperty("type", "verb");
      m_be = createHierarchyNode("be", m_verbs.getId(),
         CreateHierarchyNodesRequestType.placeholder, properties, m_session);

      properties[0] = createProperty("type", "verb");
      m_do = createHierarchyNode("do", m_verbs.getId(),
         CreateHierarchyNodesRequestType.placeholder, properties, m_session);
   }

   /**
    * Create a property for th esupplied parameters.
    * 
    * @param name the property name, not <code>null</code> or empty.
    * @param value the property value, may be <code>null</code> or empty.
    * @return the new property, never <code>null</code>.
    */
   protected static PSHierarchyNodePropertiesProperty createProperty(String name,
      String value)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      PSHierarchyNodePropertiesProperty property = new PSHierarchyNodePropertiesProperty();

      property.setName(name);
      property.setValue(value);

      return property;
   }

   /**
    * Deletes all hierarchy nodes used for testing.
    * 
    * @param session the session to use, not <code>null</code>.
    * @throws Exception for any error.
    */
   protected static void deleteTestHierarchyNodes(String session) throws Exception
   {
      UiDesignSOAPStub binding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(binding, session);

      PSHierarchyNode words = null;
      FindHierarchyNodesRequest findRequest = new FindHierarchyNodesRequest();
      findRequest.setPath("Words");
      PSObjectSummary[] summaries = binding.findHierarchyNodes(findRequest);
      if (summaries != null && summaries.length > 0)
      {
         LoadHierarchyNodesRequest loadRequest = new LoadHierarchyNodesRequest();
         long[] ids = new long[] { summaries[0].getId() };
         loadRequest.setId(ids);
         loadRequest.setLock(true);

         PSHierarchyNode[] nodes = binding.loadHierarchyNodes(loadRequest);
         if (nodes != null && nodes.length > 0)
            words = nodes[0];
      }

      if (words != null)
      {
         try
         {
            deleteHierarchyNode(words, m_session);
         }
         catch (Exception e)
         {
            System.out.println("Could not delete test node: "
               + m_words.getName());
         }
      }
   }

   /**
    * Create a new hierarcchy node for the specified name and parent.
    * 
    * @param name the node name, not <code>null</code> or empty.
    * @param parent the parent to which to attach the new node as child,
    *    0 to create a new root node.
    * @param type the node type, not <code>null</code>.
    * @param properties the node properties, may be <code>null</code> or empty. 
    * @param session the rhythmyx session to use for all service calls, not
    *    <code>null</code> or empty.
    * @return the new created hierarchy node, never <code>null</code>.
    * @throws Exception for any error creating the new hierarchy node.
    */
   protected static PSHierarchyNode createHierarchyNode(String name, long parent,
      CreateHierarchyNodesRequestType type,
      PSHierarchyNodePropertiesProperty[] properties, String session)
      throws Exception
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      UiDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      CreateHierarchyNodesRequest createRequest = new CreateHierarchyNodesRequest();
      createRequest.setName(new String[] { name });
      createRequest.setParentId(new long[] { parent });
      createRequest.setType(new CreateHierarchyNodesRequestType[] { type });
      PSHierarchyNode[] nodes = binding.createHierarchyNodes(createRequest);
      if (properties != null)
         nodes[0].setProperties(properties);

      SaveHierarchyNodesRequest saveRequest = new SaveHierarchyNodesRequest();
      saveRequest.setPSHierarchyNode(nodes);
      binding.saveHierarchyNodes(saveRequest);

      return nodes[0];
   }

   /**
    * Delete the supplied hierarchy node.
    * 
    * @param node the node to be deleted, may be <code>null</code>.
    * @param session the rhythmyx session to use for all service calls, not
    *    <code>null</code> or empty.
    * @throws Exception for any error deleting the supplied node.
    */
   protected static void deleteHierarchyNode(PSHierarchyNode node, String session)
      throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (node != null)
      {
         UiDesignSOAPStub binding = getDesignBinding(null);

         PSTestUtils.setSessionHeader(binding, session);

         long[] ids = new long[1];
         ids[0] = node.getId();

         // lock nodes
         lockHierarchyNodes(ids, session);

         // now delete it
         DeleteHierarchyNodesRequest request = new DeleteHierarchyNodesRequest();
         request.setId(ids);
         binding.deleteHierarchyNodes(request);
      }
   }

   /**
    * Lock all hierarchy nodes for the supplied ids.
    * 
    * @param ids the ids of the nodes to lock, not <code>null</code> or
    *    empty.
    * @param session the session for which to lock the slots, not
    *    <code>null</code> or empty.
    * @return the locked hierarchy nodes, never <code>null</code> or empty.
    * @throws Exception for any error locking the nodes.
    */
   protected static PSHierarchyNode[] lockHierarchyNodes(long[] ids, String session)
      throws Exception
   {
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      UiDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, session);

      LoadHierarchyNodesRequest loadRequest = new LoadHierarchyNodesRequest();
      loadRequest.setId(ids);
      loadRequest.setLock(true);
      PSHierarchyNode[] nodes = binding.loadHierarchyNodes(loadRequest);

      return nodes;
   }

   /**
    * The number of test nodes created, initialized in {@link #setUp()}.
    */
   protected static int m_nonTestHierarchyNodesCount = -1;

   /**
    * The number of test nodes created.
    */
   protected int m_testHierarchyNodesCount = 7;

   /**
    * Get the ids of all test hierarchy nodes.
    * 
    * @return all ids, never <code>null</code> or empty.
    */
   protected long[] getHierachyNodeIds()
   {
      long[] ids = new long[m_testHierarchyNodesCount];
      ids[0] = m_words.getId();
      ids[1] = m_nouns.getId();
      ids[2] = m_book.getId();
      ids[3] = m_picture.getId();
      ids[4] = m_verbs.getId();
      ids[5] = m_be.getId();
      ids[6] = m_do.getId();

      return ids;
   }

   /**
    * Gte all test hierarchy nodes.
    * 
    * @return all test hierarchy nodes, never <code>null</code> or empty.
    */
   protected PSHierarchyNode[] getHierarchyNodes()
   {
      PSHierarchyNode[] nodes = new PSHierarchyNode[m_testHierarchyNodesCount];
      nodes[0] = m_words;
      nodes[1] = m_nouns;
      nodes[2] = m_book;
      nodes[3] = m_picture;
      nodes[4] = m_verbs;
      nodes[5] = m_be;
      nodes[6] = m_do;

      return nodes;
   }

   // test hierarchy nodes
   protected static PSHierarchyNode m_words = null;

   protected static PSHierarchyNode m_verbs = null;

   protected static PSHierarchyNode m_do = null;

   protected static PSHierarchyNode m_be = null;

   protected static PSHierarchyNode m_nouns = null;

   protected static PSHierarchyNode m_book = null;

   protected static PSHierarchyNode m_picture = null;
}
