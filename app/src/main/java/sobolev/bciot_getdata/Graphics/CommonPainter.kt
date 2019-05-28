package sobolev.bciot_getdata.Graphics

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import sobolev.bciot_getdata.Connectivity.BrainWaveData
import kotlin.math.abs

// class for drawing of color rectangles for att, med and mixture of att and med
object CommonPainter
{
    // ------------  rendering controlled by attention level --------------------

    fun drawAttentionControlColorScene(canvas: Canvas, painter: Paint, from : Float, width: Int, height: Int)
    {
        if (BrainWaveData.IsOn==0) return
        if (BrainWaveData.LastAttentionData==0) return
        if (BrainWaveData.BrainDataToUse!=0)  return

        val fromY : Float = 50f
        val step : Float = (width - 2 * from) / (BrainWaveData.AttentionHueRange.first - BrainWaveData.AttentionHueRange.last + 1) // for calculation of shades of color
        var fromX : Float = from

        var ColorPoints = mutableListOf<Float>()

        // gradient rectangle
        for (hue in BrainWaveData.AttentionHueRange)
        {
            painter.color = Color.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 1f))
            canvas.drawRect(fromX, fromY, fromX + step, height - fromY, painter)
            ColorPoints.add((2 * fromX + step) / 2)
            fromX += step
        }

        triangle(canvas, painter,
                Color.BLACK, ColorPoints[BrainWaveData.LastAttentionData-1], fromY,
                20f,20f,true)
        triangle(canvas, painter,
                Color.BLACK, ColorPoints[BrainWaveData.LastAttentionData-1], height-fromY,
                20f,20f,false)
        canvas.drawLine(ColorPoints[BrainWaveData.LastAttentionData-1],fromY,
                ColorPoints[BrainWaveData.LastAttentionData-1],height-fromY,painter)
    }


    private fun triangle(canvas: Canvas, painter: Paint,
                         color : Int, pointX : Float, pointY:Float,
                         side: Float, height: Float, direction : Boolean)
    {
        painter.style = Paint.Style.FILL
        painter.color = color

        val s2 = side/2

        var path : Path = Path()

        if (direction) // down
        {
            path.reset()
            path.moveTo(pointX - s2, pointY - height)
            path.lineTo(pointX + s2, pointY - height)
            path.lineTo(pointX, pointY)
            path.lineTo(pointX - s2, pointY - height)
        }
        else
        {
            path.reset()
            path.moveTo(pointX - s2, pointY + height)
            path.lineTo(pointX + s2, pointY + height)
            path.lineTo(pointX, pointY)
            path.lineTo(pointX - s2, pointY + height)
        }
        canvas.drawPath(path,painter)
    }

    fun drawAttentionControlBrightnessScene(canvas: Canvas, painter: Paint, from : Float, width: Int, height: Int)
    {
        if (BrainWaveData.IsOn==0) return
        if (BrainWaveData.BrainDataToUse!=0) return

        val fromY : Float = 10f
        val span : Float = 10f
        val rectWidth : Float = (width - 2*from) / (100f + span)
        var rectHeight : Float = 10f //(height - 2*fromY) / 100f
        var fromX : Float = from

        for (level in 1..100)
        {
            if (level <= BrainWaveData.LastAttentionData)
                painter.color = Color.rgb((level*2.55).toInt(),0,0)
            else
                painter.color = Color.GRAY

            canvas.drawRect(fromX, height - fromY - rectHeight,
                        fromX + rectWidth, height-fromY, painter)
            fromX += rectWidth + 1
            rectHeight += 2
        }
    }

    fun drawAttentionControlOnOffScene (canvas: Canvas, painter: Paint, from : Float, width: Int, height: Int) {
        if (BrainWaveData.IsOn == 0) return
        if (BrainWaveData.BrainDataToUse != 0) return

        val fromY: Float = 10.0f
        val step: Float = (width - 2 * from) / 11.0f
        var fromX: Float = from


        var startYPoint: Float = height / 3f
        var rectHeight: Float = height / 1.5f

        var value: Int = BrainWaveData.LastAttentionData

        // rectangle for current used value of att, med or their combination
        var rectWidth: Float = (from + (value / 10f) * ((width - 2 * from) / 11f))
        painter.color = Color.rgb(0xc7, 0x28, 0x00)
        canvas.drawRect(from, startYPoint, rectWidth, rectHeight, painter)

        // base vertical line
        painter.color = Color.rgb(0xc7, 0x28, 0x00)
        painter.strokeWidth = 10f
        canvas.drawLine(from, fromY, from, height - fromY, painter) // rect of att value

        // // lines for off and on values with markers ----------------------
        val off: Float = BrainWaveData.OffThreshold.toFloat()
        val offX: Float = (from + (off / 10f) * ((width - 2 * from) / 11f))
        val on: Float = BrainWaveData.OnThreshold.toFloat()
        val onX: Float = (from + (on / 10f) * ((width - 2 * from) / 11f))
        val showRectHeight: Int = 35
        val showRectShift: Int = 10
        val showRectWidth: Int = 110

        painter.strokeWidth = 5f
        painter.textSize = 30f

        painter.color = Color.rgb(0, 200, 0)
        canvas.drawLine(offX, fromY, offX, height - fromY, painter)
        canvas.drawRect(offX, fromY + showRectShift, offX + showRectWidth,
                fromY + showRectShift + showRectHeight, painter)
        painter.color = Color.WHITE
        canvas.drawText("Off=${BrainWaveData.OffThreshold}",
                offX + 8, fromY + showRectShift + showRectHeight / 2 + 10, painter)

        painter.color = Color.rgb(0, 0, 230)
        canvas.drawLine(onX, fromY, onX, height - fromY, painter)
        canvas.drawRect(onX, fromY + showRectShift,
                onX + showRectWidth, fromY + showRectShift + showRectHeight, painter)
        painter.color = Color.WHITE
        canvas.drawText("On=${BrainWaveData.OnThreshold}", onX + 8, fromY + showRectShift + showRectHeight / 2 + 10, painter)

        // -------- lines for off and on values with markers ------------

        painter.style = Paint.Style.STROKE
        painter.strokeWidth = 1f
        painter.textSize = 30f

        fromX += step
        for (x in 10..100 step 10) {
            painter.color = Color.rgb(100, 100, 100)
            canvas.drawLine(fromX, fromY, fromX, height - fromY, painter)

            painter.color = Color.BLACK
            canvas.drawText("$x", fromX + 5, height - fromY - 10, painter)
            fromX += step
        }
    }


    // ------------  rendering controlled by relaxation level --------------------

    fun drawMeditationColorBrightnessScene(canvas: Canvas, painter: Paint, margin : Float, width: Int, height: Int) {
        if (BrainWaveData.IsOn==0) return
        if (BrainWaveData.BrainDataToUse!=1) return

        //BrainWaveData.LightBrightness = 100-BrainWaveData.LastMeditationData

        val fromY : Float = 10f
        val span : Float = 10f
        val rectWidth : Float = (width - 2 * margin) / (100f + span)
        var rectHeight : Float = (height - 2 * margin) / 100f
        var fromX : Float = margin

        for (level in 100 downTo 1)
        {
            if (level >= 100-BrainWaveData.LastMeditationData)
                painter.color = Color.rgb(0,(level*2.55).toInt(),0)
            else
                painter.color = Color.LTGRAY

            canvas.drawRect(fromX, height - fromY - rectHeight,
                    fromX + rectWidth, height-fromY, painter)
            fromX += rectWidth + 1
            rectHeight += 2
        }

    }

    fun drawMeditationControlOnOffScene (canvas: Canvas, painter: Paint, margin : Float, width: Int, height: Int)  {
        if (BrainWaveData.IsOn == 0) return
        if (BrainWaveData.BrainDataToUse != 1) return

        //val margin : Float = 10f
        val scale  : Float = (width - 2 * margin) / 10 // unit along x-axis

        // off rectangle
        painter.color = Color.rgb(150,150,150)
        painter.style = Paint.Style.FILL
        canvas.drawRect(margin, margin,
                (BrainWaveData.OffThreshold.toFloat() / 10f) * scale + margin,
                height - margin, painter)


        // on rectangle
        painter.color = Color.rgb(220,220,220)
        painter.style = Paint.Style.FILL
        canvas.drawRect((BrainWaveData.OnThreshold.toFloat() /10) * scale + margin, margin,
                width-margin, height - margin, painter)

        // vertical lines with numbers from 0 to 100
        painter.textSize = 30f
        for(x in 0..10)
        {
            var xcoord = x*scale + margin
            painter.color = Color.GRAY
            canvas.drawLine(xcoord,margin,xcoord,height-margin,painter)
            painter.color = Color.BLACK
            canvas.drawText((x * 10).toString(), xcoord + 5,(height - 2* margin)/2, painter)
        }

        // frame
        painter.color = Color.BLACK
        painter.style = Paint.Style.STROKE
        painter.strokeWidth = 5f
        canvas.drawRect(margin, margin, width-margin,height-margin, painter)

        triangle(canvas, painter, Color.BLUE,
                (BrainWaveData.LastMeditationData.toFloat()/10)*scale+margin, margin,
                30f,60f,false)
    }

    fun drawMeditationControlColorScene(canvas: Canvas, painter: Paint, margin : Float, width: Int, height: Int) {
        if (BrainWaveData.IsOn==0) return
        if (BrainWaveData.BrainDataToUse!=1) return
        if (BrainWaveData.LastMeditationData==0) return

        val fromY : Float = 50f
        val step : Float = (width - 2 * margin) / (BrainWaveData.MeditationHueRange.last - BrainWaveData.MeditationHueRange.first + 1) // for calculation of shades of color
        var fromX : Float = margin

        var ColorPoints = mutableListOf<Float>()

        // gradient rectangle
        for (hue in BrainWaveData.MeditationHueRange)
        {
            painter.color = Color.HSVToColor(floatArrayOf(hue.toFloat(), 1f, 1f))
            canvas.drawRect(fromX, fromY, fromX + step, height - fromY, painter)
            ColorPoints.add((2 * fromX + step) / 2)
            fromX += step
        }

        triangle(canvas, painter,
                Color.BLACK, ColorPoints[BrainWaveData.LastMeditationData*2-1], fromY,
                20f,20f,true)
        triangle(canvas, painter,
                Color.BLACK, ColorPoints[BrainWaveData.LastMeditationData*2-1], height-fromY,
                20f,20f,false)
        canvas.drawLine(ColorPoints[BrainWaveData.LastMeditationData*2-1],fromY,
                ColorPoints[BrainWaveData.LastMeditationData*2-1],height-fromY,painter)
    }

    // ----------- mixed mode graphics -----------------------------------------------------------

    fun drawMixedBalanceBrightnessScene(canvas: Canvas, painter: Paint, margin : Float, width: Int, height: Int)
    {
        if (BrainWaveData.IsOn == 0) return
        if (BrainWaveData.BrainDataToUse != 2) return
        if (BrainWaveData.LastMeditationData == 0) return
        if (BrainWaveData.LastAttentionData == 0) return


        var shift : Float = 15.0f // shift from margin along Y-axis
        var textWidth : Float = 0.0f
        val scale  : Float = (width - 2 * margin) / 10 // unit along x-axis


        painter.color = Color.rgb((BrainWaveData.LastAttentionData*2.5).toInt(),0,0)
        canvas.drawRect(margin, margin + shift + 100,
                (BrainWaveData.LastAttentionData.toFloat()/10)*scale+margin,
                margin + shift + 180, painter)

        painter.color = Color.rgb((BrainWaveData.LastMeditationData*2.5).toInt(),0,(BrainWaveData.LastMeditationData*2.5).toInt())
        canvas.drawRect((((100-BrainWaveData.LastMeditationData).toFloat()/10)*scale+margin),
                margin + shift + 300,
                width - margin,
                margin + shift + 300 + 80, painter)


        var lenOverlay : Int = BrainWaveData.LastAttentionData - (100-BrainWaveData.LastMeditationData)

        if (lenOverlay>0)
        {
            painter.strokeWidth = 1f
            painter.color = Color.rgb(200,200,200)
            canvas.drawRect(
                    (((100-BrainWaveData.LastMeditationData).toFloat()/10)*scale+margin),
                    margin + shift + 200,
                    (BrainWaveData.LastAttentionData.toFloat()/10)*scale+margin,
                    margin + shift + 200 + 80, painter)


            painter.strokeWidth = 3f
            painter.color = Color.rgb((BrainWaveData.LastMeditationData*2.5).toInt(),0,(BrainWaveData.LastMeditationData*2.5).toInt())
            canvas.drawLine(
                    (((100-BrainWaveData.LastMeditationData).toFloat()/10)*scale+margin),
                    margin + shift + 300 + 80,
                    (((100-BrainWaveData.LastMeditationData).toFloat()/10)*scale+margin),
                    margin + shift + 200, painter)
            painter.color = Color.rgb((BrainWaveData.LastAttentionData*2.5).toInt(),0,0)
            canvas.drawLine(
                    (BrainWaveData.LastAttentionData.toFloat()/10)*scale+margin,
                    margin + shift + 100,
                    (BrainWaveData.LastAttentionData.toFloat()/10)*scale+margin,
                    margin + shift + 200 + 80, painter)
            painter.strokeWidth = 1f
        }




        painter.color = Color.RED // att line and text
        painter.style = Paint.Style.STROKE
        canvas.drawLine(margin, margin + shift, width - margin, margin + shift, painter)
        painter.style = Paint.Style.FILL
        painter.textSize = 34f
        canvas.drawText("Attention level >>>>>",margin, margin, painter)


        // vertical lines
        // vertical lines with numbers from 0 to 100
        painter.textSize = 24f
        for(x in 0..10)
        {
            var xcoord = x*scale + margin
            painter.color = Color.GRAY
            canvas.drawLine(xcoord,margin + shift + 3, xcoord, height - margin - shift - 3,painter)

            if(x < 10)
            {
                painter.color = Color.RED
                canvas.drawText((x * 10).toString(), xcoord + 5, margin + shift + 27, painter)

                painter.color = Color.MAGENTA
                var rightShift = if (x > 0) 15 else 0
                canvas.drawText((x * 10).toString(), width - (xcoord + 5) - shift - rightShift, height - margin - shift - 10, painter)
            }

        }

        painter.color = Color.MAGENTA // med line and text
        painter.style = Paint.Style.STROKE
        canvas.drawLine(margin, height - margin - shift,
                width - margin, height - margin - shift, painter)
        painter.textSize = 34f
        painter.style = Paint.Style.FILL
        val relaxText = "<<<<< Relaxation level"
        textWidth = painter.measureText(relaxText)
        canvas.drawText(relaxText, width - margin - textWidth, height - margin + shift + 10, painter)

    }

    fun drawMixedBalanceColorScene(canvas: Canvas, painter: Paint, margin : Float, width: Int, height: Int)
    {

        if (BrainWaveData.IsOn==0) return
        if (BrainWaveData.BrainDataToUse!=2) return
        if (BrainWaveData.LastMeditationData==0) return
        if (BrainWaveData.LastAttentionData==0) return

        val maxWidth : Float = width - margin * 2
        val maxHeight : Float = height - margin * 2

        val minorShift : Float = 10f

        val cX : Float = (margin + maxWidth) / 2.0f
        val cY : Float = (margin + maxHeight) / 2.0f

        var diff : Float = (BrainWaveData.LastAttentionData - BrainWaveData.LastMeditationData).toFloat()


        var widthAttMeddRectanle = maxWidth * ((BrainWaveData.MAX_DATA_VALUE - BrainWaveData.BALANCE_VALUE)/200.0f)
        var balanceWidth = maxWidth * (BrainWaveData.BALANCE_VALUE / 100.0f)

        painter.style = Paint.Style.FILL

        // color of att.'s rectangle
        painter.color = if (diff > 0 && abs(diff) > BrainWaveData.BALANCE_VALUE)
            Color.HSVToColor(floatArrayOf(BrainWaveData.HUE_ATTENTION_COLOR.toFloat(),1f,1f)) else Color.GRAY
        canvas.drawRect(margin, margin, margin + widthAttMeddRectanle, height - margin, painter)

        // rectangle of "balanced state"
        painter.color = if (abs(diff) <= BrainWaveData.BALANCE_VALUE)
            Color.HSVToColor(floatArrayOf(BrainWaveData.HUE_NEUTRAL_COLOR.toFloat(),1f,1f)) else Color.LTGRAY
        canvas.drawRect(margin + widthAttMeddRectanle, margin,
                margin + widthAttMeddRectanle + balanceWidth, height - margin, painter)

        // med. rect.
        painter.color = if (diff < 0 && abs(diff) > BrainWaveData.BALANCE_VALUE)
            Color.HSVToColor(floatArrayOf(BrainWaveData.HUE_MEDITATION_COLOR.toFloat(),1f,1f)) else Color.DKGRAY
        canvas.drawRect(
                margin + widthAttMeddRectanle + balanceWidth, margin,
                margin + maxWidth, height - margin, painter)

        painter.textSize = 50f
        when {
            diff > 0 && abs(diff) > BrainWaveData.BALANCE_VALUE -> {
                val outTextLen : Float = painter.measureText(BrainWaveData.FOCUSED)
                val x = (margin * 2 + widthAttMeddRectanle - outTextLen) / 2
                painter.color = Color.WHITE
                canvas.drawText("${BrainWaveData.FOCUSED}", x, cY, painter)
            }

            abs(diff) <= BrainWaveData.BALANCE_VALUE -> {
                val outTextLen : Float = painter.measureText(BrainWaveData.BALANCED)
                val x = (margin * 2 + maxWidth - outTextLen) / 2
                painter.color = Color.BLUE
                canvas.drawText("${BrainWaveData.BALANCED}", x, cY, painter)
            }

            diff < 0 && abs(diff) > BrainWaveData.BALANCE_VALUE -> {
                val outTextLen : Float = painter.measureText(BrainWaveData.RELAXED)
                val x = margin + widthAttMeddRectanle + balanceWidth + widthAttMeddRectanle / 2 - outTextLen / 2
                painter.color = Color.YELLOW
                canvas.drawText("${BrainWaveData.RELAXED}", x, cY, painter)
            }
        }
    }
}