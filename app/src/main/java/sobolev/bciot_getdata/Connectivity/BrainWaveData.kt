package sobolev.bciot_getdata.Connectivity


import android.util.Log
import java.time.LocalDateTime
import com.google.firebase.database.*
import sobolev.bciot_getdata.Helpers.AppState
import java.util.Observable

// processing and storing of data of user's brain activity
object BrainWaveData : Observable() {
    private var currentState: AppState = AppState.CONNECTION
    private var isErrorState: Boolean = false
    private var offThreshold: Int = 40
    private var onThreshold: Int = 60
    private var paramToApply: Int = -1
    private var brainDataToUse: Int = 0
    private var oldHue: Int = -1
    private var oldNewHueGap: Int = 8
    private var isOn = 0
    private var meditation: Int = 0
    private var attention: Int = 0

    var AttentionColors = mutableListOf<Int>()
    var MeditationColors = mutableListOf<Int>()
    var AttentionHueRange = 99 downTo 0
    var MeditationHueRange = 120..320

    // reference to Google Online Realtime Database (Firebase NoSQL database)
    var bciMindWaveDBReference: DatabaseReference

    // reference to NeuroSky MindWave Headset
    var connector: BluetoothHeadsetConnector? = null

    init {
        bciMindWaveDBReference = FirebaseDatabase.getInstance().getReference() //"mindwavebci"

        bciMindWaveDBReference.addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        onThreshold = dataSnapshot.child("onThreshold").value.toString().toInt()
                        offThreshold = dataSnapshot.child("offThreshold").value.toString().toInt()
                        Log.d("reader==", dataSnapshot.child("offThreshold").value.toString())
                    }

                    override fun onCancelled(p0: DatabaseError) {/*not used*/
                    }
                })
        for (hue in AttentionHueRange) AttentionColors.add(hue)
        for (hue in MeditationHueRange) MeditationColors.add(hue)
    }

    var IsErrorState: Boolean
        get() = isErrorState
        set(value) {
            isErrorState = value
        }

    var OffThreshold: Int
        get() = offThreshold
        set(value) {
            if (value in 10..40) {
                bciMindWaveDBReference.child("offThreshold").setValue(value)
                offThreshold = value
            }
        }

    var OnThreshold: Int
        get() = onThreshold
        set(value) {
            if (value in 50..90) {
                bciMindWaveDBReference.child("onThreshold").setValue(value)
                onThreshold = value
            }
        }

    fun AttentionAndMeditationToString(): String {

        val att: String = if (LastAttentionData < 10)
            "0$LastAttentionData" else "$LastAttentionData"

        val med: String = if (LastMeditationData < 10)
            "0$LastMeditationData" else "$LastMeditationData"

        return "Brain data: attention=${att} | relaxation=${med}"
    }

    val HUE_ATTENTION_COLOR = 0
    val HUE_MEDITATION_COLOR = 320
    val HUE_NEUTRAL_COLOR = 45
    val BALANCE_VALUE = 30
    val MAX_DATA_VALUE = 100
    val OVERLAP_VALUE = 20
    val FOCUSED = "Focused"
    val RELAXED = "Relaxed"
    val BALANCED = "Balanced"

    private fun UpdateBulbData() {
        //BrainWaveData.Hue = Colors[BrainWaveData.LastAttentionData - 1]

        when (BrainDataToUse)
        {
            0 ->  // get data from attention
                when (ParamToApply) {
                    1 -> LightBrightness = LastAttentionData  // brightness
                    2 -> Hue = AttentionColors[LastAttentionData - 1]  // hue
                }
            1 -> // get data from meditation
                when (ParamToApply) {
                    1 -> LightBrightness = MAX_DATA_VALUE - LastMeditationData // brightness
                    2 -> Hue = MeditationColors[LastMeditationData * 2 - 1]  // hue
                }
            2 -> // get data from combination of att and med
                when (ParamToApply) {
                    1 -> {  // brightness
                        if (LastAttentionData > MAX_DATA_VALUE - LastMeditationData) {
                            var len = (LastAttentionData - (MAX_DATA_VALUE - LastMeditationData))
                            LightBrightness = len + if (len <= 100 - OVERLAP_VALUE) OVERLAP_VALUE else len - (100 - OVERLAP_VALUE)
                        } else
                            LightBrightness = OVERLAP_VALUE
                    }
                    2 -> {
                        if (kotlin.math.abs(LastAttentionData - LastMeditationData) <= BALANCE_VALUE) {
                            Hue = HUE_NEUTRAL_COLOR
                        }
                        else {
                            if (LastAttentionData > LastMeditationData)
                                Hue = HUE_ATTENTION_COLOR
                            else
                                Hue = HUE_MEDITATION_COLOR
                        }
                    }
                }
        }
    }

    var CurrentAppState: AppState
        get() = currentState
        set(value) {
            currentState = value
        }

    private var Hue: Int = 0  // Color bulb hue (from 0 to 360)
        set(value) {
            if (value in 0..360) {
                bciMindWaveDBReference.child("Hue").setValue(value)
                LastModified = LocalDateTime.now()
            }
        }

    var IsOn: Int // Is bulbs should be on (0) or off (1)
        get() = isOn
        set(value) {
            if (value in 0..1) {
                bciMindWaveDBReference.child("IsOn").setValue(value)
                LastModified = LocalDateTime.now()
                isOn = value
            }
        }

    var IsWhite: Int = 0 // Is only white component of color of the bulb should be changed
        set(value) {
            if (value in 0..1) {
                bciMindWaveDBReference.child("IsWhite").setValue(value)
                LastModified = LocalDateTime.now()
            }
        }

    var LastAttentionData: Int // Level of mind activity received form headset
        get() = attention
        set(value) {
            if (value in 0..100) {
                attention = value
                bciMindWaveDBReference.child("LastAttentionData").setValue(value)
                LastModified = LocalDateTime.now()
                UpdateBulbData()
                setChanged()
                notifyObservers()
            }
        }

    var LastMeditationData: Int  // Level of mind relaxation received form headset
        get() = meditation
        set(value) {
            if (value in 0..100) {
                meditation = value
                bciMindWaveDBReference.child("LastMeditationData").setValue(value)
                LastModified = LocalDateTime.now()
                UpdateBulbData()
                setChanged()
                notifyObservers()
            }
        }

    private var LastModified: LocalDateTime = LocalDateTime.now() // date and time of last changes
        set(value) {
            bciMindWaveDBReference.child("LastModified").setValue(value)
        }

    private var LightBrightness: Int = 100 // level of luminosity of the bulb
        set(value) {
            if (value in 0..100) {
                val outValue = when (value) {
                    in 0..20 -> 10
                    in 21..40 -> 31
                    in 41..60 -> 51
                    in 61..80 -> 71
                    else -> 90
                }

                bciMindWaveDBReference.child("LightBrightness").setValue(outValue)
                LastModified = LocalDateTime.now()
            }
        }

    var LightLevel: Int = 50
        set(value) {
            if (value in 0..100) {
                bciMindWaveDBReference.child("LightLevel").setValue(value)
                LastModified = LocalDateTime.now()
            }
        }

    val ParamDescription: String
        get() = "0=On-Off, 1-level, 2-Hue and brightness, 3-whiteness"

    var ParamToApply: Int
        get() = paramToApply
        set(value) {
            if (value in -1..3) {
                bciMindWaveDBReference.child("ParamToApply").setValue(value)
                LastModified = LocalDateTime.now()
                paramToApply = value
            }
        }

    var BrainDataToUse: Int // 0 - Attention, 1 - Meditaion, 2 - Mix of attention and meditation
        get() = brainDataToUse
        set(value) {
            if (value in 0..3) {
                bciMindWaveDBReference.child("BrainDataToUse").setValue(value)
                brainDataToUse = value
            }
        }

    var State: Int = 1
        set(value) {
            if (value in 0..1) {
                bciMindWaveDBReference.child("State").setValue(value)
                LastModified = LocalDateTime.now()
            }
        }

    var WaveDelta: Double = 0.0
        set(value) {
            /*if (value >= 0)
            {
                bciMindWaveDBReference.child("WaveDelta").setValue(value)
                LastModified = LocalDateTime.now()
            }*/
        }

    var WaveTheta: Double = 0.0
        set(value) {
            /*if (value >= 0)
            {
                bciMindWaveDBReference.child("WaveTheta").setValue(value)
                LastModified = LocalDateTime.now()
            }*/
        }

    var WaveHighAlpha: Double = 0.0
        set(value) {
            /*if (value >= 0)
            {

                bciMindWaveDBReference.child("WaveHighAlpha").setValue(value)
                LastModified = LocalDateTime.now()
            }*/
        }

    var WaveHighBeta: Double = 0.0
        set(value) {
            /*if (value >= 0)
            {

                bciMindWaveDBReference.child("WaveHighBeta").setValue(value)
                LastModified = LocalDateTime.now()
            }*/
        }

    var WaveHighGamma: Double = 0.0
        set(value) {
            /*if (value >= 0)
            {

                bciMindWaveDBReference.child("WaveHighGamma").setValue(value)
                LastModified = LocalDateTime.now()
            }*/
        }

    var WaveLowAlpha: Double = 0.0
        set(value) {
            //bciMindWaveDBReference.child("WaveLowAlpha").setValue(value)
            //LastModified = LocalDateTime.now()
        }

    var WaveLowBeta: Double = 0.0
        set(value) {
            //bciMindWaveDBReference.child("WaveLowBeta").setValue(value)
            //LastModified = LocalDateTime.now()
        }

    var WaveLowGamma: Double = 0.0
        set(value) {
            //bciMindWaveDBReference.child("WaveLowGamma").setValue(value)
            //LastModified = LocalDateTime.now()
        }
}