/* *****************************************************************************
 *
 * [ PSTestEffectAPI.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.effects.testing;

import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffect;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This effect is used to test the effect processing model. It implements all
 * interface methods with no functionality except that the methods write the
 * result of the call to a test application that can then be read from an
 * autotest script. The autotest scripts must make sure thet they initialize
 * the results to 'Dot Called' for all test id's before this effect is 
 * processed.
 */
public class PSTestEffectAPI extends PSEffect
{
   /**
    * Based on the supplied parameters the attempt method either passes, fails
    * or produces a programming error and writes the result to a test 
    * application from where an autotest can read it.
    * 
    * @see IPSEffect#attempt(Object[], IPSRequestContext, IPSExecutionContext, 
    *    PSEffectResult)
    */
   public void attempt(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      validateParameters(params);
      
      String testId = "PSTestEffectAPI:attempt:" + getContextString(context);
      String functionParam = (params[2] == null) ? null : params[2].toString();
      String functionality = getFunctionality(functionParam);
      int relationshipId = -1;
      if (context.getCurrentRelationship() != null)
         relationshipId = context.getCurrentRelationship().getId();
      Document resultDoc = createResult(testId, getResultString(request, 
         relationshipId, testId, functionality));
      
      executeFunctionality(request, functionality, result, resultDoc);
   }

   /**
    * Based on the supplied parameters the recover method either passes, fails
    * or produces a programming error and writes the result to a test 
    * application from where an autotest can read it.
    * 
    * @see IPSEffect#recover(Object[], IPSRequestContext, IPSExecutionContext, 
    *    PSExtensionProcessingException, PSEffectResult)
    */
   public void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException
   {
      validateParameters(params);
      
      String testId = "PSTestEffectAPI:recover:" + getContextString(context);
      String functionParam = (params[3] == null) ? null : params[3].toString();
      String functionality = getFunctionality(functionParam);
      int relationshipId = -1;
      if (context.getCurrentRelationship() != null)
         relationshipId = context.getCurrentRelationship().getId();
      Document resultDoc = createResult(testId, getResultString(request, 
         relationshipId, testId, functionality));
      
      executeFunctionality(request, functionality, result, resultDoc);
   }

   /**
    * Based on the supplied parameters the test method either passes, fails
    * or produces a programming error and writes the result to a test 
    * application from where an autotest can read it.
    * 
    * @see IPSEffect#test(Object[], IPSRequestContext, IPSExecutionContext, 
    *    PSEffectResult)
    */
   public void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      validateParameters(params);

      Set contexts = getExecutionContexts(params);
      if (!contexts.contains(RS_ALL))
      {
         if (!contexts.contains(RS_PRE_CONSTRUCTION) && 
            context.isPreConstruction()) 
         {
            result.setWarning("Construction context not supported.");
            return;
         }

         if (!contexts.contains(RS_PRE_DESTRUCTION) && 
            context.isPreDestruction()) 
         {
            result.setWarning("Destruction context not supported.");
            return;
         }

         if (!contexts.contains(RS_PRE_CHECKIN) && 
            context.isPreCheckin()) 
         {
            result.setWarning("Checkin context not supported.");
            return;
         }

         if (!contexts.contains(RS_POST_CHECKOUT) && 
            context.isPostCheckout()) 
         {
            result.setWarning("Checkout context not supported.");
            return;
         }

         if (!contexts.contains(RS_PRE_WORKFLOW) && 
            context.isPreWorkflow()) 
         {
            result.setWarning("PreWorkflow context not supported.");
            return;
         }

         if (!contexts.contains(RS_POST_WORKFLOW) && 
            context.isPostWorkflow()) 
         {
            result.setWarning("PostWorkflow context not supported.");
            return;
         }

         if (!contexts.contains(RS_PRE_CLONE) && 
            context.isPreClone()) 
         {
            result.setWarning("PreClone context not supported.");
            return;
         }

         if (!contexts.contains(RS_PRE_UPDATE) && 
            context.isPreUpdate()) 
         {
            result.setWarning("Update context not supported.");
            return;
         }
      }
      
      String testId = "PSTestEffectAPI:test:" + getContextString(context);
      String functionParam = (params[1] == null) ? null : params[1].toString();
      String functionality = getFunctionality(functionParam);
      int relationshipId = -1;
      if (context.getCurrentRelationship() != null)
         relationshipId = context.getCurrentRelationship().getId();
      Document resultDoc = createResult(testId, getResultString(request, 
         relationshipId, testId, functionality));
      
      executeFunctionality(request, functionality, result, resultDoc);
   }
   
   /**
    * Produce the result string for the supplied parameters. The result
    * string is produced out of the current result for the supplied test id
    * to which the provided functionality delimited with the currently 
    * processed relationship id (if known) is appended.
    * 
    * @param request the effect request used to lookup the current result for
    *    the supplied test id, assumed not <code>null</code>, no parameters are
    *    expected in the supplied request.
    * @param relationshipId the id of the currently processed relationship or
    *    -1 if not known. This is used to produce the result string for the
    *    currently processed functionality. If not provided (-1), nothing is 
    *    done.
    * @param testId the test id, assumed not <code>null</code> or empty.
    * @param functionality the functionality for which to produce the result
    *    string, assumed not <code>null</code>.
    * @return the result string for the supplied parameters which is the
    *    current result of the supplied test id appendend with the supplied
    *    functionality delimited with the currently processed relationship id, 
    *    never <code>null</code> or empty. The result string is ordered 
    *    ascending by relationship id.
    */
   private String getResultString(IPSRequestContext request,
      int relationshipId, String testId, String functionality)
   {
      String delimiter = ", ";
      String resultString = getResult(request, testId);
      
      // nothing to do if no relationship id was supplied
      if (relationshipId >= 0)
      {
         // use tree map to sort results ascending by relationship id
         Map<String, String> resultMap = new TreeMap<String, String>();
         
         // add the new result
         resultMap.put(Integer.toString(relationshipId), 
            functionality + ":" + relationshipId);
         
         /*
          * The value 'Not Called' is assumed to be set after each reset of the 
          * test application to which results are posted.
          */
         if (!resultString.equals("Not Called"))
         {
            // add existing results
            StringTokenizer tokenizer = new StringTokenizer(resultString, 
               delimiter);
            while (tokenizer.hasMoreTokens())
            {
               String token = tokenizer.nextToken();
               String key = token.substring(token.indexOf(":")+1);
               resultMap.put(key, token);
            }
         }
         
         // produce result string
         Object[] values = resultMap.values().toArray();
         for (int i=0; i<values.length; i++)
         {
            if (i == 0)
               resultString = "";
            else
               resultString += delimiter;
            
            resultString += values[i].toString();
         }
      }
      
      return resultString;
   }
   
   /**
    * Checks the supplied param for the point of execution and converts this 
    * to one of the <code>CTX_xxx</code> values.
    * 
    * @param context the execution context for which to get the context string,
    *    assumed not <code>null</code>.
    * @return teh context string, never <code>null</code>or empty. If it is not
    *    a known context, <code>CTX_UNKNOWN</code> is returned.
    */
   private String getContextString(IPSExecutionContext context)
   {
      if (context.isPreCheckin())
         return CTX_PRE_CHECKIN;
      else if(context.isPostCheckout())
         return CTX_POST_CHECKOUT;
      else if (context.isPreConstruction())
         return CTX_PRE_CONSTRUCTION;
      else if (context.isPreDestruction())
         return CTX_PRE_DESTRUCTION;
      else if (context.isPostWorkflow())
         return CTX_POST_WORKFLOW;
      else if (context.isPreWorkflow())
         return CTX_PRE_WORKFLOW;
      else if (context.isPreClone())
         return CTX_PRE_CLONE;
      else if (context.isPreUpdate())
         return CTX_PRE_UPDATE;
      else
         return CTX_UNKNOWN;
   }
   
   /**
    * The method behaves differently based on the value of 
    * <code>functionality</code>. If <code>functionality</code> is not equal 
    * to <code>FUNCTIONALITY_PROGRAMMING_ERROR</code>, {@link #postResult(
    * IPSRequestContext, Document) postResult} is called.
    * 
    * @param functionality the functionality to be executed, assumed not 
    *    <code>null</code> or empty and to be one of 
    *    <code>FUNCTIONALITY_PASS</code>, <code>FUNCTIONALITY_FAIL</code> or
    *    <code>FUNCTIONALITY_PROGRAMMING_ERROR</code>.
    * @param result the effect result into which to set the result, assumed
    *    not <code>null</code>.
    */
   @SuppressWarnings("null")
   private void executeFunctionality(IPSRequestContext request, 
      String functionality, PSEffectResult result, Document resultDoc)
   {
      try
      {
         if (functionality.equalsIgnoreCase(FUNCTIONALITY_FAIL))
            result.setError("Method failed.");
         else if (functionality.equalsIgnoreCase(FUNCTIONALITY_PROGRAMMING_ERROR))
         {
            // produce NullPointerException
            ///String test = null;
            //test = test.trim();
            //TODO: FIXME Broken test
         }
         else
            result.setSuccess();
      }
      finally
      {
         postResult(request, resultDoc);
      }
   }
   
   /**
    * Validates the supplied parameter array. The array cannot be 
    * <code>null</code> and the first element must not be <code>null</code> or
    * empty. 
    * 
    * @param params the parameter array to be validated, may be 
    *    <code>null</code> or empty.
    */
   private void validateParameters(Object[] params)
   {
      if (params == null)
         throw new IllegalArgumentException("params cannot be null");
         
      if (params.length <= 0 || params[0] == null)
         throw new IllegalArgumentException(
            "params[0] (executionContext) is required");
   }
   
   /**
    * Get the execution contexts for the supplied parameters.
    * 
    * @param params the parameters from which to get the executon contexts
    *    from, assumed not <code>null</code> or empty.
    * @return a set with all execution contexts. The set values are
    *    <code>String</code> objects with the execution context value. Never
    *    <code>null</code> or empty, only entries defined in 
    *    <code>VALID_CONTEXTS</code> will be returned.
    */
   private Set getExecutionContexts(Object[] params)
   {
      Set<String> contexts = new HashSet<String>();

      String param = params[0].toString().trim();
      StringTokenizer tokenizer = new StringTokenizer(param, ",");
      while (tokenizer.hasMoreTokens())
      {
         String token = tokenizer.nextToken();
         String context = (String) VALID_CONTEXTS.get(token.toLowerCase());
         if (context == null)
         {
            String message = "params[0]: executionContext \"{0}\" is not valid";
            MessageFormat.format(message, new Object[] { token} );
            throw new IllegalArgumentException(message);
         }
         
         contexts.add(context);
      }
      
      if (contexts.contains(RS_ALL))
      {
         contexts = new HashSet<String>();
         contexts.add(RS_ALL);
      }
      
      return contexts;
   }
   
   /**
    * Get the method functionality. The suppliedd functionality can be 
    * <code>null</code> in which case <code>FUNCTIONALITY_PASS</code> is 
    * returned. For all unknown functionality strings an 
    * <code>IllegalArgumentException</code> will be thrown.
    * 
    * @param params the parameters from which to get the effect method
    *    functionality, assumed not <code>null</code> or empty.
    * @return the functionality that a method should perform, never 
    *    <code>null</code> or empty, always one of 
    *    <code>FUNCTIONALITY_PASS</code>, <code>FUNCTIONALITY_FAIL</code> or
    *    <code>FUNCTIONALITY_PROGRAMMING_ERROR</code>. If functionality is 
    *    <code>null</code>, <code>FUNCTIONALITY_PASS</code> is returned.
    */
   private String getFunctionality(String functionality)
   {
      if (functionality == null)
         return FUNCTIONALITY_PASS;

      if (!functionality.equalsIgnoreCase(FUNCTIONALITY_PASS) &&
         !functionality.equalsIgnoreCase(FUNCTIONALITY_FAIL) &&
         !functionality.equalsIgnoreCase(FUNCTIONALITY_PROGRAMMING_ERROR))
      {
         String message = "params[1]: functionality \"{0}\" is not valid";
         MessageFormat.format(message, new Object[] { functionality} );
         throw new IllegalArgumentException(message);
      }
      
      return functionality;
   }
   
   /**
    * Lookup the current result for the supplied test id from the test
    * application that stores all test results for the relationship effect
    * test script.
    * 
    * @param request teh request used to do the lookup, assumed not
    *    <code>null</code>. No parameters are expected in the supplied request.
    * @param testId the test id for which to lookup the result, assumed
    *    not <code>null</code> or empty.
    * @return the current result string looked up from the test application
    *    for the supplied test id, never <code>null</code>, mey be empty.
    */
   private String getResult(IPSRequestContext request, String testId) 
   {
      String resultString = "";
         
      try
      {
         Map<String, String> params = new HashMap<String, String>();
         params.put("tst_resultid", testId);
         IPSInternalRequest ir = request.getInternalRequest(
            "testRelationshipSupport/getResults", params, false);
         if (ir != null)
         {
            Document doc = ir.getResultDoc();
            NodeList results = doc.getElementsByTagName("Result");
            if (results != null)
            {
               Element result = (Element) results.item(0);
               NodeList values = result.getChildNodes();
               for (int i=0; i<values.getLength(); i++)
               {
                  Node value = values.item(i);
                  if (value instanceof Text)
                     resultString += ((Text) value).getData();
               }
            }
         }
      }
      catch (PSException e)
      {
         /*
          * This can only happen if the test application is not installed or
          * not running. The autotest would fail before using this exit for 
          * that case.
          */
         e.printStackTrace();
      }
         
      return resultString;
   }
   
   /**
    * Post the supplied result to the test relationship results table.
    * 
    * @param request the request used to make the post, assumed not 
    *    <code>null</code>.
    * @param result the result document to post, assumed not 
    *    <code>null</code> or empty.
    */
   private void postResult(IPSRequestContext request, Document result) 
   {
      try
      {
         request.setInputDocument(result);
      
         Map<String, String> params = new HashMap<String, String>();
         params.put("DBActionType", "UPDATE");
         IPSInternalRequest ir = request.getInternalRequest(
            "testRelationshipSupport/putResults", params, false);
         if (ir != null)
            ir.performUpdate();
      }
      catch (PSException e)
      {
         /*
          * This can only happen if the test application is not installed or
          * not running. The autotest would fail before using this exit for 
          * that case.
          */
         e.printStackTrace();
      }
   }
   
   /**
    * Create a result document that can be posted to the 
    * <code>testRelationshipSupport/putResults</code> resource. The DTD is:
    * &lt;!ELEMENT TestEffectAPIResults (Result*)&gt;
    * &lt;!ELEMENT Result (#PCDATA)&gt;
    * &lt;!ATTLIST Result
    *    id CDATA #REQUIRED
    * &gt;
    * 
    * @param testId the result id, assumed not <code>null</code> or empty.
    * @param resultText the result text, assumed not <code>null</code> or empty.
    * @return the result document, never <code>null</code>.
    */
   private Document createResult(String testId, String resultText)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      Element root = doc.createElement("TestRelationshipResults");
      doc.appendChild(root);
      
      Element result = doc.createElement("Result");
      result.setAttribute("id", testId);
      root.appendChild(result);
      
      Text resultValue = doc.createTextNode(resultText);
      result.appendChild(resultValue);
      
      return doc;
   }
   
   /**
    * Constant that defines the functionaliy to pass.
    */
   private static final String FUNCTIONALITY_PASS = "pass";

   /**
    * Constant that defines the functionaliy to fail.
    */
   private static final String FUNCTIONALITY_FAIL = "fail";

   /**
    * Constant that defines the functionaliy to produce a programming error.
    */
   private static final String FUNCTIONALITY_PROGRAMMING_ERROR = 
      "programmingerror";
   
   /**
    * The context type used to run effects in all contexts.
    */
   private static final String RS_ALL = "all";
   
   /**
    * The context type used to run effects before a relationship construction.
    */
   private static final String RS_PRE_CONSTRUCTION = "preconstruction";

   /**
    * The context type used to run effects before a relationship destruction.
    */
   private static final String RS_PRE_DESTRUCTION = "predestruction";

   /**
    * The context type used to run effects before a workflow transition is
    * executed.
    */
   private static final String RS_PRE_WORKFLOW = "preworkflow";

   /**
    * The context type used to run effects after a workflow transition is
    * executed.
    */
   private static final String RS_POST_WORKFLOW = "postworkflow";

   /**
    * The context type used to run effects before a checkin.
    */
   private static final String RS_PRE_CHECKIN = "precheckin";

   /**
    * The context type used to run effects before a checkout.
    */
   private static final String RS_POST_CHECKOUT = "postcheckout";

   /**
    * The context type used to run effects before a relationship is updated.
    */
   private static final String RS_PRE_UPDATE = "preupdate";

   /**
    * The context type used to run effects just before creating a clone of an 
    * existing item is createed.
    */
   private static final String RS_PRE_CLONE = "preclone";
   
   /**
    * A map with all valid execution context strings, never <code>null</code>
    * or empty.
    */
   private static final Map<String, String> VALID_CONTEXTS = new HashMap<String, String>();
   static
   {
      VALID_CONTEXTS.put(RS_PRE_CONSTRUCTION, RS_PRE_CONSTRUCTION);
      VALID_CONTEXTS.put(RS_PRE_DESTRUCTION, RS_PRE_DESTRUCTION);
      VALID_CONTEXTS.put(RS_PRE_WORKFLOW, RS_PRE_WORKFLOW);
      VALID_CONTEXTS.put(RS_POST_WORKFLOW, RS_POST_WORKFLOW);
      VALID_CONTEXTS.put(RS_PRE_CHECKIN, RS_PRE_CHECKIN);
      VALID_CONTEXTS.put(RS_POST_CHECKOUT, RS_POST_CHECKOUT);
      VALID_CONTEXTS.put(RS_PRE_UPDATE, RS_PRE_UPDATE);
      VALID_CONTEXTS.put(RS_PRE_CLONE, RS_PRE_CLONE);
      VALID_CONTEXTS.put(RS_ALL, RS_ALL);
   };
   
   // execution context strings
   private static final String CTX_PRE_CHECKIN = "pre-checkin";
   private static final String CTX_POST_CHECKOUT = "post-checkout";
   private static final String CTX_PRE_CONSTRUCTION = "pre-construction";
   private static final String CTX_PRE_DESTRUCTION = "pre-destruction";
   private static final String CTX_POST_WORKFLOW = "post-workflow";
   private static final String CTX_PRE_WORKFLOW = "pre-workflow";
   private static final String CTX_PRE_CLONE = "pre-clone";
   private static final String CTX_PRE_UPDATE = "pre-update";
   private static final String CTX_UNKNOWN = "unknown";
}
