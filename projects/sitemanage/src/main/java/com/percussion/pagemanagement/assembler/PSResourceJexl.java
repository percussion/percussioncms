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
package com.percussion.pagemanagement.assembler;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <strong>For Documentation Please See: {@link PSResourceLinkAndLocationUtils}.</strong>
 * 
 * @author adamgent
 * @see PSResourceLinkAndLocationUtils
 */
public class PSResourceJexl implements IPSJexlExpression
{
    
    public String escapePathForUrl(String path) {
        return PSResourceLinkAndLocationUtils.escapePathForUrl(path);
    }
    
    public List<PSResourceLinkAndLocation> createLinkAndLocations() {
        return new ArrayList<PSResourceLinkAndLocation>();
    }
    public List<PSResourceLinkAndLocation> createDefaultLinkAndLocations(
            PSResourceScriptEvaluatorContext evalContext) {
        return PSResourceLinkAndLocationUtils.createDefaultLinkAndLocations(evalContext);
    }
    
    public PSResourceLinkAndLocation createLinkAndLocation(
            String filePath, String url) {
        return PSResourceLinkAndLocationUtils.createLinkAndLocation(filePath, url);
    }
    
    public PSResourceLinkAndLocation createLinkAndLocationForFileName(PSResourceScriptEvaluatorContext evalContext,
            String fileName)
    {
        return PSResourceLinkAndLocationUtils.createLinkAndLocationForFileName(evalContext.getResourceInstance(),
                fileName);
    }
    
    public String concatPath(String start, String ... end) {
        return PSResourceLinkAndLocationUtils.concatPath(start, end);
    }
    
    @Override
    public void init(@SuppressWarnings("unused") IPSExtensionDef extensionDef, 
            @SuppressWarnings("unused") File file)
    {     
    }
}
