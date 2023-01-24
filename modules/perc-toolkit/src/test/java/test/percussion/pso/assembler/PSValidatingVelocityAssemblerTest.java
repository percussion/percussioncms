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
package test.percussion.pso.assembler;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import junit.framework.Assert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;


/**
 * @author natechadwick
 *
 */
public class PSValidatingVelocityAssemblerTest {

	Mockery context = new JUnit4Mockery();
	IPSAssemblyItem item;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		item = context.mock(IPSAssemblyItem.class);
	}

	@Test
	/**
	 * Test that a clean XHTML file is echo'd clean.
	 */
	public void XHTMLCleanTest() throws Exception{
	}
	
	@Test
	/**
	 * Test that a broken XHTML file is echo'd as is if No Clean and Force Publish are set.
	 */
	public void XHTMLForcePublishNoCleanTest() throws Exception{

	}
	
	
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

}
