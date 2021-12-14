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

package com.percussion.deploy.server;

import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSArchive;
import com.percussion.deploy.objectstore.PSArchiveManifest;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSDeployComponentUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Handling saving and retrieving IDTypes and dependencies to and from
 * a <code>PSArchive</code> object.
 */
public class PSArchiveHandler
{

   /**
    * Constructing the handler from a <code>PSArchive</code> object.
    * The caller is responsible for calling the {@link #close()} method when
    * finished.
    *
    * @param archive The archive object file. It may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    * @throws PSDeployException If any other error occures.
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
    * @throws PSDeployException If any error occures.
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
    * @throws PSDeployException If any error occures while adding the files
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

      // Can only loop through the Iterator once, but it is needed twice here,
      // So let's build a list from it, which will be used later.
      List dupFiles = new ArrayList();

      // Adds to the archive first
      while (files.hasNext())
      {
         PSDependencyFile depFile = (PSDependencyFile) files.next();
         setArchiveLocation(depFile, dep, dupFiles);
         
         m_archive.storeFile(depFile.getFile(), getNormalizedArchivePath(
            depFile));
         dupFiles.add(depFile);
      }
      
      // then update the manifest
      m_archiveMan.addFiles(dep, dupFiles.iterator());
   }

   /**
    * Set the archive location for a given <code>PSDependencyFile</code> object
    * according to a given <code>PSDependency</code> and a list of
    * <code>PSDendencyFile</code> objects. The path of the archive location
    * should be unique among the given list of <code>PSDependencyFile</code>
    * objects.
    *
    * @param depFile The dependency file, which will be set, assume it is not
    * <code>null</code>.
    * @param dep The dependency object, assume it is not <code>null</code>.
    * @param depFiles A list of <code>PSDendencyFile</code> objects. Assume
    * it is not <code>null</code>, but may be empty.
    */
   private void setArchiveLocation(PSDependencyFile depFile, PSDependency dep,
      List depFiles)
   {
      File location = new File(dep.getKey(), depFile.getFile().getName());
      while ( ! hasUniquePath(location, depFiles.iterator()) )
      {
         String uniqueName = Long.toString(System.currentTimeMillis()) +
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
   private boolean hasUniquePath(File location, Iterator depFiles)
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
    * @throws PSDeployException if there are any errors.
    */
   public void addIdTypes(PSDependency dep, PSApplicationIDTypes idTypes)
      throws PSDeployException
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
    * archive and it contains the IDTypes, <code>null</code> otherwise.
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
   public void addDbmsInfoList(PSDependency dep, List dbmsInfoList)
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
    * @return the input stream, never <code>null</code>.  Caller is reponsible
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
    * @return size.  Caller is reponsible for closing the stream.
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
}
