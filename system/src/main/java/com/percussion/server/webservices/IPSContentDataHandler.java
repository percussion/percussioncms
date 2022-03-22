package com.percussion.server.webservices;

import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import org.w3c.dom.Element;

import java.util.List;

public interface IPSContentDataHandler extends IPSPortActionHandler {
    /**
     * The resource path used to purge content items.
     */
    String PURGE_PATH = "sys_cxSupport/purgecontent.html";

    static void purgeItems(PSRequest request, List<String> itemIds){}

    void newCopy(PSRequest request) throws PSException;

    PSServerItem updateItem(
            PSRequest request,
            Element item,
            PSLocator loc,
            long typeId)
            throws PSException;

    void processInsertItem(PSRequest request, String folderContenttype, Element toXml) throws PSException;
}
