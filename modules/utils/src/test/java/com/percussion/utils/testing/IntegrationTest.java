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

package com.percussion.utils.testing;

/**
 * Add an annotation to a test to mark it as an integration test for Maven
 * @Category(IntegrationTest.class)
 * 
 * add to surefire plugin
 * <excludedGroups>com.percussion.utils.testing.IntegrationTest</excludedGroups>
 * 
 * add to failsafe plugin
 * 
 * <groups>com.percussion.utils.testing.IntegrationTest</groups>
 * 
 * @author stephenbolton
 *
 */
public interface IntegrationTest
{

}
