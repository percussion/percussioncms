/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.validation;

// Imports


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSFieldValidator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.data.PSConversionException;
import com.percussion.server.IPSRequestContext;

import com.percussion.util.PSPurgableTempFile;

/**
 * A Percussion CMS post-exit to perform content field validation. This exit uses the
 * same code as in in the com.percussion.extensions.general.PSFileInfo class.
 *
 * @author Roy Kiesler
 * @version 1.0
 */
public class PSOFileUploadValidation implements IPSFieldValidator
{
   // Constants
   private final String CLASSNAME = getClass().getName();
   private static Log log = LogFactory.getLog(PSOFileUploadValidation.class); 
   // Fields
   private String fieldName = "";
   private String excludedMimeTypes = "";
   private long maxFileSize = 0;
   
   /**
    * @param extensionDef default extension definition
    * @param codeRoot the 'root' directory for this extension.
    * @throws PSExtensionException if the codeRoot does not exist, or is not
    * accessible. Also thrown for any other initialization errors that will
    * prohibit this extension from doing its job correctly, such as invalid or
    * missing properties.
    */
   public void init(IPSExtensionDef extensionDef, java.io.File codeRoot)
      throws PSExtensionException
   {
      log.debug( "Initializing " + CLASSNAME + "...." );
   }

   /**
    * @param params list of parameters
    * <ul>
    * <li><code>fieldName</code> - the name attribute of the PSXField that is
    * mapped to a sys_File control. Cannot be null.</li>
    * <li><code>maxSize</code> - the maximum allowed file size (in bytes);
    * (-1) means unlimited.</li>
    * <li><code>mimeTypes</code> - comma-separated list of allowed Mime types,
    * e.g., text/html, image/jpeg, etc.</li>
    * </ul>
    * @param request the request context object
    * @return the result document, if no validation errors encountered;
    * otherwise, an instance of PSItemErrorDoc.
    * @throws PSConversionException if an error occurred during data conversion.
    * This exception takes two parameters, a message code and an argument. You
    * should always pass in zero (0) for the message code.
    * <p>An example of usage in a content editor definition follows:</p>
    * <code>
    * &lt;FieldRules&gt;<br/>
    * &nbsp;&nbsp;&lt;PSXFieldValidationRules&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXRule&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXExtensionCallSet id="0"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXExtensionCall id="0"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;name&gt;Java/user/PSOFileUploadValidation&lt;/name&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXExtensionParamValue id="0"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXTextLiteral id="0"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;text&gt;fileupload&lt;/text&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXTextLiteral&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXExtensionParamValue&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXExtensionParamValue id="1"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXTextLiteral id="0"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;text&gt;50000&lt;/text&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXTextLiteral&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXExtensionParamValue&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXExtensionParamValue id="1"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXTextLiteral id="0"&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;text&gt;text/plain&lt;/text&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXTextLiteral&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXExtensionParamValue&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXExtensionCall&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXExtensionCallSet&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/PSXRule&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&lt;ErrorMessage&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;PSXDisplayText&gt;File size exceeds limit&lt;/PSXDisplayText&gt;<br/>
    * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/ErrorMessage&gt;<br/>
    * &nbsp;&nbsp;&lt;/PSXFieldValidationRules&gt;<br/>
    * &lt;/FieldRules&gt;
    * </code>
    */
   public Object processUdf(
      Object[] params,
      IPSRequestContext request ) throws PSConversionException
   {
      final String METHOD_NAME = "processUdf";
      String emsg; 
      // check parameters
      if ( params.length >= 1 )
      {
         if ( params[0] == null || params[0].toString().trim().length() == 0 )
         {
            emsg = "Empty or null field name parameter.";
            log.error(emsg); 
            throw new PSConversionException( 0, emsg );
         }
         fieldName = params[0].toString().trim();
      }

      // get the maximum file size, if specified
      if ( params.length >= 2 && params[1] != null )
      {
         String mxSizeParam = params[1].toString().trim();
         
         if (StringUtils.isBlank(mxSizeParam) )
         {
            // assume unlimited size is allowed
            maxFileSize = (-1);
         }
         else
            try {
               maxFileSize = Long.decode( mxSizeParam ).longValue();
            } catch ( NumberFormatException nfex ) {
               log.error(" Invalid file size parameter " + mxSizeParam);
               throw new PSConversionException( 0,
               "Invalid file size parameter." );
            }
      }

      // get the list of Mime types to be excluded, if specified
      if ( params.length >= 3 )
      {
         if ( params[2] != null && params[2].toString().trim().length() > 0 )
         {
            excludedMimeTypes = params[2].toString().trim();
         }
      }

      return new Boolean( doValidation( request ) );
   }

   /**
    * Perform validation - executes file upload validation routines
    * @param request the request context
    * @return false if validation fails
    * @throws PSConversionException if an error occurred during data conversion.
    * This exception takes two parameters, a message code and an argument. You
    * should always pass in zero (0) for the message code.
    * to the error document
    */
   private boolean doValidation(
      IPSRequestContext request ) throws PSConversionException
   {
      final String METHOD_NAME = "doValidation";
      boolean bFlag = true;

      // validate file upload size
      bFlag = doFileSizeValidation( request );
      if ( !bFlag ) return bFlag;

      // validate file Mime type
      bFlag = doFileMimeValidation( request );
      if ( !bFlag ) return bFlag;

      // done
      return bFlag;
   }

   /**
    * Prevent uploads of files whose size exceeds a specified limit
    * @param request the request context
    * @return false if validation fails
    * @throws PSConversionException if an error occurred during data conversion.
    * This exception takes two parameters, a message code and an argument. You
    * should always pass in zero (0) for the message code.
    */
   private boolean doFileSizeValidation(
      IPSRequestContext request ) throws PSConversionException
   {
      final String METHOD_NAME = "doFileSizeValidation";
      boolean bFlag = true;

      if ( maxFileSize != (-1) )
      {
         // verify that we are not trying to clear the upload field
         String clearFlag = request.getParameter( fieldName + "_clear" );
         if ( clearFlag != null && clearFlag.trim().length() > 0 )
            return true;

         // get the file being uploaded
         Object obj = request.getParameterObject( fieldName );
         if ( obj instanceof PSPurgableTempFile )
         {
            PSPurgableTempFile tempFile = (PSPurgableTempFile)obj;
            // mime type	  String s7 = pspurgabletempfile.getSourceContentType();
            long fileSize = tempFile.length();
            if ( fileSize > maxFileSize ) bFlag = false;
         }
      }

      // done
      return bFlag;
   }

   /**
    * Prevent uploads of files whose mime type is prohibited
    * @param request the request context
    * @return false if validation fails
    * @throws PSConversionException if an error occurred during data conversion.
    * This exception takes two parameters, a message code and an argument. You
    * should always pass in zero (0) for the message code.
    */
   private boolean doFileMimeValidation(
      IPSRequestContext request ) throws PSConversionException
   {
      final String METHOD_NAME = "doFileMimeValidation";
      boolean bFlag = true;

      if ( excludedMimeTypes != null && excludedMimeTypes.trim().length() > 0 )
      {
         // verify that we are not trying to clear the upload field
         String clearFlag = request.getParameter( fieldName + "_clear" );
         if ( clearFlag != null && clearFlag.trim().length() > 0 )
            return true;

         // get the file being uploaded
         Object obj = request.getParameterObject( fieldName );
         if ( obj instanceof PSPurgableTempFile )
         {
            PSPurgableTempFile tempFile = (PSPurgableTempFile)obj;
            long fileSize = tempFile.length();
    		log.debug("File size is = " + fileSize);
    		if ( fileSize > 0 ) 
			{
            	String mimeType = tempFile.getSourceContentType(); // <-- NULL POINTER
            	if ( excludedMimeTypes.indexOf( mimeType ) >= 0 ) bFlag = false;
			}
            }
      }

      // done
      return bFlag;
   }
}