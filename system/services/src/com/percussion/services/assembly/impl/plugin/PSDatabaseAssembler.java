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
package com.percussion.services.assembly.impl.plugin;

import com.percussion.data.PSCachedStylesheet;
import com.percussion.data.PSTransformErrorListener;
import com.percussion.data.PSUriResolver;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.util.PSBase64Encoder;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.utils.xml.PSSaxCopier;
import com.percussion.utils.xml.PSSaxHelper;
import com.percussion.xml.PSStylesheetCacheManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.parsers.SAXParser;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The database assembly plugin produces an xml document that is appropriate for
 * the database publishing plugin. It uses StAX to create the XML output stream.
 * <p>
 * The output data is assembled from specific bindings. The table def is
 * expected as the
 * <q>source</q>
 * of the template.
 * 
 * @author dougrand
 * 
 */
public class PSDatabaseAssembler extends PSAssemblerBase
{
   /**
    * Jexl expression constant
    */
   private static final IPSScript ROW = PSJexlEvaluator
         .createStaticExpression("$row");

   /**
    * Jexl expression constant
    */
   private static final IPSScript DB_PARENT = PSJexlEvaluator
         .createStaticExpression("$db.parent");

   /**
    * Jexl expression constant
    */
   private static final IPSScript SYS_TEMPLATE = PSJexlEvaluator
         .createStaticExpression("$sys.template");

   /**
    * Jexl expression constant
    */
   private static final IPSScript DB_DATABASE = PSJexlEvaluator
         .createStaticExpression("$db.database");

   /**
    * Jexl expression constant
    */
   private static final IPSScript DB_DRIVERTYPE = PSJexlEvaluator
         .createStaticExpression("$db.drivertype");

   /**
    * Jexl expression constant
    */
   private static final IPSScript DB_RESOURCE = PSJexlEvaluator
         .createStaticExpression("$db.resource");

   /**
    * Jexl expression constant
    */
   private static final IPSScript DB_ORIGIN = PSJexlEvaluator
         .createStaticExpression("$db.origin");

   /**
    * Jexl expression constant
    */
   private static final IPSScript DB_ACTION = PSJexlEvaluator
         .createStaticExpression("$db.action");

   /**
    * Child expressions calculated, may be empty for non-child case, see
    * {@link #getChildExpression(int)} for more info
    */
   private static final List<IPSScript> ms_children = new ArrayList<IPSScript>();

   /**
    * Child expressions calculated, may be empty for non-child case, see
    * {@link #getDbChildExpression(int)} for more info
    */
   private static final List<IPSScript> ms_db_children = new ArrayList<IPSScript>();

   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSDatabaseAssembler.class);

   /**
    * Encoding constant
    */
   private static String ms_encoding = "$encoding";

   /**
    * Current indentation level for pretty printing
    */
   private int m_indent = 0;

   /**
    * When <code>true</code> the formatting needs to put a CR and indent
    * before and end element
    */
   private boolean m_needCR = false;

   /**
    * rxDir relative path to preview stylesheet used for context 0.
    */
   private static final String ms_previewStyleSheet = 
      "sys_resources/stylesheets/sys_DatabasePublishingPreview.xsl";

   @Override
   public IPSAssemblyResult assembleSingle(IPSAssemblyItem item)
   {
      // Grab the essential data
      PSJexlEvaluator eval = new PSJexlEvaluator(item.getBindings());
      StringWriter writer = new StringWriter();
      XMLOutputFactory ofact = XMLOutputFactory.newInstance();
      try
      {
         XMLStreamWriter formatter = ofact.createXMLStreamWriter(writer);

         String db_action = eval.getStringValue(DB_ACTION, "r", false);
         String db_origin = eval.getStringValue(DB_ORIGIN, null, false);
         String db_resource = eval.getStringValue(DB_RESOURCE, null, true);
         String db_drivertype = eval.getStringValue(DB_DRIVERTYPE, null, true);
         String db_database = eval.getStringValue(DB_DATABASE, null, false);

         ms_log.debug("Starting db assembly for " + item.getId());

         formatter.writeStartDocument();
         startElement(formatter, "datapublisher");
         if (StringUtils.isNotBlank(db_database))
         {
            formatter.writeAttribute("dbname", db_database);
         }
         formatter.writeAttribute("drivertype", db_drivertype);
         if (StringUtils.isNotBlank(db_origin))
         {
            formatter.writeAttribute("origin", db_origin);
         }
         formatter.writeAttribute("resourceName", db_resource);
         formatter.writeCharacters("\n");

         // Copy the template into place
         PSSaxCopier copier = new PSSaxCopier(formatter, null, true);
         SAXParser parser = PSSaxHelper.newSAXParser(copier);
         String template = (String) eval.evaluate(SYS_TEMPLATE);
         if (StringUtils.isBlank(template))
         {
            return getFailureResult(item, "Missing required table definition template");
         }

         parser.parse(new ByteArrayInputStream(template.getBytes()), copier);

         // Render the tree of content
         startElement(formatter, "tabledataset");
         renderItem(formatter, -1, eval, db_action);
         endElement(formatter);

         endElement(formatter);
         formatter.writeEndDocument();

         formatter.close();
         
         String dbdoc = writer.toString();
         dbdoc = StringUtils.replace(dbdoc, PSSaxCopier.RX_FILLER, "");

         if (item.getContext() == 0)
         {
            StringWriter errorWriter = new StringWriter();
            // record transformation errors so they can be added to the response
            PSTransformErrorListener errorListener = new PSTransformErrorListener(
                  new PrintWriter(errorWriter));
            File previewSS = new File(PSServer.getRxDir(), ms_previewStyleSheet);
            PSCachedStylesheet ss = PSStylesheetCacheManager
               .getStyleSheetFromCache(previewSS.toURL());
            Transformer nt = ss.getStylesheetTemplate().newTransformer();
            nt.setErrorListener(errorListener);
            nt.setURIResolver(new PSUriResolver());

            Source src = new StreamSource(new StringReader(dbdoc));
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Result res = new StreamResult(bout);

            nt.transform(src, res);
            String errors = errorWriter.toString();
            if (errors.length() > 0)
            {
               throw new Exception(errors);
            }
            
            item.setResultData(bout.toByteArray());
            item.setMimeType("text/html");
         }
         else
         {
            item.setResultData(dbdoc.getBytes("UTF8"));
            item.setMimeType("text/xml");
         }
         item.setStatus(IPSAssemblyResult.Status.SUCCESS);
      }
      catch (IllegalArgumentException e)
      {
         return getFailureResult(item, e.getLocalizedMessage());
      }
      catch (Exception e)
      {
         return getFailureResult(item, "Serious problem "
               + e.getLocalizedMessage());
      }

      return (IPSAssemblyResult) item;
   }

   /**
    * Do the start of an element, including contextual pretty printing
    * 
    * @param formatter the formatter
    * @param element the element name
    * @throws XMLStreamException
    */
   private void startElement(XMLStreamWriter formatter, String element)
         throws XMLStreamException
   {
      formatter.writeCharacters("\n");
      for (int i = 0; i < m_indent; i++)
      {
         formatter.writeCharacters(" ");
      }
      formatter.writeStartElement(element);
      m_indent += 2;
      m_needCR = false;
   }

   /**
    * End an element, manage the indentation
    * 
    * @param formatter
    * @throws XMLStreamException
    */
   private void endElement(XMLStreamWriter formatter) throws XMLStreamException
   {
      m_indent -= 2;
      if (m_needCR)
      {
         formatter.writeCharacters("\n");
         for (int i = 0; i < m_indent; i++)
         {
            formatter.writeCharacters(" ");
         }
      }
      formatter.writeEndElement();
      m_needCR = true;
   }

   /**
    * Handle character output
    * 
    * @param formatter the StAX formatter, assumed never <code>null</code>
    * @param chars the characters to output, assumed never <code>null</code>
    * @throws XMLStreamException if there's a problem writing the character
    *            output
    */
   private void characters(XMLStreamWriter formatter, String chars)
         throws XMLStreamException
   {
      formatter.writeCharacters(chars);
      m_needCR = false;
   }

   /**
    * Render a specific item. Each item is either the parent or child.
    * 
    * @param formatter the output formatter, assumed not <code>null</code>
    * @param child the child to render, -1 indicates the parent, assumed not
    *           <code>null</code>
    * @param eval the JEXL evaluator, assumed not <code>null</code>
    * @param action the db action, assumed not <code>null</code>
    * 
    * @return <code>false</code> if the child does not exist
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   private boolean renderItem(XMLStreamWriter formatter, int child,
         PSJexlEvaluator eval, String action) throws Exception
   {
      IPSScript base;
      String tablename;
      String element;

      if (child < 0)
      {
         base = ROW;
         tablename = eval.getStringValue(DB_PARENT, null, true);
         element = "table";
      }
      else
      {
         base = getChildExpression(child);
         IPSScript dbchild = getDbChildExpression(child);
         tablename = eval.getStringValue(dbchild, null, true);
         element = "childtable";
      }

      Object data;
      try
      {
         data = eval.evaluate(base);
      }
      catch (Exception e)
      {
         ms_log.error("Problem evaluating child: " + base);
         return false;
      }

      checkType(base.getParsedText(), data, Map.class, true);

      startElement(formatter, element);
      formatter.writeAttribute("name", tablename);

      Map<String, Object> datamap = (Map<String, Object>) data;

      Object enc = datamap.get(ms_encoding);
      Map<String, String> encodings = null;
      checkType(base.getParsedText() + "." + ms_encoding, enc, Map.class, false);
      encodings = (Map<String, String>) enc;

      int count = checkMap(tablename, datamap, child == -1);

      for (int i = 0; i < count; i++)
      {
         startElement(formatter, "row");
         formatter.writeAttribute("action", action);
         for (Map.Entry<String, Object> row : datamap.entrySet())
         {
            String encoding = null;
            if (encodings != null)
            {
               encoding = encodings.get(row.getKey());
            }
            Object columnValue = extractColumnValue(i, row);
            renderSingleColumn(formatter, row.getKey(), columnValue, encoding);
         }
         if (child == -1)
         {
            if (!renderChildren(formatter, eval, action))
               return false;
         }
         endElement(formatter);
      }

      endElement(formatter);
      return true;

   }

   /**
    * Extract the value for a given column. Handles lists and value arrays.
    * 
    * @param i the index to extract for a list or value array, it is an error
    * to pass a value other than zero if the base value is not a list or 
    * array
    * 
    * @param row the row data map, assumed never <code>null</code>
    * @return the value to use
    */
   private Object extractColumnValue(int i, Map.Entry<String, Object> row)
   {
      Object value = row.getValue();
      Object columnValue = null;
      if (value instanceof List)
      {
         columnValue = ((List) value).get(i);
      }
      else if (value instanceof Value[])
      {
         columnValue = ((Value[]) value)[i];
      }
      else
      {
         columnValue = value;
      }
      return columnValue;
   }

   /**
    * Render any children
    * 
    * @param formatter the formatter being used for the output document, assumed
    *           never <code>null</code>
    * @param eval the evaluator being used to extract the data for the output
    *           document, assumed never <code>null</code>
    * @param action the action to take, assumed never <code>null</code> or
    *           empty
    * @return <code>true</code> if there's no problem, <code>false</code>
    *         indicates that there's a data error, e.g. a child was repeated,
    *         or there was an error rendering the child
    * @throws Exception
    */
   private boolean renderChildren(XMLStreamWriter formatter,
         PSJexlEvaluator eval, String action) throws Exception
   {
      // Render children as long as we find a table name
      int j = 0;
      Set<String> childnames = new HashSet<String>();
      while (true)
      {
         IPSScript childname = getDbChildExpression(j);
         String childtable = eval.getStringValue(childname, null, false);
         if (childtable == null)
            break;
         //FB: GC_UNRELATED_TYPES NC 1-17-16
         if (childnames.contains(childtable))
         {
            ms_log.error("More than one child table of the name " + childtable
                  + " defined");
            return false;
         }
         childnames.add(childname.getParsedText());
         boolean result = renderItem(formatter, j, eval, action);
         if (!result)
            return false;
         j++;
      }
      return true;
   }

   /**
    * Get the desired child variable expression
    * 
    * @param child the child index, starts at 0
    * @return the expression, never <code>null</code>
    */
   private static synchronized IPSScript getDbChildExpression(int child)
   {
      while (child >= ms_db_children.size())
      {
         String e = "$db.child[" + ms_db_children.size() + "]";
         ms_db_children.add(PSJexlEvaluator.createStaticExpression(e));
      }
      return ms_db_children.get(child);
   }

   /**
    * Get the desired child variable expression
    * 
    * @param child the child index, starts at 0
    * @return the expression, never <code>null</code>
    */
   private static synchronized IPSScript getChildExpression(int child)
   {
      while (child >= ms_children.size())
      {
         String e = "$child[" + ms_children.size() + "]";
         ms_children.add(PSJexlEvaluator.createStaticExpression(e));
      }
      return ms_children.get(child);
   }

   /**
    * Render one column of the output document
    * 
    * @param formatter the formatter to use
    * @param cname the column name
    * @param value the value to use
    * @param encoding the encoding to use or <code>null</code> for no encoding
    * @throws XMLStreamException
    * @throws RepositoryException
    * @throws IllegalStateException
    * @throws ValueFormatException
    */
   private void renderSingleColumn(XMLStreamWriter formatter, String cname,
         Object value, String encoding) throws XMLStreamException,
         ValueFormatException, IllegalStateException, RepositoryException
   {
      // Ignore encoding "columns"
      if (cname.equalsIgnoreCase(ms_encoding))
         return;

      startElement(formatter, "column");
      formatter.writeAttribute("name", cname);

      if (encoding != null)
      {
         formatter.writeAttribute("encoding", encoding);
      }

      if (value != null)
      {
         if (encoding == null)
         {
            String outval = null;
            if (value instanceof Value)
            {
               outval = ((Value) value).getString();
            }
            else if (value instanceof Property)
            {
               outval = ((Property) value).getString();
            }
            else if (value instanceof List)
            {
               outval = ((List) value).get(0).toString();
            }
            else
            {
               outval = value.toString();
            }
            characters(formatter, outval);
         }
         else if (encoding.equalsIgnoreCase("base64"))
         {
            String out = null;
            if (value instanceof byte[])
            {
               out = new String(PSBase64Encoder.encode((byte[]) value));
            }
            else if (value instanceof Value)
            {
               try
               {
                  Value avalue = (Value) value;
                  if (avalue.getType() == PropertyType.BINARY)
                  {
                     ByteArrayOutputStream os = new ByteArrayOutputStream();
                     InputStream is = avalue.getStream();
                     byte[] buf = new byte[1024];
                     int count;
                     while ((count = is.read(buf)) > 0)
                     {
                        os.write(buf, 0, count);
                     }
                     out = new String(PSBase64Encoder.encode(os.toByteArray()));
                  }
                  else
                  {
                     out = avalue.getString();
                  }
               }
               catch (Exception e)
               {
                  ms_log.error("Could not get value for column " + cname, e);
               }
            }
            else
            {
               out = PSBase64Encoder.encode(value.toString(),
                     IPSUtilsConstants.RX_JAVA_ENC);
            }
            characters(formatter, out);
         }
         else
         {
            ms_log.error("Unknown encoding specified for column " + cname);
         }
      }

      endElement(formatter);
   }

   /**
    * Check the validity of the map. Children can have multiple elements per
    * "variable" in the map, in which case they all must match. Parents have one
    * element per variable only. An element for a child can have either a single
    * value or a list.
    * 
    * @param tablename the tablename, assumed not <code>null</code>
    * @param data the datamap, assumed not <code>null</code>
    * @param parent <code>true</code> if checking parent
    * @return the number of elements
    */
   private int checkMap(String tablename, Map<String, Object> data,
         boolean parent)
   {
      int count = 1;

      for (Map.Entry<String, Object> entry : data.entrySet())
      {
         if (count == 1)
         {
            if (entry.getValue() instanceof List)
            {
               count = ((List) entry.getValue()).size();
            }
            else if (entry.getValue() instanceof Value[])
            {
               count = ((Value[]) entry.getValue()).length;
            }
         }
         if (entry.getValue() instanceof List)
         {
            int candidate = ((List) entry.getValue()).size();
            if (count != candidate)
            {
               throw new IllegalStateException("Found a " + candidate
                     + " values where " + count
                     + " values where expected for table " + tablename
                     + " row " + entry.getKey());
            }
         }
         else if (entry.getValue() instanceof Value[])
         {
            int candidate = ((Value[]) entry.getValue()).length;
            if (count != candidate)
            {
               throw new IllegalStateException("Found a " + candidate
                     + " values where " + count
                     + " values where expected for table " + tablename
                     + " row " + entry.getKey());
            }
         }
      }

      if (parent && count > 1)
      {
         throw new IllegalStateException("Found multiple values for a "
               + "parent table");
      }

      return count;
   }

   /**
    * Check to see if the passed value is of the right type
    * 
    * @param propertyname the name of the property, never <code>null</code> or
    *           empty
    * @param value the value, checked if not null
    * @param clazz the class, never <code>null</code>
    * @param required if <code>true</code>, the value must be not
    *           <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void checkType(String propertyname, Object value, Class clazz,
         boolean required)
   {
      if (StringUtils.isBlank(propertyname))
      {
         throw new IllegalArgumentException(
               "propertyname may not be null or empty");
      }
      if (clazz == null)
      {
         throw new IllegalArgumentException("clazz may not be null");
      }
      if (required && value == null)
      {
         throw new IllegalArgumentException("The binding " + propertyname
               + " is required");
      }
      if (value != null && !clazz.isAssignableFrom(value.getClass()))
      {
         throw new IllegalArgumentException("The value for binding "
               + propertyname + " is of type " + value.getClass().getName()
               + " when a value of type " + clazz.getName() + " is expected");
      }
   }
}
