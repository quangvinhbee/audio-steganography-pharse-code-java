/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import audio.AudioSampleReader;
import audio.AudioSampleWriter;
import static audio.Common.getWaveFromAudio;
import binary.Binary;
import binary.BinaryTool;
import org.apache.commons.math3.complex.Complex;
import fourier.FFTData;
import fourier.FFT;
import fourier.FFTDataAnalyzer;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import javax.sound.sampled.AudioFileFormat;
import jm.util.Read;
import org.apache.commons.math3.*;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

/**
 *
 * @author lamqu
 */
public class run {

    private static Scanner scann = new Scanner(System.in);

    public static void Decode() throws Exception {

        System.out.println("Nhập độ dài chuỗi: ");
        int len = Integer.parseInt(scann.nextLine());

        for (int k = 3; k <= len; k++) {

            int msglen = 8 * k; // 1 byte = 8bit
            int temp_int = (int) Math.round(Math.ceil(Math.log(msglen) / Math.log(2)));
            double temp_1 = 2 * Math.pow(2, temp_int);
            int seglen = (int) temp_1;

            String audioFile = "C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\encoded_Run.wav";
            ArrayList<Float> wave = getWaveFromAudio(new File("C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\encoded_Run.wav"));//đọc file và biến đổi thành sóng
            AudioSampleReader sampleReader = new AudioSampleReader(new File(audioFile));

            float[] dataFloat = Read.audio(audioFile);
            double[] dataWave = new double[dataFloat.length];

            for (int i = 0; i < dataFloat.length; i++) {
                dataWave[i] = dataFloat[i];
            }

            double[] channelOne = new double[dataFloat.length / 2];
            for (int i = 0; i < dataWave.length; i += 2) {
                channelOne[i / 2] = dataWave[i];
                // System.out.print(" "+channelOne[i / 2]);
            }

            channelOne = FFT.correctDataLength(channelOne);

            FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);

            Complex[] complexRe = transformer.transform(channelOne, TransformType.FORWARD);
            System.out.println("\nDecode\n");

            int[] bin = new int[msglen];

            System.out.println("Result length = " + k);
            for (int i = 0; i < msglen; i++) {

                double temp = complexRe[i].getReal();
                if (temp < 0) {
                    bin[i] = 0;
                } else {
                    bin[i] = 1;
                }

            }
            System.out.println(BinaryTool.binaryToString(bin));

        }

    }

    public static void Encode() throws Exception {
        String audioFile = "C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\file_example_WAV_5MG.wav";
        ArrayList<Float> wave = getWaveFromAudio(new File("C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\file_example_WAV_5MG.wav"));//đọc file và biến đổi thành sóng
        AudioSampleReader sampleReader = new AudioSampleReader(new File(audioFile));
        System.out.println(sampleReader);
        float[] dataFloat = Read.audio(audioFile);
//        for (int i = 0; i < dataFloat.length / 2; i++) {
//            System.out.print(" " + dataFloat[i]);
//        }
//        System.out.println(wave);
        System.out.println("Nhập tin cần giấu: ");
        String messageSecret = scann.nextLine();
        int msglen = 8 * messageSecret.length(); // 1 byte = 8bit
        int temp = (int) Math.round(Math.ceil(Math.log(msglen) / Math.log(2)));
        double temp_1 = 2 * Math.pow(2, temp);
        int seglen = (int) temp_1;
        int segnum = (int) Math.ceil(wave.size() / seglen); // tính độ dài các đoạn
        int[] msgBin = BinaryTool.ASCIIToBinary(messageSecret).getIntArray(); // biến đổi thành chuỗi nhị phân
        double[] msgPi = new double[msgBin.length];
        System.out.println("length = " + msglen);

        for (int i = 0; i < msgPi.length; i++) {
            
            if (msgBin[i] == 0) {
                msgPi[i] = -1 * Math.PI / 2;
            } else {
                msgPi[i] = 1 * Math.PI / 2;
            }
        }

        double[] dataWave = new double[dataFloat.length];
        for (int i = 0; i < dataFloat.length; i++) {
            dataWave[i] = dataFloat[i];
        }

        // chia làm 2 channel
        double[] channelOne = new double[dataFloat.length / 2];
        for (int i = 0; i < dataWave.length; i += 2) {
            channelOne[i / 2] = dataWave[i];
            // System.out.print(" "+channelOne[i / 2]);
        }
        // biến đổi Fourier
//        FFTData[] freqMag = FFT.getMag(channelOne, 44100); // TODO: don't hardcode
//        FFTDataAnalyzer analyzer = new FFTDataAnalyzer(freqMag);
//        boolean isRest = analyzer.isRest();
//
        channelOne = FFT.correctDataLength(channelOne);

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);

        Complex[] complexFFT = transformer.transform(channelOne, TransformType.FORWARD); //biến đổi theo tần số

        for (int i = 0; i < msgPi.length; i += 1) {
            complexFFT[i] = new Complex(msgPi[i] * complexFFT[i].abs());
        }
        Complex[] complexI_FFT = transformer.transform(complexFFT, TransformType.FORWARD);// biến đổi theo thời gian (trạng thái ban đầu)

        double[] complexTemp = new double[complexI_FFT.length];

        for (int i = 0; i < complexI_FFT.length; i++) {
            complexTemp[i] = complexI_FFT[i].getReal();
        }

        for (int i = 0; i < complexI_FFT.length; i++) {
            channelOne[i] = complexI_FFT[i].getReal();
        }

        Complex[] complexRe = transformer.transform(channelOne, TransformType.FORWARD);

        for (int i = 0; i < channelOne.length; i++) {
            dataWave[i * 2] = channelOne[i];
        }

        

        File outFile = new File("C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\encoded_Run.wav");
        AudioSampleWriter audioWriter = new AudioSampleWriter(outFile, sampleReader.getFormat(), AudioFileFormat.Type.WAVE);
        audioWriter.write(dataWave);
        audioWriter.close();

//        double[] freqs = FFT.getFreqs(complexData.length, 44100); // đây là mảng tần số sao khi biến đổi fourier
//
//        double[] Angle = new double[freqs.length];
//
//        for (int i = 0; i < freqs.length/2; i++) {
//            Angle[i] = Math.PI * 2 * freqs[i];
//            System.out.print("   " + freqs[i]);
//        }
    }

    public static void main(String[] args) throws Exception {
        Encode();
        Decode();
    }
}
