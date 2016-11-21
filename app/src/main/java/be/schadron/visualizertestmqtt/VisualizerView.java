package be.schadron.visualizertestmqtt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class gives a visual representation of the FFT data
 */

public class VisualizerView extends View implements View.OnLongClickListener, FFTListener {
    private static final int[][] COLORS;
    private static final int FFT_CHANNELS = 32;
    private static final int LEDS_COUNT = 16;
    private static final int LED_LEVEL = 2;
    private static final int LED_PEAK = 4;
    private static final int MAX_MDB = 50000;
    private static int theme;
    private Band[] bands;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
        setKeepScreenOn(true);
    }

    static {
        COLORS = new int[][]{
                new int[]{-16777216, -16777216, -16737844, -16737844, -13388315, -13388315},
                new int[]{-16777216, -16777216, -10053376, -10053376, -6697984, -6697984},
                new int[]{-16777216, -16777216, -30720, -30720, -17613, -17613}
        };
        theme = 0;
    }

    private int getColor(int type) {
        return COLORS[theme][type];
    }

    public boolean onLongClick(View v) {
        theme++;
        if (theme >= COLORS.length) {
            theme = 0;
        }
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawColor(Color.WHITE);
        if (this.bands == null)
            return;
        Paint paintLevel = new Paint();
        paintLevel.setColor(getColor(LED_LEVEL));
        Paint paintPeak = new Paint();
        paintPeak.setColor(getColor(LED_PEAK));
        int width = getWidth();
        int height = getHeight();
        int ledWidth = width / FFT_CHANNELS;
        int ledHeight = height / LEDS_COUNT;
        int ledsCount = height / ledHeight;
        for (int i = 0; i < FFT_CHANNELS; i++) {
            float x1 = (float) (i * ledWidth);
            float x2 = (((float) ledWidth) + x1) - 1.0f;
            int level = (this.bands[i].getLevel() * ledsCount) / MAX_MDB;
            int levelPeak = (this.bands[i].getLevelPeak() * ledsCount) / MAX_MDB;
            for (int j = 0; j <= levelPeak; j++) {
                float y1 = (float) ((ledsCount - j) * ledHeight);
                float y2 = y1 - ((float) (ledHeight - 1));
                if (j <= level) {
                    canvas.drawRect(x1, y2, x2, y1, paintLevel);
                }
                if (j == levelPeak) {
                    canvas.drawRect(x1, y2, x2, y1, paintPeak);
                }
            }
        }
    }

    @Override
    public void onCalculationFinished(Band[] bands) {
        this.bands = bands;
        invalidate();
    }
}
