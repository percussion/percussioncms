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

package com.percussion.soln.linkback.servlet;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Locale;

public class NamespacedInternalResourceViewResolver extends InternalResourceViewResolver {

    private String m_namespace;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.servlet.view.UrlBasedViewResolver#loadView(java
     * .lang.String, java.util.Locale)
     */
    @Override
    protected View loadView(String viewName, Locale locale) throws Exception {
        if (m_namespace == null)
            throw new IllegalStateException("namespace must be assigned");

        // only handle requests whose view name is prefixed with a specific
        // namespace
        if (viewName.startsWith(m_namespace)) {
            return super.loadView(viewName.substring(m_namespace.length()), locale);
        }
        return null;
    }

    /**
     * @return the namespace
     */
    public String getNamespace() {
        return m_namespace;
    }

    /**
     * @param namespace
     *            the namespace to set
     */
    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }
}
