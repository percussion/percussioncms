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

package com.percussion.rest.folders;

import com.percussion.rest.errors.BackendException;

import java.net.URI;

public interface IFolderAdaptor
{
    public Folder getFolder(URI baseURI, String site, String path, String folderName) throws BackendException;

    public Folder updateFolder(URI baseURI, Folder folder) throws BackendException;

    public void deleteFolder(URI baseURI, String siteName, String path, String folderName, boolean includeSubFolders) throws BackendException;

    public Folder getFolder(URI baseURI, String id) throws BackendException;
    
    public void moveFolderItem(URI baseURI, String itemPath, String targetFolderPath) throws BackendException;
    
    public void moveFolder(URI baseURI, String folderPath, String targetFolderPath) throws BackendException;
    
    public Folder renameFolder(URI baseURI, String site, String path, String folderName, String newName) throws BackendException;

    public void copyFolderItem(URI baseURI, String itemPath, String targetFolderPath) throws Exception;

    public void copyFolder(URI baseURI, String folderPath, String targetFolderPath) throws Exception;

    public void deleteFolderItem(URI baseURI, String itemPath) throws BackendException;

}
