package de.lulebe.designer.propertyEditing

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import de.lulebe.designer.R
import de.lulebe.designer.data.styles.BaseStyle


class StyleExtractor<Style: BaseStyle> {
    fun createStyle(style: Style, context: Context, cb: (Style) -> Unit) {
        val v = LayoutInflater.from(context).inflate(R.layout.dialog_styleextractor, null)
        AlertDialog.Builder(context)
                .setView(v)
                .setTitle("extract Style")
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialogInterface, i ->
                    style.name = (v.findViewById(R.id.field_style_name) as EditText).text.toString()
                    cb(style)
                })
                .show()
    }
}