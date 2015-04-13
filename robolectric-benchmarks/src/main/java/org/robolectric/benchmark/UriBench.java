package org.robolectric.benchmark;

import android.net.Uri;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class UriBench {
  @Benchmark
  public Result robolectric() {
    return JUnitCore.runClasses(AndroidUriTest.class);
  }

  @Benchmark
  public Result freestanding() {
    return JUnitCore.runClasses(FreestandingUriTest.class);
  }

  public static void main(String[] args) throws Exception {
    JUnitCore.runClasses(Bootstrap.class);
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(manifest = Config.NONE, shadows = UriShadow.class)
  public static class Bootstrap {
    @Test
    public void bootstrap() throws RunnerException {
      new Runner(new OptionsBuilder()
          .include(UriBench.class.getSimpleName())
          // We can't fork because we are inside the Robolectric
          // environment
          .forks(0)
          .warmupIterations(20)
          .build()).run();
    }
  }

  @Implements(Uri.class)
  public static class UriShadow {

  }
}
