package com.percussion.pso.relationshipbuilder.exit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSEditCommandHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSQueryCommandHandler;
import com.percussion.data.PSConversionException;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.relationshipbuilder.IPSRelationshipBuilder;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * This class is shared code between the several exits that are provided for
 * relationship building.
 * 
 * @author Adam Gent
 * @since 6.0
 */
public class PSExtensionHelper
{

   public static final String IDS_FIELD_NAME = "fieldName";

   private static final String DEFAULT_OUTPUT = "";

   private final transient IPSRelationshipBuilder m_builder;

   private final transient Map<String, String> m_parameters;

   private final transient IPSRequestContext m_request;
   
   public static final String ARRAY_DELIMETER = ";";

   public PSExtensionHelper(IPSRelationshipBuilder builder,
         Map<String, String> parameters, IPSRequestContext m_request)
   {
      super();
      this.m_parameters = parameters;
      this.m_builder = builder;
      this.m_request = m_request;
   }
   /**
    * We need to do this because the content editor will only set selected
    * what already exists in the database (PSCoreItem field) 
    * but since we are not using the database to determine whats selected
    * but rather our sys_Lookup query resource we need to set all the display choices
    * that it returns selected to "yes".
    * 
    * @param resultDoc
    * @param selectAll
    * @throws PSExtensionProcessingException
    */
   public void updateDisplayChoices(Document resultDoc, boolean selectAll) 
       throws PSExtensionProcessingException {
       ms_log.debug("Starting Updating display choices with builder: " + 
               m_builder.getClass().getCanonicalName());
       String errorMesg = "Error updating display choices";
       if (resultDoc == null)
           throw new IllegalArgumentException(errorMesg + " Result Doc cannot be null");
       PSOExtensionParamsHelper extParamHelper = new PSOExtensionParamsHelper(
               m_parameters, m_request, ms_log);
       String fieldName = extParamHelper.getRequiredParameter(IDS_FIELD_NAME);
       
       Collection<Integer> ids = null;
       if ( ! selectAll ) {
           int contentId = extParamHelper.
               getRequiredParameterAsNumber(IPSHtmlParameters.SYS_CONTENTID).intValue();
           try {
                ids = m_builder.retrieve(contentId);
                ms_log.debug("Selecting ids: " + ids);
           } catch (PSAssemblyException e) {
               ms_log.error(errorMesg, e);
               throw new PSExtensionProcessingException(errorMesg,e);
           } catch (PSException e) {
               ms_log.error(errorMesg, e);
               throw new PSExtensionProcessingException(errorMesg,e);
           }
       }
       
       NodeList controlNodes = resultDoc.getElementsByTagName("Control");
       Element controlElement = null;
       for(int i = 0; controlNodes != null && i < controlNodes.getLength(); i++) {
           Element node = (Element) controlNodes.item(i);
           if (node.hasAttribute("paramName") 
                   && fieldName.equals(node.getAttribute("paramName"))) {
               controlElement = node;
               break;
           }
       }
       if (controlElement == null) {
           ms_log.warn("Field: " + fieldName + " could not be found in the content editor xml");
       }
       else {
           NodeList displayChoicesNodes = controlElement.getElementsByTagName("DisplayChoices");
           Element displayChoicesElement = displayChoicesNodes != null 
               && displayChoicesNodes.getLength() == 1 
               ? (Element) displayChoicesNodes.item(0) : null;
           if (displayChoicesElement == null) {
               ms_log.debug("No DisplayChoice Elements. Checking for Value e.g. multi value no child table, ; separated");
               NodeList valueNodes = controlElement.getElementsByTagName("Value");
               if (valueNodes.getLength() > 0) {
            	  String itemList = valueNodes.item(0).getTextContent();
            	  ms_log.debug("Found selections " + itemList);
            	  String replacementString = "";
            	  for (int id : ids) {
            		  if (replacementString.length() >0 ) replacementString+=";";
            		  replacementString += id;
            	  }
            	  ms_log.debug("Replacing with ids " + replacementString);
            	  valueNodes.item(0).setTextContent(replacementString);
               }
               
               
           }
           else {
               if (ms_log.isTraceEnabled()) {
                   String xml = PSXmlDocumentBuilder.toString(displayChoicesElement);
                   ms_log.trace("Nabeel changes...");
                   ms_log.trace("Display Choices XML: " + xml);
               }
               ms_log.debug("Displayed choice... ");
               NodeList displayEntryNodes = 
                   displayChoicesElement.getElementsByTagName("DisplayEntry");
               ms_log.debug("Got display entry elements, there are:  " + displayEntryNodes.getLength());
               for(int i = 0; displayEntryNodes != null 
                   && i < displayEntryNodes.getLength(); i++) {
                   Element displayEntryElement = (Element) displayEntryNodes.item(i);
                   ms_log.debug("displayEntryElement: " + displayEntryElement);
                   ms_log.debug("selectALL: " + selectAll);
                   if (selectAll) {
                       displayEntryElement.setAttribute("selected", "yes");
                   }
                   else {
                       String idString = displayEntryElement
                           .getElementsByTagName("Value").item(0).getTextContent();
                       ms_log.debug("idString: " + idString);
                       int id = 0;
                       
                      //When the Aging Agent tries to move an item from Public, it tends to fail when the DisplayEntry element
                      //is null which it tends to be
                      try
                      {
                    	   id = Integer.parseInt(idString);
                      }
                      catch (NumberFormatException nfe)
                      {
                    	   ms_log.warn("The value for the control is not appropriately formatted or is not a number. Using 0.");
                    	   id = 0;
                      }
                      
                       if (ids.contains(id)) {
                           displayEntryElement.setAttribute("selected", "yes");   
                       }
                   }
               }
           }
       }
       ms_log.debug("Finished updating display choices builder: " 
               + m_builder.getClass().getCanonicalName());
   }
   
   /**
    * Returns a list of the owner content ids from the relationships in the slot
    * identified by the "slotname" parameter that have the request's content
    * item as their dependent.
    * 
    * @return a list of owner content ids as ";" delimited string from the
    *         slot's matching relationships, or <code>null</code> if there are
    *         no matching relationships, e.g. <code>692;651;339</code>.
    * @throws PSConversionException if request does not include a sys_contentid
    *            parameter, if "slotname" parameter is missing or empty, if slot
    *            cannot be found, or if relationship API throws exception.
    */
   public Object retrieveIds() throws PSConversionException
   {
      IPSRequestContext request = m_request;
       ms_log.debug("Started retrieving target ids using exit: " + m_builder.getClass().getName());
      // get the current content item from the request
      final String contentId = request
            .getParameter(IPSHtmlParameters.SYS_CONTENTID);
      ms_log.debug("\tProcessing for content id: " + contentId);
      String idsString = DEFAULT_OUTPUT;
      
      if (StringUtils.isNumeric(contentId))
      {
         final int cid = Integer.parseInt(contentId);
         try
         {
            final Collection<Integer> ids = m_builder.retrieve(cid);
            idsString = convertToFieldValue(ids);
         }
         catch (PSAssemblyException e)
         {
            throw new PSConversionException(0, e);
         }
         catch (PSException e)
         {
            ms_log.error("\tFailure in relationship API", e);
            throw new PSConversionException(0, e);
         }
      }
      else
      {
         /*
          * not every request will have a content id (such as creating a new
          * item), so don't throw exception if it is missing.
          */
         ms_log.debug("\tskipping extract; no content id in request");
      }
      ms_log.debug("\tReturned string for udf: " + idsString);
      ms_log.debug("Finished extracting ids from slot exit: " + m_builder.getClass().getName());
      return idsString;
   }

   /**
    * Maintains active-assembly-style relationships between the request's
    * content item and a list of content items -- missing relationships will be
    * created, existing relationships with items not in the list will be
    * deleted. The request's content item is the dependent, and the list of
    * content items become its parents. The details of the relationship are
    * provided as parameters (relationship type, slot id, and variant id).
    * 
    * @throws PSParameterMismatchException if any required parameter is blank.
    * @throws PSExtensionProcessingException if the assembly or relationship
    *            APIs report an error.
    */
   public void buildRelationships()
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      /*
       * This exit should only act when editing an item, not when editing the
       * child, or performing other content editor operations like inline link
       * updates.
       */

      if (isRequestToBeProcessedForBuilding(m_request))
      {
         ms_log.debug("Starting building relationships exit with builder " + 
                 m_builder.getClass().getName());
         Map<String, String> paramMap = m_parameters;
         PSOExtensionParamsHelper extParamHelper = new PSOExtensionParamsHelper(paramMap, m_request, ms_log);
         String contentId = extParamHelper.getParameter(IPSHtmlParameters.SYS_CONTENTID);
         ms_log.debug("\tProcessing for content id: " + contentId);
         if (StringUtils.isNumeric(contentId))
         {
            int cid = Integer.parseInt(contentId);
            String fieldName = extParamHelper.getRequiredParameter(IDS_FIELD_NAME);
            try
            {
                Object[] fieldValues = m_request.getParameterList(fieldName);
                buildFromFieldValues(m_builder, cid, fieldName, fieldValues );
            }
            catch (PSAssemblyException e)
            {
               ms_log.error("Failure in assembly API", e);
               throw new PSExtensionProcessingException(0, e);
            }
            catch (PSException e)
            {
               ms_log.error("Failure in relationship API", e);
               throw new PSExtensionProcessingException(0, e);
            }
            catch (Exception e) {
                String mesg = "An unexpected exception happened in the relationshipbuilder.";
                ms_log.error(mesg,e);
                throw new RuntimeException(mesg,e);
            }
            finally {
               ms_log.debug("Finished processing exit with builder " + m_builder.getClass().getName());
            }
         }
      }
   }


   public void buildFromFieldValues(
           IPSRelationshipBuilder m_builder, 
           int cid, String fieldName, Object[] fieldValues ) throws PSAssemblyException, PSException {
       /*
        * Remove Duplicate entries so that we don't get multiple
        * relationships to the same item. This is usually caused by
        * child fields.
        */
       Set <Integer> fieldValuesSet = new HashSet<Integer>();
       Collection <Object> invalid = convert (fieldValues, fieldValuesSet);
       ms_log.debug("\tField values for fieldname '" + fieldName +"' is : " + 
             fieldValuesSet);
       if (invalid.size() == 1 && invalid.contains("")) {
           ms_log.debug("\tEmpty String only.  No items checked.  Removing relationships");
           m_builder.synchronize(cid, fieldValuesSet);
       }
       else if (invalid.size() != 0) {
          ms_log.debug("\tInvalid id(s) were passed. Not building any relationships");
          ms_log.debug("\tInvalid: " + invalid);
       }
       else {
          m_builder.synchronize(cid, fieldValuesSet);
       }
   }
   
   public static void logRequestCommand(IPSRequestContext request) {
       String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);  
       String page = request
             .getParameter(PSContentEditorHandler.PAGE_ID_PARAM_NAME);
       String processInlineLink = request
             .getParameter(IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);
       ms_log.trace("Command is: " + command 
               + " page is: " + page 
               + " processInlineLink is: " + processInlineLink );
   }
   
   public static boolean isRequestToBeProcessedForBuilding(IPSRequestContext request) {
       Map<String, String> noParams = new HashMap<String, String>();
       PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(noParams,request,null);
       Number contentId = helper.getOptionalParameterAsNumber(IPSHtmlParameters.SYS_CONTENTID, 
               "0");
       String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
       String page = request
             .getParameter(PSContentEditorHandler.PAGE_ID_PARAM_NAME);
       String processInlineLink = request
             .getParameter(IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);

       return (page != null
             && contentId.intValue() != 0
             && command.equals(PSModifyCommandHandler.COMMAND_NAME)
             && page.equals(String
                   .valueOf(PSQueryCommandHandler.ROOT_PARENT_PAGE_ID))
             && (processInlineLink == null || !processInlineLink.equals("yes")));
   }
   
   public static boolean isRequestToBeProcessedForSelecting(IPSRequestContext request) {
       Map<String, String> noParams = new HashMap<String, String>();
       PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(noParams,request,null);
       Number contentId = helper.getOptionalParameterAsNumber(IPSHtmlParameters.SYS_CONTENTID, 
               "0");
       String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
       String page = request
             .getParameter(PSContentEditorHandler.PAGE_ID_PARAM_NAME);
       String processInlineLink = request
             .getParameter(IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);
       return (page != null
             && contentId.intValue() != 0
             && command.equals(PSEditCommandHandler.COMMAND_NAME)
             && page.equals(String
                   .valueOf(PSQueryCommandHandler.ROOT_PARENT_PAGE_ID))
             && (processInlineLink == null || !processInlineLink.equals("yes")));
   }
   
   public static String convertToFieldValue(Collection<Integer> ids) {
       StringBuffer idsStringBuffer = new StringBuffer();
       if (ids != null && ids.size() > 0)
       {
          idsStringBuffer.append("");
          Iterator<Integer> iter = ids.iterator();
          while (iter.hasNext())
          {
             int id = iter.next();
             idsStringBuffer.append("" + id);
             if (iter.hasNext())
             {
                idsStringBuffer.append(ARRAY_DELIMETER);
             }
          }
       }
       else
       {
        idsStringBuffer.append(DEFAULT_OUTPUT);
       }

       return idsStringBuffer.toString();
   }
   
   /**
    * Converts an array of objects to a list of integers. Non-parsable elements
    * indicies in the inputIds array are returned.
    * 
    * @param inputIds
    * @param output list of converted ids
    * @return the collection of objects that failed to convert.
    */
   public static Collection<Object> convert(Object[] inputIds,
         final Set<Integer> output)
   {
      Collection<Object> invalid = new ArrayList<Object>();

      if (output == null)
      {
         throw new IllegalArgumentException("Output Set cannot be null.");
      }
      if (inputIds != null)
      {
    	 if (inputIds.length == 1 ) {
    		 //split up ; separated value
    		 inputIds = inputIds[0].toString().split(";");
    		 
    	 }
         for (int i = 0; i < inputIds.length; i++)
         {
            Object contentId = inputIds[i];
            if (contentId instanceof Integer)
            {
               output.add((Integer) contentId);
            }
            else if (contentId != null 
                  && StringUtils.isNotBlank(contentId.toString())
                  && StringUtils.isNumeric(contentId.toString()))
            {
               output.add(Integer.valueOf(contentId.toString()));
            }
            else
            {
               // log and return any non-parsables
               ms_log
                     .warn("\ttaking note of non-parsable element in array <"
                           + contentId + ">");
               invalid.add(contentId);               
            }
         }
      }
      return invalid;
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log ms_log = LogFactory.getLog(PSExtensionHelper.class);

}
