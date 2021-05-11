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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.itemmanagement.extension;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Gets the url parameters which can be used to generate the read-only editor view link for a page or asset.
 */
public class PSGenerateReadOnlyLink extends com.percussion.extension.PSSimpleJavaUdfExtension
{
    private static final Logger log = LogManager.getLogger(PSGenerateReadOnlyLink.class);
    
    private IPSIdMapper idMapper;
    private IPSWorkflowHelper workflowHelper;
    
    @Override
    public void init(IPSExtensionDef extDef, File file) throws PSExtensionException
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    /**
     * It returns the read-only editor view link for a page or asset.
     * 
     * See {@link IPSUdfProcessor#processUdf(Object[], IPSRequestContext) processUdf} for detail.
     * 
     * @param params the parameters for this extension, never <code>null</code>.  It is expected to contain 5 elements:
     * the content ID of the page or asset in question, revision of the page or asset in question, host name, port, and
     * flag which indicates if the url should use http or https.
     * @param request the parameter request, never <code>null</code>.
     * @return URL object, may be <code>null</code>.
     */
    public Object processUdf(Object[] params, IPSRequestContext request) throws PSConversionException {
        try {
            if (params.length < 5) {
                throw new IllegalArgumentException("params must contain 5 parameters.");
            }

            URL url = null;

            IPSGuid id = getContentId(params);

            IPSContentWs service = PSContentWsLocator.getContentWebservice();
            String[] paths = service.findItemPaths(id);
            if (paths.length == 0) {
                String msg1 = "Failed to generate read-only link for content ID = " + id;
                IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
                if (cmsMgr.findItemEntry(id.getUUID()) != null) {
                    log.warn(msg1 + " as the item does not exist.");
                }
                else {
                    log.warn(msg1 + " as the item is not under a folder.");
                }

                return url;
            }

            String host = getStringParameter(params, 2);
            Integer port = getIntParameter(params, 3);
            Boolean useHttps = getBooleanParameter(params, 4);

            String itemId = idMapper.getString(id);
            String finderPath = PSPathUtils.getFinderPath(paths[0]);

            String site = null;
            String view;
            String pathType;

            if (workflowHelper.isPage(itemId)) {
                site = StringUtils.split(finderPath, "/")[1];
                view = "editor";
                pathType = "page";
            } else {
                view = "editAsset";
                pathType = "asset";
            }

            Map<String, String> urlParams = new HashMap<>();
            urlParams.put("view", view);

            if (site != null) {
                urlParams.put("site", site);
            }

            urlParams.put("mode", "readonly");
            urlParams.put("id", itemId);
            urlParams.put("name", PSFolderPathUtils.getName(finderPath));
            urlParams.put("path", finderPath);
            urlParams.put("pathType", pathType);

                url = PSUrlUtils.createUrl(host, port, "/cm/app/", urlParams.entrySet().iterator(), null, request);

                //If we have to use SSL modify the URL to use https

                if (PSServer.isRequestBehindProxy(null)) {
                    String proxyScheme = PSServer.getProperty("proxyScheme", url.getProtocol());
                    url = new URL(proxyScheme, url.getHost(), url.getPort(), url.getFile()); // host and prot is already commming as proxy configured
                } else {
                    if (useHttps) {
                        url = new URL("https", url.getHost(), url.getPort(), url.getFile());
                    }
                }

            return url;
        } catch (PSNotFoundException | PSValidationException | MalformedURLException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSConversionException(e);
        }
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSWorkflowHelper getWorkflowHelper()
    {
        return workflowHelper;
    }

    public void setWorkflowHelper(IPSWorkflowHelper workflowHelper)
    {
        this.workflowHelper = workflowHelper;
    }

    /**
     * Gets the content ID from the specified parameters.
     * @param params the parameters, expecting 1st parameter is the content ID and 2nd is the revision.
     * @return the content ID in GUID, never <code>null</code>.
     */
    private IPSGuid getContentId(Object[] params)
    {
        if (params.length < 2) {
			throw new IllegalArgumentException("params must contain 2 parameters.");
		}
        
        int contentId = getIntParameter(params, 0);
        int revision = getIntParameter(params, 1);
        
        return new PSLegacyGuid(contentId, revision);
    }
    
    private int getIntParameter(Object params[], int index)
    {
        Object p = params[index];
        if (!(p instanceof Integer)) {
			throw new IllegalArgumentException("Parameter[" + index + "] is not Integer.");
		}
        
        return ((Integer)p).intValue();
    }
    
    private String getStringParameter(Object params[], int index)
    {
        Object p = params[index];
        if (!(p instanceof String)) {
			throw new IllegalArgumentException("Parameter[" + index + "] is not String.");
		}
        
        return (String) p;
    }
    
    private Boolean getBooleanParameter(Object params[], int index)
    {
        Object p = params[index];
        if (!(p instanceof Boolean)) {
			throw new IllegalArgumentException("Parameter[" + index + "] is not Boolean.");
		}
        
        return (Boolean) p;
    }
 
}
