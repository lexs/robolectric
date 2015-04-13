package org.robolectric.benchmark;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Startup {
  @State(Scope.Benchmark)
  public static class CleanFiles {
    @Setup
    public void setup() throws IOException {
      clearDirectory(Paths.get(System.getProperty("java.io.tmpdir")));
    }
  }

  @State(Scope.Benchmark)
  public static class TempFiles {
    @Param({"0", "10000", "20000", "30000", "40000", "50000", "60000"})
    int numFiles;

    @Setup
    public void setup() throws IOException {
      Path temp = Paths.get(System.getProperty("java.io.tmpdir"));
      int count = getNumFiles(temp);
      if (count > numFiles) clearDirectory(temp);
      for (int i = count; i < numFiles; i++) {
        Files.createTempDirectory("robolectric");
      }
    }

    private int getNumFiles(Path path) throws IOException {
      int count = 0;
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
        for (Path p : directoryStream) {
          count++;
        }
      }

      return count;
    }
  }

  @Benchmark
  public Result startupBaseline() {
    return JUnitCore.runClasses(JUnitTest.class);
  }

  @Benchmark
  @Warmup(iterations = 30)
  public Result startupOverheadFiles(TempFiles files) {
    return JUnitCore.runClasses(RoboTest.class);
  }

  public static void main(String[] args) throws Exception {
    Path temp = Files.createTempDirectory("robolectric-bench");
    System.out.println("Using temp directory: " + temp);
    if (!Files.exists(temp)) {
      Files.createDirectory(temp);
    } else {
      clearDirectory(temp);
    }

    new Runner(new OptionsBuilder().include(Startup.class.getSimpleName())
        .forks(1)
        .jvmArgsAppend("-Djava.io.tmpdir=" + temp)
        .build()).run();

    clearDirectory(temp);
  }

  private static void clearDirectory(final Path directory) throws IOException {
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (!dir.equals(directory)) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @RunWith(RobolectricTestRunner.class) @Config(manifest = Config.NONE)
  public static class RoboTest {
    @Test
    public void testInit() {
      Blackhole.consumeCPU(20);
    }
  }

  public static class JUnitTest {
    @Test
    public void testInit() {
      Blackhole.consumeCPU(20);
    }
  }
}
