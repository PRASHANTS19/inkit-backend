package com.inkit.backend.case_mgmt;
import java.util.UUID;

import com.inkit.backend.auth.User;
import com.inkit.backend.common.enums.CaseType;
import com.inkit.backend.common.enums.Court;
import com.inkit.backend.common.enums.Priority;
import com.inkit.backend.firm.Firm;

import com.inkit.backend.common.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cases")
public class Case {

  @Id
  @GeneratedValue
  private UUID id;

  private String caseTitle;
  private String caseNumber;
  private String clientName;
  private String clientContact;
  
  @Enumerated(EnumType.STRING)
  private Court court;

  @Enumerated(EnumType.STRING)
  private CaseType caseType;

  @Enumerated(EnumType.STRING)
  private Status status;

  @Enumerated(EnumType.STRING)
  private Priority priority;

  private String caseDescription;

  @ManyToOne
  private User assignedTo;

  @ManyToOne
  private Firm firm;
}
