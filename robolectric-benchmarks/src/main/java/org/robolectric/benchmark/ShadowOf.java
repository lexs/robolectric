package org.robolectric.benchmark;

import android.app.Application;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
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
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ShadowOf {
  Application application = RuntimeEnvironment.application;

  @Benchmark
  public Object shadowOf() {
    return ShadowExtractor.extract(application);
  }

  public static void main(String[] args) throws Exception {
    JUnitCore.runClasses(Bootstrap.class);
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(manifest = Config.NONE)
  public static class Bootstrap {
    @Test
    public void bootstrap() throws RunnerException {
      new Runner(new OptionsBuilder()
          .include(ShadowOf.class.getSimpleName())
          // We can't fork because we are inside the Robolectric
          // environment
          .forks(0)
          .warmupIterations(40)
          .build()).run();
    }
  }
}
