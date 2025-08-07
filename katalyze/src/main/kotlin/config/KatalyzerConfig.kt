package config

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.decoder.NonNullableLeafDecoder
import com.sksamuel.hoplite.decoder.toValidated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.icpclive.cds.tunning.TuningRule
import rules_kt.RuleInterface
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

private fun Node.toJsonElement() : JsonElement = when (this) {
    is ArrayNode -> JsonArray(elements.map { it.toJsonElement() })
    is MapNode -> JsonObject(map.mapValues { it.value.toJsonElement() })
    is BooleanNode -> JsonPrimitive(value)
    is NullNode -> JsonNull
    is DoubleNode -> JsonPrimitive(value)
    is LongNode -> JsonPrimitive(value)
    is StringNode -> JsonPrimitive(value)
    Undefined -> error("Unexpected undefined node")
}

data class KatalyzerConfig(
    val db: DB = DB(),
    val web: Web? = null,
    @param:ConfigAlias("tuning_rules") val tuningRules: List<TuningRule> = emptyList(),
    val rules: List<RuleInterface?>,
) {
    @Serializable
    data class DB(val enable: Boolean = true)

    @Serializable
    data class Web(val enable: Boolean = true, val compress: Boolean = true, val port: Int)

    class RuleInterfaceDecoder : Decoder<RuleInterface?> {
        override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<RuleInterface?> {
            when (node) {
                is StringNode -> {
                    val result = runCatching {
                        val rule = node.value
                        findRuleClassByName(rule, type).createInstance()
                    }.toValidated { ConfigFailure.DecodeError(node, type) }
                    return result
                }

                is MapNode -> {
                    val keys = node.map.keys.toList()
                    if (keys.size != 1) {
                        return ConfigFailure.Generic("Expected exactly one key for rule, found $keys").invalid()
                    }
                    val ruleNode = node[keys[0]]
                    val result = runCatching {
                        findRuleClassByName(keys[0], type).createType()
                    }.toValidated { ConfigFailure.Generic("Failed to decode rule: ${it.message}") }
                        .flatMap { ruleType ->
                            context.decoder(ruleType).flatMap { decoder ->
                                decoder.decode(ruleNode, ruleType, context)
                            }
                        }.flatMap { decodedRule ->
                            (decodedRule as? RuleInterface)?.valid()
                                ?: ConfigFailure.Generic("Decoded $decodedRule but it's not RuleInterface").invalid()
                        }

                    return result.flatMap {
                        context.decoder(typeOf<Enabled>()).flatMap { decoder ->
                            decoder.decode(ruleNode, typeOf<Enabled>(), context)
                        }.map { enabled ->
                            if ((enabled as Enabled).enable) it
                            else null
                        }
                    }
                }

                else -> return ConfigFailure.DecodeError(node, type).invalid()
            }
        }

        private fun findRuleClassByName(name: String, baseKType: KType): KClass<out RuleInterface> {
            return Class.forName("rules_kt.$name").kotlin.run {
                if (createType().isSubtypeOf(baseKType)) {
                    @Suppress("UNCHECKED_CAST")
                    this as KClass<out RuleInterface>
                } else {
                    throw ClassNotFoundException("Class $name is not a subclass of RuleInterface")
                }
            }
        }

        override fun supports(type: KType): Boolean = type == typeOf<RuleInterface?>()

        data class Enabled(val enable: Boolean = true)
    }

    class NoArgumentDecoder : Decoder<RuleInterface> {
        override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<RuleInterface> =
            runCatching {
                (type.classifier as KClass<*>).createInstance() as RuleInterface
            }.toValidated {
                ConfigFailure.DecodeError(node, type)
            }

        override fun supports(type: KType): Boolean {
            if (!type.isSubtypeOf(typeOf<RuleInterface>())) return false
            val kClass = type.classifier as? KClass<*> ?: return false
            return !kClass.isData && kClass.constructors.any { it.parameters.isEmpty() }
        }
    }

    class AdvancedPropertiesDecoder: Decoder<TuningRule> {
        override fun decode(
            node: Node,
            type: KType,
            context: DecoderContext
        ): ConfigResult<TuningRule> {
            return runCatching {
                Json.decodeFromJsonElement<TuningRule>(node.toJsonElement())
            }.toValidated { ConfigFailure.Generic(it.localizedMessage) }
        }

        override fun supports(type: KType) = type.classifier == TuningRule::class
    }
}