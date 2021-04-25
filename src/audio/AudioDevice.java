/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import binary.BinaryTool;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.Encoding;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Class that allows directly passing PCM float mono
 * data to the sound card for playback. The sampling 
 * rate of the PCM data must be 44100Hz. 
 * 
 * @author mzechner
 *
 */
public class AudioDevice 
{
	private final static int BUFFER_SIZE = 1024;

	public SourceDataLine getOut() {
		return out;
	}

	private final SourceDataLine out;

	private byte[] buffer = new byte[BUFFER_SIZE*2];
	ArrayList<Byte[]> buffer_list = new ArrayList<>();

	public ArrayList<Byte[]> getBuffer_list() {
		return buffer_list;
	}

	public void setBuffer_list(ArrayList<Byte[]> buffer_list) {
		this.buffer_list = buffer_list;
	}

	public AudioDevice( ) throws Exception
	{
		AudioFormat format = new AudioFormat( Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false );
		out = AudioSystem.getSourceDataLine( format );
		out.open(format);	
		out.start();

	}

	public void writeSamples( float[] samples ) throws IOException {
		fillBuffer( samples );
		out.write( buffer, 0, buffer.length );

		buffer_list.add(BinaryTool.toObjects(buffer));
	}



	private void fillBuffer( float[] samples )
	{
		for( int i = 0, j = 0; i < samples.length; i++, j+=2 )
		{
			short value = (short)(samples[i] * Short.MAX_VALUE);
			buffer[j] = (byte)(value | 0xff);
			buffer[j+1] = (byte)(value >> 8 );
		}

	}

	public void playSamples(ArrayList<Float> samplesWave) throws IOException {
		float[] samples = new float[1024];
		int slice = 0;
		for (int i = 0; i < samplesWave.size(); i++)
		{
			samples[slice] = samplesWave.get(i);
//            System.out.println(samples[slice]);
			slice++;
			if(slice == 1024){
				writeSamples( samples );
				samples = new float[1024];
				slice = 0;
//                System.out.println(samples.length);
			}
		}
		if(slice != 0){
			writeSamples( samples );
		}
	}

	public void writeToFile(ArrayList<Float> samplesWave,File file) throws IOException {

		final double sampleRate = 44100.0;
		final double frequency = 440;
		final double frequency2 = 90;
		final double amplitude = 1.0;
		final double seconds = 2.0;
		final double twoPiF = 2 * Math.PI * frequency;
		final double piF = Math.PI * frequency2;

		float[] buffer = new float[samplesWave.size()];

		int t = 0;
		for(float b : samplesWave){
			buffer[t] = b;
			t++;
		}

		final byte[] byteBuffer = new byte[buffer.length * 2];

		int bufferIndex = 0;
		for (int i = 0; i < byteBuffer.length; i++) {
			final int x = (int)(buffer[bufferIndex++] * 32767.0);

			byteBuffer[i++] = (byte)x;
			byteBuffer[i] = (byte)(x >>> 8);
		}

		File out = file;

		final boolean bigEndian = false;
		final boolean signed = true;

		final int bits = 16;
		final int channels = 1;

		AudioFormat format = new AudioFormat((float)sampleRate, bits, channels, signed, bigEndian);
		ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
		AudioInputStream audioInputStream = new AudioInputStream(bais, format, buffer.length);
		AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
		audioInputStream.close();
	}

	public  static  void playSample(ArrayList<Float> samples) throws Exception {
		AudioDevice device = new AudioDevice();
		device.playSamples(samples);
	}
	
	public static void main( String[] argv ) throws Exception
	{
		float[] samples = new float[1024];
		waveDecoder reader = new waveDecoder( new FileInputStream( "C:\\Users\\lamqu\\Desktop\\LAB\\LAB_KTGT\\phase-coding-audio\\file_example_WAV_5MG.wav" ) );
		AudioDevice device = new AudioDevice( );

		while( reader.readSamples( samples ) > 0 )
		{
			device.writeSamples( samples );
		}

	}
}
