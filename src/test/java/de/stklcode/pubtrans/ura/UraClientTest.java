package de.stklcode.pubtrans.ura;

import org.junit.Test;

/**
 * Created by stefan on 24.12.16.
 */
public class UraClientTest {
    private static final String BASE_ASEAG = "http://ivu.aseag.de";

    @Test
    public void listStopsTest() {
        new UraClient(BASE_ASEAG).listStops();
    }
}
