package com.percussion.pso.relationshipbuilder;


import static java.util.Collections.singleton;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.util.IPSHtmlParameters;

public abstract class PSActiveAssemblyRelationshipBuilder extends
        PSRelationshipBuilder {
	
	  /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log ms_log = LogFactory
            .getLog(PSActiveAssemblyRelationshipBuilder.class);
    
	private String slotName;
	private String templateName;
	
	 private IPSAssemblyService m_assemblyService;
	 public PSActiveAssemblyRelationshipBuilder() {
		// TODO Auto-generated constructor stub
	}
	 
	 public void init() {
	        if (m_assemblyService == null)
	            m_assemblyService = PSAssemblyServiceLocator.getAssemblyService();
	        super.init();
	 }
	 
	protected void setupFilter() throws PSAssemblyException, PSException {
		 if ( slotName != null ) {
	    	   IPSTemplateSlot slot = findSlot(slotName);
	    	   super.getFilter().setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slot
              .getGUID().longValue()));
	       }
	       
         if ( templateName != null ) {
      	  IPSAssemblyTemplate template = findTemplate(templateName);
            super.getFilter().setProperty(IPSHtmlParameters.SYS_VARIANTID, String.valueOf(template
                  .getGUID().longValue()));   
         }
         super.setupFilter();
	 }
	 public String getSlotName() {
			return slotName;
		}

		public void setSlotName(String slotName) {
			this.slotName = slotName;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}
		
		  public void addRelationships(
		            Collection<Integer> ids) throws PSAssemblyException, PSException {
		        IPSAssemblyTemplate template = findTemplate(templateName);
		        IPSTemplateSlot slot = findSlot(slotName);
		        validateSlot(slot);
		        Collection<PSLocator> ownerLocators = isParent() ? asLocators(singleton(getId())) : asLocators(ids);
		        Collection<PSLocator> dependentLocators = isParent() ? asLocatorsNoRev(ids) : asLocatorsNoRev(singleton(getId()));
		        Collection<PSRelationship> relationshipSet = createEmptyRelationshipCollection();
		        for (PSLocator ownerLoc : ownerLocators) {
		            for (PSLocator dependentLoc : dependentLocators) {
		                PSAaRelationship newRelationship = new PSAaRelationship(ownerLoc,
		                        dependentLoc, slot, template);
		                ms_log.debug("Adding relation Owner id="+ownerLoc.getId()+" Owner Revision="+ownerLoc.getRevision());
		                ms_log.debug("Adding relation Dependent id="+dependentLoc.getId()+" Dependent Revision="+dependentLoc.getRevision());
		                
		                relationshipSet.add(newRelationship);
		            }
		        }
		        saveRelationships(relationshipSet);
		        
		    }
		  
		   /**
		     * Validates that the slot is setup correctly to add relationships to. Emits
		     * log messages to help the user find errors.
		     * 
		     * @param slot
		     * @return 0 if successful, non-zero otherwise.
		     */
		    private int validateSlot(IPSTemplateSlot slot)
		    {
		       int rvalue = 1;
		       if (slot.getRelationshipName() == null
		             || StringUtils.isBlank(slot.getRelationshipName()))
		       {
		          ms_log
		                .warn("The slot does not have relationship name set."
		                      + "The relationship name should be active assembly."
		                      + "Check the Slot type table to make sure the relationship name is set.");
		          rvalue = 1;
		       }
		       else
		       {
		          rvalue = 0;
		       }
		       
		       return rvalue;
		    }
	  /**
	     * Finds the definition for a slot given its name, using the assembly
	     * service.
	     * 
	     * @param templateName name of the template to find. not <code>null</code>,
	     *           must exist.
	     * @throws PSAssemblyException if the template is not found
	     */
	    private IPSAssemblyTemplate findTemplate(String templateName)
	            throws PSAssemblyException {
	     	if (m_assemblyService == null) init();
	        return m_assemblyService.findTemplateByName(templateName);
	    }

	    /**
	     * Finds the definition for a slot given its name, using the assembly
	     * service.
	     * 
	     * @param slotname name of the slot to find. not <code>null</code>, must
	     *           exist.
	     * @return the slot definition for the specified name
	     * @throws PSAssemblyException propagated from assembly service if the slot
	     *            is not found
	     */
	    private IPSTemplateSlot findSlot(String slotname) throws PSAssemblyException {
	    	if (m_assemblyService == null) init();
	        return m_assemblyService.findSlotByName(slotname);
	    }
 
}
