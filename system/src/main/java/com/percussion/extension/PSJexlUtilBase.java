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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * A base class for Jexl based extensions. Extend this class to ensure that
 * your JEXL extensions implement the correct interface.
 * 
 * @author dougrand
 */
public class PSJexlUtilBase implements IPSJexlExpression
{
   public static final String  VELOCITY_LOGGER="velocity";
   public static final String LOG_ERROR_DEFAULT="Error in $rx.pageutils.{}: {}";
   public  static final Logger log = LogManager.getLogger(VELOCITY_LOGGER);

   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // Do nothing
   }

}
