package com.camera.gps;

import com.camera.gps.premium.PremiumManager;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PremiumManagerTest {

    @Test
    public void templateClassificationMatchesProductPlan() {
        assertFalse(PremiumManager.isTemplatePremium(1));
        assertFalse(PremiumManager.isTemplatePremium(2));
        assertTrue(PremiumManager.isTemplatePremium(3));
        assertTrue(PremiumManager.isTemplatePremium(4));
        assertFalse(PremiumManager.isTemplatePremium(5));
        assertFalse(PremiumManager.isTemplatePremium(6));
        assertTrue(PremiumManager.isTemplatePremium(7));
        assertTrue(PremiumManager.isTemplatePremium(8));
        assertTrue(PremiumManager.isTemplatePremium(9));
    }

    @Test
    public void existingFreeFontsRemainFree() {
        assertFalse(PremiumManager.isFontPremium("SF Pro Display.otf"));
        assertFalse(PremiumManager.isFontPremium("Roboto Regular.ttf"));
        assertFalse(PremiumManager.isFontPremium("Cabo Rounded Regular.otf"));
        assertTrue(PremiumManager.isFontPremium("Adobe Caslon Pro.otf"));
    }

    @Test
    public void freeTemplateDateFormatsRemainFree() {
        assertFalse(PremiumManager.isDateTimePremium("dd-MM-yyyy HH:mm:ss a"));
        assertFalse(PremiumManager.isDateTimePremium("MM/dd/yyyy hh:mm aa"));
        assertFalse(PremiumManager.isDateTimePremium("MMM dd, yyyy hh:mm:ss a"));
        assertFalse(PremiumManager.isDateTimePremium("EEEE, MMMM dd, yyyy HH:mm a"));
        assertTrue(PremiumManager.isDateTimePremium("yyyy-MM-dd HH:mm"));
    }
}
