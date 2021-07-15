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
import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcTableSchemaCollection;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.testing.PSTestResourceUtils;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.content.LoadLocalesRequest;
import com.percussion.webservices.content.PSAutoTranslation;
import com.percussion.webservices.content.PSContentTemplateDesc;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.content.PSContentTypeSummary;
import com.percussion.webservices.content.PSKeyword;
import com.percussion.webservices.content.PSKeywordChoice;
import com.percussion.webservices.content.PSLocale;
import com.percussion.webservices.contentdesign.ContentDesignSOAPStub;
import com.percussion.webservices.contentdesign.CreateLocalesRequest;
import com.percussion.webservices.contentdesign.DeleteContentTypesRequest;
import com.percussion.webservices.contentdesign.DeleteKeywordsRequest;
import com.percussion.webservices.contentdesign.DeleteLocalesRequest;
import com.percussion.webservices.contentdesign.FindContentTypesRequest;
import com.percussion.webservices.contentdesign.FindKeywordsRequest;
import com.percussion.webservices.contentdesign.FindLocalesRequest;
import com.percussion.webservices.contentdesign.LoadAssociatedTemplatesRequest;
import com.percussion.webservices.contentdesign.LoadContentTypesRequest;
import com.percussion.webservices.contentdesign.LoadKeywordsRequest;
import com.percussion.webservices.contentdesign.LoadTranslationSettingsRequest;
import com.percussion.webservices.contentdesign.SaveAssociatedTemplatesRequest;
import com.percussion.webservices.contentdesign.SaveContentTypesRequest;
import com.percussion.webservices.contentdesign.SaveKeywordsRequest;
import com.percussion.webservices.contentdesign.SaveLocalesRequest;
import com.percussion.webservices.contentdesign.SaveTranslationSettingsRequest;
import com.percussion.webservices.rhythmyxdesign.ContentDesignLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.rpc.ServiceException;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Provides base class functionality for content and content design web service
 * unit tests.
 */
public abstract class PSContentTestBase extends PSTestBase
{
   public PSContentTestBase(String name)
   {
      super(name);
   }

   public PSContentTestBase()
   {}

   /**
    * Makes a web service call to create a new content type.
    * 
    * @param name The name of the content type, may be <code>null</code> or 
    * empty to test contract enforcement.
    * @param session The current session token, may be <code>null</code> or 
    * empty to test authentication.
    * 
    * @return The new content type, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   protected PSContentType createContentType(String name, String session)
      throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      String[] request = new String[1];
      request[0] = name;
      PSContentType[] contentTypes = binding.createContentTypes(request);

      return contentTypes[0];
   }

   /**
    * Creates new content types to be used for testing purposes. Also 
    * creates the matching tables if they do not exists, and populates the 
    * {@link #m_testCEMap} for access to the content editor defintion.
    *  
    * @param session the session token, not <code>null</code> or empty.
    * @return the list of content types, never <code>null</code> or empty.
    * @throws Exception if there are any errors.
    */
   protected List<PSContentType> createTestContentTypes(String session)
      throws Exception
   {
      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      cleanupTestContentTypes(session);
      
      m_testCEMap.clear();

      List<PSContentType> results = new ArrayList<PSContentType>(2);
      results.add(createTestContentType("test1", session));
      results.add(createTestContentType("test2", session));

      return results;
   }

   /**
    * A cleanup method is used by {@link #createTestContentTypes(String)}
    * to make sure the success of the content type creation.
    * 
    * @param session the session id; assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void cleanupTestContentTypes(String session) throws Exception
   {
      PSObjectSummary[] cts = findContentTypes("test*", session);
      if (cts.length == 0)
         return;  // do nothing if already clean
      
      long[] ids = new long[cts.length];
      for (int i=0; i < cts.length; i++)
      {
         ids[i] = cts[i].getId();
      }
      deleteContentTypes(ids, session);
   }
   /**
    * Creates one new content type for the supplied name and populates the 
    * {@link #m_testCEMap} for access to the content editor definition.
    * 
    * @param name the name of the content type to create, not <code>null</code>
    *    or empty. A content editor definition must exist under 
    *    <code>UnitTestResources</code> for the supplied name, see 
    *    {@link #loadContentEditor(String)} for the expected name format.
    * @param session the current user session, not <code>null</code> or empty.
    * @return the newly created content type, initialized and readdy for use,
    *    never <code>null</code>.
    * @throws Exception for any error.
    */
   protected PSContentType createTestContentType(String name, String session)
      throws Exception
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      PSContentType ct = createContentType(name, session);
      assertTrue(ct.getName().equals(name));
      assertTrue(ct.getContentEditor() != null);

      // update from source to add fields, mappings, etc.
      PSContentEditor ce = loadContentEditor(ct.getName());
      StringReader reader = new StringReader(ct.getContentEditor());
      Document doc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
      PSContentEditor src = new PSContentEditor(doc.getDocumentElement(), null,
         null);
      ce.setContentType(src.getContentType());
      ce.setWorkflowId(src.getWorkflowId());

      // reset the content editor
      ct.setContentEditor(PSXmlDocumentBuilder.toString(ce
         .toXml(PSXmlDocumentBuilder.createXmlDocument())));

      m_testCEMap.put(ct.getName(), ce);

      // create tables if necessary
      createCETable();

      return ct;
   }

   /**
    * Creates the required table for the test content editors (see 
    * {@link #createTestContentTypes(String)}.
    * 
    * @throws Exception if there are any errors.
    */
   private void createCETable() throws Exception
   {
      // create designer conn and os
      Properties info = getConnectionProps(CONN_TYPE_RXSERVER);
      PSDesignerConnection conn = new PSDesignerConnection(info);
      PSObjectStore os = new PSObjectStore(conn);

      // get datasource conn
      PSConnectionDetail detail = os.getConnectionDetail(null);

      // get table def
      File tableDef = PSTestResourceUtils.getFile(PSContentTestBase.class, RESOURCE_BASE +"ceTestTableDef.xml",null);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
         new FileInputStream(tableDef), false);
      PSJdbcTableSchemaCollection coll = new PSJdbcTableSchemaCollection(doc,
         new PSJdbcDataTypeMap(null, detail.getDriver(), null));

      os.saveTableDefinitions(new PSTableLocator(
         new PSBackEndCredential("test")), coll);
   }

   /**
    * Loads the content editor of the supplied name from its corresponding 
    * definition on disk under UnitTestResources.
    *  
    * @param name The name of the content editor, assumed not <code>null</code> 
    *    or empty.
    * 
    * @return The content editor, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   private PSContentEditor loadContentEditor(String name) throws Exception
   {
      File ceFile = PSTestResourceUtils.getFile(PSContentTestBase.class,RESOURCE_BASE +"rxs_ce" + name + ".xml",null);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
         new FileInputStream(ceFile), false);

      return new PSContentEditor(doc.getDocumentElement(), null, null);
   }

   /**
    * Calls the <code>findContentTypes</code> web service
    *  
    * @param name The name to search on, may be <code>null</code> or empty or
    * contain the "*" wildcard.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * 
    * @return The object summaries returned by the web service call, never 
    * <code>null</code>, may be empty.
    * 
    * @throws Exception if there are any errors.
    */
   public PSObjectSummary[] findContentTypes(String name, String session) 
      throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      PSObjectSummary[] value = null;
      FindContentTypesRequest req = new FindContentTypesRequest();
      req.setName(name);
      value = binding.findContentTypes(req);

      return value;
   }

   /**
    * Calls {@link #loadContentTypes(long[], String, boolean, String, String) 
    * loadContentTypes(ids, session, lock, null, null)}
    */
   public List<PSContentType> loadContentTypes(long[] ids, String session, 
      boolean lock) throws Exception
   {
      return loadContentTypes(ids, session, lock, null, null);
   }

   /**
    * Calls 
    * {@link #loadContentTypes(long[], String, boolean, boolean, String, String) 
    * loadContentTypes(ids, session, lock, lock, user, pwd)} - note that if
    * locking, will always override existing locks.
    */
   protected List<PSContentType> loadContentTypes(long[] ids, String session,
      boolean lock, String user, String pwd) throws Exception
   {
      return loadContentTypes(ids, session, lock, lock, user, pwd);
   }

   /**
    * Calls the <code>loadContentTypes</code> design web service call.
    * 
    * @param ids The array of ids to load, may be <code>null</code> or empty.
    * @param session The current session, may be <code>null</code> or empty to
    * test authentication.
    * @param lock <code>true</code> to lock the design objects on load,
    * <code>false</code> to load them read-only.
    * @param overrideLock <code>true</code> to override existing locks if
    * locking, <code>false</code> otherwise.
    * @param user The user to use. If <code>null</code>, the supplied session
    * is used, otherwise a new session is created by logging in with this user
    * and the supplied <code>pwd</code>.
    * @param pwd The password to use, may be <code>null</code> or empty,
    * ignored if <code>user</code> is <code>null</code>.
    * 
    * @return The list, never <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */   
   protected List<PSContentType> loadContentTypes(long[] ids, String session,
      boolean lock, boolean overrideLock, String user, String pwd) 
      throws Exception   
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
      {
         session = PSTestUtils.login(user, pwd);
      }

      if (session != null)
      {
         PSTestUtils.setSessionHeader(binding, session);
      }

      PSContentType[] value = null;
      LoadContentTypesRequest req = new LoadContentTypesRequest();
      req.setId(ids);
      req.setLock(lock);
      req.setOverrideLock(overrideLock);
      value = binding.loadContentTypes(req);

      return Arrays.asList(value);   
   }
   
   /**
    * Calls the <code>deleteContentTypes</code> webservice.
    * 
    * @param ids The ids to delete, may be <code>null</code> or empty for 
    * testing contract enforcement.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * 
    * @throws Exception If there are any errors.
    */
   protected void deleteContentTypes(long[] ids, String session)
      throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);
      DeleteContentTypesRequest req = new DeleteContentTypesRequest();
      req.setId(ids);
      req.setIgnoreDependencies(true);
      binding.deleteContentTypes(req);
   }

   /**
    * Calls the <code>loadContentTypes</code> webservice.
    * 
    * @param name The name to search on, may be <code>null</code> or empty or
    * contain the "*" wildcard.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * 
    * @return The list of content type summaries, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   protected PSContentTypeSummary[] loadContentTypeSummaries(String name,
      String session) throws Exception
   {
      PSContentTypeSummary[] value = null;
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      com.percussion.webservices.content.LoadContentTypesRequest req = new com.percussion.webservices.content.LoadContentTypesRequest();
      req.setName(name);
      value = binding.loadContentTypes(req);

      return value;
   }

   /**
    * Calls the <code>saveContentTypes</code> web service.
    * 
    * @param ctypes The list of content types to save, may be <code>null</code> 
    * or empty to test contract enforcement.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param release <code>true</code> to release the lock after save, 
    * <code>false</code> to keep it.
    * 
    * @throws Exception if there are any errors.
    */
   protected void saveContentTypes(List<PSContentType> ctypes, String session,
      boolean release) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveContentTypesRequest req = new SaveContentTypesRequest();
      PSContentType[] arr = null;
      if (ctypes != null)
         arr = ctypes.toArray(new PSContentType[ctypes.size()]);
      req.setPSContentType(arr);
      req.setRelease(release);
      binding.saveContentTypes(req);
   }

   /**
    * Makes a web service call to create a new locale. 
    * 
    * @param lang The language string of the locale, may be <code>null</code> or 
    * empty to test contract enforcement.
    * @param label The label of the locale, may be <code>null</code> or 
    * empty to test contract enforcement
    * @param session The current session token, may be <code>null</code> or 
    * empty to test authentication.
    * 
    * @return The new locale, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   protected PSLocale createLocale(String lang, String label, String session)
      throws Exception
   {
      ContentDesignSOAPStub desBinding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(desBinding, session);

      String[] langs = new String[1];
      langs[0] = lang;
      String[] labels = new String[1];
      labels[0] = label;

      CreateLocalesRequest req = new CreateLocalesRequest(langs, labels);
      PSLocale[] locales = desBinding.createLocales(req);

      return locales[0];
   }

   /**
    * Removes the locales created by {@link #createTestLocales()}
    * 
    * @throws Exception If there are any errors.
    */
   protected void cleanupTestLocales() throws Exception
   {
      PSLocale[] locales = loadLocales("", "Test*", m_session);
      if (locales.length == 0)
         return;
      
      List<PSLocale> localesToFind = Arrays.asList(locales);
      long[] ids = localesListToIdArray(localesToFind);
      loadLocales(ids, m_session, true, true, null, null);
      deleteLocales(ids, m_session, false);
   }
   
   /**
    * Makes a web service call to create the test locales.
    * 
    * @return The new locales, never <code>null</code> or empty
    * 
    * @throws Exception If there are any errors.
    */
   protected List<PSLocale> createTestLocales() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      PSTestUtils.setSessionHeader(binding, m_session);

      //pick obscure langs to reduce possibility of conflict
      String[] langs = new String[2];
      langs[0] = "sq-al";
      langs[1] = "be-by";
      String[] labels = new String[2];
      labels[0] = "Test Albanian Albania";
      labels[1] = "Test Belarusian Belarus";

      CreateLocalesRequest req = new CreateLocalesRequest(langs, labels);
      PSLocale[] locales = binding.createLocales(req);
      for (int i = 0; i < locales.length; i++)
      {
         assertEquals(locales[i].getCode(), langs[i]);
         assertEquals(locales[i].getLabel(), labels[i]);
      }

      return Arrays.asList(locales);
   }

   /**
    * Calls the <code>loadLocales</code> webservice.
    * 
    * @param code The code to search on, may be <code>null</code> or empty.
    * @param name The name to search on, may be <code>null</code> or empty or
    * contain the "*" wildcard.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * 
    * @return The list of content type summaries, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   protected PSLocale[] loadLocales(String code, String name, String session)
      throws Exception
   {
      PSLocale[] value = null;
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      LoadLocalesRequest req = new LoadLocalesRequest();
      req.setCode(code);
      req.setName(name);
      value = binding.loadLocales(req);

      return value;
   }

   /**
    * Calls {@link #loadLocales(long[], String, boolean, String, String) 
    * loadLocales(ids, session, lock, null, null)}
    */
   protected List<PSLocale> loadLocales(long[] ids, String session, boolean lock)
      throws Exception
   {
      return loadLocales(ids, session, lock, null, null);
   }

   /**
    * Calls the <code>loadLocales</code> design web service call.
    * 
    * @param ids The array of ids to load, may be <code>null</code> or empty.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param lock <code>true</code> to lock the design objects on load, 
    * <code>false</code> to load them read-only.
    * @param isOverrideLock <code>true</code> to override lock, 
    * <code>false</code> not to override lock.
    * @param user The user to use.  If <code>null</code>, the supplied 
    * session is used, otherwise a new session is created by logging in with 
    * this user and the supplied <code>pwd</code>.
    * @param pwd The password to use, may be <code>null</code> or empty, ignored
    * if <code>user</code> is <code>null</code>.
    * 
    * @return The list, never <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */
   protected List<PSLocale> loadLocales(long[] ids, String session,
      boolean lock, boolean isOverrideLock, String user, String pwd) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
      {
         session = PSTestUtils.login(user, pwd);
      }

      if (session != null)
      {
         PSTestUtils.setSessionHeader(binding, session);
      }

      PSLocale[] value = null;
      com.percussion.webservices.contentdesign.LoadLocalesRequest req = new com.percussion.webservices.contentdesign.LoadLocalesRequest();
      req.setId(ids);
      req.setLock(lock);
      req.setOverrideLock(isOverrideLock);
      value = binding.loadLocales(req);

      return Arrays.asList(value);
   }

   /**
    * Convenience method, simply call 
    * {@link #loadLocales(long[], String, boolean, boolean, String, String)
    * loadLocales(long[], String, boolean, false, String, String)}. 
    */
   protected List<PSLocale> loadLocales(long[] ids, String session,
     boolean lock, String user, String pwd) throws Exception
   {
      return loadLocales(ids, session, lock, false, user, pwd);
   }
   
   /**
    * Calls the <code>findLocales</code> web service
    *  
    * @param code The code to search on, may be <code>null</code> or empty.
    * @param name The name to search on, may be <code>null</code> or empty or
    * contain the "*" wildcard.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * 
    * @return The object summaries returned by the web service call, never 
    * <code>null</code>, may be empty.
    * 
    * @throws Exception if there are any errors.
    */
   protected PSObjectSummary[] findLocales(String code, String name,
      String session) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      PSObjectSummary[] value = null;
      FindLocalesRequest req = new FindLocalesRequest();
      req.setCode(code);
      req.setName(name);
      value = binding.findLocales(req);

      return value;
   }

   /**
    * Saves the supplied locales
    * @param locales The list of content types to save, may be <code>null</code> 
    * or empty to test contract enforcement.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param release <code>true</code> to release the lock after save, 
    * <code>false</code> to keep it.
    * 
    * @throws Exception if there are any errors.
    */
   protected void saveLocales(List<PSLocale> locales, String session,
      boolean release) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveLocalesRequest req = new SaveLocalesRequest();

      PSLocale[] arr = null;
      if (locales != null)
         arr = locales.toArray(new PSLocale[locales.size()]);
      req.setPSLocale(arr);
      req.setRelease(release);
      binding.saveLocales(req);
   }

   /**
    * Calls the <code>deleteLocales</code> webservice.
    * 
    * @param ids The ids to delete, may be <code>null</code> or empty for 
    * testing contract enforcement.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param ignoreDependencies <code>true</code> to ignore dependencies when
    * deleting, <code>false</code> to fail to delete if dependencies exist.
    * 
    * @throws Exception If there are any errors.
    */
   protected void deleteLocales(long[] ids, String session,
      boolean ignoreDependencies) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);
      DeleteLocalesRequest req = new DeleteLocalesRequest();
      req.setId(ids);
      req.setIgnoreDependencies(ignoreDependencies);
      binding.deleteLocales(req);
   }

   /**
    * Saves the specified templates associations
    * 
    * @param contentTypeId The content type id, must be locked for this to
    * succeed.
    * @param templateIds The list of templates to associate, may be 
    * <code>null</code> or empty.
    * @param session The session to use, may be <code>null</code> or empty.
    * @param release <code>true</code> to release the lock, <code>false</code>
    * to keep it.
    * 
    * @throws Exception If there are any errors. 
    */
   public void saveTemplateAssociations(long contentTypeId, 
      long[] templateIds, String session, boolean release) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveAssociatedTemplatesRequest req = new SaveAssociatedTemplatesRequest();
      req.setContentTypeId(contentTypeId);
      req.setTemplateId(templateIds);
      req.setRelease(release);

      binding.saveAssociatedTemplates(req);
   }

   /**
    * Convenience method that calls 
    * {@link #loadTemplateAssociations(long, String, boolean, String, String)
    * loadTemplateAssociations(contentTypeId, session, lock, null, null)}
    */
   public List<PSContentTemplateDesc> loadTemplateAssociations(
      long contentTypeId, String session, boolean lock) throws Exception
   {
      return loadTemplateAssociations(contentTypeId, session, lock, null, null);
   }

   /**
    * Load the template associations for a content type.
    * @param contentTypeId The content type to get associates for.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param lock <code>true</code> to lock the design objects on load, 
    * <code>false</code> to load them read-only.
    * @param user The user to use.  If <code>null</code>, the supplied 
    * session is used, otherwise a new session is created by logging in with 
    * this user and the supplied <code>pwd</code>.
    * @param pwd The password to use, may be <code>null</code> or empty, ignored
    * if <code>user</code> is <code>null</code>.
    * 
    * @return The list of associations, never <code>null</code>, may be empty.
    * 
    * @throws Exception if there are any errors.
    */
   protected List<PSContentTemplateDesc> loadTemplateAssociations(
      long contentTypeId, String session, boolean lock, String user, String pwd)
      throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
      {
         session = PSTestUtils.login(user, pwd);
      }

      if (session != null)
      {
         PSTestUtils.setSessionHeader(binding, session);
      }

      PSContentTemplateDesc[] value = null;
      LoadAssociatedTemplatesRequest req = new LoadAssociatedTemplatesRequest();
      req.setContentTypeId(contentTypeId);
      req.setLock(lock);
      value = binding.loadAssociatedTemplates(req);

      return Arrays.asList(value);
   }

   /**
    * Create a new binding for the content Design SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new 
    *    binding.
    */
   public static ContentDesignSOAPStub getDesignBinding(Integer timeout)
      throws AssertionFailedError
   {
      if (timeout != null && timeout < 1000)
         throw new IllegalArgumentException("timeout must be >= 1000");

      try
      {
         ContentDesignLocator locator = new ContentDesignLocator();
         locator
            .setcontentDesignSOAPEndpointAddress(getEndpoint("contentDesignSOAP"));

         ContentDesignSOAPStub binding = (ContentDesignSOAPStub) locator
            .getcontentDesignSOAP();
         assertNotNull("binding is null", binding);

         if (timeout == null)
            binding.setTimeout(600000);
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
    * Create a new binding for the content SOAP port.
    * 
    * @param timeout the timeout in milliseconds, defaults to 1 minute if not 
    *    supplied, must be > 1000.
    * @return the new binding, never <code>null</code>.
    * @throws AssertionFailedError for any error creating the new 
    *    binding.
    */
   protected ContentSOAPStub getBinding(Integer timeout)
      throws AssertionFailedError
   {
      return getContentSOAPStub(timeout);
   }

   /**
    * Converts the supplied list of content types to an array of ids.
    * 
    * @param ctypes The list of content types, may not be <code>null</code>.
    * 
    * @return The array of ids, never <code>null</code>.
    */
   protected long[] ctypesListToIdArray(List<PSContentType> ctypes)
   {
      if (ctypes == null)
         throw new IllegalArgumentException("ctypes may not be null");

      long[] ids = new long[ctypes.size()];
      for (int i = 0; i < ids.length; i++)
      {
         ids[i] = ctypes.get(i).getId();
      }
      return ids;
   }

   /**
    * Converts the supplied list of locales to an array of ids.
    * 
    * @param locales The list of locales, may not be <code>null</code>.
    * 
    * @return The array of ids, never <code>null</code>.
    */
   protected long[] localesListToIdArray(List<PSLocale> locales)
   {
      if (locales == null)
         throw new IllegalArgumentException("locales may not be null");

      long[] ids = new long[locales.size()];
      for (int i = 0; i < ids.length; i++)
      {
         ids[i] = locales.get(i).getId();
      }
      return ids;
   }

   /**
    * Create all keywords used for testing.
    * 
    * @throws Exception for any error.
    */
   protected static void createTestKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      PSKeyword[] keywords = binding.createKeywords(ms_testKeywordNames);
      int choiceCount = 5;
      for (int i = 0; i < keywords.length; i++)
      {
         keywords[i].setDescription("description_" + i);

         PSKeywordChoice[] choices = new PSKeywordChoice[choiceCount];
         for (int j = 0; j < choiceCount; j++)
         {
            PSKeywordChoice choice = new PSKeywordChoice();
            choice.setLabel("choice_" + i + "." + j);
            choice.setDescription("description_" + i + "." + j);
            choice.setValue("value_" + i + "." + j);
            choice.setSequence(j);

            choices[j] = choice;
         }

         keywords[i].setChoices(choices);
      }

      SaveKeywordsRequest saveRequest = new SaveKeywordsRequest();
      saveRequest.setPSKeyword(keywords);
      binding.saveKeywords(saveRequest);
   }

   /**
    * Delete all test keywords.
    *
    * @throws Exception for any error.
    */
   protected static void deleteTestKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      List<PSObjectSummary> objects = new ArrayList<PSObjectSummary>();

      FindKeywordsRequest findRequest = new FindKeywordsRequest();
      for (String name : ms_testKeywordNames)
      {
         findRequest.setName(name);
         PSObjectSummary[] summaries = binding.findKeywords(findRequest);
         if (summaries.length > 0)
            objects.add(summaries[0]);
      }

      if (objects.size() > 0)
      {
         long[] ids = new long[objects.size()];
         for (int i = 0; i < objects.size(); i++)
            ids[i] = objects.get(i).getId();

         lockKeywords(ids, m_session);

         DeleteKeywordsRequest deleteRequest = new DeleteKeywordsRequest();
         deleteRequest.setId(ids);
         binding.deleteKeywords(deleteRequest);
      }
   }

   /**
    * Lock all keywords for the supplied ids.
    * 
    * @param ids the ids of the keywords to lock, not <code>null</code> or
    *    empty.
    * @param session the session for which to lock the objects, not
    *    <code>null</code> or empty.
    * @return the locked keywords, never <code>null</code> or empty.
    * @throws Exception for any error locking the objects.
    */
   protected static PSKeyword[] lockKeywords(long[] ids, String session)
      throws Exception
   {
      if (ids == null || ids.length == 0)
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      ContentDesignSOAPStub binding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(binding, session);

      LoadKeywordsRequest loadRequest = new LoadKeywordsRequest();
      loadRequest.setId(ids);
      loadRequest.setLock(true);
      PSKeyword[] keywords = binding.loadKeywords(loadRequest);

      return keywords;
   }

   /**
    * Get the ids of all test keywords.
    * 
    * @return the ids of all test keywords, never <code>null</code>, may be 
    *    empty.
    * @throws Exception for any error.
    */
   protected long[] getTestKeywordIds() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      FindKeywordsRequest findRequest = new FindKeywordsRequest();
      findRequest.setName("keyword_*");
      PSObjectSummary[] summaries = binding.findKeywords(findRequest);

      long[] ids = new long[summaries.length];
      for (int i = 0; i < summaries.length; i++)
         ids[i] = summaries[i].getId();

      return ids;
   }

   /**
    * Get all test keywords in read-only mode.
    * 
    * @return all test keywords as read-only, never <code>null</code>, may be 
    *    empty.
    * @throws Exception for any error.
    */
   protected PSKeyword[] getTestKeywords() throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      long[] ids = getTestKeywordIds();
      if (ids.length == 0)
         return new PSKeyword[0];

      LoadKeywordsRequest request = new LoadKeywordsRequest();
      request.setId(ids);
      return binding.loadKeywords(request);
   }

   /**
    * An array of names for which we will create test keywords.
    */
   protected static final String[] ms_testKeywordNames = { "keyword_0",
      "keyword_1", "keyword_2" };

   /**
    * Create a list of test auto translations
    * 
    * @return The list, never <code>null</code> or empty.
    */
   protected List<PSAutoTranslation> createTestTranslations()
   {
      PSAutoTranslation[] result = new PSAutoTranslation[4];
      for (int i = 0; i < result.length; i++)
      {
         result[i] = createAutoTranslation(i);
      }

      return Arrays.asList(result);
   }

   /**
    * Create a test auto translation using the supplied ordinal to create test
    * values.
    * 
    * @param ordinal The ordinal value.
    * 
    * @return The result, never <code>null</code>.
    */
   private PSAutoTranslation createAutoTranslation(int ordinal)
   {
      PSAutoTranslation src = new PSAutoTranslation();
      
      PSGuid communityId = new PSGuid(PSTypeEnum.COMMUNITY_DEF, 100 + ordinal);
      src.setCommunityId(new PSDesignGuid(communityId).getValue());
      
      PSGuid contentTypeId = new PSGuid(PSTypeEnum.NODEDEF, 200 + ordinal);
      src.setContentTypeId(new PSDesignGuid(contentTypeId).getValue());
      
      src.setLocale("test" + ordinal);
      
      PSGuid wfId = new PSGuid(PSTypeEnum.WORKFLOW, 300 + ordinal);
      src.setWorkflowId(new PSDesignGuid(wfId).getValue());

      return src;
   }

   /**
    * Calls the <code>loadAutoTranslations</code> webservice.
    * 
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * 
    * @return The list of auto translations, never <code>null</code>.
    * 
    * @throws Exception If there are any errors.
    */
   protected List<PSAutoTranslation> loadAutoTranslations(String session)
      throws Exception
   {
      PSAutoTranslation[] value = null;
      ContentSOAPStub binding = getBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      value = binding.loadTranslationSettings();

      return Arrays.asList(value);
   }

   /**
    * Calls the <code>loadAutoTranslations</code> design web service call.
    * 
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param lock <code>true</code> to lock the design objects on load, 
    * <code>false</code> to load them read-only.
    * @param user The user to use.  If <code>null</code>, the supplied 
    * session is used, otherwise a new session is created by logging in with 
    * this user and the supplied <code>pwd</code>.
    * @param pwd The password to use, may be <code>null</code> or empty, ignored
    * if <code>user</code> is <code>null</code>.
    * 
    * @return The list, never <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */
   protected List<PSAutoTranslation> loadAutoTranslations(String session,
      boolean lock, String user, String pwd) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (user != null)
      {
         session = PSTestUtils.login(user, pwd);
      }

      if (session != null)
      {
         PSTestUtils.setSessionHeader(binding, session);
      }

      PSAutoTranslation[] value = null;
      LoadTranslationSettingsRequest req = new LoadTranslationSettingsRequest();
      req.setLock(lock);
      value = binding.loadTranslationSettings(req);

      return Arrays.asList(value);
   }

   /**
    * Saves the supplied translations
    * @param translations The list of translations to save, may be 
    * <code>null</code> or empty to test contract enforcement.
    * @param session The current session, may be <code>null</code> or empty to 
    * test authentication.
    * @param release <code>true</code> to release the lock after save, 
    * <code>false</code> to keep it.
    * 
    * @throws Exception if there are any errors.
    */
   protected void saveAutoTranslations(List<PSAutoTranslation> translations,
      String session, boolean release) throws Exception
   {
      ContentDesignSOAPStub binding = getDesignBinding(null);

      if (session != null)
         PSTestUtils.setSessionHeader(binding, session);

      SaveTranslationSettingsRequest req = new SaveTranslationSettingsRequest();

      PSAutoTranslation[] arr = null;
      if (translations != null)
         arr = translations.toArray(new PSAutoTranslation[translations.size()]);
      req.setPSAutoTranslation(arr);
      req.setRelease(release);
      binding.saveTranslationSettings(req);
   }

   /**
    * Base resource file location.
    */
   protected static final String RESOURCE_BASE = "/com/percussion/webservices/";

   /**
    * Map of created content editors, never <code>null</code>, cleared and 
    * populated with each call to {@link #createTestContentTypes(String)}.
    */
   protected Map<String, PSContentEditor> m_testCEMap = new HashMap<String, PSContentEditor>();
}
