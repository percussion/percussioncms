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
package com.percussion.pagemanagement.assembler.impl;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;

import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.sitemanage.data.PSSiteSummary;

/**
 * A linking context that contains legacy assembly information.
 * @author adamgent
 *
 */
public class PSAssemblyRenderLinkContext extends PSRenderLinkContext
{
    
    @NotNull
    private PSSiteSummary site;
    
    @NotBlank
    @NotNull
    private String filter;
    
    @NotNegative
    @NotNull
    private Number legacyLinkContext;
    
    @NotNegative
    @NotNull
    private Number legacyFileContext;
    
    @Override
    public PSSiteSummary getSite()
    {
        return site;
    }
    public void setSite(PSSiteSummary site)
    {
        this.site = site;
    }
    @Override
    public Mode getMode()
    {
        if (legacyLinkContext == null || 
        		legacyLinkContext.intValue() == 0) {
            return Mode.PREVIEW;    
        }
        return Mode.PUBLISH;
        
    }
    public void setMode(Mode mode)
    {
        if(Mode.PUBLISH == mode) {
            legacyFileContext = 10;
            legacyLinkContext = 20;
        }
        else {
            legacyFileContext = 0;
            legacyLinkContext = 0;
        }
    }
    
    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    /**
     * This is the assembly context which is normally <code>sys_context</code>
     * or <code>sys_assembly_context</code>.
     * parameter in the assembly url.
     * @return never <code>null</code>.
     */
    public Number getLegacyLinkContext()    
    {
        return legacyLinkContext;
    }

    public void setLegacyLinkContext(Number legacyLinkContext)
    {
        this.legacyLinkContext = legacyLinkContext;
    }

    /**
     * This is the delivery context which is the context used to generate file locations in 
     * in the content list generator.
     * @return never <code>null</code>.
     */
    public Number getLegacyFileContext()
    {
        return legacyFileContext;
    }

    public void setLegacyFileContext(Number legacyFileContext)
    {
        this.legacyFileContext = legacyFileContext;
    }
    
    
}

