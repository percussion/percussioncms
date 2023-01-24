/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.xmldom;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.StringWriter;

/**
 * This preprocessor exit moves the XML InputDocument into an
 * HTML parameter and removes it from the request context.
 * <p>
 * If an XML document is uploaded with the sys_File control (for example)
 * it will wind up in the Input Document, even if it has a different
 * file name.  Unfortunately, the filename, size, encoding, etc. are
 * lost when this happens.
 *
 * This exit has one one parameter: the Name of the HTML parameter to create.
 *
 */
public class PSXdMoveInputDocument extends PSDefaultExtension
      implements IPSRequestPreProcessor
{

   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdMoveInputDocument} for parameter details.
    * @param request the request context for this request
    *
    * @throws PSParameterMismatchException when the HTML parameter name is
    * missing or empty.
    */
   public void preProcessRequest(Object[] params,
                                 IPSRequestContext request)
         throws PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      String HTMLParamName = PSXmlDomUtils.getParameter( params, 0, null );
      if (null == HTMLParamName)
         throw new PSParameterMismatchException( params.length, 1 );

      Document inputDoc = request.getInputDocument();
      if (inputDoc == null)
         return; //no document, we are done...

      PSXmlTreeWalker walker = new PSXmlTreeWalker( inputDoc );
      StringWriter sw = new StringWriter();
      try
      {
         walker.write( sw, false );  //write the document WITHOUT indenting
      }
      catch(IOException ioe)
      {
         throw new PSExtensionProcessingException(0, ioe.getMessage());
      }
      request.setParameter( HTMLParamName, sw.toString() );
      request.setInputDocument( null );
   }
}
