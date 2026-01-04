#ifndef OBOETEST_AUDIOENGINE_H
#define OBOETEST_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include <math.h>
#include <atomic>

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
    std::atomic<bool> isPlaying{false};
    std::atomic<float> currentFrequency{440.0f};
    std::atomic<int32_t> remainingFrames{0};

    double phase = 0.0;
    int32_t sampleRate = 48000;
};

#endif //OBOETEST_AUDIOENGINE_H
