/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge.TemplateAndPage;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * This is used to assemble a page. Similar with {@link PSPageAssembler}, 
 * but this will use a predefined template to render the page, it does not
 * use the template that is registered to the page.
 * The template ID can be specified by {@link com.percussion.util.IPSHtmlParameters#SYS_VARIANTID}
 * or {@link com.percussion.util.IPSHtmlParameters#SYS_TEMPLATE}
 * <p>
 * This assembler is typically used to render a page in non-HTML format, such as XML.
 * 
 * @author YuBingChen
 */
public class PSPageVariateAssembler extends PSPageAssembler
{
    /**
     * This calls {@link PSPageAssemblyContextFactory#createContext(IPSAssemblyItem, TemplateAndPage, boolean) PSPageAssemblyContextFactory.createContext(IPSAssemblyItem, TemplateAndPage, false)}
     */
    protected PSPageAssemblyContext createContext(IPSAssemblyItem assemblyItem, TemplateAndPage templateAndPage) throws Exception
    {
        return getPageAssemblyContextFactory().createContext(assemblyItem, templateAndPage, false);
    }
    

    @Override
    protected String getTemplateSource(IPSAssemblyItem assemblyItem)
    {
        IPSAssemblyTemplate template = getAssemblyTemplate(assemblyItem);
        if (template != null) {
            return template.getTemplate();
        }
        else {
            return super.getTemplateSource(assemblyItem);
        }
    }

    /**
     * Gets the specified template {@link com.percussion.util.IPSHtmlParameters#SYS_VARIANTID}
     * 
     * @param assemblyItem the item, assumed not <code>null</code>.
     * 
     * @return the template, it may be <code>null</code> if cannot find the specified template.
     */
    private IPSAssemblyTemplate getAssemblyTemplate(IPSAssemblyItem assemblyItem)
    {
        String variantid = assemblyItem.getParameterValue(IPSHtmlParameters.SYS_VARIANTID, null);        
        String sys_template = assemblyItem.getParameterValue(IPSHtmlParameters.SYS_TEMPLATE, null);
        if (isBlank(variantid) && isBlank(sys_template))
        {
            throw new IllegalStateException("Cannot find template ID from the parameter of \"" 
                    + IPSHtmlParameters.SYS_VARIANTID + "\" or \"" + IPSHtmlParameters.SYS_TEMPLATE + "\"");
        }
        
        try
        {
            IPSAssemblyService service = PSAssemblyServiceLocator.getAssemblyService();
            int id = variantid != null ? Integer.parseInt(variantid) : Integer.parseInt(sys_template);
            IPSGuid guid = new PSGuid(PSTypeEnum.TEMPLATE, id);
            return service.findTemplate(guid);
        }
        catch (Exception e)
        {
            String errorMsg = "Failed to find template, variantid = " + variantid;
            log.error(errorMsg, e);
            
            return null;
        }
    }
    

    private static final Logger log = LogManager.getLogger(PSPageVariateAssembler.class);
}
