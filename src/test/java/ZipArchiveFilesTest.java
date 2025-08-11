import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ZipArchiveFilesTest {

    private InputStream resource(String cpPath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(cpPath);
        assertNotNull(is, "Resource not found in classpath: " + cpPath);
        return is;
    }

    private ZipInputStream openZip() {
        return new ZipInputStream(resource("archive.zip"));
    }

    @Test
    @DisplayName("Список файлов в ZIP (smoke)")
    void listZipEntries() throws Exception {
        int filesCount = 0;
        try (ZipInputStream zip = openZip()) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if (!e.isDirectory()) {
                    System.out.println("entry: " + e.getName());
                    filesCount++;
                }
                zip.closeEntry();
            }
        }
        assertThat(filesCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("PDF из архива: проверка количества страниц")
    void pdfFromZip() throws Exception {
        boolean found = false;
        try (ZipInputStream zip = openZip()) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if ("exmple.pdf".equals(e.getName())) {
                    PDF pdf = new PDF(zip);
                    assertEquals(1, pdf.numberOfPages);
                    found = true;
                }
                zip.closeEntry();
            }
        }
        assertTrue(found, "exmple.pdf не найден в archive.zip");
    }

    @Test
    @DisplayName("XLSX из архива: проверка шапки и значений")
    void xlsxFromZip() throws Exception {
        boolean found = false;
        try (ZipInputStream zip = openZip()) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if ("import_ou_xlsx.xlsx".equals(e.getName())) {
                    XLS xls = new XLS(zip);
                    var sh = xls.excel.getSheetAt(0);

                    assertAll(
                            () -> assertEquals("Внешний идентификатор для импорта", sh.getRow(0).getCell(0).getStringCellValue()),
                            () -> assertEquals("Вышестоящий отдел", sh.getRow(0).getCell(1).getStringCellValue()),
                            () -> assertEquals("Название", sh.getRow(0).getCell(2).getStringCellValue())
                    );

                    assertAll(
                            () -> assertEquals("OU001", sh.getRow(1).getCell(0).getStringCellValue()),
                            () -> assertEquals("Коммерческий департамент", sh.getRow(1).getCell(2).getStringCellValue()),
                            () -> assertEquals("Маректинг и реклама", sh.getRow(2).getCell(2).getStringCellValue()),
                            () -> assertEquals("OU006", sh.getRow(6).getCell(0).getStringCellValue())
                    );

                    found = true;
                }
                zip.closeEntry();
            }
        }
        assertTrue(found, "import_ou_xlsx.xlsx не найден в archive.zip");
    }

    @Test
    @DisplayName("CSV из архива: UTF-8 и разделитель ';'")
    void csvFromZip() throws Exception {
        boolean found = false;
        try (ZipInputStream zip = openZip()) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if ("username.csv".equals(e.getName())) {
                    CSVReader reader = new CSVReaderBuilder(
                            new InputStreamReader(zip, StandardCharsets.UTF_8))
                            .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                            .build();

                    List<String[]> rows = reader.readAll();
                    assertEquals(7, rows.size());
                    assertArrayEquals(new String[]{"Username", " Identifier", "First name", "Last name"}, rows.get(0));
                    assertArrayEquals(new String[]{"booker12","9012","Rachel","Booker"}, rows.get(1));
                    assertArrayEquals(new String[]{"grey07","2070","Laura","Grey"}, rows.get(2));

                    found = true;
                }
                zip.closeEntry();
            }
        }
        assertTrue(found, "username.csv не найден в archive.zip");
    }

    @Test
    @DisplayName("Проверка отсутствия лишнего файла в ZIP")
    void missingEntryHandled() throws Exception {
        boolean unexpectedPresent = false;
        try (ZipInputStream zip = openZip()) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                if ("no_such_file.txt".equals(e.getName())) {
                    unexpectedPresent = true;
                }
                zip.closeEntry();
            }
        }
        assertFalse(unexpectedPresent, "В архиве найден неожиданный файл no_such_file.txt");
    }
}
