package cn.miskk.blog.gateway.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 静态资源转发：/api/static/{user|article}/** → 对应服务的 /uploads/**（字节流透传）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StaticHandler {

    private final RestTemplate loadBalancedRestTemplate;

    /**
     * @param serviceId Nacos 服务名（miskk-user / miskk-blog）
     * @param remainder 匹配 ** 后的剩余路径，如 uploads/article/202610/x.png
     */
    public ResponseEntity<byte[]> forward(String serviceId, String remainder) {
        String path = remainder == null ? "" : (remainder.startsWith("/") ? remainder : "/" + remainder);
        if (!path.startsWith("/uploads/")) {
            return notFound();
        }
        try {
            ResponseEntity<byte[]> resp = loadBalancedRestTemplate
                    .getForEntity("http://" + serviceId + path, byte[].class);
            return ResponseEntity.status(resp.getStatusCode())
                    .contentType(resp.getHeaders().getContentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM
                            : resp.getHeaders().getContentType())
                    .body(resp.getBody());
        } catch (HttpStatusCodeException e) {
            log.debug("静态资源不存在: {}{}", serviceId, path);
            return notFound();
        }
    }

    private ResponseEntity<byte[]> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
