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
package com.percussion.extension;

import com.percussion.design.objectstore.PSNotFoundException;

/**
 * The handler for Java script functions. Takes a body of text that is
 * pre-compiled and stored as an executable object in a 3rd party JS engine.
 */
public class PSJavaScriptExtensionHandler extends PSExtensionHandler
{
   /**
    * Returns the name of this extension handler.
    *
    * @return The name of this handler. Never <code>null</code>.
    */
   public String getName()
   {
      return "JavaScript";
   }

   /**
    * Get the extension based on the extension reference.
    *
    * See {@link PSExtensionHandler#loadExtension(PSExtensionRef)
    * loadExtension} in the super class for a description.
    *
    * @return a PSJavaScriptUdfExtension object that is ready to be executed.
    */
   protected IPSExtension loadExtension(PSExtensionRef ref)
      throws PSExtensionException, PSNotFoundException
   {
      return prepare( getExtensionDef( ref ));
   }

   /**
    * See {@link IPSExtensionHandler#prepare(PSExtensionRef) prepare} in the
    * interface for a description.
    *
    * @return a PSJavaScriptUdfExtension object that is ready to be executed.
    */
   public IPSExtension prepare( IPSExtensionDef def )
      throws PSNotFoundException, PSExtensionException
   {
      if (def == null)
         throw new IllegalArgumentException(
            "prepare: extension definition can't be null");

      PSJavaScriptUdfExtension ext = new PSJavaScriptUdfExtension();
      ext.init( def, getCodeBase(def) );
      return ext;
   }
}
