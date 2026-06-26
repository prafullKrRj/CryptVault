package com.cryptvault.domain.usecase

import com.cryptvault.domain.model.GeneratorConfig
import com.cryptvault.domain.model.PasswordStrength
import com.cryptvault.domain.model.StrengthBucket
import java.security.SecureRandom

private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
private const val DIGITS = "0123456789"
private const val SYMBOLS = "!@#\$%^&*()-_=+[]{};:,.<>/?~"
private const val AMBIGUOUS = "Il1O0o`'\"|;:.,"

class GeneratePasswordUseCase(private val rng: SecureRandom = SecureRandom()) {

    operator fun invoke(config: GeneratorConfig): String {
        val pools = mutableListOf<String>()
        if (config.useUpper) pools += UPPER
        if (config.useLower) pools += LOWER
        if (config.useDigits) pools += DIGITS
        if (config.useSymbols) pools += SYMBOLS
        require(pools.isNotEmpty()) { "Select at least one character class" }
        val finalPools = if (config.excludeAmbiguous) pools.map { it.removeAmbiguous() } else pools
        require(finalPools.all { it.isNotEmpty() }) { "Filtered pool is empty" }
        val length = config.length.coerceIn(4, 128)
        val combined = finalPools.joinToString("")

        val chars = CharArray(length)
        // Guarantee at least one char from each selected pool
        var i = 0
        finalPools.forEach { pool ->
            if (i < length) {
                chars[i] = pool[rng.nextInt(pool.length)]
                i++
            }
        }
        while (i < length) {
            chars[i] = combined[rng.nextInt(combined.length)]
            i++
        }
        // Shuffle (Fisher-Yates)
        for (j in length - 1 downTo 1) {
            val k = rng.nextInt(j + 1)
            val t = chars[j]; chars[j] = chars[k]; chars[k] = t
        }
        return String(chars)
    }

    fun strength(config: GeneratorConfig, sample: String = ""): PasswordStrength {
        var poolSize = 0
        if (config.useUpper) poolSize += if (config.excludeAmbiguous) UPPER.removeAmbiguous().length else UPPER.length
        if (config.useLower) poolSize += if (config.excludeAmbiguous) LOWER.removeAmbiguous().length else LOWER.length
        if (config.useDigits) poolSize += if (config.excludeAmbiguous) DIGITS.removeAmbiguous().length else DIGITS.length
        if (config.useSymbols) poolSize += SYMBOLS.length
        poolSize = poolSize.coerceAtLeast(1)
        val length = config.length.coerceAtLeast(1)
        val entropy = length * (Math.log(poolSize.toDouble()) / Math.log(2.0))
        val bucket = when {
            entropy < 28 -> StrengthBucket.VeryWeak
            entropy < 40 -> StrengthBucket.Weak
            entropy < 60 -> StrengthBucket.Fair
            entropy < 80 -> StrengthBucket.Strong
            else -> StrengthBucket.Excellent
        }
        val score = when (bucket) {
            StrengthBucket.VeryWeak -> 0
            StrengthBucket.Weak -> 1
            StrengthBucket.Fair -> 2
            StrengthBucket.Strong -> 3
            StrengthBucket.Excellent -> 4
        }
        return PasswordStrength(score = score, label = bucket.label, entropyBits = entropy)
    }

    private fun String.removeAmbiguous(): String = this.filter { it !in AMBIGUOUS }
}