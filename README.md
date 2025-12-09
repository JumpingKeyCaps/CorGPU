# CorGPU

A demonstration Android project comparing **classic CPU-bound Kotlin Coroutines** with **GPU-accelerated computation via AGSL shaders**. This project showcases how to orchestrate high-performance calculations using Kotlin Coroutines and leverage the GPU for massively parallel tasks.

---

## Concept

- **CPU-only approach**:  
  Uses standard Kotlin Coroutines (`launch` or `async`) on `Dispatchers.Default` to perform heavy calculations. Limited by CPU threads and sequential dependencies.

- **GPU approach**:  
  Coroutines orchestrate the workflow on the CPU while the GPU executes massively parallel computations via AGSL shaders. Coroutine `async/await` ensures non-blocking execution and synchronization of results.

---

## Technical Details

### Kotlin Coroutines

- **Scope**: defines the lifecycle of coroutines (`viewModelScope`, `lifecycleScope`, custom `CoroutineScope`).
- **Dispatcher**: decides on which thread or thread pool a coroutine executes.
  - `Dispatchers.Default` → CPU cores, good for CPU-bound tasks.
  - `Dispatchers.IO` → extensible pool, optimized for I/O-bound tasks.
  - Custom dispatcher → `newFixedThreadPoolContext(n)`, threads remain alive when idle.
- **Launch vs Async**:
  - `launch { ... }` → fire & forget, useful for I/O or orchestration.
  - `async { ... }` → returns `Deferred<T>`, can be `await()`ed, ideal for parallel computations.
- **Suspend functions**: allow coroutines to **pause without blocking threads**, freeing the dispatcher for other coroutines.

**Coroutine Limitations / Considerations**:
- Threads are a limited resource; creating a custom dispatcher with too many threads increases memory usage and context switching.
- Coroutines themselves are very lightweight (~1KB stack), but scheduling millions on a small dispatcher can still create contention.
- Sequential CPU-bound tasks don’t benefit from multiple threads; parallelism requires independent computations.

---

### GPU / AGSL Compute

- **Shaders are not limited to graphics**:
  - Each “pixel” or thread is independent → can perform arbitrary math.
  - Fully parallelizable tasks (matrix ops, FFT, convolution) see massive speedups.
- **Workflow**:
  1. Coroutine prepares input buffers / uniforms.
  2. Launch shader computation on GPU.
  3. GPU executes thousands/millions of parallel threads.
  4. Results copied back to CPU; coroutine `await()` resumes with data.
- **AGSL Notes**:
  - Operations limited to supported shader instructions (add, multiply, sin, cos, etc.).
  - Memory transfer CPU↔GPU is a cost; amortized by batching large computations.
  - Threads on GPU are **massively parallel but stateless**; no suspend/resume like CPU coroutines.

---

## Architecture Diagram
```
+---------------------+        +----------------------+        +--------------------+
| CPU / ViewModel     | launch | Kotlin Coroutine     | async  | GPU Shader (AGSL)  |
| Coroutine Scope     |------->| Scheduler / Thread   |------->| Compute Parallel   |
| - Orchestrate job   |        | pool                 |        | - Each thread = op|
| - Prepare buffers   |        | - Suspend / resume   |        | - Parallel ops    |
+---------------------+        +----------------------+        +--------------------+
        ^                                                              |
        |                                                              |
        +------------------- await / Flow -----------------------------+
```
---

## Example Usage

**CPU-only**:
```kotlin
viewModelScope.launch(Dispatchers.Default) {
    val start = System.currentTimeMillis()
    val result = multiplyMatricesCpu(matrixA, matrixB)
    val end = System.currentTimeMillis()
    _timeLiveData.value = end - start
    _resultLiveData.value = result
}
```

**Coroutine + GPU**
```kotlin
viewModelScope.launch {
    val start = System.currentTimeMillis()
    val deferred = async { multiplyMatricesGpu(matrixA, matrixB) }
    val result = deferred.await()
    val end = System.currentTimeMillis()
    _timeLiveData.value = end - start
    _resultLiveData.value = result
}
```


---
## Observations

- **CPU-only**:
  - Sequential CPU-bound tasks → limited by number of cores.
  - Tasks that can be parallelized → `async/await` helps.
  - Main Thread stays free if properly launched in `Dispatchers.Default`.

- **GPU + Coroutine**:
  - Massively parallel tasks → huge performance gain.
  - Main Thread remains responsive.
  - Coroutine orchestrates CPU→GPU→CPU efficiently.
  - Best for tasks where operations are independent and can be executed in parallel.

- **Limitations**:
  - Dependent sequential tasks → GPU parallelism offers no speedup.
  - Dispatcher threads are still limited; avoid creating too many threads unnecessarily.
  - GPU memory transfer can be a bottleneck for very small tasks.
  - Not all CPU operations can be directly mapped to GPU; AGSL has limited math instructions.

---

## Requirements

- Android 13+ (AGSL shaders)
- Kotlin 1.9+
- Jetpack Compose (UI)
- Modern GPU on device for meaningful results

---

## Next Steps

- Add adjustable matrix size / task complexity for live performance comparison.
- Compare RenderScript / Vulkan compute for deeper GPU analysis.
- Add real-time profiling (CPU/GPU usage, energy consumption).
- Extend GPU shader operations for more complex numerical tasks (FFT, physics, audio).

---
