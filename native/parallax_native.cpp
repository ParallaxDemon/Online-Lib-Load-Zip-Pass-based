#include <jni.h>
#include <algorithm>
#include <dirent.h>
#include <string>
#include <vector>
#include <sys/stat.h>

namespace {
constexpr char KEY[] = "ParallaxPapa";
constexpr char SALT[] = "PX";
const char HEX[] = "0123456789ABCDEF";

std::string fromJava(JNIEnv *env, jstring value) {
    if (!value) return {};
    const char *chars = env->GetStringUTFChars(value, nullptr);
    if (!chars) return {};
    std::string copy(chars);
    env->ReleaseStringUTFChars(value, chars);
    return copy;
}
int hexValue(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return c - 'a' + 10;
    if (c >= 'A' && c <= 'F') return c - 'A' + 10;
    return -1;
}
std::string encode(const std::string &value) {
    std::string raw = std::string(SALT) + value;
    std::string output;
    output.reserve(raw.size() * 2);
    constexpr size_t keyLength = sizeof(KEY) - 1;
    for (size_t i = 0; i < raw.size(); ++i) {
        unsigned char b = static_cast<unsigned char>(raw[i] ^ KEY[i % keyLength]);
        output.push_back(HEX[b >> 4]);
        output.push_back(HEX[b & 15]);
    }
    return output;
}
bool decode(const std::string &hex, std::string &result) {
    if (hex.empty() || hex.size() % 2) return false;
    std::string raw;
    raw.reserve(hex.size() / 2);
    constexpr size_t keyLength = sizeof(KEY) - 1;
    for (size_t i = 0; i < hex.size(); i += 2) {
        int hi = hexValue(hex[i]), lo = hexValue(hex[i + 1]);
        if (hi < 0 || lo < 0) return false;
        raw.push_back(static_cast<char>(((hi << 4) | lo) ^ KEY[(i / 2) % keyLength]));
    }
    if (raw.compare(0, sizeof(SALT) - 1, SALT) != 0) return false;
    result = raw.substr(sizeof(SALT) - 1);
    return true;
}
// Do not traverse symlinks, including symlinked subdirectories.
void discover(const std::string &dir, std::vector<std::string> &files, int depth) {
    if (depth > 24 || files.size() >= 256) return;
    DIR *handle = opendir(dir.c_str());
    if (!handle) return;
    std::vector<std::string> names;
    while (dirent *entry = readdir(handle)) {
        std::string name(entry->d_name);
        if (name != "." && name != "..") names.push_back(name);
    }
    closedir(handle);
    std::sort(names.begin(), names.end());
    for (const auto &name : names) {
        if (files.size() >= 256) break;
        std::string path = dir + "/" + name;
        struct stat info{};
        if (lstat(path.c_str(), &info) != 0) continue;
        if (S_ISDIR(info.st_mode)) discover(path, files, depth + 1);
        else if (S_ISREG(info.st_mode) && name.size() > 3 &&
                 name.compare(name.size() - 3, 3, ".so") == 0) files.push_back(path);
    }
}
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_parallax_papa_bol_ParallaxCrypto_nativeEncrypt(JNIEnv *env, jclass, jstring value) {
    if (!value) return nullptr;
    return env->NewStringUTF(encode(fromJava(env, value)).c_str());
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_Parallax_Papa_ParallaxCrypto_nativeDecrypt(JNIEnv *env, jclass, jstring value) {
    if (!value) return nullptr;
    std::string result;
    if (!decode(fromJava(env, value), result)) return nullptr;
    return env->NewStringUTF(result.c_str());
}
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_Parallax_Papa_ParallaxLoader_nativeCandidates(JNIEnv *env, jclass, jstring directory) {
    std::vector<std::string> files;
    if (directory) discover(fromJava(env, directory), files, 0);
    jclass type = env->FindClass("java/lang/String");
    if (!type) return nullptr;
    jobjectArray array = env->NewObjectArray(static_cast<jsize>(files.size()), type, nullptr);
    for (size_t i = 0; array && i < files.size(); ++i) {
        jstring path = env->NewStringUTF(files[i].c_str());
        if (!path) break;
        env->SetObjectArrayElement(array, static_cast<jsize>(i), path);
        env->DeleteLocalRef(path);
    }
    return array;
}
