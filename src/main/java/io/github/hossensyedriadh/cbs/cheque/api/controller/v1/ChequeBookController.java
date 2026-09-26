package io.github.hossensyedriadh.cbs.cheque.api.controller.v1;

import io.github.hossensyedriadh.cbs.cheque.api.command.CancelChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.command.IssueChequeBookCommand;
import io.github.hossensyedriadh.cbs.cheque.api.model.ChequeBookView;
import io.github.hossensyedriadh.cbs.cheque.api.service.ChequeBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/cheque-books/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class ChequeBookController {
    private final ChequeBookService chequeBookService;

    @Autowired
    public ChequeBookController(ChequeBookService chequeBookService) {
        this.chequeBookService = chequeBookService;
    }

    @InitBinder
    private void bind(WebDataBinder binder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<List<ChequeBookView>> getChequeBooks(@PathVariable("id") Long accountId) {
        List<ChequeBookView> chequeBooks = this.chequeBookService.getChequeBooks(accountId);

        if (chequeBooks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(chequeBooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChequeBookView> getChequeBook(@PathVariable("id") Long chequeBookId) {
        Optional<ChequeBookView> chequeBookView = this.chequeBookService.getChequeBook(chequeBookId);

        return chequeBookView.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ChequeBookView> issueChequeBook(@RequestBody IssueChequeBookCommand command) {
        ChequeBookView chequeBookView = chequeBookService.issueChequeBook(command);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(chequeBookView.getId()).toUri();

        return ResponseEntity.created(location).body(chequeBookView);
    }

    @PostMapping(value = "/cancel", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> cancelChequeBook(@RequestBody CancelChequeBookCommand command) {
        this.chequeBookService.cancelChequeBook(command);

        return ResponseEntity.noContent().build();
    }
}
