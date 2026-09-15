#!/usr/bin/env bash
# Calender28 Automated Benchmark Runner (Bash)

set -e

echo "========================================"
echo " ⚡ Calender28 Performance Benchmark Runner"
echo "========================================"

echo ""
echo "[1/2] Running JVM Microbenchmarks..."
./gradlew testDebugUnitTest --tests "com.l1khith.calender28.benchmark.*"
echo "✅ JVM Microbenchmarks Passed!"

echo ""
echo "[2/2] Checking for connected Android devices..."
if command -v adb >/dev/null 2>&1 && adb devices | grep -q "device$"; then
    echo "Connected device found. Running 3 startup benchmark runs..."
    for i in {1..3}; do
        adb shell am force-stop com.l1khith.calender28
        sleep 0.5
        echo "Iteration $i:"
        adb shell am start-activity -W -n com.l1khith.calender28/.MainActivity -c android.intent.category.LAUNCHER -a android.intent.action.MAIN | grep -E "ThisTime|TotalTime|WaitTime"
    done
else
    echo "ℹ️ No physical/emulator Android device connected. Skipping on-device benchmarks."
fi

echo ""
echo "🎉 Benchmark run finished successfully!"
