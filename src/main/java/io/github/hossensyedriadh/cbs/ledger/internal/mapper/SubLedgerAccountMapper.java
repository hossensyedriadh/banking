package io.github.hossensyedriadh.cbs.ledger.internal.mapper;

import io.github.hossensyedriadh.cbs.configuration.mapper.MapperConfiguration;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.SubLedgerAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapperConfiguration.class)
public interface SubLedgerAccountMapper {
    @Mappings({
            @Mapping(target = "ledgerAccountId", source = "ledgerAccount.id")
    })
    SubLedgerAccountView toView(SubLedgerAccount subLedgerAccount);
}
