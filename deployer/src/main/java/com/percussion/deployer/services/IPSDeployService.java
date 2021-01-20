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
