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
package com.percussion.xmldom;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSPurgableTempFile;

import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.Document;

/**
 * A Rhythmyx extension that copies a temporary XML document into an attached
 * file.
 * <p>
 * The text representation of the node or document is copied into an XML file.
 * When called as a pre-exit, the output is stored in an attached file.
 * <p>
 * The parameters to the extension are:
 *
 * <table border="1">
 *   <tr><th>Param #</th><th>Name</th><th>Description</th><th>Required?</th>
 *   <th>default value</th><tr>
 *   <tr>
 *     <td>1</td>
 *     <td>sourceObjectName</td>
 *     <td>the name of the temporary XML document object.</td>
 *     <td>no</td>
 *     <td>XMLDOM</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>sourceNodeName</td>
 *     <td>identifies the node within the source document which will be copied</td>
 *     <td>no</td>
 *     <td>"." will cause the entire document to be copied.</td>
 *   </tr>
 *   <tr>
 *     <td>3</td>
 *     <td>destFileName</td>
 *     <td>destination file to store data into</td>
 *     <td>no</td>
 *     <td>&nbsp;</td>
 *   </tr>
 *   <tr>
 *     <td>4</td>
 *     <td>encoding</td>
 *     <td>Java name of encoding</td>
 *     <td>no</td>
 *     <td>platform default</td>
 *   </tr>
 * </table>
 * <p>
 * When called as a pre-exit, the sourceName <code>InputDocument</code> can
 * also be supplied. This name refers to the XML document loaded with the
 * update request.
 * <p>
 * The output for a pre-exit is always stored in an HTML parameter as a File.
 **/
public class PSXdDomToFile extends PSDefaultExtension
    implements IPSRequestPreProcessor
{

   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdDomToFile} for parameter details.
    *
    * @param request the request context for this request
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {

      PSXmlDomContext contxt = new PSXmlDomContext(ms_className, request);

      String sourceObjectName = PSXmlDomUtils.getParameter(params,0,
            PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);
      String sourceNodeName = PSXmlDomUtils.getParameter(params,1,"");
      String destName = PSXmlDomUtils.getParameter(params,2,"");
      String encoding = PSXmlDomUtils.getParameter(params,3,"");

      Document sourceDoc;
      if(sourceObjectName.equals("InputDocument"))
      {
         sourceDoc = request.getInputDocument();
      }
      else
      {
         sourceDoc = (Document)request.getPrivateObject(sourceObjectName);
      }

      if(sourceDoc == null)
      {
         request.printTraceMessage("Source Document not present");
         return;
      }

      FileOutputStream tfstream = null;
      try
      {
         String resultText = PSXmlDomUtils.copyTextFromDocument(contxt,sourceDoc,
            sourceNodeName);

         PSPurgableTempFile tempfile = new PSPurgableTempFile("xml","xml",null);
         tfstream = new FileOutputStream((File)tempfile);
         if(encoding.trim().length() == 0)
         {
            //no encoding specified, use platform default
            tfstream.write(resultText.getBytes());
         }
         else
         {
            tfstream.write(resultText.getBytes(encoding));
         }

         request.setParameter(destName, tempfile);
      }
      catch(Exception e)
      {
         contxt.handleException(e);
      }
      finally
      {
         if (tfstream != null)
         {
            try { tfstream.close(); }
            catch (Exception eio) {}  // ignore any error
         }
      }
   }

  /**
    * the name of the class: used for error handling.
    */
   private static final String  ms_className = "PSXdDomToFile";
}
