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
       comments = new ArrayList<>();
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
           this.comments = new ArrayList<>();
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
