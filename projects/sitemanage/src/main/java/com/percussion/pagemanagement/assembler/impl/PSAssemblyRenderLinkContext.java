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

