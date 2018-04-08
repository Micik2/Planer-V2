import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static spark.Spark.*;
/**
 * Created by Maciej on 2017-03-31.
 */

public class Main {
    public static Meetings meeting;
    public static Meetings meeting1;
    public static Rooms room0;
    public static Rooms room1;
    public static ArrayDeque<Meetings> meetings;
    public static ArrayDeque<Rooms> rooms;
    public static Iterator<Meetings> iterator;

    public static void main(String[] args) throws ParseException {
        Spark.staticFileLocation("/public");
        port(5000);
        Main main = new Main();
        meetings = new ArrayDeque<Meetings>();
        rooms = new ArrayDeque<Rooms>();
        // Example data
        room0 = new Rooms(15, rooms);
        room1 = new Rooms(20, rooms);
        // Example data
        meeting = new Meetings("The first meeting", new Date("2017/04/04"), "16:00", "18:00", "Default description", 0, meetings);
        meeting1 = new Meetings("The second meeting", new Date("2017/04/05"), "17:00", "18:00", "Default description", 1, meetings);

        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
        Configuration cfg = new Configuration();
        cfg.setTemplateLoader(new ClassTemplateLoader(Main.class, "/"));
        freeMarkerEngine.setConfiguration(cfg);

        get("/", (request, response) -> main.renderContent("index.html"));

        get("/newMeetingIn", (request, response) -> {
            Map<String, Object> input = new HashMap<>();
            input.put("rooms", rooms);
            return freeMarkerEngine.render(new ModelAndView(input, "newMeetingIn.html"));
        });

        post("/newMeetingOut", (request, response) -> {
            String title = request.queryParams("title");
            String meetingDate = request.queryParams("meetingDate");
            String startTime = request.queryParams("startTime");
            String endTime = request.queryParams("endTime");
            String description = request.queryParams("description");
            String idRoomC = request.queryParams("idRoom");
            int idRoom = Integer.parseInt(idRoomC);
            Date sTime = Meetings.changeToDateForTime(startTime);
            Date eTime = Meetings.changeToDateForTime(endTime);
            response.cookie("title", title);
            response.cookie("description", description);
            response.cookie("idRoom", idRoomC);
            response.cookie("meetingDate", meetingDate);
            response.cookie("startTime", startTime);
            response.cookie("endTime", endTime);
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            long dur = timeFormat.parse(endTime).getTime() - timeFormat.parse(startTime).getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date mDate = dateFormat.parse(meetingDate);
            if (dur < 900000 || dur > 7200000) {
                response.redirect("/errorDuration");
                return "";
            }
            iterator = meetings.iterator();
            while (iterator.hasNext()) {
                Meetings meet = iterator.next();
                if (meet.getMeetingDate().equals(meetingDate) && meet.getIdRoom() == idRoom) {
                    if ((sTime.after(meet.getStartTime()) && sTime.before(meet.getEndTime())) || (eTime.after(meet.getStartTime()) && eTime.before(meet.getEndTime()))) {
                        response.redirect("/errorTime");
                        return "";
                    }
                }
            }

            new Meetings(title, mDate, startTime, endTime, description, idRoom, meetings);
            response.redirect("/");

            return "";
        });

        get("/errorTime", (request, response) -> {
            Random generator = new Random();
            String proposedStartTime = null;

            String mDate = request.cookie("meetingDate");
            String sTime = request.cookie("startTime");
            String eTime = request.cookie("endTime");

            long duration = meeting.getDuration(sTime, eTime);
            Date dur = new Date(duration);
            Date meetingDate = Meetings.changeToDateForDate(mDate);
            iterator = meetings.iterator();
            while (iterator.hasNext()) {
                Meetings meet = iterator.next();
                if (meet.getMeetingDate().equals(mDate)) {
                    String hours;
                    String minutes;

                    int hour = generator.nextInt(24);
                    int minute = generator.nextInt(60);

                    if (hour < 10)
                        hours = "0" + Integer.toString(hour);
                    else
                        hours = Integer.toString(hour);
                    if (minute < 10)
                        minutes = "0" + Integer.toString(minute);
                    else
                        minutes = Integer.toString(minute);

                    proposedStartTime = hours + ":" + minutes;
                    Date pSTime = Meetings.changeToDateForTime(proposedStartTime);
                    long pETime = pSTime.getTime() + dur.getTime();
                    Date dPETime = new Date(pETime);

                    if ((dPETime.after(meet.getStartTime()) && !dPETime.before(meet.getEndTime())) || (!dPETime.after(meet.getStartTime()) && dPETime.before(meet.getEndTime()))) {
                        if (pSTime.before(meet.getStartTime()) || pSTime.after(meet.getEndTime())) {
                            response.cookie("mDate", mDate);
                            response.cookie("proposedStartTime", proposedStartTime);
                            DateFormat ft = new SimpleDateFormat("HH:mm");
                            String proposedEndTime = ft.format(dPETime);
                            response.cookie("proposedEndTime", proposedEndTime);
                            Map<String, Object> input = new HashMap<>();

                            input.put("proposedStartTime", proposedStartTime);
                            input.put("mDate", mDate);

                            return freeMarkerEngine.render(new ModelAndView(input, "errorTime.html"));
                        }
                        else
                            iterator = meetings.iterator();
                    }
                    else
                        iterator = meetings.iterator();
                }
            }
            return "";
        });

        post("/agreed", (request, response) -> {
            String title = request.cookie("title");
            String description = request.cookie("description");
            String idRoomC = request.cookie("idRoom");
            int idRoom = Integer.parseInt(idRoomC);
            Date meetingDate = Meetings.changeToDateForDate(request.cookie("mDate"));
            String sTime = request.cookie("proposedStartTime");
            String eTime = request.cookie("proposedEndTime");

            new Meetings(title, meetingDate, sTime, eTime, description, idRoom, meetings);
            response.redirect("/meetings");
            return "";
        });

        get("/errorDuration", (request, response) -> main.renderContent("errorDuration.html"));

        get("/meetings", (request, response) -> {
            Map<String, Object> input = new HashMap<>();
            input.put("meetings", meetings);

            return freeMarkerEngine.render(new ModelAndView(input, "meetings.html"));
        });

        post("/meeting", (request, response) -> {
            String idS = request.queryParams("btn");
            response.cookie("idMeeting", idS);
            int id = Integer.parseInt(idS);
            response.status(200);
            response.type("text/html");
            Map<String, Object> input = new HashMap<>();

            iterator = meetings.iterator();
            while (iterator.hasNext()) {
                Meetings meet = iterator.next();
                if (meet.getId() == id) {
                    input.put("meet", meet);
                    break;
                }
            }

            return freeMarkerEngine.render(new ModelAndView(input, "meeting.html"));
        });

        post("/remove", (request, response) -> {
            String idS = request.cookie("idMeeting");
            int id = Integer.parseInt(idS);
            String agree = request.queryParams("btnRemove");

            iterator = meetings.iterator();
            while (iterator.hasNext()) {
                Meetings meet = iterator.next();
                if (meet.getId() == id) {
                    meetings.remove(meet);
                    break;
                }
            }
            response.redirect("/meetings");

            return "";
        });
    }

    private String renderContent(String htmlFile) throws URISyntaxException, IOException {
        try {
            URL url = getClass().getResource(htmlFile);
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), Charset.defaultCharset());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }
}
