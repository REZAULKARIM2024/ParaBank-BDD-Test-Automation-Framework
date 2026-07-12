package utils;

import java.io.InputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
    public static Object[][] getTestData(String fileName, String sheetName) {
        try {
            InputStream is = ExcelUtils.class.getClassLoader().getResourceAsStream(fileName);
            if (is == null) {
                throw new RuntimeException("Resource not found: " + fileName);
            }
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(sheetName);
            int rowCount = sheet.getPhysicalNumberOfRows();
            int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
            Object[][] data = new Object[rowCount - 1][colCount];
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row.getCell(j);
                    data[i - 1][j] = cell == null ? "" : cell.toString();
                }
            }
            workbook.close();
            return data;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}