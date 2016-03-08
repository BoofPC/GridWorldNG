package info.gridworld.actor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import info.gridworld.grid.Location;
import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {
  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
  public class Either<L, R> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Getter private final Object value;
    @Getter private final boolean right;

    public static <L, R> Either<L, R> right(R right) {
      return Either.of(right, true);
    }

    public static <L, R> Either<L, R> left(L left) {
      return Either.of(left, false);
    }

    public boolean isLeft() {
      return !right;
    }

    @SuppressWarnings("unchecked")
    public R getRightValue() {
      return right ? (R) value : null;
    }

    @SuppressWarnings("unchecked")
    public L getLeftValue() {
      return right ? null : (L) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T either(Function<L, T> ifLeft, Function<R, T> ifRight) {
      if (right) {
        return ifRight.apply((R) value);
      } else {
        return ifLeft.apply((L) value);
      }
    }

    @SuppressWarnings("unchecked")
    public <T> void either(Consumer<L> ifLeft, Consumer<R> ifRight) {
      if (right) {
        ifRight.accept((R) value);
      } else {
        ifLeft.accept((L) value);
      }
    }
  }
  @Data
  public class PipeStream {
    private final Pipe pipe;
    private final InputStream in;
    private final OutputStream out;

    public PipeStream() {
      Pipe pipe_ = null;
      try {
        pipe_ = Pipe.open();
      } catch (IOException e) {
        e.printStackTrace();
      }
      pipe = pipe_;
      in = Channels.newInputStream(pipe.source());
      out = Channels.newOutputStream(pipe.sink());
    }
  }

  public Stream<Actor> actorsInRadius(final Shell that,
    final double shoutRange) {
    val stream = Stream.<Actor>builder();
    val grid = that.getGrid();
    val maxRow = grid.getNumRows() - 1;
    val maxCol = grid.getNumCols() - 1;
    val myLoc = that.getLocation();
    val myRow = myLoc.getRow();
    val myCol = myLoc.getCol();
    val startRow = (int) Math.max(myRow - shoutRange, 0);
    val endRow = (int) Math.min(myRow + shoutRange,
      (maxRow == -1) ? Integer.MAX_VALUE : maxRow);
    // wheeee, square radii
    for (int row = startRow; row < endRow; row++) {
      val startCol = (int) Math.max(myCol - shoutRange, 0);
      val endCol = (int) Math.min(myCol + shoutRange,
        (maxCol == -1) ? Integer.MAX_VALUE : maxCol);
      for (int col = Math.max(startCol, 0); col < endCol; col++) {
        if (row == myRow && col == myCol) {
          continue;
        }
        val loc = new Location(row, col);
        val actor = grid.get(loc);
        if (actor == null) {
          continue;
        }
        stream.add(actor);
      }
    }
    return stream.build();
  }

  @UtilityClass
  public class Pairs {
    public <A> Pair<A, A> dup(A a) {
      return new Pair<>(a, a);
    }

    /**
     * Apply a <code>BiFunction</code> using a <code>Pair</code>'s values as arguments.
     * 
     * @param p the values to be used
     * @param fun the function to apply
     * @return the application of <code>xs</code> to <code>fun</code>
     */
    public <A, B, C> C apply(Pair<A, B> p, BiFunction<A, B, C> fun) {
      return fun.apply(p.getKey(), p.getValue());
    }

    /**
     * Thread a <code>Pair</code> of <code>Function</code>s
     * 
     * @param p
     * @param keyFun
     * @param valFun
     * @return
     */
    public <A, B> Pair<B, B> thread(Pair<A, A> p, Function<A, B> fun) {
      return thread(p, Pairs.dup(fun));
    }

    public <A, B, C, D> Pair<B, D> thread(Pair<A, C> p,
      Pair<Function<A, B>, Function<C, D>> funs) {
      return thread(funs, p,
        new Pair<BiFunction<Function<A, B>, A, B>, BiFunction<Function<C, D>, C, D>>(
          Function::apply, Function::apply));
    }

    public <A, B, C> Pair<C, C> thread(Pair<A, A> p, Pair<B, B> q,
      BiFunction<A, B, C> fun) {
      return thread(p, q, Pairs.dup(fun));
    }

    public <A, B, C, D, E, F> Pair<C, F> thread(Pair<A, D> p, Pair<B, E> q,
      Pair<BiFunction<A, B, C>, BiFunction<D, E, F>> funs) {
      return new Pair<>(funs.getKey().apply(p.getKey(), q.getKey()),
        funs.getValue().apply(p.getValue(), q.getValue()));
    }

    public <A, B, C> C applyNullable(Pair<A, B> p, BiFunction<A, B, C> fun) {
      return Util.applyNullable(p.getKey(), p.getValue(), fun);
    }

    public <A, B, C> C applyNullableOrDefault(Pair<A, B> p,
      BiFunction<A, B, C> fun, C defaultValue) {
      return Util.applyNullableOrDefault(p.getKey(), p.getValue(), fun,
        defaultValue);
    }

    public <A, B> Pair<A, B> liftNull(Pair<A, B> p) {
      return Util.applyNullable(p.getKey(), p.getValue(), Pair<A, B>::new);
    }

    public <A, B> Pair<A, B> liftNullOrDefault(Pair<A, B> p,
      Pair<A, B> defaultValue) {
      return Util.applyNullableOrDefault(p.getKey(), p.getValue(),
        Pair<A, B>::new, defaultValue);
    }
  }

  public <A> A orElse(A value, A defaultValue) {
    return value == null ? defaultValue : value;
  }

  public <A, B> B applyNullable(A a, Function<A, B> fun) {
    return a == null ? null : fun.apply(a);
  }

  public <A, B> B applyNullableOrDefault(A a, Function<A, B> fun,
    B defaultValue) {
    return a == null ? defaultValue : fun.apply(a);
  }

  public <A, B, C> C applyNullable(A a, B b, BiFunction<A, B, C> fun) {
    return (a == null || b == null) ? null : fun.apply(a, b);
  }

  public <A, B, C> C applyNullableOrDefault(A a, B b, BiFunction<A, B, C> fun,
    C defaultValue) {
    return (a == null || b == null) ? defaultValue : fun.apply(a, b);
  }

  public Pair<Double, Double> rectToPolar(double x, double y) {
    return new Pair<>(Math.hypot(x, y), Math.atan2(y, x));
  }

  /**
   * Converts a polar coordinate into a rectangular coordinate.
   * 
   * @param r Radius
   * @param theta Angle in radians
   * @return Pair of x, y in rectangular form
   */
  public Pair<Double, Double> polarToRect(double r, double theta) {
    return Pairs.thread(new Pair<>(Math.cos(theta), Math.sin(theta)),
      x -> r * x);
  }

  public double normalizeRadians(double theta) {
    return (theta + Math.PI) % (2 * Math.PI) - Math.PI;
  }

  public double normalizeDegrees(double theta) {
    return (theta + 180) % (360) - 180;
  }

  public Pair<Integer, Integer> locToRectInt(Location loc) {
    return new Pair<>(-loc.getRow(), loc.getCol());
  }

  public Pair<Double, Double> locToRect(Location loc) {
    return new Pair<>((double) -loc.getRow(), (double) loc.getCol());
  }

  public Location rectToLoc(Pair<Double, Double> rect) {
    return new Location(-rect.getKey().intValue(),
      (int) rect.getValue().intValue());
  }
}
