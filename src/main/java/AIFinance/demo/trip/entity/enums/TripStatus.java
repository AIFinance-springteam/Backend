package AIFinance.demo.trip.entity.enums;

public enum TripStatus {
    ACTIVE, // 영수증, 상품, 부담내역 수정 가능
    SETTLING, // 정산 확정, 송금 진행 중
    COMPLETED // 모든 송금 확인 후 정산 완료
}
