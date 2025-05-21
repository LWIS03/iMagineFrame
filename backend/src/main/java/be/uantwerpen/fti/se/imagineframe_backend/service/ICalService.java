package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ICalService {

    public String convertEventsToICal(List<Event> events) {
        StringBuilder ical = new StringBuilder();
        ical.append("BEGIN:VCALENDAR\r\n");
        ical.append("VERSION:2.0\r\n");
        ical.append("PRODID:-//ImagineLab//Event Calendar//EN\r\n");
        for (Event event : events) {
            ical.append("BEGIN:VEVENT\r\n");
            ical.append("UID:").append(event.getId()).append("@imaginelab.club\r\n");
            ical.append("DTSTAMP:").append(formatDateToICal(LocalDateTime.now())).append("\r\n");
            ical.append("DTSTART:").append(formatDateToICal(event.getStartdate())).append("\r\n");
            ical.append("DTEND:").append(formatDateToICal(event.getEnddate())).append("\r\n");
            ical.append("SUMMARY:").append(escapeICalText(event.getName())).append("\r\n");
            ical.append("DESCRIPTION:").append(escapeICalText(event.getDescription())).append("\r\n");
            ical.append("LOCATION:").append(escapeICalText(event.getLocation())).append("\r\n");
            ical.append("END:VEVENT\r\n");
        }
        ical.append("END:VCALENDAR\r\n");
        return ical.toString();
    }
    private String formatDateToICal(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        return dateTime.format(formatter);
    }
    private String escapeICalText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
    }
}