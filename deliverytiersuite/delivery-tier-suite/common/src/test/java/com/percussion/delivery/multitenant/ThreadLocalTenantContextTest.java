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

package com.percussion.delivery.multitenant;

import com.percussion.error.PSExceptionUtils;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalTenantContextTest extends TestCase 
{
	private static final Logger log = LogManager.getLogger(ThreadLocalTenantContextTest.class);
	@Test
	public void testMultipleThreads()
	{
		List<ThreadLocalRunner> runners = new ArrayList<ThreadLocalRunner>();
		for(int i = 0; i < 10; i++)
		{
			ThreadLocalRunner runner = new ThreadLocalRunner(i + 1, "key_" + (i + 1));
			runner.start();
			try 
			{
				Thread.sleep(3);
			} 
			catch (InterruptedException e) {
				log.error(PSExceptionUtils.getMessageForLog(e));
				log.debug(PSExceptionUtils.getDebugMessageForLog(e));
			}
		}
		
		try 
		{
			Thread.sleep(4);
		} 
		catch (InterruptedException e) {
			log.error(PSExceptionUtils.getMessageForLog(e));
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
		}
		for(ThreadLocalRunner r : runners)
			r.deactivate();
	}
	
	
	class ThreadLocalRunner extends Thread
	{

		private String key;
		private int num;
		private boolean active = true;
		
		public ThreadLocalRunner(int num, String key)
		{
			this.key = key;
			this.num = num;
		}
		
		public void deactivate()
		{
			active = false;
		}
		
		@Override
		public void run()
		{
			PSThreadLocalTenantContext.setTenantId(key);
			while(active)
			{
				try 
				{
					Thread.sleep(5 * 1000);
					log.info("Thread #: " + num + " key: " + key);
				} 
				catch (InterruptedException ignore){
					log.error(PSExceptionUtils.getMessageForLog(ignore));
					log.debug(ignore);
					Thread.currentThread().interrupt();
				}
			}
		}
		
	}

}
