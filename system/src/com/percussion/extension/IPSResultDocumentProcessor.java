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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.extension;

import com.percussion.server.IPSRequestContext;

import org.w3c.dom.Document;

/**
 * The IPSResultDocumentProcessor interface must be implemented by
 * extensions which process the result document.
 * <P>
 * When Rhythmyx processes the request, it generates an XML document.
 *
 * @author   Tas Giakouminakis
 * @version   1.1
 * @since   1.0
 */
public interface IPSResultDocumentProcessor extends IPSExtension
{
/**
 * Is the processResultDocument method implemented by this class
 * capable of modifying the style sheet? If the method can change
 * the style sheet by setting the XML-stylesheet processing
 * instruction, this method returns <code>true</code>.
 * If the method never attempts to modify the XML-stylesheet
 * processing instruction, this method returns
 * <code>false</code>. This answer helps the designer
 * understand the implications of calling this extension.
 *
 * @return   <code>true</code> if the
 *              {@link #processResultDocument processResultDocument}
 *              method may modify the XML-stylesheet
 *              processing instruction
 */
   public boolean canModifyStyleSheet();

   /**
    * Modifies the result document that will be returned to the requestor.
    * <p>
    * When Rhythmyx processes the request, it generates an XML document.
    * <p>
    * If certain nodes in the tree will be changed, but the overall
    * structure is not changed, the document passed into this method
    * should be returned. If an unrelated document is being built, or
    * major structural changes are being made, it may be better to
    * create a new XML document.
    * <P>
    * <em>NOTE:</em> The implementation of this method must be
    * safe for multi-threaded use. One instance of the extension will
    * be defined for each usage in an application. For
    * example, if the application defines the same extension in five
    * different cases of different data as input), five instances
    * of this extension will be created. When processing requests, the
    * same instance may be accessed for several threads, each with its own
    * set of data. As such, any variables used during run-time execution
    * should be defined within the method (not the class). Another
    * alternative is to use variables of type java.lang.ThreadLocal to
    * define thread specific copies of the variable.
    *
    * @param   params      the parameter values supplied with the request in
    *                         the appropriate order
    *
    * @param   request     the request context object
    *
    * @param   resultDoc   the result XML document, can be <code>null</code>
    *                      
    *
    * @return   the processed document. If no changes are made, returns the
    *             resultDoc which was passed in.
    *              If <code>null</code> is returned, the HTTP status code
    *              404, file not found, will be returned to the caller of this method.
    *
    * @exception   PSParameterMismatchException
    *                          if a call to setParamValues was never made,
    *                          or the runtime parameters specified in that
    *                          call are incorrect for the usage of this
    *                          extension.
    *
    * @exception   PSExtensionProcessorException
    *                          if any other exception occurs which
    *                          prevents the proper handling of this request.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException,
         PSExtensionProcessingException;
}

