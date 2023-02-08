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

package com.percussion.pagemanagement.web.service;

import com.percussion.share.test.PSObjectRestClient;
import com.percussion.theme.data.PSRegionCSS;
import com.percussion.theme.data.PSRegionCssList;
import com.percussion.theme.data.PSTheme;
import com.percussion.theme.data.PSThemeSummary;

import java.util.List;

public class PSThemeRestClient extends PSObjectRestClient
{
    private String path = "/Rhythmyx/services/pagemanagement/theme/";

    public List<PSThemeSummary> findAll()
    {
        return getObjectsFromPath(concatPath(path, "summary/all"), PSThemeSummary.class);
    }
    
    public PSTheme loadCSS(String name)
    {
        return getObjectFromPath(concatPath(path, "css", name), PSTheme.class);
    }
    
    public PSThemeSummary create(String newTheme, String existingTheme)
    {
        return getObjectFromPath(concatPath(path, "create", newTheme, existingTheme), PSThemeSummary.class);
    }
    
    public void delete(String name)
    {
        super.delete(concatPath(path, "delete", name));
    }
    
    public PSRegionCSS getRegionCSS(String theme, String templatename, String outerregion, String region)
    {
        return getObjectFromPath(concatPath(path, "regioncss", theme, templatename, outerregion, region), PSRegionCSS.class);
    }
    
    public String saveRegionCSS(String theme, String templatename, PSRegionCSS regionCSS)
    {
        return postObjectToPath(concatPath(path, "regioncss", theme, templatename), regionCSS);
    }
    
    public void deleteRegionCSS(String theme, String templatename, String outerregion, String region)
    {
        DELETE(concatPath(path, "regioncss", theme, templatename, outerregion, region));
    }
    
    public void mergeRegionCSS(String theme, String templatename, PSRegionCssList regions)
    {
        postObjectToPath(concatPath(path, "regioncss/merge", theme, templatename), regions);
    }

    public void prepareForEditRegionCSS(String theme, String templatename)
    {
        POST(concatPath(path, "regioncss/prepareForEdit", theme, templatename), (String)null);
    }

    public void clearCacheRegionCSS(String theme, String templatename)
    {
        DELETE(concatPath(path, "regioncss/clearCache", theme, templatename));
    }
}
