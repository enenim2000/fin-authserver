package com.elara.authorizationservice.service;

import com.elara.authorizationservice.auth.RequestUtil;
import com.elara.authorizationservice.domain.Approval;
import com.elara.authorizationservice.domain.ApprovalItemSetup;
import com.elara.authorizationservice.domain.Audit;
import com.elara.authorizationservice.domain.User;
import com.elara.authorizationservice.dto.approval.Loan;
import com.elara.authorizationservice.dto.request.*;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.enums.*;
import com.elara.authorizationservice.exception.AppException;
import com.elara.authorizationservice.repository.ApprovalItemSetupRepository;
import com.elara.authorizationservice.repository.ApprovalRepository;
import com.elara.authorizationservice.repository.AuditRepository;
import com.elara.authorizationservice.repository.UserRepository;
import com.elara.authorizationservice.util.JsonConverter;
import com.elara.authorizationservice.util.PaginationUtil;
import com.elara.authorizationservice.util.ReflectionUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class ApprovalService {

    private final ModelMapper modelMapper;
    private final ApprovalItemSetupRepository approvalItemSetupRepository;
    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;

    private final AuditRepository auditRepository;
    private final MessageService messageService;
    private final Gson gson;
    private final NotificationService notificationService;
    private final PermissionService permissionService;

    @Value("${app.mail.sender}")
    String senderMail;

    @PersistenceContext
    EntityManager entityManager;

    public ApprovalService(ModelMapper modelMapper,
                           ApprovalItemSetupRepository approvalItemSetupRepository,
                           ApprovalRepository approvalRepository,
                           UserRepository userRepository,
                           AuditRepository auditRepository,
                           MessageService messageService,
                           Gson gson,
                           NotificationService notificationService,
                           PermissionService permissionService) {
        this.modelMapper = modelMapper;
        this.approvalItemSetupRepository = approvalItemSetupRepository;
        this.approvalRepository = approvalRepository;
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
        this.messageService = messageService;
        this.gson = gson;
        this.notificationService = notificationService;
        this.permissionService = permissionService;
    }

    public ViewApprovalItemTypesResponse getApprovalItemTypes() {
        return ViewApprovalItemTypesResponse.builder()
                .data(Arrays.asList(ApprovalItemType.values()))
                .build();
    }

    public GetApprovalItemSetupsResponse getApprovalItemSetups() {
        GetApprovalItemSetupsResponse response = new GetApprovalItemSetupsResponse();
        response.setData(new ArrayList<>());
        List<ApprovalItemSetup> setups = approvalItemSetupRepository.findAll();
        for (ApprovalItemSetup setup : setups) {
            GetApprovalItemSetupsResponse.Data data = modelMapper.map(setup, GetApprovalItemSetupsResponse.Data.class);
            data.setApprovalStageStaffIds(gson.fromJson(setup.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType()));
            data.setApprovalStageAmounts(gson.fromJson(setup.getApprovalStageAmounts(), new TypeToken<HashMap<Integer, Double>>() {}.getType()));
            response.getData().add(data);
        }
        return response;
    }

    public CreateApprovalItemSetupResponse saveApprovalItemSetup(CreateApprovalItemSetupRequest request) {

        if (request.getApprovalItemType() == null) {
            throw new AppException(messageService.getMessage("approvalItemType.required"));
        }

        if (!ApprovalItemType.isValid(request.getApprovalItemType())) {
            throw new AppException(messageService.getMessage("approvalItemType.invalid"));
        }

        if (request.getApprovalLevels() < 1) {
            throw new AppException(messageService.getMessage("approvalItemType.level.invalid"));
        }

        Map<Integer, List<Long>> approvalStageStaffIds = gson.fromJson(request.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType());
        Map<Integer, Double> approvalStageAmounts = gson.fromJson(request.getApprovalStageAmounts(), new TypeToken<HashMap<Integer, Double>>() {}.getType());

        ApprovalItemSetup approvalItemSetup = approvalItemSetupRepository.findByCompanyCodeAndApprovalItemType(RequestUtil.getAuthToken().getCompanyCode(), request.getApprovalItemType());
        if (approvalItemSetup == null) {
            approvalItemSetup = new ApprovalItemSetup();
            approvalItemSetup.setCreatedAt(new Date());
            approvalItemSetup.setCreatedBy(RequestUtil.getAuthToken().getUsername());
        } else {
            approvalItemSetup.setUpdatedAt(new Date());
            approvalItemSetup.setUpdatedBy(RequestUtil.getAuthToken().getUsername());
        }

        approvalItemSetup.setCompanyCode(RequestUtil.getAuthToken().getCompanyCode());
        approvalItemSetup.setApprovalItemType(request.getApprovalItemType());
        approvalItemSetup.setApprovalLevels(request.getApprovalLevels());
        approvalItemSetup.setStatus(EntityStatus.Enabled.name());

        for (int i = 1; i <= request.getApprovalLevels(); i++) {
            if (!approvalStageStaffIds.containsKey(i) || approvalStageStaffIds.get(i).isEmpty()) {
                throw new AppException(messageService.getMessage("approvalItemType.level.inConsistent.staffId"));
            }
            if (!approvalStageAmounts.containsKey(i) || approvalStageAmounts.get(i) == null) {
                throw new AppException(messageService.getMessage("approvalItemType.level.inConsistent.amount"));
            }
        }

        for (Map.Entry<Integer, List<Long>> entry : approvalStageStaffIds.entrySet()) {
            List<Long> staffIds = entry.getValue();
            validateStaffExist(staffIds);
        }

        approvalItemSetup.setApprovalStageStaffIds(gson.toJson(approvalStageStaffIds));
        approvalItemSetup.setApprovalStageAmounts(gson.toJson(approvalStageAmounts));
        approvalItemSetupRepository.save(approvalItemSetup);

        CreateApprovalItemSetupResponse response = new CreateApprovalItemSetupResponse();
        response.setData(modelMapper.map(approvalItemSetup, CreateApprovalItemSetupResponse.Data.class));
        response.getData().setApprovalStageStaffIds(gson.fromJson(approvalItemSetup.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType()));
        response.getData().setApprovalStageAmounts(gson.fromJson(approvalItemSetup.getApprovalStageAmounts(),  new TypeToken<HashMap<Integer, Double>>() {}.getType()));
        return response;
    }

    private void validateStaffExist(List<Long> staffIds) {
        for (Long staffId : staffIds) {
            User user = userRepository.findById(staffId).orElse(null);
            if (user == null) {
                throw new AppException(messageService.getMessage("User.Id.NotFound").replace("{0}", String.valueOf(staffId)));
            }
        }
    }

    private void validateUserPermissionToApprove(Approval approval, ApprovalItemSetup approvalItemSetup) {
        String username = RequestUtil.getAuthToken().getUsername();
        User user = userRepository.findByUsername(username);
        Map<Integer, List<Long>> approvalStageStaffIds = gson.fromJson(approvalItemSetup.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType());
        if (!approvalStageStaffIds.get(approval.getCurrentApprovalStage()).contains(user.getId())) {
            throw new AppException(messageService.getMessage("approval.permission.denied")
                    .replace("{0}", approval.getApprovalItemType().toLowerCase())
                    .replace("{1}", approval.getCurrentApprovalStage() + ""));
        }

        if (BookStatus.Booked.name().equalsIgnoreCase(approval.getBookStatus()) && !RequestUtil.getAuthToken().getEmail().equalsIgnoreCase(approval.getBookedBy())) {
            throw new AppException(messageService.getMessage("approval.item.book.denied").replace("{0}", approval.getBookedBy()));
        }
    }

    public ApproveRequestResponse approveRequest(ApproveRequest request) {

        if (request.getComment() == null || request.getComment().trim().equals("")) {
            throw new AppException("Comment is required");
        }

        Approval approval = approvalRepository.findByReference(request.getApprovalReference());
        if (approval == null) {
            throw new AppException(messageService.getMessage("Approval.NotFound"));
        }

        ApprovalItemSetup approvalItemSetup = approvalItemSetupRepository.findByCompanyCodeAndApprovalItemType(approval.getCompanyCode(), approval.getApprovalItemType());

        if (approvalItemSetup == null) {
            throw new AppException(messageService.getMessage("ApprovalItemSetup.NotFound"));
        }

        validateUserPermissionToApprove(approval, approvalItemSetup);

        int currentApprovalStage = approval.getCurrentApprovalStage();

        if (!(ApprovalStatus.Pending.name().equals(approval.getApprovalStatus()) || ApprovalStatus.InProgress.name().equals(approval.getApprovalStatus()))) {
            return buildApprovalResponse(approval);
        }

        Map<Integer, Double> approvalStageAmounts = gson.fromJson(approvalItemSetup.getApprovalStageAmounts(), new TypeToken<HashMap<Integer, Double>>() {}.getType());

        boolean approvalItemConditionFulfilled = false;
        if (ApprovalItemType.Loan.name().equals(approval.getApprovalItemType())) {
            Double maxAmountForCurrentStage = approvalStageAmounts.get(currentApprovalStage);
            Loan loan = gson.fromJson(approval.getApprovalRequestJson(), Loan.class);
            approvalItemConditionFulfilled = loan.getLoanAmount() <= maxAmountForCurrentStage ;
        }

        if (ApprovalStatus.Pending.name().equals(approval.getApprovalStatus()) && ApprovalAction.Reject.equals(request.getApprovalAction())) {
            if (currentApprovalStage <= approvalItemSetup.getApprovalLevels()) {
                approval.setApprovalStatus(ApprovalStatus.Rejected.name());
                approval.setComment(request.getComment());
                approval.setMailToCustomerRequired(true);
                approval.setCurrentApprovalStage(1);
                approval.setUpdatedAt(new Date());
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            }
        } else if (ApprovalStatus.Pending.name().equals(approval.getApprovalStatus()) && ApprovalAction.Rework.equals(request.getApprovalAction())) {
            if (currentApprovalStage <= approvalItemSetup.getApprovalLevels()) {
                approval.setApprovalStatus(ApprovalStatus.Rework.name());
                approval.setComment(request.getComment());
                approval.setMailToCustomerRequired(true);
                approval.setUpdatedAt(new Date());
                approval.setCurrentApprovalStage(1);
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            }
        } else if (ApprovalStatus.Pending.name().equals(approval.getApprovalStatus()) && ApprovalAction.Approve.equals(request.getApprovalAction())) {
            if (currentApprovalStage <= approvalItemSetup.getApprovalLevels() && approvalItemConditionFulfilled) {
                approval.setApprovalStatus(ApprovalStatus.Approved.name());
                approval.setComment(request.getComment());
                approval.setMailToCustomerRequired(true);
                approval.setUpdatedAt(new Date());
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            } else if (currentApprovalStage < approvalItemSetup.getApprovalLevels()) {
                approval.setApprovalStatus(ApprovalStatus.InProgress.name());
                approval.setCurrentApprovalStage(currentApprovalStage + 1); //Move approval to next stage
                approval.setComment(request.getComment());
                approval.setMailToNextApprovalRequired(true);
                approval.setUpdatedAt(new Date());
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            }
        } else if (ApprovalStatus.InProgress.name().equals(approval.getApprovalStatus()) && ApprovalAction.Approve.equals(request.getApprovalAction())) {
            if (currentApprovalStage <= approvalItemSetup.getApprovalLevels() && approvalItemConditionFulfilled) {
                approval.setApprovalStatus(ApprovalStatus.Approved.name());
                approval.setComment(request.getComment());
                approval.setMailToCustomerRequired(true);
                approval.setUpdatedAt(new Date());
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            } else if (currentApprovalStage < approvalItemSetup.getApprovalLevels()) {
                approval.setApprovalStatus(ApprovalStatus.InProgress.name());
                approval.setCurrentApprovalStage(currentApprovalStage + 1); //Move approval to next stage
                approval.setComment(request.getComment());
                approval.setMailToNextApprovalRequired(true);
                approval.setUpdatedAt(new Date());
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            }
        } else if (ApprovalStatus.InProgress.name().equals(approval.getApprovalStatus()) && ApprovalAction.Reject.equals(request.getApprovalAction())) {
            if (currentApprovalStage <= approvalItemSetup.getApprovalLevels()) {
                approval.setApprovalStatus(ApprovalStatus.Rejected.name());
                approval.setComment(request.getComment());
                approval.setMailToCustomerRequired(true);
                approval.setCurrentApprovalStage(1);
                approval.setUpdatedAt(new Date());
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            }
        } else if (ApprovalStatus.InProgress.name().equals(approval.getApprovalStatus()) && ApprovalAction.Rework.equals(request.getApprovalAction())) {
            if (currentApprovalStage <= approvalItemSetup.getApprovalLevels()) {
                approval.setApprovalStatus(ApprovalStatus.Rework.name());
                approval.setComment(request.getComment());
                approval.setMailToCustomerRequired(true);
                approval.setUpdatedAt(new Date());
                approval.setCurrentApprovalStage(1);
                approval.setBookStatus(BookStatus.Open.name());
                approval.setBookedBy(null);
            }
        }

        if (approval.getApprovalActivityLog() != null && !approval.getApprovalActivityLog().trim().equals("")) {
            HashMap<Integer, String> body = gson.fromJson(approval.getApprovalActivityLog(), new TypeToken<HashMap<Integer, String>>() {}.getType());
            if (!body.containsKey(currentApprovalStage)) {
                body.put(currentApprovalStage, RequestUtil.getAuthToken().getEmail());
                approval.setApprovalActivityLog(gson.toJson(body));
            }
        } else {
            HashMap<Integer, String> approvalActivityLog = new HashMap<>();
            String email = RequestUtil.getAuthToken().getEmail();
            approvalActivityLog.put(currentApprovalStage, email);
            String approvalActivityLogJson = gson.toJson(approvalActivityLog);
            approval.setApprovalActivityLog(approvalActivityLogJson);
        }

        if (approval.getApprovalActivityLogComment() != null && !approval.getApprovalActivityLogComment().trim().equals("")) {
            HashMap<Integer, String> body = gson.fromJson(approval.getApprovalActivityLogComment(), new TypeToken<HashMap<Integer, String>>() {}.getType());
            if (!body.containsKey(currentApprovalStage)) {
                body.put(currentApprovalStage, request.getComment());
                approval.setApprovalActivityLogComment(gson.toJson(body));
            }
        } else {
            HashMap<Integer, String> approvalActivityLogComment = new HashMap<>();
            approvalActivityLogComment.put(currentApprovalStage, request.getComment());
            approval.setApprovalActivityLogComment(gson.toJson(approvalActivityLogComment));
        }

        approvalRepository.save(approval);

        return buildApprovalResponse(approval);
    }

    private ApproveRequestResponse buildApprovalResponse(Approval approval) {
        ApproveRequestResponse response = ApproveRequestResponse.builder()
                .data(modelMapper.map(approval, ApproveRequestResponse.Data.class))
                .build();
        response.getData().setBody(gson.fromJson(approval.getApprovalRequestJson(), new TypeToken<HashMap<Object, Object>>() {}.getType()));
        response.getData().setApprovalActivityLog(gson.fromJson(approval.getApprovalActivityLog(), new TypeToken<HashMap<Integer, String>>() {}.getType()));
        response.getData().setApprovalActivityLogComment(gson.fromJson(approval.getApprovalActivityLogComment(), new TypeToken<HashMap<Integer, String>>() {}.getType()));
        if (ApprovalStatus.Approved.name().equals(approval.getApprovalStatus())) {
            response.setResponseMessage("Loan has already been approved");
        }
        if (ApprovalStatus.Rejected.name().equals(approval.getApprovalStatus())) {
            response.setResponseMessage("Loan has already been rejected");
        }
        if (ApprovalStatus.Rework.name().equals(approval.getApprovalStatus())) {
            response.setResponseMessage("Loan has already been treated. It requires rework from the customer");
        }
        return response;
    }

    public GetApprovalsResponse getApprovals(String approvalItemType, String approvalStatus) {
        GetApprovalsResponse response = new GetApprovalsResponse();
        response.setData(new ArrayList<>());
        List<Approval> approvals = approvalRepository.findByCompanyCodeAndApprovalItemTypeAndApprovalStatus(RequestUtil.getAuthToken().getCompanyCode(), approvalItemType, approvalStatus);
        for (Approval approval : approvals) {
            GetApprovalsResponse.Data data = modelMapper.map(approval, GetApprovalsResponse.Data.class);
            data.setBody(new Gson().fromJson(approval.getApprovalRequestJson(), new TypeToken<HashMap<Object, Object>>() {}.getType()));
            data.setApprovalActivityLog(new Gson().fromJson(approval.getApprovalActivityLog(), new TypeToken<HashMap<Integer, String>>() {}.getType()));
            response.getData().add(data);
        }

        return response;
    }

    public GetApprovalStatusResponse getApprovalStatus() {
        return GetApprovalStatusResponse.builder()
                .data(Arrays.asList(ApprovalStatus.values()))
                .build();
    }

    public void sendApprovalNotification() {
       List<Approval> approvals = approvalRepository.findNotificationPending(true, true);
       for (Approval approval : approvals) {
           if (approval.isMailToCustomerRequired()) {
               String message = "";
               if (ApprovalStatus.Approved.name().equalsIgnoreCase(approval.getApprovalStatus())) {
                   message = messageService.getMessage("approval.notification.customer.approve");
                   message = message.replace("{0}", approval.getApprovalItemType())
                           .replace("{1}", approval.getReference())
                           .replace("{2}", approval.getComment());
               }
               if (ApprovalStatus.Rework.name().equalsIgnoreCase(approval.getApprovalStatus())) {
                   message = messageService.getMessage("approval.notification.customer.rework");
                   message = message.replace("{0}", approval.getApprovalItemType())
                           .replace("{1}", approval.getReference())
                           .replace("{2}", approval.getComment());
               }
               if (ApprovalStatus.Rejected.name().equalsIgnoreCase(approval.getApprovalStatus())) {
                   message = messageService.getMessage("approval.notification.customer.reject");
                   message = message.replace("{0}", approval.getApprovalItemType())
                           .replace("{1}", approval.getReference())
                           .replace("{2}", approval.getComment());
               }
               notificationService.sendEmail(NotificationRequest.builder()
                       .message(message)
                       .html(message)
                       .companyCode(approval.getCompanyCode())
                       .recipientEmail(approval.getCustomerEmail())
                       .senderEmail(senderMail)
                       .requiredValidation(false)
                       .subject(messageService.getMessage("notification.customer.subject").replace("{0}", approval.getApprovalItemType()))
                       .build());

               approval.setMailToCustomerRequired(false);
               approvalRepository.save(approval);
           }

           if (approval.isMailToNextApprovalRequired()) {
               String message = messageService.getMessage("approval.notification.approver").replace("{0}", approval.getApprovalItemType())
                       .replace("{1}", approval.getReference());

               ApprovalItemSetup approvalItemSetup = approvalItemSetupRepository.findByCompanyCodeAndApprovalItemType(approval.getCompanyCode(), approval.getApprovalItemType());
               HashMap<Integer, List<Long>> approvalStageStaffIds = gson.fromJson(approvalItemSetup.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType());
               List<Long> staffIds = approvalStageStaffIds.get(approval.getCurrentApprovalStage());

               for (Long staffId : staffIds) {
                   Optional<User> user = userRepository.findById(staffId);
                   user.ifPresent(value -> notificationService.sendEmail(NotificationRequest.builder()
                           .message(message)
                           .html(message)
                           .companyCode(approval.getCompanyCode())
                           .recipientEmail(value.getEmail())
                           .senderEmail(senderMail)
                           .requiredValidation(false)
                           .subject(messageService.getMessage("approval.notification.subject"))
                           .build()));
               }
               approval.setMailToNextApprovalRequired(false);
               approvalRepository.save(approval);
           }
       }
    }

    public FilterApprovalResponse filterApproval(FilterApprovalRequest dto) {
        dto.sanitize();
        Pageable pageable = PaginationUtil.getPageRequest(dto.getPageIndex(), dto.getPageSize());
        Page<Approval> filtered = approvalRepository.searchApprovals(
                dto.getCompanyCode(),
                dto.getApprovalItemType(),
                dto.getReference(),
                dto.getCurrentApprovalStage(),
                dto.getApprovalStatus(),
                dto.getStartDate(),
                dto.getEndDate(),
                pageable
        );

        HashMap<Integer, List<Long>> approvalStageStaffIds = new HashMap<>();
        User user = userRepository.findByUsername(RequestUtil.getAuthToken().getUsername());
        if (dto.getAllowOnlyLoggedInUser() != null && dto.getAllowOnlyLoggedInUser()) {
            ApprovalItemSetup approvalItemSetup = approvalItemSetupRepository.findByCompanyCodeAndApprovalItemType(dto.getCompanyCode(), dto.getApprovalItemType());
            approvalStageStaffIds = gson.fromJson(approvalItemSetup.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType());
        }

        List<FilterApprovalResponse.Data> results = new ArrayList<>();

        for (Approval approval : filtered.getContent()) {
            if (dto.getAllowOnlyLoggedInUser() != null && dto.getAllowOnlyLoggedInUser()
                    && approvalStageStaffIds.containsKey(approval.getCurrentApprovalStage())
                    && !approvalStageStaffIds.get(approval.getCurrentApprovalStage()).contains(user.getId())) {
                continue;
            }

            if (dto.getAllowOnlyLoggedInUser() != null && dto.getAllowOnlyLoggedInUser()
                    && (ApprovalStatus.Rejected.name().equals(approval.getApprovalStatus()) || ApprovalStatus.Approved.name().equals(approval.getApprovalStatus()) || ApprovalStatus.Rework.name().equals(approval.getApprovalStatus()))) {
                continue;
            }

            FilterApprovalResponse.Data data = modelMapper.map(approval, FilterApprovalResponse.Data.class);
            data.setBody(gson.fromJson(approval.getApprovalRequestJson(), new TypeToken<HashMap<Object, Object>>() {}.getType()));
            data.setApprovalActivityLog(gson.fromJson(approval.getApprovalActivityLog(), new TypeToken<HashMap<Integer, String>>() {}.getType()));
            data.setApprovalActivityLogComment(gson.fromJson(approval.getApprovalActivityLogComment(), new TypeToken<HashMap<Integer, String>>() {}.getType()));
            results.add(data);
        }

        FilterApprovalResponse response = new FilterApprovalResponse();
        response.setData(results);
        response.setPageIndex(filtered.getNumber());
        response.setPageSize(filtered.getSize());
        response.setTotalContent(filtered.getTotalElements());
        response.setHasNextPage(filtered.hasNext());
        response.setHasPreviousPage(filtered.hasPrevious());
        response.setTotalPages(filtered.getTotalPages());
        return response;
    }

    public SubmitApprovalResponse submitApprovalRequest(SubmitApprovalRequest request) {
        SubmitApprovalResponse response = new SubmitApprovalResponse();
        response.setData(new SubmitApprovalResponse.Data());
        Approval approval = approvalRepository.findByReference(request.getReference());
        if (approval != null) {
            if (ApprovalStatus.Pending.name().equals(approval.getApprovalStatus())) {
                modelMapper.map(approval, response.getData());
                return response;
            }
            throw new AppException(messageService.getMessage("approval.exist").replace("{0}", approval.getReference()));
        }
        approval = modelMapper.map(request, Approval.class);
        approval.setApprovalStatus(ApprovalStatus.Pending.name());
        approval.setMailToNextApprovalRequired(true);
        approval.setMailToCustomerRequired(false);
        approval.setCurrentApprovalStage(1);
        approval.setApprovalAction("CREATE");//CREATE/DELETE/UPDATE
        approval.setCreatedAt(new Date());
        approval.setCreatedBy(request.getCustomerEmail());
        approval = approvalRepository.save(approval);
        modelMapper.map(approval, response.getData());
        return response;
    }

    public ViewApprovalActionsResponse getApprovalActions() {
        return ViewApprovalActionsResponse.builder()
                .data(Arrays.asList(ApprovalAction.values()))
                .build();
    }

    public BookApprovalResponse bookApprovalRequest(String reference) {
        Approval approval = approvalRepository.findByReference(reference);
        if (approval == null) {
            throw new AppException(messageService.getMessage("Approval.NotFound"));
        }

        if (BookStatus.Booked.name().equalsIgnoreCase(approval.getBookStatus())) {
            throw new AppException(messageService.getMessage("Approval.Booked").replace("{0}", approval.getBookedBy()));
        }

        approval.setBookedBy(RequestUtil.getAuthToken().getEmail());
        approval.setBookStatus(BookStatus.Booked.name());

        approvalRepository.save(approval);

        return new BookApprovalResponse();
    }

    public BookApprovalResponse unBookApprovalRequest(String reference) {

        Approval approval = approvalRepository.findByReference(reference);
        if (approval == null) {
            throw new AppException(messageService.getMessage("Approval.NotFound"));
        }

        if (BookStatus.Booked.name().equalsIgnoreCase(approval.getBookStatus())) {
            approval.setBookedBy(null);
            approval.setBookStatus(BookStatus.Open.name());
            approvalRepository.save(approval);
        }

        return new BookApprovalResponse();
    }

    public GetApprovalItemSetupByIdResponse getApprovalItemSetupById(Long id) {
        GetApprovalItemSetupByIdResponse response = new GetApprovalItemSetupByIdResponse();

        ApprovalItemSetup setup = approvalItemSetupRepository.findById(id).orElse(null);

        if (setup == null) {
            throw new AppException(messageService.getMessage("ApprovalItemSetup.NotFound"));
        }

        response.setData(modelMapper.map(setup, GetApprovalItemSetupByIdResponse.Data.class));
        response.getData().setApprovalStageStaffIds(gson.fromJson(setup.getApprovalStageStaffIds(), new TypeToken<HashMap<Integer, List<Long>>>() {}.getType()));
        response.getData().setApprovalStageAmounts(gson.fromJson(setup.getApprovalStageAmounts(), new TypeToken<HashMap<Integer, Double>>() {}.getType()));

        return response;
    }

    public FilterAuthorizationResponse filterAuthorizationRequest(FilterAuthorizationRequest dto) throws ClassNotFoundException {
        dto.sanitize();
        if (dto.getIncludeLoggedInUser()) {
            //maker = RequestUtil.getAuthToken().getEmail();
        }
        Pageable pageable = PaginationUtil.getPageRequest(dto.getPageIndex(), dto.getPageSize());
        Page<Audit> filtered = auditRepository.searchApprovals(dto.getApprovalItemType(), dto.getId(), null, dto.getApprovalStatus(), true, dto.getStartDate(), dto.getEndDate(), pageable);

        List<FilterAuthorizationResponse.Data> results = new ArrayList<>();

        for (Audit approval : filtered.getContent()) {

            FilterAuthorizationResponse.Data data = modelMapper.map(approval, FilterAuthorizationResponse.Data.class);

            if (approval.getBefore() != null && !approval.getBefore().trim().equals("")) {
                Class<?> responseType = Class.forName(approval.getResponseType());
                data.setInitialState(JsonConverter.getGson().fromJson(approval.getBefore(), responseType));
            }

            if (approval.getAfter() != null && !approval.getAfter().trim().equals("")) {
                Class<?> responseType = Class.forName(approval.getResponseType());
                data.setFinalState(JsonConverter.getGson().fromJson(approval.getAfter(), responseType));
            }

            results.add(data);
        }

        FilterAuthorizationResponse response = new FilterAuthorizationResponse();
        response.setData(results);
        response.setPageIndex(filtered.getNumber());
        response.setPageSize(filtered.getSize());
        response.setTotalContent(filtered.getTotalElements());
        response.setHasNextPage(filtered.hasNext());
        response.setHasPreviousPage(filtered.hasPrevious());
        response.setTotalPages(filtered.getTotalPages());
        return response;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ApproveAuthorizationResponse approveAuthorizationRequest(ApproveAuthorizationRequest dto) throws ClassNotFoundException {
        if (dto.getComment() == null || dto.getComment().trim().equals("")) {
            throw new AppException("Comment is required");
        }

        Audit audit = auditRepository.findById(dto.getId()).orElse(null);
        if (audit == null) {
            throw new AppException(messageService.getMessage("approval.request.notfound"));
        }

        if (ApprovalStatus.Approved.name().equals(audit.getStatus())) {
            throw new AppException(messageService.getMessage("approval.action.approved"));
        }

        if (ApprovalStatus.Rejected.name().equals(audit.getStatus())) {
            throw new AppException(messageService.getMessage("approval.action.rejected"));
        }

        if (!(ApprovalAction.Approve.name().equals(dto.getApprovalAction()) || ApprovalAction.Reject.name().equals(dto.getApprovalAction()))) {
            throw new AppException(messageService.getMessage("approval.action.notvalid"));
        }

        if (!ApprovalStatus.Pending.name().equals(audit.getStatus())) {
            throw new AppException(messageService.getMessage("approval.action.pending"));
        }

        if (RequestUtil.getAuthToken().getEmail().equalsIgnoreCase(audit.getMaker())) {
            throw new AppException(messageService.getMessage("approval.maker.restricted"));
        }

        String status = ApprovalStatus.Rejected.name();
        String entityStatus = EntityStatus.Rejected.name();

        if (ApprovalAction.Approve.name().equals(dto.getApprovalAction())) {
            status = ApprovalStatus.Approved.name();
            entityStatus = EntityStatus.Enabled.name();
        }

        audit.setStatus(status);
        audit.setComment(dto.getComment());
        audit.setChecker(RequestUtil.getAuthToken().getEmail());
        audit.setUpdatedAt(new Date());
        audit.setUpdatedBy(RequestUtil.getAuthToken().getEmail());

        if (audit.getEntity() == null) {
            if (ApprovalItemType.UserGroupPermission.name().equalsIgnoreCase(audit.getApprovalItemType())) {
                AssignUserGroupRequest req = JsonConverter.getGson().fromJson(audit.getApprovalDependency(), AssignUserGroupRequest.class);
                permissionService.assignGroupToUserMakerChecker(req, true);
            } else if (ApprovalItemType.UserPermission.name().equalsIgnoreCase(audit.getApprovalItemType())) {
                AssignUserPermissionRequest req = JsonConverter.getGson().fromJson(audit.getApprovalDependency(), AssignUserPermissionRequest.class);
                permissionService.assignPermissionToUserMakerChecker(req, true);
            } else if (ApprovalItemType.GroupPermission.name().equalsIgnoreCase(audit.getApprovalItemType())) {
                AssignGroupPermissionRequest req = JsonConverter.getGson().fromJson(audit.getApprovalDependency(), AssignGroupPermissionRequest.class);
                permissionService.assignPermissionToGroupMakerChecker(req, true);
            }
            auditRepository.save(audit);
            return new ApproveAuthorizationResponse();
        }

        Object entity = getEntity(audit.getEntity(), audit.getEntityId());
        if (entity != null) {

            if (CrudOperation.Create.name().equalsIgnoreCase(audit.getAction())) {
                ReflectionUtil.setFieldValue(entity.getClass(), "status", entityStatus, entity);
                ReflectionUtil.setFieldValue(entity.getClass(), "skipAudit", true, entity);
                ReflectionUtil.setFieldValue(entity.getClass(), "skipAuthorization", true, entity);
                entityManager.merge(entity);
            } else if (CrudOperation.Update.name().equalsIgnoreCase(audit.getAction())) {
                entity = JsonConverter.getGson().fromJson(audit.getAfter(), entity.getClass());
                ReflectionUtil.setFieldValue(entity.getClass(), "skipAudit", true, entity);
                ReflectionUtil.setFieldValue(entity.getClass(), "skipAuthorization", true, entity);
                entityManager.merge(entity);
            } else if (CrudOperation.Delete.name().equalsIgnoreCase(audit.getAction())) {
                ReflectionUtil.setFieldValue(entity.getClass(), "skipAudit", true, entity);
                ReflectionUtil.setFieldValue(entity.getClass(), "skipAuthorization", true, entity);
                entityManager.remove(entity);
            }
            auditRepository.save(audit);
        }

        ApprovalMail[] mailList = JsonConverter.getGson().fromJson(audit.getApprovalEmail(), ApprovalMail[].class);
        ApprovalPhone[] smsList = JsonConverter.getGson().fromJson(audit.getApprovalSms(), ApprovalPhone[].class);

        for (ApprovalMail mail : mailList) {
            notificationService.sendEmail(NotificationRequest.builder()
                    .message(mail.getMessage())
                    .html(mail.getMessage())
                    .companyCode(null)
                    .recipientEmail(mail.getEmail())
                    .senderEmail(senderMail)
                    .requiredValidation(false)
                    .subject(mail.getSubject())
                    .build());
        }

        for (ApprovalPhone sms : smsList) {
            notificationService.sendSms(NotificationRequest.builder()
                    .message(sms.getMessage())
                    .html(null)
                    .companyCode(null)
                    .recipientPhone(sms.getPhone())
                    .senderPhone(null)
                    .requiredValidation(false)
                    .subject(sms.getSubject())
                    .build());
        }

        return new ApproveAuthorizationResponse();
    }

    private Object getEntity(String entityType, Long entityId) throws ClassNotFoundException {
        Class<?> type = Class.forName(entityType);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        Root<?> fromRoot = criteriaQuery.from(type);
        criteriaQuery.select(fromRoot);

        criteriaQuery.where(criteriaBuilder.equal(fromRoot.get("id"), entityId));

        List<Object> resultList = entityManager.createQuery(criteriaQuery).getResultList();
        Object entity = null;
        if (!resultList.isEmpty()) {
            entity = resultList.get(0);
            log.info("***** ENTITY INFO: {}", JsonConverter.getJsonRecursive(entity));
        }

        return entity;
    }

    public LoanStatusResponse getStatus(String reference) {
        Approval approval = approvalRepository.findByReference(reference);
        if (approval == null) {
            throw new AppException(messageService.getMessage("Approval.NotFound"));
        }
        LoanStatusResponse response = new LoanStatusResponse();
        response.setData(LoanStatusResponse.Data.builder().build());
        response.getData().setReference(approval.getReference());
        response.getData().setApprovalItemType(approval.getApprovalItemType());
        response.getData().setComment(approval.getComment());
        response.getData().setCurrentApprovalStage(approval.getCurrentApprovalStage());
        response.getData().setApprovalStatus(approval.getApprovalStatus());
        return response;
    }

    public GetAuthorizationByIdResponse getAuthorizationRequestById(Long id) throws ClassNotFoundException {
        GetAuthorizationByIdResponse response = new GetAuthorizationByIdResponse();
        Optional<Audit> audit = auditRepository.findById(id);

        if (audit.isEmpty()) {
            throw new AppException(messageService.getMessage("Approval.Request.NotFound"));
        }

        Audit approval = audit.get();

        response.setData(modelMapper.map(approval, GetAuthorizationByIdResponse.Data.class));

        if (approval.getBefore() != null && !approval.getBefore().trim().equals("")) {
            Class<?> responseType = Class.forName(approval.getResponseType());
            response.getData().setInitialState(JsonConverter.getGson().fromJson(approval.getBefore(), responseType));
        }

        if (approval.getAfter() != null && !approval.getAfter().trim().equals("")) {
            Class<?> responseType = Class.forName(approval.getResponseType());
            response.getData().setFinalState(JsonConverter.getGson().fromJson(approval.getAfter(), responseType));
        }
        return response;
    }
}
