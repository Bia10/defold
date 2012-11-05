package com.dynamo.cr.parted;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public class ParticleLibrary {
    static {
        Native.register("particle_shared");
    }

    public interface RenderInstanceCallback extends Callback {
        void invoke(Pointer userContext, Pointer material, Pointer texture, int blendMode, int vertexIndex, int vertexCount, Pointer constants, int constantCount);
    }

    public interface FetchAnimationCallback extends Callback {
        int invoke(Pointer tileSource, long hash, AnimationData outAnimationData);
    }

    public static native Pointer Particle_NewPrototype(Buffer buffer, int bufferSize);

    public static native void Particle_DeletePrototype(Pointer prototype);

    public static native boolean Particle_ReloadPrototype(Pointer prototype, Buffer buffer, int bufferSize);

    public static native Pointer Particle_CreateContext(int maxEmitterCount, int maxParticleCount);

    public static native Pointer Particle_DestroyContext(Pointer context);

    public static native int Particle_GetContextMaxParticleCount(Pointer context);

    public static native void Particle_SetContextMaxParticleCount(Pointer context, int maxParticleCount);

    public static native Pointer Particle_CreateInstance(Pointer context, Pointer prototype);

    public static native void Particle_DestroyInstance(Pointer context, Pointer instance);

    public static native void Particle_ReloadInstance(Pointer context, Pointer instance, boolean replay);

    public static native void Particle_StartInstance(Pointer context, Pointer instance);

    public static native void Particle_StopInstance(Pointer context, Pointer instance);

    public static native void Particle_ResetInstance(Pointer context, Pointer instance);

    public static native void Particle_SetPosition(Pointer context, Pointer instance, Vector3 position);

    public static native void Particle_SetRotation(Pointer context, Pointer instance, Quat rotation);

    public static native boolean Particle_IsSleeping(Pointer context, Pointer instance);

    public static native void Particle_Update(Pointer context, float dt, Buffer vertexBuffer,
            int vertexBufferSize,
            IntByReference outVertexBufferSize, FetchAnimationCallback callback);

    public static native void Particle_Render(Pointer context, Pointer userContext, RenderInstanceCallback callback);

    public static native void Particle_SetMaterial(Pointer prototype, int emitterIndex, Pointer material);

    public static native void Particle_SetTileSource(Pointer prototype, int emitterIndex, Pointer tileSource);

    public static native long Particle_Hash(String value);

    public static native void Particle_GetStats(Pointer context, Stats stats);

    public static native void Particle_GetInstanceStats(Pointer context, Pointer instance, InstanceStats stats);

    public static native int Particle_GetVertexBufferSize(int particle_count);

    public static class Stats extends Structure {

        public Stats() {
            structSize = size();
        }

        public int particles;
        public int maxParticles;
        public int structSize;
    }

    public static class InstanceStats extends Structure {

        public InstanceStats() {
            structSize = size();
        }

        public float time;
        public int structSize;
    }

    public static class Vector3 extends Structure {

        public Vector3() {
        }

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3(Pointer position) {
            super(position);
        }

        public float x;
        public float y;
        public float z;
        public float pad;
    }

    public static class Quat extends Structure {

        public Quat() {
        }

        public Quat(float x, float y, float z, float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public float x;
        public float y;
        public float z;
        public float w;
    }

    public static interface AnimPlayback {
        public static final int ANIM_PLAYBACK_NONE = 0;
        public static final int ANIM_PLAYBACK_ONCE_FORWARD = 1;
        public static final int ANIM_PLAYBACK_ONCE_BACKWARD = 2;
        public static final int ANIM_PLAYBACK_LOOP_FORWARD = 3;
        public static final int ANIM_PLAYBACK_LOOP_BACKWARD = 4;
        public static final int ANIM_PLAYBACK_LOOP_PINGPONG = 5;
    }

    public static interface FetchAnimationResult {
        public static final int FETCH_ANIMATION_OK = 0;
        public static final int FETCH_ANIMATION_NOT_FOUND = -1;
        public static final int FETCH_ANIMATION_UNKNOWN_ERROR = -1000;
    }

    public static class AnimationData extends Structure {
        public AnimationData() {
            super();
            setFieldOrder(new String[] { "texture", "texCoords", "playback", "tileWidth", "tileHeight", "startTile", "endTile", "fps", "hFlip",
                    "vFlip", "structSize" });
        }

        public Pointer texture;
        public FloatBuffer texCoords;
        public int playback;
        public int tileWidth;
        public int tileHeight;
        public int startTile;
        public int endTile;
        public int fps;
        public int hFlip;
        public int vFlip;
        // Used to validate the struct size in particle.cpp
        public int structSize;
    }
}
