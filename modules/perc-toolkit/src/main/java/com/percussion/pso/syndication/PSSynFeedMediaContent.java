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

import com.rometools.modules.mediarss.types.MediaContent;

import java.util.ArrayList;
import java.util.List;

/***
 * Provides a Velocity friendly wrapper for Media content.
 * 
 *   <media:content
               url="http://www.foo.com/movie.mov"
               fileSize="12216320"
               type="video/quicktime"
               medium="video"
               isDefault="true"
               expression="full"
               bitrate="128"
               framerate="25"
               samplingrate="44.1"
               channels="2"
               duration="185"
               height="200"
               width="300"
               lang="en" />
 * 
 * @author natechadwick
 *
 */
public class PSSynFeedMediaContent {

	private MediaContent content;

	/***
	 * channels is number of audio channels in the media object.
	 * @return
	 */
	public Integer getAudioChannels() {
		Integer ret = 0;

		if(content!=null)
			if(content.getAudioChannels()!=null)
				ret = content.getAudioChannels();
	
		return ret;
	}
	
	/***
	 * bitrate is the kilobits per second rate of media.
	 * @return
	 */
	public Float getBitrate() {
		Float ret = (float) 0;
		
		if(content!=null)
			if(content.getBitrate()!=null)
				ret = content.getBitrate();
		
		return ret;
		
	}
    
	/***
	 * duration is the number of seconds the media object plays.
	 * @return
	 */
	public Long	getDuration() {
		Long ret = (long) 0;
		
		if(content!=null)
			if(content.getDuration()!=null)
				ret = content.getDuration();

		return ret;
	}
    
	/***
	 * expression determines if the object is a sample or the full version of the object, or even if it is a continuous stream (sample | full | nonstop).
	 * @return
	 */
	public String getExpression() {
		String ret = "";
		
		if(content!=null)
			if(content.getExpression()!=null)
				ret = content.getExpression().toString();
		
		return ret;
	}
    
	/***
	 * fileSize is the number of bytes of the media object.
	 * @return
	 */
	public Long	getFileSize() {
		Long ret = (long)0;

		if(content!=null)
			if(content.getFileSize()!=null)
				ret = content.getFileSize();

		return ret;
	}
    
	/***
	 * framerate is the number of frames per second for the media object.
	 * @return
	 */
	public Float getFramerate() {
		Float ret = (float)0;

		if(content!=null)
			if(content.getFramerate()!=null)
				ret = content.getFramerate();

		return ret;
	}
    
	/***
	 * height is the height of the media object.
	 * @return
	 */
	public Integer getHeight() {
		Integer ret = 0;
		
		if(content!=null)
			if(content.getHeight()!=null)
				ret = content.getHeight();
		
		return ret;
	}

	/***
	 * lang is the primary language encapsulated in the media object.
	 * @return
	 */
    public String getLanguage() {
    	String ret = "";
    	
    	if(content!=null)
    		if(content.getLanguage()!=null)
    			ret = content.getLanguage();
    	return ret;
    }
    
    /***
     * The player or URL reference for the item
     * <media:player>
     * @return
     */
    public String getPlayerUrl(){
    	String ret = "";
    	
    	if(content!=null)
    		if(content.getPlayer()!=null)
    			if(content.getPlayer().getUrl()!=null)
    				ret = content.getPlayer().getUrl().toString();
    	return ret;
    }

    public String getTargetUrl(){
    	String ret = "";
    	
    	if(content!=null)
    		if(content.getReference()!=null)
    			ret = content.getReference().toString();
    	return ret;
    }
    
    /***
     * samplingrate is the number of samples per second taken to create the media object.
     * @return
     */
public Float getSamplingrate() {
	Float ret = (float) 0;
		
	if(content!=null)
		if(content.getSamplingrate()!=null)
			ret = content.getSamplingrate();
	
	return ret;
}
    
/***
 * type is the standard MIME type of the object.
 * @return
 */
public String	getType() {
	String ret = "";
	
	if(content!=null)
		if(content.getType()!=null)
			ret = content.getType();
	
	return ret;
}

/***
 * Width is the width of the media object.
 * @return
 */
public Integer	getWidth() {
	Integer ret = 0;
	
	if(content!=null)
		if(content.getWidth()!=null)
			ret =  content.getWidth();

	return ret;
}

/***
 * isDefault determines if this is the default object that should be used
 * @return
 */
public boolean isDefaultContent() {
	boolean ret = false;
	
	if(content!=null)
		ret = content.isDefaultContent();
	
	return ret;
}
    //Meta Data	
	/***
	 * <media:category>
	 */
	public List<PSSynFeedCategory> getCategoriesList() {
		 ArrayList<PSSynFeedCategory> ret = new ArrayList<PSSynFeedCategory>();
		 
		 for(int i=0;i<content.getMetadata().getCategories().length;i++){
			 ret.add(new PSSynFeedCategory(content.getMetadata().getCategories()[i]));
		 }
		 
		 return ret;
	 }
     
	 public String getCategories(){
		 String ret = "";
		 
		 if(content!=null)
			 if(content.getMetadata()!=null)
				 if(content.getMetadata().getCategories()!=null)
					 for(int i=0;i<content.getMetadata().getCategories().length;i++){
						 if(ret != "")
							 ret = ret +" , "; 

			 ret = ret + (new PSSynFeedCategory(content.getMetadata().getCategories()[i])).toString();
		 }
		 
		 return ret;
	 }
	 
	 /***
	  * <media:copyright>
	  * @return
	  */
	 public String getCopyright() {
		 String ret = "";
		 
		 if(content!=null)
			 if(content.getMetadata()!=null)
				 if(content.getMetadata().getCopyright()!=null)
					ret =  content.getMetadata().getCopyright();
		 
		 return ret;
	 }
	
	 /***
	  * <media:credit>
	  * @return
	  */
	 public List<PSSynFeedCredit> getCreditList(){ 
		 ArrayList<PSSynFeedCredit> ret = new ArrayList<PSSynFeedCredit>();

		 for(int i=0;i<content.getMetadata().getCredits().length;i++){
			 ret.add(new PSSynFeedCredit(content.getMetadata().getCredits()[i]));
		 }
		 return ret;
	 }

	 public String getCredits(){
		 String ret = "";
		 
		 if(content!=null)
			 if(content.getMetadata()!=null)
				 if(content.getMetadata().getCredits()!=null)
					 for(int i=0;i<content.getMetadata().getCredits().length;i++){
						 if(ret != "")
							 ret = ret +" , "; 

			 ret = ret + (new PSSynFeedCredit(content.getMetadata().getCredits()[i])).toString();
		 }
		 
		 return ret;
	 }
	 
 /***
  * <media:copyright>
  * @return
  */
public String getCopyrightUrl() {
	String ret = "";
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getCopyrightUrl()!=null)
				ret = content.getMetadata().getCopyrightUrl().toString();
	return ret;
}
    
/***
 * <media:description>
 * @return
 */
public String getDescription() {
	String ret = "";
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getDescription()!=null)
				ret = content.getMetadata().getDescription();

	return ret;
}
     
/***
 * 
 * @return
 */
public String getDescriptionType() {
	String ret="";
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getDescriptionType()!=null)
				ret = content.getMetadata().getDescriptionType();
	return ret;
}

/***
 *    <media:hash>
 * @return
 */
public String getHash(){
	String ret = "";
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getHash()!=null)
				if(content.getMetadata().getHash().getValue()!=null)
					ret = content.getMetadata().getHash().getValue();

	return ret;
}

/***
 * Returns the Algorith used to creat the hash. 
 * @return
 */
public String getHashAlgorithm(){
	String ret = "";
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getHash()!=null)
				if(content.getMetadata().getHash().getAlgorithm()!=null)
					ret = content.getMetadata().getHash().getAlgorithm();
	return ret;
}

/***
 *  <media:keywords>
 * @return
 */
public List<String> getKeywordsList() {
	ArrayList<String> ret = new ArrayList<String>();
	
	for(int i=0;i<content.getMetadata().getKeywords().length;i++){
		ret.add(content.getMetadata().getKeywords()[i]);
	}
	return ret;
}

public String getKeywords() {
	String ret = "";
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getKeywords()!=null){
			
					for(int i=0;i<content.getMetadata().getKeywords().length;i++){
						if(ret == ""){
							ret = content.getMetadata().getKeywords()[i];
						}else{
							ret = ret + ", " + content.getMetadata().getKeywords()[i];
						}
					}			
			}

	return ret;
}
    
/***
 * <media:rating>
 * @return
 */
public List<PSSynFeedRating>getRatingsList() {
	ArrayList<PSSynFeedRating> ret = new ArrayList<PSSynFeedRating>();
	
	for(int i=0;i<content.getMetadata().getRatings().length;i++){
		ret.add(new PSSynFeedRating(content.getMetadata().getRatings()[i]));
	}
	
	return ret;
}
     
public String getRatings(){
	return ""; //@TODO: Implement Me
}

/***
 *  <media:restriction>
 * @return
 */
public List<PSSynFeedRestriction> getRestrictionsList() {
	ArrayList<PSSynFeedRestriction> ret = new ArrayList<PSSynFeedRestriction>();
	
	for(int i=0;i<content.getMetadata().getRestrictions().length;i++){
		ret.add(new PSSynFeedRestriction(content.getMetadata().getRestrictions()[i]));
	}
	return ret;
}
    
public String getRestrictions(){
	return ""; //@TODO: Implement Restrictions.
}
/***
 * <media:text>

Allows the inclusion of a text transcript, closed captioning, or lyrics of the media content. Many of these elements are permitted to provide a time series of text. In such cases, it is encouraged, but not required, that the elements be grouped by language and appear in time sequence order based on the start time. Elements can have overlapping start and end times. It has 4 optional attributes.

        <media:text type="plain" lang="en" start="00:00:03.000"
        end="00:00:10.000"> Oh, say, can you see</media:text>

        <media:text type="plain" lang="en" start="00:00:10.000"
        end="00:00:17.000">By the dawn's early light</media:text>
 
type specifies the type of text embedded. Possible values are either 'plain' or 'html'. Default value is 'plain'. All html must be entity-encoded. It is an optional attribute.

lang is the primary language encapsulated in the media object. Language codes possible are detailed in RFC 3066. This attribute is used similar to the xml:lang attribute detailed in the XML 1.0 Specification (Third Edition). It is an optional attribute.

start specifies the start time offset that the text starts being relevant to the media object. An example of this would be for closed captioning. It uses the NTP time code format (see: the time attribute used in <media:thumbnail>). It is an optional attribute.

end specifies the end time that the text is relevant. If this attribute is not provided, and a start time is used, it is expected that the end time is either the end of the clip or the start of the next <media:text> element.
 * @return
 */
public String getTranscript() {
	//@TODO: Implement me.
	return "";
}
     
public String getTranscriptStart(){
	return "";
}

public String getTranscriptEnd(){
	return "";
}

public String getTranscriptType(){
	return "";
}

/***
 * <media:thumbnail>
 * @return
 */
public String getThumbnailUrl() {
	
	String ret = "";
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getThumbnail().length>0)
				if(content.getMetadata().getThumbnail()[0]!=null)
					if(content.getMetadata().getThumbnail()[0].getUrl()!=null)	
						ret = content.getMetadata().getThumbnail()[0].getUrl().toString();
	
	return ret;
}
    
public Integer getThumbnailHeight(){
	Integer ret = 0;
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getThumbnail().length>0)				
				if(content.getMetadata().getThumbnail()[0]!=null)
					if(content.getMetadata().getThumbnail()[0].getHeight()!=null)	
						ret = content.getMetadata().getThumbnail()[0].getHeight();
	return ret;
}

public Integer getThumbnailWidth(){
	Integer ret = 0;
	
	if(content!=null)
		if(content.getMetadata()!=null)
			if(content.getMetadata().getThumbnail().length>0)
				if(content.getMetadata().getThumbnail()[0]!=null)
					if(content.getMetadata().getThumbnail()[0].getWidth()!=null)	
						ret = content.getMetadata().getThumbnail()[0].getWidth();
	return ret;
}

public String getFilename(){
	String ret = "";

	if(content!=null)
		if(content.getReference()!=null){
					String url = content.getReference().toString();
					if(url!=null && url.trim()!=""){
						url = url.replace("\\","/");
						if(url.lastIndexOf("/") == url.length()){
							url = url.substring(0,url.length()-1);
						}
						
						ret = url.substring(url.lastIndexOf("/")+1);
					}
		}
	return ret;
	
}

public String getFileExtension(){
	String ret = "";

	String t = getFilename();
	if(t.lastIndexOf(".")>0){
		ret = t.substring(t.lastIndexOf("."));
	}
	return ret;
}
public String getThumbnailFilename(){
String ret = "";

if(content!=null)
	if(content.getMetadata()!=null)
		if(content.getMetadata().getThumbnail()!=null)
			if(content.getMetadata().getThumbnail().length>0)
				if(content.getMetadata().getThumbnail()[0].getUrl()!=null){
					String url = content.getMetadata().getThumbnail()[0].getUrl().toString();
					if(url!=null && url.trim()!=""){
						url = url.replace("\\","/");
						if(url.lastIndexOf("/") == url.length()){
							url = url.substring(0,url.length()-1);
						}
						
						ret = url.substring(url.lastIndexOf("/")+1);
				}
 }

return ret;
}

public String getTitle() {
	return content.getMetadata().getTitle();
}

/***
 * <media:title>
 * @return
 */
public String	getTitleType() {
	return content.getMetadata().getTitleType();
}
	
public PSSynFeedMediaContent(MediaContent arg){
		content = arg;
	}
}
