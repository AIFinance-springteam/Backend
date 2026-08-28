package AIFinance.demo.receipt.repository;

import AIFinance.demo.receipt.entity.ItemShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemShareRepository extends JpaRepository<ItemShare, Long> {

    List<ItemShare> findByItem_Id(Long itemId);

    void deleteByItem_Id(Long itemId);

    boolean existsByItem_Id(Long itemId);

    List<ItemShare> findByItem_IdInAndTripMember_Id(List<Long> itemIds, Long tripMemberId);
}