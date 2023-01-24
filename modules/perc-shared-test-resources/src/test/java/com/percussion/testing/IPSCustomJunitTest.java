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

/**
 * This is purely a marker class. If a JUnit test does not fall into any of 
 * the existing categories, then it should implement this interface. All
 * classes implementing this interface will not be executed in the nightly
 * suite. 
 * <p>An example of why this might be required is a unit test for a daemon 
 * class. The unit test would require the daemon to be running but has no
 * dependencies on the Rx server.
 *
 * @author paulhoward
 */
public interface IPSCustomJunitTest 
{
}
