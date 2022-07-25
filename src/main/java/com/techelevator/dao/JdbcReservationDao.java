package com.techelevator.dao;

import com.techelevator.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcReservationDao implements ReservationDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int createReservation(int siteId, String name, LocalDate fromDate, LocalDate toDate) {
        String sql =
                "INSERT INTO reservation (create_date, site_id, name, from_Date, to_Date) " +
                        "VALUES ((SELECT now()), ?,?,?,?) RETURNING reservation_id;";
        return jdbcTemplate.queryForObject(sql, Integer.class, siteId, name, fromDate, toDate);
    }

    @Override
    public List<Reservation> getUpcomingReservations(int parkId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql =
                "SELECT *  " +
                        "FROM reservation " +
                        "JOIN site USING (site_id) " +
                        "JOIN campground USING (campground) " +
                        "WHERE park_id = ? " +
                        "AND from_date BETWEEN (SELECT NOW()) " +
                        "AND (SELECT now() + INTERVAL '30 day') " +
                        "ORDER BY from_date;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

        while (results.next()) {
            reservations.add(mapRowToReservation(results));
        }

        return reservations;
    }

    private Reservation mapRowToReservation(SqlRowSet results) {
        Reservation r = new Reservation();
        r.setReservationId(results.getInt("reservation_id"));
        r.setSiteId(results.getInt("site_id"));
        r.setName(results.getString("name"));
        r.setFromDate(results.getDate("from_date").toLocalDate());
        r.setToDate(results.getDate("to_date").toLocalDate());
        r.setCreateDate(results.getDate("create_date").toLocalDate());
        return r;
    }


}
