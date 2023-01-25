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
package com.percussion.install;

import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.PSContainerUtilsFactory;
import org.w3c.dom.Element;

import java.io.PrintStream;


/**
 * This pluguin is used to migrate relationships data from 5.x to Rhino (6.x?)
 */
public class PSUpgradePluginJetty implements IPSUpgradePlugin {
    /**
     * Default Constructor.
     */
    public PSUpgradePluginJetty() {
    }


    /**
     * Implements process method of IPSUpgradePlugin.
     *
     * @param config   IPSUpgradeModule object. may not be <code>null<code>.
     * @param elemData data element of plugin.
     */
    public PSPluginResponse process(IPSUpgradeModule module, Element elemData) {
        int respType = PSPluginResponse.SUCCESS;
        String respMessage = "";

        PrintStream logger = module.getLogStream();

        String log = module.getLogFile();

        logger.println("Running Jetty Congiguration Upgrade");


        try {
            DefaultConfigurationContextImpl configContext = PSContainerUtilsFactory.getConfigurationContextInstance();
            configContext.load();
            configContext.save();

        } catch (Exception e) {
            e.printStackTrace(module.getLogStream());
            respType = PSPluginResponse.EXCEPTION;
            respMessage = "Failed to update jetty configuration from jboss, " + "see the \"" + log + "\" located in "
                    + RxUpgrade.getPostLogFileDir() + " for errors.";


        }
        module.getLogStream().println("Finished process() of the plugin Upgrade Server Mappings...");
        return new PSPluginResponse(respType, respMessage);
    }
}
