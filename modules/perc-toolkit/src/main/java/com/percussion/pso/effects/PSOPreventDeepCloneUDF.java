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
package com.percussion.pso.effects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;

import java.util.Arrays;
import java.util.List;

/**
 * Allocates the next number in a sequence. 
 *
 * @author davidbenua,stephenbolton,natechadwick
 *
 */
public class PSOPreventDeepCloneUDF extends PSSimpleJavaUdfExtension
      implements
         IPSUdfProcessor
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSOPreventDeepCloneUDF.class);
   

	private static IPSWorkflowService wf;

	private static IPSCmsContentSummaries sumsvc;


	  private static void initServices()
	   {
	      if(wf == null)
	      {
	    		
	    		wf = PSWorkflowServiceLocator.getWorkflowService();
	    		sumsvc = PSCmsContentSummariesLocator.getObjectManager();
	      }
	   }
	
	
   /**
    * 
    */
   public PSOPreventDeepCloneUDF()
   {
      super();
   }
   /**
    * Checks for deep cloning exclusion flag in private object.  This 
    * is used in the deep cloning conditionals
    * <code>params[0]</code>.  
    * @param params the parameter array
    * @param request the callers request context
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[], com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {
	   	
	   initServices();
	   log.info("Filtering items for revision" );
	
	   
	   String stateList = getParameter(params,0,""); 
	      if (stateList == null)
	         throw new PSConversionException(0,
	            "states parameter name cannot be null" );
	      
	  
	   	List<String> states = Arrays.asList(stateList.split(","));
	   	log.debug("Deep cloning configured for states {}", states);
	   	String contentid = request.getParameter("sys_contentid");
	   	log.debug("Checking deep cloning for id {}", contentid);
	   	
	   	int id = Integer.parseInt(contentid);
	   	
	   	
	      PSComponentSummary sum = sumsvc.loadComponentSummary(id);
	      if(sum == null)
	      {
	         String emsg = "Content item not found "+ id;
	         log.error(emsg); 
	         throw new PSConversionException(0,emsg);
	      }
	      
	   	log.debug("Got summary for id {}", id);

	   	int wfid = sum.getWorkflowAppId();
	   	int stateid = sum.getContentStateId();
	   	
	   	//Always clone non workflow objects.
	   	if(wfid > 0 && stateid > 0){
	   	
		   	
		   	String wfName = getWorkflowName(wfid);
		   	String stateName = getStateName(wfid, stateid);
		   	
		   	log.debug("Item is in workflow= {} state={}", wfName, stateName);
		   
		   	if (states.contains(stateName)) {
		   		log.debug("Found item state in deep clone list ");
		   		return true;
		   	}
		   	
	   	}else{
	   		log.debug("Item is not workflowable..skipping.");
	   		return true;
	   	}
	   return false;
   }
   
   private String getWorkflowName(int id) {
		initServices();
		log.debug("Getting workflow name");
		PSWorkflow workflow = wf.loadWorkflow(new PSGuid(PSTypeEnum.WORKFLOW,id));
		log.debug("got workflow name "+workflow.getName());
		return workflow.getName();
	}
   
	private String getStateName(int wfid,int stateid) {
		initServices();
		log.debug("Getting state name");
		PSState state = wf.loadWorkflowState(new PSGuid(PSTypeEnum.WORKFLOW_STATE,stateid),new PSGuid(PSTypeEnum.WORKFLOW,wfid));
		log.debug("Got state name"+state.getName());
		return state.getName();
	}
	
	    /**
	    * Safely gets the specified index of the parameter array as a String.
	    * The default value will be returned if parameter array is null, or does not
	    * contain a non-empty string at the specified index.
	    *
	    * @param params array of parameter objects from the calling function.
	    * @param index specifies which parameter from the array will be returned
	    * @param defaultValue returned if array does not have a non-empty
	    * string at the specified index.
	    *
	    * @return the parameter at the specified index (converted to a String and
	    * trimmed), or the defaultValue.
	    */
	   private static String getParameter(Object[] params, int index,
	                                     String defaultValue) {
	      if (params == null || params.length < index + 1 || params[index] == null ||
	            params[index].toString().trim().length() == 0) {
	         return defaultValue;
	      } else {
	         return params[index].toString().trim();
	      }
	   }

}
