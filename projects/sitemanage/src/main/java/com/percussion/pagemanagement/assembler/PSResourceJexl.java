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
        return new ArrayList<>();
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
