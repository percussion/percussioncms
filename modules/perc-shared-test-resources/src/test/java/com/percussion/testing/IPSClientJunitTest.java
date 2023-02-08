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
 * Marker interface indicating that a test case requires a Rhythmyx server
 * to run and it is invoked as a remote client (of the Rhythmyx server).
 * In contrast to {@link IPSClientBasedJunitTest} this test does not need
 * assistance in obtaining a server connection.
 */
public interface IPSClientJunitTest
{
}
