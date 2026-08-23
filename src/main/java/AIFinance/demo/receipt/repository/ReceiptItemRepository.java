package AIFinance.demo.receipt.repository;

import AIFinance.demo.receipt.entity.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {

    Optional<ReceiptItem> findByIdAndReceipt_Id(Long itemId, Long receiptId);

    List<ReceiptItem> findByReceipt_Id(Long receiptId);

    List<ReceiptItem> findByReceipt_IdIn(List<Long> receiptIds);
}
