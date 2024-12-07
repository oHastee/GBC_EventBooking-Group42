package ca.gbc.approvalservice.controller;

import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalRequestStaff;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
public class ApprovalController {
    private final ApprovalService approvalService;

    @PostMapping()
    public ResponseEntity<ApprovalResponse> createApproval(@RequestBody ApprovalRequest approvalRequest) {
        var response = approvalService.createApproval(approvalRequest);

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/approved")
    public ResponseEntity<List<ApprovalResponse>> getApprovedRequests() {
        return new ResponseEntity<>(approvalService.getApprovedRequests(), HttpStatus.OK);
    }

    @GetMapping("/denied")
    public ResponseEntity<List<ApprovalResponse>> getDeniedRequests() {
        return new ResponseEntity<>(approvalService.getDeniedRequests(), HttpStatus.OK);
    }
    @GetMapping("/pending")
    public ResponseEntity<List<ApprovalResponse>> getPendingRequests() {
        return new ResponseEntity<>(approvalService.getPendingRequests(), HttpStatus.OK);
    }

    @GetMapping("/approved/{eventId}")
    public ResponseEntity<List<ApprovalResponse>> getApprovedRequestsByEventId(@PathVariable String eventId) {
        return new ResponseEntity<>(approvalService.getApprovedRequestsByEventId(eventId), HttpStatus.OK);
    }
    @GetMapping("/pending/{eventId}")
    public ResponseEntity<List<ApprovalResponse>> getPendingRequestsByEventId(@PathVariable String eventId) {
        return new ResponseEntity<>(approvalService.getPendingRequestsByEventId(eventId), HttpStatus.OK);
    }
    @GetMapping("/denied/{eventId}")
    public ResponseEntity<List<ApprovalResponse>> getDeniedRequestsByEventId(@PathVariable String eventId) {
        return new ResponseEntity<>(approvalService.getDeniedRequestsByEventId(eventId), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        return new ResponseEntity<>(approvalService.getAllApprovals(), HttpStatus.OK);
    }
    @GetMapping("/{approvalId}")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable String approvalId) {
        var response = approvalService.getApprovalById(approvalId);
        if (response == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{userId}/{approvalId}")
    public ResponseEntity<ApprovalResponse> setApprovalAction(@PathVariable long userId, @PathVariable String approvalId, @RequestBody ApprovalRequestStaff approvalRequest) {
        if (approvalService.getApprovalById(approvalId) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var response = approvalService.setApprovalAction(userId, approvalId, approvalRequest);
        if (response == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/{approvalId}")
    public ResponseEntity<?> withdrawApproval(@PathVariable long userId, @PathVariable String approvalId) {
        var approval = approvalService.getApprovalById(approvalId);
        if(approval == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        approvalService.withdrawApproval(userId, approval.eventId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
