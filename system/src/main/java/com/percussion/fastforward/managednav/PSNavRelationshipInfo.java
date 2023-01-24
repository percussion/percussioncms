/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
      StringBuilder sb = new StringBuilder();
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
