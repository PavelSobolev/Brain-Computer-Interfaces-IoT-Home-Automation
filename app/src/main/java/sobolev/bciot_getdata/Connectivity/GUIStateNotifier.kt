package sobolev.bciot_getdata.Connectivity

import java.util.*

// class notifies GUI (fragments) about changes in state of user's brain activity
object GUIStateNotifier : Observable()
{
    fun updateFragmentsGUI()
    {
        setChanged()
        notifyObservers()
    }
}