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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.impl.plugin.PSAssemblerBase;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

/**
 * A dispatch like assembler that assembles resources.
 * <p>
 * Currently the assembler delegates to the assembly template
 * that is registered to the resource definition 
 * ({@link PSAssetResource#getLegacyTemplate()}).
 * <p>
 * However in the future the resource assembler will use the template code from
 * the resource itself.
 *  
 * @author adamgent
 *
 */
public class PSResourceAssembler extends PSAssemblerBase implements IPSAssembler
{

    private PSAssemblyItemBridge assemblyItemBridge;
    private IPSAssemblyService assemblyService;
    
    @Override
    public IPSAssemblyResult assembleSingle(IPSAssemblyItem assemblyItem)
    {
        try
        {
            if ( ! isExpanded(assemblyItem)) {
                /*
                 * This assembly item has not been expanded yet.
                 */
                getAssemblyItemBridge().setAssemblyResultExpander(assemblyItem);
                assemblyItem.setPaginated(true);
                return getUnexpandedResult(assemblyItem);
            }
            
            PSResourceInstance r = getAssemblyItemBridge().createResourceInstance(assemblyItem);
            getAssemblyItemBridge().setResourceInstance(assemblyItem, r);
            
            
            String templateName = r.getResourceDefinition().getLegacyTemplate();
            IPSAssemblyTemplate template = getAssemblyService().findTemplateByName((String) templateName);
            if (template == null)
            {
               return getFailureResult(assemblyItem, "could not find template information");
            }
            
            assemblyItem.setTemplate(template);
            
            List<IPSAssemblyItem> items = new ArrayList<>();
            items.add(assemblyItem);
            List<IPSAssemblyResult> results = getAssemblyService().assemble(items);
            return results.get(0);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
        
    }
    
    private boolean isExpanded(IPSAssemblyItem work)
    {
        return getAssemblyItemBridge().getResourceDefinitionId(work) != null;
    }
    
    
    protected IPSAssemblyResult getUnexpandedResult(IPSAssemblyItem work)
    {
        String message = "The assembly item did not have a resource definition id.";
        work.setStatus(Status.SUCCESS);
        work.setMimeType("text/plain");
        try
        {
            work.setResultData(message.getBytes("UTF8"));
            return (IPSAssemblyResult) work;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e); // not possible
        }
    }



    public PSAssemblyItemBridge getAssemblyItemBridge()
    {
        return assemblyItemBridge;
    }

    public void setAssemblyItemBridge(PSAssemblyItemBridge assemblyItemBridge)
    {
        this.assemblyItemBridge = assemblyItemBridge;
    }

    public IPSAssemblyService getAssemblyService()
    {
        return assemblyService;
    }


    public void setAssemblyService(IPSAssemblyService assemblyService)
    {
        this.assemblyService = assemblyService;
    }

    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException
    {
        super.init(def, codeRoot);
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    
    


}

