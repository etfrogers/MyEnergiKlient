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
class CtMeter(
    val name: String,
    val power: Int,
    val phase: Int?
)

typealias Meters = Map<String, CtMeter>
@Serializable
sealed class MyEnergiDevice{
    @SerialName("dat") lateinit var date: String
        internal set
    @SerialName("tim") lateinit var inputTime: String
        internal set
    lateinit var deviceClass: String
        internal set
    @SerialName("sno") internal var serialNumberInt by notNullVal<Int>()
    @SerialName("fwv") lateinit var firmwareVersion: String
        internal set

    var ctMeters: Meters = mapOf()
        private set

    val time: Instant
        get() = DateTimeComponents.parse("$date $inputTime", myEnergiDateFormat).toInstantUsingOffset()

    val dataAge: Duration
        get() = Clock.System.now() - time

    val serialNumber: String
        get() = serialNumberInt.toString()

    fun setMeters(meters: Meters){
        ctMeters = meters
    }
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
        internal set
    lateinit var productCode: String
        internal set
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
        internal set
    @SerialName("ht2") lateinit var heater2: String
        internal set
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
        internal set
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
    @SerialName("beingTamperedWith") var isBeingTamperedWith by notNullVal<Boolean>()
    lateinit var phaseSetting: String
        internal set

    val mode: ZappiMode
        get() = ZappiMode.fromInt(modeInt)

    val status: ZappiStatus
        get() = ZappiStatus.fromInt(statusInt)

    val carStatus: ZappiCarStatus
        get() = ZappiCarStatus.fromStringCode(pStatusCode)

    val isSmartBoost: Boolean
        get() = isSmartBoostInt != 0

    var lockStatus: ZappiLockStatus = ZappiLockStatus()


//    self._values['Zappi'] = self.charge_rate


    /** Return True if any kind of boost is active */
    val isBoostActive: Boolean
        get() = isManualBoost || isSmartBoost || isTimedBoost

    /** Returns True if car is connected */
    val isCarConnected: Boolean
        get() = carStatus != ZappiCarStatus.DISCONNECTED

    val isWaitingForExport: Boolean
        get() = isCarConnected && status == ZappiStatus.WAITING_FOR_EXPORT

    /** Return the min charge rate in watts */
    val minChargeRateWatts: Int
        get() = (voltage * 6 * minGreenLevel / 100).toInt()

    /*
    def get_values(self, key):
        """Return a tuple of (watts, amps) for a given device"""
        return (self._values[key], self._values[key] / self.voltage)
 */

}

class ZappiLockStatus(lockValue: Int = 0) {
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
}

internal fun buildCtMeters(names: Map<Int, String>, powers: Map<Int, Int>, phases: Map<Int, Int>): Map<String, CtMeter> {


    return names.map{meter ->
        val index = meter.key
        val name = meter.value
        val power = powers[index] ?: 0
        val phase = phases[index]
        name to CtMeter(name, power, phase)
    }.toMap()
/*
ct = 0
        while True:
            ct += 1
            # These are present in Harvi data for some reason.
            ct_phase = self._glimpse_safe(data, 'ect{}p'.format(ct))
            ct_name_key = 'ectt{}'.format(ct)
            if ct_phase not in {1, 0}:
                log.debug('CT %s is on phase %d', ct_name_key, ct_phase)
            if ct_name_key not in data:
                break
            value = self._glimpse_safe(data, 'ectp{}'.format(ct))
            ct_name = self._glimpse(data, ct_name_key)
            if ct_name == 'None':
                continue
            if ct_name == 'Internal Load':
                continue
            if self.sno in house_data and ct_name_key in house_data[self.sno]:
                ct_name = house_data[self.sno][ct_name_key]
                value = value * -1
            if ct_name != 'Grid':
                if ct_name in self._values:
                    self._values[ct_name] += value
                else:
                    self._values[ct_name] = value
            else:
                if 'Grid' not in self._values:
                    # only take the first grid value for non-netting 3 phase
                    self._values['Grid'] = value
                else:
                    if 'net_phases' in house_data and house_data['net_phases']:
                        # 3 phase all report with same name "grid" so need to sum them
                        # note this produces a net import/export number.
                        # if phases are not netted Zappi assumes export monitoring on phase 1
                        self._values['Grid'] = self._values['Grid'] + value
        log.debug(self._values)

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