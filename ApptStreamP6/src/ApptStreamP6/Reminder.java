
//Reminder Object



package ApptStreamP6;

import java.time.ZonedDateTime;

public class Reminder {

    private Contact contact;
    private String remindertxt;
    private ZonedDateTime zdt;

    Reminder() {}

    Reminder(Contact C, String rtext, ZonedDateTime apptime)
    {
        this.contact=C;
        this.remindertxt=rtext;
        this.zdt=apptime;
    }

    public Contact getContact() {
        return contact;
    }


    public String getReminder()
    {
        return remindertxt;
    }

    public ZonedDateTime getTime()
    {
        return zdt;
    }


    @Override
    public String toString() {
        //String s = "Contact: " + this.contact  + " \nreminder: " + this.remindertxt + " \nappt reminder time: " + this.zdt;
        String t = this.remindertxt;
        return t;
    }

}
