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
package com.percussion.designmanagement.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A file system service which receives a root folder path and manages file system
 * operations over it. It uses paths similar to the ones used in the Finder. All
 * these paths are relative to the root folder path.
 * 
 * @author miltonpividori
 *
 */
public interface IPSFileSystemService
{
    String getRootFolderPath();
    
    void setRootFolderPath(String rootFolderPath);
    
    /**
     * Given a path, it returns a File object pointing to that file, relative to the
     * root folder path. It doesn't check whether the file exists or not.
     * <p>
     * Note that the paths handled by implementations of this interfaces are not
     * supposed to handle internal name mappings. So paths must directly point to
     * the file system.
     * 
     * @param path A file path relative to the root folder. Cannot be <code>null</code>.
     * @return A File object pointing to the file path given. Never <code>null</code>.
     */
    File getFile(String path);
    
    /**
     * Given a File object, it returns its name. It takes care of cases where
     * the File object was created with a name like "/", then it automatically
     * returns the name of the parent File.
     * 
     * @param file A File object. Cannot be <code>null</code>.
     * @return The name of the File object. Never <code>null</code>.
     */
    String getNameFromFile(File file);
    
    /**
     * Given a folder path, it gets the children and returns a List of File objects.
     * 
     * @param path The path of the folder. Assumed not <code>null</code>.
     * @return A list of children of the folder path represented by 'path'.
     * @throws IOException 
     */
    List<File> getChildren(String path) throws FileNotFoundException;
    
    /**
     * Adds a new folder with a fixed name specified by the implementation class (for example,
     * "New Folder") in the parent folder given as an argument. So after calling this method
     * successfully, a new folder with that name is created. A {@link File} object is returned
     * pointing to the newly created folder.
     * <p>
     * Note that subsequent calls using the same path will not file. If the folder name prefix
     * specified by the implementation is, for instance, "New Folder", a second call will create
     * a folder with the name "New Folder 1". The number is incremented in subsequent calls. 
     * 
     * @param parentPath Parent folder path. Cannot be <code>null</code>.
     * @return A File object pointing to the newly created folder. Never <code>null<code>.
     * @throws IOException
     */
    File addFolder(String parentPath) throws IOException;
    
    /**
     * Renames a folder. It receives the full folder path that will be renamed, and the new
     * folder name.
     * <p>
     * The implementation class should check at least three things when renaming a folder, and
     * return the following exceptions when the condition is met:
     * <ul>
     *  <li>Max folder name length: {@link PSFolderNameLengthLimitException}</li>
     *  <li>Invalid folder name: {@link PSInvalidFolderNameException}</li>
     *  <li>Destination folder already exists: {@link PSExistingFolderException}</li>
     * </ul>
     * 
     * @param oldFolderPath The full folder path to be renamed. Cannot be <code>null</code>.
     * @param newFolderName New folder name. Cannot be <code>null</code>.
     * @return A File object pointing to the newly renamed folder.
     * @throws PSFolderOperationException
     */
    File renameFolder(String oldFolderPath, String newFolderName) throws PSFolderOperationException;
    
    /**
     * Handles the deletion of a file. It deletes the given file from the
     * filesystem.
     * 
     * @param path the path of the given file to remove. Cannot be <code>null</code>.
     * 
     * @throws PSFileOperationException if the file could not be deleted.
     */
    void deleteFile(String path) throws PSFileOperationException;
    
    /**
     * Handles the deletion of a folder. It deletes the given folder from the
     * filesystem.
     * 
     * @param folderPath the path of the given folder to remove. Cannot be <code>null</code>.
     * @throws IOException
     */
    void deleteFolder(String folderPath) throws IOException;
    
    /**
     * Gets the parent folder path of the given path.
     * 
     * @param path the path of which we want the parent. Assumed not <code>null</code>
     * 
     * @return a String containing the parent folder path. Never <code>null</code>
     */
    String getParentFolder(String path);
    
    /**
     * If the given name does not exists in the list it returns <code>true</code>
     * 
     * @param name the name we are searching
     * @param files an arrays of strings that represent files and folder names
     * 
     * @return <code>true</code> if none of the strings in the list is equal to the given name. <code>false</code> otherwise 
     */
    boolean foldernameAvailable(String name, String[] files);
    
    /**
     * Gets the new folder name according to those files or folders in the given path. 
     * Iterates over all of the files and folders and compares its names with the prefix. 
     * If it finds the given prefix, it adds a number as the suffix.
     * 
     * @param filesAndFolders a list of strings representing the name of each file or folder 
     *  from which we want to get the new folder name.
     * 
     * @return a String, never <code>null</code>
     */
    String getNewFolderName(String[] filesAndFolders);

    /**
     * Validates that a file with the given path can be uploaded. If there is
     * another file with the same name, it will return an error response, so the
     * UI can show a message. If there is no file named the same, it return
     * success.
     * 
     * @param path the path to the given file.
     * @throws PSFileOperationException if the validation is not passed
     */
    void validateFileUpload(String path) throws PSFileOperationException;

    /**
     * Checks if the string passed as a parameter is a reserved filename, no
     * matter the case it has.
     * 
     * @param name String that holds the name we want to check.
     * @return <code>true</code> if the name is a reserved filename.
     *         <code>false</code>
     */
    boolean isReservedFilename(String name);
    
    /**
     * Handles the upload of a file. Checks if the path exists and saves the
     * file. If another file with the same name already exists, it overwrites
     * it. If the file name is a reserved name, or there is a folder with the
     * same name, it returns an error.
     * 
     * @param path the path to the new file. It includes its name.
     * @param pageContent the file to save
     * @return the response gotten from the logic.
     */
    void fileUpload(String path, InputStream pageContent) throws PSFileOperationException;
    
    /**
     * Checks if the name contains an invalid character.
     * 
     * @param newFolderName the name to check. Assumed not <code>null</code>
     * @return <code>true</code> if the name contains an invalid character.
     *         <code>false</code> otherwise.
     */
    boolean containsInvalidChars(String name);

    
    public static class PSFolderOperationException extends Exception
    {
    }
    
    public static class PSFolderNameLengthLimitException extends PSFolderOperationException
    {
    }
    
    public static class PSInvalidFolderNameException extends PSFolderOperationException
    {
    }
    
    public static class PSExistingFolderException extends PSFolderOperationException
    {
    }
    
    public static class PSInvalidCharacterInFolderNameException extends PSFolderOperationException
    {
        private String invalidChars;
        
        public PSInvalidCharacterInFolderNameException(String chars)
        {
            invalidChars = chars;
        }
        
        public String getInvalidChars()
        {
            return invalidChars;
        }
        
        public void setInvalidChars(String invalidChars)
        {
            this.invalidChars = invalidChars;
        }
    }
    
    public static class PSFileOperationException extends Exception
    {
        public PSFileOperationException(String message, Exception ex)
        {
            super(message, ex);
        }
        
        public PSFileOperationException(String message)
        {
            this(message, null);
        }
    }
    
    public static class PSFileAlreadyExistsException extends PSFileOperationException 
    {
        public PSFileAlreadyExistsException(String message)
        {
            super(message);
        }
    }
    
    public static class PSFileNameInUseByFolderException extends PSFileOperationException
    {
        public PSFileNameInUseByFolderException(String message)
        {
            super(message);
        }
    }

    public static class PSReservedFileNameException extends PSFileOperationException
    {
        public PSReservedFileNameException(String message)
        {
            super(message);
        }
    }

    public static class PSInvalidCharacterInFileNameException extends PSFileOperationException
    {
        public PSInvalidCharacterInFileNameException(String message)
        {
            super(message);
        }
    }

    public static class PSFileSizeExceededException extends PSFileOperationException
    {
        public PSFileSizeExceededException(String message)
        {
            super(message);
        }
    }
}
