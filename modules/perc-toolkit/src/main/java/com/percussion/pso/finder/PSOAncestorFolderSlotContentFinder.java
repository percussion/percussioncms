/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.finder;

import static java.text.MessageFormat.*;
import static java.util.Collections.*;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.pso.jexl.PSOFolderTools;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;

/**
 * Finds a single content item of a given content type in the current folder of the assembly item
 * or an ancestor folder. 
 * 
 * The finder first looks in the current folder and if not found then proceeds up the folder tree until an
 * item with the given content type is found. If no item is found the slot will be empty.
 * 
 * The slot will always have either one or zero items.
 * 
 * @author adamgent
 *
 */
public class PSOAncestorFolderSlotContentFinder extends PSBaseSlotContentFinder {
    
    private PSOFolderTools folderTools;
    private IPSContentWs contentWs;
    
    public static final String PARAM_CONTENTTYPE = "contenttype";
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(PSOAncestorFolderSlotContentFinder.class);
    
    /**
     * Constructor for Extensions Manager.
     */
    public PSOAncestorFolderSlotContentFinder() {
    
    }
    
    /**
     * Preferred Constructor for programatic use outside jexl.
     * @param contentWs Content web service.
     * @param folderTools folder tools.
     */
    public PSOAncestorFolderSlotContentFinder(
            IPSContentWs contentWs,
            PSOFolderTools folderTools) {
        super();
        init(contentWs, folderTools);
    }
    
    protected void init(IPSContentWs contentWs, PSOFolderTools folderTools) {
        this.contentWs = contentWs;
        this.folderTools = folderTools;
    }

    @Override
    public Set<SlotItem> getSlotItems(IPSAssemblyItem assemblyItem,
            IPSTemplateSlot slot, Map<String, Object> params)
            throws RepositoryException, PSFilterException, PSAssemblyException {
        Map<String, ? extends Object> args = slot.getFinderArguments();
        PSItemSummary sum = null;
        String contentType = getValue(args, params, PARAM_CONTENTTYPE, null);
        if ( contentType == null ) throw new IllegalArgumentException("contenttype cannot be null");
        
        try {
            sum = findAncestorItem(assemblyItem, contentType);

        } catch (Exception e) {
            log.error("Error finding ancestor item.");
            throw new RuntimeException("Error finding ancestor item: ", e);
        }
        if (sum == null) {
            log.warn(format("Did not find an ancestor item of type: {0}  for item: {1}", 
                    contentType, assemblyItem.getId()));
            return new HashSet<SlotItem>();
        }
        else if (log.isDebugEnabled()) {
            log.debug(format("Found ancestor item of type: {0} with id: {1} for item: {2}",
                    contentType, sum.getGUID(), assemblyItem.getId()));
        }
        
        HashSet<SlotItem> rvalue = new HashSet<SlotItem>();
        rvalue.add(new SlotItem(sum.getGUID(),null, 0));
        return rvalue;
        
    }
    
    
    private PSItemSummary findAncestorItem(IPSAssemblyItem assemblyItem, String contentType) 
        throws PSExtensionProcessingException, PSErrorResultsException, PSErrorException, PSCmsException {
        String path = getFolderTools().getParentFolderPath(assemblyItem);
        List<IPSGuid> folderIds =  getContentWs().findPathIds(path);
        reverse(folderIds);
        for(IPSGuid fid : folderIds) {
            List<PSItemSummary> sums = getContentWs().findFolderChildren(fid, false);
            for(PSItemSummary sum : sums) {
                if(contentType.equals(sum.getContentTypeName())) {
                    return sum;
                }
            }
        }
        return null;
    }

    public Type getType() {
        return Type.AUTOSLOT;
    }

    public void init(IPSExtensionDef extDef, File file)
            throws PSExtensionException {
        super.init(extDef, file);
        IPSContentWs cws = PSContentWsLocator.getContentWebservice();
        PSOFolderTools ft = new PSOFolderTools();
        ft.setContentWs(cws);
        ft.setGuidManager(PSGuidManagerLocator.getGuidMgr());
        init(cws, ft);
    }

    public PSOFolderTools getFolderTools() {
        return folderTools;
    }

    public void setFolderTools(PSOFolderTools folderTools) {
        this.folderTools = folderTools;
    }

    public IPSContentWs getContentWs() {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }



}
