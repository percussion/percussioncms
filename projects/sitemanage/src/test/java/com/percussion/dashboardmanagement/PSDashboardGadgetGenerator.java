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

package com.percussion.dashboardmanagement;

import com.percussion.content.PSGenericContentGenerator;
import com.percussion.dashboardmanagement.data.DashboardContent;

import java.io.InputStream;

/**
 * This class provide services to add gadgets to the dashboard. You are also
 * able to do a cleanup, selectively removing gadgets in the dashboard.
 * <p>
 * A server URL and an XML file defining the content to be generated are
 * required. User and password to authenticate against the server are optional.
 * 
 * @author miltonpividori
 * 
 */
public class PSDashboardGadgetGenerator extends PSGenericContentGenerator<DashboardContent>
{
    /**
     * The gadget generator. This is the instance responsible for generating
     * and cleaning up gadgets.
     */
    private PSGadgetGenerator gadgetGenerator;
    
    /**
     * @see PSDashboardGadgetGenerator
     * @param serverUrl
     * @param xmlData
     * @param username
     * @param password
     */
    public PSDashboardGadgetGenerator(String serverUrl, InputStream xmlData, String username, String password)
    {
        super(serverUrl, xmlData, username, password);
        
        this.gadgetGenerator = new PSGadgetGenerator(this.serverUrl, this.username, this.password);
    }

    public static void main(String[] args) throws Exception
    {
        PSGenericContentGenerator.runMainMethod(args, PSDashboardGadgetGenerator.class);
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.content.PSGenericContentGenerator#getRootDataType()
     */
    protected Class<DashboardContent> getRootDataType()
    {
        return DashboardContent.class;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.content.PSGenericContentGenerator#generateAllContent()
     */
    protected void generateAllContent()
    {
        gadgetGenerator.addGadgets(content.getGadgetDef());
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.content.PSGenericContentGenerator#cleanupAllContent()
     */
    protected void cleanupAllContent()
    {
        gadgetGenerator.cleanup(content.getGadgetDef());
    }
}
