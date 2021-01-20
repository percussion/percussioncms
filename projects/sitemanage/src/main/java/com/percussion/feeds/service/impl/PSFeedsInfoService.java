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
package com.percussion.feeds.service.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.feeds.data.PSFeedInfo;
import com.percussion.feeds.error.PSFeedInfoServiceException;
import com.percussion.feeds.service.IPSFeedsInfoService;
import com.percussion.pagemanagement.service.IPSRenderService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSWebserviceUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author erikserating
 *
 */
public class PSFeedsInfoService implements IPSFeedsInfoService
{

    private IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
    private IPSRelationshipService relService = PSRelationshipServiceLocator.getRelationshipService();
    private PSItemDefManager iDefMgr = PSItemDefManager.getInstance();
    private IPSPublisherService pubService = PSPublisherServiceLocator.getPublisherService();
    private IPSRenderService renderService;
    private PSFeedsInfoQueue queue;
    private int contentTypePage = -1;
    private int contentTypeTemplate = -1;
    private IPSDeliveryInfoService deliveryInfoService;

    /**
     * Used to hold flags for each site that an empty descriptor list was already
     * queued. We want to avoid sending empty lists for no reason, but we need to do it
     * at least once so that the feed service removes feeds that no longer exist.
     */
    private Set<Long> emptyFeedSetSent = new HashSet<Long>();

    /**
     * Logger for this service.
     */
    public static Log log = LogFactory.getLog(PSFeedsInfoService.class);


    @Autowired
    public PSFeedsInfoService(IPSRenderService renderService, PSFeedsInfoQueue queue, IPSDeliveryInfoService deliveryInfoService)
    {
        this.renderService = renderService;
        this.queue = queue;
        this.deliveryInfoService = deliveryInfoService;
    }

    /**
     * Initialize members for use by the service.
     */
    public void setContentTypeIds()
    {
        if(contentTypePage != -1)
            return;
        try
        {
            contentTypePage = (int)iDefMgr.contentTypeNameToId(CONTENT_TYPE_PAGE);
            contentTypeTemplate = (int)iDefMgr.contentTypeNameToId(CONTENT_TYPE_TEMPLATE);
        }
        catch(PSInvalidContentTypeException e)
        {
            log.error(e.getLocalizedMessage());
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.service.IPSFeedsInfoService#getFeeds(java.lang.String)
     */
    public Collection<PSFeedInfo> getFeeds(long serverId) throws PSFeedInfoServiceException
    {
        setContentTypeIds();
        Collection<String> feedContentTypes = getFeedContentTypes();
        Collection<PSFeedInfo> feeds;
        try
        {
            feeds = getFeedEnabledContentItems(feedContentTypes);
            addParentItems(feeds);
            filterFeeds(feeds, serverId);
            addQueries(feeds);
        }
        catch (Exception e)
        {
            throw new PSFeedInfoServiceException(e);
        }

        return feeds;
    }

    /* (non-Javadoc)
     * @see com.percussion.feeds.service.IPSFeedsInfoService#pushFeeds(com.percussion.services.sitemgr.IPSSite)
     */
    public void pushFeeds(IPSSite site, PSPubServer server) throws PSFeedInfoServiceException
    {
        notNull(site);

        Collection<PSFeedInfo> feeds = getFeeds(server.getServerId());
        if (feeds.isEmpty() && emptyFeedSetSent.contains(site.getSiteId()))
        {
            log.info("No feeds found to push to feeds service for site or server is selected none" + site.getName());
            return;
        }

        if(server.getPropertyValue("publishServer")!=null && server.getPropertyValue("publishServer").equalsIgnoreCase(IPSPubServerService.DEFAULT_DTS)){
            log.info("server is selected none" + site.getName());
            return;
        }
        try
        {

            String descriptors = createDescriptorsJson(site, feeds, server.getServerType(),server.getPropertyValue("publishServer"));
            log.info("Queuing " + feeds.size() + " feeds for site " + site.getName());
            queue.queueDescriptors(site.getName(), descriptors, server.getServerType());
            if(feeds.isEmpty())
                emptyFeedSetSent.add(site.getSiteId());
            else if(emptyFeedSetSent.contains(site.getSiteId()))
                emptyFeedSetSent.remove(site.getSiteId());
        }
        catch (JSONException e)
        {
            throw new PSFeedInfoServiceException("Error occurred while trying to create descriptors.", e);
        }
    }

    /**
     * Helper method to create the descriptors json object string to be sent
     * to the feed service.
     * @param site assumed not <code>null</code>.
     * @param feeds assumed not <code>null</code>.
     * @return the json string never <code>null</code> or empty.
     * @throws JSONException
     * @throws PSFeedInfoServiceException
     */
    private String createDescriptorsJson(IPSSite site, Collection<PSFeedInfo> feeds, String serverType,String adminURL) throws JSONException, PSFeedInfoServiceException
    {
        PSDeliveryInfo deliveryInfo = deliveryInfoService.findByService(PSDeliveryInfo.SERVICE_FEEDS, serverType,adminURL);
        if(deliveryInfo == null)
        {
            String error = "Failed to find delivery server info";
            log.error(error);
            throw new PSFeedInfoServiceException(error);
        }

        String deliveryUrl = deliveryInfo.getUrl();
        String host = null;
        try
        {
            URI uri = new URI(deliveryUrl);
            host = uri.getHost();
            int port = uri.getPort();
            if (port != -1)
                host += ":" + port;
            host = uri.getScheme() + "://" + host;
        }
        catch (URISyntaxException e)
        {
            String error = "Failed to parse host from feed service url: " + deliveryUrl;
            log.error(error);
            throw new RuntimeException(error);
        }

        JSONObject obj = new JSONObject();
        JSONArray descriptors = new JSONArray();
        obj.put("site", site.getName());
        for(PSFeedInfo feed : feeds)
        {
            JSONObject d = new JSONObject();
            d.put("name", feed.getName());
            d.put("site", site.getName());
            d.put("description", feed.getDesc());
            d.put("link", host + feed.getOwnerPageLocation());
            d.put("title", feed.getTitle());
            d.put("query", feed.getQuery());
            d.put("type", feed.getType());
            descriptors.put(d);
        }
        obj.put("descriptors", descriptors);
        return obj.toString();
    }

    /**
     * Retrieves a collection of content types that use the feeds shared
     * field group.
     * @return list of content type names of content types that use the feeds
     * shared fields group. Never <code>null</code>, may be empty.
     */
    private Collection<String> getFeedContentTypes()
    {
        Collection<String> cts = new ArrayList<String>();
        try
        {
            String[] results = iDefMgr.getContentTypesUsingSharedFieldGroup("rssfeeds");
            for(String ct : results)
                cts.add(ct);
        }
        catch (PSInvalidContentTypeException e)
        {
            log.error(e.getLocalizedMessage());
        }
        return cts;
    }

    /**
     * Retrieves all feed enabled content items as feed info objects.
     * @param contentTypes list of feed enabled content types, assumed not <code>null</code>.
     * @return collection of feed info objects for each feed enabled content item found. Never
     * <code>null</code>, may be empty.
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    private Collection<PSFeedInfo> getFeedEnabledContentItems(Collection<String> contentTypes) throws InvalidQueryException, RepositoryException
    {
        Collection<PSFeedInfo> feeds = new ArrayList<PSFeedInfo>();
        for(String ct : contentTypes)
        {
            String queryString =
                    "select rx:sys_contentid, rx:feed_name, rx:feed_title, rx:feed_description from rx:"
                            + ct + " where rx:enable_rss_feed='Enable Rss feed'";
            Query query = contentMgr.createQuery(queryString, Query.SQL);
            QueryResult qresults = contentMgr.executeQuery(query, -1, null, null);
            RowIterator rows = qresults.getRows();
            while (rows.hasNext())
            {
                Row nrow = rows.nextRow();
                Value[] vals = nrow.getValues();
                PSFeedInfo feed = new PSFeedInfo(
                        Integer.valueOf(vals[0].getString()),
                        vals[1].getString(),
                        vals[2].getString(),
                        vals[3].getString()
                );
                feeds.add(feed);
            }
        }
        return feeds;
    }

    /**
     * Locates parent pages and templates that contain the feed item as dependents. For templates we find
     * all pages that use the template. Modifies the feed info objects passed in.
     * @param feeds the list of feed enabled items, assumed not <code>null</code>.
     * @throws PSException
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    private void addParentItems(Collection<PSFeedInfo> feeds) throws PSException, InvalidQueryException, RepositoryException
    {
        if (feeds.isEmpty())
            return;

        PSRelationshipFilter pFilter = new PSRelationshipFilter();
        PSRelationshipFilter tFilter = new PSRelationshipFilter();
        for(PSFeedInfo feed : feeds)
        {

            PSLocator loc = PSWebserviceUtils.getItemLocator(new PSLegacyGuid(feed.getId(), -1));
            pFilter.setDependent(loc);
            pFilter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
            pFilter.setOwnerContentTypeId(contentTypePage);
            for(PSRelationship r: relService.findByFilter(pFilter))
            {
                feed.getPages().add(r.getOwner().getId());
            }

            tFilter.setDependent(loc);
            tFilter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
            tFilter.setOwnerContentTypeId(contentTypeTemplate);
            for(PSRelationship r: relService.findByFilter(tFilter))
            {
                feed.getTemplates().add(r.getOwner().getId());
                // Locate pages that use templates
                IPSGuid guid = PSGuidUtils.makeGuid(r.getOwner().getId(), PSTypeEnum.LEGACY_CONTENT);
                String queryString =
                        "select rx:sys_contentid from rx:percPage where rx:templateid='" + guid.toString() + "'";
                Query query = contentMgr.createQuery(queryString, Query.SQL);
                QueryResult qresults = contentMgr.executeQuery(query, -1, null, null);
                RowIterator rows = qresults.getRows();
                while (rows.hasNext())
                {
                    Row nrow = rows.nextRow();
                    Value[] vals = nrow.getValues();
                    feed.getPages().add(Integer.valueOf(vals[0].getString()));
                }
            }

        }
    }

    /**
     * Filters out any feed enabled items that are not within the specified site and that are
     * not currently published. Also adds site specific info to the feed info nodes for the earliest page that
     * contains the feed. Modifies the feed info objects passed in.
     * @param feeds the list of feed enabled items, assumed not <code>null</code>.
     * @param serverId the site to filter by.
     */
    private void filterFeeds(Collection<PSFeedInfo> feeds, long serverId)
    {
        if (feeds.isEmpty())
            return;

        Map<Integer, IPSSiteItem> sItems = new HashMap<Integer, IPSSiteItem>();
        IPSGuid sGuid = PSGuidUtils.makeGuid(serverId, PSTypeEnum.PUBLISHING_SERVER);
        Collection<PSFeedInfo> removeFeeds = new ArrayList<PSFeedInfo>();
        for(IPSSiteItem si : pubService.findSiteItemsByPubServer(sGuid, DELIVERY_CONTEXT))
        {
            sItems.put(si.getContentId(), si);
        }
        for(PSFeedInfo feed : feeds)
        {
            Collection<Integer> remove = new ArrayList<Integer>();
            Integer ownerPage = null;
            long pageDate = -1;
            for(Integer p : feed.getPages())
            {
                if(!sItems.containsKey(p))
                {
                    remove.add(p);
                }
                else
                {
                    long current = sItems.get(p).getDate().getTime();
                    if(pageDate == -1 || current < pageDate)
                    {
                        pageDate = current;
                        ownerPage = p;
                    }
                }
            }
            //Remove pages not published
            for(Integer rmv : remove)
                feed.getPages().remove(rmv);

            //Use earliest published page as feed page parent and for site info
            if(ownerPage != null)
            {
                IPSSiteItem oPage = sItems.get(ownerPage);

                feed.setOwnerPageId(oPage.getContentId());
                feed.setOwnerPageLocation(oPage.getLocation());
                feed.setOwnerFolderId(oPage.getFolderId());
            }
            else
            {
                removeFeeds.add(feed);
            }

        }
        //Remove feeds without pages
        for(PSFeedInfo rmv : removeFeeds)
            feeds.remove(rmv);
    }

    /**
     * Gets the metadata query for the feed from the rendered page and adds it to the
     * feed info object.
     * @param feeds assumed not <code>null</code>.
     */
    private void addQueries(Collection<PSFeedInfo> feeds)
    {
        if (feeds.isEmpty())
            return;

        Iterator<PSFeedInfo> it = feeds.iterator();

        while(it.hasNext())
        {
            String data = null;
            PSFeedInfo feed = it.next();
            int pageid = feed.getOwnerPageId();
            IPSGuid guid = PSGuidUtils.makeGuid(pageid, PSTypeEnum.LEGACY_CONTENT);
            // Render the page, the query will be created and put in an element
            String page = renderService.renderPage(guid.toString());
            // Extract the query from the page

            Document doc = Jsoup.parse(page);
            Element div = doc.select("div[data-name=feedQuery_" + feed.getName() + "]").first();
            if (div != null)
            {
                data = div.attr("data-query");
            }

            if (data!=null)
                feed.setQuery(data);
            else
                // Remove item
                it.remove();
        }
    }

    private static final String CONTENT_TYPE_PAGE = "percPage";
    private static final String CONTENT_TYPE_TEMPLATE = "percPageTemplate";
    private static final int DELIVERY_CONTEXT = 10;


}
