#ifndef OBOETEST_AUDIOENGINE_H
#define OBOETEST_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include <math.h>
#include <atomic>
#include <array>
#include <mutex>

constexpr int MAX_VOICES = 5;

struct ADSR {
    float attackTime = 0.005f;   // 5ms
    float decayTime = 0.02f;     // 20ms
    float sustainLevel = 0.7f;   // 70%
    float releaseTime = 0.05f;   // 50ms
};

struct Voice {
    std::atomic<bool> isActive{false};
    float frequency = 440.0f;
    std::atomic<int32_t> remainingFrames{0};
    int32_t totalFrames = 0;
    double phase = 0.0;
    bool useADSR = false;
    ADSR adsr;
};

class AudioEngine : public oboe::AudioStreamDataCallback {
public:
    AudioEngine();
    ~AudioEngine();

    bool start();
    void stop();
    void playTone(float frequency, float durationSeconds);
    void playToneWithADSR(float frequency, float durationSeconds);

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
