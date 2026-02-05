package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.service.ReissueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReissueController {
    private final ReissueService reissueService;

    public ReissueController(ReissueService reissueService) {
        this.reissueService = reissueService;
    }


    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return reissueService.reissue(request, response);
    }
}
