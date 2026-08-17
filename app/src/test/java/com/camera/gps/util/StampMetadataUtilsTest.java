package com.camera.gps.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StampMetadataUtilsTest {

    @Test
    public void missingAndInvalidCoordinatesAreUnavailable() {
        assertTrue(Double.isNaN(StampMetadataUtils.latitudeOrNaN(null)));
        assertTrue(Double.isNaN(StampMetadataUtils.latitudeOrNaN(" ")));
        assertTrue(Double.isNaN(StampMetadataUtils.latitudeOrNaN("not-a-number")));
        assertTrue(Double.isNaN(StampMetadataUtils.latitudeOrNaN("91")));
        assertTrue(Double.isNaN(StampMetadataUtils.longitudeOrNaN("181")));
        assertFalse(StampMetadataUtils.hasCoordinates(Double.NaN, 10d));
    }

    @Test
    public void validCoordinatesAreTrimmedAndParsed() {
        double latitude = StampMetadataUtils.latitudeOrNaN(" 19.0760 ");
        double longitude = StampMetadataUtils.longitudeOrNaN("72.8777");

        assertEquals(19.0760d, latitude, 0d);
        assertEquals(72.8777d, longitude, 0d);
        assertTrue(StampMetadataUtils.hasCoordinates(latitude, longitude));
    }

    @Test
    public void missingStampValuesUseSafeDefaults() {
        assertNull(StampMetadataUtils.optionalText("  "));
        assertEquals("Title", StampMetadataUtils.optionalText(" Title "));
        assertEquals(1, StampMetadataUtils.templateIdOrDefault(null));
        assertEquals(1, StampMetadataUtils.templateIdOrDefault(99));
        assertEquals(3, StampMetadataUtils.mapTypeOrDefault(null, 3));
        assertEquals(2, StampMetadataUtils.ratioOrDefault(99));
        assertEquals(0xFF112233, StampMetadataUtils.colorOrDefault(0, 0xFF112233));
    }
}
