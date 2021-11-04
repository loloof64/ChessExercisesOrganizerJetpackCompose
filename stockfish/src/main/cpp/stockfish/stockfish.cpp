/*
 * Using code from the Stockfish main.cpp source file.
 */
#include <jni.h>
#include <string>
#include <chrono>
#include <thread>
#include "lockedstringqueue.h"

loloof64::LockedStringQueue inputs;
loloof64::LockedStringQueue outputs;

void mainLoopProcess() {
    while (true) {
        if (!inputs.empty()) {
            auto nextInput = inputs.pullNext();
            if (nextInput == "exit") {
                break;
            }

            auto nextOutput = std::string("Got input from outside: [");
            nextOutput += nextInput;
            nextOutput += "]";

            outputs.push(nextOutput);
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(200));
    }
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