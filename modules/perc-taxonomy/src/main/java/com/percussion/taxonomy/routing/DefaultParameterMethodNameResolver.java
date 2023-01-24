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

package com.percussion.taxonomy.routing;

import javax.servlet.http.HttpServletRequest;

//@TODO: Update to use anotations
public class DefaultParameterMethodNameResolver {

    private String paramName, defaultMethod;

    public String getHandlerMethodName(HttpServletRequest request)
             {
        String name = request.getParameter(paramName);
        if (name == null || name.equals("")) {
            name = defaultMethod;
        }
        if (name == null) {
            //TODO: fix me
           // throw new NoSuchRequestHandlingMethodException(request);
        }
        return name;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setDefaultMethod(String defaultMethod) {
        this.defaultMethod = defaultMethod;
    }
}
