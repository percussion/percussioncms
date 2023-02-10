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

package com.percussion.data.vfs;

import com.percussion.conn.PSServerException;
import com.percussion.data.IPSDataErrors;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.server.IPSObjectStoreHandler;
import com.percussion.server.PSConsole;
import com.percussion.server.PSUserSession;

import java.io.File;
import java.io.IOException;

/**
 * See {@link IPSVirtualDirectory} for details.
 */
public class PSVirtualApplicationDirectory implements IPSVirtualDirectory,
      IPSDataErrors
{
   /**
    * Construct a virtual directory entry that answers to the given <CODE>
    * virtualDir</CODE> with an actual physical location of <CODE>appDir
    * </CODE>.
    * 
    * @author chad loder
    * 
    * @version 1.0 1999/7/15
    * 
    * @param virtualDir The virtual directory that this entry "answers to". For
    * applications, this will be the requestRoot.
    * 
    * @param appDir The physical directory where the application's files live.
    * If <CODE>null</CODE>, it means that the application exists but has no
    * associated directory.
    * 
    * @param osHandler The object store handler that this entry can use to
    * obtain dynamic information about itself (security, the files contained
    * within, etc.).
    */
   public PSVirtualApplicationDirectory(String virtualDir, File appDir,
      IPSObjectStoreHandler osHandler)
   {
      // TODO: validate input params
      m_virtualDir = virtualDir;
      m_appRoot = appDir;
      m_osHandler = osHandler;
   }

   /**
    * Gets the physical path that the given file would have within this virtual
    * directory.
    * 
    * If this method returns <CODE>null</CODE>, it means that the application
    * exists but has no associated directory, therefore queries for the
    * directory contents should return an empty list.
    * 
    * @author chad loder
    * 
    * @version 1.0 1999/7/14
    * 
    * 
    * @param relPath
    * 
    * @return File
    */
   public File getPhysicalPath(File relPath)
   {
      if (relPath == null)
         throw new IllegalArgumentException("vfs convert path error" + relPath);

      if (relPath.isAbsolute() || relPath.getName().length() == 0)
         throw new IllegalArgumentException("vfs convert path error"
            + relPath.toString());

      File retVal = null;
      try
      {
         retVal = new File(m_appRoot, relPath.toString()).getCanonicalFile();
      }
      catch (IOException e)
      {
         PSConsole.printMsg("Data", e);
      }
      return retVal;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.data.vfs.IPSVirtualDirectory#getPhysicalLocation()
    */
   public File getPhysicalLocation()
   {
      return m_appRoot;
   }

   /**
    * Returns true if all the permissions are held.
    * 
    * @author chad loder
    * 
    * @version 1.0 1999/7/15
    * 
    * @param session The user session whose permissions should be returned.
    * 
    * @return boolean <CODE>true</CODE> if all the permissions are held by
    * this session for this virtual directory.
    */
   public boolean hasPermissions(PSUserSession session, int permissions)
   {
      boolean has = false;
      if (session != null)
      {
         try
         {
            has = m_osHandler.checkApplicationSecurity(m_virtualDir,
               permissions, session);
         }
         catch (PSNotFoundException e)
         {
            // ignore
         }
         catch (PSServerException e)
         {
            // ignore
         }
      }
      return has;
   }

   /**
    * Gets the name of the virtual directory that this object represents.
    * 
    * @author chad loder
    * 
    * @version 1.0 1999/7/14
    * 
    * @return String
    */
   public String getVirtualDirectory()
   {
      return m_virtualDir;
   }

   private String m_virtualDir;

   private File m_appRoot;

   private IPSObjectStoreHandler m_osHandler;
}
