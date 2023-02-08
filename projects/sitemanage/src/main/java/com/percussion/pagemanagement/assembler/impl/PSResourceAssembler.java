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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.plugin.PSAssemblerBase;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.percussion.services.assembly.PSAssemblyException.UNEXPECTED_ASSEMBLY_ERROR;

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
    public IPSAssemblyResult assembleSingle(IPSAssemblyItem assemblyItem) throws PSAssemblyException {
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
            throw new PSAssemblyException(UNEXPECTED_ASSEMBLY_ERROR,e);
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

        work.setResultData(message.getBytes(StandardCharsets.UTF_8));
        return (IPSAssemblyResult) work;
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

