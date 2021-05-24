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
package com.percussion.designmanagement.service.impl;

import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.pathmanagement.service.IPSPathService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.trimLeadingCharacter;
import static org.springframework.util.StringUtils.trimTrailingCharacter;

/**
 * An {@link IPSFileSystemService} implementation to handle file system operations,
 * specially under {rxDir}/web_resources (however this is configurable), in a way
 * similar as the Finder works to create pages and folders, i.e. new folders are
 * created with the "New Folder" prefix (like the Finder under "Sites").
 * <p>
 * It supports a list of includes, so only those file names under the root
 * folder are returned.
 * 
 * @author miltonpividori
 * @see IPSFileSystemService
 */
public class PSFileSystemService implements IPSFileSystemService
{
    
    /**
     * Prefix for new folders names.
     */
    public static final String NEW_FOLDER_NAME_PREFIX = "New-Folder";
    
    /**
     * Max length allowed for folder names.
     */
    public static final Integer FOLDER_NAME_MAX_LENGTH = 255;
    
    /**
     * The maximum allowed size for a file. It is configurable via spring.
     */
    public Float maxFileSize;

    /**
     * A list of reserved file names. These ones cannot be used as a folder or file name.
     */
    private static final List<String> RESERVED_FILENAMES = Arrays.asList(new String[]{".", "..", "CON", "PRN", "AUX", 
            "CLOCK$", "NUL", "COM0", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", 
            "LPT0", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"});
    /**
     * A list of characters that can't be used in the file or folder name
     */
    private static final List<Character> INVALID_CHARS = Arrays.asList(new Character[]
    {'/', '\\', ':', '*', '?', '"', '<', '>', '|'});

    /**
     * The root file system path.
     */
    private String rootFolderPath;
    
    /**
     * The root folder File object. It's cached in this field.
     */
    private File rootDirectory;
    
    /**
     * A list of includes file names. This is only applied to the root directory, not sub directories.
     * Only these ones are visible when getting the children of the root directory.
     */
    protected List<String> includes = new ArrayList<>();
    
    public PSFileSystemService(String rootFolderPath)
    {
        this.rootFolderPath = rootFolderPath;
    }
    
    public String getRootFolderPath()
    {
        return rootFolderPath;
    }

    public void setRootFolderPath(String rootFolderPath)
    {
        this.rootFolderPath = rootFolderPath;
        this.rootDirectory = null;
    }
    
    public List<String> getIncludes()
    {
        return includes;
    }
    
    public void setIncludes(List<String> includes)
    {
        this.includes = includes;
    }
    
    public Float getMaxFileSize()
    {
        return maxFileSize;
    }

    public void setMaxFileSize(Float maxFileSize)
    {
        this.maxFileSize = maxFileSize;
    }

    
    /**
     * Gets the root directory of this {@link IPSPathService} implementation. It
     * caches the File object returned.
     * 
     * @return A {@link File} object representing the root directory of this
     * {@link IPSPathService} implementation.
     */
    private File getRootDirectory()
    {
        if (rootDirectory == null) {
            rootDirectory = new File(rootFolderPath);
        }

        return rootDirectory;
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#getChildren(java.lang.String)
     */
    public List<File> getChildren(String path) throws FileNotFoundException
    {
        File root = getRootDirectory();
        File pathFile = new File(root, path);
        
        if (!pathFile.exists()) {
            throw new FileNotFoundException("The path doesn't exist: " + path);
        }

        File[] children = pathFile.listFiles();
        
        // Filter only for root path
        if (includes.isEmpty() || !StringUtils.equals(path, "/")) {
            return Arrays.asList(children);
        }

        List<File> result = new ArrayList<>();
        //FB: NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE NC 1-17-16
        if(children!= null){
	        for (File child : children)
	        {
	            if (includes.contains(child.getName())) {
                    result.add(child);
                }
	        }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemManagerService#getFile(java.lang.String)
     */
    public File getFile(String path)
    {
        Validate.notNull(path, "path must not be null");
        
        return new File(getRootDirectory(), path);
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#addFolder(java.lang.String)
     */
    public File addFolder(String newFolderPath) throws IOException
    {
        Validate.notNull(newFolderPath, "newFolderPath cannot be null");
        
        // first we get the new folder name
        File folderPath = getFile(newFolderPath);
        
        // we need to check if the folderPath is actually a folder or file
        if(folderPath.isFile())
        {
            // the path is a file, we need the parent folder
            folderPath = folderPath.getParentFile();
        }
        
        String newName = getNewFolderName(folderPath.list());
        
        // build the new file
        File newFolder = new File(folderPath.getAbsolutePath(), newName);

        Files.createDirectory(newFolder.toPath());
        File parent = folderPath.getParentFile();
        setParentFolderPermissionsToChild(parent,newFolder);
        
        return newFolder;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#getNewFolderName(java.lang.String[])
     */
    @Override
    public String getNewFolderName(String[] filesAndFolders)
    {
        String regex = NEW_FOLDER_NAME_PREFIX + " [0-9]+";
        int numberOfMatches = -1;
        
        if(filesAndFolders != null)
        {
            for(String file : filesAndFolders)
            {
                if(NEW_FOLDER_NAME_PREFIX.equals(file) && numberOfMatches < 0)
                {
                    numberOfMatches = 0;
                }
                else if (Pattern.matches(regex, file))
                {
                    // get the integer value and see if it is the greatest
                    Integer number = Integer.valueOf(file.substring(NEW_FOLDER_NAME_PREFIX.length() + 1));
                    if(number > numberOfMatches)
                    {
                        numberOfMatches = number;
                    }
                }
            }
        }
        
        return (numberOfMatches >= 0)? NEW_FOLDER_NAME_PREFIX + " " + (numberOfMatches + 1) : NEW_FOLDER_NAME_PREFIX;
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#renameFolder(java.lang.String, java.lang.String)
     */
    public File renameFolder(String oldFolderPath, String newFolderName) throws PSFolderOperationException
    {
        Validate.notNull(oldFolderPath, "oldFolderPath cannot be null");
        Validate.notNull(newFolderName, "newFolderName cannot be null");
        
        // first we get the list of folders and files that are in the old folder
        // to see if we can rename it as the user wants
        File oldFolder = getFile(oldFolderPath);        
        File parentFolder = oldFolder.getParentFile();

        // check if the new name contains invalid chars
        if(containsInvalidChars(newFolderName))
        {
            throw new PSInvalidCharacterInFolderNameException(getInvalidCharsAsString());
        }
        
        // check the length of the new name
        if(newFolderName.length() > FOLDER_NAME_MAX_LENGTH)
        {
            throw new PSFolderNameLengthLimitException();
        }
        
        // check if the name is a reserved word
        if(isReservedFilename(newFolderName))
        {
            throw new PSInvalidFolderNameException();
        }
        
        // check that the name does not already exists
        if(!foldernameAvailable(newFolderName, parentFolder.list()))
        {
            throw new PSExistingFolderException();     
        }
        
        // rename the folder
        File newFolder = new File(parentFolder.getAbsolutePath(), newFolderName);
        oldFolder.renameTo(newFolder);
        
        return newFolder;
    }
    
    /**
     * Checks if the name contains an invalid character.
     * 
     * @param name the name to check. Assumed not <code>null</code>
     * @return <code>true</code> if the name contains an invalid character.
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean containsInvalidChars(String name)
    {
        for(Character invalidChar : INVALID_CHARS)
        {
            if(StringUtils.contains(name, invalidChar))
            {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#foldernameAvailable(java.lang.String, java.lang.String[])
     */
    @Override
    public boolean foldernameAvailable(String name, String[] files)
    {
        // this should never happen
        if(files != null)
        {
            for(String file : files)
            {
                if(file.equalsIgnoreCase(name))
                {
                    return false;
                }
            }
            return true;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#deleteFolder(java.lang.String)
     */
    @Override
    public void deleteFolder(String folderPath) throws IOException
    {
        Validate.notNull(folderPath, "path cannot be null");
        
        File fileToDelete = getFile(folderPath);
        
        FileUtils.deleteDirectory(fileToDelete);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.designmanagement.service.IPSFileSystemService#deleteFile
     * (java.lang.String)
     */
    public void deleteFile(String filePath) throws PSFileOperationException
    {
        Validate.notNull(filePath, "path cannot be null");

        File fileToDelete = getFile(filePath);
        if (fileToDelete.exists())
        {
            try {
                Files.delete(fileToDelete.toPath());
            } catch (IOException e) {
                throw new PSFileOperationException("Could not delete the file '" + fileToDelete.getName() + "'." + e.getMessage());
            }
        }

    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#getNameFromFile(java.io.File)
     */
    @Override
    public String getNameFromFile(File file)
    {
        Validate.notNull(file, "file cannot be null");
        
        String name = file.getName();
        
        if (StringUtils.isBlank(name)) {
            name = file.getParentFile().getName();
        }

        return name;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#getParentFolder(java.lang.String)
     */
    @Override
    public String getParentFolder(String path)
    {
        String parentFolder = "/";
        
        String[] paths = path.split("/");
        for(int i = 1; i < paths.length - 1; i++)
        {
            parentFolder += paths[i];
            parentFolder += "/";
        }
        
        return parentFolder;
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#validateUploadFile(java.lang.String)
     */
    @Override
    public void validateFileUpload(String path) throws PSFileOperationException
    {
        File file = getFile(path);
        File parentFolder = file.getParentFile();
        
        // check if the file will be under 'themes'
        if(!isUnderThemes(path))
        {
            throw new PSFileOperationException("File operations are only allowed under the 'themes' folder.");
        }
        
        // check if the file name contains invalid characters
        if(containsInvalidChars(file.getName()))
        {
            throw new PSInvalidCharacterInFileNameException("File names can not have the following characters: "
                    + getInvalidCharsAsString());
        }
        
        
        // see if there is a file with that name
        String[] files = parentFolder.list(FileFilterUtils.fileFileFilter());
        if(!foldernameAvailable(file.getName(), files))
        {
            throw new PSFileAlreadyExistsException(
                    "A file with that name already exists in the selected folder, and will be overwritten.");
        }
        
        // if the file is a directory the name can not be used
        String[] directories = parentFolder.list(FileFilterUtils.directoryFileFilter());
        if (!foldernameAvailable(file.getName(), directories))
        {
            throw new PSFileNameInUseByFolderException(
                    "A folder with that name already exists in the selected location.");
        }
        
        // check if the name is not reserved
        if(isReservedFilename(file.getName()))
        {
            throw new PSReservedFileNameException("Cannot create file '" + file.getName()
                    + "' because that is a reserved file name.");
        }
    }

    /**
     * Builds a string containing the invalid characters separated with a blank
     * space.
     * 
     * @return a String object, may be empty but never <code>null<code>
     */
    private String getInvalidCharsAsString()
    {
        String chars = "";
        for(Character invalidChar : INVALID_CHARS)
        {
            chars += invalidChar + " ";
        }
        return chars;
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#isReservedWord(java.lang.String)
     */
    @Override
    public boolean isReservedFilename(String name)
    {
        for(String reservedWord : RESERVED_FILENAMES)
        {
            if(reservedWord.equalsIgnoreCase(name))
            {
                return true;
            }
        }       
        return false;
    }

    /* (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#fileUpload(java.lang.String, java.io.InputStream)
     */
    @Override
    public void fileUpload(String path, InputStream pageContent) throws PSFileOperationException {
        try (BufferedInputStream in = new BufferedInputStream(pageContent)) {
            try {
                validateFileUpload(path);
            }catch(PSFileAlreadyExistsException fae){
                //We are already checking for this validation and getting confirmation from client.
                //so, it can eb ignored
            }
            File file = getFile(path);
            File parent = file.getParentFile();

            // create the file if it does not exists

            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {

                int bytesCopied = IOUtils.copy(in, out);
                out.flush();

                // validate the file length
                if (fileSizeExceeded(bytesCopied)) {
                    FileUtils.deleteQuietly(file);
                    throw new PSFileSizeExceededException("The maximum allowed size for a file is " + maxFileSize + " MB.");
                }
                setParentFolderPermissionsToChild(parent,file);
            }

        } catch (IOException e) {
            throw new PSFileOperationException("An error ocurred when uploading the file.", e);
        }
    }

    private void setParentFolderPermissionsToChild(File parent, File child) throws IOException {
        //set parent owner/permissions to newly created file as parents'.
        PosixFileAttributeView posixViewParent = Files.getFileAttributeView(parent.toPath(),
                PosixFileAttributeView.class);
        if(posixViewParent == null){
            return;
        }
        PosixFileAttributes parentAttribs = posixViewParent.readAttributes();
        GroupPrincipal group = parentAttribs.group();
        UserPrincipal owner = parentAttribs.owner();
        Set<PosixFilePermission> permissions = parentAttribs.permissions();
        PosixFileAttributeView posixViewFile = Files.getFileAttributeView(child.toPath(),
                PosixFileAttributeView.class);
        posixViewFile.setPermissions(permissions);
        posixViewFile.setGroup(group);
        posixViewFile.setOwner(owner);
    }
    /*
     * (non-Javadoc)
     * @see com.percussion.designmanagement.service.IPSFileSystemService#fileSizeExceeded(java.io.InputStream)
     */
    private boolean fileSizeExceeded(int fileSize)
    {
        long maxSizeInBytes = Float.valueOf(maxFileSize * 1024).longValue() * 1024;

        // fileSize < 0 is necessary as that means the copied bytes are more
        // than Integer.MAX_VALUE
        return fileSize > maxSizeInBytes || fileSize < 0;
    }

    /**
     * Checks if the given path is under the themes folder. The path would be
     * for example '/themes/folder' or '/themes/file.css'.
     * 
     * @param path the path to check. Assumed not <code>null</code> 
     * @return <code>true</code> if the path is under themes, <code>false</code> if not.
     */
   private boolean isUnderThemes(String path)
    {
        // validate that the folder we are about to delete is below the 'themes' folder
        String auxPath = trimLeadingCharacter(trimTrailingCharacter(path, '/'), '/');
        String paths[] = auxPath.split("/");
        return (paths.length >= 2);
    }

}
