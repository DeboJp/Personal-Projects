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

public class Appointment {
    private final String appointmentID;
    private final String patient;
    private final String caregiver;
    private final String vaccine;
    private final Date time;

    private Appointment(AppointmentBuilder builder) {
        this.appointmentID = builder.appointmentID;
        this.patient = builder.patient;
        this.caregiver = builder.caregiver;
        this.vaccine = builder.vaccine;
        this.time = builder.time;
    }

    private Appointment(AppointmentGetter getter) {
        this.appointmentID = getter.appointmentID;
        this.patient = getter.patient;
        this.caregiver = getter.caregiver;
        this.vaccine = getter.vaccine;
        this.time = getter.time;
    }

    public Date getTime() {
        return time;
    }

    public String appointmentID() {
        return appointmentID;
    }

    public String patient() {
        return patient;
    }

    public String caregiver() {
        return caregiver;
    }

    public String vaccine() {
        return vaccine;
    }


    public static class AppointmentBuilder {
        private final String appointmentID;
        private final String patient;
        private final String caregiver;
        private final String vaccine;
        private final Date time;

        public AppointmentBuilder(String appointmentID, String patient, String caregiver, String vaccine, Date time) {
            this.appointmentID = appointmentID;
            this.patient = patient;
            this.caregiver = caregiver;
            this.vaccine = vaccine;
            this.time = time;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }

    public static class AppointmentGetter {
        private final String appointmentID;
        private final String patient;
        private final String caregiver;
        private final String vaccine;
        private final Date time;

        public AppointmentGetter(String appointmentID, String patient, String caregiver, String vaccine, Date time) {
            this.appointmentID = appointmentID;
            this.patient = patient;
            this.caregiver = caregiver;
            this.vaccine = vaccine;
            this.time = time;
        }

        public Appointment build() {
            return new Appointment(this);
        }
    }

}
