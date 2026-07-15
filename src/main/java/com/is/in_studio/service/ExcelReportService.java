package com.is.in_studio.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.is.in_studio.entity.Membership;
import com.is.in_studio.entity.User;

@Service
public class ExcelReportService {

    private static final String[] HEADERS = {
        "ID", "First Name", "Last Name", "Email",
        "Start Date", "End Date", "Credits Left", "Credits Total", "Status"
    };

    public byte[] buildExpiredMembershipsReport(List<Membership> memberships) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Expired Memberships");

            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Membership m : memberships) {
                Row row = sheet.createRow(rowNum++);
                User user = m.getUser();
                row.createCell(0).setCellValue(m.getMembershipId());
                row.createCell(1).setCellValue(user.getFirstName());
                row.createCell(2).setCellValue(user.getLastName());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(m.getStartDate().toString());
                row.createCell(5).setCellValue(m.getEndDate().toString());
                row.createCell(6).setCellValue(m.getCreditsLeft());
                row.createCell(7).setCellValue(m.getCreditsTotal());
                row.createCell(8).setCellValue(m.getStatus().name());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }
}
