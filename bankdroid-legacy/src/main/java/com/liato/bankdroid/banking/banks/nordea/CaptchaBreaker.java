package com.liato.bankdroid.banking.banks.nordea;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class CaptchaBreaker {

    private final static int[][][] NUMBERS = CaptchaBreakerNumbers.NUMBERS;

    public static String iMustBreakYou(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ArrayList<Segment> segments = new ArrayList<Segment>();

        boolean numberpart = false;
        Segment segment = new Segment();
        for (int x = 0; x < width; x++) {
            boolean numberpartcol = false;
            for (int y = 0; y < height; y++) {
                int color = bitmap.getPixel(x, y);
                if (color != 0xffffffff) {
                    if (!numberpart) {
                        segment.start = x;
                    }
                    numberpart = numberpartcol = true;
                    break;
                }
            }
            if (numberpart && !numberpartcol) {
                numberpart = false;
                segment.end = x - 1;
                segments.add(segment);
                segment = new Segment();
            }
        }
        if (segment.end == -1 && segment.start >= 0) {
            segment.end = width - 1;
            segments.add(segment);
        }
        StringBuilder sb = new StringBuilder(segments.size());
        for (Segment s : segments) {
            Bitmap numberSegment = Bitmap.createBitmap(bitmap, s.start, 0, s.end - s.start, height);
            sb.append(extractNumber(numberSegment));
            numberSegment.recycle();
            numberSegment = null;
        }
        return sb.toString();

    }

    private final static String extractNumber(Bitmap bitmap) {
        int width = bitmap.getWidth();
        for (int i = 0; i < NUMBERS.length; i++) {
            int matches = 0;
            int[][] number = NUMBERS[i];
            for (int pi = 0; pi < number.length; pi++) {
                int[] point = number[pi];
                if (point[0] >= width) {
                    break;
                }
                int color = bitmap.getPixel(point[0], point[1]);
                if ((color == 0xffffffff && point[2] == 0) || (color != 0xffffffff
                        && point[2] == 1)) {
                    matches++;
                }
            }
            if (matches == number.length) {
                return Integer.toString(i);
            }
        }
        return "?";
    }


}

class Segment {

    public int start = -1;

    public int end = -1;

    @Override
    public String toString() {
        return String.format("Segment {start=%d, end=%d}", start, end);
    }
}
