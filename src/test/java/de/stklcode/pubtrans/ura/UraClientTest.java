package de.stklcode.pubtrans.ura;

import de.stklcode.pubtrans.ura.model.Stop;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ UraClient.class, URL.class })
public class UraClientTest {
    private static final String BASE_ASEAG = "http://ivu.aseag.de";

    @Test
    public void listStopsTest() throws Exception {
        /* Mock the HTTP call */
        URL mockURL = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(mockURL);
        PowerMockito.when(mockURL.openStream()).thenReturn(getClass().getResourceAsStream("instant_stops.txt"));

        /* List stops and verify some values */
        List<Stop> stops = new UraClient("mocked").listStops();
        assertThat(stops, hasSize(10));
        assertThat(stops.get(0).getId(), is("100210"));
        assertThat(stops.get(1).getName(), is("Brockenberg"));
        assertThat(stops.get(2).getState(), is(0));;
        assertThat(stops.get(3).getLatitude(), is(50.7578775));
        assertThat(stops.get(4).getLongitude(), is(6.0708663));
    }
}
