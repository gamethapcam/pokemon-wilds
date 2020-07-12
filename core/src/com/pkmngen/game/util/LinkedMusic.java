package com.pkmngen.game.util;

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

    public LinkedMusic(String m1, String m2) {
        this.music1 = Gdx.audio.newMusic(Gdx.files.internal(m1 + ".ogg"));
        this.music1.setLooping(false);
        this.music2 = Gdx.audio.newMusic(Gdx.files.internal(m2 + ".ogg"));
        this.music2.setLooping(true);
        this.music2.play();
        this.music2.pause();
        this.music2.setPosition(0f);
        this.currMusic = this.music1;

        this.music1.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(final Music music) {
                music2.play();
                currMusic = music2;
            }
        });
    }

    @Override
    public void play() {
        this.currMusic.play();
    }

    @Override
    public void pause() {
        this.currMusic.pause();
    }

    @Override
    public void stop() {
        // reset to be played from beginning.
        this.music1.stop();
        this.music2.stop();
        this.currMusic = this.music1;
    }

    @Override
    public boolean isPlaying() {
        return this.currMusic.isPlaying();
    }

    @Override
    public void setLooping(boolean isLooping) {
        this.currMusic.setLooping(isLooping);
    }

    @Override
    public boolean isLooping() {
        return this.currMusic.isLooping();
    }

    @Override
    public void setVolume(float volume) {
        this.music1.setVolume(volume);
        this.music2.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return this.currMusic.getVolume();
    }

    @Override
    public void setPan(float pan, float volume) {
        this.currMusic.setPan(pan, volume);
    }

    @Override
    public void setPosition(float position) {
        this.currMusic.setPosition(position);
    }

    @Override
    public float getPosition() {
        return this.currMusic.getPosition();
    }

    @Override
    public void dispose() {
        this.music1.dispose();
        this.music2.dispose();
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        this.currMusic.setOnCompletionListener(listener);
    }
}
