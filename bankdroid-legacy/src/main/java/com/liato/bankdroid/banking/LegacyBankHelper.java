package com.liato.bankdroid.banking;

import com.liato.bankdroid.provider.IBankTypes;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class LegacyBankHelper {

    private static Map<String, Integer> legacyProviderReferences;
    private static Map<Integer, String> providerReferences;

    public static String getReferenceFromLegacyId(int legacyId) {
        if(providerReferences == null) {
            generateLegacyProviderReferences();
        }
        return providerReferences.get(legacyId);
    }

    // TODO Used during refactoring. Remove before 2.0
    public static int getLegacyIdFromReference(String reference) {
        if(legacyProviderReferences == null) {
            generateLegacyProviderReferences();
        }
        return legacyProviderReferences.get(reference);
    }

    private static void generateLegacyProviderReferences() {
        Map<Integer, String> references = new HashMap<>();
        Map<String, Integer> legacyIds = new HashMap<>();
        Field[] fields = IBankTypes.class.getFields();
        for(int i = 0 ; i < fields.length; i++) {
            Field field = fields[i];
            try {
                String reference = field.getName().toLowerCase().replaceAll("_", "-");
                Integer legacyId = field.getInt(new IBankTypes() {
                });
                references.put(legacyId, reference);
                legacyIds.put(reference, legacyId);
            } catch(IllegalAccessException e) {
                //TODO log if provider could not be mapped.
            }
        }
        legacyProviderReferences = legacyIds;
        providerReferences = references;
    }
}
