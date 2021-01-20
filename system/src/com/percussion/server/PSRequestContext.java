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
package com.percussion.server;

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSUserContextExtractor;
import com.percussion.debug.PSDebugLogHandler;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.log.PSLogHandler;
import com.percussion.security.PSRoleManager;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSUserEntry;
import com.percussion.util.IPSHtmlParameters;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The PSRequestContext class contains all the context information of a given
 * request.
 */
@SuppressWarnings(value={"unchecked"})
public class PSRequestContext implements IPSRequestContext
{
   /**
    * Construct a PSRequestContext object.
    *
    * @param req  the request
    *
    * @throws IllegalArgumentException if request is <code>null</code>
    */
   public PSRequestContext(PSRequest req)
   {
      if ( null == req )
         throw new IllegalArgumentException( "Request can't be null" );
      m_requestToProxy = req;
   }

   /***  IPSRequestContext implementation ***/
   
   // see IPSRequestContext
   public PSSubject getOriginalSubject()
   {
      return m_requestToProxy.getOriginalSubject();
   }

   /**
    * @see com.percussion.server.IPSRequestContext#getServerHostAddress()
    */
   public String getServerHostAddress()
   {
      return PSServer.getHostAddress();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getServerListenerPort()
    */
   public int getServerListenerPort()
   {
      return PSServer.getListenerPort();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getUserSessionId()
    */
   public String getUserSessionId()
   {
      return m_requestToProxy.getUserSessionId();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestFileURL()
    */
   public String getRequestFileURL()
   {
      return m_requestToProxy.getRequestFileURL();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestRoot()
    */
   public String getRequestRoot()
   {
      return m_requestToProxy.getRequestRoot();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestPage()
    */
   public String getRequestPage()
   {
      return m_requestToProxy.getRequestPage();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestPage(boolean)
    */
   public String getRequestPage(boolean includeExtension)
   {
      return m_requestToProxy.getRequestPage(includeExtension);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestPageType()
    */
   public int getRequestPageType()
   {
      return m_requestToProxy.getRequestPageType();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestPageExtension()
    */
   public String getRequestPageExtension()
   {
      return m_requestToProxy.getRequestPageExtension();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getHookURL()
    */
   public String getHookURL()
   {
      return m_requestToProxy.getHookURL();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getParameter(java.lang.String)
    */
   public String getParameter(String name)
   {
      return m_requestToProxy.getParameter(name);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getParameter(java.lang.String, java.lang.Object)
    */
   public String getParameter(String name, Object defValue)
   {
      if(defValue == null)
         return m_requestToProxy.getParameter(name);
      else
         return m_requestToProxy.getParameter(name, defValue.toString());
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getParameterObject(java.lang.String, java.lang.Object)
    */
   public Object getParameterObject(String name, Object defValue)
   {
      return m_requestToProxy.getSingleParameterObject(name, defValue);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getParameterObject(java.lang.String)
    */
   public Object getParameterObject(String name)
   {
      return getParameterObject(name, null);
   }

   /**
    * @see com.percussion.server.IPSRequestContext#getParameters()
    * @deprecated
    */
   public HashMap getParameters()
   {
      return m_requestToProxy.getParameters();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getParameterList(java.lang.String)
    */
   public Object[] getParameterList(String name)
   {
      return m_requestToProxy.getParameterList(name);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#removeParameter(java.lang.String)
    */
   public Object removeParameter(String name)
   {
      return m_requestToProxy.removeParameter(name);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#appendParameter(java.lang.String, java.lang.Object)
    */
   public void appendParameter(String name, Object value)
   {
      m_requestToProxy.appendParameter(name, value);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getParametersIterator()
    */
   public Iterator getParametersIterator()
   {
      return m_requestToProxy.getParametersIterator();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#hasMultiValuesForAnyParameter()
    */
   public boolean hasMultiValuesForAnyParameter()
   {
      return m_requestToProxy.hasMultiValuesForAnyParameter();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getFileCharacterSet()
    */
   public String getFileCharacterSet()
   {
      return m_requestToProxy.getFileCharacterSet();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setFileCharacterSet(java.lang.String)
    */
   public void setFileCharacterSet(String encoding)
   {
      m_requestToProxy.setFileCharacterSet(encoding);
   }

   /**
    * @see com.percussion.server.IPSRequestContext#setParameters(java.util.HashMap)
    * @deprecated
    */
   public void setParameters(HashMap params)
   {
      m_requestToProxy.setParameters(params);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setParameter(java.lang.String, java.lang.Object)
    */
   public void setParameter(String name, Object value)
   {
      m_requestToProxy.setParameter(name, value);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setParameterList(java.lang.String, java.lang.Object[])
    */
   public void setParameterList(String name, Object[] values)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      Object value;
      if (values == null || values.length == 0)
         value = null;
      else if (values.length == 1)
         value = values[0];
      else
      {
         ArrayList list = new ArrayList( values.length );
         for (int i = 0; i < values.length; i++)
         {
            Object val = values[i];
            if (!(val instanceof String))
               throw new IllegalArgumentException(
                  "all array values must be of type String");

            list.add(val);
         }

         value = list;
      }
      m_requestToProxy.setParameter( name, value );
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getTruncatedParameters()
    */
   public Map getTruncatedParameters()
   {
      return m_requestToProxy.getTruncatedParameters();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getCgiVariable(java.lang.String)
    */
   public String getCgiVariable(String name)
   {
      return m_requestToProxy.getCgiVariable(name);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestCookie(java.lang.String, java.lang.String)
    */
   public String getRequestCookie(String name, String defValue)
   {
      return m_requestToProxy.getCookie(name, defValue);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRequestCookie(java.lang.String)
    */
   public String getRequestCookie(String name)
   {
      return m_requestToProxy.getCookie(name);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setResponseCookie(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, boolean)
    */
   public void setResponseCookie(String name, String value, String path,
                                 String domain, java.util.Date expires,
                                 boolean secure)
   {
      m_requestToProxy.getResponse().setCookie(name, value, path, domain,
         expires, secure);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getResponseCookies()
    */
   public java.util.HashMap getResponseCookies()
   {
      return m_requestToProxy.getResponse().getCookies();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setResponseCookies(java.util.HashMap)
    */
   public void setResponseCookies(java.util.HashMap cookies)
   {
      m_requestToProxy.getResponse().setCookies(cookies);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getInputDocument()
    */
   public org.w3c.dom.Document getInputDocument()
   {
      return m_requestToProxy.getInputDocument();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setInputDocument(org.w3c.dom.Document)
    */
   public void setInputDocument(org.w3c.dom.Document inDoc)
   {
      m_requestToProxy.setInputDocument(inDoc);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getPreferredLocale()
    */
   public java.util.Locale getPreferredLocale()
   {
      return m_requestToProxy.getPreferredLocale();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getCurrentApplicationAccessLevel()
    */
   public int getCurrentApplicationAccessLevel()
   {
      return m_requestToProxy.getCurrentApplicationAccessLevel();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getCurrentApplicationName()
    */
   public String getCurrentApplicationName()
   {
      PSApplicationHandler ah = m_requestToProxy.getApplicationHandler();
      if (ah != null)
         return ah.getRequestRoot();
      else
         return "";
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getPrivateObject(java.lang.Object)
    */
   public Object getPrivateObject(Object key)
      throws com.percussion.error.PSRuntimeException
   {
      return m_requestToProxy.getPrivateObject(key);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setPrivateObject(java.lang.Object, java.lang.Object)
    */
   public void setPrivateObject(Object key, Object o)
      throws com.percussion.error.PSRuntimeException
   {
      m_requestToProxy.setPrivateObject(key, o);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSessionPrivateObject(java.lang.Object)
    */
   public Object getSessionPrivateObject(Object key)
   {
      return m_requestToProxy.getUserSession().getPrivateObject(key);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setSessionPrivateObject(java.lang.Object, java.lang.Object)
    */
   public void setSessionPrivateObject(Object key, Object o)
   {
      m_requestToProxy.getUserSession().setPrivateObject(key, o);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSessionObject(java.lang.String)
    */
   public Object getSessionObject(String key)
   {
      return m_requestToProxy.getUserSession().getSessionObject(key);
   }
   
   // see IPSRequestContext
   public List getUserRoles(String provider, String providerInstance,
      String name)
   {
      PSSubject subject = new PSGlobalSubject(name, 
         PSSubject.SUBJECT_TYPE_USER, null);
      
      return getSubjectRoles(subject);
   }
   
   // see IPSRequestContext
   public List getSubjectGlobalAttributes(String subjectNameFilter,
      int subjectType, int providerType, String providerInstance,
      String roleName, String attributeNameFilter, boolean includeEmptySubjects)
   {
      String communityId = (String) getPrivateObject(
         IPSHtmlParameters.SYS_ITEM_COMMUNITYID);

      return getSubjectGlobalAttributes(subjectNameFilter, subjectType, 
         roleName, attributeNameFilter, includeEmptySubjects, communityId);
   }

   // see IPSRequestContext
   public List getSubjectRoleAttributes(String subjectNameFilter,
      int subjectType, int providerType, String providerInstance,
      String roleName, String attributeNameFilter)
   {
      return getSubjectRoleAttributes(subjectNameFilter, subjectType, 
         roleName, attributeNameFilter);
   }

   /**
    * Sets the given object on the user's session see
    * {@link PSUserSession#setSessionObject(String, Object)}.
    * 
    * @param key the key to the object, must never be <code>null</code>.
    *           Checked by called method.
    * @param o the object, must never be <code>null</code> Checked by called
    *           method.
    */
   public void setSessionObject(String key, Object o)
   {
      m_requestToProxy.getUserSession().setSessionObject((String)key, o);
   }

   /**
    * Clears the named key from the user's session see 
    * {@link PSUserSession#clearSessionObject(String)}. 
    * @param key the key to the object, must never be <code>null</code>. 
    * Checked by called method.
    * @return original value that was cleared, may be <code>null</code>.
    */
   public Object clearSessionObject(String key)
   {
      return m_requestToProxy.getUserSession().clearSessionObject(key);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getOriginalHost()
    */
   public String getOriginalHost()
   {
      return m_requestToProxy.getUserSession().getOriginalHost();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getOriginalPort()
    */
   public int getOriginalPort()
   {
      return m_requestToProxy.getUserSession().getOriginalPort();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getOriginalProtocol()
    */
   public String getOriginalProtocol()
   {
      return m_requestToProxy.getUserSession().getOriginalProtocol();
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getUserName()
    */
   public String getUserName()
   {
      return m_requestToProxy.getServletRequest().getRemoteUser();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getContentHeaderOverride()
    */
   public String getContentHeaderOverride()
   {
      return m_requestToProxy.getContentHeaderOverride();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setContentHeaderOverride(java.lang.String)
    */
   public void setContentHeaderOverride(String header)
   {
      m_requestToProxy.setContentHeaderOverride(header);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectRoles()
    */
   public List getSubjectRoles()
   {
      return getSubjectRoles((PSSubject) null);
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectRoles(java.lang.String)
    */
   public List getSubjectRoles( String subjectName )
   {
      return PSRoleManager.getInstance().memberRoleList(
            m_requestToProxy.getUserSession(), subjectName );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectRoles(com.percussion.design.objectstore.PSSubject)
    */
   public List getSubjectRoles( PSSubject subject )
   {
      if ( subject == null )
      {
         subject = getSubjectFromSession();
         if ( null == subject )
            return new ArrayList();
      }

      return PSRoleManager.getInstance().memberRoleList(
            m_requestToProxy.getUserSession(), subject );
   }

   // see IPSRequestContext for a description
   public Set getRoleEmailAddresses(String roleName, String emailAttributeName,
      String community)
   {
      return getRoleEmailAddresses(roleName, emailAttributeName, community, 
         null);
   }
   
   // see IPSRequestContext for a description
   public Set getRoleEmailAddresses(String roleName, String emailAttributeName,
      String community, Set subjectsWithoutEmail)
   {
      if (roleName == null)
         throw new IllegalArgumentException("roleName cannot be null");
         
      roleName = roleName.trim();
      if (roleName.length() == 0)
         throw new IllegalArgumentException("roleName cannot be empty");
         
      Set emails = new TreeSet();
      
      emails.addAll(PSRoleManager.getInstance().getRoleEmailAddresses(roleName, 
         emailAttributeName, community, subjectsWithoutEmail));

      return emails;
   }
   
   // see IPSRequestContext for a description
   public Set getSubjectEmailAddresses(String subjectName, 
      String emailAttributeName, String community)
   {
      if (subjectName == null)
         throw new IllegalArgumentException("roleName cannot be null");
         
      subjectName = subjectName.trim();
      if (subjectName.length() == 0)
         throw new IllegalArgumentException("roelName cannot be empty");
         
      Set emails = new HashSet();

      emails.addAll(PSRoleManager.getInstance().getSubjectEmailAddresses(
         subjectName, emailAttributeName, community));
      
      return emails;
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRoleSubjects(java.lang.String)
    */
   public List getRoleSubjects(String roleName)
   {
      return getRoleSubjects(roleName, 0, null);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRoleSubjects(java.lang.String, int, java.lang.String)
    */
   public List getRoleSubjects(String roleName, int memberFlags,
         String filter)
   {
      return PSRoleManager.getInstance().roleMembers(roleName, memberFlags, 
         filter);
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRoles()
    */
   public List getRoles()
   {
      return PSRoleManager.getInstance().getRoles();
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRoleAttributes(java.lang.String)
    */
   public List getRoleAttributes( String roleName )
   {
      return PSRoleManager.getInstance().getRoleAttributes(roleName);
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectGlobalAttributes()
    */
   public List getSubjectGlobalAttributes()
   {
      return getSubjectGlobalAttributes(null);
   }

   // see IPSRequestContext for desc
   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectGlobalAttributes(com.percussion.design.objectstore.PSSubject)
    */
   public List getSubjectGlobalAttributes( PSSubject subject )
   {
      if ( null == subject )
      {
         subject = getSubjectFromSession();
         if ( null == subject )
            return new ArrayList();
      }
      return getSubjectGlobalAttributes( subject.getName(), subject.getType(),
         null, null, false );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectGlobalAttributes(java.lang.String, int, int, java.lang.String, java.lang.String, java.lang.String, boolean)
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter,
         int subjectType, String roleName, String attributeNameFilter,
         boolean includeEmptySubjects, String communityId)
   {
      return PSRoleManager.getInstance().getSubjectGlobalAttributes(
         subjectNameFilter, subjectType, roleName, attributeNameFilter, 
         includeEmptySubjects, communityId);
   }

   // see IPSRequestContext for desc
   public List getSubjectGlobalAttributes(String subjectNameFilter,
         int subjectType, String roleName, String attributeNameFilter,
         boolean includeEmptySubjects)
   {
      return getSubjectGlobalAttributes(subjectNameFilter, subjectType, 
         roleName, attributeNameFilter, includeEmptySubjects, null);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectRoleAttributes(java.lang.String)
    */
   public List getSubjectRoleAttributes( String roleName )
   {
      return getSubjectRoleAttributes( null, roleName );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjectRoleAttributes(com.percussion.design.objectstore.PSSubject, java.lang.String)
    */
   public List getSubjectRoleAttributes( PSSubject subject, String roleName )
   {
      if ( null == roleName || roleName.trim().length() == 0 )
      {
         throw new IllegalArgumentException(
               "role name can't be null or empty" );
      }

      if ( null == subject )
      {
         subject = getSubjectFromSession();
         if ( null == subject )
            return new ArrayList();
      }
      return getSubjectRoleAttributes( subject.getName(), subject.getType(),
                                       roleName, null);
   }

   // see IPSRequestContext for desc
   public List getSubjectRoleAttributes(String subjectNameFilter,
                                        int subjectType, String roleName,
                                        String attributeNameFilter)
   {
      return PSRoleManager.getInstance().getSubjectRoleAttributes(
         subjectNameFilter, subjectType, roleName, attributeNameFilter);
   }

   /**
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getRoleMembers(java.lang.String, int, int)
    * 
    * @deprecated
    */
   public List getRoleMembers(String roleName, int flags, int memberFlags)
   {
      return getRoleSubjects(roleName, memberFlags, null );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSubjects(java.lang.String)
    */
   public List getSubjects( String subjectNameFilter )
   {
      return getRoleSubjects( null, PSSubject.SUBJECT_TYPE_USER,
            subjectNameFilter );
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getUserContextInformation(java.lang.String, java.lang.Object)
    */
   public Object getUserContextInformation(String contextItem, Object defValue)
      throws PSDataExtractionException
   {
      if ((contextItem == null) || (contextItem.length() == 0))
         throw new IllegalArgumentException(
            "Null or empty context Item string is invalid");

      return PSUserContextExtractor.getUserContextInformation(contextItem,
               m_requestToProxy, defValue);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#printTraceMessage(java.lang.String)
    */
   public void printTraceMessage(String message)
   {
      // get the log handler
      PSLogHandler lh = m_requestToProxy.getLogHandler();
      if(!(lh instanceof PSDebugLogHandler))
         return;
         
      PSDebugLogHandler dlh = (PSDebugLogHandler)lh;

      // create an exit execution message
      if (dlh.isTraceEnabled(PSTraceMessageFactory.EXIT_EXEC_FLAG))
         dlh.printTrace(PSTraceMessageFactory.EXIT_EXEC_FLAG, message);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#isTraceEnabled()
    */
   public boolean isTraceEnabled()
   {
      // check to see if exit execution tracing is enabled
      PSLogHandler lh = m_requestToProxy.getLogHandler();
      if (lh instanceof PSDebugLogHandler)
      {
         PSDebugLogHandler dh = (PSDebugLogHandler) lh;
         return dh.isTraceEnabled(PSTraceMessageFactory.EXIT_EXEC_FLAG);
      }
      
      return false;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setGeneralHeader(
    *    java.lang.String, java.lang.String)
    */
   public boolean setGeneralHeader( String name, String value )
   {
      if ( null == name || null == value )
         throw new IllegalArgumentException(
            "Header name and value cannot be null." );
      return m_requestToProxy.getResponse().setGeneralHeader( name, value );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setEntityHeader(java.lang.String, java.lang.String)
    */
   public boolean setEntityHeader( String name, String value )
   {
      if ( null == name || name.trim().length() == 0 || null == value )
         throw new IllegalArgumentException(
               "Header name and value cannot be null and name cannot be empty."
               );
      return m_requestToProxy.getResponse().setEntityHeader( name, value );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setResponseHeader(java.lang.String, java.lang.String)
    */
   public boolean setResponseHeader( String name, String value )
   {
      if ( null == name || null == value )
         throw new IllegalArgumentException(
            "Header name and value cannot be null." );
      return m_requestToProxy.getResponse().setResponseHeader( name, value );
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getInternalRequest(java.lang.String)
    */
   public IPSInternalRequest getInternalRequest(String resource)
   {
      return getInternalRequest( resource, null );
   }


   /**
    * Wrapper method that calls 
    * {@link #getInternalRequest(String, Map, boolean) 
    * getInternalRequest(resource, null, true)}.
    */
   public IPSInternalRequest getInternalRequest(String resource,
      Map extraParams)
   {
      return getInternalRequest(resource, extraParams, true);
   }


   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getInternalRequest(java.lang.String, java.util.Map, boolean)
    */
   public IPSInternalRequest getInternalRequest(String resource,
                                                Map extraParams,
                                                boolean inheritCurrentParams)
   {
      PSInternalRequest ir = PSServer.getInternalRequest( resource,
         m_requestToProxy, extraParams, inheritCurrentParams );

      if (ir != null)
      {
         // use a proxy so exits can't get the real request object
         return new PSInternalRequestProxy( ir );
      }
      else
         return null;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getSecurityToken()
    */
   public PSSecurityToken getSecurityToken()
   {
      return m_requestToProxy.getSecurityToken();
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#pauseRequestTimer()
    */
   public void pauseRequestTimer()
   {
      m_requestToProxy.getRequestTimer().pause();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#continueRequestTimer()
    */
   public void continueRequestTimer()
   {
      m_requestToProxy.getRequestTimer().cont(); 
   }

   

   /* (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getHeaders()
    */
   public Enumeration getHeaders()
   {
      return m_requestToProxy.getServletRequest().getHeaderNames();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#setCgiVariable(java.lang.String, java.lang.String)
    */
   public void setCgiVariable(String cgiVarName, String cgiVarValue)
   {
      m_requestToProxy.setCgiVariable(cgiVarName, cgiVarValue);
   }
   
   /**
    * Get the request. In order to prevent Exit to access the request object,
    * made this method to be package protected.
    *
    * @return the request object, never <code>null</code>.
    */
   public PSRequest getRequest()
   {
      return m_requestToProxy;
   }

   /**
    * Builds a new PSSubject from this object's request context. If there are
    * no authenticated users, <code>null</code> is returned. Note that if the
    * session contains more than 1 authenticated user, you will get the first
    * one that happens to be in the array.
    *
    * @return A valid subject based on the 1st authenticated user in the
    *    session, or <code>null</code> if there are no authenticated users
    *    therein.
    */
   private PSSubject getSubjectFromSession()
   {
      PSUserEntry [] users =
            m_requestToProxy.getUserSession().getAuthenticatedUserEntries();
      if ( users.length == 0 )
         return null;
      else
         return new PSGlobalSubject(users[0].getName(),
            PSSubject.SUBJECT_TYPE_USER, null );
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#getUserLocale()
    */
   public String getUserLocale()
   {
      String lang = (String) getSessionPrivateObject(
            PSI18nUtils.USER_SESSION_OBJECT_SYS_LANG);
      if (lang == null)
         lang = PSI18nUtils.DEFAULT_LANG;
      return lang;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.server.IPSRequestContext#expandGroups(
    * Set<PSSubject> subjects)
    */
   public Set<PSSubject> expandGroups(Set<PSSubject> subjects)
   {
      return PSRoleManager.getInstance().expandGroups(subjects);
   }   
   
   /**
    * The request to proxy.  Never <code>null</code> after construction.
    */
   private PSRequest m_requestToProxy;

}
