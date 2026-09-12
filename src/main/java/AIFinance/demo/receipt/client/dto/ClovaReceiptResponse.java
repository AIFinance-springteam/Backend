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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StoreInfo(Value name) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentInfo(DateValue date, TimeValue time) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateValue(String text, FormattedDate formatted) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FormattedDate(String year, String month, String day) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TimeValue(String text, FormattedTime formatted) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FormattedTime(String hour, String minute, String second) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubResult(List<Item> items) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Value name, Value count, PriceInfo price) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PriceInfo(Value price, Value unitPrice) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TotalPrice(Value price, Value creditCardPrice) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(String text, FormattedValue formatted) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FormattedValue(String value) {}
}
