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

package com.percussion.services.filestorage.data;

import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
/**
 * Enitity to store the actual binary content for the item
 * @author stephenbolton
 *
 */
@Entity
@Table(name="PSX_BINARYDATA")
public class PSBinaryData implements Serializable
{
   
   private static Logger ms_logger = Logger.getLogger(PSBinaryData.class);
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
 
   
   @Id  
   @Column(name="ID", nullable = false)  
   @GeneratedValue(generator="gen")  
   @GenericGenerator(name="gen", strategy="foreign",   
   parameters=@Parameter(name="property", value="binary")) 
   int id;
   
   @OneToOne  
   @PrimaryKeyJoinColumn  
   private PSBinary binary; 
   
   @Version
   @Column(name = "version")
   private Integer version;
   
   
   
   public Integer getVersion()
   {
      return version;
   }


   public void setVersion(Integer version)
   {
      this.version = version;
   }

   
   public PSBinary getBinary()
   {
      return binary;
   }

   public void setBinary(PSBinary binary)
   {
      this.binary = binary;
   }
   @Lob
   @Column(name = "CONTENT", nullable = false)
   private Blob content;
   
   public PSBinaryData() {
      
   }
  
   public PSBinaryData(Blob blob) {
      this.content = blob;
   }
   
   public int getId()
   {
      return id;
   }
   public void setId(int id)
   {
      this.id = id;
   }
   
   /**
    * @return an input stream to the Blob content.  May be
    * <code>null</code>.
    */
   public InputStream getContent()
   {
      try
      {
         if (content == null)
            return null;
         
         return content.getBinaryStream();
      }
      catch (Exception e)
      {
         String errorMsg = "Failed to get binary strean";
         ms_logger.error(errorMsg, e);
         throw new RuntimeException(errorMsg, e);         
      }
   }

   
   
   /**
    * @param content  InputStream for the content not <code>null</code>
    */
   public void setContent(Blob content)
   {
      this.content = content;
   }
   
   
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((content == null) ? 0 : content.hashCode());
      result = prime * result + id;
      return result;
   }
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PSBinaryData other = (PSBinaryData) obj;
      if (content == null)
      {
         if (other.content != null)
            return false;
      }
      else if (!content.equals(other.content))
         return false;
      if (id != other.id)
         return false;
      return true;
   }
   
   
}
