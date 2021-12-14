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
