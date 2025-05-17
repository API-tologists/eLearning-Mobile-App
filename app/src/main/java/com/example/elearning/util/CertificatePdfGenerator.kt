package com.example.elearning.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import com.example.elearning.model.Certificate
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CertificatePdfGenerator(private val context: Context) {
    private val TAG = "CertificatePdfGenerator"

    fun generatePdf(certificate: Certificate): File? {
        try {
            val pdfDocument = PrintedPdfDocument(
                context,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )

            val page = pdfDocument.startPage(0)
            val canvas = page.canvas

            // Set background color
            canvas.drawColor(android.graphics.Color.WHITE)

            // Create paint objects
            val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(33, 150, 243) // Material Blue
                textSize = 40f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            val subtitlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(97, 97, 97) // Material Grey
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val boldTextPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 24f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }

            // Draw border
            val borderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(33, 150, 243)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 5f
            }
            canvas.drawRect(50f, 50f, canvas.width - 50f, canvas.height - 50f, borderPaint)

            // Draw content
            val centerX = canvas.width / 2f
            var y = 150f

            // Title
            canvas.drawText("Certificate of Completion", centerX, y, titlePaint)
            y += 80f

            // Certificate Number
            canvas.drawText("Certificate No: ${certificate.certificateNumber}", centerX, y, subtitlePaint)
            y += 80f

            // Main content
            canvas.drawText("This is to certify that", centerX, y, textPaint)
            y += 50f

            canvas.drawText(certificate.studentName, centerX, y, boldTextPaint)
            y += 50f

            canvas.drawText("has successfully completed the course", centerX, y, textPaint)
            y += 50f

            canvas.drawText(certificate.courseName, centerX, y, boldTextPaint)
            y += 80f

            // Date and Instructor
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(certificate.issueDate))

            canvas.drawText("Instructor: ${certificate.instructorName}", centerX - 200f, y, textPaint)
            canvas.drawText("Date: $formattedDate", centerX + 200f, y, textPaint)
            y += 80f

            // Verification note
            canvas.drawText(
                "This certificate can be verified using the certificate number",
                centerX,
                y,
                subtitlePaint
            )

            pdfDocument.finishPage(page)

            // Create PDF file
            val fileName = "certificate_${certificate.certificateNumber}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF", e)
            return null
        }
    }
} 