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

package com.percussion.services.sitemgr;


import java.util.Set;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.sitemgr.data.PSSiteProperty;
import com.percussion.utils.guid.IPSGuid;

/**
 * Represents a publishing site. Sites are primarily cataloged through
 * the site manager in rhino. The site manager performs all CRUD operations
 * on site objects.
 * 
 * @author dougrand
 */
public interface IPSSite extends IPSCatalogSummary
{
   /**
    * The site base url, may be <code>null</code> or empty.
    * 
    * @return Returns the baseUrl.
    */
   String getBaseUrl();

   /**
    * @param baseUrl The baseUrl to set, may be <code>null</code> or empty
    */
   void setBaseUrl(String baseUrl);

   /**
    * The site description, may be <code>null</code> or empty.
    * 
    * @return Returns the description.
    */
   String getDescription();

   /**
    * @param description The description to set, may be <code>null</code> or
    *           empty
    */
   void setDescription(String description);

   /**
    * Get the site's root folder, which is the folder path in the repository for
    * this site, may be <code>null</code> or empty
    * 
    * @return Returns the folderRoot.
    */
   String getFolderRoot();

   /**
    * @param folderRoot The folderRoot to set, may be <code>null</code> or
    *           empty.
    */
   void setFolderRoot(String folderRoot);

   /**
    * Get the global template name for the site. If the site is using velocity
    * this will be interpreted as the template name, for old style assemblers
    * this will be used as is for the xsl file name. It is possible to then have
    * both a template and a file and support both - although it may look a
    * little silly to register a velocity based template with foo.xsl as the
    * name.
    * 
    * @return Returns the globalTemplate, may be <code>null</code> or empty
    */
   String getGlobalTemplate();

   /**
    * @param globalTemplate The globalTemplate to set, may be <code>null</code>
    *           or empty
    */
   void setGlobalTemplate(String globalTemplate);

   /**
    * Get the site's IP address.
    * 
    * @return Returns the ipAddress, may be <code>null</code> or empty
    */
   String getIpAddress();

   /**
    * @param ipAddress The ipAddress to set, may be <code>null</code> or empty
    */
   void setIpAddress(String ipAddress);

   /**
    * Get the site name
    * 
    * @return Returns the name, may be <code>null</code> or empty
    */
   String getName();

   /**
    * @param name The name to set, may be <code>null</code> or empty
    */
   void setName(String name);

    /**
     * @param name The previous name of the site to set, may be <code>null</code> or empty
     */
    void setPreviousName(String previousName);

    /**
     * Get the previous site name. Used after a site is renamed.
     *
     * @return Returns the name, may be <code>null</code> or empty
     */
    String getPreviousName();

   /**
    * Get the un-publishing flags. It is one or more characters corresponding to 
    * content valid flags used in the workflow, multiples should be separated 
    * by commas.
    * 
    * @return the flags described above, never <code>null</code>. It defaults
    * to 'u' if not specified.
    */
   String getUnpublishFlags();

   /**
    * Sets the un-publishing flags.
    * 
    * @param flags the new flags, never <code>null</code> or empty. It is one 
    * or more characters corresponding to content valid flags used in the 
    * workflow, multiples should be separated by commas.
    * 
    * @see #getUnpublishFlags()
    */
   void setUnpublishFlags(String flags);
   
   /**
    * Get the managed nav theme for the site
    * 
    * @return Returns the navTheme, may be <code>null</code> or empty
    */
   String getNavTheme();

   /**
    * @param navTheme The navTheme to set, may be <code>null</code> or empty
    */
   void setNavTheme(String navTheme);

   /**
    * Get the FTP password
    * 
    * @return Returns the password, may be <code>null</code> or empty
    */
   String getPassword();

   /**
    * The name of the file where the private key for sFtp publishing will be
    * stored. This is used for SFTP login only, not for FTP. The file will be
    * stored under the <installRoot>/rxconfig/ssh-keys folder. This field only
    * represents the name of the file, not the complete path.
    * 
    * @return returns the private key file name, may be <code>null</code> or
    *         empty.
    */
   String getPrivateKey();
   
   /**
    * Sets the name of the file that contains the private key for the given
    * site. It is not the full path, just the file name.
    * 
    * @param privateKey the file name, it may be <code>null</code> or empty.
    */
   void setPrivateKey(String privateKey);
   
   /**
    * @param password The password to set, may be <code>null</code> or empty
    */
   void setPassword(String password);

   /**
    * Get the site port
    * 
    * @return Returns the port, may be <code>null</code>
    */
   Integer getPort();

   /**
    * @param port The port to set, may be <code>null</code>
    */
   void setPort(Integer port);

   /**
    * Get property names defined for the given context
    * 
    * @param context the context, never <code>null</code>
    * @return a set of names defined in that context, never <code>null</code>
    *         but could be empty
    * @deprecated use {@link #getPropertyNames(IPSGuid)} instead
    */
   Set<String> getPropertyNames(IPSPublishingContext context);

   /**
    * Get property names defined for the given context
    * 
    * @param contextId the context ID, never <code>null</code>
    * @return a set of names defined in that context, never <code>null</code>
    *         but could be empty
    */
   Set<String> getPropertyNames(IPSGuid contextId);

   /**
    * Get the named property for the given context
    * 
    * @param name the property name, never <code>null</code> or empty
    * @param context the context, never <code>null</code>
    * @return the value, could be <code>null</code> if the property isn't
    *         defined in the given context.
    * @deprecated use {@link #getProperty(String, IPSGuid)} instead
    */
   String getProperty(String name, IPSPublishingContext context);

   /**
    * Get the named property for the given context
    * 
    * @param name the property name, never <code>null</code> or empty
    * @param contextId the context ID, never <code>null</code>
    * @return the value, could be <code>null</code> if the property isn't
    *         defined in the given context.
    */
   String getProperty(String name, IPSGuid contextId);

   /**
    * Set the named property for the given context
    * 
    * @param name the property name, never <code>null</code> or empty
    * @param contextId the context ID, never <code>null</code>
    * @param value the value, never <code>null</code> or empty
    * 
    * @return the added property, never <code>null</code>.
    */
   PSSiteProperty setProperty(String name, IPSGuid contextId,
         String value);
   
   /**
    * Set the named property for the given context
    * 
    * @param name the property name, never <code>null</code> or empty
    * @param context the context, never <code>null</code>
    * @param value the value, never <code>null</code> or empty
    * 
    * @return the added property, never <code>null</code>.
    * 
    * @deprecated use {@link #setProperty(String, IPSGuid, String)} instead.
    */
   PSSiteProperty setProperty(String name,
         IPSPublishingContext context, String value);
   
  /**
    * Remove the named property for the given context
    * 
    * @param name the property name, never <code>null</code> or empty
    * @param context the context, never <code>null</code>
    */
   void removeProperty(String name, IPSGuid context);

   /**
    * Remove the named propery for the given context
    * 
    * @param name the property name, never <code>null</code> or empty
    * @param context the context, never <code>null</code>
    * 
    * @deprecated use {@link #removeProperty(String, IPSGuid)} instead.
    */
   void removeProperty(String name,
         IPSPublishingContext context);

   /**
    * Remove the named property for all contexts
    * 
    * @param name the property name, never <code>null</code> or empty.
    */
   void removeProperty(String name);
   
   /**
    * Get the root for the site, which forms the base of the publishing path,
    * only used for publishing to a file system.
    * 
    * @return Returns the root, may be <code>null</code> or empty
    */
   String getRoot();

   /**
    * @param root The root to set, may be <code>null</code> or empty
    */
   void setRoot(String root);

   /**
    * @return Returns the siteId.
    */
   Long getSiteId();

   /**
    * Get the site's state
    * @return Returns the state, may be <code>null</code>
    * @deprecated this is not used any more.
    */
   Integer getState();

   /**
    * @param state The state to set, may be <code>null</code>
    * @deprecated this is not used any more.
    */
   void setState(Integer state);

   /**
    * Get the FTP user id
    * 
    * @return Returns the userId, may be <code>null</code> or empty
    */
   String getUserId();

   /**
    * @param userId The userId to set, may be <code>null</code> or empty
    */
   void setUserId(String userId);

   /**
    * Get the unique id for the site. Legacy sites will not have a truly unique
    * id.
    * @return the guid, never <code>null</code>
    */
   IPSGuid getGUID();
   
   /**
    * The allowed namespaces is a comma separated list of namespaces that
    * can be used in this site. The list is intersected with the configured
    * namespaces for the Rhythmyx server. If a namespace is not configured,
    * then including the prefix here will have no affect. 
    * 
    * @return Returns the allowedNamespaces, may be <code>null</code> or empty
    */
   String getAllowedNamespaces();

   /**
    * @param allowedNamespaces a comma separated list to set, may be 
    * <code>null</code> or empty
    */
   void setAllowedNamespaces(String allowedNamespaces);

   /**
    * Get the modifiable set of associated templates for this site.
    * @return a set of associated templates, never <code>null</code>
    */
   Set<IPSAssemblyTemplate> getAssociatedTemplates();
   
   /**
    * Copy the all properties from the given site except the ID and the
    * version of the site.
    * @param site the source site, may not be <code>null</code>.
    */
   void copy(IPSSite site);
   
   /**
    * Determines if the site is secured  or not.
    * @return <code>true</code> if the site is (marked) secured.
    */
   boolean isSecure();

   /**
    * Enable or disable secure of the site.
    * 
    * @param isSecure <code>true</code> if enable secure the site; otherwise
    *           disable secure the site.
    */
   void setSecure(boolean isSecure);

   /**
    * The relative path to the sitewide loggin page information.
    * For now this is used by Login widget, and protected region feature in templates
    * Eg: /index
    * 
    * @author federicoromanelli
    * @return the path to the sitewide login page
    */
   String getLoginPage();
   
   /**
    * The relative path to the sitewide loggin page information.
    * For now this is used by Login widget, and protected region feature in templates
    * Eg: /index
    *
    * @author federicoromanelli
    * @param loginPage - the path to the sitewide login page
    */
   void setLoginPage(String loginPage);
   
   /**
    * The relative path to the sitewide registration page information.
    * For now this is used by Registration widget, and protected region feature in templates
    * Eg: /index
    * 
    * @author rafaelsalis
    * @return the path to the sitewide registration page
    */
   String getRegistrationPage();

   /**
    * The relative path to the sitewide registration page information.
    * For now this is used by Registration widget, and protected region feature in templates
    * Eg: /index
    *
    * @author rafaelsalis
    * @param registrationPage - the path to the sitewide registration page
    */
   void setRegistrationPage(String registrationPage);
   
   /**
    * The relative path to the sitewide registration confirmation page.
    * Used by Registration widget.
    * Eg: /registration/registration_confirmation.html
    * 
    * @author jshirai
    * @return the path to the sitewide registration confirmation page
    */
   String getRegistrationConfirmationPage();

   /**
    * The relative path to the sitewide registration confirmation page.
    * Used by Registration widget.
    * Eg: /registration/registration_confirmation.html
    *
    * @author jshirai
    * @param registrationConfirmationPage - the path to the sitewide registration confirmation page
    */
   void setRegistrationConfirmationPage(String registrationConfirmationPage);

   /**
    * The relative path to the sitewide reset page information.
    * For now this is used by Registration widget, and protected region feature in templates
    * Eg: /index
    * 
    * @author rafaelsalis
    * @return the path to the sitewide reset page
    */
   String getResetPage();

   /**
    * The relative path to the sitewide reset page information.
    * For now this is used by Registration widget, and protected region feature in templates
    * Eg: /index
    *
    * @author rafaelsalis
    * @param resetPage - the path to the sitewide reset page
    */
   void setResetPage(String resetPage);
   
   /**
    * The relative path to the sitewide reset request password page information.
    * For now this is used by Login widget, and protected region feature in templates
    * Eg: /index
    * 
    * @author rafaelsalis
    * @return the path to the sitewide reset request password page
    */
   String getResetRequestPasswordPage();

   /**
    * The relative path to the sitewide reset request password page information.
    * For now this is used by Login widget, and protected region feature in templates
    * Eg: /index
    *
    * @author rafaelsalis
    * @param resetRequestPasswordPage - the path to the sitewide reset page
    */
   void setResetRequestPasswordPage(String resetRequestPasswordPage);
   
   /**
    * @return the default server where the site is published.
    */
   Long getDefaultPubServer();
   
   /**
    * @param defaultPubServer The default server where the site is published.
    */
   void setDefaultPubServer(Long defaultPubServer);
   
   /**
    * @param the default file extension used when creating a new page.
    */
   public void setDefaultFileExtention(String defaultFileExtention);
   
   /**
    * Gets the default file extension.
    * 
    * @return the default file extension used when creating a new page.
    */
   public String getDefaultFileExtention();
   
   /**
    * Determines if canonical tags should be rendered or not during the publishing.
    * @return <code>true</code> if the site is (marked) to render canonical tags.
    */
   public boolean isCanonical();
   
   /**
    * Enable or disable canonical tags rendering.
    * 
    * @param setCanonical <code>true</code> if enable rendering of canonical tags; otherwise
    *           disable rendering for the site.
    */
   public void setCanonical(boolean isCanonical);

   /**
    * @param the URLs' protocol ("http" or "https") used when rendering canonical tags.
    */
   public void setSiteProtocol(String siteProtocol);

   /**
    * Gets the canonical URLs' protocol ("http" or "https").
    * 
    * @return the URLs' protocol ("http" or "https") used when rendering canonical tags.
    */
   public String getSiteProtocol();

   /**
    * @param the site's default document (like "index.html") used when rendering canonical tags.
    */
   public void setDefaultDocument(String defaultDocument);

   /**
    * Gets the site's default document (like "index.html").
    * 
    * @return the site's default document (like "index.html") used when rendering canonical tags.
    */
   public String getDefaultDocument();
   
   /**
    * @param the URLs' destination ("sections" or "pages") used when rendering canonical tags.
    */
   public void setCanonicalDist(String canonicalDist);

   /**
    * Gets the canonical URLs' destination ("sections" or "pages").
    * 
    * @return the URLs' destination ("sections" or "pages") used when rendering canonical tags.
    */
   public String getCanonicalDist();

   /**
    * Determines if custom (existing) canonical tags should be replaced with rendered ones or not during the publishing.
    * @return <code>true</code> if the site is (marked) to replace custom canonical tags.
    */
   public boolean isCanonicalReplace();
   
   /**
    * Enable or disable replacing custom canonical tags with rendered.
    * 
    * @param setCanonical <code>true</code> if enable replacing of custom canonical tags with rendered; otherwise
    *           disable replacing for the site.
    */
   public void setCanonicalReplace(boolean isCanonicalReplace);

    public boolean isOverrideSystemJQuery();

    public void setOverrideSystemJQuery(boolean overrideSystemJQuery) ;

    public boolean isOverrideSystemFoundation() ;

    public void setOverrideSystemFoundation(boolean overrideSystemFoundation) ;

    public boolean isOverrideSystemJQueryUI();

    public void setOverrideSystemJQueryUI(boolean overrideSystemJQueryUI) ;

    public boolean isMobilePreviewEnabled();

    public void setMobilePreviewEnabled(boolean mobilePreviewEnabled);

    public String getSiteAdditionalHeadContent() ;

    public void setSiteAdditionalHeadContent(String siteAdditionalHeadContent) ;

    public String getSiteBeforeBodyCloseContent() ;

    public void setSiteBeforeBodyCloseContent(String siteBeforeBodyCloseContent) ;

    public String getSiteAfterBodyOpenContent() ;

    public void setSiteAfterBodyOpenContent(String siteAfterBodyOpenContent) ;

   
}
