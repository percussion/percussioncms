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
package com.percussion.pso.assembler;

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.util.IPSHtmlParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;


/**
 * This class can be used by content assemblers to validate rendered content 
 * with the JTIDY class library.
 * 
 * @author natechadwick
 */
public class ValidatingContentAssemblerMerge {

	  
	  /**
	    * Logger for this class
	    */
	   private static final Logger log = LogManager.getLogger(ValidatingContentAssemblerMerge.class);

	   private ValidatingContentAssemblerMerge(){
	   }
	   
	   /*
	    * Handles merging the assembled content and pushing them through JTIDY
	    */
	   public static IPSAssemblyResult merge(IPSExtensionDef def, IPSAssemblyResult result) 
	      throws Exception 
	   {
	      if(result.isDebug())
	      {
	         log.debug("Debug Assembly enabled"); 
	         return result;
	      }
	      	      
	      String configTidyPropertiesFile=null;
	      boolean configForcePublish=false;
	      boolean configDontFixMarkup=false;
	      boolean configFailPublishOnErrors=true;
	      boolean configFailPublishOnWarnings=false;
	      boolean configIsXMLContent = false;
	      boolean configIsXHTMLContent = true;
	      boolean configIsHTMLContent = false;
	      
	      log.debug("Init Params : {}" , def.getInitParameterNames());
	      //Load up our parameters
	      //Specifies the location of the Tidy properties file for this Assembler.
	      configTidyPropertiesFile = PSServer.getRxFile(def.getInitParameter("com.percussion.extension.assembly.TidyPropertiesFile"));
		  
	      //When set we tell Tidy to ignore errors and force output.
	      configForcePublish = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.ForcePublish"));

	      //When set we tell Tidy to do it's thing, but we don't want it trying to do any real cleanup for us
	      configDontFixMarkup = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.DontFixMarkup"));
	      
	      //If Tidy finds errors, we'll tell the CMS to fail the publish job for this item.
	      configFailPublishOnErrors = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.FailPublishOnErrors"));	      
	      
	      configFailPublishOnErrors = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.FailPublishOnWarnings"));	      
	       	  
	      configIsXMLContent = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.IsXMLContent"));	  
	    	  
	      //If set, we tell Tidy to treat the content as XHTML.
	      configIsXHTMLContent = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.IsXHTMLContent"));	  
	    	  
	      //If set, we tell Tidy to treat the content as HTML.
	      configIsHTMLContent = Boolean.parseBoolean(def.getInitParameter("com.percussion.extension.assembly.IsHTMLContent"));	  
	    	
	      //Get the context we are running in.
	      String contextStr = result.getParameterValue(IPSHtmlParameters.SYS_CONTEXT, null);
	      int context;
	      
	      if(contextStr == null || contextStr.equals("0"))
	      {
	         log.debug("Preview Mode");
	         context=0;
	      } else {
	    	 context=1; // Modify if we are checking for contexts > 1
	    	 log.debug("Publish Mode");
	      }
	     
	     
	      IPSAssemblyItem parent = result.getCloneParentItem();
	      
	      boolean isSnippet;
	      try{
	      if ((parent != null) && (result.getId()!= parent.getId())) {
	    	  log.debug("Not processing. Item {} has parent Item id={}",
					  result.getId() ,parent.getId());
	    	  isSnippet = true;
	      } else {
	    	  log.debug("No Parent Item Processing Item id={}", result.getId());
	    	  isSnippet = false;
	      }
	      }catch(Exception ex){
	      	log.error("Unexpected Error {} Snippet detection logic",
				  PSExceptionUtils.getMessageForLog(ex));}
	      finally{
	    	isSnippet=false;  //@TODO: FIGURE OUT WHY THIS IS CRASHING ON EVENTS
	      }
	     
	      
	      String mimeType = result.getMimeType();
	      String outputDoc = result.toResultString();
	      
	      PSAssemblyWorkItem work = (PSAssemblyWorkItem) result; 
	     
	      if (!isSnippet && context!=0) {

	    	  Tidy tidy = new Tidy();
	    	
	    	  tidy.setMakeClean(!configDontFixMarkup);
	    	  tidy.setOnlyErrors(!configFailPublishOnWarnings);
	    	  tidy.setQuiet(configForcePublish);
	    	  tidy.setShowWarnings(configFailPublishOnWarnings);
	    	  
	    	  if(configIsHTMLContent){
	    		  tidy.setXHTML(false);
	    		  tidy.setXmlOut(false);
	    	  }else{
	    		  tidy.setXHTML(configIsXHTMLContent);
	    		  tidy.setXmlOut(configIsXMLContent);
	    	  }
	    	  
	    	  log.debug("Loading Tidy Properties From: {} " , configTidyPropertiesFile);
	    	  tidy.setConfigurationFromFile(configTidyPropertiesFile);
	    	  
	    	  ByteArrayOutputStream errors_out = new ByteArrayOutputStream();
	    	  PrintWriter tidyErrors = new PrintWriter(errors_out);
	    	  tidy.setErrout(tidyErrors);    	  
	    	 
	    	  ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	
	    	  //Parse the content.
	    	  tidy.parse(result.getResultStream(),out);
	    	  
	    	  tidyErrors.flush();
	    	  tidyErrors.close();
	    	  
	    	  if((errors_out.size()>0)&&(configFailPublishOnErrors || configFailPublishOnWarnings)){	    		  
	    		  work.setResultData(errors_out.toString().getBytes(StandardCharsets.UTF_8));
	    		  work.setStatus(Status.FAILURE);
	    	  }else{
	    		  work.setResultData(out.toString().getBytes(StandardCharsets.UTF_8));
	    	  }
	    	  
	      } else {
	    	work.setResultData(outputDoc.getBytes(StandardCharsets.UTF_8));
	      }
	      
	      work.setMimeType(mimeType); 
	    
	      return work;
	   }

}
