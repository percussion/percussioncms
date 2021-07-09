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
package com.percussion.widgetbuilder.utils;

import com.percussion.utils.PSTokenReplacingReader;
import com.percussion.widgetbuilder.utils.xform.PSAclFileTransformer;
import com.percussion.widgetbuilder.utils.xform.PSContentTypeFileTransformer;
import com.percussion.widgetbuilder.utils.xform.PSControlManager;
import com.percussion.widgetbuilder.utils.xform.PSResourceFileTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;


/**
 * Build a widget package from a supplied specification
 * 
 * @author JaySeletz
 *
 */
public class PSWidgetPackageBuilder
{
    private static final String binExt = ".png";
    private static final String WIDGET_TEMPLATE_NAME = "percWidgetTemplate";
    
    private List<IPSWidgetFileTransformer> xformList = new ArrayList<>();
    private File srcFile;
    private File tmpDir;

    /**
     * Create a reusable instance of the package builder for a given source widget package file.
     * 
     * @param srcFile A valid file reference to a source template pkg file, not <code>null</code>.
     * @param tmpDir A valid file reference to a temp directory to use.
     */
    public PSWidgetPackageBuilder(File srcFile, File tmpDir)
    {
        Validate.notNull(srcFile);
        Validate.notNull(tmpDir);
        
        this.srcFile = srcFile;
        this.tmpDir = tmpDir;
        xformList.add(new PSAclFileTransformer());
        xformList.add(new PSContentTypeFileTransformer(new PSControlManager()));
        xformList.add(new PSResourceFileTransformer());
    }

    /**
     * Can override the default file transformers, used by unit tests.
     * 
     * @param xformList The list of file transformers to use, not <code>null</code>.
     */
    public void setFileTransformers(List<IPSWidgetFileTransformer> xformList)
    {
        Validate.notNull(xformList);
        this.xformList = xformList;
    }
    
    /**
     * Generate the package file in the specified directory using the supplied spec.
     * 
     * @param tgtDir The directory in which the package is to be created, not <code>null</code> 
     * @param packageSpec The package spec to use, not <code>null</code>.
     * 
     * @throws PSWidgetPackageBuilderException if there are any errors. 
     */
    public File generatePackage(File tgtDir, PSWidgetPackageSpec packageSpec) throws PSWidgetPackageBuilderException
    {
        Validate.notNull(tgtDir);
        Validate.notNull(packageSpec);
        
        if (!tgtDir.isDirectory())
            throw new IllegalArgumentException("tgtDir must be a valid directory");
        
        File tmpPkgDir = extractAndResolveFiles(packageSpec);
        
        return createPackage(tmpPkgDir, packageSpec, tgtDir);
    }



    /**
     * Extract the files from the template src zip file, transforming as necessary.
     * 
     * @param packageSpec The spec to use for transforms
     * 
     * @return a file reference to the directory containing the package files
     * 
     * @throws PSWidgetPackageBuilderException 
     */
    private File extractAndResolveFiles(PSWidgetPackageSpec packageSpec) throws PSWidgetPackageBuilderException
    {
        File rootDir = new File(tmpDir, packageSpec.getWidgetName());
        
        ZipInputStream zin = null;
        Reader reader = null;
        FileOutputStream fout = null;
        try
        {
            // make sure we start clean
            if (rootDir.exists())
                FileUtils.deleteDirectory(rootDir);
            
            FileInputStream in = new FileInputStream(srcFile);
            zin = new ZipInputStream(in);
            

            ZipEntry entry = zin.getNextEntry();
            while(entry != null)
            {
                if (!entry.isDirectory())
                {

                    String resolvePath = resolvePath(entry.getName(), packageSpec);
                    File file = new File(rootDir, resolvePath);
                    IPSWidgetFileTransformer xform = getFileTransformer(file);
                    if (xform != null)
                    {
                        file = xform.transformPath(file, packageSpec);
                    }
                    
                    file.getParentFile().mkdirs();
                    fout = new FileOutputStream(file);
                    
                    if (isTextFile(file))
                    {
                        reader = new PSTokenReplacingReader(new InputStreamReader(zin), new PSWidgetPackageResolver(packageSpec));
                        if (xform != null)
                            reader = xform.transformFile(file, reader, packageSpec);
                        IOUtils.copy(reader, fout);
                    }
                    else
                    {
                        IOUtils.copy(zin, fout);
                    }
                    
                    zin.closeEntry();
                    IOUtils.closeQuietly(fout);
                }
                
                
                entry = zin.getNextEntry();
            }

            return rootDir;
        }
        catch (Exception e)
        {
            throw new PSWidgetPackageBuilderException("Error generating widget package file contents: " + e.getLocalizedMessage(), e);
        }
        finally
        {
            IOUtils.closeQuietly(zin);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(fout);
        }
    }


    /**
     * Get a transformer for the specified file.
     * 
     * @param file
     * 
     * @return The transformer to use, or <code>null</code> if none is found.
     */
    private IPSWidgetFileTransformer getFileTransformer(File file)
    {
        for (IPSWidgetFileTransformer xform : xformList)
        {
            if (xform.handleFile(file))
                return xform;
        }
        return null;
    }

    /**
     * Determine if the supplied file reference is a transformable text file, or a binary file.
     * 
     * @param file
     * 
     * @return <code>true</code> if a text file, <code>false</code> if a binary.
     */
    private boolean isTextFile(File file)
    {
        return !file.getName().endsWith(binExt);
    }

    /**
     * Resolve the supplied path with the supplied package spec
     * @param path
     * @param packageSpec 
     * 
     * @return The resolved path
     */
    private String resolvePath(String path, PSWidgetPackageSpec packageSpec)
    {
        return StringUtils.replace(path, WIDGET_TEMPLATE_NAME, packageSpec.getFullWidgetName());
    }


    /**
     * Create the package file from the files in the supplied directory. 
     *  
     * @param packageSpec The package spec to use 
     * @param tmpPkgDir The temp directory containing the files to add to the package
     * @param tgtFile The file to create
     * 
     * @return A reference to the created package file.
     * 
     * @throws PSWidgetPackageBuilderException 
     * 
     */
    private File createPackage(File tmpPkgDir, PSWidgetPackageSpec packageSpec, File tgtDir) throws PSWidgetPackageBuilderException
    {
        File tgtFile = new File(tgtDir, packageSpec.getPackageName() + ".ppkg");
        ZipOutputStream zout = null;
        try
        {
            zout = new ZipOutputStream(new FileOutputStream(tgtFile));
            writeFiles(tmpPkgDir, zout, tmpPkgDir);
                    
            return tgtFile;
        }
        catch (Exception e)
        {
            throw new PSWidgetPackageBuilderException("Error writing widget package file: " + e.getLocalizedMessage(), e);
        }
        finally
        {
            IOUtils.closeQuietly(zout);
        }
    }

    /**
     * Recursively write files to the zip stream recursing into subdirectories.
     * @param filesDir The directory to check for files
     * @param zout The zip output stream to write to
     * @param rootDir Root directory of all files, zip entry path should be relative to this.
     * @throws IOException 
     */
    private void writeFiles(File filesDir, ZipOutputStream zout, File rootDir) throws IOException
    {
        File[] files = filesDir.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                writeFiles(file, zout, rootDir);
                continue;
            }
            
            FileInputStream fin = null;
            try
            {
                // remove tmpdir and widget name from path
                String path = file.getCanonicalPath();
                path = StringUtils.removeStart(path, rootDir.getCanonicalPath());
                path = StringUtils.removeStart(path, File.separator);
                if ('\\' == File.separatorChar)
                    path = path.replace('\\', '/');
                fin = new FileInputStream(file);
                zout.putNextEntry(new ZipEntry(path));
                IOUtils.copy(fin, zout);
                zout.closeEntry();
            }
            finally
            {
                IOUtils.closeQuietly(fin);
            }
        }
        
    }
}
