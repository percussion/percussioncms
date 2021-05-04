/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imp.assembler;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.pso.restservice.IItemRestService;
import com.percussion.pso.restservice.ItemRestServiceLocator;
import com.percussion.pso.restservice.model.Item;
import com.percussion.pso.restservice.utils.ItemServiceHelper;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class can be used by content assemblers to validate rendered content 
 * with the JTIDY class library.
 * 
 * @author natechadwick
 * @version $Revision: 1.0 $
 */
public class ImportContentAssemblerMerge {

	  
	  /**
	    * Logger for this class
	    */
	   private static final Log log = LogFactory.getLog(ImportContentAssemblerMerge.class);

	   /**
	    * Constructor for ImportContentAssemblerMerge.
	    */
	   private ImportContentAssemblerMerge(){
	   }
	   
	   /*
	    * Handles merging the assembled content and pushing them through JTIDY
	    */
	   /**
	    * Method merge.
	    * @param def IPSExtensionDef
	    * @param result IPSAssemblyResult
	    * @return IPSAssemblyResult
	    * @throws Exception
	    */
	   public static IPSAssemblyResult merge(IPSExtensionDef def, IPSAssemblyResult result) 
	      throws Exception 
	   {
	      if(result.isDebug())
	      {
	         log.debug("Debug Assembly enabled"); 
	         return result;
	      }
	      	      
	      
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
	    	  log.debug("Not processing. Item "+ result.getId() + " has parent Item id="+parent.getId());
	    	  isSnippet = true;
	      } else {
	    	  log.debug("No Parent Item Processing Item id="+ result.getId());
	    	  isSnippet = false;
	      }
	      }catch(Exception ex){log.debug("Unexpected Error " + ex.getMessage() + " Snippet detection logic");}
	      finally{
	    	isSnippet=false;  //@TODO: FIGURE OUT WHY THIS IS CRASHING ON EVENTS
	      }
	     
	      
	      String mimeType = result.getMimeType();
	      String outputDoc = result.toResultString();
	      
	      PSAssemblyWorkItem work = (PSAssemblyWorkItem) result; 
	     
	      if (!isSnippet) {
	    	log.debug("Parsing item xml");
	    	result.getResultStream();
	    	ItemRestServiceLocator.getItemServiceBase();
	    	IItemRestService itemService = ItemRestServiceLocator.getItemServiceBase();
	    	Item item = ItemServiceHelper.getItemFromXml(result.getResultStream());
	    	
	    	
	    	
	    	
	    	if (item!=null) {
	    		log.debug("Xml In:" +ItemServiceHelper.getItemXml(item));
	    		Item res = itemService.updateItem(item);
	    		
	    		log.debug("Xml Out:" +ItemServiceHelper.getItemXml(res));
	    		work.setResultData(ItemServiceHelper.getItemXml(res).getBytes());
	    	} else {
	    		log.debug("Cannot parse import item");
	    	}
	    	  
	      } else {
	    	work.setResultData(outputDoc.getBytes("UTF-8"));
	      }
	      
	      work.setMimeType(mimeType); 
	    
	      return work;
	   }

}
