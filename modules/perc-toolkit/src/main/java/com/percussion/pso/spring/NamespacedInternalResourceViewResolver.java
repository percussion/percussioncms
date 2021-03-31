/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.spring;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

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
