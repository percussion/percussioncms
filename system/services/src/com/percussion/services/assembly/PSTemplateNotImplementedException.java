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
package com.percussion.services.assembly;

/**
 * Thrown if the configured assembly plugin can't handle the passed template
 * @author dougrand
 */
public class PSTemplateNotImplementedException extends Exception
{
   /**
    * 
    */
   private static final long serialVersionUID = 3257004375779063864L;

   /**
    * 
    */
   public PSTemplateNotImplementedException() {
      super();
      // TODO Auto-generated constructor stub
   }

   /**
    * @param message
    */
   public PSTemplateNotImplementedException(String message) {
      super(message);
      // TODO Auto-generated constructor stub
   }

   /**
    * @param cause
    */
   public PSTemplateNotImplementedException(Throwable cause) {
      super(cause);
      // TODO Auto-generated constructor stub
   }

   /**
    * @param message
    * @param cause
    */
   public PSTemplateNotImplementedException(String message, Throwable cause) {
      super(message, cause);
      // TODO Auto-generated constructor stub
   }

}
