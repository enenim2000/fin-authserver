package com.elara.authorizationservice.service;

import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.domain.Audit;
import com.elara.authorizationservice.domain.AuditBaseModel;
import com.elara.authorizationservice.enums.ApprovalStatus;
import com.elara.authorizationservice.enums.CrudOperation;
import com.elara.authorizationservice.enums.EntityStatus;
import com.elara.authorizationservice.enums.GroupType;
import com.elara.authorizationservice.util.JsonConverter;
import com.elara.authorizationservice.util.ReflectionUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Aspect
@Component
public class AuditAdvice {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired MessageService messageService;

    private Object intercept(ProceedingJoinPoint jp, Object entity, Audit audit) throws Throwable {

        Boolean skipAudit = true;
        Boolean skipAuthorization = true;

        if (entity instanceof AuditBaseModel) {
            skipAudit = (Boolean) ReflectionUtil.getFieldValue(entity.getClass(), "skipAudit", entity);
            skipAuthorization = (Boolean) ReflectionUtil.getFieldValue(entity.getClass(), "skipAuthorization", entity);
            skipAudit = skipAudit == null || skipAudit;
            skipAuthorization = skipAuthorization == null || skipAuthorization;

        }

        Object response = null;
        String message = null;

        boolean callProceed = true;
        if (entity instanceof AuditBaseModel && !skipAudit) {
            if (CrudOperation.Create.name().equalsIgnoreCase(audit.getAction())) {
                if (!skipAuthorization) {
                    ReflectionUtil.setFieldValue(entity.getClass(), "status", EntityStatus.Pending.name(), entity);
                }
                audit.setBefore(null);
                response = jp.proceed();
                audit.setAfter(JsonConverter.getJsonRecursive(entity));
                message = messageService.getMessage("create_authorization_message");
            } else if (CrudOperation.Delete.name().equalsIgnoreCase(audit.getAction())) {
                audit.setBefore(getEntityBefore(entity));
                audit.setAfter(null);
                response = entity;
                message = messageService.getMessage("delete_authorization_message");
            } else if (CrudOperation.Update.name().equalsIgnoreCase(audit.getAction())) {
                audit.setBefore(getEntityBefore(entity));
                audit.setAfter(JsonConverter.getJsonRecursive(entity));
                response = entity;
                message = messageService.getMessage("update_authorization_message");
            }

            if (!skipAuthorization) {
                RequestUtil.setApprovalMessage(message);
                callProceed = false;
                entityManager.detach(entity); // To prevent impacting the DB
            }
            audit.setApprovalRequired(!skipAuthorization);
            auditOperation(entity, audit);
        }

        if (callProceed) {
            try {
                response = jp.proceed();
                String approvalItemType = messageService.getMessage(entity.getClass().getSimpleName());
                if (CrudOperation.Update.name().equalsIgnoreCase(audit.getAction())) {
                    message = messageService.getMessage("update_message").replace("{0}", approvalItemType);
                } else if (CrudOperation.Delete.name().equalsIgnoreCase(audit.getAction())) {
                    message = messageService.getMessage("delete_message").replace("{0}", approvalItemType);
                } else if (CrudOperation.Create.name().equalsIgnoreCase(audit.getAction())) {
                    message = messageService.getMessage("create_message").replace("{0}", approvalItemType);
                }

                if (entity instanceof AuditBaseModel) {
                    RequestUtil.setApprovalMessage(message);
                }
            } catch (Throwable throwable) {
                log.error("Error during authorization interceptor: ", throwable);
            }
        }
        return response;
    }

    private void auditOperation(Object metaModel, Audit audit) {
        audit.setIpAddress("");
        audit.setUserAgent("");
        audit.setUserType(RequestUtil.getUserType());
        audit.setEntity(metaModel.getClass().getName());
        audit.setEntityId((Long) ReflectionUtil.getFieldValue(metaModel.getClass(), "id", metaModel));
        audit.setCreatedAt(new Date());
        audit.setCreatedBy(RequestUtil.getAuthToken().getEmail());

        if (audit.isApprovalRequired() && (audit.getUserType() != null && !GroupType.Customer.name().equalsIgnoreCase(audit.getUserType()))) {
            Object emailList = ReflectionUtil.getFieldValue(metaModel.getClass(), "approvalMails", metaModel);
            Object smsList = ReflectionUtil.getFieldValue(metaModel.getClass(), "approvalSms", metaModel);
            Object approvalDependencies = ReflectionUtil.getFieldValue(metaModel.getClass(), "approvalDependencies", metaModel);
            String responseType = (String) ReflectionUtil.getFieldValue(metaModel.getClass(), "responseType", metaModel);
            audit.setApprovalSms(JsonConverter.getJsonRecursive(smsList));
            audit.setApprovalEmail(JsonConverter.getJsonRecursive(emailList));
            audit.setApprovalDependency(JsonConverter.getJsonRecursive(approvalDependencies));
            audit.setStatus(ApprovalStatus.Pending.name());
            audit.setMaker(RequestUtil.getAuthToken().getEmail());
            audit.setResponseType(responseType);
            audit.setApprovalItemType(metaModel.getClass().getSimpleName());
            entityManager.persist(audit);
        }
    }

    @Around("execution(* jakarta.persistence.EntityManager.persist(..)) && !execution(* jakarta.persistence.EntityManager.persist(com.elara.authorizationservice.domain.Audit))" + " && args(entity,..)")
    public Object interceptCreate(ProceedingJoinPoint jp, Object entity) throws Throwable {
        Audit audit = new Audit();
        audit.setAction(CrudOperation.Create.name());
        audit.setDescription(messageService.getMessage("audit_create").replace("{0}", messageService.getMessage(entity.getClass().getSimpleName())));
        return intercept(jp, entity, audit);
    }

    @Around("execution(* jakarta.persistence.EntityManager.merge(..)) && !execution(* jakarta.persistence.EntityManager.merge(com.elara.authorizationservice.domain.Audit))" + " && args(entity,..)")
    public Object interceptUpdate(ProceedingJoinPoint jp, Object entity) throws Throwable {
        Audit audit = new Audit();
        audit.setAction(CrudOperation.Update.name());
        audit.setDescription(messageService.getMessage("audit_update").replace("{0}", messageService.getMessage(entity.getClass().getSimpleName())));
        return intercept(jp, entity, audit);
    }

    @Around("execution(* jakarta.persistence.EntityManager.remove(..))" + " && args(entity,..)")
    public Object interceptDelete(ProceedingJoinPoint jp, Object entity) throws Throwable {
        Audit audit = new Audit();
        audit.setAction(CrudOperation.Delete.name());
        audit.setDescription(messageService.getMessage("audit_delete").replace("{0}", messageService.getMessage(entity.getClass().getSimpleName())));
        return intercept(jp, entity, audit);
    }

    private String getEntityBefore(Object entity) {
        return (String) ReflectionUtil.getFieldValue(entity.getClass(), "before", entity);
    }
}

