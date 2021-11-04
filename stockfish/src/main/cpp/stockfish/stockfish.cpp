/*
 * Using code from the Stockfish main.cpp source file.
 */
#include <jni.h>
#include <string>
#include <chrono>
#include <thread>
#include "bitboard.h"
#include "endgame.h"
#include "position.h"
#include "psqt.h"
#include "search.h"
#include "syzygy/tbprobe.h"
#include "thread.h"
#include "tt.h"
#include "uci.h"

#include "sharedioqueues.h"

loloof64::LockedStringQueue inputs;
loloof64::LockedStringQueue outputs;

using namespace Stockfish;

void mainLoopProcess() {
    outputs.push(engine_info());

    UCI::init(Options);
    Tune::init();
    PSQT::init();
    Bitboards::init();
    Position::init();
    Bitbases::init();
    Endgames::init();
    Threads.set(size_t(Options["Threads"]));
    Search::clear(); // After threads are up
    Eval::NNUE::init();

    UCI::loop();

    Threads.set(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_loloof64_stockfish_StockfishLib_mainLoop(JNIEnv * /*env*/, jobject /*thisz*/) {
    std::thread loopThread(mainLoopProcess);
    loopThread.join();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_loloof64_stockfish_StockfishLib_readNextOutput(JNIEnv *env, jobject /*thisz*/) {
    std::string result;

    if (outputs.empty()) {
        result = "@@@Empty output@@@";
    }
    else {
        result = outputs.pullNext();
    }

    return env->NewStringUTF(result.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_loloof64_stockfish_StockfishLib_sendCommand(JNIEnv *env, jobject /*thisz*/, jstring command) {
    jboolean isCopy;
    const char * str = env->GetStringUTFChars(command, &isCopy);
    auto newInput = std::string(str);

    inputs.push(newInput);

    env->ReleaseStringUTFChars(command, str);
}