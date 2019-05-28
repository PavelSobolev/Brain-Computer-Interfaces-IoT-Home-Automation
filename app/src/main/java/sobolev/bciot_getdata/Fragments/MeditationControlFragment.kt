package sobolev.bciot_getdata.Fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_meditation_control.view.*
import sobolev.bciot_getdata.Activities.BulbControlActivity
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.Connectivity.GUIStateNotifier
import sobolev.bciot_getdata.Graphics.RelaxationControlScene
import sobolev.bciot_getdata.R
import java.util.*

// fragment shows user's brain relaxation
class MeditationControlFragment : Fragment(), Observer
{

    var fragmentView : View? = null

    // get notification to change state of GUI controls of the fragment
    @SuppressLint("RestrictedApi")
    override fun update(o: Observable?, arg: Any?)
    {
        fragmentView?.switchRelaxTurnBulb?.isChecked = false
        fragmentView?.switchRelaxColorBulb?.isChecked = false
        fragmentView?.switchRelaxBrightnessBulb?.isChecked = false
        fragmentView?.infoRelaxationText?.text = resources.getString(R.string.info_meditation_empty)

        BrainWaveData.IsOn = 0
        BrainWaveData.ParamToApply = -1
        BrainWaveData.BrainDataToUse = 1

        BulbControlActivity.refFab3?.visibility = View.INVISIBLE
        BulbControlActivity.refFab2?.visibility = View.INVISIBLE
        BulbControlActivity.refFab?.visibility = View.INVISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_meditation_control, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view

        view.relaxationGraph?.addView(RelaxationControlScene(context!!))
        GUIStateNotifier.addObserver(this)

        BulbControlActivity.refFab2?.visibility = View.INVISIBLE

        view.switchRelaxTurnBulb?.setOnClickListener { _ ->
            BulbControlActivity.refFab3?.visibility = View.VISIBLE
            BulbControlActivity.refFab2?.visibility = View.VISIBLE
            BulbControlActivity.refFab?.visibility = View.VISIBLE
            if (view.switchRelaxTurnBulb?.isChecked!!)
            {
                BrainWaveData.BrainDataToUse = 1
                BrainWaveData.ParamToApply = 0
                BrainWaveData.IsOn = 1
                view.switchRelaxBrightnessBulb?.isChecked = false
                view.switchRelaxColorBulb?.isChecked = false
                view.infoRelaxationText?.text = resources.getString(R.string.info_meditation_onoff)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
                //off()
            }
        }

        view.switchRelaxBrightnessBulb?.setOnClickListener {_->
            BulbControlActivity.refFab3?.visibility = View.INVISIBLE
            BulbControlActivity.refFab2?.visibility = View.VISIBLE
            BulbControlActivity.refFab?.visibility = View.VISIBLE

            if (view.switchRelaxBrightnessBulb?.isChecked!!)
            {
                BrainWaveData.BrainDataToUse = 1
                BrainWaveData.ParamToApply = 1
                BrainWaveData.IsOn = 1
                view.switchRelaxTurnBulb?.isChecked = false
                view.switchRelaxColorBulb?.isChecked = false
                view.infoRelaxationText?.text = resources.getString(R.string.info_meditation_brightness)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }

        view.switchRelaxColorBulb?.setOnClickListener { _ ->
            BulbControlActivity.refFab3?.visibility = View.INVISIBLE
            BulbControlActivity.refFab2?.visibility = View.VISIBLE
            BulbControlActivity.refFab?.visibility = View.VISIBLE

            if (view.switchRelaxColorBulb?.isChecked!!)
            {
                BrainWaveData.BrainDataToUse = 1
                BrainWaveData.ParamToApply = 2
                BrainWaveData.IsOn = 1
                view.switchRelaxTurnBulb?.isChecked = false
                view.switchRelaxBrightnessBulb?.isChecked = false
                view.infoRelaxationText?.text = resources.getString(R.string.info_meditation_color)
            }
            else
            {
                GUIStateNotifier.updateFragmentsGUI()
            }
        }
        BrainWaveData.BrainDataToUse = 1
    }
}
