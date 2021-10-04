/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.extensions.general;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A Rhythmyx pre-exit that tokenizes delimited input parameters into multiple
 * lists for insert.
 *
 * <p>
 * For example, the related content search screen contains a series of
 * checkboxes. The value of each checkbox contains the contentid and
 * variantid of the inserted child document separated by a delimiter.
 * All  of the checkboxes have the same name.  This results in a list of values
 * in an ArrayList.
 * <p>
 * The function of this exit is to parse the delimited array into
 * two or more other arrays. The number of arrays parsed depends on the number
 * of parameters passed.
 *
 * <p>
 * There are N parameters:
 * <ul>
 *   <li>CheckBoxArrayName
 *   <li>FirstOutputArrayName
 *   <li>SecondOutputArrayName
 *   <li>etc, etc.
 * </ul>
 *
 * <p>
 * This exit supports 3 delimiters: semicolon, period, and comma.
 *
**/
public class PSParameterTokenizer extends PSDefaultExtension implements
IPSRequestPreProcessor {

   private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);

/**
 * Process the pre-exit request.
 *
 * @param params the array of parameter objects. See the class summary for
 * details.
 *
 * @param request the request context for the exit
 *
 *
 * @throws PSExtensionProcessingException in case of any errors
 *
 **/
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
   int i;
   ArrayList<HTMLParameter> outputParams = new ArrayList<>(params.length);

   try  {

      if(params[0] == null || params[0].toString().trim().length() == 0) {
         throw new PSExtensionProcessingException(0,
         "The Input Parameter Name must not be null");
         }
      String checkBoxArrayName=params[0].toString().trim();

      /*
      * This exit will be called every time the resource is invoked
      * perform a quick check to see if there's anything to do
      */
      Map<String,Object> htmlParams = request.getParameters();
      if(htmlParams == null) {
         // no, this request has no HTML parameters at all
         request.printTraceMessage("No HTML parameters found");
         return;
         }
      Object inputArray = htmlParams.get(checkBoxArrayName);
      if(inputArray == null) {
         // there's no input parameter in the map.
         request.printTraceMessage("The input parameter is null");
         return;
         }

      //special case: an empty string, we leave now.
      //Note: this won't catch an empty array list
      if(inputArray.toString().trim().length() == 0) {
         request.printTraceMessage("The input parameter is empty");
         return;
         }

      // now look at the rest of the parameter list and build the name list.
      for(i=1; i < params.length && params[i] != null; i++) {
         String pName = params[i].toString().trim();
         if(pName.length() > 0) {
            outputParams.add(new HTMLParameter(pName));
            }
         }

      if(inputArray instanceof ArrayList) {
         // this is an array, so we need to iterate across it
         ArrayList inputList = (ArrayList)inputArray;

         //make sure that the list is not empty before we start
         if(inputList.isEmpty()){
            request.printTraceMessage("the input list is empty");
            return;
            }
         request.printTraceMessage("multiple values found:"
            + inputArray);

         //iterate across the list of input parameters
         for (Object o : inputList) {
            String inputValue = (String) o;
            StringTokenizer tok = new StringTokenizer(inputValue, SEPARATORS);
            //now iterate across the tokens of the string
            for (HTMLParameter currParam : outputParams) {
               if (tok.hasMoreTokens()) {
                  currParam.addParamValue(tok.nextToken());
               } else {
                  currParam.addParamValue("");
               }
            }   // while more tokens
         }  // while more parameters
         // now go back and add the parameters to the request's map
         for (HTMLParameter currParam : outputParams) {
            htmlParams.put(currParam.getName(), currParam.getArray());
         }
         }
      else
         {
         //not a list, so assume it's a single valued string
         String inputValue = inputArray.toString();
         request.printTraceMessage("single value found:" + inputValue);
         StringTokenizer tok = new StringTokenizer(inputValue,SEPARATORS);

         //iterate across the tokens
            for (HTMLParameter currParam : outputParams) {
               //this is much easier, just add to the HTML parameter map directly
               if (tok.hasMoreTokens()) {
                  htmlParams.put(currParam.getName(), tok.nextToken());
               }
            }
         }

   } catch (Exception e){ // just in case we missed something
      log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      request.printTraceMessage(PSExceptionUtils.getMessageForLog(e));
      throw new PSExtensionProcessingException(this.getClass().getName(),e);
      }
}

/**
* Separator characters determine the boundaries between the tokens.
**/
private static final String SEPARATORS = ";.,";

/**
* This inner class represents a single HTML parameter, which may have multiple
* values.
**/
private class HTMLParameter {

   /**
   * the name of this HTML parameter. This name will appear in the HTML form,
   * and in the resource's mapper.
   **/
   private String m_ParaName;

   /**
   * the array of values for this parameter.  It will be <code>null</code> for
   * new paraemeters and parameters which have only one value.
   **/
   private ArrayList m_Array;

   /**
   * build a new HTMLParameter with the specified name.
   *
   * @param pName the name of the HTML parameter.
   **/
   HTMLParameter(String pName) {
      m_ParaName = pName;
      }

   /**
   * add a String value to an existing parameter.
   *
   * @param sValue the string value to append to this parameter.
   **/
   private void addParamValue(String sValue) {
      //the array is not always needed
      // so we add it the first time through
      if(m_Array == null) {
         m_Array = new ArrayList();
         }
      m_Array.add(sValue);
      }

   /**
   * return the array value of this parameter. Will be <code>null</code>
   * if the HTML parameter is newly initialized, or only has a single value
   *
   * @return the array value, which may be <code>null</code>.
   **/
   private ArrayList getArray() {
      return m_Array;
      }

   /**
   * return the name of this HTML Parameter.
   *
   * @return the name of the HTML parameter.
   **/
   String getName() {
      return m_ParaName;
      }
   }
}
