package gme;

// Evan - had to make this public and move to sep file

import java.awt.Label;

public final class PlayerWithUpdate extends PlayerList
{
	Label time;
	
	public PlayerWithUpdate( int sampleRate )
	{
		super( sampleRate );
	}
	
	char [] str = new char [5];
	int last = -1;
	
	protected void idle()
	{
		try
		{
			super.idle();
			/*
			if ( !isPlaying() )
			{
				next();
				last = -1;
			}
			*/
			int secs = getCurrentTime();
			if ( last != secs )
			{
				last = secs;
				str [4] = (char) ('0' + secs % 10);
				str [3] = (char) ('0' + secs / 10 % 6);
				str [2] = (char) (':');
				str [1] = (char) ('0' + secs / 60 % 10);
				str [0] = (char) ((secs >= 600 ? '0' + secs / 600 : ' '));
				
				time.setText( new String( str ) );
			}
		}
		catch ( Exception ex ) { ex.printStackTrace(); }
	}
};