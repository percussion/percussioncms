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

package com.percussion.share.validation;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import com.percussion.share.validation.PSErrors.PSObjectError;

public class PSErrorsTest {
	
	  @Test
	  public void testSerialization() throws JAXBException{
		
		PSErrors ex = new PSErrors();
		PSObjectError e = new PSObjectError();
		
		e.setCode("TEST");
		e.setDefaultMessage("UNIT TEST");
		List<String> args = Arrays.asList("ARG1","ARG2");
		e.setArguments(args);
		PSErrorCause cause = new PSErrorCause();
		cause.setCause(new Throwable("TEST"));
		cause.setLocalizedMessage("TEST");
		cause.setMessage("TEST");
		e.setCause(cause);
		ex.setGlobalError(e);
		// Get a JAXB Context for the object we created above
		JAXBContext context = JAXBContext.newInstance(ex.getClass());
	    
		  // To convert ex to XML, I need a JAXB Marshaller
		Marshaller marshaller = context.createMarshaller();
	
		// Make the output pretty
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
	
		// marshall the object to XML
		 marshaller.marshal(ex, sw);
		
		// print it out for this example
		System.out.println(sw.toString());
		  
	  }

}
