package com.percussion.recent.dao;

import com.percussion.recent.data.PSRecent;
import com.percussion.share.dao.IPSGenericDao;

import java.util.List;

public interface IPSRecentDao {

      List<PSRecent> find(String user, String siteName, PSRecent.RecentType type);
      void saveAll(List<PSRecent> recentList);
      void delete(PSRecent recent);
      void deleteAll(List<PSRecent> recentList);
      void save(PSRecent recent) throws IPSGenericDao.SaveException;

}
