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
                "application/pdf", // PDF
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PPTX
                "application/vnd.ms-powerpoint", // PPT
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
                "text/plain", // TXT
                "application/vnd.ms-excel", // XLS
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
                "image/jpeg", // JPEG
                "image/png", // PNG
                "text/csv", // CSV
                "application/xml", // XML
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

    public static String getTemplateType(String request){
        String type = "doc";
        if(request.contains("admin")){
            type="tmp";
        }
        return type;
    }
}
