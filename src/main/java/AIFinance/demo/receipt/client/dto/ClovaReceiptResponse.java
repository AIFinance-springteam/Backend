package AIFinance.demo.receipt.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClovaReceiptResponse(List<Image> images) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String inferResult, Receipt receipt) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Receipt(Result result) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(StoreInfo storeInfo, PaymentInfo paymentInfo, List<SubResult> subResults, TotalPrice totalPrice) {}
    public record StoreInfo(Value name) {}
    public record PaymentInfo(DateValue date, Value time) {}
    public record DateValue(String text, FormattedDate formatted) {}
    public record FormattedDate(String year, String month, String day) {}
    public record SubResult(List<Item> items) {}
    public record Item(Value name, Count count, PriceInfo price) {}
    public record PriceInfo(Value price, Value unitPrice) {}
    public record Count(Value text) {}
    public record TotalPrice(Value price, Value creditCardPrice) {}
    public record Value(String text, FormattedValue formatted) {}
    public record FormattedValue(String value) {}
}
