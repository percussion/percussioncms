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
