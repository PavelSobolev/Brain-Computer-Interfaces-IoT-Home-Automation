package sobolev.bciot_getdata.Graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import sobolev.bciot_getdata.Connectivity.BrainWaveData

// control of external device by level of attention
class AttentionControlScene (var cntext : Context) : View(cntext)
{
    override fun onDraw(canvas : Canvas)
    {
        super.onDraw(canvas)
        var painter : Paint = Paint()

        var startXPoint : Float = 10.0f

        when(BrainWaveData.ParamToApply)
        {
            0 -> CommonPainter.drawAttentionControlOnOffScene(canvas, painter, startXPoint, width, height)
            1 -> CommonPainter.drawAttentionControlBrightnessScene(canvas, painter, startXPoint, width, height)
            2 -> CommonPainter.drawAttentionControlColorScene(canvas, painter, startXPoint, width, height)
        }

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = super.onSizeChanged(w, h, oldw, oldh)
}