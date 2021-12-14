/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.syndication;

import com.percussion.pso.utils.HTTPProxyClientConfig;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/***
 * Serves as a Velocity friendly proxy class for the underlying syndication 
 * libraries.
 *
 *
 */
public class PSSynFeedProxy {

	private static final Logger log = LogManager.getLogger(PSSynFeedProxy.class);
	private static final String HTTP_IFMODIFIED="If-Modified-Since";
	private static final String HTTP_IFNONEMATCH="If-None-Match";
	
	private SyndFeed feed;
	
	/***
	 * Returns the name of the first feed author in the collection of authors. 
	 * 
	 * @return
	 */
	public String getAuthor(){
		return feed.getAuthor();
	}

	/***
	 *  Returns the feed authors. 
	 * @return
	 */
	public List<SyndPerson> getAuthorList(){
		return feed.getAuthors();
	}

	/***
	 * Convenience method that returns the list of Authors as a comma separated string.
	 * @return
	 */
	public String getAuthors(){
		String ret="";
		Object a;
		
		//@TODO: Add Atom Support
		for(int i=0;i<feed.getAuthors().size();i++){
			a = feed.getAuthors().get(i);
			if(!(a instanceof SyndPerson)){
				if(ret=="")
					ret = a.toString();
				else
					ret.concat("," + a.toString());
			}
		}
		return ret;
	}

	/***
	 * Returns the feed categories. 
	 * @return
	 */
	public List getCategoriesList(){
		return feed.getCategories();
	}

	/***
	 * Returns the feed categories as a comma separated string.
	 * @return
	 */
	public String getCategories(){
		String ret="";
		Object a;
	
		for(int i=0;i<feed.getCategories().size();i++){
			a = feed.getCategories().get(i);
				if(ret=="")
					ret = (String)a;
				else
					ret.concat("," + (String)a);
		}
		
		return ret;
	}

	/***
	 * the feed author.
	 * @return
	 */
	public String getContributors() {
		String ret="";
		Object a;
		
		//@TODO: Add Atom Support
		for(int i=0;i<feed.getContributors().size();i++){
			a = feed.getContributors().get(i);
			if(!(a instanceof SyndPerson)){
				if(ret=="")
					ret = a.toString();
				else
					ret.concat("," + a.toString());
			}
		}
		return ret;

	}
	
	public List getContributorsList() {
		return feed.getContributors();
	}
	
	/***
	 * Returns the feed copyright. 
	 * @return
	 */
	public String getCopyright() {
		return feed.getCopyright();
	}
    
	/***
	 * Returns the feed description. 
	 * @return
	 */
	public String getDescription(){
		//@TODO: Add Ext Description support.
		return feed.getDescription();
	}

	/***
	 * Returns the charset encoding of a the feed. 
	 * @return
	 */
	public String getEncoding(){
		return feed.getEncoding();
	}
    
	/***
	 * Returns the feed entries. 
	 * @return
	 */
	public List<PSSynFeedEntry> getEntries(){
		
		ArrayList<PSSynFeedEntry> ret = new ArrayList<PSSynFeedEntry>();
		
		for(int i=0;i<feed.getEntries().size();i++ ){
			ret.add(new PSSynFeedEntry((SyndEntry)feed.getEntries().get(i)));
		}
		return ret;		
	}

	/***
	 * Returns the wire feed type the feed had/will-have when coverted from/to a WireFeed.
	 * @return
	 */
	public String getFeedType() {
		return feed.getFeedType();
	}

    /***
	 * Returns the feed image. 
	 * @return
	 */
    public PSSynFeedImage getImage(){
		return new PSSynFeedImage(feed.getImage());
	}

    /***
     * Returns the feed language. 
  w   * @return
     */
    public String getLanguage() {
    	return feed.getLanguage();
    }
    
    /***
     * Returns the feed link. 
     * @return
     */
    public String getLink() {
    	return feed.getLink();
    }
    
    /***
     * Returns the entry links  
     * @return
     */
    public List<String> getLinks() {	
    	ArrayList<String> ret = new ArrayList<String>();
    	
    	for(int i=0;i<feed.getLinks().size();i++){
    		ret.add(feed.getLinks().get(i).getHref());
    	}
    	return ret;
    }
    
    /***
     * Returns the feed published date. 
     * @return
     */
    public Date getPublishedDate() {
    	return feed.getPublishedDate();
    }
    
    /***
     * Returns the feed title. 
     * @return
     */
    public String getTitle() {
    	//@TODO: Add support for title EX.
    	return feed.getTitle();
    }
    
    /***
     * Returns the feed URI. 
     * @return
     */
    public String getUri() {
    	return feed.getUri();
    }
    


	/***
	 * Initializes this instances of the proxy with the specified feed url.
	 * 
	 * @param urlString
	 * @throws HttpException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public PSSynFeedProxy(String urlString) throws HttpException, IOException, IllegalArgumentException, FeedException, FeedException {
		HttpClient client = new HttpClient();
		
		//Set up the proxy server if there is one.
		HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
		
		if(!proxy.getProxyServer().equals("")){
			client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
		}
			
		HttpMethod get = new GetMethod(urlString);
	
		try{
			int code = client.executeMethod(get);
			
			SyndFeedInput input = new SyndFeedInput();
			log.debug("Requesting feed from " + urlString);
			SyndFeed f = input.build(new XmlReader(get.getResponseBodyAsStream()));
			this.feed = f;	
		}finally{
			get.releaseConnection();
		}	
	}
	
	public PSSynFeedProxy(String urlString, String eTag, String lastModified) throws IllegalArgumentException, FeedException, IOException{
		HttpClient client = new HttpClient();
		
		//Set up the proxy server if there is one.
		HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
		
		if(!proxy.getProxyServer().equals("")){
			log.debug("Setting Proxy server to " + proxy.getProxyServer()+ ":" + proxy.getProxyPort());
			client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
		}
		client.getParams().setConnectionManagerTimeout(2000);
	
		GetMethod get = new GetMethod(urlString);
		
		try{
		//Add the modification check headers if we have valid params.
		if(eTag!=null && !eTag.trim().equals("")){
			get.addRequestHeader(HTTP_IFNONEMATCH, eTag);
		}
		
		if(lastModified!=null && !lastModified.trim().equals("")){
			get.addRequestHeader(HTTP_IFMODIFIED,lastModified);
		}
		
		get.setFollowRedirects(true);
		int code = client.executeMethod(get);
		
		if(code==HttpStatus.SC_NOT_MODIFIED){
			log.debug("Feed URL not modified.");
		}else if(code==HttpStatus.SC_OK){
			SyndFeedInput input = new SyndFeedInput();
			log.debug("Requesting feed from " + urlString);
			this.feed  = input.build(new XmlReader(get.getResponseBodyAsStream()));	
			
			//@TODO: Add logic into this section to persist the lastmodified header.
			
		}else{
			log.debug("Unexpected response from server for url" + urlString + " Response Code:" + code);
					}
		}finally{
			get.releaseConnection();
		}

	}
}
