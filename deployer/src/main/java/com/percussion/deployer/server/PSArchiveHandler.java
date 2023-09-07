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

package com.percussion.deployer.server;

import com.percussion.cms.IPSConstants;
import com.percussion.deployer.objectstore.*;
import com.percussion.deployer.server.dependencies.PSAclDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSContentTypeDependencyHandler;
import com.percussion.deployer.server.dependencies.PSSupportFileDependencyHandler;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Handling saving and retrieving IDTypes and dependencies to and from
 * a <code>PSArchive</code> object.
 */
public class PSArchiveHandler
{

   private static final Logger log = LogManager.getLogger(IPSConstants.PACKAGING_LOG);

   /**
    * Constructing the handler from a <code>PSArchive</code> object.
    * The caller is responsible for calling the {@link #close()} method when
    * finished.
    *
    * @param archive The archive object file. It may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    * @throws PSDeployException If any other error occurs.
    */
   public PSArchiveHandler(PSArchive archive) throws PSDeployException
   {
      if ( archive == null )
         throw new IllegalArgumentException("archive may not be null");

      m_archive = archive;
      if ( m_archive.isWriting() )
         m_archiveMan = new PSArchiveManifest();
      else
         m_archiveMan = m_archive.getArchiveManifest();
   }

   /**
    * Closes all archive resources currently maintained by this object.
    * If the archive of the handler is opened for writing,
    * the current <code>PSArchiveManifest</code> will be saved into the
    * archive before closing the archive. Any subsequent method calls may
    * throw exceptions, and so the reference to the instance of this class
    * should be discarded after calling this method.
    *
    * @throws PSDeployException If any error occurs.
    */
   public void close() throws PSDeployException
   {
      if ( ! m_archive.isClosed() )
      {
         if (m_archive.isWriting())
            m_archive.storeArchiveManifest(m_archiveMan);
         m_archive.close();
      }
   }

   /**
    * Adds (or appends) a dependency along with related dependency files
    * into the archive of this object.
    *
    * @param dep The dependency object. It may not be <code>null</code>.
    * @param files An iterator over one or more <code>PSDependencyFile</code>
    * objects. It may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    * @throws IllegalStateException if the archive is opened for read only
    * @throws PSDeployException If any error occurs while adding the files
    * to the archive.
    */
   public void addFiles(PSDependency dep, Iterator files)
      throws PSDeployException
   {
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");
      if ( files == null || (! files.hasNext()) )
         throw new IllegalArgumentException("files may not be null or empty");
      if ( ! m_archive.isWriting() )
         throw new IllegalStateException("archive is not opened for writing");

      String name = dep.getDisplayName();
      String key = dep.getKey();
      String[] parsedKey = PSDependency.parseKey(key);
      String type = parsedKey[0];
            
      String tmpName = (type.equals(PSAclDefDependencyHandler.DEPENDENCY_TYPE)) ?
            dep.getParentDependency().getDisplayName() : name;
      tmpName = tmpName.replace(" ", "");
     
      String tmpExt;
      if (type.equals(PSSupportFileDependencyHandler.DEPENDENCY_TYPE))
      {
         tmpExt = "";
      }
      else
      {
         tmpExt = ".";
         if (type.equals(PSAclDefDependencyHandler.DEPENDENCY_TYPE))
         {
            String parentType = dep.getParentDependency().getObjectType();
            tmpExt += parentType.substring(0, 1).toLowerCase() + parentType.substring(1) + ".";
         }
         
         tmpExt += type.substring(0, 1).toLowerCase() + type.substring(1);
      }
           
      // Can only loop through the Iterator once, but it is needed twice here,
      // So let's build a list from it, which will be used later.
      List<PSDependencyFile> dupFiles = new ArrayList<>();

      List<File> tmpFiles = new ArrayList<>();
      try
      {
         // Adds to the archive first
         while (files.hasNext())
         {
            PSDependencyFile depFile = (PSDependencyFile) files.next();
            String fileName = depFile.getFile().getName();

            log.debug("Dependency File Name: {}", fileName);

            // Generate meaningful, unique names for files with names created using temporary files (design objects).
            if (depFile.getFile().exists() && fileName.startsWith("dpl_") && (fileName.endsWith(".xml") || fileName.endsWith(".tmp")))
            {
               if (ms_tmpDir == null)
               {
                  File tmpFile = File.createTempFile("tmp", ".tmp");
                  tmpFile.deleteOnExit();
                  ms_tmpDir = tmpFile.getParentFile();
               }

               String tmpFileName = tmpName + tmpExt;
               if (type.equals(PSContentTypeDependencyHandler.DEPENDENCY_TYPE))
               {
                  tmpFileName = tmpName + '.' + ms_depFileTypeMap.get(depFile.getType()) + tmpExt;
               }
               File tmp = new File(ms_tmpDir, tmpFileName);
               log.debug("Created temp file: {} in {}",
                       tmpFileName, ms_tmpDir.getAbsolutePath());

               int i = 1;
               while (tmp.exists())
               {
                  log.debug("Creating temp file: {} count: {}",tmpName,i);
                  tmp = new File(ms_tmpDir, tmpName + '(' + i++ + ')'
                        + tmpExt);
               }
               log.debug("Adding file: {}", tmp.getAbsolutePath());
               tmpFiles.add(tmp);

               log.debug("Copying file {} to temp file: {}",
                       depFile.getFile().getAbsolutePath(),
                       tmp.getAbsolutePath());
         FileUtils.copyFile(depFile.getFile(), tmp);

               depFile = new PSDependencyFile(depFile.getType(), tmp,
                     depFile.getOriginalFile());
            }

            if(depFile.getFile().exists()) {
               setArchiveLocation(depFile, dep, dupFiles);

               m_archive.storeFile(depFile.getFile(),
                       getNormalizedArchivePath(depFile));
               dupFiles.add(depFile);
            }
         }

         // then update the manifest
         if(hasDependencyFiles(dep))
            m_archiveMan.addFiles(dep, dupFiles.iterator());
      }
      catch (IOException e)
      {
         Object[] args = {m_archive.getArchiveRef().getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_WRITE_ERROR,
            args);
      }
      finally
      {
         for (File tmpFile : tmpFiles)
         {
            tmpFile.delete();
         }
      }
   }

   /**
    * Set the archive location for a given <code>PSDependencyFile</code> object
    * according to a given <code>PSDependency</code> and a list of
    * <code>PSDependencyFile</code> objects. The path of the archive location
    * should be unique among the given list of <code>PSDependencyFile</code>
    * objects.
    *
    * @param depFile The dependency file, which will be set, assume it is not
    * <code>null</code>.
    * @param dep The dependency object, assume it is not <code>null</code>.
    * @param depFiles A list of <code>PSDependencyFile</code> objects. Assume
    * it is not <code>null</code>, but may be empty.
    */
   private void setArchiveLocation(PSDependencyFile depFile, PSDependency dep,
      List<PSDependencyFile> depFiles)
   {
      File location = new File(dep.getKey(), depFile.getFile().getName());
      while ( ! hasUniquePath(location, depFiles.iterator()) )
      {
         String uniqueName = System.currentTimeMillis() +
            depFile.getFile().getName();
         location = new File(dep.getKey(), uniqueName);
      }

      depFile.setArchiveLocation(location);
   }

   /**
    * Determines if the location (as <code>File</code>) object has a unique
    * location path amount a list of <code>PSDependencyFile</code> objects.
    *
    * @param location The location object to be checked with. Assume it is not
    * <code>null</code>
    * @param depFiles A list of <code>PSDependencyFile</code> objects.
    *
    * @return <code>true</code> if the path of the <code>location</code>
    * object is unique amount the list of <code>PSDependencyFile</code> objects;
    * <code>false</code> otherwise.
    */
   private boolean hasUniquePath(File location, Iterator<PSDependencyFile> depFiles)
   {
      while (depFiles.hasNext())
      {
         PSDependencyFile depFile = (PSDependencyFile) depFiles.next();
         if (location.getPath().equals(depFile.getArchiveLocation().getPath()))
            return false;
      }
      return true;
   }

   /**
    * Determines if the archive already contains a list of dependency files
    * for a given dependency.
    *
    * @param dep The dependency object to be asked for. It may not be
    * <code>null</code>.
    *
    * @return <code>true</code> if current archive already contains a list of
    * dependency files for the given dependency; <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException If <code>dep</code> is <code>null</code>
    * @throws PSDeployException if there are any errors.
    */
   public boolean hasDependencyFiles(PSDependency dep) throws PSDeployException
   {
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");

      return m_archiveMan.hasDependencyFiles(dep);
   }

   /**
    * Adds (or append) a dependency along with related IDTypes into the archive
    * of this object.
    *
    * @param dep The dependency object. It may not be <code>null</code>.
    * @param idTypes The IDTypes which relate to the given dependency object.
    * It may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    * @throws IllegalStateException If the archive used to construct this
    * handler is not opened for writing, or if the <code>close()</code> method
    * has been called
    */
   public void addIdTypes(PSDependency dep, PSApplicationIDTypes idTypes)
   {
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");
      if ( idTypes == null )
         throw new IllegalArgumentException("idTypes may not be null");
      if (!m_archive.isWriting())
         throw new IllegalStateException("archive is not opened for writing");
      if (m_archive.isClosed())
         throw new IllegalStateException("ArchiveHandler has been closed");

      m_archiveMan.addIdTypes(dep, idTypes);
   }

   /**
    * Get a list of dependency files for a given dependency.
    *
    * @param dep The dependency which relates to the list of dependency files.
    * It may not be <code>null</code>.
    *
    * @return an iterator over zero or more <code>PSDependencyFile</code>
    * objects.
    *
    * @throws IllegalArgumentException If <code>dep</code> is <code>null</code>
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getFiles(PSDependency dep) throws PSDeployException
   {
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");

      return m_archiveMan.getFiles(dep);
   }

   /**
    * Get the IDTypes for a given dependency.
    *
    * @param dep The related dependency object for the IDTypes. It may not be
    * <code>null</code>.
    *
    * @return The related IDTypes if the dependency exists in the current
    * archive, and it contains the IDTypes, <code>null</code> otherwise.
    *
    * @throws IllegalArgumentException If <code>dep</code> is <code>null</code>
    */
   public PSApplicationIDTypes getIdTypes(PSDependency dep)
      throws PSDeployException
   {
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");

      return m_archiveMan.getIdTypes(dep);
   }
   
   /**
    * Adds (or append) a dependency along with a list of external databases 
    * referenced by this dependency.
    * 
    * @param dep The dependency for which the list is to be set.  May not be
    * <code>null</code>.
    * @param dbmsInfoList The list, may not be <code>null</code>, may be empty.
    * 
    * @throws IllegalStateException If the archive used to construct this
    * handler is not opened for writing, or if the <code>close()</code> method
    * has been called
    */
   public void addDbmsInfoList(PSDependency dep, List<PSDatasourceMap> dbmsInfoList)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!m_archive.isWriting())
         throw new IllegalStateException("archive is not opened for writing");

      if (m_archive.isClosed())
         throw new IllegalStateException("ArchiveHandler has been closed");
         
      if (dbmsInfoList == null)
         throw new IllegalArgumentException("dbmsInfoList may not be null");
         
      m_archiveMan.addDbmsInfoList(dep, dbmsInfoList);
   }

   /**
    * Gets an input stream to the specified dependency file in the archive.
    *
    * @param file The dependency file object.  Itself and its archive location
    * (the return from <code>getArchiveLocation()</code>) may not be
    * <code>null</code>.
    *
    * @return the input stream, never <code>null</code>.  Caller is responsible
    * for closing the stream.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for writing.
    * @throws PSDeployException if an error occurs reading from the archive or
    * if file is not found in archive.
    */
   public InputStream getFileData(PSDependencyFile file)
      throws PSDeployException
   {
      if (file == null || file.getArchiveLocation() == null)
         throw new IllegalArgumentException(
            "file or its archive location may not be null");

      String archivePath = getNormalizedArchivePath(file);

      return m_archive.getFile(archivePath);
   }
   
   /**
    * Gets size of the specified dependency file in the archive.
    *
    * @param file The dependency file object.  Itself and its archive location
    * (the return from <code>getArchiveLocation()</code>) may not be
    * <code>null</code>. 
    *
    * @return size.  Caller is responsible for closing the stream.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for writing.
    * @throws PSDeployException if an error occurs reading from the archive or
    * if file is not found in archive.
    */ 
   public int getFileSize(PSDependencyFile file)
      throws PSDeployException
   {
      if (file == null || file.getArchiveLocation() == null)
         throw new IllegalArgumentException(
            "file or its archive location may not be null");

      String archivePath = getNormalizedArchivePath(file);

      return m_archive.getFileSize(archivePath);
   }

   
   
   /**
    * Gets the platform independent archive location of the supplied dependency
    * file.
    * 
    * @param file The dependency file, assumed not <code>null</code> and to have
    * a non-<code>null</code>, non-empty archive location set.
    * 
    * @return The platform independent location, never <code>null</code> or 
    * empty.
    */
   private String getNormalizedArchivePath(PSDependencyFile file)
   {
      // make sure to store using "/" path separator, assumes that no 
      // filenames contain a "\".
      return PSDeployComponentUtils.getNormalizedPath(
         file.getArchiveLocation().getPath());
   }

   /**
    * The <code>PSArchive</code> object, which is managed by the current
    * object. Initialized by the constructor. It will never be
    * <code>null</code> after that.
    */
   private PSArchive m_archive;

   /**
    * The Manifest of the archive object. Initialized by the constructor. Never
    * <code>null</code> after that."
    */
   private PSArchiveManifest m_archiveMan;
   
   /**
    * Temporary directory set and used in {@link #addFiles(PSDependency, Iterator)}.
    */
   private static File ms_tmpDir;
   
   /**
    * Map of dependency file type to meaningful name.
    */
   private static Map<Integer, String> ms_depFileTypeMap = new HashMap<>();
   
   static
   {
      ms_depFileTypeMap.put(PSDependencyFile.TYPE_DBMS_SCHEMA, "schemaDef");
      ms_depFileTypeMap.put(PSDependencyFile.TYPE_ITEM_DEFINITION, "itemDef");
      ms_depFileTypeMap.put(PSDependencyFile.TYPE_NODE_DEFINITION, "nodeDef");
   }
}
