package sobolev.bciot_getdata.Graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.view.View
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import sobolev.bciot_getdata.R

// training of mediation level
class MeditationPromotingScene (var cntext : Context) : View(cntext)
{
    private var currentPos : Int = 130

    private var hgt : Int = 0 // height
    private var wdth : Int = 0 // width
    private var maxSize : Int = 2

    private var up : Boolean = true
    var player : MediaPlayer? = MediaPlayer.create(cntext, R.raw.sea_0)


    override fun onDraw(canvas : Canvas)
    {
        super.onDraw(canvas)

        var painter = Paint()
        drawMovingObjectInTheMaze(canvas, painter)

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
    {
        super.onSizeChanged(w, h, oldw, oldh)
        wdth = w
        hgt = h
    }

    //var spn : Int = 0
    fun drawMovingObjectInTheMaze(canvas: Canvas, painter: Paint)
    {
        if (up)
        {
            if (currentPos < hgt-maxSize*100)
                currentPos++
            else
            {
                up = false
            }
        }
        else
        {
            if (currentPos > maxSize*100)
                currentPos--
            else
            {
                up = true
            }
        }

        if (BrainWaveData.LastMeditationData>=50) // if level of med. is higher than 50, make sound of player proportional to its's value
        {
            var vol: Float = BrainWaveData.LastMeditationData / 100f
            player?.setVolume(vol, vol)
        }
        else
        {
            player?.setVolume(0.1f,0.1f)
        }

        // circle reflects level of mind relaxation - the higher the level the more violet and larger the circle

        painter.color = Color.rgb((BrainWaveData.LastMeditationData*2.5).toInt(),0,
                (BrainWaveData.LastMeditationData*2.5).toInt())
        painter.style = Paint.Style.FILL
        canvas.drawCircle((wdth/2).toFloat(), currentPos.toFloat(),
                BrainWaveData.LastMeditationData.toFloat()*maxSize, painter)

        painter.style = Paint.Style.STROKE

        painter.color = Color.MAGENTA // max level of concentration
        canvas.drawCircle((wdth/2).toFloat(), currentPos.toFloat(),
                100f*maxSize, painter)


        if (BrainWaveData.ParamToApply == 0)
        {
            painter.color = Color.GREEN // level of turning off the bulb
            canvas.drawCircle((wdth / 2).toFloat(), currentPos.toFloat(),
                    BrainWaveData.OffThreshold * maxSize.toFloat(), painter)

            painter.color = Color.BLUE // level of turning on the bulb
            canvas.drawCircle((wdth / 2).toFloat(), currentPos.toFloat(),
                    BrainWaveData.OnThreshold * maxSize.toFloat(), painter)
        }

        if (BrainWaveData.ParamToApply == 2)
        {
            // gradient color from green to red
            val shift : Float = 118.5F
            painter.style = Paint.Style.FILL
            painter.color = Color.HSVToColor(floatArrayOf((BrainWaveData.LastMeditationData*2+shift).toFloat(),1f,1f))
            canvas.drawCircle((wdth / 2).toFloat(), currentPos.toFloat(),
                    BrainWaveData.LastMeditationData * maxSize.toFloat(), painter)
        }
    }
}