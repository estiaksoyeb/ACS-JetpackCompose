#!/bin/bash
# Universal NDK Patcher for Ubuntu-on-Android (ARM64)
# This script makes official Google NDKs work in proot/chroot environments.

NDK_DIR=$1

if [ -z "$NDK_DIR" ] || [ ! -d "$NDK_DIR" ]; then
    echo "Usage: $0 /path/to/android-ndk-folder"
    exit 1
fi

# Convert to absolute path
NDK_DIR=$(cd "$NDK_DIR" && pwd)
echo "------------------------------------------"
echo "Patching NDK at: $NDK_DIR"
echo "------------------------------------------"

# 1. Patch architecture detection (Map aarch64 to x86_64 folder)
COMMON_SH="$NDK_DIR/build/tools/ndk_bin_common.sh"
if [ -f "$COMMON_SH" ]; then
    sed -i 's/arm64) HOST_ARCH=arm64;;/arm64|aarch64) HOST_ARCH=x86_64;;/' "$COMMON_SH"
    echo "[✓] Patched ndk_bin_common.sh"
else
    echo "[!] Warning: ndk_bin_common.sh not found. Skipping."
fi

# 2. Mass Symlink Native Tools
BIN_DIR="$NDK_DIR/toolchains/llvm/prebuilt/linux-x86_64/bin"
if [ -d "$BIN_DIR" ]; then
    cd "$BIN_DIR" || exit
    echo "[*] Symlinking LLVM tools..."
    for tool in clang clang++ lld ld.lld ld64.lld llvm-ar llvm-as llvm-nm llvm-objcopy llvm-objdump llvm-ranlib llvm-readelf llvm-strip; do
        if [ -f "$tool" ] || [ -L "$tool" ]; then
            # Backup original if it's not already a symlink
            [ ! -L "$tool" ] && mv "$tool" "${tool}.bak" 2>/dev/null
            
            # Link to system 18 versions
            case $tool in
                clang|clang++) ln -sf /usr/bin/${tool}-18 $tool ;;
                lld|ld.lld|ld64.lld) ln -sf /usr/bin/ld.lld-18 $tool ;;
                *) ln -sf /usr/bin/${tool}-18 $tool ;;
            esac
        fi
    done
    
    # Special case for clang-18 (some NDKs use versioned names)
    ln -sf /usr/bin/clang-18 clang-18 2>/dev/null
    echo "[✓] Symlinked compiler tools"
else
    echo "[!] Error: LLVM bin directory not found!"
fi

# 3. Symlink Make
MAKE_BIN="$NDK_DIR/prebuilt/linux-x86_64/bin/make"
if [ -d "$(dirname "$MAKE_BIN")" ]; then
    [ -f "$MAKE_BIN" ] && [ ! -L "$MAKE_BIN" ] && mv "$MAKE_BIN" "${MAKE_BIN}.bak"
    ln -sf /usr/bin/make "$MAKE_BIN"
    echo "[✓] Symlinked make"
fi

# 4. Fix ARM64 Linker Libraries (API 24 example)
# We find the library directory for aarch64
LIB_DIR=$(find "$NDK_DIR" -type d -name "24" | grep "aarch64-linux-android" | head -n 1)

if [ -n "$LIB_DIR" ]; then
    echo "[*] Fixing libraries in $LIB_DIR"
    
    # Find the actual library files in the NDK
    BUILTINS=$(find "$NDK_DIR" -name "libclang_rt.builtins-aarch64-android.a" | head -n 1)
    UNWIND=$(find "$NDK_DIR" -name "libunwind.a" | grep "aarch64" | head -n 1)
    ATOMIC=$(find "$NDK_DIR" -name "libatomic.a" | grep "aarch64" | head -n 1)

    [ -n "$BUILTINS" ] && ln -sf "$BUILTINS" "$LIB_DIR/"
    [ -n "$UNWIND" ] && ln -sf "$UNWIND" "$LIB_DIR/"
    [ -n "$ATOMIC" ] && ln -sf "$ATOMIC" "$LIB_DIR/"
    
    # Create the critical libgcc.a script
    echo "INPUT(libclang_rt.builtins-aarch64-android.a libunwind.a)" > "$LIB_DIR/libgcc.a"
    echo "[✓] Fixed ARM64 linker libraries and created libgcc.a script"
else
    echo "[!] Warning: Could not find aarch64 library directory for API 24."
fi

echo "------------------------------------------"
echo "Patching Complete!"
echo "------------------------------------------"
