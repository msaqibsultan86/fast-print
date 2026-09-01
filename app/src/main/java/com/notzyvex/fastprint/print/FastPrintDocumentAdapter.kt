package com.notzyvex.fastprint.print

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.util.Log
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.PrintSettings
import java.io.FileOutputStream
import java.io.IOException

private const val TAG = "FastPrintAdapter"

/**
 * Renders the job to a PDF that the Android print framework hands to the chosen print service.
 *
 * Copies are emitted as repeated pages rather than left to PrintAttributes. The framework has no
 * copies field on PrintAttributes — copy count is the print service's business, and support is
 * inconsistent — so duplicating the page is the only way to make the app's own Copies control
 * reliably produce that many sheets.
 */
class FastPrintDocumentAdapter(
    private val context: Context,
    private val settings: PrintSettings,
    private val transform: ImageTransform,
    private val bitmap: Bitmap?,
    private val onFinished: () -> Unit = {},
) : PrintDocumentAdapter() {

    private var pdfDocument: PrintedPdfDocument? = null
    private val totalPages get() = settings.copies.coerceIn(1, 99)

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        pdfDocument?.close()
        pdfDocument = PrintedPdfDocument(context, newAttributes)

        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("fast-print.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(totalPages)
            .build()

        // Changed attributes mean different geometry, so always report a layout change.
        callback.onLayoutFinished(info, oldAttributes != newAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        val document = pdfDocument
        if (document == null) {
            callback.onWriteFailed("Print document was not laid out")
            return
        }

        try {
            for (pageIndex in 0 until totalPages) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onWriteCancelled()
                    document.close()
                    pdfDocument = null
                    return
                }
                if (!pageIndex.isIn(pages)) continue

                val page = document.startPage(pageIndex)
                PageRenderer.render(
                    canvas = page.canvas,
                    pageWidth = page.info.pageWidth,
                    pageHeight = page.info.pageHeight,
                    settings = settings,
                    transform = transform,
                    bitmap = bitmap,
                )
                document.finishPage(page)
            }

            FileOutputStream(destination.fileDescriptor).use { document.writeTo(it) }
            callback.onWriteFinished(pages)
        } catch (e: IOException) {
            Log.e(TAG, "failed writing print document", e)
            callback.onWriteFailed(e.message)
        } finally {
            document.close()
            pdfDocument = null
        }
    }

    override fun onFinish() {
        pdfDocument?.close()
        pdfDocument = null
        onFinished()
    }

    private fun Int.isIn(ranges: Array<out PageRange>): Boolean =
        ranges.isEmpty() || ranges.any { this >= it.start && this <= it.end }
}
