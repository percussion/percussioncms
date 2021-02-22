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
