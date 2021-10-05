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

package com.percussion.debug;

import com.percussion.server.PSRequest;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Used to generate trace messages for the File Information trace messages type (0x0004) if a post includes one or more files.  Includes the name, mime type and length for each, and if each is treated as XML or a single value.
 */
public class PSTraceFileInfo extends PSTraceMessage
{

   /**
    * Constructor for this class
    *
    * @param typeFlag the type of trace message this object will generate
    * @roseuid 39FDD08D0138
    */
   public PSTraceFileInfo(int typeFlag)
   {
      super(typeFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      return ms_bundle.getString("traceFileInfo_dispname");
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.  For all files included in a POST, prints out
    * the Name, Mime type, and length of each file, or, if the file is treated as XML.
    * @param source a PSRequest object containing the information required for the
    * trace message.  Call getParameters on it and check for a parameter with a value
    * that is an instance of a File.  Also, if getInputDocument does not return a
    * <code>null</code>, then its a file treated as XML.
    * @return the message body
    * @roseuid 39FEE2F2030D
    */
   protected String getMessageBody(java.lang.Object source)
   {

      StringBuilder buf = new StringBuilder();
      PSRequest request = (PSRequest)source;

      Map<String,Object> params = request.getParameters();

      if (params != null)
      {
         // walk params and build file list to get count
         ArrayList fileList = new ArrayList();
         Iterator entries = params.entrySet().iterator();
         File val;
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            // skip over fileList - they will be handled by the PSTraceFileInfo type
            if (entry.getValue() instanceof PSPurgableTempFile)
            {
               val = (File)entry.getValue();
               fileList.add(val);
            }

         }

         int size = fileList.size();
         if (size == 0)
            buf.append(ms_bundle.getString("traceFileInfo_nofiles"));
         else if (size == 1)
            buf.append(ms_bundle.getString("traceFileInfo_onefile"));
         else
         {
            Object[] args = {Integer.toString(size)};
            buf.append(MessageFormat.format(
               ms_bundle.getString("traceFileInfo_files"), args));
         }
         buf.append(NEW_LINE);

         // walk it again and write out info for each file
         Object[] args = {null};
         Iterator files = fileList.iterator();
         while (files.hasNext())
         {
            PSPurgableTempFile file = (PSPurgableTempFile)files.next();

            // name
            buf.append(file.getSourceFileName());
            buf.append(": ");

            // character set encoding
            if (file.getCharacterSetEncoding() != null)
            {
               args[0] = file.getCharacterSetEncoding();
               buf.append(MessageFormat.format(
                  ms_bundle.getString("traceFileInfo_charencoding"), args));
               buf.append(", ");
            }

            // length
            args[0] = Long.toString(file.length());
            buf.append(MessageFormat.format(
               ms_bundle.getString("traceFileInfo_bytes"), args));
            buf.append(", ");

            // content type
            args[0] = file.getSourceContentType();
            buf.append(MessageFormat.format(
               ms_bundle.getString("traceFileInfo_contenttype"), args));
         }

      }

      // now check for XML file
      Document doc = request.getInputDocument();
      if (doc != null)
      {
         buf.append(NEW_LINE);
         buf.append("XML file used as input document - text follows");
         String msg = "";
         try
         {
            StringWriter w = new StringWriter();
            PSXmlDocumentBuilder.write(doc, w);
            msg = w.toString();
         }
         catch (IOException ioe)
         {
            msg = "Exception occurred while trying to print doc: "
                  + ioe.getLocalizedMessage();
         }
         buf.append(NEW_LINE);
         buf.append(msg);
      }
      return new String(buf);

   }
}
