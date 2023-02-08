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
