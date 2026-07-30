package com.inkit.backend.case_mgmt.dto;

import lombok.Data;
import java.util.List;

@Data
public class ECourtResponse {
    private DataContainer data;

    @Data
    public static class DataContainer {
        private CourtCaseData courtCaseData;
        private EntityInfo entityInfo;
    }

    @Data
    public static class CourtCaseData {
        private String cnr;
        // e.g. "019992" — combined with cnrYear to form the display case number
        private String cnrCaseNumber;
        private String cnrYear;
        private String caseType;       // e.g. "SLP_CRL"
        private String caseStatus;     // e.g. "HEARING"
        private String filingNumber;   // e.g. "19992/2026"
        private String filingDate;     // e.g. "2026-04-03"
        private String registrationNumber; // e.g. "6896/2026"
        private String registrationDate;   // e.g. "2026-04-16"
        private String nextHearingDate;    // e.g. "2026-05-18"
        private String lastHearingDate;
        private String stageOfCase;
        private List<String> judges;
        private List<String> petitioners;
        private List<String> petitionerAdvocates;
        private List<String> respondents;
        private List<String> respondentAdvocates;
        private List<Hearing> historyOfCaseHearings;
        private List<Order> interimOrders;
    }

    @Data
    public static class EntityInfo {
        private String nextDateOfHearing;
        private String lastDateOfHearing;
        private String dateCreated;
        private String dateModified;
    }

    @Data
    public static class Hearing {
        private String judge;
        private String businessOnDate;
        private String hearingDate;
        private String purposeOfListing;
    }

    @Data
    public static class Order {
        private String orderDate;
        private String description;
        private String orderUrl;
    }
}