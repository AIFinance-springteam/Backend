package AIFinance.demo.receipt.repository;

import AIFinance.demo.receipt.entity.Receipt;
import AIFinance.demo.receipt.entity.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    // 여행방과 영수증 조회
    Optional<Receipt> findByIdAndTrip_Id(Long receiptId, Long tripId);

    List<Receipt> findAllByTrip_IdAndStatusNot(Long tripId, ReceiptStatus status);
    List<Receipt> findByTrip_Id(Long tripId);
}
