# OboeTest

*English description follows the Japanese version / 英語の説明は日本語版の後に記載しています*

---

## 日本語

Oboeライブラリを使用した高精度オーディオタイミングを実証するProof of ConceptのAndroidアプリケーションです。

### 概要

このプロジェクトは、リズムベースのオーディオアプリケーションにおけるGoogleのOboeライブラリの技術的実現可能性を検証します。以下の機能を実証しています：

- ✅ **高精度タイミング**（平均誤差±2.31ms、目標±5msを大幅に達成）
- ✅ **ポリフォニック音声**（5音同時再生）
- ✅ **高速テンポ**（BPM 240、16分音符62.5ms間隔）
- ✅ **3連符タイミング**（4分音符3連・8分音符3連）
- ✅ **ADSRエンベロープ**（Attack/Decay/Sustain/Releaseによるクリック軽減）

### 機能

#### V0: ライブラリ読み込み
- Oboeライブラリの初期化確認
- 読み込みステータス表示

#### V1: 単一音テスト
- サイン波による単一音生成
- 周波数制御（440Hz、880Hz）
- 再生時間制御（0.1秒、0.5秒）

#### V2: タイミング精度測定
- BPM 120でのメトロノーム
- 100ビートのタイミング分析
- 統計レポート（平均/最大/最小誤差、成功率）
- 合格/不合格判定の可視化

#### V3: 高速テンポ・同時再生
- BPM 240高速連打テスト（16分音符32回連続）
- 5音和音再生（Cメジャー: C4-E4-G4-C5-E5）

#### V4: 3連符タイミング検証
- 4分音符3連符（BPM 120で333.33ms間隔）
- 8分音符3連符（BPM 120で166.67ms間隔）
- 90ビート3連符タイミング測定
- 独立した結果表示

#### V5: ADSRエンベロープ
- Attack: 5ms、Decay: 20ms、Sustain: 70%、Release: 50ms
- クリック/ポップノイズの軽減
- 通常音とADSR音の比較
- 単音・和音の両対応

### 技術スタック

- **言語:** Kotlin + C++17
- **オーディオライブラリ:** [Oboe](https://github.com/google/oboe)（Googleの低レイテンシオーディオライブラリ）
- **UIフレームワーク:** Jetpack Compose with Material Design 3
- **ビルドシステム:** Gradle 8.x with CMake 3.22.1
- **NDK:** 27.0.12077973
- **Min SDK:** API 30 (Android 11.0)
- **Target SDK:** API 36

### アーキテクチャ

```
app/
├── MainActivity.kt          # UIとコルーチンベースのタイミング制御
├── TimingAnalyzer.kt        # 高精度タイミング測定
└── cpp/
    ├── native-lib.cpp       # JNIブリッジ
    ├── AudioEngine.h        # オーディオエンジンインターフェース
    └── AudioEngine.cpp      # Oboe AudioStreamとボイス管理
```

#### 主要コンポーネント

**AudioEngine (C++)**
- Oboe AudioStream管理
- 5ボイスポリフォニックシステム
- ADSRエンベロープ処理
- リアルタイムオーディオコールバック

**TimingAnalyzer (Kotlin)**
- `System.nanoTime()`によるマイクロ秒精度
- 統計分析（平均、最大、最小誤差）
- 3連符間隔計算
- コルーチンベーススケジューリング

**MainActivity (Kotlin)**
- Jetpack Compose UI
- ライフサイクル対応オーディオエンジン管理
- リアルタイム進捗・結果表示

### パフォーマンス結果

#### タイミング精度 (V2)
- **平均誤差:** 2.31ms（目標: ±5ms）
- **最大誤差:** 4.81ms
- **最小誤差:** 0.00ms
- **成功率:** 100%（全ビートが±5ms以内）

#### 設定
- **パフォーマンスモード:** LowLatency
- **共有モード:** Exclusive
- **オーディオフォーマット:** Float
- **チャンネル数:** Mono
- **サンプルレート:** 48000 Hz（デバイス依存）
- **最大ボイス数:** 5

### ビルド方法

1. リポジトリをクローン:
```bash
git clone <repository-url>
cd OboeTest
```

2. Oboeサブモジュールを初期化:
```bash
git submodule update --init --recursive
```

または手動でOboeをクローン:
```bash
cd app/src/main
git clone https://github.com/google/oboe.git
```

3. Android Studioで開いてビルド:
```bash
./gradlew assembleDebug
```

### 要件

- Android Studio Arctic Fox以降
- Android NDK（SDK Manager経由で自動ダウンロード）
- CMake 3.22.1以降
- API 30以上のAndroidデバイスまたはエミュレータ

### 使用方法

1. Androidデバイスにアプリをインストール
2. V0-V5のセクションを順に確認
3. ボタンをタップして音を再生または測定を実行
4. リアルタイムのタイミング統計を確認

### 技術検証結果

このPoCは、Oboeが以下を達成できることを実証しました：
- 一貫した5ms未満のタイミング精度
- 高速テンポ（BPM 240以上）での安定したパフォーマンス
- 信頼性の高いポリフォニック再生（5ボイス以上）
- ADSRによる自然な音のエンベロープ

これらの結果により、Oboeは正確なタイミングを必要とするリズムベースのオーディオアプリケーションにとって優れた選択肢であることが検証されました。

### ライセンス

MIT License - 詳細はLICENSEファイルを参照

### 参考資料

- [Oboe GitHubリポジトリ](https://github.com/google/oboe)
- [Android低レイテンシオーディオガイド](https://developer.android.com/ndk/guides/audio/audio-latency)
- [Oboeドキュメント](https://google.github.io/oboe/)

### 作成者

adzumag

### 謝辞

- 優れた低レイテンシオーディオライブラリを提供するGoogle Oboeチーム
- 堅牢なネイティブ開発ツールを提供するAndroid NDKチーム

---

## English

A proof-of-concept Android application demonstrating high-precision audio timing using the Oboe library.

### Overview

This project validates the technical feasibility of using Google's Oboe library for rhythm-based audio applications. It demonstrates:

- ✅ **High-precision timing** (±2.31ms average error, well under the ±5ms target)
- ✅ **Polyphonic audio** (5-voice simultaneous playback)
- ✅ **High-speed tempo** (BPM 240, 16th notes at 62.5ms intervals)
- ✅ **Triplet timing** (quarter-note and eighth-note triplets)
- ✅ **ADSR envelope** (Attack/Decay/Sustain/Release for click reduction)

### Features

#### V0: Library Loading
- Confirms Oboe library initialization
- Displays loading status

#### V1: Basic Tone Playback
- Single sine wave tone generation
- Frequency control (440Hz, 880Hz)
- Duration control (0.1s, 0.5s)

#### V2: Timing Precision Measurement
- Metronome at BPM 120
- 100-beat timing analysis
- Statistical reporting (average/max/min error, success rate)
- Pass/fail criteria visualization

#### V3: High-Speed Tempo & Simultaneous Playback
- BPM 240 rapid-fire test (32 consecutive 16th notes)
- 5-voice chord playback (C Major: C4-E4-G4-C5-E5)

#### V4: Triplet Timing Verification
- Quarter-note triplets (333.33ms intervals at BPM 120)
- Eighth-note triplets (166.67ms intervals at BPM 120)
- 90-beat triplet timing measurement
- Independent results display

#### V5: ADSR Envelope
- Attack: 5ms, Decay: 20ms, Sustain: 70%, Release: 50ms
- Click/pop noise reduction
- Side-by-side comparison (normal vs ADSR)
- Single tone and chord support

### Technical Stack

- **Language:** Kotlin + C++17
- **Audio Library:** [Oboe](https://github.com/google/oboe) (Google's low-latency audio library)
- **UI Framework:** Jetpack Compose with Material Design 3
- **Build System:** Gradle 8.x with CMake 3.22.1
- **NDK:** 27.0.12077973
- **Min SDK:** API 30 (Android 11.0)
- **Target SDK:** API 36

### Architecture

```
app/
├── MainActivity.kt          # UI and coroutine-based timing
├── TimingAnalyzer.kt        # High-precision timing measurement
└── cpp/
    ├── native-lib.cpp       # JNI bridge
    ├── AudioEngine.h        # Audio engine interface
    └── AudioEngine.cpp      # Oboe audio stream & voice management
```

#### Key Components

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

### Performance Results

#### Timing Precision (V2)
- **Average Error:** 2.31ms (target: ±5ms)
- **Max Error:** 4.81ms
- **Min Error:** 0.00ms
- **Success Rate:** 100% (all beats within ±5ms)

#### Configuration
- **Performance Mode:** LowLatency
- **Sharing Mode:** Exclusive
- **Audio Format:** Float
- **Channel Count:** Mono
- **Sample Rate:** 48000 Hz (device-dependent)
- **Max Voices:** 5

### Building

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

### Requirements

- Android Studio Arctic Fox or later
- Android NDK (automatically downloaded via SDK Manager)
- CMake 3.22.1 or later
- Android device or emulator with API 30+

### Usage

1. Install the app on an Android device
2. Navigate through V0-V5 sections
3. Tap buttons to play tones or run measurements
4. View real-time timing statistics

### Technical Validation

This PoC demonstrates that Oboe can achieve:
- Sub-5ms timing precision consistently
- Stable performance at high tempos (BPM 240+)
- Reliable polyphonic playback (5+ voices)
- Natural sound envelope with ADSR

These results validate Oboe as an excellent choice for rhythm-based audio applications requiring precise timing.

### License

MIT License - see LICENSE file for details

### References

- [Oboe GitHub Repository](https://github.com/google/oboe)
- [Android Low-Latency Audio Guide](https://developer.android.com/ndk/guides/audio/audio-latency)
- [Oboe Documentation](https://google.github.io/oboe/)

### Author

adzumag

### Acknowledgments

- Google Oboe team for the excellent low-latency audio library
- Android NDK team for robust native development tools
