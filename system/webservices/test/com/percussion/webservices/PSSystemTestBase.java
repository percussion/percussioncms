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

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.util.PSCollection;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.assembly.data.PSTemplateSlot;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.rhythmyxdesign.SystemDesignLocator;
import com.percussion.webservices.system.PSFilterRule;
import com.percussion.webservices.system.PSFilterRuleParam;
import com.percussion.webservices.system.PSItemFilter;
import com.percussion.webservices.system.PSRelationshipConfig;
import com.percussion.webservices.system.PSSharedProperty;
import com.percussion.webservices.system.RelationshipCategory;
import com.percussion.webservices.system.SystemSOAPStub;
import com.percussion.webservices.systemdesign.CreateRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.DeleteItemFiltersRequest;
import com.percussion.webservices.systemdesign.DeleteRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.DeleteSharedPropertiesRequest;
import com.percussion.webservices.systemdesign.FindItemFiltersRequest;
import com.percussion.webservices.systemdesign.FindRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.LoadItemFiltersRequest;
import com.percussion.webservices.systemdesign.LoadRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.LoadSharedPropertiesRequest;
import com.percussion.webservices.systemdesign.SaveItemFiltersRequest;
import com.percussion.webservices.systemdesign.SaveRelationshipTypesRequest;
import com.percussion.webservices.systemdesign.SaveSharedPropertiesRequest;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Implements utilities used by all system test cases.
 */
@Category(IntegrationTest.class)
public class PSSystemTestBase extends PSTestBase
{
   /**
    * Create a new binding for the system SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new binding.
    */
   public SystemSOAPStub getBinding(Integer timeout)
      throws AssertionFailedError
   {
      return getSystemSOAPStub(timeout);
   }

   /**
    * Create a new binding for the system design SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new assembly
    *    binding.
    */
   protected SystemDesignSOAPStub getDesignBinding(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         SystemDesignLocator locator = new SystemDesignLocator();
         locator
            .setsystemDesignSOAPEndpointAddress(getEndpoint("systemDesignSOAP"));

         SystemDesignSOAPStub binding = (SystemDesignSOAPStub) locator
            .getsystemDesignSOAP();
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
    * Calls {@link #loadSharedProperties(String[], String, boolean, String, 
    * String) loadSharedProperties(ids, session, lock, null, null)}
    */
   protected List<PSSharedProperty> loadSharedProperties(String[] names,
      String session, boolean lock) throws Exception
   {
      return loadSharedProperties(names, session, lock, null, null);
   }

   /**
    * Calls the <code>loadSharedProperties</code> design web service call.
    * 
    * @param names the names of the properties to load, may be <code>null</code> 
    *    or empty to load all, asterisk wildcards are accepted.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @param lock <code>true</code> to lock the design objects on load, 
    *    <code>false</code> to load them read-only.
    * @param user the user to use.  If <code>null</code>, the supplied 
    *    session is used, otherwise a new session is created by logging in with 
    *    this user and the supplied <code>pwd</code>.
    * @param pwd the password to use, may be <code>null</code> or empty, 
    *    ignored if <code>user</code> is <code>null</code>.
    * @return the list, never <code>null</code> or empty.
    * @throws Exception if there are any errors.
    */
   protected List<PSSharedProperty> loadSharedProperties(String[] names,
      String session, boolean lock, String user, String pwd) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
         session = PSTestUtils.login(user, pwd);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      LoadSharedPropertiesRequest req = new LoadSharedPropertiesRequest();
      req.setName(names);
      req.setLock(lock);
      PSSharedProperty[] results = binding.loadSharedProperties(req);

      return Arrays.asList(results);
   }

   /**
    * Saves the supplied shared properties.
    * 
    * @param properties the list of shared properties to save, may be 
    *    <code>null</code> or empty to test contract enforcement.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @param release <code>true</code> to release the lock after save, 
    *    <code>false</code> to keep it.
    * @throws Exception if there are any errors.
    */
   protected void saveProperties(List<PSSharedProperty> properties,
      String session, boolean release) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveSharedPropertiesRequest req = new SaveSharedPropertiesRequest();

      PSSharedProperty[] arr = null;
      if (properties != null)
         arr = properties.toArray(new PSSharedProperty[properties.size()]);
      req.setPSSharedProperty(arr);
      req.setRelease(release);
      binding.saveSharedProperties(req);
   }

   /**
    * Calls the <code>deleteSharedProperties</code> webservice.
    * 
    * @param properties the properties to delete, may be 
    *    <code>null</code> or empty to test contracts.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @param ignoreDependencies <code>true</code> to ignore dependencies when
    *    deleting, <code>false</code> to fail to delete if dependencies exist.
    * @throws Exception if there are any errors.
    */
   protected void deleteSharedProperties(List<PSSharedProperty> properties,
      String session, boolean ignoreDependencies) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      DeleteSharedPropertiesRequest req = new DeleteSharedPropertiesRequest();
      req.setPSSharedProperty(properties
         .toArray(new PSSharedProperty[properties.size()]));
      req.setIgnoreDependencies(ignoreDependencies);
      binding.deleteSharedProperties(req);
   }

   @BeforeClass
   public static void setup() throws Exception
   {


      PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
      assemblyTest.deleteTestSlots(m_session);

      try
      {
         createTestSlots();
      }
      catch (Exception e)
      {
         // ignore, tests will fail
      }
   }

   @AfterClass
   public static void tearDown() throws Exception
   {
      PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();
      
      List<Long> idList = new ArrayList<Long>();
      if (m_eigerSlot != null)
      {
         idList.add(m_eigerSlot.getId());
         try
         {
            assemblyTest.deleteSlot(m_eigerSlot, m_session);
         }
         catch (Exception e)
         {
            System.out.println("Could not delete test slot: "
               + m_eigerSlot.getName());
         }
      }
      
      if (m_jungfrauSlot != null)
      {
         idList.add(m_jungfrauSlot.getId());
         try
         {
            assemblyTest.deleteSlot(m_jungfrauSlot, m_session);
         }
         catch (Exception e)
         {
            System.out.println("Could not delete test slot: "
               + m_jungfrauSlot.getName());
         }
      }

      if (!idList.isEmpty()) {
         long[] ids = new long[idList.size()];
         for (int i = 0; i < ids.length; i++)
            ids[i] = idList.get(i);

         try {
            PSTestUtils.releaseLocks(m_session, ids);
         } catch (Exception e) {
            System.out.println("Unable to release locks for test slots: " +
                    idList);
         }
      }
   }

   /**
    * Adds a new server acl entry for editor1 with full data and design but 
    * design update access and saves that to the server configuration.
    * 
    * @return the new acl entry added, never <code>null</code>.
    * @throws Exception for any error modifying the server configuration.
    */
   protected PSAclEntry addServerAcl() throws Exception
   {
      PSObjectStore os = new PSObjectStore(new PSDesignerConnection(
         getServerProperties()));

      int access = PSAclEntry.SACE_ADMINISTER_SERVER |
         PSAclEntry.SACE_ACCESS_DATA |
         PSAclEntry.SACE_CREATE_APPLICATIONS |
         PSAclEntry.SACE_DELETE_APPLICATIONS;
      PSAclEntry entry = new PSAclEntry("editor1", PSAclEntry.ACE_TYPE_USER);
      entry.setAccessLevel(access);
      
      PSServerConfiguration config = os.getServerConfiguration(
         true, true);
      PSAcl acl = config.getAcl();
      PSCollection entries = acl.getEntries();
      entries.add(entry);

      os.saveServerConfiguration(config, true);
      
      return entry;
   }
   
   /**
    * Remove the supplied acl entry from the current server configuration.
    * Nothing happens if the supplied acl entry does not exist.
    * 
    * @param entry the acl entry to remove from the server configuration,
    *    may be <code>null</code> in which case this method does nothing.
    * @throws Exception for any error removing the acl entry from the server
    *    configuration.
    */
   protected void removeServerAcl(PSAclEntry entry) throws Exception
   {
      if (entry != null)
      {
         PSObjectStore os = new PSObjectStore(new PSDesignerConnection(
            getServerProperties()));
         
         PSServerConfiguration config = os.getServerConfiguration(
            true, true);
         PSAcl acl = config.getAcl();
         PSCollection entries = acl.getEntries();
         entries.remove(entry);
   
         os.saveServerConfiguration(config, true);
      }
   }

   /**
    * Creates all slots used for testing.
    * 
    * @throws Exception for any error.
    */
   protected static void createTestSlots() throws Exception
   {
      PSAssemblyTestBase assemblyTest = new PSAssemblyTestBase();

      m_eigerSlot = assemblyTest.createSlot("eiger", true, m_session);
      m_jungfrauSlot = assemblyTest.createSlot("jungfrau", true, m_session);
   }

   /**
    * Makes a web service call to create a new item filter.
    * 
    * @param name the filter name, may be <code>null</code> or empty to test 
    *    contract enforcement.
    * @param ruleCount the sumber of rules to create.
    * @param paramCount the number or rule parameters to create.
    * @param session the current session token, may be <code>null</code> or 
    *    empty to test authentication.
    * @return the new item filter, never <code>null</code>.
    * @throws Exception if there are any errors.
    */
   protected PSItemFilter createItemFilter(String name, int ruleCount,
      int paramCount, String session) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      PSItemFilter[] filters = binding.createItemFilters(new String[] { name });

      // initialize filter
      PSItemFilter filter = filters[0];
      filter.setDescription("description");

      if (ruleCount > 0)
      {
         PSFilterRule[] rules = new PSFilterRule[ruleCount];
         filter.setRules(rules);
         for (int i = 0; i < ruleCount; i++)
         {
            Map<String, String> params = new HashMap<String, String>();
            if (paramCount > 0)
            {
               for (int j = 0; j < paramCount; j++)
                  params.put(name + "_param_" + i + "." + j, "value_" + i + "."
                     + j);
            }

            rules[i] = createFilterRule(PSItemFilterRuleDef.TEST_RULE_NAME,
               params);
         }
      }

      return filter;
   }

   /**
    * Creat an item filter rule for the supplied name and parametes.
    * 
    * @param name the rule name, assumed not <code>null</code> or empty.
    * @param params the rule parameters, assumed not <code>null</code>, 
    *    may be empty.
    * @return the new created rule, never <code>null</code>.
    */
   private PSFilterRule createFilterRule(String name, Map<String, String> params)
   {
      PSFilterRule rule = new PSFilterRule();
      rule.setName(name);

      PSFilterRuleParam[] ruleParams = new PSFilterRuleParam[params.size()];
      rule.setParameters(ruleParams);
      int index = 0;
      for (String paramName : params.keySet())
      {
         PSFilterRuleParam ruleParam = new PSFilterRuleParam(paramName, params
            .get(paramName));

         ruleParams[index++] = ruleParam;
      }

      return rule;
   }

   /**
    * Makes a web service call to create the test item filters.
    * 
    * @return the new item filters, never <code>null</code> or empty.
    * @throws Exception if there are any errors.
    */
   protected List<PSItemFilter> createTestItemFilters() throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, m_session);

      String[] names = new String[] { 
            "testparent" + java.lang.System.currentTimeMillis(), 
            "testchild" + java.lang.System.currentTimeMillis() };

      List<PSItemFilter> testFilters = new ArrayList<PSItemFilter>();

      int ruleCount = 1;
      int paramCount = 3;
      for (String name : names)
         testFilters.add(createItemFilter(name, ruleCount, paramCount,
            m_session));

      return testFilters;
   }

   /**
    * Saves the supplied item filters.
    * 
    * @param filters a list of item filters to save, may be <code>null</code> 
    *    or empty to test contract enforcement.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @param release <code>true</code> to release the lock after save, 
    *    <code>false</code> to keep it.
    * @throws Exception if there are any errors.
    */
   protected void saveItemFilters(List<PSItemFilter> filters, String session,
      boolean release) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveItemFiltersRequest request = new SaveItemFiltersRequest();

      PSItemFilter[] array = null;
      if (filters != null)
         array = filters.toArray(new PSItemFilter[filters.size()]);

      request.setPSItemFilter(array);
      request.setRelease(release);
      binding.saveItemFilters(request);
   }

   /**
    * Calls the <code>deleteItemFlters</code> webservice.
    * 
    * @param ids the ids to delete, may be <code>null</code> or empty for 
    *    testing contract enforcement.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @param ignoreDependencies <code>true</code> to ignore dependencies when
    *    deleting, <code>false</code> to fail to delete if dependencies exist.
    * @throws Exception if there are any errors.
    */
   protected void deleteItemFilters(long[] ids, String session,
      boolean ignoreDependencies) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      DeleteItemFiltersRequest request = new DeleteItemFiltersRequest();
      request.setId(ids);
      request.setIgnoreDependencies(ignoreDependencies);
      binding.deleteItemFilters(request);
   }

   /**
    * Converts the supplied list of item filters to an array of ids.
    * 
    * @param filters the list of item filters, may not be <code>null</code>.
    * @return the array of ids, never <code>null</code>.
    */
   protected long[] itemFiltersListToIdArray(List<PSItemFilter> filters)
   {
      if (filters == null)
         throw new IllegalArgumentException("filters may not be null");

      long[] ids = new long[filters.size()];
      for (int i = 0; i < ids.length; i++)
         ids[i] = filters.get(i).getId();

      return ids;
   }

   /**
    * Calls {@link #loadItemFilters(long[], String, boolean, String, String) 
    * loadItemFilters(ids, session, lock, null, null)}
    */
   protected List<PSItemFilter> loadItemFilters(long[] ids, String session,
      boolean lock) throws Exception
   {
      return loadItemFilters(ids, session, lock, null, null);
   }

   /**
    * Calls the <code>loadItemFilters</code> design web service call.
    * 
    * @param ids the array of ids to load, may be <code>null</code> or empty.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @param lock <code>true</code> to lock the design objects on load, 
    *    <code>false</code> to load them read-only.
    * @param user the user to use. If <code>null</code>, the supplied 
    *    session is used, otherwise a new session is created by logging in with 
    *    this user and the supplied <code>pwd</code>.
    * @param pwd the password to use, may be <code>null</code> or empty, 
    *    ignored if <code>user</code> is <code>null</code>.
    * @return the list, never <code>null</code> or empty.
    * @throws Exception if there are any errors.
    */
   protected List<PSItemFilter> loadItemFilters(long[] ids, String session,
      boolean lock, String user, String pwd) throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
         session = PSTestUtils.login(user, pwd);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      LoadItemFiltersRequest request = new LoadItemFiltersRequest();
      request.setId(ids);
      request.setLock(lock);
      PSItemFilter[] value = binding.loadItemFilters(request);

      return Arrays.asList(value);
   }

   /**
    * Calls the <code>findItemFilters</code> web service
    *  
    * @param name the name to search on, may be <code>null</code> or empty or
    *    contain the "*" wildcard.
    * @param session the current session, may be <code>null</code> or empty to 
    *    test authentication.
    * @return the object summaries returned by the web service call, never 
    *    <code>null</code>, may be empty.
    * @throws Exception if there are any errors.
    */
   protected PSObjectSummary[] findItemFilters(String name, String session)
      throws Exception
   {
      SystemDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      FindItemFiltersRequest request = new FindItemFiltersRequest();
      request.setName(name);
      PSObjectSummary[] value = binding.findItemFilters(request);

      return value;
   }

   /**
    * Removes the specified user defined relationship.
    *  
    * @param binding the object used to communicate with server, never 
    *    <code>null</code>.
    * @param relationshipName the name of the user defined relationship,
    *    never <code>null</code> or empty.
    *    
    * @throws Exception if any error occurs.
    */
   protected void cleanupRelationshipType(SystemDesignSOAPStub binding,
      String relationshipName) throws Exception
   {
      if (binding == null)
         throw new IllegalArgumentException("binding may not be null.");
      if (StringUtils.isBlank(relationshipName))
         throw new IllegalArgumentException(
            "relationshipName may not be null or empty.");

      PSRelationshipConfig config;
      // cleanup
      PSObjectSummary[] summs = findRelationshipTypes(binding,
         relationshipName, null);
      if (summs.length == 1)
      {
         config = loadRelationshipType(binding, summs[0].getId());
         deleteRelationshipType(binding, config.getId());
      }
   }

   /**
    * Convenience method, simply calls 
    * {@link #loadRelationshipType(SystemDesignSOAPStub, long, boolean) 
    * loadRelationshipType(binding, id, true)}
    */
   protected PSRelationshipConfig loadRelationshipType(
      SystemDesignSOAPStub binding, long id) throws Exception
   {
      return loadRelationshipType(binding, id, true);
   }

   /**
    * Loads the specified relationship type.
    * 
    * @param binding the stub used to communicate with server, never 
    *    <code>null</code>.
    * @param id the id of the relationship type.
    * @param lockObject it is <code>true</code> if wanting to lock the
    *    loaded relationship type; otherwise, the loaded relationship type
    *    will not be locked afterwards.
    *    
    * @return the loaded relationship type, never </code>null</code>.
    * 
    * @throws Exception if failed to load the relationship type.
    */
   protected PSRelationshipConfig loadRelationshipType(
      SystemDesignSOAPStub binding, long id, boolean lockObject)
      throws Exception
   {
      if (binding == null)
         throw new IllegalArgumentException("binding may not be null.");

      LoadRelationshipTypesRequest lreq = new LoadRelationshipTypesRequest(
         new long[] { id }, lockObject, true);
      return binding.loadRelationshipTypes(lreq)[0];
   }

   /**
    * Looks up the specified relationship type by the specified name and/or
    * category.
    * 
    * @param binding the stub used to communicate with server, never 
    *    <code>null</code>.
    * @param name the name of the searched relationship type, may be
    *    <code>null</code> or empty if the result is not filtered by name.
    * @param category the category of the searched relationship types,
    *    may be <code>null</code> or empty if the result is not filtered
    *    by the category.
    *    
    * @return the specified relationship types, never <code>null</code>, 
    *    may be empty.
    * 
    * @throws Exception if any error occurs.
    */
   protected PSObjectSummary[] findRelationshipTypes(
      SystemDesignSOAPStub binding, String name, RelationshipCategory category)
      throws Exception
   {
      if (binding == null)
         throw new IllegalArgumentException("binding may not be null.");

      FindRelationshipTypesRequest freq = new FindRelationshipTypesRequest();
      freq.setName(name);
      freq.setCategory(category);
      return binding.findRelationshipTypes(freq);
   }

   /**
    * Deletes the specified relationship type.
    * 
    * @param binding the stub used to communicate with server, never 
    *    <code>null</code>.
    * @param id the id of the to be deleted relationship type.
    * 
    * @throws Exception if any error occurs.
    */
   protected void deleteRelationshipType(SystemDesignSOAPStub binding, long id)
      throws Exception
   {
      DeleteRelationshipTypesRequest dreq = new DeleteRelationshipTypesRequest();
      dreq.setId(new long[] { id });
      //dreq.setIgnoreDependencies(Boolean.TRUE);
      binding.deleteRelationshipTypes(dreq);
   }

   /**
    * Creates and saves the specified relationship type.
    * 
    * @param binding the stub used to communicate with server, never 
    *    <code>null</code>.
    * @param name the name of the relationship type, not <code>null</code> or
    *    empty.
    * @param category the category of the relationship type, not 
    *    <code>null</code>.
    *    
    * @return the persisted relationship type, never <code>null</code>.
    * 
    * @throws Exception if failed to create and save the specified relationship 
    *    type.
    */
   protected PSRelationshipConfig createSaveRelationshipType(
      SystemDesignSOAPStub binding, String name, RelationshipCategory category)
      throws Exception
   {
      if (binding == null)
         throw new IllegalArgumentException("binding may not be null.");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be null or empty.");
      if (category == null)
         throw new IllegalArgumentException("category may not be null.");

      CreateRelationshipTypesRequest creq = new CreateRelationshipTypesRequest();
      creq.setName(new String[] { name });
      creq.setCategory(new RelationshipCategory[] { category });
      PSRelationshipConfig[] configs = binding.createRelationshipTypes(creq);
      assertTrue(configs.length == 1);

      saveRelationshipType(binding, configs[0], true);

      return configs[0];
   }

   /**
    * Saves the specified relationship type.
    * 
    * @param binding the stub used to communicate with server, never 
    *    <code>null</code>.
    * @param config the to be saved relationship type, not <code>null</code>.
    * @param releaseLock it is <code>true</code> if releasing the lock 
    *    afterwards; otherwise the lock will not be released.
    *    
    * @throws Exception if any error occurs.
    */
   protected void saveRelationshipType(SystemDesignSOAPStub binding,
      PSRelationshipConfig config, boolean releaseLock) throws Exception
   {
      SaveRelationshipTypesRequest sreq = new SaveRelationshipTypesRequest();
      sreq.setPSRelationshipConfig(new PSRelationshipConfig[] { config });
      sreq.setRelease(releaseLock);
      binding.saveRelationshipTypes(sreq);
   }

   // test slots
   protected static PSTemplateSlot m_eigerSlot = null;

   protected static PSTemplateSlot m_jungfrauSlot = null;
}
