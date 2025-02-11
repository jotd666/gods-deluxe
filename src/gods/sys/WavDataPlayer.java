package gods.sys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

class WavDataPlayer implements Runnable
{
	static final int MAX_LINE = Math.max(4, Math.min(Runtime.getRuntime().availableProcessors()-1, 7));
	static final int MAX_LINE_PER_TYPE = MAX_LINE - 2;
	
	static final ExecutorService soundService = Executors.newFixedThreadPool(MAX_LINE);
	static final Map<Integer, Queue<SourceDataLine>> soundLines = new ConcurrentHashMap<>();
	
	byte[] data = null;
	final AtomicInteger sample_position = new AtomicInteger(0);
	DataLine.Info info;
	int info_id;
	String name;
	int frame_size;
	int rewind_mark;
	final AtomicBoolean running = new AtomicBoolean(false);
	boolean loop = false;
	
	WavDataPlayer(File fileIn)
	{
		try
		{
			if (!fileIn.exists()) {
				throw new IllegalArgumentException("File does not exist: " + fileIn);
			}
			this.name = fileIn.getName();
			
			try (AudioInputStream audioStream = AudioSystem
					.getAudioInputStream(new ByteArrayInputStream(Files.readAllBytes(fileIn.toPath())))) {
				
				AudioFormat audioFormat = audioStream.getFormat();
				info = new DataLine.Info(SourceDataLine.class, audioFormat);
				info_id = info.toString().hashCode();
				
				// Load clip data from input stream
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			    int nRead;
			    byte[] chunk = new byte[4096];
			    while ((nRead = audioStream.read(chunk, 0, chunk.length)) != -1) {
			        buffer.write(chunk, 0, nRead);
			    }

			    buffer.flush();
			    data = buffer.toByteArray();
			    
				frame_size = audioFormat.getFrameSize();
				rewind_mark = data.length / 2;
				//System.out.println("clip "+ name + " rewind: " + rewind_mark + " total: " + data.length + " frame: " + frame_size);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		catch (Exception ex) {
			System.err.println("error: on loading sound - " + ex);
			data = null;
		}	
	}
	
	private SourceDataLine crateLine() {
		try {
			SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(line.getFormat(), 4096 * line.getFormat().getFrameSize());
			//clipLines.forEach((k,v) -> {debug("line [" + k + "][" + v.size() + "]");});
			return line;
		} catch (LineUnavailableException e) {
			throw new IllegalStateException("Invalid line info " + info, e);
		}
	}
	
	private SourceDataLine acquireLine() {
		Queue<SourceDataLine> lines = soundLines.computeIfAbsent(info_id, key -> new ConcurrentLinkedQueue<>());
		SourceDataLine line = lines.poll();
		if (line == null) {
			line = crateLine();
		}
		return line;
	}
	
	private void releaseLine(SourceDataLine line) {
		Queue<SourceDataLine> queue = soundLines.get(info_id);
		if (queue.size() < MAX_LINE_PER_TYPE) {
			// Keep line in queue
			//debug("offer");
			queue.offer(line);
		}
		else {
			// Free the line
        	//debug("close");
        	line.stop();
        	line.flush();
        	line.close();
		};	
	}
	
	@Override
	public void run() {
		try {
			//debug("run");
			
			SourceDataLine sdl = acquireLine();
			sdl.start();
			
			int buffer_size = sdl.getBufferSize();
			int pos = seek(0);
			
			do {
				forward(buffer_size);
				sdl.write(data, pos, Math.min(data.length - pos, buffer_size));
			} while (running.get() && (pos = sample_position.get()) < data.length);
			
			releaseLine(sdl);	
		}
		catch (Exception e) {
			System.err.println("error: playing - " + name + " : " + e);
		}
		finally {
			running.set(false);
		}
	}
	
	private int integral(int value, int slice) {
		return (value/slice) * slice;
	}
	
	private int forward(int length) {
		int next = sample_position.get() + length;
		if (next >= data.length && loop) {
			return seek(0);
		} 
		return seek(Math.min(next, data.length));
	}
	
	boolean runnable() {
		return running.compareAndSet(false, true);
	}
	
	int seek(int pos) {
		if (pos == 0 || pos == data.length) {
			return sample_position.updateAndGet(curr -> pos);
		}
		int adjust = Math.max(Math.min(pos, data.length), 0);
		return sample_position.updateAndGet(curr -> integral(adjust, frame_size));
	}
	
	void start(boolean loop) {
		//debug("start");
		this.loop = loop;
		soundService.execute(this);
	}
	
	void rewind() {
		seek(0);
	}
	
	void loop() {
		loop = true;
	}
	
	void pause() {
		loop = false;
	}
	
	void stop() {
		running.set(false);
	}
	
	void close() {
		//debug("close");
		stop();
		data = null;
		info = null;
	}
	
	void debug(String action) {
		System.out.println(action + " " + name + "[" + info_id + "] - pos: " + sample_position.get() +
				" loop: " + loop + " thread: " + Thread.currentThread());
	}
	
	static void shutdown() {
		soundService.shutdown();
	}
}