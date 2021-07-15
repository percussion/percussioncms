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
package com.percussion.webservices;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.security.PSPermissions;
import com.percussion.testing.IPSUnitTestConfigHelper;
import com.percussion.testing.PSClientTestCase;
import com.percussion.testing.PSConfigHelperTestCase;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCallResult;
import com.percussion.webservices.rhythmyx.ContentLocator;
import com.percussion.webservices.rhythmyx.SecurityLocator;
import com.percussion.webservices.rhythmyx.SystemLocator;
import com.percussion.webservices.rhythmyxdesign.SystemDesignLocator;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSLocale;
import com.percussion.webservices.security.data.PSLogin;
import com.percussion.webservices.system.LoadRelationshipsRequest;
import com.percussion.webservices.system.LoadWorkflowsRequest;
import com.percussion.webservices.system.PSAccessLevelImpl;
import com.percussion.webservices.system.PSAclEntryImpl;
import com.percussion.webservices.system.PSAclImpl;
import com.percussion.webservices.system.PSRelationship;
import com.percussion.webservices.system.PSRelationshipFilter;
import com.percussion.webservices.system.PSWorkflow;
import com.percussion.webservices.system.SystemSOAPStub;
import com.percussion.webservices.systemdesign.DeleteAclsRequest;
import com.percussion.webservices.systemdesign.SaveAclsRequest;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.apache.axis.client.Call;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Implements utilities used by all webservice test cases.
 */
@Category(IntegrationTest.class)
public class PSTestBase extends PSClientTestCase
{
   public PSTestBase(String name)
   {
      super(name);
   }

   public PSTestBase()
   {}

   /**
    * Create the endpoint url from the <code>conn_rxserver.properties</code>
    * file if found, otherwise use the default of 
    * <code>http://localhost:9992/Rhythmyx/webservices/ + service</code>.
    * 
    * @param service the name of the service for which to create the endpoint, 
    *    e.g. assemblySOAP, contentSOAP, etc., not <code>null</code> or empty.
    * @return the endpoint url for the supplied service, never <code>null</code>
    *    or empty.
    */
   public static String getEndpoint(String service)
   {
      Properties properties = getServerProperties();
      
      String endpoint = 
         properties.getProperty(PSDesignerConnection.PROPERTY_PROTOCOL) + 
         "://" + properties.getProperty(PSDesignerConnection.PROPERTY_HOST) + 
         ":" + properties.getProperty(PSDesignerConnection.PROPERTY_PORT) + 
         "/" + properties.getProperty("root") + "/webservices/" + service;

      return endpoint;
   }
   
   /**
    * Get the server connection properties used for testing. If none are found
    * the defaults will be set for 'protocol', 'host', 'port' and 'root'.
    * 
    * @return the server connection properties, never <code>null</code> or
    *    empty. At least the defaults for 'protocol', 'host', 'port' and 'root'
    *    are returned. 
    */
   protected static Properties getServerProperties()
   {
      Properties properties = null;
      try
      {
         properties = getConnectionProps(
            PSConfigHelperTestCase.CONN_TYPE_RXSERVER);
      }
      catch (IOException e)
      {
         System.out.println("Failed to load the server configuration for "
            + "the following reason: " + e.getLocalizedMessage() + "We will "
            + "continue with the default endpoint of "
            + "http://localhost:9992/Rhythmyx/webservices/assemblySOAP");
      }

      if (properties == null)
      {
         // use defaults
         properties = new Properties();
         properties.setProperty(PSDesignerConnection.PROPERTY_PROTOCOL, "http");
         properties.setProperty(PSDesignerConnection.PROPERTY_HOST, "localhost");
         properties.setProperty(PSDesignerConnection.PROPERTY_PORT, "9992");
         properties.setProperty("root", "Rhythmyx");
      }
      else
      {
         if (properties.get(PSDesignerConnection.PROPERTY_PROTOCOL) == null)
         {
            String useSsl = properties.getProperty(
               IPSUnitTestConfigHelper.PROP_USESSL, "false");
            if (useSsl.equalsIgnoreCase("true") || 
               useSsl.equalsIgnoreCase("yes") || useSsl.equalsIgnoreCase("y"))
            {
               properties.setProperty(PSDesignerConnection.PROPERTY_PROTOCOL, 
                  "https");
            }
            else
            {
               properties.setProperty(PSDesignerConnection.PROPERTY_PROTOCOL, 
                  "http");
            }
         }

         if (properties.get(PSDesignerConnection.PROPERTY_HOST) == null)
            properties.setProperty(PSDesignerConnection.PROPERTY_HOST, 
               "localhost");

         if (properties.get(PSDesignerConnection.PROPERTY_PORT) == null)
            properties.setProperty(PSDesignerConnection.PROPERTY_PORT, 
               "9992");

         if (properties.get("root") == null)
            properties.setProperty("root", "Rhythmyx");
      }
      
      return properties;
   }

   /**
    * Create a new binding for the content SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 10 minutes if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new 
    *    binding.
    */
   protected ContentSOAPStub getContentSOAPStub(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         ContentLocator locator = new ContentLocator();
         locator.setcontentSOAPEndpointAddress(getEndpoint("contentSOAP"));

         ContentSOAPStub binding = (ContentSOAPStub) locator.getcontentSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(600000);
         else
            binding.setTimeout(timeout);

         Call call = binding._getCall();
         if (call == null)
            call = binding._createCall();
         call.setProperty(Call.ATTACHMENT_ENCAPSULATION_FORMAT,
            Call.ATTACHMENT_ENCAPSULATION_FORMAT_MIME);

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
    * Create a new binding for the system SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new binding.
    */
   public SystemSOAPStub getSystemSOAPStub(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         SystemLocator locator = new SystemLocator();
         locator.setsystemSOAPEndpointAddress(getEndpoint("systemSOAP"));

         SystemSOAPStub binding = (SystemSOAPStub) locator.getsystemSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(60000);
         else
            binding.setTimeout(timeout);

         Call call = binding._getCall();
         if (call == null)
            call = binding._createCall();
         call.setProperty(Call.ATTACHMENT_ENCAPSULATION_FORMAT,
            Call.ATTACHMENT_ENCAPSULATION_FORMAT_MIME);

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
    * Create a new binding for the security SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new binding.
    */
   protected SecuritySOAPStub getSecuritySOAPStub(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         SecurityLocator locator = new SecurityLocator();
         locator.setsecuritySOAPEndpointAddress(getEndpoint("securitySOAP"));

         SecuritySOAPStub binding = (SecuritySOAPStub) locator
            .getsecuritySOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(60000);
         else
            binding.setTimeout(timeout);
         
         Call call = binding._getCall();
         if (call == null)
            call = binding._createCall();
         call.setProperty(Call.ATTACHMENT_ENCAPSULATION_FORMAT,
            Call.ATTACHMENT_ENCAPSULATION_FORMAT_MIME);

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
      Properties properties = null;
      try
      {
         properties = getConnectionProps(PSConfigHelperTestCase.CONN_TYPE_RXSERVER);
      }
      catch (IOException e)
      {
         System.out.println("Failed to load the server configuration for "
            + "the following reason: " + e.getLocalizedMessage() + "We will "
            + "continue with the default login credentials of admin1/demo.");
      }

      // setup defaults
      String user = "admin1";
      String password = "demo";
      if (properties != null)
      {
         String property = properties
            .getProperty(IPSUnitTestConfigHelper.PROP_LOGIN_ID);
         if (!StringUtils.isBlank(property))
            user = property;

         property = properties
            .getProperty(IPSUnitTestConfigHelper.PROP_LOGIN_PW);
         if (!StringUtils.isBlank(property))
            password = property;
      }

      m_login = PSTestUtils.login(user, password, null, null);
      m_session = m_login.getSessionId();

   }

   /* (non-Javadoc)
    * @see junit.framework.TestCase#tearDown()
    */
   @AfterClass
   protected static void tearDown() throws Exception
   {
   }

   /**
    * Setup a community runtime permission for the supplied parameters.
    * 
    * @param id the id of the object for which to setup a community runtime
    *    permission, assumed to be valid.
    * @param communityName the name for the community for which to setup the
    *    runtime permission, not <code>null</code> or empty.
    * @param session the session used, not <code>null</code> or empty.
    * @return a list with all created ACL's, never <code>null</code> or empty.
    * @throws Exception for any error.
    */
   protected List<PSAclImpl> setupCommunityAcls(long id, String communityName,
      String session) throws Exception
   {
      if (StringUtils.isBlank(communityName))
         throw new IllegalArgumentException(
            "communityName cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      SystemDesignLocator locator = new SystemDesignLocator();
      locator
         .setsystemDesignSOAPEndpointAddress(getEndpoint("systemDesignSOAP"));

      SystemDesignSOAPStub binding = (SystemDesignSOAPStub) locator
         .getsystemDesignSOAP();
      PSTestUtils.setSessionHeader(binding, session);

      PSAclImpl[] acls = binding.createAcls(new long[] { id });

      PSAclEntryImpl entry = new PSAclEntryImpl();
      entry.setName(communityName);
      entry.setType(PrincipalTypes.COMMUNITY.getOrdinal());
      entry.setId(-1);
      entry.setAclId(acls[0].getId());
      acls[0].setEntries(new PSAclEntryImpl[] { entry });

      PSAccessLevelImpl[] permissions = new PSAccessLevelImpl[4];
      PSAccessLevelImpl permission = new PSAccessLevelImpl();
      permission.setId(-1);
      permission.setAclEntryId(entry.getId());
      permission.setPermission(PSPermissions.READ.getOrdinal());
      permissions[0] = permission;

      permission = new PSAccessLevelImpl();
      permission.setId(-1);
      permission.setAclEntryId(entry.getId());
      permission.setPermission(PSPermissions.UPDATE.getOrdinal());
      permissions[1] = permission;

      permission = new PSAccessLevelImpl();
      permission.setId(-1);
      permission.setAclEntryId(entry.getId());
      permission.setPermission(PSPermissions.DELETE.getOrdinal());
      permissions[2] = permission;

      permission = new PSAccessLevelImpl();
      permission.setId(-1);
      permission.setAclEntryId(entry.getId());
      permission.setPermission(PSPermissions.RUNTIME_VISIBLE.getOrdinal());
      permissions[3] = permission;

      entry.setPermissions(permissions);

      binding.saveAcls(new SaveAclsRequest(acls, true));

      return Arrays.asList(acls);
   }

   /**
    * Catalog all workflows.
    * 
    * @param session the session to use for cataloging, not <code>null</code>
    *    or empty.
    * @return all defined workflows in the system, never <code>null</code>
    *    or empty.
    * @throws Exception for any error.
    */
   protected List<PSWorkflow> catalogWorkflows(String session) throws Exception
   {
      SystemLocator locator = new SystemLocator();
      locator.setsystemSOAPEndpointAddress(getEndpoint("systemSOAP"));

      SystemSOAPStub binding = (SystemSOAPStub) locator.getsystemSOAP();
      PSTestUtils.setSessionHeader(binding, session);

      LoadWorkflowsRequest request = new LoadWorkflowsRequest();
      request.setName("*");
      PSWorkflow[] workflows = binding.loadWorkflows(request);

      return Arrays.asList(workflows);
   }

   /**
    * Load relationships that match the supplied filter.
    * 
    * @param binding Used to make the request. Never <code>null</code>.
    * 
    * @param filter Which relationiships to retrieve. If <code>null</code>, all
    * rels are returned.
    * 
    * @return The rels that pass the supplied filter. May be <code>null</code>
    * or empty if no matches.
    * 
    * @throws Exception
    */
   protected PSRelationship[] loadRelationships(SystemSOAPStub binding,
      PSRelationshipFilter filter) throws Exception
   {
      if (null == binding)
      {
         throw new IllegalArgumentException("binding cannot be null");  
      }
      LoadRelationshipsRequest lrReq = new LoadRelationshipsRequest();
      lrReq.setPSRelationshipFilter(filter);
      return binding.loadRelationships(lrReq);
   }

   /**
    * Delete the specified acls
    * 
    * @param ids the acl ids, may be <code>null</code> to test contracts.
    * @param session the session to use, may be <code>null</code> or empty to
    *    test invalid sessions.
    * @param ignoreDependencies <code>true</code> to ignore dependencies,
    *    <code>false</code> to check them when deleting.
    * @throws Exception if the delete fails.
    */
   protected void deleteAcls(long[] ids, String session,
      boolean ignoreDependencies) throws Exception
   {
      SystemDesignLocator locator = new SystemDesignLocator();
      locator
         .setsystemDesignSOAPEndpointAddress(getEndpoint("systemDesignSOAP"));

      SystemDesignSOAPStub binding = (SystemDesignSOAPStub) locator
         .getsystemDesignSOAP();

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      DeleteAclsRequest req = new DeleteAclsRequest();
      req.setId(ids);
      req.setIgnoreDependencies(ignoreDependencies);

      binding.deleteAcls(req);
   }

   /**
    * Get the default community from the active login information.
    * 
    * @return the active default community, never <code>null</code>.
    * @throws IllegalStateException if the current login information is in
    *    an invalid state.
    */
   protected PSCommunity getDefaultCommunity() throws IllegalStateException
   {
      PSCommunity[] communities = m_login.getCommunities();
      PSCommunity defaultCommunity = null;
      for (PSCommunity community : communities)
      {
         if (community.getName().equals("Default"))
            defaultCommunity = community;

         if (community.getName().equals(m_login.getDefaultCommunity()))
         {
            defaultCommunity = community;
            break;
         }
      }

      if (defaultCommunity == null)
         throw new IllegalStateException(
            "No default community found in the current login.");

      return defaultCommunity;
   }

   /**
    * Get the default locale from the active login information.
    * 
    * @return the active default locale, never <code>null</code>.
    * @throws IllegalStateException if the current login information is in
    *    an invalid state.
    */
   protected PSLocale getDefaultLocale()
   {
      PSLocale[] locales = m_login.getLocales();
      PSLocale defaultLocale = null;
      for (PSLocale locale : locales)
      {
         if (locale.getCode().equals(m_login.getDefaultLocaleCode()))
         {
            defaultLocale = locale;
            break;
         }
      }

      if (defaultLocale == null)
         throw new IllegalStateException(
            "No default locale found in the current login.");

      return defaultLocale;
   }

   /**
    * Creates a GUID id which is an instance of {@link PSLegacyGuid} with
    * the specified content id and <code>-1</code> revision.
    *  
    * @param contentId the content id of the Legacy Guid.
    * 
    * @return the value of the Legacy Guid described above.
    */
   protected long getLegacyGuid(int contentId)
   {
      PSLegacyGuid id = new PSLegacyGuid(contentId, -1);
      return new PSDesignGuid(id).getValue();
   }
   
   /**
    * Verify that the error results fault defines the correct success or error.
    * 
    * @param fault the error results fault to verify, not <code>null</code>.
    * @param errorIndex the index which should contain an error, -1 if only 
    *    errors are expected.
    * @param expectedObject the name or the object expected in the success case,
    *    may be <code>null</code> or empty to skip this test.
    */
   protected void verifyErrorResultsFault(PSErrorResultsFault fault, 
      int errorIndex, String expectedObject)
   {
      if (fault == null)
         throw new IllegalArgumentException("fault cannot be null");
      
      PSErrorResultsFaultServiceCall[] calls = fault.getServiceCall();
      for (int i = 0; i < calls.length; i++)
      {
         PSErrorResultsFaultServiceCall call = calls[i];
         boolean expectError = false;
         if (errorIndex == -1)
            expectError = true;
         else
            expectError = i == errorIndex;

         if (expectError)
         {
            assertTrue(call.getResult() == null);
            assertTrue(call.getError() != null);
         }
         else
         {
            assertTrue(call.getResult() != null);
            assertTrue(call.getError() == null);
            
            verifyErrorResultsFaultResult(call.getResult(), expectedObject);
         }
      }
   }
   
   /**
    * Verify that the supplied contains a result of the expecetd object type.
    * 
    * @param result the result to verify, not <code>null</code>. 
    * @param expectedObject the expected object type, may be <code>null</code>
    *    or empty to skip verification.
    */
   protected void verifyErrorResultsFaultResult(
      PSErrorResultsFaultServiceCallResult result, String expectedObject)
   {
      if (result == null)
         throw new IllegalArgumentException("result cannot be null");
      
      if (!StringUtils.isBlank(expectedObject))
      {
         if (result.getPSAaRelationship() != null)
            assertTrue(result.getPSAaRelationship().getClass().getName().equals(
               expectedObject));
         else if (result.getPSAclImpl() != null)
            assertTrue(result.getPSAclImpl().getClass().getName().equals(
               expectedObject));
         else if (result.getPSAction() != null)
            assertTrue(result.getPSAction().getClass().getName().equals(
               expectedObject));
         else if (result.getPSAssemblyTemplate() != null)
            assertTrue(result.getPSAssemblyTemplate().getClass().getName().equals(
               expectedObject));
         else if (result.getPSAuditTrail() != null)
            assertTrue(result.getPSAuditTrail().getClass().getName().equals(
               expectedObject));
         else if (result.getPSAutoTranslation() != null)
            assertTrue(result.getPSAutoTranslation().getClass().getName().equals(
               expectedObject));
         else if (result.getPSChildEntry() != null)
            assertTrue(result.getPSChildEntry().getClass().getName().equals(
               expectedObject));
         else if (result.getPSCommunity() != null)
            assertTrue(result.getPSCommunity().getClass().getName().equals(
               expectedObject));
         else if (result.getPSContentType() != null)
            assertTrue(result.getPSContentType().getClass().getName().equals(
               expectedObject));
         else if (result.getPSDisplayFormat() != null)
            assertTrue(result.getPSDisplayFormat().getClass().getName().equals(
               expectedObject));
         else if (result.getPSFolder() != null)
            assertTrue(result.getPSFolder().getClass().getName().equals(
               expectedObject));
         else if (result.getPSHierarchyNode() != null)
            assertTrue(result.getPSHierarchyNode().getClass().getName().equals(
               expectedObject));
         else if (result.getPSItem() != null)
            assertTrue(result.getPSItem().getClass().getName().equals(
               expectedObject));
         else if (result.getPSItemFilter() != null)
            assertTrue(result.getPSItemFilter().getClass().getName().equals(
               expectedObject));
         else if (result.getPSItemStatus() != null)
            assertTrue(result.getPSItemStatus().getClass().getName().equals(
               expectedObject));
         else if (result.getPSKeyword() != null)
            assertTrue(result.getPSKeyword().getClass().getName().equals(
               expectedObject));
         else if (result.getPSLocale() != null)
            assertTrue(result.getPSLocale().getClass().getName().equals(
               expectedObject));
         else if (result.getPSRelationshipConfig() != null)
            assertTrue(result.getPSRelationshipConfig().getClass().getName().equals(
               expectedObject));
         else if (result.getPSSearchDef() != null)
            assertTrue(result.getPSSearchDef().getClass().getName().equals(
               expectedObject));
         else if (result.getPSSharedProperty() != null)
            assertTrue(result.getPSSharedProperty().getClass().getName().equals(
               expectedObject));
         else if (result.getPSTemplateSlot() != null)
            assertTrue(result.getPSTemplateSlot().getClass().getName().equals(
               expectedObject));
         else if (result.getPSViewDef() != null)
            assertTrue(result.getPSViewDef().getClass().getName().equals(
               expectedObject));
         else if (result.getState() != null)
            assertTrue(result.getState().getClass().getName().equals(
               expectedObject));
         else
            assertTrue("Tried to verify unknown object type.", false);
      }
   }

   /**
    * The login object contains the session as well as default and user 
    * communities and locales.
    */
   protected static PSLogin m_login = null;

   /**
    * The rhythmyx session used for the entire test.
    */
   protected static String m_session = null;
}
