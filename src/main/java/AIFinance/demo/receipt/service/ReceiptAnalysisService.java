package AIFinance.demo.receipt.service;

import AIFinance.demo.receipt.client.ClovaOcrClient;
import AIFinance.demo.receipt.client.dto.ClovaReceiptResponse;
import AIFinance.demo.receipt.dto.ReceiptAnalysisResponse;
import AIFinance.demo.receipt.entity.*;
import AIFinance.demo.receipt.exception.ReceiptItemException;
import AIFinance.demo.receipt.exception.code.ReceiptItemErrorCode;
import AIFinance.demo.receipt.repository.ReceiptRepository;
import AIFinance.demo.trip.entity.*;
import AIFinance.demo.trip.entity.enums.*;
import AIFinance.demo.trip.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ReceiptAnalysisService {
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ReceiptRepository receiptRepository;
    private final ClovaOcrClient clovaOcrClient;

    @Transactional
    public ReceiptAnalysisResponse analyze(Long userId, Long tripId, MultipartFile file) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.TRIP_NOT_FOUND));
        if (trip.getStatus() != TripStatus.ACTIVE) throw new ReceiptItemException(ReceiptItemErrorCode.INVALID_TRIP_STATUS);
        TripMember member = tripMemberRepository.findByTrip_IdAndUser_IdAndStatus(tripId, userId, TripMemberStatus.ACTIVE)
                .orElseThrow(() -> new ReceiptItemException(ReceiptItemErrorCode.TRIP_MEMBER_REQUIRED));
        if (file == null || file.isEmpty()) throw new ReceiptItemException(ReceiptItemErrorCode.INVALID_IMAGE);
        Receipt receipt = receiptRepository.save(Receipt.builder().trip(trip).uploaderMember(member).build());
        receipt.startAnalysis();
        try {
            ClovaReceiptResponse response = clovaOcrClient.analyze(file.getBytes(), Objects.requireNonNullElse(file.getOriginalFilename(), "receipt.jpg"),
                    Objects.requireNonNullElse(file.getContentType(), "image/jpeg"));
            ClovaReceiptResponse.Image image = response != null && response.images() != null && !response.images().isEmpty() ? response.images().get(0) : null;
            if (image == null || !"SUCCESS".equals(image.inferResult()) || image.receipt() == null || image.receipt().result() == null) throw new ReceiptItemException(ReceiptItemErrorCode.OCR_FAILED);
            ClovaReceiptResponse.Result result = image.receipt().result();
            Long total = amount(result.totalPrice() == null ? null : result.totalPrice().price());
            LocalDateTime paidAt = parseDate(result.paymentInfo() == null ? null : result.paymentInfo().date());
            String merchant = text(result.storeInfo() == null ? null : result.storeInfo().name());
            if (result.subResults() != null) for (var group : result.subResults()) if (group.items() != null) for (var item : group.items()) {
                String name = text(item.name()); if (name == null || name.isBlank()) continue;
                int quantity = integer(item.count() == null ? null : item.count().text(), 1);
                Long unit = amount(item.price() == null ? null : item.price().unitPrice());
                Long original = amount(item.price() == null ? null : item.price().price());
                if (original == null && unit != null) original = unit * quantity;
                if (original == null) continue;
                receipt.addItem(ReceiptItem.builder().itemName(name).quantity(quantity).unitPrice(unit).originalAmount(original).settlementAmount(original).build());
            }
            receipt.completeAnalysis(merchant, paidAt, total);
            return ReceiptAnalysisResponse.from(receipt);
        } catch (ReceiptItemException e) { receipt.failAnalysis(); throw e;
        } catch (IOException | RuntimeException e) { receipt.failAnalysis(); throw new ReceiptItemException(ReceiptItemErrorCode.OCR_FAILED); }
    }
    private String text(ClovaReceiptResponse.Value v) { return v == null ? null : v.formatted() != null && v.formatted().value() != null ? v.formatted().value() : v.text(); }
    private Long amount(ClovaReceiptResponse.Value v) { String s = text(v); if (s == null) return null; try { return Long.valueOf(s.replaceAll("[^0-9-]", "")); } catch (NumberFormatException e) { return null; } }
    private int integer(ClovaReceiptResponse.Value v, int fallback) { Long n = amount(v); return n == null ? fallback : n.intValue(); }
    private LocalDateTime parseDate(ClovaReceiptResponse.DateValue d) { if (d == null || d.formatted() == null) return null; try { return LocalDate.parse(d.formatted().year()+"-"+d.formatted().month()+"-"+d.formatted().day()).atStartOfDay(); } catch (RuntimeException e) { return null; } }
}
