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
package com.percussion.services.filestorage;

import com.percussion.services.filestorage.data.PSBinary;
import com.percussion.services.filestorage.data.PSBinaryMetaKey;
import com.percussion.services.filestorage.data.PSHashedColumn;
import java.io.InputStream;
import java.sql.Blob;
import java.util.List;
import java.util.Set;

/**
 * @author stephenbolton
 * 
 */

public interface IPSHashedFileDAO
{

   /**
    * Get a binary using the sha1 hash value
    * @param hash
    * @return
    */
   public PSBinary getBinary(String hash);

   /**
    * Save a binary
    * @param binary
    */
   public void save(PSBinary binary);

   /**
    * Delete a binary using its sha1 hash
    * @param hash
    */
   public void delete(String hash);

   /**
    * Delete a binary
    * @param file
    */
   public void delete(PSBinary file);

   /**
    * Does a binary with the specified sha1 has exist
    * @param hash
    * @return
    */
   public boolean exists(String hash);

   /**
    * remove all metadata for a binary;
    * @param binary
    */
   public void deleteMeta(PSBinary binary);

   /**
    * Count the number of binaries that have not been touched within specified
    * number of days.
    * @param days
    * @return
    */
   public long countOlderThan(int days);

   /**
    * Delete the number of binaries that have not been touched within specified
    * number of days.
    * @param days
    * @return
    */
   public long deleteOlderThan(int days);

   /**
    * Touch items by their hash.  Items are touched to the day.
    * @param hashes
    */
   public void touch(List<String> hashes);

   /**
    * get the list of all metadata keys.
    * @return list of metadata key entities
    */
   public List<PSBinaryMetaKey> getMetaKeys();

   /**
    * Get a metadata key by key name
    * @param keyname
    * @return the metadata key object
    */
   public PSBinaryMetaKey getMetaKey(String keyname);

   /**
    * Mark all metadata for reparsing.
    */
   public void setReparseAllMeta();

   /**
    * Get a batch of binary items marked for reparsing metadata
    * @param batchSize
    * @return a list of binary items
    */
   public List<PSBinary> getReparseBatch(int batchSize);

   /**
    * Returning and existing or create a new metadata key
    * @param name
    * @param enabled
    * @return
    */
   public PSBinaryMetaKey findOrCreateMetaKey(String name, boolean enabled);

   /**
    * Return a unique list of hashes in the database from the specified set of columns
    * @param columns
    * @return
    */
   public List<String> getAllHashes(Set<PSHashedColumn> columns);
   
   /**
    * Return a paged list of all binary items.
    * @param pageNum  zero based page number
    * @param pageSize
    * @return
    */
   public List<PSBinary> findAllBinary(int pageNum, int pageSize);

   boolean hasLegacyTable();

   public Blob createBlob(InputStream is, long l);

}
