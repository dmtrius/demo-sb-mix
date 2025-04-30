package com.example.demo.apps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClientLoginExample {
  void main() throws Exception {
    CookieManager cookieManager = new CookieManager();
    try (HttpClient client = HttpClient.newBuilder()
        .cookieHandler(cookieManager)
        .connectTimeout(Duration.ofSeconds(10))
        .build()) {

      String loginUrl = "https://cmap.jud11.flcourts.org/Login"; // Replace with actual login URL
      String formData = "Input.UserName=mrosenberg%40roiglawyers.com&Input.Password=roig1255"; // Adjust based on form field names

      HttpRequest getLoginPage = HttpRequest.newBuilder()
          .uri(new URI(loginUrl))
          .GET()
          .build();
      HttpResponse<String> loginPageResponse = client.send(getLoginPage, HttpResponse.BodyHandlers.ofString());
      if (loginPageResponse.statusCode() != 200) {
        System.out.println("Failed to fetch login page: " + loginPageResponse.statusCode());
        return;
      }

      String loginPage = loginPageResponse.body();
      String csrfToken = extractCsrfToken(loginPage);
      if (csrfToken == null) {
        System.out.println("CSRF token not found in login page.");
        return;
      }

      HttpRequest loginRequest = HttpRequest.newBuilder()
          .uri(new URI(loginUrl))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(formData + "&__RequestVerificationToken=" + csrfToken))
          .build();

      HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
      if (loginResponse.statusCode() >= 400) {
        System.out.println("Login failed with status: " + loginResponse.statusCode());
        return;
      }

      List<HttpCookie> cookies = cookieManager.getCookieStore().get(new URI(loginUrl));
      String aspNetCoreAntiforgeryCookie = null;
      for (HttpCookie cookie : cookies) {
        if (cookie.getName().startsWith(".AspNetCore.Antiforgery.")) {
          aspNetCoreAntiforgeryCookie = cookie.getName() + "=" + cookie.getValue();
          break;
        }
      }

      if (aspNetCoreAntiforgeryCookie == null) {
        System.out.println("No .AspNetCore cookie found. Login may have failed.");
        return;
      }

      String caseNumber = "2023182955SP26";
      String judgeIdUrl = "https://cmap.jud11.flcourts.org/Scheduling/NewEvent";
      String json = "{\"case\":{\"caseNumber\":\"" + caseNumber + "\"}}";
      HttpRequest judgeIdRequest = HttpRequest.newBuilder()
          .uri(new URI(judgeIdUrl))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(json))
          .build();

      HttpResponse<String> judgeIdResponse = client.send(judgeIdRequest, HttpResponse.BodyHandlers.ofString());
      if (judgeIdResponse.statusCode() >= 400) {
        System.out.println("Failed to access protected resource: " + judgeIdResponse.statusCode());
        return;
      }
      String judgeId = extractJudgeId(judgeIdResponse.body());
      if (judgeId == null) {
        System.out.println("Judge ID not found in response.");
        return;
      }
      System.out.println("Judge ID: " + judgeId);

      // GET calendar
      ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS); // Current day at 00:00:00Z
      ZonedDateTime oneMonthLater = now.plusMonths(1); // One month from now
      // Format dates in ISO 8601 (e.g., 2025-04-28T00:00:00.000Z)
      DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
      String fromDate = formatter.format(now);
      String toDate = formatter.format(oneMonthLater);

      // URL-encode dates for query parameters
      String encodedFromDate = URLEncoder.encode(fromDate, StandardCharsets.UTF_8);
      String encodedToDate = URLEncoder.encode(toDate, StandardCharsets.UTF_8);

      // Construct URL with query parameters
      String baseCalendarUrl = "https://cmap.jud11.flcourts.org/Scheduling/ReadPlanOccurrencesAvailability";
      String calendarUrl = String.format("%s?fromDate=%s&toDate=%s&sort=&group=&filter=&__RequestVerificationToken.__RequestVerificationToken=%s&section.id=%s",
          baseCalendarUrl, encodedFromDate, encodedToDate, aspNetCoreAntiforgeryCookie, judgeId);
      HttpRequest calendarRequest = HttpRequest.newBuilder()
          .uri(new URI(calendarUrl))
          .GET()
          .build();

      HttpResponse<String> calendarResponse = client.send(calendarRequest, HttpResponse.BodyHandlers.ofString());
      String calendarPage = calendarResponse.body();

      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      HearingResponse response = mapper.readValue(calendarPage, HearingResponse.class);

      // Access specific fields
      if (!Objects.isNull(response.getData())) {
        HearingSlot slot = response.getData().getFirst();
        System.out.println("Hearing Slot ID: " + slot.getId());
        System.out.println("Title: " + slot.getTitle());
        System.out.println("Start Time: " + slot.getStart());
        System.out.println("Category: " + slot.getCategory());
        System.out.println("Hearing Type: " + slot.getHearingType());
        System.out.println("Section: " + slot.getSection());
        System.out.println("Status: " + slot.getStatus());
      }
    }
  }

  private static String extractCsrfToken(String page) {
    String regex = "<input\\s+name=\"__RequestVerificationToken\"\\s+type=\"hidden\"\\s+value=\"([^\"]+)\"";
    return extractValue(page, regex);
  }

  private static String extractJudgeId(String page) {
    String regex = "assignedJudgeSection.+?id\\D+:(\\d+)\\D+";
    return extractValue(page, regex);
  }

  private static String extractValue(String page, String regexp) {
    Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(page);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }
}
// ===================================================================
// Root class for the JSON response
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class HearingResponse {
  @JsonProperty("Data")
  private List<HearingSlot> data;
}

// Entity for each hearing slot in the Data array
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class HearingSlot {
  @JsonProperty("EventCount")
  private Integer eventCount;

  @JsonProperty("AllocatedMinutes")
  private Integer allocatedMinutes;

  @JsonProperty("BlockedMinutes")
  private Integer blockedMinutes;

  @JsonProperty("TotalMinutes")
  private Integer totalMinutes;

  @JsonProperty("Selected")
  private Boolean selected;

  @JsonProperty("OutsidePlanRange")
  private Boolean outsidePlanRange;

  @JsonProperty("Full")
  private Boolean full;

  @JsonProperty("Blocked")
  private Boolean blocked;

  @JsonProperty("HasZoomMeeting")
  private Boolean hasZoomMeeting;

  @JsonProperty("Id")
  private Integer id;

  @JsonProperty("Title")
  private String title;

  @JsonProperty("Start")
  private OffsetDateTime start;

  @JsonProperty("End")
  private OffsetDateTime end;

  @JsonProperty("RepeatFrom")
  private OffsetDateTime repeatFrom;

  @JsonProperty("RepeatUntil")
  private OffsetDateTime repeatUntil;

  @JsonProperty("Description")
  private String description;

  @JsonProperty("JudicialOfficer")
  private String judicialOfficer;

  @JsonProperty("Category")
  private Category category;

  @JsonProperty("HearingType")
  private HearingType hearingType;

  @JsonProperty("Division")
  private Object division;

  @JsonProperty("Section")
  private Section section;

  @JsonProperty("Status")
  private Status status;

  @JsonProperty("Comments")
  private String comments;

  @JsonProperty("Capacity")
  private Integer capacity;

  @JsonProperty("MinEventDuration")
  private Integer minEventDuration;

  @JsonProperty("MaxEventDuration")
  private Integer maxEventDuration;

  @JsonProperty("WeekDays")
  private String weekDays;

  @JsonProperty("ScheduleInternalOnly")
  private Boolean scheduleInternalOnly;

  @JsonProperty("CancelInternalOnly")
  private Boolean cancelInternalOnly;

  @JsonProperty("BufferDays")
  private Integer bufferDays;

  @JsonProperty("IsAllDay")
  private Boolean isAllDay;

  @JsonProperty("StartTimezone")
  private String startTimezone;

  @JsonProperty("EndTimezone")
  private String endTimezone;

  @JsonProperty("RecurrenceRule")
  private String recurrenceRule;

  @JsonProperty("ExtendedTitle")
  private String extendedTitle;
}

// Category entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Category {
  @JsonProperty("HearingTypes")
  private List<Object> hearingTypes; // Null in JSON

  @JsonProperty("Id")
  private Integer id;

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Label")
  private String label;

  @JsonProperty("Description")
  private String description;
}

// HearingType entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class HearingType {
  @JsonProperty("DivisionId")
  private Integer divisionId;

  @JsonProperty("Id")
  private Integer id;

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Label")
  private String label;

  @JsonProperty("Description")
  private String description;
}

// Section entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Section {
  @JsonProperty("Id")
  private Integer id;

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Label")
  private String label;

  @JsonProperty("DivisionId")
  private Integer divisionId;

  @JsonProperty("Division")
  private Object division; // Null in JSON
}

// Status entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Status {
  @JsonProperty("Id")
  private Integer id;

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Label")
  private String label;

  @JsonProperty("Description")
  private String description;
}
