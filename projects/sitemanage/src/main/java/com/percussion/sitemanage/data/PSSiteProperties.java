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
package com.percussion.sitemanage.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.share.data.PSAbstractPersistantObject;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * It contains the modifiable properties for a particular site.
 *
 * @author yubingchen
 */
@XmlRootElement(name = "SiteProperties")
public class PSSiteProperties extends PSAbstractPersistantObject
{

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;

    /**
     * Gets the site ID.
     * 
     * @return the site ID, not blank for a valid site.
     */
    @XmlElement
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Sets the site ID.
     * 
     * @param id the new ID of the site, not blank for a valid site.
     */
    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Gets the site name.
     * 
     * @return the site name, not blank for a valid site.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Sets site name.
     * 
     * @param name the new site name, not blank for a valid site.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Gets the description of the site.
     * 
     * @return the description, may be blank.
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * Sets the description of the site.
     * 
     * @param desc the new description, may be blank.
     */
    public void setDescription(String desc)
    {
        description = desc;
    }
    
    /**
     * Gets the home page's link text of the site.
     *  
     * @return the link text of the home page, not blank for a valid site.
     */
    public String getHomePageLinkText()
    {
        return homePageLinkText;
    }
    
    /**
     * Sets the home page's link text.
     * 
     * @param linkTitle the new link text, not blank for a valid site.
     */
    public void setHomePageLinkText(String linkTitle)
    {
        this.homePageLinkText = linkTitle;
    }

    /**
     * Gets the permission of the site root folder.
     * 
     * @return the folder permission, not <code>null</code> for a valid site.
     */
    public PSFolderPermission getFolderPermission()
    {
        return folderPermission;
    }
    
    /**
     * Sets the permission of the site root folder.
     * 
     * @param permission the new permission, not <code>null</code> for a valid site.
     */
    public void setFolderPermission(PSFolderPermission permission)
    {
        folderPermission = permission;
    }

   /**
    * The relative path to the sitewide loggin page information.
    * For now this is used by Login widget, and protected region feature in templates
    * Eg: /index
    * 
    * @author federicoromanelli
    * @return the path to the sitewide login page
    */
    public String getLoginPage()
    {
       return loginPage;
    }

   /**
    * The relative path to the sitewide loggin page information.
    * For now this is used by Login widget, and protected region feature in templates
    * Eg: /index
    *
    * @author federicoromanelli
    * @param loginPage - the path to the sitewide login page
    */
    public void setLoginPage(String loginPage)
    {
       this.loginPage = loginPage;
    }
   
   /**
    * The relative path to the sitewide loggin error page.
    * For now this is used by Login widget, and protected region feature in templates.
    * When user is not able to loggin with the login widget, and this error page is set then he's
    * redirected to this page.
    * Eg: /errorpage.html
    * 
    * @author federicoromanelli
    * @return the path to the sitewide login error page
    */
    public String getLoginErrorPage()
    {
       return loginErrorPage;
    }
   
   /**
    * The relative path to the sitewide loggin error page.
    * For now this is used by Login widget, and protected region feature in templates.
    * When user is not able to loggin with the login widget, and this error page is set then he's
    * redirected to this page.
    * Eg: /errorpage.html
    * 
    * @author federicoromanelli
    * @param loginErrorPage - the path to the sitewide login error page
    */
    public void setLoginErrorPage(String loginErrorPage)
    {
       this.loginErrorPage = loginErrorPage;
    }

    /**
     * @return <code>true<code> if the site is secure. <code>false<code> otherwise.
     */
    @XmlElement(name="isSecure")
    public boolean isSecure()
    {
        return isSecure;
    }
    
    public void setSecure(boolean is_secure)
    {
        this.isSecure = is_secure;
    }
    
    /**
     * The relative path to the sitewide registration page information.
     * For now this is used by Registration widget, and protected region feature in templates
     * Eg: /index
     * 
     * @author rafaelsalis
     * @return the path to the sitewide registration page
     */
    public String getRegistrationPage()
    {
        return registrationPage;
    }

    /**
     * The relative path to the sitewide registration page information.
     * For now this is used by Registration widget, and protected region feature in templates
     * Eg: /index
     *
     * @author rafaelsalis
     * @param registration - the path to the sitewide login page
     */
    public void setRegistrationPage(String registrationPage)
    {
       this.registrationPage = registrationPage;
    }
    
    /**
     * The relative path to the sitewide registration confirmation page.
     * Used by Registration widget.
     * Eg: /registration/registration_confirmation.html
     *
     * @author jshirai
     * @param registrationConfirmationPage - the path to the sitewide registration confirmation page
     */
    public String getRegistrationConfirmationPage()
    {
        return registrationConfirmationPage;
    }

    /**
     * The relative path to the sitewide registration confirmation page.
     * Used by Registration widget.
     * Eg: /registration/registration_confirmation.html
     *
     * @author jshirai
     * @param registrationConfirmationPage - the path to the sitewide registration confirmation page
     */
    public void setRegistrationConfirmationPage(String registrationConfirmationPage)
    {
       this.registrationConfirmationPage = registrationConfirmationPage;
    }
     
    /**
     * The relative path to the sitewide reset page information. For now this is
     * used by Registration widget, and protected region feature in templates
     * Eg: /index
     * 
     * @author rafaelsalis
     * @return the path to the sitewide reset page
     */
    public String getResetPage()
    {
        return resetPage;
    }

    /**
     * The relative path to the sitewide reset page information. For now this is
     * used by Registration widget, and protected region feature in templates
     * Eg: /index
     * 
     * @author rafaelsalis
     * @param resetPage - the path to the sitewide registration page
     */
    public void setResetPage(String resetPage)
    {
        this.resetPage = resetPage;
    }

    /**
     * The relative path to the sitewide reset request password page
     * information. For now this is used by Login widget, and protected region
     * feature in templates Eg: /index
     * 
     * @author rafaelsalis
     * @return the path to the sitewide reset request password page
     */
    public String getResetRequestPasswordPage()
    {
        return resetRequestPasswordPage;
    }

    /**
     * The relative path to the sitewide reset request password page
     * information. For now this is used by Login widget, and protected region
     * feature in templates Eg: /index
     * 
     * @author rafaelsalis
     * @param resetRequestPasswordPage - the path to the sitewide reset request
     *            password page
     */
    public void setResetRequestPasswordPage(String resetRequestPasswordPage)
    {
        this.resetRequestPasswordPage = resetRequestPasswordPage;
    }
    
    /**
     * @param cssClassNames the class names used with navigation widget.
     */
    public void setCssClassNames(String cssClassNames)
    {
        this.cssClassNames = cssClassNames;
    }

    /**
     * Gets the css class names of the section folder.
     * 
     * @return the css class names used with navigation widget.
     */
    public String getCssClassNames()
    {
        return cssClassNames;
    }

    /**
     * @param the default file extension used when creating a new page.
     */
    @XmlElement(name="defaultFileExtention")
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

    /**
    * Determines if canonical tags should be rendered or not during the publishing.
    * @return <code>true<code> if the site is (marked) to render canonical tags. <code>false<code> otherwise.
     */
    @XmlElement(name="isCanonical")
    public boolean isCanonical()
    {
        return isCanonical;
    }
    
    /**
     * Enable or disable canonical tags rendering.
     * 
     * @param setCanonical <code>true</code> if enable rendering of canonical tags; otherwise
     *           disable rendering for the site.
     */
    public void setCanonical(boolean is_canonical)
    {
        this.isCanonical = is_canonical;
    }
    
    /**
     * @param the URLs' protocol ("http" or "https") used when rendering canonical tags.
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
     * @param the site's default document (like "index.html") used when rendering canonical tags.
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
     * @param the URLs' destination ("sections" or "pages") used when rendering canonical tags.
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
     * @return <code>true<code> if the site is (marked) to replace custom canonical tags. <code>false<code> otherwise.
     */
    @XmlElement(name="isCanonicalReplace")
    public boolean isCanonicalReplace()
    {
        return isCanonicalReplace;
    }
    
    /**
     * Enable or disable replacing custom canonical tags with rendered.
     * 
     * @param setCanonical <code>true</code> if enable replacing of custom canonical tags with rendered; otherwise
     *           disable replacing for the site.
     */
    public void setCanonicalReplace(boolean is_canonical_replace)
    {
        this.isCanonicalReplace = is_canonical_replace;
    }
    
    /**
     * Determine if pubservers were changed as part of a save operation
     * 
     * @return <code>true</code> if changed, <code>false</code> otherwise
     */
    public boolean isPubServersChanged()
    {
        return isPubServerChanged;
    }

    /**
     * See {@link #didPubServersChanged()}
     * @param isPubServerChanged
     */
    public void setPubServersChanged(boolean isPubServerChanged)
    {
        this.isPubServerChanged = isPubServerChanged;
    }

    public boolean isOverrideSystemJQuery() {
        return overrideSystemJQuery;
    }

    public void setOverrideSystemJQuery(boolean overrideSystemJQuery) {
        this.overrideSystemJQuery = overrideSystemJQuery;
    }

    public boolean isOverrideSystemFoundation() {
        return overrideSystemFoundation;
    }

    public void setOverrideSystemFoundation(boolean overrideSystemFoundation) {
        this.overrideSystemFoundation = overrideSystemFoundation;
    }

    public boolean isOverrideSystemJQueryUI() {
        return overrideSystemJQueryUI;
    }

    public void setOverrideSystemJQueryUI(boolean overrideSystemJQueryUI) {
        this.overrideSystemJQueryUI = overrideSystemJQueryUI;
    }

    public boolean isMobilePreviewEnabled() {
        return mobilePreviewEnabled;
    }

    public void setMobilePreviewEnabled(boolean mobilePreviewEnabled) {
        this.mobilePreviewEnabled = mobilePreviewEnabled;
    }

    public String getSiteAdditionalHeadContent() {
        return siteAdditionalHeadContent;
    }

    public void setSiteAdditionalHeadContent(String siteAdditionalHeadContent) {
        this.siteAdditionalHeadContent = siteAdditionalHeadContent;
    }

    public String getSiteBeforeBodyCloseContent() {
        return siteBeforeBodyCloseContent;
    }

    public void setSiteBeforeBodyCloseContent(String siteBeforeBodyCloseContent) {
        this.siteBeforeBodyCloseContent = siteBeforeBodyCloseContent;
    }

    public String getSiteAfterBodyOpenContent() {
        return siteAfterBodyOpenContent;
    }

    public void setSiteAfterBodyOpenContent(String siteAfterBodyOpenContent) {
        this.siteAfterBodyOpenContent = siteAfterBodyOpenContent;
    }

    /**
     * The relative path to the sitewide loggin page information.
     */
    private String loginPage;

    /**
     * The relative path to the sitewide loggin error page.
     */
    private String loginErrorPage;

    /**
     * The relative path to the sitewide registration page.
     */
    private String registrationPage;

    /**
     * The relative path to the sitewide registration confirmation page.
     */
    private String registrationConfirmationPage;

    /**
     * The relative path to the sitewide reset page.
     */
    private String resetPage;

    /**
     * The relative path to the sitewide reset request password page.
     */
    private String resetRequestPasswordPage;

    /**
     * See {@link #getFolderPermission()} for detail.
     */
    private PSFolderPermission folderPermission;

    /**
     * See {@link #getHomePageLinkText()} for detail.
     */
    private String homePageLinkText;

    /**
     * See {@link #getDescription()} for detail.
     */
    private String description;

    /**
     * See {@link #getName()} for detail.
     */
    private String name;

    /**
     * See {@link #getId()} for detail
     */
    private String id;

    /**
     * See {@link #isSecure()} for detail
     */
    private boolean isSecure;

    /**
     * Field to save the css class names used when rendering navigation widgets.
     */
    private String cssClassNames;
    
    /**
     * Field to save the default file extension used when creating a new page.
     */
    private String defaultFileExtention;
    
    /**
     * Determines if the site is marked to render the canonical tags or not.
     */
    private boolean isCanonical;

    /**
     * Determines canonical URL's protocol ("http" or "https").  
     */
    String siteProtocol;
    
    /**
     * Determines the site's default document (like "index.html") used when rendering canonical tags.  
     */
    String defaultDocument;
    
    /**
     * Determines where canonical URL should point: "sections"(mysite.com/mysection/) or "pages"(mysite.com/mysection/index.html).
     */
    private String canonicalDist;
    
    /**
     * Determines if the site is marked to replace custom canonical tags with rendered or not.
     */
    private boolean isCanonicalReplace;

    /**
     * Transient flag to indicate if pubserver was modified during a save operation
     */
    private transient boolean isPubServerChanged;

    /***
     * Indicates that the system JQuery version should not be injected into any pages or Templates.
     */
    private boolean overrideSystemJQuery;

    /***
     * Indicates the the system Foundation version should not be injected into any Pages or Templates
     */
    private boolean overrideSystemFoundation;

    /***
     * Indicates that the system JQueryUI version should not be injected into Templates
     */
    private boolean overrideSystemJQueryUI;

    /***
     * Indicates if the mobile preview control is rendered on preview.
     */
    private boolean mobilePreviewEnabled;

    /***
     * Indicates head content that is global to all templates and pages on a site.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String siteAdditionalHeadContent;

    /***
     * Indicates Before Body close content that is globally injected into all Pages on a site.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String siteBeforeBodyCloseContent;

    /***
     * Indicates After Body Open content that is globally injected into all Pages on a site.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String siteAfterBodyOpenContent;
}
