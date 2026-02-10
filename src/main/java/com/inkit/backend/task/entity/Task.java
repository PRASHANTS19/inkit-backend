package com.inkit.backend.task.entity;
import java.time.LocalDate;
import java.util.UUID;

import com.inkit.backend.auth.User;
import com.inkit.backend.case_mgmt.entity.Case;
import com.inkit.backend.common.enums.TaskPriority;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {
  @Id
  @GeneratedValue
  private UUID id;

  private String title;
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  private TaskPriority priority;

  @ManyToOne
  private Case caseRef;

  @ManyToOne
  private User assignedTo;
}
