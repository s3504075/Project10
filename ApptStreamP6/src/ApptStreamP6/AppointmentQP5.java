//Giancarlo Fruzzetti
// COP 2805 Project 8
//3-29-2023
//main program

//link information used for producing some of the code
//datetime manipulator functions https://docs.oracle.com/javase/10/docs/api/java/time/ZonedDateTime.html#get(java.time.temporal.TemporalField)
//StringBuilder manipulator functions https://docs.oracle.com/javase/7/docs/api/java/lang/StringBuilder.html
//Guide to Date Time Formatter: https://www.baeldung.com/java-datetimeformatter
//Chinese characters from Google translate convert to unicode here: https://pages.ucsd.edu/~dkjordan/resources/unicodemaker.html
//https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html

package ApptStreamP6;

import java.io.*;
import java.time.*;
import java.time.format.FormatStyle;
import java.util.*;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentLinkedQueue;
import Dispatch.Dispatcher; //import local dispatcher

public class AppointmentQP5 implements CalendarReminder, Dispatcher<Reminder>
{

    //public static final Scanner input = new Scanner(System.in); //a global scanner input for # of appts
    public static int NUMBERAPPTS=1; //number of client appointments
    private static final String USER_FILE = "appointmentcontacts.dat"; //data file of users

    private ArrayList<Appointment> clientappts = new ArrayList<>();//array list of client appointments
    private ConcurrentLinkedQueue tsqueue =
            new ConcurrentLinkedQueue( new LinkedList<Reminder>()); //dispatching queue

    private Stream<Reminder> stream = tsqueue.stream();


    public AppointmentQP5() {
    }

    public void dispatch(Reminder robj) {
        //takes reminder object and adds it to queue outputs that reminder is being sent
        this.tsqueue.add(robj);
        remindertypes rem;
        rem=robj.getContact().getReminder();
        //System.out.println("current queue length is " + this.queue.size());
        if(rem==remindertypes.EMAIL) {  //text reminder
            System.out.println("sending appointment email reminder to " + robj.getContact().getName()
                    +  " "  + robj.getContact().getEmail() + "\n");
            System.out.println(robj.toString() + "\n");

        }
        else { //email reminder
            System.out.println("sending appointment text reminder to " + robj.getContact().getName()
                    +  " "  + robj.getContact().getPhone() + "\n");
            System.out.println(robj.toString() + "\n");

        }
    }

    public void runAppts(ZonedDateTime reminder, AppointmentQP5 a1)
    //constructs and sends the reminders to the queue
    {
        int timecomparison;
        //String Rem;
        ZonedDateTime currentTime;
        Reminder robj; //reminder objectd

        for (Appointment A : a1.clientappts) //for each appt in queue
        {
            currentTime=ZonedDateTime.now();
            timecomparison=currentTime.compareTo(reminder);
            //System.out.println(timecomparison);
            if(timecomparison > 0) //if it is later than when the remider was created
            {
                robj=a1.buildReminder(A);  //build reminder object
                a1.sendReminder(robj);  //send reminder object for each one in queue
            }

        }

    }

    public Reminder buildReminder(Appointment appt) //build reminder and return to main
    {

        StringBuilder S = new StringBuilder("");
        StringBuilder plusses = new StringBuilder("\n");
        Reminder O; //reminder object
        String appt_as_string; //returns final string
        LocalDate ldt; //for printing local date
        int hour;  //to print local hour
        int minute; //to print local minute
        int max,min,maxlen,insloc,firststars; //setting string lengths with indexof

        try {
            ResourceBundle res = ResourceBundle.getBundle("ApptStreamP6.Apptproperties",
                    appt.getContact().getLocale());
            S.append("\n+ Date: " + appt.getDtf());
            S.append("\n+ " + res.getString("Hello") + " " + appt.getContact().getName());
            S.append("\n+ " + res.getString("translaterem"));
            S.append("\n+ Provider:" + res.getString("title") + " " + "Dr. I.M.A. Quack.");
            S.append("\n+ Description: " + appt.getDescription());

            //ldt=appt.getZdt().toLocalDate();
            //hour=appt.getZdt().getHour();
            //minute=appt.getZdt().getMinute();
            //S.append("\n+ Date: " + ldt);
            //S.append("\n+ Time: " + hour + ":" + minute + " " + appt.getZdt().getZone());
            min = S.indexOf("+ Date", 0);
            max = S.indexOf("Description.", 0) + 84; //beginning + end
            //System.out.println(max+ " " + min);
            maxlen = max - min;
            //System.out.println(maxlen);
            for (int i = 0; i < maxlen + 1; i++) {
                plusses.append("+");
            }
            S.append(plusses);
            insloc = S.indexOf("+ Date");
            //System.out.println(insloc);
            S.insert(insloc - 1, plusses);
            appt_as_string = S.toString();
        } catch (java.util.MissingResourceException e) { //if missing resource default appointment output
            System.err.println(e);
            S.append("\n\nSending the following SMS message to " + appt.getContact().getName()+
                    " " + appt.getContact().getPhone());
            S.append("\n\n+ Hello: " + appt.getContact().getName());
            S.append("\n+");
            S.append("\n+ This is a reminder that you have an upcoming appointment.");
            S.append("\n+ Title:" +appt.getAppointmentTitle());
            S.append("\n+ Description: " + appt.getDescription());
            //day=appt.getZdt().get(ChronoField.DAY_OF_MONTH);
            //year=appt.getZdt().get(ChronoField.YEAR);
            //m=appt.getZdt().getMonth();
            ldt=appt.getZdt().toLocalDate();
            hour=appt.getZdt().getHour();
            minute=appt.getZdt().getMinute();
            S.append("\n+ Date: " + ldt);
            S.append("\n+ Time: " + hour + ":" + minute + " " + appt.getZdt().getZone());
            min=S.indexOf("+ This",0);
            max=S.indexOf("appointment.",0)+12; //beginning + end
            //System.out.println(max+ " " + min);
            maxlen=max-min;
            //System.out.println(maxlen);
            for (int i=0; i < maxlen+1; i++)
            {
                plusses.append("+");
            }
            S.append(plusses);
            insloc=S.indexOf("+ Hello");
            S.insert(insloc-1,plusses);
            appt_as_string=S.toString();
        }
        O=new Reminder(appt.getContact(), appt_as_string, appt.getReminderTime());
        return O;

    }

    public void sendReminder(Reminder robj) //sends reminders with the constructed reminder objects
    {
        //System.out.println(O.toString());

        //lambda call for dispatcher
        Dispatcher<Reminder> d = (c)-> {
            this.tsqueue.add(c);


            //provides queue length removed for this project
            //System.out.println("current queue length is " + this.tsqueue.size());

            //removed, outputs reminder info to console
            /*if(robj.getContact().getReminder()==Reminder.TEXT) {  //text reminder
                System.out.println("sending appointment email reminder to " + c.getContact().getName()
                        +  " "  + c.getContact().getEmail() + "\n");
                //System.out.println(c.toString() + "\n");

            }
            else { //email reminder
                System.out.println("sending appointment text reminder to " + c.getContact().getName() +  " "
                        + c.getContact().getPhone() + "\n");
                //System.out.println(c.toString() + "\n");

            }*/
        };
        d.dispatch(robj);
    }


    public void addAppointments(Appointment... appointments) {
        for (Appointment A : appointments) {
            clientappts.add(A);
        }
    }

    public static int getRandomMonth() { //gets random month for the appointments
        // define the range
        int max = 12;
        int min = 1;
        int range = max - min + 1;

        // generate random numbers within 1 to 10
        int rand = (int) (Math.random() * range) + min;
        //System.out.println("Months ahead appt:" + rand); testing random month
        return (rand);

  }

    public static int getRandomHours() {  //gets random hours for the appointments
        // define the range
        int max = 24;
        int min = 2;
        int range = max - min + 1;

        // generate random numbers within 1 to 10
        int rand = (int) (Math.random() * range) + min;
        //System.out.println("hours before reminder:" + rand); testing random hours
        return (rand);

    }

    public static DateTimeFormatter Dtf(Appointment clientappt)
    //formats the date time with the formatter for each client
    {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        formatter =
                formatter.localizedBy(clientappt.getContact().getLocale());
        return(formatter);
    }

    public static void Wait()
    {
        // wait for a bit
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            System.out.println("sleep interrupted! " + ie);
        }

    }

    public ArrayList<Contact> readContacts() {
        ArrayList<Contact> list = new ArrayList();

        try (ObjectInputStream userData =
                     new ObjectInputStream(
                             new FileInputStream(USER_FILE));) {
            list = (ArrayList<Contact>) (userData.readObject());
            for (Contact c : list)
                System.out.println("read Contacts: read " + c);
        } catch (FileNotFoundException e) {
            // not  a problem if nothing was saved
            System.err.println("read Contacts: no input file");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }


    public int writeContacts(ArrayList<Contact> contacts) //note no return value if
            //an exception occurs, so return 1 only for success
    {
        try (ObjectOutputStream userData =  new ObjectOutputStream(
                new FileOutputStream(USER_FILE));) {
            userData.writeObject(contacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }
    public static void main(String[] args) {

//r            //random appt month, currentmonth, number appts to generate as per user
        int month, currentmonth, currentyr, numberappts;
        int result=0;  //to make decision if IOexception for writing file
        ZonedDateTime currentTime, apptdate, appttime, reminder;
        var zone = ZoneId.of("US/Eastern");
        //int minushours;
        //String tbf;
        //final int appthours = 12; //set offset for the appointment time vs now
        //int timecomparison; //is current time later than reminder time
        //String Rem;  //for the final reminder string in the box
        String Dateastxt; //for the formatted date through the formatter
        //default info
        /*Contact client = new Contact("Olivia", "Migiano", "OliviaM@att.net",
                "904-666-2424", Reminder.EMAIL, new Locale("EN"), zone);
        Contact client2 = new Contact("Olivier", "Giroud", "olg@acmilan.com",
                "606-11-2232", Reminder.EMAIL, new Locale("FR"), zone);
        Contact client3 = new Contact("Robin", "Gosens", "RG8@intermilan.com",
                "606-31-2071", Reminder.TEXT, new Locale("DE"), zone);
        Contact client4 = new Contact("Ciro", "Immobile", "Ciro@lazio.com",
                "696-969-6666", Reminder.TEXT, new Locale("IT"),zone );
        Contact client5 = new Contact("Eric", "Zheng", "Eric@China.com",
                "232-22-2232", Reminder.TEXT, new Locale("ZH"), zone);

        //2 new added clients not used for this project
        Contact client6 = new Contact("Federico", "Chiesa", "Federico@juventusfc.com",
                "414-33-5577", Reminder.TEXT, new Locale("IT"), zone);
        Contact client7 = new Contact("Kaio", "Jorge", "Kaio@juventusfc.com",
                "230-22-2111", Reminder.TEXT, new Locale("BR"), zone);*/

        String apptitle = "Medical Appointment with Dr. I.M.A. Quack.";
        String description = "Pending Appointment.";
        AppointmentQP5 A1 = new AppointmentQP5();  //object

        ArrayList<Contact> contactlist = A1.readContacts(); //added code for project 8

        AppointmentProcessor processor = new AppointmentProcessor(A1.tsqueue);


        reminder=ZonedDateTime.now(); //set this default to this moment to ensure delivery


        if(contactlist.isEmpty())
        {
           contactlist.add(new Contact("Olivia", "Migiano", "OliviaM@att.net",
                   "904-666-2424", remindertypes.EMAIL, new Locale("EN"), zone));
            contactlist.add(new Contact("Olivier", "Giroud", "olg@acmilan.com",
                    "606-11-2232", remindertypes.EMAIL, new Locale("FR"), zone));
            contactlist.add(new Contact("Robin", "Gosens", "RG8@intermilan.com",
                    "606-31-2071", remindertypes.TEXT, new Locale("DE"), zone));
            contactlist.add(new Contact("Ciro", "Immobile", "Ciro@lazio.com",
                    "696-969-6666", remindertypes.TEXT, new Locale("IT"),zone ));
            contactlist.add(new Contact("Eric", "Zheng", "Eric@China.com",
                    "232-22-2232", remindertypes.EMAIL, new Locale("ZH"), zone));
        }

        else {
            System.out.println("Contacts were read from data file");

        }

        //set appt general information
        for (int i = 0; i < NUMBERAPPTS; i++) //create n random appts for clients
        {
            Appointment clientappt = new Appointment(); //pass info to constructor here
            Appointment clientappt2 = new Appointment();
            Appointment clientappt3 = new Appointment();
            Appointment clientappt4 = new Appointment();
            Appointment clientappt5 = new Appointment();
            //Appointment clientappt6 = new Appointment();
            //Appointment clientappt7 = new Appointment();

            clientappt.setContact(contactlist.get(0)); //set the client info,
                                                       // clientappt.getContact() gets the contact
            clientappt2.setContact(contactlist.get(1));
            clientappt3.setContact(contactlist.get(2));
            clientappt4.setContact(contactlist.get(3));
            clientappt5.setContact(contactlist.get(4));
            //clientappt6.setContact(client6);
            //clientappt7.setContact(client7);



            currentTime = ZonedDateTime.now(); //get current time
            String formattedZdt = currentTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
            ZonedDateTime zoneddatetime = ZonedDateTime.parse(formattedZdt);
            month = AppointmentQP5.getRandomMonth(); //get random month for the appt
            currentmonth = zoneddatetime.getMonthValue(); //get current month to help set the appointment date
            //minushours = AppointmentQP5.getRandomHours();
            //reminder = apptdate.minusHours(minushours);
            //currentyr = zoneddatetime.getYear();

            //System.out.println(currentTime + " " + month + " " + currentmonth + " " + currentyr + " " + minushours);
            if (month + currentmonth < 12) { //you don't need to add a year to the current one, appt in this year
                apptdate = currentTime.plusMonths(month); //set to random month n months into the future
                //System.out.println(apptdate);
                //apptdate=ZonedDateTime.now().plusHours(appthours);

                apptdate = apptdate.plusHours(1); //add hour to initial time
                Dateastxt=apptdate.format(Dtf(clientappt));  //client 1 appointment date
                clientappt.setAppointment(apptitle, description, Dateastxt, reminder); //set appt for client 1

                apptdate = apptdate.plusHours(2); //add 2 hours to the initial date for next appt
                Dateastxt=apptdate.format(Dtf(clientappt2)); //client 2 date string for reminder
                clientappt2.setAppointment(apptitle, description, Dateastxt, reminder); //set appt for client 2

                apptdate = apptdate.plusHours(3); //add 3 hours for next appt
                Dateastxt=apptdate.format(Dtf(clientappt3)); //client 3 date string for reminder
                clientappt3.setAppointment(apptitle, description, Dateastxt, reminder); //create appt object

                apptdate = apptdate.plusHours(4); //add 4 hours for next appt
                Dateastxt=apptdate.format(Dtf(clientappt4)); //client 4
                clientappt4.setAppointment(apptitle, description, Dateastxt, reminder); //create appt object

                apptdate = apptdate.plusHours(5); //add 5 hours for next appt
                Dateastxt=apptdate.format(Dtf(clientappt5)); //client 5
                clientappt5.setAppointment(apptitle, description, Dateastxt, reminder);//create appt object

                A1.addAppointments(clientappt,clientappt2,clientappt3,clientappt4, clientappt5);
                A1.runAppts(reminder, A1);

                A1.Wait();
                A1.addAppointments(clientappt,clientappt2,clientappt3,clientappt4, clientappt5);
                A1.runAppts(reminder, A1);
                A1.Wait();

                //to generate new times for the 2 new clients, reference prior program vrsion to re-add below here

                processor.endProcessing(); //kill thread
                result=A1.writeContacts(contactlist);
                if(result == 1)
                {
                    System.out.println("Contacts written to file \n");
                }
                else
                {
                    System.out.println("Error writing to file. \n");
                }


            }
            else //overlapped to 13 mos
            {

                apptdate = currentTime.plusYears(1); //add year
                apptdate = apptdate.plusMonths(month + currentmonth - 12); //add month
                //System.out.println(apptdate);

                //reminder = apptdate.minusHours(minushours);
                //apptdate=ZonedDateTime.now().plusHours(appthours);
                apptdate = apptdate.plusHours(1); //add hour to initial time
                Dateastxt=apptdate.format(Dtf(clientappt));  //client 1 appointment date
                clientappt.setAppointment(apptitle, description, Dateastxt, reminder); //set appt for client 1

                apptdate = apptdate.plusHours(2); //add 2 hours to the initial date for next appt
                Dateastxt=apptdate.format(Dtf(clientappt2)); //client 2 date string for reminder
                clientappt2.setAppointment(apptitle, description, Dateastxt, reminder); //set appt for client 2

                apptdate = apptdate.plusHours(3); //add 3 hours for next appt
                Dateastxt=apptdate.format(Dtf(clientappt3)); //client 3 date string for reminder
                clientappt3.setAppointment(apptitle, description, Dateastxt, reminder); //create appt object

                apptdate = apptdate.plusHours(4); //add 4 hours for next appt
                Dateastxt=apptdate.format(Dtf(clientappt4)); //client 4
                clientappt4.setAppointment(apptitle, description, Dateastxt, reminder); //create appt object

                apptdate = apptdate.plusHours(5); //add 5 hours for next appt
                Dateastxt=apptdate.format(Dtf(clientappt5)); //client 4
                clientappt5.setAppointment(apptitle, description, Dateastxt, reminder);//create appt object

                A1.addAppointments(clientappt,clientappt2,clientappt3,clientappt4, clientappt5);
                A1.runAppts(reminder, A1);

                A1.Wait(); //sleep the thread

                A1.addAppointments(clientappt,clientappt2,clientappt3,clientappt4, clientappt5);
                A1.runAppts(reminder, A1);

                A1.Wait(); //sleep the thread

                //to generate new times for the 2 new clients, reference prior program version to re-add below here

                processor.endProcessing(); //kill thread
                result=A1.writeContacts(contactlist);
                if(result == 1)
                {
                    System.out.println("CONTACTS WRITTEN TO FILE! \n");
                }
                else
                {
                    System.out.println("Error writing to file. \n");
                }
            }
        }
        //System.out.println("starting forEach");
        //A1.stream.forEach(System.out::println);
    }
}
