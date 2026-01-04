cmake_minimum_required(VERSION 3.22.1)
project("oboetest")

# Oboeライブラリの追加
set(OBOE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../oboe)
add_subdirectory(${OBOE_DIR} ./oboe)
include_directories(${OBOE_DIR}/include)

# ネイティブライブラリの作成
add_library(oboetest SHARED
    native-lib.cpp
    AudioEngine.cpp
)

# ログライブラリとOboeをリンク
target_link_libraries(oboetest
    oboe
    log
)
