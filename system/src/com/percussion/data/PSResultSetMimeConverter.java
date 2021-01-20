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

package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndColumn;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSHttpErrors;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSResponse;
import com.percussion.util.*;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Stack;


/**
 * The PSResultSetMimeConverter class implements the IPSResultSetConverter
 * interface, providing conversion to the specified MIME output type.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultSetMimeConverter implements IPSResultSetConverter
{
   /**
    * Construct a ResultSet to MIME output type converter.
    *
    * @param      app   the application containing the data set
    *
    * @param      def   the data set definition
    *
    * @exception   PSIllegalArgumentException
    *                                    if request link generation is
    *                                    desired but the target data set
    *                                    cannot be found
    */
   public PSResultSetMimeConverter(PSApplicationHandler app, PSDataSet def)
         throws PSNotFoundException, PSIllegalArgumentException
   {
      super();

      /* there must be only one pipe with one column containing
       * the mime data
       */

      /* Hang on to the requestor for content information */
      m_requestor = def.getRequestor();

      PSPipe pipe = def.getPipe();
      if (pipe == null)
      {
         Object[] args = { "0" };
         throw new PSIllegalArgumentException(
               IPSDataErrors.MIME_CONV_ONE_PIPE_REQD, args);
      }
      if (!(pipe instanceof
            com.percussion.design.objectstore.PSQueryPipe))
      {
         throw new PSIllegalArgumentException(
               IPSDataErrors.MIME_CONV_QUERY_PIPE_REQD);
      }

      PSDataMapper maps = pipe.getDataMapper();
      if (maps.size() != 1)
      {
         Object[] args = { String.valueOf(maps.size()) };
         throw new PSIllegalArgumentException(
               IPSDataErrors.MIME_CONV_ONE_MAPPING_REQD, args);
      }

      PSDataMapping map = (PSDataMapping) maps.get(0);
      String[] beCols = map.getBackEndMapping().getColumnsForSelect();
      if (beCols.length != 1)
      {
         Object[] args = { String.valueOf(beCols.length) };
         throw new PSIllegalArgumentException(
               IPSDataErrors.MIME_CONV_ONE_COLUMN_REQD, args);
      }
   }


   /* ********** IPSResultSetConverter Interface Implementation ********** */

   /**
    * See {@link IPSResultSetConverter#convert(PSExecutionData,
    * IPSResultSetDataFilter) base class} for full details. More specifically,
    * this class performs the following steps during conversion:
    * <ol>
    * <li>verify reqUrl is supported</li>
    * <li>create the XML document</li>
    * <li>initialize the output with the appropriate header info
    * (content type, etc.)</li>
    * <li>set the XML data on the response</li>
    * </ol>
    *
    * @param filter This parameter is ignored by this converter as this
    *    converter only processes a single row. In the future, we could be
    *    smarter about this.
    */
   public void convert(PSExecutionData data, IPSResultSetDataFilter filter)
         throws PSConversionException, PSUnsupportedConversionException
   {
      processMimeContent(data, true);
   }

   /**
    * Retrieve a column mime-data and optionally its mime type from the back
    * end database.  Sets the data on the  response if indicated.
    *
    * @param   data  The execution data for this query. Never <code>null</code>.
    *
    * @param   setResponse  Should we set the result on response? <code>true</code>
    *    indicates to set the result on the response, while <code>false</code>
    *    will cause the temporary file and mime type information to be stored in
    *    the execution data to be retrieved by the caller.
    *
    * @throws  PSConversionException  if the mime content fails to be prepared
    *    for return to the user (or internal caller).
    *
    * @throws PSUnsupportedConversionException  if any lower level call throws
    *    this exception
    *
    * @throws IllegalArgumentException If the execution data is <code>null</code>.
    */
   public void processMimeContent(PSExecutionData data, boolean setResponse)
         throws PSConversionException, PSUnsupportedConversionException
   {
      if (data == null)
         throw new IllegalArgumentException("Execution data must be supplied.");

      /* if there's more than one result set on the stack, we're
       * in trouble!!! we must have missed a join.
       */
      Stack stack = data.getResultSetStack();
      if (stack.size() > 1)
         throw new PSConversionException(
               IPSDataErrors.CANNOT_CONVERT_MULTIPLE_RESULT_SETS,
               new Integer(stack.size()));
      else if (stack.size() == 0)
         throw new PSConversionException(
               IPSDataErrors.NO_DATA_FOR_CONVERSION);

      int colsToAccept = 1;
      boolean mimeFromColumn = false;

      PSRequest request = data.getRequest();

      IPSReplacementValue val = m_requestor.getOutputMimeType();
      if (val != null)
      {
         if (val instanceof PSBackEndColumn)
         {
            mimeFromColumn = true;
            colsToAccept++;
         }
      }


      /* build the response object */
      PSResponse resp = null;
      if (setResponse)
      {
         resp = request.getResponse();
         if (resp == null)
         {
            /* this should never happen! */
            throw new PSConversionException(IPSDataErrors.NO_RESPONSE_OBJECT);
         }
      }

      byte[] rawData = null;

      ResultSet rs = null;
      try
      {
         rs = (ResultSet) data.getResultSetStack().pop();

         ResultSetMetaData meta = rs.getMetaData();

         if (meta.getColumnCount() > colsToAccept)
         {
            /* there can be only one column */
            Object[] args = { String.valueOf(meta.getColumnCount()) };
            throw new PSConversionException(
                  IPSDataErrors.MIME_CONV_MULTICOL_RESULT, args);
         }

         int rowsSelected = 0;
         int mimeDataColType = meta.getColumnType(1);

         while (rs.next())
         {
            rowsSelected++;   /* increment the rows selected counter */
            if (rowsSelected > 1)
            {
               /* there can be only one row of data at this time! */
               throw new PSConversionException(
                     IPSDataErrors.MIME_CONV_MULTIROW_RESULT);
            }


            /**
             * *** NOTE *** NOTE *** NOTE *** NOTE *** NOTE *** NOTE ***
             *
             * This rather ugly piece of code is required for various
             * kludgy reasons.
             *
             *      1. jdbc clears the stream after a call to next or getXXX
             *      2. rs.getBytes does character translation?! which breaks
             *         binary data
             *      3.   the length of the stream is not guaranteed -- that is,
             *         a call to available is not necessarily the amount of
             *         data pending!
             *      4. when writing the data, HTTP required Content-Length
             *         be set before sending any data
             *
             * So instead, I'm reading the data a chunk at a time into
             * an output stream. The last chunk of data read will be less
             * than our stream size, which is our end condition.
             *
             * To avoid allocating huge chunks of memory, we'll write the
             * retrieved data to disk and serve the file up.
             */
            byte[] aData = new byte[2048];
            InputStream rowData = null;

            Reader reader = null;

            switch (mimeDataColType)
            {
               case Types.CHAR:
               case Types.VARCHAR:
               case Types.LONGVARCHAR:
                  reader = rs.getCharacterStream(1);
                  break;

               case Types.CLOB:
                  Clob c = rs.getClob(1);
                  if (c != null)
                     reader = c.getCharacterStream();
                  break;

               default:
                  rowData = rs.getBinaryStream(1);
            }

            /*
             * rs.getCharacterStream() and rs.getBinaryStream() may return null
             * if the column's value is a SQL NULL.
             * This is not an error, and we must handle this case
             * (fixes bug #Rx-01-09-0010)
             *
             * However, this method is responsible for generating the response
             * to a sys_command=binary request, so we need to send a empty
             * response (or an error) if setResponse is true.
             */

            if (rowData != null || reader != null)
            {
               String encoding = m_requestor.getCharacterEncoding();

               // we're going
               PSPurgableTempFile f = 
                  new PSPurgableTempFile("psm", "tmp", null);
               FileOutputStream fout = null;
               FileInputStream fin = null;

               try
               {
                  fout = new FileOutputStream(f);

                  if (rowData != null)
                  {
                     for (int iRead = aData.length; iRead <= aData.length;)
                     {
                        iRead = rowData.read(aData);
                        if (iRead == -1)
                           break;
                        fout.write(aData, 0, iRead);
                     }
                     fout.flush();
                     fout.close();
                  }
                  else  // we have a reader
                  {
                     OutputStreamWriter osw = null;
                     if (encoding != null && encoding.length() > 0)
                        osw = new OutputStreamWriter(fout,
                              PSCharSets.getJavaName(encoding));
                     else
                        osw = new OutputStreamWriter(fout);

                     BufferedWriter writer = new BufferedWriter(osw);
                     IOTools.writeStream(reader, writer, 2048);
                     writer.close();
                  }
                  fout = null;

                  String mimeType = null;
                  if (mimeFromColumn)
                  {
                     /* Use getString as some DBMS' return null when asked
                        for getObject() for a string! */
                     mimeType = rs.getString(2);
                  }
                  else
                  {
                     mimeType = PSResultSetXmlConverter.getMimeTypeForRequestor(
                           m_requestor, request.getRequestPageExtension(), data);
                  }

                  if (!setResponse)
                  {
                     // add to the exec data and return
                     data.setTempFileResource(
                           new PSMimeContentResult(f, mimeType));
                  }
                  else
                  {
                     // the temp file will be delete after closing the stream
                     fin = new PSPurgableFileInputStream(f);

                     String contentHeader = PSBaseHttpUtils.
                           constructContentTypeHeader(mimeType, encoding);
                     resp.setContent(fin, f.length(), contentHeader);
                     fin = null;
                  }
               }
               finally
               {
                  if (fout != null)
                  {
                     try
                     {
                        fout.close();
                     }
                     catch (Exception e)
                     {
                        /* ignore, we're done */
                     }
                  }

                  if (setResponse)
                  {
                     if (fin != null)
                     {
                        try
                        {
                           fin.close();
                        }
                        catch (Exception e)
                        {
                           /* ignore, we're done */
                        }
                        // done with the temp file, delete it now
                        f.release();
                     }

                  }
               }
            }
            else
            {
               if (setResponse)
               {
                  // if the binary column is null, return an empty response
                  // (this is not an error -- fixed bug RX-01-10-0110)
                  resp.setStatus(IPSHttpErrors.HTTP_NO_CONTENT);
               }
            }
         }

         /* update the statistics with the number of rows selected */
         PSRequestStatistics stats = null;
         if (request != null)
         {
            stats = request.getStatistics();
            if (stats != null)
               stats.incrementRowsSelected(rowsSelected);
         }
      }
      catch (Throwable t)
      {
         Object[] args = { request.getUserSessionId(), t.toString() };
         throw new PSConversionException(
               IPSDataErrors.XML_CONV_EXCEPTION, args);
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (SQLException e)
            {
               /* don't need this, ignore the error */
            }
         }
      }
   }

   /**
    * What is the default MIME type for this converter?
    *
    * @return               the default MIME type
    */
   public String getDefaultMimeType()
   {
      return null;
   }

   /**
    * Evaluate any result page conditionals to determine the index of
    * the result page to use.
    *
    * @param   data         the execution data associated with this request.
    *                        This includes all context data, result sets, etc.
    *
    * @return               the 0-based index of the result page or -1
    */
   public int getResultPageIndex(PSExecutionData data)
   {
      return -1;
   }

   /**
    * Generate the results for this request.
    *
    * @param   data          the execution data associated with this request.
    *                        This includes all context data, result sets, etc.
    *
    * @exception   PSConversionException
    *                        if the conversion fails
    *
    * @exception  PSUnsupportedConversionException
    *                         if conversion to the format required by the
    *                         specified request URL is not supported
    */
   public void generateResults(PSExecutionData data)
         throws PSConversionException, PSUnsupportedConversionException
   {
      /* simply call convert */
      convert(data, null);
   }


   /* *********************  Protected Implementation ******************** */

   /**
    * Is the request URL supported by this converter? The request URL may
    * contain an extension. When it does, this is used in defining the
    * output which will be returned.
    *
    * @param   reqUrl      the URL which was specified when making this
    *                      request
    *
    * @return              <code>true</code> if conversion is supported,
    *                      <code>false</code> otherwise
    */
   protected boolean isSupported(String reqPageURL)
   {
      /* Unfortunately, we really don't have a simple check here.
       * The mime type may not relate to the extension directly!
       * We'll assume this is correct.
       */
      return true;
   }


   /**
    *    The requestor associated with this converter, to be used to
    *    determine the mime type and character encoding setting.
    */
   private PSRequestor m_requestor;
}

