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

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.lucene.textconverter.IPSLuceneTextConverter;
import com.percussion.search.lucene.textconverter.PSLuceneTextConverterFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to convert data from multiple file formats to either plain
 * text.
 */
public class PSContentConverter
{
   /**
    * Construct a converter with the specified options.
    * 
    * @param outputFormat The output format used to return the converted data,
    * either {@link #FORMAT_TEXT} or {@link #FORMAT_HTML}.
    * 
    * @param useLinefeed <code>true</code> to convert carriage returns to
    * linefeeds, <code>false</code> to use carriage returns for line endings.
    * Ignored if <code>outputFormat</code> is <code>TYPE_HTML</code>.
    * 
    * @param htmlTemplate The template file to use to generate HTML results, may
    * be <code>null</code> or empty to use the default template. To use a
    * conversion template other than the default, it must be placed on the
    * server hosting the search engine (which may not be hosting the Rx server).
    * This parameter value must specify a valid file path to that template file,
    * and it must be relative to the value specified for INSTALL_DIR, which is
    * the value provided when running the installer (defaults to
    * rxroot/sys_search/rware).
    * 
    * @param outputEncoding Specifies encoding to use for the converted output.
    * If not specified, then WINDOWS-1252 is used for TEXT, UTF-8 for HTML.
    * Specifying an unsupported encoding will produce an error . Note that this
    * may result in loss of data if the source file uses characters outside of
    * that character set. Currently only the following encodings may be
    * specified:
    * <ul>
    * <li>SHIFT_JIS (Japanese)</li>
    * <li>EUC_KR (Korean)</li>
    * <li>GB2312 (Simple Chinese)</li>
    * <li>BIG5 (Traditional Chinese)</li>
    * </ul>
    * May be <code>null</code> or empty. Comparison is case-insensitive.
    * 
    * @param pdfConversion Indicates how the conversion should be performed if
    * the file type is determined to be a PDF (it is ignored otherwise). Must be
    * one of the <code>PDF_XXX</code> values.
    * @throws PSContentConversionException this contructor has been dropped use
    * {@link PSContentConverter(String)}.
    * 
    * @deprecated This constructor has been deprecated. Use
    * {@link PSContentConverter(String)}.
    */
   public PSContentConverter(int outputFormat, boolean useLinefeed,
         String htmlTemplate, String outputEncoding, int pdfConversion)
      throws PSContentConversionException
   {
      throw new PSContentConversionException(
            IPSContentErrors.UNSUPPORTED_CONVERT_CONSTRUCTOR);
   }
   
   /**
    * @param mimetype The mimetype of the input stream must not be
    * <code>null</code> or empty.
    */
   public PSContentConverter(String mimetype) throws PSContentConversionException
   {
      if (StringUtils.isEmpty(mimetype))
         throw new IllegalArgumentException("mimetype must not be null.");
      m_mimetype = mimetype;
      IPSLuceneTextConverter converter = PSLuceneTextConverterFactory
            .getInstance().getLuceneTextConverter(m_mimetype);
      if (converter == null)
      {
         throw new PSContentConversionException(
               IPSContentErrors.UNSUPPORTED_MIMETYPE);
      }
      m_converter = converter;
   }
   
   /**
    * Performs the conversion using the options specified during construction.
    * 
    * @param data The data to convert, may not be <code>null</code>. This
    * method assumes ownership of the supplied stream.
    * 
    * @return A results object that may be used to retrieve the converted data,
    * never <code>null</code>.
    * 
    * @throws PSContentConversionException if an unsupported file type is
    * supplied, or if there are any errors.
    * @deprecated This method has been deprecated use extractText method. Throws
    * PSContentConversionException if any if called.
    */
   public PSConversionResults convert(InputStream data)
      throws PSContentConversionException
   {
      throw new PSContentConversionException(
            IPSContentErrors.UNSUPPORTED_CONVERT_METHOD);
   }
   
   /**
    * Extracts the text from the supplied input stream.
    * 
    * @param data The data from which the text needs to be extracted. Must not
    * be <code>null</code>. This method assumes ownership of the supplied
    * stream.
    * @return Extracted text may be empty but never <code>null</code>.
    * 
    * @throws PSContentConversionException in case of an error converting to
    * text.
    */
   public String extractText(InputStream data) throws PSContentConversionException
   {
      if (data == null)
         throw new IllegalArgumentException("data must not be null");

      String extractedText = "";
      try
      {
         extractedText = m_converter.getConvertedText(data, m_mimetype);
      }
      catch (PSExtensionProcessingException e)
      {
         Object[] args = { e.getClass().getName(), e.getLocalizedMessage() };
         throw new PSContentConversionException(
               IPSContentErrors.CONTENT_CONVERSION_UNEXPECTED_ERROR, args);
      }
      finally
      {
         if (data != null)
         {
            try
            {
               data.close();
            }
            catch (IOException e)
            {
               // Ignore this
            }
         }
      }
      return StringUtils.defaultString(extractedText);
   }
  
   /**
    * The mimetype of the input stream  
    */
   private String m_mimetype;

   /**
    * The converter for the given mimetype. Initialized in ctor never
    * <code>null</code> after that.
    */
   private IPSLuceneTextConverter m_converter;

   /**
    * Reference to log for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSContentConverter.class);
}
