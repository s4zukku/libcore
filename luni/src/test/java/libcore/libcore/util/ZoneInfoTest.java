/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.libcore.util;

import com.android.i18n.timezone.ZoneInfoData;
import junit.framework.TestCase;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.TimeZone;

import libcore.timezone.testing.ZoneInfoTestHelper;
import libcore.util.ZoneInfo;

/**
 * Tests for {@link ZoneInfo}
 */
public class ZoneInfoTest extends TestCase {

  /**
   * Checks that we can read the serialized form of a {@link ZoneInfo} created in pre-OpenJDK
   * AOSP.
   *
   * <p>One minor difference is that in pre-OpenJDK {@link ZoneInfo#mDstSavings} can be non-zero
   * even if {@link ZoneInfo#mUseDst} was false. That was not visible externally (except through
   * the {@link ZoneInfo#toString()} method) as the {@link ZoneInfo#getDSTSavings()} would check
   * {@link ZoneInfo#mUseDst} and if it was false then would return 0. This checks to make sure
   * that is handled properly. See {@link ZoneInfo#readObject(ObjectInputStream)}.
   */
  public void testReadSerialized() throws Exception {
    ZoneInfo zoneInfoRead;
    try (InputStream is = getClass().getResourceAsStream("ZoneInfoTest_ZoneInfo.golden.ser");
         ObjectInputStream ois = new ObjectInputStream(is)) {
      Object object = ois.readObject();
      assertTrue("Not a ZoneInfo instance", object instanceof ZoneInfo);
      zoneInfoRead = (ZoneInfo) object;
    }

    long[][] transitions = {
        { -5000, 0 },
        { -2000, 1 },
        { -500, 0 },
        { 0, 2 },
    };
    int[][] types = {
        { 3600, 0 },
        { 1800, 1 },
        { 5400, 0 }
    };
    ZoneInfo zoneInfoCreated = createZoneInfo(
            "test", transitions, types, timeFromSeconds(-1));

    assertEquals("Read ZoneInfo does not match created one", zoneInfoCreated, zoneInfoRead);
    assertEquals("useDaylightTime() mismatch",
        zoneInfoCreated.useDaylightTime(), zoneInfoRead.useDaylightTime());
    assertEquals("getDSTSavings() mismatch",
        zoneInfoCreated.getDSTSavings(), zoneInfoRead.getDSTSavings());
  }

  /**
   * Test consistency among DST-related APIs supported by ZoneInfo.
   *
   * This test may use TimeZone APIs only, the implementation comes from ZoneInfo.
   */
  public void testUseDaylightTime_consistency() {
    for (String tzId : TimeZone.getAvailableIDs()) {
      TimeZone tz = TimeZone.getTimeZone(tzId);
      assertEquals("TimeZone API does not report consistently in this zone:" + tzId,
              tz.useDaylightTime(), tz.getDSTSavings() != 0);
    }
  }

  private static Instant timeFromSeconds(long timeInSeconds) {
    return Instant.ofEpochSecond(timeInSeconds);
  }

  private ZoneInfo createZoneInfo(String name, long[][] transitions, int[][] types,
          Instant currentTime) throws Exception {
    ZoneInfoTestHelper.ZicDataBuilder builder =
            new ZoneInfoTestHelper.ZicDataBuilder()
                    .setTransitionsAndTypes(transitions, types);
    ZoneInfoData data = ZoneInfoData.createZoneInfo(name, ByteBuffer.wrap(builder.build()));
    return ZoneInfo.createZoneInfo(data, currentTime.toEpochMilli());
  }
}
