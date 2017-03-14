package gme;

/* Simple front-end for VGMPlayer
To build gme.jar:

	javac -source 1.4 *.java
	jar cf gme.jar *.class
*/

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class PlayerList extends VGMPlayer
{
	public Label titleLabel;
	public Label trackLabel;
	int playlistIndex;
	final ArrayList list = new ArrayList();
	
	PlayerList( int rate )
	{
		super( rate );
		this.trackLabel = new Label("");
		this.titleLabel = new Label("");
	}
	
	private void updateTrack()
	{
		trackLabel.setText( (playlistIndex + 1) + "/" + list.size() );
	}
	
	public void prev() throws Exception
	{
		if ( getCurrentTime() < 4 && isPlaying() && playlistIndex > 0 )
			playlistIndex--;
		playIndex( playlistIndex );
	}
	
	public void next() throws Exception
	{
		if ( playlistIndex < list.size() - 1 )
		{
			playlistIndex++;
			playIndex( playlistIndex );
		}
	}
	
	private static final class Entry
	{
		String url;
		String path;
		int track;
		String title;
		int time;
	}
	
	private void playIndex( int i ) throws Exception
	{
		playlistIndex = i;
		updateTrack();
		Entry e = (Entry) list.get( i );
		titleLabel.setText( e.title );
		loadFile( e.url, e.path );
		startTrack( e.track - 1, e.time );
	}
	
	public void add( String url, String path, int track, String title, int time, boolean playNow ) throws Exception
	{
		if ( title.length() == 0 )
		{
			title = path;
			if ( title.length() == 0 )
				title = url;
			
			title = title.substring( title.lastIndexOf( '/' ) + 1 );
		}
		
		Entry e = new Entry();
		e.url   = url;
		e.path  = path;
		e.track = track;
		e.title = title;
		e.time  = time;
		list.add( e );
		
		if ( playNow )
			playIndex( list.size() - 1 );
		else
			updateTrack();
	}
}



public final class gme extends Applet implements ActionListener
{
	// Plays file at given URL (HTTP only). If it's an archive (.zip)
	// then path specifies the file within the archive. Track ranges
	// from 1 to number of tracks in file.
	public void playFile( String url, String path, int track, String title, int time )
	{
		try
		{
			player.add( url, path, track, title, time, !playlistEnabled.getState() || !player.isPlaying() );
		}
		catch ( Exception e ) { e.printStackTrace(); }
	}
	
	public void playFile( String url, String path, int track, String title )
	{
		playFile( url, path, track, title, 150 );
	}
	
	public void playFile( String url, String path, int track )
	{
		playFile( url, path, track, "" );
	}
	
	// Stops currently playing file, if any
	public void stopFile()
	{
		try { player.stop(); }
		catch ( Exception e ) { e.printStackTrace(); }
	}
	
// Applet

	PlayerWithUpdate player;
	boolean backgroundPlayback;
	Checkbox playlistEnabled;
	
	private Button newBut( String name )
	{
		Button b = new Button( name );
		b.setActionCommand( name );
		b.addActionListener( this );
		add( b );
		return b;
	}
	
	void createGUI()
	{
		add( player.time = new Label( "          " ) );
		add( player.trackLabel = new Label( "          " ) );
		
		newBut( "Prev" );
		newBut( "Next" );
		newBut( "Stop" );
		
		add( player.titleLabel = new Label( "                                                  " ) );
		
		playlistEnabled = new Checkbox( "Playlist" );
		add( playlistEnabled );
	}
	
	// Returns integer parameter passed to applet, or defaultValue if missing
	int getIntParameter( String name, int defaultValue )
	{
		String p = getParameter( name );
		return (p != null ? Integer.parseInt( p ) : defaultValue);
	}
	
	// Returns string parameter passed to applet, or defaultValue if missing
	String getStringParameter( String name, String defaultValue )
	{
		String p = getParameter( name );
		return (p != null ? p : defaultValue);
	}
	
	// Called when applet is first loaded
	public void init()
	{
		try
		{
			// Setup player and sample rate
			int sampleRate = getIntParameter( "SAMPLERATE", 44100 );
			player = new PlayerWithUpdate( sampleRate );
			player.setVolume( 1.0 );
			
			backgroundPlayback = getIntParameter( "BACKGROUND", 0 ) != 0;
			if ( getIntParameter( "NOGUI", 0 ) == 0 )
				createGUI();
			
			// Optionally start playing file immediately
			String url = getParameter( "PLAYURL" );
			if ( url != null )
				playFile( url, getStringParameter( "PLAYPATH", "" ),
						getIntParameter( "PLAYTRACK", 1 ) );
		}
		catch ( Exception e ) { e.printStackTrace(); }
	}
	
	static int rand( int range )
	{
		return (int) (java.lang.Math.random() * range + 0.5);
	}
	
	// Called when button is clicked
	public void actionPerformed( ActionEvent e )
	{
		try
		{
			String cmd = e.getActionCommand();
			if ( cmd == "Stop" )
			{
				if ( player.isPlaying() )
					player.pause();
				else
					player.play();
				return;
			}
			
			if ( cmd == "Prev" )
			{
				player.prev();
				return;
			}
			
			if ( cmd == "Next" )
			{
				player.next();
				return;
			}
		}
		catch ( Exception ex ) { ex.printStackTrace(); }
	}
	
	// Called when applet's page isn't active
	public void stop()
	{
		if ( !backgroundPlayback )
			stopFile();
	}
	
	public void destroy()
	{
		try
		{
			stopFile();
		}
		catch ( Exception e ) { e.printStackTrace(); }
	}
}
