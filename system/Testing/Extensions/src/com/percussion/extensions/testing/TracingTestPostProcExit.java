/* *****************************************************************************
 *
 * [ TracingTestPostProcExit.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;

import java.util.Enumeration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class TracingTestPostProcExit
   implements IPSResultDocumentProcessor
{
   /**
    * Return false (this extension can not modify the style sheet).
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }


   /**
    * No-op
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {}

   /**
    * Adds a CGI variable and modifies the XML hierarchy by adding a node
    *
    * @param      params         the parameters for this extension; this is an
    * array of 4 Objects, a toString() is called to convert the object to a
    * String representation.
    *    nodeName - the name of the node to add
    *    nodeValue - the value of the node to add
    *    cgiVarName - the name of the cgiVar to add
    *    cgiVarValue - the value of the cgiVar to add
    *
    * @param      request         the request context
    *
    * @param      resultDoc      the result XML document
    *
    * @return                     <code>resultDoc</code> is always returned
    *
    * @exception  PSParameterMismatchException  if the parameter number is incorrect
    * @exception  PSExtensionProcessingException      if any parameter is <code>null</code>
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      // send out a trace message
      request.printTraceMessage("TracingTestPostProcExit is executing");

      // validate our input params
      int len = (params == null) ? 0 : params.length;
      if (len != 4)  // four parameters are required
         throw new PSParameterMismatchException(len, 4);

      for (int j=0; j < 4; j++){
         if (params[j] == null){
            String msg = "parameters must not be null to call processResultDocument";
            IllegalArgumentException ex = new IllegalArgumentException(msg);
            throw new PSExtensionProcessingException( getClass().getName(), ex);
         }
      }

      String nodeName    = params[0].toString();
      String nodeValue   = params[1].toString();
      String cgiVarName  = params[2].toString();
      String cgiVarValue = params[3].toString();

      // get the doc root
      if (resultDoc == null)   // no doc, no work
         return resultDoc;

      Element root = resultDoc.getDocumentElement();
      if (root == null)   // no root, no work
         return resultDoc;

      // add the element
      PSXmlDocumentBuilder.addElement(resultDoc, root, nodeName, nodeValue);

      // get the headers from the request
      boolean hasVars = false;
      if (request != null)
      {
         Enumeration headers = request.getHeaders();
         hasVars = headers.hasMoreElements();
      }
      if (!hasVars)
         return resultDoc;

      // now add the new one
      request.setCgiVariable(cgiVarName, cgiVarValue);
      // TODO - how to handle this case?
      // request.setCgiVariables(cgiVars);
      throw new UnsupportedOperationException("Can't add cgivars");

   }

}

