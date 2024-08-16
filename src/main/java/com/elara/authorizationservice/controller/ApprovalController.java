package com.elara.authorizationservice.controller;

import com.elara.authorizationservice.auth.Permission;
import com.elara.authorizationservice.dto.request.*;
import com.elara.authorizationservice.dto.response.*;
import com.elara.authorizationservice.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/approvals")
@RestController
@Tag(name = "Approval Management", description = "Approval Management")
public class ApprovalController {

  private final ApprovalService approvalService;

  public ApprovalController(ApprovalService approvalService) {
    this.approvalService = approvalService;
  }

  @Operation(summary = "View Approval Item Types")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View Approval Item Types",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ViewApprovalItemTypesResponse.class))})})
  @GetMapping("/item/types")
  public ResponseEntity<ViewApprovalItemTypesResponse> getApprovalItemTypes(){
    return ResponseEntity.ok(approvalService.getApprovalItemTypes());
  }

  @Operation(summary = "View Approval Actions")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View Approval Actions",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ViewApprovalActionsResponse.class))})})
  @GetMapping("/actions")
  public ResponseEntity<ViewApprovalActionsResponse> getApprovalActions(){
    return ResponseEntity.ok(approvalService.getApprovalActions());
  }

  @Operation(summary = "View Approval Status")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View Approval Status",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = GetApprovalStatusResponse.class))})})
  @GetMapping("/status")
  public ResponseEntity<GetApprovalStatusResponse> getApprovalStatus(){
    return ResponseEntity.ok(approvalService.getApprovalStatus());
  }

  @Operation(summary = "View Approval Item Setups")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View Approval Item Setups",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = GetApprovalItemSetupsResponse.class))})})
  @GetMapping("/item/setup")
  public ResponseEntity<GetApprovalItemSetupsResponse> getApprovalItemSetups(){
    return ResponseEntity.ok(approvalService.getApprovalItemSetups());
  }

  @Operation(summary = "View Approval Item Setup By Id")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View Approval Item Setup By Id",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = GetApprovalItemSetupByIdResponse.class))})})
  @GetMapping("/item/setup/{id}")
  public ResponseEntity<GetApprovalItemSetupByIdResponse> getApprovalItemSetupById(@PathVariable Long id){
    return ResponseEntity.ok(approvalService.getApprovalItemSetupById(id));
  }

  @Operation(summary = "Create or Update Approval Item Setup")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Create or Update Approval Item Setup",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = CreateApprovalItemSetupResponse.class))})})
  @Permission("CREATE_APPROVAL_ITEM_SETUP")
  @PostMapping("/item/setup")
  public ResponseEntity<CreateApprovalItemSetupResponse> createApprovalItemSetup(@RequestBody CreateApprovalItemSetupRequest request){
    return ResponseEntity.ok(approvalService.saveApprovalItemSetup(request));
  }

  @Operation(summary = "View Approvals")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "View Approvals",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ApproveRequestResponse.class))})})
  @Permission("VIEW_APPROVALS")
  @GetMapping
  public ResponseEntity<GetApprovalsResponse> getApprovals(@RequestParam("approvalStatus") String approvalStatus, @RequestParam("approvalItemType") String approvalItemType){
    return ResponseEntity.ok(approvalService.getApprovals(approvalItemType, approvalStatus));
  }

  @Operation(summary = "Filter Approvals")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Filter Approvals",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = FilterApprovalResponse.class))})})
  @Permission("FILTER_APPROVALS")
  @PostMapping("/filter")
  public ResponseEntity<FilterApprovalResponse> filterApprovals(@RequestBody FilterApprovalRequest dto){
    return ResponseEntity.ok(approvalService.filterApproval(dto));
  }

  @Operation(summary = "Approve Request")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Approve Request",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ApproveRequestResponse.class))})})
  @Permission("APPROVE_REQUEST")
  @PostMapping(value = "/approve", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApproveRequestResponse> approveRequest(@RequestBody ApproveRequest request){
    return ResponseEntity.ok(approvalService.approveRequest(request));
  }

  @Operation(summary = "Submit Approval Request")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Submit Approval Request",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = SubmitApprovalResponse.class))})})
  @PostMapping("/submit")
  public ResponseEntity<SubmitApprovalResponse> submitApproval(@RequestBody SubmitApprovalRequest request){
    return ResponseEntity.ok(approvalService.submitApprovalRequest(request));
  }

  @Operation(summary = "Book Approval Request")
  @Permission("BOOK_APPROVAL_ITEM")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Book Approval Request",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = BookApprovalResponse.class))})})
  @PostMapping("/{reference}/book")
  public ResponseEntity<BookApprovalResponse> bookApproval(@PathVariable String reference){
    return ResponseEntity.ok(approvalService.bookApprovalRequest(reference));
  }

  @Operation(summary = "Unbook Approval Request")
  @Permission("UNBOOK_APPROVAL_ITEM")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Unbook Approval Request",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = BookApprovalResponse.class))})})
  @PostMapping("/{reference}/unbook")
  public ResponseEntity<BookApprovalResponse> unBookApproval(@PathVariable String reference){
    return ResponseEntity.ok(approvalService.unBookApprovalRequest(reference));
  }

  @Operation(summary = "Filter Authorization Requests")
  @Permission("FILTER_AUTHORIZATION_REQUESTS")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Filter Authorization Requests",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = FilterAuthorizationResponse.class))})})
  @PostMapping("/requests/filter")
  public ResponseEntity<FilterAuthorizationResponse> filterApprovalRequests(@RequestBody FilterAuthorizationRequest dto) throws ClassNotFoundException {
    return ResponseEntity.ok(approvalService.filterAuthorizationRequest(dto));
  }

  @Operation(summary = "Get Authorization By Id")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Get Authorization By Id",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = GetAuthorizationByIdResponse.class))})})
  @GetMapping("/requests/{id}")
  public ResponseEntity<GetAuthorizationByIdResponse> getAuthorizationById(@PathVariable("id") Long id) throws ClassNotFoundException {
    return ResponseEntity.ok(approvalService.getAuthorizationRequestById(id));
  }

  @Operation(summary = "Approve Authorization Requests")
  @Permission("APPROVE_AUTHORIZATION_REQUESTS")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Approve Authorization Requests",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ApproveAuthorizationResponse.class))})})
  @PostMapping("/requests/approve")
  public ResponseEntity<ApproveAuthorizationResponse> approveApprovalRequests(@RequestBody ApproveAuthorizationRequest dto) throws ClassNotFoundException {
    return ResponseEntity.ok(approvalService.approveAuthorizationRequest(dto));
  }

  @Operation(summary = "Loan Status")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Loan Status",
                  content = {@Content(mediaType = "application/json",
                          schema = @Schema(implementation = ApproveRequestResponse.class))})})
  @GetMapping(value = "/loan/{reference}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LoanStatusResponse> loanStatus(@PathVariable("reference") String reference){
    return ResponseEntity.ok(approvalService.getStatus(reference));
  }
}
