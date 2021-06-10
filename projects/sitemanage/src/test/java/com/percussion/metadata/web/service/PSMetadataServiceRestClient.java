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

package com.percussion.metadata.web.service;

import com.percussion.dashboardmanagement.data.PSDashboardConfiguration;
import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.test.PSDataServiceRestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

public class PSMetadataServiceRestClient extends PSDataServiceRestClient<PSMetadata>
{

    private static final String GADGETS_KEY = "perc.user.Admin.dash.page.0";
    private static final String GADGETS_PREFS_KEY = "perc.user.Admin.dash.page.0.prefs";
    
    public PSMetadataServiceRestClient(String url)
    {
        super(PSMetadata.class, url, "/Rhythmyx/services/metadatamanagement/metadata/");
        
//        PSDashboardConfiguration dash = new PSDashboardConfiguration();
//        
//        JSONObject dashConfigJson = new JSONObject();
//        dashConfigJson.put("DashboardConfig", JSONObject.fromObject(dash));
//        
//        PSMetadata metadata = new PSMetadata(GADGETS_KEY, "\"" + dashConfigJson.toString() + "\"");
//        JSONObject metadataJson = new JSONObject();
//        metadataJson.put("metadata", JSONObject.fromObject(metadata));
//        dashboardConfigWithEmptyGadgetList = metadataJson.toString();
    }

    public void save(JSONObject obj)
    {
        POST(getPath(), obj.toString());
    }

    public List<PSGadget> getCurrentGadgets()
    {
        PSDashboardConfiguration config = null;
        
        try
        {
            String response = GET(concatPath(getPath(), GADGETS_KEY));
            PSMetadata metadata = PSSerializerUtils.unmarshal(response, PSMetadata.class);
            
            JSONObject jsonObject = (JSONObject) JSONObject.fromObject(metadata.getData()).get("DashboardConfig");
            Map classMap = new HashMap();  
            classMap.put("gadgets", PSGadget.class);
            config = (PSDashboardConfiguration) JSONObject.toBean(jsonObject, PSDashboardConfiguration.class, classMap);  
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
        return config.getGadgets();
    }
    
    public void saveGadgets(List<PSGadget> gadgets)
    {
        PSDashboardConfiguration dashboardConfig = new PSDashboardConfiguration();
        dashboardConfig.setGadgets(gadgets);
        
        try
        {
            // Save gadgets along with common settings (like extended, row, col, etc).
            POST(getPath(), getMetadataJson(dashboardConfig).toString(), MediaType.APPLICATION_JSON);
            
            // Save specific gadget's preferences
            POST(getPath(), getMetadataJsonForSpecificGadgetSettings(dashboardConfig).toString(),
                    MediaType.APPLICATION_JSON);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private JSONObject getMetadataJsonForSpecificGadgetSettings(PSDashboardConfiguration dashboardConfig)
    {
        JSONObject userConfig = new JSONObject();
        
        for (PSGadget gad : dashboardConfig.getGadgets())
        {
            JSONObject specificGadgetSettings = new JSONObject();
            
            for (String prefName : gad.getSettings().keySet())
                specificGadgetSettings.put(prefName, gad.getSettings().get(prefName));
            
            userConfig.put("mid_" + gad.getInstanceId(), specificGadgetSettings);
        }
        
        JSONObject userPrefJson = new JSONObject();
        userPrefJson.put("userprefs", userConfig);
        
        PSMetadata metadata = new PSMetadata(GADGETS_PREFS_KEY, "\"" + userPrefJson.toString() + "\"");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("metadata", JSONObject.fromObject(metadata));
        
        return metadataJson;
    }
    
    private JSONObject getMetadataJson(PSDashboardConfiguration dashboardConfig)
    {
        JSONObject dashboardConfigJson = new JSONObject();
        dashboardConfigJson.put("DashboardConfig", JSONObject.fromObject(dashboardConfig));
        
        PSMetadata metadata = new PSMetadata(GADGETS_KEY, "\"" + dashboardConfigJson.toString() + "\"");
        JSONObject metadataJson = new JSONObject();
        metadataJson.put("metadata", JSONObject.fromObject(metadata));
        
        return metadataJson;
    }

    public void removeAllGadgets()
    {
        saveGadgets(new ArrayList<PSGadget>());
    }
    
    public void removeGadgets(List<String> gadgetUrls)
    {
        List<PSGadget> currentGadgets = getCurrentGadgets();
        List<PSGadget> newGadgetList = new ArrayList<PSGadget>();
        
        // Remove the specified gadgets
        for (PSGadget aGadget : currentGadgets)
        {
            if (gadgetUrls.contains(aGadget.getUrl()))
                continue;
            
            newGadgetList.add(aGadget);
        }
        
        saveGadgets(newGadgetList);
    }
}
