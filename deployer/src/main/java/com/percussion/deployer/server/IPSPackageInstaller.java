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
package com.percussion.deployer.server;

import com.percussion.error.PSDeployException;
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
