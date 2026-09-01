package com.notzyvex.fastprint.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notzyvex.fastprint.state.DocType
import com.notzyvex.fastprint.state.ImageTransform
import com.notzyvex.fastprint.state.Margins
import com.notzyvex.fastprint.state.Orientation
import com.notzyvex.fastprint.state.PaperUnit
import com.notzyvex.fastprint.state.PrintSettings
import com.notzyvex.fastprint.state.Scale

enum class JobStatus { DONE, FAILED }

/**
 * One finished print job. Everything needed to reprint is stored, so "Print again"
 * can restore the exact settings and image the user had.
 */
@Entity(tableName = "print_jobs")
data class PrintJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docType: String,
    val width: Double,
    val height: Double,
    val unit: String,
    val orientation: String,
    val copies: Int,
    val color: Boolean,
    val dpi: Int,
    val margins: String,
    val scale: String,
    val duplex: Boolean,
    val printer: String,
    /** Absolute path to the copy of the image kept in app storage; null for no upload. */
    val imagePath: String?,
    val offsetX: Float,
    val offsetY: Float,
    val zoom: Float,
    val rotation: Int,
    val status: String,
    val createdAt: Long,
) {
    fun toSettings() = PrintSettings(
        docType = DocType.fromLabel(docType),
        width = width,
        height = height,
        unit = enumValueOf<PaperUnit>(unit),
        orientation = enumValueOf<Orientation>(orientation),
        copies = copies,
        color = color,
        dpi = dpi,
        margins = enumValueOf<Margins>(margins),
        scale = enumValueOf<Scale>(scale),
        duplex = duplex,
    )

    fun toTransform() = ImageTransform(offsetX, offsetY, zoom, rotation)

    fun jobStatus() = enumValueOf<JobStatus>(status)

    /** "8.5 × 11 in · 2 copies · Color · HP OfficeJet" — the history row's meta line. */
    fun metaLine(): String {
        val s = toSettings()
        val copiesWord = if (copies == 1) "copy" else "copies"
        return "${s.sizeLabel()} · $copies $copiesWord · ${if (color) "Color" else "B&W"} · $printer"
    }

    companion object {
        fun from(
            settings: PrintSettings,
            transform: ImageTransform,
            printer: String,
            imagePath: String?,
            status: JobStatus,
            createdAt: Long,
        ) = PrintJobEntity(
            docType = settings.docType.label,
            width = settings.width,
            height = settings.height,
            unit = settings.unit.name,
            orientation = settings.orientation.name,
            copies = settings.copies,
            color = settings.color,
            dpi = settings.dpi,
            margins = settings.margins.name,
            scale = settings.scale.name,
            duplex = settings.duplex,
            printer = printer,
            imagePath = imagePath,
            offsetX = transform.offsetX,
            offsetY = transform.offsetY,
            zoom = transform.zoom,
            rotation = transform.rotation,
            status = status.name,
            createdAt = createdAt,
        )
    }
}
