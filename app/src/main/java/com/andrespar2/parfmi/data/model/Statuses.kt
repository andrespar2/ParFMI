package com.andrespar2.parfmi.data.model

import com.andrespar2.parfmi.core.TextNormalizer

/**
 * Estados de seguridad como valores de tres o mas opciones.
 *
 * Regla fundamental de ParFMI: cuando el proveedor no devuelve un dato, el
 * estado es [UNKNOWN] (o equivalente) y NUNCA se presenta como "OFF",
 * "desbloqueado" o "sin bloqueo". Mostrar un falso negativo en una consulta de
 * FMI inducira a error al usuario.
 *
 * El analisis acepta tanto valores exactos ("ON", "OFF") como descripciones
 * mas largas que algunos servicios de Sickw devuelven.
 */
enum class FmiStatus {
    ON,
    OFF,
    UNKNOWN;

    companion object {
        private val ON_VALUES = setOf(
            "ON", "YES", "TRUE", "ENABLED", "LOCKED", "LOCKED ON", "FMI ON",
            "ICLOUD ON", "1", "Y",
        )
        private val OFF_VALUES = setOf(
            "OFF", "NO", "FALSE", "DISABLED", "UNLOCKED", "CLEAN", "FMI OFF",
            "ICLOUD OFF", "0", "N",
        )

        fun fromRaw(raw: String?): FmiStatus = when (TextNormalizer.normalize(raw)) {
            in ON_VALUES -> ON
            in OFF_VALUES -> OFF
            else -> UNKNOWN
        }
    }
}

/** Estado de Activation Lock (bloqueo de activacion). */
enum class ActivationLockStatus {
    LOCKED,
    UNLOCKED,
    UNKNOWN;

    companion object {
        private val LOCKED_VALUES = setOf(
            "LOCKED", "ON", "YES", "TRUE", "ENABLED", "ACTIVE", "1",
            "ACTIVATION LOCK ON", "FMI ON",
        )
        private val UNLOCKED_VALUES = setOf(
            "UNLOCKED", "OFF", "NO", "FALSE", "DISABLED", "INACTIVE", "CLEAN", "0",
            "ACTIVATION LOCK OFF", "FMI OFF", "NOT LOCKED",
        )

        fun fromRaw(raw: String?): ActivationLockStatus =
            when (TextNormalizer.normalize(raw)) {
                in LOCKED_VALUES -> LOCKED
                in UNLOCKED_VALUES -> UNLOCKED
                else -> UNKNOWN
            }
    }
}

/** Indica si el dispositivo ya ha sido activado alguna vez. */
enum class ActivationState {
    ACTIVATED,
    NOT_ACTIVATED,
    UNKNOWN;

    companion object {
        /**
         * Se comprueban primero las formas negativas: "NOT ACTIVATED" contiene
         * la palabra "ACTIVATED" y no debe clasificarse como activado.
         */
        private val NOT_ACTIVATED_VALUES = setOf(
            "NOT ACTIVATED", "NO ACTIVADO", "UNACTIVATED", "DEACTIVATED",
            "NEVER ACTIVATED", "NO", "FALSE", "0", "OFF", "NOT ACTIVE",
        )
        private val ACTIVATED_VALUES = setOf(
            "ACTIVATED", "ACTIVE", "SI", "YES", "TRUE", "1", "ON", "ACTIVATION DONE",
        )

        fun fromRaw(raw: String?): ActivationState {
            val normalized = TextNormalizer.normalize(raw)
            return when {
                normalized.isEmpty() -> UNKNOWN
                normalized in NOT_ACTIVATED_VALUES -> NOT_ACTIVATED
                normalized in ACTIVATED_VALUES -> ACTIVATED
                normalized.contains("NOT ACTIVATED") -> NOT_ACTIVATED
                normalized.contains("ACTIVATED") -> ACTIVATED
                else -> UNKNOWN
            }
        }
    }
}

/** Estado de la lista de bloqueo (policial / de operadores). */
enum class BlacklistStatus {
    CLEAN,
    BLACKLISTED,
    UNKNOWN;

    companion object {
        private val CLEAN_VALUES = setOf(
            "CLEAN", "NOT BLACKLISTED", "NOT IN BLACKLIST", "NO", "FALSE", "0",
            "OK", "NOT FOUND", "NO RECORD", "NO RECORD FOUND", "GOOD",
        )
        private val BLACKLISTED_VALUES = setOf(
            "BLACKLISTED", "BLACK LIST", "YES", "TRUE", "1", "LOST", "STOLEN",
            "IN BLACKLIST", "REPORTED", "BAD",
        )

        fun fromRaw(raw: String?): BlacklistStatus {
            val normalized = TextNormalizer.normalize(raw)
            return when {
                normalized.isEmpty() -> UNKNOWN
                normalized in CLEAN_VALUES -> CLEAN
                normalized in BLACKLISTED_VALUES -> BLACKLISTED
                normalized.contains("NOT BLACKLISTED") -> CLEAN
                normalized.contains("BLACKLIST") -> BLACKLISTED
                else -> UNKNOWN
            }
        }
    }
}

/** Estado de inscripcion en MDM (gestion de dispositivos). */
enum class MdmStatus {
    ENROLLED,
    NOT_ENROLLED,
    UNKNOWN;

    companion object {
        private val ENROLLED_VALUES = setOf(
            "ENROLLED", "ON", "YES", "TRUE", "1", "MDM ON", "SUPERVISED MDM",
        )
        private val NOT_ENROLLED_VALUES = setOf(
            "NOT ENROLLED", "CLEAN", "OFF", "NO", "FALSE", "0", "MDM OFF",
            "NOT SUPERVISED", "NONE",
        )

        fun fromRaw(raw: String?): MdmStatus = when (TextNormalizer.normalize(raw)) {
            in ENROLLED_VALUES -> ENROLLED
            in NOT_ENROLLED_VALUES -> NOT_ENROLLED
            else -> UNKNOWN
        }
    }
}

/** Bloqueo del dispositivo a un operador concreto (SIM Lock). */
enum class SimLockStatus {
    LOCKED,
    UNLOCKED,
    UNKNOWN;

    companion object {
        private val LOCKED_VALUES = setOf(
            "LOCKED", "SIM LOCKED", "CARRIER LOCKED", "YES", "TRUE", "ON", "1",
            "LOCKED TO CARRIER",
        )
        private val UNLOCKED_VALUES = setOf(
            "UNLOCKED", "SIM FREE", "FACTORY UNLOCKED", "NO", "FALSE", "OFF", "0",
            "ANY CARRIER", "CLEAN",
        )

        fun fromRaw(raw: String?): SimLockStatus = when (TextNormalizer.normalize(raw)) {
            in LOCKED_VALUES -> LOCKED
            in UNLOCKED_VALUES -> UNLOCKED
            else -> UNKNOWN
        }
    }
}

/** Estado de la cuenta de iCloud en los servicios que lo distinguen. */
enum class IcloudState {
    CLEAN,
    LOST,
    ERASED,
    UNKNOWN;

    companion object {
        private val CLEAN_VALUES = setOf("CLEAN", "NOT LOST", "OFF", "NO", "0", "OK")
        private val LOST_VALUES = setOf("LOST", "LOST MODE", "IN LOST MODE", "YES", "1")
        private val ERASED_VALUES = setOf("ERASED", "WIPED", "ERASE PENDING")

        fun fromRaw(raw: String?): IcloudState {
            val normalized = TextNormalizer.normalize(raw)
            return when {
                normalized.isEmpty() -> UNKNOWN
                normalized in CLEAN_VALUES -> CLEAN
                normalized in LOST_VALUES -> LOST
                normalized in ERASED_VALUES -> ERASED
                normalized.contains("LOST") -> LOST
                normalized.contains("ERASED") -> ERASED
                else -> UNKNOWN
            }
        }
    }
}

/** Estado de cobertura o garantia. */
enum class CoverageStatus {
    IN_WARRANTY,
    OUT_OF_WARRANTY,
    UNKNOWN;

    companion object {
        // Orden importante: primero las variantes con "not"/"no coverage".
        private val OUT_VALUES = setOf(
            "OUT OF WARRANTY", "OUT OF WARRANTY NO COVERAGE", "EXPIRED",
            "NO COVERAGE", "NOT COVERED", "NO", "FALSE", "0", "INACTIVE",
            "COVERAGE EXPIRED", "INELIGIBLE",
        )
        private val IN_VALUES = setOf(
            "IN WARRANTY", "COVERED", "ACTIVE", "YES", "TRUE", "1", "VALID",
            "COVERAGE ACTIVE", "ELIGIBLE", "IN WARRANTY ACTIVE",
        )

        fun fromRaw(raw: String?): CoverageStatus {
            val normalized = TextNormalizer.normalize(raw)
            return when {
                normalized.isEmpty() -> UNKNOWN
                normalized in OUT_VALUES -> OUT_OF_WARRANTY
                normalized in IN_VALUES -> IN_WARRANTY
                normalized.contains("OUT OF WARRANTY") -> OUT_OF_WARRANTY
                normalized.contains("NO COVERAGE") -> OUT_OF_WARRANTY
                normalized.contains("EXPIRED") -> OUT_OF_WARRANTY
                normalized.contains("IN WARRANTY") -> IN_WARRANTY
                else -> UNKNOWN
            }
        }
    }
}
