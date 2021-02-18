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
package com.percussion.sitemanage.importer;

import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
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
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws IOException
    {
        String templateId   = request.getParameter("templateId");
        String siteName     = request.getParameter("siteName");
        boolean existsFlag   = ("true".equalsIgnoreCase(request.getParameter("exists"))? true : false);
        
                
        PSSite site = null;
        if (StringUtils.isBlank(templateId) && !StringUtils.isBlank(siteName))
        {
            try
            {
                site = siteDao.find(siteName);
                if (site==null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't load site: " + siteName);
                    return;   
                }
                templateId = site.getBaseTemplateName();
            }
            catch (PSDataServiceException e)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't load site: " + siteName);
                return;                
            }
        }

        List<PSImportLogEntry> logs = null;
        String templateName = "";
        if (!StringUtils.isBlank(templateId))
        {
            PSTemplateSummary sum = templateService.find(templateId);
            if (sum != null)
            {
                templateName = sum.getName();
                logs = logDao.findAll(templateId, PSLogObjectType.TEMPLATE.name());
            }
        }
        
        if (logs == null || logs.isEmpty())
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't find import log");
            return;
        }

        if (existsFlag)
        {
            response.sendError(HttpServletResponse.SC_OK, "Import log exists");
            return;
        }
        
        if (StringUtils.isBlank(siteName))
        {
            try
            {
                siteName = siteMgr.getItemSites(idMapper.getGuid(templateId)).get(0).getName();
                site = siteDao.find(siteName);
            }
            catch (PSDataServiceException e)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Couldn't load for template: " + templateName);
                return; 
            }
        }

        
        PSImportLogEntry templateLogEntry = getLatestLogEntry(logs);
        
        // now see if template is home page template, if so, get all page import logs for the site
        List<Long> pageLogIds = null;
        if (templateName.equals(site.getTemplateName()))
        {
            try
            {
                List<String> itemIds = folderHelper.findItemIdsByPath(site.getFolderPath());
                pageLogIds = logDao.findLogIdsForObjects(itemIds, PSLogObjectType.PAGE.name());
            }
            catch (Exception e)
            {
                log.error("Failed to load page import logs for Site: " + siteName); 
            }
        }
        
        // Get all pages in site (see search) - .25
        // Get all logids for those pages, sort them ascending - .25
        // For each, get and stream output - .25
       
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment;filename=" + siteName + "-" + templateName + "-importlog.txt");
        PrintWriter out = response.getWriter();        
        if (templateLogEntry != null)
        {
            out.println(templateLogEntry.getLogData());
        }
        
        // now write out each page's log
        if (pageLogIds != null)
        {
            for (Long pageLogId : pageLogIds)
            {
                PSImportLogEntry pageLog = logDao.findLogEntryById(pageLogId);
                if (pageLog != null)
                {
                    out.println(pageLog.getLogData());
                }
            }
        }
    }

    private PSImportLogEntry getLatestLogEntry(List<PSImportLogEntry> logs)
    {
        PSImportLogEntry logEntry;
        Collections.sort(logs, new Comparator<PSImportLogEntry>(){
            @Override
            public int compare(PSImportLogEntry log1, PSImportLogEntry log2)
            {
                return log1.getLogEntryDate().compareTo(log2.getLogEntryDate());
            }
        });
        
        logEntry = logs.get(logs.size() - 1);
        return logEntry;
    }
    
    /**
     * Call doGet method
     * @author federicoromanelli
     * @throws IOException
     */    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws IOException
    {
        doGet(req, resp);
    }
    
    /* Getters and Setters to inject spring dependencies */
    private IPSImportLogDao logDao;
    private IPSTemplateService templateService;
    private IPSiteDao siteDao;
    private IPSPageService pageService;
    private IPSSiteManager siteMgr;
    private IPSIdMapper idMapper;
    private IPSFolderHelper folderHelper;

    public IPSImportLogDao getLogDao()
    {
        return logDao;
    }

    public void setLogDao(IPSImportLogDao logDao)
    {
        this.logDao = logDao;
    }

    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }

    public IPSiteDao getSiteDao()
    {
        return siteDao;
    }

    public void setSiteDao(IPSiteDao siteDao)
    {
        this.siteDao = siteDao;
    }

    public IPSPageService getPageService()
    {
        return pageService;
    }

    public void setPageService(IPSPageService pageService)
    {
        this.pageService = pageService;
    }

    public IPSSiteManager getSiteMgr()
    {
        return siteMgr;
    }

    public void setSiteMgr(IPSSiteManager siteMgr)
    {
        this.siteMgr = siteMgr;
    }

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }
    
    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSiteImportLogViewer.class);    
    
}
