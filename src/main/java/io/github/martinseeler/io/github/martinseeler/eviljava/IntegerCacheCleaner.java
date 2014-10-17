package io.github.martinseeler.io.github.martinseeler.eviljava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * "http://stackoverflow.com/questions/2481862/how-to-limit-setaccessible-to-only-legitimate-uses"
 */
public final class IntegerCacheCleaner {

  public static void main(final String... args) throws Exception {
    // get the class of Integer's nested class IntegerCache
    final Class<?> intCache = Class.forName("java.lang.Integer$IntegerCache");
    // get the Field describing 'cache', which is a private and final field
    final Field cache = intCache.getDeclaredField("cache");
    // disable the private and final modifiers of 'cache'
    disableFinalPrivate(cache);
    // replace the current cache with a new one.
    // 'cache' is static, so no instance is required.
    cache.set(null, getUltimateAnswer());

    // now see what you get for 1 + 1...
    // note that (1 + 1) is an int, and is boxed in an Integer only
    // when passed to format().
    System.out.println(String.format("1 + 1 = %d", 1 + 1));

    for (int i = 0; i < Integer.valueOf(0); i++) {
      System.out.println(String.format("%d", i));
    }

    // node: the hack only changed the particular Field pointed by cache.
    // subsequent calls to intCache.getDeclaredField("cache") will have
    // to be hacked again in order to use them to modify 'cache' again.
  }

  public static void disableFinalPrivate(Field field) throws Exception {
    // first, disable the 'private' modifier (this is not a hack - it's
    // what setAccessible is designed to do)
    field.setAccessible(true);
    // all modifiers are stored in one int, acting as a bitmap
    final int fieldModifiers = field.getModifiers();
    if (Modifier.isFinal(fieldModifiers)) {
      // the field is final, as its 'modifiers' member indicates.
      // we'd like to change that member, but it's private.
      final Field fieldMod = Field.class.getDeclaredField("modifiers");
      // so now we disable the private limitation
      fieldMod.setAccessible(true);
      // and turn the FINAL bit off. Note that the object whose
      // 'modifiers' member is being modified is the original
      // field, which describes Integer$IntegerCache.cache
      fieldMod.setInt(field, fieldModifiers & ~Modifier.FINAL);
      // So now we've used reflection to work around its own limitations,
      // and the final modifier has been disabled...
      // this is sufficient, because when modifying a final field
      // using reflection, the exception is thrown by the reflection
      // code and not by some lower-level JVM stuff.
    }
  }

  static Integer[] getUltimateAnswer() {
    // return an array of 256 integers (as is the original cache),
    // all pointing on Integer(42)
    Integer[] ret = new Integer[256];
    Arrays.fill(ret, 42);
    return ret;
  }
}
