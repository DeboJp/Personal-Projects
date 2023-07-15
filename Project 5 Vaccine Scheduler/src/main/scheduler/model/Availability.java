package scheduler.model;

import scheduler.db.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sun.source.tree.IfTree;
import scheduler.db.ConnectionManager;
import scheduler.model.Appointment;
import scheduler.model.Availability;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Random;
import java.util.Objects;

public class Availability {
    private final Date time;
    private final String username;

    private Availability(AvailabilityBuilder builder) {
        this.time = builder.time;
        this.username = builder.username;
    }

    private Availability(AvailabilityGetter getter) {
        this.time = getter.time;
        this.username = getter.username;
    }

    public Date getTime() {
        return time;
    }

    public String getUsername() {
        return username;
    }

    public static class AvailabilityBuilder {
        private final Date time;
        private final String username;

        public AvailabilityBuilder(String username, Date time) {
            this.username = username;
            this.time = time;
        }

        public Availability build() {
            return new Availability(this);
        }
    }

    public static class AvailabilityGetter {
        private final Date time;
        private final String username;

        public AvailabilityGetter(String username, Date time) {
            this.username = username;
            this.time = time;
        }

        public Availability build() {
            return new Availability(this);
        }
    }

}
