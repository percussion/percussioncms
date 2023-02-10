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

package com.percussion.soln.p13n.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringSetup {
    static ApplicationContext context;
    
    public synchronized static void loadXmlBeanFiles(String... files) {
        context = new FileSystemXmlApplicationContext(files);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> beanType) {
        if (context == null) throw new IllegalStateException("Context is not loaded. Use loadXmlBeanFiles first.");
        return (T) context.getBean(name);
    }
    
    public static void destroyContext() {
        context = null;
    }

}
