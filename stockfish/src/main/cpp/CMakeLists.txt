# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("stockfish")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(
        # Sets the name of the library.
        stockfish

        # Sets the library as a static library.
        SHARED

        # Provides a relative path to your source file(s).
        stockfish/benchmark.cpp
        stockfish/bitbase.cpp
        stockfish/bitboard.cpp
        stockfish/endgame.cpp
        stockfish/evaluate.cpp
        stockfish/lockedstringqueue.cpp
        stockfish/material.cpp
        stockfish/misc.cpp
        stockfish/movegen.cpp
        stockfish/movepick.cpp
        stockfish/pawns.cpp
        stockfish/position.cpp
        stockfish/psqt.cpp
        stockfish/search.cpp
        stockfish/stockfish.cpp
        stockfish/thread.cpp
        stockfish/timeman.cpp
        stockfish/tt.cpp
        stockfish/tune.cpp
        stockfish/uci.cpp
        stockfish/ucioption.cpp
        stockfish/nnue/evaluate_nnue.cpp
        stockfish/nnue/features/half_ka_v2_hm.cpp
        stockfish/syzygy/tbprobe.cpp
)

# Directories of header files.
include_directories(
        stockfish/
        stockfish/incbin
        stockfish/nnue
        stockfish/syzygy
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# find_library( # Sets the name of the path variable.
#               log-lib
#
#              # Specifies the name of the NDK library that
#              # you want CMake to locate.
#              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# target_link_libraries( # Specifies the target library.
#                       stockfish
#
#                       # Links the target library to the log library
#                       # included in the NDK.
#                       ${log-lib} )