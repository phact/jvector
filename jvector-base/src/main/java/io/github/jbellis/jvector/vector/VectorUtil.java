/*
 * All changes to the original code are Copyright DataStax, Inc.
 *
 * Please see the included license file for details.
 */

/*
 * Original license:
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jbellis.jvector.vector;

import java.util.List;

/** Utilities for computations with numeric arrays */
public final class VectorUtil {

  private static final VectorUtilSupport impl =
      VectorizationProvider.getInstance().getVectorUtilSupport();

  private VectorUtil() {}

  /**
   * Returns the vector dot product of the two vectors.
   *
   * @throws IllegalArgumentException if the vectors' dimensions differ.
   */
  public static float dotProduct(float[] a, float[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("vector dimensions differ: " + a.length + "!=" + b.length);
    }
    float r = impl.dotProduct(a, b);
    assert Float.isFinite(r);
    return r;
  }

  public static float dotProduct(float[] a, int aoffset, float[] b, int boffset, int length) {
    //This check impacts FLOPS
    /*if ( length > Math.min(a.length - aoffset, b.length - boffset) ) {
      throw new IllegalArgumentException("length must be less than the vectors remaining space at the given offsets: a(" +
              (a.length - aoffset) + "), b(" + (b.length - boffset) + "), length(" + length + ")");
    }*/
    float r = impl.dotProduct(a, aoffset, b, boffset, length);
    assert Float.isFinite(r);
    return r;
  }

  /**
   * Returns the cosine similarity between the two vectors.
   *
   * @throws IllegalArgumentException if the vectors' dimensions differ.
   */
  public static float cosine(float[] a, float[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("vector dimensions differ: " + a.length + "!=" + b.length);
    }
    float r = impl.cosine(a, b);
    assert Float.isFinite(r);
    return r;
  }

  /** Returns the cosine similarity between the two vectors. */
  public static float cosine(byte[] a, byte[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("vector dimensions differ: " + a.length + "!=" + b.length);
    }
    return impl.cosine(a, b);
  }

  /**
   * Returns the sum of squared differences of the two vectors.
   *
   * @throws IllegalArgumentException if the vectors' dimensions differ.
   */
  public static float squareDistance(float[] a, float[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("vector dimensions differ: " + a.length + "!=" + b.length);
    }
    float r = impl.squareDistance(a, b);
    assert Float.isFinite(r);
    return r;
  }

  /**
   * Returns the sum of squared differences of the two vectors, or subvectors, of the given length.
   */
  public static float squareDistance(float[] a, int aoffset, float[] b, int boffset, int length) {
    float r = impl.squareDistance(a, aoffset, b, boffset, length);
    assert Float.isFinite(r);
    return r;
  }

  /** Returns the sum of squared differences of the two vectors. */
  public static int squareDistance(byte[] a, byte[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("vector dimensions differ: " + a.length + "!=" + b.length);
    }
    return impl.squareDistance(a, b);
  }

  /**
   * Modifies the argument to be unit length, dividing by its l2-norm. IllegalArgumentException is
   * thrown for zero vectors.
   *
   * @return the input array after normalization
   */
  public static float[] l2normalize(float[] v) {
    l2normalize(v, true);
    return v;
  }

  /**
   * Modifies the argument to be unit length, dividing by its l2-norm.
   *
   * @param v the vector to normalize
   * @param throwOnZero whether to throw an exception when <code>v</code> has all zeros
   * @return the input array after normalization
   * @throws IllegalArgumentException when the vector is all zero and throwOnZero is true
   */
  public static float[] l2normalize(float[] v, boolean throwOnZero) {
    double squareSum = 0.0f;
    int dim = v.length;
    for (float x : v) {
      squareSum += x * x;
    }
    if (squareSum == 0) {
      if (throwOnZero) {
        throw new IllegalArgumentException("Cannot normalize a zero-length vector");
      } else {
        return v;
      }
    }
    double length = Math.sqrt(squareSum);
    for (int i = 0; i < dim; i++) {
      v[i] /= length;
    }
    return v;
  }

  /**
   * Adds the second argument to the first
   *
   * @param u the destination
   * @param v the vector to add to the destination
   */
  public static void add(float[] u, float[] v) {
    for (int i = 0; i < u.length; i++) {
      u[i] += v[i];
    }
  }

  /**
   * Dot product computed over signed bytes.
   *
   * @param a bytes containing a vector
   * @param b bytes containing another vector, of the same dimension
   * @return the value of the dot product of the two vectors
   */
  public static int dotProduct(byte[] a, byte[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("vector dimensions differ: " + a.length + "!=" + b.length);
    }
    return impl.dotProduct(a, b);
  }

  /**
   * Dot product score computed over signed bytes, scaled to be in [0, 1].
   *
   * @param a bytes containing a vector
   * @param b bytes containing another vector, of the same dimension
   * @return the value of the similarity function applied to the two vectors
   */
  public static float dotProductScore(byte[] a, byte[] b) {
    // divide by 2 * 2^14 (maximum absolute value of product of 2 signed bytes) * len
    float denom = (float) (a.length * (1 << 15));
    return 0.5f + dotProduct(a, b) / denom;
  }

  /**
   * Checks if a float vector only has finite components.
   *
   * @param v bytes containing a vector
   * @return the vector for call-chaining
   * @throws IllegalArgumentException if any component of vector is not finite
   */
  public static float[] checkFinite(float[] v) {
    for (int i = 0; i < v.length; i++) {
      if (!Float.isFinite(v[i])) {
        throw new IllegalArgumentException("non-finite value at vector[" + i + "]=" + v[i]);
      }
    }
    return v;
  }

  public static float[] sum(List<float[]> vectors) {
    if (vectors.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be empty");
    }

    return impl.sum(vectors);
  }

  public static float sum(float[] vector) {
    return impl.sum(vector);
  }

  public static void divInPlace(float[] vector, float divisor) {
    impl.divInPlace(vector, divisor);
  }

  public static void addInPlace(float[] v1, float[] v2) {
    impl.addInPlace(v1, v2);
  }

  public static float[] sub(float[] lhs, float[] rhs) {
    return impl.sub(lhs, rhs);
  }
}
