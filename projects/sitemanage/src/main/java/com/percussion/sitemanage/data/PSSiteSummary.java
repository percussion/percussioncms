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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSDataItemSummary;
import com.percussion.share.data.PSDataItemSummarySingleFolderPath;
import com.percussion.sitemanage.service.IPSSiteDataService;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.DEFAULT)
public class PSSiteSummary extends PSDataItemSummarySingleFolderPath implements IPSItemSummary, IPSFolderPath
{

    /***
     * Default constructor.
     */
    public PSSiteSummary(){
        super();
    }

    @NotBlank
    @NotNull
    private String name;
    
    private String description;
    
    private String baseUrl;
    
    private String defaultFileExtention;
    
    private boolean isCanonical = true;

    private boolean overrideSystemJQuery = false;

    private boolean overrideSystemFoundation = false;

    private boolean overrideSystemJQueryUI = false;

    private String siteAdditionalHeadContent;

    private String siteBeforeBodyCloseContent;

    private String siteAfterBodyOpenContent;

    /**
     * Determines canonical URL's protocol ("http" or "https").  
     */
    private String siteProtocol = "https";
    
    /**
     * Determines the site's default document (like "index.html") used when rendering canonical tags.  
     */
    private String defaultDocument = "index.html";
    
    private String canonicalDist = "pages";
    
    private boolean isCanonicalReplace = true;

    private PSPubInfo pubInfo;
    
    private boolean isCM1Site = false;

    private String guid;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public boolean isCM1Site() {
        return isCM1Site;
    }

    public void setCM1Site(boolean CM1Site) {
        isCM1Site = CM1Site;
    }

    /**
     * The legacy id for the site. This is used by the allowed sites properties for assets root level folders dialog.
     */
    private Long siteId;
    
    {
        setType(PSDataItemSummary.TYPE_SITE);
    }
    
    
    
    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public void setName(String name)
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    public Boolean getOverrideSystemJQuery() { return overrideSystemJQuery; }


    public void setOverrideSystemJQuery(Boolean overrideSystemJQuery) { this.overrideSystemJQuery = overrideSystemJQuery; }


    public Boolean getOverrideSystemFoundation() { return overrideSystemFoundation; }


    public void setOverrideSystemFoundation(Boolean overrideSystemFoundation) { this.overrideSystemFoundation = overrideSystemFoundation; }


    public Boolean getOverrideSystemJQueryUI() { return overrideSystemJQueryUI; }


    public void setOverrideSystemJQueryUI(Boolean overrideSystemJQueryUI) { this.overrideSystemJQueryUI = overrideSystemJQueryUI; }


    public String getSiteAdditionalHeadContent() { return siteAdditionalHeadContent; }


    public void setSiteAdditionalHeadContent(String siteAdditionalHeadContent) { this.siteAdditionalHeadContent = siteAdditionalHeadContent; }


    public String getSiteBeforeBodyCloseContent() { return siteBeforeBodyCloseContent; }


    public void setSiteBeforeBodyCloseContent(String siteBeforeBodyCloseContent) { this.siteBeforeBodyCloseContent = siteBeforeBodyCloseContent; }


    public String getSiteAfterBodyOpenContent() { return siteAfterBodyOpenContent; }


    public void setSiteAfterBodyOpenContent(String siteAfterBodyOpenContent) { this.siteAfterBodyOpenContent = siteAfterBodyOpenContent; }


    public String getBaseUrl()
    {
        return baseUrl;
    }


    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * TODO: Check why getId is returning the name property of the class.
     * Need to investigate the consequence of doing this change.
     */
    @Override
    public String getId() {
        return getName();
    }


    @Override
    public void setId(String id) {
        setName(id);
    }

    public Long getSiteId() {
        return this.siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    /**
     * @param defaultFileExtention default file extension used when creating a new page.
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

    /**
     * Determines if canonical tags should be rendered or not during the publishing.
     * @return <code>true</code> if the site is (marked) to render canonical tags.
     */
    public boolean isCanonical()
    {
       return isCanonical;
    }
    
    /**
     * Enable or disable canonical tags rendering.
     * 
     * @param isCanonical <code>true</code> if enable rendering of canonical tags; otherwise
     *           disable rendering for the site.
     */
    public void setCanonical(boolean isCanonical)
    {
       this.isCanonical = isCanonical;
    }

    /**
     * @param siteProtocol URLs' protocol ("http" or "https") used when rendering canonical tags.
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
     * @param defaultDocument site's default document (like "index.html") used when rendering canonical tags.
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
     * @param canonicalDist URLs' destination ("sections" or "pages") used when rendering canonical tags.
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
       return isCanonicalReplace;
    }
    
    /**
     * Enable or disable replacing custom canonical tags with rendered.
     * 
     * @param isCanonicalReplace <code>true</code> if enable replacing of custom canonical tags with rendered; otherwise
     *           disable replacing for the site.
     */
    public void setCanonicalReplace(boolean isCanonicalReplace)
    {
       this.isCanonicalReplace = isCanonicalReplace;
    }

    /**
     * The value may be <code>null</code>, this data is set only when specifically requested for, see {@link IPSSiteDataService#findAll(boolean)}.
     * Also see {@link IPSPubServerService#getS3PubInfo(com.percussion.utils.guid.IPSGuid)} for more information.
     * @return Publishing info may be <code>null</code>.
     */
    public PSPubInfo getPubInfo()
    {
        return pubInfo;
    }


    public void setPubInfo(PSPubInfo pubInfo)
    {
        this.pubInfo = pubInfo;
    }

    private static final long serialVersionUID = 1496690238764003673L;

}
