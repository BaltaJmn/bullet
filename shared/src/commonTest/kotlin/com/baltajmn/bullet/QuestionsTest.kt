package com.baltajmn.bullet

import com.baltajmn.bullet.i18n.QUESTION_COUNT
import com.baltajmn.bullet.i18n.S
import com.baltajmn.bullet.model.nextQuestion
import com.baltajmn.bullet.model.questionsAfter
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// 29. Preguntas (docs/tecnico.md 10, 12.3, #65).
class QuestionsTest {
    @Test
    fun chainedReviewsDoNotRepeatWhileOthersAreLeft() {
        val random = Random(7)
        var used = emptyList<Int>()
        val asked = mutableListOf<Int>()
        repeat(QUESTION_COUNT) {
            val q = nextQuestion(used, random)
            asked += q
            used = questionsAfter(used, q)
        }
        assertEquals(QUESTION_COUNT, asked.toSet().size)

        // The bank is used up: a new round starts without the last one asked.
        val last = asked.last()
        repeat(200) { assertNotEquals(last, nextQuestion(used, Random(it))) }
        val next = nextQuestion(used, random)
        assertEquals(listOf(last, next), questionsAfter(used, next))
    }

    @Test
    fun everyQuestionExistsInTheFiveLanguages() {
        for (lang in listOf("en", "es", "pt", "de", "fr")) {
            S.lang = lang
            assertTrue((0 until QUESTION_COUNT).all { S.question(it).isNotBlank() }, lang)
        }
        S.lang = "en"
    }
}
