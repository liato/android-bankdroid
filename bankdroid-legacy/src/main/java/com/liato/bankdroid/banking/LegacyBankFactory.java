package com.liato.bankdroid.banking;

import com.liato.bankdroid.banking.banks.AkeliusInvest;
import com.liato.bankdroid.banking.banks.AkeliusSpar;
import com.liato.bankdroid.banking.banks.americanexpress.AmericanExpress;
import com.liato.bankdroid.banking.banks.AppeakPoker;
import com.liato.bankdroid.banking.banks.AvanzaMini;
import com.liato.bankdroid.banking.banks.BetterGlobe;
import com.liato.bankdroid.banking.banks.Bioklubben;
import com.liato.bankdroid.banking.banks.BlekingeTrafiken;
import com.liato.bankdroid.banking.banks.Bredband2VoIP;
import com.liato.bankdroid.banking.banks.BrummerKF;
import com.liato.bankdroid.banking.banks.CSN;
import com.liato.bankdroid.banking.banks.Chalmrest;
import com.liato.bankdroid.banking.banks.DanskeBank;
import com.liato.bankdroid.banking.banks.Everydaycard;
import com.liato.bankdroid.banking.banks.FirstCard;
import com.liato.bankdroid.banking.banks.Hemkop;
import com.liato.bankdroid.banking.banks.Hors;
import com.liato.bankdroid.banking.banks.IKEA;
import com.liato.bankdroid.banking.banks.IkanoBank;
import com.liato.bankdroid.banking.banks.Jojo;
import com.liato.bankdroid.banking.banks.McDonalds;
import com.liato.bankdroid.banking.banks.Meniga;
import com.liato.bankdroid.banking.banks.MinPension;
import com.liato.bankdroid.banking.banks.Nordnet;
import com.liato.bankdroid.banking.banks.OKQ8;
import com.liato.bankdroid.banking.banks.Ostgotatrafiken;
import com.liato.bankdroid.banking.banks.Osuuspankki;
import com.liato.bankdroid.banking.banks.Payson;
import com.liato.bankdroid.banking.banks.PlusGirot;
import com.liato.bankdroid.banking.banks.SevenDay;
import com.liato.bankdroid.banking.banks.SveaDirekt;
import com.liato.bankdroid.banking.banks.SvenskaSpel;
import com.liato.bankdroid.banking.banks.TestBank;
import com.liato.bankdroid.banking.banks.TicketRikskortet;
import com.liato.bankdroid.banking.banks.Vasttrafik;
import com.liato.bankdroid.banking.banks.Zidisha;
import com.liato.bankdroid.banking.banks.avanza.Avanza;
import com.liato.bankdroid.banking.banks.bitcoin.Bitcoin;
import com.liato.bankdroid.banking.banks.coop.Coop;
import com.liato.bankdroid.banking.banks.ica.ICA;
import com.liato.bankdroid.banking.banks.lansforsakringar.Lansforsakringar;
import com.liato.bankdroid.banking.banks.nordea.Nordea;
import com.liato.bankdroid.banking.banks.rikslunchen.Rikslunchen;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class LegacyBankFactory {

    public static Bank fromBanktypeId(int id, Context context) throws BankException {
        switch (id) {
            case IBankTypes.TESTBANK:
                return new TestBank(context);
            case IBankTypes.AKELIUSINVEST:
                return new AkeliusInvest(context);
            case IBankTypes.AKELIUSSPAR:
                return new AkeliusSpar(context);
            case IBankTypes.NORDEA:
                return new Nordea(context);
            case IBankTypes.LANSFORSAKRINGAR:
                return new Lansforsakringar(context);
            case IBankTypes.COOP:
                return new Coop(context);
            case IBankTypes.ICA:
                return new ICA(context);
            case IBankTypes.AVANZA:
                return new Avanza(context);
            case IBankTypes.AVANZAMINI:
                return new AvanzaMini(context);
            case IBankTypes.OKQ8:
                return new OKQ8(context);
            case IBankTypes.FIRSTCARD:
                return new FirstCard(context);
            case IBankTypes.PAYSON:
                return new Payson(context);
            case IBankTypes.JOJO:
                return new Jojo(context);
            case IBankTypes.IKANOBANK:
                return new IkanoBank(context);
            case IBankTypes.RIKSLUNCHEN:
                return new Rikslunchen(context);
            case IBankTypes.HEMKOP:
                return new Hemkop(context);
            case IBankTypes.NORDNET:
                return new Nordnet(context);
            case IBankTypes.SEVENDAY:
                return new SevenDay(context);
            case IBankTypes.OSUUSPANKKI:
                return new Osuuspankki(context);
            case IBankTypes.CSN:
                return new CSN(context);
            case IBankTypes.AMERICANEXPRESS:
                return new AmericanExpress(context);
            case IBankTypes.MCDONALDS:
                return new McDonalds(context);
            case IBankTypes.PLUSGIROT:
                return new PlusGirot(context);
            case IBankTypes.IKEA:
                return new IKEA(context);
            case IBankTypes.DANSKEBANK:
                return new DanskeBank(context);
            case IBankTypes.VASTTRAFIK:
                return new Vasttrafik(context);
            case IBankTypes.EVERYDAYCARD:
                return new Everydaycard(context);
            case IBankTypes.MENIGA:
                return new Meniga(context);
            case IBankTypes.RIKSKORTET:
                return new TicketRikskortet(context);
            case IBankTypes.BIOKLUBBEN:
                return new Bioklubben(context);
            case IBankTypes.CHALMREST:
                return new Chalmrest(context);
            case IBankTypes.SVENSKASPEL:
                return new SvenskaSpel(context);
            case IBankTypes.APPEAKPOKER:
                return new AppeakPoker(context);
            case IBankTypes.BRUMMER_KF:
                return new BrummerKF(context);
            case IBankTypes.ZIDISHA:
                return new Zidisha(context);
            case IBankTypes.BETTERGLOBE:
                return new BetterGlobe(context);
            case IBankTypes.BITCOIN:
                return new Bitcoin(context);
            case IBankTypes.SVEADIREKT:
                return new SveaDirekt(context);
            case IBankTypes.BLEKINGETRAFIKEN:
                return new BlekingeTrafiken(context);
            case IBankTypes.OSTGOTATRAFIKEN:
                return new Ostgotatrafiken(context);
            case IBankTypes.BREDBAND2VOIP:
                return new Bredband2VoIP(context);
            case IBankTypes.MINPENSION:
                return new MinPension(context);
            case IBankTypes.HORS:
                return new Hors(context);
            default:
                throw new BankException("BankType id not found: " + id);
        }
    }

    public static List<Bank> listBanks(Context context) {
        List<Bank> banks = new ArrayList<>();
        banks.add(new AkeliusInvest(context));
        banks.add(new AkeliusSpar(context));
        banks.add(new BrummerKF(context));
        banks.add(new Nordea(context));
        banks.add(new Lansforsakringar(context));
        banks.add(new Coop(context));
        banks.add(new ICA(context));
        banks.add(new Avanza(context));
        banks.add(new AvanzaMini(context));
        banks.add(new OKQ8(context));
        banks.add(new FirstCard(context));
        banks.add(new Payson(context));
        banks.add(new Jojo(context));
        banks.add(new IkanoBank(context));
        banks.add(new Rikslunchen(context));
        banks.add(new Hemkop(context));
        banks.add(new Nordnet(context));
        banks.add(new SevenDay(context));
        banks.add(new Osuuspankki(context));
        banks.add(new CSN(context));
        banks.add(new McDonalds(context));
        banks.add(new AmericanExpress(context));
        banks.add(new PlusGirot(context));
        banks.add(new IKEA(context));
        banks.add(new Vasttrafik(context));
        banks.add(new Everydaycard(context));
        banks.add(new Meniga(context));
        banks.add(new TicketRikskortet(context));
        banks.add(new Bioklubben(context));
        banks.add(new Chalmrest(context));
        banks.add(new SvenskaSpel(context));
        banks.add(new AppeakPoker(context));
        banks.add(new Zidisha(context));
        banks.add(new BetterGlobe(context));
        banks.add(new Bitcoin(context));
        banks.add(new SveaDirekt(context));
        banks.add(new BlekingeTrafiken(context));
        banks.add(new Ostgotatrafiken(context));
        banks.add(new Bredband2VoIP(context));
        banks.add(new MinPension(context));
        banks.add(new Hors(context));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("debug_mode", false)) {
            banks.add(new TestBank(context));
        }
        return banks;
    }
}
