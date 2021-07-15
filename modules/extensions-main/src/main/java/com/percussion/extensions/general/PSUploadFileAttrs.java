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

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.PSPurgableTempFile;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Map;

/**
 * This class calculates the size of an uploaded file and appends an html
 * parameter with current date value in a given date format.
 */
public class PSUploadFileAttrs extends PSDefaultExtension
   implements IPSRequestPreProcessor
{


  /**
   * Does the all the work for the class.  See the class description.
   *
   * @param params An assortment of settings, including:
   *    -An html param name that gets the file content. That is, the name
   *     attribute value in the tag <input type=file name=contentbody>
   *     i.e. contentbody.
   *    -An html param name to receive the size of the uploaded file in bytes.
   *    -An html param name to get the current datetime stamp in the format
   *     specified for next parameter. This can be for example, used to store
   *     as modifed date.
   *    -A Date format literal. e.g. MM/dd/yyyy hh:mm:ss a.  This can be
   *     backend database specific.
   *    -A literal value to specify a size limit. If the size of the file being
   *     uploaded exceeds this limit, an exception is thrown. This parameter is
   *     option. A blank or <= 0 means no limit.
   *
   * @param request An object holding more HTML parameters.
   *
   * @throws PSExtensionProcessingException
   *            -If one or more parameters missing in PSUploadFileAttrs.
   *            -If empty or null file name parameter in PSUploadFileAttrs.
   *            -If empty or null file size parameter in PSUploadFileAttrs.
   *            -If empty or null HTML Parameters table.
   *            -If file size exceeds the byte limit.
   */
   public void preProcessRequest( java.lang.Object[] params,
                             com.percussion.server.IPSRequestContext request)
      throws PSAuthorizationException,
            PSRequestValidationException,
            PSParameterMismatchException,
            PSExtensionProcessingException
   {
      // validate parameters
      if(params.length < 2)
      {
         throw new PSExtensionProcessingException( 0,
                       "One or more parameters missing in PSUploadFileAttrs.");
      }
      if(params[0] == null || params[0].toString().trim().length() == 0)
      {
         throw new PSExtensionProcessingException( 0,
                    "Empty or null file name parameter in PSUploadFileAttrs.");
      }
      if(params[1] == null || params[1].toString().trim().length() == 0)
      {
         throw new PSExtensionProcessingException( 0,
                     "Empty or null file size parameterin PSUploadFileAttrs.");
      }

      String fileNameParam =  params[0].toString().trim();
      String fileSizeParam =  params[1].toString().trim();

      String dateParam = null;
      if(params.length > 2 && null != params[2])
         dateParam = params[2].toString().trim();


      String dateFormatString = null;
      if(params.length > 3 && null != params[3])
         dateFormatString = params[3].toString().trim();

      if(null == dateFormatString || dateFormatString.length() < 1)
      {
         dateFormatString = "MM/dd/yyyy hh:mm:ss a";
      }
      else
         dateFormatString = dateFormatString.trim();

      Long   fileSizeMax   = new Long(0L);
      if(params.length > 4)
      {
         try
         {
            String tmp = params[4].toString().trim();
            if(null != tmp)
               fileSizeMax = new Long(tmp);
         }
         catch(Exception e)
         {
         }
      }

      // retrieve file contents from HTML parameters hash table
      Map<String,Object> htmlParams = request.getParameters();
      if (htmlParams == null || htmlParams.isEmpty())
      {
         throw new PSExtensionProcessingException( 0,
                                      "Empty or null HTML Parameters table." );
      }

      if(!htmlParams.containsKey(fileNameParam))
         return;

      PSPurgableTempFile tmpFile =
                           (PSPurgableTempFile)htmlParams.get( fileNameParam );

      if(tmpFile != null)
      {
         Long fileSize = new Long( tmpFile.length());

         // check for maximum size limit
         if(fileSizeMax.intValue() > 0 &&
                                         fileSize.compareTo( fileSizeMax ) > 0)
         {
            throw new PSExtensionProcessingException(0,
               "File size (" + fileSize.toString() +
               ") exceeds limit of " + fileSizeMax.toString() + " bytes.");
         }
         // write file size to HTML parameters hash table
         htmlParams.put( fileSizeParam, fileSize.toString() );

         if(null == dateParam || dateParam.length() < 1)
            return;

         FastDateFormat format = FastDateFormat.getInstance(dateFormatString);
         Date current = new Date();

         // write current date to HTML parameters hash table
         htmlParams.put(dateParam, format.format(current));
      }
   }
}
