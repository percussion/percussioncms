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

package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSValidationContext;
import com.percussion.design.objectstore.PSSystemValidationException;


/**
 * Implements some of the methods in the IPSComponent interface that don't
 * vary across all or many of the components that will be implemented in the
 * CMS layer.
 * <p>Derived classes must save and restore the id in their to/fromXml methods.
 * They may need to override clone() to fulfill the deep-copy contract of
 * IPSComponent.
 */
public abstract class PSCmsComponent implements IPSComponent
{
   /**
    * Creates an instance. Designed only for use as a base class.
    */
   protected PSCmsComponent()
   {}

   //see interface for description
   public Object clone()
   {
      PSCmsComponent copy = null;
      try
      {
         copy = (PSCmsComponent) super.clone();
      }
      catch (CloneNotSupportedException e) {} // cannot happen
      return copy;
   }


   //see interface for description
   public int getId()
   {
      return m_id;
   }

   //see interface for description
   public void setId(int id)
   {
      m_id = id;
   }

   /**
    * The CMS won't be using this initially.
    *
    * @throws PSSystemValidationException Never thrown.
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {}

   /**
    * A storage slot for users of components. Derived classes must re/store this
    * value in their to/from Xml methods. Defaults to 0.
    */
   private int m_id = 0;
}
