package io.github.hossensyedriadh.cbs.ledger.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.ledger.api.command.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.command.UpdateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfiguration.class)
public interface LedgerAccountMapper {
    LedgerAccountView toView(LedgerAccount entity);

    LedgerAccount toEntity(CreateLedgerAccountCommand command);

    void toPatchedEntity(UpdateLedgerAccountCommand command, @MappingTarget LedgerAccount entity);
}
