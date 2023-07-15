package scheduler;

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


public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // printing greetings text
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Done)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Done)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Debo)
        System.out.println("> quit");
        System.out.println();

        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        // create_patient <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the patient
        try {
            Patient patient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to patient information to our database
            patient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
        menuDisplay();
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            Caregiver caregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            caregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
        menuDisplay();
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentPatient != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
        menuDisplay();
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
        menuDisplay();
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        // TODO: Part 2
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("You are not logged in, Please login first!");
            return;
        }
        if (tokens.length != 2) {
            System.out.println("Date input Inavlid(past current), Please try again!");
            return;
        }

        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String caregiver = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username";
        String numofdoses = "SELECT Name, Doses FROM Vaccines";

        try {
            LocalDate today = LocalDate.now();
            LocalDate schedule = LocalDate.parse(tokens[1]);
            if (schedule.isBefore(today)) {
                System.out.println("Date invalid, Please try again!");
                return;
            }
            Date date = Date.valueOf(tokens[1]);

            PreparedStatement statementCaregiver = con.prepareStatement(caregiver);
            PreparedStatement statementVaccine = con.prepareStatement(numofdoses);

            statementCaregiver.setDate(1, date);

            ResultSet resultSetCaregiver = statementCaregiver.executeQuery();
            ResultSet resultSetVaccine = statementVaccine.executeQuery();

            System.out.print("Available Caregivers for " + date + " :");
            if (!resultSetCaregiver.next()){
                System.out.println(" No Caregiver available");
                return;
            }

            System.out.print(" " + resultSetCaregiver.getString(1));
            while (resultSetCaregiver.next()) {
                System.out.print(", " + resultSetCaregiver.getString(1));
            }
            System.out.println();
            System.out.print("Vaccine and available doses: ");

            while (resultSetVaccine.next()) {
                System.out.print(resultSetVaccine.getString(1) + "=" + resultSetVaccine.getInt(2) + " ");
            }
            System.out.println();
        }catch (SQLException e ){
            System.out.println("Something invalid, please try again!");
        } finally {
            cm.closeConnection();
        }
        menuDisplay();

    }

    private static void reserve(String[] tokens) {
        // TODO: Part 2 - SEEMS TO BE WORKING
        if (currentCaregiver != null) {
            System.out.println("Please login as a patient!");
            return;
        } else if (currentPatient == null) {
            System.out.println("Please login first!");
            return;
        }
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String Time = tokens[1];
        String Vaccine = tokens[2];
        Random r = new Random();
        int appointment_ID = Math.abs(r.nextInt(9999) + Time.hashCode() + Vaccine.hashCode());


        Vaccine vaccine1 = null;
        int number_of_dose = 0;
        try {
            vaccine1 = new Vaccine.VaccineGetter(Vaccine).get();
            number_of_dose = vaccine1.getAvailableDoses();
        } catch (SQLException e) {
            System.out.println("Error occured when checking doses");
            System.err.println(e.getMessage());
        }
        if (number_of_dose == 0) {
            System.out.println("Not enough available doses!");
            return;
        }

        String Patient = currentPatient.getUsername();

        ConnectionManager cm0 = new ConnectionManager();
        Connection con0 = cm0.createConnection();
        String selectUsername = "SELECT Username FROM Availabilities WHERE Time = ? ORDER BY Username asc";
        String Caregiver = "";
        try {
            PreparedStatement statementCaregiver = con0.prepareStatement(selectUsername);
            statementCaregiver.setString(1, Time);
            ResultSet resultSetCaregiver = statementCaregiver.executeQuery();
            if (!resultSetCaregiver.next()) {
                System.out.println("There is no caregiver");
                return;
            }
            Caregiver = resultSetCaregiver.getString(1);
            System.out.println("Successfully made the reservation for " + Time + ". Your appointment information is: ");
            System.out.print("Appointment ID: {" + appointment_ID + "}, Caregiver name: {" + Caregiver + "}");
//            System.out.println( appointment_ID + ", " + Caregiver + ", " + Time + ", " + Patient + ", " + Vaccine);
        } catch (SQLException e) {
            System.out.println("Error appears when assign careGiver");
            System.err.println(e.getMessage());
        } finally {
            cm0.closeConnection();
        }

        //making apointment
        ConnectionManager cm1 = new ConnectionManager();
        Connection con1 = cm1.createConnection();

        String Username = Caregiver;
        String addAppointment = "INSERT INTO Appointments VALUES (?, ?, ?, ?, ?)";
        String updateAvailability = "DELETE FROM Availabilities WHERE Time = ? AND Username = ?";
        String removeDoses = "UPDATE Vaccines SET Doses = ? WHERE NAME = ?";

        try {
            // UPdating availabilities
            PreparedStatement statement = con1.prepareStatement(updateAvailability);
            statement.setString(1, Time);
            statement.setString(2, Username);
            statement.executeUpdate();
// updating appointment
            PreparedStatement statement1 = con1.prepareStatement(addAppointment);
            statement1.setInt(1, appointment_ID);
            statement1.setString(2, Caregiver);
            statement1.setString(3, Time);
            statement1.setString(4, Patient);
            statement1.setString(5, Vaccine);
            statement1.executeUpdate();
// updating num of dose
            PreparedStatement statement2 = con1.prepareStatement(removeDoses);
            statement2.setInt(1, number_of_dose - 1);
            statement2.setString(2, Vaccine);
            statement2.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error when making appointments");
            System.err.println(e.getMessage());
        } finally {
            cm1.closeConnection();
        }
        System.out.println();
        menuDisplay();
    }
    public static void menuDisplay(){
        System.out.println();
        System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
        System.out.println("*** Please enter one of the following commands ***");
        System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Done)
        System.out.println("> create_caregiver <username> <password>");
        System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Done)
        System.out.println("> login_caregiver <username> <password>");
        System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
        System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
        System.out.println("> upload_availability <date>");
        System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
        System.out.println("> add_doses <vaccine> <number>");
        System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
        System.out.println("> logout");  // TODO: implement logout (Debo)
        System.out.println("> quit");
        System.out.println();
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit -- Done
        // Functionalities to add: Remove appointment(should remove for both) done
        //Increase dose done
        //Add-availability done
        //You cannot cancel this one - you can cancel your own only DONE WORKS!!!!

        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
            return;
        } else if (tokens.length != 2){
            System.out.println("Invalid input");
            return;
        }

        int appointment_ID = Integer.parseInt(tokens[1]);

        // checks if appointment ID is real or not - nest cancels appointments
        String checkAppointment = "SELECT * FROM Appointments WHERE appointment_ID = ?";
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String cancelAppointment = "DELETE FROM Appointments WHERE appointment_ID = ?";
        try {
            PreparedStatement statement = con.prepareStatement(checkAppointment);
            statement.setInt(1, appointment_ID);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()){
                System.out.println("Appointment ID doesn't exist try again");
                return;
            }

            String checkUser = "SELECT Caregiver, Patient FROM Appointments WHERE appointment_ID = ?";
            PreparedStatement statement5 = con.prepareStatement(checkUser);
            statement5.setInt(1, appointment_ID);
            ResultSet resultSet5 = statement.executeQuery();
            String pseudoCaregiver1 = "";
            String pseudoPatient1 = "";
            if (resultSet5.next()) {
                pseudoCaregiver1 = resultSet5.getString(2);//Caregiver
                pseudoPatient1 = resultSet5.getString(4);//Patient
                if (currentPatient == null) {
                    if (!currentCaregiver.getUsername().equals(pseudoCaregiver1)) {
                        System.out.println( currentCaregiver.getUsername() + pseudoCaregiver1);
                        System.out.println("You (cannot) do not have acces to cancel someone elses reservation. Please try again! C");
                        return;
                    } else {
                        //Updates availability table
                        String updateAvail = "Select Time, Caregiver, Vaccine From Appointments WHERE appointment_ID = ?";
                        PreparedStatement statement2 = con.prepareStatement(updateAvail);
                        statement2.setInt(1, appointment_ID);
                        ResultSet resultSet2 = statement2.executeQuery();
                        Date pseudoTime = null;
                        String pseudoCaregiver = null;
                        String pseudoVaccine = null;
                        if (resultSet2.next()) {
                            pseudoTime = resultSet2.getDate(1);//time
                            pseudoCaregiver = resultSet2.getString(2);//caregiver
                            pseudoVaccine = resultSet2.getString(3);//Vaccine
                        }
                        String addAvailability = "INSERT INTO Availabilities VALUES (? , ?)";
                        PreparedStatement statement3 = con.prepareStatement(addAvailability);
                        statement3.setDate(1, pseudoTime);
                        statement3.setString(2, pseudoCaregiver);
                        statement3.executeUpdate();

                        // UPdates Vaccines
                        Vaccine vaccine1 = new Vaccine.VaccineGetter(pseudoVaccine).get();
                        int number_of_dose = vaccine1.getAvailableDoses();
                        String addDoses = "UPDATE vaccines SET Doses = ? WHERE name = ?;";
                        PreparedStatement statement4 = con.prepareStatement(addDoses);
                        statement4.setInt(1, number_of_dose + 1);
                        statement4.setString(2, pseudoVaccine);
                        statement4.executeUpdate();

                        //removes from app table
                        PreparedStatement statement1 = con.prepareStatement(cancelAppointment);
                        statement1.setInt(1, appointment_ID);
                        statement1.executeUpdate();
                        System.out.println("Appointment Cancelled");
                    }
                } else if (currentCaregiver == null) {
                    if (!currentPatient.getUsername().equals(pseudoPatient1)) {
                        System.out.println( currentPatient.getUsername() + pseudoPatient1);
                        System.out.println("You (cannot) do not have acces to cancel someone elses reservation. Please try again! P");
                        return;
                    } else {
                        //Updates availability table
                        String updateAvail = "Select Time, Caregiver, Vaccine From Appointments WHERE appointment_ID = ?";
                        PreparedStatement statement2 = con.prepareStatement(updateAvail);
                        statement2.setInt(1, appointment_ID);
                        ResultSet resultSet2 = statement2.executeQuery();
                        Date pseudoTime = null;
                        String pseudoCaregiver = null;
                        String pseudoVaccine = null;
                        if (resultSet2.next()) {
                            pseudoTime = resultSet2.getDate(1);//time
                            pseudoCaregiver = resultSet2.getString(2);//caregiver
                            pseudoVaccine = resultSet2.getString(3);//Vaccine
                        }
                        String addAvailability = "INSERT INTO Availabilities VALUES (? , ?)";
                        PreparedStatement statement3 = con.prepareStatement(addAvailability);
                        statement3.setDate(1, pseudoTime);
                        statement3.setString(2, pseudoCaregiver);
                        statement3.executeUpdate();

                        // UPdates Vaccines
                        Vaccine vaccine1 = new Vaccine.VaccineGetter(pseudoVaccine).get();
                        int number_of_dose = vaccine1.getAvailableDoses();
                        String addDoses = "UPDATE vaccines SET Doses = ? WHERE name = ?;";
                        PreparedStatement statement4 = con.prepareStatement(addDoses);
                        statement4.setInt(1, number_of_dose + 1);
                        statement4.setString(2, pseudoVaccine);
                        statement4.executeUpdate();

                        //removes from app table
                        PreparedStatement statement1 = con.prepareStatement(cancelAppointment);
                        statement1.setInt(1, appointment_ID);
                        statement1.executeUpdate();
                        System.out.println("Appointment Cancelled");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException");
            System.err.println(e.getMessage());
        } finally {
            cm.closeConnection();
        }
        menuDisplay();

    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        // TODO: Seems to work Perfectly
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("You are not logged in, Please login first!");
            return;
        }
            if(currentCaregiver != null){ //For Caregivers
                ConnectionManager cm = new ConnectionManager();
                Connection con = cm.createConnection();
                String getAppointment = "SELECT appointment_ID, Vaccine, Time, Patient FROM Appointments WHERE Caregiver = ? ORDER BY appointment_ID";
                try {
                    PreparedStatement statement = con.prepareStatement(getAppointment);
                    statement.setString(1, currentCaregiver.getUsername());
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next() == false) {
                        System.out.println("You don't seem to have any appointments, Please try again!");
                    } else {
                        System.out.print("Appointment ID : " + resultSet.getInt(1));
                        System.out.print("   Vaccine name : " + resultSet.getString(2));
                        System.out.print("   Date : " + resultSet.getString(3));
                        System.out.print("   Patient name : " + resultSet.getString(4));
                        System.out.println();
                    }
                    while (resultSet.next()) {
                        System.out.print("Appointment ID : " + resultSet.getInt(1));
                        System.out.print("   Vaccine name : " + resultSet.getString(2));
                        System.out.print("   Date : " + resultSet.getString(3));
                        System.out.print("   Patient name : " + resultSet.getString(4));
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("Not working for caregiveer");
                    System.err.println(e.getMessage());
                } finally {
                    cm.closeConnection();
                }
            } else if (currentPatient != null) {//For Patients
                ConnectionManager cm = new ConnectionManager();
                Connection con = cm.createConnection();
                String getAppointment = "SELECT appointment_ID, Vaccine, Time, Caregiver FROM Appointments WHERE Patient = ? ORDER BY appointment_ID";
                try {
                    PreparedStatement statement = con.prepareStatement(getAppointment);
                    statement.setString(1, currentPatient.getUsername());
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next() == false) {
                        System.out.println("You don't seem to have any appointments, Please try again!");
                    } else {
                        System.out.print("Appointment ID : " + resultSet.getInt(1));
                        System.out.print("   Vaccine name : " + resultSet.getString(2));
                        System.out.print("   Date : " + resultSet.getString(3));
                        System.out.print("   Caregiver name : " + resultSet.getString(4));
                        System.out.println();
                    }
                    while (resultSet.next()) {
                        System.out.print("Appointment ID : " + resultSet.getInt(1));
                        System.out.print("   Vaccine name : " + resultSet.getString(2));
                        System.out.print("   Date : " + resultSet.getString(3));
                        System.out.print("   Caregiver name : " + resultSet.getString(4));
                        System.out.println();
                    }
                } catch (SQLException e) {
                    System.out.println("Not working for patient");
                    System.err.println(e.getMessage());
                } finally {
                    cm.closeConnection();
                }
            }
        menuDisplay();
    }

    private static void logout(String[] tokens) {
        // TODO: Works
        if (currentCaregiver == null && currentPatient == null) {
            System.out.println("You are not logged in, Please login first!");
            return;
        }
        currentPatient = null;
        currentCaregiver = null;
        System.out.println("Successfully logged out!");
        menuDisplay();
    }
}