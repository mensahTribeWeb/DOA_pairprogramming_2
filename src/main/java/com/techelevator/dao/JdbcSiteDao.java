package com.techelevator.dao;

import com.techelevator.model.Site;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class JdbcSiteDao implements SiteDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcSiteDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Site> getSitesThatAllowRVs(int parkId) {
        List<Site> sites = new ArrayList<>();

        String sql =
                "SELECT * " +
                        "FROM site " +
                        "JOIN campground USING (campground_id ) " +
                        "WHERE max_rv_length > 0 " +
                        "AND park_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, parkId);

        while (results.next()) {
            sites.add(mapRowToSite(results));
        }
        return sites;
    }

    @Override
    public List<Site> getAvailableSites(int parkId) {
        List<Site> sites = new ArrayList<>();
        String sql =
                "SELECT campground.name, site_id, accessible, campground_id, max_occupancy, max_rv_length, site_number, utilities " +
                        "FROM site  " +
                        "LEFT JOIN reservation USING (site_id)  " +
                        "JOIN campground USING (campground_id)  " +
                        "WHERE park_id = ? " +
                        "AND (from_date > (SELECT NOW() + INTERVAL '1 day'))  " +
                        "OR to_date < (SELECT NOW() - INTERVAL '1 day'))  " +
                        "ORDER BY from_date;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, parkId);


        return sites;
    }



    private Site mapRowToSite(SqlRowSet results) {
        Site site = new Site();
        site.setSiteId(results.getInt("site_id"));
        site.setCampgroundId(results.getInt("campground_id"));
        site.setSiteNumber(results.getInt("site_number"));
        site.setMaxOccupancy(results.getInt("max_occupancy"));
        site.setAccessible(results.getBoolean("accessible"));
        site.setMaxRvLength(results.getInt("max_rv_length"));
        site.setUtilities(results.getBoolean("utilities"));
        return site;
    }
}
