import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;

/**
 * Created by Maciej on 2017-04-01.
 */

public class Meetings {
    private int idMeeting;
    private String title;
    private Date meetingDate;
    private Date startDate;
    private Date endDate;
    private String startTime;
    private String endTime;
    private long durationMeeting;
    private String description;
    private ArrayDeque<Meetings> allMeetings = new ArrayDeque<Meetings>();
    private int idRoom;

    public Meetings(String title, Date meetingDate, String startTime, String endTime, String description, int idRoom, ArrayDeque<Meetings> allMeetings) throws ParseException {
        this.title = title;
        this.meetingDate = meetingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMeeting = getDuration(startTime, endTime);
        this.description = description;
        this.allMeetings = allMeetings;
        this.idRoom = idRoom;
        if (allMeetings.isEmpty())
            this.idMeeting = 0;
        else
            this.idMeeting = allMeetings.getLast().idMeeting + 1;
        allMeetings.add(new Meetings(idMeeting, title, meetingDate, startTime, endTime, durationMeeting, description, idRoom));
    }

    public Meetings(int idM, String t, Date cD, String sT, String eT, long dM, String d, int iR) {
        this.idMeeting = idM;
        this.title = t;
        this.meetingDate = cD;
        this.startTime = sT;
        this.endTime = eT;
        this.durationMeeting = dM;
        this.description = d;
        this.idRoom = iR;
    }

    public ArrayDeque<Meetings> getAllMeetings() { return allMeetings; }

    public int getId() {
        return idMeeting;
    }

    public String getTitle() { return title; }

    public String getName() { return title; }

    public String getMeetingDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(meetingDate);
    }

    public static Date changeToDateForDate(String dateS) throws ParseException {
        Date date = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            date = format.parse(dateS);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date changeToDateForTime(String timeS) throws ParseException {
        Date time = null;
        try {
            DateFormat format = new SimpleDateFormat("HH:mm");
            time = format.parse(timeS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public Date getStartTime() throws ParseException {
        Date time = null;
        try {
            time = changeToDateForTime(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public Date getEndTime() throws ParseException {
        Date time = null;
        try {
            time = changeToDateForTime(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public String getsTime() { return startTime; }

    public String geteTime() { return endTime; }

    public long getDuration(String startTime, String endTime) throws ParseException {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        try {
            startDate = timeFormat.parse(startTime);
            endDate = timeFormat.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        durationMeeting = endDate.getTime() - startDate.getTime();
        return durationMeeting;
    }

    public String getDescription() {
        return description;
    }

    public int getIdRoom() { return idRoom; }

    public void removeAllMeetings() { allMeetings.clear(); }
}
