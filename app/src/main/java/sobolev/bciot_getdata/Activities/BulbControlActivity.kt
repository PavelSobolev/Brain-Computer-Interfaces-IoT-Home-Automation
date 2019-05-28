package sobolev.bciot_getdata.Activities

//import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.neurosky.connection.ConnectionStates

import kotlinx.android.synthetic.main.activity_bulb_control.*
import kotlinx.android.synthetic.main.content_bulb_control.*
import sobolev.bciot_getdata.Helpers.AppState
import sobolev.bciot_getdata.Connectivity.BluetoothHeadsetConnector
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.R
import sobolev.bciot_getdata.Helpers.TabsAdaptor
import java.util.*
import android.support.v7.app.AlertDialog
import android.view.View
import kotlinx.android.synthetic.main.attention_freshold_layout.view.*
import sobolev.bciot_getdata.Connectivity.GUIStateNotifier
import sobolev.bciot_getdata.Graphics.AttentionAttractiveScene
import sobolev.bciot_getdata.Graphics.MeditationPromotingScene

class BulbControlActivity : AppCompatActivity(), Observer {
    private var currentView: Int = -1

    companion object {
        // floating buttons (references for using in external classes)
        var refFab3: FloatingActionButton? = null
        var refFab2: FloatingActionButton? = null
        var refFab: FloatingActionButton? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bulb_control)
        setSupportActionBar(toolbar)

        BrainWaveData.BrainDataToUse = 0
        refFab3 = fab3
        refFab2 = fab2
        refFab = fab

        // set up tabs for this activity
        viewPager.adapter = TabsAdaptor(supportFragmentManager, brainTab.tabCount)

        // event of scrolling of tabs in TabLayout
        brainTab.addOnTabSelectedListener(
                object : TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab) {/*not used*/
                    }

                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewPager.currentItem = tab.position
                        GUIStateNotifier.updateFragmentsGUI()
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {/*not used*/
                    }
                })
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(brainTab))

        fab.setOnClickListener { _ ->
            if (BrainWaveData.BrainDataToUse == 0) { // attention is used
                alertView(resources.getString(R.string.help_attention), "Information about Attention")
            }

            if (BrainWaveData.BrainDataToUse == 1) { // relaxation is used
                alertView(resources.getString(R.string.help_meditation), "Information about Meditation")
            }
        }

        fab2.setOnClickListener { _ ->
            if (BrainWaveData.ParamToApply == -1) return@setOnClickListener

            if (BrainWaveData.BrainDataToUse == 0) {
                alertViewImage()
            } else if (BrainWaveData.BrainDataToUse == 1) {
                alertMeditation()
            }
        }

        fab3.setOnClickListener { _ ->
            when (BrainWaveData.ParamToApply) {
                0 -> alertSettings()
                1 -> return@setOnClickListener
                2 -> return@setOnClickListener
            }

        }

        // start getting live data from brainwave headset
        BrainWaveData.CurrentAppState = AppState.SMART_HOME_CONTROL
        BrainWaveData.addObserver(this)
        BrainWaveData.connector?.addObserver(this)
    }

    private fun alertViewImage() {
        val builder = AlertDialog.Builder(this)

        var viewAnimation: AttentionAttractiveScene = AttentionAttractiveScene(this)
        viewAnimation.setPadding(0, 0, 0, 0)

        builder.setPositiveButton("Ok", { dialog, id -> })
                .setTitle("Watch the moving ball")
                .setView(viewAnimation)

        val dialog = builder.create()
        dialog.show()
    }

    private fun alertMeditation() {
        val builder = AlertDialog.Builder(this)

        var viewAnimation: MeditationPromotingScene = MeditationPromotingScene(this)
        viewAnimation.setPadding(0, 0, 0, 0)


        viewAnimation.player?.setVolume(BrainWaveData.LastMeditationData / 100f, BrainWaveData.LastMeditationData / 100f)
        viewAnimation.player?.isLooping = true
        viewAnimation.player?.start()

        builder.setPositiveButton("Ok") { dialog, id ->
            viewAnimation.player?.stop()
            viewAnimation.player?.release()
            viewAnimation.player = null
        }
                .setTitle("Promote level of Meditation")
                .setView(viewAnimation)

        val dialog = builder.create()
        dialog.show()
    }

    private fun alertSettings() {

        val builder = AlertDialog.Builder(this)

        var settingsView: View = layoutInflater.inflate(R.layout.attention_freshold_layout, null)

        builder.setView(settingsView)
                .setPositiveButton("Ok") { dialog, which ->
                    BrainWaveData.OffThreshold = settingsView.offThreshold.value
                    BrainWaveData.OnThreshold = settingsView.onThreshold.value
                }
                .setNegativeButton("Cancel") { _, _ -> return@setNegativeButton }

        settingsView.onThreshold.minValue = 50
        settingsView.onThreshold.maxValue = 90
        settingsView.onThreshold.value = BrainWaveData.OnThreshold

        settingsView.offThreshold.minValue = 10
        settingsView.offThreshold.maxValue = 40
        settingsView.offThreshold.value = BrainWaveData.OffThreshold

        val dialog = builder.create()
        dialog.show()
    }

    private fun alertView(message: String, title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton("Ok") { dialog, id -> }

        // Create the AlertDialog object and return it
        val dialog = builder.create()
        dialog.show()
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
            // when going back to previous activity - stop getting notifications from the headset
            BrainWaveData.deleteObserver(this)
            BrainWaveData.connector?.deleteObserver(this)
        } finally {
            super.onBackPressed()
        }
    }
}
