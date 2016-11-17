package be.schadron.visualizertestmqtt;

/**
 * Created by jenss on 16/11/2016.
 */

public class Band {
    public static final int FALL_SPEED = 1500;
    public static final double NOISE_LEVEL = 0.699999988079071d;
    public static final int PEAK_TIME = 30;
    private static double levelMax;
    private int level;
    private double levelAnalog;
    private int levelPeak;
    private int topTime;

    public Band() {
        this.levelAnalog = 0.0d;
        this.level = 0;
        this.levelPeak = 0;
        this.topTime = 0;
    }

    static {
        levelMax = 0.0d;
    }

    public void setLevel(int levelNew) {
        this.level = levelNew;
    }

    public void add(byte levelR, byte levelI) {
        double levelAnalogNew = Math.sqrt((double) ((levelR * levelR) + (levelI * levelI)));
        if (levelAnalogNew > this.levelAnalog) {
            this.levelAnalog = levelAnalogNew;
        }
    }

    public void calculate() {
        int levelNew = 0;
        if (this.levelAnalog > NOISE_LEVEL) {
            levelNew = (int) (20000.0d * Math.log10(this.levelAnalog - NOISE_LEVEL));
            if (levelNew < 0) {
                levelNew = 0;
            }
        }
        if (levelNew > this.level) {
            this.level = levelNew;
        } else if (this.level > 0) {
            if (this.level > FALL_SPEED) {
                this.level -= 1500;
            } else {
                this.level = 0;
            }
        }
        if (this.level > this.levelPeak) {
            this.levelPeak = this.level;
            this.topTime = PEAK_TIME;
        } else if (this.topTime > 0) {
            this.topTime--;
        } else if (this.levelPeak > FALL_SPEED) {
            this.levelPeak -= 1500;
        } else {
            this.levelPeak = 0;
        }
        if (this.levelAnalog > levelMax) {
            levelMax = this.levelAnalog;
        }
        this.levelAnalog = 0.0d;
    }

    public int getLevel() {
        return this.level;
    }

    public int getLevelPeak() {
        return this.levelPeak;
    }
}
