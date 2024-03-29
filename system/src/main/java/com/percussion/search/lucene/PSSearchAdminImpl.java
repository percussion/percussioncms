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
import com.percussion.error.PSExceptionUtils;
import com.percussion.search.PSSearchAdmin;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
         // content editors being added or removed dynamically. The philosophy
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
            String msg = "Failed to delete the index directory for content "
                  + "type id ({0}).";
            log.error(MessageFormat.format(msg, key.getPart()));
         }
         String msg = "Successfully deleted the index directory for content "
               + "type id ({0}).";
         log.debug(MessageFormat.format(msg, key.getPart()));
      }
      catch (Exception e)
      {
         String msg = "Failed to delete the index directory for content "
               + "type id ({0}).";
         throw new PSSearchException(MessageFormat.format(msg, key.getPart()),
               e);
      }
   }
   

   /**
    * Closes an IndexWriter that is associated with the supplied content type.
    * @param key Content type key whose index writer needs to be closed.
    */
   private void closeIndexWriter(PSKey key)
      throws IOException
   {
      Map<Long, IndexWriter> iws = PSSearchIndexerImpl.getLuceneIndexWriters();
      Long ctypeId = (long) key.getPartAsInt();
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
         if (children != null) {
            for (String child : children) {
               boolean success = deleteDir(new File(dir, child));
               if (!success) {
                  return false;
               }
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
         Long ctypeId = (long) key.getPartAsInt();
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
            } catch (IOException e)
            {
               throw new PSSearchException(msg, e);
            }
         }
      }
      return successList.toArray(new PSKey[0]);
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
         String idAsString = String.valueOf(key.getPartAsInt());
         boolean success = (new File(PSSearchEngineImpl
                 .getLuceneIndexRootPath()
                 + idAsString)).mkdirs();
         if(success) {

            PSSearchIndexEventQueue.getInstance().indexContentType(
                    key.getPartAsInt());
            successList.add(key);
            String msg = "Successfully queued items of content type id ({0}) " +
                    "for reindexing.";
            log.debug(MessageFormat.format(msg, key.getPart()));
         }else{
            String msg = "Failed to create an index directory for content "
                    + "type id ({0})";
            log.info(MessageFormat.format(msg, idAsString));
         }
      }
      return successList.toArray(new PSKey[0]);
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

      for (PSKey type : types) {
         if (type == null)
            throw new IllegalArgumentException("No null entries in array.");
      }
   }
   
   /**
    * This is not used for Lucene
    */
   @Override
   public void save() throws IOException, PSSearchException
   {
      //This method does nothing as there is nothing to save in case of lucene.
   }

   /**
    * This is not used for Lucene
    */
   @Override
   protected boolean update(PSItemDefinition def, boolean notify)
         throws PSSearchException
   {
      //This method does nothing as there is nothing to update in case of lucene.
      //return false so that it does not warn per reindexing.
      return false;
   }

   /**
    * The scheme for creating the lucene indexes is to create a folder with
    * content type id under indexes folder. The folder indexes is created under
    * search config directory if it does not exist. This method walks through
    * all the folders under indexes folder and checks whether a
    * content type with the id exists or not if not it logs that folder other
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
         if(indexDirs!=null) {
            for (File indexDir : indexDirs) {
               if (!indexDir.isDirectory())
                  continue;
               Long id = null;
               try {
                  id = Long.parseLong(indexDir.getName());
               } catch (NumberFormatException e) {
                  // ignore this folder and continue with other folders
               }
               if (id != null)
                  currentIndexes.add(id);
            }
         }
      }
      Set<Long> currentIndexesCopy = new HashSet<>(currentIndexes);

      Set<Long> knownContentTypesCopy = new HashSet<>(knownContentTypes);

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
            String msg = "Created an index directory for content type id ({0}, submitting the type for indexing...)";
            log.info(MessageFormat.format(msg, id.toString()));

            PSKey[] keys = new PSKey[1];
            keys[0] = PSContentType.createKey(id.intValue());
            rebuildIndexes(keys);
         }

   }

   /**
    * Call this method during server startup only.
    */
   @Override
   public void clearIndexLocks() {
      File rootDir = new File(PSSearchEngineImpl.getLuceneIndexRootPath());
      if (rootDir.exists())
      {
         File[] indexDirs = rootDir.listFiles();
         if(indexDirs!=null) {
            for (File indexDir : indexDirs) {
               if (!indexDir.isDirectory())
                  continue;
               // Delete the lock and try again
               String[] lockedFiles = indexDir.list((dir, name) -> name.toLowerCase().endsWith(".lock"));
               if(lockedFiles!=null) {
                  for (String lf : lockedFiles) {
                     File file = new File(indexDir.getAbsolutePath()
                             + File.separator + lf);

                     try {
                        Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
                     } catch (IOException e) {
                        log.warn("Unable to delete lock file: {} Error: {}",
                                file.getAbsolutePath(),
                                PSExceptionUtils.getMessageForLog(e)
                                );
                     }
                  }
               }
            }
         }
      }
   }
   
   /**
    * Reference to log for this class
    */
   private final static Logger log = LogManager.getLogger(IPSConstants.SEARCH_LOG);


   
}
