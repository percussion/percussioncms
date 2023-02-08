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

package com.percussion.delivery.utils.spring;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractGenericContextLoader;
import org.springframework.util.ObjectUtils;

public class PSNonValidatingGenericXMLContextLoader extends AbstractGenericContextLoader {


    /**
     * Factory method for creating a new {@link BeanDefinitionReader} for loading
     * bean definitions into the supplied {@link GenericApplicationContext context}.
     *
     * @param context the context for which the {@code BeanDefinitionReader}
     *                should be created
     * @return a {@code BeanDefinitionReader} for the supplied context
     * @see #loadContext(String...)
     * @see #loadBeanDefinitions
     * @see BeanDefinitionReader
     * @since 2.5
     */
    @Override
    protected BeanDefinitionReader createBeanDefinitionReader(GenericApplicationContext context) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        reader.setValidating(false);
        reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        context.setAllowBeanDefinitionOverriding(true);
        context.setAllowCircularReferences(true);
        return reader;
    }

    /**
     * Get the suffix to append to {@link ApplicationContext} resource locations
     * when detecting default locations.
     * <p>Subclasses must provide an implementation of this method that returns
     * a single suffix. Alternatively subclasses may provide a  <em>no-op</em>
     * implementation of this method and override {@link #getResourceSuffixes()}
     * in order to provide multiple custom suffixes.
     *
     * @return the resource suffix; never {@code null} or empty
     * @see #generateDefaultLocations(Class)
     * @see #getResourceSuffixes()
     * @since 2.5
     */
    @Override
    protected String getResourceSuffix() {
        return "-context.xml";
    }

    /**
     * Ensure that the supplied {@link MergedContextConfiguration} does not
     * contain {@link MergedContextConfiguration#getClasses() classes}.
     * @since 4.0.4
     * @see AbstractGenericContextLoader#validateMergedContextConfiguration
     */
    @Override
    protected void validateMergedContextConfiguration(MergedContextConfiguration mergedConfig) {
        if (mergedConfig.hasClasses()) {
            String msg = String.format(
                    "Test class [%s] has been configured with @ContextConfiguration's 'classes' attribute %s, "
                            + "but %s does not support annotated classes.", mergedConfig.getTestClass().getName(),
                    ObjectUtils.nullSafeToString(mergedConfig.getClasses()), getClass().getSimpleName());
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
    }

}
