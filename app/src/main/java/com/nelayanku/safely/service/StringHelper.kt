package com.nelayanku.myapplication


object StringHelper {
    fun padNumberWithZeros(number: Int, paddingLength: Int): String {
        val numberString = number.toString()
        if (numberString.length >= paddingLength) {
            return numberString
        }
        val paddedNumber = StringBuilder()
        val numZeros = paddingLength - numberString.length
        for (i in 0 until numZeros) {
            paddedNumber.append("0")
        }
        paddedNumber.append(numberString)
        return paddedNumber.toString()
    }
}
