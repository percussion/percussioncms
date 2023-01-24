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
import com.percussion.extension.PSExtensionException;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge.TemplateAndPage;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.PSAssemblyJexlEvaluator;
import com.percussion.services.assembly.impl.plugin.PSDatabaseAssembler;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This assembler is used to publish a page to a set of database tables.
 * It provides "$perc" binding variable (as {@link PSPageAssemblyContext}) 
 * in addition of other binding variables provided by {@link PSDatabaseAssembler}
 * 
 * @author YuBingChen
 */
public class PSPageDatabaseAssembler extends PSDatabaseAssembler
{
    private PSPageAssemblyContextFactory pageAssemblyContextFactory;
    private PSAssemblyItemBridge assemblyItemBridge;

    @Override
    public void init(IPSExtensionDef def, File file) throws PSExtensionException
    {
        super.init(def, file);
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#preProcessItemBinding(com.percussion.services.assembly.IPSAssemblyItem, com.percussion.services.assembly.impl.PSAssemblyJexlEvaluator)
     */
    public void preProcessItemBinding(IPSAssemblyItem pageItem, PSAssemblyJexlEvaluator eval) throws PSAssemblyException
    {
        try
        {
            // clone the page item, which may be polluted by the following process.
            IPSAssemblyItem assemblyItem = (IPSAssemblyItem) pageItem.clone();
            
            TemplateAndPage tp = assemblyItemBridge.getTemplateAndPage(assemblyItem);
            PSPageAssemblyContext context = getPageAssemblyContextFactory().createContext(assemblyItem, tp, false);
            
            eval.bind("$perc", context);
        }
        catch (Exception e)
        {
            String msg = "Failed to create page assembly context ($perc). The underlying error is: " + e.getMessage();
            log.error(msg, e);
            throw new PSAssemblyException(IPSAssemblyErrors.UNKNOWN_ERROR, e, msg);
        }
    }
    
    
    public PSPageAssemblyContextFactory getPageAssemblyContextFactory()
    {
        return pageAssemblyContextFactory;
    }

    public void setPageAssemblyContextFactory(PSPageAssemblyContextFactory pageAssemblyContextFactory)
    {
        this.pageAssemblyContextFactory = pageAssemblyContextFactory;
    }
    

    public PSAssemblyItemBridge getAssemblyItemBridge()
    {
        return assemblyItemBridge;
    }

    public void setAssemblyItemBridge(PSAssemblyItemBridge assemblyItemBridge)
    {
        this.assemblyItemBridge = assemblyItemBridge;
    }

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSPageDatabaseAssembler.class);
    

}
