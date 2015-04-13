package org.robolectric.benchmark;

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
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.ShadowExtractor;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Micro {
  ValueReturner returner;
  ValueReturnerShadow shadow;
  int x = 42;
  volatile static int staticX = 1337;

  public static void main(String[] args) throws Exception {
    JUnitCore.runClasses(Bootstrap.class);
  }

  @Setup
  public void setup() {
    returner = new ValueReturner(this);
    shadow = (ValueReturnerShadow) ShadowExtractor.extract(returner);
  }

  @Benchmark
  public Object regularObjectNew() {
    return new UninstrumentedValueReturner(this);
  }

  public static class UninstrumentedValueReturner {
    private Micro data;

    public UninstrumentedValueReturner(Micro data) {
      this.data = data;
    }
  }

  @Instrument
  public static class ValueReturner {
    private Micro data;

    public ValueReturner(Micro data) {
      throw new IllegalStateException();
    }

    public static int getStaticValue() {
      throw new UnsupportedOperationException();
    }

    public static int getStaticValueUnshadowed() {
      return Micro.staticX;
    }

    public int getValue() {
      throw new UnsupportedOperationException();
    }

    public int getValueUnshadowed() {
      return data.x;
    }
  }

  @Implements(ValueReturner.class)
  public static class ValueReturnerShadow {
    @RealObject ValueReturner real;
    private Micro data;

    public void __constructor__(Micro data) {
      this.data = data;
      real.data = data;
    }

    @Implementation
    public int getValue() {
      return data.x;
    }

    @Implementation
    public static int getStaticValue() {
      return Micro.staticX;
    }
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(manifest = Config.NONE, shadows = { ValueReturnerShadow.class })
  public static class Bootstrap {
    @Test
    public void bootstrap() throws RunnerException {
      new Runner(new OptionsBuilder().include(Micro.class.getCanonicalName())
          // We can't fork because we are inside the Robolectric
          // environment
          .forks(0)
          .build()).run();
    }
  }
}
