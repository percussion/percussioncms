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

import com.percussion.content.PSGenerator;
import com.percussion.dashboardmanagement.data.DashboardContent.GadgetDef;
import com.percussion.dashboardmanagement.data.DashboardContent.GadgetDef.UserPref;
import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.metadata.web.service.PSMetadataServiceRestClient;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * Adds gadgets to the dashboard of the given server. It also provides
 * a way to remove them from there.
 * 
 * @author miltonpividori
 *
 */
public class PSGadgetGenerator extends PSGenerator<PSMetadataServiceRestClient>
{
    private static final String GADGET_REPOSITORY_PATH = "/cm/gadgets/repository/";
    
    private int[] nextGadgetRowByColumn;
    
    public PSGadgetGenerator(String baseUrl, String uid, String pw)
    {
        super(PSMetadataServiceRestClient.class, baseUrl, uid, pw);
    }

    public void addGadgets(List<GadgetDef> allGadgetDefs)
    {
        List<PSGadget> gadgetsList = new ArrayList<PSGadget>();
        
        int gadgetsCount = allGadgetDefs.size();
        nextGadgetRowByColumn = new int[2];
        
        log.info("Count of gadgets to be added: " + gadgetsCount);
        
        for (int i=0; i<gadgetsCount; i++)
        {
            GadgetDef gadgetDef = allGadgetDefs.get(i);
            PSGadget gadget = createGadget(i, gadgetDef);
            log.info("Adding gadget: " + gadget);
            gadgetsList.add(gadget);
        }
        
        getRestClient().saveGadgets(gadgetsList);
    }
    
    public void cleanup(List<GadgetDef> allGadgetDefs)
    {
        List<String> gadgetUrlsToRemove = new ArrayList<String>();
        
        for (GadgetDef gadgetDef : allGadgetDefs)
        {
            log.info("Cleaning up gadget: " +
                    FilenameUtils.getName(getGadgetUrl(gadgetDef.getGadgetType())));
            gadgetUrlsToRemove.add(getGadgetUrl(gadgetDef.getGadgetType()));
        }
        
        getRestClient().removeGadgets(gadgetUrlsToRemove);
    }
    
    private PSGadget createGadget(int i, GadgetDef gadgetDef)
    {
        PSGadget gadget = new PSGadget();
        gadget.setCol(gadgetDef.getColumn());
        gadget.setExpanded(gadgetDef.isExpanded());
        gadget.setInstanceId(i + 1);
        gadget.setRow(nextGadgetRowByColumn[gadget.getCol()]++);
        gadget.setUrl(getGadgetUrl(gadgetDef.getGadgetType()));
        
        // Gadget specific settings
        for (UserPref userPref : gadgetDef.getUserPref())
            gadget.getSettings().put(userPref.getName(), userPref.getValue());
        
        return gadget;
    }
    
    private String getGadgetUrl(String gadgetType)
    {
        if (!gadgetType.contains("/"))
            return GADGET_REPOSITORY_PATH +
                gadgetType + "/" + gadgetType + ".xml";
        else
            return GADGET_REPOSITORY_PATH + gadgetType;
    }
}
