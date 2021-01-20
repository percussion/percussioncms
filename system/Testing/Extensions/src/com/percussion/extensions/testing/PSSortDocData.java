/******************************************************************************
 *
 * [ PSSortDocData.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.data.PSDataConverter;

import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.IPSExtensionErrors;

import com.percussion.server.PSRequest;
import com.percussion.server.IPSRequestContext;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
                                
import java.text.Collator;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This exit sorts the result document elements by given sort order fields in
 * ascending order of their values. If the element values are of type string,
 * they are compared case insensitive. Duplicate rows of document are ignored.
 * It needs 2 parameters.
 * <li>parameter 1 - xml element names separated by comma in the order specified
 * in mapper
 * <li>parameter 2 - same xml element names in the required sort order.
 * The sort order fields should be subset or equivalent to mapper fields.
 * If any of the parameters passed in is <code>null</code> or empty, it returns
 * the document without modifying. This is to provide sorting independent of
 * database.
 */
public class PSSortDocData extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{

   /*
    * Implementation of the method required by
    * <code>IPSResultDocumentProcessor</code> interface. See this class
    * description for description of this method.
    *
    * @param params array of parameters to this exit, returns the passed in
    * document if this is <code>null</code> or it's length not equal to 2 or
    * any element of the parameter is <code>null</code> or empty.
    * @param request the request context of the request, may be
    * <code>null</code>
    * @param resultDoc the document to be modified, may not be <code>null</code>
    *
    * @return the modified document, never <code>null</code>
    * @throws PSExtensionProcessingException if there is mismatch between mapper
    * fields and sort order fields or the document does not contain the elements
    * with names specified in mapper fields.
    *
    * @throws IllegalArgumentException if <code>resultDoc</code> is
    * <code>null</code>
    */
   public  Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSExtensionProcessingException
   {
      if(resultDoc == null)
         throw new IllegalArgumentException(
            "The document to modify for sorting can not be null");

      if(params == null || params.length != 2)
         return resultDoc;

      if(params[0] == null || params[0].toString().trim().length() == 0 ||
         params[1] == null || params[1].toString().trim().length() == 0 )
         return resultDoc;

      String mapper_fields = params[0].toString().trim();
      String sort_order = params[1].toString().trim();

      StringTokenizer st = new StringTokenizer(mapper_fields, ",");

      List mapFields = new ArrayList();
      while(st.hasMoreTokens())
      {
         mapFields.add(st.nextToken().trim());
      }

      st = new StringTokenizer(sort_order, ",");
      List sortOrderFields = new ArrayList();
      while(st.hasMoreTokens())
      {
         sortOrderFields.add(st.nextToken().trim());
      }
      if(mapFields.size() < sortOrderFields.size() ||
         !mapFields.containsAll(sortOrderFields))
      {
         String message = "The exit " +  m_def.getRef().getExtensionName() +
            " received invalid parameters \r\n" +
            "Mapper fields - " + mapper_fields
            + "; Sort order fields - " + sort_order +
            "\r\n message: Sort order fields is not a sub set of mapper fields";

         throw new PSExtensionProcessingException(
            IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, message);
      }

      return applySorting(resultDoc, mapFields, sortOrderFields, mapper_fields);
   }


   /**
    * Sorts elements in the document according to the order specified in
    * <code>sortOrderFields</code>.
    *
    * @param doc the document to be sorted, assumed not <code>null</code>
    * @param mapFields the fields to look for in the document, assumed neither
    * <code>null</code> nor empty. The elements in this list also should not be
    * <code>null</code> or empty.
    * @param sortOrderFields the sort order of fields, assumed neither
    * <code>null</code> nor empty. The elements in this list also should not be
    * <code>null</code> or empty.
    * @param mapper_fields the mapper fields parameter, assumed neither
    * <code>null</code> nor empty.
    *
    * @return the document with sorted data, never <code>null</code>
    *
    * @throws PSExtensionProcessingException if the document does not contain
    * the elements with names specified in <code>mapFields</code>.
    **/
   private Document applySorting(Document doc, List mapFields,
      List sortOrderFields, String mapper_fields)
      throws PSExtensionProcessingException
   {
      //Get elements specified in mapper fields from document for all rows
      Element root = doc.getDocumentElement();
      List mapElements = new ArrayList();
      Iterator iter = mapFields.iterator();
      while(iter.hasNext())
      {
         String elementName = (String)iter.next();
         NodeList list = root.getElementsByTagName(elementName);
         if( list != null && list.getLength() > 0)
            mapElements.add(list);
         else
         {
            String message = "The exit " +  m_def.getRef().getExtensionName() +
            " received invalid parameter \r\n" +
            "Mapper fields - " + mapper_fields +
            "\r\n message: Missing '" + elementName +
            "' element in the document";

            throw new PSExtensionProcessingException(
               IPSExtensionErrors.EXT_PARAM_VALUE_INVALID, message);
         }
      }

      //Sort the rows of elements according to sort order
      NodeList elements = (NodeList)mapElements.get(0);
      Comparator comparator = new DataComparator(sortOrderFields);
      TreeSet sortElements = new TreeSet(comparator);

      int i = 0;
      while( i < elements.getLength())
      {
         Map elementData = new HashMap();
         iter = mapElements.iterator();
         while(iter.hasNext())
         {
            Node dataElement = ((NodeList)(iter.next())).item(i);
            elementData.put( ((Element)dataElement).getTagName(),
               PSXmlTreeWalker.getElementData(dataElement) );
         }
         sortElements.add(elementData);
         i++;
      }

      //Write to document after sorting
      Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(resultDoc, root.getTagName());

      iter = sortElements.iterator();
      while(iter.hasNext())
      {
         Map data = (Map)iter.next();
         Iterator fieldsIterator = mapFields.iterator();
         while(fieldsIterator.hasNext())
         {
            String key = (String)fieldsIterator.next();
            PSXmlDocumentBuilder.addElement(resultDoc, root, key,
               (String)data.get(key));
         }
      }

      return resultDoc;
   }

   //see interface for description
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Inner class to implement <code>Comparator</code> interface which is used
    * for comparison while sorting.
    **/
   private class DataComparator implements Comparator
   {
      /**
       * Compares its two arguments for order.  Returns a negative integer,
       * zero, or a positive integer as the first argument is less than, equal
       * to, or greater than the second. The arguments should be of type
       * <code>Map</code> and each argument should have element names specified
       * in sort order fields as keys in the entries. The values of these keys
       * are compared in the order of sort order fields which are specified in
       * constructor.
       *
       * @param o1 the first object to be compared, may not be <code>null</code>
       * @param o2 the second object to be compared, may not be
       * <code>null</code>
       *
       * @return a negative integer, zero, or a positive integer as the
       * first argument is less than, equal to, or greater than the second.
       *
       * @throws ClassCastException if the arguments' types prevent them from
       * being compared by this Comparator.
       *
       * @throws IllegalArgumentException if any of argument is null or their
       * values prevent them from being compared by this Comparator.
       **/
      public int compare(Object o1, Object o2)
      {
         if(o1 == null || o2 == null)
            throw new IllegalArgumentException(
               "The objects to be compared can not be null");

         Map m1 = (Map)o1;
         Map m2 = (Map)o2;

         if(m1.size() != m2.size() || m1.size() < m_sortOrderFields.size())
            throw new IllegalArgumentException("Illegal Values for comparing");

         String s1 = null;
         String s2 = null;

         int result = 0;
         Iterator iter = m_sortOrderFields.iterator();
         while(result == 0 && iter.hasNext())
         {
            String fieldName = (String)iter.next();
            String m1Value = (String)m1.get(fieldName);
            if(m1Value == null || m1Value.trim().length() == 0)
               m1Value = "";
            String m2Value = (String)m2.get(fieldName);
            if(m2Value == null || m2Value.trim().length() == 0)
               m2Value = "";

            //Get comparison data type based on values
            int type = getType(m1Value, m2Value);

            switch(type)
            {
               case DATE:
                  result = compareDate(m1Value, m2Value);
                  break;
               case NUMBER:
                  result = compareNumber(m1Value, m2Value);
                  break;
               case STRING:
                  result = m_collator.compare(m1Value, m2Value);
                  break;
            }
         }

         return result;
      }

      public boolean equals(Object object) {
         if (this == object) return true;
         if (!(object instanceof DataComparator)) return false;
         if (!super.equals(object)) return false;
         DataComparator that = (DataComparator) object;
         return java.util.Objects.equals(m_sortOrderFields, that.m_sortOrderFields) &&
                 java.util.Objects.equals(m_collator, that.m_collator);
      }

      public int hashCode() {
         return Objects.hash(super.hashCode(), m_sortOrderFields, m_collator);
      }

      /**
       * Gets data type for comparison of values. Initially it checks both
       * values for number type, if not checks for date type, otherwise treats
       * them as strings.
       *
       * @param value1 the first value in the comparison values, assumed neither
       * <code>null</code> nor empty
       * @param value2 the second value in the comparison values, assumed
       * neither <code>null</code> nor empty
       *
       * @return the type of values to be compared
       **/
      private int getType(String value1, String value2)
      {
         int type = STRING;
         try {
            Double.parseDouble(value1);
            Double.parseDouble(value2);
            type = NUMBER;
         }
         catch(NumberFormatException ne)
         {
            try {
               PSDataConverter.parseStringToDate(value1);
               PSDataConverter.parseStringToDate(value2);
               type = DATE;
            }
            catch(ParseException e){}
         catch(IllegalArgumentException e){}
         }
         return type;
      }

      /**
       * Compares the passed in strings as dates.
       *
       * @param value1 the first value to be compared, assumed neither
       * <code>null</code> nor empty and should confirm to a valid date format.
       * @param value2 the second value to be compared, assumed neither
       * <code>null</code> nor empty and should confirm to a valid date format.
       *
       * @return a negative integer, zero, or a positive integer as the
       * first argument is less than, equal to, or greater than the second.
       *
       * @throws IllegalArgumentException if arguments do not confirm to valid
       * date format.
       **/
      private int compareDate(String value1, String value2)
      {
         int result = 0;
         try {
            Date date1 = PSDataConverter.parseStringToDate(value1);
            Date date2 = PSDataConverter.parseStringToDate(value2);
            result = compareNumber(date1.getTime(), date2.getTime());
         }
         catch(ParseException e)
         {
            throw new IllegalArgumentException(
               "Illegal arguments for comparing as dates" +
               value1 + "," + value2);
         }
         return result;
      }

      /**
       * Compares the passed in strings as numbers.
       *
       * @param value1 the first value to be compared, assumed neither
       * <code>null</code> nor empty and is a valid number.
       * @param value2 the second value to be compared, assumed neither
       * <code>null</code> nor empty and is a valid number.
       *
       * @return a negative integer, zero, or a positive integer as the
       * first argument is less than, equal to, or greater than the second.
       *
       * @throws IllegalArgumentException if arguments can not be coverted to
       * numbers
       **/
      private int compareNumber(String value1, String value2)
      {
         int result = 0;
         try {
            double d1 = Double.parseDouble(value1);
            double d2 = Double.parseDouble(value2);
            result = compareNumber(d1, d2);
         }
         catch(NumberFormatException e)
         {
            throw new IllegalArgumentException(
               "Illegal arguments for comparing as numbers" +
               value1 + "," + value2);
         }
         return result;
      }

      /**
       * Compares two numbers.
       *
       * @param d1 the first value to be compared
       * @param d2 the second value to be compared
       *
       * @return -1, 0, or 1 as the first argument is less than, equal to, or
       * greater than the second.
       **/
      private int compareNumber(double d1, double d2)
      {
         int result = 0;
         if(d1 < d2)
            result = -1;
         else if(d1 == d2)
            result = 0;
         else
            result = 1;

         return result;
      }

      /**
       * Constructor for creating comparator instance.
       *
       * @param sortFields the sort order of fields, may not be
       * <code>null</code> or empty.The elements in this list also should not be
       * <code>null</code> or empty. This is used while comparing for order of
       * fields to be compared.
       *
       * @throws IllegalArgumentException if <code>sortFields</code> is invalid.
       **/
      public DataComparator(List sortFields)
      {
         if(sortFields == null || sortFields.isEmpty())
            throw new IllegalArgumentException(
               "The sort order fields used for comparison " +
               "can not be null or empty");

         Iterator iter = sortFields.iterator();
         while(iter.hasNext())
         {
            String fieldName = (String)iter.next();
            if(fieldName == null || fieldName.trim().length() == 0)
               throw new IllegalArgumentException(
               "The name specified in sort order list which is used for " +
               "comparison can not be null or empty");
         }
         m_sortOrderFields = sortFields;
         m_collator = Collator.getInstance();
         m_collator.setStrength(Collator.SECONDARY);
      }

      /**
       * List of sort order fields, initialized in constructor and never
       * <code>null</code> after that. The elements in this list also should not
       * be <code>null</code> or empty.
       */
      private List m_sortOrderFields;

      /**
       * The instance of collator used for case insensitive comparing of two
       * strings, initialized in constructor and never <code>null</code> after
       * that.
       */
      private Collator m_collator;
   }

   /** Constant for number type **/
   private static final int NUMBER = 0;

   /** Constant for date type **/
   private static final int DATE = 1;
   
   /** Constant for string type **/
   private static final int STRING = 2;
}

