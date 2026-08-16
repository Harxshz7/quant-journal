package com.tradingjournal.presentation.webhook;

import com.tradingjournal.application.webhook.WebhookService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.UserRepository;
import com.tradingjournal.presentation.dto.TradingViewWebhookPayload;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks/tradingview")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;
    private final UserRepository userRepository;

    public WebhookController(WebhookService webhookService, UserRepository userRepository) {
        this.webhookService = webhookService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> handleTradingViewAlert(
            @PathVariable String token,
            @RequestBody TradingViewWebhookPayload payload,
            HttpServletRequest request
    ) {
        Optional<User> userOpt = userRepository.findByWebhookToken(token);
        if (userOpt.isEmpty()) {
            log.warn("Webhook received with invalid token — ip={}, token={}", request.getRemoteAddr(), token);
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Invalid webhook token"
            ));
        }

        User user = userOpt.get();
        Map<String, Object> result = webhookService.processTradingViewAlert(user, payload, request);
        return ResponseEntity.ok(result);
    }
}
