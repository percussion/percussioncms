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

package com.percussion.rest.sites;

import com.percussion.rest.Guid;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Site")
@Schema(name="Site")
public class Site {

    public Site(){}

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

    private boolean canonicalReplace = true;

    private boolean pageBasedSite = false;

    private Guid guid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultFileExtention() {
        return defaultFileExtention;
    }

    public void setDefaultFileExtention(String defaultFileExtention) {
        this.defaultFileExtention = defaultFileExtention;
    }

    public boolean isCanonical() {
        return isCanonical;
    }

    public void setCanonical(boolean canonical) {
        isCanonical = canonical;
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

    public String getSiteProtocol() {
        return siteProtocol;
    }

    public void setSiteProtocol(String siteProtocol) {
        this.siteProtocol = siteProtocol;
    }

    public String getDefaultDocument() {
        return defaultDocument;
    }

    public void setDefaultDocument(String defaultDocument) {
        this.defaultDocument = defaultDocument;
    }

    public String getCanonicalDist() {
        return canonicalDist;
    }

    public void setCanonicalDist(String canonicalDist) {
        this.canonicalDist = canonicalDist;
    }

    public boolean isCanonicalReplace() {
        return canonicalReplace;
    }

    public void setCanonicalReplace(boolean canonicalReplace) {
        this.canonicalReplace = canonicalReplace;
    }

    public boolean isPageBasedSite() {
        return pageBasedSite;
    }

    public void setPageBasedSite(boolean pageBasedSite) {
        this.pageBasedSite = pageBasedSite;
    }

    public Guid getGuid() {
        return guid;
    }

    public void setGuid(Guid guid) {
        this.guid = guid;
    }
}
