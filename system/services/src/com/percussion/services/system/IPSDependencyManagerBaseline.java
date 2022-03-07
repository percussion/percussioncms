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

    List<String> getDeploymentType(PSTypeEnum valueOf);

    Iterator<IPSDependencyBaseline> getAncestors(PSSecurityToken tok, IPSDependencyBaseline dep)throws PSDeployException, PSNotFoundException;

    PSTypeEnum getGuidType(String objectType);

    IPSDependencyBaseline findDependency(PSSecurityToken tok, String depType, String depId) throws PSDeployException, PSNotFoundException;
}
