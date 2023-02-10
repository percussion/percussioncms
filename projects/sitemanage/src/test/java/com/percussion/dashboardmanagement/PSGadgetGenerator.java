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
