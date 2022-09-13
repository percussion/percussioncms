/*******
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * @author AdamGent
 * @author DavidBenua
 */
package com.percussion.soln.utilities.rx.jexl;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.util.IPSHtmlParameters;

/**
 * Utilities for dispatch templates.  These utilities will dispatch to a template based on a content
 * type. The originating item may be specified as an assembly item, or by content type name.  
 * <p>
 * The user must specify a map that contains 0 or more content types with the name of the template 
 * to be used for each content type. This is the name that will be returned if an item of that content
 * type is dispatched.  If the content type does not match, the default content type name will be returned 
 * instead. 
 * 
 * @author AdamGent
 * @author DavidBenua
 *
 */
public class DispatchTemplateUtil extends SolnJexlBase {

    
    private static IPSAssemblyService assemblyService = null; 
    
    /**
     * Logger for this class.
     */
    private static Log log = LogFactory.getLog(DispatchTemplateUtil.class); 
    
    /**
     * Initialize service pointers. 
     */
    private static void initServices()
    {
       if(assemblyService == null)
       {
          assemblyService = PSAssemblyServiceLocator.getAssemblyService();
       }
    }
    
    /**
     * Picks a template to dispatch to. 
     * @param item the assembly item to dispatch.
     * @param templateMap the map templates by content type. The key of the map is the content type name, 
     * and the value of the map is the desired template name for that type. 
     * @param defaultTemplate the default template if no matching content type names is found. 
     * @return the name of the selected template. 
     * @throws RepositoryException
     */
    @IPSJexlMethod(description = "Picks the template to dispatch to.", params =
    {
          @IPSJexlParam(name = "assemblyItem", description = "$sys.assemblyItem"),
          @IPSJexlParam(name = "templateMap", description = "The template map of content type to template name."),
          @IPSJexlParam(name = "defaultTemplate", description = "the base url to append the parameters to")
    }, returns = "String")
    public String pickTemplate(IPSAssemblyItem item, Map<String,Object> templateMap, String defaultTemplate) 
        throws RepositoryException 
    {
        if (item == null) throw new IllegalArgumentException("item cannot be null");
        return pickTemplateByType(((IPSNodeDefinition)item.getNode().getDefinition()).getInternalName(), 
                templateMap, defaultTemplate);
    }
    
    /**
     * Picks a template from the map based on the content type name. 
     * @param contentType the content type name. 
     * @param templateMap the map templates by content type. The key of the map is the content type name, 
     * and the value of the map is the desired template name for that type. 
     * @param defaultTemplate the default template if no matching content type names is found.
     * @return the name of the template, or the default name, if not found. 
     */
    @IPSJexlMethod(description = "Picks the template to dispatch to.", params =
    {
            @IPSJexlParam(name = "contenttype", description = "content type name"),
            @IPSJexlParam(name = "templateMap", description = "The template map of content type to template name."),
            @IPSJexlParam(name = "defaultTemplate", description = "the base url to append the parameters to")
    }, returns = "String")
    public String pickTemplateByType(String contentType, Map<String,Object> templateMap, String defaultTemplate) 
    {
        if (templateMap == null) throw new IllegalArgumentException("templateMap cannot be null");
        Object template = templateMap.get(contentType);
        log.debug("pickTemplateByType: return is " + template); 
        return template == null ? defaultTemplate : template.toString();
    }
    
    /**
     * Gets the template name based on the parameter map. Only the <code>sys_variantid</code> is used.  
     * @param params the parameter map. 
     * @return the template or <code>null</code> if the template could not be found. 
     * @throws PSAssemblyException
     */
    @IPSJexlMethod(description = "Gets the template name from the location scheme parameters ($sys.params)", 
            params =
    {
            @IPSJexlParam(name = "parameters", description = "$sys.params")
    }, returns = "IPSAssemblyTemplate")
    public IPSAssemblyTemplate getLocationSchemeTemplate(Map<String,Object> params) 
        throws PSAssemblyException 
    {
        initServices();
        String variantid = (String) params.get(IPSHtmlParameters.SYS_VARIANTID);
        if (variantid != null) {
            return assemblyService.loadUnmodifiableTemplate(variantid);
        }
        return null;
    }

    /**
     * Generates a location for the default template based on a target item. If the location cannot be generated 
     * because no default template exists, the default location will be returned instead.  
     * @param targetItem the assembly item to generation.
     * @param defaultLocation the default returned when there is not location to generate. 
     * @return the generated location or the default location. 
     */
    @IPSJexlMethod(description = "generate a  default template url from the target item. The resulting url will be escaped for use in xhtml/xml.", params =
    {   @IPSJexlParam(name = "targetItem", description = "the target assembly item"),
        @IPSJexlParam(name = "defaultLocation", description = "location returned on failing to find default template.")
    }, returns = "The generated location if no failure and defaultLocation on failure")
    public String safeLocationGenerate(IPSAssemblyItem targetItem, String defaultLocation) {
        PSLocationUtils utils = new PSLocationUtils();
        try {
            return utils.generate(targetItem, utils.findDefaultTemplate(targetItem));
        } catch (PSAssemblyException e) {
            return defaultLocation;
        }
    }
    
  
    /**
     * Gets the assembly service.  Used for unit testing only. 
     * @return the assembly service
     */
    public IPSAssemblyService getAssemblyService() {
        return assemblyService;
    }

    /**
     * Sets the assembly service. Used for unit testing only. 
     * @param assemblyService the assembly service to set. 
     */
    public void setAssemblyService(IPSAssemblyService assemblyService) {
        DispatchTemplateUtil.assemblyService = assemblyService;
    }
    
}
