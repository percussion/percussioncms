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
package com.percussion.design.objectstore.server;

import com.percussion.conn.PSServerException;
import com.percussion.data.IPSInternalRequestHandler;
import com.percussion.data.PSDatabaseMetaData;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.PSTableMetaData;
import com.percussion.data.jdbc.PSFileSystemDriver;
import com.percussion.data.vfs.IPSVirtualDirectory;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSCommandHandlerStylesheets;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSDatabaseComponentException;
import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSObjectFactory;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSTableSet;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.security.PSAclHandler;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.datasource.PSHibernateDialectConfig;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.util.IOTools;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSPathUtil;
import com.percussion.util.PSSortTool;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.servlet.PSServletUtils;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * This class provides server-only access to the XML object store. This class
 * follows the singleton pattern. Provides methods to get and set XML objects,
 * assuming that security has already been checked and locks have already been
 * obtained. The PSXmlObjectStoreHandler will delegate calls to this class.
 * Methods in this class should ensure that appropriate locks have already been
 * obtained where necessary. Only some functionality from the
 * PSXmlObjectStoreHandler has been extracted and moved to this class. In the
 * future more may be moved here as required.
 */
public class PSServerXmlObjectStore extends PSObjectFactory
{
   /**
    * Used to create and obtain the single instance of this class. This method
    * should only be called once (this should be done by the server when
    * initializing).
    * 
    * @param os The objectstore handler. May not be <code>null</code>.
    * @return The instance of this class.
    * @throws IllegalStateException if {@link
    * #createInstance(PSXmlObjectStoreHandler)} has already been called.
    */
   public static PSServerXmlObjectStore createInstance(
         PSXmlObjectStoreHandler os)
   {
      // make sure this hasn't already been created.
      if (ms_objectStore != null)
         throw new IllegalStateException(
               "PSServerXmlObjectStore already created.");

      ms_objectStore = new PSServerXmlObjectStore(os);

      return ms_objectStore;
   }

   /**
    * Used to obtain the single instance of this class. Class must have been
    * instantiated by a call to {@link #createInstance
    * (PSXmlObjectStoreHandler)} (this should have been done by the server when
    * initializing).
    * 
    * @return The instance of this class.
    * @throws IllegalStateException if {@link
    * #createInstance(PSXmlObjectStoreHandler)} has not already been called.
    */
   public static PSServerXmlObjectStore getInstance()
   {
      // make sure this has already been created
      if (ms_objectStore == null)
         throw new IllegalArgumentException(
               "PSServerXmlObjectStore not yet created.");

      return ms_objectStore;
   }

   /**
    * This class used to recover the application file and application directory
    * should saving application failed.
    * <p>
    * The original file or directory will be renamed to a temporary one (if
    * exists), which can be deleted, renamed or recovered (to the original one)
    * afterwards. The purpose of immediate renaming original file or directory
    * to a temporary one is to make sure the original and a new file or
    * directory can be coexisted in Windows (where the file name is case
    * insensitive).
    */
   public static class RecoverableFile
   {
      /**
       * The original file or directory, set by constructor, never
       * <code>null</code> after that.
       */
      private File m_src;

      /**
       * The file or directory that is renamed from {@link #m_src}. It may be
       * <code>null</code> if the original file or directory does not exist.
       */
      private File m_tempFile = null;

      /**
       * Creates an instance from the given source file or directory.
       * 
       * @param src the source file or directory, never <code>null</code>.
       */
      RecoverableFile(File src) throws PSException {
         if (src == null)
            throw new IllegalArgumentException("src may not be null.");

         m_src = src;
         renameSrcToBackup();
      }

      /**
       * Renames the original file or directory to a temporary file or directory
       * if the original file or directory exists; otherwise does nothing.
       */
      void renameSrcToBackup() throws PSException {
         if (!m_src.exists())
            return;

         Random rand = new Random();
         for (int i = 0; i < 100; i++)
         {
            File bkup = new File(m_src.getParentFile(), m_src.getName()
                  + "bkup" + rand.nextLong());
            if (!bkup.exists())
            {
               if (m_src.renameTo(bkup))
               {
                  m_tempFile = bkup;
                  return;
               }
            }
         }
         // should never be here. log error in case it does get here.
         String errorMsg = "failed to make backup for '"
               + m_src.getAbsolutePath() + "' file/directory.";
         ms_log.error(errorMsg);
         throw new PSException(errorMsg);
      }

      /**
       * Recovers the original file or directory if it has been renamed to a
       * different file or directory.
       * 
       * @return <code>true</code> if successful; otherwise return
       * <code>false</code> where the file may have recovered already or does
       * not exist anymore.
       */
      boolean recover()
      {
         if (m_tempFile == null)
            return false;

         try
         {
            deleteFile(m_src);
            boolean renamed = m_tempFile.renameTo(m_src);
            m_tempFile = null;
            return renamed;
         }
         catch (Exception e)
         {
            // ignore error if any
            ms_log.error("Failed to recover '" + m_src.getAbsolutePath()
                  + "' file", e);
         }
         return false;
      }

      /**
       * Renames the file or directory to the given destination.
       * 
       * @param dest the destination, assumed not <code>null</code>.
       * @return <code>true</code> if and only if the renaming succeeded;
       * <code>false</code> otherwise
       */
      boolean renameTo(File dest)
      {
         if (m_tempFile == null)
            return true;

         deleteFile(dest);
         if (m_tempFile.renameTo(dest))
         {
            m_tempFile = dest;
            return true;
         }
         return false; // failed to rename
      }

      /**
       * Deletes the file or directory if exists.
       * 
       * @return <code>true</code> if successful; otherwise return
       * <code>false</code> where the file may have deleted already or does
       * not exist anymore.
       */
      boolean delete()
      {
         if (m_tempFile != null)
         {
            boolean deleted = deleteFile(m_tempFile);
            m_tempFile = null;
            return deleted;
         }
         return false;
      }
   }

   /**
    * Creates an empty application object with an acl containing default and
    * anonymous entries.
    * 
    * @return The application object.
    */
   public PSApplication createEmptyApplication()
   {
      return PSObjectFactory.createApplication(true);
   }

   /**
    * Same as
    * {@link #getApplicationObject(IPSLockerId, String, PSSecurityToken, boolean)}
    * with last parameter <code>true</code>.
    */
   public PSApplication getApplicationObject(String appName,
         PSSecurityToken tok)
      throws PSServerException, PSNotFoundException,
      PSAuthenticationRequiredException, PSAuthorizationException
   {
      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return getApplicationObject(appName, tok, true);
   }

   /**
    * Loads application from disk in read-only mode and returns it as an object.
    * Application is not locked as a result of this call.
    * 
    * @param appName The name of the application. May not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * @param fixupCeFields flag to indicate the content editor field fixup is
    * required. <code>true</code> to fixup <code>false</code> otherwise. See
    * {@link #fixupFields(PSFieldSet, Iterator, Map)}.
    * @return The application object.
    * @throws PSNotFoundException if an application be that name does not exist.
    * @throws PSAuthorizationException if the user is not allowed access to the
    * application.
    * @throws PSAuthenticationRequiredException if we have not tried to
    * authenticate this user.
    * @throws PSServerException for any other errors encountered.
    */
   public PSApplication getApplicationObject(String appName,
         PSSecurityToken tok, boolean fixupCeFields)
      throws PSServerException, PSNotFoundException,
      PSAuthenticationRequiredException, PSAuthorizationException
   {
      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      PSApplication app = loadApplicationObject(appName, fixupCeFields);

      // make sure they can read it
      tok.setResourceType("read application");
      tok.setResourceName(app.getName());
      checkCanReadApplication(app, tok, false);

      return app;
   }

   /**
    * Same as
    * {@link #getApplicationObject(IPSLockerId, String, PSSecurityToken, boolean)}
    * with last parameter <code>true</code>.
    */
   public PSApplication getApplicationObject(IPSLockerId lockId,
         String appName, PSSecurityToken tok)
      throws PSServerException, PSNotFoundException, PSLockedException,
      PSNotLockedException, PSAuthenticationRequiredException,
      PSAuthorizationException
   {
      // validate params
      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return getApplicationObject(lockId, appName, tok, true);
   }

   /**
    * Locks and loads application from disk in edit-mode and returns it as an
    * object.
    * 
    * @param lockId The lock id that identifies the user. App must already be
    * locked with the given id. Lock will be extended.
    * @param appName The name of the application. May not be <code>null</code>.
    * @return The application object.
    * @param tok The user's security token. May not be <code>null</code>.
    * @param fixupCeFields flag to indicate the content editor field fixup is
    * required. <code>true</code> to fixup <code>false</code> otherwise. See
    * {@link #fixupFields(PSFieldSet, Iterator, Map)}.
    * @throws PSNotFoundException if an application be that name does not exist.
    * @throws PSNotLockedException if the app is not already locked by the id.
    * @throws PSLockedException if there is a problem extending the lock.
    * @throws PSAuthorizationException if the user is not allowed access to the
    * application.
    * @throws PSAuthenticationRequiredException if we have not tried to
    * authenticate this user.
    * @throws PSServerException for any other errors encountered.
    */
   public PSApplication getApplicationObject(IPSLockerId lockId,
         String appName, PSSecurityToken tok, boolean fixupCeFields)
      throws PSServerException, PSNotFoundException, PSLockedException,
      PSNotLockedException, PSAuthenticationRequiredException,
      PSAuthorizationException
   {
      // validate params
      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // Check to see if app is locked
      if (!isApplicationLocked(lockId, appName))
      {
         Object[] args = { appName };
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               args);
      }
      else
      {
         // extend the lock to be sure we keep it
         getApplicationLock(lockId, appName, 30);
      }

      // get the object
      PSApplication app = getApplicationObject(appName, tok, fixupCeFields);

      // make sure user can edit it
      tok.setResourceType("edit application");
      tok.setResourceName(app.getName());
      checkCanReadApplication(app, tok, true);

      return app;
   }

   /**
    * Loads application from disk and returns it as an XML Doc. Application is
    * not locked as a result of this call.
    * 
    * @param appName The name of the application. May not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * @return The application XML Doc.
    * @throws PSNotFoundException if an application be that name does not exist.
    * @throws PSAuthorizationException if the user is not allowed access to the
    * application.
    * @throws PSAuthenticationRequiredException if we have not tried to
    * authenticate this user.
    * @throws PSServerException for any other errors encountered.
    */
   public Document getApplicationDoc(String appName, PSSecurityToken tok)
      throws PSServerException, PSNotFoundException,
      PSAuthenticationRequiredException, PSAuthorizationException
   {
      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // get the doc - this will validate security for us as well
      PSApplication app = getApplicationObject(appName, tok);
      Document appDoc = app.toXml();

      return appDoc;
   }

   /**
    * Locks and loads application from disk and returns it as an Xml Doc.
    * 
    * @param lockId The lock id that identifies the user. App must already be
    * locked with the given id. Lock will be extended.
    * @param appName The name of the application. May not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * @return The application XML document.
    * @throws PSNotFoundException if an application be that name does not exist.
    * @throws PSNotLockedException if the app is not already locked by the id.
    * @throws PSLockedException if there is a problem extending the lock.
    * @throws PSAuthorizationException if the user is not allowed access to the
    * application.
    * @throws PSAuthenticationRequiredException if we have not tried to
    * authenticate this user.
    * @throws PSServerException for any other errors encountered.
    */
   public Document getApplicationDoc(IPSLockerId lockId, String appName,
         PSSecurityToken tok)
      throws PSServerException, PSNotFoundException, PSLockedException,
      PSNotLockedException, PSAuthenticationRequiredException,
      PSAuthorizationException
   {
      // validate params
      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // get the object - this will validate lock and security for us
      PSApplication app = getApplicationObject(lockId, appName, tok);
      Document appDoc = app.toXml();

      return appDoc;
   }

   /**
    * Gets a list of files located below an application root.
    * 
    * @param appRoot The name of the application root directory, may be
    * <code>null</code> or empty. I <code>null</code> or empty then the
    * server's application root will be used.
    * 
    * @param includeDirs flag indicating if directories should be included in
    * the returned list.
    * 
    * @param recurse flag indicating that the method should recurse into all the
    * directories.
    * 
    * @return An Iterator over <code>0</code> or more File objects, never
    * <code>null</code>. Each has a path relative to the specified
    * application root.
    * 
    * @throws PSServerException if there are any errors.
    */
   public Iterator<File> getAppRootFileList(String appRoot,
         boolean includeDirs, boolean recurse) throws PSServerException
   {
      List<File> newFileList = new ArrayList<File>();

      File appDir = getAppRootDir(appRoot);
      if (appDir.exists() && appDir.isDirectory())
      {
         List<File> fileList = catalogFiles(appRoot, includeDirs, recurse);
         // need to remove top level app directory
         for (File file : fileList)
         {
            String tmpPath = file.getPath();
            int sepPos = tmpPath.indexOf(File.separator);
            if (sepPos > -1)
               tmpPath = tmpPath.substring(sepPos + 1, tmpPath.length());
            newFileList.add(new File(tmpPath));
         }
      }
      return newFileList.iterator();
   }

   /**
    * Gets the application's files and returns a list of File objects relative
    * to the app root.
    * 
    * @param appName The name of the application. May not be <code>null</code>
    * or empty.
    * 
    * @return An Iterator over <code>0</code> or more File objects, never
    * <code>null</code>.
    * 
    * @throws PSServerException for any other errors encountered.
    */
   @SuppressWarnings("unchecked")
   public Iterator getApplicationFiles(String appName)
      throws PSServerException
   {
      // validate inputs
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      Iterator result = null;
      // get request root
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums
            .getSummary(appName);
      if (sum != null)
      {
         String requestRoot = sum.getAppRoot();
         result = getAppRootFileList(requestRoot, false, true);
      }

      if (result == null)
         result = PSIteratorUtils.emptyIterator();

      return result;
   }

   /**
    * Opens and returns an input stream to the specified file below the
    * application directory of the specified app.
    * 
    * @param appName The name of the application whose directory contains the
    * specified file. May not be <code>null</code> or empty.
    * @param appFile The file to retrieve, relative from the application root
    * directory. May not be <code>null</code> and must exist.
    * @param tok The security token to use for authorization, may not be
    * <code>null</code>.
    * 
    * @return An <code>InputStream</code> to the specified file. Caller is
    * responsible for closing the stream when finished. Never <code>null</code>.
    * 
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated
    * @throws PSAuthorizationException If user does not have design read access
    * to the specified application
    * @throws PSNotFoundException If the specified application or file cannot be
    * found.
    * @throws PSServerException If any other errors occur.
    */
   public InputStream getApplicationFile(String appName, File appFile,
         PSSecurityToken tok)
      throws PSAuthorizationException, PSNotFoundException, PSServerException
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanReadApplication(appName, tok, false);

      // we assume the request root is the application directory
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums
            .getSummary(appName);

      if (sum == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_ROOT_REQD,
               appName);

      String appRoot = sum.getAppRoot();
      File appDir = getAppRootDir(appRoot);

      // if the application dir does not exist, or it's not a directory,
      // then error
      if (!(appDir.exists() && appDir.isDirectory()))
      {
         Object[] args = { appName, appDir.getPath() };
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_DIR_NOT_FOUND,
               args);
      }

      File appFileName = new File(appDir, appFile.getPath());
      InputStream in = null;
      try
      {
         in = m_objectStoreHandler.lockInputStream(appFileName);
         ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
         IOTools.copyStream(in, tmpOut);
         ByteArrayInputStream bIn = new ByteArrayInputStream(tmpOut
               .toByteArray());

         return bIn;
      }
      catch (FileNotFoundException e)
      {
         Object[] args = { appName, appFileName.getPath() };
         throw new PSNotFoundException(
               IPSObjectStoreErrors.APP_FILE_NOT_FOUND, args);

      }
      catch (IOException e)
      {
         Object[] args = new Object[] { appFileName.toString(), e.toString() };
         throw new PSServerException(IPSObjectStoreErrors.APP_FILE_IO_ERROR,
               args);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               m_objectStoreHandler.releaseInputStream(in, appFileName);
            }
            catch (IOException e)
            {
               Object[] args = new Object[] { appFileName.toString(),
                     e.toString() };
               throw new PSServerException(
                     IPSObjectStoreErrors.APP_FILE_IO_ERROR, args);
            }
         }
      }
   }

   /**
    * Saves the application file or folder if the correct permissions exist.
    * 
    * @param appName The name of the application whose directory contains the
    * specified file. May not be <code>null</code> or empty.
    * @param appFile The file to save, relative from the application root
    * directory. May not be <code>null</code> and may or may not already
    * exist.
    * @param in The input stream to the file data. May not be <code>null</code>.
    * This method will close the stream when finished.
    * @param overWrite If <code>true</code> and file already exists, it will
    * be overwritten. If <code>false</code> and the file already exists, no
    * action will be taken.
    * @param lockId The id used to lock the app. It must already be locked with
    * this id.
    * @param tok The security token to use for authorization, may not be
    * <code>null</code>.
    * 
    * @return <code>true</code> if the file already existed,
    * <code>false</code> otherwise.
    * 
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated
    * @throws PSAuthorizationException If user does not have design read access
    * to the specified application
    * @throws PSNotFoundException If the specified application or file cannot be
    * found.
    * @throws PSNotLockedException If the application is not already locked.
    * @throws PSNotLockedException If the application lock cannot be extended.
    * @throws PSServerException If any other errors occur.
    */
   public boolean saveApplicationFile(String appName, File appFile,
         InputStream in, boolean overWrite, IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSAuthorizationException, PSNotFoundException,
      PSNotLockedException, PSLockedException, PSServerException
   {
      return saveApplicationFile(appName, appFile, in, overWrite, lockId, tok,
            false);
   }

   /**
    * Saves the application file or folder if the correct permissions exist.
    * 
    * @param appName The name of the application whose directory contains the
    * specified file. May not be <code>null</code> or empty.
    * @param appFile The file to save, relative from the application root
    * directory. May not be <code>null</code> and may or may not already
    * exist.
    * @param in The input stream to the file data. May not be <code>null</code>
    * unless this is a folder. This method will close the stream when finished.
    * @param overWrite If <code>true</code> and file already exists, it will
    * be overwritten. If <code>false</code> and the file already exists, no
    * action will be taken.
    * @param lockId The id used to lock the app. It must already be locked with
    * this id.
    * @param tok The security token to use for authorization, may not be
    * <code>null</code>.
    * @param isFolder flag indicating that this app file is a folder
    * 
    * @return <code>true</code> if the file already existed,
    * <code>false</code> otherwise.
    * 
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated
    * @throws PSAuthorizationException If user does not have design read access
    * to the specified application
    * @throws PSNotFoundException If the specified application or file cannot be
    * found.
    * @throws PSNotLockedException If the application is not already locked.
    * @throws PSNotLockedException If the application lock cannot be extended.
    * @throws PSServerException If any other errors occur.
    */
   public boolean saveApplicationFile(String appName, File appFile,
         InputStream in, boolean overWrite, IPSLockerId lockId,
         PSSecurityToken tok, boolean isFolder)
      throws PSAuthorizationException, PSNotFoundException,
      PSNotLockedException, PSLockedException, PSServerException
   {
      
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanReadApplication(appName, tok, true);

      // shouldn't be able to save files to apps that aren't locked
      if (!isApplicationLocked(lockId, appName))
      {
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               appName);
      }
      else
      {
         // extend the lock to be sure we keep it
         getApplicationLock(lockId, appName, 30);
      }

      // we assume the request root is the application directory
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums
            .getSummary(appName);

      if (sum == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_ROOT_REQD,
               appName);

      String appRoot = sum.getAppRoot();
      File appDir = getAppRootDir(appRoot);
      String path = PSPathUtil.getNormalizedPath(appFile.getPath());

      // If this app file is flagged as a folder then just check for existance
      // and create the directory if it does not exist.
      if (isFolder)
      {
         File appFolder = new File(appDir, path);
         if (appFolder.exists() && appDir.isDirectory())
         {
            return true;
         }
         else
         {
            appFolder.mkdirs();
            return false;
         }
      }

      // if the application dir does not exist, or it's not a directory,
      // then error
      if ((!appDir.exists() && !appDir.mkdir()) || !appDir.isDirectory())
      {
         Object[] args = { appName, appDir.getPath() };
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_DIR_NOT_FOUND,
               args);
      }

      File appFileName = new File(appDir, path);
      boolean exists = false;
      if (appFileName.exists())
      {
         if (!appFileName.isFile())
         {
            Object[] args = { appName, appFileName.getPath() };
            throw new PSNotFoundException(
                  IPSObjectStoreErrors.APP_FILE_NOT_FOUND, args);
         }
         exists = true;
      }
      else
      {
         // the file could be in a subdirectory under the application
         // directory, so create all necessary subdirectories
         File parentDir = appFileName.getParentFile();
         if (parentDir != null)
         {
            if ((!parentDir.exists() && !parentDir.mkdirs())
                  || !parentDir.isDirectory())
            {
               Object[] args = { appName, appFileName.getPath() };
               throw new PSServerException(
                     IPSObjectStoreErrors.APP_FILE_MKSUBDIR_ERROR, args);
            }
         }
      }

      OutputStream out = null;
      boolean isSaved = false;
      try
      {
         if (!exists || (exists && overWrite))
         {
            out = m_objectStoreHandler.lockOutputStream(appFileName);
            IOTools.copyStream(in, out);
            isSaved = true;
         }

      }
      catch (IOException e)
      {
         Object[] args = new Object[] { appFileName.toString(), e.toString() };
         throw new PSServerException(IPSObjectStoreErrors.APP_FILE_IO_ERROR,
               args);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               m_objectStoreHandler.releaseOutputStream(out, appFileName);
            }
            catch (IOException e)
            {
               Object[] args = { appFileName.toString(), e.toString() };
               throw new PSServerException(
                     IPSObjectStoreErrors.APP_FILE_IO_ERROR, args);
            }
         }

         try
         {
            in.close();
         }
         catch (IOException e)
         {
         }

      }

      if (isSaved)
      {
         // notify the objects that care about file changes
         PSNotificationHelper.notifyFile(appFileName);
      }

      return exists;
   }
   
   /**
    * Saves the application file or folder if the correct permissions exist.
    * 
    * @param appName The name of the application whose directory contains the
    * specified file. May not be <code>null</code> or empty.
    * @param appFile The file to save, relative from the application root
    * directory. May not be <code>null</code> and may or may not already
    * exist.
    * @param in The input stream to the file data. May not be <code>null</code>
    * unless this is a folder. This method will close the stream when finished.
    * @param overWrite If <code>true</code> and file already exists, it will
    * be overwritten. If <code>false</code> and the file already exists, no
    * action will be taken.
    * @param tok The security token to use for authorization, may not be
    * <code>null</code>.
    * @param isFolder flag indicating that this app file is a folder
    * 
    * @return <code>true</code> if the file already existed,
    * <code>false</code> otherwise.
    * 
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated
    * @throws PSAuthorizationException If user does not have design read access
    * to the specified application
    * @throws PSNotFoundException If the specified application or file cannot be
    * found.
    * @throws PSNotLockedException If the application is not already locked.
    * @throws PSNotLockedException If the application lock cannot be extended.
    * @throws PSServerException If any other errors occur.
    */
   public boolean saveApplicationFileWithoutLocking(String appName, File appFile,
         InputStream in, boolean overWrite,
         PSSecurityToken tok, boolean isFolder)
      throws PSAuthorizationException, PSNotFoundException,
      PSNotLockedException, PSLockedException, PSServerException
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanReadApplication(appName, tok, true);

      // we assume the request root is the application directory
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums
            .getSummary(appName);

      if (sum == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_ROOT_REQD,
               appName);

      String appRoot = sum.getAppRoot();
      File appDir = getAppRootDir(appRoot);
      String path = PSPathUtil.getNormalizedPath(appFile.getPath());

      // If this app file is flagged as a folder then just check for existance
      // and create the directory if it does not exist.
      if (isFolder)
      {
         File appFolder = new File(appDir, path);
         if (appFolder.exists() && appDir.isDirectory())
         {
            return true;
         }
         else
         {
            appFolder.mkdirs();
            return false;
         }
      }

      // if the application dir does not exist, or it's not a directory,
      // then error
      if ((!appDir.exists() && !appDir.mkdir()) || !appDir.isDirectory())
      {
         Object[] args = { appName, appDir.getPath() };
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_DIR_NOT_FOUND,
               args);
      }

      File appFileName = new File(appDir, path);
      boolean exists = false;
      if (appFileName.exists())
      {
         if (!appFileName.isFile())
         {
            Object[] args = { appName, appFileName.getPath() };
            throw new PSNotFoundException(
                  IPSObjectStoreErrors.APP_FILE_NOT_FOUND, args);
         }
         exists = true;
      }
      else
      {
         // the file could be in a subdirectory under the application
         // directory, so create all necessary subdirectories
         File parentDir = appFileName.getParentFile();
         if (parentDir != null)
         {
            if ((!parentDir.exists() && !parentDir.mkdirs())
                  || !parentDir.isDirectory())
            {
               Object[] args = { appName, appFileName.getPath() };
               throw new PSServerException(
                     IPSObjectStoreErrors.APP_FILE_MKSUBDIR_ERROR, args);
            }
         }
      }

      OutputStream out = null;
      boolean isSaved = false;
      try
      {
         if (!exists || (exists && overWrite))
         {
            out = m_objectStoreHandler.lockOutputStream(appFileName);
            IOTools.copyStream(in, out);
            isSaved = true;
         }

      }
      catch (IOException e)
      {
         Object[] args = new Object[] { appFileName.toString(), e.toString() };
         throw new PSServerException(IPSObjectStoreErrors.APP_FILE_IO_ERROR,
               args);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               m_objectStoreHandler.releaseOutputStream(out, appFileName);
            }
            catch (IOException e)
            {
               Object[] args = { appFileName.toString(), e.toString() };
               throw new PSServerException(
                     IPSObjectStoreErrors.APP_FILE_IO_ERROR, args);
            }
         }

         try
         {
            in.close();
         }
         catch (IOException e)
         {
         }

      }

      if (isSaved)
      {
         // notify the objects that care about file changes
         PSNotificationHelper.notifyFile(appFileName);
      }

      return exists;
   }

   /**
    * Deletes a file or directory If it is a directory then it recursively
    * deletes the directory and all of its children.
    * 
    * @param file the file or directory to be deleted, assumed not
    * <code>null</code>.
    * @return <code>true</code> if successful.
    */
   static private boolean deleteFile(File file)
   {
      if (!file.exists())
         return false;
      if (file.isDirectory())
      {
         File[] children = file.listFiles();
         for (int i = 0; i < children.length; i++)
         {
            if (!deleteFile(children[i]))
               return false;
         }
      }
      return file.delete();
   }

   /**
    * Removes the specified application file from the application directory.
    * This application has to be locked prior to execution
    * 
    * @param app The application, whose directory contains the specified file.
    * May not be <code>null</code> or empty.
    * @param appFile The file to save, actual path. May not be <code>null</code>.
    * @param lockId The id used to lock the app. It must already be locked with
    * this id.
    * @param tok The security token to use for authorization, may not be
    * <code>null</code>.
    * 
    * @return <code>true</code> if the file has been deleted,
    * <code>false</code> otherwise.
    * 
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated
    * @throws PSAuthorizationException If user does not have design read access
    * to the specified application
    * @throws PSNotFoundException If the specified application or file cannot be
    * found.
    * @throws PSNotLockedException If the application is not already locked.
    * @throws PSNotLockedException If the application lock cannot be extended.
    * @throws PSServerException If any other errors occur.
    */
   public boolean removeApplicationFile(PSApplication app, File appFile,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSAuthorizationException, PSNotFoundException,
      PSNotLockedException, PSLockedException, PSServerException
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null or empty");

      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanSaveApplication(app, tok);

      // shouldn't be able to save files to apps that aren't locked
      if (!isApplicationLocked(lockId, app.getName()))
      {
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               app.getName());
      }
      else
      {
         // extend the lock to be sure we keep it
         getApplicationLock(lockId, app.getName(), 30);
      }

      // we assume the request root is the application directory
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums.getSummary(app
            .getName());

      if (sum == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_ROOT_REQD, app
               .getName());

      String appRoot = sum.getAppRoot();
      File appDir = new File(appRoot);

      // if the application dir does not exist, or it's not a directory,
      // then error
      if ((!appDir.exists() && !appDir.mkdir()) || !appDir.isDirectory())
      {
         Object[] args = { app.getName(), appDir.getPath() };
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_DIR_NOT_FOUND,
               args);
      }

      // delete the app file if it exists
      if (!appFile.exists() || !appFile.isFile())
      {
         Object[] args = { app.getName(), appFile.getPath() };
         throw new PSNotFoundException(
               IPSObjectStoreErrors.APP_FILE_NOT_FOUND, args);
      }

      return deleteFile(appFile);
   }
   
   /**
    * Removes the specified application file from the application directory.
    * 
    * @param app The application, whose directory contains the specified file.
    * May not be <code>null</code> or empty.
    * @param appFile The file to save, actual path. May not be <code>null</code>.
    * @param lockId The id used to lock the app. It must already be locked with
    * this id.
    * @param tok The security token to use for authorization, may not be
    * <code>null</code>.
    * @param dontLockApp This flag decides if the app needs to be locked
    * 
    * @return <code>true</code> if the file has been deleted,
    * <code>false</code> otherwise.
    * 
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated
    * @throws PSAuthorizationException If user does not have design read access
    * to the specified application
    * @throws PSNotFoundException If the specified application or file cannot be
    * found.
    * @throws PSNotLockedException If the application is not already locked.
    * @throws PSNotLockedException If the application lock cannot be extended.
    * @throws PSServerException If any other errors occur.
    */
   public boolean removeApplicationFile(PSApplication app, File appFile,
         IPSLockerId lockId, PSSecurityToken tok, boolean dontLockApp)
      throws PSAuthorizationException, PSNotFoundException,
      PSNotLockedException, PSLockedException, PSServerException
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null or empty");

      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanSaveApplication(app, tok);

      if(!dontLockApp) {
         // shouldn't be able to save files to apps that aren't locked
         if (!isApplicationLocked(lockId, app.getName()))
         {
            throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
                  app.getName());
         }
         else
         {
            // extend the lock to be sure we keep it
            getApplicationLock(lockId, app.getName(), 30);
         }
      }

      // we assume the request root is the application directory
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums.getSummary(app
            .getName());

      if (sum == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_ROOT_REQD, app
               .getName());

      String appRoot = sum.getAppRoot();
      File appDir = new File(appRoot);

      // if the application dir does not exist, or it's not a directory,
      // then error
      if ((!appDir.exists() && !appDir.mkdir()) || !appDir.isDirectory())
      {
         Object[] args = { app.getName(), appDir.getPath() };
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_DIR_NOT_FOUND,
               args);
      }

      // delete the app file if it exists
      if (!appFile.exists() || !appFile.isFile())
      {
         Object[] args = { app.getName(), appFile.getPath() };
         throw new PSNotFoundException(
               IPSObjectStoreErrors.APP_FILE_NOT_FOUND, args);
      }

      return deleteFile(appFile);
   }

   /**
    * Catalogs all files below the specified directory.
    * 
    * @param dirName The directory, assumed not <code>null</code> or empty,
    * must be a sub-directory relative to the Rhythmyx root directory.
    * 
    * @param includeDirs flag indicating if directories should be included in
    * the returned list.
    * 
    * @param recurse flag indicating that the method should recurse into all the
    * directories.
    * 
    * @return An iterator over zero or more <code>File</code>, never
    * <code>null</code>. File paths will include the supplied directory.
    * 
    * @throws PSServerException if there are any errors.
    */
   private List<File> catalogFiles(String dirName, boolean includeDirs,
         boolean recurse) throws PSServerException
   {
      List<File> retFiles = new ArrayList<File>();
      List<File> dirFiles = new ArrayList<File>();

      File dir = getAppRootDir(dirName);
      if (dir.isDirectory())
      {
         File[] files = dir.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            File file = new File(dirName, files[i].getName());
            if (files[i].isFile())
               retFiles.add(file);
            else
               dirFiles.add(file);
         }
      }

      if (recurse && !dirFiles.isEmpty())
      {
         for (File subDir : dirFiles)
         {
            retFiles.addAll(catalogFiles(subDir.getPath(), includeDirs,
                  recurse));
         }
      }
      if (includeDirs)
         retFiles.addAll(dirFiles);
      return retFiles;
   }

   /**
    * Saves the application file to disk, updating the app if it already exists.
    * App must already be locked. Lock will be extended once it has been
    * determined that the lock exists.
    * 
    * @param appDoc The application Xml doc. May not be <code>null</code>.
    * @param lockId The id used to lock the app. It must already be locked with
    * this id.
    * @param tok The user's security token. May not be <code>null</code>.
    * @param validate If <code>true</code>, runtime validation will be
    * performed.
    * @throws PSSystemValidationException If app fails validation
    * @throws PSUnknownDocTypeException If the app exists on disk already and
    * that file has an invalid format.
    * @throws PSUnknownNodeTypeException If the app does not have a valid name
    * @throws IOException If there is an error reading or writing the app to or
    * from disk.
    * @throws PSNotLockedException If the app is not already locked by the
    * supplied Id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSNonUniqueException if the requestroot is already in use by a
    * different app.
    * @throws PSNotFoundException if app is cannot be located on restart.
    * @throws PSServerException for any other errors encountered.
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated.
    * @throws PSAuthorizationException if user is not allowed to perform this
    * operation.
    */
   public void saveApplication(Document appDoc, IPSLockerId lockId,
         PSSecurityToken tok, boolean validate)
      throws PSServerException, PSNotLockedException, PSSystemValidationException,
      PSUnknownDocTypeException, PSUnknownNodeTypeException, IOException,
      PSLockedException, PSNonUniqueException, PSAuthorizationException,
      PSAuthenticationRequiredException, PSNotFoundException
   {
      // validate inputs
      if (appDoc == null)
         throw new IllegalArgumentException("appDoc may not be null");

      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // save it
      PSApplication app = new PSApplication(appDoc);
      saveApplication(app, lockId, tok, validate);

   }

   /**
    * Calls {@link #deleteApplication(int, IPSLockerId, PSSecurityToken)} after
    * conveting the supplied application name to an id.
    * 
    * @param appName The name of the app, may not be <code>null</code> or
    * empty.
    */
   public boolean deleteApplication(String appName, IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSNotFoundException, PSServerException, PSNotLockedException,
      PSLockedException
   {
      if (StringUtils.isBlank(appName))
         throw new IllegalArgumentException("appName may not be null or empty");

      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      boolean deleted = false;
      int appId = m_objectStoreHandler.getApplicationIdFromName(appName);
      if (appId != 0)
         deleted = deleteApplication(appId, lockId, tok);

      return deleted;
   }

   /**
    * Deletes the specified application, its files, and notifies any listeners.
    * Silently returns if the specified app does not exist.
    * 
    * @param appId The app id.
    * @param lockId The lock id to use, may not be <code>null</code>.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the application was deleted,
    * <code>false</code> if not.
    * 
    * @throws PSAuthenticationRequiredException If the session represented by
    * the token has not been authenticated.
    * @throws PSAuthorizationException If the user represented by the session is
    * not authorized to perform this operation.
    * @throws PSNotFoundException If the application file specified by the id
    * cannot be found.
    * @throws PSNotLockedException If the use does not have the application
    * locked.
    * @throws PSLockedException If someone else has the application locked.
    * @throws PSServerException If any other errors occur.
    */
   public boolean deleteApplication(int appId, IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSNotFoundException, PSServerException, PSNotLockedException,
      PSLockedException
   {
      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      String appName = m_objectStoreHandler.getApplicationNameFromId(appId);
      if (appName == null)
         return false;

      // check security
      tok.setResourceType("delete application");
      tok.setResourceName(appName);
      PSApplication app = loadApplicationObject(appName, false);
      checkCanDeleteApplication(app, tok);

      File appFile = m_objectStoreHandler.getApplicationFile(appName);

      /*
       * Check to see if app is locked - be sure it's the current app in case of
       * a rename.
       */
      if (!isApplicationLocked(lockId, appName))
      {
         Object[] args = { appName };
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               args);
      }
      else
      {
         // extend the lock to be sure we keep it
         getApplicationLock(lockId, appName, 30);
      }

      PSApplicationSummaryCollection appSums = m_objectStoreHandler.m_appSums;

      appSums.removeSummary(appId);
      appFile.delete();
      IPSVirtualDirectory vDir = PSFileSystemDriver
            .removeVirtualDirectory(appName);
      if (vDir != null)
      {
         File rootDir = vDir.getPhysicalLocation();
         if (rootDir != null && rootDir.isDirectory())
         {
            deleteDirectory(rootDir);
         }
      }

      // notify any listeners
      try
      {
         m_objectStoreHandler.notifyApplicationListeners(app, false, true);
      }
      catch (PSSystemValidationException e)
      {
         // ignore, should never happen or matter if it does happen
      }

      return true;
   }

   /**
    * Recursively deletes all contents of a directory and then the directory
    * itself.
    * 
    * @param dir The directory. Must be a directory, assumed not
    * <code>null</code> and to be a directory.
    */
   private void deleteDirectory(File dir)
   {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         if (files[i].isDirectory())
            deleteDirectory(files[i]);
         else
            files[i].delete();
      }

      dir.delete();
   }

   /**
    * Saves the application object to disk, updating the app if it already
    * exists. App must already be locked. Lock will be extended once it has been
    * determined that the lock exists.
    * 
    * @param app The application object. May not be <code>null</code>.
    * @param lockId The id used to lock the app. It must already be locked with
    * this id.
    * @param tok The user's security token. May not be <code>null</code>.
    * @param validate If <code>true</code>, runtime validation will be
    * performed.
    * @throws PSSystemValidationException If app fails validation
    * @throws IOException If there is an error reading or writing the app to or
    * from disk.
    * @throws PSNotLockedException If the app is not already locked by the
    * supplied Id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSNonUniqueException if the requestroot is already in use by a
    * different app.
    * @throws PSNotFoundException if app is cannot be located on restart.
    * @throws PSServerException for any other errors encountered.
    * @throws PSAuthenticationRequiredException if user has not been
    * authenticated.
    * @throws PSAuthorizationException if user is not allowed to perform this
    * operation.
    */
   public void saveApplication(PSApplication app, IPSLockerId lockId,
         PSSecurityToken tok, boolean validate)
      throws PSServerException, PSNotLockedException, PSSystemValidationException,
      IOException, PSLockedException, PSNonUniqueException,
      PSAuthorizationException, PSAuthenticationRequiredException,
      PSNotFoundException
   {
      boolean renameRoot = false;
      boolean renameApp = false;
      boolean isNewApp = false;
      String curName = app.getName();
      String curRoot = app.getRequestRoot();
      File newAppDir = null;

      File appFile = m_objectStoreHandler.getApplicationFile(curName);

      // validate inputs
      if (app == null)
         throw new IllegalArgumentException("app may not be null");

      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if ((curName == null) || (curName.length() == 0))
         throw new IllegalArgumentException("app name may not be null");

      if (validate)
      {
         m_objectStoreHandler.validateApplicationObject(app);
      }

      // check security
      tok.setResourceType("save application");
      tok.setResourceName(app.getName());
      checkCanSaveApplication(app, tok);

      // see if renaming app or root
      PSApplicationSummaryCollection appSums = m_objectStoreHandler.m_appSums;
      PSApplicationSummary curSum = appSums.getSummary(app.getId());
      if (curSum != null)
      {
         curName = curSum.getName();
         curRoot = curSum.getAppRoot();
         if (!curName.equals(app.getName()))
            renameApp = true;
         if (!curRoot.equals(app.getRequestRoot()))
            renameRoot = true;
      }
      else
      {
         isNewApp = true;
      }

      /*
       * Check to see if app is locked - be sure it's the current app in case of
       * a rename.
       */
      if (!isApplicationLocked(lockId, curName))
      {
         Object[] args = { curName };
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               args);
      }
      else
      {
         // extend the lock to be sure we keep it
         getApplicationLock(lockId, curName, 30);
      }

      RecoverableFile curAppFile = null;
      RecoverableFile curAppRoot = null;

      boolean success = false;
      try
      {
         if (renameApp)
         {
            curAppFile = new RecoverableFile(m_objectStoreHandler
                  .getApplicationFile(curName));
         }

         /*
          * if root has changed, pre-emptively create the new request root
          * directory to hold the name for later (we will delete it if there is
          * an error)
          */
         if (renameRoot)
         {
            curAppRoot = new RecoverableFile(getAppRootDir(curRoot));

            File newRoot = getAppRootDir(app.getRequestRoot());

            // pre-emptively create the new request root directory to
            // hold the name for later (we will
            // delete it if there is an error)
            if (!newRoot.mkdir())
            {
               Object[] args = { curSum.getAppRoot(), app.getRequestRoot() };
               throw new PSNonUniqueException(
                     IPSObjectStoreErrors.APP_ROOT_RENAME_FAILED, args);
            }

            newAppDir = newRoot;
         }

         /*
          * if changing the name or the root, remove the virtual directory so no
          * one will be able to use the app while the name and/or root is being
          * changed.
          */
         if (renameRoot)
         {
            m_objectStoreHandler
                  .removeVirtualAppDirectory(curSum.getAppRoot());
         }

         // if this is a new application or a newly validated application,
         // then generate an ID for it
         if (isNewApp || app.getId() < 1)
         {
            synchronized (appSums)
            {
               // set the ID, it will get written out to disk with the obj
               int id = appSums.addSummary(app, true);
               app.setId(id);
               curSum = appSums.getSummary(id);

               String reqRoot = app.getRequestRoot();
               if (reqRoot != null && reqRoot.length() > 0)
               {
                  // add a virtual directory entry for this new app
                  m_objectStoreHandler.addVirtualAppDirectory(reqRoot,
                        getAppRootDir(reqRoot));
               }

               com.percussion.server.PSConsole.printMsg("ObjectStore",
                     "Allocated application ID: " + id + " for application "
                           + app.getName(), null);
            }
         }

         // finally, write it to the disk file
         OutputStream fout = new BufferedOutputStream(m_objectStoreHandler
               .lockOutputStream(appFile));
         try
         {
            PSXmlDocumentBuilder.write(app.toXml(), fout);
         }
         finally
         {
            m_objectStoreHandler.releaseOutputStream(fout, appFile);
         }

         synchronized (appSums)
         {
            /*
             * and rename the application's request root if necessary and not
             * renaming the app
             */
            if (renameRoot || renameApp)
            {
               // if renaming also change the app name and aclhandler
               if (renameApp)
               {
                  // need to remove and re-add the summary under the new key
                  appSums.removeSummary(app.getId());
                  appSums.addSummary(app, false);
                  curSum = appSums.getSummary(app.getName());
               }

               if (renameRoot)
               {
                  if (!curAppRoot
                        .renameTo(getAppRootDir(app.getRequestRoot())))
                  {
                     Object[] args = { curSum.getAppRoot(),
                           app.getRequestRoot() };
                     throw new PSNonUniqueException(
                           IPSObjectStoreErrors.APP_ROOT_RENAME_FAILED, args);
                  }

                  // if also renaming, already fixed up the summary, otherwise
                  // do it now
                  if (!renameApp)
                     curSum.setAppRoot(app.getRequestRoot());
               }

               // add the new virtual directory
               String reqRoot = app.getRequestRoot();
               m_objectStoreHandler.addVirtualAppDirectory(reqRoot,
                     getAppRootDir(reqRoot));
            }

            // create a new ACL handler for the app, whether new or renamed etc.
            curSum.setAclHandler(new PSAclHandler(app.getAcl()));
         }

         // Now delete the old xml file if this is a rename scenario
         if (renameApp)
         {
            curAppFile.delete();
            curAppFile = null;
         }

         // notify any listeners
         m_objectStoreHandler.notifyApplicationListeners(app, isNewApp, false);
         success = true;
      }
      catch (IllegalArgumentException | PSException e)
      {
         throw new PSServerException(e);
      }
      finally
      {
         if (!success)
         {
            // undo our preemptive creation of the new dir
            if (newAppDir != null)
               newAppDir.delete();
            newAppDir = null;

            // recover both application file and directory if needed
            if (curAppFile != null)
               curAppFile.recover();
            if (curAppRoot != null)
               curAppRoot.recover();
         }
      }
   }

   /**
    * Gets the role configuration object for this server. The supplied token
    * must have admin access or a PSAuthorizationException is thrown. It's
    * assumed the config lock is being managed by the caller of this method.
    * 
    * @param tok The security token for the individual making the request. Never
    * <code>null</code>. To gain access to the role config object, the token
    * must be for a user w/ admin access.
    * 
    * @return A valid object containing 0 or more roles.
    * 
    * @throws PSServerException If any problems occur loading the data. Note:
    * This is a misuse of this exception, which is supposed to indicate a
    * connection to the server couldn't be made. I continue with its misuse
    * because it would be a lot of work to correct and we don't have time to do
    * it now.
    * 
    * @throws PSAuthorizationException If the supplied token doesn't have admin
    * access.
    * 
    * @throws PSAuthenticationRequiredException If the supplied tok is not for
    * an authenticated user.
    */
   public PSRoleConfiguration getRoleConfigurationObject(PSSecurityToken tok)
      throws PSServerException, PSAuthorizationException,
      PSAuthenticationRequiredException
   {
      checkCanEditServerConfig(tok);
      try
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSRoleConfiguration config = new PSRoleConfiguration();
         config.fromDb(new PSDatabaseComponentLoader(req));
         return config;
      }
      catch (Exception e)
      {
         throw new PSServerException(
               IPSObjectStoreErrors.ROLE_CFG_LOAD_EXCEPTION, e.toString());
      }
   }

   /**
    * Gets the server configuration object. The server config is not locked as a
    * result of this call.
    * 
    * @param tok The user's security token. May not be <code>null</code>.
    * @return The server configuration object.
    * 
    * @throws PSServerException for any errors encountered.
    */
   public PSServerConfiguration getServerConfigObject(PSSecurityToken tok)
      throws PSServerException
   {

      PSServerConfiguration config = null;
      try
      {
         Document cfgDoc = getServerConfigDoc(tok);
         config = new PSServerConfiguration(cfgDoc);
      }
      catch (PSUnknownNodeTypeException ne)
      {
         throw new PSServerException(ne.getClass().toString(), ne
               .getErrorCode(), ne.getErrorArguments());
      }
      catch (PSUnknownDocTypeException de)
      {
         throw new PSServerException(de.getClass().toString(), de
               .getErrorCode(), de.getErrorArguments());
      }

      return config;
   }

   /**
    * Gets the server configuration document. The server config is not locked as
    * a result of this call. If the user does not have admin access, the config
    * is returned minus any sensitive information.
    * 
    * @param tok The user's security token. May not be <code>null</code>.
    * @return The server configuration as an XML document.
    * 
    * @throws PSServerException for any error encountered.
    */
   public Document getServerConfigDoc(PSSecurityToken tok)
      throws PSServerException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null.");

      Document cfgDoc = m_objectStoreHandler.loadServerConfig();
      Element cfgRoot = cfgDoc.getDocumentElement();

      boolean hideAcl = false;
      try
      {
         checkCanEditServerConfig(tok);
      }
      catch (Exception e)
      {
         // this tells us they cannot read the whole thing
         hideAcl = true;
      }

      // deal with hideAcl
      if (hideAcl)
      {
         clearElementsByTagName(cfgRoot, "userId");
         clearElementsByTagName(cfgRoot, "PSXAcl");

         /* and remove all passwords */
         clearElementsByTagName(cfgRoot, "password");
         clearElementsByTagName(cfgRoot, "loginPw");
      }

      return cfgDoc;
   }

   /**
    * Locks and returns the server configuration object.
    * 
    * @param lockId The id used to lock the config. Config must already be
    * locked with the given id. Lock will be extended.
    * @param tok The user's security token. May not be <code>null</code>.
    * @return The server configuration object.
    * @throws PSNotLockedException If the config is not already locked by the
    * id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSAuthorizationException if the user is not allowed access to the
    * config.
    * @throws PSAuthenticationRequiredException if we have not tried to
    * authenticate this user.
    * @throws PSServerException for any other errors encountered.
    */
   public PSServerConfiguration getServerConfigObject(IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSNotLockedException, PSServerException, PSLockedException,
      PSAuthorizationException, PSAuthenticationRequiredException
   {
      // validate params
      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      PSServerConfiguration config;
      try
      {
         Document cfgDoc = getServerConfigDoc(lockId, tok);
         config = new PSServerConfiguration(cfgDoc);
      }
      catch (PSUnknownNodeTypeException ne)
      {
         throw new PSServerException(ne.getClass().toString(), ne
               .getErrorCode(), ne.getErrorArguments());
      }
      catch (PSUnknownDocTypeException de)
      {
         throw new PSServerException(de.getClass().toString(), de
               .getErrorCode(), de.getErrorArguments());
      }

      return config;
   }

   /**
    * Locks and returns the server configuration document.
    * 
    * @param lockId The id used to lock the config. Config must already be
    * locked with the given id. Lock will be extended.
    * @param tok The user's security token.
    * @return The server configuration as an XML document.
    * @throws PSNotLockedException If the config is not already locked by the
    * id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSAuthorizationException if the user is not allowed access to the
    * config.
    * @throws PSAuthenticationRequiredException if we have not tried to
    * authenticate this user.
    * @throws PSServerException for any other errors encountered.
    */
   public Document getServerConfigDoc(IPSLockerId lockId, PSSecurityToken tok)
      throws PSLockedException, PSNotLockedException, PSServerException,
      PSLockedException, PSAuthorizationException,
      PSAuthenticationRequiredException
   {
      // validate params
      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // Check to see if config is locked
      if (!isServerConfigLocked(lockId))
      {
         Object[] args = { "Server Configuration" };
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               args);
      }
      else
      {
         // extend the lock to be sure we keep it
         getServerConfigLock(lockId, 30);
      }

      tok.setResourceType("Edit");
      tok.setResourceName("Server Configuration");
      checkCanEditServerConfig(tok);

      // now load the config
      Document cfgDoc = m_objectStoreHandler.loadServerConfig();

      return cfgDoc;
   }

   /**
    * Saves the role configuration to persistent storage. Server Config must
    * have been locked prior to this call by the supplied Id. Lock will be
    * extended once it has been determined that the lock exists.
    * 
    * @param config The server config object. May not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * @throws PSNotLockedException If the config is not already locked by the
    * supplied Id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSServerException for any other errors encountered.
    */
   public void saveRoleConfiguration(PSRoleConfiguration config,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSNotLockedException, PSServerException, PSLockedException,
      PSAuthorizationException, PSAuthenticationRequiredException
   {
      // validate inputs
      if (config == null)
         throw new IllegalArgumentException("config may not be null");

      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // check to see if user is allowed
      tok.setResourceType("Save");
      tok.setResourceName("Server Configuration");
      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      if (!isServerConfigLocked(lockId))
      {
         Object[] args = { "Server Configuration" };
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               args);
      }
      else
      {
         // extend the lock to be sure we keep it
         getServerConfigLock(lockId, 30);
      }

      PSExecutionData data = null;
      try
      {
         Document doc = config.toDbXml();
         PSRequest req = PSRequest.getContextForRequest();
         req.setInputDocument(doc);
         IPSInternalRequestHandler rh = PSServer
               .getInternalRequestHandler("sys_components/UpdateRoleCfg");

         data = rh.makeInternalRequest(req);
      }
      catch (PSInternalRequestCallException ire)
      {
         throw new PSServerException(ire.getErrorCode(), ire
               .getErrorArguments());
      }
      catch (PSDatabaseComponentException dbe)
      {
         throw new PSServerException(dbe.getErrorCode(), dbe
               .getErrorArguments());
      }
      catch (PSAuthenticationFailedException afe)
      {
         throw new PSAuthenticationRequiredException(afe.getErrorCode(), afe
               .getErrorArguments());
      }
      finally
      {
         // Release the execution data for the internal request.
         if (data != null)
            data.release();
      }
   }

   /**
    * Saves the server config object to disk. Server Config must have been
    * locked prior to this call by the supplied Id. Lock will be extended once
    * it has been determined that the lock exists.
    * 
    * @param config The server config object. May not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * @throws PSNotLockedException If the config is not already locked by the
    * supplied Id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSSystemValidationException if the config is invalid.
    * @throws PSServerException for any other errors encountered.
    */
   public void saveServerConfig(PSServerConfiguration config,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSNotLockedException, PSServerException, PSSystemValidationException,
      PSLockedException, PSAuthorizationException,
      PSAuthenticationRequiredException
   {
      // validate inputs
      if (config == null)
         throw new IllegalArgumentException("config may not be null");

      if (lockId == null)
         throw new IllegalArgumentException("lockId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // check to see if user is allowed
      tok.setResourceType("Save");
      tok.setResourceName("Server Configuration");
      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      if (!isServerConfigLocked(lockId))
      {
         Object[] args = { "Server Configuration" };
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               args);
      }
      else
      {
         // extend the lock to be sure we keep it
         getServerConfigLock(lockId, 30);
      }

      try
      {
         // validate the config
         PSValidatorAdapter validator = new PSValidatorAdapter(
               m_objectStoreHandler);
         validator.throwOnErrors(true);
         validator.validateServerConfiguration(config);

         // save it
         File cfgFile = m_objectStoreHandler.getServerConfigFile();
         OutputStream fout = new BufferedOutputStream(m_objectStoreHandler
               .lockOutputStream(cfgFile));
         try
         {
            PSXmlDocumentBuilder.write(config.toXml(), fout);
         }
         finally
         {
            m_objectStoreHandler.releaseOutputStream(fout, cfgFile);
         }

         // nofify listeners
         m_objectStoreHandler.notifyServerListeners(config);
      }
      catch (PSAuthorizationException e)
      {
         throw new PSServerException(e);
      }
      catch (PSNonUniqueException e)
      {
         throw new PSServerException(e);
      }
      catch (IOException e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Saves the server config document to disk. Server Config must have been
    * locked prior to this call by the supplied Id. Lock will be extended once
    * it has been determined that the lock exists.
    * 
    * @param config The server config XML doc. May not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * @throws PSNotLockedException If the config is not already locked by the
    * supplied Id.
    * @throws PSLockedException If there is a problem extending the lock.
    * @throws PSSystemValidationException if the config is invalid.
    * @throws PSServerException for any other errors encountered.
    */
   public void saveServerConfig(Document config, IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSNotLockedException, PSServerException, PSLockedException,
           PSSystemValidationException, PSAuthorizationException,
      PSAuthenticationRequiredException
   {
      // validate inputs
      if (config == null)
         throw new IllegalArgumentException("config cannot be null.");

      if (lockId == null)
         throw new IllegalArgumentException("lockId cannot be null.");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      try
      {
         PSServerConfiguration cfg = new PSServerConfiguration(config);
         saveServerConfig(cfg, lockId, tok);
      }
      catch (PSUnknownNodeTypeException ne)
      {
         throw new PSServerException(ne.getClass().toString(), ne
               .getErrorCode(), ne.getErrorArguments());
      }
      catch (PSUnknownDocTypeException de)
      {
         throw new PSServerException(de.getClass().toString(), de
               .getErrorCode(), de.getErrorArguments());
      }

   }

   /**
    * Aquires or extends an application lock.
    * 
    * @param id A locker id that identifies the user attempting to obtain or
    * extend a lock. If extending, must identify the same user that obtained the
    * current lock. May not be <code>null</code>.
    * @param appName The name of the app to lock. May not be <code>null</code>.
    * @param lockMins The number of minutes to hold the lock before it should
    * expire. Must be a non-negative number.
    * @throws PSLockedException If the lock cannot be aquired.
    * @throws PSServerException if there are errors obtaining the lock.
    */
   public void getApplicationLock(IPSLockerId id, String appName, int lockMins)
      throws PSLockedException, PSServerException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      if (lockMins < 0)
         throw new IllegalArgumentException(
               "lockMins must be a non-negative number");

      try
      {
         Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
               PSObjectFactory.createApplication().getClass(), appName,
               IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);

         // the lock manager fills this out with detailed error info on failure
         PSLockedException ex = new PSLockedException(0, null);

         boolean acquired = m_objectStoreHandler.m_lockMgr.acquireLock(id,
               lockKey, lockMins * 60000, // convert minutes to milliseconds
               0, // don't time out
               ex);

         if (!acquired)
         {
            ex.setObjectName(appName);
            ex.constructArguments();
            throw ex;
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }
      catch (PSLockAcquisitionException e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Releases a lock held on an application.
    * 
    * @param id The locker id that identifies the user attempting to release the
    * lock. If the id does not identify the same user that obtained the lock,
    * then the lock will not be released.
    * @param appName The name of the application to unlock.
    * @throws PSServerException if there are errors releasing the lock.
    */
   public void releaseApplicationLock(IPSLockerId id, String appName)
      throws PSServerException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      try
      {
         Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
               PSObjectFactory.createApplication().getClass(), appName,
               IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);

         m_objectStoreHandler.m_lockMgr.releaseLock(id, lockKey);
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Determines if the supplied locker id currently has the application locked.
    * 
    * @param id The locker id that identifies the user who should have the lock.
    * @param appName The name of the application to check.
    * @return Returns <code>true</code> if the application is locked by the
    * user identified by the id. Returns <code>false</code> if the app is not
    * locked or locked by another user.
    * @throws PSServerException if there are errors checking the lock.
    */
   public boolean isApplicationLocked(IPSLockerId id, String appName)
      throws PSServerException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      boolean isLocked;
      try
      {
         Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
               PSObjectFactory.createApplication().getClass(), appName,
               IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);
         isLocked = m_objectStoreHandler.m_lockMgr.isLocked(id, lockKey);
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }

      return isLocked;
   }

   /**
    * Get the locker properties for the specified application
    * 
    * @param id The locker id, may not be <code>null</code>.
    * @param appName The application name, may not be <code>null</code> or
    * empty.
    * 
    * @return The lock info as properties, <code>null</code> if it is not
    * locked:
    * <ul>
    * <li>lockerName</li>
    * <li>lockerSession</li>
    * <li></li>
    * </ul>
    */
   public Properties getApplicationLockInfo(IPSLockerId id, String appName)
   {
      Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
            PSObjectFactory.createApplication().getClass(), appName,
            IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);

      return m_objectStoreHandler.m_lockMgr.getLockInfo(id, lockKey);
   }

   /**
    * Aquires or extends a lock on the server config.
    * 
    * @param id A locker id that identifies the user attempting to obtain or
    * extend a lock. May not be <code>null</code>.
    * @param lockMins The number of minutes to hold the lock before it should
    * expire. Must be a non-negative number.
    * @throws PSLockedException If the lock cannot be aquired.
    * @throws PSServerException if there are errors obtaining the lock.
    */
   public void getServerConfigLock(IPSLockerId id, int lockMins)
      throws PSLockedException, PSServerException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      if (lockMins < 0)
         throw new IllegalArgumentException(
               "lockMins must be a non-negative number");

      m_objectStoreHandler.printMsg("Locking server configuration for "
            + lockMins + " minutes");

      try
      {
         Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
               PSObjectFactory.createServerConfiguration().getClass(),
               SERVER_CONFIG_LOCK_ID,
               IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);

         PSLockedException ex = new PSLockedException(0, null);

         boolean acquired = m_objectStoreHandler.m_lockMgr.acquireLock(id,
               lockKey, lockMins * 60000, // convert minutes to milliseconds
               0, // don't time out
               ex);

         if (!acquired)
         {
            ex.setObjectName("Server configuration");
            ex.constructArguments();
            throw ex;
         }
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }
      catch (PSLockAcquisitionException e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Releases a lock held on the server configuration.
    * 
    * @param id The locker id that identifies the user attempting to release the
    * lock. If the id does not identify the same user that obtained the lock,
    * then the lock will not be released.
    * @throws PSServerException if there are errors releasing the lock.
    */
   public void releaseServerConfigLock(IPSLockerId id)
      throws PSServerException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      try
      {
         m_objectStoreHandler.printMsg("Unlocking server configuration.");

         Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
               PSObjectFactory.createServerConfiguration().getClass(),
               SERVER_CONFIG_LOCK_ID,
               IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);

         m_objectStoreHandler.m_lockMgr.releaseLock(id, lockKey);
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }

   }

   /**
    * Determines if the supplied locker id currently has the server
    * configuration locked.
    * 
    * @param id The locker id that identifies the user who should have the lock.
    * @return Returns <code>true</code> if the server config is locked by the
    * user identified by the id. Returns <code>false</code> if the config is
    * not locked or locked by another user.
    * @throws PSServerException if there is an error checking the lock.
    */
   public boolean isServerConfigLocked(IPSLockerId id)
      throws PSServerException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      boolean isLocked;
      try
      {
         Object lockKey = m_objectStoreHandler.m_lockMgr.getLockKey(
               PSObjectFactory.createServerConfiguration().getClass(),
               SERVER_CONFIG_LOCK_ID,
               IPSObjectStoreLockManager.LOCKTYPE_EXCLUSIVE);
         isLocked = m_objectStoreHandler.m_lockMgr.isLocked(id, lockKey);
      }
      catch (IllegalArgumentException e)
      {
         throw new PSServerException(e);
      }

      return isLocked;
   }

   /**
    * Checks to see if user has full permission to read, edit and save the
    * server configuration. If reading (not edit mode), then a failure from this
    * method indicates that the user does not have permission to read the
    * sensitive information from the config (ACL, userids, and passwords).
    * 
    * @param tok The user's security token. May not be <code>null</code>.
    * 
    * @throws PSAuthorizationException if user is not allowed the access
    * @throws PSAuthenticationRequiredException if type of request requires
    * authentication and user has not been authenticated.
    */
   public void checkCanEditServerConfig(PSSecurityToken tok)
      throws PSAuthorizationException, PSAuthenticationRequiredException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null.");

      // only administrators can read all, edit, or save
      PSServer.checkAccessLevel(tok, PSAclEntry.SACE_ADMINISTER_SERVER);
   }

   /**
    * Checks to see if user can read (and possibly edit) an application.
    * 
    * @param app The app the user wishes to read or edit.
    * @param tok The user's security token. May not be <code>null</code>.
    * @param isEditing <code>True</code> if the user is editing the app (not
    * opening it read only).
    * 
    * @throws PSAuthorizationException if user is not allowed the access
    * @throws PSAuthenticationRequiredException if type of request requires
    * @throws PSServerException for any other errors authentication and user has
    * not been authenticated.
    */
   public void checkCanReadApplication(PSApplication app, PSSecurityToken tok,
         boolean isEditing)
      throws PSAuthorizationException, PSAuthenticationRequiredException,
      PSServerException
   {

      try
      {
         if (tok == null)
            throw new IllegalArgumentException("tok may not be null.");

         // get app aclhandler
         PSAclHandler aclHandler = loadAclHandler(app);

         // see if user has required acess to read
         int appAccessLevel = aclHandler.getUserAccessLevel(tok);

         if ((PSAclEntry.AACE_DESIGN_READ & appAccessLevel) != PSAclEntry.AACE_DESIGN_READ)
            throw new PSAuthorizationException("read application", app
                  .getName(), tok.getUserSessionId());

         if (isEditing)
         {
            // must have update and delete
            if ((PSAclEntry.AACE_DESIGN_UPDATE & appAccessLevel) != PSAclEntry.AACE_DESIGN_UPDATE)
               throw new PSAuthorizationException("edit application", app
                     .getName(), tok.getUserSessionId());
         }
      }
      catch (PSNotFoundException e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Checks to see if user can read (and possibly edit) an application.
    * 
    * @param appName The name of the app the user wishes to read or edit. May
    * not be <code>null</code> or empty.
    * @param tok The user's security token. May not be <code>null</code>.
    * @param isEditing <code>true</code> if the user is editing the app (not
    * opening it read only).
    * 
    * @throws PSAuthorizationException if user is not allowed the access
    * @throws PSAuthenticationRequiredException if type of request requires
    * @throws PSServerException for any other errors authentication and user has
    * not been authenticated.
    */
   public void checkCanReadApplication(String appName, PSSecurityToken tok,
         boolean isEditing)
      throws PSAuthorizationException, PSAuthenticationRequiredException,
      PSServerException
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      try
      {
         if (tok == null)
            throw new IllegalArgumentException("tok may not be null.");

         // get app aclhandler
         PSAclHandler aclHandler = m_objectStoreHandler
               .loadAclHandler(appName);

         // see if user has required acess to read
         int appAccessLevel = aclHandler.getUserAccessLevel(tok);

         if ((PSAclEntry.AACE_DESIGN_READ & appAccessLevel) != PSAclEntry.AACE_DESIGN_READ)
            throw new PSAuthorizationException("read application", appName,
                  tok.getUserSessionId());

         if (isEditing)
         {
            // must have update and delete
            if ((PSAclEntry.AACE_DESIGN_UPDATE & appAccessLevel) != PSAclEntry.AACE_DESIGN_UPDATE)
               throw new PSAuthorizationException("edit application", appName,
                     tok.getUserSessionId());
         }
      }
      catch (PSNotFoundException e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Checks to see if user can save an application.
    * 
    * @param app The app the user wishes to save.
    * @param tok The user's security token. May not be <code>null</code>.
    * 
    * @throws PSAuthorizationException if user is not allowed the access
    * @throws PSAuthenticationRequiredException if type of request requires
    * authentication and user has not been authenticated.
    * @throws PSNotFoundException if updating an app and the old app cannot be
    * located.
    * @throws PSServerException for any other errors.
    */
   public void checkCanSaveApplication(PSApplication app, PSSecurityToken tok)
      throws PSAuthorizationException, PSAuthenticationRequiredException,
      PSNotFoundException, PSServerException
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null.");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null.");

      // see if user is creating a new app
      String curAppName = app.getName();
      boolean isNewApp = false;
      PSApplicationSummary sum = m_objectStoreHandler.m_appSums.getSummary(app
            .getId());
      if (sum == null)
      {
         // no summary, so this is new
         isNewApp = true;
      }
      else
      {
         curAppName = sum.getName();
      }

      if (isNewApp)
      {
         // See if they can create
         tok.setResourceName(curAppName);
         tok.setResourceType("create application");
         PSServer.checkAccessLevel(tok, PSAclEntry.SACE_CREATE_APPLICATIONS);
      }
      else
      {
         // See if they can update
         PSAclHandler aclHandler = loadAclHandler(app);
         int appAccessLevel = aclHandler.getUserAccessLevel(tok);

         if ((PSAclEntry.AACE_DESIGN_UPDATE & appAccessLevel) != PSAclEntry.AACE_DESIGN_UPDATE)
            throw new PSAuthorizationException(tok.getResourceName(), tok
                  .getResourceName(), tok.getUserSessionId());

         /*
          * Check if the acl has changed, first check if the user has acl
          * modification access, as that is the faster thing to check
          */
         if ((PSAclEntry.AACE_DESIGN_MODIFY_ACL & appAccessLevel) != PSAclEntry.AACE_DESIGN_MODIFY_ACL)
         {
            /* Check to ensure they haven't modified the ACL */
            PSAcl newAcl = null;
            PSAcl oldAcl = null;

            /* Grab the old document */
            try
            {
               PSApplication oldApp = loadApplicationObject(curAppName, true);
               oldAcl = oldApp.getAcl();
               newAcl = app.getAcl();
            }
            catch (Exception e)
            {
               throw new PSAuthorizationException("AclModificationCheck",
                     curAppName, tok.getUserSessionId());
            }
            if (!newAcl.getEntries().equals(oldAcl.getEntries()))
               throw new PSAuthorizationException("AclModification",
                     curAppName, tok.getUserSessionId());
         }
      }
   }

   /**
    * Checks to see if user can delete an application.
    * 
    * @param app The app the user wishes to delete, may not be <code>null</code>.
    * @param tok The user's security token. May not be <code>null</code>.
    * 
    * @throws PSAuthorizationException if user is not allowed the access
    * @throws PSAuthenticationRequiredException if type of request requires
    * authentication and user has not been authenticated.
    * @throws PSNotFoundException if the app cannot be located.
    * @throws PSServerException for any other errors.
    */
   public void checkCanDeleteApplication(PSApplication app, PSSecurityToken tok)
      throws PSAuthorizationException, PSAuthenticationRequiredException,
      PSNotFoundException, PSServerException
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null.");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null.");

      // See if they can update
      PSAclHandler aclHandler = loadAclHandler(app);
      int appAccessLevel = aclHandler.getUserAccessLevel(tok);

      if ((PSAclEntry.AACE_DESIGN_DELETE & appAccessLevel) != PSAclEntry.AACE_DESIGN_DELETE)
         throw new PSAuthorizationException(tok.getResourceName(), tok
               .getResourceName(), tok.getUserSessionId());
   }

   /**
    * Private constructor. Class follows Singleton pattern. Use
    * {@link #createInstance(PSXmlObjectStoreHandler)} to create and obtain the
    * one instance intially, and {@link #getInstance()} to obtain the one
    * instance after that.
    * 
    * @param os The objectstore handler. May not be <code>null</code>.
    */
   private PSServerXmlObjectStore(PSXmlObjectStoreHandler os)
   {
      if (os == null)
         throw new IllegalArgumentException("os may not be null");

      // Setup paths
      File configDir = new File(PSServer.getRxFile(CFG_DIR));
      File cmsDir = new File(configDir, CMS_DIR);
      m_cfgDirShared = new File(cmsDir, CMS_SHARED_DIR);
      m_cfgFileSystemDef = new File(cmsDir, "ContentEditorSystemDef.xml");

      m_objectStoreHandler = os;
   }

   /**
    * Loads application from disk and returns it as an XML Doc. Application is
    * not locked and security is not checked as a result of this call.
    * 
    * @param appName The name of the application. May not be <code>null</code>.
    * @return The application XML Doc.
    * @throws PSNotFoundException if an application be that name does not exist.
    * @throws PSServerException for any other errors encountered.
    */
   private Document loadApplicationDoc(String appName)
      throws PSServerException, PSNotFoundException
   {
      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      // get the doc
      Document appDoc = null;
      appDoc = m_objectStoreHandler.loadApplication(appName);

      return appDoc;
   }

   /**
    * Loads application object from disk. Application is not locked and security
    * is not checked as a result of this call.
    * 
    * @param appName The name of the application. May not be <code>null</code>.
    * @param fixupCeFields flag to indicate content editor field fixup is
    * required, <code>true</code> to fixup <code>false</code> otherwise.
    * @return The application object.
    * @throws PSNotFoundException if an application be that name does not exist.
    * @throws PSServerException for any other errors encountered.
    */
   private PSApplication loadApplicationObject(String appName,
         boolean fixupCeFields) throws PSServerException, PSNotFoundException
   {
      if (appName == null)
         throw new IllegalArgumentException("appName may not be null");

      PSApplication app = null;
      try
      {
         Document appDoc = loadApplicationDoc(appName);
         app = new PSApplication(appDoc);

         // Need to call fixupFields() to set the "correct" search
         // properties for binary fields
         Iterator dataSets = app.getDataSets().iterator();
         while (dataSets.hasNext())
         {
            Object dataSet = dataSets.next();
            if ((dataSet instanceof PSContentEditor) && fixupCeFields)
            {
               PSContentEditor ce = (PSContentEditor) dataSet;
               PSContentEditorPipe pipe = (PSContentEditorPipe) ce.getPipe();

               try
               {
                  fixupFields(pipe.getMapper().getFieldSet(), pipe
                        .getLocator().getTableSets(), pipe.getLocator()
                        .getBackEndTables());
               }
               catch (SQLException se)
               {
                  throw new RuntimeException(se.getLocalizedMessage());
               }

            }
         }

      }
      catch (PSUnknownNodeTypeException ne)
      {
         throw new PSServerException(ne.getClass().toString(), ne
               .getErrorCode(), ne.getErrorArguments());
      }
      catch (PSUnknownDocTypeException de)
      {
         throw new PSServerException(de.getClass().toString(), de
               .getErrorCode(), de.getErrorArguments());
      }

      return app;
   }

   /**
    * Clear elements in a document based on the tag name, remove all the
    * elements' children
    * 
    * @param rootElement Element to start searching from.
    * @param tagName The name of the tag to remove
    */
   private void clearElementsByTagName(Element rootElement, String tagName)
   {
      if (rootElement == null)
         throw new IllegalArgumentException("rootElement may not be null.");
      if (tagName == null)
         throw new IllegalArgumentException("tagName may not be null.");

      NodeList nodes = rootElement.getElementsByTagName(tagName);
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node n = nodes.item(i);
         if (n.hasChildNodes())
         {
            NodeList children = n.getChildNodes();
            for (int j = 0; j < children.getLength(); j++)
            {
               n.removeChild(children.item(j));
            }
         }
      }
   }

   /**
    * A utility method to get the ACL handler for an application, loading or
    * re-loading it from disk if necessary.
    * 
    * @param app The name of the app.
    * 
    * @return The PSAclHandler. Never <code>null</code>.
    * @throws PSNotFoundException if the app does not exist on disk
    * @throws PSServerException for any other errors encountered.
    */
   PSAclHandler loadAclHandler(PSApplication app)
      throws PSNotFoundException, PSServerException
   {
      if (app == null)
         throw new IllegalArgumentException("app may not be null");

      String appName = m_objectStoreHandler.getApplicationNameFromId(app
            .getId());

      if (appName == null)
         appName = app.getName();

      return m_objectStoreHandler.loadAclHandler(appName);
   }

   /**
    * Loads and caches the system definition for Content Editors. Cache is
    * cleared when the file's modification timestamp changes.
    * 
    * @return The content editor system def object. May be <code>null</code>
    * if the file was not found.
    * 
    * @throws IOException if an error occurs while reading the file
    */
   public PSContentEditorSystemDef getContentEditorSystemDef()
      throws IOException, SAXException, PSUnknownDocTypeException,
      PSUnknownNodeTypeException
   {
      InputStream in = null;
      try
      {
         // Set the flag to false to start with.
         boolean isModifedOnDisk = false;
         if (m_cfgFileSystemDef.exists())
         {
            if (m_cfgFileSystemDef.lastModified() != m_ModifiedDateTimeSystemDef)
            {
               // Set the flag to true so that the object is constructed from
               // the file on the disk
               isModifedOnDisk = true;
               // Update the last modifed date time stamp
               m_ModifiedDateTimeSystemDef = m_cfgFileSystemDef.lastModified();
            }
         }
         else if (m_ModifiedDateTimeSystemDef > 0)
         {
            // the file was existing and is deleted from the disk
            m_contentEditorSystemDef = null;
         }

         if (isModifedOnDisk)
         {
            // now try to load it
            try
            {
               in = m_objectStoreHandler.lockInputStream(m_cfgFileSystemDef);
               Document cfgDoc = PSXmlDocumentBuilder.createXmlDocument(in,
                     false);
               m_contentEditorSystemDef = new PSContentEditorSystemDef(cfgDoc);

               // reset information on the fields that isn't stored
               PSFieldSet fs = m_contentEditorSystemDef.getFieldSet();
               PSContainerLocator loc = m_contentEditorSystemDef
                     .getSystemLocator();
               if (null != fs && null != loc)
               {
                  // must be done in this order
                  fs.fixupBackEndColumns(loc.getBackEndTables());
                  fixupFields(fs, loc.getTableSets(), loc.getBackEndTables());
               }
            }
            catch (SQLException se)
            {
               throw new RuntimeException(se.getLocalizedMessage());
            }
            catch (PSSystemValidationException ve)
            {
               // this should never happen
               throw new RuntimeException(ve.getLocalizedMessage());
            }
            catch (FileNotFoundException e)
            {
               // if the file doesn't exist, just return null
            }
         }
      }
      finally
      {
         if (in != null)
            try
            {
               m_objectStoreHandler.releaseInputStream(in, m_cfgFileSystemDef);
            }
            catch (Exception e)
            {
            }
      }
      return m_contentEditorSystemDef;
   }

   /**
    * Saves the content editor system def.
    * 
    * @param def The system def, may not be <code>null</code>.
    * 
    * @throws IOException if there are any errors.
    */
   public void saveContentEditorSystemDef(PSContentEditorSystemDef def)
      throws IOException
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      OutputStream out = null;
      try
      {
         out = m_objectStoreHandler.lockOutputStream(m_cfgFileSystemDef);
         PSXmlDocumentBuilder.write(def.toXml(), out);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               m_objectStoreHandler.releaseOutputStream(out,
                     m_cfgFileSystemDef);
            }
            catch (IOException e)
            {
            }
         }
      }

   }

   /**
    * Loads the shared definition for Content Editors. There may be multiple
    * shared def files located under the rxconfig directory. In this case they
    * are loaded in no particular order, and a single shared def is created by
    * using the following logic: <br>
    * <ol>
    * <li>All shared field groups are combined into a single list of groups.</li>
    * <li>The first ApplicationFlow found will be used in the new def</li>
    * <li>The first CommandHandlerStylesheets found will be used in the new
    * def.</li>
    * </ol>
    * Therefore only one of the files should contain ApplicationFlow and
    * Stylesheet definitions.
    * 
    * @return The content editor shared def object. May be <code>null</code>
    * if there was an error loading the def from the Xml.
    */
   @SuppressWarnings("unchecked")
   public PSContentEditorSharedDef getContentEditorSharedDef()
      throws IOException, SAXException, PSUnknownDocTypeException,
      PSUnknownNodeTypeException
   {
      // Assume the file(s) on the disk is not modified initially
      boolean isModifiedOnDisk = false;

      // create our temp version
      PSContentEditorSharedDef resultDef = null;

      File[] defs = getContentEditorSharedDefFiles();

      // no directory or no files in the directory so return an empty shared def
      if (null == defs || defs.length == 0)
      {
         m_SharedDefFilesAndModifedDates.clear();
         return new PSContentEditorSharedDef();
      }

      /*
       * The following loop is performed irrespective of whether the flag
       * bModifiedOnDisk is set to true since we need to update the map
       * m_SharedDefFilesAndModifedDates, if required.
       */
      File curFile = null;
      String curFilePath = null;
      long dateTimeCurrent = 0;
      long dateTime = 0;
      long size = 0;
      long sizeCurrent = 0;
      Vector filesOnDisk = new Vector(defs.length);
      for (int i = 0; i < defs.length; i++)
      {
         curFile = new File(m_cfgDirShared, defs[i].getName());
         curFilePath = curFile.getPath();
         filesOnDisk.add(i, curFilePath);
         dateTimeCurrent = curFile.lastModified();
         sizeCurrent = curFile.length();
         if (m_SharedDefFilesAndModifedDates.containsKey(curFilePath))
         {
            PSPair timeAndSize = (PSPair) m_SharedDefFilesAndModifedDates
                  .get(curFilePath);
            dateTime = ((Long) timeAndSize.getFirst()).longValue();
            size = ((Long) timeAndSize.getSecond()).longValue();
            if (dateTimeCurrent != dateTime || size != sizeCurrent)
            {
               m_SharedDefFilesAndModifedDates.put(curFilePath, new PSPair(
                     new Long(curFile.lastModified()), new Long(curFile
                           .length())));
               isModifiedOnDisk = true;
            }
         }
         else
         {
            m_SharedDefFilesAndModifedDates.put(curFilePath,
                  new PSPair(new Long(curFile.lastModified()), new Long(
                        curFile.length())));
            isModifiedOnDisk = true;
         }
      }

      /*
       * Loop to see if any file on disk is deleted and cleanup the mess
       */
      if (m_contentEditorSharedDef != null)
      {
         Iterator fgIter = m_contentEditorSharedDef.getFieldGroups();
         Set<String> filesInDef = new HashSet<String>();
         while (fgIter.hasNext())
         {
            PSSharedFieldGroup fg = (PSSharedFieldGroup) fgIter.next();
            filesInDef.add(new File(m_cfgDirShared, fg.getFilename())
                  .getPath());
         }
         Iterator iter = filesInDef.iterator();
         while (iter.hasNext())
         {
            if (!filesOnDisk.contains(iter.next()))
            {
               m_SharedDefFilesAndModifedDates.remove(curFilePath);
               isModifiedOnDisk = true;
            }
         }
      }
      filesOnDisk = null;

      // No file on the disk is modified, return whatever we had
      if (!isModifiedOnDisk && m_contentEditorSharedDef != null)
         return m_contentEditorSharedDef;
      /*
       * At least one of the files in directory is not in sync, so build the
       * object from scratch.
       */
      for (int i = 0; i < defs.length; i++)
      {
         // load the def file and create an xml doc
         PSContentEditorSharedDef tmpDef = getContentEditorSharedDef(defs[i]
               .getName());

         // merge it into the def to return
         if (resultDef == null)
            resultDef = tmpDef;
         else
         {
            // first copy app flow
            PSApplicationFlow resAppFlow = resultDef.getApplicationFlow();
            PSApplicationFlow curAppFlow = tmpDef.getApplicationFlow();
            if (resAppFlow != null)
            {
               if (curAppFlow != null)
               {
                  Iterator flows = curAppFlow.getCommandHandlerNames();
                  while (flows.hasNext())
                  {
                     String name = (String) flows.next();
                     PSCollection coll = new PSCollection(curAppFlow
                           .getRedirects(name));
                     resAppFlow.addRedirects(name, coll);
                  }
               }
            }
            else if (curAppFlow != null)
               resultDef.setApplicationFlow(curAppFlow);

            // copy field groups
            Iterator resFields = resultDef.getFieldGroups();
            PSCollection curFields = new PSCollection(tmpDef.getFieldGroups());
            if (resFields.hasNext())
            {
               if (curFields != null)
               {
                  PSCollection resFieldColl = new PSCollection(resFields);
                  resFieldColl.addAll(curFields);
                  resultDef.setFieldGroups(resFieldColl);
               }
            }
            else if (curFields != null)
               resultDef.setFieldGroups(curFields);

            // copy stylesheets
            PSCommandHandlerStylesheets resSheets = resultDef
                  .getStylesheetSet();
            PSCommandHandlerStylesheets curSheets = tmpDef.getStylesheetSet();
            if (resSheets != null)
            {
               if (curSheets != null)
               {
                  Iterator sheets = curSheets.getCommandHandlerNames();
                  while (sheets.hasNext())
                  {
                     String name = (String) sheets.next();
                     resSheets.addStylesheets(name, new PSCollection(curSheets
                           .getStylesheets(name)));
                  }
               }
            }
            else if (curSheets != null)
               resultDef.setStylesheetSet(curSheets);
         }

         // now set the permanent version to the one we've created
         m_contentEditorSharedDef = resultDef;

      }

      return m_contentEditorSharedDef;
   }

   /**
    * Gets the shared def contained in specified file.
    * 
    * @param fileName The file, may not be <code>null</code>.
    * 
    * @return The shared def, never <code>null</code>.
    * 
    * @throws IOException if there are any errors loading the file.
    * @throws PSUnknownDocTypeException if the file is not an XML file.
    * @throws SAXException if the Xml cannot be parsed.
    * @throws PSUnknownNodeTypeException if the Xml in the file is malformed.
    */
   public PSContentEditorSharedDef getContentEditorSharedDef(String fileName)
      throws IOException, SAXException, PSUnknownDocTypeException,
      PSUnknownNodeTypeException
   {
      if (fileName == null)
         throw new IllegalArgumentException("fileName may not be null");

      InputStream in = null;
      File cfgFile = new File(m_cfgDirShared, fileName);
      try
      {
         in = m_objectStoreHandler.lockInputStream(cfgFile);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSContentEditorSharedDef def = new PSContentEditorSharedDef(doc);

         if (def.getApplicationFlow() != null)
            def.getApplicationFlow().setFilename(fileName);

         if (def.getStylesheetSet() != null)
            def.getStylesheetSet().setFilename(fileName);

         Iterator fieldGroups = def.getFieldGroups();
         while (fieldGroups.hasNext())
         {
            PSSharedFieldGroup group = (PSSharedFieldGroup) fieldGroups.next();
            group.setFilename(fileName);
         }

         return def;
      }
      finally
      {
         if (in != null)
         {
            try
            {
               m_objectStoreHandler.releaseInputStream(in, cfgFile);
            }
            catch (IOException e)
            {
            }
         }
      }

   }

   /**
    * Get a list of content editor shared def files, does not include the path.
    * 
    * @return The files, may be <code>null</code> or empty if no files exist.
    */
   public File[] getContentEditorSharedDefFiles()
   {
      FileFilter filter = new FileFilter()
      {
         public boolean accept(File f)
         {
            return (f.isFile() && f.canRead()
                  && f.getName().length() > XML_FILE_EXTENSION.length() && f
                  .getName().toLowerCase().endsWith(XML_FILE_EXTENSION));
         }
      };

      File[] files = m_cfgDirShared.listFiles(filter);
      File[] defFiles = null;
      if (files != null)
      {
         defFiles = new File[files.length];
         for (int i = 0; i < files.length; i++)
         {
            defFiles[i] = new File(files[i].getName());
         }
      }

      return defFiles;
   }

   /**
    * Saves the supplied content editor shared def to a file.
    * 
    * @param sharedDef The def to save, may not be <code>null</code>.
    * @param name The name of the file to use, may not be <code>null</code> or
    * empty, must have a ".xml" extension.
    * 
    * @throws IOException if there is an error saving the file.
    */
   public void saveContentEditorSharedDefFile(
         PSContentEditorSharedDef sharedDef, String name) throws IOException
   {
      if (sharedDef == null)
         throw new IllegalArgumentException("sharedDef may not be null");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      if (!name.endsWith(".xml"))
         throw new IllegalArgumentException("invalid name");

      File defFile = new File(m_cfgDirShared, name);
      OutputStream out = null;

      try
      {
         out = m_objectStoreHandler.lockOutputStream(defFile);
         PSXmlDocumentBuilder.write(sharedDef.toXml(), out);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               m_objectStoreHandler.releaseOutputStream(out, defFile);
            }
            catch (IOException e)
            {
            }

         }
      }
   }

   /**
    * Save the supplied merged shared definition into the correct files as
    * specified for each application flow, stylesheet set and field groups
    * element.
    * 
    * @param def the shared definition to save, not <code>null</code>.
    * @throws IOException if there is an error saving the files.
    */
   public void saveContentEditorSharedDefFile(PSContentEditorSharedDef def)
      throws IOException
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      Map<String, PSContentEditorSharedDef> sharedDefs = new HashMap<String, PSContentEditorSharedDef>();

      // put the application flow into the correct file
      PSApplicationFlow applicationFlow = def.getApplicationFlow();
      if (applicationFlow != null)
      {
         String filename = applicationFlow.getFilename();
         if (StringUtils.isBlank(filename))
            throw new IllegalArgumentException(
                  "missing filename in merged shared definition application flow");

         PSContentEditorSharedDef sharedDef = sharedDefs.get(filename);
         if (sharedDef == null)
         {
            sharedDef = new PSContentEditorSharedDef();
            sharedDefs.put(filename, sharedDef);
         }

         sharedDef.setApplicationFlow(applicationFlow);
      }

      // put the stylesheets into the correct file
      PSCommandHandlerStylesheets stylesheets = def.getStylesheetSet();
      if (stylesheets != null)
      {
         String filename = stylesheets.getFilename();
         if (StringUtils.isBlank(filename))
            throw new IllegalArgumentException(
                  "missing filename in merged shared definition stylesheets");

         PSContentEditorSharedDef sharedDef = sharedDefs.get(filename);
         if (sharedDef == null)
         {
            sharedDef = new PSContentEditorSharedDef();
            sharedDefs.put(filename, sharedDef);
         }

         sharedDef.setStylesheetSet(stylesheets);
      }

      // create the field group mappings
      Map<String, PSCollection> fieldGroupMappings = new HashMap<String, PSCollection>();

      Iterator fieldGroups = def.getFieldGroups();
      while (fieldGroups.hasNext())
      {
         PSSharedFieldGroup fieldGroup = (PSSharedFieldGroup) fieldGroups
               .next();

         String filename = fieldGroup.getFilename();
         if (StringUtils.isBlank(filename))
            throw new IllegalArgumentException(
                  "missing filename in merged shared definition field group");

         PSCollection fieldGroupMapping = fieldGroupMappings.get(filename);
         if (fieldGroupMapping == null)
         {
            fieldGroupMapping = new PSCollection(PSSharedFieldGroup.class);
            fieldGroupMappings.put(filename, fieldGroupMapping);
         }

         fieldGroupMapping.add(fieldGroup);
      }

      // put the field group mappings into the correct file
      Iterator filenames = fieldGroupMappings.keySet().iterator();
      while (filenames.hasNext())
      {
         String filename = (String) filenames.next();

         PSContentEditorSharedDef sharedDef = sharedDefs.get(filename);
         if (sharedDef == null)
         {
            sharedDef = new PSContentEditorSharedDef();
            sharedDefs.put(filename, sharedDef);
         }

         sharedDef.setFieldGroups(fieldGroupMappings.get(filename));
      }

      // save all shared definitions
      filenames = sharedDefs.keySet().iterator();
      Set<String> newFiles = new HashSet<String>();
      while (filenames.hasNext())
      {
         String filename = (String) filenames.next();
         saveContentEditorSharedDefFile(sharedDefs.get(filename), filename);
         newFiles.add(filename);
      }
      // Let us delete the shared def files that were not submitted -> deleted
      File[] existing = getContentEditorSharedDefFiles();
      for (int i = 0; i < existing.length; i++)
      {
         File f = existing[i];
         // Does every shared file on the disk exists in the set submitted?
         if (!newFiles.contains(f.getName()))
         {
            File defFile = new File(m_cfgDirShared, f.getName());
            // no - try deleting, if fails log to console
            if (!defFile.delete())
            {
               PSConsole.printMsg("ObjectStore",
                     "Failed to delete the shared def file: " + f.getName());
            }
         }
      }
   }

   /**
    * Creates an array of application summary objects. If the user has read
    * access to the application, or is a server administrator, they will be
    * shown the application in the summaries. However, an attempt to access the
    * design of an application they are not readers on will fail.
    * 
    * @param tok the security token to use for security, may not be
    * <code>null</code>.
    * @param showHiddenApps <code>true</code> to include hidden applications
    * in the results, <code>false</code> to exclude them.
    * 
    * @return an array of PSApplicationSummary objects which the user is allowed
    * to see, never <code>null</code>, may be empty, sorted ascending by
    * application name.
    */
   @SuppressWarnings("unchecked")
   public PSApplicationSummary[] getApplicationSummaryObjects(
         PSSecurityToken tok, boolean showHiddenApps)
   {
      boolean isServerAdmin;
      try
      {
         PSServer.checkAccessLevel(tok, PSAclEntry.SACE_ADMINISTER_SERVER);

         isServerAdmin = true;
      }
      catch (Exception e)
      {
         /*
          * obviously they're not an admin (threw PSAuthorizationException or
          * PSAuthenticationRequiredException)
          */
         isServerAdmin = false;
      }

      List retSums = new java.util.ArrayList();

      synchronized (m_objectStoreHandler.m_appSums)
      {
         PSApplicationSummary[] sums = m_objectStoreHandler.m_appSums
               .getSummaries();
         for (int i = 0; i < sums.length; i++)
         {
            PSApplicationSummary sum = sums[i];

            if (!showHiddenApps && sum.isHidden())
               continue;

            try
            {
               if (!isServerAdmin)
                  checkCanReadApplication(sum.getName(), tok, false);
               retSums.add(sum);
            }
            catch (Exception e)
            {
               // no matter what the error, we'll just skip the bad summary
            }
         }
      }

      PSApplicationSummary[] retArray = new PSApplicationSummary[retSums
            .size()];
      if (retSums.size() != 0)
      {
         retSums.toArray(retArray);
         PSSortTool.MergeSort(retArray, PSApplicationSummary.getComparator());
      }
      return retArray;
   }

   /**
    * Walks this fieldset and performs the following operations on the fields
    * that it finds, including read-only fields (performing a recursive search):
    * <ul>
    * <li>Find the JDBC type of the column for each field associated with the
    * database and validate the field datatype with that value. </li>
    * <li>For each field that isn't associated with a db column, look at the
    * replacement value. If it is derived from PSLiteral, the data type will be
    * set based on the derived class. </li>
    * <li>For each field that isn't associated with a db column, look at the
    * replacement value. If it is a PSExtensionCall, all of its parameters are
    * checked. If any of them are PSBackEndColumn, they are fixed up in the
    * standard way. See {@link PSFieldSet#fixupBackEndColumns(Map)}. </li>
    * <li>Otherwise, the data type will not be set on the field. We could set
    * it to text, but by leaving it blank, the user can infer that we don't know
    * and can still use text if they want. </li>
    * <li>If the mime type of the field is empty, a mime type is set based on
    * the data type. For all except binary, the mime type will be set to
    * text/plain. For binary, it will be set to application/octet-stream. </li>
    * </ul>
    * 
    * @param fs the set to fixup, never <code>null</code>.
    * 
    * @param tableSets An iterator over PSTableSet objects for this content
    * editor (assumes at least 1). Never <code>null</code>. If there are no
    * elements, the method returns without changing the fieldSet.
    * 
    * @param tables A list of all tables defined in this editor. Each key is the
    * table alias (lowercased) and each value is the PSBackEndTable that has all
    * properties properly specified. The Map is treated read-only.
    */
   @SuppressWarnings("unchecked")
   public static void fixupFields(PSFieldSet fs, Iterator tableSets, Map tables)
      throws SQLException
   {
      if (null == fs)
         throw new IllegalArgumentException("Fieldset must be supplied.");
      if (null == tableSets)
         throw new IllegalArgumentException("Table sets must be supplied.");

      PSField[] rFields = fs.getAllFields(false);
      PSField[] roFields = fs.getAllFields(true);
      PSField[] allFields = new PSField[rFields.length + roFields.length];
      int j = 0;
      for (int i = 0; i < rFields.length; i++)
      {
         allFields[j++] = rFields[i];
      }
      for (int i = 0; i < roFields.length; i++)
      {
         allFields[j++] = roFields[i];
      }

      Collection beFields = new ArrayList();

      for (int i = 0; i < allFields.length; i++)
      {
         if (allFields[i].getLocator() instanceof PSBackEndColumn)
            beFields.add(allFields[i]);
         else
         {
            // set the data type if we know it, otherwise, leave blank
            if (allFields[i].getLocator() instanceof PSDateLiteral)
               allFields[i].setDataType(PSField.DT_DATETIME);
            else if (allFields[i].getLocator() instanceof PSNumericLiteral)
               allFields[i].setDataType(PSField.DT_INTEGER);
            else if (allFields[i].getLocator() instanceof PSTextLiteral)
               allFields[i].setDataType(PSField.DT_TEXT);
            else if (allFields[i].getLocator() instanceof PSExtensionCall)
            {
               /*
                * I was debating where this code belongs and settled on her.
                * It's not the ideal location, but I didn't see a better one.
                */
               PSExtensionParamValue[] params = ((PSExtensionCall) allFields[i]
                     .getLocator()).getParamValues();
               for (int k = 0; k < params.length; k++)
               {
                  if ((null != params[k]) && (params[k].isBackEndColumn()))
                  {
                     PSBackEndColumn col = (PSBackEndColumn) params[k]
                           .getValue();
                     PSBackEndTable table = (PSBackEndTable) tables.get(col
                           .getTable().getAlias().toLowerCase());
                     if (null == table)
                     {
                        PSSystemValidationException e = new PSSystemValidationException(
                              IPSObjectStoreErrors.BE_TABLE_NULL);
                        throw new SQLException(e.getLocalizedMessage());
                     }
                     try
                     {
                        col.setTable(table);
                     }
                     catch (IllegalArgumentException iae)
                     {
                        throw new IllegalArgumentException(iae
                              .getLocalizedMessage());
                     }
                  }
               }
            }
         }
      }

      // get the meta data for each table set
      Collection meta = new ArrayList();
      while (tableSets.hasNext())
      {
         PSTableSet ts = (PSTableSet) tableSets.next();
         meta.add(PSMetaDataCache.getCachedDatabaseMetaData(ts));
      }

      if (meta.size() == 0)
         return;

      PSDatabaseMetaData[] dbMeta = new PSDatabaseMetaData[meta.size()];
      meta.toArray(dbMeta);

      // walk all the fields and look up the db field type
      Iterator itFields = beFields.iterator();

      boolean searchEnabled = PSServer.getServerConfiguration()
            .getSearchConfig().isFtsEnabled();
      while (itFields.hasNext())
      {
         PSField field = (PSField) itFields.next();
         PSBackEndColumn col = (PSBackEndColumn) field.getLocator();
         String colName = col.getColumn();
         for (int i = 0; i < dbMeta.length; i++)
         {
            PSBackEndTable t = col.getTable();
            String tableName = t.getTable();
            if (tableName == null)
               tableName = t.getAlias();

            PSMetaDataCache.loadConnectionDetail(t);
            PSTableMetaData tableMeta = dbMeta[i].getTableMetaData(t
                  .getConnectionDetail().getOrigin(), tableName);
            if (null != tableMeta)
            {
               tableMeta.loadDataTypes(col.getTable().getAlias());
               field.fixupDataType(tableMeta.getColumnType(colName), tableMeta
                     .getSize(colName), tableMeta.getScale(colName), tableMeta
                     .getTypeName(colName), searchEnabled);
            }
         }
      }

      // now fixup the mime types as best we can
      for (int i = 0; i < allFields.length; i++)
      {
         String mimeType = allFields[i].getMimeType();
         if (null == mimeType)
         {
            if (allFields[i].getDataType().equals(PSField.DT_BINARY))
               allFields[i].setMimeType("application/octet-stream");
            else
               allFields[i].setMimeType("text/plain");
         }
      }
   }

   /**
    * Get all of the current role and subject cataloger configurations. Note
    * that these are not the active instances of the catalogers, but merely the
    * object representation of their configurations.
    * 
    * @param lockId Used to load the configs locked for editing, may be
    * <code>null</code> to get them for read-only use.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @return The list of configurations, never <code>null</code>, may be
    * empty.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public List<PSCatalogerConfig> getCatalogerConfigs(IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSServerException, PSNotLockedException, PSLockedException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      if (lockId != null)
         extendServerConfigLock(lockId);

      try
      {
         return PSRoleMgrLocator.getRoleManager().getCatalogerConfigs();
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Save the supplied cataloger configurations. Note that these represent only
    * the configurations, and they will not result in active subject or role
    * cataloger instances until the server is restarted.
    * 
    * @param configs The configs to save, may be empty, never <code>null</code>.
    * @param lockId The lock id to use, may not be <code>null</code>. The
    * server config must already be locked with the supplied id.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public void saveCatalogerConfigs(List<PSCatalogerConfig> configs,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSServerException, PSNotLockedException, PSLockedException
   {
      if (configs == null)
         throw new IllegalArgumentException("resolver may not be null");

      if (lockId == null)
         throw new IllegalArgumentException("lockerId may not be null");

      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      extendServerConfigLock(lockId);

      try
      {
         PSRoleMgrLocator.getRoleManager().saveCatalogerConfigs(configs);
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Loads the object representations of the JNDI datasource configurations.
    * 
    * @param lockId Used to load the datasources locked for editing, may be
    * <code>null</code> to get them for read-only use.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @return The datasources, never <code>null</code>, may be empty.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public List<IPSJndiDatasource> getJndiDatasources(IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSServerException, PSNotLockedException, PSLockedException,
      PSAuthenticationRequiredException, PSAuthorizationException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      if (lockId != null)
         extendServerConfigLock(lockId);
      return getJndiDatasources(lockId != null);
   }

   /**
    * Utility method to get the JNDI datasources, intended for use by the
    * {@link PSXmlObjectStoreHandler} class. Other classes should use the
    * {@link #getJndiDatasources(IPSLockerId, PSSecurityToken)} method.
    * @param fromDisk 
    * 
    * @return The datsources, never <code>null</code>, may be empty.
    * 
    * @throws PSServerException If there are any errors.
    */
   static List<IPSJndiDatasource> getJndiDatasources(boolean fromDisk) throws PSServerException
   {
      try
      {
         return PSContainerUtilsFactory.getInstance().getDatasources();
      }
      catch (Exception e)
      {
         ms_log.error("Error getting datasource ",e);
         throw new PSServerException(e);
      }
   }

   /**
    * Saves the supplied JNDI datasource configurations replacing any existing
    * configurations.
    * 
    * @param datasources The configurations to save, may not be
    * <code>null</code> or empty.
    * @param lockId The lock id to use, may not be <code>null</code>. The
    * server config must already be locked with the supplied id.
    * @param tok The security token to use.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public void saveJndiDatasources(List<IPSJndiDatasource> datasources,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSServerException, PSNotLockedException, PSLockedException,
      PSAuthenticationRequiredException, PSAuthorizationException
   {
      if (datasources == null || datasources.isEmpty())
         throw new IllegalArgumentException(
               "datasources may not be null or empty");
      if (lockId == null)
         throw new IllegalArgumentException("lockerId may not be null");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      extendServerConfigLock(lockId);

      try
      {
          DefaultConfigurationContextImpl configContext = PSContainerUtilsFactory.getConfigurationContextInstance();
          configContext.getConfig().setDatasources(datasources);
          configContext.save();
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Get the datasource resolver containing the datasource connection
    * configurations.
    * 
    * @param lockId Used to load the configs locked for editing, may be
    * <code>null</code> to get them for read-only use.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @return The resolver that contains the list of configs, never
    * <code>null</code>, may be empty.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public IPSDatasourceResolver getDatasourceConfigs(IPSLockerId lockId,
         PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSServerException, PSNotLockedException, PSLockedException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      if (lockId != null)
         extendServerConfigLock(lockId);
      return getDatasourceResolver();
   }

   /**
    * Utility method to get the datasource resolver, only intended for use by
    * the {@link PSXmlObjectStoreHandler} class. Other classes should use the
    * {@link #getDatasourceConfigs(IPSLockerId, PSSecurityToken)} method.
    * 
    * @return The resolver, never <code>null</code>.
    * 
    * @throws PSServerException If there are any errors.
    */
   static IPSDatasourceResolver getDatasourceResolver()
      throws PSServerException
   {
      try
      {
           return PSContainerUtilsFactory.getInstance().getDatasourceResolver();
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Save the datasource resolver containing the datasource connection
    * configurations.
    * 
    * @param resolver The datasource resolver, may not be <code>null</code>
    * and must contain at least one datasource config and have the repository
    * set.
    * @param lockId The lock id to use, may not be <code>null</code>. The
    * server config must already be locked with the supplied id.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public void saveDatasourceConfigs(IPSDatasourceResolver resolver,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSServerException, PSNotLockedException, PSLockedException
   {
      if (resolver == null)
         throw new IllegalArgumentException("resolver may not be null");
      if (resolver.getDatasourceConfigurations().isEmpty()
            || StringUtils.isBlank(resolver.getRepositoryDatasource()))
      {
         throw new IllegalArgumentException("invalid resolver configuration");
      }
      if (lockId == null)
         throw new IllegalArgumentException("lockerId may not be null");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      extendServerConfigLock(lockId);

      try
      {
        PSContainerUtilsFactory.getInstance().setDatasourceResolver(resolver);
        PSContainerUtilsFactory.getConfigurationContextInstance().save();

      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Load the object representation hibernate dialect config.
    * 
    * @param lockId Used to load the config locked for editing, may be
    * <code>null</code> to get it for read-only use.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @return The config, never <code>null</code>.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public PSHibernateDialectConfig getHibernateDialectConfig(
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSServerException, PSNotLockedException, PSLockedException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      if (lockId != null)
         extendServerConfigLock(lockId);
      try
      {
         File configFile = new File(PSServletUtils.getSpringConfigDir(),
               PSServletUtils.SERVER_BEANS_FILE_NAME);
         PSSpringConfiguration springConfig = new PSSpringConfiguration(
               configFile);

         return (PSHibernateDialectConfig) springConfig
               .getBean(PSServletUtils.HIBERNATE_DIALECTS_NAME);
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Saves the hibernate dialect configuration.
    * 
    * @param config The config to save, may not be <code>null</code> or empty.
    * @param lockId The lock id to use, may not be <code>null</code>. The
    * server config must already be locked with the supplied id.
    * @param tok The security token to use, may not be <code>null</code>.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If a <code>lockId</code> is supplied but
    * the lock is not held by that id.
    * @throws PSAuthorizationException If the user represented by the token is
    * not authorized to perform this action.
    * @throws PSAuthenticationRequiredException If the token represents an
    * unauthenticated session.
    * @throws PSServerException If there are any other errors.
    */
   public void saveHibernateDialectConfig(PSHibernateDialectConfig config,
         IPSLockerId lockId, PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException,
      PSServerException, PSNotLockedException, PSLockedException
   {
      if (config == null || config.getDialects().isEmpty())
         throw new IllegalArgumentException("config may not be null or empty");
      if (lockId == null)
         throw new IllegalArgumentException("lockerId may not be null");
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      checkCanEditServerConfig(tok);

      // Check to see if config is locked
      extendServerConfigLock(lockId);

      try
      {
         File configFile = new File(PSServletUtils.getSpringConfigDir(),
               PSServletUtils.SERVER_BEANS_FILE_NAME);
         PSSpringConfiguration springConfig = new PSSpringConfiguration(
               configFile);
         springConfig.setBean(config);
         springConfig.save();
      }
      catch (Exception e)
      {
         throw new PSServerException(e);
      }
   }

   /**
    * Ensures the supplied lock id holds the server config lock, and then
    * extends it by 30 minutes.
    * 
    * @param lockId The lock id, assumed not <code>null</code>.
    * 
    * @throws PSLockedException If an attempt to extend the lock fails.
    * @throws PSNotLockedException If held by the supplied id.
    * @throws PSServerException
    */
   private void extendServerConfigLock(IPSLockerId lockId)
      throws PSServerException, PSNotLockedException, PSLockedException
   {
      if (!isServerConfigLocked(lockId))
      {
         throw new PSNotLockedException(IPSObjectStoreErrors.LOCK_NOT_HELD,
               "Server Configuration");
      }
      else
      {
         // extend the lock to be sure we keep it
         getServerConfigLock(lockId, 30);
      }
   }

   /**
    * Get a file reference to the directory for the specified app root.
    * 
    * @param appRoot The name of the application root directory, Assumed not
    * <code>null</code> or empty.
    * 
    * @return The file reference, never <code>null</code>.
    */
   protected static File getAppRootDir(String appRoot)
   {
      return new File(PSServer.getRxDir(), appRoot);
   }

   private static final String XML_FILE_EXTENSION = ".xml";

   /**
    * Stores the file path as key and <code>PSPair</code> of last modified
    * date and length of the file as value.
    */
   private HashMap m_SharedDefFilesAndModifedDates = new HashMap();

   /**
    * The object containing the system wide definitions for content editors.
    * Initialized by the first call to {@link #getContentEditorSystemDef()}.
    */
   private PSContentEditorSystemDef m_contentEditorSystemDef = null;

   /**
    * Date time value of the system def file's modfied data. This shall be
    * compared with file's current modified date time to see if the
    * <code>PSContentEditorSystemDef</code> needs to be reconstructed from the
    * newer file from the disk. Modifed by the first call to
    * {@link #getContentEditorSystemDef()}.
    */
   private long m_ModifiedDateTimeSystemDef = 0;

   /**
    * The object containing the shared definitions for content editors.
    * Initialized by the first call to {@link #getContentEditorSharedDef()}.
    */
   private PSContentEditorSharedDef m_contentEditorSharedDef = null;

   /**
    * Constant for the directory containing content management configurations.
    * Assumed to be relative to the Rx root directory.
    */
   private static final String CFG_DIR = "rxconfig";

   /**
    * Constant for the directory containing content editor configurations.
    * Assumed to be relative to the {@link #CFG_DIR} directory.
    */
   private static final String CMS_DIR = "Server/ContentEditors";

   /**
    * Constant for the directory containing content editor shared configs.
    * Assumed to be relative to the {@link #CMS_DIR} directory.
    */
   private static final String CMS_SHARED_DIR = "shared";

   /**
    * Reference to the File object of the System Def file. nitialized in ctor
    */
   private File m_cfgFileSystemDef;

   /**
    * Reference to the File object of the Shared Def file Directory. I
    */
   private File m_cfgDirShared;

   /**
    * The single instance of this class. <code>null</code> until {@link
    * #createInstance(PSXmlObjectStoreHandler)} is called, never
    * <code>null</code> after that.
    */
   private static PSServerXmlObjectStore ms_objectStore = null;

   /**
    * The objectstore handler. Reference is required until no dependency on that
    * object no longer exists. <code>null</code> until {@link
    * #createInstance(PSXmlObjectStoreHandler)} is called, never
    * <code>null</code> after that.
    */
   private PSXmlObjectStoreHandler m_objectStoreHandler = null;

   /**
    * Constant to use as identifying object when locking the server config.
    */
   private static final String SERVER_CONFIG_LOCK_ID = "~Server_Config";

   /**
    * The logger for this class.
    */
   private final static Log ms_log = LogFactory
         .getLog(PSServerXmlObjectStore.class);
}
