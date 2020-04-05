
package micromod;

import javax.sound.sampled.*;
import java.net.*;
import java.io.*;

public class MicromodPlayer implements Runnable {
	private final int SAMPLE_RATE = 44100;
	private final int BUFFER_MS = 40;//15;
	private Micromod micromod;
	private boolean songloop, running;
	private int start_offset = 0;
	private int songlen, remain;
	/*
		Constructor.
	*/
	public MicromodPlayer( URL modfile ) throws IOException {
		URLConnection urlc = modfile.openConnection();
		int datalen = urlc.getContentLength();
		if( datalen < 0 ) throw new IllegalArgumentException( "Could not determine data length!" );
		DataInputStream dis = new DataInputStream( urlc.getInputStream() );
		byte[] songdata = new byte[ datalen ];
		dis.readFully( songdata );
		micromod = new Micromod( songdata, SAMPLE_RATE );
		setloop( true );
	}

	public int get_position()
	{
		return songlen - remain;
	}
	
	public void set_start_offset(int pos)
	{
		start_offset = pos;
	}
	/*
		Set whether the song is to loop continuously or not.
		The default is to loop.
	*/
	public void setloop( boolean loop ) {
		songloop = loop;
	}

	/*
		Begin playback.
		This method will return once the song has finished, or stop has been called.
	*/
	public void run() {
		running = true;
		int buflen = SAMPLE_RATE * BUFFER_MS / 1000;
		int[] lbuf = new int[ buflen ];
		int[] rbuf = new int[ buflen ];
		byte[] obuf = new byte[ buflen << 2 ];
		try {
			AudioFormat af = new AudioFormat( SAMPLE_RATE, 16, 2, true, false );
			DataLine.Info lineInfo = new DataLine.Info( SourceDataLine.class, af );
			SourceDataLine line = (SourceDataLine)AudioSystem.getLine(lineInfo);
			line.open();
			line.start();
			songlen = micromod.getlen();
			remain = songlen;
			long timer, bufms = -1000; // Number of ms in buffer.
			while( remain > 0 && running ) 
			{
				int count = buflen;
				if ( count > remain ) count = remain;
				
				boolean skip = start_offset > (songlen - remain);
				
				micromod.mix( lbuf, rbuf, 0, count );
				if (!skip)
				{
					int optr = 0;
					for( int n = 0; n < count; n++ ) {
						obuf[ optr++ ] = ( byte ) ( lbuf[ n ] & 0xFF );
						obuf[ optr++ ] = ( byte ) ( lbuf[ n ] >> 8 );
						obuf[ optr++ ] = ( byte ) ( rbuf[ n ] & 0xFF );
						obuf[ optr++ ] = ( byte ) ( rbuf[ n ] >> 8 );
						lbuf[ n ] = rbuf[ n ] = 0;
					}
					line.write( obuf, 0, count << 2 );
				}
				
				remain -= count;
				if( remain <= 0 && songloop ) remain = songlen;
				/*
					Attempt to keep the cpu usage even by writing BUFFER_MS
					milliseconds at a time, otherwise the thread will spike
					every half second when the line.write() method unblocks.
					Sometimes Thread.sleep() will sleep for much longer than it
					should, so the amount of time spent sleeping needs to be
					taken into account.
				*/
				if (!skip)
				{
					bufms += BUFFER_MS;
					if( bufms > 0 ) {
						timer = System.currentTimeMillis();
						try{ Thread.sleep( bufms ); } catch( InterruptedException ie ) {}
						bufms -= System.currentTimeMillis() - timer;
					}
				}
			}
			line.drain();
			line.close();
		} catch( LineUnavailableException e ) {
			e.printStackTrace();
			return;
		}
	}

	/*
		Instruct the run() method to finish playing and return.
	*/
	public void stop() {
		running = false;
	}

	/*
		Plays the module specified on the command line once.
	*/
	public static void main( String[] args ) throws Exception {
		if( args.length == 0 ) {
			System.out.println( "Please specify a module file to play." );
			return;
		}
		MicromodPlayer mmp = new MicromodPlayer( new File( args[ 0 ] ).toURI().toURL() );
		mmp.setloop( false );
		new Thread( mmp ).start();
	}
}

