package com.example.serverapp.service;

import com.example.serverapp.model.MaintenanceSchedule;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfGenerationService {

    public byte[] generateMonthlyReportPdf(List<MaintenanceSchedule> schedules, String month, int year) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);

        document.open();
        document.add(new Paragraph("Laporan Pemeliharaan Bulanan", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("Bulan: " + month + " Tahun: " + year, FontFactory.getFont(FontFactory.HELVETICA, 14)));
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addTableHeader(table);

        for (MaintenanceSchedule schedule : schedules) {
            addScheduleToTable(table, schedule);
        }

        document.add(table);
        document.close();
        return outputStream.toByteArray();
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"Mesin", "Tanggal", "Jenis Pemeliharaan", "Teknisi", "Status", "Deskripsi"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private void addScheduleToTable(PdfPTable table, MaintenanceSchedule schedule) {
        table.addCell(schedule.getMesin().getEntityName());
        table.addCell(schedule.getTanggal().toString());
        table.addCell(schedule.getAction());
        table.addCell(schedule.getTechnician());
        table.addCell(schedule.getStatus());
        table.addCell(schedule.getDescription());
    }
}
