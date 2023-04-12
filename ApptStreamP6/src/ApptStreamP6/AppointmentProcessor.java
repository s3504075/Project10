package ApptStreamP6;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AppointmentProcessor extends Thread
        implements ApptProcessor, ContactsLogger<Reminder>{

    private ApptLogger logger;
    private Socket logSocket;
    private ObjectOutputStream streamToServer;
    private ConcurrentLinkedQueue<Reminder> safeQueue;
    private boolean stopped = false;

    private static final String LOGFILE = "programlog.txt";

    public AppointmentProcessor(ConcurrentLinkedQueue<Reminder> safeQueue) {
        this.safeQueue = safeQueue;

        logger = new ApptLogger();
        try {
            logSocket = new Socket("localhost", ApptLogger.LOGPORT);
            System.out.println("connected to log server");

            streamToServer = new ObjectOutputStream(logSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // start polling (invokes run(), below)
        this.start();
    }

    // remove messages from the queue and process them
    public void processAppointments() {
        //System.out.println("before processing, queue size is " + safeQueue.size());
        safeQueue.stream().forEach(e -> {
            // Do something with each element
            e = safeQueue.remove();
            System.out.print(e);
            log(e);
        });
        //System.out.println("after processing, queue size is now " + safeQueue.size());
    }

    // allow external class to stop us
    public void endProcessing() {
        this.stopped = true;
        try {
            logSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }

    //appointment logger
    @Override
    public void log(Reminder o) {
        LocalDateTime local =  LocalDateTime.now(Clock.systemUTC());  //local time in UTC for logfile

        //LocalDateTime.from(Instant.now().atZone(ZoneId.systemDefault()));

        //following code creates the log string with local time in UTC to ms and the Reminderect sent to patients
        String msg = local.truncatedTo(ChronoUnit.MILLIS) +
                ":Reminder sent to:" + o.getContact();
        try (BufferedWriter cardlog = Files.newBufferedWriter(Path.of(LOGFILE),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
            cardlog.write(msg);
            cardlog.newLine();
        } catch (IOException e) {
            System.err.println("LOG FAIL" + msg);
            e.printStackTrace();
        }

        logserver(o); //try to log to server
    }

    public void logserver(Reminder o)
    {

        //log name
        String msg2 = ":appointment reminder sent:" + o.getContact().getName();
        try {
            streamToServer.writeObject(msg2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // poll queue for cards
    public void run() {
        final int SLEEP_TIME = 1000; // ms
        while (true) {
            try {
                processAppointments();
                Thread.sleep(SLEEP_TIME);
                System.out.println("polling");
            } catch (InterruptedException ie) {
                // see if we should exit
                if (this.stopped == true) {
                    System.out.println("poll thread received exit signal");
                    break;
                }
            }
        }
    }

}
