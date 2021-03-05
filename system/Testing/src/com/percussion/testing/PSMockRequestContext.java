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
package com.percussion.testing;

import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSRuntimeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import org.w3c.dom.Document;

import java.util.*;

/**
 * This is a mock object to allow the running of a subset of code that requires
 * a {@link com.percussion.server.IPSRequestContext}. Note that right now only
 * some of the methods provide useful information. More may be added, along with
 * set methods over time.
 * 
 * @author dougrand
 */
public class PSMockRequestContext implements IPSRequestContext
{
   private String originalProtocol = "http";
   private int originalPort = 8123;
   
   public void setOriginalProtocol(String originalProtocol)
   {
      this.originalProtocol = originalProtocol;
   }
   
   public String getOriginalProtocol()
   {
      return originalProtocol;
   }
   public void setOriginalPort (int port)
   {
      this.originalPort = port;
   }
   
   public int getOriginalPort()
   {
      return originalPort;
   }
   
   public String getServerHostAddress()
   {
      return "10.10.10.1";
   }

   public int getServerListenerPort()
   {
      return 7123;
   }

   public String getUserSessionId()
   {
      return "-user-session-id-";
   }

   public String getOriginalHost()
   {
      return "originalhost";
   }

   public String getRequestFileURL()
   {
      return "";
   }

   public String getRequestRoot()
   {
      return "";
   }

   public String getRequestPage()
   {
      return null;
   }

   public String getRequestPage(boolean includeExtension)
   {
      return null;
   }

   public int getRequestPageType()
   {
      return 0;
   }

   public String getRequestPageExtension()
   {
      return null;
   }

   public String getHookURL()
   {
      return null;
   }

   public String getParameter(String name, Object defValue)
   {
      return null;
   }

   public String getParameter(String name)
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

   public Map<String,Object> getParameters()
   {
      return null;
   }

   public Object[] getParameterList(String name)
   {
      return null;
   }

   public Object removeParameter(String name)
   {
      return null;
   }

   public void appendParameter(String name, Object value)
   {
   }

   public boolean hasMultiValuesForAnyParameter()
   {
      return false;
   }

   public Iterator getParametersIterator()
   {
      return null;
   }

   public String getFileCharacterSet()
   {
      return null;
   }

   public void setFileCharacterSet(String encoding)
   {            
   }

   public void setParameters(Map<String,Object> params)
   {  
   }

   public void setParameter(String name, Object value)
   {
   }

   public void setParameterList(String name, Object[] values)
   {  
   }

   public Map getTruncatedParameters()
   {
      return null;
   }

   public String getCgiVariable(String name)
   {
      return null;
   }

   public HashMap getCgiVariables()
   {
      return null;
   }

   public void setCgiVariables(@SuppressWarnings("unused") HashMap cgiVars)
   {            
   }

   public String getRequestCookie(String name, String defValue)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getRequestCookie(String name)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public HashMap getRequestCookies()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setRequestCookies(@SuppressWarnings("unused") HashMap cookies)
   {
      // TODO Auto-generated method stub
      
   }

   public void setResponseCookie(String name, String value, String path, String domain, Date expires, boolean secure)
   {
      // TODO Auto-generated method stub
      
   }

   public HashMap getResponseCookies()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setResponseCookies(HashMap cookies)
   {
      // TODO Auto-generated method stub
      
   }

   public Document getInputDocument()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setInputDocument(Document inDoc)
   {
      // TODO Auto-generated method stub
      
   }

   public Locale getPreferredLocale()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public int getCurrentApplicationAccessLevel()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public String getCurrentApplicationName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getPrivateObject(Object key) throws PSRuntimeException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setPrivateObject(Object key, Object o) throws PSRuntimeException
   {
      // TODO Auto-generated method stub
      
   }

   public Object getSessionPrivateObject(Object key)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setSessionPrivateObject(Object key, Object o)
   {
      // TODO Auto-generated method stub
      
   }

   public Object getSessionObject(String key)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getUserRoles(String provider, String providerInstance, String name)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoles(PSSubject subject)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoles()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoles(String subjectName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getRoles()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getRoleSubjects(String roleName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getRoleSubjects(String roleName, int memberFlags, String subjectNameFilter)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjects(String subjectNameFilter)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getRoleAttributes(String roleName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectGlobalAttributes(String subjectNameFilter, int subjectType, int providerType, String providerInstance, String roleName, String attributeNameFilter, boolean includeEmptySubjects)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectGlobalAttributes()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectGlobalAttributes(PSSubject subject)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoleAttributes(String subjectNameFilter, int subjectType, int providerType, String providerInstance, String roleName, String attributeNameFilter)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoleAttributes(String roleName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoleAttributes(PSSubject subject, String roleName)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getRoleMembers(String roleName, int flags, int memberFlags)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getUserContextInformation(String contextItem, Object defValue) throws PSDataExtractionException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void printTraceMessage(String message)
   {
      // TODO Auto-generated method stub
      
   }

   public boolean isTraceEnabled()
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean setGeneralHeader(String name, String value)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean setEntityHeader(String name, String value)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean setResponseHeader(String name, String value)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public IPSInternalRequest getInternalRequest(String resource)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public IPSInternalRequest getInternalRequest(String resource, Map extraParams, boolean inheritCurrentParams)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getContentHeaderOverride()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setContentHeaderOverride(String header)
   {
      // TODO Auto-generated method stub
      
   }

   public PSSecurityToken getSecurityToken()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getUserLocale()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void pauseRequestTimer()
   {
      // TODO Auto-generated method stub
      
   }

   public void continueRequestTimer()
   {
      // TODO Auto-generated method stub
      
   }

   public List getSubjectGlobalAttributes(String subjectNameFilter, int subjectType, String roleName, String attributeNameFilter, boolean includeEmptySubjects, String communityId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectGlobalAttributes(String subjectNameFilter, int subjectType, String roleName, String attributeNameFilter, boolean includeEmptySubjects)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getSubjectRoleAttributes(String subjectNameFilter, int subjectType, String roleName, String attributeNameFilter)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public PSSubject getOriginalSubject()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getRoleEmailAddresses(String roleName, String emailAttributeName, String community)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getRoleEmailAddresses(String roleName, String emailAttributeName, String community, Set subjectsWithoutEmail)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getSubjectEmailAddresses(String subjectName, String emailAttributeName, String community)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Enumeration getHeaders()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setCgiVariable(String cgiVarName, String cgiVarValue)
   {
      // TODO Auto-generated method stub
      
   }

   public String getUserName()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Set<PSSubject> expandGroups(Set<PSSubject> subjects)
   {
      return null;
   }      
}
