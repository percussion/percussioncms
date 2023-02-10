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
package com.percussion.extension;

import com.percussion.error.PSNotFoundException;

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
