# OboeTest

A proof-of-concept Android application demonstrating high-precision audio timing using the Oboe library.

## Overview

This project validates the technical feasibility of using Google's Oboe library for rhythm-based audio applications. It demonstrates:

- ✅ **High-precision timing** (±2.31ms average error, well under the ±5ms target)
- ✅ **Polyphonic audio** (5-voice simultaneous playback)
- ✅ **High-speed tempo** (BPM 240, 16th notes at 62.5ms intervals)
- ✅ **Triplet timing** (quarter-note and eighth-note triplets)
- ✅ **ADSR envelope** (Attack/Decay/Sustain/Release for click reduction)

## Features

### V0: Library Loading
- Confirms Oboe library initialization
- Displays loading status

### V1: Basic Tone Playback
- Single sine wave tone generation
- Frequency control (440Hz, 880Hz)
- Duration control (0.1s, 0.5s)

### V2: Timing Precision Measurement
- Metronome at BPM 120
- 100-beat timing analysis
- Statistical reporting (average/max/min error, success rate)
- Pass/fail criteria visualization

### V3: High-Speed Tempo & Simultaneous Playback
- BPM 240 rapid-fire test (32 consecutive 16th notes)
- 5-voice chord playback (C Major: C4-E4-G4-C5-E5)

### V4: Triplet Timing Verification
- Quarter-note triplets (333.33ms intervals at BPM 120)
- Eighth-note triplets (166.67ms intervals at BPM 120)
- 90-beat triplet timing measurement
- Independent results display

### V5: ADSR Envelope
- Attack: 5ms, Decay: 20ms, Sustain: 70%, Release: 50ms
- Click/pop noise reduction
- Side-by-side comparison (normal vs ADSR)
- Single tone and chord support

## Technical Stack

- **Language:** Kotlin + C++17
- **Audio Library:** [Oboe](https://github.com/google/oboe) (Google's low-latency audio library)
- **UI Framework:** Jetpack Compose with Material Design 3
- **Build System:** Gradle 8.x with CMake 3.22.1
- **NDK:** 27.0.12077973
- **Min SDK:** API 30 (Android 11.0)
- **Target SDK:** API 36

## Architecture

```
app/
├── MainActivity.kt          # UI and coroutine-based timing
├── TimingAnalyzer.kt        # High-precision timing measurement
└── cpp/
    ├── native-lib.cpp       # JNI bridge
    ├── AudioEngine.h        # Audio engine interface
    └── AudioEngine.cpp      # Oboe audio stream & voice management
```

### Key Components

**AudioEngine (C++)**
- Oboe AudioStream management
- 5-voice polyphonic system
- ADSR envelope processing
- Real-time audio callback

**TimingAnalyzer (Kotlin)**
- `System.nanoTime()` for microsecond precision
- Statistical analysis (average, max, min error)
- Triplet interval calculation
- Coroutine-based scheduling

**MainActivity (Kotlin)**
- Jetpack Compose UI
- Lifecycle-aware audio engine management
- Real-time progress and result display

## Performance Results

### Timing Precision (V2)
- **Average Error:** 2.31ms (target: ±5ms)
- **Max Error:** 4.81ms
- **Min Error:** 0.00ms
- **Success Rate:** 100% (all beats within ±5ms)

### Configuration
- **Performance Mode:** LowLatency
- **Sharing Mode:** Exclusive
- **Audio Format:** Float
- **Channel Count:** Mono
- **Sample Rate:** 48000 Hz (device-dependent)
- **Max Voices:** 5

## Building

1. Clone the repository:
```bash
git clone <repository-url>
cd OboeTest
```

2. Initialize Oboe submodule:
```bash
git submodule update --init --recursive
```

Or manually clone Oboe:
```bash
cd app/src/main
git clone https://github.com/google/oboe.git
```

3. Open in Android Studio and build:
```bash
./gradlew assembleDebug
```

## Requirements

- Android Studio Arctic Fox or later
- Android NDK (automatically downloaded via SDK Manager)
- CMake 3.22.1 or later
- Android device or emulator with API 30+

## Usage

1. Install the app on an Android device
2. Navigate through V0-V5 sections
3. Tap buttons to play tones or run measurements
4. View real-time timing statistics

## Technical Validation

This PoC demonstrates that Oboe can achieve:
- Sub-5ms timing precision consistently
- Stable performance at high tempos (BPM 240+)
- Reliable polyphonic playback (5+ voices)
- Natural sound envelope with ADSR

These results validate Oboe as an excellent choice for rhythm-based audio applications requiring precise timing.

## License

MIT License - see LICENSE file for details

## References

- [Oboe GitHub Repository](https://github.com/google/oboe)
- [Android Low-Latency Audio Guide](https://developer.android.com/ndk/guides/audio/audio-latency)
- [Oboe Documentation](https://google.github.io/oboe/)

## Author

adzumag

## Acknowledgments

- Google Oboe team for the excellent low-latency audio library
- Android NDK team for robust native development tools
