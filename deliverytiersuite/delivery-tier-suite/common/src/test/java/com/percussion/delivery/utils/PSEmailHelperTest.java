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
package com.percussion.delivery.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.mail.EmailException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.percussion.delivery.email.data.PSEmailRequest;

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
		} catch (EmailException e) {
			Assert.assertTrue("Google Send Should Have Failed",e.getMessage().contains("smtp.gmail.com"));					
		}finally{}
	}
}
