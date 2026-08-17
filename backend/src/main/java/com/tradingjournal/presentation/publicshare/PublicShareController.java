package com.tradingjournal.presentation.publicshare;

import com.tradingjournal.application.share.PublicShareService;
import com.tradingjournal.presentation.dto.PublicShareDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicShareController {

    private final PublicShareService publicShareService;

    public PublicShareController(PublicShareService publicShareService) {
        this.publicShareService = publicShareService;
    }

    @GetMapping("/{shareToken}")
    public ResponseEntity<PublicShareDTO> getShare(@PathVariable String shareToken) {
        return ResponseEntity.ok(publicShareService.getShare(shareToken));
    }
}