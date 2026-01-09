package com.usp3.audit;

import com.usp3.platform.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActorType actorType;

    @Column(nullable = false)
    private String actorId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditOutcome outcome;

    @Column(columnDefinition = "TEXT")
    private String details;

    protected AuditLog() {}

    public AuditLog(
            ActorType actorType,
            String actorId,
            String action,
            String entityType,
            String entityId,
            AuditOutcome outcome,
            String details,
            String tenantId,
            String traceId
    ) {
        this.actorType = actorType;
        this.actorId = actorId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
        this.details = details;
        this.tenantId = tenantId;
        this.traceId = traceId;
    }

    public ActorType getActorType() { return actorType; }
    public String getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public AuditOutcome getOutcome() { return outcome; }
    public String getDetails() { return details; }
}
