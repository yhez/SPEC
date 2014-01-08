package codec.util;

import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class JCA {

    protected static Map aliases_ = initAliasLookup();

    private JCA() {
    }


    static private Map initAliasLookup() {
        Enumeration e;
        Provider[] provider;
        String k;
        String v;
        String s;
        String p;
        Map map;
        int i;
        int j;
        map = new HashMap();
        provider = Security.getProviders();
        for (i = provider.length - 1; i >= 0; i--) {
            e = provider[i].propertyNames();

            while (e.hasMoreElements()) {
                k = (String) e.nextElement();
                v = provider[i].getProperty(k);

                if (!k.startsWith("Alg.Alias.")) {
                    continue;
                }
                k = k.substring(10).toLowerCase();
                j = k.indexOf('.');

                if (j < 1) {
                    continue;
                }
                s = k.substring(0, j);
                k = k.substring(j + 1);

                if (k.length() < 1) {
                    continue;
                }
                if (Character.isDigit(k.charAt(0))) {
                    p = (String) map.get("oid." + k);

                    if (p != null && p.length() >= v.length()) {
                        continue;
                    }
                    map.put("oid." + k, v);
                    map.put(s + "." + k, v);
                }
                else if (k.startsWith("oid.")) {
                    k = k.substring(4);
                    v = v.toLowerCase();

                    map.put("oid." + v, k);
                }
                else {
                    map.put(s + "." + k, v);
                }
            }
        }
        return map;
    }


    public static String getName(String oid) {
        if (oid == null) {
            throw new NullPointerException("OID is null!");
        }
        if (oid.startsWith("OID.") || oid.startsWith("oid.")) {
            oid = oid.substring(4);
        }
        return (String) aliases_.get("oid." + oid);
    }


    public static String resolveAlias(String engine, String alias) {
        if (alias == null || engine == null) {
            throw new NullPointerException("Engine or alias is null!");
        }
        if (alias.length() < 1) {
            throw new IllegalArgumentException("Zero-length alias!");
        }
        return (String) aliases_.get(engine.toLowerCase() + "."
                + alias.toLowerCase());
    }


    public static String getOID(String algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm is null!");
        }
        if (algorithm.length() < 1) {
            throw new IllegalArgumentException("Algorithm name is empty!");
        }
        if (Character.isDigit(algorithm.charAt(0))) {
            return algorithm;
        }
        return (String) aliases_.get("oid." + algorithm.toLowerCase());
    }


    public static String getOID(String algorithm, String engine) {
        String oid;

        oid = getOID(algorithm);

        if (oid != null) {
            return oid;
        }
        algorithm = resolveAlias(engine, algorithm);

        if (algorithm == null) {
            return null;
        }
        return getOID(algorithm);
    }
}
