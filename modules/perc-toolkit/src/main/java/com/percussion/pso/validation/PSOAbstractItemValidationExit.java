/*******************************************************************************
 * (c) 2005-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemValidator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.workflow.IPSOWorkflowInfoFinder;
import com.percussion.pso.workflow.PSOWorkflowInfoFinder;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.workflow.data.PSState;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * Base class for Item Validation exits. Provides generic routines for handling of 
 * error documents, fields and lookup of destination workflow states. 
 *
 * @author DavidBenua
 *
 */
public abstract class PSOAbstractItemValidationExit
      extends PSOItemXMLSupport
      implements
         IPSItemValidator,
         IPSResultDocumentProcessor
{
   private static Log log = LogFactory.getLog(PSOAbstractItemValidationExit.class); 
  
   private IPSOWorkflowInfoFinder finder = null; 
   /**
    * Default constructor.
    */
   public PSOAbstractItemValidationExit()
   {
     
   }
   
   /**
    * Initialize the service pointers. 
    */
   private void initServices()
   {
      if(finder == null)
      {
         finder = new PSOWorkflowInfoFinder();
      }
   }
   /**
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   /**
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[], com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
//      if(log.isTraceEnabled())
//      {
//         String idoc = PSXmlDocumentBuilder.toString(resultDoc);
//         log.trace("result doc is " + idoc); 
//      }
      Document errorDoc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         validateDocs(resultDoc, errorDoc,  request, params);
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }
      if(hasErrors(errorDoc))
      {
         log.debug("validation errors found"); 
         return errorDoc;
      }
      log.debug("validation successful"); 
      return null;    
   }
   
   protected abstract void validateDocs(Document inputDoc, Document errorDoc, IPSRequestContext req, Object[] params)
     throws Exception;
   
   
   /**
    * Determines if an error document contains errors. 
    * @param errorDoc the error document 
    * @return <code>true</code> if there are any errors. 
    */
   protected boolean hasErrors(Document errorDoc)
   {
      Element root = errorDoc.getDocumentElement();
      if(root == null)
      {
         return false;
      }
      PSXmlTreeWalker w = new PSXmlTreeWalker(root);
      Element e = w.getNextElement(PSItemErrorDoc.ERROR_FIELD_SET_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if(e == null)
      {
         return false;
      }
      e = w.getNextElement(PSItemErrorDoc.ERROR_FIELD_ELEM, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if(e == null)
      {
         return false;
      }
      return true; 
   }
   
   /**
    * Matches the current item workflow state and transition ids with a comma delimited list of workflow 
    * state names. 
    * This method will return true if the destination state matches one of the listed states.  If the state
    * list is blank or contains a single "*" it is assumed to match all states. 
    * @param contentid the content id of the item 
    * @param transitionid the transition id
    * @param allowedStates the list of destination state names that match. 
    * @return <code>true</code> when a match occurs,<code>false</code> otherwise. 
    * @throws PSException 
    */
   protected boolean matchDestinationState(String contentid, String transitionid, String allowedStates) throws PSException
   {
      if(StringUtils.isBlank(allowedStates))
      { //match everything
         return true;
      }
      if(allowedStates.trim().equals("*"))
      {
         return true; 
      }
      initServices();
      List<String> allowed = splitAndTrim(allowedStates);
      PSState state = finder.findDestinationState(contentid, transitionid);
      if(state == null)
      {
    	  log.warn("Workflow state not found for item " + contentid); 
    	  return false; //assume no match. 
      }
      return allowed.contains(state.getName())? true : false;  
   }
   
   protected List<String> splitAndTrim(String input)
   {
	   return splitAndTrim(input,","); 
   }
   protected List<String> splitAndTrim(String input, String delimiter)
   {
	   List<String> result = new ArrayList<String>(); 
	   if(StringUtils.isBlank(input))
		   return result; 
	   String[] parts = input.split(delimiter); 
	   for(String part : parts)
	   {
		   if(!StringUtils.isBlank(part))
		   {
			   result.add(part.trim()); 
		   }
	   }
	   return result; 
	   
   }
   /**
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

   /**
    * @param finder the finder to set. Used only for unit test.
    */
   protected void setFinder(IPSOWorkflowInfoFinder finder)
   {
      this.finder = finder;
   }
}
