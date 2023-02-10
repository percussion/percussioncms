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

package com.percussion.testing;

import com.percussion.utils.annotations.IgnoreInWebAppSpringContext;

/** @Configuration
@ImportResource({        "classpath:/com/percussion/testing/test-spring-context.xml",
"file:../modules/perc-distribution-tree/target/distribution/jetty/base/webapps/Rhythmyx/WEB-INF/config/spring/" + PSServletUtils.BEANS_FILE_NAME,
"file:../modules/perc-distribution-tree/target/distribution/jetty/base/webapps/Rhythmyx/WEB-INF/config/spring/" + PSServletUtils.DESIGN_BEANS_FILE_NAME
})
 */
@IgnoreInWebAppSpringContext
public class PSSpringContextTestConfig {

}
