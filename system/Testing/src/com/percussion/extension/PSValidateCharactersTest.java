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
package com.percussion.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSRuntimeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;

/**
 * Tests the field character validator.
 * 
 * See {@link #setup()}
 * 
 * @author adamgent
 *
 */
public class PSValidateCharactersTest
{
   private PSValidateCharacters vc;
   private MockRequest request;
   private String fieldValue;
   private String fieldName;
   
   @Before
   public void setup()
   {
      vc = new PSValidateCharacters();
      vc.parameterNames = new String[] {"invalidChars", "fieldName"};
      request = new MockRequest();
      fieldName = PSValidateCharacters.DEFAULT_FIELD_NAME;
   }
   
   @Test
   public void testInvalidCharactersWithArgs() throws Exception
   {
      fieldName = "blah";
      fieldValue = "foo";
      String invalidChars = "abo";
      boolean result = (Boolean) vc.processUdf(new Object[] {fieldName, invalidChars} , request);
      
      assertFalse("Should be invalid since foo has an 'o'", result);
   }
   
   @Test
   public void testValidCharactersWithDefaults() throws Exception
   {
      fieldValue = "foo";
      boolean result = (Boolean) vc.processUdf(new Object[] {} , request);
      
      assertTrue("Should be valid since 'foo' has legal characters", result);
   }
   
   @Test
   public void testInvalidCharactersWithDefaults() throws Exception
   {
      fieldValue = "foo#";
      boolean result = (Boolean) vc.processUdf(new Object[] {} , request);
      
      assertFalse("Should be invalid since 'foo#' has '#'", result);
      
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void testValidCharactersWithBadArguments() throws Exception
   {
      fieldValue = "foo";
      vc.processUdf(new Object[] {""} , request);
   }
   
   @Test(expected = IllegalArgumentException.class)
   public void testInValidCharactersWithBadParameterNames() throws Exception
   {
      vc.parameterNames = new String[] {};
      fieldValue = "foo";
      vc.processUdf(new Object[] {"", ""} , request);
   }
   
   
   
   @SuppressWarnings("unchecked")
   private class MockRequest implements IPSRequestContext {

      public String getParameter(String name)
      {
         assertEquals("Bad field name",fieldName, name);
         return fieldValue;
      }
      
      /*
       * The following methods do not matter
       */
      
      /*
       * (non-Javadoc)
       * @see com.percussion.server.IPSRequestContext#appendParameter(java.lang.String, java.lang.Object)
       */
      public void appendParameter(String name, Object value)
      {
      }

      public void continueRequestTimer()
      {
      }

      public Set<PSSubject> expandGroups(Set<PSSubject> subjects)
      {
         
         return null;
      }

      public String getCgiVariable(String name)
      {
         
         return null;
      }

      public String getContentHeaderOverride()
      {
         
         return null;
      }

      public int getCurrentApplicationAccessLevel()
      {
         
         return 0;
      }

      public String getCurrentApplicationName()
      {
         
         return null;
      }

      public String getFileCharacterSet()
      {
         
         return null;
      }

      public Enumeration getHeaders()
      {
         
         return null;
      }

      public String getHookURL()
      {
         
         return null;
      }

      public Document getInputDocument()
      {
         
         return null;
      }

      public IPSInternalRequest getInternalRequest(String resource)
      {
         
         return null;
      }

      public IPSInternalRequest getInternalRequest(String resource,
            Map extraParams, boolean inheritCurrentParams)
      {
         
         return null;
      }

      public String getOriginalHost()
      {
         
         return null;
      }

      public int getOriginalPort()
      {
         
         return 0;
      }

      public String getOriginalProtocol()
      {
         
         return null;
      }

      public PSSubject getOriginalSubject()
      {
         
         return null;
      }

      public String getParameter(String name, Object defValue)
      {
         
         return null;
      }

      public Object[] getParameterList(String name)
      {
         
         return null;
      }

      public Object getParameterObject(String name, Object defValue)
      {
         
         return null;
      }

      public Object getParameterObject(String name)
      {
         
         return null;
      }


      public HashMap getParameters()
      {
         
         return null;
      }

      public Iterator getParametersIterator()
      {
         
         return null;
      }

      public Locale getPreferredLocale()
      {
         
         return null;
      }

      public Object getPrivateObject(Object key) throws PSRuntimeException
      {
         
         return null;
      }

      public String getRequestCookie(String name, String defValue)
      {
         
         return null;
      }

      public String getRequestCookie(String name)
      {
         
         return null;
      }

      public String getRequestFileURL()
      {
         
         return null;
      }

      public String getRequestPage()
      {
         
         return null;
      }

      public String getRequestPage(boolean includeExtension)
      {
         
         return null;
      }

      public String getRequestPageExtension()
      {
         
         return null;
      }

      public int getRequestPageType()
      {
         
         return 0;
      }

      public String getRequestRoot()
      {
         
         return null;
      }

      public HashMap getResponseCookies()
      {
         
         return null;
      }

      public List getRoleAttributes(String roleName)
      {
         
         return null;
      }

      public Set getRoleEmailAddresses(String roleName,
            String emailAttributeName, String community)
      {
         
         return null;
      }

      public Set getRoleEmailAddresses(String roleName,
            String emailAttributeName, String community,
            Set subjectsWithoutEmail)
      {
         
         return null;
      }

      public List getRoleMembers(String roleName, int flags, int memberFlags)
      {
         
         return null;
      }

      public List getRoleSubjects(String roleName)
      {
         
         return null;
      }

      public List getRoleSubjects(String roleName, int memberFlags,
            String subjectNameFilter)
      {
         
         return null;
      }

      public List getRoles()
      {
         
         return null;
      }

      public PSSecurityToken getSecurityToken()
      {
         
         return null;
      }

      public String getServerHostAddress()
      {
         
         return null;
      }

      public int getServerListenerPort()
      {
         
         return 0;
      }

      public Object getSessionObject(String key)
      {
         
         return null;
      }

      public Object getSessionPrivateObject(Object key)
      {
         
         return null;
      }

      public Set getSubjectEmailAddresses(String subjectName,
            String emailAttributeName, String community)
      {
         
         return null;
      }

      public List getSubjectGlobalAttributes(String subjectNameFilter,
            int subjectType, int providerType, String providerInstance,
            String roleName, String attributeNameFilter,
            boolean includeEmptySubjects)
      {
         
         return null;
      }

      public List getSubjectGlobalAttributes(String subjectNameFilter,
            int subjectType, String roleName, String attributeNameFilter,
            boolean includeEmptySubjects, String communityId)
      {
         
         return null;
      }

      public List getSubjectGlobalAttributes(String subjectNameFilter,
            int subjectType, String roleName, String attributeNameFilter,
            boolean includeEmptySubjects)
      {
         
         return null;
      }

      public List getSubjectGlobalAttributes()
      {
         
         return null;
      }

      public List getSubjectGlobalAttributes(PSSubject subject)
      {
         
         return null;
      }

      public List getSubjectRoleAttributes(String subjectNameFilter,
            int subjectType, int providerType, String providerInstance,
            String roleName, String attributeNameFilter)
      {
         
         return null;
      }

      public List getSubjectRoleAttributes(String subjectNameFilter,
            int subjectType, String roleName, String attributeNameFilter)
      {
         
         return null;
      }

      public List getSubjectRoleAttributes(String roleName)
      {
         
         return null;
      }

      public List getSubjectRoleAttributes(PSSubject subject, String roleName)
      {
         
         return null;
      }

      public List getSubjectRoles(PSSubject subject)
      {
         
         return null;
      }

      public List getSubjectRoles()
      {
         
         return null;
      }

      public List getSubjectRoles(String subjectName)
      {
         
         return null;
      }

      public List getSubjects(String subjectNameFilter)
      {
         
         return null;
      }

      public Map getTruncatedParameters()
      {
         
         return null;
      }

      public Object getUserContextInformation(String contextItem,
            Object defValue) throws PSDataExtractionException
      {
         
         return null;
      }

      public String getUserLocale()
      {
         
         return null;
      }

      public String getUserName()
      {
         
         return null;
      }

      public List getUserRoles(String provider, String providerInstance,
            String name)
      {
         
         return null;
      }

      public String getUserSessionId()
      {
         
         return null;
      }

      public boolean hasMultiValuesForAnyParameter()
      {
         
         return false;
      }

      public boolean isTraceEnabled()
      {
         
         return false;
      }

      public void pauseRequestTimer()
      {
         
         
      }

      public void printTraceMessage(String message)
      {
         
         
      }

      public Object removeParameter(String name)
      {
         
         return null;
      }

      public void setCgiVariable(String cgiVarName, String cgiVarValue)
      {
         
         
      }

      public void setContentHeaderOverride(String header)
      {
         
         
      }

      public boolean setEntityHeader(String name, String value)
      {
         
         return false;
      }

      public void setFileCharacterSet(String encoding)
      {
         
         
      }

      public boolean setGeneralHeader(String name, String value)
      {
         
         return false;
      }

      public void setInputDocument(Document inDoc)
      {
         
         
      }

      public void setParameter(String name, Object value)
      {
         
         
      }

      public void setParameterList(String name, Object[] values)
      {
         
         
      }

      public void setParameters(HashMap params)
      {
         
         
      }

      public void setPrivateObject(Object key, Object o)
            throws PSRuntimeException
      {
         
         
      }

      public void setResponseCookie(String name, String value, String path,
            String domain, Date expires, boolean secure)
      {
         
         
      }

      public void setResponseCookies(HashMap cookies)
      { 
      }

      public boolean setResponseHeader(String name, String value)
      {
         
         return false;
      }

      public void setSessionPrivateObject(Object key, Object o)
      {
         
         
      }
   
   }
}
