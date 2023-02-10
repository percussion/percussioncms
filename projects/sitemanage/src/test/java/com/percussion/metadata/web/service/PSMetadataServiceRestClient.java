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

package com.percussion.metadata.web.service;

import com.percussion.dashboardmanagement.data.PSDashboardConfiguration;
import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.error.PSExceptionUtils;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.test.PSDataServiceRestClient;
import net.sf.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
