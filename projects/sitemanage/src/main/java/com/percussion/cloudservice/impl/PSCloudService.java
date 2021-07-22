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

package com.percussion.cloudservice.impl;

import com.percussion.cloudservice.IPSCloudService;
import com.percussion.cloudservice.data.PSCloudLicenseType;
import com.percussion.cloudservice.data.PSCloudServiceInfo;
import com.percussion.cloudservice.data.PSCloudServicePageData;
import com.percussion.error.PSExceptionUtils;
import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.licensemanagement.error.PSLicenseServiceException;
import com.percussion.licensemanagement.service.impl.PSLicenseService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderService;
import com.percussion.server.PSServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.PSItemProperties;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;

@Service("cloudService")
@Path("/cloudservice")
public class PSCloudService implements IPSCloudService {

	protected static final String PAGE_OPTIMIZER_KEY_PROP = "PAGE_OPTIMIZER_KEY";
	protected static final String PAGE_OPTIMIZER_URL_PROP = "PAGE_OPTIMIZER_URL";
	protected static final String PAGE_THUMB_ROOT = "/rx_resources/images/TemplateImages/";
	protected static final String PAGE_THUMB_SUFFIX = "-page.jpg";
	protected static final String CLOUD_SERVICE_TYPE_CM1 = "CM1";

	protected IPSFolderHelper folderHelper;
	protected IPSRenderService renderService;
	protected IPSPageService pageService;
	protected PSLicenseService licenseService;
	protected boolean isLogged;
	protected static Logger log;
	
	@Autowired
	public PSCloudService(IPSFolderHelper folderHelper, IPSRenderService renderService, 
	        IPSPageService pageService, PSLicenseService licenseService) {
		this.folderHelper = folderHelper;
	    this.renderService = renderService;
	    this.pageService = pageService;
		this.licenseService = licenseService;
		this.log = LogManager.getLogger(PSCloudService.class);
	}

	@Override
    @GET
    @Path("/active")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isActive() {
        return isValidLicense(PSCloudLicenseType.PAGE_OPTIMIZER);
    }
	
    @Override
    @GET
    @Path("/{licenseType}/active")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public boolean isActive(@PathParam("licenseType") PSCloudLicenseType licenseType) {
        return isValidLicense(licenseType);
	}
    
	@Override
    @GET
    @Path("/activestates")
    @Produces(MediaType.TEXT_PLAIN)
    public String getActiveState() {
	    JSONObject states = new JSONObject();
        for (PSCloudLicenseType type: PSCloudLicenseType.values()) {
            Boolean valid = isValidLicense(type);
            states.put(type.toString(), valid);
        }
        
        return states.toString();
    }
	
	@Override
	@GET
	@Path("/info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public PSCloudServiceInfo getInfo() throws PSCloudServiceException {
       PSModuleLicense poLic = null;
       
        try {
            poLic = getLicense(PSCloudLicenseType.PAGE_OPTIMIZER);
        } catch (PSLicenseServiceException le) { }
        
        if (poLic == null) {
            try {
                poLic = getLicense(PSCloudLicenseType.SOCIAL_PROMOTION);
            } catch (PSLicenseServiceException le) { }
        }
        
        if (poLic == null) {
            throw new PSCloudServiceException("No cloud services are not enabled for this instance of CM1");
        }
        
        PSCloudServiceInfo info = new PSCloudServiceInfo();
        info.setClientIdentity(generateClientIdentity(poLic));
        info.setUiProvider(StringUtils.defaultString(poLic.getUiProvider()));
        return info;
	}
	
    @Override
    @GET
    @Path("/{licenseType}/info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public PSCloudServiceInfo getInfo(@PathParam("licenseType") PSCloudLicenseType licenseType) {
        PSModuleLicense poLic = null; 

        try {
            poLic = getLicense(licenseType);
        } catch (PSLicenseServiceException le) {
			log.error(le.getMessage());
			log.debug(le.getMessage(),le);
        	throw new WebApplicationException(licenseType.toFriendlyString() + " is not enabled for this instance of Percussion CMS");
        }
        
        PSCloudServiceInfo info = new PSCloudServiceInfo();
        info.setClientIdentity(generateClientIdentity(poLic));
        info.setUiProvider(StringUtils.defaultString(poLic.getUiProvider()));
        return info;
    }
    
    @Override
    @GET
    @Path("/pagedata/{pageId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSCloudServicePageData getPageData(@PathParam("pageId") String pageId) {
	    try {
            PSCloudServiceInfo info = getInfo();
            return getPageData(info, pageId);
        } catch (PSCloudServiceException e) {
	        log.error(e.getMessage());
	        log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @Override
    @GET
    @Path("/{licenseType}/pagedata/{pageId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSCloudServicePageData getPageData(@PathParam("licenseType") PSCloudLicenseType licenseType,
            @PathParam("pageId") String pageId) {
	    try {
            PSCloudServiceInfo info = getInfo(licenseType);
            return getPageData(info, pageId);
        } catch (PSCloudServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage() ,e);
	    	throw new WebApplicationException(e);
        }
    }
    
    @Override
    @POST
    @Path("/pagedata")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void savePageData(PSCloudServicePageData pageData) {
        String pageId = pageData.getId();
        PSPage page = null;
        
        try {
            page = pageService.load(pageId);
            page.setTitle(pageData.getPageTitle());
            page.setDescription(pageData.getPageDescription());
            pageService.save(page);
        }
        catch (Throwable cause) {
            throw new WebApplicationException(cause);
        }
    }
    
	/**
	 * Generates client identity, creates a json object and returns to string of
	 * it.
	 * 
	 * @return Stringified json object of client identity.
	 *         {"id":"License String","type":"CM1"}
	 */
	public String generateClientIdentity(PSModuleLicense poLic) {
		JSONObject ci = new JSONObject();
		ci.put("id", poLic.getKey());
		ci.put("type", CLOUD_SERVICE_TYPE_CM1);
		ci.put("token", poLic.getHandshake());
		return ci.toString();
	}

	/**
	 * Generates the thumb url, if the thumbnail doesn't exist, it returns a
	 * running message.
	 * 
	 * @param pageId
	 *            assumed not <code>null</code>.
	 * @param siteName
	 *            assumed not <code>null</code>
	 * @return The thumb url for the supplied page.
	 */
	public String generateThumbUrl(String pageId, String siteName) {
		String thumbUrl = PAGE_THUMB_ROOT + siteName + "/" + pageId
				+ PAGE_THUMB_SUFFIX;
		File file = new File(PSServer.getRxDir() + thumbUrl);
		if (!file.exists())
			thumbUrl = "";
		else
			thumbUrl = "/Rhythmyx" + thumbUrl;

		return thumbUrl;
	}

	/**
	 * Determine whether cloud service is licensed for the given license type
	 */
    public boolean isValidLicense(PSCloudLicenseType licenseType) {
		PSModuleLicense poLic = null;
		try {
			poLic = getLicense(licenseType);
		} catch (PSLicenseServiceException le) {
			if (!isLogged) {
				isLogged = true;
				log.info("{} is not enabled for this instance of CM1, activate the license using license monitor gadget.",licenseType.toFriendlyString());
				log.error(PSExceptionUtils.getMessageForLog(le));
				log.debug(le);
			}
		}
		return poLic != null;
	}

    /**
     * Get the license for the given license type
     * @param licenseType the module being licensed
     * @return The module license
     * @throws PSLicenseServiceException
     */
	public PSModuleLicense getLicense(PSCloudLicenseType licenseType)
			throws PSLicenseServiceException {
		String licType = (licenseType == PSCloudLicenseType.PAGE_OPTIMIZER) 
		        ? PSLicenseService.MODULE_LICENSE_TYPE_PAGE_OPTIMIZER
				: PSLicenseService.MODULE_LICENSE_TYPE_REDIRECT;
		
		PSModuleLicense poLic = licenseService.findModuleLicense(licType);
		return poLic;
	}

    private PSCloudServicePageData getPageData(PSCloudServiceInfo info, String pageId) throws PSCloudServiceException {
        PSItemProperties itemProps = null;
        String siteName = "";
        PSPage page = null;
        
        try {
            itemProps = folderHelper.findItemPropertiesById(pageId);
            List<IPSSite> sites = folderHelper.getItemSites(pageId);
            siteName = sites.get(0).getName();
            page = pageService.load(pageId);
        }
        catch (Throwable cause) {
            throw new PSCloudServiceException(cause);
        }
        
        PSCloudServicePageData pageData = createFromItemProps(itemProps);
        pageData.setClientIdentity(info.getClientIdentity());
        pageData.setUiProviderUrl(info.getUiProvider());
        pageData.setThumbUrl(generateThumbUrl(pageId, siteName));
        pageData.setPageTitle(page.getTitle());
        pageData.setPageDescription(page.getDescription());
        return pageData;
    }

    private PSCloudServicePageData createFromItemProps(PSItemProperties itemProps) {
        String pageId = itemProps.getId();
        String path = itemProps.getPath();
        PSCloudServicePageData pageData = new PSCloudServicePageData();
        pageData.setId(pageId);
        pageData.setPath(path);
        
        String folders[] = path.split("/");
        String pagePath = "";
        for (int i = 3; i < folders.length; i++) {
            pagePath += "/" + folders[i];
        }
        pageData.setPagePath(pagePath);
        
        int index = path.lastIndexOf("/");
        if (path.length() > index){
            pageData.setPageName(path.substring(path.lastIndexOf("/") + 1));
        }
        else {
            log.error("Failed to find the name for the page with path {}", path);
        }
        
        pageData.setLastPublished(itemProps.getLastPublishedDate());
        pageData.setStatus(itemProps.getStatus());
        pageData.setWorkflow(itemProps.getWorkflow());
        pageData.setLastEdited(itemProps.getLastModifiedDate());
        pageData.setLastPublished(itemProps.getLastPublishedDate());
        
        String renderedPage = renderService.renderPage(pageId);
        pageData.setPageHtml(renderedPage);
        
        return pageData;
    }
}
