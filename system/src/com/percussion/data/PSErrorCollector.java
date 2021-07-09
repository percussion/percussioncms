/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.data;

import com.percussion.cms.PSDisplayFieldElementBuilder;
import com.percussion.cms.PSEditorDocumentBuilder;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSPreviewCommandHandler;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * A container to collect item and/or field validation errors.
 */
public class PSErrorCollector
{
   /**
    * Create a new error collector with the maximal allowed errors before the
    * process should stop.
    *
    * @param type the validation type this error collector should be created
    *    for, one of TYPE_xxx.
    * @param maxErrorsToStop the maximal allowed errors alowed before the
    *   process should stop.
    * @throws IllegalArgumentException if the provided type is unknown.
    */
   public PSErrorCollector(int type, int maxErrorsToStop)
   {
      if (type != TYPE_FIELD && type != TYPE_ITEM)
         throw new IllegalArgumentException("unknown validation type");

      m_type = type;
      m_maxErrorsToStop = maxErrorsToStop;
   }

   /**
    * The status if errors had occurred.
    *
    * @return <code>true</code> if errors have occurred since the creation
    *   of this object, <code>false</code> otherwise.
    */
   public boolean hasErrors()
   {
      return (m_errorCount > 0);
   }

   /**
    * Is the maximal allowed number of errors exceeded?
    *
    * @param <code>true</code> if the maximal number of errors is exceeded,
    *   <code>false</code> otherwise.
    */
   public boolean maxErrorsExceeded()
   {
      return (m_errorCount >= m_maxErrorsToStop);
   }

   /**
    * Get the error count.
    *
    * @return the error count.
    */
   public int getErrorCount()
   {
      return m_errorCount;
   }

   /**
    * Set the generic error message. Will overwrite the existing error
    * message. Will be the first message in the produced error document.
    *
    * @param message a new error message, not <code>null</code>, might be
    *    empty.
    * @throws IllegalArgumentException if the provided message is
    *    <code>null</code>.
    */
   public void set(String message)
   {
      if (message == null)
         throw new IllegalArgumentException(
            "message cannot be null");

      m_genericError = message;
   }

   /**
    * Add a new field validation error for the provided pageid and field.
    *
    * @param pageId the id of the page where the error occurred.
    * @param eval the evaluator for which the validation failed, not
    *    <code>null</code>.
    * @throws IllegalArgumentException if any parameter is <code>null</code>.
    */
   public void add(Integer pageId, PSFieldValidationRulesEvaluator eval)
   {
      if (pageId == null || eval == null)
         throw new IllegalArgumentException("parameters cannot be null");

      List errors = (List) m_fieldErrors.get(pageId);
      if (errors == null)
         errors = new ArrayList();
      errors.add(eval);

      m_fieldErrors.put(pageId, errors);

      m_errorCount++;
   }

   /**
    * Add a new page specific error message.
    *
    * @param pageId the pageid, not <code>null</code>.
    * @param submitNames the field submit names causing the validation error.
    *    Not <code>null</code>.
    * @param displayNames the field display names causing the validation error.
    *    Not <code>null</code>.
    * @param message the error message, not <code>null</code>.
    * @param args the arguments matching the pattern provided in the message,
    *    might be <code>null</code>.
    * @throws IllegalArgumentException if any parameter is <code>null</code>.
    */
   public void add(Integer pageId, List submitNames,
      List displayNames, String message, List args)
   {
      if (pageId == null || submitNames == null || displayNames == null
          || message == null)
         throw new IllegalArgumentException("parameters cannot be null");

      if (args != null)
         message = MessageFormat.format(message, args.toArray());

      List error = new ArrayList();
      error.add(submitNames);
      error.add(displayNames);
      error.add(message);

      List errors = (List) m_itemErrors.get(pageId);
      if (errors == null)
         errors = new ArrayList();
      errors.add(error);

      m_itemErrors.put(pageId, errors);

      m_errorCount++;
   }

   /**
    * Add a new error page url for the provided pageId.
    *
    * @param pageId the pageid, not <code>null</code>.
    * @param url the url string, the complete url leading to the error page,
    *    not <code>null</code> or empty.
    * @throws IllegalArgumentException if any parameter is <code>null</code>
    *    or the url is empty.
    */
   public void add(Integer pageId, String url)
   {
      if (pageId == null)
         throw new IllegalArgumentException("pageId cannot be null");
      if (url == null || url.trim().length() == 0)
         throw new IllegalArgumentException("url cannot be null or empty");

      m_errorPages.put(pageId, url);

      m_errorCount++;
   }

   /**
    * Add a new item validation error document.
    *
    * @param an item validation error document conforming to the
    *    sys_ItemValidation.dtd, not <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public void add(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      m_itemErrorDocuments.add(doc);

      m_errorCount++;
   }

   /**
    * Walks the list of all item error documents collected and creates the
    * appropriate item error message.
    *
    * @param pageMap a map of all item pages. This is used to match the error
    *    document info to the appropriate page.
    * @throws IllegalArgumentException if the provided pageMap is
    *    <code>null</code>.
    */
   public void createItemErrors(Map pageMap)
   {
      if (pageMap == null)
         throw new IllegalArgumentException("the page map cannot be null");

      for (int i=0; i<m_itemErrorDocuments.size(); i++)
      {
         Document doc = (Document) m_itemErrorDocuments.get(i);
         NodeList errors = doc.getElementsByTagName(VALIDATION_ERROR_ELEM);
         for (int j=0; j<errors.getLength(); j++)
         {
            Element error = (Element) errors.item(j);

            NodeList fields = error.getElementsByTagName(ERROR_FIELD_ELEM);
            List submitNames = getNames(fields, SUBMIT_NAME_ATTR);
            List displayNames = getNames(fields, DISPLAY_NAME_ATTR);

            NodeList messages = error.getElementsByTagName(ERROR_MESSAGE_ELEM);
            List args = new ArrayList();
            String pattern = getPatternAndArgs(messages, args);

            Integer pageId = getPageId((String) submitNames.get(0), pageMap);

            add(pageId, submitNames, displayNames, pattern, args);
         }
      }
   }

   /**
    * Get the error document with all errors created during item validation.
    *
    * @param request the request to create the error document for, not
    *    <code>null</code>.
    * @param pageMap a map of all pages in this item. The map key is the
    *    pageid as Integer, the value is the page Document. Not
    *    <code>null</code>.
    * @return the error document, might be <code>null</code>.
    * @throws IllegalArgumentException if any parameter is <code>null</code>.
    */
   public Document getErrorDocument(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("parameters cannot be null");

      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element itemErrorElem = doc.createElement(ITEM_ERROR_ELEM);
         doc.appendChild(itemErrorElem);

         Iterator errors = m_itemErrors.keySet().iterator();
         while (errors.hasNext())
         {
            Integer pageId = (Integer) errors.next();
            List pageErrors = (List) m_itemErrors.get(pageId);

            Element errorSet = doc.createElement(ERROR_SET_ELEM);
            itemErrorElem.appendChild(errorSet);

            Element errorScreen = doc.createElement(ERROR_SCREEN_ELEM);
            errorSet.appendChild(errorScreen);

            Element errorMessage = doc.createElement(ERROR_MESSAGE_ELEM);
            errorSet.appendChild(errorMessage);

            String url = request.getRequestFileURL();
            Map params = new HashMap();
            params.put(PSContentEditorHandler.COMMAND_PARAM_NAME,
               PSPreviewCommandHandler.COMMAND_NAME);
            params.put(IPSHtmlParameters.SYS_CONTENTID,
               PSHtmlParameters.get(IPSHtmlParameters.SYS_CONTENTID,
                  request.getParameters()));
            params.put(IPSHtmlParameters.SYS_REVISION,
               PSHtmlParameters.get(IPSHtmlParameters.SYS_REVISION,
                  request.getParameters()));
            URL completeUrl = PSUrlUtils.createUrl(null, null, url,
               params.entrySet().iterator(), null,
               new PSRequestContext(request));
            errorScreen.setAttribute(SCREEN_URL_ATTR,
               completeUrl.toExternalForm());

            for (int i=0; i<pageErrors.size(); i++)
            {
               List itemError = (List) pageErrors.get(i);

               Element errorFieldSet = doc.createElement(ERROR_FIELD_SET_ELEM);
               errorSet.appendChild(errorFieldSet);

               // create the error field set
               List submitNames = (List) itemError.get(0);
               List displayNames = (List) itemError.get(1);
               for (int j=0; j<submitNames.size(); j++)
               {
                  Element errorField = doc.createElement(ERROR_FIELD_ELEM);
                  errorFieldSet.appendChild(errorField);
                  errorField.setAttribute(SUBMIT_NAME_ATTR,
                     submitNames.get(j).toString());
                  errorField.setAttribute(DISPLAY_NAME_ATTR,
                     displayNames.get(j).toString());
               }

               Text messageText = doc.createTextNode(
                  (String) itemError.get(2) + "<br id=\"xsplit\">");
               errorMessage.appendChild(messageText);
               errorMessage.setAttribute("no-escaping", "yes");
            }
         }

         return doc;
      }
      catch (MalformedURLException e)
      {
         // should never happen
         throw new RuntimeException(e.getLocalizedMessage());
      }
   }

   /**
    * This method finds the pageid for the provided submit name.
    *
    * @param submitName the submit name we want the pageId for,
    *    assumed not <code>null</code>.
    * @param pageMap a map of all pages contained in this item, assumed not
    *    <code>null</code>.
    * @return the first pageid found, never <code>null</code>.
    */
   private Integer getPageId(String submitName, Map pageMap)
   {
      Iterator pages = pageMap.keySet().iterator();
      while (pages.hasNext())
      {
         Integer pageId = (Integer) pages.next();
         Map childpageMap = (Map) pageMap.get(pageId);
         Iterator iter = childpageMap.values().iterator();
         while( iter.hasNext())
         {
            Document page = (Document) iter.next();
            NodeList controls = page.getElementsByTagName(
               PSDisplayFieldElementBuilder.CONTROL_NAME);
            for (int i=0; i<controls.getLength(); i++)
            {
               Element control = (Element) controls.item(i);
               String paramName = control.getAttribute(
                  PSDisplayFieldElementBuilder.PARAMNAME_NAME);
               if (paramName.equals(submitName))
                  return pageId;
            }
         }
      }

      // this should never happen
      return new Integer(0);
   }

   /**
    * Get all attribute values for the provided list of fields and
    * attribute name.
    *
    * @param fields a list of all fields we want the attribute values for,
    *    assumed not <code>null</code>.
    * @param attrName the name of the attribute we want the values for,
    *    assumed not <code>null</code>.
    * @return a list of attribute values found in the provided field list,
    *    never <code>null</code>.
    */
   private List getNames(NodeList fields, String attrName)
   {
      ArrayList attrs = new ArrayList();
      for (int i=0; i<fields.getLength(); i++)
      {
         Element field = (Element) fields.item(i);
         attrs.add(field.getAttribute(attrName));
      }

      return attrs;
   }

   /**
    * Get the pattern string and a list of all parameters.
    *
    * @param messages the list of messages, assumed not <code>null</code> and
    *    not empty.
    * @param args the list to be filled with all arguments, aeeumed not
    *    <code>null</code>.
    * @return the pattern string, never <code>null</code> .
    */
   private String getPatternAndArgs(NodeList messages, List args)
   {
      Element message = (Element) messages.item(0);

      Element patternElem = (Element) message.getFirstChild();
      Text pattern = (Text) patternElem.getFirstChild();

      NodeList arguments = message.getElementsByTagName(ARGUMENT_ELEM);
      for (int i=0; i<arguments.getLength(); i++)
      {
         Element argumentElem = (Element) arguments.item(i);
         Text argument = (Text) argumentElem.getFirstChild();
         args.add(argument.getNodeValue());
      }

      return pattern.getNodeValue();
   }

   /**
    * Merge all collected errors into the provided document.
    *
    * @param pageId the page for which to merge the errors into the
    *    provided document, not <code>null</code>.
    * @param doc the document into which we want to merge all collected
    *    errors for the supplied page. A document conforming to the
    *    sys_ContentEditor.dtd is expected. Not <code>null</code>.
    * @throws IllegalArgumentException if any of the provided parameters is
    *    <code>null</code>.
    */
   public void mergeErrors(Integer pageId, Document doc)
   {
      if (pageId == null || doc == null)
         throw new IllegalArgumentException(
            "pageid and document cannot be null");

      switch (m_type)
      {
         case TYPE_FIELD:
            mergeFieldErrors(pageId, doc);
            break;

         case TYPE_ITEM:
            mergeItemErrors(doc);
            break;
      }
   }

   /**
    * Create the DisplayError element for the provided document.
    *
    * @param doc the document to create the element for, assumed not
    *    <code>null</code>.
    * @return the new DisplayElement created, never <code>null</code>.
    */
   private Element createDisplayError(Document doc)
   {
      // create the element that contains all error messages
      Element displayError = doc.createElement(DISPLAY_ERROR_ELEM);
      displayError.setAttribute(ERROR_COUNT_ATTR,
         Integer.toString(getErrorCount()));

      Element genericMessage = doc.createElement(GENERIC_ERROR_ELEM);
      Text genericText = doc.createTextNode(m_genericError);
      genericMessage.appendChild(genericText);
      displayError.appendChild(genericMessage);

      return displayError;
   }

   /**
    * Merge all collected field errors into the provided document for the
    * pageid supplied.
    *
    * @param pageId the id of the page to merge field error for, assumed
    *    not <code>null</code>.
    * @param doc the document to merge the errors to, assumed not
    *    <code>null</code>.
    */
   private void mergeFieldErrors(Integer pageId, Document doc)
   {
      // create the element that contains all error messages
      Element displayError = createDisplayError(doc);

      List errorFields = (List) m_fieldErrors.get(pageId);
      if (errorFields == null)
         return;

      Element details = doc.createElement(DETAILS_ELEM);
      displayError.appendChild(details);

      /*
       * For each error field add the error message to the Details
       * element. Also collect a map for error labels (key= submitName,
       * value= PSDisplayText). The map value is set to the regular label if
       * no error label is specified (either null or blank)
       */
      Map labelMap = new HashMap();
      Iterator evals = errorFields.iterator();
      while (evals.hasNext())
      {
         PSFieldValidationRulesEvaluator eval =
            (PSFieldValidationRulesEvaluator) evals.next();

         PSField field = eval.getField();
         PSUISet uiSet = eval.getUISet();
         PSDisplayText errorLabel = uiSet.getErrorLabel();
         if (errorLabel == null || errorLabel.getText().trim().length()<1)
            errorLabel = uiSet.getLabel();
         labelMap.put(field.getSubmitName(), errorLabel);

         Element fieldError = doc.createElement(FIELD_ERROR_ELEM);
         fieldError.setAttribute(SUBMIT_NAME_ATTR, field.getSubmitName());
         fieldError.setAttribute(DISPLAY_NAME_ATTR, uiSet.getLabel().getText());
         PSDisplayText errorMessage =
            field.getValidationRules().getErrorMessage();
         if (errorMessage != null)
         {
            Text fieldText = doc.createTextNode(errorMessage.getText());
            fieldError.appendChild(fieldText);
         }

         details.appendChild(fieldError);
      }

      //add the DislayError element as the first child of the ContentEditor
      Element contentEditor = doc.getDocumentElement();
      contentEditor.setAttribute(PSEditorDocumentBuilder.DOCTYPE_NAME,
         PSEditorDocumentBuilder.DOCTYPE_ERROR);
      Node first = contentEditor.getFirstChild();
      if (first != null)
         contentEditor.insertBefore(displayError, first);
      else
         contentEditor.appendChild(displayError);

      /*
       * Get all Control elements in the document and find all which failed
       * to get to the DisplayField elements. Then change the DisplayField
       * to reflect an error.
       */
      NodeList controls = doc.getElementsByTagName(
         PSDisplayFieldElementBuilder.CONTROL_NAME);
      for (int i=0; i<controls.getLength(); i++)
      {
         Element control = (Element) controls.item(i);
         String paramName = control.getAttribute(
            PSDisplayFieldElementBuilder.PARAMNAME_NAME);
         if (paramName != null)
         {
            // set the DisplayField to error
            Element displayField = (Element) control.getParentNode();

            PSDisplayText errorLabel = (PSDisplayText) labelMap.get(paramName);
            if (errorLabel != null)
            {
               displayField.setAttribute(
                  PSDisplayFieldElementBuilder.DISPLAYTYPE_NAME,
                  PSDisplayFieldElementBuilder.DISPLAY_TYPE_ERROR);

               Element errorElement = doc.createElement(
                  PSDisplayFieldElementBuilder.LABEL_NAME);
               Text errorText = doc.createTextNode(errorLabel.getText());
               errorElement.appendChild(errorText);

               String labelSrcType =
                  ((Element) displayField.getFirstChild()).getAttribute(
                     PSDisplayFieldElementBuilder.SOURCE_TYPE_NAME);
               errorElement.setAttribute(
                 PSDisplayFieldElementBuilder.SOURCE_TYPE_NAME, labelSrcType);


               Element displayLabel = (Element) displayField.getFirstChild();
               if (displayLabel == null)
               {
                  // no label specified, add one
                  displayField.appendChild(errorElement);
               }
               else
               {
                  // replace the existing label with the error label
                  displayField.replaceChild(errorElement, displayLabel);
               }
            }
         }
      }
   }

   /**
    * Merge all collected item errors into the provided document for all
    * pages that had errors.
    *
    * @param doc the document to merge the errors to, assumed not
    *    <code>null</code>.
    */
   private void mergeItemErrors(Document doc)
   {
      // create the element that contains all error messages
      Element displayError = createDisplayError(doc);

      Element details = doc.createElement(DETAILS_ELEM);
      displayError.appendChild(details);

      Iterator pageIds = m_itemErrors.keySet().iterator();
      while (pageIds.hasNext())
      {
         Integer pageId = (Integer) pageIds.next();

         Element itemError = doc.createElement(ITEM_ERROR_ELEM);
         itemError.setAttribute(PAGE_URL_ATTR,
            (String) m_errorPages.get(pageId));
         details.appendChild(itemError);

         List itemErrors = (List) m_itemErrors.get(pageId);
         for (int i=0; i<itemErrors.size(); i++)
         {
            Text errorText = doc.createTextNode((String) itemErrors.get(i));
            itemError.appendChild(errorText);
         }
      }

      // add the DislayError element as the first child of the ContentEditor
      Element contentEditor = doc.getDocumentElement();
      contentEditor.setAttribute(PSEditorDocumentBuilder.DOCTYPE_NAME,
         PSEditorDocumentBuilder.DOCTYPE_ERROR);
      Node first = contentEditor.getFirstChild();
      if (first != null)
         contentEditor.insertBefore(displayError, first);
      else
         contentEditor.appendChild(displayError);
   }

   /** XML document element name. */
   public static final String DISPLAY_ERROR_ELEM = "DisplayError";
   /** XML document attribute name. */
   public static final String ERROR_COUNT_ATTR = "errorCount";
   /** XML document element name. */
   public static final String GENERIC_ERROR_ELEM = "GenericMessage";
   /** XML document element name. */
   public static final String DETAILS_ELEM = "Details";
   /** XML document element name. */
   public static final String FIELD_ERROR_ELEM = "FieldError";
   /** XML document attribute name. */
   public static final String SUBMIT_NAME_ATTR = "submitName";
   /** XML document element name. */
   public static final String ITEM_ERROR_ELEM = "ItemError";
   /** XML document element name. */
   public static final String ERROR_MESSAGE_ELEM = "ErrorMessage";
   /** XML document attribute name. */
   public static final String PAGE_URL_ATTR = "pageUrl";
   /** XML document element name. */
   public static final String VALIDATION_ERROR_ELEM = "ValidationError";
   /** XML document element name. */
   public static final String ERROR_FIELD_ELEM = "ErrorField";
   /** XML document attribute name. */
   public static final String DISPLAY_NAME_ATTR = "displayName";
   /** XML document element name. */
   public static final String ARGUMENT_ELEM = "Argument";
   /** XML document element name. */
   public static final String ERROR_SCREEN_ELEM = "ErrorScreen";
   /** XML document element name. */
   public static final String ERROR_SET_ELEM = "ErrorSet";
   /** XML document element name. */
   public static final String ERROR_FIELD_SET_ELEM = "ErrorFieldSet";
   /** XML document attribute name. */
   public static final String SCREEN_URL_ATTR = "screenUrl";

   /**
    * Specifies that this error collector is used for item level validation.
    */
   public final static int TYPE_ITEM = 0;

   /**
    * Specifies that this error collector is used for field level validation.
    */
   public final static int TYPE_FIELD = 1;

   /**
    * The validation type this error collector was constructed for.
    * Initialized during construction, never changed after that.
    */
   private int m_type = -1;

   /**
    * The maximum number of errors allowed before the process should be
    * stopped. Set during construction.
    */
   private int m_maxErrorsToStop = 0;

   /**
    * Counter for all errors occurred. Will be incremented by one for all
    * errors added, no matter of what type.
    */
   private int m_errorCount = 0;

   /**
    * A generic error message added to the top of the error page during the
    * merge process. Never <code>null</code>, might be empty.
    */
   private String m_genericError = "";

   /**
    * A map of item specific error messages. The map key is the pageid while
    * the value is an array list. Element 0 is an array of String objects
    * for the field submit names, element 1 is an array of String objects
    * for the field display names and element 2 is the error message.
    */
   private Map m_itemErrors = new HashMap();

   /**
    * A list of item error documents collected during item validation. The
    * documents conform to the sys_ItemValidation.dtd. Never
    * <code>null</code> after construction, might be empty.
    */
   private List m_itemErrorDocuments = new ArrayList();

   /**
    * A map of lists of field errors. The map key is the pageid where the
    * error occurred while the value is a list of
    * PSFieldValidationRulesEvaluator objects for all fields that had
    * validation errors on the appropriate page. Never <code>null</code>,
    * might be empty.
    */
   private Map m_fieldErrors = new HashMap();

   /**
    * A map of error page urls. The key is the pageid, while the value is a
    * String containing the complete url (including all paramaeters) to the
    * error page.
    */
   private Map m_errorPages = new HashMap();
}
