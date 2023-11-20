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
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
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
        try(PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain");
            String outputMsg = null;
            String templateId = request.getParameter("templateId");
            String siteName = request.getParameter("siteName");


            PSSite site = null;


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
                    outputMsg = "No report log found for this template";
                    out.write(outputMsg);
                    return;
                }
            }
            if (logs != null && !logs.isEmpty()) {
                if (StringUtils.isBlank(siteName)) {
                    try {
                        siteName = siteMgr.getItemSites(idMapper.getGuid(templateId)).get(0).getName();
                        site = siteDao.find(siteName);
                    } catch (PSDataServiceException e) {
                        log.error("Couldn't load template: {} Error: {}", templateName,
                                PSExceptionUtils.getMessageForLog(e));
                        log.debug(PSExceptionUtils.getDebugMessageForLog(e));

                        outputMsg = "No report log found for this template";
                        out.write(outputMsg);
                        return;
                    }
                }


                PSImportLogEntry templateLogEntry = getLatestLogEntry(logs);

                // now see if template is home page template, if so, get all page import logs for the site
                List<Long> pageLogIds = null;

                if (site != null && templateName.equals(site.getTemplateName())) {
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


                response.setHeader("Content-Disposition", "attachment;filename=" + SecureStringUtils.stripAllLineBreaks(
                        siteName) + "-" + SecureStringUtils.stripAllLineBreaks(templateName) + "-importlog.txt");

                if (templateLogEntry != null) {
                    out.println(templateLogEntry.getLogData());
                }

                // now write out each page's log
                if (!pageLogIds.isEmpty()) {
                    for (Long pageLogId : pageLogIds) {
                        PSImportLogEntry pageLog = logDao.findLogEntryById(pageLogId);
                        if (pageLog != null) {
                            out.println(pageLog.getLogData());
                        }
                    }
                }
            }else{
                outputMsg = "No report log found for this template";
                out.write(outputMsg);
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
     * @throws ServletException
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
