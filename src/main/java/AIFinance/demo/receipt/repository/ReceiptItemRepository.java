package AIFinance.demo.receipt.repository;

import AIFinance.demo.receipt.entity.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {

}
