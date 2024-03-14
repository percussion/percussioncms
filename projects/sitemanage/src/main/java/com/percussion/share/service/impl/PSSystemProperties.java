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
package com.percussion.share.service.impl;

import com.percussion.server.PSServer;
import com.percussion.share.service.IPSSystemProperties;

import com.percussion.util.PSSiteManageBean;
import org.apache.commons.lang.Validate;

import javax.ws.rs.ext.Provider;

/**
 * @author JaySeletz
 *
 */
@Provider
@PSSiteManageBean("psSystemProperties")
public class PSSystemProperties implements IPSSystemProperties
{

    @Override
    public String getProperty(String name)
    {
        Validate.notEmpty(name);
        return PSServer.getServerProps().getProperty(name);
    }

}
