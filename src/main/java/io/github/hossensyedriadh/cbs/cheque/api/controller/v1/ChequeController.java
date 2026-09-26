package io.github.hossensyedriadh.cbs.cheque.api.controller.v1;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.StopChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/cheques/v1", consumes = {MediaType.APPLICATION_JSON_VALUE})
public class ChequeController {
    private final ChequeService chequeService;

    @Autowired
    public ChequeController(ChequeService chequeService) {
        this.chequeService = chequeService;
    }

    @InitBinder
    private void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ChequeView>> getCheques(@PathVariable("bookId") Long bookId) {
        List<ChequeView> chequeViews = this.chequeService.getCheques(bookId);

        if (chequeViews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequeViews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChequeView> getCheque(@PathVariable("id") Long id) {
        Optional<ChequeView> chequeView = this.chequeService.getCheque(id);

        return chequeView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/number/{chequeNo}")
    public ResponseEntity<ChequeView> getChequeByNumber(@PathVariable("chequeNo") Long chequeNo) {
        Optional<ChequeView> chequeView = this.chequeService.getChequeByNumber(chequeNo);

        return chequeView.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/stop", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> stopCheque(@RequestBody StopChequeCommand command) {
        this.chequeService.stopCheque(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/cancel", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> cancelCheque(@RequestBody CancelChequeCommand command) {
        this.chequeService.cancelCheque(command);

        return ResponseEntity.noContent().build();
    }
}
