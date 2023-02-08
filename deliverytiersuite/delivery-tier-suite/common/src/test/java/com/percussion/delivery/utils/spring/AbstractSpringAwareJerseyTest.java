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

package com.percussion.delivery.utils.spring;

import org.glassfish.jersey.test.JerseyTest;

/**
 * Test class which will wire itelf into your the Spring context which
 * is configured on the WebAppDecriptor built for your tests.
 * Ensure you configure annotation-aware support into your contexts,
 * and annotate any auto-wire properties on your test class
 * @author George McIntosh
 *
 */
public abstract class AbstractSpringAwareJerseyTest extends JerseyTest {

	 /***
     * Override the port. 
     */
 //   @Override
    protected int getPort(int port){
       return 10178; 
    }
	
//	public AbstractSpringAwareJerseyTest(WebAppDescriptor wad) {
//		super(wad);
//	}
	
//	protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
//		return new SpringAwareGrizzlyTestContainerFactory(this);
//	}
	
}
