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

package com.percussion.rest;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Indicates the permissions that the given subject / role can have to an object")
public enum Permissions {

    /**
     * ACL permission Read.
     */
    READ,
    /**
     * ACL permission Update.
     */
    UPDATE,
    /**
     * ACL permission Delete.
     */
    DELETE,
    /**
     * ACL permission Runtime Visibility.
     */
    RUNTIME_VISIBLE,
    /**
     * Owner of the ACL means access to modify the ACL
     */
    OWNER
}
