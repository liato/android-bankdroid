package com.liato.bankdroid.compatibility;

import android.content.Context;

import com.bankdroid.core.repository.ConnectionEntity;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.LegacyBankFactory;
import com.liato.bankdroid.banking.LegacyBankHelper;
import com.liato.bankdroid.banking.exceptions.BankException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @deprecated Only used during migration. Should be removed before next major version (2.0)
 */
@Deprecated
public class ConnectionTransformer {

    private final Context context;

    public ConnectionTransformer(Context context) {
        this.context = context;
    }

    public ConnectionEntity asConnection(Bank bank) {
        return ConnectionEntity.builder()
                .id(bank.getDbId())
                .providerId(LegacyBankHelper.getReferenceFromLegacyId(bank.getBanktypeId()))
                .properties(bank.getProperties())
                .name(bank.getDisplayName())
                .enabled(!bank.isDisabled())
                //TODO map lastUpdated?
                //.lastUpdated()
                // TODO map accounts
                .build();

    }

    public Bank asBank(ConnectionEntity connection) throws BankException {
        Bank bank = LegacyBankFactory.fromBanktypeId(
                LegacyBankHelper.getLegacyIdFromReference(connection.providerId()),
                context
        );
        bank.setDbid(connection.id());
        bank.setCustomName(connection.name());
        bank.setProperties(connection.properties());
        bank.setDisabled(!connection.enabled());
        //TODO map accounts
        //bank.setAccounts();
        return bank;
    }

    public Collection<Bank> asBanks(Collection<ConnectionEntity> connections) throws BankException {
        Collection<Bank> banks = new ArrayList<>();
        if(banks != null) {
            for(ConnectionEntity connection : connections) {
                banks.add(asBank(connection));
            }
        }
        return banks;
    }


}
