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
    private val decimalSeparator = ","

    fun formatForVisual(input: String): String {

        val split = input.split(decimalSeparator)

        val intPart = split[0].commaSeparated()


        val fractionPart = split.getOrNull(1)

        return if (fractionPart == null) intPart else intPart + decimalSeparator + fractionPart
    }
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
    if (length <= 3) return this

    // Last 3 digits separate, then groups of 2
    val lastThree = takeLast(3)
    val remaining = dropLast(3)

    val formatted = remaining
        .reversed()
        .chunked(2)
        .joinToString(",")
        .reversed()

    return "$formatted,$lastThree"
}