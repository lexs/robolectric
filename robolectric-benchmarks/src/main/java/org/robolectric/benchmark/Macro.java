package org.robolectric.benchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Macro {
  int n = 25;
  int array[] = createArray(100000);
  int x = 24;
  int y = 16;
  int z = 8;
  Richards richards = new Richards();

  public static void main(String[] args) throws Exception {
    JUnitCore.runClasses(Bootstrap.class);
  }

  @Benchmark
  public int richards() {
    // 100k
    return richards.run();
  }

  @Benchmark
  public int[] mergesort() {
    return new MergeSort(array).sort();
  }

  @Benchmark
  public int fib() {
    return new FibCalc(n).get();
  }

  @Benchmark
  public int tak() {
    return Tak.tak(x, y, z);
  }

  private int[] createArray(int size) {
    int[] array = new int[size];

    Random r = new Random(size);
    for (int i = 0; i < size; i++) {
      array[i] = r.nextInt();
    }

    return array;
  }

  @Instrument
  public static class MergeSort {
    private final int[] array;
    private int[] helper;

    public MergeSort(int[] array) {
      this.array = array;
      helper = new int[array.length];
    }

    public int[] sort() {
      mergesort(0, array.length - 1);
      return array;
    }

    private void mergesort(int low, int high) {
      // check if low is smaller then high, if not then the array is sorted
      if (low < high) {
        // Get the index of the element which is in the middle
        int middle = low + (high - low) / 2;
        // Sort the left side of the array
        mergesort(low, middle);
        // Sort the right side of the array
        mergesort(middle + 1, high);
        // Combine them both
        merge(low, middle, high);
      }
    }

    private void merge(int low, int middle, int high) {
      // Copy both parts into the helper array
      System.arraycopy(array, low, helper, low, high + 1 - low);

      int i = low;
      int j = middle + 1;
      int k = low;
      // Copy the smallest values from either the left or the right side back
      // to the original array
      while (i <= middle && j <= high) {
        if (helper[i] <= helper[j]) {
          array[k] = helper[i];
          i++;
        } else {
          array[k] = helper[j];
          j++;
        }
        k++;
      }
      // Copy the rest of the left side of the array into the target array
      while (i <= middle) {
        array[k] = helper[i];
        k++;
        i++;
      }

    }
  }

  @Instrument
  public static class FibCalc {
    int n;

    public FibCalc(int n) {
      throw new IllegalStateException();
    }

    public static int calculate(int n) {
      if (n == 0) return 0;
      if (n == 1) return 1;
      return calculate(n - 1) + calculate(n - 2);
    }

    public int get() {
      if (n == 0) {
        return 0;
      } else if (n == 1) {
        return 1;
      } else {
        return new FibCalc(n - 1).get() + new FibCalc(n - 2).get();
      }
    }
  }

  @Implements(FibCalc.class)
  public static class FibCalcShadow {
    @RealObject
    FibCalc real;

    public void __constructor__(int n) {
      real.n = n;
    }
  }

  @Instrument
  public static class Tak {
    public static int tak(int x, int y, int z) {
      if (y < x) {
        //noinspection SuspiciousNameCombination
        return tak(tak(x - 1, y, z), tak(y - 1, z, x), tak(z - 1, x, y));
      } else {
        return z;
      }
    }
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(manifest = Config.NONE, shadows = { FibCalcShadow.class })
  public static class Bootstrap {
    @Test
    public void bootstrap() throws RunnerException {
      new Runner(new OptionsBuilder().include(Macro.class.getCanonicalName())
          // We can't fork because we are inside the Robolectric
          // environment
          .forks(0)
          .shouldDoGC(true)
          .build()).run();
    }
  }
}
