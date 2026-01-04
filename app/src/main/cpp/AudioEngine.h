#ifndef OBOETEST_AUDIOENGINE_H
#define OBOETEST_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include <math.h>
#include <atomic>
#include <array>
#include <mutex>

constexpr int MAX_VOICES = 5;

struct Voice {
    std::atomic<bool> isActive{false};
    float frequency = 440.0f;
    std::atomic<int32_t> remainingFrames{0};
    double phase = 0.0;
};

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine();
    ~AudioEngine();

    bool start();
    void stop();
    void playTone(float frequency, float durationSeconds);

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames) override;

private:
    oboe::AudioStream *stream = nullptr;
    std::array<Voice, MAX_VOICES> voices;
    std::mutex voicesMutex;
    int32_t sampleRate = 48000;

    int findAvailableVoice();
};

#endif //OBOETEST_AUDIOENGINE_H
