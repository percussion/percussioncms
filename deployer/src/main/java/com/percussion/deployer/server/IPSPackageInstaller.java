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
package com.percussion.deployer.server;

import com.percussion.deployer.error.PSDeployException;
import com.percussion.services.error.PSNotFoundException;

import java.io.File;

/**
 * @author JaySeletz
 *
 */
public interface IPSPackageInstaller
{
    /**
     * Install a package file.
     * 
     * @param packageFile The file to install, must exist and be a valid package file.
     * 
     * @throws PSDeployException If there are any errors
     */
    void installPackage(File packageFile) throws PSDeployException, PSNotFoundException;
    
    /**
     * Install a package file.
     * 
     * @param packageFile The file to install, must exist and be a valid package file.
     * 
     * @param shouldValidateVersion <code>false</code> to skip the version check for reverted packages on uninstall of patch
     * 
     * @throws PSDeployException If there are any errors
     */
    void installPackage(File packageFile, boolean shouldValidateVersion) throws PSDeployException, PSNotFoundException;
}
