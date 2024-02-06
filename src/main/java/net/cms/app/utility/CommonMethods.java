package net.cms.app.utility;

public class CommonMethods {
    public static String parseNullString(Object o) {
        if (o == null) {
            return ("");
        }
        String s = o.toString();
        if (s.trim().equalsIgnoreCase("null")) {
            return ("");
        }
        else {
            return (s.trim());
        }
    }

    static int parseNullInt(Object o)
    {
        if (o == null) return 0;
        String s = o.toString();
        if (s.equals("null")) return 0;
        if (s.isEmpty()) return 0;
        return Integer.parseInt(s);
    }
    double parseNullDouble(Object o)
    {
        if (o == null) return 0;
        String s = o.toString();
        if (s.equals("null")) return 0;
        if (s.isEmpty()) return 0;
        return Double.parseDouble(s);
    }

    long parseLong(Object o) {
        String s = parseNullString(o);
        if (!s.isEmpty()) {
            try {
                return Long.parseLong(s);
            }
            catch (Exception e) {
                return 0;
            }
        }
        else {
            return 0;
        }
    }
}
