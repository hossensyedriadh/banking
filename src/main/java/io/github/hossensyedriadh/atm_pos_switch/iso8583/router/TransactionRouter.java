package io.github.hossensyedriadh.atm_pos_switch.iso8583.router;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.handler.TransactionHandler;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TransactionRouter {
    private final Map<String, TransactionHandler> transactionHandlers;

    @Autowired
    public TransactionRouter(List<TransactionHandler> transactionHandlers) {
        this.transactionHandlers = transactionHandlers.stream().collect(Collectors.toMap(TransactionHandler::processingCode,
                Function.identity()));
    }

    public ISOMsg route(ISOMsg request) {
        String processingCode = request.getString(3);

        TransactionHandler handler = this.transactionHandlers.get(processingCode);

        if (handler == null) {
            return null;
        }

        return handler.handle(request);
    }
}
