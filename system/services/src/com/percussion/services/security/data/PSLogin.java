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
package com.percussion.services.security.data;

import com.percussion.design.objectstore.PSRole;
import com.percussion.i18n.PSLocale;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

public class PSLogin implements Serializable 
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -8163377713290656891L;

   /**
    * The session id for the logged in user, may be null, not empty. This is 
    * required for all other web services as input parameter.
    */
   private String sessionId;

   /**
    * The session timeout in milliseconds, -1 if the session does not timeout.
    */
   private long sessionTimeout;

   /**
    * The name of the default community for the logged in user, may be
    * <code>null</code>, not empty.
    */
   private String defaultCommunity;

   /**
    * The default locale code for the logged in user, may be <code>null</code>,
    * not empty.
    */
   private String defaultLocaleCode;
   
   /**
    * A list with all communities to which the logged in user is a member,
    * never <code>null</code>, may be empty.
    */
   private List<PSCommunity> communities = new ArrayList<>();
   
   /**
    * A list with all roles to which the logged in user is a member, never
    * <code>null</code>, may be empty.
    */
   private List<PSRole> roles = new ArrayList<>();
   
   /**
    * A list of enabled locales for the logged in user, never <code>null</code>,
    * may be empty.
    */
   private List<PSLocale> locales = new ArrayList<>();

   /**
    * Default constructor.
    */
   public PSLogin()
   {
   }
   
   /**
    * Constructs a new login for the supplied parameters.
    * 
    * @param sessionId the session id for the new login, not <code>null</code>
    *    or empty.
    */
   public PSLogin(String sessionId)
   {
      setSessionId(sessionId);
   }
   
   /**
    * Get the session id for the logged in user. This is required for all 
    * other web services as input parameter.
    * 
    * @return the login session id, may be <code>null</code>, never empty.
    */
   public String getSessionId()
   {
      return sessionId;
   }
   
   /**
    * Set a new session id for the logged in user.
    * 
    * @param sessionId the new session id, not <code>null</code> or empty.
    */
   public void setSessionId(String sessionId)
   {
      if (StringUtils.isBlank(sessionId))
         throw new IllegalArgumentException(
            "sessionId cannot be null or empty");
      
      this.sessionId = sessionId;
   }
   
   /**
    * Get the session time out.
    * 
    * @return the session timeout in milliseconds, -1 if the session does
    *    not timeout.
    */
   public long getSessionTimeout()
   {
      return sessionTimeout;
   }
   
   /**
    * Set a new session timeout.
    * 
    * @param sessionTimeout the new session timeout in milliseconds, -1 if
    *    the session does not timeout.
    */
   public void setSessionTimeout(long sessionTimeout)
   {
      this.sessionTimeout = sessionTimeout;
   }
   
   /**
    * Get the name of the default community for the logged in user.
    * 
    * @return the default ccommunity for the loggedd in user, may be 
    *    <code>null</code>, not empty.
    */
   public String getDefaultCommunity()
   {
      return defaultCommunity;
   }
   
   /**
    * Set the new default community name for the logged in user.
    * 
    * @param defaultCommunity the new dedault community name, may be
    *    <code>null</code>, not empty.
    */
   public void setDefaultCommunity(String defaultCommunity)
   {
      if (StringUtils.isWhitespace(defaultCommunity))
         throw new IllegalArgumentException("defaultCommunity cannot be empty");
      
      this.defaultCommunity = defaultCommunity;
   }
   
   /**
    * Get the default locale code for the logged in user.
    * 
    * @return the efault locale code for the logged in user, may be
    *    <code>null</code>, not empty.
    */
   public String getDefaultLocaleCode()
   {
      return defaultLocaleCode;
   }
   
   /**
    * Set a new default locale codde for the logged in user.
    * 
    * @param defaultLocaleCode the new default locale code, may be
    *    <code>null</code, not empty.
    */
   public void setDefaultLocaleCode(String defaultLocaleCode)
   {
      if (StringUtils.isWhitespace(defaultLocaleCode))
         throw new IllegalArgumentException(
            "defaultLocaleCode cannot be empty");
      
      this.defaultLocaleCode = defaultLocaleCode;
   }
   
   /**
    * Get all communities to which the logged in user is a member of.
    * 
    * @return all communities to which the logged in user is a member of, 
    *    never <code>null</code>, may be empty.
    */
   public List<PSCommunity> getCommunities()
   {
      return communities;
   }
   
   /**
    * Set the list of communities to which the logged in user is a member of.
    * 
    * @param communities the list of communities to which the logged in user 
    *    is a member of, may be <code>null</code> or empty.
    */
   public void setCommunities(List<PSCommunity> communities)
   {
      if (communities == null)
         this.communities = new ArrayList<>();
      else
         this.communities = communities;
   }
   
   /**
    * Add a new community to which the logged in user is a member.
    * 
    * @param community a community to which the logged in user is a member, 
    *    not <code>null</code>.
    */
   public void addCommunity(PSCommunity community)
   {
      if (community == null)
         throw new IllegalArgumentException("community cannot be null");
      
      communities.add(community);
   }
   
   /**
    * Get all roles to which the logged in user is a member of.
    * 
    * @return all roles to which the logged in user is a member of, 
    *    never <code>null</code>, may be empty.
    */
   public List<PSRole> getRoles()
   {
      return roles;
   }
   
   /**
    * Set the list of roles to which the logged in user is a member of.
    * 
    * @param roles the list of roles to which the logged in user 
    *    is a member of, may be <code>null</code> or empty.
    */
   public void setRoles(List<PSRole> roles)
   {
      if (roles == null)
         this.roles = new ArrayList<>();
      else
         this.roles = roles;
   }
   
   /**
    * Add a new role to which the logged in user is a member.
    * 
    * @param role a role to which the logged in user is a member, 
    *    not <code>null</code>.
    */
   public void addRole(PSRole role)
   {
      if (role == null)
         throw new IllegalArgumentException("role cannot be null");
      
      roles.add(role);
   }
   
   /**
    * Get all locales enabled for the logged in user.
    * 
    * @return all locales enabled for the logged in user, never 
    *    <code>null</code>, may be empty.
    */
   public List<PSLocale> getLocales()
   {
      return locales;
   }
   
   /**
    * Set the list of locales enabled for the logged in user.
    * 
    * @param locales the list of locales enabled for the logged in user, 
    *    may be <code>null</code> or empty.
    */
   public void setLocales(List<PSLocale> locales)
   {
      if (locales == null)
         this.locales = new ArrayList<>();
      else
         this.locales = locales;
   }
   
   /**
    * Add a new locale enabled for the logged in user.
    * 
    * @param locale a locale enabled for the logged in user, not 
    *    <code>null</code>.
    */
   public void addLocale(PSLocale locale)
   {
      if (locale == null)
         throw new IllegalArgumentException("locale cannot be null");
      
      locales.add(locale);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSLogin)) return false;
      PSLogin psLogin = (PSLogin) o;
      return getSessionTimeout() == psLogin.getSessionTimeout() && Objects.equals(getSessionId(), psLogin.getSessionId()) && Objects.equals(getDefaultCommunity(), psLogin.getDefaultCommunity()) && Objects.equals(getDefaultLocaleCode(), psLogin.getDefaultLocaleCode()) && Objects.equals(getCommunities(), psLogin.getCommunities()) && Objects.equals(getRoles(), psLogin.getRoles()) && Objects.equals(getLocales(), psLogin.getLocales());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getSessionId(), getSessionTimeout(), getDefaultCommunity(), getDefaultLocaleCode(), getCommunities(), getRoles(), getLocales());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSLogin{");
      sb.append("sessionId='").append(sessionId).append('\'');
      sb.append(", sessionTimeout=").append(sessionTimeout);
      sb.append(", defaultCommunity='").append(defaultCommunity).append('\'');
      sb.append(", defaultLocaleCode='").append(defaultLocaleCode).append('\'');
      sb.append(", communities=").append(communities);
      sb.append(", roles=").append(roles);
      sb.append(", locales=").append(locales);
      sb.append('}');
      return sb.toString();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

