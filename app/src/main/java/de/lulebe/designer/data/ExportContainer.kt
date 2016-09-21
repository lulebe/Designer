package de.lulebe.designer.data

import de.lulebe.designer.data.objects.BaseObject
import de.lulebe.designer.data.objects.BoardObject
import de.lulebe.designer.data.objects.CopyObject
import de.lulebe.designer.data.styles.BoxStyle
import de.lulebe.designer.data.styles.ColorStyle
import de.lulebe.designer.data.styles.TextStyle


class ExportContainer {
    val objects = mutableMapOf<Long, BaseObject>()
    val boxStyles = mutableMapOf<Long, BoxStyle>()
    val colorStyles = mutableMapOf<Long, ColorStyle>()
    val textStyles = mutableMapOf<Long, TextStyle>()
    val images = mutableSetOf<Long>()
    val fonts = mutableSetOf<Long>()

    fun exportTo (board: BoardObject, fromBoard: BoardObject) {
        /*
        1. add all styles
        2. add all images
        3. add all fonts
        4. add all objects
         */
        val newObjUIDs = mutableMapOf<Long, Long>()
        for ((key, obj) in objects) {
            val newObj = obj.clone()
            newObjUIDs.put(key, newObj.uid)
            if (obj is CopyObject && obj.source != null)
                objects.put(obj.source!!.uid, obj.source!!)
        }
        for ((key, obj) in objects) {
            if (obj is CopyObject)
                obj.sourceId = newObjUIDs[obj.sourceId]!!
        }
    }
}