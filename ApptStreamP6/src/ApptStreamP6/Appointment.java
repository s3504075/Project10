
//Giancarlo Fruzzetti
// COP 2805 Project 5
//2-24-2023
//Appiontment class



package ApptStreamP6;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {


    private Contact contact;
    private String appointment_title;
    private String appointment_desc;
    private ZonedDateTime appointmenttime;
    private ZonedDateTime remindertime;

    private String dtf;



    // accessor and mutator for user
    public Contact getContact() {
        return contact;
    }

    public String getAppointmentTitle()
    {
        return appointment_title;
    }

    public String getDescription()
    {
        return appointment_desc;
    }

    public ZonedDateTime getZdt()
    {
        return appointmenttime;
    }

    public String getDtf()
    {
        return dtf;
    }

    public ZonedDateTime getReminderTime()
    {
        return remindertime;
    }


    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setAppointment(String apptitle, String apptdesc, ZonedDateTime appt, ZonedDateTime rm)
    {
        this.appointment_title=apptitle;
        this.appointment_desc=apptdesc;
        this.appointmenttime=appt;
        this.remindertime=rm;
    }

    public void setAppointment(String apptitle, String apptdesc, String apptdtf, ZonedDateTime rm)
    {
        this.appointment_title=apptitle;
        this.appointment_desc=apptdesc;
        this.dtf=apptdtf;
        this.remindertime=rm;
    }
    Appointment() //blank constructor
    {

    }

    public void display() //needed to get all info of the object when called
    {
        System.out.println("\n\nTitle: " + this.appointment_title);
        System.out.println("Description: " + this.appointment_desc);
        System.out.println("Client: " + this.contact.toString());
        System.out.println("Appointment Date and Time: " + this.appointmenttime);
        System.out.println("Reminder Time: " + this.remindertime);
    }



}
