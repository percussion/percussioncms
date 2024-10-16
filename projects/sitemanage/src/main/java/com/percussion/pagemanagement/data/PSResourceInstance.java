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
package com.percussion.pagemanagement.data;

import static org.apache.commons.lang.StringUtils.removeEnd;

import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.share.data.IPSLinkableContentItem;
import com.percussion.sitemanage.data.PSSiteSummary;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a fully loaded resource ready for link/rendering processing.
 * 
 * If links have not been processed yet then {@link #getLinkAndLocations()} will 
 * be <code>null</code>.
 * <p>
 * This is not a serializable object and as it has some behavior and transient data
 * associated with it.
 * 
 * @author adamgent
 *
 */
public class PSResourceInstance  {
    
    /*
     * Note to developers: resource instances are akin
     * AssemblyItem/AssemblyResults but for resources processing only.
     */
    private PSAssetResource resourceDefinition;
    private PSRenderLinkContext linkContext;
    private IPSLinkableContentItem item;
    private PSSiteSummary site;
    private List<PSResourceLinkAndLocation> linkAndLocations = new ArrayList<>();
    private URL baseUrl;
    private String locationFolderPath;
    

    /**
     * The physical folder path for the resource instance. The path will
     * always have <code>'/'</code> as a separator regardless
     * of the platform.
     * <p>
     * <strong>This path is not url escaped</strong>
     * @return never <code>null</code>.
     */
    public String getLocationFolderPath()
    {
        return locationFolderPath;
    }
    

    public void setLocationFolderPath(String locationFolderPath)
    {
        this.locationFolderPath = locationFolderPath;
    }
    
    /**
     * If the item is in a different site than linking context
     * than its a cross site link.
     * @return <code>true</code> if cross site.
     */
    public boolean isCrossSite() {
        String contextSiteId = linkContext.getSite().getId();
        String itemSiteId = site.getId();
        return ! StringUtils.equals(contextSiteId, itemSiteId);
    }
    
    /**
     * The base URL of the site that the resource is to be published to.
     * @return never <code>null</code>.
     */
    public URL getBaseUrl()
    {
        return baseUrl;
    }
    
    /**
     * Gets the relative URL as a uri object.
     * If the link is a cross site link the returned object
     * will include the host and port information. Otherwise it will
     * only include the path info.
     * @return never <code>null</code>.
     * @see #isCrossSite()
     */
    public URI getRelativeBaseUri() {
        try
        {
            if (isCrossSite()) {
                return getBaseUrl().toURI();
            }
            return new URI(getBaseUrl().toURI().getPath());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * The resource definition for this resource instance.
     * @return never <code>null</code>.
     */
    public PSAssetResource getResourceDefinition()
    {
        return resourceDefinition;
    }
    public void setResourceDefinition(PSAssetResource resourceDefinition)
    {
        this.resourceDefinition = resourceDefinition;
    }
    
    /**
     * The Link context.
     * @return never <code>null</code>.
     */
    public PSRenderLinkContext getLinkContext()
    {
        return linkContext;
    }
    public void setLinkContext(PSRenderLinkContext linkContext)
    {
        this.linkContext = linkContext;
    }
    
    /**
     * The content item associated with this resource.
     * @return never <code>null</code>.
     */
    public IPSLinkableContentItem getItem()
    {
        return item;
    }

    public void setItem(IPSLinkableContentItem item)
    {
        this.item = item;
    }
    
    /**
     * The site that the {@link #getItem() item} belongs to.
     * @return never <code>null</code>.
     */
    public PSSiteSummary getSite()
    {
        return site;
    }
    public void setSite(PSSiteSummary site)
    {
        try
        {
            String url = site.getBaseUrl();
            url = removeEnd(url, "/") + "/";
            baseUrl = new URL(url);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Site " + site + " has a bad base url", e);
        }
        this.site = site;
    }
    
    /**
     * 
     * The link and locations of the resource.
     * Multiple {@link PSResourceLinkAndLocation}s represent pagination.
     * It maybe <code>null</code> if links have not been processed yet.
     * 
     * @return maybe <code>null</code> or empty, usually its a list containing
     *  only one {@link PSResourceLinkAndLocation}
     */
    public List<PSResourceLinkAndLocation> getLinkAndLocations()
    {
        return linkAndLocations;
    }
    public void setLinkAndLocations(List<PSResourceLinkAndLocation> links)
    {
        this.linkAndLocations = links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSResourceInstance)) return false;
        PSResourceInstance that = (PSResourceInstance) o;
        return Objects.equals(getResourceDefinition(), that.getResourceDefinition()) && Objects.equals(getLinkContext(), that.getLinkContext()) && Objects.equals(getItem(), that.getItem()) && Objects.equals(getSite(), that.getSite()) && Objects.equals(getLinkAndLocations(), that.getLinkAndLocations()) && Objects.equals(getBaseUrl(), that.getBaseUrl()) && Objects.equals(getLocationFolderPath(), that.getLocationFolderPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResourceDefinition(), getLinkContext(), getItem(), getSite(), getLinkAndLocations(), getBaseUrl(), getLocationFolderPath());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSResourceInstance{");
        sb.append("resourceDefinition=").append(resourceDefinition);
        sb.append(", linkContext=").append(linkContext);
        sb.append(", item=").append(item);
        sb.append(", site=").append(site);
        sb.append(", linkAndLocations=").append(linkAndLocations);
        sb.append(", baseUrl=").append(baseUrl);
        sb.append(", locationFolderPath='").append(locationFolderPath).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public PSResourceInstance clone()
    {
        try
        {
            return (PSResourceInstance) BeanUtils.cloneBean(this);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot clone", e);
        }
    }
    
    
    
       
}

