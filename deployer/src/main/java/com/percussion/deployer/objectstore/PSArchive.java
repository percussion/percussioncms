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

package com.percussion.deployer.objectstore;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.util.PSArchiveFiles;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Class to use to create and manage an archive, store and retrieve files to and
 * from it, and extract manifest and version information.
 */
public class PSArchive
{

   /**
    * Construct this object from an existing archive file.  This will open the
    * archive for reading, and the caller is responsible for calling the
    * {@link #close()} method when finished.
    *
    * @param archive The local archive file to open.  May not be
    * <code>null</code> and must point to an existing valid archive file.
    *
    * @throws IllegalArgumentException If any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public PSArchive(File archive)
      throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (!archive.exists())
         throw new IllegalArgumentException("archive must exist");

      m_archiveFile = archive;
      m_writing = false;
      getArchiveInfo(true);
   }

   /**
    * Creates a new archive.  This will create the archive file and open it for
    * writing operations.  If <code>archiveFile</code> points to an existing
    * file, it will be overwritten.  The caller is responsible for calling the
    * {@link #close()} method when finished.
    *
    * @param archiveFile The archive file that will be created, may not be
    * <code>null</code>.
    * @param info The archive info desribing this archive, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public PSArchive(File archiveFile, PSArchiveInfo info)
      throws PSDeployException
   {
      if (archiveFile == null)
         throw new IllegalArgumentException("archive may not be null");

      if (info == null)
         throw new IllegalArgumentException("info may not be null");

      m_archiveFile = archiveFile;

      m_writing = true;
      storeArchiveInfo(info);
   }

   /**
    * @return <code>true</code> if the object is in writing mode, it can only
    * process writing operations; <code>false</code> if it is in read only mode.
    */
   public boolean isWriting()
   {
      return m_writing;
   }

   /**
    * @return <code>true</code> if the object has been closed;
    * <code>false</code> otherwise.
    */
   public boolean isClosed()
   {
      return m_closed;
   }

   /**
    * Returns a boolean indicating whether the encapsulated archive file is a
    * Rhythmyx sample application archive. Rhythmyx sample application archive
    * is installed under the Rhythmyx tree by the installer. This archive
    * contains sample applications such as RxArticle, RxBrief etc. The user
    * can install sample archive using MSM even if the server is not licensed
    * for MSM.
    *
    * @return <code>true</code> if the encapsulated archive file is a
    * Rhythmyx sample application archive, <code>false</code> otherwise.
    * Returns <code>false</code> if an <code>IOException</code> occurs
    * while obtaining the archive type.
    */
   public boolean isSampleArchive()
   {
      String sampleArchiveType =
         PSExportDescriptor.ARCHIVE_TYPE_ENUM[PSExportDescriptor.ARCHIVE_TYPE_SAMPLE];

      try
      {
         ZipFile zf = getZipFile();
         byte[] extra = PSArchiveFiles.getExtra(zf, ARCHIVE_INFO_PATH);
         if (extra != null)
         {
            String s = new String(extra);
            if (s.equalsIgnoreCase(sampleArchiveType))
             return true;
         }
      }
      catch (IOException e)
      {
      }
      return false;
   }


   /**
    * Gets the archive manifest from this archive if one has been stored.
    *
    * @return The manifest, may be <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public PSArchiveManifest getArchiveManifest() throws PSDeployException
   {
      try
      {
         if (m_archiveManifest == null && !m_writing && hasEntry(getZipFile(),
            ARCHIVE_MANIFEST_PATH))
         {
            Document doc = PSXmlDocumentBuilder.createXmlDocument(getFile(
                  ARCHIVE_MANIFEST_PATH), false);

            PSArchiveManifest man = new PSArchiveManifest();
            man.fromXml(doc.getDocumentElement());
            m_archiveManifest = man;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         Object args[] = {m_archiveFile.getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_READ_ERROR,
            args);
      }

      return m_archiveManifest;
   }

   /**
    * Stores the supplied archive manifest in this archive, replacing the
    * previous manifest if one has already been stored.
    *
    * @param manifest The manifest to store, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>manifest</code> is
    * <code>null</code>.
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for reading.
    * @throws PSDeployException if there are any errors.
    */
   public void storeArchiveManifest(PSArchiveManifest manifest)
      throws PSDeployException
   {
      if (manifest == null)
         throw new IllegalArgumentException("manifest may not be null");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      doc.appendChild(manifest.toXml(doc));
      storeXmlDocument(doc, ARCHIVE_MANIFEST_PATH, null);
      m_archiveManifest = manifest;
   }

   /**
    * Gets the archive info from this archive.  The archive info may optionally
    * include a <code>PSArchiveDetail</code> object.
    *
    * @param includeDetail If <code>true</code>, the returned archive info
    * object will include a <code>PSArchiveDetail</code> object if one has been
    * stored (see {@link PSArchiveInfo#getArchiveDetail() getArchiveDetail}.
    * If <code>false</code>, detail will not be included.
    *
    * @throws IllegalStateException if {@link #close()} has been called.
    * @throws PSDeployException if there are any errors.
    */
   public PSArchiveInfo getArchiveInfo(boolean includeDetail)
      throws PSDeployException
   {
      PSArchiveInfo result = null;

      // determine the archive ref from the file name when opened for reading
      String newArchiveRef = m_archiveFile.getName();
      int dotPos = newArchiveRef.lastIndexOf(".");
      if (dotPos != -1)
         newArchiveRef = newArchiveRef.substring(0, dotPos);

      try
      {
         if (m_archiveInfoDoc == null)
            m_archiveInfoDoc = PSXmlDocumentBuilder.createXmlDocument(getFile(
               ARCHIVE_INFO_PATH), false);

         Element infoRoot = m_archiveInfoDoc.getDocumentElement();

         if (includeDetail)
         {
            if (m_archiveInfoFull == null)
            {
               m_archiveInfoFull = new PSArchiveInfo(infoRoot);
               if (!m_writing)
               {
                  m_archiveInfoFull.setArchiveRef(newArchiveRef);
                  updateDbmsInfoList(m_archiveInfoFull.getArchiveDetail());
               }
            }
            result = m_archiveInfoFull;
         }
         else
         {
            if (m_archiveInfoNoDetail == null)
            {
               PSArchiveInfo archiveInfoNoDetail = new PSArchiveInfo(infoRoot);
               archiveInfoNoDetail.setArchiveDetail(null);
               m_archiveInfoNoDetail = archiveInfoNoDetail;
               if (!m_writing)
                  m_archiveInfoNoDetail.setArchiveRef(newArchiveRef);
            }

            result = m_archiveInfoNoDetail;
         }
      }
      catch (PSDeployException e)
      {
         throw e;
      }
      catch (Exception e) // this will get anything else
      {
         Object args[] = {m_archiveFile.getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_READ_ERROR,
            args);
      }


      return result;
   }

   /**
    * Stores the supplied archive info in this archive, replacing any archive
    * info already stored.
    *
    * @param info The info, may not be <code>null</code>.
    *
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for reading.
    * @throws PSDeployException if there are any errors.
    */
   public void storeArchiveInfo(PSArchiveInfo info) throws PSDeployException
   {
      if (info == null)
         throw new IllegalArgumentException("info may not be null");

      String archiveType =
         PSExportDescriptor.ARCHIVE_TYPE_ENUM[PSExportDescriptor.ARCHIVE_TYPE_NORMAL];

      PSArchiveDetail detail = info.getArchiveDetail();
      if (detail != null)
      {
         int type = detail.getExportDescriptor().getArchiveType();
         archiveType = PSExportDescriptor.ARCHIVE_TYPE_ENUM[type];
      }

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      doc.appendChild(info.toXml(doc));
      storeXmlDocument(doc, ARCHIVE_INFO_PATH, archiveType.getBytes());
      m_archiveInfoDoc = doc;
      m_archiveInfoFull = info;
      m_archiveInfoNoDetail = null;
   }

   /**
    * Updates the external dbms info list for each dependency in each package in
    * the supplied archive detail from this archive's manifest if one has been
    * stored.
    *
    * @param detail The detail to update, assumed not <code>null</code>.
    *
    * @throws PSDeployException if there are any errors
    */
   private void updateDbmsInfoList(PSArchiveDetail detail)
      throws PSDeployException
   {
      // archive manifest may not have been read yet
      if (getArchiveManifest() != null)
      {
         Iterator pkgs = detail.getPackages();
         while (pkgs.hasNext())
         {
            Set infoSet = new HashSet();
            PSDeployableElement pkg = (PSDeployableElement)pkgs.next();
            Iterator deps = pkg.getDependencies();
            if (deps != null)
            {
               while (deps.hasNext())
               {
                  PSDependency dep = (PSDependency)deps.next();
                  updateDbmsInfoList(dep, infoSet);
               }
            }
            detail.setDbmsInfoList(pkg, new ArrayList(infoSet));
         }
      }
   }

   /**
    * Recursive worker method for {@link #updateDbmsInfoList(PSArchiveDetail)}.
    *
    * @param dep The dependency for which the dbms info list in the supplied
    * <code>detail</code> is to be updated, assumed not <code>null</code>.
    * @param infoSet The set to which dbmsInfo objects are to be added, assumed
    * not <code>null</code>.
    */
   private void updateDbmsInfoList(PSDependency dep, Set infoSet)
   {
      // if hit a child that's a deployable element, can stop
      if (m_archiveManifest != null && !(dep instanceof PSDeployableElement))
      {
         // only need to check if dependency is included
         if (dep.isIncluded())
         {
            List infoList = m_archiveManifest.getDbmsInfoList(dep);
            if (infoList != null)
               infoSet.addAll(infoList);
         }

         // recurse children even if not included
         Iterator deps = dep.getDependencies();
         if (deps != null)
         {
            while (deps.hasNext())
            {
               PSDependency child = (PSDependency)deps.next();
               updateDbmsInfoList(child, infoSet);
            }
         }
      }
   }

   /**
    * Stores the supplied XML document in the archive.
    *
    * @param doc The document, assumed not <code>null</code>.
    * @param archivePath Where to store the doc, assumed not <code>null</code>
    * or empty.
    * @param extra the value to be set in the optional extra field data for
    * the entry corresponding to the specified file entry, may be
    * <code>null</code>
    *
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for reading.
    * @throws PSDeployException if there are any errors.
    *
    * @todo Add method to com.percussion.util.PSArchiveFiles class in later
    * version that allows adding archive entry from a stream and perhaps even a
    * document (cannot modify that class in 4.0 tree).
    */
   private void storeXmlDocument(Document doc, String archivePath, byte[] extra)
      throws PSDeployException
   {
      PSPurgableTempFile file = null;
      FileOutputStream out = null;
      try
      {
         file = new PSPurgableTempFile("dpl_", ".xml", null);
         out = new FileOutputStream(file);
         PSXmlDocumentBuilder.write(doc, out);
         out.close();
         out = null;
         storeFile(file, archivePath, extra);
      }
      catch (IOException e)
      {
         Object args[] = {m_archiveFile.getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_WRITE_ERROR,
            args);
      }
      finally
      {
         if (out != null)
            try {out.close();} catch (IOException ioe){}
         if (file != null)
            file.release();
      }
   }

   /**
    * Gets a reference to the archive file this object was constructed from.
    *
    * @return The file, never <code>null</code>.
    */
   public File getArchiveRef()
   {
      return m_archiveFile;
   }

   /**
    * Closes any archive resources currently maintained by this class and marks
    * this archive as closed.  Any subsequent method calls may throw exceptions,
    * and so the reference to the instance of this class should be discarded
    * after calling this method.
    */
   public void close()
   {
      if (m_closed)
         return;

      closeResources();

      m_closed = true;
   }

   /**
    * Stores the supplied file in the archive.
    *
    * Version of {@link #storeFile(File, archiveEntryPath)}
    * with an additional <code>extra</code> parameter described below.
    *
    * @param extra the value to be set in the optional extra field data for
    * the entry corresponding to the specified file entry, may be
    * <code>null</code>
    */
   public void storeFile(File file, String archiveEntryPath, byte[] extra)
      throws PSDeployException
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");

      if (!file.exists())
         throw new IllegalArgumentException("file must exist");

      if (archiveEntryPath == null || archiveEntryPath.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveEntryPath may not be null or empty");

      try
      {
         PSArchiveFiles.archiveFile(getZipOutputStream(), archiveEntryPath,
            file, extra);
      }
      catch (IOException e)
      {
         Object args[] = {m_archiveFile.getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_WRITE_ERROR,
            args);
      }
   }

   /**
    * Stores the supplied file in the archive.
    *
    * @param file The file to store.  May not be <code>null</code> and must be
    * an existing file.
    * @param archiveEntryPath The path to use when storing the file in the
    * archive.  May not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for reading.
    * @throws PSDeployException if the source file is not found or if there is
    * an error writing to the archive.
    */
   public void storeFile(File file, String archiveEntryPath)
      throws PSDeployException
   {
      storeFile(file, archiveEntryPath, null);
   }

   /**
    * Gets an input stream to the specified file in the archive.
    *
    * @param archiveEntryPath The path of the file in the archive.  May not be
    * <code>null</code> or empty.
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
   public InputStream getFile(String archiveEntryPath)
      throws PSDeployException
   {
      if (archiveEntryPath == null || archiveEntryPath.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveEntryPath may not be null or empty");

      try
      {
         return PSArchiveFiles.getFile(getZipFile(), archiveEntryPath);
      }
      catch (IOException e)
      {
         Object args[] = {m_archiveFile.getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_READ_ERROR,
            args);
      }

   }

   
   /**
    * Gets size of the specified file in the archive.
    *
    * @param archiveEntryPath The path of the file in the archive.  May not be
    * <code>null</code> or empty.
    *
    * @return size.  Caller is reponsible for closing the stream.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for writing.
    * @throws PSDeployException if an error occurs reading from the archive or
    * if file is not found in archive.
    */
   public int getFileSize(String archiveEntryPath)
      throws PSDeployException
   {
      if (archiveEntryPath == null || archiveEntryPath.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveEntryPath may not be null or empty");

      try
      {
         return PSArchiveFiles.getFileSize(getZipFile(), archiveEntryPath);
      }
      catch (IOException e)
      {
         Object args[] = {m_archiveFile.getPath(), e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.ARCHIVE_READ_ERROR,
            args);
      }

   }

   /**
    * Closes any archive resources currently maintained by this class.
    */
   private void closeResources()
   {
      if (m_zipFile != null)
         try {m_zipFile.close();} catch (IOException e){}

      if (m_zipOutputStream != null)
         try {m_zipOutputStream.close();} catch (IOException e){}
   }



   /**
    * Gets the archive for extracting files, first opening it if necessary.
    *
    * @return The zip file, never <code>null</code>.
    *
    * @throws IllegalStateException if {@link #close()} has been called, or if
    * the archive was opened for writing.
    * @throws IOException If there are any errors.
    */
   private ZipFile getZipFile() throws IOException
   {
      if (m_closed)
         throw new IllegalStateException("archive has been closed");

      if (m_writing)
         throw new IllegalStateException("archive has been opened for writing");

      if (m_zipFile == null)
      {
         m_zipFile = PSArchiveFiles.openArchive(m_archiveFile.getPath(),
            PSArchiveFiles.ZIP_FILE_TYPE);
      }

      return m_zipFile;
   }

   /**
    * Gets an output stream for writing to the archive.  If one has not been
    * opened yet, it will be opened, creating the archive.  Any previous archive
    * will be overwritten.
    *
    * @return The stream, never <code>null</code>.
    *
    * @throws IllegalStateException if {@link #close()} has been called or if
    * the archive has been opened for reading.
    * @throws IOException if there are any errors.
    */
   private ZipOutputStream getZipOutputStream() throws IOException
   {
      if (m_closed)
         throw new IllegalStateException("archive has been closed");

      if (!m_writing)
         throw new IllegalStateException("archive has been opened for reading");

      if (m_zipOutputStream == null)
      {
         m_zipOutputStream = PSArchiveFiles.createArchive(m_archiveFile,
            PSArchiveFiles.ZIP_FILE_TYPE, null);
      }

      return m_zipOutputStream;
   }

   /**
    * Determine if an enry for the supplied file ref exists in the archive.
    *
    * @parma zipFile The archive to check.  Assumed not <code>null</code>.
    * @param archiveEntryPath The path of the file in the archive.  Assumed not
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if an entry exists, <code>false</code>
    * otherwise.
    *
    * @todo Move this to com.percussion.util.PSArchiveFiles class in later
    * version (cannot modify that class in 4.0 tree).
    */
   private boolean hasEntry(ZipFile zipFile, String archiveEntryPath)
   {
      ZipEntry entry = zipFile.getEntry(archiveEntryPath);
      return (entry != null);
   }

   /**
    * The archive file this object represents.  Never <code>null</code> or
    * modified after ctor.
    */
   private File m_archiveFile;

   /**
    * The document restored from the archive containing the XML representation
    * of the archive's PSArchiveInfo object.  <code>null</code> until the first
    * call to {@link #getArchiveInfo(boolean)} or
    * {@link #storeArchiveInfo(PSArchiveInfo)}, never <code>null</code> after
    * that.
    */
   private Document m_archiveInfoDoc = null;

   /**
    * The full archive info object stored in this archive.  <code>null</code>
    * until the first call to {@link #getArchiveInfo(boolean)
    * getArchiveInfo(true)} or {@link #storeArchiveInfo(PSArchiveInfo)}, never
    * <code>null</code> after that.
    */
   private PSArchiveInfo m_archiveInfoFull = null;

   /**
    * The archive info object stored in this archive with its
    * <code>PSArchiveDetail</code> object set to <code>null</code>.
    * <code>null</code> until the first call to {@link #getArchiveInfo(boolean)
    * getArchiveInfo(false)}, set to <code>null</code> by a call to
    * {@link #storeArchiveInfo(PSArchiveInfo)}.
    */
   private PSArchiveInfo m_archiveInfoNoDetail = null;

   /**
    * The archive manifest stored in this archive if one has been stored.  May
    * be <code>null</code>.
    */
   private PSArchiveManifest m_archiveManifest = null;

   /**
    * Used to retrieve files from this archive, <code>null</code> until first
    * call to {@link #getZipFile()}, set to <code>null</code> by a call to
    * {@link #close()}.
    */
   private ZipFile m_zipFile = null;

   /**
    * Used to store files in this archive, <code>null</code> until first
    * call to {@link #getZipOutputStream()}, set to <code>null</code> by a call
    * to {@link #close()}.
    */
   private ZipOutputStream m_zipOutputStream = null;

   /**
    * Set to <code>true</code> by {@link #close()}, indicating this archive has
    * been closed and is no longer valid for use.  <code>false</code> otherwise.
    */
   private boolean m_closed = false;

   /**
    * Determines if archive has been opened for reading or writing.  Creating
    * a new archive will open it for writing, and instantiating this class with
    * an existing archive will open it for reading.  If <code>true</code>,
    * archive is opened for writing, if <code>false</code>, archive is opened
    * for reading.
    */
   private boolean m_writing = false;

   /**
    * Archive file ref for the <code>PSArchiveInfo</code> document.
    */
   public static final String ARCHIVE_INFO_PATH = "psx_archiveInfo.xml";

   /**
    * Archive file ref for the <code>PSArchiveManifest</code> document.
    */
   public static final String ARCHIVE_MANIFEST_PATH =
      "psx_archiveManifest.xml";

}
