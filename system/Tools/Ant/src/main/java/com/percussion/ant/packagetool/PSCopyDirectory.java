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

package com.percussion.ant.packagetool;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

public class PSCopyDirectory extends Copy
{

    /**
     * @param rootDirPath the rootDirPath to set
     */
    public void setRootDirPath(String rootDirPath)
    {
        this.rootDirPath = rootDirPath;
    }

    /**
     * @param packageName the packageName to set
     */
    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    /**
     * @param destDir the destDir to set
     */
    public void setDestDir(String destDir)
    {
        this.destDir = destDir;
    }

    @Override
    public void execute() throws BuildException
    {
        setTodir(new File(destDir));
        FileSet fileset = new FileSet();
        String packageFolderName = packageName.substring(0, packageName.lastIndexOf('.'));
        fileset.setDir(new File(rootDirPath + File.separator + packageFolderName));
        addFileset(fileset);
        super.execute();
    }

    /**
     * Destination directory
     */
    private String destDir;

    /**
     * Root directory path for packages
     */
    private String rootDirPath;

    /**
     * Package name
     */
    private String packageName;
}
