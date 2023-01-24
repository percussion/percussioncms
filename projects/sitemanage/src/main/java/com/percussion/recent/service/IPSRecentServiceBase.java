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

package com.percussion.recent.service;

import com.percussion.recent.data.PSRecent.RecentType;

import java.util.List;

public interface IPSRecentServiceBase
{
    List<String> findRecent(String user, String siteName, RecentType type);

    void addRecent(String user, String siteName, RecentType type, String value);

    void deleteRecent(String user, String siteName, RecentType type);

    void deleteRecent(String user, String siteName, RecentType type, List<String> toDelete);

    void renameSiteRecent(String oldSiteName, String newSiteName);
}
