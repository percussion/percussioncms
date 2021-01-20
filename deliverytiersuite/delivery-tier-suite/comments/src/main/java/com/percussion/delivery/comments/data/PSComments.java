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
package com.percussion.delivery.comments.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A simple container. Its use is just to add
 * a root element name for Jersey to spit out when 
 * serializing to JSON.
 * @author erikserating
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "comments"
})
@XmlRootElement(name = "comments")
public class PSComments
{
   private List<IPSComment> comments;
   
   public PSComments()
   {
       comments = new ArrayList<IPSComment>();
   }
   
   /**
    * Constructor for the {@link PSComments} object.
    * 
    * @param comments. Never <code>null</code>. 
    */
   public PSComments(List<IPSComment> comments)
   {
       if(comments == null)
       {
           this.comments = new ArrayList<IPSComment>();
       }
       else
       {
           this.comments = comments;
       }
   }
   
   /**
    * 
    * @return the list of comments. Never <code>null</code>. 
    */
   public List<IPSComment> getComments()
   {
      return comments;
   }
}
