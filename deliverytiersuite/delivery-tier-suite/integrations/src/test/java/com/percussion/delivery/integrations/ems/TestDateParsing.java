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

package com.percussion.delivery.integrations.ems;

import junit.framework.TestCase;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;

import java.text.ParseException;

public class TestDateParsing extends TestCase{

	@Test
	public void testDateFormat() throws ParseException{
		
		// and format: yyyy-MM-dd'T'HH:mm:ss.SSSXXX
	
		System.out.println(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2018-05-21T10:42:00".replace("T"," ")));
	}
}
