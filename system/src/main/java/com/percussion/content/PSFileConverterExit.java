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
package com.percussion.content;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This extension enables the conversion of data from a number of file formats
 * to HTML or text.
 * @deprecated Deprecated use PSTextExtractorExit instead.
 */
public class PSFileConverterExit extends PSDefaultExtension 
   implements IPSItemInputTransformer
{
   // see interface
   @Override
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      super.init(extensionDef, file);
      ms_fullExtensionName = extensionDef.getRef().toString();
   }
   
   /**
    * Converts the data specified by the params to either text or html.
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
    * <td>Contains a reference to the file containing the data to convert. If
    * the value of this parameter is a File object, it is used directly,
    * otherwise toString() is called on the value of this parameter and the
    * result is assumed to be the data to convert as base64 encoded text. </td>
    * <td>yes</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The name of the request parameter in which the converted text or html
    * output is to be returned. The returned value will be stored as a
    * <code>String</code>, never <code>null</code>, may be empty. </td>
    * <td>yes</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>The name of the request parameter in which to return the file type
    * used for the conversion. If not supplied, the type is not returned. Value
    * returned is a text description of the file type returned by the conversion
    * implementaion as a <code>String</code>, never <code>null</code> or
    * empty. </td>
    * <td>no</td>
    * </tr>
    * <tr>
    * <td>3</td>
    * <td>The name of the request parameter in which any error messages are to
    * be stored as text. If not supplied, the extension will throw exceptions
    * for any errors encountered. If supplied, any error encountered will be
    * written to this parameter, and the exit will silently return. In this case
    * the specified Output parameter value will be unmodified (unless that name
    * is passed as to this parameter as well), and the file type parameter may
    * or may not be modified. </td>
    * <td>no</td>
    * </tr>
    * 
    * <tr>
    * <td>4</td>
    * <td>The output format. Allowed values are TEXT and HTML,
    * case-insensitive. If not supplied, defaults to TEXT. </td>
    * <td>no</td>
    * </tr>
    * <tr>
    * <td>5</td>
    * <td>Flag to indicate if line endings should be returned as carriage
    * returns or converted to linefeeds. Supply Y (case-insensitive) to convert
    * to linefeeds. Any other value (or if ommitted) indicates that carriage
    * returns are to be used. Ignored if output format is HTML. </td>
    * <td>no</td>
    * </tr>
    * <tr>
    * <td>6</td>
    * <td>Optional override for template to use when converting to HTML.
    * Ignored if output format is to be TEXT. To use a conversion template other
    * than the default, it must be placed on the server hosting the search
    * engine (which may not be hosting the Rx server). This parameter value must
    * specify a valid file path to that template file, and it must be relative
    * to the value specified for INSTALL_DIR, which is the value provided when
    * running the installer (defaults to rxroot/sys_search/rware). toString() is
    * called on the parameter value to obtain the path. If not supplied, the
    * system default template is used (rx/resource/sys_html_rx.tpt). </td>
    * <td>no</td>
    * </tr>
    * <tr>
    * <td>7</td>
    * <td>Specifies encoding to use for output character set. If not specified,
    * then WINDOWS-1252 is used for TEXT, UTF-8 for HTML. Specifying an
    * unsupported encoding will produce an error. Note that this may result in
    * loss of data if the source file uses characters outside of that character
    * set. Currently the only following encodings may be specified (case
    * insensitive):
    * <ul>
    * <li>SHIFT_JIS (Japanese)</li>
    * <li>EUC_KR (Korean)</li>
    * <li>GB2312 (Simple Chinese)</li>
    * <li>BIG5 (Traditional Chinese)</li>
    * </ul>
    * </td>
    * <td>no</td>
    * </tr>
    * <tr>
    * <td>8</td>
    * <td>A value to indicate how the conversion should be performed if the
    * file type is determined to be a PDF (it is ignored otherwise). There are
    * two allowed values, case-insensitive:
    * <ul>
    * <li>SINGLE - does not handle multi-byte characters in the source data,
    * creates output using system default character set, does handle multiple
    * columns in the source data. Any output format specified is ignored and
    * TEXT is used. </li>
    * <li>MULTI - does handle multi-byte characters in the source data, creates
    * output using the specified encoding (if supplied), does not handle multple
    * columns in the source data. Output format may be specified as TEXT or
    * HTML.</li>
    * </ul>
    * If not supplied, defaults to SINGLE. </td>
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
    * 
    * @deprecated this exit has been deprecated and throws
    * PSContentConversionException(IPSContentErrors.UNSUPPORTED_EXTRACTION_EXIT)
    * when used.
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request) 
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String msg = ms_fullExtensionName + " exit has been dropped,"
            + " update your content editor applications to use sys_textExtractor exit.";
      request.printTraceMessage(msg);
      ms_log.info(msg);
      throw new PSExtensionProcessingException(
            new PSContentConversionException(
                  IPSContentErrors.UNSUPPORTED_EXTRACTION_EXIT));
   }

   /**
    * The fully qualified name of this extension. Intialized in the 
    * {@link #init(IPSExtensionDef, File)} method, never <code>null</code>, 
    * empty, or modified after that.
    */
   static private String ms_fullExtensionName = "";
   
   /**
    * Reference to log for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSFileConverterExit.class);
}
