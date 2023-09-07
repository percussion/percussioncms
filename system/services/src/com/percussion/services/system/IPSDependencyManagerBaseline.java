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

package com.percussion.services.system;

import com.percussion.error.PSDeployException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;

import java.util.Iterator;
import java.util.List;

/**
 * The minimum interface a dependency manager can implement.
 */
public interface IPSDependencyManagerBaseline {

    void setIsDependencyCacheEnabled(boolean b);

    List<String> getDeploymentType(PSTypeEnum valueOf) throws PSDeployException;

    Iterator<IPSDependencyBaseline> getAncestors(PSSecurityToken tok, IPSDependencyBaseline dep)throws PSDeployException, PSNotFoundException;

    PSTypeEnum getGuidType(String objectType);

    IPSDependencyBaseline findDependency(PSSecurityToken tok, String depType, String depId) throws PSDeployException, PSNotFoundException;
}
