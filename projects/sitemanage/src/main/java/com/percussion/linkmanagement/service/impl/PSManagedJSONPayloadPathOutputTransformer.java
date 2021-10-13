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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.linkmanagement.service.impl;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.linkmanagement.service.IPSManagedLinkService;
import com.percussion.server.IPSRequestContext;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;

/**
 * Field output transformer to update the managed item paths on edit. This is a thin wrapper, calls the managedlink service to do the actual work.
 * This version follows the JSONPayload spec. 
 * 
 * @author Nate Chadwick
 *
 */
public class PSManagedJSONPayloadPathOutputTransformer extends PSDefaultExtension implements IPSFieldOutputTransformer
{
	private static final Logger log = LogManager.getLogger(PSManagedJSONPayloadPathOutputTransformer.class);
	
    private IPSManagedLinkService service;
    
    /*
     * (non-Javadoc)
     * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
     */
    @Override
    public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException
    {
        PSExtensionParams ep = new PSExtensionParams(params);
        String jsonPayload = ep.getStringParam(0, null, true);

        //Fix Old Data for Image Slider
        if(request != null && "percImageSlider.xml".equalsIgnoreCase(request.getRequestPage())){
        	if(jsonPayload != null){
				jsonPayload = jsonPayload.replaceAll(IPSManagedLinkService.PERC_OLD_IMAGE_SLIDER_CONFIG_ATTR,IPSManagedLinkService.PERC_CONFIG);
				jsonPayload = jsonPayload.replaceAll(IPSManagedLinkService.PERC_OLD_IMAGE_SLIDER_IMAGEPATH_ATTR,IPSManagedLinkService.PERC_IMAGEPATH);
				log.info("Updated Old data in ImageSlider");
			}
        }
   
        JSONObject object = null;
        String cid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
	
        if(log.isDebugEnabled()) {
			log.debug("Processing with Content Id:" + cid);
		}
        try {
        	 if(log.isDebugEnabled()) {
				 log.debug("Parsing JSON Payload" + jsonPayload);
			 }
        	 
        	 if(StringUtils.isEmpty(jsonPayload)) {
				 return "";
			 }
        	 
        	object = new JSONObject(jsonPayload);
       	 	if(log.isDebugEnabled()) {
				log.debug("Done parsing payload, parsing " + IPSManagedLinkService.PERC_CONFIG + " array.");
			}
       	 
       	 
	        JSONArray objectArray = object.getJSONArray(IPSManagedLinkService.PERC_CONFIG);
	     	if(log.isDebugEnabled()) {
				log.debug("Done parsing payload array");
			}
       	    
	        String newPath = "";
	        
	        for (int i = 0; i < objectArray.length(); i++) {
	            JSONObject entry = objectArray.getJSONObject(i);
	        	if(log.isDebugEnabled()) {
					log.debug("Processing payload entry " + i);
				}
	  
	            //Images
	            if(entry.has(IPSManagedLinkService.PERC_IMAGEPATH)){
	            	if(entry.has(IPSManagedLinkService.PERC_IMAGEPATH_LINKID)){
	            		if(!StringUtils.isBlank(entry.getString(IPSManagedLinkService.PERC_IMAGEPATH_LINKID))
	            				&& (!StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))){
	            			if(log.isDebugEnabled()) {
								log.debug("Getting updated path for Image entry: " + entry.getString(IPSManagedLinkService.PERC_IMAGEPATH_LINKID) +
										" with current path of " + entry.get(IPSManagedLinkService.PERC_IMAGEPATH));
							}
	            			newPath = renderItemPath(entry.getString(IPSManagedLinkService.PERC_IMAGEPATH_LINKID));
	            			if(log.isDebugEnabled()) {
								log.debug("Updating payload for Image entry: " + entry.getString(IPSManagedLinkService.PERC_IMAGEPATH_LINKID) + " with new path of " + newPath);
							}
	            			
	            			entry.put(IPSManagedLinkService.PERC_IMAGEPATH, newPath);
		            		objectArray.put(i,entry);
		            		if(log.isDebugEnabled()) {
								log.debug("Done updating.");
							}
	            		}
	            	}
	            }
	            
	            //Files
	            if(entry.has(IPSManagedLinkService.PERC_FILEPATH)){
	            	if(entry.has(IPSManagedLinkService.PERC_FILEPATH_LINKID)){
	            		if(!StringUtils.isBlank(entry.getString(IPSManagedLinkService.PERC_FILEPATH_LINKID))
	            				&& (!StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))){
	            			if(log.isDebugEnabled()) {
								log.debug("Getting updated path for File entry: " + entry.getString(IPSManagedLinkService.PERC_FILEPATH_LINKID) +
										" with current path of " + entry.get(IPSManagedLinkService.PERC_FILEPATH));
							}
		            		newPath = renderItemPath(entry.getString(IPSManagedLinkService.PERC_FILEPATH_LINKID));
		            		if(log.isDebugEnabled()) {
								log.debug("Updating payload for File entry: " + entry.getString(IPSManagedLinkService.PERC_FILEPATH_LINKID) + " with new path of " + newPath);
							}
	            			
		              		entry.put(IPSManagedLinkService.PERC_FILEPATH, newPath);
		            		objectArray.put(i,entry);
		            		if(log.isDebugEnabled()) {
								log.debug("Done updating.");
							}
	            		}
	            	}
	            }
	            
	            //Pages
	            if(entry.has(IPSManagedLinkService.PERC_PAGEPATH)){
	            	if(entry.has(IPSManagedLinkService.PERC_PAGEPATH_LINKID)){
	            		if(!StringUtils.isBlank(entry.getString(IPSManagedLinkService.PERC_PAGEPATH_LINKID))
	            				&& (!StringUtils.isBlank(cid) || !StringUtils.isNumeric(cid))){
	            			if(log.isDebugEnabled()) {
								log.debug("Getting updated path for Page entry: " + entry.getString(IPSManagedLinkService.PERC_PAGEPATH_LINKID) +
										" with current path of " + entry.get(IPSManagedLinkService.PERC_PAGEPATH));
							}
		            		newPath = renderItemPath(entry.getString(IPSManagedLinkService.PERC_PAGEPATH_LINKID));
		            		if(log.isDebugEnabled()) {
								log.debug("Updating payload for File entry: " + entry.getString(IPSManagedLinkService.PERC_PAGEPATH_LINKID) + " with new path of " + newPath);
							}
		            		entry.put(IPSManagedLinkService.PERC_PAGEPATH, newPath);
		            		objectArray.put(i,entry);
		            		if(log.isDebugEnabled()) {
								log.debug("Done updating.");
							}
	            		}
	            	}
	            }
	        }
	     
	        if(log.isDebugEnabled()) {
				log.debug("Updating return payload.");
			}
	        object.put(IPSManagedLinkService.PERC_CONFIG, objectArray);
	        if(log.isDebugEnabled()) {
				log.debug("Done updating.");
			}
		} catch (JSONException ex) {
			log.error("An error occurred while trying to manage links in a JSONPayload field." );
			if(log.isDebugEnabled()) {
				log.debug("Error occurred.  Returning original payload: " + jsonPayload, ex);
			}
			return jsonPayload;
		}
		
    	if(log.isDebugEnabled()) {
			log.debug("Returning updated payload with any managed path updates: " + object.toString());
		}
      return object.toString();

    }
    
    private String renderItemPath(String linkId){
    	return service.renderItemPath(null, linkId);
    }
    
    /* (non-Javadoc)
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
        super.init(def, codeRoot);
        //This is for wiring the services
        PSSpringWebApplicationContextUtils.injectDependencies(this);

    }


    /**
     * Setter for dependency injection
     * 
     * @param service the service to set
     */
    public void setService(IPSManagedLinkService service)
    {
        this.service = service;
    }
}
