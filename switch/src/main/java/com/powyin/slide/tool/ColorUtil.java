package com.powyin.slide.tool;

/**
 * Created by powyin on 2016/8/8.
 */
public class ColorUtil {

    /**
     * @param startColor   起始颜色
     * @param endColor     末尾颜色
     * @param radio        过度值 -1 ~ +1
     * @return             混合颜色
     */
    public static int calculationColor(int startColor, int endColor, float radio) {
        radio = Math.max(0, radio);
        radio = Math.min(1, radio);

        int sA = startColor >>> 24;
        int eA = endColor >>> 24;
        int rA = (int) (sA + (eA - sA) * radio);

        int sR = startColor << 8 >>> 24;
        int eR = endColor << 8 >>> 24;
        int rR = (int) (sR + (eR - sR) * radio);

        int sG = startColor << 16 >>> 24;
        int eG = endColor << 16 >>> 24;
        int rG = (int) (sG + (eG - sG) * radio);

        int sB = startColor << 24 >>> 24;
        int eB = endColor << 24 >>> 24;
        int rB = (int) (sB + (eB - sB) * radio);

        int result = 0;

        result |= rA;
        result <<= 8;
        result |= rR;
        result <<= 8;
        result |= rG;
        result <<= 8;
        result |= rB;

        return result;
    }

}
