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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to show the unassigned pages component in the Design View.
 * 
 * @author Santiago M. Murchio
 * 
 */
@JsonRootName(value = "UnassignedResults")
public class PSUnassignedResults extends PSAbstractDataObject
{
    private UnassignedItemList unassignedItemList;
    
    private ImportStatus importStatus;
 
    public PSUnassignedResults()
    {
        this.unassignedItemList = new UnassignedItemList();
        this.importStatus = new ImportStatus();        
    }
    
    public PSUnassignedResults(UnassignedItemList unassignedItemList, ImportStatus importStatus)
    {
        this.unassignedItemList = unassignedItemList;
        this.importStatus = importStatus;
    }
    
    public UnassignedItemList getUnassignedItemList()
    {
        return unassignedItemList;
    }

    public void setUnassignedItemList(UnassignedItemList unassignedItemList)
    {
        this.unassignedItemList = unassignedItemList;
    }

    public ImportStatus getImportStatus()
    {
        return importStatus;
    }

    public void setImportStatus(ImportStatus importStatus)
    {
        this.importStatus = importStatus;
    }

    /**
     * Class that represents the items in the unassined pages component.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public static class UnassignedItemList extends PSAbstractDataObject
    {
        private Integer startIndex;
        
        private Integer childrenCount;

        private List<UnassignedItem> childrenInPage;
        
        public UnassignedItemList()
        {
            this.startIndex = 0;
            this.childrenCount = 0;
            this.childrenInPage = new ArrayList<UnassignedItem>();
        }
        
        public UnassignedItemList(Integer startIndex, Integer childrenCount, List<UnassignedItem> childrenInPage)
        {
            this.startIndex = startIndex;
            this.childrenCount = childrenCount;

            if(childrenInPage == null)
            {
                this.childrenInPage = new ArrayList<UnassignedItem>();
            }
            else
            {
                this.childrenInPage = childrenInPage;
            }
        }

        /**
         * The start index corresponding to the first child element. It is
         * 1-based, so the first element has an index of 1, not 0.
         * 
         * @return {@link Integer} not <code>null</code>.
         */
        public Integer getStartIndex()
        {
            return startIndex;
        }

        /**
         * Set the startIndex value. The index is 1-based, so the first element
         * is 1, not 0.
         * 
         * @param startIndex {@link Integer} assumed not <code>null</code>.
         */
        public void setStartIndex(Integer startIndex)
        {
            this.startIndex = startIndex;
        }

        /**
         * The {@link UnassignedItem items} that belong to this page.
         * 
         * @return {@link List}<{@link UnassignedItem}> not <code>null</code>
         *         after constructor.
         */
        public List<UnassignedItem> getChildrenInPage()
        {
            return childrenInPage;
        }

        /**
         * @see #getChildrenInPage()
         * @param childrenInPage {@link List}<{@link UnassignedItem}> assumed
         *            not <code>null</code>.
         */
        public void setChildrenInPage(List<UnassignedItem> childrenInPage)
        {
            this.childrenInPage = childrenInPage;
        }

        /**
         * @see #getChildrenInPage()
         * @param childrenCount {@link Integer} assumed not <code>null</code>.
         */
        public void setChildrenCount(Integer childrenCount)
        {
            this.childrenCount = childrenCount;
        }

        /**
         * The number of items that this page contains.
         * 
         * @return {@link Integer} not <code>null</code> after constructor.
         */
        public Integer getChildrenCount()
        {
            return childrenCount;
        }

        public String toString()
        {
            return "startIndex: " + startIndex + ", childrenCount: " + childrenCount + ", pageLength: " + childrenInPage.size();
        }
    }

    /**
     * Class that represents the status of the import proces, for unassigned
     * pages.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public static class ImportStatus extends PSAbstractDataObject
    {
        private Integer catalogedPageCount;
        
        private Integer importedPageCount;
        
        public ImportStatus()
        {
            this.catalogedPageCount = 0;
            this.importedPageCount = 0;
        }
        
        public ImportStatus(Integer catalogedPageCount, Integer importedPageCount)
        {
            this.catalogedPageCount = catalogedPageCount;
            this.importedPageCount = importedPageCount;
        }

        /**
         * Represents the total number of cataloged items in the process.
         * 
         * @return {@link Integer} not <code>null</code> after constructor.
         */
        public Integer getCatalogedPageCount()
        {
            return catalogedPageCount;
        }

        /**
         * @see #getCatalogedPageCount()
         * @param catalogedPageCount {@link Integer} assumed not
         *            <code>null</code>.
         */
        public void setCatalogedPageCount(Integer catalogedPageCount)
        {
            this.catalogedPageCount = catalogedPageCount;
        }

        /**
         * Represents the amount of items that have been imported.
         * 
         * @return {@link Integer} not <code>null</code> after constructor.
         */
        public Integer getImportedPageCount()
        {
            return importedPageCount;
        }

        /**
         * @see #getImportedPageCount()
         * @param importedPageCount {@link Integer} assumed not
         *            <code>null</code>.
         */
        public void setImportedPageCount(Integer importedPageCount)
        {
            this.importedPageCount = importedPageCount;
        }
        
    }
    
    /**
     * 
     * @author Santiago M. Murchio
     * 
     */
    public static class UnassignedItem extends PSAbstractDataObject
    {
        private String id;

        private String name;

        private String path;
        
        private ItemStatus status;
        
        public UnassignedItem() 
        {
            
        }
        
        public UnassignedItem(String id, String name, String path, ItemStatus type)
        {
            this.id = id;
            this.name = name;
            this.path = path;
            this.status = type;
        }

        /**
         * The id of the cataloged item.
         * 
         * @return {@link String} should not be <code>null</code> after
         *         construction.
         */
        public String getId()
        {
            return id;
        }

        /**
         * @see #getId()
         * 
         * @param id {@link String} assumed not <code>null</code>.
         */
        public void setId(String id)
        {
            this.id = id;
        }

        /**
         * The name of this cataloged item.
         * 
         * @return {@link String} should not be <code>null</code> after
         *         construction.
         */
        public String getName()
        {
            return name;
        }

        /**
         * @see #getName()
         * @param name {@link String} assumed not <code>null</code>.
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * The status of this item.
         * 
         * @return {@link ItemStatus} should not be <code>null</code>
         *         after constructor.
         */
        public ItemStatus getStatus()
        {
            return status;
        }

        /**
         * @see #getStatus()
         * @param status {@link ItemStatus} assumed not
         *            <code>null</code>.
         */
        public void setStatus(ItemStatus status)
        {
            this.status = status;
        }

        /**
         * The path of this cataloged item.
         * @return {@link String} should not be <code>null</code> after construction.
         */
        public String getPath()
        {
            return path;
        }
        
        /**
         * @see #getPath()
         * 
         * @param path {@link String} assumed not <code>null</code>.
         */
        public void setPath(String path)
        {
            this.path = path;
        }
    }

    /**
     * Enumeration used to represent the status of the Cataloged item.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public enum ItemStatus 
    {
        /**
         * The item has already been imported.
         */
        Imported("Imported"),
        
        /**
         * The item is being imported right now.
         */
        Importing("Importing"), 
        
        /**
         * The item is yet to be imported.
         */
        Cataloged("Cataloged");

        private String name;

        private ItemStatus(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }
}
