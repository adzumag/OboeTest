#include "AudioEngine.h"
#include <android/log.h>

#define LOG_TAG "AudioEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

AudioEngine::AudioEngine() {
}

AudioEngine::~AudioEngine() {
    stop();
}

bool AudioEngine::start() {
    oboe::AudioStreamBuilder builder;
    builder.setDataCallback(this)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setFormat(oboe::AudioFormat::Float)
            ->setChannelCount(oboe::ChannelCount::Mono);

    oboe::Result result = builder.openStream(&stream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to open stream: %s", oboe::convertToText(result));
        return false;
    }

    sampleRate = stream->getSampleRate();
    LOGI("Stream opened: sample rate = %d", sampleRate);

    result = stream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start stream: %s", oboe::convertToText(result));
        return false;
    }

    LOGI("Stream started successfully");
    return true;
}

void AudioEngine::stop() {
    if (stream != nullptr) {
        stream->stop();
        stream->close();
        delete stream;
        stream = nullptr;
    }
}

int AudioEngine::findAvailableVoice() {
    for (int i = 0; i < MAX_VOICES; i++) {
        if (!voices[i].isActive.load()) {
            return i;
        }
    }
    return -1; // No available voice
}

void AudioEngine::playTone(float frequency, float durationSeconds) {
    std::lock_guard<std::mutex> lock(voicesMutex);

    int voiceIndex = findAvailableVoice();
    if (voiceIndex == -1) {
        LOGE("No available voice slots");
        return;
    }

    Voice& voice = voices[voiceIndex];
    voice.frequency = frequency;
    voice.remainingFrames = static_cast<int32_t>(durationSeconds * sampleRate);
    voice.phase = 0.0;
    voice.isActive = true;

    LOGI("Playing tone: %f Hz for %f seconds on voice %d", frequency, durationSeconds, voiceIndex);
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {

    auto *outputBuffer = static_cast<float *>(audioData);

    // Clear buffer
    for (int i = 0; i < numFrames; i++) {
        outputBuffer[i] = 0.0f;
    }

    // Mix all active voices
    for (auto& voice : voices) {
        if (!voice.isActive.load()) continue;

        for (int i = 0; i < numFrames; i++) {
            if (voice.remainingFrames > 0) {
                // サイン波生成してミックス
                float sample = 0.2f * sinf(2.0f * M_PI * voice.phase);
                outputBuffer[i] += sample;

                voice.phase += voice.frequency / sampleRate;
                if (voice.phase >= 1.0) {
                    voice.phase -= 1.0;
                }

                voice.remainingFrames--;
                if (voice.remainingFrames <= 0) {
                    voice.isActive = false;
                    voice.phase = 0.0;
                }
            }
        }
    }

    return oboe::DataCallbackResult::Continue;
}
