package task1;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

interface ReportFormatter {
    String format(String title, String[] headers, String[][] rows);
}

class JsonFormatter implements ReportFormatter {

    private String jsonValue(String value) {
        if (value == null) return "";

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder result = new StringBuilder();

        result.append("{\n");
        result.append("  \"report\": \"")
              .append(jsonValue(title))
              .append("\",\n");
        result.append("  \"records\": [\n");

        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            result.append("    {");
            for (int column = 0; column < headers.length; column++) {
                if (column > 0) result.append(", ");
                String cell = column < rows[rowIndex].length
                        ? rows[rowIndex][column] : "";

                result.append("\"")
                      .append(jsonValue(headers[column]))
                      .append("\": \"")
                      .append(jsonValue(cell))
                      .append("\"");
            }

            result.append("}");

            if (rowIndex + 1 < rows.length) result.append(",");
            result.append("\n");
        }

        result.append("  ]\n}");
        return result.toString();
    }
}

class CsvFormatter implements ReportFormatter {

    private String csvCell(String text) {
        if (text == null) return "\"\"";
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private void appendLine(StringBuilder output, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (i != 0) output.append(",");
            output.append(csvCell(values[i]));
        }
        output.append("\n");
    }
    @Override
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder output = new StringBuilder();

        output.append("# TITLE: ").append(title).append("\n");
        appendLine(output, headers);

        for (String[] record : rows) {
            appendLine(output, record);
        }

        return output.toString();
    }
}

interface Exporter {
    void export(String destination, String data);
}

class BaseFileExporter implements Exporter {

    @Override
    public void export(String destination, String data) {
        int size = data.getBytes(StandardCharsets.UTF_8).length;

        System.out.printf(
                "[DISK WRITE] Writing %d bytes to file://%s%n",
                size, destination
        );

        System.out.println("----- FILE PAYLOAD BEGIN -----");
        System.out.println(data);
        System.out.println("----- FILE PAYLOAD END -----");
    }
}

abstract class ExporterDecorator implements Exporter {
    protected final Exporter wrappedExporter;

    protected ExporterDecorator(Exporter next) {
        if (next == null) {
            throw new IllegalArgumentException("Exporter is required");
        }
        wrappedExporter = next;
    }

    @Override
    public void export(String destination, String data) {
        wrappedExporter.export(destination, data);
    }
}

class HeaderFooterDecorator extends ExporterDecorator {
    private final String timestamp;

    HeaderFooterDecorator(Exporter next, String timestamp) {
        super(next);
        this.timestamp = timestamp;
    }

    @Override
    public void export(String destination, String data) {
        StringBuilder document = new StringBuilder();

        document.append("=== METADATA HEADER: ")
                .append(timestamp)
                .append(" ===\n");

        document.append(data);

        document.append(
                "\n=== INTEGRITY CHECK: SHA-256 OK | END RECORD ==="
        );

        super.export(destination, document.toString());
    }
}

class Base64EncryptDecorator extends ExporterDecorator {

    Base64EncryptDecorator(Exporter next) {
        super(next);
    }

    @Override
    public void export(String destination, String data) {
        byte[] original = data.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getEncoder().encodeToString(original);

        super.export(
                destination,
                "[BASE64_CIPHERTEXT:" + encoded + "]"
        );
    }
}

public class Task1Runner {
    public static void main(String[] args) {
        String[] headers = {"ID", "User", "Role"};

        String[][] rows = {
                {"101", "sufyan", "Admin"},
                {"102", "alex", "Dev"}
        };

        ReportFormatter formatter = new JsonFormatter();
        String report = formatter.format(
                "UserAuditReport", headers, rows
        );

        System.out.println(
                ">>> TESTING DECORATOR STACK: Base64(HeaderFooter(BaseFileExporter)) <<<"
        );

        Exporter exporter = new Base64EncryptDecorator(
                new HeaderFooterDecorator(
                        new BaseFileExporter(),
                        "2026-11-02 10:00:00 UTC"
                )
        );

        exporter.export("/var/logs/audit.sec", report);
    }
}
