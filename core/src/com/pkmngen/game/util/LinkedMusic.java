package com.pkmngen.game.util;

import gme.VGMPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Sets music1 to jump to beginning of music2 after it completes. Music2 will
 * repeat. Shouldn't be any lag when it jumps between music. Useful for any
 * music that has an intro, and needs to loop after that. (battle themes, etc).
 */
public class LinkedMusic implements Music {
    Music music1;
    Music music2;
    Music currMusic;
//    public static int sampleRate = 44100;
    public static VGMPlayer gbsPlayer = new VGMPlayer(44100);

    public enum Type {
        NORMAL,
        GME
    }
    public Type type = Type.NORMAL;

    public LinkedMusic(String m1, String m2) {
        // TODO: this was an attempt at using gbs music,
        //       had various issues.
        // TODO: might need to do more tracks
        if (m1.contains("pkmnmansion1") || m2.contains("pkmnmansion1")) {
            this.type = Type.GME;
            try {
//                LinkedMusic.gbsPlayer.loadFile("C:/Users/Evan/Desktop/pokemon_gbs/Pokemon Gold zophar/DMG-AAUJ-JPN.gbs",
//                                               "C:/Users/Evan/Desktop/pokemon_gbs/Pokemon Gold zophar/DMG-AAUJ-JPN.gbs");

//                LinkedMusic.gbsPlayer.loadFile("C:/Users/Evan/Desktop/pokemon_gbs/PM_Y_C_Stereo_GBS/Pokemon Crystal (2001)(Game Freak, Nintendo).gbs",
//                                   "C:/Users/Evan/Desktop/pokemon_gbs/PM_Y_C_Stereo_GBS/Pokemon Crystal (2001)(Game Freak, Nintendo).gbs");
                LinkedMusic.gbsPlayer.loadFile("DMG-APEE-USA.gbs",
                                               "DMG-APEE-USA.gbs");
                //don't trust file number in directory - look inside m3u file for GBS,<track number>
                LinkedMusic.gbsPlayer.startTrack(38, 100);  // 150 is somehow related to fade
//                LinkedMusic.gbsPlayer.setVolume(.7);
//                LinkedMusic.gbsPlayer.startTrack(88, 150);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        this.music1 = Gdx.audio.newMusic(Gdx.files.internal(m1 + ".ogg"));
        this.music1.setLooping(false);
        this.currMusic = this.music1;
        if (!m2.equals("")) {
            this.music1.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(final Music music) {
                    music2.play();
                    currMusic = music2;
                }
            });
            this.music2 = Gdx.audio.newMusic(Gdx.files.internal(m2 + ".ogg"));
            this.music2.setLooping(true);
            this.music2.play();
            this.music2.pause();
            this.music2.setPosition(0f);
        }

    }

    @Override
    public void play() {
        if (this.type == Type.GME) {
            try {
                // doing play() when music was already playing caused stuff to break.
                if (!LinkedMusic.gbsPlayer.isPlaying()) {
                    LinkedMusic.gbsPlayer.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        this.currMusic.play();
    }

    @Override
    public void pause() {
        if (this.type == Type.GME) {
            try {
                if (LinkedMusic.gbsPlayer.isPlaying()) {
                    LinkedMusic.gbsPlayer.pause();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        this.currMusic.pause();
    }

    @Override
    public void stop() {
        if (this.type == Type.GME) {
            try {
                LinkedMusic.gbsPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        // reset to be played from beginning.
        if (this.music2 != null) {
            this.music2.stop();
        }
        this.music1.stop();
        this.currMusic = this.music1;
    }

    @Override
    public boolean isPlaying() {
        if (this.type == Type.GME) {
            return LinkedMusic.gbsPlayer.isPlaying();
        }
        return this.currMusic.isPlaying();
    }

    @Override
    public void setLooping(boolean isLooping) {
        if (this.type == Type.GME) {
            System.out.println("hi");
            return;
        }
        this.currMusic.setLooping(isLooping);
    }

    @Override
    public boolean isLooping() {
        if (this.type == Type.GME) {
            return true;
        }
        return this.currMusic.isLooping();
    }

    @Override
    public void setVolume(float volume) {
        if (this.type == Type.GME) {
            try {
                LinkedMusic.gbsPlayer.setVolume(volume);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (this.music2 != null) {
            this.music2.setVolume(volume);
        }
        this.music1.setVolume(volume);
    }

    @Override
    public float getVolume() {
        if (this.type == Type.GME) {
            return (float)LinkedMusic.gbsPlayer.getVolume();
        }
        return this.currMusic.getVolume();
    }

    @Override
    public void setPan(float pan, float volume) {
        if (this.type == Type.GME) {
            return;
        }
        this.currMusic.setPan(pan, volume);
    }

    @Override
    public void setPosition(float position) {
        if (this.type == Type.GME) {
            return;
        }
        this.currMusic.setPosition(position);
    }

    @Override
    public float getPosition() {
        if (this.type == Type.GME) {
            return 0f;
        }
        return this.currMusic.getPosition();
    }

    @Override
    public void dispose() {
        if (this.type == Type.GME) {
            return;
        }
        this.music1.dispose();
        if (this.music2 != null) {
            this.music2.dispose();
        }
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        if (this.type == Type.GME) {
            return;
        }
        this.currMusic.setOnCompletionListener(listener);
    }
}
