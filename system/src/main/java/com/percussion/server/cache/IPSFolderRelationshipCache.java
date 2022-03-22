package com.percussion.server.cache;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.utils.guid.IPSGuid;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

public interface IPSFolderRelationshipCache extends IPSNotificationListener {
    //Default number of levels / recursion
    int DEFAULT_MAX_RECURSION = 20;

    static IPSFolderRelationshipCache createInstance(){return null;};

    static IPSFolderRelationshipCache getInstance(){return null;}

    void reinitialize(boolean isEnabled) throws PSCacheException;

    void notifyEvent(PSNotificationEvent notify);

    PSItemSummaryCache getItemCache();

    List<PSLocator> getOwnerLocators(PSLocator itemLocator, String relationshipTypeName);

    String[] getParentPaths(PSLocator locator, String relationshipTypeName);

    List<PSLocator> getChildLocators(PSLocator locator);

    List<Integer> getChildIDs(Integer parentID);

    List<PSLocator> getParentLocators(PSLocator locator);

    void update(PSRelationshipSet relationships);

    PSLocator findChildOfType(PSLocator current, List<Long> types);

    void delete(PSRelationshipSet relationships);

    PSRelationship getRelationship(int rid) throws PSNotFoundException;

    List<PSRelationship> getChildren(PSLocator parent, PSRelationshipFilter filter);

    List<PSRelationship> getParents(PSLocator child);

    int getIdByPath(List<String> paths, String relationshipTypeName);

    List<IPSGuid> getFolderDescendants(IPSGuid parentGuid);

    Element getCacheStatistics(Document doc);

    void deleteOwnerRevisions(int ownerid, Collection<Integer> revisions);

    List<PSRelationship> getAaChildren(PSLocator parent, String slot);

    Collection<PSRelationship> getAAParents(boolean publicRev, boolean tip, boolean current, String slot, PSLocator child);
}
