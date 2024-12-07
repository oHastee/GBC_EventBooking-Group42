package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.client.EventClient;
import ca.gbc.approvalservice.client.UserClient;
import ca.gbc.approvalservice.dto.*;
import ca.gbc.approvalservice.exception.NotFoundException;
import ca.gbc.approvalservice.exception.ValidationException;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.model.Event;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService{
    private final ApprovalRepository approvalRepository;
    private final EventClient eventClient;
    private final UserClient userClient;
    @Override
    public ApprovalResponse createApproval(ApprovalRequest approvalRequest) {
        log.info("Creating approval {} {} {}", approvalRequest.eventId(), approvalRequest.type(), approvalRequest.action());

        if (!approvalRequest.eventId().equals(approvalRequest.pendingObj().getEventId())) {
            throw new ValidationException("Event IDs don't match");
        }

        Approval approval = Approval.builder()
                .eventId(approvalRequest.eventId())
                .type(approvalRequest.type())
                .pendingObj(approvalRequest.pendingObj())
                .action(approvalRequest.action())
                .build();

        approvalRepository.save(approval);
        return mapToApprovalResponse(approval);
    }

    private ApprovalResponse mapToApprovalResponse(Approval approval) {
        return new ApprovalResponse(approval.getApprovalId(), approval.getEventId(), approval.getType(),
                approval.getPendingObj(), approval.getAction());
    }

    @Override
    public List<ApprovalResponse> getApprovedRequests() {
        return approvalRepository.findAllByActionContaining("approve").stream()
                .map(this::mapToApprovalResponse).toList();
    }

    @Override
    public List<ApprovalResponse> getDeniedRequests() {
        return approvalRepository.findAllByActionContaining("denied").stream()
                .map(this::mapToApprovalResponse).toList();
    }

    @Override
    public List<ApprovalResponse> getPendingRequests() {
        return approvalRepository.findAllByActionContaining("pending").stream()
                .map(this::mapToApprovalResponse).toList();
    }

    @Override
    public List<ApprovalResponse> getApprovedRequestsByEventId(String eventId) {
        return approvalRepository.findByActionAndEventId("approve", eventId).stream()
                .map(this::mapToApprovalResponse).toList();
    }

    @Override
    public List<ApprovalResponse> getDeniedRequestsByEventId(String eventId) {
        return approvalRepository.findByActionAndEventId("denied", eventId).stream()
                .map(this::mapToApprovalResponse).toList();
    }

    @Override
    public List<ApprovalResponse> getPendingRequestsByEventId(String eventId) {
        return approvalRepository.findByActionAndEventId("pending", eventId).stream()
                .map(this::mapToApprovalResponse).toList();
    }

    @Override
    public List<ApprovalResponse> getAllApprovals() {
        List<Approval> approvals = approvalRepository.findAll();
        return approvals.stream().map(this::mapToApprovalResponse).toList();
    }

    @Override
    public ApprovalResponse getApprovalById(String approvalId) {
        Approval approval = approvalRepository.findByApprovalId(approvalId);
        if (approval != null) {
            return mapToApprovalResponse(approval);
        }
        throw new NotFoundException("Approval with " + approvalId + " does not exist");
    }

    @Override
    public ApprovalResponse setApprovalAction(long userId, String approvalId, ApprovalRequestStaff approvalRequest) {
        User user = getUser(userId);
        Approval approval = getApproval(approvalId);
        Event event = approval.getPendingObj();

        EventRequestUpdateApproval eventRequestUpdateApproval = new EventRequestUpdateApproval(
                event.getEventId(),
                event.getEventName(),
                event.getRoomId(),
                event.getEventType(),
                event.getEventStart(),
                event.getEventEnd(),
                event.getExpectedAttendees()
        );

        log.info("Built event request for approval with id {} name {} room {} type {} start {} end {} attendees {}",
                event.getEventId(), event.getEventName(), event.getRoomId(), event.getEventType(),
                event.getEventStart(), event.getEventEnd(), event.getExpectedAttendees());

        if (user.role().equals("staff")) {
            if (approvalRequest.action().equals("approve")) {
                approval.setAction(approvalRequest.action());
                eventClient.updateApprovedEvent(event.getEventId(), eventRequestUpdateApproval);
                return mapToApprovalResponse(approvalRepository.save(approval));
            }
            if (approvalRequest.action().equals("deny")) {
                approval.setAction(approvalRequest.action());
                eventClient.updateRejectedEvent(event.getEventId(), eventRequestUpdateApproval);
                return mapToApprovalResponse(approvalRepository.save(approval));
            }
            else {
                throw new ValidationException("Invalid action request");
            }
        }
        throw new ValidationException("Only staff can approve a request");
    }

    @Override
    public void withdrawApproval(long userId, String eventId) {
        getUser(userId);
        List<Approval> approvals = approvalRepository.findByActionAndEventId("pending", eventId);
        if (!approvals.isEmpty()) {
            EventResponse event = null;
            try {
                event = eventClient.getEvent(eventId);
                if (event == null) {
                    throw new NotFoundException("Event with " + eventId + " does not exist");
                }
            } catch (Exception e) {
                log.warn("Withdraw: Got exception while fetching event with id {}. Message {}", eventId, e.getMessage());
                throw new NotFoundException("Event with " + eventId + " does not exist");
            }

            if(event.organizerId() != userId) {
                throw new ValidationException("Only organizer can withdraw an approval");
            }

            approvalRepository.deleteAll(approvals);
            return;
        }
        throw new NotFoundException("No pending approvals found for event with " + eventId);
    }

    private Approval getApproval(String approvalId) {
        Approval approval = approvalRepository.findByApprovalId(approvalId);
        if (approval == null) {
            throw new NotFoundException("Approval with " + approvalId + " does not exist");
        }
        return approval;
    }

    private User getUser(long userId) {
        User user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User with " + userId + " does not exist");
        }
        return user;
    }
}
