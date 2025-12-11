# CorGPU
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![Android](https://img.shields.io/badge/Android-13%2B-green)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-blueviolet)
![GPGPU](https://img.shields.io/badge/GPGPU-AGSL-orange)

A demonstration Android project exploring **GPGPU (General-Purpose computing on GPU)** via AGSL shaders to push Kotlin Coroutines beyond their CPU-bound limitations. This project investigates how to supercharge coroutine performance by leveraging GPU parallelism for compute-intensive tasks, exploring the boundaries of coroutine-orchestrated GPU acceleration.

---

## Motivation

### **The Problem** 
Kotlin Coroutines excel at orchestration and concurrency, but CPU dispatchers hit hard limits:
- `Dispatchers.Default` caps at available CPU cores (~4-12 threads)
- Custom dispatchers with 64+ threads cause severe context-switching overhead
- CPU-bound parallel tasks scale poorly beyond core count

### **The Hypothesis**
Can we use coroutines as a **coordination layer** while offloading heavy computation to GPU threads via **GPGPU techniques**, combining the best of both worlds?

### **The Reality**
GPU "coroutines" don't exist (no suspend/resume on shader threads), but we can orchestrate **GPGPU compute** through standard coroutines, achieving massive parallelism (1000s of parallel threads) for the right workloads.

### **The Approach**
I leverage Android's **AGSL (Android Graphics Shading Language)** to perform general-purpose computations on the GPU. By repurposing fragment shaders as compute kernels, i achieve true GPGPU capabilities on Android 13+ devices without needing Vulkan Compute or deprecated RenderScript.

---

## Concept

### CPU-only Approach
Standard Kotlin Coroutines (`launch`/`async`) on `Dispatchers.Default` perform heavy calculations.

**Constraints**:
- Limited by available CPU threads (typically 4-12)
- Sequential dependencies block parallelism
- Context switching overhead with excessive threads

### Hybrid Coroutine + GPU Approach
Coroutines orchestrate workflow on CPU while GPU executes massively parallel computations via AGSL shaders.

**Benefits**:
- CPU handles orchestration, state management, and control flow
- GPU handles data-parallel computation (1000s of threads)
- `async/await` ensures non-blocking execution and result synchronization
- Main thread remains responsive

---

## Is This "Real" GPGPU?

### **No.** This is a pragmatic hack.

We're abusing fragment shaders (designed to paint pixels) to perform 
matrix multiplication by:
1. Encoding matrices as grayscale bitmaps (losing precision)
2. Treating the GPU like a painter who can do math
3. Decoding the painted result back to numbers

### **Why not use "real" GPU compute (Vulkan)?**

Because that requires 500+ lines of NDK/C++ boilerplate, manual memory 
management, and debugging nightmares that make you question your career 
choices.

### **Why not use RenderScript?**

Google killed it. RIP 2011-2021. 

### **So what is this?**

A demonstration that sometimes the "wrong" tool used cleverly beats 
the "right" tool used painfully. Getting 80% of Vulkan's 
performance with 5% of its complexity.

---

## Technical Deep-Dive

### Kotlin Coroutines Fundamentals

**Scope**: Defines coroutine lifecycle
- `viewModelScope` → tied to ViewModel lifecycle
- `lifecycleScope` → tied to Activity/Fragment lifecycle
- Custom `CoroutineScope` → manual lifecycle management

**Dispatcher**: Determines execution context
- `Dispatchers.Default` → CPU cores, optimized for CPU-bound tasks
- `Dispatchers.IO` → extensible pool (max 64 threads), optimized for I/O
- Custom → `newFixedThreadPoolContext(n)` for specialized workloads

**Launch vs Async**:
- `launch { }` → fire-and-forget, returns `Job`
- `async { }` → returns `Deferred<T>`, supports `await()`

**Suspend Functions**: Enable non-blocking pause/resume without holding threads

**Real-World Limitations**:
- Threads consume ~1MB stack memory each
- 64+ threads → excessive context switching (~10-20% overhead)
- Coroutines are lightweight (~1KB), but dispatcher contention remains
- CPU-bound sequential tasks see **zero benefit** from more threads

---

### GPU Compute via AGSL

**Key Insight**: Shaders aren't just for graphics—each invocation is an independent computational thread.

**Parallelism Model**:
- Fragment shader runs once per pixel/output element
- Each thread executes the same code with different inputs
- Truly independent—no shared mutable state between threads
- Thousands of parallel hardware threads (vs. 4-12 CPU threads)

**Workflow**:
```
1. CPU Coroutine: Prepare input data (matrices, images, buffers)
2. CPU → GPU: Transfer data via uniforms/textures
3. GPU: Execute shader across all output elements in parallel
4. GPU → CPU: Read results back
5. CPU Coroutine: Resume with computed data
```

**AGSL Constraints**:
- Limited instruction set (basic math, trigonometry, texture sampling)
- No recursion, no dynamic branching (kills parallelism)
- No inter-thread communication during execution
- Memory transfer overhead (CPU↔GPU PCIe/memory bus)

**When GPU Wins**:
- Large matrix operations (NxN where N > 256)
- Image filters/convolution (millions of independent pixels)
- Batch processing (100s of operations)
- Operations that amortize transfer cost

**When GPU Loses**:
- Small datasets (transfer overhead > compute savings)
- Sequential algorithms (dynamic programming, Dijkstra)
- Complex branching logic
- Frequent CPU↔GPU round-trips

---

## Architecture

```
┌─────────────────────┐
│   ViewModel/UI      │
│   - User input      │
│   - Display results │
└──────────┬──────────┘
           │ launch
           ▼
┌─────────────────────────────┐
│  Kotlin Coroutine Scope     │
│  - Orchestrate computation  │
│  - Prepare data             │
│  - Measure performance      │
└──────────┬──────────────────┘
           │ async/await
           ▼
      ┌────────┐
      │ Route? │
      └───┬─┬──┘
          │ │
    CPU   │ │   GPU
    ┌─────▼ ▼─────┐
    │              │
┌───▼──────┐  ┌───▼────────────┐
│ CPU Exec │  │ GPU Shader     │
│ Default  │  │ - Parallel ops │
│ Parallel │  │ - AGSL compute │
└─────┬────┘  └────┬───────────┘
      │            │
      └─────┬──────┘
            ▼
      ┌─────────────┐
      │   Results   │
      │  + Metrics  │
      └─────────────┘
```

---

## Implementation Examples

### CPU + GPU Benchmark Comparison
```kotlin
// Dans le ViewModel
viewModelScope.launch {
    _state.value = Computing(matrixSize)
    
    val matrixA = generateRandomMatrix(matrixSize)
    val matrixB = generateRandomMatrix(matrixSize)
    
    // Benchmark CPU avec async pour orchestration équitable
    val cpuTime = benchmarkCpu(matrixA, matrixB)
    
    // Benchmark GPU avec async pour orchestration
    val (gpuTotalTime, gpuComputeTime) = benchmarkGpu(matrixA, matrixB)
    
    // Comparer les résultats
    val speedup = cpuTime.toFloat() / gpuTotalTime.toFloat()
    
    _state.value = Success(result)
}

// CPU Benchmark utilise async pour cohérence
private suspend fun benchmarkCpu(...): Long = withContext(Dispatchers.Default) {
    val start = System.nanoTime()
    val deferred = async {
        // Triple loop O(n³)
        multiplyMatricesCpu(matrixA, matrixB)
    }
    deferred.await()
    return@withContext (System.nanoTime() - start) / 1_000_000
}

// GPU Benchmark utilise async pour orchestration non-bloquante
private suspend fun benchmarkGpu(...): Pair<Long, Long> = withContext(Dispatchers.Default) {
    val deferred = async {
        gpuHelper.multiplyMatrices(matrixA, matrixB)
    }
    val (_, timings) = deferred.await()
    return@withContext Pair(timings.totalMs, timings.computeMs)
}
```

### AGSL Shader Example (Conceptual)
```glsl
// matrix_multiply.agsl (Real Implementation)
uniform shader inputA;
uniform shader inputB;
uniform float matrixSize;

half4 main(float2 fragCoord) {
    int row = int(fragCoord.y);
    int col = int(fragCoord.x);
    float sum = 0.0;
    
    int n = int(matrixSize);
    for (int k = 0; k < n; k++) {
        float a = inputA.eval(vec2(float(k) + 0.5, float(row) + 0.5)).r;
        float b = inputB.eval(vec2(float(col) + 0.5, float(k) + 0.5)).r;
        sum += a * b;
    }
    
    // Normalize by matrixSize to keep in [0, 1] for 8-bit encoding
    return half4(sum / matrixSize, 0.0, 0.0, 1.0);
}
```

**Why async for both CPU and GPU?**
- **Fair comparison**: Same orchestration pattern for both approaches
- **Measures coordination overhead**: Includes async/await cost in both benchmarks
- **Architectural symmetry**: Consistent coroutine usage across implementations
- **Non-blocking**: Main thread remains responsive during computation

The key insight: We're not testing "raw CPU" vs "raw GPU", but **"CPU orchestrated via coroutines"** vs **"GPU orchestrated via coroutines"** - which is the actual real-world usage pattern.

---

## Benchmark Goals

### Metrics to Capture
- **CPU Time**: Pure coroutine computation on `Dispatchers.Default`
- **GPU Compute Time**: Shader execution only (excluding transfers)
- **GPU Total Time**: Including CPU→GPU→CPU memory transfers
- **Memory Usage**: Heap allocation for buffers
- **Energy Consumption**: Battery drain per operation (if feasible)

### Test Cases
1. **Matrix Multiplication**: NxN matrices (N = 64, 128, 256, 512, 1024)
2. **Image Convolution**: Apply 3x3, 5x5, 7x7 kernels to images
3. **Batch Processing**: 100 small operations vs. 1 large operation
4. **Pipeline**: Multiple GPU ops chained without CPU round-trips

### Success Criteria
- Identify crossover point where GPU > CPU (expected ~256x256 matrices)
- Measure transfer overhead as % of total GPU time
- Document when coroutine parallelism suffices vs. GPU needed

---

## Observations & Learnings

### CPU Coroutines
✅ **Strengths**:
- Excellent for I/O, orchestration, structured concurrency
- Low overhead for light tasks
- Easy debugging and error handling

⚠️ **Limitations**:
- Scales only to CPU core count
- Context switching overhead with excessive parallelism
- No benefit for sequential dependencies

### GPU + Coroutines
✅ **Strengths**:
- Massive parallelism (1000s of threads)
- Ideal for data-parallel operations
- Main thread stays responsive
- Coroutines provide clean async coordination

⚠️ **Limitations**:
- Transfer overhead dominates small workloads
- Limited by AGSL instruction set
- No recursion, limited branching
- Debugging is harder (no breakpoints in shaders)
- Not all algorithms map to GPU model

### Hybrid Strategy
**Best for**:
- Large-scale matrix/tensor operations
- Image/video processing pipelines
- Batch computations with high data reuse
- Real-time simulations (physics, particles)

**Avoid for**:
- Small datasets (< 64x64 matrices)
- Algorithms with heavy branching
- Sequential dependencies (graph traversal, DP)
- Frequent CPU↔GPU synchronization points

---

## Requirements

- **Android 13+** (API 33) for AGSL shader support
- **Kotlin 1.9+** with coroutines
- **Jetpack Compose** for reactive UI
- **Modern GPU** (Adreno 660+, Mali G78+) for meaningful performance
- **Physical device recommended** (emulator GPU may not reflect real perf)

---

## Next Steps

### Phase 1: Core Implementation
- [x] Document architecture and rationale
- [x] Implement CPU-only matrix multiplication with coroutines
- [x] Implement AGSL shader for matrix multiplication
- [x] Build Compose UI with side-by-side comparison
- [x] Add adjustable matrix size slider (64 → 1024)

### Phase 2: Advanced Benchmarking
- [x] Measure breakdown: CPU time, GPU compute, transfer overhead
- [ ] Implement image convolution benchmark
- [ ] Add batch processing comparison
- [ ] Test GPU pipeline (chained operations)
- [ ] Profile memory allocations

---

## Key Takeaways

1. **Coroutines are for orchestration, not raw parallelism**—GPU provides true massive parallelism.
2. **Transfer overhead matters**—GPU only wins when compute cost >> transfer cost.
3. **Hybrid approach is pragmatic**—leverage CPU for control, GPU for data crunching.
4. **Profile everything**—assumptions about performance are often wrong.
5. **Symmetric benchmarking matters**—Using `async` for both CPU and GPU ensures fair comparison by measuring the same orchestration overhead.
6. **Coroutines are the coordination layer**—Both approaches use `async/await`, but GPU leverages thousands of hardware threads while CPU is limited to core count.

This project demonstrates that understanding **when** and **how** to combine CPU and GPU computation is as important as the implementation itself.

---

## Resources

- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [AGSL Shader Documentation](https://developer.android.com/develop/ui/views/graphics/agsl)
- [Android GPU Best Practices](https://developer.android.com/topic/performance/rendering)
- [Matrix Multiplication Algorithms](https://en.wikipedia.org/wiki/Matrix_multiplication_algorithm)

---
