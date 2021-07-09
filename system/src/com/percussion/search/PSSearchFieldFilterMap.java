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
package com.percussion.search;

import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates all required search field filters and provides a map of field names
 * to filters.  Derived classes need only implement 
 * {@link #getDocumentFromServer(String)}.
 */
public abstract class PSSearchFieldFilterMap
{
   /**
    * Construct a filter map using the supplied slot id
    * 
    * @param slotId The slot id used to create the filter, may not be
    * <code>null</code> or empty.
    */
   public PSSearchFieldFilterMap(String slotId)
   {
      if (slotId == null || slotId.trim().length() == 0)
         throw new IllegalArgumentException("slotId may not be null or empty");
      
      m_slotId = slotId;     
   }
   
   /**
    * Creates a keywords <code>List</code> of Content Types allowed for the
    * slot as <code>PSEntry</code> objects.
    *
    * @param slotid for which allowed content types list is needed, assumed not 
    * <code>null</code> or empty.
    *
    * @return List Each entry in the list is a <code>PSEntry</code> object
    * defining the value and its corresponding display text.
    * 
    * @throws IOException if there are any errors.
    */
   private List getSlotContentTypes(String slotid)
      throws IOException
   {
      List ctypes = null;
      if(slotid == null || slotid.trim().length()==0)
      {
          throw new IllegalArgumentException(
            "slotid must not be null or empty");
      }
      
      ctypes = new ArrayList();
      String url = SLOT_CTYPE_URL + "?" + IPSHtmlParameters.SYS_SLOTID +
         "=" + slotid;
      Document doc = getDocumentFromServer(url);
      NodeList nl = doc.getElementsByTagName(PSEntry.XML_NODE_NAME);
      PSXmlTreeWalker tree = null;
      if(nl!=null && nl.getLength() > 0)
      {
         for(int i=0; i<nl.getLength(); i++)
         {
            tree = new PSXmlTreeWalker(nl.item(i));
            String label = tree.getElementData(PSDisplayText.XML_NODE_NAME);
            String value = tree.getElementData("Value");
            ctypes.add(new PSEntry(value,new PSDisplayText(label)));
         }
      }
      
      return ctypes;
   }

   /**
    * Get a readonly copy of the filter map.
    * 
    * @return The filter map, where key is the field name as a 
    * <code>String</code> and the value is a <code>PSSearchFieldFilter</code>, 
    * never <code>null</code>, may be empty.
    * 
    * @throws IOException if there are any errors.
    */
   public Map getFilterMap() throws IOException
   {
      if (m_filterMap == null)
      {
         List slotContent = getSlotContentTypes(m_slotId);
         PSSearchFieldFilter filter = new PSSearchFieldFilter(
         IPSHtmlParameters.SYS_CONTENTTYPEID, slotContent,
            PSSearchFieldFilter.SEARCH_FILTER_TYPE_INTERSECTION);
         m_filterMap = new HashMap();
         m_filterMap.put(filter.getSearchFieldName(), filter);
      }
      return Collections.unmodifiableMap(m_filterMap);
   }

   /**
    * Gets the xml document from the server using the supplied url.  Derived
    * classes must implement this method to handle the request appropriately.
    * 
    * @param url The url to use, not <code>null</code> or empty, and in the 
    * form appName/resource.xml?params (relative to the rhythmyx server root).
    * 
    * @return The document, never <code>null</code>.
    * 
    * @throws IOException if there are any errors.
    */
   protected abstract Document getDocumentFromServer(String url) 
      throws IOException;

   /**
    * Url to retrieve slot content types
    */
   private static final String SLOT_CTYPE_URL =
      "sys_psxContentEditorCataloger/ContentTypeLookup.xml";

   /**
    * Map of field names as <code>String</code> objects to their corresponding
    * filters as <code>PSSearchFieldFilter</code> objects, initially 
    * <code>null</code>, lazily instantiated and populated by first call to
    * {@link #getFilterMap()}, never <code>null</code> or modified after that, 
    * may be empty.
    */
   private Map m_filterMap = null;
   
   /**
    * The slot id supplied during construction used to build the filter for 
    * sys_contenttypeid.  Never <code>null</code> or empty or modified after
    * construction.
    */
   private String m_slotId;  

}
