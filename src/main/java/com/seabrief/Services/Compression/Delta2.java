package com.seabrief.Services.Compression;

public class Delta2 {
    public static long[] encode(long[] input) {
        if (input.length < 2) {
            return input;
        } 
        
        long[] compressed = new long[input.length];

        compressed[0] = input[0];
        long last = input[1] - input[0];
        compressed[1] = last;

        int i = 2;
        while (i < input.length)
        {
            long delta = input[i] - input[i - 1];
            long delta2 = delta - last;
            compressed[i] = delta2;
            last = delta;
            i++;
        }

        return compressed;
    }

    public static long[] decode(long[] input) {
        if (input.length < 2) {
            return input;
        } 

        long[] decompressed = new long[input.length];

        decompressed[0] = input[0];

        long last = 0;
        for (int i = 1; i < input.length; i++)
        {
            last += input[i];
            decompressed[i] = decompressed[i - 1] + last;
        }

        return decompressed;
    }
}