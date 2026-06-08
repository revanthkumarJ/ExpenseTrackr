package com.revanthdev.expensetrackr.core.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class DecimalInputVisualTransformation(
    private val decimalFormatter: DecimalFormatter
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val inputText = text.text
        val formattedNumber = decimalFormatter.formatForVisual(inputText)

        val newText = AnnotatedString(
            text = formattedNumber,
            spanStyles = text.spanStyles,
            paragraphStyles = text.paragraphStyles
        )

        val offsetMapping = DecimalOffsetMapping(
            original = inputText,
            formatted = formattedNumber
        )

        return TransformedText(newText, offsetMapping)
    }
}


class DecimalFormatter() {

    fun formatForVisual(input: String): String = input.commaSeparated()
}


private class DecimalOffsetMapping(
    private val original: String,
    private val formatted: String
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        var digitsSeen = 0
        var transformedIndex = 0

        while (transformedIndex < formatted.length && digitsSeen < offset) {
            if (formatted[transformedIndex].isDigit() || formatted[transformedIndex] == '.') {
                digitsSeen++
            }
            transformedIndex++
        }

        return transformedIndex
    }

    override fun transformedToOriginal(offset: Int): Int {
        var digitsSeen = 0
        var transformedIndex = 0

        while (transformedIndex < offset && transformedIndex < formatted.length) {
            if (formatted[transformedIndex].isDigit() || formatted[transformedIndex] == '.') {
                digitsSeen++
            }
            transformedIndex++
        }

        return digitsSeen.coerceAtMost(original.length)
    }
}

private fun String.commaSeparated(): String {
    if (isEmpty()) return this

    // Only the integer part is grouped; the decimal part (incl. the '.') is kept as-is.
    // e.g. "1234567.89" -> intPart "1234567", fractionPart ".89"
    val dotIndex = indexOf('.')
    val intPart = if (dotIndex == -1) this else substring(0, dotIndex)
    val fractionPart = if (dotIndex == -1) "" else substring(dotIndex)

    if (intPart.length <= 3) return intPart + fractionPart

    // Indian grouping: last 3 digits, then groups of 2.
    val lastThree = intPart.takeLast(3)
    val remaining = intPart.dropLast(3)

    val grouped = remaining
        .reversed()
        .chunked(2)
        .joinToString(",")
        .reversed()

    return "$grouped,$lastThree$fractionPart"
}