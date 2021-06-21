package com.percussion.pso.jexl;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.syndication.PSSynFeedProxy;
import com.percussion.pso.utils.PSOEmailUtils;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.MediaGroup;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Provides tools to work with Feeds such as RSS or Atom
 *
 */
public class PSOFeedTools extends PSJexlUtilBase implements IPSJexlExpression {

	  /**
	    * Logger for this class
	    */

	private static final Logger log = LogManager.getLogger(PSOFeedTools.class);
	 
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
	         params={@IPSJexlParam(name="url", description="The URL of the feed to download.")})
	   public PSSynFeedProxy getFeed(String urlString) throws IllegalArgumentException, FeedException, IOException {
		 return new PSSynFeedProxy(urlString);
	 	}

	 @IPSJexlMethod(description="Returns a ROME SyndFeed instance for the given URL.  Supports Optional GET based upon cached ETAG and LastModified Date.  Do not use unless cacheing these values.",
	         params={@IPSJexlParam(name="url", description="The URL of the feed to download."),
			 		 @IPSJexlParam(name="eTag", description="A cached HTTP ETAG Headerfor the feed. "),
			 		 @IPSJexlParam(name="lastModified", description="A cached HTTP LastModified Header for the feed. ")})
	   public PSSynFeedProxy getFeed(String urlString, String eTag, String lastModified) throws IllegalArgumentException, FeedException, IOException {

		 return new PSSynFeedProxy(urlString,eTag,lastModified);

	 }

	 @IPSJexlMethod(description="Does a little cleanup of the sys_title.  URL encoding it if possible.",
	         params={@IPSJexlParam(name="title", description="The sys_title to cleanup")})
  	  public String cleanupItemTitle(String title){
		 String ret=title;
		 URLCodec codec = new URLCodec();

		 ret = ret.replace(" ", "_");
		 try{
			 ret = codec.encode(ret);
		 }catch(Exception ex){
			 log.error("Url encoding issue in cleanupItemTitle Error: {}", ex.getMessage());
			 log.debug(ex.getMessage(),ex);
		 }
		 return ret;
	 }

	 @IPSJexlMethod(description="Returns a ROME MediaEntryModule to handle Media RSS content.",
	         params={@IPSJexlParam(name="entry", description="A ROME SyndEntry instance.")})
	  public List<MediaContent> getMediaRSSModuleContent(Object entry){
		  MediaEntryModule mediaModule = null;
		  SyndEntry item = (SyndEntry) entry;
		  List<MediaContent> contents = new ArrayList<MediaContent>();

		  mediaModule = (MediaEntryModule) item.getModule(MediaEntryModule.URI);
		  if (mediaModule!=null && mediaModule instanceof MediaEntryModule ){
		        MediaEntryModule mentry = (MediaEntryModule ) mediaModule;


		        for (MediaGroup mg : mentry.getMediaGroups()) {
		            for (MediaContent mc : mg.getContents()) {
		            	contents.add(mc);
		            }
		        }
		  }
		  return contents;
	  }

	 @IPSJexlMethod(description="Returns a boolean if the specified module is available.",
			 params ={@IPSJexlParam(name="entry", description="A ROME SyndEntry instance.")})
	  public boolean isMediaRSSContentAvailable(Object entry){
		  boolean ret=false;
		  SyndEntry e = (SyndEntry) entry;

		 if(e!=null){
			 MediaEntryModule mediaModule = (MediaEntryModule) e.getModule(MediaEntryModule.URI);

			 //First check to make sure there is Media RSS content.
			 if (mediaModule instanceof MediaEntryModule ){

				 //Now check at the content level.
				 if(mediaModule.getMediaContents().length>0){
					 ret = true;
				 }
	
				  //Now check at the group level for alternative formats, could be either or.
				  if(mediaModule.getMediaGroups().length>0){
					  for(int i = 0;i<mediaModule.getMediaGroups().length;i++){
						  if(mediaModule.getMediaGroups()[i].getContents().length>0){
							  ret = true;
							  break;
						  }
					  }
				  }
			  }
		  }
		  return ret;
	  }

	 @IPSJexlMethod(description="Returns the base filename portion for a given url.",
			 params ={@IPSJexlParam(name="entry", description="A valid url")})
	 public String getBaseFilename(String url){
		 String ret = "";

		 if(url!=null && url.trim()!=""){
			url = url.replace("\\","/");
			if(url.lastIndexOf("/") == url.length()){
				url = url.substring(0,url.length()-1);
			}

			ret = url.substring(url.lastIndexOf("/"));
		 }
		 return ret;
	 }

	 @IPSJexlMethod(description="Returns a folder name of the right length.",
			 params ={@IPSJexlParam(name="folder", description="Folder name")})
	 public String getCleanFoldername(String folder){
		 if(folder.length()>100) {
			 return folder.substring(0, 100);
		 }
		 else {
			 return folder;
		 }
	 }


	 @IPSJexlMethod(description="Sends an email via velocity.",
			 params ={@IPSJexlParam(name="from_line", description="Comma seperated list of email addresses from which the email is sent."),
			 @IPSJexlParam(name="to_line", description="Comma seperated list of email addresses to send message to."),
			 @IPSJexlParam(name="cc_line", description="Comma seperated list of email addresses to be carbon copied on the message."),
			 @IPSJexlParam(name="bcc_line", description="Comma seperated list of email addresses to be blind copied on the message."),
			 @IPSJexlParam(name="subject", description="The subject of the message."),
			 @IPSJexlParam(name="body", description="The text of the message.")
	 	})
	 public void sendEmail(String from_line, String to_line, String cc_line, String bcc_line, String subject, String body ){

		 PSOEmailUtils.sendEmail(from_line, to_line, cc_line, bcc_line, subject, body);

	 }
}
