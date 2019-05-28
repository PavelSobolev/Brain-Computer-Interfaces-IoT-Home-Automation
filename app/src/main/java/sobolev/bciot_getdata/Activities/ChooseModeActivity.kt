package sobolev.bciot_getdata.Activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.neurosky.connection.ConnectionStates
import sobolev.bciot_getdata.*
import sobolev.bciot_getdata.Connectivity.BluetoothHeadsetConnector
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.Helpers.AppState
import java.util.*

class ChooseModeActivity : AppCompatActivity(), Observer {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_mode)

        initActivity()

        findViewById<ImageView>(R.id.goBrainControl).setOnClickListener() { it ->
            if (!BrainWaveData.IsErrorState) {
                BrainWaveData.deleteObserver(this)
                BrainWaveData.connector?.deleteObserver(this)

                findViewById<ImageView>(R.id.goBrainControl).imageAlpha = 125
                findViewById<ImageView>(R.id.goTraining).imageAlpha = 125

                var goToBrainControl: Intent = Intent(this, BulbControlActivity::class.java)
                startActivityForResult(goToBrainControl, 0)
                initActivity()
            }
        }

        findViewById<ImageView>(R.id.goTraining).setOnClickListener() { it ->
            if (!BrainWaveData.IsErrorState) {
                BrainWaveData.deleteObserver(this)
                BrainWaveData.connector?.deleteObserver(this)

                findViewById<ImageView>(R.id.goTraining).imageAlpha = 125
                findViewById<ImageView>(R.id.goBrainControl).imageAlpha = 125


                var goToBrainControl: Intent = Intent(this, TrainingActivity::class.java)
                startActivityForResult(goToBrainControl, 0)
                initActivity()
            }
        }
    }

    private fun initActivity() {
        BrainWaveData.CurrentAppState = AppState.CHOOSE_MODE
        BrainWaveData.addObserver(this)
        BrainWaveData.connector?.addObserver(this)

        findViewById<ImageView>(R.id.goBrainControl).imageAlpha = 255
        findViewById<ImageView>(R.id.goTraining).imageAlpha = 255
    }

    override fun onBackPressed() {
        // cancel back navigation
        if (BrainWaveData.CurrentAppState == AppState.CHOOSE_MODE) return
    }

    override fun update(observable: Observable?, p1: Any?) {

        if (observable is BrainWaveData) {
            title = BrainWaveData.AttentionAndMeditationToString()
        } else if (observable is BluetoothHeadsetConnector) {
            // error while communicating with EEG device
            if (!intArrayOf(ConnectionStates.STATE_CONNECTED,
                            ConnectionStates.STATE_WORKING).contains(BrainWaveData.connector?.connectionStatus
                            ?: 0)) {
                // error during data reading - something went wrong
                runOnUiThread() {
                    Toast.makeText(this, BrainWaveData.connector!!.connectionMessage, Toast.LENGTH_LONG).show()
                }

                BrainWaveData.CurrentAppState = AppState.CONNECTION
                BrainWaveData.deleteObserver(this)
                BrainWaveData.connector?.deleteObserver(this)

                startActivity(Intent(this, DataActivity::class.java))
            }
        }
    }
}