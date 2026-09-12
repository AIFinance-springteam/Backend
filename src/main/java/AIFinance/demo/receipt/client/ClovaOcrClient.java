package AIFinance.demo.receipt.client;

import AIFinance.demo.receipt.client.dto.ClovaReceiptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClovaOcrClient {
    private final RestClient.Builder restClientBuilder;
    @Value("${clova.ocr.invoke-url}") private String invokeUrl;
    @Value("${clova.ocr.secret-key}") private String secretKey;

    public ClovaReceiptResponse analyze(byte[] image, String filename, String contentType) {
        String format = formatOf(filename, contentType);
        String message = "{\"version\":\"V2\",\"requestId\":\"" + UUID.randomUUID()
                + "\",\"timestamp\":" + System.currentTimeMillis()
                + ",\"images\":[{\"format\":\"" + format + "\",\"name\":\"receipt\"}]}";
        ByteArrayResource resource = new ByteArrayResource(image) {
            @Override public String getFilename() { return filename; }
        };
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", message);
        body.add("file", new HttpEntity<>(resource, fileHeaders));
        return restClientBuilder.build().post().uri(invokeUrl).header("X-OCR-SECRET", secretKey)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve()
                .body(ClovaReceiptResponse.class);
    }

    private String formatOf(String filename, String contentType) {
        String ext = filename != null && filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("pdf") || ext.equals("tif") || ext.equals("tiff")) return ext;
        if (contentType != null && contentType.equals("image/jpeg")) return "jpg";
        if (contentType != null && contentType.equals("image/png")) return "png";
        throw new IllegalArgumentException("Unsupported image format");
    }
}
