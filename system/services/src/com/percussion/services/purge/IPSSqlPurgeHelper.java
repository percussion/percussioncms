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
package com.percussion.services.purge;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.services.purge.data.RevisionData;

import java.util.Collection;
import java.util.List;

public interface IPSSqlPurgeHelper
{
   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param item
    * @return
    * @throws PSException
    */
   public int purge(PSLocator item) throws PSException;
  
   /**
    * This is a fast Database method for purging Navon and Navtree items from a
    * folder and subfolders. This will prevent inconsistent navigation making
    * sure navon items are not left without a parent.
    * 
    * @param item
    * @return
    * @throws PSException
    */
   public int purgeNavigation(PSLocator item) throws PSException;
 

   
   /**
    * This is a fast Database method for purging Navon and Navtree and folder
    * items from a folder and subfolders. This is a replacement for the older
    * remove from folder on a folder functionality. This mechanism is not
    * normally recommended as it will orphan all the other content making it
    * only available from search.
    * 
    * @param items
    * @return
    * @throws PSException
    */
   public int purgeNavigationAndFolders(List<PSLocator> items) throws PSException;
 

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param items
    * @return 
    * @throws PSException
    */
    public int purgeAll(PSLocator parent, Collection<PSLocator> items) throws PSException;
   

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed.
    * 
    * @param items
    * @return
    * @throws PSException
    */
   public  int purgeAll(Collection<PSLocator> items) throws PSException;

   /**
    * This is a fast Database method for purging items and folders from the
    * system If folders are selected subfolders will be recursively purged as
    * well as content unless the items are also linked to other folders not
    * being purged. No effects will be run on this action to prevent any side
    * effects, all relationships to and from the items will be removed. If
    * typeFilter is not null, only the specified content type ids will be
    * removed.
    * 
    * @param parent - the parent folder if available. items will not be purged
    *           from this folder if they are linked elsewhere.
    * @param items
    * @param typeFilter
    * @return
    * @throws PSException
    */
   public int purgeAll(PSLocator parent, Collection<PSLocator> items,
         List<Integer> typeFilter) throws PSException;
  
   /**
   * Uses direct database access to purge revision items matching provided
   * critera
   *
   * min cond. #    1) ALWAYS keep minimum number X of revisions to keep [number]
   * min cond. date 2) ALWAYS keep all revisions D younger than number of days [number]
   *
   * if after that anything is left, than:
   *
   * max cond #     3) delete all revisions above Y number (count) (delete [Y+1,....max])
   * max cond date  4) delete all revisions above E days old (delete [E+1,....max])
   *
   * Y must be greater than X (if set); error if not.
   * E must be greater than D (if set); error if not.
   *
   * @param typeFilter
   * @return the number of revisions removed across all items.
   * @throws Exception
   */
   public int purgeRevisions(RevisionData data) throws Exception;

   /**
    * The relationship type for folder object
    */
   public static final String FOLDER_RELATE_TYPE = PSRelationshipConfig.TYPE_FOLDER_CONTENT;
   
}