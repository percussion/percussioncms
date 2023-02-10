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

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.dependencies.PSDependencyHandler;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;


/**
 * Interface for dependency handlers that utilize services.
 */
public interface IPSServiceDependencyHandler
{
   /**
    * Performs the task of installing dependency files as described by 
    * {@link PSDependencyHandler#installDependencyFiles(PSSecurityToken,
    * PSArchiveHandler, PSDependency, PSImportCtx)}.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param archive The archive handler to use to retrieve the required files
    * from the archive.  May not be <code>null</code>.
    * @param dep The dependency for which files are to be installed.  May not be
    * <code>null</code> and must be of the type supported by the handler.
    * @param ctx The import context to aid in the installation.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public void doInstallDependencyFiles(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
           throws PSDeployException, PSNotFoundException;
}
