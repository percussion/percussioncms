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
