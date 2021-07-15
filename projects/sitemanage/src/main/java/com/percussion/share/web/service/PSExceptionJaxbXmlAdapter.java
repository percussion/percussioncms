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
package com.percussion.share.web.service;

import com.percussion.share.service.exception.PSErrorUtils;
import com.percussion.share.validation.PSErrors;
import org.springframework.stereotype.Component;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Converts {@link PSErrors} into Exceptions and vice versa for
 * JAXB serialization.
 * @author adamgent
 *
 */
@Provider
@Component
@Produces(MediaType.APPLICATION_JSON)
public class PSExceptionJaxbXmlAdapter extends XmlAdapter<PSErrors, Throwable>
{

    @Override
    public PSErrors marshal(Throwable throwable) throws Exception
    {
        if (throwable == null) return null;
        return PSErrorUtils.createErrorsFromException(throwable);
    }

    @Override
    public Throwable unmarshal(PSErrors errors) throws Exception
    {
        if (errors == null) return null;
        return PSErrorUtils.createExceptionFromErrors(errors);
    }

    
}

