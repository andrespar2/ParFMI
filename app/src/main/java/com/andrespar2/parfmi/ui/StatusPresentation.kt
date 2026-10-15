package com.andrespar2.parfmi.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.andrespar2.parfmi.R
import com.andrespar2.parfmi.data.model.ActivationLockStatus
import com.andrespar2.parfmi.data.model.ActivationState
import com.andrespar2.parfmi.data.model.BlacklistStatus
import com.andrespar2.parfmi.data.model.CoverageStatus
import com.andrespar2.parfmi.data.model.FmiStatus
import com.andrespar2.parfmi.data.model.IcloudState
import com.andrespar2.parfmi.data.model.MdmStatus
import com.andrespar2.parfmi.data.model.SimLockStatus
import com.andrespar2.parfmi.ui.theme.ParfmiColors

/**
 * Presentacion visual de un estado: etiqueta traducida y color semantico.
 *
 * Regla clave: [com.andrespar2.parfmi.data.model.FmiStatus.UNKNOWN] y el resto
 * de estados desconocidos usan siempre el color de advertencia y la etiqueta
 * "Desconocido"; nunca se pintan como si el dispositivo estuviera limpio.
 */
data class StatusView(
    @StringRes val labelRes: Int,
    val color: Color,
)

fun FmiStatus.toView(): StatusView = when (this) {
    FmiStatus.ON -> StatusView(R.string.status_on, ParfmiColors.StatusNegative)
    FmiStatus.OFF -> StatusView(R.string.status_off, ParfmiColors.StatusPositive)
    FmiStatus.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun ActivationLockStatus.toView(): StatusView = when (this) {
    ActivationLockStatus.LOCKED -> StatusView(R.string.status_locked, ParfmiColors.StatusNegative)
    ActivationLockStatus.UNLOCKED -> StatusView(R.string.status_unlocked, ParfmiColors.StatusPositive)
    ActivationLockStatus.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun ActivationState.toView(): StatusView = when (this) {
    ActivationState.ACTIVATED -> StatusView(R.string.status_activated, ParfmiColors.Info)
    ActivationState.NOT_ACTIVATED -> StatusView(R.string.status_not_activated, ParfmiColors.TextSecondary)
    ActivationState.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun BlacklistStatus.toView(): StatusView = when (this) {
    BlacklistStatus.CLEAN -> StatusView(R.string.status_clean, ParfmiColors.StatusPositive)
    BlacklistStatus.BLACKLISTED -> StatusView(R.string.status_blacklisted, ParfmiColors.StatusNegative)
    BlacklistStatus.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun MdmStatus.toView(): StatusView = when (this) {
    MdmStatus.ENROLLED -> StatusView(R.string.status_mdm_enrolled, ParfmiColors.StatusUnknown)
    MdmStatus.NOT_ENROLLED -> StatusView(R.string.status_mdm_clean, ParfmiColors.StatusPositive)
    MdmStatus.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun SimLockStatus.toView(): StatusView = when (this) {
    SimLockStatus.LOCKED -> StatusView(R.string.status_sim_locked, ParfmiColors.StatusUnknown)
    SimLockStatus.UNLOCKED -> StatusView(R.string.status_sim_unlocked, ParfmiColors.StatusPositive)
    SimLockStatus.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun IcloudState.toView(): StatusView = when (this) {
    IcloudState.CLEAN -> StatusView(R.string.status_icloud_clean, ParfmiColors.StatusPositive)
    IcloudState.LOST -> StatusView(R.string.status_icloud_lost, ParfmiColors.StatusNegative)
    IcloudState.ERASED -> StatusView(R.string.status_icloud_erased, ParfmiColors.Info)
    IcloudState.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}

fun CoverageStatus.toView(): StatusView = when (this) {
    CoverageStatus.IN_WARRANTY -> StatusView(R.string.status_coverage_in, ParfmiColors.StatusPositive)
    CoverageStatus.OUT_OF_WARRANTY -> StatusView(R.string.status_coverage_out, ParfmiColors.StatusUnknown)
    CoverageStatus.UNKNOWN -> StatusView(R.string.status_unknown, ParfmiColors.StatusUnknown)
}
