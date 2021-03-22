package gme;

// Video game music player that runs emulator and plays through speaker
// http://www.slack.net/~ant/

/* Load a music file into player, then start a track. Volume can be
adjusted, track can be paused and resumed, a new track can be started,
or a new file can be loaded at any time.

The file is specified as an HTTP address and optional filename to use
if it's a ZIP archive. To avoid loading file more than necessary over
HTTP, the most recently loaded file is kept in memory and a load request
for the same URL is eliminated. This allows a web page to switch between
several tracks in a ZIP archive or of a multi-track music file, without
having to keep track of whether the file was already loaded. */

import javax.sound.sampled.*;
import java.io.*;

/* Copyright (C) 2007-2008 Shay Green. This module is free software; you
can redistribute it and/or modify it under the terms of the GNU Lesser
General Public License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version. This
module is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
details. You should have received a copy of the GNU Lesser General Public
License along with this module; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA */

class EmuPlayer implements Runnable
{
	// Number of tracks
	public int getTrackCount() { return emu.trackCount(); }
	
	// Starts new track playing, where 0 is the first track.
	// After time seconds, the track starts fading.
	public void startTrack( int track, int time ) throws Exception
	{
		pause();
		if ( line != null )
			line.flush();
		emu.startTrack( track );
		emu.setFade( time, 6 );
		play();
	}
	
	// Currently playing track
	public int getCurrentTrack() { return emu.currentTrack(); }
	
	// Number of seconds played since last startTrack() call
	public int getCurrentTime() { return (emu == null ? 0 : emu.currentTime()); }
	
	// Sets playback volume, where 1.0 is normal, 2.0 is twice as loud.
	// Can be changed while track is playing.
	public void setVolume( double v )
	{
		volume_ = v;
		
		if ( line != null )
		{
			FloatControl mg = (FloatControl) line.getControl( FloatControl.Type.MASTER_GAIN );
			if ( mg != null )
				mg.setValue( (float) (Math.log( v ) / Math.log( 10.0 ) * 20.0) );
		}
	}
	
	// Current playback volume
	public double getVolume() { return volume_; }
	
	// Pauses if track was playing.
	public void pause() throws Exception
	{
		if ( thread != null )
		{
			playing_ = false;
			thread.join();
			thread = null;
		}
	}
	
	// True if track is currently playing
	public boolean isPlaying() { return playing_; }
	
	// Resumes playback where it was paused
	public void play() throws Exception
	{
		if ( line == null )
		{
			line = (SourceDataLine) AudioSystem.getLine( lineInfo );
			line.open( audioFormat );
			setVolume( volume_ );
		}
		thread = new Thread( this );
		playing_ = true;
		thread.start();
	}
	
	// Stops playback and closes audio
	public void stop() throws Exception
	{
		pause();
		
		if ( line != null )
		{
			line.close();
			line = null;
		}
	}
	
	// Called periodically when a track is playing
	protected void idle() { }
	
// private

	// Sets music emulator to get samples from
	void setEmu( MusicEmu emu, int sampleRate ) throws Exception
	{
		stop();
		this.emu = emu;
		if ( emu != null && line == null && this.sampleRate != sampleRate )
		{
			audioFormat = new AudioFormat( AudioFormat.Encoding.PCM_SIGNED,
					sampleRate, 16, 2, 4, sampleRate, true );
			lineInfo = new DataLine.Info( SourceDataLine.class, audioFormat );
			this.sampleRate = sampleRate;
		}
	}
	
	private int sampleRate = 0;
	AudioFormat audioFormat;
	DataLine.Info lineInfo;
	MusicEmu emu;
	Thread thread;
	volatile boolean playing_;
	SourceDataLine line;
	double volume_ = 1.0;
	
	public void run()
	{
		line.start();
		
		// play track until stop signal
		byte [] buf = new byte [8192];
		while ( playing_ && !emu.trackEnded() )
		{
			int count = emu.play( buf, buf.length / 2 );
			line.write( buf, 0, count * 2 );
			idle();
		}
		
		playing_ = false;
		line.stop();
	}
}

public class VGMPlayer extends EmuPlayer
{
	int sampleRate;
	
	public VGMPlayer( int sampleRate ) { this.sampleRate = sampleRate; }
	
	// Stops playback and loads file from given URL (HTTP only).
	// If it's an archive (.zip) then path specifies the file within
	// the archive.
	public void loadFile( String url, String path ) throws Exception
	{
	    System.out.println(System.getProperty("user.dir"));
		stop();
		
		if ( !loadedUrl.equals( url ) || !loadedPath.equals( path ) )
		{
			byte [] data = readFile( url, path );
			
			String name = url.toUpperCase();
			if ( name.endsWith( ".ZIP" ) )
				name = path.toUpperCase();
			
			if ( name.endsWith( ".GZ" ) )
				name = name.substring( 0, name.length() - 3 );
			
			MusicEmu emu = createEmu( name );
			if ( emu == null )
				return; // TODO: throw exception?
			int actualSampleRate = emu.setSampleRate( sampleRate );
			emu.loadFile( data );
			
			// now that new emulator is ready, replace old one
			setEmu( emu, actualSampleRate );
			loadedUrl  = url;
			loadedPath = path;
		}
	}
	
	// Stops and closes current file and unloads things from memory
	void closeFile() throws Exception
	{
		stop();
		setEmu( null, 0 );
		archiveUrl  = "";
		archiveData = null;
		loadedUrl   = "";
		loadedPath  = "";
	}
	
// private
	
	String loadedUrl  = ""; // URL and path of file loaded into emulator
	String loadedPath = "";
	
	String archiveUrl = ""; // URL of (ZIP) file cached in archiveData
	byte [] archiveData;
	
	// Creates appropriate emulator for given filename
	MusicEmu createEmu( String name )
	{
		if ( name.endsWith( ".VGM" ) || name.endsWith( ".VGZ" ) )
			return new VgmEmu();
		
		if ( name.endsWith( ".GBS" ) )
			return new GbsEmu();
		
		if ( name.endsWith( ".NSF" ) )
			return new NsfEmu();
		
		if ( name.endsWith( ".SPC" ) )
			return new SpcEmu();
		
		return null;
	}
			
	// Loads given URL and file within archive, and caches archive for future access
	byte [] readFile( String url, String path ) throws Exception
	{
		InputStream in = null;
		String name = url.toUpperCase();
		
		//Evan - open non-url here
		if (url.equals(path)) {
			in =  DataReader.openFile( path );
		}
		else if ( !name.endsWith( ".ZIP" ) )
		{
			archiveData = null; // dump previously cached ZIP file
			archiveUrl  = "";
			
			in = DataReader.openHttp( url );
			//System.out.println( "Load " + url );
		}
		else
		{
			if ( !archiveUrl.equals( url ) )
			{
				archiveData = DataReader.loadData( DataReader.openHttp( url ) );
				archiveUrl = url;
				//System.out.println( "Load " + url );
			}
			
			in   = DataReader.openZip( new ByteArrayInputStream( archiveData ), path );
			name = path.toUpperCase();
			//System.out.println( "Unzip " + url );
		}
		
		if ( name.endsWith( ".GZ" ) || name.endsWith( ".VGZ" ) )
			in = DataReader.openGZIP( in );
		
		return DataReader.loadData( in );
	}
}
