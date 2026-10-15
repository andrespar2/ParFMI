package com.andrespar2.parfmi.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Los estados son el nucleo de la app: un dato ausente jamas debe
 * interpretarse como OFF, desbloqueado o sin bloqueo.
 */
class StatusParsingTest {

    @Test
    fun `fmi status parses explicit values`() {
        assertEquals(FmiStatus.ON, FmiStatus.fromRaw("ON"))
        assertEquals(FmiStatus.ON, FmiStatus.fromRaw("on"))
        assertEquals(FmiStatus.ON, FmiStatus.fromRaw("Yes"))
        assertEquals(FmiStatus.OFF, FmiStatus.fromRaw("OFF"))
        assertEquals(FmiStatus.OFF, FmiStatus.fromRaw("clean"))
        assertEquals(FmiStatus.ON, FmiStatus.fromRaw("LOCKED"))
    }

    @Test
    fun `fmi status is unknown when the provider says nothing useful`() {
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw(null))
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw(""))
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw("   "))
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw("N/A"))
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw("-"))
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw("Unknown"))
        assertEquals(FmiStatus.UNKNOWN, FmiStatus.fromRaw("no data"))
    }

    @Test
    fun `activation lock status never guesses`() {
        assertEquals(ActivationLockStatus.LOCKED, ActivationLockStatus.fromRaw("ON"))
        assertEquals(ActivationLockStatus.LOCKED, ActivationLockStatus.fromRaw("Activation Lock: ON"))
        assertEquals(ActivationLockStatus.UNLOCKED, ActivationLockStatus.fromRaw("OFF"))
        assertEquals(ActivationLockStatus.UNKNOWN, ActivationLockStatus.fromRaw("maybe"))
        assertEquals(ActivationLockStatus.UNKNOWN, ActivationLockStatus.fromRaw(null))
    }

    @Test
    fun `activation state distinguishes not activated from activated`() {
        assertEquals(ActivationState.ACTIVATED, ActivationState.fromRaw("Activated"))
        assertEquals(ActivationState.ACTIVATED, ActivationState.fromRaw("ACTIVE"))
        assertEquals(ActivationState.NOT_ACTIVATED, ActivationState.fromRaw("Not Activated"))
        assertEquals(ActivationState.NOT_ACTIVATED, ActivationState.fromRaw("No"))
        assertEquals(ActivationState.UNKNOWN, ActivationState.fromRaw("something else"))
        assertEquals(ActivationState.UNKNOWN, ActivationState.fromRaw(null))
    }

    @Test
    fun `blacklist status distinguishes clean from blacklisted`() {
        assertEquals(BlacklistStatus.CLEAN, BlacklistStatus.fromRaw("Clean"))
        assertEquals(BlacklistStatus.CLEAN, BlacklistStatus.fromRaw("Not blacklisted"))
        assertEquals(BlacklistStatus.BLACKLISTED, BlacklistStatus.fromRaw("BLACKLISTED"))
        assertEquals(BlacklistStatus.BLACKLISTED, BlacklistStatus.fromRaw("Reported"))
        assertEquals(BlacklistStatus.UNKNOWN, BlacklistStatus.fromRaw(""))
    }

    @Test
    fun `mdm status distinguishes enrolled from not enrolled`() {
        assertEquals(MdmStatus.ENROLLED, MdmStatus.fromRaw("Enrolled"))
        assertEquals(MdmStatus.NOT_ENROLLED, MdmStatus.fromRaw("Clean"))
        assertEquals(MdmStatus.NOT_ENROLLED, MdmStatus.fromRaw("No"))
        assertEquals(MdmStatus.UNKNOWN, MdmStatus.fromRaw("n/a"))
    }

    @Test
    fun `sim lock status distinguishes locked from unlocked`() {
        assertEquals(SimLockStatus.LOCKED, SimLockStatus.fromRaw("Locked"))
        assertEquals(SimLockStatus.UNLOCKED, SimLockStatus.fromRaw("Unlocked"))
        assertEquals(SimLockStatus.UNLOCKED, SimLockStatus.fromRaw("Sim free"))
        assertEquals(SimLockStatus.UNKNOWN, SimLockStatus.fromRaw(""))
    }

    @Test
    fun `icloud state distinguishes clean lost and erased`() {
        assertEquals(IcloudState.CLEAN, IcloudState.fromRaw("Clean"))
        assertEquals(IcloudState.LOST, IcloudState.fromRaw("Lost"))
        assertEquals(IcloudState.LOST, IcloudState.fromRaw("LOST / ERASED"))
        assertEquals(IcloudState.ERASED, IcloudState.fromRaw("Erased"))
        assertEquals(IcloudState.UNKNOWN, IcloudState.fromRaw("ON"))
    }

    @Test
    fun `coverage status understands longer descriptions`() {
        assertEquals(
            CoverageStatus.OUT_OF_WARRANTY,
            CoverageStatus.fromRaw("Out Of Warranty (No Coverage)"),
        )
        assertEquals(CoverageStatus.OUT_OF_WARRANTY, CoverageStatus.fromRaw("Expired"))
        assertEquals(CoverageStatus.IN_WARRANTY, CoverageStatus.fromRaw("In Warranty"))
        assertEquals(CoverageStatus.IN_WARRANTY, CoverageStatus.fromRaw("Coverage Active"))
        assertEquals(CoverageStatus.UNKNOWN, CoverageStatus.fromRaw(""))
        assertEquals(CoverageStatus.UNKNOWN, CoverageStatus.fromRaw("Loaned out"))
    }

    @Test
    fun `security info reports whether any value is known`() {
        assertEquals(false, SecurityInfo().hasAnyKnownValue)
        assertEquals(true, SecurityInfo(fmi = FmiStatus.OFF).hasAnyKnownValue)
        assertEquals(true, SecurityInfo(simLock = SimLockStatus.UNLOCKED).hasAnyKnownValue)
    }

    @Test
    fun `empty group models report no values`() {
        assertEquals(false, DeviceInfo().hasAnyValue)
        assertEquals(false, CoverageInfo().hasAnyValue)
        assertEquals(false, NetworkInfo().hasAnyValue)
        assertEquals(true, NetworkInfo(carrier = "AT&T").hasAnyValue)
    }
}
