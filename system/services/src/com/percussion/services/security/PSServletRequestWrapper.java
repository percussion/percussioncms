/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services.security;


import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;



/**
 * Wrap an existing servlet request to add authentication information. This
 * class also holds some data that we need at random places in the Rhythmyx
 * server, that would cause problems if we called the actual servlet request
 * due to lifecycle issues.
 * 
 * @author dougrand
 */
public class PSServletRequestWrapper extends HttpServletRequestWrapper
{
   /**
    * Keep a reference to the subject for use in calls to get user, 
    * group and role information
    */
   Subject m_subject;
   
   /**
    * Used to cache calculated roles obtained from the subject
    */
   Collection<String> m_roles = null;
   
   /**
    * Hold the secure information
    */
   boolean m_isSecure = false;
   
   /**
    * Hold the scheme information
    */
   String m_scheme = null;
   
   /**
    * Create a new request wrapper with a JAAS subject from an authenticated
    * user.
    * @param req the request, never <code>null</code>.  This request should not
    * be modified by this class.
    * @param subject the subject, may be <code>null</code>
    */
   public PSServletRequestWrapper(HttpServletRequest req, Subject subject)
   {
      super(req);

      m_subject = subject;
      m_isSecure = req.isSecure();
      m_scheme = req.getScheme();
   }
   
   /**
    * Set a new subject. Also resets the cached roles of this wrapper to make
    * sure they are recataloged on the next access.
    * 
    * @param subject the new subject to set for this wrapper, may be 
    *    <code>null</code> or empty.
    */
   public void setSubject(Subject subject)
   {
      m_subject = subject;
      m_roles = null;
   }
   
   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
    */
   @Override
   public String getRemoteUser()
   {
      String remoteUser = null;
      if (m_subject != null)
      {
         Iterator<Principal> principals = m_subject.getPrincipals().iterator();
         while (principals.hasNext())
         {
            Principal p = principals.next();
            if (p instanceof Group)
               continue;
            if (p instanceof IPSTypedPrincipal)
            {
               IPSTypedPrincipal tp = (IPSTypedPrincipal)p;
               if (tp.getPrincipalType().equals(PrincipalTypes.GROUP) || 
                  tp.getPrincipalType().equals(PrincipalTypes.ROLE))
               {
                  continue;
               }
            }
            remoteUser = p.getName();
            break;
         }
      }
      
      return remoteUser;
   }
   
   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
    */
   @Override
   public boolean isUserInRole(String role)
   {
      if (m_subject == null)
      {
         return false;
      }
      else
      {
         getRoles(); // Ensure that roles are populated
         Iterator it = m_roles.iterator();
         boolean exists = false;
         while(it.hasNext())
         {
            if ( StringUtils.equalsIgnoreCase(role, (String)it.next()) )
            {
               exists = true;
               break;
            }
         }
         return exists;
      }
   }

   /**
    * @return the list of roles for the current user
    */
   public Collection<String> getRoles()
   {
      if (m_roles == null)
      {
         m_roles = new HashSet<>();
         Group roles = PSJaasUtils.findOrCreateGroup(m_subject.getPrincipals(),
            PSJaasUtils.ROLE_GROUP_NAME);
         Enumeration<? extends Principal> renum = roles.members();
         while(renum.hasMoreElements())
         {
            Principal role = (Principal) renum.nextElement();
            m_roles.add(role.getName());
         }
      }
      return m_roles;
   }

   /**
    * @return Returns the isSecure.
    */
   @Override
   public boolean isSecure()
   {
      return m_isSecure;
   }

   /**
    * @return Returns the scheme.
    */
   @Override
   public String getScheme()
   {
      return m_scheme;
   }


}
