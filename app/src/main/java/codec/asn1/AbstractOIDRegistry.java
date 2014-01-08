package codec.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


public abstract class AbstractOIDRegistry extends OIDRegistry {

    protected static void loadOIDMap(Map map, String path) {
        loadOIDMap2(map, "codec/asn1");
        loadOIDMap2(map, path);
    }

    protected abstract String getPrefix();

    private static void loadOIDMap2(Map map, String path) {
        ASN1ObjectIdentifier oid;
        InputStream in;
        Properties props;
        Iterator i;
        String key;
        String val;
        int n;

        if ((map == null) || (path == null)) {
            throw new NullPointerException("map or path");
        }

        n = 0;
        in = ClassLoader.getSystemResourceAsStream(path + "/oid0.map");
        if (in == null) {
            in = AbstractOIDRegistry.class.getResourceAsStream(path
                    + "/oid0.map");

            if (in == null) {
                in = AbstractOIDRegistry.class.getResourceAsStream("/" + path
                        + "/oid0.map");

                if (in == null) {
                    System.out.println("Warning: could not get resource at "
                            + path);
                }
            }
        }

        while (in != null) {
            try {
                props = new Properties();
                props.load(in);

                for (i = props.keySet().iterator(); i.hasNext(); ) {
                    key = (String) i.next();

                    if (key.indexOf(';') != -1) {
                        key = key.substring(0, key.indexOf(';'));
                    }

                    if (key.endsWith(".")) {
                        continue;
                    }

                    val = props.getProperty(key);
                    oid = new ASN1ObjectIdentifier(key.trim());

                    map.put(oid, val);
                }
            } catch (IOException e) {
                System.err
                        .println("Bad OID map: " + path + "/oid" + n + ".map");
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
            n++;
            in = ClassLoader.getSystemResourceAsStream(path + "/oid" + n
                    + ".map");
        }
        if (map.size() == 0) {
            System.err.println("Warning: no OIDs loaded from " + path);
        }
    }
    public AbstractOIDRegistry() {
        this(null);
    }
    public AbstractOIDRegistry(OIDRegistry parent) {
        super(parent);
    }
    protected abstract Map getOIDMap();

    protected ASN1Type getLocalASN1Type(ASN1ObjectIdentifier oid) {
        Object o;
        Class c;
        Map map;
        map = getOIDMap();
        o = map.get(oid);
        if (o == null) {
            return null;
        }
        try {
            if (o instanceof String) {
                c = Class.forName(getPrefix() + o);
                map.put(new ASN1ObjectIdentifier(oid.getOID()), c);
                o = c;
            }
            c = (Class) o;

            return (ASN1Type) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
