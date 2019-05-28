package sobolev.bciot_getdata.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_attention_control.*
import kotlinx.android.synthetic.main.fragment_attention_control.view.*
import sobolev.bciot_getdata.Activities.BulbControlActivity
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.Connectivity.GUIStateNotifier
import sobolev.bciot_getdata.Graphics.AttentionControlScene
import sobolev.bciot_getdata.R
import java.util.*

// fragment shows user's brain attention
class AttentionControlFragment : Fragment(), Observer
{
    var fragmentView : View? = null

    @SuppressLint("RestrictedApi")
    override fun update(o: Observable?, arg: Any?)
    {
        BulbControlActivity.refFab3?.visibility = View.INVISIBLE
        BulbControlActivity.refFab2?.visibility = View.INVISIBLE
        BulbControlActivity.refFab?.visibility = View.INVISIBLE

        BrainWaveData.ParamToApply = -1
        BrainWaveData.IsOn = 0 // turn off the light
        BrainWaveData.BrainDataToUse = 0

        fragmentView?.infoText?.text = resources.getString(R.string.info_attention_empty)
        fragmentView?.switchBrightnessBulb?.isChecked = false
        fragmentView?.switchColorBulb?.isChecked = false
        fragmentView?.switchTurnBulb?.isChecked = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_attention_control, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        
        fragmentView = view

        BulbControlActivity.refFab2?.visibility = View.INVISIBLE
        view.infoText.text = resources.getString(R.string.info_attention_empty)

        view.attentionGraph?.addView(AttentionControlScene(context!!))
        GUIStateNotifier.addObserver(this)

        view.switchTurnBulb?.setOnClickListener { it ->
            BulbControlActivity.refFab3?.visibility = View.VISIBLE
            BulbControlActivity.refFab2?.visibility = View.VISIBLE
            BulbControlActivity.refFab?.visibility = View.VISIBLE

            if (switchTurnBulb.isChecked)
            {
                view.switchColorBulb?.isChecked = false
                view.switchBrightnessBulb?.isChecked = false
                BrainWaveData.IsOn = 1 // turn on the light
                BrainWaveData.ParamToApply = 0
                BrainWaveData.BrainDataToUse = 0
                view.infoText.text = resources.getString(R.string.info_attention_onoff)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }

        view.switchBrightnessBulb?.setOnClickListener { it->
            BulbControlActivity.refFab3?.visibility = View.INVISIBLE
            BulbControlActivity.refFab2?.visibility = View.VISIBLE
            BulbControlActivity.refFab?.visibility = View.VISIBLE

            if (switchBrightnessBulb.isChecked)
            {
                view.switchColorBulb?.isChecked = false
                view.switchTurnBulb?.isChecked = false
                BrainWaveData.IsOn = 1 // turn on the light
                BrainWaveData.ParamToApply = 1
                BrainWaveData.BrainDataToUse = 0
                view.infoText.text = resources.getString(R.string.info_attention_brightness)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }

        view.switchColorBulb?.setOnClickListener { it ->
            BulbControlActivity.refFab3?.visibility = View.INVISIBLE
            BulbControlActivity.refFab2?.visibility = View.VISIBLE
            BulbControlActivity.refFab?.visibility = View.VISIBLE

            if (switchColorBulb.isChecked)
            {
                view.switchTurnBulb?.isChecked = false
                view.switchBrightnessBulb?.isChecked = false
                BrainWaveData.IsOn = 1 // turn on the light
                BrainWaveData.ParamToApply = 2
                BrainWaveData.BrainDataToUse = 0
                view.infoText.text = resources.getString(R.string.info_attention_color)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }
    }
}