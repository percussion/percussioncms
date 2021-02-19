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
package com.percussion.services.filestorage.extensions;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.filestorage.IPSFileMeta;
import com.percussion.services.filestorage.PSFileStorageServiceLocator;
import com.percussion.services.filestorage.data.PSMeta;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.isTrue;

/**
 * Similar to PSFleInfo but a single copy of the
 * file is stored using the file storage service and a unique
 * hash value is set on the field with suffix
 * {@link #HASH_PARAM_SUFFIX}.  Meta data is also extracted
 * from the file and stored in fields that are suffixed
 * '_fieldType' like 'img1_length'.
 * 
 * @author adamgent
 * @author stephenbolton
 */
public class PSChecksumFileInfo extends PSDefaultExtension
      implements
         IPSRequestPreProcessor,
         IPSHashFileInfoExtension
{


   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSChecksumFileInfo.class);

   /**
    * This method handles the pre-exit request.
    * 
    * @param params This method does not use any parameters.
    * 
    * @param request the request context for this request
    * 
    * @throws PSAuthorizationException Never.
    * @throws PSRequestValidationException Never.
    * @throws PSParameterMismatchException Never.
    * @throws PSExtensionProcessingException Never.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      String command =  request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (command == null || !command.equals("modify"))
           return;
      // make a copy to avoid ConcurrentModificationException
      Set<?> paramKeys = new HashSet<>(request.getParameters().keySet());
      Iterator<?> iter = paramKeys.iterator();
      String wifxFlag = request.getParameter("webimagefxupload");
      
      
      boolean isWifxUpload = wifxFlag != null
            && (wifxFlag.equalsIgnoreCase("true") || (wifxFlag
                  .equalsIgnoreCase("yes")));
      if (isWifxUpload)
         parseWifxMultiValueFields(request);
      while (iter.hasNext())
      {
         String paramName = (String) iter.next();
         
         if ( ! isHashParameter(paramName) ) continue;
         String baseName = prefixParameter(paramName);
         String clearParameter = clearParameter(baseName);
       
         // See if file was uploaded with this request from editor
         Object obj = request.getParameterObject(baseName+HASH_UPLOAD_PARAM_SUFFIX);
         if (obj==null) {
            //Maybe file uploaded to original file baseName, this will happen
            //through webservices etc.
            obj = request.getParameterObject(baseName);
            
            if(obj == null) {
               obj = request.getParameterObject(paramName);
            }
         }
         String hash = "";
         if (isClearSet(request, clearParameter))
         {
            // We need to handle clearing fields for word docs and images that
            // are entered via the Word accelerator and WebImageFx. These docs
            // do not actually get uploaded and therefore no PSPurgableTempfile
            // object will exist and we won't get to the normal
            // field clearing block (fix for bug #Rx-02-08-0054)
            clearParameters(request, paramName);
         }
         else if (obj instanceof PSPurgableTempFile)
         {
            PSPurgableTempFile temp = (PSPurgableTempFile) obj;
            if (temp.length() > 0) {
               doTempFile(request, temp, isWifxUpload, paramName);
               continue;
            }
            String sum = request.getParameter(paramName);
            if (isNotBlank(sum) && sum.length() < 50)
               doMetaExtraction(request, baseName, sum);
            else {
                clearParameters(request, paramName);
            }
         }
         else { 
            String sum = request.getParameter(paramName);
            if (isNotBlank(sum) && sum.length() < 50)
               doMetaExtraction(request, baseName, sum);
            else {
                clearParameters(request, paramName);
            }
         }

      }
   }

  
   /**
    * The Clear parameter for the hash field.
    * If this is set the hash of the file will be removed
    * but the file will still stay in the file repo.
    * @param paramName not null or empty.
    * @return not null.
    */
   protected String clearParameter(String paramName)
   {
      return paramName + CLEAR_BINARY_PARAM_SUFFIX;
   }

   /**
    * Checks if this parameter is a hash parameter.
    * The parameter must have a suffix of '_hash'.
    * @param paramName not null or empty.
    * @return not null or empty.
    */
   protected boolean isHashParameter(String paramName)
   {
      return endsWith(paramName, HASH_PARAM_SUFFIX);
   }
   
   /**
    * Checks if this parameter is a hash parameter.
    * The parameter must have a suffix of '_hash'.
    * @param paramName not null or empty.
    * @return not null or empty.
    */
   protected boolean isHashUploadParameter(String paramName)
   {
      return endsWith(paramName, HASH_UPLOAD_PARAM_SUFFIX);
   }
   
   /**
    * Gets the prefix for the hash parameter used
    * to figure out names of the other meta data fields.
    * @param paramName not null or empty.
    * @return not null or empty.
    */
   protected String prefixParameter(String paramName) {
      return removeEnd(paramName, HASH_PARAM_SUFFIX);
   }

   /**
    * Checks if the clear parameters is set.
    * @param request not null.
    * @param paramName the fully qualified clear parameter (includes _clear suffix).
    * @return true if its set.
    */
   protected boolean isClearSet(IPSRequestContext request, String paramName)
   {
      return paramName.endsWith(CLEAR_BINARY_PARAM_SUFFIX) && 
      request.getParameter(paramName) != null && request.getParameter(paramName).equals(CLEAR_PARAM_TRUE);
   }

   /**
    * Processes a temp file for extraction
    * @param request not null.
    * @param temp not null.
    * @param isWifxUpload if its web image fx.
    * @param paramName not null.
    */
   protected void doTempFile(IPSRequestContext request,
         PSPurgableTempFile temp, boolean isWifxUpload, String paramName)
   {
      logTempFile(temp);
      if (temp.length() > 0)
      {
         String sourceName = temp.getSourceFileName();
         String prefixParamName = paramName;
         if (paramName.endsWith(HASH_PARAM_SUFFIX))
         {
            prefixParamName = prefixParameter(paramName);
            log.debug("Hash Field new param name=" + prefixParamName);
            clearParameters(request, paramName);
         }
         // only set the parameters if an uploaded file is found
         // first, check to see if this file is being cleared
         // Only for old binary fields now
         boolean isClearSet = isClearSet(request, clearParameter(paramName));
         if (isClearSet)
         {
            clearParametersEvenThoughFileIsUploaded(request, prefixParamName);
         }
         else if (sourceName != null && sourceName.trim().length() > 0)
         {
            String sum = storeFile(temp);
            request.setParameter(prefixParamName + "_filename", sourceName);
            doMetaExtraction(request, prefixParamName, sum);
         }
      }
      else
      {
         log.debug("No file uploaded - no change");
      }
   }

   /**
    * Clear parameters.
    * @param request not null.
    * @param paramName not null.
    */
   protected void clearParameters(IPSRequestContext request, String paramName)
   {
        log.debug("Clear field name " + paramName);
        isTrue(isHashParameter(paramName));
        String paramPrefix = prefixParameter(paramName);
        
        log.debug("clearing fileinfo params for word document or webImageFX image");
        clearParameters(request, paramPrefix, "filename", "encoding", "type", "height", "width", "ext", "size", "hash","encoding");
     
   }

   /**
    * Stores the file in the db.
    * @param temp not null.
    * @return the hash of the file.
    */
   protected String storeFile(PSPurgableTempFile temp)
   {
      try
      {
         String sum = PSFileStorageServiceLocator.getFileStorageService().store((File) temp);

         return sum;
      }
      catch (Exception e)
      {
         log.error(e);
         throw new RuntimeException("Error storing file",e);
      }
   }

   /**
    * Gets the meta data from the hash field.
    * @param request not null.
    * @param paramNamePrefix not null ( this is minus the '_').
    * @param sum not null or empty.
    * @return not null.
    */
   protected IPSFileMeta doMetaExtraction(IPSRequestContext request, String paramNamePrefix, String sum)
   {
      IPSFileMeta meta = PSFileStorageServiceLocator.getFileStorageService().getMeta(sum);
      if (log.isDebugEnabled())
         log.debug("Got Meta " + meta);
      String mimeType = meta.getMimeType();
     
      log.debug("Image Width key = "
               + PSMeta.IMAGE_WIDTH.getName());
      
      if (meta.containsKey(PSMeta.IMAGE_WIDTH.getName()))
      {
         String width = meta.get(PSMeta.IMAGE_WIDTH.getName());
         log.debug("Image width is " + width);
         request.setParameter(paramNamePrefix + "_width", width);
      }
      if (meta.containsKey(PSMeta.IMAGE_LENGTH.getName()))
      {
         String height = meta.get(PSMeta.IMAGE_LENGTH.getName());
         log.debug("Image height is " + height);
         request.setParameter(paramNamePrefix + "_height", height);
      }
      
      request.setParameter(paramNamePrefix + "_size", meta.getLength());
      
      //  If _filename is set on content editor then use that one
      String filename=request.getParameter(paramNamePrefix+"_filename");
      
      
      // _ext is not a specific metadata property but is extracted from the filename.
      int pos = filename.lastIndexOf(".");
      if (pos >= 0)
      {
         request.setParameter(paramNamePrefix + "_ext", filename
               .substring(pos));
      }
      request.setParameter(paramNamePrefix + "_type", mimeType);
      request.setParameter(paramNamePrefix + HASH_PARAM_SUFFIX, sum);
      
      if (meta.getEncoding()!=null)
      {
         String encoding = meta.getEncoding();
         log.debug("File encoding is " + encoding);
         request.setParameter(paramNamePrefix + "_encoding", encoding);
      }
      return meta;
   }


   /**
    * logs file info.
    * @param temp not null.
    */
   protected void logTempFile(PSPurgableTempFile temp)
   {
      log.debug("File exists = " + temp.exists());
      log.debug("File length=" + temp.length());
      log.debug("File name=" + temp.getSourceFileName());
   }

   protected void clearParametersEvenThoughFileIsUploaded(IPSRequestContext request, String prefixParamName)
   {
      // the file is being cleared, clear the fileinfo params
      log.debug("clearing fileinfo params for " + prefixParamName);
      clearParameters(request, prefixParamName, 
            "filename", "ext", "type", "encoding", "size", "hash");
      request.setParameter(prefixParamName, EMPTY);
   }
   
   private void clearParameters(IPSRequestContext request, String paramName, String ... params) {
      for (String p : params) {
         request.setParameter(paramName + "_" + p, EMPTY);
      }
   }

  

   /**
    * Parses any delimited wifx multi value form fields that have been passed
    * in.
    * 
    * @param request the request being processed , assumed not <code>null</code>.
    * @throws PSExtensionProcessingException on any error
    */
   private void parseWifxMultiValueFields(IPSRequestContext request)
         throws PSExtensionProcessingException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      String contentTypeId = request.getParameter("sys_contenttypeid");
      String communityId = request.getParameter("sys_communityid");
      if (contentTypeId != null && contentTypeId.trim().length() > 0
            && communityId != null && communityId.trim().length() > 0)
      {
         try
         {
            PSItemDefinition item = mgr.getItemDef(Integer
                  .parseInt(contentTypeId), Integer.parseInt(communityId));
            PSServerItem sItem = new PSServerItem(item);
            Iterator<?> it = sItem.getAllFields();
            PSItemField field = null;
            while (it.hasNext())
            {
               field = (PSItemField) it.next();
               if (field.isMultiValue())
                  delimitedParameterToList(request, field.getName(), ";");
            }
         }
         catch (Exception e)
         {
            throw new PSExtensionProcessingException(
                  IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, e);
         }

      }

   }

   /**
    * Changes a delimited request parameter to a list of parameter values.
    * 
    * @param request the request being processed , assumed not <code>null</code>.
    * @param name the name of the parameter to be processed. Cannot be
    *            <code>null</code> or empty.
    * @param delimiter the delimiter to be used, cannot be <code>null</code>
    *            or empty.
    */
   private void delimitedParameterToList(IPSRequestContext request,
         String name, String delimiter)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");
      if (delimiter == null || delimiter.trim().length() == 0)
         throw new IllegalArgumentException(
               "Delimiter cannot be null or empty.");
      String param = request.getParameter(name);
      if (param == null || param.trim().length() == 0)
         return;
      StringTokenizer st = new StringTokenizer(param, delimiter);
      Object obj = null;
      if (st.countTokens() <= 1)
      {
         obj = st.nextToken();
      }
      else
      {
         final List<String> list = new ArrayList<>();
         while (st.hasMoreTokens())
            list.add(st.nextToken());
         obj = list;
      }
      request.setParameter(name, obj);
   }

   /**
    * Constant that will map to <code>null</code>; used to clear the file
    * info parameters.
    */
   private static final String EMPTY = "";

   /**
    * Constant for value of the binary clear parameter if it is to indicate that
    * the field should be cleared.  Never <code>null</code> or empty.
    */
   private static final String CLEAR_PARAM_TRUE = "yes";

   /**
    * Constant string that will be appended to the binary field name to form
    * the clear parameter name
    */
   private static final String CLEAR_BINARY_PARAM_SUFFIX = "_clear";
   
   /**
    * See {@link #uploadHashParameter(String)}
    */
   private static final String HASH_UPLOAD_PARAM_SUFFIX = "_hupload";

}
