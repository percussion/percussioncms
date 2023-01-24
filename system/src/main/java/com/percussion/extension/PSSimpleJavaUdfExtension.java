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

import java.io.File;

/**
 * A simple abstract class that has a default implementation for the
 * IPSExtension interface methods. By default save the parameters passed in the
 * init method, making them available to derived classes via protected access.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.1
 */
public abstract class PSSimpleJavaUdfExtension implements IPSUdfProcessor
{
   /**
    * This is a default implementation that does nothing except save the input
    * values in protected local storage for use by derived classes.
    * <p>
    * See {@link IPSExtension#init(IPSExtensionDef, File) init} for details.
    */
   public void init( IPSExtensionDef def, File codeRoot )
      throws PSExtensionException
   {}

   /**
    * This is the definition for this extension. You may want to use it for
    * validation purposes in the <code>processUdf</code> method.
    */
   protected IPSExtensionDef m_def;

   /**
    * This value contains the 'root' directory for this extension. When
    * installed, all files are installed relative to this location. Files can
    * be loaded from anywhere under this directory and no where else (by
    * default, the actual security policy may vary). This object could be used
    * to load a property file when executing the UDF.
    */
   protected File m_codeRoot;
}
