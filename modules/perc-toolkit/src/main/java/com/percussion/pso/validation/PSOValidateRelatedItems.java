/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.validation;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemValidator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSItemErrorDoc;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Purpose of this class is to validate the existance of items in a slot.
 * Through an internal resource pointing to a query resource this class
 * retrieves a xml dom doc and locates the slot names occuring therein. It then
 * checks it against the parameters passed to the processResultDocument.
 * 
 * Parameters must be passed in the following order:
 * <ol>
 * <li>url to query resource</li>
 * <li>transitionid against which to validate</li>
 * <li>variantid of the parent document to validate</li>
 * <li>slotname of required items</li>
 * <li>occurance parameters MUST be one of the following: "MORE_THAN" or
 * "LESS_THAN" or "EXACTLY"</li>
 * <li>number to correspond to parameter 5</li>
 * </ol>
 * Read 3 ,4, 5 and 6 as follows: VARIANTID with SLOT_NAME occurs MORE_THAN 5
 * 
 * To accomodate more validations on the related item simple repeat 3, 4 , 5 and
 * 6 arguments.
 * 
 * <p>
 * In the latest version, the URL and Variant id are not longer needed, as this
 * version uses the Relationship API rather than making an internal request.
 * </p>
 * 
 * @author Scott M. Morales
 * @version 1.1 - added the ability to wild card the transitionid. It will check
 *          for a number and act on that transition only or a * and act on all.
 * @version 2.0 - rewritten to use relationship API instead of making internal
 *          requests. The URL and VariantId are no longer used.
 * @version 2.1 - modified to only fire on workflow requests
 */

public class PSOValidateRelatedItems extends PSDefaultExtension implements
		IPSResultDocumentProcessor, IPSItemValidator 
  {
   /**
    * Logger for this class
    */
   private static final Log logger = LogFactory.getLog(PSOValidateRelatedItems.class);

    
   private static IPSGuidManager gmgr = null; 
   private static IPSContentWs cws = null; 
	/**
	 * Indicates slot must contain more than a number of related content.
	 */
	private final static String MORE_THAN = "MORE_THAN";

	/**
	 * Indicates less than a number of related content.
	 */
	private final static String LESS_THAN = "LESS_THAN";

	/**
	 * Indicates exactly the number of related content.
	 */
	private final static String EXACTLY = "EXACTLY";

	/**
	 * Check to see if the slotName exists in the map. If it does, makes a quick
	 * check against the slotName mustOccur numoftimes values.
	 * 
	 * @param slotSize -
	 *            the actual size of the slot.
	 * @param mustOccur -
	 *            the mustOccur operation value.
	 * @param numoftimes -
	 *            the number of times value.
	 * @throws PSParameterMismatchException
	 *             when mustOccur is not MORE_THAN, LESS_THAN or EXACTLY.
	 */
	protected boolean isValid(int slotSize, String mustOccur, int numoftimes)
			throws PSParameterMismatchException {
		if (mustOccur.equalsIgnoreCase(MORE_THAN))
			return slotSize > numoftimes;
		else if (mustOccur.equalsIgnoreCase(LESS_THAN))
			return slotSize < numoftimes;
		else if (mustOccur.equalsIgnoreCase(EXACTLY))
			return slotSize == numoftimes;
		else {
			String msg = "invalid operation code " + mustOccur;
			logger.error(msg);
			throw new PSParameterMismatchException(msg);
		}
	}

	/**
	 * Method implemented by IPSResultDocumentProcessor interface
	 */
	public boolean canModifyStyleSheet() {
		return false;
	}

	/**
	 * Initialize extension method implemented by class PSDefaultExtension
	 */
	public void init(IPSExtensionDef extensionDef, java.io.File codeRoot)
			throws PSExtensionException {
		System.out.println("2- Initializing PSOValidateRelatedItems exit");
	}

	/**
	 * <P>
	 * Required method implemented by interface IPSResultDocumentProcessor This
	 * is the method which is called by the rx server. It uses the Object[]
	 * params index 0 to get the location of a query resource, then uses the
	 * IPSRequestContext to make an internal request to that resource which
	 * returns a Document. It then uses that Document and compares it against
	 * what has been passed in with the params Object[]. If the comparison
	 * returns false this method returns a error page to the user.
	 * </P>
	 * 
	 * <P>
	 * The Document must have NODE_TO_CHECK node. Its NODE_TO_CHECK node must
	 * have the ATT_TO_CHECK attribute.
	 * </P>
	 * 
	 * <P>
	 * It uses the params Object[] to determine the location of the query
	 * resource, transitionid on which to act, the slot name to check, how it
	 * should check the slot name and for how many items.
	 * </P>
	 * 
	 * <P>
	 * EXAMPLE:<BR/> Parameters coming in should look like this:
	 * </P>
	 * 
	 * sys_AssemblerInfo/sys_AssemblerInfo<BR/> 1<BR/> 312<BR/>
	 * Behind_Scenes_Index_Slot<BR/> MORE_THAN<BR/> 3<BR/> 312<BR/>
	 * Behind_Scenes_Index_Slot<BR/> LESS_THAN<BR/> 9<BR/>
	 * <P>
	 * The first param is the location to the query resource. Not used, included
	 * for backwards compatibility.<BR/> The second param is the transition id
	 * on which to act.(can use * for act on all transitions)<BR/> The third
	 * and the seventh param is the Variant which holds the slot to be checked.
	 * Not used, included for backwards compatibility.<BR/> The fourth and the
	 * eigth is the Slot Name to check.<BR/> The fifth and ninth is how to
	 * check that slot.<BR/> The sixth and tenth is the number of items.
	 * </P>
	 * 
	 * @param params
	 *            Parameters must be passed in the following order: 1 - url to
	 *            query resource
	 * @param request
	 * @param resultDoc
	 * @return an XML document - Error page if validation fails, null if not.
	 * @exception PSParameterMismatchException
	 * @exception PSExtensionProcessingException
	 */
	public Document processResultDocument(Object[] params,
			IPSRequestContext request, Document resultDoc)
			throws PSParameterMismatchException, PSExtensionProcessingException {

	    initServices();
		/* Mainly, if the incoming request is an "edit" or "search" request, 
		 * this should not do anything.
		 */
		String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
		if (!command.equals("workflow")) {
			logger.debug("This is not a workflow request, doing nothing");
			return resultDoc;
		} 
		logger.debug("workflow request...continuing");

		Document errDoc = PSXmlDocumentBuilder.createXmlDocument();
		logger.debug("params are " + Arrays.asList(params));
		validateParams(params);

		String contentId = request
				.getParameter(IPSHtmlParameters.SYS_CONTENTID);
		String revision = request.getParameter(IPSHtmlParameters.SYS_REVISION);
		PSLocator loc = new PSLocator(contentId, revision);

		logger.debug("locator is " + loc);

		// get transition id and check for value:
		String transParam = params[1].toString().trim();
		if (matchTransitionId(request, transParam)) {
			//List ErrMsgs = new ArrayList();
			boolean errors = false;


			try {
			    

				String varId; // for backwards compatibility only
				String slotName;
				String mustOccur;
				String numOfTimes;
				boolean argsarenull = false;
				int numTimes;

				PSRelationshipFilter filter = new PSRelationshipFilter(); 
				filter.limitToOwnerRevision(true);
				filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_ACTIVE_ASSEMBLY);
				filter.setOwner(loc);
				List<PSAaRelationship> relations = cws.loadContentRelations(filter, true);
				
				for (int i = 2; i < params.length; i += 4) {
					logger.debug("i=" + i);
					varId = PSUtils.getParameter(params, i);
					slotName = PSUtils.getParameter(params, i + 1);
					mustOccur = PSUtils.getParameter(params, i + 2);
					numOfTimes = PSUtils.getParameter(params, i + 3);
					if (varId == null && slotName == null) {
						logger.debug("no more slots to validate");
						break;
					}
					if (slotName == null || mustOccur == null
							|| numOfTimes == null) {
						logger.error("Unable to validate parameters " + varId
								+ slotName + mustOccur + numOfTimes);
						throw new PSParameterMismatchException(
								"Incorrect Parameters for Slot Validation");
					}
					// convert to int for method:
					numTimes = Integer.parseInt(numOfTimes);

			        

					int slotSize = 0; 
					for(PSAaRelationship rel : relations)
					{
					   if(slotName.equalsIgnoreCase(rel.getSlotName()))
					   { //we found one
					      slotSize++; 
					   }
					}
					
					logger.debug("Slot Size " + slotSize + " must be "
							+ mustOccur + " number " + numTimes);
					if (!isValid(slotSize, mustOccur, numTimes)) {
						// create message and add to ArrayList
						String[] msgargs = new String[3];
						msgargs[0] = slotName;
						msgargs[1] = mustOccur;
						msgargs[2] = numOfTimes;
						PSItemErrorDoc
								.addError(errDoc, "Related Content",
										"Related Content", MSG_SLOT_VALIDATION,
										msgargs);
						logger.info("Found validation Error ");
						errors = true;
					} // close isValid
				} // for

			} catch (PSParameterMismatchException e) {
				throw e;
			} catch (Exception e) {
				logger.error(this.getClass().getName(), e);
				throw new PSExtensionProcessingException(this.getClass()
						.getName(), e);
			}

			if (errors) {
				logger.debug("returning validation errors");
				return errDoc;
			} 
			logger.debug("returning without errors");
			return null;
			
		} // close transition id condition

		// no errors must return null:
		return null;
	}

	private static final String MSG_SLOT_VALIDATION = "Slot name {0} must have {1} {2} related items.";

	/**
	 * Validates the parameters passed into the processResultDocument method.
	 * The sixth parameter must be either AND or OR inorder to validate
	 * additional slots.
	 * 
	 * @param args the params to be validated.
	 */
	private void validateParams(Object[] args)
			throws PSParameterMismatchException {

		logger.debug("Validating Parameters");
		String transId = new String();
		String varId = new String();
		String mustOccur = new String();
		String numOfTimes = new String();
		String slotName = new String();

		// check for all params:
		if (args.length < 6)
		{
			throw new PSParameterMismatchException(
					"At least 6 arguments are required for this exit.");
		}
		// check for transitionid
		transId = PSUtils.getParameter(args, 1);
		if (transId != null && !transId.equals("*")) {
		   try {
		      int tid = Integer.parseInt(transId);
		   } catch (NumberFormatException e) {
		      throw new PSParameterMismatchException(
		      "Transition id must be a number.");
		   }
		}

		// check rest of params for null and required values:
		for (int i = 2; i < args.length; i += 4) {
		   varId = PSUtils.getParameter(args, i);
		   if (varId == null) {
		      logger.debug("Validation Complete");
		      return;
		   }
		   slotName = PSUtils.getParameter(args, i + 1);
		   mustOccur = PSUtils.getParameter(args, i + 2);
		   numOfTimes = PSUtils.getParameter(args, i + 3);
		   // convert to int for method:
		   if (numOfTimes != null) {
		      try {
		         int numoftimes = Integer.parseInt(numOfTimes);
		      } catch (NumberFormatException e) {
		         throw new PSParameterMismatchException(
		               "Required argument numOfTimes is not a number: Value is "
		               + numOfTimes);
		      }
		   } else {
		      throw new PSParameterMismatchException(
		      "Required argument numOfTimes is null");
		   }
		   if (varId == null)
		      throw new PSParameterMismatchException(
		      "Required argument varId is null");
		   if (slotName == null)
		      throw new PSParameterMismatchException(
		      "Required argument slotName is null");
		   if (mustOccur == null)
		      throw new PSParameterMismatchException(
		      "Required argument mustOccur is null");
		   if (numOfTimes == null)
		      throw new PSParameterMismatchException(
		      "Required argument numOfTimes is null");

		   if ((!mustOccur.equals(MORE_THAN))
		         & (!mustOccur.equals(LESS_THAN))
		         & (!mustOccur.equals(EXACTLY)))
		      throw new PSParameterMismatchException(
		            "Usage: MORE_THAN, LESS_THAN, EXACTLY;  What occured: "
		            + mustOccur);
		}


	}

	 
	  /**
	    * Checks if the current transition matches a specified value.  This routine 
	    * is intended for use in validation exits where a specific transition id 
	    * is provided as a parameter.  
	    * <p>
	    * If the parameter supplied is an "*", then this method always returns true. 
	    * Otherwise, the parameter is compared with the 
	    * <code>sys_transitionid</code> HTML parameter and then of the 
	    * <code>WFAction</code> HTML parameter.  If either of these values match,
	    * then <code>true</code> is returned.     
	    * @param request the callers request context.
	    * @param transParameter the match parameter to be compared.  Use "*" to 
	    * match any transition. 
	    * @throws IllegalArgumentException when the <code>transParameter</code> 
	    * is <code>null</code>.
	    * @return <code>true</code> if the transition matches, <code>false</code>
	    * otherwise. 
	    */
	   private static boolean matchTransitionId(IPSRequestContext request, String transParameter)
	   {
	      if(transParameter == null)
	      {
	         logger.error("Transition name parameter must not be null"); 
	         throw new IllegalArgumentException("Transition name parameter must not be null"); 
	      }
	      if(transParameter.equals("*"))
	      {
	         logger.debug("transition id match *");
	         return true; 
	      }
	      String transid = request.getParameter(IPSHtmlParameters.SYS_TRANSITIONID);
	      if(transid != null && transParameter.equals(transid))
	      { 
	         logger.debug("transition id matches sys_transitionid");
	         return true; 
	      }
	      transid = request.getParameter("WFAction");
	      if(transid != null && transParameter.equals(transid))
	      { 
	         logger.debug("transition id matches WFAction");
	         return true; 
	      }
	      
	      logger.debug("No match for transition id "); 
	      return false;
	   }
	   private static void initServices()
       {
          if(gmgr == null)
          {
             cws = PSContentWsLocator.getContentWebservice(); 
             gmgr = PSGuidManagerLocator.getGuidMgr(); 
          }
       }
}
