package sobolev.bciot_getdata.Helpers
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import sobolev.bciot_getdata.Fragments.AttentionControlFragment
import sobolev.bciot_getdata.Fragments.MeditationControlFragment
import sobolev.bciot_getdata.Fragments.MixedControlFragment
class TabsAdaptor(val fm: FragmentManager?,
                  private val numOfTabs : Int) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment?
    {
        //BrainWaveData.BrainDataToUse = position

        return when (position) {
            0 -> AttentionControlFragment()
            1 -> MeditationControlFragment()
            2 -> MixedControlFragment()
            else -> null
        }

        return null
    }

    override fun getCount() : Int = numOfTabs
}