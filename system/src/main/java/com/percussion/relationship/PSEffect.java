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
package com.percussion.relationship;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;

import java.io.File;
/**
 * All effect implementations should derive from this base class and not directly
 * implement the <code>IPSEffect</code> interface. This class will  provide
 * generic functionality useful in all effect implementations.
 */
public abstract class PSEffect implements IPSEffect
{
   /**
    * Saves references to the provided extension definition and code root,
    * which might be of use in the effect implementation.
    *
    * See <code>IPSExtension</code> for description.
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      if (def == null || codeRoot == null)
         throw new IllegalArgumentException("def and codeRoot cannot be null");

      m_def.set(def);
      m_codeRoot.set(codeRoot);
      m_name = def.getRef().toString();
   }

   /**
    * Get the extension definition.
    *
    * @return the extension definition, never <code>null</code>.
    */
   public IPSExtensionDef getExtensionDef()
   {
      return (IPSExtensionDef) m_def.get();
   }

   /**
    * Get the extension code root.
    *
    * @return the extension code root, never <code>null</code>.
    */
   public File getCodeRoot()
   {
      return (File) m_codeRoot.get();
   }

   /**
    * Derived class must implement this method. See interface for more details
    * of the method.
    */
   public abstract void test(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException;
   /**
    * Derived class must implement this method. See intreface for more details
    * of the method.
    */
   public abstract void attempt(Object[] prams, IPSRequestContext request,
      IPSExecutionContext context, PSEffectResult result)
      throws PSExtensionProcessingException, PSParameterMismatchException;

   /**
    * Derived class must implement this method. See intreface for more details
    * of the method.
    */
   public abstract void recover(Object[] params, IPSRequestContext request,
      IPSExecutionContext context, PSExtensionProcessingException e,
      PSEffectResult result)
      throws PSExtensionProcessingException;

   /**
     * This holds the definition for this extension, initialized in
    * {#link init(IPSExtensionDef, File)}, never changed or <code>null</code>
    * after that.
    */
   private ThreadLocal m_def = new ThreadLocal();

   /**
    * This holds the 'root' directory for this extension. When installed, all
    * files are installed relative to this location. Files can be loaded from
    * anywhere under this directory and no where else (by default, the actual
    * security policy may vary). This object could be used to load a property
    * file when executing the Effect. Initialized in
    * {#link init(IPSExtensionDef, File)}, never changed or <code>null</code>
    * after that.
    */
   private ThreadLocal m_codeRoot = new ThreadLocal();

   /**
    * Name of the effect as registered. Initialized in the init() method,
    * never <code>null</code> or empty after that.
    */
   protected String m_name = "";

}
