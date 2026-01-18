package org.mjdev.tts.engine.misaki

import android.util.Log
import org.mjdev.tts.engine.data.MToken
import org.mjdev.tts.engine.data.TokenContext
import java.util.regex.Pattern

@Suppress("PrivatePropertyName", "RegExpRedundantEscape", "LocalVariableName")
class G2P(
    private val lexicon: Lexicon,
    private val fallback: ((String) -> String?)? = null
) {
    private val SUBTOKEN_REGEX = Pattern.compile(
        "^['‘’]+|\\p{Lu}(?=\\p{Lu}\\p{Ll})|(?:^-)?(?:\\d?[,.]?\\d)+|[-_]+|['‘’]{2,}|\\p{L}*?(?:['‘’]\\p{L})*?\\p{Ll}(?=\\p{Lu})|\\p{L}+(?:['‘’]\\p{L})*|[^-_\\p{L}'‘’\\d]|['‘’]+$"
    )
    private val LINK_REGEX = Pattern.compile("\\[([^\\]]+)\\]\\(([^\\)]*)\\)")
    private val PUNCT_TAG_PHONEMES = mapOf(
        "-LRB-" to "(", "-RRB-" to ")",
        "``" to "\u201C", "\"\"" to "\u201D", "''" to "\u201D"
    )

    private fun simpleTokenize(text: String): List<MToken> {
        val tokens = mutableListOf<MToken>()
        val rawTokens = text.split(Regex("(?<=\\s)|(?=\\s)"))
        for (raw in rawTokens) {
            val t = raw.trim()
            if (t.isEmpty()) continue
            val parts = splitPunctuation(t)
            tokens.addAll(parts.map {
                MToken(
                    text = it,
                    tag = guessTag(it),
                    whitespace = if (raw.endsWith(" ")) " " else ""
                )
            })
        }
        for (i in 0 until tokens.size - 1) {
            tokens[i].whitespace = " "
        }
        if (tokens.isNotEmpty()) tokens.last().whitespace = ""
        return tokens
    }

    private fun splitPunctuation(word: String): List<String> {
        if (word.length == 1) return listOf(word)
        val res = mutableListOf<String>()
        val p = Pattern.compile("^(\\p{Punct}*)(.*?)(\\p{Punct}*)$")
        val m = p.matcher(word)
        if (m.find()) {
            val pre = m.group(1) ?: ""
            val word = m.group(2) ?: ""
            val post = m.group(3) ?: ""
            if (pre.isNotEmpty()) res.add(pre)
            if (word.isNotEmpty()) res.add(word)
            if (post.isNotEmpty()) res.add(post)
        } else {
            res.add(word)
        }
        return res
    }

    private fun guessTag(word: String): String {
        if (word.all { !it.isLetterOrDigit() }) {
            if (PUNCT_TAG_PHONEMES.containsKey(word)) return word // simplistic
            if (word == ".") return "."
            if (word == ",") return ","
            return ":"
        }
        if (word.all { it.isDigit() }) return "CD"
        if (word.equals("the", ignoreCase = true)) return "DT"
        if (word.equals("a", ignoreCase = true) || word.equals("an", ignoreCase = true)) return "DT"
        if (word.endsWith("ing")) return "VBG"
        if (word.endsWith("ed")) return "VBD"
        if (word.endsWith("ly")) return "RB"
        if (word.isNotEmpty() && word[0].isUpperCase()) return "NNP"
        return "NN"
    }

    private val NUMBER_REGEX = Pattern.compile("\\d+")

    private fun preprocess(text: String): Triple<String, List<String>, Map<Int, Any>> {
        val matcher = LINK_REGEX.matcher(text)
        val sb = StringBuffer()
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) ?: "")
        }
        matcher.appendTail(sb)
        var processedText = sb.toString()
        val numMatcher = NUMBER_REGEX.matcher(processedText)
        val sbNum = StringBuffer()
        while (numMatcher.find()) {
            try {
                val numStr = numMatcher.group()
                if (numStr.length < 18) {
                    val num = numStr.toLong()
                    val words = Num2Words.convert(num)
                    numMatcher.appendReplacement(sbNum, words)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ignored.", e)
            }
        }
        numMatcher.appendTail(sbNum)
        processedText = sbNum.toString()
        return Triple(processedText, emptyList(), emptyMap())
    }

    private fun retokenize(tokens: List<MToken>): List<MToken> {
        val result = mutableListOf<MToken>()
        for (token in tokens) {
            val subMatcher = SUBTOKEN_REGEX.matcher(token.text)
            val subs = mutableListOf<String>()
            while (subMatcher.find()) {
                subs.add(subMatcher.group())
            }
            if (subs.isEmpty()) {
                result.add(token)
            } else {
                for ((i, sub) in subs.withIndex()) {
                    val newT = token.copy(
                        text = sub,
                        whitespace = if (i == subs.size - 1) token.whitespace else ""
                    )
                    newT.attributes.isHead = i == 0
                    result.add(newT)
                }
            }
        }
        return result
    }

    @Suppress("UnusedVariable")
    fun phonemize(text: String): String {
        val (preprocessedText, tokens, features) = preprocess(text)
        val mTokens = simpleTokenize(preprocessedText)
        val processedTokens = retokenize(mTokens)
        var ctx = TokenContext()
        val result = StringBuilder()
        for (token in processedTokens) {
            if (token.phonemes == null) {
                val (ps, rating) = lexicon.getWord(
                    token.text,
                    token.tag,
                    token.attributes.stress,
                    ctx
                )
                token.phonemes = ps
                token.attributes.rating = rating
                if (token.phonemes == null && fallback != null) {
                    token.phonemes = fallback.invoke(token.text)
                }
                if (token.phonemes == null) {
                    Log.d(TAG, "No phonemes found.")
                }
            }
            ctx = tokenContext(ctx, token.phonemes, token)
            result.append(token.phonemes ?: "")
            result.append(token.whitespace)
        }
        return result.toString()
            .replace("ɾ", "T")
            .replace("ʔ", "t")
            .trim()
    }

    private fun tokenContext(
        ctx: TokenContext,
        ps: String?,
        token: MToken
    ): TokenContext {
        var vowel = ctx.futureVowel
        if (!ps.isNullOrEmpty()) {
            val first = ps[0]
            val VOWELS = "AIOQWYaiuæɑɒɔəɛɜɪʊʌᵻ"
            vowel = VOWELS.contains(first)
        }
        val futureTo = token.text.equals(
            "to",
            ignoreCase = true
        ) || (token.text == "TO" && (token.tag == "TO" || token.tag == "IN"))
        return TokenContext(futureVowel = vowel, futureTo = futureTo)
    }

    companion object {
        private val TAG = G2P::class.simpleName
    }
}
