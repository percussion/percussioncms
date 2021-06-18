/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
