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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
