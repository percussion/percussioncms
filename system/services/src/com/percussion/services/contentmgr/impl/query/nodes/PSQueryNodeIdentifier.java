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
package com.percussion.services.contentmgr.impl.query.nodes;

import com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodeVisitor;
import com.percussion.utils.jsr170.PSStringEncoder;

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a single query identifier
 * 
 * @author dougrand
 */
public class PSQueryNodeIdentifier implements IPSQueryNode
{
   /**
    * The name, never <code>null</code> or empty
    */
   private String m_name;

   /**
    * The type, may be set in the ctor or set after construction by the type
    * visitor
    */
   private int m_type = 0;

   /**
    * Ctor
    * 
    * @param name the name, never <code>null</code> or empty
    */
   public PSQueryNodeIdentifier(String name) {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_name = PSStringEncoder.decode(name);
   }
   
   /**
    * ctor
    * 
    * @param name the name, never <code>null</code> or empty
    * @param type the type
    */
   public PSQueryNodeIdentifier(String name, int type) {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_name = PSStringEncoder.decode(name);
      m_type = type;
   }   

   /**
    * Get the name of the identifier
    * 
    * @return the name, never <code>null</code> or empty
    */
   public String getName()
   {
      return m_name;
   }

   public String toString()
   {
      return "id(" + m_name + ")";
   }

   public Op getOp()
   {
      return null;
   }

   /**
    * Get the type
    * 
    * @return the type, a value from {@link javax.jcr.PropertyType}
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Set the type
    * 
    * @param type a value from {@link javax.jcr.PropertyType}
    */
   public void setType(int type)
   {
      m_type = type;
   }

   public IPSQueryNode accept(PSQueryNodeVisitor visitor) throws InvalidQueryException
   {
      return visitor.visitIdentifier(this);
   }
}
