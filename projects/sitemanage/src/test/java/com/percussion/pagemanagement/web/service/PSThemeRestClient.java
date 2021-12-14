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
