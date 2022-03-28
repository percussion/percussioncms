package com.percussion.services.assembly.impl.finder;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.utils.guid.IPSGuid;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.Set;

public interface IPSAutoFinderUtils {

    Set<PSContentFinderBase.ContentItem> getContentItems(IPSAssemblyItem sourceItem,
                                                         long slotId, Map<String, Object> params, IPSGuid templateId) throws PSSiteManagerException, PSNotFoundException, RepositoryException;
}
