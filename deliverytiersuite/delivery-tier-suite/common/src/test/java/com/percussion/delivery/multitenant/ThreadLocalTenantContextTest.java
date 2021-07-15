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

package com.percussion.delivery.multitenant;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ThreadLocalTenantContextTest extends TestCase 
{
	
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
			catch (InterruptedException e) {}			
		}
		
		try 
		{
			Thread.sleep(4);
		} 
		catch (InterruptedException e) {}
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
					System.out.println("Thread #: " + num + " key: " + key);
				} 
				catch (InterruptedException ignore){}
			}
		}
		
	}

}
