package service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.*;
import javax.imageio.ImageIO;

public final class ExportService {
    private ExportService() {}
    private static String xml(Object s) { return String.valueOf(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
    /** Workbook OOXML thật, không đổi đuôi CSV thành Excel. Chuỗi luôn là inlineStr để tránh thực thi công thức. */
    public static void excel(Path file, List<List<Object>> rows) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            entry(zip, "[Content_Types].xml", "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
            entry(zip, "_rels/.rels", "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            entry(zip, "xl/workbook.xml", "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Bao cao\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            entry(zip, "xl/_rels/workbook.xml.rels", "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
            StringBuilder sheet = new StringBuilder("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><cols><col min=\"1\" max=\"1\" width=\"48\" customWidth=\"1\"/><col min=\"2\" max=\"10\" width=\"22\" customWidth=\"1\"/></cols><sheetData>");
            for (List<Object> row : rows) { sheet.append("<row>"); for (Object cell : row) {
                if (cell instanceof Number) sheet.append("<c><v>").append(cell).append("</v></c>");
                else sheet.append("<c t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(xml(cell)).append("</t></is></c>");
            } sheet.append("</row>"); }
            entry(zip, "xl/worksheets/sheet1.xml", sheet.append("</sheetData></worksheet>").toString());
        }
    }
    private static void entry(ZipOutputStream z, String name, String data) throws IOException {
        z.putNextEntry(new ZipEntry(name)); z.write(data.getBytes(StandardCharsets.UTF_8)); z.closeEntry();
    }
    /** PDF A4 nhiều trang; vẽ bằng font Java để giữ đúng dấu tiếng Việt mà không cần thư viện ngoài. */
    public static void pdf(Path file, List<String> content) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D metricsGraphics = measure.createGraphics();
        metricsGraphics.setFont(new Font("SansSerif", Font.PLAIN, 22));
        FontMetrics metrics = metricsGraphics.getFontMetrics();
        for (String line : content) {
            String remaining = line;
            while (metrics.stringWidth(remaining) > 1110) {
                int limit = remaining.length();
                while (limit > 1 && metrics.stringWidth(remaining.substring(0, limit)) > 1110) limit--;
                int cut = remaining.lastIndexOf(' ', limit); if (cut < 1) cut = limit;
                lines.add(remaining.substring(0, cut)); remaining = remaining.substring(cut).stripLeading();
            }
            lines.add(remaining);
        }
        metricsGraphics.dispose();
        int pages = Math.max(1, (lines.size() + 49) / 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream(); List<Integer> offsets = new ArrayList<>();
        write(out, "%PDF-1.4\n"); offsets.add(0);
        object(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder kids = new StringBuilder(); for (int p = 0; p < pages; p++) kids.append(3 + p * 3).append(" 0 R ");
        object(out, offsets, 2, "<< /Type /Pages /Count " + pages + " /Kids [" + kids + "] >>");
        for (int p = 0; p < pages; p++) {
            BufferedImage img = new BufferedImage(1240, 1754, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics(); g.setColor(Color.WHITE); g.fillRect(0, 0, 1240, 1754);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.BLACK); g.setFont(new Font("SansSerif", Font.PLAIN, 22));
            for (int i = 0; i < 50 && p * 50 + i < lines.size(); i++) g.drawString(lines.get(p * 50 + i), 65, 85 + i * 31);
            g.drawString("Gia Thành  |  Trang " + (p + 1) + "/" + pages, 65, 1685); g.dispose();
            ByteArrayOutputStream jpeg = new ByteArrayOutputStream(); ImageIO.write(img, "jpg", jpeg); byte[] bytes = jpeg.toByteArray();
            int id = 3 + p * 3;
            object(out, offsets, id, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /XObject << /Im0 " + (id + 2) + " 0 R >> >> /Contents " + (id + 1) + " 0 R >>");
            String draw = "q 595 0 0 842 0 0 cm /Im0 Do Q";
            object(out, offsets, id + 1, "<< /Length " + draw.length() + " >>\nstream\n" + draw + "\nendstream");
            offsets.add(out.size()); write(out, (id + 2) + " 0 obj\n<< /Type /XObject /Subtype /Image /Width 1240 /Height 1754 /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length " + bytes.length + " >>\nstream\n");
            out.write(bytes); write(out, "\nendstream\nendobj\n");
        }
        int xref = out.size(); write(out, "xref\n0 " + offsets.size() + "\n0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) write(out, String.format("%010d 00000 n \n", offsets.get(i)));
        write(out, "trailer\n<< /Size " + offsets.size() + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF");
        Files.write(file, out.toByteArray());
    }
    private static void object(ByteArrayOutputStream out, List<Integer> offsets, int id, String body) throws IOException {
        offsets.add(out.size()); write(out, id + " 0 obj\n" + body + "\nendobj\n");
    }
    private static void write(ByteArrayOutputStream out, String text) throws IOException { out.write(text.getBytes(StandardCharsets.US_ASCII)); }
}
