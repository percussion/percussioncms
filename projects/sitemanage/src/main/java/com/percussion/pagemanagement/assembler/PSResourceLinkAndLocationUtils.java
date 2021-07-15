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
package com.percussion.pagemanagement.assembler;

import static org.apache.commons.lang.StringUtils.containsAny;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.pagemanagement.data.PSRenderLink;
import com.percussion.pagemanagement.data.PSRenderLinkContext.Mode;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;
import com.percussion.pagemanagement.data.PSResourceLocation;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.share.data.IPSLinkableContentItem;
import com.phloc.commons.url.URLValidator;

/**
 * 
 * Static utility methods for resource locations
 * and links.
 * 
 * @author adamgent
 *
 */
public class PSResourceLinkAndLocationUtils
{

    /**
     * URL escapes a path string while keeping the slashes
     * of the path. The slashes ('<code>/</code>') will not
     * be escaped.
     * <p>
     * <strong>This is url escaping for links not encoding
     * for forms.</strong> 
     * In other words it is not <code>application/x-www-form-urlencoded</code>
     * but rather URI escaping.
     * 

     * @param path never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static String escapePathForUrl(String path) {
        notNull(path);
        try
        {
            URI uri = new URI("http","localhost",path, null);
            return uri.getRawPath();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Bad path",e);
        }
    }
    
    /**
     * Concatenates a path by adding the appropriate path
     * separator between each argument
     * @param start never <code>null</code> or empty.
     * @param end folder parts.
     * @return never <code>null</code>.
     */
    public static String concatPath(String start, String ... end) {
        isTrue(isNotBlank(start), "start cannot be blank");
        notEmpty(end, "Must have end paths.");
        String path = start;
        for (String p : end ) {
            if (isNotBlank(p)) {
				path = removeEnd(path, "/") + "/" + removeStart(p, "/");
			}
        }
        if ("/".equals(path)){
        	return path;}

			return removeEnd(path, "/");

    }
    
    /**
     * Creates a single link and location for the given filename.
     * The default resolution for link and location path will be used.
     * The link will also be properly url escaped.
     * @param r ($perc.resourceInstance) never <code>null</code>.
     * @param fileName never <code>null</code> or empty and should not contain
     *  <code>'/'</code>
     * @return never <code>null</code>.
     */ 
    public static PSResourceLinkAndLocation createLinkAndLocationForFileName(
    		PSResourceInstance r, String fileName) {
    	notEmpty(fileName, "fileName");
    	isTrue( ! fileName.contains("/"), "fileName cannot contain slashes");
    	String path = r.getLocationFolderPath();
    	path = concatPath(path, fileName);
    	String urlPath = removeStart(escapePathForUrl(path), "/");
    	URI baseUri = r.getRelativeBaseUri();
    	notNull(baseUri);
    	URI uri = baseUri.resolve( urlPath );
    	String url = uri.toASCIIString();

    	// if our link is published.
    	url = appendAnalyticsId(r, url);

    	return createLinkAndLocation(path, url);
    }

	/**
	 * Append the analytics id encoded onto the end of the url.
	 * 
	 * @param r The content containing our link and link context.  Used to obtain the analytics Id.
	 * @param url Our url we will append onto
	 * @return  The url plus the analytics id if valid when combined.
	 */
	private static String appendAnalyticsId(PSResourceInstance r, String url) {
		if(r.getLinkContext().getMode().equals(Mode.PUBLISH)){
			
			IPSLinkableContentItem contentItem = r.getItem();
			
			// Node content type node attached to our resource.
			if (contentItem == null){
				return url;
			}
			
    		String analyticsId = (String) contentItem.getFields().get("analyticsId");
    		
    		// if this is the case the ? may have been removed from the beginning 
    		// of the analyticsid and we need to prepend it.  we check only if it's
    		// not the first character of the id as to preserve the input as much as possible
    		if(StringUtils.isNotBlank(analyticsId)) {
    			int indexOfQuestionMark = analyticsId.indexOf("?");
    			if(indexOfQuestionMark != 0) {
					analyticsId = "?" + analyticsId;
				}
    		}
    		
    		analyticsId = StringUtils.substringAfter(analyticsId, "?");
    		
    		if(StringUtils.isNotBlank(analyticsId)){
    			String[] analyticsIdHalves = analyticsId.split("&");
    			for(int i = 0; i < analyticsIdHalves.length; i++) {
	    			
	    			try {
	    				if(analyticsId.contains("=")) {
		    				String[] individualParam = analyticsIdHalves[i].split("=");
		    				String encodedValue = URLEncoder.encode(individualParam[0], UTF_8) + "=" + URLEncoder.encode(individualParam[1], UTF_8);
		    				if(i == 0) {
		    					url += "?" + encodedValue;
		    				}
		    				else {
		    					url += "&" + encodedValue;
		    				}
	    				}
	    				else {
	    					url += "?" + URLEncoder.encode(analyticsId, UTF_8);
	    				}
	    			}
	    			catch (UnsupportedEncodingException e) {
	    				log.error("Failed to encode url: {}.  Exception is: {}",url, e);
	    			}
    			} // end for loop
    			// cross site links are fully qualified non cross site are relative.
    			String checkUrl = url.startsWith("http") ? url : "http://localhost" + url;
    			if(!URLValidator.isValid(checkUrl)){
    				log.warn("The link to asset with analyticsid: {} appears to be invalid.",url);
    			}
    			
    		} // end if isNotBlank(analyticsId)
    	} // end outer if
		
		
		
		return url;
	}
    
    /**
     * Creates a default link and location use the items title.
     * It also append the "suffix" of the template if the context is delivery, which is the publish location.
     * 
     * @param evalContext never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static PSResourceLinkAndLocation createDefaultLinkAndLocation(PSResourceScriptEvaluatorContext evalContext) 
    {
        return createDefaultLinkAndLocation(evalContext.getResourceInstance(), getAssemblyService());
    }
    
    private static IPSAssemblyService getAssemblyService()
    {
        if (ms_assemblyService == null) {
			ms_assemblyService = PSAssemblyServiceLocator.getAssemblyService();
		}
        
        return ms_assemblyService;
    }    
    private static IPSAssemblyService ms_assemblyService = null;
    
    /**
     * This does the same as {@link #createDefaultLinkAndLocation(PSResourceScriptEvaluatorContext)}.
     * @param r the resource instance, never <code>null</code>.
     * @param assemblyService the assembly service, never <code>null</code>.
     * @return
     */
    public static PSResourceLinkAndLocation createDefaultLinkAndLocation(PSResourceInstance r, IPSAssemblyService assemblyService)
    {
        notNull(r);
        notNull(assemblyService);
        
        if(log.isDebugEnabled()) {
            log.debug("Getting default links for resource: {}" , r);
        }
        String baseName = (String) r.getItem().getFields().get("sys_title");
        String fileName = baseName;
        if (r.getLinkContext().isDeliveryContext())
        {
            String suffix = getLocationSuffix(r.getResourceDefinition(), assemblyService);
            if (!isBlank(suffix)) {
				fileName = fileName + suffix;
			}
        }
        return createLinkAndLocationForFileName(r, fileName);
    }
    
    /**
     * Gets the location suffix property from the template that is defined by the specified asset resource.
     * @param r the asset resource, assumed not <code>null</code>.
     * @return the location suffix. It may be <code>null</code> if the template does not exist.
     */
    private static String getLocationSuffix(PSAssetResource r, IPSAssemblyService assemblyService)
    {
        String templateName = r.getLegacyTemplate();
        try
        {
            IPSAssemblyTemplate template = assemblyService.findTemplateByName(templateName);
            return template.getLocationSuffix();
        }
        catch (PSAssemblyException e)
        {
            log.error("Failed to find template: \"" + templateName + "\"", e);
            return null;
        }
    }
    

    /**
     * Create a list with a single entry of the default link and location for the item.
     * 
     * @param evalContext never <code>null</code>.
     * @return a list with a single link and location, the list can be updated and is not immutable.
     */
    public static List<PSResourceLinkAndLocation> createDefaultLinkAndLocations(
            PSResourceScriptEvaluatorContext evalContext) {
        List<PSResourceLinkAndLocation> locations = new ArrayList<>();
        locations.add(createDefaultLinkAndLocation(evalContext));
        return locations;
    }
    
    /**
     * Creates a link and location.
     * @param filePath never <code>null</code> or empty.
     * @param url should be an escaped url, never <code>null</code> or empty. 
     * @return never <code>null</code>.
     */
    public static PSResourceLinkAndLocation createLinkAndLocation(String filePath, String url) {
        notEmpty(filePath, "filePath");
        notEmpty(url, "url");
        PSResourceLinkAndLocation rl = new PSResourceLinkAndLocation();
        PSRenderLink link = new PSRenderLink();
        PSResourceLocation location = new PSResourceLocation();
        link.setUrl(url);
        location.setFilePath(filePath);
        rl.setRenderLink(link);
        rl.setResourceLocation(location);
        return rl;
    }
    
    /**
     * Validates that the path is safe to use as a physical filesystem
     * folder path on windows and linux.
     * <p>
     * Mainly checks to see if there are any bad characters
     * that in the folder path.
     * The bad characters that are not allowed come from the
     * windows operating system as it is more restrictive than unix.
     * <p>
     * The following characters ar bad:
     * <pre>
     * \ | < > ? " : *
     * </pre> 
     * 
     * @param path never <code>null</code> or empty.
     * @throws IllegalArgumentException if the path is bad
     */
    public static void validateAsPhysicalPath(String path) {
        notEmpty(path,"invalid path");
        isTrue( ! containsAny(path, "\\|<>?\":*"), "invalid path" );
    }
    

	private static final Logger log = LogManager.getLogger(PSResourceLinkAndLocationUtils.class);
    private static final String UTF_8 = "UTF-8";
}

