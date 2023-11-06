package icu.twtool.leetcode.api.enumerate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = QuestionDifficulty.Serializer::class)
enum class QuestionDifficulty() {
    EASY,
    MEDIUM,
    HARD,
    UNKNOWN;

    class Serializer: KSerializer<QuestionDifficulty> {
        override fun deserialize(decoder: Decoder): QuestionDifficulty {
            return try {
                valueOf(decoder.decodeString().uppercase())
            } catch (_: Exception) {
                UNKNOWN
            }
        }

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("QuestionDifficulty", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: QuestionDifficulty) {
            return encoder.encodeString(value.name)
        }
    }
}