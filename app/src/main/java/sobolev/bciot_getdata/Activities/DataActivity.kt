package sobolev.bciot_getdata.Activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

import com.neurosky.connection.ConnectionStates
import kotlinx.android.synthetic.main.activity_data.*
import sobolev.bciot_getdata.*
import sobolev.bciot_getdata.Connectivity.BluetoothHeadsetConnector
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.Helpers.AppState

import java.util.*

class DataActivity : AppCompatActivity(), Observer {
    override fun update(observableObject: Observable?, observableData: Any?) {
        // brain activity data were updated
        if (observableObject is BrainWaveData) {
            // new data started arriving
            if (BrainWaveData.LastMeditationData != 0 && BrainWaveData.LastAttentionData != 0) {
                runOnUiThread()
                {
                    // if current activity is a start screen
                    if (BrainWaveData.CurrentAppState == AppState.CONNECTION) {
                        try { // remove current activity from observers collections of observable classes
                            BrainWaveData.deleteObserver(this)
                            BrainWaveData.connector?.deleteObserver(this)
                        }
                        finally { // and go to the next activity
                            wheelView.visibility = View.INVISIBLE
                            imageView.imageAlpha = 255
                            imageView.isEnabled = false

                            if (!BrainWaveData.IsErrorState) {
                                val chooseModeIntent: Intent = Intent(this, ChooseModeActivity::class.java)
                                startActivity(chooseModeIntent)
                            }
                        }
                    }
                }
            }
        }

        // status of connection with device was changed (error or lost connection)
        if (observableObject is BluetoothHeadsetConnector) {
            runOnUiThread {
                if (BrainWaveData.connector?.connectionStatus == ConnectionStates.STATE_CONNECTED) {
                    // show gif animation during the process of retrieving of data from the device
                    wheelView.visibility = View.VISIBLE
                }

                statusText.text = BrainWaveData.connector?.connectionMessage
                imageView.imageAlpha = 255
                imageView.isEnabled = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        wheelView.loadUrl(resources.getString(R.string.gif_adress))

        // ---------- connecting to headset after clicking on central picture -
        imageView.setOnClickListener() { it -> initBrainDataConnection(it) }
        //---------- end connecting to headset --------------------------------
    }

    fun initBrainDataConnection(view: View) {
        imageView.isEnabled = false

        val netService: ConnectivityManager =
                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val inetInfo: NetworkInfo? = netService.activeNetworkInfo

        try {
            BrainWaveData.deleteObserver(this)
        } finally {
            BrainWaveData.addObserver(this)
        }

        BrainWaveData.connector =
                BluetoothHeadsetConnector(inetInfo?.isConnected
                        ?: false, applicationContext.resources)
        BrainWaveData.connector?.addObserver(this)

        var (code, message) =
                BrainWaveData.connector?.startConnection()
                        ?: Pair(0, resources.getString(R.string.severe_error))

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        statusText.text = message

        if (code in 0..1) {
            imageView.imageAlpha = 255
            imageView.isEnabled = true
            return
        } else {
            imageView.imageAlpha = 125
            BrainWaveData.BrainDataToUse = 0
            BrainWaveData.connector?.startReadingBrainWaveData() // here data from device becomes available
        }
    }

    override fun onResume() = super.onResume()

    override fun onDestroy() {
        BrainWaveData.connector?.stopReadingBrainWaveData()
        BrainWaveData.IsOn = 0
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        BrainWaveData.IsOn = 0
    }

    override fun onBackPressed() {
        // if something is going wrong then there is no ability to use another windows (activities)
        // until situation becomes OK
        if (BrainWaveData.IsErrorState) return
    }
}


/*
    add onResume and onSuspend events handlers
 */

/*

Scenario of application:
There are three modes of functioning of application:
    1) Simple: controlling of attention or mediation level by turning bulb on or off
    2) Intermediate: controlling of attention or meditation level by changing level of brightness of the lamp
    3) Advanced: controlling of bulb's color by changing proportion of attention and meditation

    --------------------------------

    Implement function of turning of notifications during the exercise
    And sent automatic SMS with message "i am now exercising (meditating)" if level of meditation is above some level
    or control some functions of saxophone by mind activity with respect of alpha, beta etc. midwives

    ------------------------------------

    Implement Raspberry Server for ZWave hub

 */


/*

https://mindwavebci.firebaseio.com/

JSON structure
{
    "State" : Boolean,
    "Level" : integer, // 0 .. 100
    "Hue" : integer,   // 0 .. 360
    "Brightness" : integer, // 0 .. 100
    "IsWhite" : Boolean,
    "ParamToApply" : Integer // 0-level, 1-Hue and brightness, 2-whiteness
    "LastAttentionData" : Integer, // 0..100
    "LastMeditationData" : Integer,
    "Delta" : Integer,
    "Theta" : Integer,
    "LowAlpha" : Integer,
    "HighAlpha" : Integer,
    "LowBeta" : Integer,
    "HighBeta" : Integer,
    "LowGamma" : Integer,
    "HighGamma" : Integer,
    "LastModified" : "10-30-2018 12:51:25"
}


--------------------
        delta (0-4HZ)<br />
        theta (4-8HZ) <br />
        low alpha (8-10 Hz) <br />
        high alpha (10-12 Hz) <br />
        low beta (12-18 Hz) <br />
        high beta (18-30 Hz) <br />
        low gamma (30-50 Hz) <br />
        high gamma (50-70 Hz)
-----------------------

 */

