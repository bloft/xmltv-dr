package dk.lbloft.xmltv.dr;

import com.google.common.collect.ImmutableMap;
import dk.lbloft.xmltv.dr.drxml.Parser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    private static SimpleDateFormat inTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // 2020-01-30T04:27:29.000
    private static SimpleDateFormat outTime = new SimpleDateFormat("yyyyMMddHHmmss Z"); // 20080715003000 -0600

    public static void main(String[] args) throws IOException, XMLStreamException {
        xmltv(System.out);
    }

    private static void xmltv(PrintStream out) throws IOException, XMLStreamException {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
        out.println("<tv>");
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            xmltv(out, httpclient, "https://www.dr.dk/Tjenester/epglive/epg.DR1.drxml");
            xmltv(out, httpclient, "https://www.dr.dk/Tjenester/epglive/epg.DR2.drxml");
            xmltv(out, httpclient, "https://www.dr.dk/Tjenester/epglive/epg.DRRamasjang.drxml");
        }
        out.println("</tv>");
    }

    private static void xmltv(PrintStream out, CloseableHttpClient httpclient, String url) throws IOException, XMLStreamException {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            if(response.getStatusLine().getStatusCode() < 400) {
                xmltv(out, Parser.parse(response.getEntity().getContent()));
            }
        }
    }

    public static void xmltv(PrintStream out, Parser.Schedule schedule) {
        xmltv(out, schedule.getChannel());
        for (Parser.Program program : schedule.getPrograms()) {
            xmltv(out, program);
        }
    }

    public static void xmltv(PrintStream out, Parser.Channel channel) {
        out.printf("  <channel id=\"%s\">%n", channel.getName());
        out.printf("    <display-name>%s</display-name>%n", channel.getName());
        out.printf("  </channel>%n");
    }

    private static void xmltv(PrintStream out, Parser.Program program) {
        try {
            String start = time(program.getPro_publish().getPpu_start_timestamp_presentation());
            String stop = time(program.getPro_publish().getPpu_stop_timestamp_presentation());

            out.printf("  <programme start=\"%s\" stop=\"%s\" channel=\"%s\">%n", start, stop, program.getPro_publish().getPpu_channel());
            xmltv(out, "title", program.getPro_title());
            xmltv(out, "title", ImmutableMap.of("lang", "en"), program.getPro_publish().getPpu_title_alt());
            xmltv(out, "category", program.getPrd_genre_text());
            xmltv(out, "desc", program.getPro_publish().getPpu_description());
            xmltv(out, "episode-num", ImmutableMap.of("system", "onscreen"), program.getPrd_episode_number());
            out.printf("  </programme>%n");
        } catch (ParseException e) {
        }
    }

    private static void xmltv(PrintStream out, String tag, Map<Object, Object> attributs, Object value) {
        String atts = attributs.entrySet().stream().map(entry -> entry.getKey() + "=\"" + entry.getValue() + "\"").collect(Collectors.joining(" "));
        if(value instanceof Number && (int)value <= 0) value = null;
        if(value != null && !value.toString().isEmpty()) {
            out.printf("    <%s %s>%s</%s>%n", tag, atts, value, tag);
        }
    }

    private static void xmltv(PrintStream out, String tag, Object value) {
        xmltv(out, tag, Collections.emptyMap(), value);
    }

    private static String time(String value) throws ParseException {
        return outTime.format(inTime.parse(value));
    }
}
