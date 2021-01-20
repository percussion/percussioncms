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

package com.percussion.data.vfs;

import com.percussion.server.PSUserSession;

import java.io.File;

/**
 * Represents a virtual application directory.
 */
public interface IPSVirtualDirectory
{
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
   public File getPhysicalPath(File relPath);

   /**
    * Get the actual directory this virtual directory represents.
    * 
    * @return The directory, may be <code>null</code> if the application exists
    * but has no associated directory.  
    */
   public File getPhysicalLocation();
   
   /**
    * Returns true if all the permissions are held.
    * 
    * @author chad loder
    * 
    * @version 1.0 1999/7/15
    * 
    * @param session The user session whose permissions should be returned.
    * 
    * @param permissions The permissions from PSAclEntry
    * @return boolean <CODE>true</CODE> if all the permissions are held by
    * this session for this virtual directory.
    */
   public boolean hasPermissions(PSUserSession session, int permissions);

   /**
    * Gets the name of the virtual directory that this object represents.
    * 
    * @author chad loder
    * 
    * @version 1.0 1999/7/14
    * 
    * @return String
    */
   public String getVirtualDirectory();

}
