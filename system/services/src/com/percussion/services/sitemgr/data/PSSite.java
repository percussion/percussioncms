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
package com.percussion.services.sitemgr.data;


import static com.percussion.util.PSBase64Decoder.decode;
import static com.percussion.util.PSBase64Encoder.encode;
import static org.apache.commons.lang.StringUtils.isBlank;

import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A site represents a logical (and currently physical) place to publish
 * content. The site is associated with a portion of the site folder tree in the
 * repository. The site publishes to a specific publisher and may be referenced
 * by one or more editions.
 *
 * @author dougrand
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSSite")
@NaturalIdCache
@Table(name = "RXSITES")
public class PSSite implements IPSSite, IPSCatalogItem
{

   public void copy(IPSSite isite)
   {
      if (isite == null)
         throw new IllegalArgumentException("isite may not be null.");
      if (!(isite instanceof PSSite))
         throw new IllegalArgumentException(
               "isite must be an instance of PSSite.");

      PSSite site = (PSSite) isite;

      allowedNamespaces = site.allowedNamespaces;
      baseUrl = site.baseUrl;
      description = site.description;
      folderRoot = site.folderRoot;
      globalTemplate = site.globalTemplate;
      ipAddress = site.ipAddress;
      name = site.name;
      previousName = site.previousName;
      navTheme = site.navTheme;
      password = site.password;
      port = site.port;
      root = site.root;
      state = site.state;
      userId = site.userId;
      is_secure = site.is_secure;
      defaultPubServer = site.defaultPubServer;
      defaultFileExtention = site.defaultFileExtention;
      is_canonical = site.is_canonical;
      siteProtocol = site.siteProtocol;
      defaultDocument = site.defaultDocument;
      canonicalDist = site.canonicalDist;
      is_canonical_replace = site.is_canonical_replace;

      //deal w/ collections
      templates = new HashSet<>();
      for (IPSAssemblyTemplate t : site.templates)
      {
         addTemplateGuidToCollection(t.getGUID());
      }
      properties = new HashSet<>();
      for (PSSiteProperty prop : site.properties)
      {
         setProperty(prop.getName(), prop.getContextId(), prop.getValue());
      }
   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("site-property", PSSiteProperty.class);
      PSXmlSerializationHelper.addType("template-id", PSGuid.class);
   }

   @Id
   @Column(name = "SITEID")
   Long siteId;

   @Version
   @Column(name = "VERSION")
   Integer version;

   @Basic
   @Column(name = "SITENAME" ,nullable = false, unique = true)
   @NaturalId(mutable = true)
   String name;

   @Basic
   @Column(name = "PREVSITENAME")
   String previousName;

   @Basic
   @Column(name = "SITEDESC")
   String description;

   @Basic
   @Column(name = "BASEURL")
   String baseUrl;

   @Basic
   @Column(name = "ROOT")
   String root;

   @Basic
   @Column(name = "IPADDRESS")
   String ipAddress;

   @Basic
   @Column(name = "PORT")
   Integer port;

   @Basic
   @Column(name = "USERID")
   String userId;

   @Basic
   @Column(name = "PASSWORD")
   String password;

   @Basic
   @Column(name = "PRIVATE_KEY")
   String privateKey;

   @Basic
   @Column(name = "STATE")
   Integer state;

   @Basic
   @Column(name = "FOLDER_ROOT")
   String folderRoot;

   @Basic
   @Column(name = "NAV_THEME")
   String navTheme;

   @Basic
   @Column(name = "GLOBALTEMPLATE")
   String globalTemplate;

   @Basic
   String allowedNamespaces;

   @Basic
   @Column(name = "UNPUBLISH_FLAGS")
   String unpublishFlags;

   @Basic
   @Column(name = "LOGIN_PAGE")
   String loginPage;

   @Basic
   @Column(name = "REGISTRATION_PAGE")
   String registrationPage;

   @Basic
   @Column(name = "REGISTRATION_CONFIRMATION_PAGE")
   String registrationConfirmationPage;

   @Basic
   @Column(name = "RESET_PAGE")
   String resetPage;

   @Basic
   @Column(name = "RESET_REQUEST_PASSWORD_PAGE")
   String resetRequestPasswordPage;

   @Basic
   @Column(name = "DEFAULT_PUBSERVERID")
   Long defaultPubServer;

   @Basic
   @Column(name = "DEFAULT_FILE_EXT",  nullable = true)
   String defaultFileExtention;

   /**
    * Determines if the site is marked as a secured site or not.
    * 'T' if is secure site; otherwise it is not a secure site.
    */
   @Column(name = "IS_SECURE", nullable = true)
   private String is_secure;

   /**
    * Determines if the site is marked to render the canonical tags or not.
    * 'T' if is canonical site; otherwise it is not set to render canonical tags.
    */
   @Column(name = "IS_CANONICAL", nullable = true)
   String is_canonical;

   /**
    * Determines canonical URL's protocol ("http" or "https").
    */
   @Basic
   @Column(name = "SITE_PROTOCOL")
   String siteProtocol;

   /**
    * Determines the site's default document (like "index.html") used when rendering canonical tags.
    */
   @Basic
   @Column(name = "DEFAULT_DOCUMENT")
   String defaultDocument;

   /**
    * Determines where canonical URL should point: "sections"(mysite.com/mysection/) or "pages"(mysite.com/mysection/index.html).
    */
   @Basic
   @Column(name = "CANONICAL_DIST")
   String canonicalDist;

   /**
    * Determines if the site is marked to replace custom canonical tags with rendered or not.
    * 'T' if is canonical replace site; otherwise it is not set replace custom canonical tags.
    */
   @Column(name = "IS_CANONICAL_REPLACE", nullable = true)
   String is_canonical_replace;

   @Basic
   @Column(name="OVERRIDE_JQUERY", nullable = true)
   String overrideJQuery;

   @Basic
   @Column(name="OVERRIDE_JQUERYUI", nullable = true)
   String overrideJQueryUI;

   @Basic
   @Column(name="OVERRIDE_FOUNDATION", nullable = true)
   String overrideFoundation;

   @Basic
   @Column(name="ENABLE_MOBILE_PREVIEW", nullable = true)
   String enableMobilePreview;

   @Basic
   @Column(name="ADDL_HEAD_CONTENT", nullable = true)
   String additionalHeadContent;

   @Basic
   @Column(name="AFTER_BODY_START", nullable = true)
   String afterBodyStartContent;

   @Basic
   @Column(name="BEFORE_BODY_CLOSE", nullable = true)
   String beforeBodyCloseContent;

   @OneToMany(targetEntity = PSSiteProperty.class, cascade =
   {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
   @JoinColumn(name = "SITEID", nullable = false, insertable = false, updatable = false)
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE,
         region = "PSSite_Property")
   @Fetch(FetchMode. SUBSELECT)
   Set<PSSiteProperty> properties = new HashSet<>();

   @ManyToMany(targetEntity = PSAssemblyTemplate.class,
    cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(name = "PSX_VARIANT_SITE", joinColumns =
   {@JoinColumn(name = "SITEID")}, inverseJoinColumns =
   {@JoinColumn(name = "VARIANTID")})
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE,
         region = "PSSite_Template")
   @Fetch(FetchMode. SUBSELECT)
   Set<IPSAssemblyTemplate> templates = new HashSet<>();


   public String getBaseUrl()
   {
      return baseUrl;
   }

   public void setBaseUrl(String baseUrl)
   {
      this.baseUrl = baseUrl;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getFolderRoot()
   {
      return folderRoot;
   }

   public void setFolderRoot(String folderRoot)
   {
      this.folderRoot = folderRoot;
   }

   public String getGlobalTemplate()
   {
      return globalTemplate;
   }

   public void setGlobalTemplate(String globalTemplate)
   {
      this.globalTemplate = globalTemplate;
   }

   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.SITE, siteId);
   }

   public String getIpAddress()
   {
      return ipAddress;
   }

   public void setIpAddress(String ipAddress)
   {
      this.ipAddress = ipAddress;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

    public String getPreviousName()
    {
        return previousName;
    }

    public void setPreviousName(String previousName)
    {
        this.previousName = previousName;
    }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getNavTheme()
    */
   public String getNavTheme()
   {
      return navTheme;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#setNavTheme(java.lang.String)
    */
   public void setNavTheme(String navTheme)
   {
      this.navTheme = navTheme;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getPassword()
    */
   public String getPassword()
   {
      if (isBlank(password))
         return password;
      else
         return decode(password);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#setPassword(java.lang.String)
    */
   public void setPassword(String password)
   {
      if (isBlank(password))
         this.password = password;
      else
         this.password = encode(password);
   }

   public String getPrivateKey()
   {
      return privateKey;
   }

   public void setPrivateKey(String privateKey)
   {
      this.privateKey = privateKey;
   }

   /**
    * {@inheritDoc}
    */
   public String getLoginPage()
   {
      return loginPage;
   }

   /**
    * {@inheritDoc}
    */
   public void setLoginPage(String loginPage)
   {
      this.loginPage = loginPage;
   }

   /**
    * {@inheritDoc}
    */
   public String getRegistrationPage()
   {
      return registrationPage;
   }

   /**
    * {@inheritDoc}
    */
   public void setRegistrationPage(String registrationPage)
   {
      this.registrationPage = registrationPage;
   }

   /**
    * {@inheritDoc}
    */
   public String getRegistrationConfirmationPage()
   {
      return registrationConfirmationPage;
   }

   /**
    * {@inheritDoc}
    */
   public void setRegistrationConfirmationPage(String registrationConfirmationPage)
   {
      this.registrationConfirmationPage = registrationConfirmationPage;
   }

   /**
    * {@inheritDoc}
    */
   public String getResetPage()
   {
      return resetPage;
   }

   /**
    * {@inheritDoc}
    */
   public void setResetPage(String resetPage)
   {
      this.resetPage = resetPage;
   }

   /**
    * {@inheritDoc}
    */
   public String getResetRequestPasswordPage()
   {
      return resetRequestPasswordPage;
   }

   /**
    * {@inheritDoc}
    */
   public void setResetRequestPasswordPage(String resetRequestPasswordPage)
   {
      this.resetRequestPasswordPage = resetRequestPasswordPage;
   }

   /**
    * {@inheritDoc}
    */
   public Long getDefaultPubServer()
   {
      return defaultPubServer;
   }

   /**
    * {@inheritDoc}
    */
   public void setDefaultPubServer(Long defPubServer)
   {
      this.defaultPubServer = defPubServer;
   }

   /**
    * {@inheritDoc}
    */
   public Integer getPort()
   {
      return port;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#setPort(java.lang.Integer)
    */
   public void setPort(Integer port)
   {
      if ( port != null && port > 0 )
         this.port = port;
   }

   /**
    * Get the site properties, not visible in public API
    * @return the site properties
    */
   public Set<PSSiteProperty> getProperties()
   {
      return properties;
   }

   /**
    * Set the site properties, not visible in public API
    * @param props the site properties, may be <code>null</code>
    */
   public void setProperties(Set<PSSiteProperty> props)
   {
      if (props == null && properties != null)
         properties.clear();
      else
      {
         for (PSSiteProperty property : props)
         {
            addProperty(property);
         }
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getRoot()
    */
   public String getRoot()
   {
      return root;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#setRoot(java.lang.String)
    */
   public void setRoot(String root)
   {
      this.root = root;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getSiteId()
    */
   public Long getSiteId()
   {
      return siteId;
   }

   /*
    * @param siteId The siteId to set.
    */
   public void setSiteId(Long siteId)
   {
      this.siteId = siteId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getState()
    */
   public Integer getState()
   {
      return state;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#setState(java.lang.Integer)
    */
   public void setState(Integer state)
   {
      this.state = state;
   }


   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getUserId()
    */
   public String getUserId()
   {
      return userId;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#setUserId(java.lang.String)
    */
   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   /**
    * @return Returns the version.
    */
   public Integer getVersion()
   {
      return version;
   }


   /**
    * @param version The version to set.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("Version can only be set once");

      this.version = version;
   }

   /**
    * @return Returns the allowedNamespaces.
    */
   public String getAllowedNamespaces()
   {
      return allowedNamespaces;
   }

   /**
    * @param allowedNamespaces The allowedNamespaces to set.
    */
   public void setAllowedNamespaces(String allowedNamespaces)
   {
      this.allowedNamespaces = allowedNamespaces;
   }

   /**
    * @param defaultFileExtention the default file extension used when creating a new page.
    */
   public void setDefaultFileExtention(String defaultFileExtention)
   {
       this.defaultFileExtention = defaultFileExtention;
   }

   /**
    * Gets the default file extension.
    *
    * @return the default file extension used when creating a new page.
    */
   public String getDefaultFileExtention()
   {
       return defaultFileExtention;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getPropertyNames(com.percussion.services.sitemgr.IPSPublishingContext)
    */
   public Set<String> getPropertyNames(IPSGuid contextId)
   {
      Set<String> rval = new HashSet<>();
      for(PSSiteProperty p : properties)
      {
         if (p.getContextId().equals(contextId))
         {
            rval.add(p.getName());
         }
      }
      return rval;
   }

   public Set<String> getPropertyNames(IPSPublishingContext context)
   {
      return getPropertyNames(context.getGUID());
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getProperty(java.lang.String, com.percussion.services.sitemgr.IPSPublishingContext)
    */
   public String getProperty(String propname, IPSGuid contextId)
   {
      for(PSSiteProperty p : properties)
      {
         if (p.getName().equals(propname) && p.getContextId().equals(contextId))
         {
            return p.getValue();
         }
      }
      return null;
   }

   public String getProperty(String propname, IPSPublishingContext context)
   {
      return getProperty(propname, context.getGUID());
   }

   /**
    * Add a single property to the Site.
    *
    * @param prop the SiteProperty to add, may be  <code>null</code>
    */

   public void addProperty(PSSiteProperty prop)
   {
      if (properties != null)
      {
         for (PSSiteProperty p : properties)
         {
            if (p.getPropertyId() == prop.getPropertyId())
            {
               p.setValue(prop.getValue());
               p.setName(prop.getName());

               //MSM deserialization fails to finish if p.getSite() is null
               if ( p.getSite() == null )
                  p.setSite(prop.getSite());
               else if ( ! p.getSite().equals(prop.getSite()))
                  p.setSite(prop.getSite());

               if ( p.getContextId() == null )
                  p.setContextId(prop.getContextId());
               else if ( !p.getContextId().equals(prop.getContextId()))
                  p.setContextId(prop.getContextId());
               return;
            }
         }
      }
      if (properties == null)
         properties = new HashSet<>();

      properties.add(prop);
   }


   /**
    * Property to remove
    * @param propname  the property name, never <code>null</code>
    */
   public void removeProperty(String propname)
   {
      if (StringUtils.isBlank(propname))
      {
         throw new IllegalArgumentException("propname may not be null or empty");
      }
      PSSiteProperty found = null;
      for (PSSiteProperty p : properties)
      {
         if (p.getName().equals(propname))
         {
            found = p;
            break;
         }
      }
      if (found != null)
      {
         properties.remove(found);
      }

   }

   /**
    * Removes a property by its id. This is not exposed through {@link IPSSite}.
    *
    * @param guid the GUID of the removed property.
    */
   public void removeProperty(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid must not be null.");

      long id = guid.longValue();
      PSSiteProperty found = null;
      for (PSSiteProperty p : properties)
      {
         if (p.getPropertyId() == id)
         {
            found = p;
            break;
         }
      }
      if (found != null)
      {
         properties.remove(found);
      }
   }

   public PSSiteProperty setProperty(String propname, IPSGuid contextId,
         String value)
   {
      PSSiteProperty prop = null;
      for(PSSiteProperty p : properties)
      {
         if (p.getName().equals(propname) && p.getContextId().equals(contextId))
         {
            prop = p;
            break;
         }
      }
      if (prop == null)
      {
         prop = new PSSiteProperty();
         prop.setPropertyId(
               PSGuidHelper.generateNextLong(PSTypeEnum.SITE_PROPERTY));
         prop.setContextId(contextId);
         prop.setName(propname);
         prop.setSite(this);
         properties.add(prop);
      }
      prop.setValue(value);

      return prop;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#removeProperty(java.lang.String, com.percussion.services.sitemgr.IPSPublishingContext)
    */
   public void removeProperty(String propname, IPSGuid contextId)
   {
      PSSiteProperty prop = null;
      for(PSSiteProperty p : properties)
      {
         if (p.getName().equals(propname) && p.getContextId().equals(contextId))
         {
            prop = p;
            break;
         }
      }
      if (prop != null)
      {
         properties.remove(prop);
      }
   }

   public PSSiteProperty setProperty(String propname,
         IPSPublishingContext context, String value)
   {
      return setProperty(propname, context.getGUID(), value);
   }

   public void removeProperty(String propname, IPSPublishingContext context)
   {
      removeProperty(propname, context.getGUID());
   }

   public String getUnpublishFlags()
   {
      return StringUtils.isBlank(unpublishFlags) ? "u" : unpublishFlags;
   }

   public void setUnpublishFlags(String flags)
   {
      if (StringUtils.isBlank(flags))
         throw new IllegalArgumentException("flags may not be null or empty.");

      unpublishFlags = flags;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSSite)) return false;
      PSSite psSite = (PSSite) o;
      return Objects.equals(name, psSite.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this).toString();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.sitemgr.IPSSite#getAssociatedTemplates()
    */
   @IPSXmlSerialization(suppress = true)
   public Set<IPSAssemblyTemplate> getAssociatedTemplates()
   {
      return templates;
   }

   /**
    * Return the GUIDS as strings, all the associated templates
    *
    * @return set of template guids as strings
    */
   public Set<String> getTemplateIds()
   {
      Set<String> ids = new HashSet<>();
      if ( templates !=  null && !templates.isEmpty() )
      {
         for (IPSAssemblyTemplate tmp : templates)
            ids.add(tmp.getGUID().toString());
      }
      return ids;
   }

   /**
    * Add the Template Guid, represented by a string to the template collection
    * @param tmpId the string form of the guid, never <code>null</code>
    */
   public void addTemplateId(String tmpId)
   {
      IPSGuid g = null;
      if ( StringUtils.isBlank(tmpId))
         throw new IllegalArgumentException("template guid may not be null");
      g = new PSGuid(tmpId);

      addTemplateId(g);
   }

   /**
    * Add the Template Guid, represented by a string to the template collection
    * @param id the ID of the Template, never <code>null</code>
    */
   public void addTemplateId(IPSGuid id)
   {
      if ( id == null)
         throw new IllegalArgumentException("template guid may not be null");

      for (IPSAssemblyTemplate t : templates)
      {
         if (t.getGUID().equals(id))
            return;
      }
      addTemplateGuidToCollection(id);
   }


   /**
    * Given a collection of String guids, sync the existing collection with
    * the new string guids. This method will add/subtract to reflect the new
    * collection of String guids
    * @param newT  Collection of String Guids. May be empty or <code>null</code>,
    * in which case the existing ids will be cleared.
    */
   @SuppressWarnings("unchecked")
   public void setTemplateIds(Set<String>newT)
   {
      if ( newT == null || newT.isEmpty())
      {
         templates.clear();
         return;
      }
      Set<IPSGuid>newTmps = new HashSet<>();
      for (String t : newT)
         newTmps.add(new PSGuid(t));

      // if the current template set is empty
      if ( templates.isEmpty() )
      {
         for (IPSGuid guid : newTmps)
         {
            addTemplateGuidToCollection(guid);
         }
         return;
      }
      // get all existing tmp guids associated with this site
      Set<IPSGuid> curTmps = new HashSet<>();
      for (IPSAssemblyTemplate t : templates)
         curTmps.add(t.getGUID());
      /**
       * 1. commonTmps = intersection of curTmps, newTmps
       * 2. removeTmps = curTmps - newTmps
       * 3. delete removeTmps from curTmps
       * 4. delete commonTmps from newTmps
       */
      Collection<IPSGuid> common = CollectionUtils.intersection(curTmps, newTmps);
      Collection<IPSGuid> remove = CollectionUtils.subtract(curTmps, newTmps);
      curTmps.removeAll(remove);
      newTmps.removeAll(common);
      curTmps.addAll(newTmps);
      templates.clear();

      for (IPSGuid guid : curTmps)
         addTemplateGuidToCollection(guid);
   }


   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      siteId = newguid.longValue();
   }

   /**
    * A util method to add a single template guid to the Site.
    *
    * @param g the template guid to add, never <code>null</code>
    */
   private void addTemplateGuidToCollection(IPSGuid g)
   {
      if ( g == null )
         throw new IllegalArgumentException("template guid may not be null");
      IPSAssemblyTemplate tmp = null;
      try
      {
          IPSAssemblyService ms_asSvc = PSAssemblyServiceLocator
          .getAssemblyService();
         tmp = ms_asSvc.loadTemplate(g, true);
      }
      catch (PSAssemblyException e)
      {
      }

      if ( tmp == null )
         return;

      if (templates.contains(tmp))
         return;

      templates.add(tmp);
   }

   /**
    * Remove a single template from the Site.
    *
    * @param t the template to remove, never <code>null</code>
    */
   public void removeTemplate(IPSAssemblyTemplate t)
   {
      if (t == null)
      {
         throw new IllegalArgumentException("template may not be null");
      }
      templates.remove(t);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }

   public boolean isSecure()
   {
      return "T".equalsIgnoreCase(is_secure);
   }

   public void setSecure(boolean isSecure)
   {
      this.is_secure = isSecure ? "T" : null;
   }

   /**
    * Determines if canonical tags should be rendered or not during the publishing.
    * @return <code>true</code> if the site is (marked) to render canonical tags.
    */
   public boolean isCanonical()
   {
      return "T".equalsIgnoreCase(is_canonical);
   }

   /**
    * Enable or disable canonical tags rendering.
    *
    * @param isCanonical <code>true</code> if enable rendering of canonical tags; otherwise
    *           disable rendering for the site.
    */
   public void setCanonical(boolean isCanonical)
   {
      this.is_canonical = isCanonical ? "T" : null;
   }

   /**
    * @param siteProtocol the URLs' protocol ("http" or "https") used when rendering canonical tags.
    */
   public void setSiteProtocol(String siteProtocol)
   {
       this.siteProtocol = siteProtocol;
   }

   /**
    * Gets the canonical URLs' protocol ("http" or "https").
    *
    * @return the URLs' protocol ("http" or "https") used when rendering canonical tags.
    */
   public String getSiteProtocol()
   {
       return siteProtocol;
   }

   /**
    * @param defaultDocument the site's default document (like "index.html") used when rendering canonical tags.
    */
   public void setDefaultDocument(String defaultDocument)
   {
       this.defaultDocument = defaultDocument;
   }

   /**
    * Gets the site's default document (like "index.html").
    *
    * @return the site's default document (like "index.html") used when rendering canonical tags.
    */
   public String getDefaultDocument()
   {
       return defaultDocument;
   }

   /**
    * @param canonicalDist the URLs' destination ("sections" or "pages") used when rendering canonical tags.
    */
   public void setCanonicalDist(String canonicalDist)
   {
       this.canonicalDist = canonicalDist;
   }

   /**
    * Gets the canonical URLs' destination ("sections" or "pages").
    *
    * @return the URLs' destination ("sections" or "pages") used when rendering canonical tags.
    */
   public String getCanonicalDist()
   {
       return canonicalDist;
   }

   /**
    * Determines if custom (existing) canonical tags should be replaced with rendered ones or not during the publishing.
    * @return <code>true</code> if the site is (marked) to replace custom canonical tags.
    */
   public boolean isCanonicalReplace()
   {
      return "T".equalsIgnoreCase(is_canonical_replace);
   }

   /**
    * Enable or disable replacing custom canonical tags with rendered.
    *
    * @param isCanonicalReplace <code>true</code> if enable replacing of custom canonical tags with rendered; otherwise
    *           disable replacing for the site.
    */
   public void setCanonicalReplace(boolean isCanonicalReplace)
   {
      this.is_canonical_replace = isCanonicalReplace ? "T" : null;
   }

    @Override
    public boolean isOverrideSystemJQuery() {
        return "T".equalsIgnoreCase(overrideJQuery);
    }

    @Override
    public void setOverrideSystemJQuery(boolean overrideSystemJQuery) {
         this.overrideJQuery = overrideSystemJQuery ? "T" : null;
    }

    @Override
    public boolean isOverrideSystemFoundation() {
        return "T".equalsIgnoreCase(this.overrideFoundation);
    }

    @Override
    public void setOverrideSystemFoundation(boolean overrideSystemFoundation) {
      this.overrideFoundation = overrideSystemFoundation ? "T" : null;
    }

    @Override
    public boolean isOverrideSystemJQueryUI() {
      return  "T".equalsIgnoreCase(this.overrideJQueryUI);
    }

    @Override
    public void setOverrideSystemJQueryUI(boolean overrideSystemJQueryUI) {
      this.overrideJQueryUI = overrideSystemJQueryUI ? "T" : null;
    }

    @Override
    public boolean isMobilePreviewEnabled() {
        return "T".equalsIgnoreCase(this.enableMobilePreview);
    }

    @Override
    public void setMobilePreviewEnabled(boolean mobilePreviewEnabled) {
         this.enableMobilePreview = mobilePreviewEnabled ? "T" : null;
    }

    @Override
    public String getSiteAdditionalHeadContent() {
        return this.additionalHeadContent;
    }

    @Override
    public void setSiteAdditionalHeadContent(String siteAdditionalHeadContent) {
      this.additionalHeadContent = siteAdditionalHeadContent;
    }

    @Override
    public String getSiteBeforeBodyCloseContent() {
        return this.beforeBodyCloseContent;
    }

    @Override
    public void setSiteBeforeBodyCloseContent(String siteBeforeBodyCloseContent) {
      this.beforeBodyCloseContent = siteBeforeBodyCloseContent;
    }

    @Override
    public String getSiteAfterBodyOpenContent() {
        return this.afterBodyStartContent;
    }

    @Override
    public void setSiteAfterBodyOpenContent(String siteAfterBodyOpenContent) {
      this.afterBodyStartContent = siteAfterBodyOpenContent;
    }

    /**
    * This method does the following:
    *  1. creates a XML document
    *  2. from the document, extract the template ids and build a list of guids
    *  3. returns a set of guids again for the templates
    * @param siteStr the original site as a XML string representation from which
    * the templates are extracted, never <code>null</code> or empty
    * @return the templates as a guid collection may be empty,
    * never <code>null</code>
    * @throws IOException if an I/O error occurs
    * @throws SAXException if a parsing error occurs
    */
   public static Set<IPSGuid> getTemplateIdsFromSite(String siteStr)
         throws IOException, SAXException
   {
      if (StringUtils.isBlank(siteStr))
         throw new IllegalArgumentException("siteStr may not be null or empty");

      Set<IPSGuid> tmpGuids = new HashSet<>();
      Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
            siteStr), false);
      Element root = doc.getDocumentElement();
      NodeList nTmpList = root.getElementsByTagName(XML_TEMPLATEIDS_NAME);
      if (nTmpList != null && nTmpList.getLength() > 0)
      {
         Element tmpList = (Element) nTmpList.item(0);
         NodeList nl = tmpList.getElementsByTagName(XML_TEMPLATEID_NAME);
         for (int i = 0; (nl != null) && (i < nl.getLength()); i++)
         {
            Element tmpId = (Element) nl.item(i);
            String tmp = PSXMLDomUtil.getElementData(tmpId);
            if (StringUtils.isBlank(tmp))
               continue;
            IPSGuid g = new PSGuid(PSTypeEnum.TEMPLATE, tmp);
            tmpGuids.add(g);
         }
      }
      return tmpGuids;
   }

   /**
    * This method is specifically used by MSM to replace the template ids from
    * the serialized data with the new ids back into the serialized data
    * @param siteStr the original site as XML string representation,
    * never <code>null</code> or empty*-
    * @param newTmps the list of new templates that need to be added may
    * not be <code>null</code>, may or may not be empty
    * @return the original site with replaced template GUIDS as a an XML string
    * representation
    * @throws IOException if an I/O error occurs
    * @throws SAXException if a parsing error occurs
    */
   public static String replaceTemplateIdsFromSite(String siteStr,
         Set<IPSGuid> newTmps) throws IOException, SAXException
   {
      if (StringUtils.isBlank(siteStr))
         throw new IllegalArgumentException("siteStr may not be null or empty");
      if (newTmps == null)
         throw new IllegalArgumentException("template list may not be null");

      Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
            siteStr), false);
      Element root = doc.getDocumentElement();
      NodeList tmpIdsElem = root.getElementsByTagName(XML_TEMPLATEIDS_NAME);
      Element oldTmpList = (Element)tmpIdsElem.item(0);
      Element newTmpList = doc.createElement(XML_TEMPLATEIDS_NAME);
      for (IPSGuid g : newTmps)
      {
         PSXmlDocumentBuilder.addElement(doc, newTmpList, XML_TEMPLATEID_NAME,
               g.toString());
      }
      oldTmpList.getParentNode().replaceChild(newTmpList, oldTmpList);
      return PSXmlDocumentBuilder.toString(doc);
   }

   /**
    * Node name for the templateids list representation.
    */
   private static String XML_TEMPLATEIDS_NAME  = "template-ids";

   /**
    * Node name for the templateid that is a child of templateids list.
    */
   private static String XML_TEMPLATEID_NAME   = "template-id";

}
