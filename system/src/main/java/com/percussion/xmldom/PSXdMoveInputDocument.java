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
