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

package com.percussion.data.vfs;

import com.percussion.conn.PSServerException;
import com.percussion.data.IPSDataErrors;
import com.percussion.design.objectstore.PSNotFoundException;
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
