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
package com.percussion.sitemanage.importer;

import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.security.SecureStringUtils;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.data.PSImportLogEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet that returns the content of a specific template's import log
 * 
 * @author federicoromanelli
 *
 */
public class PSSiteImportLogViewer extends HttpServlet  {
    
    private static final long serialVersionUID = 1L;
    
    public PSSiteImportLogViewer()
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);        
    }

    /**
     * Gets the log entry for a specific template id and returns the information as a txt file
     * @author federicoromanelli
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String templateId = request.getParameter("templateId");
            String siteName = request.getParameter("siteName");
            boolean existsFlag = ("true".equalsIgnoreCase(request.getParameter("exists")));


            PSSite site = null;
            if (StringUtils.isBlank(templateId) && !StringUtils.isBlank(siteName)) {
                try {
                    site = siteDao.find(siteName);
                    if (site == null) {
                        log.error("Couldn't load site: {}", siteName);
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't load site.");
                        return;
                    }
                    templateId = site.getBaseTemplateName();
                } catch (PSDataServiceException e) {
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't load site.");
                    return;
                }
            }

            List<PSImportLogEntry> logs = null;
            String templateName = "";
            if (!StringUtils.isBlank(templateId)) {
                try {
                    PSTemplateSummary sum = templateService.find(templateId);
                    if (sum != null) {
                        templateName = sum.getName();
                        logs = logDao.findAll(templateId, PSLogObjectType.TEMPLATE.name());
                    }
                } catch (PSDataServiceException e) {
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error");
                    return;
                }
            }

            if (logs == null || logs.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "Couldn't find import log");
                return;
            }

            if (existsFlag) {
                response.sendError(HttpServletResponse.SC_OK, "Import log exists");
                return;
            }

            if (StringUtils.isBlank(siteName)) {
                try {
                    siteName = siteMgr.getItemSites(idMapper.getGuid(templateId)).get(0).getName();
                    site = siteDao.find(siteName);
                } catch (PSDataServiceException e) {
                    log.error("Couldn't load template: {} Error: {}", templateName,
                            PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));

                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't load template.");
                    return;
                }
            }


            PSImportLogEntry templateLogEntry = getLatestLogEntry(logs);

            // now see if template is home page template, if so, get all page import logs for the site
            List<Long> pageLogIds = null;

            if (site !=null && templateName.equals(site.getTemplateName())) {
                try {
                    List<String> itemIds = folderHelper.findItemIdsByPath(site.getFolderPath());
                    pageLogIds = logDao.findLogIdsForObjects(itemIds, PSLogObjectType.PAGE.name());
                } catch (Exception e) {
                    log.error("Failed to load page import logs for Site: {}, Error: {}", siteName,
                            PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                }
            }

            // Get all pages in site (see search) - .25
            // Get all logids for those pages, sort them ascending - .25
            // For each, get and stream output - .25

            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment;filename=" + SecureStringUtils.stripAllLineBreaks(
                    siteName) + "-" + SecureStringUtils.stripAllLineBreaks(templateName) + "-importlog.txt");
            PrintWriter out = response.getWriter();
            if (templateLogEntry != null) {
                out.println(templateLogEntry.getLogData());
            }

            // now write out each page's log
            if (pageLogIds != null) {
                for (Long pageLogId : pageLogIds) {
                    PSImportLogEntry pageLog = logDao.findLogEntryById(pageLogId);
                    if (pageLog != null) {
                        out.println(pageLog.getLogData());
                    }
                }
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private PSImportLogEntry getLatestLogEntry(List<PSImportLogEntry> logs)
    {
        PSImportLogEntry logEntry;
        logs.sort((log1, log2) -> log1.getLogEntryDate().compareTo(log2.getLogEntryDate()));
        
        logEntry = logs.get(logs.size() - 1);
        return logEntry;
    }
    
    /**
     * Call doGet method
     * @author federicoromanelli
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {
        doGet(req, resp);
    }
    
    /* Getters and Setters to inject spring dependencies */
    private static IPSImportLogDao logDao;
    private static IPSTemplateService templateService;
    private static IPSiteDao siteDao;
    private static IPSPageService pageService;
    private static IPSSiteManager siteMgr;
    private static IPSIdMapper idMapper;
    private static IPSFolderHelper folderHelper;

    public static IPSImportLogDao getLogDao()
    {
        return logDao;
    }

    public static void setLogDao(IPSImportLogDao logDao)
    {
        PSSiteImportLogViewer.logDao = logDao;
    }

    public static IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    public static void setTemplateService(IPSTemplateService templateService)
    {
        PSSiteImportLogViewer.templateService = templateService;
    }

    public static IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    public static void setSiteDao(IPSiteDao siteDao)
    {
        PSSiteImportLogViewer.siteDao = siteDao;
    }

    public static IPSPageService getPageService()
    {
        return pageService;
    }

    public static void setPageService(IPSPageService pageService)
    {
        PSSiteImportLogViewer.pageService = pageService;
    }

    public static IPSSiteManager getSiteMgr()
    {
        return siteMgr;
    }

    public static void setSiteMgr(IPSSiteManager siteMgr)
    {
        PSSiteImportLogViewer.siteMgr = siteMgr;
    }

    public static IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public static void setIdMapper(IPSIdMapper idMapper)
    {
        PSSiteImportLogViewer.idMapper = idMapper;
    }
    
    public static void setFolderHelper(IPSFolderHelper folderHelper)
    {
        PSSiteImportLogViewer.folderHelper = folderHelper;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSiteImportLogViewer.class);
    
}
