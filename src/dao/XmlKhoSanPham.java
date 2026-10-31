package dao;

import entity.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public final class XmlKhoSanPham implements KhoSanPham {
    private static final long MAX_FILE_SIZE = 32L * 1024 * 1024;
    private final Path file;

    public XmlKhoSanPham(Path file) { this.file = file.toAbsolutePath().normalize(); }

    public static XmlKhoSanPham macDinh() {
        return new XmlKhoSanPham(Path.of(System.getProperty("giathanh.dataDir", "data"), "san-pham.xml"));
    }

    private DocumentBuilder builder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            @Override public void fatalError(org.xml.sax.SAXParseException e) throws SAXException { throw e; }
            @Override public void error(org.xml.sax.SAXParseException e) throws SAXException { throw e; }
        });
        return builder;
    }

    @Override public DuLieuSanPham doc() throws IOException {
        if (!Files.exists(file)) return null;
        if (Files.size(file) > MAX_FILE_SIZE) throw new IOException("File sản phẩm vượt quá 32 MB.");
        try (InputStream input = Files.newInputStream(file)) {
            Element root = builder().parse(input).getDocumentElement();
            if (!root.getTagName().equals("catalog") || !root.getAttribute("version").equals("1")) {
                throw new IOException("Định dạng file sản phẩm không được hỗ trợ.");
            }
            long revision = Long.parseLong(root.getAttribute("revision"));
            if (revision < 1) throw new IllegalArgumentException("Phiên bản dữ liệu không hợp lệ");
            for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node instanceof Element e && !e.getTagName().equals("category") && !e.getTagName().equals("product")) {
                    throw new IOException("File sản phẩm có phần tử không được hỗ trợ: " + e.getTagName());
                }
            }
            List<DanhMuc> categories = new ArrayList<>();
            List<SanPham> products = new ArrayList<>();
            for (Element e : children(root, "category")) {
                categories.add(new DanhMuc(UUID.fromString(e.getAttribute("id")), e.getAttribute("name"), text(e, "description")));
            }
            for (Element e : children(root, "product")) {
                String active = e.getAttribute("active");
                if (!active.equals("true") && !active.equals("false")) throw new IllegalArgumentException("Trạng thái không hợp lệ");
                products.add(new SanPham(UUID.fromString(e.getAttribute("id")), e.getAttribute("sku"), e.getAttribute("name"),
                        UUID.fromString(e.getAttribute("categoryId")), e.getAttribute("brand"),
                        new BigDecimal(e.getAttribute("price")), Integer.parseInt(e.getAttribute("warranty")),
                        Boolean.parseBoolean(active), text(e, "description"), text(e, "image"),
                        Instant.parse(e.getAttribute("created")), Instant.parse(e.getAttribute("updated"))));
            }
            return new DuLieuSanPham(revision, categories, products);
        } catch (ParserConfigurationException | SAXException | IllegalArgumentException | java.time.DateTimeException e) {
            throw new IOException("Không đọc được dữ liệu sản phẩm. File hiện có được giữ nguyên.", e);
        }
    }

    private List<Element> children(Element parent, String tag) {
        List<Element> result = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element element && element.getTagName().equals(tag)) result.add(element);
        }
        return result;
    }

    private String text(Element parent, String tag) throws IOException {
        List<Element> elements = children(parent, tag);
        if (elements.size() != 1) throw new IOException("File sản phẩm thiếu hoặc trùng trường " + tag + ".");
        return elements.get(0).getTextContent();
    }

    @Override public void luu(DuLieuSanPham data, long expectedRevision) throws IOException {
        Files.createDirectories(file.getParent());
        Path lockPath = file.resolveSibling(file.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            try (FileLock lock = channel.tryLock()) {
                if (lock == null) throw new IOException("Dữ liệu đang được lưu ở cửa sổ khác. Vui lòng thử lại.");
                DuLieuSanPham current = doc();
                long actual = current == null ? 0 : current.phienBan();
                if (actual != expectedRevision) {
                    throw new IOException("Dữ liệu đã thay đổi ở cửa sổ khác. Hãy Tải lại dữ liệu trước khi lưu.");
                }
                if (data.phienBan() != expectedRevision + 1) throw new IOException("Phiên bản lưu không hợp lệ.");
                byte[] xml = encode(data);
                if (xml.length > MAX_FILE_SIZE) throw new IOException("Dữ liệu vượt quá 32 MB. Hãy dùng ảnh dung lượng nhỏ hơn.");
                Path temporary = Files.createTempFile(file.getParent(), "san-pham-", ".tmp");
                try {
                    try (FileChannel output = FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(xml);
                        while (buffer.hasRemaining()) output.write(buffer);
                        output.force(true);
                    }
                    Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } finally { Files.deleteIfExists(temporary); }
            }
        } catch (OverlappingFileLockException e) {
            throw new IOException("Dữ liệu đang được lưu. Vui lòng thử lại.", e);
        }
    }

    private byte[] encode(DuLieuSanPham data) throws IOException {
        try {
            Document document = builder().newDocument();
            Element root = document.createElement("catalog");
            root.setAttribute("version", "1");
            root.setAttribute("revision", Long.toString(data.phienBan()));
            document.appendChild(root);
            for (DanhMuc c : data.danhMuc()) {
                Element e = document.createElement("category");
                e.setAttribute("id", c.id().toString()); e.setAttribute("name", c.ten());
                append(document, e, "description", c.moTa()); root.appendChild(e);
            }
            for (SanPham p : data.sanPham()) {
                Element e = document.createElement("product");
                e.setAttribute("id", p.id().toString()); e.setAttribute("sku", p.sku()); e.setAttribute("name", p.ten());
                e.setAttribute("categoryId", p.danhMucId().toString()); e.setAttribute("brand", p.thuongHieu());
                e.setAttribute("price", p.giaBan().toPlainString()); e.setAttribute("warranty", Integer.toString(p.baoHanhThang()));
                e.setAttribute("active", Boolean.toString(p.dangKinhDoanh()));
                e.setAttribute("created", p.taoLuc().toString()); e.setAttribute("updated", p.capNhatLuc().toString());
                append(document, e, "description", p.moTa()); append(document, e, "image", p.anhBase64()); root.appendChild(e);
            }
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(bytes));
            return bytes.toByteArray();
        } catch (ParserConfigurationException | TransformerException e) { throw new IOException("Không ghi được dữ liệu sản phẩm.", e); }
    }

    private void append(Document document, Element parent, String tag, String value) {
        Element element = document.createElement(tag); element.setTextContent(value); parent.appendChild(element);
    }
}
