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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.assembler;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge;
import com.percussion.pagemanagement.assembler.impl.PSAssemblyItemBridge.TemplateAndPage;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.impl.plugin.PSVelocityAssembler;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.jexl.PSJexlEvaluator;
import org.springframework.util.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * The entry point for the assembly service to assemble pages
 * or templates. The assembler can determine if its rendering a page
 * or template safely.
 * 
 * @see PSPageAssemblyContextFactory
 * @author adamgent
 *
 */
public class PSPageAssembler extends PSVelocityAssembler
{

    private PSPageAssemblyContextFactory pageAssemblyContextFactory;
    private PSAssemblyItemBridge assemblyItemBridge;

    

    @Override
    public void init(IPSExtensionDef def, File file) throws PSExtensionException
    {
        super.init(def, file);
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }

    /**
     * Gets the template source (or content) from the specified item.
     * @param assemblyItem the item used to retrieve the template.
     * @return the template source, not blank.
     */
    protected String getTemplateSource(IPSAssemblyItem assemblyItem)
    {
        IPSAssemblyTemplate template = assemblyItem.getTemplate();
        return template.getTemplate();
    }
    
    /**
     * This calls {@link PSPageAssemblyContextFactory#createContext(IPSAssemblyItem, TemplateAndPage, boolean) PSPageAssemblyContextFactory.createContext(IPSAssemblyItem, TemplateAndPage, true)}
     */
    protected PSPageAssemblyContext createContext(IPSAssemblyItem assemblyItem, TemplateAndPage templateAndPage) throws Exception
    {
        return getPageAssemblyContextFactory().createContext(assemblyItem, templateAndPage, true);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected IPSAssemblyResult doAssembleSingle(IPSAssemblyItem assemblyItem) throws Exception
    {
        StopWatch sw = new StopWatch("#doAssemblySingle");
        sw.start("templateAndPage");
        TemplateAndPage tp = assemblyItemBridge.getTemplateAndPage(assemblyItem);
        sw.stop();
        sw.start("createContext");
        PSPageAssemblyContext context = createContext(assemblyItem, tp);
        sw.stop();
        
        PSJexlEvaluator eval =  PSJexlUtils.getBindings(assemblyItem);
        eval.bind("$perc", context);
        String templateSource = getTemplateSource(assemblyItem); 
        eval.bind("$sys.template", templateSource);
        
        assemblyItem.setBindings(eval.getVars());
        
        sw.start("assemble");
        IPSAssemblyResult result = super.doAssembleSingle(assemblyItem);
        sw.stop();
        
        log.debug(sw.prettyPrint());
        
        return result;
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

    private static final Logger log = LogManager.getLogger(PSPageAssembler.class);
    

}
