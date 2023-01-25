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

package com.percussion.ant.install;

import com.percussion.install.InstallUtil;


/**
 * PSBrandProduct will brand the product with the brand code in the
 * installers property file.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="brandProduct"
 *              class="com.percussion.ant.install.PSBrandProduct"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to brand the product.
 *
 *  <code>
 *  &lt;brandProduct/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSBrandProduct extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      InstallUtil.brandProduct(getRootDir());
   }
}
