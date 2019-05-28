package sobolev.bciot_getdata.Fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_mixed_control.view.*
import sobolev.bciot_getdata.Activities.BulbControlActivity
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.Connectivity.GUIStateNotifier
import sobolev.bciot_getdata.Graphics.MixedControlScene
import sobolev.bciot_getdata.R
import java.util.*

// fragment mixes data about attention and relaxation
class MixedControlFragment : Fragment(), Observer
{

    private var fragmentView : View? = null

    // get notification to change state of GUI controls of the fragment
    @SuppressLint("RestrictedApi")
    override fun update(o: Observable?, arg: Any?)
    {
        fragmentView?.switchMixColor?.isChecked = false
        fragmentView?.switchMixBrightness?.isChecked = false
        fragmentView?.infoMixText?.text = resources.getString(R.string.info_mix_empty)

        BrainWaveData.IsOn = 0
        BrainWaveData.ParamToApply = -1
        BrainWaveData.BrainDataToUse = 2

        BulbControlActivity.refFab2?.visibility = View.INVISIBLE
        BulbControlActivity.refFab?.visibility = View.INVISIBLE
        BulbControlActivity.refFab3?.visibility = View.INVISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_mixed_control, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        fragmentView = view

        GUIStateNotifier.addObserver(this)

        // hide control buttons for settings and att/med promotion
        BulbControlActivity.refFab3?.visibility = View.INVISIBLE
        BulbControlActivity.refFab2?.visibility = View.INVISIBLE

        view.mixedGraph?.addView(MixedControlScene(context!!))

        view.switchMixBrightness?.setOnClickListener { _ ->
            BulbControlActivity.refFab3?.visibility = View.INVISIBLE
            BulbControlActivity.refFab2?.visibility = View.INVISIBLE
            BulbControlActivity.refFab?.visibility = View.INVISIBLE

            if (view.switchMixBrightness?.isChecked!!)
            {
                BrainWaveData.BrainDataToUse = 2
                BrainWaveData.ParamToApply = 1
                BrainWaveData.IsOn = 1
                BrainWaveData.BrainDataToUse = 2
                view.switchMixColor?.isChecked = false
                view.infoMixText?.text = resources.getString(R.string.info_mix_brightness)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }

        view.switchMixColor?.setOnClickListener { _ ->
            BulbControlActivity.refFab3?.visibility = View.INVISIBLE
            BulbControlActivity.refFab2?.visibility = View.INVISIBLE
            BulbControlActivity.refFab?.visibility = View.INVISIBLE

            if (view.switchMixColor?.isChecked!!)
            {
                BrainWaveData.BrainDataToUse = 2
                BrainWaveData.ParamToApply = 2
                BrainWaveData.IsOn = 1
                BrainWaveData.BrainDataToUse = 2
                view.switchMixBrightness?.isChecked = false
                view.infoMixText?.text = resources.getString(R.string.info_mix_color)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }
    }
}
