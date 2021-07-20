/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.jexl;


import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.restservice.IItemRestService;
import com.percussion.pso.restservice.ItemRestServiceLocator;
import com.percussion.pso.restservice.impl.ItemRestServiceImpl;
import com.percussion.pso.restservice.model.Field;
import com.percussion.pso.restservice.model.HttpDOMResponse;
import com.percussion.pso.restservice.model.HttpHtmlResponse;
import com.percussion.pso.restservice.model.Item;
import com.percussion.pso.restservice.utils.HtmlLinkHelper;
import com.percussion.pso.restservice.utils.ItemServiceHelper;
import com.percussion.pso.utils.HTTPProxyClientConfig;
import com.percussion.server.PSRequest;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 */
public class PSOImportJexl  extends PSJexlUtilBase implements IPSJexlExpression {

	/**
	 * Logger for this class
	 */

	private static final Logger log = LogManager.getLogger(PSOImportJexl.class);
	private static final String HTTP_IFMODIFIED="If-Modified-Since";
	private static final String HTTP_IFNONEMATCH="If-None-Match";
	
	/**
	 * Method getPostBodyAsDom.
	 * @return Document
	 * @throws SAXNotRecognizedException
	 * @throws SAXNotSupportedException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws IOException
	 */
	@IPSJexlMethod(description="Gets html posted to the template and converts it to tidied xml compliant output ",
			params={})
			public Document getPostBodyAsDom() throws SAXNotRecognizedException, SAXNotSupportedException, TransformerFactoryConfigurationError, TransformerException, IOException 
			{
		PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		HttpServletRequest sreq = req.getServletRequest();

		XMLReader reader = new Parser();
		reader.setFeature(Parser.namespacesFeature, true);
		reader.setFeature(Parser.namespacePrefixesFeature, true);

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DocumentResult dr = new DocumentResult();
		transformer.transform(new SAXSource(reader, new InputSource(sreq.getInputStream())), 
				dr);
		return dr.getDocument();
			}


	
	/**
	 * Method getHttpAsDom.
	 * @param url String
	 * @return Document
	 */
	@IPSJexlMethod(description="Extracts html from a url and convert to dom",
			params={@IPSJexlParam(name="url",description="the url to connect to")})

			public Document getHttpAsDom(String url) {
		Document doc = null;
		HttpClient client = new HttpClient();
		
		//Set up the proxy server if there is one.
		HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
		
		if(!proxy.getProxyServer().equals("")){
			client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
		}
		client.getParams().setConnectionManagerTimeout(2000);

		GetMethod get = new GetMethod(url);
		get.setFollowRedirects(true);

		try {
			int iGetResultCode = client.executeMethod(get);
			InputStream responseBody = get.getResponseBodyAsStream();

		
			XMLReader reader = new Parser();
			reader.setFeature(Parser.namespacesFeature, true);
			reader.setFeature(Parser.namespacePrefixesFeature, true);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DocumentResult dr = new DocumentResult();
			transformer.transform(new SAXSource(reader, new InputSource(responseBody)), 
					dr);
			doc=dr.getDocument();

		} catch (Exception ex) {
			log.error(ex.getMessage());
			log.debug(ex.getMessage(), ex);
		} finally {
			get.releaseConnection();
		}
		return doc;
	}

	/**
	 * Method getHttpAsDom.
	 * 
	 * This routine will try to download the specified URL.  If the server says the file hasn't changed, it will not be downloaded.
	 * 
	 * The resulting document will have a block of XML in the headers: namespace that represent the headers the server returns.
	 * This XML block can then be cached with the resulting item for subsequant calls to this routine.
	 * 
	 * This is to avoid the expensive process of downloading content unecessarily in Feed scenarios, when the Feed is updated but many of the items
	 * in the feed are not.
	 * 
	 * @param url String HTTP Resource to download
	 * @param keyfield Indicates the field in the system that stores the hash code for the specified URL
	 * @param contextRoot Indicates the path in the system where this item may already be stored.
	 * 
	 * @return Document
	 */
	@IPSJexlMethod(description="Extracts html from a url and convert to dom. Given the specified keyfield and contextroot, will attempt to find any items that match the url's has code in the keyfield and path.  If found any previous cached etag and/or last modified header data will be used whn checking to see if the item needs to be downloaded.",
			params={@IPSJexlParam(name="url",description="the url to connect to"),
			@IPSJexlParam(name="keyfield",description="The field containing the hash code for the specified URL."),
			@IPSJexlParam(name="contextroot",description="The root path within the system where this item may already be stored.")})

	public HttpDOMResponse getHttpAsDom(String url, String keyfield, String contextRoot) {
		HttpDOMResponse ret = null;
		Document doc = null;
		String etag=null;
		String lastModified=null;
		
		/** Before we do anything we need to check to see if this item exists. If it does
		 * we want to get the cached ETag and Last Modified headers to we don't download it unecessarily. 
		 */
		//IItemRestService svc = ItemRestServiceLocator.getItemServiceBase();
		ItemRestServiceImpl svc = new ItemRestServiceImpl();
		
		Item item = svc.findByKeyField(Integer.toString(url.hashCode()),keyfield,contextRoot);
	
		if(item!=null && item.getContentId()!=null){
			log.debug("Located existing item for URL " + url);
			Field etag_field = item.getField("cached_etag");
			if(etag_field!=null){
				etag = etag_field.getStringValue();
				log.debug("ETag Header set to " + etag);
			}
			
			Field lm_field = item.getField("cached_lastmodified");
			if(lm_field != null){
				lastModified = lm_field.getStringValue();
				log.debug("Last-Modified Header set to " + lastModified);
			}
		}
		
		
		HttpClient client = new HttpClient();
		
		//Set up the proxy server if there is one.
		HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
		
		if(!proxy.getProxyServer().equals("")){
			log.debug("Setting Proxy server to " + proxy.getProxyServer()+ ":" + proxy.getProxyPort());
			client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
		}
		client.getParams().setConnectionManagerTimeout(2000);

		GetMethod get = new GetMethod(url);
		
		//Add the modification check headers if we have valid params.
		if(etag!=null && !etag.trim().equals("")){
			get.addRequestHeader(HTTP_IFNONEMATCH, etag);
		}
		
		if(lastModified!=null && !lastModified.trim().equals("")){
			get.addRequestHeader(HTTP_IFMODIFIED,lastModified);
		}
		
		get.setFollowRedirects(true);

		try {
			int code = client.executeMethod(get);
			
			if(code != HttpStatus.SC_NOT_MODIFIED){
				InputStream responseBody = get.getResponseBodyAsStream();
	
				XMLReader reader = new Parser();
				reader.setFeature(Parser.namespacesFeature, true);
				reader.setFeature(Parser.namespacePrefixesFeature, true);
	
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				DocumentResult dr = new DocumentResult();
				transformer.transform(new SAXSource(reader, new InputSource(responseBody)), 
						dr);
				doc=dr.getDocument();
				
				ret = new HttpDOMResponse(doc,get.getResponseHeaders());
				ret.setExistingItem(item);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			log.debug(ex.getMessage(), ex);
		} finally {
			get.releaseConnection();
		}
		
		return ret;
	}

	
	/**
	 * Method getPostBody.
	 * @return String
	 * @throws IOException
	 */
	@IPSJexlMethod(description="Gets posted body as string",
			params={})
			public String getPostBody() throws IOException 
			{
		PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		HttpServletRequest sreq = req.getServletRequest();

		BufferedReader br
		= new BufferedReader(
				new InputStreamReader(sreq.getInputStream()));

		StringBuilder sb = new StringBuilder();

		String str;
		while((str = br.readLine()) != null)
		{
			sb.append(str);
		}
		return sb.toString();
			}

	/**
	 * Method getPostDom.
	 * @return Document
	 * @throws IOException
	 */
	@IPSJexlMethod(description="Gets posted body as DOM",
			params={})
			public Document getPostDom() throws IOException 
			{
		PSRequest req = (PSRequest)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
		HttpServletRequest sreq = req.getServletRequest();

		Document document = null;
		SAXReader reader = new SAXReader();
		try
		{
			document = reader.read( sreq.getInputStream());
		}
		catch (DocumentException e)
		{
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}

		return document;
			}

	/**
	 * Method getDomFromString.
	 * @param string String
	 * @return Document
	 * @throws IOException
	 */
	@IPSJexlMethod(description="Gets Dom from String",
			params={@IPSJexlParam(name="string",description="the xml string ")})
			public Document getDomFromString(String string) throws IOException 
			{

		Document document = null;
		SAXReader reader = new SAXReader();

		try
		{
			document = reader.read( new StringReader(string));
		}
		catch (DocumentException e)
		{
			log.error(e.getMessage());
			log.debug(e.getMessage(), e);
		}

		return document;
			}


	/**
	 * Method xpathSelectSingleNode.
	 * @param doc Object
	 * @param xpathString String
	 * @param namespaces Map<String,String>
	 * @return Node
	 */
	@IPSJexlMethod(description="Uses xpath to search a document containing namespaces",
			params={@IPSJexlParam(name="node",description="the node or document"),
			@IPSJexlParam(name="xpath",description="the xpath string"),
			@IPSJexlParam(name="namespaces",description="a map of namespace prefixes")
	})
	public Node xpathSelectSingleNode(Object doc, String xpathString, Map<String,String> namespaces) {
		Node nd = (Node) doc;

		XPath xpath = nd.createXPath(xpathString);
		xpath.setNamespaceURIs( namespaces );
		
		return xpath.selectSingleNode(nd);          
	}

	/**
	 * Method xpathSelectNodes.
	 * @param doc Object
	 * @param xpathString String
	 * @param namespaces Map<String,String>
	 * @return List<?>
	 */
	@IPSJexlMethod(description="Uses xpath to search a document containing namespaces",
			params={@IPSJexlParam(name="node",description="the node or document"),
			@IPSJexlParam(name="xpath",description="the xpath string"),
			@IPSJexlParam(name="namespaces",description="a map of namespace prefixes")
	})
	public List<?> xpathSelectNodes(Object doc, String xpathString, Map<String,String> namespaces) {
		XPath xpath = DocumentHelper.createXPath(xpathString);
		xpath.setNamespaceURIs( namespaces );
		
		return xpath.selectNodes(doc);        
	}

	/**
	 * Method getItemXml.
	 * @param contentId int
	 * @return Document
	 */
	@IPSJexlMethod(description="Gets gets the xml for an existing content item by id",
			params={@IPSJexlParam(name="contentId",description="the id for the Item")})
			public Document getItemXml(int contentId) {
		IItemRestService itemservice = ItemRestServiceLocator.getItemServiceBase();
		/*	ItemRestService itemservice = JAXRSClientFactory.create("http://localhost:9992/Rhythmyx/services",ItemRestService.class,"admin1","demo",null);
		WebClient.client(itemservice).accept("text/xml");
		WebClient.client(itemservice).header("RX_USEBASICAUTH","true");
		 */
		Item item = itemservice.getItem(contentId);

		return ItemServiceHelper.getItemDOM(item);
	}

	
	/**
	 * Method getHttpAsDom.
	 * 
	 * This routine will try to download the specified URL.  If the server says the file hasn't changed, it will not be downloaded.
	 * 
	 * The resulting document will have a block of XML in the headers: namespace that represent the headers the server returns.
	 * This XML block can then be cached with the resulting item for subsequant calls to this routine.
	 * 
	 * This is to avoid the expensive process of downloading content unecessarily in Feed scenarios, when the Feed is updated but many of the items
	 * in the feed are not.
	 * 
	 * @param url String HTTP Resource to download
	 * @param keyfield Indicates the field in the system that stores the hash code for the specified URL
	 * @param contextRoot Indicates the path in the system where this item may already be stored.
	 * 
	 * @return Document
	 */
	@IPSJexlMethod(description="Extracts html from a url and convert to dom. Given the specified keyfield and contextroot, will attempt to find any items that match the url's has code in the keyfield and path.  If found any previous cached etag and/or last modified header data will be used whn checking to see if the item needs to be downloaded.",
			params={@IPSJexlParam(name="url",description="the url to connect to"),
			@IPSJexlParam(name="keyfield",description="The field containing the hash code for the specified URL."),
			@IPSJexlParam(name="contextroot",description="The root path within the system where this item may already be stored.")})

	public HttpHtmlResponse getWildHtmlAsDom(String url, String keyfield, String contextRoot) {
		HttpHtmlResponse ret = null;
		org.jsoup.nodes.Document doc = null;
		String etag=null;
		String lastModified=null;
		
		/** Before we do anything we need to check to see if this item exists. If it does
		 * we want to get the cached ETag and Last Modified headers to we don't download it unnecessarily. 
		 */
		//IItemRestService svc = ItemRestServiceLocator.getItemServiceBase();
		ItemRestServiceImpl svc = new ItemRestServiceImpl();
		
		Item item = svc.findByKeyField(Integer.toString(url.hashCode()),keyfield,contextRoot);
	
		if(item!=null && item.getContentId()!=null){
			log.debug("Located existing item for URL " + url);
			Field etag_field = item.getField("cached_etag");
			if(etag_field!=null){
				etag = etag_field.getStringValue();
				log.debug("ETag Header set to " + etag);
			}
			
			Field lm_field = item.getField("cached_lastmodified");
			if(lm_field != null){
				lastModified = lm_field.getStringValue();
				log.debug("Last-Modified Header set to " + lastModified);
			}
		}
		
		
		HttpClient client = new HttpClient();
		
		//Set up the proxy server if there is one.
		HTTPProxyClientConfig proxy = new HTTPProxyClientConfig();
		
		if(!proxy.getProxyServer().equals("")){
			log.debug("Setting Proxy server to " + proxy.getProxyServer()+ ":" + proxy.getProxyPort());
			client.getHostConfiguration().setProxy(proxy.getProxyServer(), Integer.parseInt(proxy.getProxyPort()));
		}
		client.getParams().setConnectionManagerTimeout(2000);

		GetMethod get = new GetMethod(url);
		
		//Add the modification check headers if we have valid params.
		if(etag!=null && !etag.trim().equals("")){
			get.addRequestHeader(HTTP_IFNONEMATCH, etag);
		}
		
		if(lastModified!=null && !lastModified.trim().equals("")){
			get.addRequestHeader(HTTP_IFMODIFIED,lastModified);
		}
		
		//Specify that we want UTF-8 content.
		get.addRequestHeader("Content-Type","text/xhtml; charset=UTF-8");
		
		get.setFollowRedirects(true);

		try {
			int code = client.executeMethod(get);
			
			if(code != HttpStatus.SC_NOT_MODIFIED){
		
				InputStream responseBody = get.getResponseBodyAsStream();
				
				log.debug("Response Character Set: " + get.getResponseCharSet());
				
				if(!get.getRequestCharSet().equals("UTF-8")){
					log.warn("Warning, source character set is not UTF-8");
				}
				
					doc = HtmlLinkHelper.convertLinksToAbsolute(url,Jsoup.parse(responseBody,"UTF-8", HtmlLinkHelper.getBaseLink(url)));
					
				
					ret = new HttpHtmlResponse(doc,get.getResponseHeaders());

				ret.setExistingItem(item);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
			log.debug(ex.getMessage(), ex);
		} finally {
			
			get.releaseConnection();
		}
		
		return ret;
	}

	/**
	 * Method getHttpAsDom.
	 * @param url String
	 * @return Document
	 */
	@IPSJexlMethod(description="Extracts html from a url and convert to dom",
			params={@IPSJexlParam(name="url",description="The base url"),
			@IPSJexlParam(name="html",description="The body content")})
	public String cleanRelativeLinks(String url, String html ){
		
		String ret = html;
		
		try {
			ret = HtmlLinkHelper.convertLinksToAbsolute(url, html);
		} catch (MalformedURLException e) {
			log.debug(e,e);
			ret = html;
			log.warn("An error occurred while cleaning relative links, content may still contain relative links.");
		} catch (URISyntaxException e) {
			log.debug(e,e);
			ret = html;
			log.warn("An error occurred while cleaning relative links, content may still contain relative links.");
		}
	
		return ret;
	}

	@IPSJexlMethod(description="Returns a SHA-1 hash for the specified string.",
			params={@IPSJexlParam(name="data",description="The data to hash")})
	public String getHash(String data){
		String ret = String.valueOf(data.hashCode());
	
	    try {
			MessageDigest checksum = MessageDigest.getInstance("SHA-256");
			checksum.reset();
			checksum.update(data.getBytes());
	    
			ret = asHex(checksum.digest());
	    } catch (NoSuchAlgorithmException e) {
			log.debug(e,e);
		}
	    return ret;
	}
	
    private static String asHex(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
            Integer.toString(( b[i] & 0xff ) + 0x100, 16).substring(1 );
        }
        return result;
    }
    
}
