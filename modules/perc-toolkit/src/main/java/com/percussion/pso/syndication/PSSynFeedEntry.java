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
package com.percussion.pso.syndication;

import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.feed.synd.SyndPerson;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/****
 * Provides Velocity friendly methods for a 
 * Syndication Feed entry.
 * 
 * @author natechadwick
 */
public class PSSynFeedEntry {

	private SyndEntry entry;
	
	/***
	 * Returns the name of the first entry author in the collection of authors.
	 * @return
	 */
	 public String	getAuthor() {
		 String ret = "";
		 
		 if(entry!=null)
			 if(entry.getAuthor()!=null)
		 		 ret=entry.getAuthor();
		 return ret;
	 }
     
	 /***
	  * Returns the entry authors.
	  * @return
	  */
	 public List getAuthorsList() {
		 return entry.getAuthors();
	 }
	 
	 /***
	  * Returns a comma seperated list of the entry authors.
	  * @return
	  */
	 public String getAuthors(){
		 String ret="";
			Object a;
			
			//@TODO: Add Atom Support
			for(int i=0;i<entry.getAuthors().size();i++){
				a = entry.getAuthors().get(i);
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
			return entry.getCategories();
		}

		/***
		 * Returns the feed categories as a comma separated string.
		 * @return
		 */
		public String getCategories(){
			String ret="";
			SyndCategory a;
		
			for(int i=0;i<entry.getCategories().size();i++){
				a = (SyndCategory)entry.getCategories().get(i);
					if(ret=="")
						ret = a.getName();
					else
						ret.concat("," + a.getName());
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
			for(int i=0;i<entry.getContributors().size();i++){
				a = entry.getContributors().get(i);
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
			return entry.getContributors();
		}
		     
	    /***
	     * Returns the entry contents.
	     * @return
	     */
	    public String getContents(){
	    	String ret = "";
	    	
	    	for(int i=0;i<entry.getContents().size();i++){
	    		if(ret =="")
	    			ret = ((SyndContent)entry.getContents().get(i)).getValue();
	    		else
	    			ret = ret + "\r\n" + entry.getContents().get(i).getValue();
	    	}
	    	return ret;
	    }
	 
	    /***
	     * Returns the entry description.
	     * @return
	     */
	    public String getDescription(){
	    	String ret = "";
	    	
	    	if(entry!=null)
	    		if(entry.getDescription()!=null)
	    			if(entry.getDescription().getValue()!=null)
	    				ret = entry.getDescription().getValue();
	    	return ret;
	    }
     
public List<PSSynFeedEnclosure> getEnclosures() {
	ArrayList<PSSynFeedEnclosure> ret = new ArrayList<PSSynFeedEnclosure>();

	for(int i=0;i<entry.getEnclosures().size();i++){
		ret.add(new PSSynFeedEnclosure((SyndEnclosure)entry.getEnclosures().get(i)));
	}
	return ret;
}
    
/***
 *  Returns the entry link.
 */
public String getLink() {
	String ret = "";
	
	if(entry!=null)
		if(entry.getLink()!=null)
			ret=entry.getLink();

	return ret;
}

/***
 * Returns the entry links
 * @return
 */
public List<SyndLink> getLinks(){
	List<SyndLink> links = entry.getLinks();
	return links;
}
     
public List<PSSynFeedMediaContent> getMediaRSSContent(){
	  MediaEntryModule mediaModule = null;

	  List<PSSynFeedMediaContent> contents = new ArrayList<PSSynFeedMediaContent>();
	  
	  mediaModule = (MediaEntryModule) entry.getModule(MediaEntryModule.URI);
	  if (mediaModule!=null && mediaModule instanceof MediaEntryModule ){
	        MediaEntryModule mentry = (MediaEntryModule ) mediaModule;

	        for (MediaContent mc : mentry.getMediaContents()) {
	            	contents.add(new PSSynFeedMediaContent(mc));
	            
	        }
	  }
	  return contents;
}

/***
 * Returns the entry published date.
 * @return
 */
public Date	getPublishedDate() {
	return entry.getPublishedDate();
}
     
/***
 *  Returns the entry title. 
 * @return
 */
public String getTitle() {
	String ret="";
	if(entry!=null)
		if(entry.getTitle()!=null)
			ret=entry.getTitle();
	return ret;
}
  
/***
 *  Returns the entry updated date.
 * @return
 */
public Date getUpdatedDate() {
	if(entry.getUpdatedDate()!=null){
		return entry.getUpdatedDate();
	}else{
		 Calendar calendar = Calendar.getInstance();
		 
		 return calendar.getTime();
	}
}
    
public String	getUri() {
	return entry.getUri();
}

public PSSynFeedEntry(SyndEntry arg){
	entry = arg;
}
}
