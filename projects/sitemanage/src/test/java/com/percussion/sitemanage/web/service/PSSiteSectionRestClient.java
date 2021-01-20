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

package com.percussion.sitemanage.web.service;

import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.sitemanage.data.PSCreateExternalLinkSection;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSMoveSiteSection;
import com.percussion.sitemanage.data.PSReplaceLandingPage;
import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.sitemanage.data.PSSiteBlogPosts;
import com.percussion.sitemanage.data.PSSiteBlogProperties;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSUpdateSectionLink;

import java.util.List;

/**
 * This is used by unit test {@link PSSiteTemplateServiceTest} for testing
 * all site section operations.
 *
 * @author yubingchen
 */
public class PSSiteSectionRestClient extends PSDataServiceRestClient<PSSiteSection>
{
    public PSSiteSectionRestClient(String url) 
    {
        super(PSSiteSection.class, url, "/Rhythmyx/services/sitemanage/section/");
    }
    
    public PSSiteSection create(PSCreateSiteSection req)
    {
        String resp = postObjectToPath(getPath() + "create", req);
        PSSiteSection sec = objectFromResponseBody(resp, PSSiteSection.class);
        return sec;
    }
    
    public PSSiteSection createSectionLink(String targetSectionGuid, String parentSectionGuid)
    {
        return getObjectFromPath(getPath() + "createSectionLink/" + targetSectionGuid + "/" + parentSectionGuid,
                PSSiteSection.class);
    }
    
    public PSSiteSection createExternalLinkSection(PSCreateExternalLinkSection req)
    {
        String resp = postObjectToPath(getPath() + "createExternalLinkSection", req);
        PSSiteSection sec = objectFromResponseBody(resp, PSSiteSection.class);
        return sec;
    }
    
    public PSSiteSectionProperties getSectionProperties(String id)
    {
        return getObjectFromPath(getPath() + "properties/" + id, PSSiteSectionProperties.class);
    }
    
    public PSSiteSection update(PSSiteSectionProperties req)
    {
        String resp = postObjectToPath(getPath() + "update", req);
        PSSiteSection sec = objectFromResponseBody(resp, PSSiteSection.class);
        return sec;
    }

    public PSSiteSection move(PSMoveSiteSection req)
    {
        String resp = postObjectToPath(getPath() + "move", req);
        PSSiteSection sec = objectFromResponseBody(resp, PSSiteSection.class);
        return sec;
    }
    

    public PSSiteSection loadRoot(String siteName)
    {
        PSSiteSection section = getObjectFromPath(getPath() + "root/" + siteName);
        return section;
    }
    
    public PSSectionNode loadTree(String siteName)
    {
        PSSectionNode section = (PSSectionNode) objectFromResponseBody(GET(getPath() + "tree/" + siteName),
                PSSectionNode.class);
        return section;
    }
    
    public List<PSSiteSection> loadChildSections(PSSiteSection section)
    {
        String resp = postObjectToPath(getPath() + "childSections", section);
        List<PSSiteSection> sections = objectsFromResponseBody(resp, PSSiteSection.class);
        return sections;
    }
    
    public PSReplaceLandingPage replaceLandingPage(PSReplaceLandingPage req)
    {
        String resp = postObjectToPath(getPath() + "replaceLandingPage", req);
        PSReplaceLandingPage rlp = objectFromResponseBody(resp, PSReplaceLandingPage.class);
        return rlp;
    }
    
    public List<PSSiteBlogProperties> getBlogsForSite(String siteName)
    {
        return getObjectsFromPath(concatPath(getPath(), "blogs", siteName), PSSiteBlogProperties.class);
    }
    
    public List<PSSiteBlogProperties> getAllBlogs()
    {
        return getObjectsFromPath(concatPath(getPath(), "allBlogs"), PSSiteBlogProperties.class);
    }
    
    public PSSiteBlogPosts getBlogPosts(String blogId)
    {
        return getObjectFromPath(concatPath(getPath(), "blogPosts", blogId), PSSiteBlogPosts.class);
    }

    /**
     * Posts a request to update a section link and returns its response.
     * 
     * @param updateRequest {@link PSUpdateSectionLink} request, assumed not
     *            <code>null</code>.
     * @return {@link PSSiteSection} object, never <code>null</code>.
     */
    public PSSiteSection updateSectionLink(PSUpdateSectionLink updateRequest)
    {
        String resp = postObjectToPath(getPath() + "updateSectionLink", updateRequest);
        PSSiteSection sec = objectFromResponseBody(resp, PSSiteSection.class);
        return sec;
    }
}
