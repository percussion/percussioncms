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
package com.percussion.delivery.utils;

import com.percussion.delivery.email.data.PSEmailRequest;
import com.percussion.delivery.exceptions.PSEmailException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author natechadwick
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:beans.xml"})
public class PSEmailHelperTest extends TestCase {

	private static String BCC_LIST="a@a.com,b@b.com";
	private static String CC_LIST = "c@c.com,d@d.com";
	private static String TO_LIST = "e@e.com,f@f.com,j@j.com";
	private static String BODY="Test Body";
	private static String SUBJECT="Test Subject";
	
	@Autowired 
	IPSEmailHelper emailHelper;
	
	@Test
	public void testCreate() throws PSEmailServiceNotInitializedException {
PSEmailRequest r = new PSEmailRequest();
		
		r.setBCCList(BCC_LIST);
		r.setCCList(CC_LIST);
		r.setToList(TO_LIST);
		r.setBody(BODY);
		r.setSubject(SUBJECT);
	
		Assert.assertEquals(BCC_LIST, r.getBCCList());
		Assert.assertEquals(CC_LIST, r.getCCList());
		Assert.assertEquals(TO_LIST, r.getToList());
		Assert.assertEquals(BODY, r.getBody());
		Assert.assertEquals(SUBJECT,r.getSubject());
		
		try{
			this.emailHelper.sendMail(r);
		} catch (PSEmailException e) {
			Assert.assertTrue("Google Send Should Have Failed",e.getMessage().contains("smtp.gmail.com"));					
		}
	}
}
