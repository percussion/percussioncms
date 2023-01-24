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
