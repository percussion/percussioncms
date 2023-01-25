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

package com.percussion.rx.services.deployer;

import com.percussion.services.error.PSNotFoundException;

import java.util.List;


public interface IPSPackageUninstaller {

    /**
     * Uninstall a package.
     * 
     * @param packageName the package name to uninstall, i.e. perc.widget.form
     * @return a list of package uninstall messages
     */
    List<PSUninstallMessage> uninstallPackages(String packageName) throws PSNotFoundException;
    
    /**
     * Uninstalls a package
     * 
     * @param packageName the name of the package to uninstall, i.e. perc.widget.form
     * @param isRevertEntry <code>true</code> if is marked as REVERT in InstallPackages.xml.
     * 
     * This flag is needed in some cases if a package being uninstalled contains dependencies and
     * if the package has a status of REVERT.  If this is the case we do not want to uninstall it.
     * 
     * @return the list of uninstall messages
     */
    List<PSUninstallMessage> uninstallPackages(String packageName, boolean isRevertEntry) throws PSNotFoundException;

}
