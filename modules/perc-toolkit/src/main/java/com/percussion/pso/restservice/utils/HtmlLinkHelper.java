/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


/***
 * Provides helper methods for processing links in
 * text. 
 * 
 * @author natechadwick
 *
 */
public class HtmlLinkHelper {

	
	private static final Logger log = LogManager.getLogger(HtmlLinkHelper.class);
	
	/***
	 * Given the specified base url, will convert the supplied link from relative
	 * to abolute and return a string the result.
	 * @param base
	 * @param src
	 * @return An absolute version of the supplied url
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static String convertToAbsoluteLink(String base, String src) throws URISyntaxException, MalformedURLException{ 		
	
	URI u = new URI(base).parseServerAuthority();
	
	URI t = u.resolve(src);
	return t.toASCIIString();
	}
	
		
	/***
	 * Returns the base link 
	 * @param link
	 * @return
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static String getBaseLink(String link) throws URISyntaxException, MalformedURLException{
		
		URI u = new URI(link).parseServerAuthority();
	
		return u.getScheme() + "://" + u.getAuthority() + "/";
	}
	
	
	public static org.jsoup.nodes.Document convertLinksToAbsolute(String link, org.jsoup.nodes.Document doc) throws MalformedURLException, URISyntaxException{
		  
		doc.setBaseUri(getBaseLink(link));
		
		Elements links = doc.select("a");
			
		for(Element e : links){
			e.setBaseUri(doc.baseUri());
			
			//Skip internal book marks.
			if(!e.attr("href").startsWith("#")){
				e.attr("href", e.attr("abs:href"));
			}
		}
		
	    links = doc.select("img");
	    for(Element e : links){
	    	e.setBaseUri(doc.baseUri());
			e.attr("src", e.attr("abs:src"));
		}
		 
	    links = doc.select("script");
	    for(Element e : links){
	    	e.setBaseUri(doc.baseUri());
			e.attr("src", e.attr("abs:src"));
		}
	    
	    links = doc.select("link");
	    for(Element e : links){
	    	e.setBaseUri(doc.baseUri());
			e.attr("href", e.attr("abs:href"));
		}
	    
	   
	   return doc;
	
	}
	
	/***
	 * Converts all links in the specified string to absolute.
	 * @param link
	 * @param text
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static String convertLinksToAbsolute(String link, String text) throws MalformedURLException, URISyntaxException{
		  
		org.jsoup.nodes.Document doc = Jsoup.parse(text);

		return convertLinksToAbsolute(link, doc).html();
	}	
	
}
