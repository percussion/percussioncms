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
package com.percussion.sitemanage.service.impl;

import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSFolderHelper.PathTarget;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.sitemanage.service.IPSSiteSectionMetaDataService;
import com.percussion.webservices.content.IPSContentWs;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

@Component("siteSectionMetaDataService")
@Lazy
public class PSSiteSectionMetaDataService implements IPSSiteSectionMetaDataService
{

    private IPSFolderHelper folderHelper;
    private IPSContentWs contentWs;

    @Autowired
    public PSSiteSectionMetaDataService(IPSFolderHelper folderHelper, IPSContentWs contentWs)
    {
        super();
        notNull(folderHelper);
        this.folderHelper = folderHelper;
        this.contentWs = contentWs;
    }

    public void addItem(IPSFolderPath section, String category, String itemId)
    {
        String path = sectionToPath(section, category);
        validateItemId(itemId);
        try
        {
            folderHelper.addItem(path, itemId);
        }
        catch (Exception e)
        {
            handleException("add", e, section, category, itemId);
        }

    }

    /**
     * Gets the actual folder path of the category of the specified section.
     * 
     * @param section the path of the section, not blank.
     * @param category the category of the section.
     * 
     * @return the folder path, not blank.
     */
    protected String sectionToPath(IPSFolderPath section, String category)
    {
        String sectionPath = section.getFolderPath();
        notNull(sectionPath);
        validateSection(sectionPath);
        validateCategory(category);
        return folderHelper.concatPath(sectionPath, SECTION_SYSTEM_FOLDER_NAME, category);
    }
    
    public boolean containCategoryFolder(IPSFolderPath section)
    {
        notNull(section, "section");
        String sectionPath = section.getFolderPath();
        validateSection(sectionPath);
        String categoryPath = folderHelper.concatPath(sectionPath, SECTION_SYSTEM_FOLDER_NAME);
        return contentWs.getIdByPath(categoryPath) != null;
    }

    public List<String> findCategories(IPSFolderPath section)
    {
        notNull(section, "section");
        String sectionPath = section.getFolderPath();
        validateSection(sectionPath);
        String systemSectionPath = folderHelper.concatPath(sectionPath, SECTION_SYSTEM_FOLDER_NAME);
        List<String> catPaths = new ArrayList<>();
        try
        {
            catPaths = folderHelper.findChildren(systemSectionPath);
        }
        catch (Exception e)
        {
            log.error("failed to find children for path: " + systemSectionPath);
        }
        List<String> paths = new ArrayList<>();
        for (String c : catPaths)
        {
            paths.add(folderHelper.name(c));
        }

        return paths;
    }

    public List<IPSItemSummary> findItems(IPSFolderPath section, String category) throws IPSDataService.DataServiceNotFoundException {
        String path = sectionToPath(section, category);
        PathTarget p = folderHelper.pathTarget(path);
        if ( p.isToNothing())
        {
            return emptyList();
        }
        try
        {
            return folderHelper.findItems(path);
        }
        catch (Exception e)
        {
            throw new PSSiteSectionMetaDataServiceException(format(
                    "Error happened while finding items for section: {0} for category: {1}", section, category), e);
        }
    }

    public void removeItem(IPSFolderPath section, String category, String itemId)
    {
        String path = sectionToPath(section, category);
        validateItemId(itemId);
        try
        {
            folderHelper.removeItem(path, itemId, false);
        }
        catch (Exception e)
        {
            handleException("remove", e, section, category, itemId);
        }
    }

    public List<IPSFolderPath> findSections(String category, String itemId)
    {
        try
        {
            String sep = folderHelper.pathSeparator();
            String matchPath = sep + folderHelper.concatPath(SECTION_SYSTEM_FOLDER_NAME, category);
            matchPath = removeEnd(matchPath, sep);
            List<String> paths = folderHelper.findPaths(itemId);

            if (log.isDebugEnabled())
            {
                log.debug(format("findSections - category:{3}, itemId:{2}, matchPath:{0}, paths:{1}", matchPath, paths,
                        itemId, category));
            }

            List<IPSFolderPath> sections = new ArrayList<>();
            for (String p : paths)
            {
                if (endsWith(p, matchPath))
                {
                    String path = removeEnd(p, matchPath);
                    isTrue(isNotBlank(path), "The section path should not be empty.");
                    sections.add(new SectionPath(path));
                }
            }
            if (log.isDebugEnabled())
            {
                log.debug("findSections - sections: " + sections);
            }
            return sections;
        }
        catch (Exception e)
        {
            throw new PSSiteSectionMetaDataServiceException(
                    format("Error occurred trying to find sections/sites for category: {0} and item id: {1}", category,
                            itemId), e);
        }
    }

    public void removeCategory(IPSFolderPath siteSection, String category)
    {
        String path = sectionToPath(siteSection, category);
        try
        {
            folderHelper.deleteFolder(path);
        }
        catch (Exception e)
        {
            throw new PSSiteSectionMetaDataServiceException(format(
                    "Failed to remove category: {0} from section path: {1}", category, siteSection.getFolderPath()));
        }
    }

    public static class SectionPath implements IPSFolderPath
    {

        private String folderPath;

        public SectionPath(String folderPath)
        {
            super();
            this.folderPath = folderPath;
        }

        public String getFolderPath()
        {
            return folderPath;
        }

        public void setFolderPath(String folderPath)
        {
            this.folderPath = folderPath;
        }

        @Override
        public boolean equals(Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }

    }

    private void handleException(String action, Exception e, IPSFolderPath section, String category, String itemId)
    {
        String error = format("Failed to {3} item id: {0} to section path: {1}, category: {2}", itemId, section,
                category, action);
        throw new PSSiteSectionMetaDataServiceException(error, e);
    }

    protected void validateItemId(String itemId)
    {
        isTrue(isNotBlank(itemId), "Item id cannot be blank");
    }

    protected void validateSection(String section)
    {
        isTrue(startsWith(section, "//"), "Section paths must begin with //");
    }

    protected void validateCategory(String category)
    {
        isTrue(!contains(category, folderHelper.pathSeparator()), "Category must not contain a '"
                + folderHelper.pathSeparator() + "'");
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSiteSectionMetaDataService.class);

}
