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
package com.percussion.services.purge;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.services.purge.data.RevisionData;
import com.percussion.share.service.exception.PSValidationException;

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
   public int purge(PSLocator item) throws PSException, PSValidationException;
  
   /**
    * This is a fast Database method for purging Navon and Navtree items from a
    * folder and subfolders. This will prevent inconsistent navigation making
    * sure navon items are not left without a parent.
    * 
    * @param item
    * @return
    * @throws PSException
    */
   public int purgeNavigation(PSLocator item) throws PSException, PSValidationException;
 

   
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
   public int purgeNavigationAndFolders(List<PSLocator> items) throws PSException, PSValidationException;
 

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
    public int purgeAll(PSLocator parent, Collection<PSLocator> items) throws PSException, PSValidationException;
   

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
   public  int purgeAll(Collection<PSLocator> items) throws PSException, PSValidationException;

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
         List<Integer> typeFilter) throws PSException, PSValidationException;
  
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
