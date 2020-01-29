package dk.lbloft.xmltv.dr.drxml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Data;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Parser {
    public static Schedule parse(InputStream is) throws IOException, XMLStreamException {
        XMLInputFactory f = XMLInputFactory.newFactory();
        XMLStreamReader sr = f.createXMLStreamReader(is);
        XmlMapper xmlMapper = new XmlMapper();
        Message value = xmlMapper.readValue(sr, Message.class);
        sr.close();
        return value.getSchedule();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private Schedule schedule;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Schedule {
        private Channel channel;
        private List<Program> programs;
    }



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Channel {
        private String name;
        private String www_url;
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Program {
        private int prd_number;
        private String pro_title;
        private int prd_episode_number;
        private int prd_episode_total_number;
        private int prd_prodyear;
        private String prd_genre_text;
        private Publish pro_publish;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Publish {
        private String ppu_title;
        private String ppu_title_alt;
        private String ppu_punchline;
        private String ppu_description;
        private String ppu_www_url;
        private String ppu_channel;

        private String ppu_start_timestamp_presentation;
        private String ppu_stop_timestamp_presentation;

        private String ppu_islive;
        private String ppu_isrerun;
    }
}
