package com.etfrogers.myenergiklient

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.format.parse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.BitSet
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration

private val myEnergiDateFormat = DateTimeComponents.Format {
    dayOfMonth(); char('-'); monthNumber(); char('-'); year()
    char(' ')
    hour(); char(':'); minute(); char(':'); second(); char('.')
}

@Serializable
sealed class MyEnergiDevice(
) {
    @SerialName("dat") lateinit var date: String
    @SerialName("tim") lateinit var inputTime: String
    lateinit var deviceClass: String
    @SerialName("sno") internal var serialNumberInt by notNullVal<Int>()
    @SerialName("fwv") lateinit var firmwareVersion: String
    @SerialName("ectp1") var ctPower1 by notNullVal<Int>()
    @SerialName("ectt1") var ctName1: String = ""
    @SerialName("ectp2") var ctPower2 by notNullVal<Int>()
    @SerialName("ectt2") var ctName2: String = ""
    @SerialName("ectp3") var ctPower3 by notNullVal<Int>()
    @SerialName("ectt3") var ctName3: String = ""

    val time: Instant
        get() = DateTimeComponents.parse("$date $inputTime", myEnergiDateFormat).toInstantUsingOffset()

    val dataAge: Duration
        get() = Clock.System.now() - time

    val serialNumber: String
        get() = serialNumberInt.toString()

    // generate cts
}

@Serializable
sealed class MyEnergiDiverter: MyEnergiDevice() {
    // A Myenergi diverter device
    @SerialName("vol") var rawVoltage by notNullVal<Int>()
    @SerialName("bsm") var isManualBoostInt by notNullVal<Int>()
    @SerialName("bst") var isTimedBoostInt by notNullVal<Int>()

    @SerialName("frq") var frequency by notNullVal<Float>()

    @SerialName("grd") var grid by notNullVal<Int>()

    @SerialName("gen") var generation by notNullVal<Int>()
    @SerialName("pha") var phaseCount by notNullVal<Int>()
    @SerialName("pri") var priority by notNullVal<Int>()

    @SerialName("che") var chargeAdded by notNullVal<Float>()

    @SerialName("div") var chargeRate by notNullVal<Int>()
    @SerialName("batteryDischargeEnabled") var isBatteryDischargeEnabled by notNullVal<Boolean>()
    @SerialName("newAppAvailable") var isNewAppAvailable by notNullVal<Boolean>()
    @SerialName("newBootloaderAvailable") var isNewBootloaderAvailable by notNullVal<Boolean>()
    lateinit var g100LockoutState: String
    lateinit var productCode: String
    var isVHubEnabled by notNullVal<Boolean>()


    // Daylight savings and Time Zone.
    @SerialName("dst") var daylightSavingsOffset by notNullVal<Int>()
    @SerialName("tz") var timezoneOffset by notNullVal<Int>()
    @SerialName("cmt") var cmt by notNullVal<Int>()

    val voltage: Float
        get() = if (rawVoltage > 1000) rawVoltage.toFloat() / 10 else rawVoltage.toFloat()

    val isManualBoost: Boolean
        get() = isManualBoostInt != 0

    val isTimedBoost: Boolean
        get() = isTimedBoostInt != 0
}

@Serializable
class Eddi: MyEnergiDiverter(){
    @SerialName("sta") internal var statusInt by notNullVal<Int>()
    @SerialName("hpri") var heaterPriority by notNullVal<Int>()
    @SerialName("hno") var heaterNumber by notNullVal<Int>()
    @SerialName("rbt") val remainingBoostTimeSeconds: Int = 0
    // These appear to be names, but not the same as shown in the app.
    @SerialName("ht1") lateinit var heater1: String
    @SerialName("ht2") lateinit var heater2: String
    internal var r1a: Int = 0
    internal var r2a: Int = 0
    @SerialName("rbc") internal var relayBoardInt by notNullVal<Int>()
    @SerialName("tp1") var temp1 by notNullVal<Int>()
    @SerialName("tp2") var temp2 by notNullVal<Int>()

    val status: EddiStatus
        get() = EddiStatus.fromInt(statusInt)

    val isRelayBoard: Boolean
        get() = relayBoardInt != 0

    val isRelay1Active: Boolean
        get() = r1a != 0

    val isRelay2Active: Boolean
        get() = r2a != 0

//    self.relay_1_boost_type = EBT[self._glimpse(data, 'r1b')]
//    self.relay_2_boost_type = EBT[self._glimpse(data, 'r2b')]
// Eddi Relay Boost Types.
// val EBT = listOf("Not boostable", "Boiler", "Heat Pump", "Battery")

}

//@Serializable
class Zappi: MyEnergiDiverter() {

    // Zappi specific
    @SerialName("sta") var statusInt by notNullVal<Int>()
    @SerialName("pst") lateinit var pStatusCode: String
    @SerialName("zmo") var modeInt: Int by notNullVal<Int>()

    @SerialName("mgl") var minGreenLevel by notNullVal<Int>()
    @SerialName("lck") var lockInt: Int = 0
        set(value) {
            lockStatus = ZappiLockStatus(lockInt)
            field = value
        }

    @SerialName("tbk") var manualBoostLevel by notNullVal<Int>()
    @SerialName("sbk") var smartBoostLevel by notNullVal<Int>()
    @SerialName("sbh") var smartBoostHour by notNullVal<Int>()
    @SerialName("sbm") var smartBoostMinute by notNullVal<Int>()
    @SerialName("bss") var isSmartBoostInt by notNullVal<Int>()

    var pwm by notNullVal<Int>()
    var zs by notNullVal<Int>()
    var zsl by notNullVal<Int>()
    var rdc by notNullVal<Int>()
    var rac by notNullVal<Int>()
    var rrac by notNullVal<Int>()
    var zsh by notNullVal<Int>()
    lateinit var ectt4: String
    lateinit var ectt5: String
    lateinit var ectt6: String
    @SerialName("beingTamperedWith") var isBeingTamperedWith by notNullVal<Boolean>()
    lateinit var phaseSetting: String


    val mode: ZappiMode
        get() = ZappiMode.fromInt(modeInt)

    val status: ZappiStatus
        get() = ZappiStatus.fromInt(statusInt)

    val carStatus: ZappiCarStatus
        get() = ZappiCarStatus.fromStringCode(pStatusCode)

    val isSmartBoost: Boolean
        get() = isSmartBoostInt != 0

    var lockStatus: ZappiLockStatus = ZappiLockStatus()

}

class ZappiLockStatus(lockValue: Int = 0){
    /*
    'lck' - representation of current PIN lock settings and zappi lock status
    Bit 0: Locked Now
    Bit 1: Lock when plugged in
    Bit 2: Lock when unplugged.
    Bit 3: Charge when locked.
    Bit 4: Charge Session Allowed (Even if locked)
     */
    private val bits: BitSet = BitSet(lockValue)
    val isLockedNow: Boolean = bits.get(0)
    val isLockedWhenPluggedIn: Boolean = bits.get(1)
    val isLockedWhenUnplugged: Boolean = bits.get(2)
    val isChargeWhenLocked: Boolean = bits.get(3)
    val isChargeSessionAllowed: Boolean = bits.get(4)
    /*
    class Zappi(MyEnergiDiverter):
        """A Zappi class"""

        def __init__(self, data, hc):

            self._values['Zappi'] = self.charge_rate


        def boost_active(self):
            """Return True if any kind of boost is active"""
            return self.manual_boost or self.smart_boost or self.timed_boost

        def car_connected(self):
            """Returns True if car is connected"""
            return self.pstatus != 'Disconnected'

        def waiting_for_export(self):
            return self.car_connected() and self.status == 'Waiting for export'

        def min_charge_rate_with_level(self):
            """Return the min charge rate in watts"""
            return int(self.voltage * 6 * self.min_green_level / 100)

        def get_values(self, key):
            """Return a tuple of (watts, amps) for a given device"""
            return (self._values[key], self._values[key] / self.voltage)
     */
}

internal fun <T : Any> notNullVal(): ReadWriteProperty<Any?, T> = NotNullVal()

private class NotNullVal<T : Any>() : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value != null) {
            throw IllegalStateException("Property ${property.name} may only be written once.")
        }
        this.value = value
    }

    override fun toString(): String =
        "NotNullProperty(${if (value != null) "value=$value" else "value not initialized yet"})"
}

enum class ZappiMode{
    FAULT_OR_STARTUP, FAST, ECO, ECO_PLUS, STOP;

    companion object {
        private val VALUES = entries.toTypedArray()
        fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
    }
}

enum class ZappiStatus {
    STARTING, WAITING_FOR_EXPORT, DSR, DIVERTING, BOOSTING, HOT;
    companion object {
        private val VALUES = ZappiStatus.entries.toTypedArray()
        fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
    }
}

enum class ZappiCarStatus(val str: String){
    DISCONNECTED("A"), CONNECTED ("B1"), WAITING_FOR_EV("B2"),
    CHARGE_STARTING("C1"), CHARGING("C2"), FAULT("F");

    companion object {
        private val VALUES = ZappiCarStatus.entries.toTypedArray()
        fun fromStringCode(str: String) = VALUES.first { it.str == str }
    }
}


enum class EddiStatus{
    UNKNOWN, WAITING_FOR_SURPLUS, PAUSED, DIVERTING,
    BOOST, MAX_TEMP_REACHED, STOPPED;
    companion object {
        private val VALUES = EddiStatus.entries.toTypedArray()
        fun fromInt(value: Int) = VALUES.first { it.ordinal == value }
    }
}


class Harvi
class Libbi