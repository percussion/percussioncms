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

package com.percussion.extensions.general;

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
import com.percussion.util.PSBaseHttpUtils;
import com.percussion.util.PSHttpUtils;
import com.percussion.util.PSPurgableTempFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class is a Rhythmyx pre-exit which examines the incoming HTML
 * parameters for attached files.  When an attached file is found, the file
 * name, MIME Type and character encoding (as reported by the web browser) are
 * added as additional HTML parameters.  When the clear binary parameter (named
 * by adding "_clear" to the end of the parameter that contains the file) is
 * true, the additional HTML parameters are added as null values. This exit
 * also handles the parsing of delimited multi value fields that are passed
 * in by the webImageFx component.
 * <p>
 * The name of the each new parameter is formed by adding a suffix onto the
 * end of the HTML parameter that contains the attached file.
 * <table>
 * <tr><th>Suffix</th><th>Meaning</th></tr>
 * <tr>
 *   <td valign="top">_fullFilepath</td>
 *   <td valign="top">the original file path and name of the uploaded file</td>
 * </tr>
 * <tr>
 *   <td>_filename</td><td>the original file name of the uploaded file</td>
 * </tr>
 * <tr>
 *   <td valign="top">_ext</td>
 *   <td>the file extension, defined as all characters in the filename after
 *       the last dot ("<code>.</code>")</td>
 * </tr>
 * <tr>
 *   <td>_type</td>
 *   <td>the MIME Type and Subtype</td>
 * </tr>
 * <tr>
 *   <td>_encoding</td>
 *   <td>the character encoding.</td>
 * </tr>
 * <tr>
 *   <td>_size</td>
 *   <td>the length of the file, in bytes</td>
 * </tr>
 * </table>
 * It is important to remember that these values are reported by the Web
 * browser, and some web browsers may not report some or all of these values.
 * If the value is not present (or the file length is 0), the corresponding
 * parameter will not be added to the server's parameter map.
 */
public class PSFileInfo extends PSDefaultExtension implements
   IPSRequestPreProcessor
{

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
    **/
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException,
                PSRequestValidationException,
                PSParameterMismatchException,
                PSExtensionProcessingException
   {
      // make a copy to avoid ConcurrentModificationException
      Set paramKeys = new HashSet(request.getParameters().keySet());
      Iterator iter = paramKeys.iterator();
      String wifxFlag = request.getParameter("webimagefxupload");
      boolean isWifxUpload =  wifxFlag!= null &&
        (wifxFlag.equalsIgnoreCase("true") ||
        (wifxFlag.equalsIgnoreCase("yes")));
      if(isWifxUpload)
         parseWifxMultiValueFields(request);
      while(iter.hasNext()) {
         String paramName = (String)iter.next();
         Object obj = request.getParameterObject(paramName);


         if(obj instanceof PSPurgableTempFile) {
            PSPurgableTempFile temp = (PSPurgableTempFile) obj;
            String sourceName = temp.getSourceFileName();

            // first, check to see if this file is being cleared
            // (fix for bug #Rx-01-10-0113)
            String clearValue = request.getParameter
                  (paramName + CLEAR_BINARY_PARAM_SUFFIX);
            if (clearValue != null &&
                  clearValue.equals(CLEAR_PARAM_TRUE))
            {
               // the file is being cleared, clear the fileinfo params
               request.printTraceMessage(
                  "clearing fileinfo params for " +paramName);
               request.setParameter(paramName + "_fullFilepath", EMPTY );
               request.setParameter(paramName + "_filename", EMPTY );
               request.setParameter(paramName + "_ext", EMPTY );
               request.setParameter(paramName + "_type", EMPTY );
               request.setParameter(paramName + "_encoding", EMPTY );
               request.setParameter(paramName + "_size", EMPTY );
            }
            // only set the parameters if an uploaded file is found
            // fix for bug #Rx-01-09-0013
            else if (sourceName != null && sourceName.trim().length() > 0)
            {
               request.setParameter(paramName + "_fullFilepath", sourceName );

               String pathSeparator = isWifxUpload ?
                  "\\" :
                  PSHttpUtils.getRequestorDirectorySeperator(request);
               String filename = getFilename(sourceName, pathSeparator);

                //For debugging only
               request.printTraceMessage(
                  "PSFileInfo.preProcessRequest filename==" + filename);
               request.printTraceMessage(
                  "PSFileInfo.preProcessRequest sourceName==" + sourceName);
               request.printTraceMessage(
                  "PSFileInfo.preProcessRequest pathSeparator==" +
                  pathSeparator);

               request.setParameter(paramName + "_filename", filename );

               int pos = sourceName.lastIndexOf(".");
               if ( pos >= 0 )
               {
                  request.setParameter(paramName + "_ext",
                        sourceName.substring( pos ));
               }

               // the name of the mimetype parameter must be kept in synch with
               // PSBinaryCommandHandler.createBinaryResources()
               String MIMEType = determineMimeType(
                  temp.getSourceContentType(),
                  filename);
               request.setParameter(paramName + "_type", MIMEType);

               String encoding = temp.getCharacterSetEncoding();
               request.setParameter(paramName + "_encoding", encoding);

               long filesize = temp.length();
               if (filesize > 0)
               {
                  request.setParameter(paramName + "_size",
                        String.valueOf(filesize));
               }

               request.printTraceMessage("Found file: " + paramName +
                  "\n   original file name: " + sourceName +
                  "\n   MIME type: " + MIMEType +
                  "\n   encoding: " + encoding);
            }
         // We need to handle clearing fields for word docs and images that
         // are entered via the Word accelerator and WebImageFx. These docs
         // do not actually get uploaded and therefore no PSPurgableTempfile
         // object will exist and we won't get to the normal
         // field clearing block (fix for bug #Rx-02-08-0054)
         }else if(paramName.endsWith(CLEAR_BINARY_PARAM_SUFFIX) &&
                 request.getParameter(paramName) != null &&
                 request.getParameter(paramName).equals(CLEAR_PARAM_TRUE)){

                 String paramPrefix = paramName.substring(0,
                    paramName.length() - CLEAR_BINARY_PARAM_SUFFIX.length());
                 String paramFilename = request.getParameter(paramPrefix+"_filename");

                   if(paramFilename != null && paramFilename.length()>0)
                   {
                     request.printTraceMessage(
                       "clearing fileinfo params for word document or webImageFX image");

                     request.setParameter(paramPrefix+"_filename",EMPTY);
                     request.setParameter(paramPrefix+"_encoding",EMPTY);
                     request.setParameter(paramPrefix+"_type",EMPTY);
                     request.setParameter(paramPrefix+"_height",EMPTY);
                     request.setParameter(paramPrefix+"_width",EMPTY);
                     request.setParameter(paramPrefix+"_ext",EMPTY);
                     request.setParameter(paramPrefix+"_size",EMPTY);
                   }

             }
      }
   }

   /**
    * Returns the filename portion of the provided fully qualified path.
    *
    * @param fullPathname The full path of the file, assumed not
    * <code>null</code> or empty.
    *
    * @param pathSep The path separator to use, assumed not <code>null
    * </code>.
    *
    * @return The filename portion of the full path, based on the pathSep, or
    * the fullPathname if the provided pathSep is not found in the provided
    * fullPathname.  Never <code>null</code>, may be emtpy if the fullPathname
    * ends in the pathSep.
    */
   private static String getFilename(String fullPathname, String pathSep)
   {
      String fileName = "";

      // add 1 to the index so that we do not include the separator in the
      // filename string
      int startOfFilename = fullPathname.lastIndexOf(pathSep) + 1;
      if (startOfFilename < fullPathname.length())
         fileName = fullPathname.substring(startOfFilename);

      return fileName;
   }
   
   /**
    * This method tries to make a more intelligent decision to determine
    * the appropriate Mime type by looking at both the type guess made
    * by the browser and the file extension. Some browser do not always 
    * correctly determine an uploaded files Mime type for HTML files. If the
    * type guessed by the browser is text or octet-stream and the file extension
    * is one of the well known extensions then we use that extensions 
    * Mime type.
    * 
    * @param type the Mime type guessed by the browser, cannot be <code>
    * null</code> or empty.
    * @param filename the filename for the uploaded file, cannot be
    * <code>null</code> or empty.
    * @return the Mime type, never <code>null</code> or empty.
    */
   private static String determineMimeType(String type, String filename)
   {
      if(type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("The type cannot be null or empty.");
      if(filename == null || filename.trim().length() == 0)
         throw new IllegalArgumentException("The filename cannot be null or empty.");
                  
      int pos = filename.lastIndexOf('.');
      String ext = pos == -1 ? "" : filename.substring(pos + 1).toLowerCase();
      
      if(ms_wellKnownExts.containsKey(ext)
         && (type.toLowerCase().equals("application/octet-stream")
            || type.toLowerCase().equals("text/plain")))
       {
           return ms_wellKnownExts.get(ext);  
       }
      return type;
   }

   /**
    * Parses any delimited wifx multi value form fields that have been
    * passed in.
    * @param request the request being processed , assumed
    * not <code>null</code>.
    * @throws PSExtensionProcessingException on any error
    */
   private void parseWifxMultiValueFields(IPSRequestContext request)
      throws PSExtensionProcessingException
   {
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      String contentTypeId = request.getParameter("sys_contenttypeid");
      String communityId = request.getParameter("sys_communityid");
      if(contentTypeId != null && contentTypeId.trim().length() > 0 &&
         communityId != null && communityId.trim().length() > 0)
      {
         try
         {
           PSItemDefinition item =
              mgr.getItemDef(Integer.parseInt(contentTypeId),
                 Integer.parseInt(communityId));
           PSServerItem sItem = new PSServerItem(item);
           Iterator it = sItem.getAllFields();
           PSItemField field = null;
           while(it.hasNext())
           {
              field = (PSItemField)it.next();
              if(field.isMultiValue())
              delimitedParameterToList(request, field.getName(), ";");
           }
         }
         catch (Exception e)
         {
           Object args = new Object[]{"sys_FileInfo", e.getMessage()};
           throw new PSExtensionProcessingException(
              IPSExtensionErrors.EXT_PROCESSOR_EXCEPTION, args);
         }

      }

   }

   /**
    * Changes a delimited request parameter to a list of
    * parameter values.
    *
    * @param request the request being processed , assumed
    * not <code>null</code>.
    * @param name the name of the parameter to be processed.
    * Cannot be <code>null</code> or empty.
    * @param delimiter the delimiter to be used, cannot be <code>null</code>
    * or empty.
    */
   private void delimitedParameterToList(
      IPSRequestContext request, String name, String delimiter)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");
      if(delimiter == null || delimiter.trim().length() == 0)
         throw new IllegalArgumentException(
            "Delimiter cannot be null or empty.");
      String param = request.getParameter(name);
      if(param == null || param.trim().length() == 0)
         return;
      StringTokenizer st = new StringTokenizer(param, delimiter);
      Object obj = null;
      if(st.countTokens() <= 1)
      {
         obj = st.nextToken();
      }
      else
      {
         final List<String> list = new ArrayList<String>();
         while(st.hasMoreTokens())
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
    * These are a map of well know file extensions that the browser should not guess
    * as either text or octet-stream Mime types, but some browser like IE can make
    * a mistake with these. This list should be expanded as we find other problem
    * extension types.
    */
   private static final Map<String, String> ms_wellKnownExts =
         new HashMap<String, String>(2);   
   static
   {
      ms_wellKnownExts.put("htm", "text/html");
      ms_wellKnownExts.put("html", "text/html");     
   }
}
