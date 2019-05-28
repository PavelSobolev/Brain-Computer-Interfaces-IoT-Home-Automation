package sobolev.bciot_getdata.Connectivity

import android.annotation.SuppressLint
import java.util.Observable

import com.neurosky.connection.ConnectionStates
import com.neurosky.connection.DataType.MindDataType
import com.neurosky.connection.EEGPower
import com.neurosky.connection.TgStreamHandler
import com.neurosky.connection.TgStreamReader

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.res.Resources
import android.os.Handler
import android.os.Message
import sobolev.bciot_getdata.R

// receiving data from brain mobile headset (using Bluetooth channel)
// Neurosky BrainWave SDK is used
class BluetoothHeadsetConnector (var isInet: Boolean, var res:Resources) : Observable() {
    var connectionStatus: Int = ConnectionStates.STATE_INIT
    var connectionMessage: String = ""

    // "NeuroSky" device reader
    private var thinkGearReader: TgStreamReader? = null

    // bluetooth connectivity
    private var bluetoothSocket: BluetoothAdapter? = null

    // processing of device's data
    private var currentState: Int = 0
    //private val isPressing = false // !!!!!!!!!!!!!!!!!!!!!!!!!!
    private val MSG_BAD_DATA = 1001
    private val MSG_STATE = 1002
    //private val MSG_CONNECT = 1003  //////////!!!!!!!!!!!
    private var isReadFilter = false

    internal var raw: Int = 0
    private var badPacketCount = 0

    fun startConnection(): Pair<Int, String> {
        try {
            bluetoothSocket = BluetoothAdapter.getDefaultAdapter()

            var reason: Int = 0
            var message: String = ""

            if (!isInet) reason += 1
            if (bluetoothSocket == null || !bluetoothSocket!!.isEnabled()) reason += 2

            message = when (reason) {
                1 -> res.getString(R.string.no_inet)
                2 -> res.getString(R.string.no_blue_tooth)
                3 -> res.getString(R.string.no_blue_tooth_inet)
                else -> ""
            }

            if (reason > 0) return Pair(0, message)
        }
        catch (ex: Exception) {
            return Pair(1, ex.toString() + "Impossible to establish connection to device. Try to check your headset and restart the app.")
        }

        return Pair(2, "Connecting the NeuroSky MindWave device ... ")
    }

    fun startReadingBrainWaveData() {
        if (bluetoothSocket == null) {
            return
        }

        // find MindWave device among paired bluetooth devices
        var bdevice: BluetoothDevice? = null
        for (bd in bluetoothSocket!!.bondedDevices) {
            if (bd.name.toLowerCase().contains("mindwave")) {
                bdevice = bd
                break
            }
        }

        bdevice = bluetoothSocket!!.getRemoteDevice(bdevice!!.address)

        if (bdevice != null) createStreamReader(bdevice!!)
    }

    fun stopReadingBrainWaveData() {
        if (thinkGearReader != null) {
            thinkGearReader!!.stop()
            thinkGearReader = null
        }
    }

    private val LinkDetectedHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1234 -> {
                    thinkGearReader!!.MWM15_getFilterType()
                    isReadFilter = true
                }
                1235 -> {
                    thinkGearReader!!.MWM15_setFilterType(MindDataType.FilterType.FILTER_60HZ)
                    this.sendEmptyMessageDelayed(1237, 1000)
                }
                1236 -> {
                    thinkGearReader!!.MWM15_setFilterType(MindDataType.FilterType.FILTER_50HZ)
                    this.sendEmptyMessageDelayed(1237, 1000)
                }

                1237 -> {
                    thinkGearReader!!.MWM15_getFilterType()
                }

                MindDataType.CODE_FILTER_TYPE -> {
                    if (isReadFilter) {
                        isReadFilter = false
                        if (msg.arg1 == MindDataType.FilterType.FILTER_50HZ.value) {
                            this.sendEmptyMessageDelayed(1235, 1000)
                        } else if (msg.arg1 == MindDataType.FilterType.FILTER_60HZ.value) {
                            this.sendEmptyMessageDelayed(1236, 1000)
                        }
                    }
                }

                MindDataType.CODE_MEDITATION -> {
                    BrainWaveData.LastMeditationData = msg.arg1
                }

                MindDataType.CODE_ATTENTION -> {
                    BrainWaveData.LastAttentionData = msg.arg1
                }

                MindDataType.CODE_EEGPOWER -> {
                    val power = msg.obj as EEGPower
                    if (power.isValidate) {
                        BrainWaveData.WaveDelta = (power.delta.toDouble())
                        BrainWaveData.WaveTheta = (power.theta.toDouble())
                        BrainWaveData.WaveLowAlpha = (power.lowAlpha.toDouble())
                        BrainWaveData.WaveHighAlpha = (power.highAlpha.toDouble())
                        BrainWaveData.WaveLowBeta = (power.lowBeta.toDouble())
                        BrainWaveData.WaveHighBeta = (power.highBeta.toDouble())
                        BrainWaveData.WaveLowGamma = (power.lowGamma.toDouble())
                        BrainWaveData.WaveHighGamma = (power.middleGamma.toDouble())
                    }
                }
                MindDataType.CODE_POOR_SIGNAL -> {
                    val poorSignal = msg.arg1
                }
            }
            super.handleMessage(msg)
        }
    }

    private val callback = object : TgStreamHandler {

        override fun onStatesChanged(connectionStates: Int) {
            currentState = connectionStates

            when (connectionStates) {
                ConnectionStates.STATE_CONNECTED -> {
                    connectionMessage = res.getString(R.string.state_connected)
                    connectionStatus = ConnectionStates.STATE_CONNECTED
                    BrainWaveData.IsErrorState = false
                    setChanged()
                    notifyObservers()
                }

                ConnectionStates.STATE_WORKING -> {
                    LinkDetectedHandler.sendEmptyMessageDelayed(1234, 4000)
                    BrainWaveData.IsErrorState = false
                }

                ConnectionStates.STATE_GET_DATA_TIME_OUT -> {
                    // also if bluetooth is turned off
                    connectionMessage = res.getString(R.string.state_get_data_time_out)
                    connectionStatus = ConnectionStates.STATE_GET_DATA_TIME_OUT
                    BrainWaveData.IsErrorState = true
                    setChanged()
                    notifyObservers()
                }
                ConnectionStates.STATE_COMPLETE -> {
                }
                ConnectionStates.STATE_STOPPED -> {
                    connectionMessage = res.getString(R.string.state_stopped)
                    connectionStatus = ConnectionStates.STATE_STOPPED
                    BrainWaveData.IsErrorState = true
                    setChanged()
                    notifyObservers()
                }
                ConnectionStates.STATE_DISCONNECTED -> {
                    connectionMessage = res.getString(R.string.state_disconnected)
                    connectionStatus = ConnectionStates.STATE_DISCONNECTED
                    BrainWaveData.IsErrorState = true
                    setChanged()
                    notifyObservers()
                }
                ConnectionStates.STATE_ERROR -> {
                    connectionMessage = res.getString(R.string.state_error)
                    connectionStatus = ConnectionStates.STATE_ERROR
                    BrainWaveData.IsErrorState = true
                    setChanged()
                    notifyObservers()
                }
                ConnectionStates.STATE_FAILED -> {
                    connectionMessage = res.getString(R.string.state_failed)
                    connectionStatus = ConnectionStates.STATE_FAILED
                    BrainWaveData.IsErrorState = true
                    setChanged()
                    notifyObservers()
                }
            }

            val msg = LinkDetectedHandler.obtainMessage()
            msg.what = MSG_STATE
            msg.arg1 = connectionStates
            LinkDetectedHandler.sendMessage(msg)
        }

        override fun onRecordFail(a: Int) {
        }

        override fun onChecksumFail(payload: ByteArray, length: Int, checksum: Int) {
            badPacketCount++
            val msg = LinkDetectedHandler.obtainMessage()
            msg.what = MSG_BAD_DATA
            msg.arg1 = badPacketCount
            LinkDetectedHandler.sendMessage(msg)
        }

        override fun onDataReceived(datatype: Int, data: Int, obj: Any?) {
            val msg = LinkDetectedHandler.obtainMessage()
            msg.what = datatype
            msg.arg1 = data
            if (obj != null) msg.obj = obj
            LinkDetectedHandler.sendMessage(msg)
        }
    }

    private fun createStreamReader(bdevice: BluetoothDevice) {
        if (thinkGearReader == null) {
            thinkGearReader = TgStreamReader(bdevice, callback)
            thinkGearReader!!.connectAndStart()
        }
    }
}