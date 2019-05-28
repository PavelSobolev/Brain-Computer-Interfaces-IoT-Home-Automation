package sobolev.bciot_getdata.Activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.neurosky.connection.ConnectionStates

import kotlinx.android.synthetic.main.activity_training.*
import sobolev.bciot_getdata.Helpers.AppState
import sobolev.bciot_getdata.Connectivity.BluetoothHeadsetConnector
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.R
import java.util.*

class TrainingActivity : AppCompatActivity(), Observer {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        BrainWaveData.CurrentAppState = AppState.TRAINING
        BrainWaveData.addObserver(this)
        BrainWaveData.connector?.addObserver(this)
    }

    override fun update(sender: Observable?, data: Any?) {
        // new brain data is arriving
        if (sender is BrainWaveData) {
            title = BrainWaveData.AttentionAndMeditationToString()
        }

        // data from Bluetooth and Internet connector is arriving
        if (sender is BluetoothHeadsetConnector) {
            // error while communicating with EEG device
            if (!intArrayOf(ConnectionStates.STATE_CONNECTED,
                            ConnectionStates.STATE_WORKING).contains(BrainWaveData.connector?.connectionStatus
                            ?: 0)) {
                // error during data reading - something went wrong
                runOnUiThread()
                {
                    Toast.makeText(this, BrainWaveData.connector!!.connectionMessage, Toast.LENGTH_LONG).show()
                }

                BrainWaveData.CurrentAppState = AppState.CONNECTION
                BrainWaveData.deleteObserver(this)
                BrainWaveData.connector?.deleteObserver(this)

                startActivity(Intent(this, DataActivity::class.java))
            }
        }
    }

    override fun onBackPressed() {
        try {
            BrainWaveData.deleteObserver(this)
            BrainWaveData.connector?.deleteObserver(this)
        } finally {
            super.onBackPressed()
        }
    }

}