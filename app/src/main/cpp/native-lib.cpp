#include <jni.h>
#include <string>
#include <oboe/Oboe.h>
#include "AudioEngine.h"

static AudioEngine* audioEngine = nullptr;

extern "C" JNIEXPORT jstring JNICALL
Java_com_github_adzumag_oboetest_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Oboe library loaded successfully!";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_github_adzumag_oboetest_MainActivity_startAudioEngine(
        JNIEnv* env,
        jobject /* this */) {
    if (audioEngine == nullptr) {
        audioEngine = new AudioEngine();
    }
    return audioEngine->start();
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_adzumag_oboetest_MainActivity_stopAudioEngine(
        JNIEnv* env,
        jobject /* this */) {
    if (audioEngine != nullptr) {
        audioEngine->stop();
        delete audioEngine;
        audioEngine = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_adzumag_oboetest_MainActivity_playTone(
        JNIEnv* env,
        jobject /* this */,
        jfloat frequency,
        jfloat duration) {
    if (audioEngine != nullptr) {
        audioEngine->playTone(frequency, duration);
    }
}
