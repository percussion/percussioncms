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
package com.percussion.search.lucene;

import com.percussion.cms.objectstore.PSItemChildLocator;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.search.PSSearchKey;
import com.percussion.util.PSStringOperation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Handles the transformation of Rhythmyx ids to string identifiers used to 
 * uniquely identify various objects in the Lucene system.
 * <p>This is a utility class, it contains no state.
 *
 * @author bjoginipally
 *
 */
public class PSKeyConverter
{

   /**
    * The name of the library meta field that contains the Rx generated id.
    * This is typically the value returned by the one of the <code>convert
    * </code> methods that returns a String.
    */
   public static final String LUCENE_DOCID_FIELDNAME = "sys_lucenedocid";
   
   /**
    * Converts the supplied id that is stored with a Lucene document to the 
    * Rx item locator that matches it. The supplied id may be for an item
    * or for a complex child of an item. In either case, the main item's 
    * locator is returned.
    * 
    * @param docId A value originally obtained from the {@link #convert(
    * PSLocator,PSItemChildLocator)} method or one of its convenience
    * method. Never <code>null</code> or empty.
    * 
    * @return The locator for the item associated with this Lucene document.
    * Never <code>null</code>.
    * 
    * @throws NumberFormatException if the item's content id in the supplied
    * <code>docId</code> is not a number. This should never happen if the 
    * supplied <code>docId</code> originated from this class.
    */
   public static PSLocator convert(String docId)
   {
      if (null == docId)
      {
         throw new IllegalArgumentException("docId cannot be null");
      }
      /* The id has the following format - 
       *    itemId[:[childId;childRowId]]
       * Where the childId and childRowId are each optional and the delims
       * are identified by the xxx_DELIM constants in this class.
       */
      List parts = PSStringOperation.getSplittedList(docId, PRIMARY_DELIM);
      int id = Integer.parseInt(parts.get(0).toString());
      
      return new PSLocator(id, -1);
   }
   
   /**
    * Creates a unique identifier that can be stored with the Lucene
    * document. This id can then be used to determine the main item when
    * processing a query result using the {@link #convert(String)} method.
    * <p>This id may only be unique within a single Lucene index.
    * <p>The id is composed of 2 primary parts, the item id and the rest of the
    * id. The rest of the id is composed of 0 or two parts. The format is as
    * follows - 
    *    <p>itemId[:[childId;childRowId]]
    * <p>Where the childId and childRowId are each optional and the delims
    * are identified by the xxx_DELIM constants in this class. The 
    * {@link #PRIMARY_DELIM} separates the primary parts and so on for the 
    * other delim.
    * 
    * @param item Never <code>null</code>.
    * 
    * @param child May be <code>null</code> if this id is not associated with
    * a complex child entry.
    * 
    * @return A value to store with the Lucene document. Never <code>null</code> or
    * empty.
    */
   public static String convert(PSLocator item, PSItemChildLocator child)
   {
      if (null == item)
      {
         throw new IllegalArgumentException("item locator cannot be null");
      }
      String part2 = ""; 
      if (null != child)
      {
            part2 = "" + child.getChildContentType()+ SECONDARY_DELIM + child.getChildRowId();
      }

      return assemble(""+item.getId(), part2, PRIMARY_DELIM);
   }

   /**
    * Convenience method that calls {@link #convert(PSLocator, 
    * PSItemChildLocator) convert(unitId.getParentLocator(), 
    * unitId.getChildId())}.
    */
   public static String convert(PSSearchKey unitId)
   {
      return convert(new PSLocator(unitId.getParentLocator().getId()), unitId.getChildId());
   }
   
   /**
    * Determine if the supplied search key specifies the same item as the
    * supplied document ID.
    * 
    * @param unitId The search key to compare, may not be <code>null</code>.
    * @param docId The document id to compare to, may not be <code>null</code>
    * or empty.
    * 
    * @return <code>true</code> if the keys match, <code>false</code>
    * otherwise. If the supplied search key specifies a child item, then
    * <code>true</code> is returned only if the supplied doc id specifies both
    * the same parent and child locator. If the supplied search key specifies a
    * parent item, then <code>true</code> is returned for any doc id that
    * matches the parent key regardless of any child id specified by the doc id.
    */
   public static boolean isMatchingKey(PSSearchKey unitId, String docId)
   {
      if (unitId == null)
         throw new IllegalArgumentException("unitId may not be null");
      
      if (StringUtils.isBlank(docId))
         throw new IllegalArgumentException("docId may not be null or empty");
      
      String baseKey = convert(unitId);
      String baseDocId = docId.split(SECONDARY_DELIM)[0];
      
      return baseDocId.startsWith(baseKey);
   }

   /**
    * Assembles the supplied pieces into a single string using the supplied
    * delimiter, escaping any delim occurrences in the pieces.
    * 
    * @param piece1 Assumed not <code>null</code> or empty.
    * @param piece2 Assumed not <code>null</code>. If empty, piece1 is returned.
    * @param delim Assumed not <code>null</code> or empty.
    * @return Never <code>null</code>.
    */
   private static String assemble(String piece1, String piece2, String delim)
   {
      if (piece2.trim().length() == 0)
         return piece1;
      List<String> parts = new ArrayList<String>();
      parts.add(piece1);
      parts.add(piece2);  
      return PSStringOperation.append(parts, delim);      
   }

   /**
    * The char used as the seperator between the secondary key parts and the
    * item locator id.
    */
   private static final String PRIMARY_DELIM = ":";
   
   /**
    * The char used as the seperator between the parts of the key that are not
    * the item locator id.
    */
   private static final String SECONDARY_DELIM = ";";
   
   /**
    * Never instantiate these guys.
    */
   private PSKeyConverter()
   {}

}
