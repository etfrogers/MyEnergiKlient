package com.etfrogers.myenergiklient

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.format.parse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.properties.Delegates
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
    @SerialName("sno") var serialNumber by Delegates.notNull<Int>()
    @SerialName("fwv") lateinit var firmwareVersion: String
    @SerialName("ectp1") var ctPower1 by Delegates.notNull<Int>()
    @SerialName("ectt1") var ctName1: String = ""
    @SerialName("ectp2") var ctPower2 by Delegates.notNull<Int>()
    @SerialName("ectt2") var ctName2: String = ""
    @SerialName("ectp3") var ctPower3 by Delegates.notNull<Int>()
    @SerialName("ectt3") var ctName3: String = ""

    val time: Instant
        get() = DateTimeComponents.parse("$date $inputTime", myEnergiDateFormat).toInstantUsingOffset()

    val dataAge: Duration
        get() = Clock.System.now() - time


    // generate cts
}

@Serializable
sealed class MyEnergiDiverter: MyEnergiDevice() {
    // A Myenergi diverter device
    @SerialName("vol") var rawVoltage by Delegates.notNull<Int>()
    @SerialName("bsm") var isManualBoostInt by Delegates.notNull<Int>()
    @SerialName("bst") var isTimedBoostInt by Delegates.notNull<Int>()

    @SerialName("frq") var frequency by Delegates.notNull<Float>()

    @SerialName("grd") var grid by Delegates.notNull<Int>()

    @SerialName("gen") var generation by Delegates.notNull<Int>()
    @SerialName("pha") var phaseCount by Delegates.notNull<Int>()
    @SerialName("pri") var priority by Delegates.notNull<Int>()

    @SerialName("che") var chargeAdded by Delegates.notNull<Float>()

    @SerialName("div") var chargeRate by Delegates.notNull<Int>()
    @SerialName("batteryDischargeEnabled") var isBatteryDischargeEnabled by Delegates.notNull<Boolean>()
    @SerialName("newAppAvailable") var isNewAppAvailable by Delegates.notNull<Boolean>()
    @SerialName("newBootloaderAvailable") var isNewBootloaderAvailable by Delegates.notNull<Boolean>()
    lateinit var g100LockoutState: String
    lateinit var productCode: String
    var isVHubEnabled by Delegates.notNull<Boolean>()


    // Daylight savings and Time Zone.
    @SerialName("dst") var dst by Delegates.notNull<Int>()
    @SerialName("tz") var tz by Delegates.notNull<Int>()
    @SerialName("cmt") var cmt by Delegates.notNull<Int>()

    val voltage: Float
        get() = if (rawVoltage > 1000) rawVoltage.toFloat() / 10 else rawVoltage.toFloat()

    val isManualBoost: Boolean
        get() = isManualBoostInt != 0

    val isTimedBoost: Boolean
        get() = isTimedBoostInt != 0
}

@Serializable
class Eddi: MyEnergiDiverter(){
    @SerialName("sta") var statusInt by Delegates.notNull<Int>()
    var hpri by Delegates.notNull<Int>()
    var hno by Delegates.notNull<Int>()
    lateinit var ht1: String
    lateinit var ht2: String
    var r1a by Delegates.notNull<Int>()
    var r2a by Delegates.notNull<Int>()
    var rbc by Delegates.notNull<Int>()
    var tp1 by Delegates.notNull<Int>()
    var tp2 by Delegates.notNull<Int>()

    val status: EddiStatus
        get() = EddiStatus.fromInt(statusInt)

    /*
    class Eddi(MyEnergiDiverter):
    """A Eddi class"""

    def __init__(self, data, hc):
        super().__init__(data, hc)
        # Priority
        self.heater_priority = self._glimpse(data, 'hpri')

        # These appear to be names, but not the same as shown in the app.
        self._glimpse(data, 'ht1')
        self._glimpse(data, 'ht2')

        self.heater_number = self._glimpse(data, 'hno')

        self.status = ESTATUSES[self._glimpse(data, 'sta')]

        # Boost time left, in seconds.
        self.remaining_boost_time = self._glimpse_safe(data, 'rbt')

        self.temp_1 = self._glimpse(data, 'tp1')
        self.temp_2 = self._glimpse(data, 'tp2')

        relay_board = bool(self._glimpse(data, 'rbc'))
        if not relay_board:
            return

        self.relay_1_active = bool(self._glimpse(data, 'r1a'))
        self.relay_2_active = bool(self._glimpse(data, 'r2a'))

        self.relay_1_boost_type = EBT[self._glimpse(data, 'r1b')]
        self.relay_2_boost_type = EBT[self._glimpse(data, 'r2b')]
     */
}

//@Serializable
class Zappi: MyEnergiDiverter() {

    // Zappi specific
    @SerialName("sta") var statusInt by Delegates.notNull<Int>()
    @SerialName("pst") lateinit var pStatusCode: String
    @SerialName("zmo") var modeInt: Int by Delegates.notNull<Int>()

    @SerialName("mgl") var minGreenLevel by Delegates.notNull<Int>()
    @SerialName("lck") var lockInt by Delegates.notNull<Int>()
    @SerialName("tbk") var manualBoostLevel by Delegates.notNull<Int>()
    @SerialName("sbk") var smartBoostLevel by Delegates.notNull<Int>()
    @SerialName("sbh") var smartBoostHour by Delegates.notNull<Int>()
    @SerialName("sbm") var smartBoostMinute by Delegates.notNull<Int>()
    @SerialName("bss") var isSmartBoostInt by Delegates.notNull<Int>()

    var pwm by Delegates.notNull<Int>()
    var zs by Delegates.notNull<Int>()
    var zsl by Delegates.notNull<Int>()
    var rdc by Delegates.notNull<Int>()
    var rac by Delegates.notNull<Int>()
    var rrac by Delegates.notNull<Int>()
    var zsh by Delegates.notNull<Int>()
    lateinit var ectt4: String
    lateinit var ectt5: String
    lateinit var ectt6: String
    @SerialName("beingTamperedWith") var isBeingTamperedWith by Delegates.notNull<Boolean>()
    lateinit var phaseSetting: String


    val mode: ZappiMode
        get() = ZappiMode.fromInt(modeInt)

    val status: ZappiStatus
        get() = ZappiStatus.fromInt(statusInt)

    val carStatus: ZappiCarStatus
        get() = ZappiCarStatus.fromStringCode(pStatusCode)

    val isSmartBoost: Boolean
        get() = isSmartBoostInt != 0
}

/*
class Zappi(MyEnergiDiverter):
    """A Zappi class"""

    def __init__(self, data, hc):

        self._values['Zappi'] = self.charge_rate

        # https://myenergi.info/viewtopic.php?p=19026 for details
        # of locking.
        lock = self.lock
        if lock >= 16:
            # Status
            log.debug('Charge session allowed')
            lock -= 16
        if lock >= 8:
            # Setting
            log.debug('Charge when locked')
            lock -= 8
        if lock >= 4:
            log.debug('Lock when unplugged')
            lock -= 4
        if lock >= 2:
            log.debug('Lock when plugged in')
            lock -= 2
        if lock >= 1:
            log.info('Locked Now')
            lock -= 1

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

    def report(self, rep=None):
        """Return a multi-line test description of the current state"""
        if not rep:
            rep = ReportCapture()
        rep.log(self.zname+' mode is {}'.format(self.mode))
        # The min charge level is often given as 1.4kw however it needs to take into
        # account voltage.
        pf = power_format(self.min_charge_rate_with_level())
        rep.log('Min Green level is {}% ({})'.format(self.min_green_level, pf))

        rep.log('Car status is {}'.format(self.status))
        (charge_watts, charge_amps) = self.get_values('Zappi')
        if charge_watts:
            rep.log('Car is charging at {} ({:.1f} amps)'.format(power_format(charge_watts),
                                                                 charge_amps))
        if self.charge_added:
            rep.log('Car charge added {}kWh'.format(self.charge_added))
        rep.log('Plug status is {}'.format(self.pstatus))
        if self.manual_boost:
            rep.log('Device is manual boosting')
            rep.log('Manual boost is set to add {}kWh'.format(self.manual_boost_level))
        if self.smart_boost:
            rep.log('Device is smart boosting')
            rep.log('Smart boost is set to add {}kWh by {}:{:02d}'.format(self.smart_boost_level,
                                                                          self.smart_boost_hour,
                                                                          self.smart_boost_minute))

        return rep.get_log()

    def get_values(self, key):
        """Return a tuple of (watts, amps) for a given device"""
        return (self._values[key], self._values[key] / self.voltage)
 */



class Harvi
class Libbi