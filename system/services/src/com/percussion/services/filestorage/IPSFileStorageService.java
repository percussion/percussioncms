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
package com.percussion.services.filestorage;

import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.data.PSMeta;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * The file storage service manages files such that multiple
 * revisions of an item or multiple items can use a single
 * binary source.
 * 
 * @author peterfrontiero
 * @author stephenbolton
 */
public interface IPSFileStorageService
{
   
   /**
    * Stores the specified binary stream and populates the specified
    * meta object with the metadata of the file if it does
    * not already exist. 
    *  
    * @param file to store, not <code>null</code>.
    * @param contentType used to help fine grained detection calculated if <code>null</code>.
    * @param originalFilename used to help fine grained type detection and identification in db can be <code>null</code>.
    * @param encoding used to help fine grained type detection and identification in db can be <code>null</code>
    * @return a unique hash value for the file, never
    * <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public String store(InputStream is, String contentType, String originalFilename, String encoding) 
      throws Exception;
   
   
   /**
    * Stores the specified File and populates the specified
    * meta object with the metadata of the file if it does
    * not already exist. 
    *  
    * @param file to store, not <code>null</code>.  If file is a PSPurgableTemp file type, encoding, and source filename
    * are used to help extraction, otherwise just the filename.  The more information the better the detection.
   * 
    * @return a unique hash value for the file, never
    * <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   public String store(File file) 
      throws Exception;

   
   /**
    * Helper method to use Tika to extract the correct mimetype for a file. Supports
    * both regular files and PSPurgableTempFile objects where it will use the sourceFilename
    * @param file the file to check
    * @return the mime type
    */
   public String getType(File file);
   

   /**
    * Deletes the file with the specified hash, along with its
    * meta object.
    * 
    * @param hash hash of the file. Cannot be <code>null</code>
    * nor empty.
    * 
    * @throws Exception if an error occurs.
    */
   public void delete(String hash) throws Exception;
   
   /**
    * For each file, any additional properties will also be
    * stored as metadata.
    * 
    * @param hash of the file returned from a call to
    * {@link #store(File, PSMeta)}, not <code>null</code>.
    * 
    * @return the metadata for the specified file, never
    * <code>null</code>.
    */
   public IPSFileMeta getMeta(String hash);
   
   /**
    * Extract the text from the specified file.
    * 
    * @param hash of the file returned from a call to
    * {@link #store(File, PSMeta)}, not <code>null</code>.
    * 
    * @return the text of the specified file, never
    * <code>null</code>.
    */
   public String getText(String hash);

   /**
    * Gets an input stream to the specified file.
    * 
    * @param hash of the file returned from a call to
    * {@link #store(File, PSMeta)}, not <code>null</code>.
    * 
    * @return an input stream to the specified file, may be
    * <code>null</code> if the file does not exist.
    */
   public InputStream getStream(String hash);

   /**
    * Determines if the specified file has already been
    * stored.
    * 
    * @param hash of the file returned from a call to
    * {@link #store(File, PSMeta)}, not <code>null</code>.
    * 
    * @return <code>true</code> if the file exists,
    * <code>false</code> otherwise.
    * 
    * @throws Exception if an error occurs.
    */
   boolean fileExists(String hash) throws Exception;
    
   /**
    * How many items have not been touched in the specified number of days
    * Use touchAllHashes to ensure known references in sysem are not removed.
    * @param days
    * @return
    */
   public long countOlderThan(int days);
   
   /**
    * purge binary items if they have not been touched in the specified number of days.
    * Use touchAllHashes to ensure known references in sysem are not removed.
    * @param days
    * @return
    */
   public long deleteOlderThan(int days);
   
   /**
    * Touch all binaries specified in the list of columns
    * @param columns
    */
   public void touchAllHashes(Set<PSHashedColumn>columns);

   /**
    * Update the persisted original filename stored for the hash
    * This filename is used for metadata generation to help
    * identify type and item identification.  There is only
    * one filename regardless of how many times it is in the system
    * this may be used as a default filename, but actual filename
    * should be stored and maintained on the system.
    * 
    * 
    * @param hash
    * @param value
    */
   public void updateFilename(String hash, String value);

   /**
    * Modify Stored content type for the hash.  This is used
    * to help metadata regeneration.   Metadata regeneration
    * that is called after update type may modify the specified 
    * value if it is not correct.
    * @param hash
    * @param value
    */
   public void updateType(String hash, String value);

   /**
    * Modify Stored encoding for the hash.  This is used
    * to help metadata regeneration.   Metadata regeneration
    * that is called after update type may modify the specified 
    * value if it is not correct.
    * @param hash
    * @param value
    */
   public void updateEncoding(String hash, String value);

   /**
    * Get the list of known metadata keys from all processed binaries
    * 
    * @return
    */
   public List<String> getMetaKeys();
   
   /**
    * Get a list of all metadata keys that have been disabled
    * @return list of key strings
    */
   public List<String> getDisabledMetaKeys();
   
   /**
    * Enable a metadata key.
    * Metadata would need to be regenerated to persist
    * this data for existing items
    * 
    * @param keyname
    * @return was the value changed
    */
   public boolean enableMetaKey(String keyname);
   
   /**
    * Disable a metadata key.
    * Metadata would need to be regenerated to persist
    * this data for existing items
    * 
    * @param keyname
    * @return was the value changed
    */
   public boolean disableMetaKey(String keyname);

   /**
    * Mark all items for reparsing of metadata and start a thread
    * to process them
    */
   public boolean reparseMetaAll();

   /**
    * Regenerate the metadata for an individual item.  This may help to identify
    * issues the parsing. If there was a parse error the metadata on the item
    * can be accessed to identify the error.
    * @param hash
    * @return true if there was a parse error
    */
   public boolean reparseMeta(String hash);
   
   /**
    * Allows for bulk import of binaries into the system.  The directory
    * specified will be recursively processed to look for any files ending
    * with .sha1.  If this file contains a sha1 hash that is already in the system
    * this item will be skipped.  Otherwise a file in the same directory with the same name 
    * excluding the .sha1 extension.   Is imported into the system, and the .sha1 file is
    * updated if the hash is not present, or is incorrect. on unix sha1sum command could be used
    * to pre-generate hashes for files which would improve performance for items already present.
    * Otherwise an empty .sha1 file could be used and it will be updated.  Exporting creates these files
    * @param rootPath
    * @return true if the call started the processing false if it was already running
    */
   public boolean importAllBinary(String rootPath);

   /**
    * Exports all binaries to the filesystem.  Binaries are output to a folder structure based upon
    * the items sha1 hash.  The first 6 characters of the hash are used to create 3 sets of folders
    * with names 2 characters long.  The rest creates a 4th level folder.  The original filename stored
    * in the database is used to create the filename for the item.  an sha1 file is also create for the item
    * The folder structure created can be used for importAllBinary allowing for backup and restore of binaries
    * that may have been removed prevously or as an alternative way to duplicate systems with a smaller db backup
    * @param rootPath
    * @return true if the call started the processing false if it was already running
    */
   public boolean exportAllBinary(String rootPath);
   
   /**
    * This is used to export all the binaries stored in the legacy PSX_BINARYSTORE table
    * @param rootPath
    * @return true if the call started the processing false if it was already running
    */
   public boolean exportAllLegacyBinary(String rootPath);

   
   /**
    * Check to see if the import/export thread is currently running
    * @return
    */
   public boolean isImpExpRunning();
   
   /**
    * Check to see if the reparse meta thread is currently running
    * @return
    */
   public boolean isReparseMetaRunning();
   
   /**
    * Get the hashing algorithm used.  Currently sha1.  This hasing is used for 
    * uniqueness identification only so does not have security implications requiring more
    * secure hashing algorithms.
    * @return the algorithm used for checksum generation,
    * never <code>null</code>.
    */
   public String getAlgorithm();


   /**
    * Check if legacy table PSX_BINARYMETA exists.
    * @return true if table is in DB
    */
   public boolean legacyTableExists();
}
