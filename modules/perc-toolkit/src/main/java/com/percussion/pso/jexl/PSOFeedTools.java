package com.percussion.pso.jexl;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/***
 * Provides tools to work with Feeds such as RSS or Atom
 * @author natechadwick
 *
 */
public class PSOFeedTools extends PSJexlUtilBase implements IPSJexlExpression {

	  /**
	    * Logger for this class
	    */
	private static final Log log = LogFactory.getLog(PSOFeedTools.class);
	 
	public PSOFeedTools(){
		super();
	}
	
	 @IPSJexlMethod(description="Returns a Map of Feed parameters from a Jquery formatted feed. For example a post of url: 'http://rss.news.yahoo.com/rss/yahoonewsroom', targetFolder: '//Sites/EnterpriseInvestments/News'", 
	         params={@IPSJexlParam(name="params", description="The feed parameters")})
	   public Map<String,String> getFeedParameters(String params) throws IllegalArgumentException {

		 log.debug(params);
		 URLCodec decode = new URLCodec();
		 HashMap<String,String> map = new HashMap<>();
		 
		 
		 String[] opts = params.split("&");
		 String[] param;
		 for(String s : opts){
			 param = s.split("=");
			 
			 try {
				map.put(decode.decode(param[0].trim()), decode.decode(param[1].trim()));
			} catch (DecoderException e) {
				log.debug(params,e);
				log.error(e.getMessage());
			}
		 }
		 
		return map;
	 }

	
	 @IPSJexlMethod(description="Returns a ROME SyndFeed instance for the given URL", 
	         params={@IPSJexlParam(name="url", description="the item GUID")})
	   public SyndFeed getFeed(String urlString) throws IllegalArgumentException, FeedException, IOException {

			URL feedUrl = new URL(urlString);
			SyndFeedInput input = new SyndFeedInput();
			log.debug("Requesting feed from " + feedUrl);
			return input.build(new XmlReader(feedUrl));
	 }
	
}
