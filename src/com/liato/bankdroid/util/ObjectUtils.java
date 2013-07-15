package com.liato.bankdroid.util;

import java.util.Arrays;

public final class ObjectUtils {

    private ObjectUtils() {}
    
    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
      }
    
    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
      }
    
}
