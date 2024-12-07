package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalRequestStaff;
import ca.gbc.approvalservice.dto.ApprovalResponse;

import java.util.List;

public interface ApprovalService {
    ApprovalResponse createApproval(ApprovalRequest approvalRequest);
    List<ApprovalResponse> getApprovedRequests();
    List<ApprovalResponse> getDeniedRequests();
    List<ApprovalResponse> getPendingRequests();
    List<ApprovalResponse> getApprovedRequestsByEventId(String eventId);
    List<ApprovalResponse> getDeniedRequestsByEventId(String eventId);
    List<ApprovalResponse> getPendingRequestsByEventId(String eventId);
    List<ApprovalResponse> getAllApprovals();
    ApprovalResponse getApprovalById(String approvalId);
    ApprovalResponse setApprovalAction(long userId, String approvalId, ApprovalRequestStaff approvalRequest);
    void withdrawApproval(long userId, String approvalId);
}
