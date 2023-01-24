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
package com.percussion.security;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * A directory cataloger using the backend role/subject data as directory 
 * source.
 */
@SuppressWarnings(value={"unchecked"})
public class PSBackEndDirectoryCataloger extends PSDirectoryCataloger
{
   /**
    * Default ctor, sets the name of this cataloger.
    */
   public PSBackEndDirectoryCataloger()
   {
      super();
      setName(DEFAULT_NAME);
   }
   
   /**
    * Calls {@link #PSBackEndDirectoryCataloger()}, ignores params.
    */
   public PSBackEndDirectoryCataloger(Properties properties, 
      PSServerConfiguration config)
   {
      this();
   }   
   
   // see IPSDirectoryCataloger interface
   public String getAttribute(PSSubject user, String attributeName)
   {
      String attributeVal = null;
      Collection<String> attrs = new ArrayList<>(1);
      attrs.add(attributeName);
      PSSubject subject = getAttributes(user, attrs);
      PSAttribute attr = subject.getAttributes().getAttribute(
         attributeName);
      if (attr != null)
      {
         List vals = attr.getValues();
         if (!vals.isEmpty())
            attributeVal = (String) vals.get(0);
      }
      
      return attributeVal;
   }


   // see IPSDirectoryCataloger interface
   public PSSubject getAttributes(PSSubject user)
   {
      return getAttributes(user, null);
   }

   // see IPSDirectoryCataloger interface
   public PSSubject getAttributes(PSSubject user, Collection attributeNames)
   {
      // 1st clear all existing attributes from the supplied subject
      PSAttributeList userAttributes = user.getAttributes();
      while (userAttributes.size() > 0)
         userAttributes.removeElementAt(0);
      
      if (attributeNames != null)
      {
         Iterator attrs = attributeNames.iterator();
         while (attrs.hasNext())
            userAttributes.setAttribute((String) attrs.next(), null);
      }
      
      PSSubject subject = getMatchingSubject(user.getName(), attributeNames);
      if (subject != null)
      {
         Iterator attrs = subject.getAttributes().iterator();
         while (attrs.hasNext())
         {
            PSAttribute attr = (PSAttribute) attrs.next();
            userAttributes.setAttribute(attr.getName(), attr.getValues());
         }
      }

      return user;
   }

   // see IPSDirectoryCataloger interface   
   public Collection findUsers(PSConditional[] criteria, 
      Collection attributeNames)
   {
      Collection users = new HashSet();
      if (criteria == null || criteria.length == 0)
         criteria = new PSConditional[] {null};
      for (int i = 0; i < criteria.length; i++)
      {
         users.addAll(PSBackendCataloger.getSubjects(
            (HashMap)createFilter(criteria[i]), attributeNames));
      }
      
      return users;
   }

   // see IPSDirectoryCataloger interface
   public String getObjectAttributeName()
   {
      return PSBackendCataloger.FILTER_SUBJECT_NAME;
   }
   
   // see IPSDirectoryCataloger interface 
   public String getCatalogerType()
   {
      return "backend";
   }

   // see IPSDirectoryCataloger interface 
   public String getCatalogerDisplayType()
   {
      // TODO Auto-generated method stub
      return "Rhythmyx Back-end Cataloger";
   }   

   /**
    * Loads the subjects with the same name as the supplied subject and returns
    * the first one found.
    * 
    * @param name The subject name to match, assumed not <code>null</code> or 
    * empty.
    * @param attributeNames Attributes to return with the subject, may be 
    * <code>null</code> or empty.
    * 
    * @return The subject, or <code>null</code> if not found.
    */
   private PSSubject getMatchingSubject(String name, Collection attributeNames)
   {
      HashMap<String, String> filters = new HashMap<>();
      filters.put(PSBackendCataloger.FILTER_SUBJECT_NAME, name);
      Set subjects = PSBackendCataloger.getSubjects(filters, attributeNames);
      if (subjects.isEmpty())
         return null;
      else
         return (PSSubject) subjects.iterator().next();
   }
   
   /**
    * Constant for the default name of this cataloger
    */
   private static final String DEFAULT_NAME = "Default";   
}
