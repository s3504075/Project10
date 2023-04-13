package ApptStreamP6;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Locale;
import com.microsoft.sqlserver.jdbc.SQLServerException;

public final class ContactDB {

    private ContactDB() {}

    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pStatement = null;
    private static ResultSet rSet = null;
    private static boolean connected = false;
    private static boolean dbCreated = false;

    private static final String DB_NAME = "AppointmentContacts";
    private static final String USER_TABLE_NAME = "Contacts";

    // connection URLs: one for no specified DB, other for DB name
    private static final String CONN_URL = "jdbc:sqlserver://localhost:1433;" +
            "integratedSecurity=true;" +
            "dataBaseName=" + DB_NAME + ";" +
            "loginTimeout=2;" +
            "trustServerCertificate=true";
    private static final String CONN_NODB_URL = "jdbc:sqlserver://localhost:1433;" +
            "integratedSecurity=true;" +
            "loginTimeout=2;" +
            "trustServerCertificate=true";
    private static final String DB_CREATE = "CREATE DATABASE " + DB_NAME + ";";
    private static final String TABLE_CREATE = "USE " + DB_NAME + ";" +
            "CREATE TABLE " + USER_TABLE_NAME +
            " (ID smallint PRIMARY KEY NOT NULL," +
            "FNAME varchar(80) NOT NULL," +
            "LNAME varchar(80) NOT NULL," +
            "EMAIL varchar(80) NOT NULL," +
            "PHONE varchar(80) NOT NULL," +
            "REMINDER varchar(80) NOT NULL," +
            "LOCALE varchar(80) NOT NULL," +
            "ZONE varchar(80) NOT NULL);";
    private static final String TABLE_INSERT = "USE " + DB_NAME + ";" +
            "INSERT INTO " + USER_TABLE_NAME +
            "(ID, FNAME, LNAME, EMAIL, PHONE, REMINDER, LOCALE, ZONE)" +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?)"; //placeholders for strings
    private static final String TABLE_SELECT = "SELECT * FROM " + USER_TABLE_NAME + ";";
    private static final String TABLE_DROP = "DROP TABLE " + USER_TABLE_NAME + ";";
    private static final String DB_DROP = "DROP DATABASE " + DB_NAME + ";";

    public static void createDB() {

        // try to connect using the DB name first, if this fails
        // fall back to the no-DB URL and create the DB
        String url = CONN_URL;
        int tries = 0;
        while (connected == false) {
            tries++;
            if (tries > 4) {  // no infinite loops allowed
                System.out.println("could not get connection, exiting");
                System.exit(0);
            }
            try {
                con = DriverManager.getConnection(url);
                System.out.println("got connection");
                connected = true;
                ;
            } catch (SQLServerException e) {
                e.printStackTrace();

                if (tries == 1) { // failed with the db name, fall back to no-name
                    System.out.println("could not connect to DB, trying alternate URL");
                    url = CONN_NODB_URL;
                }
            } catch (SQLException e) { // Handle any errors that may have occurred.
                e.printStackTrace();
            }
        }

        if (connected == false) // no DB connection, give up
            System.exit(0);

        try {
            stmt = con.createStatement(); // this can be reused
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("could not create statement");
        }

        dbCreated = true; // assume success, may change

        // if we fell back to the no-DB URL, assume we need to create the DB
        if (url == CONN_NODB_URL) {
            try {
                stmt.executeUpdate(DB_CREATE);
                System.out.println("DB created");
            } catch (SQLException e) { // this is a problem
                dbCreated = false;
                e.printStackTrace();
                System.out.println("could not create DB");
            }
        }

        if (dbCreated == false) // no DB, give up
            System.exit(0);

        try {
            stmt.executeUpdate(TABLE_CREATE);
            System.out.println("Table created");
        } catch (SQLServerException e) {
            System.out.println("could not create table - already exists?");
            url = CONN_NODB_URL;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("could not create table");
        }

        // we're good to continue, create our data
        var zone = ZoneId.of("US/Eastern"); //needed for zoneid
        //String namestring;  //convert stringbuilder full name back to string

        //ZonedDateTime currentDate = ZonedDateTime.now();
        try {

            pStatement = con.prepareStatement(TABLE_INSERT);

            // temp list for DB insertions
            ArrayList<Contact> contactList = new ArrayList<>();

            contactList.add(new Contact(0,"Olivia", "Migiano", "OliviaM@att.net",
                    "904-666-2424", remindertypes.EMAIL, new Locale("EN"), zone));
            contactList.add(new Contact(0,"Olivier", "Giroud", "olg@acmilan.com",
                    "606-11-2232", remindertypes.EMAIL, new Locale("FR"), zone));
            contactList.add(new Contact(0,"Robin", "Gosens", "RG8@intermilan.com",
                    "606-31-2071", remindertypes.TEXT, new Locale("DE"), zone));
            contactList.add(new Contact(0,"Ciro", "Immobile", "Ciro@lazio.com",
                    "696-969-6666", remindertypes.TEXT, new Locale("IT"),zone ));
            contactList.add(new Contact(0,"Eric", "Zheng", "Eric@China.com",
                    "232-22-2232", remindertypes.EMAIL, new Locale("ZH"), zone));
            for (Contact c: contactList) { //get info to write to database
                System.out.println("key=" + c.getId());
                pStatement.setInt(1, c.getId());
                pStatement.setString(2,c.getfName());
                pStatement.setString(3,c.getlName());
                pStatement.setString(4, c.getEmail());
                pStatement.setString(5, c.getPhone());
                pStatement.setString(6, c.getReminder().toString());
                pStatement.setString(7, c.getLocale().toString());
                pStatement.setString(8, c.getZone().toString());

                pStatement.addBatch();
            }
            pStatement.executeBatch();
            System.out.println("Records inserted");
            //sleep 30 min to see database info
            //can put sleep here, use Microsoft SQL Server Management Studio to see database info, viewable under databases/table name
            /*try {
                Thread.sleep(1800000);
            } catch (InterruptedException ie) {
                System.out.println("sleep interrupted! " + ie);
            }*/

        } catch (BatchUpdateException e) { // records exist, warn and carry on
            System.out.println("could not insert record, primary key violation?");
            e.printStackTrace();
        } catch (SQLException e) { // some other problem TBD
            e.printStackTrace();
            System.out.println("could not insert record");
        }
    }

    public static ArrayList<Contact> readUserDB() {

        ArrayList<Contact> contactList = new ArrayList<>();
        // select the data from the table, save to ResultSet
        System.out.println("Reading from DB");
        try {
            rSet = stmt.executeQuery(TABLE_SELECT);

            // show the data using the next() iterator
            while (rSet.next()) {
                int id = rSet.getInt("ID");
                String fname = rSet.getString("FNAME");
                String lname = rSet.getString("LNAME");
                String email = rSet.getString("EMAIL");
                String phone = rSet.getString("PHONE");
                String reminder = rSet.getString("REMINDER");
                String locale = rSet.getString("LOCALE");
                String zone = rSet.getString("ZONE");
//                // debug
//                System.out.println(id + "," +
//                        lName + "," +
//                        fName + "," +
//                        email + "," +
//                        locale + "," +
//                        birthday);
                remindertypes rem;
                rem=remindertypes.valueOf(reminder); //to convert reminder string back to reminder enum
                //and recreate the object

                contactList.add(new Contact(id, fname, lname, email, phone, rem,
                        new Locale(locale), ZoneId.of(zone)));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return contactList;
    }

    public static void deleteDB() {

        // try to drop the table and DB
        // DB drop can fail due to "in use", deal with this when we connect
        try {
            stmt.executeUpdate(TABLE_DROP);
            stmt.executeUpdate(DB_DROP);
            System.out.println("DB dropped");
        } catch (SQLException e) {
            System.out.println("could not drop DB, in use");
        }

        // clean up
        // close can also throw an exception, we want to continue
        // to close other objects if it does so we do a
        // try/catch for each close operation
        if (rSet != null) try { rSet.close(); } catch(Exception e) {}
        if (stmt != null) try { stmt.close(); } catch(Exception e) {}
        if (pStatement != null) try { pStatement.close(); } catch(Exception e) {}
        if (con != null) try { con.close(); } catch(Exception e) {}
    }
}
