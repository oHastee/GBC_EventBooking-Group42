package ca.gbc.approvalservice.repository;

import ca.gbc.approvalservice.model.Approval;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApprovalRepository extends MongoRepository<Approval, String> {
    public List<Approval> findAllByActionContaining(String action);
    public List<Approval> findByActionAndEventId(String action, String eventId);
    public Approval findByApprovalId(String approvalId);
}
