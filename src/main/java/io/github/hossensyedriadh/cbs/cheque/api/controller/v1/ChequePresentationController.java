package io.github.hossensyedriadh.cbs.cheque.api.controller.v1;

import io.github.hossensyedriadh.cbs.cheque.api.command.PresentChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.ReturnChequeCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequePresentationView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequePresentationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/cheque-presentations/v1", consumes = {MediaType.APPLICATION_JSON_VALUE})
public class ChequePresentationController {
    private final ChequePresentationService chequePresentationService;

    @Autowired
    public ChequePresentationController(ChequePresentationService chequePresentationService) {
        this.chequePresentationService = chequePresentationService;
    }

    @InitBinder
    private void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/cheque/{chequeId}")
    public ResponseEntity<List<ChequePresentationView>> getPresentations(@PathVariable("chequeId") Long chequeId) {
        List<ChequePresentationView> chequePresentations = this.chequePresentationService.getPresentations(chequeId);

        if (chequePresentations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequePresentations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChequePresentationView> getPresentation(@PathVariable("id") Long id) {
        Optional<ChequePresentationView> chequePresentationView = this.chequePresentationService.getPresentation(id);

        return chequePresentationView.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/presentation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequePresentationView> presentCheque(@RequestBody PresentChequeCommand command) {
        ChequePresentationView presented = this.chequePresentationService.presentCheque(command);

        return ResponseEntity.ok(presented);
    }

    @PostMapping(value = "/acceptance/{id}")
    public ResponseEntity<ChequePresentationView> acceptCheque(@PathVariable("id") Long id) {
        ChequePresentationView accepted = this.chequePresentationService.acceptCheque(id);

        return ResponseEntity.ok(accepted);
    }

    @PostMapping(value = "/payment/{id}")
    public ResponseEntity<ChequePresentationView> payCheque(@PathVariable("id") Long id) {
        ChequePresentationView paid = this.chequePresentationService.payCheque(id);

        return ResponseEntity.ok(paid);
    }

    @PostMapping(value = "/return", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequePresentationView> presentCheque(@RequestBody ReturnChequeCommand command) {
        ChequePresentationView returned = this.chequePresentationService.returnCheque(command);

        return ResponseEntity.ok(returned);
    }
}
