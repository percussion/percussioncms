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
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
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
   public boolean isSecure()
   {
      return m_isSecure;
   }

   /**
    * @return Returns the scheme.
    */
   public String getScheme()
   {
      return m_scheme;
   }
}
