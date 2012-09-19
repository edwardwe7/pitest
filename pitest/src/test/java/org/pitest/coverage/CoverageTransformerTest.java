package org.pitest.coverage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.boot.InvokeReceiver;
import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.True;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.internal.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;

public class CoverageTransformerTest {
  
  private ClassByteArraySource bytes = new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader());
  
  @Mock
  private InvokeReceiver invokeQueue;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CodeCoverageStore.init(invokeQueue);
  }
  
  @After
  public void tearDown() {
    CodeCoverageStore.resetAllStaticState();
  }
  
  @Test
  public void shouldNotTransformClassesNotMatchingPredicate() throws IllegalClassFormatException {
    CoverageTransformer testee = new CoverageTransformer(False.<String>instance());
    assertNull(testee.transform(null, "anything", null, null, null));
  }
  
  @Test
  public void shouldTransformClasseMatchingPredicate() throws IllegalClassFormatException {
    CoverageTransformer testee = new CoverageTransformer(True.<String>all());
    byte[] bs = bytes.apply(String.class.getName()).value();
    assertFalse(Arrays.equals(bs,testee.transform(null, "anything", null, null, bs)));
  }
  
  @Test
  public void shouldGenerateValidClasses() throws IllegalClassFormatException {
    final StringWriter sw = new StringWriter();
    CoverageTransformer testee = new CoverageTransformer(True.<String>all());
    byte[] bs = testee.transform(null, String.class.getName(), null, null, bytes.apply(String.class.getName()).value());
    CheckClassAdapter.verify(new ClassReader(bs), false, new PrintWriter(sw));
    assertTrue(sw.toString(), sw.toString().length() == 0);
  }
 

}