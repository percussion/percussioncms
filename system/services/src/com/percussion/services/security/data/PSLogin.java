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
package com.percussion.services.security.data;

import com.percussion.design.objectstore.PSRole;
import com.percussion.i18n.PSLocale;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
   private List<PSCommunity> communities = new ArrayList<PSCommunity>();
   
   /**
    * A list with all roles to which the logged in user is a member, never
    * <code>null</code>, may be empty.
    */
   private List<PSRole> roles = new ArrayList<PSRole>();
   
   /**
    * A list of enabled locales for the logged in user, never <code>null</code>,
    * may be empty.
    */
   private List<PSLocale> locales = new ArrayList<PSLocale>();

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
         this.communities = new ArrayList<PSCommunity>();
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
         this.roles = new ArrayList<PSRole>();
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
         this.locales = new ArrayList<PSLocale>();
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
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
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

