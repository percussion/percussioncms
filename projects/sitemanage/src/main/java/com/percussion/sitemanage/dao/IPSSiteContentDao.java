package com.percussion.sitemanage.dao;

import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteSummary;

public interface IPSSiteContentDao {
    /**
     * The name of the home page item created automatically during site creation.
     */
    String HOME_PAGE_NAME = "index.html";

    void createRelatedItems(PSSite site);

    void copy(PSSite srcSite, PSSite destSite);

    PSPage getHomePage(PSSiteSummary site) throws PSNavException, PSDataServiceException;

    String getNavTitle(PSSiteSummary siteSummary) throws PSNavException, PSDataServiceException;

    void loadTemplateInfo(PSSite site) throws PSDataServiceException;

    void deleteRelatedItems(PSSiteSummary site) throws IPSGenericDao.DeleteException;
}
