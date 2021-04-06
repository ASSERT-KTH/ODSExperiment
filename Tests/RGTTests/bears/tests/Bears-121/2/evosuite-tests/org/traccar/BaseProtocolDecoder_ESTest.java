/*
 * This file was automatically generated by EvoSuite
 * Tue Dec 10 02:43:08 GMT 2019
 */

package org.traccar;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import java.net.InetAddress;
import java.util.Date;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.mock.java.net.MockInetAddress;
import org.evosuite.runtime.mock.java.net.MockInetSocketAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalChannel;
import org.junit.runner.RunWith;
import org.traccar.model.Position;
import org.traccar.protocol.ArknavProtocol;
import org.traccar.protocol.ArknavProtocolDecoder;
import org.traccar.protocol.Gl100Protocol;
import org.traccar.protocol.Gl100ProtocolDecoder;
import org.traccar.protocol.KenjiProtocol;
import org.traccar.protocol.KenjiProtocolDecoder;
import org.traccar.protocol.T55Protocol;
import org.traccar.protocol.T55ProtocolDecoder;
import org.traccar.protocol.T800xProtocol;
import org.traccar.protocol.T800xProtocolDecoder;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class BaseProtocolDecoder_ESTest extends BaseProtocolDecoder_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      Gl100Protocol gl100Protocol0 = new Gl100Protocol();
      Gl100ProtocolDecoder gl100ProtocolDecoder0 = new Gl100ProtocolDecoder(gl100Protocol0);
      String string0 = gl100ProtocolDecoder0.getProtocolName();
      assertEquals("gl100", string0);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      KenjiProtocol kenjiProtocol0 = new KenjiProtocol();
      KenjiProtocolDecoder kenjiProtocolDecoder0 = new KenjiProtocolDecoder(kenjiProtocol0);
      // Undeclared exception!
      try { 
        kenjiProtocolDecoder0.addUnknownDevice("Hn. mu 9?V");
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.traccar.BaseProtocolDecoder", e);
      }
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      T800xProtocolDecoder t800xProtocolDecoder0 = new T800xProtocolDecoder((T800xProtocol) null);
      Position position0 = new Position();
      t800xProtocolDecoder0.getLastLocation(position0, (Date) null);
      assertFalse(position0.getOutdated());
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      T800xProtocolDecoder t800xProtocolDecoder0 = new T800xProtocolDecoder((T800xProtocol) null);
      DefaultLocalClientChannelFactory defaultLocalClientChannelFactory0 = new DefaultLocalClientChannelFactory();
      DefaultChannelPipeline defaultChannelPipeline0 = new DefaultChannelPipeline();
      LocalChannel localChannel0 = defaultLocalClientChannelFactory0.newChannel(defaultChannelPipeline0);
      MockInetSocketAddress mockInetSocketAddress0 = new MockInetSocketAddress("fuelConsumption", 0);
      String[] stringArray0 = new String[6];
      // Undeclared exception!
      try { 
        t800xProtocolDecoder0.getDeviceSession(localChannel0, mockInetSocketAddress0, stringArray0);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.traccar.BaseProtocolDecoder", e);
      }
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      T55Protocol t55Protocol0 = new T55Protocol();
      T55ProtocolDecoder t55ProtocolDecoder0 = new T55ProtocolDecoder(t55Protocol0);
      InetAddress inetAddress0 = MockInetAddress.anyLocalAddress();
      MockInetSocketAddress mockInetSocketAddress0 = new MockInetSocketAddress(inetAddress0, 1483);
      String[] stringArray0 = new String[9];
      // Undeclared exception!
      try { 
        t55ProtocolDecoder0.getDeviceSession((Channel) null, mockInetSocketAddress0, stringArray0);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.traccar.BaseProtocolDecoder", e);
      }
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      ArknavProtocolDecoder arknavProtocolDecoder0 = new ArknavProtocolDecoder((ArknavProtocol) null);
      // Undeclared exception!
      try { 
        arknavProtocolDecoder0.getProtocolName();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.traccar.BaseProtocolDecoder", e);
      }
  }
}