package net.cms.app.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;

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

    public static int parseNullInt(Object o)
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

    public static boolean isSupportedFileType(String contentType) {
        String[] supportedTypes = {
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PPTX
                "text/plain", // TXT
                "application/pdf", // PDF
                "application/vnd.ms-excel" // XLS
        };

        for (String supportedType : supportedTypes) {
            if (contentType.equals(supportedType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFileSize(long size) {
        return size <= 10 * 1024 * 1024; // 5 MB
    }
}
