package com.liato.bankdroid.compatibility;

import android.content.Context;

import com.bankdroid.core.repository.AccountEntity;
import com.bankdroid.core.repository.ConnectionEntity;
import com.bankdroid.core.repository.TransactionEntity;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.LegacyBankFactory;
import com.liato.bankdroid.banking.LegacyBankHelper;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.provider.IAccountTypes;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
                .accounts(asAccounts(bank.getAccounts()))
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
        bank.setAccounts(asLegacyAccounts(connection.id(), connection.accounts()));
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

    private Collection<AccountEntity> asAccounts(Collection<Account> accounts) {
        Collection<AccountEntity> accountEntities = new ArrayList<>();
        if(accounts != null) {
            for(Account account : accounts) {
                accountEntities.add(asAccount(account));
            }
        }
        return accountEntities;
    }

    private AccountEntity asAccount(Account account) {
        return AccountEntity.builder()
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .id(account.getId())
                .hidden(account.isHidden())
                .name(account.getName())
                .notifications(account.isNotify())
                .transactions(asTransactions(account.getTransactions()))
                .build();
    }

    private Collection<TransactionEntity> asTransactions(Collection<Transaction> transactions) {
        Collection<TransactionEntity> transactionEntities = new ArrayList<>();
        if(transactions != null) {
            for(Transaction transaction : transactions) {
                transactionEntities.add(asTransaction(transaction));
            }
        }
        return transactionEntities;
    }

    private TransactionEntity asTransaction(Transaction transaction) {
        return TransactionEntity.builder()
                .id(UUID.randomUUID().toString())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getTransaction())
                .date(DateTime.parse(transaction.getDate()))
                .build();
    }

    private ArrayList<Account> asLegacyAccounts(long connectionId, Collection<AccountEntity> accounts) {
        ArrayList<Account> legacyAccounts = new ArrayList<>();
        for(AccountEntity account : accounts) {
            Account legacyAccount = new Account(account.name(),
                    account.balance(),
                    account.id(),
                    connectionId,
                    IAccountTypes.REGULAR);
            legacyAccount.setCurrency(account.currency());
            legacyAccount.setHidden(account.hidden());
            legacyAccount.setNotify(account.notifications());
            legacyAccount.setTransactions(asLegacyTransactions(account.transactions()));
            legacyAccounts.add(legacyAccount);
        }
        return legacyAccounts;
    }

    private List<Transaction> asLegacyTransactions(Collection<TransactionEntity> transactions) {
        List<Transaction> legacyTransactions = new ArrayList<>();
        for(TransactionEntity transaction : transactions) {
            Transaction legacyTransaction = new Transaction(
                    transaction.date().toString("yyyy-MM-dd"),
                    transaction.description(),
                    transaction.amount(),
                    transaction.currency());
            legacyTransactions.add(legacyTransaction);
        }
        return legacyTransactions;
    }
}
