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

package com.percussion.rx.services.deployer;

import java.util.List;


public interface IPSPackageUninstaller {

    /**
     * Uninstall a package.
     * 
     * @param packageName the package name to uninstall, i.e. perc.widget.form
     * @return a list of package uninstall messages
     */
    List<PSUninstallMessage> uninstallPackages(String packageName);
    
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
    List<PSUninstallMessage> uninstallPackages(String packageName, boolean isRevertEntry);

}
