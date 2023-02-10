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
