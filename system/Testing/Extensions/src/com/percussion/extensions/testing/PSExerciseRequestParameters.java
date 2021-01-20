/*[ PSExerciseRequestParameters.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.extensions.testing;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Rhythmyx extension used to test the changed and new methods for modifying
 * the request parameters. This has to be used as pre and post exits on a dummy
 * resource for testing. The pre-exit is used to perform action on the specified
 * request parameter <code>MODIFY_PARAM</code> based on the action specified in
 * the request parameter <code>ACTION</code>. The post-exit is used to return
 * the result document with all request parameters and the parameter which is
 * modified in pre-exit.
 * <br>
 * Tests the following methods:
 * <ol>
 * <li>setParameter - Modified</li>
 * <li>appendParameter - new</li>
 * <li>removeParameter - new</li>
 * <li>getParametersIterator - new</li>
 * <li>getParameterList - new</li>
 * <li>hasMultiValuesForAnyParameter - new</li>
 * </ol>
 */
public class PSExerciseRequestParameters extends PSDefaultExtension
      implements IPSRequestPreProcessor, IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Returns the result document with elements for all the request parameters
    * and an entry for the parameter which is specified to modify in the
    * pre-exit. Please look {@link #preProcessRequest preProcessRequest} for
    * more description on the parameter to modify. This method does not expect
    * any parameters for the extension.
    * <br>
    * The dtd of the result document is:
    * &lt;-- The root element of the result document -->
    * &lt;ELEMENT PSXExerciseRequestParameters(PSXAllParameters,
    * PSXModifiedParameter)>
    * &lt;-- The element to list all request parameters -->
    * &lt;ELEMENT PSXAllParameters (PSXParam*)>
    * &lt;-- The 'hasMultiValues' attribute represents whether any of the
    * request parameter has multiple values( <code>List</code> object) -->
    * &lt;ATTLIST PSXAllParameters
    *          hasMultiValues ("y"|"n") >
    * &lt;-- The element to represent the request parameter with name and value
    * -->
    * &lt;ELEMENT PSXParam (#PCDATA)>
    * &lt;-- The name of the parameter -->
    * &lt;ATTLIST PSXParam
    *          Name #CDATA REQUIRED>
    * &lt;-- Represents the parameter modified in the pre-exit. If no action is
    * specified for any parameter or action is 'remove', then this will not have
    * <code>PSXParam</code> element. -->
    * &lt;ELEMENT PSXModifiedParameter(PSXParam?)>
    * &lt;ATTLIST PSXModifiedParameter
    *          action ("append"|"set"|"remove") >
    * <br>
    * Typical return doc may look like the following:
    * &lt;PSXExerciseRequestParameters>
    * &lt;PSXAllParameters hasMultiValues="y">
    * &lt;PSXParam Name="values">x,y,z&lt;/PSXParam>
    * &lt;PSXParam Name="name">[x, y, z]&lt;/PSXParam>
    * &lt;PSXParam Name="action">set&lt;/PSXParam>
    * &lt;PSXParam Name="param1">test&lt;/PSXParam>
    * &lt;PSXParam Name="parameter">name&lt;/PSXParam>
    * &lt;/PSXAllParameters>
    * &lt;PSXModifiedParameter action="set">
    * &lt;PSXParam Name="name">[x, y, z]&lt;/PSXParam>
    * &lt;/PSXModifiedParameter>
    * &lt;/PSXExerciseRequestParameters>
    *
    * @param params  the parameters for this extension, may be <code>null</code>
    * or empty.
    * @param request the request context object, may not be <code>null</code>
    * @param resultDoc   the result XML document, may be <code>null</code>
    *
    * @return the result document, never <code>null</code>
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, XML_ROOT_NODE);

      root.appendChild( getAllParamsNode(doc,  request ) );
      root.appendChild( getModifiedParamNode( doc, request ) );

      return doc;
   }

   /**
    * Gets <code>XML_REQ_ALL_PARAMS_NODE</code> element with all request
    * parameters.
    *
    * @param doc the xml document, assumed not to be <code>null</code>
    * @param request the request context, assumed not to be <code>null</code>
    *
    * @return the <code>XML_REQ_ALL_PARAMS_NODE</code> element, never
    * <code>null</code>
    */
   private Element getAllParamsNode(Document doc, IPSRequestContext request)
   {
      Element root = doc.createElement(XML_REQ_ALL_PARAMS_NODE);

      addParameterElements(doc, root, request.getParametersIterator());

      if(request.hasMultiValuesForAnyParameter())
         root.setAttribute(XML_REQ_MULTIVALUES_ATTR, "y");
      else
         root.setAttribute(XML_REQ_MULTIVALUES_ATTR, "n");

      return root;
   }

   /**
    * Gets <code>XML_REQ_MOD_PARAM_NODE</code> element. The element is set with
    * the <code>ACTION</code> attribute and <code>XML_REQ_PARAM_NODE</code>
    * element if <code>ACTION</code> is specified in the request. If the
    * <code>ACTION</code> is <code>REMOVE_ACTION</code>, then the
    * <code>XML_REQ_PARAM_NODE</code> won't exist in the element.
    *
    * @param doc the xml document, assumed not to be <code>null</code>
    * @param request the request context, assumed not to be <code>null</code>
    *
    * @return the <code>XML_REQ_MOD_PARAM_NODE</code> element, never
    * <code>null</code>
    */
   private Element getModifiedParamNode(Document doc, IPSRequestContext request)
   {
      Element root = doc.createElement(XML_REQ_MOD_PARAM_NODE);

      String action = request.getParameter(ACTION);
      if(action != null)
      {
         if( action.equalsIgnoreCase(APPEND_ACTION) ||
            action.equalsIgnoreCase(SET_ACTION) ||
            action.equalsIgnoreCase(REMOVE_ACTION) )
         {
            root.setAttribute(ACTION, action);
            String paramName = request.getParameter(MODIFY_PARAM);
            if(paramName != null)
            {
               Object[] values = request.getParameterList(paramName);
               if(values != null)
               {
                  Element paramElement = PSXmlDocumentBuilder.addElement(
                     doc, root, XML_REQ_PARAM_NODE,
                     Arrays.asList(values).toString());
                  paramElement.setAttribute(XML_REQ_PARAM_NAME_ATTR, paramName);

               }
            }

         }
      }

      return root;
   }

   /**
    * Adds parameter elements to the specified root element.
    *
    * @param doc the xml document, assumed not to be <code>null</code>
    * @param root the element to add parameter elements to, assumed not to be
    * <code>null</code>
    * @param iter the iterator of parameter elements, assumed not to be
    * <code>null</code>
    */
   private void addParameterElements(Document doc, Element root, Iterator iter)
   {
      while (iter.hasNext())
      {
         Map.Entry entry = (Map.Entry) iter.next();
         String name = (String) entry.getKey();
         Object value = entry.getValue();

         Element paramElement = PSXmlDocumentBuilder.addElement(
            doc, root, XML_REQ_PARAM_NODE, value.toString());
         paramElement.setAttribute(XML_REQ_PARAM_NAME_ATTR, name);
      }
   }


   /**
    * Checks whether any <code>ACTION</code> attribute is specified in the
    * request parameters and modifies the request parameters accordingly.
    * <table border =1>
    * <tr><th>Action Name</th><th>Expected Parameters</th><th>Action</code>
    * <tr><td>set</td><td>parameter, values</td><td>creates or replaces the
    * parameter with values specified</td></tr>
    * <tr><td>append</td><td>parameter, values</td><td>appends the values
    * specified to the parameter values if parameter exists, otherwise creates
    * a parameter with values specified</td></tr>
    * <tr><td>remove</td><td>parameter</td><td>removes the parameter</td></tr>
    * </table>
    * If any expected parameters are missing, it ignores the action. The data
    * type of expected parameters is <code>String</code>. To specify  multiple
    * values to a parameter, set values delimited by ','.
    *
    * @param params  the parameters for this extension, may be <code>null</code>
    * or empty.
    * @param request the request context object, may not be <code>null</code>
    *
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      String action = request.getParameter(ACTION);

      if(action != null)
      {
         String paramName  = request.getParameter(MODIFY_PARAM);
         if(paramName != null)
         {
            if( action.equalsIgnoreCase(APPEND_ACTION) ||
               action.equalsIgnoreCase(SET_ACTION) )
            {
               List valueList = null;
               String values = request.getParameter(PARAM_VALUES);
               if(values != null && values.trim().length() != 0)
               {
                  valueList = new ArrayList();
                  StringTokenizer st = new StringTokenizer(values, ",");
                  while(st.hasMoreTokens())
                     valueList.add(st.nextToken());
               }
               if(valueList != null)
               {
                  Iterator listIter = valueList.iterator();
                  if(action.equalsIgnoreCase(APPEND_ACTION))
                  {
                     while(listIter.hasNext())
                     {
                        Object obj = listIter.next();
                        request.appendParameter(paramName, obj);
                     }
                  }
                  else
                  {
                     if(valueList.size() == 1)
                        request.setParameter(paramName, valueList.get(0));
                     else
                        request.setParameter(paramName, valueList);
                  }
               }
            }
            else if( action.equalsIgnoreCase(REMOVE_ACTION) )
               request.removeParameter(paramName);
         }
      }
   }

   /**
    * The action parameter to specify the action for modifying the request
    * parameters.
    */
   private static final String ACTION = "action";

   /**
    * The parameter to specify the parameter on which the action need to be
    * done.
    */
   private static final String MODIFY_PARAM = "parameter";

   /**
    * The values parameter to specify the values for append and set actions.
    */
   private static final String PARAM_VALUES = "values";

   /**
    * Constants to indicate append action.
    */
   private static final String APPEND_ACTION = "append";

   /**
    * Constant to indicate set action.
    */
   private static final String SET_ACTION = "set";

   /**
    * Constant to indicate remove action.
    */
   private static final String REMOVE_ACTION = "remove";

   /**
    * Constant for the root element name in the post-exit result document.
    */
   private static final String XML_ROOT_NODE = "PSXExerciseRequestParameters";

   /**
    * Constant for the name of all parameters node.
    */
   private static final String XML_REQ_ALL_PARAMS_NODE = "PSXAllParameters";

   /**
    * Constant for the name of modified parameter node.
    */
   private static final String XML_REQ_MOD_PARAM_NODE = "PSXModifiedParameter";

   /**
    * Constant for the name of parameter node.
    */
   private static final String XML_REQ_PARAM_NODE = "PSXParam";

   /**
    * Constant for the name of parameter name attribute.
    */
   private static final String XML_REQ_PARAM_NAME_ATTR = "Name";

   /**
    * Constant for the name of 'hasMultiValues' attribute for all parameters
    * node.
    */
   private static final String XML_REQ_MULTIVALUES_ATTR = "hasMultiValues";
}
