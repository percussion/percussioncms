/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.ant.install;

import org.apache.tools.ant.BuildException;

/**
 * This class is responsible for updating encryption from legacy algorithms to new ones on upgrade.
 * It will check for a configuration file that contains the currently configured algorithm and if that
 * setting does not exist all existing files and passwords will be decrypted using old algorithm and then
 * re-encrypted using the new one.
 */
public class PSUpgradeEncryption extends PSAction {


    /* TODO: Update encrypted strings in the following files
             1. rxconfig/Installer/rxrepository.properties
             2. rconfig/Server/config.xml
             3. rxconfig/Workflow/rxworkflow.properties
             4. jetty/base/perc-ds.properties
             5. USERLOGIN table passwords
     */


    /**
     * This will handle initialization of the install logger, loading of
     * PreviousVersion.properties for upgrades, and setting of the entity
     * resolver's resolution home used to find DTD's.  It also determines if
     * all files should be refreshed by date.
     */
    @Override
    public void execute() throws BuildException {
        super.execute();
    }





}
