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
package com.percussion.services.contentmgr.data;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.utils.jsr170.PSValueFactory;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

/**
 * Represents a row. Each row will contain the requested properties (which may
 * be an empty list), the sort properties (which may also be empty), plus the
 * content id and revision id (sys_contentid and sys_revision) which are never
 * missing.
 * <p>
 * It should be noted that the standard JSR-170 calls will ignore any request
 * for a value that is not present in the column list of the result.
 * 
 * @author dougrand
 */
public class PSRow implements Row
{
   /**
    * The query result, never <code>null</code> after the row is added to the
    * result
    */
   PSQueryResult m_parent = null;

   /**
    * Each result is a map of property names and data values
    */
   private Map<String,Object> m_data;

   /**
    * Create a row with a given set of data
    * 
    * @param data the data for the row, each key is a property name
    */
   public PSRow(Map<String,Object> data) {
      if (data == null)
      {
         throw new IllegalArgumentException("data may not be null");
      }
      m_data = new HashMap<>();
      
      // Fix the keys, they were probably modified for Hibernate
      for(String key : data.keySet())
      {
         m_data.put(key.replace('\u00A8',':'),data.get(key));
      }
   }

   /**
    * @return Returns the parent.
    */
   public PSQueryResult getParent()
   {
      return m_parent;
   }

   /**
    * @param parent The parent to set.
    */
   public void setParent(PSQueryResult parent)
   {
      m_parent = parent;
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Row#getValues()
    */
   public Value[] getValues() throws RepositoryException
   {
      String columns[] = m_parent.getColumnNames();
      Value values[] = new Value[columns.length];
      for (int i = 0; i < columns.length; i++)
      {
         values[i] = getRawValue(columns[i]);
      }
      return values;
   }

   /** (non-Javadoc)
    * @see javax.jcr.query.Row#getValue(java.lang.String)
    */
   public Value getValue(String arg0) throws ItemNotFoundException,
         RepositoryException
   {
      String columns[] = m_parent.getColumnNames();
      boolean valid = false;
      if (columns.length > 0)
      {
         for (String c : columns)
         {
            if (c.equals(arg0))
            {
               valid = true;
               break;
            }
         }
         if (!valid)
            throw new ItemNotFoundException("Invalid property " + arg0);
      }

      return getRawValue(arg0);
   }

   /**
    * Get a value without regard to whether the value is in the configured
    * values. The row always contains ids and sort properties, which may not be
    * included in the "requested" properties.
    * 
    * @param arg0 the name of the property to retrieve, never <code>null</code>
    *           or empty
    * @return the value object representing the desired property, may be
    *         <code>null</code>
    * @throws RepositoryException for a variety of problems
    */
   public Value getRawValue(String arg0) throws RepositoryException
   {
      Object val;

      if (arg0.equals(IPSContentPropertyConstants.JCR_PATH))
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
         Integer folderid = (Integer) m_data
               .get(IPSContentPropertyConstants.RX_SYS_FOLDERID);
         try
         {
            if (folderid != null)
            {
               String[] paths = proc.getItemPaths(new PSLocator(folderid));
               if (paths.length > 0)
                  val = paths[0];
               else
                  val = null;
            }
            else
            {
               val = null;
            }
         }
         catch (PSCmsException | PSNotFoundException e)
         {
            throw new RepositoryException("Problem getting path", e);
         }
      }
      else
      {
         val = m_data.get(arg0);
      }

      if (val != null)
         return PSValueFactory.createValue(val);
      else
         return null;
   }

   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object arg0)
   {
      PSRow b = (PSRow) arg0;
      return m_data.equals(b.m_data);
   }

   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return m_data.hashCode();
   }

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "<PSRow: " + m_data.toString() + ">";
   }
   
   

}
