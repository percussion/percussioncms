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


   /**
    * Ctor
    * @param msgCode message code used to lookup message
    * @param cause the exception that triggered this one
    */
   public PSAssemblyException(int msgCode, Throwable cause) {
      super(msgCode,cause);
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
   public static final int PAGE_FAILED_TO_ASSEMBLE_REGION=24; //12=Failed to assemble region {}
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
