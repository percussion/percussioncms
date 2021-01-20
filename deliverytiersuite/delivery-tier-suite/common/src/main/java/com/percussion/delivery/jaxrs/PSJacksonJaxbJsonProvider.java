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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.jaxrs;

import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

@Provider  
public class PSJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider
{
    // We Need to override org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper and org.codehaus.jackson.jaxrs.JsonParseExceptionMapper
    // These are included in the package we scan from com.sun.jersey.config.property.packages in web.xml
    // We still want the JacksonJaxbJsonProvider so we just extend it and find it here.  we have some more options when we upgrade
    // Jax-rs from 1.1 to 2.0
    // See discussion here https://github.com/fasterxml/jackson-jaxrs-providers/issues/22
    
}
