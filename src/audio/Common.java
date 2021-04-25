/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Common {

    public static ArrayList<Float> getWaveFromAudio(File audio) throws Exception {
        waveDecoder decoder = new waveDecoder( new FileInputStream( audio ));
        System.out.println(decoder);
        float[] samples = new float[1024];
        ArrayList<Float> originalWaveSamples = new ArrayList<Float>();

        int readSamples = 0;
        while( ( readSamples = decoder.readSamples( samples ) ) > 0 ){
            for (float b : samples){
                originalWaveSamples.add(b);
            }
        }
        return originalWaveSamples;
    }

    public static int[] pnSequenceKey(long key,int audioSize){
        int[] pn = new int[audioSize];
        Random random = new Random(key);

        for(int i = 0;i < audioSize ; i++){
            int rand = random.nextInt() % 2  == 0 ? 1 : -1;
            pn[i] = rand;
        }
        return pn;
    }
    public static void main(String[] args) {
        try {
            System.out.println(getWaveFromAudio(new File("C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\file_example_WAV_5MG.wav")).size());
        } catch (Exception ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}