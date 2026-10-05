package io.github.hossensyedriadh.cbs.eod.api.controller;

import io.github.hossensyedriadh.cbs.eod.api.model.EodRunResult;
import io.github.hossensyedriadh.cbs.eod.api.service.EndOfDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/eod/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "EOD API", description = "API endpoints for End-of-Day processing")
public class EodController {
    private final EndOfDayService endOfDayService;

    @Autowired
    public EodController(EndOfDayService endOfDayService) {
        this.endOfDayService = endOfDayService;
    }

    @Operation(summary = "Run End-of-Day processing", description = "Run End-of-Day processing steps",
            responses = {
                    @ApiResponse(responseCode = "200", description = "End-of-Day processing result", content = @Content(schema = @Schema(implementation = EodRunResult.class)))
            })
    @PostMapping("/")
    public ResponseEntity<EodRunResult> run() throws InterruptedException {
        EodRunResult result = this.endOfDayService.run();

        return ResponseEntity.ok(result);
    }
}
