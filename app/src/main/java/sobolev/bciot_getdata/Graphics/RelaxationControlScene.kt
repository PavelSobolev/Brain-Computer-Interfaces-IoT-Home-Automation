package sobolev.bciot_getdata.Graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import sobolev.bciot_getdata.Connectivity.BrainWaveData

class RelaxationControlScene (var cntext : Context) : View(cntext)
{
    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        var painter : Paint = Paint()

        var startXPoint = 5f

        when(BrainWaveData.ParamToApply)
        {
            0 -> CommonPainter.drawMeditationControlOnOffScene(canvas,painter,startXPoint,width,height)
            1 -> CommonPainter.drawMeditationColorBrightnessScene(canvas,painter,startXPoint,width,height)
            2 -> CommonPainter.drawMeditationControlColorScene(canvas,painter,startXPoint,width,height)
        }

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = super.onSizeChanged(w, h, oldw, oldh)
}