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
package com.percussion.sitemanage.service;

import java.util.List;

import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;

/**
 * Adds and removes items associated with a  folder path. This is a low level 
 * service it should not YET be exposed publicly (WS API, REST API) .
 * 
 * @author adamgent
 *
 */
public interface IPSSiteSectionMetaDataService
{
    /**
     * The category for all templates of a site.
     */
    public final static String TEMPLATES = "Templates";
    
    /**
     * The root folder for all catalogged pages of a site.
     */
    public final static String PAGE_CATALOG = "PageCatalog";
    
    /**
     * The sub-folder used to store section data.
     */
    public static final String SECTION_SYSTEM_FOLDER_NAME = ".system";
    
    public void addItem(IPSFolderPath siteSection, String category, String itemId);
    
    public void removeItem(IPSFolderPath siteSection, String category, String itemId);
    
    public void removeCategory(IPSFolderPath siteSection, String category);
    
    public List<IPSItemSummary> findItems(IPSFolderPath siteSection, String category) throws IPSDataService.DataServiceNotFoundException;
    
    public List<IPSFolderPath> findSections(String category, String itemId);
    
    public List<String> findCategories(IPSFolderPath siteSection);
    
    public boolean containCategoryFolder(IPSFolderPath siteSection);
    
    public static class PSSiteSectionMetaDataServiceException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public PSSiteSectionMetaDataServiceException(String message)
        {
            super(message);
        }

        public PSSiteSectionMetaDataServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSSiteSectionMetaDataServiceException(Throwable cause)
        {
            super(cause);
        }

    }
    
}
