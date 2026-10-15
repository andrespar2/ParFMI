package com.andrespar2.parfmi.ui

import androidx.annotation.StringRes
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.core.ValidationError
import com.andrespar2.parfmi.data.model.ProviderError

/** Mensaje traducido para un motivo de validacion. */
@StringRes
fun ValidationError.messageRes(): Int = when (this) {
    ValidationError.EMPTY -> R.string.validation_empty
    ValidationError.TOO_LONG -> R.string.validation_too_long
    ValidationError.IMEI_LENGTH -> R.string.validation_imei_length
    ValidationError.IMEI_CHECKSUM -> R.string.validation_imei_checksum
    ValidationError.SERIAL_LENGTH -> R.string.validation_serial_length
    ValidationError.SERIAL_CHARACTERS -> R.string.validation_serial_chars
}

/** Mensaje traducido para una categoria de error del proveedor. */
@StringRes
fun ProviderError.messageRes(): Int = when (this) {
    ProviderError.NO_NETWORK -> R.string.error_no_network
    ProviderError.TIMEOUT -> R.string.error_timeout
    ProviderError.UNAUTHORIZED -> R.string.error_unauthorized
    ProviderError.INSUFFICIENT_BALANCE -> R.string.error_insufficient_balance
    ProviderError.NOT_FOUND -> R.string.error_not_found
    ProviderError.RATE_LIMITED -> R.string.error_rate_limited
    ProviderError.SERVER_ERROR -> R.string.error_server
    ProviderError.PARSE_ERROR -> R.string.error_parse
    ProviderError.NO_PROVIDER_CONFIGURED -> R.string.error_no_provider
    ProviderError.REQUEST_REJECTED -> R.string.error_unknown
    ProviderError.UNKNOWN -> R.string.error_unknown
}
