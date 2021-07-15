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
package com.percussion.services.system.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single audit trail.
 */
public class PSAuditTrail implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -8037995317473435726L;
   
   private int currentRevision;
   
   private int editRevision;
   
   private List<PSAudit> audits = new ArrayList<>();

   /**
    * Default constructor.
    */
   public PSAuditTrail()
   {
   }
   
   /**
    * Get the current revision of the item for which this defines the audit
    * trail.
    * 
    * @return the current revision of the item for which this defines the 
    *    audit trail.
    */
   public int getCurrentRevision()
   {
      return currentRevision;
   }
   
   /**
    * Set the current revision of the item for which this defines the audit
    * trail.
    * 
    * @param currentRevision the new current revision of the item for which
    *    this defines the audit trail.
    */
   public void setCurrentRevision(int currentRevision)
   {
      this.currentRevision = currentRevision;
   }
   
   /**
    * Get the edit revision of the item for which this defines the audit
    * trail.
    * 
    * @return the edit revision of the item for which this defines the 
    *    audit trail.
    */
   public int getEditRevision()
   {
      return editRevision;
   }
   
   /**
    * Set the edit revision of the item for which this defines the audit
    * trail.
    * 
    * @param editRevision the new edit revision of the item for which
    *    this defines the audit trail.
    */
   public void setEditRevision(int editRevision)
   {
      this.editRevision = editRevision;
   }
   
   /**
    * Get the list with all audits.
    * 
    * @return the list with all audits, never <code>null</code>, may
    *    be empty.
    */
   public List<PSAudit> getAudits()
   {
      return audits;
   }
   
   /**
    * Set a new list of audits.
    * 
    * @param audits the new list of audits, may be <code>null</code> or
    *    empty.
    */
   public void setAudits(List<PSAudit> audits)
   {
      if (audits == null)
         this.audits = new ArrayList<>();
      else
         this.audits = audits;
   }
   
   /**
    * Add a new audit.
    * 
    * @param audit the new audit to add, not <code>null</code>.
    */
   public void addAudit(PSAudit audit)
   {
      if (audit == null)
         throw new IllegalArgumentException("audit cannot be null");
      
      audits.add(audit);
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

