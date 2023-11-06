package icu.twtool.leetcode.api.enumerate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = QuestionStatus.Serializer::class)
enum class QuestionStatus {
    NOT_STARTED,
    TRIED,
    AC;

    class Serializer : KSerializer<QuestionStatus> {
        override fun deserialize(decoder: Decoder): QuestionStatus{
            return try {
                QuestionStatus.valueOf(decoder.decodeString().uppercase())
            } catch (_: Exception) {
                NOT_STARTED
            }
        }

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("QuestionStatus", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: QuestionStatus) {
            return encoder.encodeString(value.name)
        }
    }
}