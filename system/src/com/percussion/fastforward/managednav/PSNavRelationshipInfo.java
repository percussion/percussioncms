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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.server.IPSRequestContext;

/**
 * Holds relationship info needed by Navigation Effects. The underlying
 * relationship classes only contain <code>PSLocator</code> objects. The
 * navigation system usually needs to know the content type and name of the
 * owner and/or dependent object. This class contains the
 * <code>PSComponentSummary</code> objects that contain this information.
 * 
 * @author DavidBenua
 * 
 *  
 */
public class PSNavRelationshipInfo
{
   /**
    * Constructs a new relathionship info.
    * 
    * @param relation the underlying relationship.
    * @param req the parent request context.
    * @throws PSNavException when any error occurs.
    */
   public PSNavRelationshipInfo(PSRelationship relation, IPSRequestContext req)
         throws PSNavException
   {
      m_rel = relation;
      m_owner = PSNavUtil.getItemSummary(req, m_rel.getOwner());
      m_dependent = PSNavUtil.getItemSummary(req, m_rel.getDependent());
   }

   /**
    * Gets the summary for the owner item.
    * 
    * @return the owner summary. Never <code>null</code>
    */
   public PSComponentSummary getOwner()
   {
      return m_owner;
   }

   /**
    * Gets the summary for the dependent item.
    * 
    * @return the dependent summary. Never <code>null</code>
    */
   public PSComponentSummary getDependent()
   {
      return m_dependent;
   }

   /**
    * Gets the underlying relationship object.
    * 
    * @return the relationships object.
    */
   public PSRelationship getRelation()
   {
      return m_rel;
   }

   /**
    * Produces a string suitable for logging.
    * @return
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("Relationship ");
      sb.append(m_rel.getConfig().getLabel());
      sb.append("\nOwner ");
      sb.append(m_owner.getName());
      if (m_owner.isFolder())
      {
         sb.append(" Folder ");
      }
      else
      {
         sb.append(" Content Type ");
         sb.append(m_owner.getContentTypeId());
      }
      sb.append("\nDependent ");
      sb.append(m_dependent.getName());
      if (m_dependent.isFolder())
      {
         sb.append(" Folder ");
      }
      else
      {
         sb.append(" Content Type ");
         sb.append(m_dependent.getContentTypeId());
      }
      return sb.toString();
   }

   /**
    * The underlying relationship.
    */
   private PSRelationship m_rel;

   /**
    * The component summary of the owner item.
    */
   private PSComponentSummary m_owner;

   /**
    * The component summary of the dependent item.
    */
   private PSComponentSummary m_dependent;

}
