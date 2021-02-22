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
package com.percussion.share.dao;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.StringUtils.replaceChars;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.percussion.cms.IPSConstants;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;

/**
 * 
 * Utilities for working with CM System folder paths.
 * Many of the methods are exactly the same as 
 * <a  href="http://commons.apache.org/io/api-release/org/apache/commons/io/FilenameUtils.html">
 *  Commons FilenameUtils
 * </a>.
 * 
 * @author adamgent
 *
 */
public class PSFolderPathUtils
{
    /**
     * @see #extSeparator()
     */
    private static final String EXT_SEPARATOR = ".";

    /**
     * @see #pathSeparator()
     */
    private static final String PATH_SEPARATOR = "/";
    
    /**
     * @see #replacementCharacter()
     */
    private static final String REPLACEMENT_CHARACTER = "-";
    
    /**
     * Characters that are invalid for item names (sys_title).
     * It is the combination of "invalid characters for the file name in 
     * Windows" and "unsafe URL characters".
     */
    public static String INVALID_ITEM_NAME_CHARACTERS = IPSConstants.INVALID_ITEM_NAME_CHARACTERS;
    
    /**
     * Concats paths by putting the proper path separators
     * taking care of paths that might already have separators in them.
     * @param start never <code>null</code> or empty.
     * @param end may contain separators already, never <code>null</code> or empty.
     * @return never <code>null</code> or empty.
     */
    public static String concatPath(String start, String ... end) {
        isTrue(isNotBlank(start), "start cannot be blank");
        notEmpty(end, "Must have end paths.");
        String path = start;
        for (String p : end ) {
            path = removeEnd(path, pathSeparator()) + pathSeparator() + removeStart(p, pathSeparator());
        }
        return path;
    }
    
    /**
     * Returns the title name of an items path. Or
     * in otherwords the file name minus the path.
     * @param path never <code>null</code> or empty.
     * @return maybe empty.
     */
    public static String getName(String path)
    {
        notEmpty(path, "path");
        return substringAfterLast(path, pathSeparator());
    }
    
    /**
     * Returns the title component of a path minus the extension.
     * 
     * @param path never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static String getBaseName(String path)
    {
        notEmpty(path, "path");
        return removeExtension(getName(path));
    }
    
    /**
     * Gets the extension from at path.
     * @param path never <code>null</code>.
     * @return the extension component of a path, never <code>null</code> maybe empty.
     */
    public static String getExtension(String path)
    {
        notNull(path, "path");
        if ( ! hasExtension(path) ) {
            return "";
        }
        return substringAfterLast(path, extSeparator());
    }
    
    /**
     * 
     * @param path never <code>null</code>.
     * @return never <code>null</code> maybe empty.
     */
    public static String removeExtension(String path)
    {
        notNull(path, "path");
        if ( ! hasExtension(path) ) {
            return path;
        }
        return substringBeforeLast(path, extSeparator());
    }
    
    /**
     * Checks if the file has an extesion.
     * <em>Will return true if the file extension is empty</em>
     * @param path never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static boolean hasExtension(String path) 
    {
        Integer pathIndex = lastIndexOfPathSeparator(path);
        Integer extIndex = lastIndexOfExtSeparator(path);
        /*
         * The ext separator should be after any path separators
         */
        return (pathIndex < extIndex );
    }
    
    private static Integer lastIndexOfPathSeparator(String path) {
        return path.lastIndexOf(pathSeparator());
    }
    
    private static Integer lastIndexOfExtSeparator(String path) {
        return path.lastIndexOf(extSeparator());
    }
    
    /**
     * The folder path separator: <code>'/'</code>
     * @return never <code>null</code>.
     */
    public static String pathSeparator()
    {
        return PATH_SEPARATOR;
    }
    
    /**
     * The file extension separator.
     * This is just like windows and unix and is 
     * a <code>'.'</code>.
     * @return never <code>null</code>.
     */
    public static String extSeparator()
    {
        return EXT_SEPARATOR;
    }
    
    /**
     * If for some reason a character in a filename or path needs to be replaced
     * because its invalid or not desired 
     * this will return the default replacement character.
     * @return never <code>null</code>.
     */
    public static String replacementCharacter()
    {
        return REPLACEMENT_CHARACTER;
    }
    
    /**
     * Will add a "duplicate file name" number to the file name.
     * The number will be always included before the extension if there is one.
     * Its recommended that you use Integers for numbers as floats will throw
     * of the file extension.
     * Example:
     * <pre>
     *  path=/foo/stuff.txt num=2 ---> /foo/stuff (2).txt
     *  path=/foo/stuff num=2     ---> /foo/stuff (2)
     *  path=/foo/stuff num=1.1     ---> /foo/stuff (1.1)
     * </pre>
     * @param path never <code>null</code>.
     * @param num never <code>null</code>.
     * @return never <code>null</code> or empty.
     */
    public static String addEnumeration(String path, Number num) {
        notNull(num, "num");
        String ext = getExtension(path);
        String numName = numberName(num);
        if (hasExtension(path)) {
            return removeExtension(path) + numName + EXT_SEPARATOR + ext;
        }
        return path + numName;
        
    }
    
    /**
     * Calculates the number component of a filename.
     * @param num never <code>null</code>.
     * @return never <code>null</code> or empty.
     */
    protected static String numberName(Number num) {
        return "-" + num;
    }
    
    /**
     * Finds all folderPaths that are descedent of sitePath.
     * @param sitePath Ancestor path.
     * @param folderPaths paths to be filtered, list is not modified.
     * @return a new list never <code>null</code>.
     */
    public static List<String> matchingDescedentPaths(String sitePath, Collection<String> folderPaths) {
        if (folderPaths == null) return new ArrayList<>();
        List<String> paths = new ArrayList<>();
        for(String folderPath : folderPaths) {
            if (isDescedentPath(folderPath, sitePath)) {
                paths.add(folderPath);
            }
        }
        return paths;
    }
    
    
    /**
     * 
     * Resolves a folder path from a list of items that have a
     * single folder path such as a page or a site.
     * <p>
     * The order of the resolving is done through
     * the order of the given paths.
     * <p>
     * If there is an exact folder path match then that is returned.
     * <p>
     * Otherwise descedent paths are matched 
     * {@link #isDescedentPath(String, String)}.
     * <p>
     * So the order is:
     * <ol>
     * <li>Exact match</li>
     * <li>Descendent match</li>
     * <li>null if no path is found</li>
     * </ol>
     * <code>null</code> is returned of no path can be
     * resolved.
     * 
     * @param item never <code>null</code>.
     * @param paths never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public static String resolveFolderPath(
            IPSItemSummary item,
            IPSFolderPath ... paths) {
        
        notNull(item, "item");
        notNull(paths, "paths");
        
        if (item.getFolderPaths() == null || item.getFolderPaths().isEmpty())
            return null;
        
        List<String> matchingPaths = new ArrayList<>();
        for (IPSFolderPath p : paths) {
            if (p == null || isBlank(p.getFolderPath())) 
                continue;
            if (item.getFolderPaths().contains(p.getFolderPath()))
                return p.getFolderPath();
            matchingPaths.addAll(matchingDescedentPaths(p.getFolderPath(), item.getFolderPaths()));
        }
        if (matchingPaths.isEmpty()) return null;
        
        return matchingPaths.get(0);
    }
    
    /**
     * Creates a folder path object from a string representation.
     * @param folderPath maybe <code>null</code>.
     * @return never <code>null</code>.
     */
    public static IPSFolderPath toFolderPath(String folderPath) {
        return new PSMutableFolderPath(folderPath);
    }
    
    private static class PSMutableFolderPath implements IPSFolderPath {
        
        private String folderPath;

        private PSMutableFolderPath(String folderPath)
        {
            super();
            this.folderPath = folderPath;
        }

        public String getFolderPath()
        {
            return this.folderPath;
        }

        public void setFolderPath(String path)
        {
            this.folderPath = path;
        }
    
    }
    
    /**
     * <code>true</code> if the path is a descendent of ancestorPath or is equal to 
     * the ancestorPath.
     * @param path possible descedent path, maybe <code>null</code>.
     * @param ancestorPath maybe <code>null</code>.
     * @return never <code>null</code>.
     */
    public static boolean isDescedentPath(String path, String ancestorPath) {
        String normalizedSitePath = removeEnd(ancestorPath, PATH_SEPARATOR) + PATH_SEPARATOR; 
        return (ancestorPath.equals(path) || startsWith(path, normalizedSitePath));
        
    }
    /**
     * Validates that the supplied path starts with a double slash (//).
     * @param path May be <code>null</code> or empty.
     * @throws IllegalArgumentException if it doesn't start with //.
     */
    public static void validatePath(String path) {
        isTrue(startsWith(path, "//"), "Path must start with '//'");
    }

    /**
     * Finds the parent path.
     * @param path must be a valid path: {@link #validatePath(String)}
     * @return never <code>null</code> or empty.
     */
    public static String parentPath(String path)
    {
        validatePath(path);
        isTrue( ! "//".equals(path.trim()), "No parent path for '//'" );
        path = removeEnd(path, PATH_SEPARATOR);
        String parentPath = substringBeforeLast(path, PATH_SEPARATOR);
        if(PATH_SEPARATOR.equals(parentPath)) return "//";
        return parentPath;
    }
    
    /**
     * Removes {@link #INVALID_ITEM_NAME_CHARACTERS invalid item name characters} from a filename
     * and replaces them with the replacement argument.
     * @param fileName never <code>null</code>.
     * @param replacement maybe <code>null</code>, <code>null</code> is equivalent to <code>""</code>.
     * @return never <code>null</code>.
     * @see #replacementCharacter()
     */
    public static String replaceInvalidItemNameCharacters(String fileName, String replacement) {
        notNull(fileName, "fileName");
        if (replacement != null)
            replacement = repeat(replacement, INVALID_ITEM_NAME_CHARACTERS.length());
        return replaceChars(StringUtils.trim(fileName), INVALID_ITEM_NAME_CHARACTERS, replacement);
    }
    
    private static String repeat(String pattern, int count) {

        StringBuffer buffer = new StringBuffer();
        for (int i=0; i < count; i++)

        buffer.append(pattern);
        return buffer.toString();
    }
    
    /**
     * Removes {@link #INVALID_ITEM_NAME_CHARACTERS invalid item name characters} from a filename
     * and replaces them with the default replacement character.
     * @param fileName never <code>null</code>.
     * @return never <code>null</code>.
     * @see #replacementCharacter()
     */
    public static String replaceInvalidItemNameCharacters(String fileName) {
        notNull(fileName, "fileName");
        return replaceInvalidItemNameCharacters(fileName, replacementCharacter());
    }
    
    /**
     * Checks if the supplied string contains any characters that are invalid for sys title specified in {@link IPSConstants#INVALID_ITEM_NAME_CHARACTERS}
     * @param testString  The string to check
     * @return true if invalid characters found
     */
    public static boolean testHasInvalidChars(String testString)
    {  
        return StringUtils.containsAny(testString, IPSConstants.INVALID_ITEM_NAME_CHARACTERS);                
    }
    

}

