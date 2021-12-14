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
package com.percussion.search.lucene;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.server.IPSItemDefChangeListener;
import com.percussion.cms.objectstore.server.PSFieldRetriever;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSSearchConfig;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSException;
import com.percussion.search.PSSearchAdmin;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSSearchAdminImpl extends PSSearchAdmin
{

   /**
    * Create a configuration manager for the search engine.
    */
   PSSearchAdminImpl() 
   {
      PSServerConfiguration config = PSServer.getServerConfiguration();
      PSSearchConfig searchConfig = config.getSearchConfig();
      
      if (searchConfig != null && searchConfig.isFtsEnabled())
      {
         // Register listeners on the item def manager to handle
         // content editors being added or removed dynamically. The philosphy
         // is to handle the adds/updates, and ignore the removals as the
         // removal may be temporary. A separate processing step on startup
         // will find content types that are truly removed from the RX 
         // objectstore.
         PSItemDefManager mgr = PSItemDefManager.getInstance();
         mgr.addListener(new IPSItemDefChangeListener() 
         {
            public void registered(PSItemDefinition def, boolean notify) 
               throws PSException
            {
               update(def, new PSFieldRetriever(def.getTypeId()), notify);
            }

            public void unregistered(PSItemDefinition def, boolean notify)
            {
               // Ignore
            }
         }
         );
      }
   }
   
   /**
    * This method deletes the folders related to the supplied type id.
    */
   @Override
   protected void doDelete(PSKey key) throws PSSearchException
   {
      if (key == null)
         throw new IllegalArgumentException("key may not be null");
      String ctypeDirPath = PSSearchEngineImpl.getLuceneIndexRootPath()
            + key.getPart();
      File ctypeDir = new File(ctypeDirPath);
      if (!ctypeDir.exists())
      {
         // The directory does not exist and there is nothing to delete
         // Write some debug information
         String msg = "Skipping deletion of indexes for content type id({0})."
               + " No folder exists.";
         Object[] args = { key.getPart() };
         log.debug(MessageFormat.format(msg, args));
         return;
      }
      if (!ctypeDir.isDirectory())
      {
         // If it is not a directory log and return
         // This should not happen
         String msg = "Skipping deletion of indexes for content type id({0})."
               + " As the index directory path(\"{1}\") for the supplied content "
               + " type id is not a Directory.";
         Object[] args = { key.getPart(), ctypeDirPath };
         log.debug(MessageFormat.format(msg, args));
         return;
      }
      try
      {
         // Close the index writer before deleting
         closeIndexWriter(key);
         boolean success = deleteDir(ctypeDir);
         if (!success)
         {
            String msg = "Falied to delete the index directory for content "
                  + "type id ({0}).";
            log.error(MessageFormat.format(msg, key.getPart()));
         }
         String msg = "Successfully deleted the index directory for content "
               + "type id ({0}).";
         log.debug(MessageFormat.format(msg, key.getPart()));
      }
      catch (Exception e)
      {
         String msg = "Falied to delete the index directory for content "
               + "type id ({0}).";
         throw new PSSearchException(MessageFormat.format(msg, key.getPart()),
               e);
      }
   }
   

   /**
    * Closes an IndexWriter that is associtaed with the supplied content type.
    * @param key Content type key whose index writer needs to be closed.
    * @throws CorruptIndexException
    * @throws IOException
    */
   private void closeIndexWriter(PSKey key)
      throws CorruptIndexException, IOException
   {
      Map<Long, IndexWriter> iws = PSSearchIndexerImpl.getLuceneIndexWriters();
      Long ctypeId = new Long(key.getPartAsInt());
      IndexWriter iw = iws.get(ctypeId);
      if (iw != null)
      {
         iw.close();
         iws.remove(ctypeId);
      }
      
   }
   
   /**
    * Deletes all files and subdirectories under dir. Returns true if all
    * deletions were successful. If a deletion fails, the method stops
    * attempting to delete and returns false.
    * 
    * @param dir The directory that needs to be deleted.
    * @return <code>true</code> if deletion succeeds.
    */
   private boolean deleteDir(File dir)
   {
      if (dir.isDirectory())
      {
         String[] children = dir.list();
         for (int i = 0; i < children.length; i++)
         {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success)
            {
               return false;
            }
         }
      }

      // The directory is now empty so delete it
      return dir.delete();
   }

   
   @Override
   public PSKey[] optimizeIndexes(PSKey[] contentTypes)
         throws PSSearchException
   {
      validateKeyArray(contentTypes);
      Map<Long, IndexWriter> iws = PSSearchIndexerImpl.getLuceneIndexWriters();
      List<PSKey> successList = new ArrayList<>();
      for (PSKey key : contentTypes)
      {
         Long ctypeId = new Long(key.getPartAsInt());
         IndexWriter iw = iws.get(ctypeId);
         if (iw != null)
         {
            String msg = "Failed to optimize index for content type with id"
                  + key.getPartAsInt();
            try
            {
               iw.forceMerge(1);
               successList.add(key);
               String msg1 = "Successfully optimized indexes for content " +
                     "type id ({0}).";
               log.debug(MessageFormat.format(msg1, key.getPart()));
            }
            catch (CorruptIndexException e)
            {
               throw new PSSearchException(msg, e);
            }
            catch (IOException e)
            {
               throw new PSSearchException(msg, e);
            }
         }
      }
      return successList.toArray(new PSKey[successList.size()]);
   }

   @Override
   public PSKey[] rebuildIndexes(PSKey[] contentTypes) throws PSSearchException
   {
      validateKeyArray(contentTypes);
      
      if (contentTypes.length > 1)
      {
         log.info("Rebuilding Indexes for {} Content Types.", contentTypes.length);
      }
      
      List<PSKey> successList = new ArrayList<>();
      for (PSKey key : contentTypes)
      {
         doDelete(key);
         PSSearchIndexEventQueue.getInstance().indexContentType(
               key.getPartAsInt());      
         successList.add(key);
         String msg = "Successfully queued items of content type id ({0}) " +
               "for reindexing.";
         log.debug(MessageFormat.format(msg, key.getPart()));
      }
      return successList.toArray(new PSKey[successList.size()]);
   }

   /**
    * Checks that the supplied array is non-<code>null</code>, has at least
    * 1 entry and that no entries are <code>null</code>.
    * 
    * @param types Anything allowed.
    */
   private void validateKeyArray(PSKey[] types)
   {
      if (null == types || types.length < 1)
      {
         throw new IllegalArgumentException(
               "contentTypes must have at least 1 entry");
      }
      
      for (int i=0; i < types.length; i++)
      {
         if (types[i] == null)
            throw new IllegalArgumentException("No null entries in array.");            
      }
   }
   
   /**
    * This is not used for Lucene
    */
   @Override
   public void save() throws IOException, PSSearchException
   {
      //This method does nothing as there is nothing to save incase of lucene.
   }

   /**
    * This is not used for Lucene
    */
   @Override
   protected boolean update(PSItemDefinition def, boolean notify)
         throws PSSearchException
   {
      //This method does nothing as there is nothing to update incase of lucene.
      //return false so that it does not warn per reindexing.
      return false;
   }

   /**
    * The scheme for creating the lucene indexes is to create a folder with
    * contenttype id under indexes folder. The folder indexes is created under
    * search config directory if it does not exist. This method walks through
    * all the folders under indexes folder and checks whether a
    * contenttype with the id exists or not if not it logs that folder other
    * wise it creates the folder.
    */
   @Override
   public void verify(Set<Long> knownContentTypes) throws PSSearchException
   {
      Set<Long> currentIndexes = new HashSet<>();
      File rootDir = new File(PSSearchEngineImpl.getLuceneIndexRootPath());
      if (rootDir.exists())
      {
         File[] indexDirs = rootDir.listFiles();
         for (File indexDir : indexDirs)
         {
            if (!indexDir.isDirectory())
               continue;
            Long id = null;
            try
            {
               id = Long.parseLong(indexDir.getName());
            }
            catch (NumberFormatException e)
            {
               // ignore this folder and continue with other folders
            }
            if (id != null)
               currentIndexes.add(id);
         }
      }
      Set<Long> currentIndexesCopy = new HashSet<>();
      currentIndexesCopy.addAll(currentIndexes);

      Set<Long> knownContentTypesCopy = new HashSet<>();
      knownContentTypesCopy.addAll(knownContentTypes);

      // Remove knownContentTypes from currentIndexesCopy
      currentIndexesCopy.removeAll(knownContentTypes);
      // Remove currentIndexes from knownContentTypesCopy that leaves us with
      // ids of directories that needs to be created.
      knownContentTypesCopy.removeAll(currentIndexes);

      for (Long id : currentIndexesCopy)
      {
         String msg = "A directory with name {0} exists under lucene indexes "
               + "folder {1} without a known content type.";
         Object[] args = { id.toString(),
               PSSearchEngineImpl.getLuceneIndexRootPath() };
         log.info(MessageFormat.format(msg, args));
      }
      // Create directories
      for (Long id : knownContentTypesCopy)
      {
         boolean success = (new File(PSSearchEngineImpl
               .getLuceneIndexRootPath()
               + id.toString())).mkdirs();
         if (success)
         {
            String msg = "Created an index directory for content type id ({0}, submitting the type for indexing...)";
            log.info(MessageFormat.format(msg, id.toString()));

            PSKey[] keys = new PSKey[1];
            keys[0] = PSContentType.createKey(id.intValue());
            rebuildIndexes(keys);
         }
         else
         {
            String msg = "Failed to create an index directory for content "
                  + "type id ({0})";
            log.info(MessageFormat.format(msg, id.toString()));
         }
      }

   }

   /**
    * Call this method during server startup only.
    */
   @Override
   public void clearIndexLocks()
   {
      File rootDir = new File(PSSearchEngineImpl.getLuceneIndexRootPath());
      if (rootDir.exists())
      {
         File[] indexDirs = rootDir.listFiles();

         for (File indexDir : indexDirs)
         {
            if (!indexDir.isDirectory())
               continue;
            // Delete the lock and try again
            String[] lockedFiles = indexDir.list(new FilenameFilter()
            {
               public boolean accept(File dir, String name)
               {
                  return name.toLowerCase().endsWith(".lock");
               }
            });
            for (String lf : lockedFiles)
            {
               File file = new File(indexDir.getAbsolutePath()
                     + File.separator + lf);
               file.delete();
            }
         }
      }
   }
   
   /**
    * Reference to log for this class
    */
   private final static Logger log = LogManager.getLogger(IPSConstants.SEARCH_LOG);


   
}
