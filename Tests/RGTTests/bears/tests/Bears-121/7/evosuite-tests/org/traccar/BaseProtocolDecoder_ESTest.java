/*
 * This file was automatically generated by EvoSuite
 * Tue Dec 10 03:02:55 GMT 2019
 */

package org.traccar;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.mock.java.net.MockInetAddress;
import org.evosuite.runtime.mock.java.net.MockInetSocketAddress;
import org.evosuite.runtime.mock.java.util.MockDate;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalServerChannel;
import org.junit.runner.RunWith;
import org.traccar.model.Position;
import org.traccar.protocol.DmtProtocol;
import org.traccar.protocol.DmtProtocolDecoder;
import org.traccar.protocol.MegastekProtocol;
import org.traccar.protocol.MegastekProtocolDecoder;
import org.traccar.protocol.MeiligaoProtocol;
import org.traccar.protocol.MeiligaoProtocolDecoder;
import org.traccar.protocol.NvsProtocol;
import org.traccar.protocol.NvsProtocolDecoder;
import org.traccar.protocol.Pt3000Protocol;
import org.traccar.protocol.Pt3000ProtocolDecoder;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class BaseProtocolDecoder_ESTest extends BaseProtocolDecoder_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      MegastekProtocol megastekProtocol0 = new MegastekProtocol();
      MegastekProtocolDecoder megastekProtocolDecoder0 = new MegastekProtocolDecoder(megastekProtocol0);
      MockDate mockDate0 = new MockDate(1584347501, 1584347501, 1584347501, 1584347501, 0, 0);
      // Undeclared exception!
      try { 
        megastekProtocolDecoder0.getLastLocation((Position) null, mockDate0);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.traccar.BaseProtocolDecoder", e);
      }
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      MeiligaoProtocolDecoder meiligaoProtocolDecoder0 = new MeiligaoProtocolDecoder((MeiligaoProtocol) null);
      // Undeclared exception!
      try { 
        meiligaoProtocolDecoder0.addUnknownDevice("t>eU'%]{e?c");
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
      NvsProtocol nvsProtocol0 = new NvsProtocol();
      NvsProtocolDecoder nvsProtocolDecoder0 = new NvsProtocolDecoder(nvsProtocol0);
      Position position0 = new Position();
      MockDate mockDate0 = new MockDate(0L);
      nvsProtocolDecoder0.getLastLocation(position0, mockDate0);
      assertEquals(0.0, position0.getAltitude(), 0.01);
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      NvsProtocol nvsProtocol0 = new NvsProtocol();
      NvsProtocolDecoder nvsProtocolDecoder0 = new NvsProtocolDecoder(nvsProtocol0);
      DefaultLocalServerChannelFactory defaultLocalServerChannelFactory0 = new DefaultLocalServerChannelFactory();
      DefaultChannelPipeline defaultChannelPipeline0 = new DefaultChannelPipeline();
      LocalServerChannel localServerChannel0 = defaultLocalServerChannelFactory0.newChannel(defaultChannelPipeline0);
      InetSocketAddress inetSocketAddress0 = MockInetSocketAddress.createUnresolved("online", 0);
      String[] stringArray0 = new String[9];
      // Undeclared exception!
      try { 
        nvsProtocolDecoder0.getDeviceSession(localServerChannel0, inetSocketAddress0, stringArray0);
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
      DmtProtocol dmtProtocol0 = new DmtProtocol();
      DmtProtocolDecoder dmtProtocolDecoder0 = new DmtProtocolDecoder(dmtProtocol0);
      InetAddress inetAddress0 = MockInetAddress.getByName("online");
      MockInetSocketAddress mockInetSocketAddress0 = new MockInetSocketAddress(inetAddress0, 0);
      String[] stringArray0 = new String[1];
      // Undeclared exception!
      try { 
        dmtProtocolDecoder0.getDeviceSession((Channel) null, mockInetSocketAddress0, stringArray0);
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
      Pt3000Protocol pt3000Protocol0 = new Pt3000Protocol();
      Pt3000ProtocolDecoder pt3000ProtocolDecoder0 = new Pt3000ProtocolDecoder(pt3000Protocol0);
      String string0 = pt3000ProtocolDecoder0.getProtocolName();
      assertEquals("pt3000", string0);
  }
}