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

package com.percussion.sitemanage.servlet;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.data.PSInlineLinkRequest;
import com.percussion.pagemanagement.data.PSInlineRenderLink;
import com.percussion.pagemanagement.service.impl.PSRenderLinkService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSMutableUrl;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static com.percussion.pathmanagement.service.impl.PSPathUtils.SITES_FINDER_ROOT;

/**
 * The servlet used to preview content of the item, where the item is specified by its folder path.
 * 
 * @author YuBingChen
 */
public class PSPreviewItemContent extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LogManager.getLogger(PSPreviewItemContent.class);
    private static PSRenderLinkService linkService;
    private static IPSiteDao siteDao;
    
    public PSPreviewItemContent()
    {
        super();
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    @Override
    protected void service(HttpServletRequest request,
          HttpServletResponse response)
    {
        String requestUri = request.getRequestURI();
        String revision = request.getParameter(IPSHtmlParameters.SYS_REVISION);
        
        try
        {
            String type = request.getParameter("type");
            if (requestUri.endsWith("favicon.ico"))
                return;
            String url = createAssemblyUrl(requestUri, revision, type);
            
            HttpServletRequest forwardReq = getRequestFromUrl(url, request);
            
            RequestDispatcher disp = request.getRequestDispatcher("/assembler/render");
            disp.forward(forwardReq, response);
        }catch (ServletException | UnsupportedOperationException| PSCmsException | PSDataServiceException | PSRequestParsingException | PSNotFoundException | IOException e) {
                log.error("Unable to preview resource:{} Error: {}",requestUri,
                        PSExceptionUtils.getMessageForLog(e));
                try {
                    response.sendError(404);
                } catch (IOException ioException) {
                    response.setStatus(404);
                }
        }

    }

    
    @SuppressWarnings("unchecked")
    private HttpServletRequest getRequestFromUrl(String url, HttpServletRequest request) throws PSRequestParsingException
    {
        PSMutableUrl mutableUrl = new PSMutableUrl(url);
        Map<String, String> params = mutableUrl.getParamMap();

        Map<String, String[]> params2 = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            params2.put(entry.getKey(), new String[]{entry.getValue()});
        }

        PSServletRequestWrapper wrapReq = new PSServletRequestWrapper(request);
        wrapReq.setParameterMap(params2);
        
        return wrapReq;
    }
    
    /**
     * Creates the assembly URL from the specified item path.
     * @param path the path of the item, assumed not blank.
     * @param revision the id of the revision, can be blank
     * @param renderType it is "xml", "html" or "database", assumed not blank.
     * @return the assembly URL, not blank.
     */
    private String createAssemblyUrl(String path, String revision, String renderType) throws PSDataServiceException, PSNotFoundException, PSCmsException, UnsupportedEncodingException {
        IPSGuid id = getItemId(path, revision);
        if (id!=null) {
            IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
            PSComponentSummary item = objMgr.loadComponentSummary(id.getUUID());

            if (item.isFolder()) {


                PSSiteSummary siteSum = siteDao.findByPath("/" + path);
                if (!path.endsWith("/"))
                    path += "/";
                path += siteSum.getDefaultDocument();
                // get default page id
                id = getItemId(path, revision);
            }
        }
        if (id==null)
                throw new PSNotFoundException(path);
        PSInlineLinkRequest linkRequest = new PSInlineLinkRequest();
        linkRequest.setTargetId(id.toString());
        PSInlineRenderLink renderLink;
        if (path.startsWith(SITES_FINDER_ROOT))
            renderLink = linkService.renderPreviewPageLink(id.toString(), renderType);
        else
            renderLink = linkService.renderPreviewResourceLink(linkRequest);

        return renderLink.getUrl();
    }

    /**
     * Gets the ID of the item from its path.
     * @param path the path if the item in question.
     * @param revision the id of the revision, can be blank
     * @return the ID of the item, never <code>null</code>.
     * @throws PSNotFoundException if cannot find the item from the path.
     */
    private IPSGuid getItemId(String path, String revision) throws PSNotFoundException, PSCmsException, UnsupportedEncodingException {
        path = escapeChars(path);
        path = PSPathUtils.getFolderPath(path);

        int revisionId = -1;
        PSServerFolderProcessor srv = PSServerFolderProcessor.getInstance();

            if(!StringUtils.isBlank(revision))
            {
                revisionId = Integer.parseInt(revision);
            }

            int id = srv.getIdByPath(path);



            if (id == -1)
                throw new PSNotFoundException("Cannot find item with path = \"" + path + "\".");
            
            return new PSLegacyGuid(id, revisionId);

    }

    /**
     * Manual decode the '+' character as the browser may not do it correctly, 
     * which will cause the <code>URLDecoder.decode</code> method to mistakenly 
     * replace it with a whitespace.
     * Tested in the following browsers:
     * <ul>
     * <li>Internet Explorer 8</li>
     * <li>Firefox 3.6</li>
     * <li>Safari 4</li>
     * <li>Google Chrome</li>
     * </ul>
     * 
     * @param path the path to decode manually
     * @return
     *      the same path but with the '+' symbol replaced by '%2B'
     */
    private String escapeChars(String path) throws UnsupportedEncodingException {
        String ret = path;

        String test = URLDecoder.decode(path, "utf8");
        if(!test.equals(path)) {
            ret = test;
        }

        return ret;
    }

    public static void setRenderLinkService(PSRenderLinkService service)
    {
        linkService = service;
    }
    
    public static PSRenderLinkService getRenderLinkService()
    {
        return linkService;
    }   
    
    public static IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    public static void setSiteDao(IPSiteDao dao)
    {
        siteDao = dao;
    }

}
