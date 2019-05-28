package sobolev.bciot_getdata.Graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import sobolev.bciot_getdata.Connectivity.BrainWaveData

// training of both attention and mediation
class MixedControlScene (var cntext : Context) : View(cntext)
{
    override fun onDraw(canvas : Canvas)
    {
        super.onDraw(canvas)
        var painter : Paint = Paint()

        var startXPoint : Float = 30.0f

        when(BrainWaveData.ParamToApply)
        {
            1 -> CommonPainter.drawMixedBalanceBrightnessScene(canvas, painter, startXPoint, width, height)
            2 -> CommonPainter.drawMixedBalanceColorScene(canvas, painter, startXPoint, width, height)
        }

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = super.onSizeChanged(w, h, oldw, oldh)
}