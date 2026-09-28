package io.github.hossensyedriadh.cbs.eod.api.controller;

import io.github.hossensyedriadh.cbs.eod.api.model.EodRunResult;
import io.github.hossensyedriadh.cbs.eod.api.service.EndOfDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/eod/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class EodController {
    private final EndOfDayService endOfDayService;

    @Autowired
    public EodController(EndOfDayService endOfDayService) {
        this.endOfDayService = endOfDayService;
    }

    @PostMapping("/")
    public ResponseEntity<EodRunResult> run() throws InterruptedException {
        EodRunResult result = this.endOfDayService.run();

        return ResponseEntity.ok(result);
    }
}
