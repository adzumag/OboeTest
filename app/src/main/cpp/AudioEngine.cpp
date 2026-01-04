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

void AudioEngine::playTone(float frequency, float durationSeconds) {
    currentFrequency = frequency;
    remainingFrames = static_cast<int32_t>(durationSeconds * sampleRate);
    isPlaying = true;
    LOGI("Playing tone: %f Hz for %f seconds (%d frames)", frequency, durationSeconds, remainingFrames.load());
}

oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) {

    auto *outputBuffer = static_cast<float *>(audioData);

    for (int i = 0; i < numFrames; i++) {
        if (isPlaying && remainingFrames > 0) {
            // サイン波生成
            float frequency = currentFrequency;
            outputBuffer[i] = 0.3f * sinf(2.0f * M_PI * phase);

            phase += frequency / sampleRate;
            if (phase >= 1.0) {
                phase -= 1.0;
            }

            remainingFrames--;
            if (remainingFrames <= 0) {
                isPlaying = false;
                phase = 0.0;
            }
        } else {
            outputBuffer[i] = 0.0f;
        }
    }

    return oboe::DataCallbackResult::Continue;
}
