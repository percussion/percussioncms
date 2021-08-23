/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.feeds.services;

import com.percussion.delivery.feeds.PSFeedGenerator;
import com.percussion.delivery.feeds.data.IPSFeedDescriptor;
import com.percussion.delivery.feeds.data.PSFeedDTO;
import com.percussion.delivery.feeds.data.PSFeedDescriptors;
import com.percussion.delivery.feeds.data.PSFeedItem;
import com.percussion.delivery.listeners.IPSServiceDataChangeListener;
import com.percussion.delivery.services.PSAbstractRestService;
import com.percussion.delivery.utils.security.PSHttpClient;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.security.SecureStringUtils;
import com.percussion.security.ToDoVulnerability;
import com.percussion.utils.io.PathUtils;
import com.rometools.rome.io.FeedException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import static com.percussion.security.SecureStringUtils.stripNonHttpProtocols;

/**
 * The feed service is responsible for generating RSS/ATOM feeds. The service
 * also collects feed descriptors from the CM1 which provide meta data about the
 * feed and a query used to get page data from the dynamic index service(meta
 * data service) to build the feed list.
 * 
 * @author erikserating
 * 
 */
@SuppressFBWarnings("URLCONNECTION_SSRF_FD") // It is validated - only http and https urls are allowed.
@Path("/rss")
@Component
@Scope("singleton")
public class PSFeedService extends PSAbstractRestService implements IPSFeedsRestService
{
    public PSFeedService(){

    }

    private PSHttpClient httpClient;
    private static final Logger log = LogManager.getLogger(PSFeedService.class);
    private List<IPSServiceDataChangeListener> listeners = new ArrayList<>();

    private final String[] PERC_FEEDS_SERVICE =
    {"feeds"};
    private static final String PERC_FEEDS_PROPERTIES = "/WEB-INF/feeds.properties";
    private static final String FEEDS_IP_DEFAULT="127.0.0.1";

    /**
     * The feed data access object, initialized in the ctor. Never
     * <code>null</code> after that.
     */
    private IPSFeedDao feedDao;

    /**
     * 2011-01-21T09:36:05
     */
    FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    public PSFeedService(@Qualifier("feedsDao") IPSFeedDao dao, PSHttpClient httpClient )
    {
        feedDao = dao;
        this.httpClient = httpClient;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.feeds.services.IPSFeedsRestService#getFeed(java.lang.String, java.lang.String)
	 */
    @Override
	@GET
    @Path("/{sitename}/{feedname}/{hostname}")
    @Produces("text/xml")
    public Response getFeed(@PathParam("sitename") String sitename, @PathParam("feedname") String feedname, @PathParam("hostname") String hostname,  @Context HttpServletRequest httpRequest)
    {

        sitename = SecureStringUtils.stripAllLineBreaks(
                SecureStringUtils.normalize(sitename,false));

        feedname = SecureStringUtils.stripAllLineBreaks(
                SecureStringUtils.normalize(feedname,false));

        hostname = SecureStringUtils.stripAllLineBreaks(
                SecureStringUtils.normalize(hostname,false));

    	if(StringUtils.isEmpty(sitename)) {
    		log.error("Illegal argument passed to getFeed. Site Name was missing from request.");
    		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    	}
    	
    	if(StringUtils.isEmpty(feedname)){
    		log.error("Illegal argument passed to getFeed. Feed Name was missing from request.");
    		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    	}
    	
    	if(StringUtils.isEmpty(hostname)){
    		log.error("Illegal argument passed to getFeed. Host Name was missing from request.");
    		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    	}
    	
    	if(log.isDebugEnabled()){
    		log.debug(
    		        String.format(
    		                "Searching for feed descriptor with feed name: %s with site name: %s and hostname: %s",
                            feedname,sitename,hostname));
    	}
    	
    	IPSFeedDescriptor desc = feedDao.find(feedname, sitename);
        Response resp;
        if (desc != null)
        {
        	if(log.isDebugEnabled()){
        		log.debug(
        		        String.format("Found feed descriptor: %s",desc.toString()));
        	}
        	
        	if(log.isDebugEnabled()){
        		log.debug("Searching for feed connection information...");
        	}
            IPSConnectionInfo info = feedDao.getConnectionInfo();
            if (info != null)
            {
            	if(log.isDebugEnabled()){
            		log.debug("Got connection info for feed: {}", info.toString());
            	}
            	String feed;
                try
                {
                	if(log.isDebugEnabled()){
                		log.debug("Generating feed ...");
                	}
                	feed = generateFeed(desc, hostname, httpRequest);
                }
                catch (FeedException | IOException e)
                {
                	log.error("Unexpected exception generating RSS feed: {}", e.getMessage());
                	log.debug(e.getMessage(), e);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).build();
                }
                if (StringUtils.isNotBlank(feed))
                {
                	if(log.isDebugEnabled()){
                		log.debug("Metadata Service returned results for feed: {}" , feed);
                	}
                	resp = Response.ok(feed).type(MediaType.TEXT_XML_TYPE).build();
                }
                else
                {
                	log.warn("Feed query returned no results.");
                    // Could not generate feed because no meta data exists
                    resp = Response.status(Status.NOT_FOUND).build();
                }
            }
            else
            {
                log.error("Unable to locate connection information.  Unable to query for feed.");
            	// No connection info present, send service unavailable
                resp = Response.status(Status.SERVICE_UNAVAILABLE).build();
            }

        }
        else
        {
        	log.error("Unable to locate matching feed for feed name: {} and sitename: {} ",feedname, sitename);
            // No feed descriptor found
            resp = Response.status(Status.NOT_FOUND).build();
        }

        return resp;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.feeds.services.IPSFeedsRestService#readExternalFeed(java.lang.String)
	 */
    @Override
	@POST
    @Path("/readExternalFeed")
    @Produces(MediaType.APPLICATION_XML)
    @ToDoVulnerability
    public String readExternalFeed(PSFeedDTO psFeedDTO)
    {

        URL url;
        HttpURLConnection con = null;
        String feeds = "";
        String decodedUrl="";
        String feedUrl = psFeedDTO.getFeedsUrl();

        log.debug("URL is: {}",feedUrl);
        
        try{
            //If Secure File is not Copied to DTS Yet then returning

            String decryptedUrl = PSEncryptor.decryptString(feedUrl);
            log.debug("Decrypted URL is: {}" , decryptedUrl);
            decodedUrl = URLDecoder.decode(decryptedUrl, "UTF8");
            log.debug("Decoded URL is: {}",  decodedUrl);
            
            //plaintext URL Sent---- Throw not Allowed Error
	         if(decodedUrl != null && decodedUrl.equals(feedUrl)){
                log.error("Illegal argument passed to readExternalFeed. External unEncrypted Feed Url Not Allowed.");
                throw new WebApplicationException(403);
              }

            decodedUrl = stripNonHttpProtocols(decodedUrl);

            if(StringUtils.isEmpty(decodedUrl)){
                //Url provided was not valid http or https
                throw new WebApplicationException("Invalid url supplied to getExternalFeed",
                        Status.NOT_ACCEPTABLE);
            }
        }catch(PSEncryptionException e){
            //Means EncryptionKey Not generated yet
            log.error(e.getMessage());
            log.debug(e);
            return "";

        }catch(Exception e){
        	log.error(e.getMessage());
        	log.debug(e);
        	throw new WebApplicationException(403);
        }
        
        if(StringUtils.isEmpty(feedUrl)) {
    		log.error("Illegal argument passed to readExternalFeed. Feed Url was missing from request.");
    		throw new WebApplicationException(403);
        }

        try
        {
            url = new URL(decodedUrl);
            // properly encode
            url = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef()).toURL();

            log.debug("The Url for external feed : {}" , url);

            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept-Charset", "utf-8, ISO-8859-1;q=0.7,*;q=0.7");
            con.setRequestMethod("GET");
         try(BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
             String line = null;
             StringBuilder bd = new StringBuilder();
             while ((line = rd.readLine()) != null) {
                 bd.append(line);
             }
             feeds = bd.toString();
         }

        }
        catch (Exception e)
        {
       		log.error("Exception during reading external feed : {}", e.getMessage());
       		log.debug(e);
        }
        finally
        {
            if (con != null)
            {
                con.disconnect();
            }
        }

        return feeds;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.feeds.services.IPSFeedsRestService#saveDescriptors(com.percussion.delivery.feeds.data.PSFeedDescriptors)
	 */
    @Override
	@PUT
    @Path("/descriptors")
    @RolesAllowed("deliverymanager")
    public void saveDescriptors(PSFeedDescriptors descriptors)
    {
    	if(descriptors == null) {
    		log.error("Illegal argument passed to saveDescriptors. Feed descriptors cannot be null.");
    		return;
        }

    	if(descriptors.getDescriptors().isEmpty()){
    		log.warn("Attempt to save empty list of Feed Descriptors");
    		return;
    	}
    	
        HashSet<String> sites = new HashSet<>();
        sites.add(descriptors.getSite());
    
        // Save connection info
        feedDao.saveConnectionInfo(descriptors.getServiceUrl(), descriptors.getServiceUser(),
                descriptors.getServicePass(), descriptors.isServicePassEncrypted());
        // Determine descriptor delete list
        List<IPSFeedDescriptor> deletes = new ArrayList<>();
        List<IPSFeedDescriptor> existing = feedDao.findBySite(descriptors.getSite());
        
       
        for (IPSFeedDescriptor d : existing)
        {
        	boolean match = false;
        	for(IPSFeedDescriptor nd : descriptors.getDescriptors()){
        		if((nd.getName().equals(d.getName())) && (nd.getSite().equals(d.getSite()))){
        			match = true;
        			break;
        		}
        	}
        	
            if (!match)
                deletes.add(d);
        }
        
        if(log.isDebugEnabled()){
    		log.debug("Descriptors that will be deleted are : {} " , deletes.toString());
    	}
        
        feedDao.saveDescriptors(descriptors.getDescriptors());
    
        // Remove feed descriptors for feeds that no longer exist
        feedDao.deleteDescriptors(deletes);

    }

    /**
     * Helper method to do the actual work of calling the dynamic indexer (meta
     * data service) to retrieve the data needed for the feed content. Then
     * calls the feed generator to generate the feed content.
     * 
     * @param desc the feed descriptor, assumed to not be <code>null</code>.
     * @param httpRequest the http request, assumed not
     *            <code>null</code>.
     * @return the feed xml, may be <code>null</code>.
     * @throws FeedException if any error occurs while generating the feed.
     */
    private String generateFeed(IPSFeedDescriptor desc, String hostName, HttpServletRequest httpRequest) throws FeedException, IOException {
        Properties props1 = new Properties();
        ServletContext contextPath = httpRequest.getServletContext();
        InputStream in = contextPath.getResourceAsStream(PERC_FEEDS_PROPERTIES);
        props1.load(in);
        String feedsIp = props1.getProperty("rss.feeds.ip");
        if(feedsIp==null || feedsIp.isEmpty()){
            feedsIp=FEEDS_IP_DEFAULT;
        }else{
            feedsIp = feedsIp.trim();
        }

        InetAddressValidator ipValidator = new InetAddressValidator();
        boolean isValidIp = ipValidator.isValid(feedsIp);
        boolean isIPV4Address = false;
        boolean isIPV6Address = false;
        if(isValidIp){
            if(ipValidator.isValidInet4Address(feedsIp)){
                isIPV4Address = true;
            }else if(ipValidator.isValidInet6Address(feedsIp)){
                isIPV6Address = true;
            }else{
                feedsIp = FEEDS_IP_DEFAULT;
            }
        }else{
            feedsIp = FEEDS_IP_DEFAULT;
        }
        // Call the metadata service with the query to get page listing
        PSFeedGenerator generator = new PSFeedGenerator();
        String feed = null;
        URI uri = null;
        String url = null;
        String protocol = null;
        Client client;
        
        try
        {
            client = httpClient.getSSLClient();
            uri = new URI(desc.getLink());
            if(isIPV4Address){
                url = httpRequest.getScheme()+"://"+feedsIp+":"+httpRequest.getLocalPort();
            }else if(isIPV6Address){
                url = httpRequest.getScheme()+"://["+feedsIp+"]:"+httpRequest.getLocalPort();
            }else{
                url = httpRequest.getScheme()+"://"+feedsIp+":"+httpRequest.getLocalPort();
            }

            protocol = uri.getScheme() + "://";
            log.info("The url obtained using the httpRequest.getLocalAddr() ----> {} " , url);
        }
        catch (Exception e)
        {
            client = ClientBuilder.newClient();
            log.error("Exception occurred in creating the SSL Client : {} " , e.getMessage());
            log.debug(e.getMessage(), e);
        }

        WebTarget webTarget = client.target(url + "/perc-metadata-services/metadata/get");
        
        if(log.isDebugEnabled()){
    		log.debug(
    		        "WebResource for metadata service : {}",webTarget.toString());
    	}
        
        try
        {
            List<PSFeedItem> items = new ArrayList<>();

            Invocation.Builder invocationBuilder =  ((WebTarget) webTarget).request(MediaType.APPLICATION_JSON_TYPE);

            Response  response = invocationBuilder.post(Entity.entity(desc.getQuery(), MediaType.APPLICATION_JSON));

            String jsonString =  response.readEntity(String.class);
            JSONObject resultObj = new JSONObject(jsonString);
            JSONArray data = (JSONArray) resultObj.get("results");
            
            String host = StringUtils.isBlank(hostName) ? PSFeedGenerator.getHost(desc.getLink()) : hostName;
            
            int len = data.length();
            for (int i = 0; i < len; i++)
            {
                JSONObject obj = data.getJSONObject(i);
                JSONObject props = obj.getJSONObject("properties");
                PSFeedItem item = new PSFeedItem();
                String folder = obj.getString("folder");
                String pagename = obj.getString("name");
                item.setLink(protocol + host + folder + pagename);
                if (props.has(PROP_TITLE))
                    item.setTitle(props.getString(PROP_TITLE));
                if (props.has(PROP_DESCRIPTION))
                {
                    String sitePrefix = protocol + host;
                    String replacedHtml = replaceRelativeLinks(props.getString(PROP_DESCRIPTION), sitePrefix);
                    item.setDescription(replacedHtml);
                }
                if (props.has(PROP_PUBDATE))
                {
                    TimeZone tz = TimeZone.getDefault();
                    if(props.has(PROP_CONTENTPOSTDATETZ))
                        tz = TimeZone.getTimeZone(props.getString(PROP_CONTENTPOSTDATETZ));
                    FastDateFormat tzFmt  = FastDateFormat.getInstance(dateFormat.getPattern(),tz);
                    item.setPublishDate(tzFmt.parse(props.getString(PROP_PUBDATE)));
                }
                items.add(item);
            }
            feed = generator.makeFeedContent(desc, host, items);

            log.debug("The generated feed: {}" , feed);

        }
        catch (Exception e)
        {
       		log.error("Exception during feed generation : {}" , e.getMessage());
       		log.debug(e);
            throw new FeedException(e.getMessage(), e);
        }

        return feed;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#addMetadataListener
     * (com.percussion.metadata.event.IPSMetadataListener)
     */
    /* (non-Javadoc)
	 * @see com.percussion.delivery.feeds.services.IPSFeedsRestService#addMetadataListener(com.percussion.delivery.listeners.IPSServiceDataChangeListener)
	 */
    @Override
	public void addMetadataListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        if (!listeners.contains(listener))
            listeners.add(listener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.metadata.IPSMetadataIndexerService#removeMetadataListener
     * (com.percussion.metadata.event.IPSMetadataListener)
     */
    /* (non-Javadoc)
	 * @see com.percussion.delivery.feeds.services.IPSFeedsRestService#removeMetadataListener(com.percussion.delivery.listeners.IPSServiceDataChangeListener)
	 */
    @Override
	public void removeMetadataListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        listeners.remove(listener);
    }

    /**
     * Fire a data change event for all registered listeners.
     */
    @SuppressWarnings("unused")
	private void fireDataChangedEvent(Set<String> sites)
    {
        if (sites == null || sites.isEmpty())
        {
            return;
        }

        for (IPSServiceDataChangeListener listener : listeners)
        {
            listener.dataChanged(sites, this.PERC_FEEDS_SERVICE);
        }
    }

    /**
     * Fire a data change event for all registered listeners.
     */
    @SuppressWarnings("unused")
	private void fireDataChangeRequestedEvent(Set<String> sites)
    {
        if (sites == null || sites.isEmpty())
        {
            return;
        }

        for (IPSServiceDataChangeListener listener : listeners)
        {
            listener.dataChangeRequested(sites, this.PERC_FEEDS_SERVICE);
        }
    }

    @Override
    @PUT
    @Path("/rotateKey")
    @RolesAllowed("deliverymanager")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN})
    public void rotateKey(String key) {
        byte[] backToBytes = Base64.getDecoder().decode(key);
        PSEncryptor.getInstance("AES",
                PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
        ).forceReplaceKeyFile(backToBytes,false);
    }
    
    @Override
	public String getVersion() {
    	
    	String version = super.getVersion();
    	
    	log.debug("getVersion() from PSFeedService... {}", version);
    	
    	return version;
    }
    
    /**
     * Parses the page summary of each rss post if present.
     * This is required as all inline links are currently relative
     * and external feed applications may be required to use fully
     * qualified URLs.
     * @param html the source html to parse
     * @return a String with 
     */
    private String replaceRelativeLinks(String html, String sitePrefix) {
        Document doc = Jsoup.parse(html);
        Elements elms = doc.select("img[src]");
        for (Element elm : elms) {
            String fullUrl = sitePrefix + elm.attr("src");
            elm.attr("src", fullUrl);
        }
        return doc.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response updateOldSiteEntries(String prevSiteName, String newSiteName) {
        log.info("Attempting to delete feeds entries for site name: {}",  prevSiteName);
        try {
            List<IPSFeedDescriptor> feeds = feedDao.findBySite(prevSiteName);
            feedDao.deleteDescriptors(feeds);
        } catch (Exception e) {
            log.error("Error updating feed entries for old site: {}, Error: {}",prevSiteName, e.getMessage());
            log.debug(e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Status.NO_CONTENT).build();
    }
}
