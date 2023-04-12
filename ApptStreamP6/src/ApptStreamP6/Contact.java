
//contact class

package ApptStreamP6;

import java.time.ZoneId;
import java.util.Locale;
import java.io.Serializable;


public class Contact implements Serializable
{
    private StringBuilder name;
    private String email;
    private String phone;

    Locale locale;
    private remindertypes remind;
    private ZoneId zone;

    Contact (String fName, String lName, String email, String phone, remindertypes r, Locale locale, ZoneId z) {
        this.name = new StringBuilder();
        this.name.append(fName).append(" ").append(lName);
        this.email = email;
        this.phone = phone;
        this.remind = r;
        this.zone = z;
        this.locale=locale;
    } //constructor

    public StringBuilder getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
    public remindertypes getReminder()
    {
        return remind;
    }

    public Locale getLocale() {
        return locale;
    }

    public remindertypes setReminder(remindertypes R)
    {
        this.remind=R;
        return remind;
    }
    {}

    public ZoneId getZone()
    {
        return zone;
    }

    @Override
    public String toString() {
        String s = this.name + " email: " + this.email + " phone: " + this.phone + " reminder: "
                + this.remind + " time zone: " + this.zone;
        return s;
    }

}
