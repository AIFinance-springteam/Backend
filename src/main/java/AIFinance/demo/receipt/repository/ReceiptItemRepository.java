package AIFinance.demo.receipt.repository;

import AIFinance.demo.receipt.entity.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> cc264a4 (feat: 추가 비용 항목 삭제 API 구현)
import java.util.Optional;


public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {

    Optional<ReceiptItem> findByIdAndReceipt_Id(Long itemId, Long receiptId);
<<<<<<< HEAD
    List<ReceiptItem> findByReceipt_Id(Long receiptId);
    List<ReceiptItem> findByReceipt_IdIn(List<Long> receiptIds);
=======

>>>>>>> cc264a4 (feat: 추가 비용 항목 삭제 API 구현)
}
