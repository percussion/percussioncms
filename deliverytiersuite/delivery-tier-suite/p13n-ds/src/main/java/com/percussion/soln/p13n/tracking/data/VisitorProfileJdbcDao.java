package com.percussion.soln.p13n.tracking.data;

import com.percussion.soln.p13n.tracking.IVisitorProfileDataService;
import com.percussion.soln.p13n.tracking.VisitorProfile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

/**
 * Standard JDBC DAO for visitor profiles.
 * @author adamgent
 *
 */
public class VisitorProfileJdbcDao extends JdbcDaoSupport implements IVisitorProfileDataService  {

    private static final String VISITOR_PROFILE_TABLE = "visitor_profile";
    private static final String VISITOR_PROFILE_WEIGHT_TABLE = "visitor_profile_weight";
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(VisitorProfileJdbcDao.class);
    
    public VisitorProfile createProfile() {
        return new VisitorProfile();
    }

    public VisitorProfile find(long visitorId) {
        JdbcTemplate t = getJdbcTemplate();
        try {
            VisitorProfile profile = t.queryForObject(
                    "select * from " + 
                    VISITOR_PROFILE_TABLE +
                    " where id=?", 
                visitorProfileMapper, visitorId);
            if (profile == null) return null;
            loadWeights(profile);
            return profile;
        } catch (EmptyResultDataAccessException e) {
            log.debug("Could not find profile: " + visitorId);
        }
        return null;
        
    }

    public VisitorProfile findByUserId(String userId) {
        if (userId == null) return null;
        VisitorProfile profile;
        JdbcTemplate t = getJdbcTemplate();
        List<Long> profileIds = t.query(
                "select id from " +
        		VISITOR_PROFILE_TABLE +
        		" where userid=?" +
        		" order by last_updated",
        		idMapper,
        		userId);
        if (profileIds.isEmpty()) {
            profile = null;
        }
        else if (profileIds.size() == 1){
            profile = find(profileIds.get(0));
        }
        else {
            log.warn("Found more than one profile for userId: " + userId);
            log.warn("Picking the last updated");
            profile = find(profileIds.get(0));
        }
        return profile;
    }

    public boolean hasProfile(long visitorId) {
        JdbcTemplate t = getJdbcTemplate();
        if (visitorId == 0) {
            throw new IllegalArgumentException("Visitor Id cannot be zero");
        }
        List<Map<String, Object>> profiles = 
            t.queryForList("select id from " +
            		VISITOR_PROFILE_TABLE +
            		" where id = ?", visitorId);
        return ! profiles.isEmpty();
    }
    
    public void delete(VisitorProfile profile) {
        deleteProfileWeight(profile.getId());
        getJdbcTemplate()
        .update("delete from " + VISITOR_PROFILE_TABLE + " where id = ?", profile.getId());
    }
    
    protected void deleteProfileWeight(long profileId) {
        boolean hasWeights = ! getJdbcTemplate()
            .queryForList("select id from " + 
                VISITOR_PROFILE_WEIGHT_TABLE + 
                " where id = ?", profileId).isEmpty();
        if (hasWeights) {
            getJdbcTemplate().update(
                    "delete from " + 
                    VISITOR_PROFILE_WEIGHT_TABLE +
                    " where id = ?", profileId);
        }
    }
    
    public boolean hasProfile(VisitorProfile profile) {
        if (profile == null) throw new IllegalArgumentException("Profile cannot be null");
        return hasProfile(profile.getId());
    }
    public VisitorProfile save(VisitorProfile profile) {
        //TODO handle duplicate user ids.
        JdbcTemplate t = getJdbcTemplate();
        String query;
        boolean insert = false;
        if (profile.getId() == 0) {
            insert = true;
            profile.setId(nextProfileId());
        }
        if (profile.getLastUpdated() == null) {
            profile.setLastUpdated(new Date());
        }
        Object[] params = new Object[] {
                profile.getLabel(),
                profile.getLastUpdated(),
                profile.isLockProfile(),
                profile.getUserId(),
                profile.getId()
        };
        int[] types = new int[] {
                Types.VARCHAR,
                Types.DATE,
                Types.INTEGER,
                Types.VARCHAR,
                Types.BIGINT
        };
        
        if ( ! insert && hasProfile(profile)) {
            query = "update " +
            		VISITOR_PROFILE_TABLE +
            		" set " +
            		"label = ?, " +
            		"last_updated = ?, " +
            		"lock_profile = ?, " +
            		"userid = ? " +
            		"where id = ?";
        }
        else {
            query = "insert into " +
            		VISITOR_PROFILE_TABLE +
            		" (label,last_updated,lock_profile,userid,id) " +
            		"values (?,?,?,?,?)";
        }
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL: " + query + " with params: " + asList(params));
        }
        getJdbcTemplate().update(query, params, types);
        deleteProfileWeight(profile.getId());
        query = "insert into " +
        		VISITOR_PROFILE_WEIGHT_TABLE +
        		" (id, segment_id, weight) " +
        		"values (?,?,?)";
        Map<String, Integer> weights = profile.copySegmentWeights();
        
        if (weights != null && ! weights.isEmpty()) {
            for (Entry<String, Integer> entry : weights.entrySet()) {
                String segId = entry.getKey();
                
                Object tempWeight = entry.getValue();
                Integer weight = Integer.parseInt(tempWeight.toString());
                
                if (segId == null) {
                    log.error(format("Bad entry segment id: {0} weight: {1}", segId,weight));
                }
                else {
                    t.update(query, profile.getId(), entry.getKey(), entry.getValue());
                }
            }
        }
        return profile;
    }
    
    private Map<String, Integer> retrieveWeights(long id) {
        JdbcTemplate t = getJdbcTemplate();
        Object[] p = new Object[] {id};
        final Map<String, Integer> w = new HashMap<>();
        t.query("select * from " +
        		VISITOR_PROFILE_WEIGHT_TABLE +
        		" where id=?", p, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                String segId = rs.getString("segment_id");
                if (segId != null) {
                	int weight = rs.getInt("weight");
                    w.put(segId, weight);
                }
            }
        });
        return w;
    }
    
    private void loadWeights(VisitorProfile profile) {
        profile.setSegmentWeights(retrieveWeights(profile.getId()));
    }
    
    private void loadWeights(Iterator<VisitorProfile> profiles) {
        if (profiles == null) return;
        while(profiles.hasNext()) {
            loadWeights(profiles.next());
        }
    }

    public Iterator<VisitorProfile> retrieveProfiles() {
        String sql = "select * from " +
        VISITOR_PROFILE_TABLE +
        " where label is not null";
        List<VisitorProfile> profiles = getJdbcTemplate().query(sql, visitorProfileMapper);
        loadWeights(profiles.iterator());
        return profiles.iterator();
    }

    public List<VisitorProfile> retrieveTestProfiles() {
        String sql = "select * from " +
            VISITOR_PROFILE_TABLE +
            " where label is not null " +
            " order by label";
        List<VisitorProfile> profiles = getJdbcTemplate().query(sql, visitorProfileMapper);
        loadWeights(profiles.iterator());
        return profiles;
    }
    
    private static final VisitorProfileRowMapper visitorProfileMapper = new VisitorProfileRowMapper();
    private static final IdRowMapper idMapper = new IdRowMapper();
    
    public static class IdRowMapper extends SingleColumnRowMapper<Long> {
        @Override
        public Long mapRow(ResultSet rs, int index) throws SQLException {
            return rs.getLong(1);
        }
    }
    
    public static class VisitorProfileRowMapper extends SingleColumnRowMapper<VisitorProfile> {

        @Override
        public VisitorProfile mapRow(ResultSet rs, int index)
                throws SQLException {
            VisitorProfile profile = new VisitorProfile();
            profile.setId(rs.getLong("id"));
            profile.setUserId(rs.getString("userid"));
            profile.setLabel(rs.getString("label"));
            profile.setLastUpdated(getDate("last_updated", rs));
            profile.setLockProfile(rs.getBoolean("lock_profile"));
            return profile;
        }
    
    }

    private static Date getDate(String column, ResultSet rs) throws SQLException {
        /*
         * java.sql.Date has problems so we must convert the date
         * to a java.util.Date.
         */
        Date d = rs.getDate(column);
        if (d == null) return null;
        return new Date(d.getTime());
    }
    
    private long nextProfileId() {
        return UUID.randomUUID().getMostSignificantBits();
    }
    
}