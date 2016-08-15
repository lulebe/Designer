package de.lulebe.designer.data.styles


class Styles {

    private val _colorStyles: MutableMap<Long, ColorStyle> = mutableMapOf()
    val colorStyles: MutableMap<Long, ColorStyle>
        get() = _colorStyles

    private val _boxStyles: MutableMap<Long, BoxStyle> = mutableMapOf()
    val boxStyles: MutableMap<Long, BoxStyle>
        get() = _boxStyles

    private val _textStyles: MutableMap<Long, TextStyle> = mutableMapOf()
    val textStyles: MutableMap<Long, TextStyle>
        get() = _textStyles

}