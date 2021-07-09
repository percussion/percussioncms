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

package com.percussion.ant.packagetool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Few helper methods to deal with directories paths etc for storing contents of
 * the .ppkg files
 *
 */
public class PSPackageBuildToolHelper
{

    private static final Logger log = LogManager.getLogger(PSPackageBuildToolHelper.class);

    /**
     * Files in the tempDirectory are with long paths the way in the packages.
     * Those get processed and flat file names are stored to destinationDirectory
     * along with a property file which stores mapping of flat file to its long
     * path as stored in the destinationDirectory as well
     *
     * @param tempDirectory It may not be <code>null</code> or empty.
     * @param destinationDirectory It may not be <code>null</code> or empty.
     */
    public static void moveFilesToDestinationFolder(File tempDirectory,
                                                    File destinationDirectory)
    {
        Properties props = new Properties();
        File[] listOfFiles = tempDirectory.listFiles();
        try
        {
            for (File file : listOfFiles)
            {
                if (file.isFile())
                {
                    file.renameTo(new File(destinationDirectory, file.getName()));
                }
                if (file.isDirectory())
                {
                    boolean isDesignObj = false;
                    String parentDirName = file.getName();
                    if ((parentDirName.indexOf('_') == -1 ||parentDirName.startsWith(SCHEMA_FILE) ||
                            parentDirName.startsWith(APLLICATION_FILE))
                            && file.listFiles()[0].isFile())
                    {
                        // this is a design object directory, files will be moved to
                        // destination directory
                        isDesignObj = true;
                    }

                    File[] listOfDirFiles = file.listFiles();
                    for (File dirFile : listOfDirFiles)
                    {
                        if (dirFile.isFile())
                        {
                            String dirFileName = dirFile.getName();

                            if (isDesignObj)
                            {
                                dirFile.renameTo(new File(destinationDirectory,
                                        dirFileName));
                                props.setProperty(dirFileName, parentDirName);
                            }
                            else if (parentDirName.startsWith(SYS_USER_DEPENDENCY))
                            {
                                String nameInPath = dirFileName.replace("-", "--")
                                        .replace(".", "-").replace("_", "__");
                                String folderPath = parentDirName.substring(
                                        SYS_USER_DEPENDENCY.length(), parentDirName
                                                .indexOf(nameInPath) - 1);
                                File destFolder = new File(destinationDirectory,
                                        SYS_USER_DEPENDENCY
                                                + folderPath.replace('-', '.').replace("_",
                                                File.separator).replace(
                                                File.separator + File.separator, "_"));
                                destFolder.mkdirs();
                                dirFile.renameTo(new File(destFolder, dirFileName));
                            }
                        }
                        else
                        {
                            String folderPath = parentDirName;
                            moveDirectoryToDestinationFolder(file, folderPath,
                                    destinationDirectory);
                        }
                    }
                }
            }
            props.store(new FileOutputStream(destinationDirectory.getAbsolutePath() + "/" +
                    destinationDirectory.getName()+ MAPPING_EXT),null);
            deleteDirectory(tempDirectory);
        }
        catch (IOException io)
        {
        }
    }

    /**
     * Given zip file path and the root directory(...\system\packages) returns the destination directory
     * under the root directory
     * @param zipFileWithPath cannot be <code>null<code>
     * @param rootDirectory cannot be <code>null<code>
     * @return
     */
    public static String getDestinationDirectoryPath(String zipFileWithPath,
                                                     String rootDirectory)
    {
        String subDirectory = getSubDirectory(zipFileWithPath);
        String destinationDirectory = getDestinationDirectory(subDirectory,
                rootDirectory);
        return destinationDirectory;
    }

    /**
     * Returns a list of files present in tempDir but not in sourceDir.
     *
     * @param sourceDir Source directory.
     * @param tempDir Temp directory.
     * @return A list of File objects that exists in the Temp directory, but not
     *         in the Source one.
     */
    public static List<File> findAddedFiles(File sourceDir, File tempDir)
    {
        List<File> fileList = new ArrayList<File>();
        Map<String, File> sourceDirFiles = getFilesMap(sourceDir);
        Map<String, File> tempDirFiles = getFilesMap(tempDir);

        for (String filePath : tempDirFiles.keySet())
        {
            if (!sourceDirFiles.containsKey(filePath))

                // Needs to exclude .properties files
                if (!filePath.endsWith(".properties"))
                {
                    fileList.add(tempDirFiles.get(filePath));
                }
        }
        return fileList;
    }

    public static List<File> getFilesWithSourcePath(List<File> filelist,
                                                    File sourceDir, File tempDir, boolean isDelete)
    {
        List<File> sourcePathList = new ArrayList<File>();
        String fileFullPath;

        for (File file : filelist)
        {
            if (isDelete)
            {
                sourcePathList.add(file);
            }
            else
            {
                String filePath = file.getPath();
                String pathWithSrcDir = filePath.substring(tempDir.getPath().length()+ 1, filePath.length());
                fileFullPath = sourceDir.getPath() + File.separator + pathWithSrcDir;
                sourcePathList.add(new File(fileFullPath));
            }
        }
        return sourcePathList;
    }

    /**
     * Given a directory, it generates a Map object with all its files. It works
     * recursively.
     *
     * @param sourceDir A source directory to look for files recursively.
     * @return A Map object with the file path as the key (relative to the source
     *         directory) and the File object that represents it.
     */
    private static Map<String, File> getFilesMap(File sourceDir)
    {
        Map<String, File> map = new HashMap<String, File>();

        for (Object obj : FileUtils.listFiles(sourceDir, FileFilterUtils
                .trueFileFilter(), FileFilterUtils.directoryFileFilter()))
        {
            File file = (File) obj;
            // map.put(file.getPath(), file);
            map.put(file.getName(), file);
        }

        return map;
    }

    /**
     * Check for the destination directory and if it doesn't exist then create
     * one. In this case outcome would be rootDirectory/subDirectoryName
     *
     * @param subDirectoryName name of the package excluding .ppkg
     * @param rootDirectory predefined one
     * @return destination directory
     */
    private static String getDestinationDirectory(String subDirectoryName,
                                                  String rootDirectory)
    {
        String destinationDirectory = rootDirectory + File.separator
                + subDirectoryName;
        File destinationFolder = new File(destinationDirectory);
        if (!destinationFolder.exists())
        {
            destinationFolder.mkdir();
        }
        return destinationDirectory;

    }

    /**
     * Ex: if passed in path is \system\build\dist\Packages\perc.Baseline.ppkg
     * outcome will be perc.Baseline
     *
     * @param zipFileWithPath name of the package with path
     * @return the package name without the extension
     */
    private static String getSubDirectory(String zipFileWithPath)
    {
        File file = new File(zipFileWithPath);
        String fileName = file.getName();
        String directoryName = fileName.substring(0, fileName.lastIndexOf('.'));
        return directoryName;
    }

    /**
     *
     * @param directoryToBeDeleted
     */
    private static void deleteDirectory(File directoryToBeDeleted)
    {
        if (directoryToBeDeleted.exists())
        {
            File[] files = directoryToBeDeleted.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    deleteDirectory(files[i]);
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        directoryToBeDeleted.delete();
    }

    /**
     * Move the entire directory to destination directory
     * @param dir child directory under the parent folder path Cannot be <code>null</code>
     *          or  Cannot be <code>empty</code>.
     * @param parentFolderPath Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     * @param destinationDirectory Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     */
    private static void moveDirectoryToDestinationFolder(File dir,
                                                         String parentFolderPath, File destinationDirectory)
    {
        String folderPath = parentFolderPath;

        File[] childDirs = dir.listFiles();
        for (File childDirFile : childDirs)
        {
            String childDirName = childDirFile.getName();
            if (childDirFile.isFile())
            {
                File destFolder = new File(destinationDirectory, parentFolderPath);
                destFolder.mkdirs();
                childDirFile.renameTo(new File(destFolder, childDirName));
            }
            else
            {
                File[] children = childDirFile.listFiles();
                if (children.length > 0
                        && (children[0].isDirectory() || !children[0].getName()
                        .startsWith(childDirName)))
                {
                    folderPath = parentFolderPath +  File.separator  + childDirName;
                }

                moveDirectoryToDestinationFolder(childDirFile, folderPath,
                        destinationDirectory);
            }
        }
    }

    /**
     * Builds the file paths from the directory structure to full paths the way it is in .ppkg files
     * @param packageName  Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     * @param filename     Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     * @param rootDir      Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     * @param destDirStr   Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     * @param prop mapping properties file which contains entries for few file types
     */
    public static void moveFilesToOriginalPaths(String packageName, String filename, File rootDir, String destDirStr,
                                                Properties prop)
    {

        String path = "";
        String pathBefore = "";
        String actualfile = "";
        String pathSuffix = "";
        String fullPath = "";
        File lastFile;

        File filenames = new File(filename);
        if (!filenames.isDirectory())
        {
            if (filenames.getName().equals(packageName + MAPPING_EXT))
            {
                // Skip the mapping properties file
                return;
            }
            // Gives the path of the whole file, EX: if filename=
            // C:\src\cm1comments\foldername\tmp.png then
            // path would be \foldername\tmp.png
            path = filename.substring(filename.indexOf(rootDir.getName())
                    + rootDir.getName().length(), filename.length());

            actualfile = path.substring(path.lastIndexOf(File.separator) + 1);
            // Gives path with excluding the filename
            pathBefore = path
                    .substring(0, path.length() - actualfile.length() - 1);

            String propValue = prop.getProperty(actualfile);
            if (propValue != null)
            {
                // there is an entry for this file in the properties file means this
                // file belongs to one the following types
                // ContentType, AclDef, TemplateDef
                lastFile = new File(destDirStr + File.separator + propValue);
                lastFile.mkdirs();
                filenames.renameTo(new File(lastFile.getAbsolutePath()
                        + File.separator + filenames.getName()));
            }
            else if (pathBefore.startsWith(File.separator + SYS_USER_DEPENDENCY))
            {
                pathBefore = pathBefore.substring(SYS_USER_DEPENDENCY.length() + 1,
                        pathBefore.length());
                pathBefore = pathBefore.replace("_", "__").replace(".", "-")
                        .replace(File.separator, "_");
                pathSuffix = actualfile.replace("-", "--").replace(".", "-")
                        .replace("_", "__");
                fullPath = SYS_USER_DEPENDENCY + pathBefore + File.separator
                        + pathSuffix;
                fullPath = fullPath.replace(File.separator, "_");
                lastFile = new File(destDirStr + File.separator + fullPath);
                lastFile.mkdirs();
                filenames.renameTo(new File(lastFile.getAbsolutePath()
                        + File.separator + filenames.getName()));
            }
            else if (!pathBefore.equals(""))
            {
                // Here means file must belongs to one of these types SupportFile,
                // ImageFile,Extension, StyleSheet
                if ((pathBefore.startsWith(File.separator + EXTENSION))
                        || (pathBefore.startsWith(File.separator + STYLE_SHEET)))
                {
                    // For extension type pathSuffix is same as the file name
                    // excluding extension
                    pathSuffix = actualfile
                            .substring(0, actualfile.lastIndexOf("."));
                }
                else
                {
                    //ImageFile, support file pathSuffix would be same as the
                    // filename
                    pathSuffix = actualfile;
                }
                fullPath = pathBefore + File.separator + pathSuffix;
                lastFile = new File(destDirStr + File.separator + fullPath);
                lastFile.mkdirs();
                filenames.renameTo(new File(lastFile.getAbsolutePath()
                        + File.separator + filenames.getName()));
            }
            else if (pathBefore.equals(""))
            {
                // File those directly need to go on to the top level
                filenames.renameTo(new File(destDirStr, actualfile));
            }
            return;
        }
        else
        {
            String filelists[] = filenames.list();
            for (String file : filelists)
            {
                moveFilesToOriginalPaths(packageName,
                        filename + File.separator + file, rootDir, destDirStr, prop);
            }
        }
    }

    /**
     * @param folderPath  Cannot be <code>null</code> or  Cannot be <code>empty</code>.
     * @return the folderanme.mapping.properties file
     * @throws IOException
     */
    public static Properties getPropertiesFile(String folderPath) throws IOException
    {
        Properties props = new Properties();
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(folderPath + MAPPING_EXT);
            props.load(fis);
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
        return props;
    }

    /**
     * Builds the properties file name and Copies the properties file in the
     * tempDirPath to the properties file in destDirectoryPath
     * @param tempDirPath
     * @param destDirectoryPath
     */
    public static void copyPropertiesFile(String tempDirPath,
                                          String destDirectoryPath)
    {
        String directoryName = getDirectoryName(tempDirPath);

        String propertyName = directoryName
                + PSPackageBuildToolHelper.MAPPING_EXT;

        File tempPropFile = new File(destDirectoryPath + File.separator
                + propertyName);

        // Now get the properties file in the source directory
        String srcDirectoryName = getDirectoryName(destDirectoryPath);

        String srcPropertyName = srcDirectoryName
                + PSPackageBuildToolHelper.MAPPING_EXT;

        File srcPropFile = new File(destDirectoryPath + File.separator
                + srcPropertyName);

        try
        {
            FileUtils.copyFile(tempPropFile, srcPropFile);
        }
        catch (IOException e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        finally
        {
            if(tempPropFile != null)
                tempPropFile.delete();
        }
    }

    /**
     * From the directory path returns the last part of the path
     * Ex: for C:\src\cm1comments\system\Packages\perc.Baseline.temp1
     * returns perc.Baseline.temp1
     * @param dirPath
     * @return
     */
    private static String getDirectoryName(String dirPath)
    {
        return dirPath.substring(dirPath.lastIndexOf(File.separator ) + 1, dirPath.length());
    }

    /**
     * Following constants are few files types in the packages.
     */
    private static String SYS_USER_DEPENDENCY = "sys__UserDependency--";

    private static String SCHEMA_FILE     = "Schema";

    private static String APLLICATION_FILE  = "Application";

    private static String EXTENSION = "Extension";

    private static String STYLE_SHEET = "Stylesheet";


    /**
     * The extension of the mapping properties file.
     */
    private static String MAPPING_EXT = ".mapping.properties";

}
