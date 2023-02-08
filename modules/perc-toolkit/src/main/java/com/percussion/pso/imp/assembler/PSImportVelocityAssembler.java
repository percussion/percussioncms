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
package com.percussion.pso.imp.assembler;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.impl.plugin.PSVelocityAssembler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/*
 * This class provides a validating assembler for Velocity based templates.
 * It uses the JTidy library and configuration options to validate or
 * re-form content based on the supplied tidy properties file. Located in 
 * @author NateChadwick
 */
/**
 */
public class PSImportVelocityAssembler extends PSVelocityAssembler
implements
IPSAssembler{

	/**
	 * Field m_def.
	 */
	private IPSExtensionDef m_def = null;
	
		/**
	    * Logger for this class
	    */
	   private static final Logger log = LogManager.getLogger(PSImportVelocityAssembler.class);
	   
	   
	   /**
	    * Constructor 
	    */
	   public PSImportVelocityAssembler()
	   {
	      super();
	   }

	   /**
	   
	    * @param item IPSAssemblyItem
	    * @return IPSAssemblyResult
	    * @throws Exception
	    * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#doAssembleSingle(IPSAssemblyItem) */
	   @Override
	   protected IPSAssemblyResult doAssembleSingle(IPSAssemblyItem item) throws Exception
	   {
	      IPSAssemblyResult result = super.doAssembleSingle(item); 
	      log.debug("Validating Velocity Content Assembler"); 
	      return ImportContentAssemblerMerge.merge(m_def,result);   
	   }

	 /**
	
	 * @param arg0 IPSExtensionDef
	  * @param arg1 File
	  * @throws PSExtensionException
	  * @see com.percussion.services.assembly.impl.plugin.PSAssemblerBase#doAssembleSingle(IPSAssemblyItem) */
	@Override
	public void init(IPSExtensionDef arg0, File arg1)
			throws PSExtensionException {
		m_def = arg0.clone();
		super.init(arg0, arg1);
	}

	   
	   
}
