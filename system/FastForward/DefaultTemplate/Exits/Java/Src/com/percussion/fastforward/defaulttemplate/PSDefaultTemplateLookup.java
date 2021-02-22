/******************************************************************************
 *
 * [ PSDefaultTemplateLookup.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.fastforward.defaulttemplate;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author adamgent
 * 
 */
public class PSDefaultTemplateLookup extends PSJexlUtilBase {
	
	@IPSJexlMethod(description = "A test method", params = 
	{ @IPSJexlParam(name = "first", type = "String", description = "first parameter") })
	public String test(String first) {
		return "testing param = " + first;
	}
	
	@IPSJexlMethod(description = "A test method", params = 
	{ @IPSJexlParam(name = "first", type = "int", description = "first parameter") })
	public int test2(int i) {
		return i + 1;
	}
	
	
	@IPSJexlMethod(description = "Looks up the default variant for an assembly item", 
			params = 
	{ @IPSJexlParam(name = "item", type = "IPSAssemblyItem", 
			description = "The item to find the default variant too.") })
	public String lookup(IPSAssemblyItem item) {
		List<IPSAssemblyTemplate> l = this.lookupDefaults(item);
		if (l.size() > 0){
			if (l.size() > 1){
				log.warn("More than one default variant arbitrarily selecting one");
			}
			return l.get(0).getName();
		}
		else {
			log.warn("Could not find default variant.");
			return null;
		}
		
	}
	
	/**
    * We would want to find the templates that are associated with the given
    * content type and site and publish when default.
    * 
    * To do this we take the intersection of the set templates associated with a
    * content type and the set of template associated with a site and then
    * filter out the ones that are not publish when default.
    * 
    * This may turn into a performance issue in which case Hibernate will have
    * to be used.
    * 
    * @param item work item, never <code>null</code>
    * @return a list of assembly templates, may be empty, but never 
    * <code>null</code>
    */
	public List<IPSAssemblyTemplate> lookupDefaults(IPSAssemblyItem item) {
		if (item == null) {
			throw new IllegalArgumentException("item may not be null");
		}

		try {
			IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
			IPSSiteManager sitem = PSSiteManagerLocator.getSiteManager();
			
			IPSGuid ctype = getContentTypeId(asm, item);
			IPSGuid siteid = getSiteId(item);
			
			List<IPSAssemblyTemplate> ct_templates = PSLocationUtils
               .findTemplatesByContentType(ctype);
			
			Set<IPSAssemblyTemplate> site_templates = 
				sitem.loadUnmodifiableSite(siteid)
                   .getAssociatedTemplates();
			
			if (site_templates.size() == 0) {
				log.error("No Templates Associated with the site.");
			}
			List<IPSAssemblyTemplate> defaults = 
				new ArrayList <> ();
			
			for (IPSAssemblyTemplate c_t : ct_templates) {
				
				if (isDefault(c_t) && site_templates.contains(c_t)) {
					defaults.add(c_t);
				}
			}
			return defaults;
		} 
		catch (PSAssemblyException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private IPSGuid getContentTypeId(IPSAssemblyService asm, IPSAssemblyItem item) {
		PSLegacyGuid itemLG = (PSLegacyGuid) item.getId();
		IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
		PSComponentSummary sum = cms
		.loadComponentSummary(itemLG.getContentId());
		return new PSGuid(PSTypeEnum.NODEDEF, sum.getContentTypeId());
	}
	
	private IPSGuid getSiteId(IPSAssemblyItem item) {
        String ssiteId = item.getParameterValue(
                IPSHtmlParameters.SYS_SITEID, null);
        return new PSGuid(PSTypeEnum.SITE, ssiteId);
        
	}
	
	private boolean isDefault(IPSAssemblyTemplate template) {
		return template.getPublishWhen() == IPSAssemblyTemplate.PublishWhen.Default;
	}

	Logger log = Logger.getLogger(this.getClass());
}
