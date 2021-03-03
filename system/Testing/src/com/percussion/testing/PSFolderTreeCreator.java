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
package com.percussion.testing;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

/**
 * Create a folder tree for test purposes. Inspired by
 * <code>PSContentHelper</code> that Paul wrote, but using the web services
 * apis.
 * 
 * @author dougrand
 */
public class PSFolderTreeCreator
{
   /**
    * The names of the folders use this id to make them unique
    */
   private static AtomicInteger ms_fid = new AtomicInteger();
   
   /**
    * Create a folder tree rooted at <code>destPath</code> which is seeded in
    * each folder by content selected from the folder at <code>sourcePath</code>.
    * Each folder is seeded with some random amount of cloned content from the
    * source folder.
    * 
    * @param sourcePath the source path, never <code>null</code> or empty, the
    *           content of the source folder is used to seed the destination
    *           folders.
    * @param destPath the destination path, never <code>null</code> or empty,
    *           the folders are created in the destination path folder. Items
    *           are populated starting in the destination path folder.
    * @param folderCount the total number of folders to create, must be a
    *           positive integer.
    * @param depth the depth to create folders to, a depth of <code>0</code>
    *           means that no folders will be created, but content will be
    *           cloned.
    * @param contentPercentage a number from zero to one, this indicates what
    *           maximum percentage of all content will be cloned into each
    *           folder. The actual amount will be varied.
    * @param distributionPerLevel a number from zero to one, this indicates what
    *           percentage of folders will be created for each level. The actual
    *           number may be adjusted to provide that all the folders are
    *           created. There will never be less than one folder created for a
    *           given level.
    * 
    * @throws Exception
    */
   public static void createTree(String sourcePath, String destPath,
         int folderCount, int depth, float contentPercentage,
         float distributionPerLevel) throws Exception
   {
      if (sourcePath == null || StringUtils.isBlank(sourcePath))
      {
         throw new IllegalArgumentException(
               "sourcePath may not be null or empty");
      }
      if (destPath == null || StringUtils.isBlank(destPath))
      {
         throw new IllegalArgumentException("destPath may not be null or empty");
      }
      if (folderCount < 1)
      {
         throw new IllegalArgumentException("folderCount must be positive");
      }
      if (depth < 0)
      {
         throw new IllegalArgumentException("depth must be zero or positive");
      }
      if (contentPercentage < 0.0 || contentPercentage > 1.0)
      {
         throw new IllegalArgumentException(
               "contentPercentage must be in the range {0.0:1.0}");
      }
      if (distributionPerLevel < 0.0 || distributionPerLevel > 1.0)
      {
         throw new IllegalArgumentException(
               "contentPercentage must be in the range {0.0:1.0}");
      }
      IPSContentWs contentWs = PSContentWsLocator.getContentWebservice();
      String paths[] = new String[2];
      paths[0] = sourcePath;
      paths[1] = destPath;
      List<PSFolder> folders = contentWs.loadFolders(paths);

      SecureRandom r = new SecureRandom();

      int remaining = folderCount;
      while (remaining > 0)
      {
         int count = (int) ((distributionPerLevel * r.nextFloat() 
               * folderCount) + .25);
         if (count > remaining)
         {
            count = remaining;
         }
         createFolderTree(contentWs, folders.get(0), destPath, count, depth,
               contentPercentage, distributionPerLevel, r);
         remaining -= count;
      }
   }

   /**
    * Create the folder tree starting at the given destination. At any given
    * level, the current folder is created. Then the content is cloned. Finally
    * the code recurses for each of the folders to be created at the current
    * level. This process is repeated until there are no more folders left in
    * the folder count.
    * 
    * @param contentWs the content webservice implementation
    * @param src the source folder, assumed never <code>null</code>
    * @param destPath the destination folder, assumed never <code>null</code>
    *           or empty
    * @param folderCount the count of folders to create in this folder and any
    *           sub folders, assumed greater than zero
    * @param depth the depth to traverse when creating subfolders
    * @param contentPercentage the maximum percentage of content to clone from
    *           the source directory
    * @param distributionPerLevel the percentage of folders remaining to create
    *           in each level
    * @param rand a random number generator that is shared
    * @throws PSErrorException
    * @throws PSErrorResultsException
    */
   private static void createFolderTree(IPSContentWs contentWs, PSFolder src,
         String destPath, int folderCount, int depth, float contentPercentage,
         float distributionPerLevel, Random rand) throws PSErrorException,
         PSErrorResultsException
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      // Create the folder
      String name = "folder" + ms_fid.getAndIncrement();
      String path = destPath + "/" + name;
      contentWs.addFolder(name, destPath);
      IPSGuid folderId = gmgr.makeGuid(src.getLocator());
      List<PSItemSummary> sums = contentWs.findFolderChildren(folderId, false);
      float clonePercent = (float) (.5 + rand.nextFloat()) * contentPercentage;
      int cloneCount = (int) (clonePercent * (.5 + (float) sums.size()));
      List<IPSGuid> idsToClone = new ArrayList<IPSGuid>();
      List<String> clonePaths = new ArrayList<String>();
      for (int i = 0; i < cloneCount; i++)
      {
         PSItemSummary sumToClone = sums.get(Math
               .abs(rand.nextInt(sums.size())));
         clonePaths.add(path);
         idsToClone.add(sumToClone.getGUID());
      }
      if (idsToClone.size() > 0)
      {
         contentWs.newCopies(idsToClone, clonePaths, null, false);
      }

      int remaining = folderCount;
      if (depth == 1)
      {
         // No more levels, just create the remaining folders right here
         for (int i = 0; i < remaining; i++)
         {
            createFolderTree(contentWs, src, path, 0, 0, contentPercentage,
                  distributionPerLevel, rand);
         }
      }
      else
      {
         while (remaining > 0)
         {
            int count = (int) ((distributionPerLevel * rand.nextFloat() 
                  * folderCount) + .25);
            if (count > remaining || count == 0)
            {
               count = remaining;
            }
            createFolderTree(contentWs, src, path, count, depth - 1,
                  contentPercentage, distributionPerLevel, rand);
            remaining -= count;
         }
      }
   }
}
