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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final Log log = LogFactory.getLog(PSPageDatabaseAssembler.class);
    

}
