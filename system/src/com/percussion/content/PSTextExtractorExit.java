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
package com.percussion.content;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSTextExtractorExit extends PSDefaultExtension implements
      IPSItemInputTransformer
{
   // see interface
   @Override
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      super.init(extensionDef, file);
      ms_fullExtensionName = extensionDef.getRef().toString();
      ms_msgPrefix = ms_fullExtensionName + ": ";
   }

   /**
    * Converts the data specified by the params to text, based on the
    * mimetype associated with the field.
    * 
    * @params The parameters, never <code>null</code>. The following params
    * are expected. <code>toString</code> is called on all parameters to
    * obtain their values unless otherwise specified. If a parameter value is
    * <code>null</code> or empty, it is considered to have been ommitted
    * (required parameters must be supplied):
    * 
    * <table>
    * <tr>
    * <th>Param #</th>
    * <th>Description</th>
    * <th>Required</th>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>Name of the field, that conatins the file data. </td>
    * <td>yes</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The name of the request parameter in which the converted text is to
    * be returned. The returned value will be stored as a <code>String</code>,
    * never <code>null</code>, may be empty. </td>
    * <td>yes</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>The name of the request parameter in which any error messages are to
    * be stored as text. If not supplied, the extension will throw exceptions
    * for any errors encountered. If supplied, any error encountered will be
    * written to this parameter, and the exit will silently return. </td>
    * <td>no</td>
    * </tr>
    * </table>
    * 
    * @param request The request context, guaranteed not to be <code>null</code>
    * by the interface.
    * 
    * @throws PSParameterMismatchException if a required parameter is missing,
    * or if a parameter value is invalid.
    * @throws PSExtensionProcessingException if an unsupported file type is
    * supplied, or if there are any other errors, and an error message request
    * parameter was not supplied.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
      PSParameterMismatchException, PSExtensionProcessingException
   {
      request.printTraceMessage(ms_msgPrefix + "Entering preProcessRequest()");
      // first two params required
      if (params.length < 2 || params[0] == null || params[1] == null)
      {
         throw new PSParameterMismatchException(ms_msgPrefix
               + "First two params are required");
      }
      String fieldParam = params[0].toString();
      if (StringUtils.isBlank(fieldParam))
      {
         throw new PSParameterMismatchException(ms_msgPrefix
               + "FieldName may not be empty.");
      }
      String outParam = params[1].toString();
      if (StringUtils.isBlank(outParam))
      {
         throw new PSParameterMismatchException(ms_msgPrefix
               + "OutputParam name may not be empty.");
      }
      if (params.length > 2 && params[2] != null
            && !StringUtils.isEmpty(params[2].toString()))
         m_errorMessageParam = params[2].toString();

      InputStream datais = null;
      try
      {
         // Get the field data.
         Object fieldData = request.getParameterObject(fieldParam);
         // If field data is null there is nothing to extract simply return
         if (fieldData == null)
         {
            return;
         }
         if (fieldData instanceof PSPurgableTempFile)
         {
            PSPurgableTempFile temp = (PSPurgableTempFile) fieldData;
            String sourceName = temp.getSourceFileName();
            if (StringUtils.isBlank(sourceName))
               return;
            datais = new FileInputStream((File) fieldData);
         }
         else
         {
            // assume it's base64 encoded data
            String base64Data = params[0].toString();
            if (StringUtils.isBlank(base64Data))
               return;
            datais = new ByteArrayInputStream(base64Data.getBytes());
         }

         String mimetype = extractMimetype(request, fieldParam);
         if (StringUtils.isBlank(mimetype))
         {
            String msg = ms_fullExtensionName
                  + "Failed to extract text from the field as mimetype specified on the filed is empty.";
            handleException(request, msg);
            return;
         }

         PSContentConverter converter;
         converter = new PSContentConverter(mimetype);
         String extractedText = converter.extractText(datais);
         String msg = "Extracted Text From File:\n" + extractedText;
         request.printTraceMessage(msg);
         ms_log.debug(msg);
         request.setParameter(outParam, extractedText);
      }
      catch (PSContentConversionException e)
      {
         handleException(request, e);
         return;
      }
      catch (FileNotFoundException e)
      {
         handleException(request, e);
         return;
      }
      finally
      {
         if (datais != null)
         {
            try
            {
               datais.close();
            }
            catch (IOException e)
            {
               // ignore
            }
         }
      }
   }

   /**
    * Extracts the mimetype from the request paramters for the given field.
    * 
    * @param request Assumed not <code>null</code>.
    * @param fieldParam Name of the filed for which the mimetype needs to be
    * extracted assumed not <code>null</code>.
    * @return mimetype or null if not found.
    * @throws PSExtensionProcessingException
    */
   private String extractMimetype(IPSRequestContext request, String fieldParam)
      throws PSExtensionProcessingException
   {
      String mimetype = null;
      // Extract the mimetype
      String contentTypeId = request
            .getParameter(IPSHtmlParameters.SYS_CONTENTTYPEID);
      if (StringUtils.isBlank(contentTypeId)
            || !StringUtils.isNumeric(contentTypeId))
      {
         String msg = ms_fullExtensionName
               + "Failed to extract text from field as a"
               + " required parameter sys_contenttypeid is missing in the request.";
         handleException(request, msg);
         return null;
      }

      try
      {
         PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
         PSItemDefinition itemDef = itemDefMgr.getItemDef(
               Integer.parseInt(contentTypeId), 
               PSItemDefManager.COMMUNITY_ANY);
         mimetype = itemDef.getFieldMimeType(fieldParam,request);
      }
      catch (NumberFormatException e)
      {
         //This should not happen as we are already checking for numeric
      }
      catch (PSInvalidContentTypeException e)
      {
         handleException(request, e.getLocalizedMessage());
         return null;
      }
      
      return mimetype;
   }

   /**
    * Convenient method to hanlde the exception. Writes the message to
    * m_errorMessageParam if not <code>null</code> otherwise throws
    * PSExtensionProcessingException.
    * 
    * @param request assumed not <code>null</code>.
    * @param e Exception that needs to be handled.
    * @throws PSExtensionProcessingException if m_errorMessageParam is
    * <code>null</code>.
    */
   private void handleException(IPSRequestContext request, String msg)
      throws PSExtensionProcessingException
   {
      ms_log.info(msg);
      if (m_errorMessageParam != null)
         request.setParameter(m_errorMessageParam, msg);
      else
         throw new PSExtensionProcessingException(0, msg);
   }

   /**
    * Convenient method to hanlde the exception. Writes the exception message to
    * m_errorMessageParam if not <code>null</code> otherwise throws
    * PSExtensionProcessingException.
    * 
    * @param request assumed not <code>null</code>.
    * @param e Exception that needs to be handled.
    * @throws PSExtensionProcessingException if m_errorMessageParam is
    * <code>null</code>.
    */
   private void handleException(IPSRequestContext request, Exception e)
      throws PSExtensionProcessingException
   {
      ms_log.info("Error converting the text",e);
      if (m_errorMessageParam != null)
         request.setParameter(m_errorMessageParam, e.getLocalizedMessage());
      else
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
   }

   /**
    * The fully qualified name of this extension. Intialized in the
    * {@link #init(IPSExtensionDef, File)} method, never <code>null</code>,
    * empty, or modified after that.
    */
   static private String ms_fullExtensionName = "";
   
   /**
    * The message prefix that includes the extension name and is used 
    * for logging.  Intialized in the {@link #init(IPSExtensionDef, File)}
    * method, never <code>null</code>, empty, or modified after that. 
    */
   private static String ms_msgPrefix = "";
   
   
   /**
    * The name of the request parameter in which any error messages are to be
    * stored as text. If not supplied, the extension will throw exceptions for
    * any errors encountered. If supplied, any error encountered will be written
    * to this parameter, and the exit will silently return.
    */
   private String m_errorMessageParam = null;
   
   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger ms_log = LogManager.getLogger(PSTextExtractorExit.class);

}
