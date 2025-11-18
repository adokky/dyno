package dyno

import dyno.AbstractEagerDynoSerializer.ResolveContext
import dyno.AbstractEagerDynoSerializer.ResolveResult

enum class UnknownKeysStrategy {
    Keep {
        override fun process(context: ResolveContext, schema: DynoSchema) = ResolveResult.Keep
    },
    Skip {
        override fun process(context: ResolveContext, schema: DynoSchema) = ResolveResult.Skip
    },
    Error {
        override fun process(context: ResolveContext, schema: DynoSchema) =
            throw NoSuchDynoKeySerializationException(context.keyString, schema.name())
    },
    KeepIfJsonAllowed {
        override fun process(context: ResolveContext, schema: DynoSchema) =
            when {
                context.json.configuration.ignoreUnknownKeys -> ResolveResult.Keep
                else -> Error.process(context, schema)
            }
    },
    SkipIfJsonAllowed {
        override fun process(context: ResolveContext, schema: DynoSchema) =
            when {
                context.json.configuration.ignoreUnknownKeys -> ResolveResult.Skip
                else -> Error.process(context, schema)
            }
    };

    internal abstract fun process(context: ResolveContext, schema: DynoSchema): ResolveResult
}