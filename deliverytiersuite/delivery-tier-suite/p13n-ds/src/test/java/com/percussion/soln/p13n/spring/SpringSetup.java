/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
