package com.ithinkrok.minigames.util;

import org.bukkit.Sound;

/**
 * Created by paul on 03/01/16.
 */
public class SoundEffect {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundEffect(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
