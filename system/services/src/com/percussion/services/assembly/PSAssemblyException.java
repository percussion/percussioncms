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

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Generic problem in assembly, the message will indicate what issue occurred.
 * 
 * @author dougrand
 */
public class PSAssemblyException extends PSBaseException
{
   /**
    * 
    */
   private static final long serialVersionUID = 3256726182123680309L;

   /**
    * Ctor
    * @param msgCode message code used to lookup message
    * @param arrayArgs arguments for message, may be <code>null</code>
    */
   public PSAssemblyException(int msgCode, Object... arrayArgs) {
      super(msgCode, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode message code used to lookup message
    * @param cause original exception, may be <code>null</code>
    * @param arrayArgs arguments for message, may be <code>null</code>
    */
   public PSAssemblyException(int msgCode, Throwable cause, Object... arrayArgs) {
      super(msgCode, cause, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode message code used to lookup message
    */
   public PSAssemblyException(int msgCode) {
      super(msgCode);
   }

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.assembly.PSAssemblyErrorStringBundle";
   }

   public static final int TEMPLATE_NOT_FOUND=1; //The template specified by {0} was not found
   public static final int ASSEMBLER_NOT_FOUND=2; //The assembler {0} was not found. Check the registrations in the extensions manager.
   public static final int ASSEMBLER_NOT_INSTANTIATED=3; //The assembler {0} could not be instantiated.
   public static final int TEMPLATE_NAME_REQUIRED=4; //You must specify either the variantid or the template name
   public static final int UNEXPECTED_ASSEMBLY_ERROR=5; //Unexpected exception while assembling one or more items
   public static final int AUTHTYPE_OR_FILTER_REQUIRED=6; //You must specify either the authtype or the item filter name
   public static final int SLOT_NOT_FOUND=11;//11=The slot specified by {0} was not found
/*7=An item must be identified by either sys_contentid/sys_revision parameters, guid, or sys_path.
8=The path {0} is invalid. Paths must contain at least one slash. Paths always start with a slash.
        9=The path {0} cannot be found
10=An unknown problem occurred while performing a CRUD operation

12=The finder {0} could not be loaded by the extensions manager or it could not be instantiated
13=An error occurred while creating an assembly item
14=Could not assemble snippet for landing page url. Item: {0}
15=The template {0} does not bind the required variable $pagelink
16=The template specified by {0} and content type id {1} was not found
17=Could not locate the default template for content at path {0} with content type {1} because {2}
18=The {1} specified by the name {0} returned more than one object, the name must be unique
19=While running the finder {0}, an exception occurred. The message was: {1}
20=The id {0} does not match the sys_contentid parameter {1}.
        21=The folder {0} does not match the sys_folderid parameter {1}.
        22=Unable to create resource instance for assembly item with id {0}
23=Unable to create render link context for assembly item with id {0}
   public static final int SLOT_NOT_FOUND=11;
*/

}
