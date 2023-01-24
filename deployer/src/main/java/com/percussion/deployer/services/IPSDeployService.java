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
package com.percussion.deployer.services;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.server.IPSServiceDependencyHandler;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.server.dependencies.PSDependencyHandler;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSSite;

import java.util.HashMap;

public interface IPSDeployService
{
   // A specific method for installing site files 
   public void deserializeAndSaveSite(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
         PSImportCtx ctx, PSDependencyHandler depHandler, IPSSite s, Integer ver)
         throws PSDeployServiceException;
   
   // A specific method for installing Templates files 
   public void deserializeAndSaveTemplate(PSSecurityToken tok,
                                          PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
                                          PSImportCtx ctx, PSDependencyHandler depHandler,
                                          PSAssemblyTemplate t, Integer ver, HashMap<Long, Integer> bVer)
         throws PSDeployServiceException;

   // A custom method for installing filters
   public void deserializeAndSaveFilter(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
         PSImportCtx ctx, PSDependencyHandler depHandler)
         throws PSDeployServiceException;
   
   // A specific method for installing Templates files 
   public void deserializeAndSaveVariant(PSSecurityToken tok,
                                         PSArchiveHandler archive, PSDependency dep, PSDependencyFile depFile,
                                         PSImportCtx ctx, PSDependencyHandler depHandler,
                                         PSAssemblyTemplate t, Integer ver)
         throws PSDeployServiceException;

   /**
    * Performs the task of installing dependency files.  See 
    * {@link PSDependencyHandler#installDependencyFiles(PSSecurityToken,
    * PSArchiveHandler, PSDependency, PSImportCtx)} for details.
    *
    * @param svcDepHandler The service dependency handler which will be invoked
    * to install the files.  May not be <code>null</code>.
    * 
    * @throws PSDeployServiceException if there are any errors.
    */
   public void installDependencyFiles(PSSecurityToken tok, 
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx,
         IPSServiceDependencyHandler svcDepHandler)
   throws PSDeployServiceException;
}
